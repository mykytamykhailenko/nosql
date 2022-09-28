package com.aimprosoft.phantom.dao

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Department}
import com.aimprosoft.phantom.database.DepartmentDatabase
import com.aimprosoft.phantom.util.Util._
import com.aimprosoft.util.DepartmentExceptions._
import com.google.inject.Inject
import com.outworkers.phantom.dsl._

import java.util.UUID.{randomUUID => uuid}
import scala.concurrent.Future

class DepartmentDAO @Inject()(val database: DepartmentDatabase) extends BasicDAO[Future, UUID, Department[UUID]] with DatabaseProvider[DepartmentDatabase] {

  private def insert(dept: Department[UUID]) = {
    val id = uuid()

    Batch
      .logged
      .add(database.departments.prepInsert.bind((id, dept.name, dept.description)))
      .add(database.departmentNames.prepInsert.bind(dept.name))
      .future()
      .map(_ => id.some)
  }

  /**
   * Creates a department with unique name and returns its UUID.
   *
   * Following conditions should be met for successful insert:
   *  1. The department must not have a name, which is already taken.
   *  1. The department must not have UUID.
   *
   * If you provide a department with a duplicate name, it causes the method to fail with an error.
   * If you provide an identifier, it will ignore it and won't create the department (you will receive None).
   *
   * This method keeps ''department'' and ''department_name'' in sync.
   *
   * @param dept A department
   * @return Its UUID
   */
  def create(dept: Department[UUID]): Future[Option[UUID]] = dept.id.fold {

    val name =
      database
        .departmentNames
        .select
        .where(_.name eqs ?)
        .prepare()
        .bind(dept.name)
        .one()

    val creation = for {
      naming <- name
    } yield naming.fold(insert(dept))(name => Future.failed(DepartmentNameIsAlreadyTaken(name)))

    creation.flatten

  }(_ => none[UUID].pure[Future])

  private def batchUpdate(previous: Department[UUID], current: Department[UUID]): Future[Option[Affected]] = {

    val time = currentMicro()

    Batch
      .logged
      .add(db.departments.deleteAt(time).bind(current.id.get))
      .add(db.departmentNames.deleteAt(time).bind(previous.name))
      .add(db.departments.insertAt(time + aMicrosecond).bind((current.id.get, current.name, current.description)))
      .add(db.departmentNames.insertAt(time + aMicrosecond).bind(current.name))
      .future()
      .map(_ => one.some)
  }

  /**
   * Updates the department if the name is not taken.
   *
   * There are two scenarios:
   *  1. The name is changed and it has never been used before (the ''department_name'' doesn't contain this name)
   *  1. The name stays the same (the ''department_name'' contains this name, but it is the same as the previous one)
   *  1. The name is changed, but it is occupied (the ''department_name'' contains this name, but it doesn't match the previous one)
   *
   * In first two cases it is safe to batch update.
   *
   * The third case results in an error, which is captured in the failed future.
   *
   * @param previousDept Previous department
   * @param currentDept  Current department
   * @return Updated records.
   */
  private def updateIfNotOccupied(previousDept: Department[UUID], currentDept: Department[UUID]): Future[Option[Affected]] = {

    val occupiedName =
      database
        .departmentNames
        .select
        .where(_.name eqs ?)
        .prepare()
        .bind(currentDept.name)
        .one()

    val update = for {
      name <- occupiedName
    } yield name.fold(batchUpdate(previousDept, currentDept)) { name =>
      if (previousDept.name == name) batchUpdate(previousDept, currentDept)
      else Future.failed(DepartmentNameIsAlreadyTaken(name))
    }

    update.flatten
  }

  /**
   * Updates a department.
   *
   * The name must not be taken when updating the department, otherwise, it causes a failure.
   *
   * The department must specify its UUID, otherwise, nothing will be updated.
   *
   * @param currentDept Department
   * @return Affected departments
   */
  def update(currentDept: Department[UUID]): Future[Option[Affected]] = currentDept.id.fold(none[Affected].pure[Future]) { id =>

    val previousDept =
      database
        .departments
        .select
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val up = for {
      division <- previousDept
    } yield division.fold(unaffected.some.pure[Future])(previousDept => updateIfNotOccupied(previousDept, currentDept))

    up.flatten
  }

  def readAll(): Future[Seq[Department[UUID]]] = database.departments.select.fetch()

  def readById(id: UUID): Future[Option[Department[UUID]]] = database.departments.select.where(_.id eqs ?).prepare().bind(id).one()

  private def delete(dept: Option[Department[UUID]]): Future[Affected] =
    dept.fold(unaffected.pure[Future]) { division =>
      Batch
        .logged
        .add(database.departments.delete().where(_.id eqs ?).prepare().bind(division.id.get))
        .add(database.departmentNames.delete().where(_.name eqs ?).prepare().bind(division.name))
        .future()
        .map(_ => one)
    }

  /**
   * Delete a department by its UUID.
   *
   * This method will throw an error if the department has any employees.
   *
   * This method deletes the department from ''department'' and ''department_name'' in a batch.
   *
   * @param id UUID
   * @return Affected department
   */
  def deleteById(id: UUID): Future[Affected] = {

    val dept =
      database
        .departments
        .select
        .where(_.id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val worker =
      database
        .employeeByDepartmentId
        .select(_.id)
        .where(_.department_id eqs ?)
        .prepare()
        .bind(id)
        .one()

    val deletion = for {
      employee <- worker
      division <- dept
    } yield employee.fold(delete(division))(_ => Future.failed[Affected](DepartmentIsNotEmpty(id)))

    deletion.flatten
  }

  // For testing only.
  private[dao] def truncate() = for {
    _ <- database.departments.truncate().future()
    _ <- database.departmentNames.truncate().future()
  } yield ()

}