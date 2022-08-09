package com.aimprosoft.tagless.service

import com.aimprosoft.model.{Department, Employee, EmployeeFull, Id, TIdentity}
import com.aimprosoft.tagless.BasicActionLang
import cats.Monad
import cats.data.OptionT
import com.google.inject.Inject

// Alternative solution would be to create another language with this specific operation.
class EmployeeService[F[_] : Monad] @Inject()(departmentLang: BasicActionLang[F, Department],
                                              employeeLang: BasicActionLang[F, Employee]) {

  def getEmployeeById(id: Id): F[Option[EmployeeFull]] = {

    val completeEmployee = for {
      Employee(employeeId, departmentId, name, surname) <- OptionT(employeeLang.readById(id))
      department <- OptionT(departmentLang.readById(departmentId))
    } yield EmployeeFull(employeeId, department, name, surname)

    completeEmployee.value
  }

}


