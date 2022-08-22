package com.aimprosoft.phantom.service

import cats.data.OptionT
import cats.implicits.{catsSyntaxApplicativeId, none}
import com.aimprosoft.model.{Affected, CompleteEmployee, Department, Employee}
import com.aimprosoft.phantom.database.DepartmentDatabase
import com.aimprosoft.phantom.util.Util.{nothing, one}
import com.aimprosoft.service.TEmployeeService
import com.outworkers.phantom.dsl._

import javax.inject.Inject
import scala.concurrent.Future

class EmployeeService @Inject()(val database: DepartmentDatabase) extends TEmployeeService[Future, UUID] with DatabaseProvider[DepartmentDatabase] {

  def getEmployeeById(id: UUID): Future[Option[CompleteEmployee[UUID]]] = {

    val worker =
      database
        .employee
        .select
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val dept = for {
      employee <- worker
    } yield employee.fold(none[Department[UUID]].pure[Future]) { worker =>
      database
        .department
        .select
        .where(_.id eqs ?)
        .prepare
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

  def create(value: Employee[UUID]): Future[Option[UUID]] = ???

  def update(value: Employee[UUID]): Future[Option[Affected]] = ???

  def readAll(): Future[Seq[Employee[UUID]]] = database.employee.select.fetch()

  def readById(id: UUID): Future[Option[Employee[UUID]]] = database.employee.select.where(_.id eqs ?).prepare().bind(id).one()

  def deleteById(id: UUID): Future[Affected] = {

    val dept =
      database
        .employee
        .select(_.department_id)
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val deletion = for {
      division <- dept
    } yield division.fold(nothing.pure[Future]) { dept =>

      val dropWorker =
        database
          .employee
          .delete
          .where(_.id eqs ?)
          .prepare()
          .bind(id)

      val dropDept =
        database
          .employeeByDepartmentId
          .delete
          .where(_.department_id eqs ?)
          .prepare()
          .bind(dept)

      Batch.logged.add(dropWorker).add(dropDept).future().map(_ => one)
    }

    deletion.flatten
  }


}
