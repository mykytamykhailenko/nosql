package com.aimprosoft.phantom.dao

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.model.{Affected, Employee}
import com.aimprosoft.phantom.database.DepartmentDatabase
import com.aimprosoft.phantom.util.Util._
import com.outworkers.phantom.dsl._

import java.util.UUID.{randomUUID => uuid}
import javax.inject.Inject
import scala.concurrent.Future

class EmployeeDAO @Inject()(val database: DepartmentDatabase) extends TEmployeeDAO with DatabaseProvider[DepartmentDatabase] {

  /**
   * Fetches all employees from the same department.
   *
   * It uses ''employee_by_department_id'' for quick access.
   *
   * @param id Department UUID
   * @return Employees
   */
  def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]] =
    database
      .employeeByDepartmentId
      .select
      .where(_.department_id eqs ?)
      .prepare()
      .bind(id)
      .fetch()

  private def batchCreate(employee: Employee[UUID]): Future[Option[UUID]] = {

    val id = uuid()

    Batch.logged
      .add(database.employees.prepInsert.bind((id, employee.departmentId, employee.name, employee.surname)))
      .add(database.employeeByDepartmentId.prepInsert.bind((id, employee.departmentId, employee.name, employee.surname)))
      .future()
      .map(_ => id.some)
  }

  /**
   * Creates an employee and creates UUID for it.
   *
   * This method ensures that the employee will be written to ''employee'' and ''employee_by_department_id''.
   *
   * This method checks whether the employee belongs to a department and if not, fails the Future.
   *
   * If the employee contains UUID, it won't store it, and return None.
   *
   * @param employee An employee
   * @return His identifier
   */
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

  // It is impossible to update clustering keys.
  // That is why you need delete them at first, and then insert them back.
  private def batchUpdate(employee: Employee[UUID]) = {

    val time = currentMicro()

    val id = employee.id.get

    // You must enforce ordering for those operations.
    Batch
      .logged
      .add(database.employees.deleteAt(time).bind(id))
      .add(database.employeeByDepartmentId.deleteAt(time).bind(employee.departmentId))
      .add(database.employees.insertAt(time + aMicrosecond).bind(id, employee.departmentId, employee.name, employee.surname))
      .add(database.employeeByDepartmentId.insertAt(time + aMicrosecond).bind((employee.departmentId, id, employee.name, employee.surname)))
      .future()
      .map(_ => one.some)
  }

  /**
   * Updates an employee.
   *
   * You must satisfy following conditions to update the employee.
   *  1. You should provide UUID belonging to a real employee
   *  1. The employee must be a part of a department, which exists
   *
   * If you don't provide a UUID, you will get None.
   *
   * @param employee Employee
   * @return Affected employees
   */
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


  /**
   * Deletes an employee.
   *
   * Makes sure the employee is deleted from ''employee'' and ''employee_by_department_id'' simultaneously.
   *
   * @param id UUID
   * @return Number of affected employees
   */
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
    } yield division.fold(unaffected.pure[Future])(dropBatch(id, _))

    deletion.flatten
  }


}
