package com.aimprosoft.hbase.dao

import akka.actor.{ActorSystem, Scheduler}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.hbase.AsyncConnectionLifecycle
import com.aimprosoft.hbase.Util._
import com.aimprosoft.model.{Affected, Employee}
import com.aimprosoft.util.DepException.DepartmentDoesNotExist
import io.jvm.uuid._
import org.apache.hadoop.hbase.client.Scan

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.FutureConverters.CompletionStageOps

class EmployeeDAO @Inject()(connectionLifecycle: AsyncConnectionLifecycle, system: ActorSystem)(implicit ec: ExecutionContext) extends TEmployeeDAO {

  private[this] implicit val scheduler: Scheduler = system.scheduler

  import connectionLifecycle.usingTables

  /**
   * Returns employees by their department.
   *
   * This code uses ''employee_by_department_id'' to fetch employees efficiently.
   *
   * @param id UUID of the department.
   * @return Employees sorted by surname and name.
   */
  override def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]] = usingTables { case (_, _, _, employeesByDep) =>

    // This code is semi blocking. See 'AsyncTableResultScanner'.
    val workers = for {
      employee <- employeesByDep.getScanner(createPrefixScan(id.byteArray)).asScala
    } yield buildEmployeeFromWideKey(employee.getRow)

    Future.successful(workers.toSeq)
  }

  private[dao] def createEmployeeOrReapply(employees: AsyncTable, employeeByDep: AsyncTable, employee: Employee[UUID]): Future[Option[UUID]] = {

    val currentTime = System.currentTimeMillis()

    val putEmployee = createPut(employee, currentTime)

    def createEmployee = employees.put(putEmployee).asScala

    val putEmployeeWideKey = createEmptyPut(createEmployeeWideKey(employee), employeeBytes, currentTime)

    def createEmployeeByDep = employeeByDep.put(putEmployeeWideKey).asScala

    retries(createEmployee _).zip(retries(createEmployeeByDep _)).map(_ => employee.id)
  }

  private[dao] def createEmployeeIfDepExists(employees: AsyncTable,
                                employeeByDep: AsyncTable,
                                departments: AsyncTable,
                                employee: Employee[UUID]): Future[Option[UUID]] = {

    val creation = for {
      depPresent <- departments.exists(createGet(employee.departmentId.byteArray)).asScala
    } yield if (depPresent) createEmployeeOrReapply(employees, employeeByDep, employee)
    else Future.failed(DepartmentDoesNotExist(employee.departmentId))

    creation.flatten
  }

  /**
   * Creates an employee and assigns a UUID to him.
   *
   * The department the employee is a part of must exist.
   *
   * The employee must not contain the UUID because it will be created for him.
   *
   * This method may reapply the insertion.
   * This way it will be eventually consistent unless somebody performs an action while recovery is in progress.
   * This is the application level recovery.
   * The app must be alive, or recovery or any action will fail.
   *
   * @param employee The employee
   * @return The employee's UUID
   */
  override def create(employee: Employee[UUID]): Future[Option[UUID]] = usingTables { case (departments, _, employees, employeeByDep) =>

    employee.id.fold {

      createEmployeeIfDepExists(employees, employeeByDep, departments, employee.copy(id = UUID.random.some))

    }(_ => none[UUID].pure[Future])

  }

  override def update(employee: Employee[UUID]): Future[Option[Affected]] = ???

  override def readAll(): Future[Seq[Employee[UUID]]] = usingTables { case (_, _, employees, _) =>

    for {
      workers <- employees.scanAll(new Scan()).asScala
    } yield for {
      emp <- workers.asScala.toSeq
    } yield resultToEmployee(emp)

  }

  override def readById(id: UUID): Future[Option[Employee[UUID]]] = ???

  override def deleteById(id: UUID): Future[Affected] = ???

}
