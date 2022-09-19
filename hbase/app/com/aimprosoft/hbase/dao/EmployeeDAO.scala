package com.aimprosoft.hbase.dao

import akka.actor.{ActorSystem, Scheduler}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.hbase.AsyncConnectionLifecycle
import com.aimprosoft.hbase.Util._
import com.aimprosoft.model.{Affected, Employee}
import com.aimprosoft.util.DepException.DepartmentDoesNotExist
import io.jvm.uuid._
import org.apache.hadoop.hbase.client.Scan

import java.lang.System.currentTimeMillis
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

  private[dao] def createEmployeeAndReapply(employees: AsyncTable,
                                            employeeByDep: AsyncTable,
                                            employee: Employee[UUID],
                                            currentTime: Long = currentTimeMillis()): Future[Option[UUID]] = {

    val putEmployee = createPut(employee, currentTime)

    val putEmployeeWideKey = createEmptyPut(createEmployeeWideKey(employee), employeeBytes, currentTime)

    val createEmployee = () => employees.put(putEmployee).asScala

    val createEmployeeByDep = () => employeeByDep.put(putEmployeeWideKey).asScala

    retries(createEmployee).zip(retries(createEmployeeByDep)).map(_ => employee.id)
  }

  private[dao] def createEmployeeIfDivisionExists(employees: AsyncTable,
                                                  employeeByDep: AsyncTable,
                                                  departments: AsyncTable,
                                                  employee: Employee[UUID]): Future[Option[UUID]] = {

    val creation = for {
      depPresent <- departments.exists(createGet(employee.departmentId.byteArray)).asScala
    } yield if (depPresent) createEmployeeAndReapply(employees, employeeByDep, employee)
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

      createEmployeeIfDivisionExists(employees, employeeByDep, departments, employee.copy(id = UUID.random.some))

    }(_ => none[UUID].pure[Future])

  }

  /**
   * Updates an employee by:
   *  1. Dropping old employee from ''employee_by_department_id''
   *  1. Inserting new employee in ''employee_by_department_id''
   *  1. Updating the employee in ''employee''
   *
   * This method won't do anything if the contents of the employee stay the same.
   *
   * Can reapply changes for eventual consistency.
   *
   * @param employees The ''employee'' table
   * @param employeesByDep The ''employee_by_department_id'' table
   * @param oldEmployee Old employee
   * @param employee New employee
   * @param currentTime Current time
   * @return Affected employees
   */
  def updateEmployeeOrReapply(employees: AsyncTable,
                              employeesByDep: AsyncTable,
                              oldEmployee: Employee[UUID],
                              employee: Employee[UUID],
                              currentTime: Long = currentTimeMillis()): Future[Option[Affected]] = {

    if (employee == oldEmployee) one.some.pure[Future]
    else {

      val dropOldEmployeeWideKey = createDelete(createEmployeeWideKey(oldEmployee), currentTime)

      val putEmployeeWideKey = createEmptyPut(createEmployeeWideKey(employee), employeeBytes, currentTime)

      val putEmployee = createPut(employee, currentTime)

      val deleteOldEmployeeByDep = () => employeesByDep.delete(dropOldEmployeeWideKey).asScala

      val createEmployeeByDep = () => employeesByDep.put(putEmployeeWideKey).asScala

      val createEmployee = () => employees.put(putEmployee).asScala

      Future.sequence(Seq(
        retries(deleteOldEmployeeByDep),
        retries(createEmployeeByDep),
        retries(createEmployee)))
        .map(_ => one.some)
    }
  }

  def updateIfDivisionExists(employees: AsyncTable,
                             employeesByDep: AsyncTable,
                             oldEmployee: Employee[UUID],
                             employee: Employee[UUID],
                             departmentExists: Boolean): Future[Option[Affected]] =

    if (departmentExists) updateEmployeeOrReapply(employees, employeesByDep, oldEmployee, employee)
    else Future.failed(DepartmentDoesNotExist(employee.departmentId))

  /**
   * Updates an employee.
   *
   * When updating the employee, you must ensure its department id belongs to an existing department.
   * Otherwise, this method will fail.
   *
   * This method will not update anything if the employee does not exist or its content stays the same.
   *
   * Can reapply changes to stay consistent.
   *
   * @param employee The employee
   * @return Affected employees
   */
  override def update(employee: Employee[UUID]): Future[Option[Affected]] = usingTables { case (departments, _, employees, employeesByDep) =>

    val division = departments.exists(createGet(employee.departmentId.byteArray)).asScala

    val worker = employees.get(createGet(employee.id.get.byteArray)).asScala

    val updated = for {
      dep <- division
      workman <- worker

      workmanOpt = getEmployeeOpt(workman)

    } yield workmanOpt.fold(nothingUpdated) { oldEmployee =>
      updateIfDivisionExists(employees, employeesByDep, oldEmployee, employee, dep)
    }

    updated.flatten
  }

  override def readAll(): Future[Seq[Employee[UUID]]] = usingTables { case (_, _, employees, _) =>

    for {
      staff <- employees.scanAll(new Scan()).asScala
    } yield for {
      worker <- staff.asScala.toSeq
    } yield getEmployee(worker)

  }

  override def readById(id: UUID): Future[Option[Employee[UUID]]] = usingTables { case (_, _, employees, _) =>

    for {
      worker <- employees.get(createGet(id.byteArray)).asScala
    } yield getEmployeeOpt(worker)

  }

  private[dao] def deleteEmployeeOrReapply(employees: AsyncTable,
                                           employeesByDep: AsyncTable,
                                           employee: Employee[UUID],
                                           currentTime: Long = currentTimeMillis()): Future[Affected] = {

    val dropEmployee = createDelete(employee.id.get.byteArray, currentTime)

    val dropEmployeesByDep = createDelete(createEmployeeWideKey(employee), currentTime)

    val deleteEmployee = () => employees.delete(dropEmployee).asScala

    val deleteEmployeeByDep = () => employeesByDep.delete(dropEmployeesByDep).asScala

    retries(deleteEmployee).zip(retries(deleteEmployeeByDep)).map(_ => one)
  }

  /**
   * Deletes an employee by his UUID.
   *
   * It can reapply the deletion, but it will always keep the same timestamp.
   *
   * Removes the employee from both ''employee'' and ''employee_by_department_id''.
   *
   * @param id The employee's UUID
   * @return Affected employees
   */
  override def deleteById(id: UUID): Future[Affected] = usingTables { case (_, _, employees, employeesByDep) =>

    val deletion = for {
      worker <- readById(id)
    } yield worker.fold(nothingDeleted)(emp => deleteEmployeeOrReapply(employees, employeesByDep, emp))

    deletion.flatten
  }

}
