package com.aimprosoft.service

import cats.Monad
import cats.data.OptionT
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model._
import com.google.inject.Inject

// Alternative solution would be to create another language with this specific operation.
case class EmployeeService[F[_] : Monad, K] @Inject()(departmentLang: BasicDAO[F, K, Department[K]],
                                                      employeeLang: BasicDAO[F, K, Employee[K]]) extends TEmployeeService[F, K] {

  def create(value: Employee[K]): F[Option[K]] = employeeLang.create(value)

  def update(value: Employee[K]): F[Option[Affected]] = employeeLang.update(value)

  def readAll(): F[Seq[Employee[K]]] = employeeLang.readAll()

  def readById(id: K): F[Option[Employee[K]]] = employeeLang.readById(id)

  def deleteById(id: K): F[Affected] = employeeLang.deleteById(id)

  def getCompleteEmployeeById(id: K): F[Option[CompleteEmployee[K]]] = {

    val completeEmployee = for {
      Employee(employeeId, departmentId, name, surname) <- OptionT(employeeLang.readById(id))
      department <- OptionT(departmentLang.readById(departmentId))
    } yield CompleteEmployee(employeeId, department, name, surname)

    completeEmployee.value
  }

  import cats.syntax.functor._

  // It is a highly inefficient implementation created for the showcase only.
  def getEmployeesByDepartmentId(id: K): F[Seq[Employee[K]]] =
    for {
      workers <- employeeLang.readAll()
    } yield workers.filter(_.departmentId == id)

}


