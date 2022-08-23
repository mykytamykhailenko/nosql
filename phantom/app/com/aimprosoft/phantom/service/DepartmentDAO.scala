package com.aimprosoft.phantom.service

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Department, Employee}
import com.aimprosoft.phantom.database.DepartmentDatabase
import com.aimprosoft.phantom.util.Util.{aMicrosecond, currentMicro, departmentIsNotEmpty, noAffected, one}
import com.google.inject.Inject
import com.outworkers.phantom.dsl._

import java.util.UUID.{randomUUID => uuid}
import scala.concurrent.Future

class DepartmentDAO @Inject()(val database: DepartmentDatabase) extends BasicDAO[Future, UUID, Department[UUID]] with DatabaseProvider[DepartmentDatabase] {

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

    database
      .departments
      .prepInsert
      .bind((id, dept.name, dept.description))
      .future()
      .map(_ => id.some)

  }(_ => none[UUID].pure[Future])

  // Because the department name is a part of the primary key, I cannot update it.
  // So I have to delete and insert the record in the batch.
  private def batchUpdate(department: Department[UUID]): Future[Option[Affected]] = {

    // I have to reserve to side-effects here to keep order consistent within the batch.
    val time = currentMicro()

    Batch
      .logged
      .add(db.departments.deleteAt(time).bind(department.id.get))
      .add(db.departments.insertAt(time + aMicrosecond).bind((department.id.get, department.name, department.description)))
      .future()
      .map(_ => one.some)
  }

  def update(dept: Department[UUID]): Future[Option[Affected]] = dept.id.fold(none[Affected].pure[Future]) { id =>

    val division =
      database
        .departments
        .select
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val up = for {
      div <- division
    } yield div.fold(noAffected.some.pure[Future])(_ => batchUpdate(dept))

    up.flatten
  }

  def readAll(): Future[Seq[Department[UUID]]] = database.departments.select.fetch()

  def readById(id: UUID): Future[Option[Department[UUID]]] = database.departments.select.where(_.id eqs ?).prepare().bind(id).one()

  private def deleteDepartmentIfEmpty(dept: Option[UUID], worker: Option[UUID]): Future[Affected] = dept.fold(noAffected.pure[Future]) { id =>

    val dropDivision =
      database
        .departments
        .delete()
        .where(_.id eqs ?)
        .prepare()
        .bind(id)

    // I could have used Either, but it would further complicate stuff.
    worker.fold(dropDivision.future().map(_ => one))(_ => Future.failed(departmentIsNotEmpty(id)))
  }

  def deleteById(id: UUID): Future[Affected] = {

    val worker =
      database
        .employeeByDepartmentId
        .select(_.id)
        .where(_.department_id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val dept =
      database
        .departments
        .select(_.id)
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val deletion = for {
      division <- dept
      employee <- worker
    } yield deleteDepartmentIfEmpty(division, employee)

    deletion.flatten
  }

}