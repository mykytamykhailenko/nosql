package com.aimprosoft.common.service

import com.aimprosoft.common.model._

trait TDepartmentService[F[_]] extends TBasicService[F, Department] {

  def getEmployeesByDepartmentId(id: Id): F[Seq[Employee]]

}