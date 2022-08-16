package com.aimprosoft.service

import cats.Monad
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Department, Employee, Id}
import com.google.inject.Inject

case class DepartmentService[F[_] : Monad] @Inject()(departmentLang: BasicDAO[F, Department],
                                                     employeeLang: BasicDAO[F, Employee]) extends TDepartmentService[F] {

  def create(value: Department): F[Option[Id]] = departmentLang.create(value)

  def update(value: Department): F[Option[Affected]] = departmentLang.update(value)

  def readAll(): F[Seq[Department]] = departmentLang.readAll()

  def readById(id: Id): F[Option[Department]] = departmentLang.readById(id)

  def deleteById(id: Id): F[Affected] = departmentLang.deleteById(id)

  import cats.syntax.functor._

  // It is a highly inefficient implementation created for the showcase only.
  def getEmployeesByDepartmentId(id: Id): F[Seq[Employee]] =
    for {
      workers <- employeeLang.readAll()
    } yield workers.filter(_.departmentId == id)

}
