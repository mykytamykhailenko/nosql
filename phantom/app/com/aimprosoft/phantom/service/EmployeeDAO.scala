package com.aimprosoft.phantom.service

import cats.data.OptionT
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, CompleteEmployee, Department, Employee}
import com.aimprosoft.phantom.database.DepartmentDatabase
import com.aimprosoft.phantom.util.Util.{aMicrosecond, currentMicro, departmentDoesNotExist, noAffected, one}
import com.outworkers.phantom.dsl._

import java.util.UUID.{randomUUID => uuid}
import javax.inject.Inject
import scala.concurrent.Future

class EmployeeDAO @Inject()(val database: DepartmentDatabase) extends BasicDAO[Future, UUID, Employee[UUID]] with DatabaseProvider[DepartmentDatabase] {

  def getEmployeeById(id: UUID): Future[Option[CompleteEmployee[UUID]]] = {

    val worker =
      database
        .employees
        .select
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val dept = for {
      employee <- worker
    } yield employee.fold(none[Department[UUID]].pure[Future]) { worker =>
      database
        .departments
        .select
        .where(_.id eqs ?)
        .prepare()
        .bind(worker.departmentId)
        .one()
    }

    val division = dept.flatten

    val complete = for {
      employee <- OptionT(worker)
      dept <- OptionT(division)
    } yield CompleteEmployee[UUID](employee.id, dept, employee.name, employee.surname)

    complete.value
  }

  private def batchCreate(employee: Employee[UUID]): Future[Option[UUID]] = {

    val id = uuid()

    Batch.logged
      .add(database.employees.prepInsert.bind((id, employee.departmentId, employee.name, employee.surname)))
      .add(database.employeeByDepartmentId.prepInsert.bind((id, employee.departmentId, employee.name, employee.surname)))
      .future()
      .map(_ => id.some)
  }

  def create(employee: Employee[UUID]): Future[Option[UUID]] = employee.id.fold {

    val did = employee.departmentId

    val dept =
      database
        .departments
        .select(_.id)
        .where(_.id eqs ?)
        .prepare()
        .bind(did)
        .one()

    val creation = for {
      division <- dept
    } yield division.fold(Future.failed[Option[UUID]](departmentDoesNotExist(did)))(_ => batchCreate(employee))

    creation.flatten

  }(_ => none[UUID].pure[Future])

  private def batchUpdate(employee: Employee[UUID]) = {

    val time = currentMicro()

    val id = employee.id.get

    Batch
      .logged
      .add(database.employees.deleteAt(time).bind(id))
      .add(database.employeeByDepartmentId.deleteAt(time).bind(employee.departmentId))
      .add(database.employees.insertAt(time + aMicrosecond).bind(id, employee.departmentId, employee.name, employee.surname))
      .add(database.employeeByDepartmentId.insertAt(time + aMicrosecond).bind((employee.departmentId, id, employee.name, employee.surname)))
      .future()
      .map(_ => one.some)
  }

  def update(employee: Employee[UUID]): Future[Option[Affected]] = employee.id.fold(none[Affected].pure[Future]) { id =>

    val dept =
      database
        .departments
        .select(_.id)
        .where(_.id eqs ?)
        .prepare()
        .bind(employee.departmentId)
        .one()

    val worker =
      database
        .employees
        .select(_.id)
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val batch = for {
      division <- dept
      _ <- worker
    } yield division.fold(Future.failed[Option[Affected]](departmentDoesNotExist(employee.departmentId)))(_ => batchUpdate(employee))

    batch.flatten
  }

  def readAll(): Future[Seq[Employee[UUID]]] = database.employees.select.fetch()

  def readById(id: UUID): Future[Option[Employee[UUID]]] = database.employees.select.where(_.id eqs ?).prepare().bind(id).one()

  private def dropBatch(id: UUID, departmentId: UUID): Future[Affected] =
    Batch
      .logged
      .add(database.employees.delete().where(_.id eqs ?).prepare().bind(id))
      .add(database.employeeByDepartmentId.delete().where(_.department_id eqs ?).prepare().bind(departmentId))
      .future()
      .map(_ => one)


  // Deletes an employee from ''employee'' and ''employee_by_department_id'' in batch.
  def deleteById(id: UUID): Future[Affected] = {

    val dept =
      database
        .employees
        .select(_.department_id)
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val deletion = for {
      division <- dept
    } yield division.fold(noAffected.pure[Future])(dropBatch(id, _))

    deletion.flatten
  }


}
