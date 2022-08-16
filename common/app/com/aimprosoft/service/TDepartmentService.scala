package com.aimprosoft.service

import com.aimprosoft.model._
import com.aimprosoft.model.{Department, Employee}

trait TDepartmentService[F[_]] extends TBasicService[F, Department] {

  def getEmployeesByDepartmentId(id: Id): F[Seq[Employee]]

}