package com.aimprosoft.common.service

import cats.Monad
import com.aimprosoft.common.lang.BasicDAO
import com.aimprosoft.common.model.{Affected, Department, Employee, Id}
import com.google.inject.Inject

case class DepartmentService[F[_] : Monad] @Inject()(departmentLang: BasicDAO[F, Department],
                                                     employeeLang: BasicDAO[F, Employee]) extends TDepartmentService[F] {

  def create(value: Department): F[Option[Id]] = departmentLang.create(value)

  def update(value: Department): F[Option[Affected]] = departmentLang.update(value)

  def readAll(): F[Seq[Department]] = departmentLang.readAll()

  def readById(id: Id): F[Option[Department]] = departmentLang.readById(id)

  def deleteById(id: Id): F[Affected] = departmentLang.deleteById(id)

  import cats.syntax.flatMap._
  import cats.syntax.functor._

  // It is a highly inefficient implementation created for the showcase only.
  def getEmployeesByDepartmentId(id: Id): F[Seq[Employee]] =
    for {
      department <- departmentLang.readById(id)
      employee <- employeeLang.readAll()
    } yield department.fold(Seq[Employee]())(dep => employee.filter(_.departmentId == dep.id.get))

}
