package com.aimprosoft.service

import cats.Monad
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Department, Employee}
import com.google.inject.Inject

case class DepartmentService[F[_] : Monad, K] @Inject()(departmentLang: BasicDAO[F, K, Department[K]],
                                                        employeeLang: BasicDAO[F, K, Employee[K]]) extends TDepartmentService[F, K] {

  def create(value: Department[K]): F[Option[K]] = departmentLang.create(value)

  def update(value: Department[K]): F[Option[Affected]] = departmentLang.update(value)

  def readAll(): F[Seq[Department[K]]] = departmentLang.readAll()

  def readById(id: K): F[Option[Department[K]]] = departmentLang.readById(id)

  def deleteById(id: K): F[Affected] = departmentLang.deleteById(id)

}
