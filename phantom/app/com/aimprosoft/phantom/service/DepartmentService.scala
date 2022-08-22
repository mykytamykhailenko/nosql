package com.aimprosoft.phantom.service

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.model.{Affected, Department, Employee}
import com.aimprosoft.phantom.database.DepartmentDatabase
import com.aimprosoft.phantom.util.Util.{deptIsNotEmptyError, nothing, one}
import com.aimprosoft.service.TDepartmentService
import com.google.inject.Inject
import com.outworkers.phantom.dsl._

import java.util.UUID.{randomUUID => uuid}
import scala.concurrent.Future

class DepartmentService @Inject()(val database: DepartmentDatabase) extends TDepartmentService[Future, UUID] with DatabaseProvider[DepartmentDatabase] {

  def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]] =
    database
      .employeeByDepartmentId
      .select
      .where(_.department_id eqs ?)
      .prepare()
      .bind(id)
      .fetch()

  def create(dept: Department[UUID]): Future[Option[UUID]] = dept.id.fold {

    val id = uuid()

    val insertion =
      database
        .department
        .insert
        .p_value(_.id, ?)
        .p_value(_.name, ?)
        .p_value(_.description, ?)
        .prepare
        .bind((id, dept.name, dept.description))

    insertion.future().map(_ => id.some)

  }(_ => none[UUID].pure[Future])

  def update(dept: Department[UUID]): Future[Option[Affected]] = dept.id.fold(none[Affected].pure[Future]) { id =>

    val update =
      database
        .department
        .update
        .where(_.id eqs ?)
        .modify(_.name setTo ?)
        .and(_.description setTo ?)
        .ifExists
        .prepare
        .bind((dept.name, dept.description, id))

    update.future().map(_ => one.some)
  }

  def readAll(): Future[Seq[Department[UUID]]] = database.department.select.fetch()

  def readById(id: UUID): Future[Option[Department[UUID]]] = database.department.select.where(_.id eqs ?).prepare().bind(id).one()

  private[this] def deleteDeptIfEmpty(dept: Option[UUID], worker: Option[UUID]): Future[Affected] = dept.fold(nothing.pure[Future]) { id =>

    val dropDivision =
      database
        .department
        .delete
        .where(_.id eqs ?)
        .prepare
        .bind(id)

    // I could have used Either, but it would further complicate stuff.
    worker.fold(dropDivision.future().map(_ => one))(_ => deptIsNotEmptyError(id))
  }

  def deleteById(id: UUID): Future[Affected] = {

    val worker =
      database
        .employeeByDepartmentId
        .select(_.id)
        .where(_.department_id eqs ?)
        .prepare
        .bind(id)
        .one()

    val dept =
      database
        .department
        .select(_.id)
        .where(_.id eqs ?)
        .prepare
        .bind(id)
        .one()

    val deletion = for {
      division <- dept
      employee <- worker
    } yield deleteDeptIfEmpty(division, employee)

    deletion.flatten
  }

}