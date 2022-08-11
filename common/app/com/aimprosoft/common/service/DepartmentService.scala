package com.aimprosoft.common.service

import cats.Monad
import com.aimprosoft.common.model.{Department, Employee, Id}
import com.aimprosoft.common.lang.BasicActionLang
import com.google.inject.Inject

class DepartmentService[F[_] : Monad] @Inject()(departmentLang: BasicActionLang[F, Department],
                                                employeeLang: BasicActionLang[F, Employee]) {

  import cats.syntax.flatMap._
  import cats.syntax.functor._

  // It is a highly inefficient implementation created for the showcase only.
  def getEmployeesByDepartmentId(id: Id): F[Seq[Employee]] =
    for {
      department <- departmentLang.readById(id)
      employee <- employeeLang.readAll
    } yield department.fold(Seq[Employee]())(dep => employee.filter(_.departmentId == dep.id.get))

}
