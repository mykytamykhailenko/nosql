package com.aimprosoft.common.service

import com.aimprosoft.common.model._

trait TEmployeeService[F[_]] extends TBasicService[F, Employee] {

  def getEmployeeById(id: Id): F[Option[EmployeeFull]]

}