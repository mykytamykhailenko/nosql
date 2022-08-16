package com.aimprosoft.service

import com.aimprosoft.model._
import com.aimprosoft.model.{Employee, EmployeeFull}

trait TEmployeeService[F[_]] extends TBasicService[F, Employee] {

  def getEmployeeById(id: Id): F[Option[EmployeeFull]]

}