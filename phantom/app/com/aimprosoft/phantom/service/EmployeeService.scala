package com.aimprosoft.phantom.service

import cats.data.OptionT
import cats.implicits.{catsSyntaxApplicativeId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, CompleteEmployee, Department, Employee}
import com.aimprosoft.phantom.dao.TEmployeeDAO
import com.aimprosoft.service.TEmployeeService
import com.google.inject.Inject
import com.outworkers.phantom.dsl.UUID

import scala.concurrent.{ExecutionContext, Future}

case class EmployeeService @Inject()(employees: TEmployeeDAO, departments: BasicDAO[Future, UUID, Department[UUID]])(implicit ec: ExecutionContext) extends TEmployeeService[Future, UUID] {

  def getCompleteEmployeeById(id: UUID): Future[Option[CompleteEmployee[UUID]]] = {

    val worker = employees.readById(id)

    val dept = for {
      employee <- worker
    } yield employee.fold(none[Department[UUID]].pure[Future])(worker => departments.readById(worker.departmentId))

    val division = dept.flatten

    val complete = for {
      employee <- OptionT(worker)
      dept <- OptionT(division)
    } yield CompleteEmployee[UUID](employee.id, dept, employee.name, employee.surname)

    complete.value
  }

  def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]] = employees.getEmployeesByDepartmentId(id)

  def create(value: Employee[UUID]): Future[Option[UUID]] = employees.create(value)

  def update(value: Employee[UUID]): Future[Option[Affected]] = employees.update(value)

  def readAll(): Future[Seq[Employee[UUID]]] = employees.readAll()

  def readById(id: UUID): Future[Option[Employee[UUID]]] = employees.readById(id)

  def deleteById(id: UUID): Future[Affected] = employees.deleteById(id)

}
