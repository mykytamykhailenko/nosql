package com.aimprosoft.mongo.dao

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.model.{Affected, Employee}
import com.aimprosoft.mongo.connection.Connection
import com.aimprosoft.mongo.converters.Converters._
import com.aimprosoft.util.DepartmentExceptions.DepartmentDoesNotExist
import io.jvm.uuid.UUID
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmployeeDAO @Inject()(conn: Connection)(implicit ec: ExecutionContext) extends TEmployeeDAO {

  import conn._

  override def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]] =
    for {
      workers <- employees.find(equal(departmentId, toBsonBinary(id))).collect().toFuture()
    } yield workers.flatMap(documentToEmployee)


  private[dao] def createEmployee(employeeWithoutId: Employee[UUID]) =
    employees
      .insertOne(employeeToDocument(employeeWithoutId.copy(id = Some(UUID.random))))
      .map(_.getInsertedId.asBinary.asUuid.some)
      .head()

  private[dao] def findDepartment(depId: UUID) =
    departments
      .find(equal(id, toBsonBinary(depId)))
      .first()
      .headOption()

  override def create(employee: Employee[UUID]): Future[Option[UUID]] = employee.id.fold {

    val creation = for {
      department <- findDepartment(employee.departmentId)
    } yield department.fold(Future.failed[Option[UUID]](DepartmentDoesNotExist(employee.departmentId)))(_ => createEmployee(employee))

    creation.flatten

  }(_ => none[UUID].pure[Future])

  private[dao] def updateEmployee(employee: Employee[UUID]) =
    employees
      .replaceOne(equal(id, toBsonBinary(employee.id.get)), employeeToDocument(employee))
      .toFuture()
      .map(_.getModifiedCount.toInt.some)

  override def update(employee: Employee[UUID]): Future[Option[Affected]] = employee.id.fold(none[Affected].pure[Future]) { _ =>

    val updated = for {
      department <- findDepartment(employee.departmentId)
    } yield department.fold(Future.failed[Option[Affected]](DepartmentDoesNotExist(employee.departmentId)))(_ => updateEmployee(employee))

    updated.flatten
  }

  override def readAll(): Future[Seq[Employee[UUID]]] =
    employees.find().collect().toFuture().map(_.flatMap(documentToEmployee))

  override def readById(employeeId: UUID): Future[Option[Employee[UUID]]] = for {
    worker <- employees.find(equal(id, toBsonBinary(employeeId))).first().headOption()
  } yield worker.flatMap(documentToEmployee)

  override def deleteById(employeeId: UUID): Future[Affected] =
    employees.deleteOne(equal(id, toBsonBinary(employeeId))).toFuture().map(_.getDeletedCount.toInt)


}
