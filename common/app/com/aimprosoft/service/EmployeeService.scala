package com.aimprosoft.service

import cats.Monad
import cats.data.OptionT
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model._
import com.google.inject.Inject

// Alternative solution would be to create another language with this specific operation.
case class EmployeeService[F[_] : Monad] @Inject()(departmentLang: BasicDAO[F, Department],
                                                   employeeLang: BasicDAO[F, Employee]) extends TEmployeeService[F] {

  def create(value: Employee): F[Option[Id]] = employeeLang.create(value)

  def update(value: Employee): F[Option[Affected]] = employeeLang.update(value)

  def readAll(): F[Seq[Employee]] = employeeLang.readAll()

  def readById(id: Id): F[Option[Employee]] = employeeLang.readById(id)

  def deleteById(id: Id): F[Affected] = employeeLang.deleteById(id)

  def getEmployeeById(id: Id): F[Option[EmployeeFull]] = {

    val completeEmployee = for {
      Employee(employeeId, departmentId, name, surname) <- OptionT(employeeLang.readById(id))
      department <- OptionT(departmentLang.readById(departmentId))
    } yield EmployeeFull(employeeId, department, name, surname)

    completeEmployee.value
  }

}


