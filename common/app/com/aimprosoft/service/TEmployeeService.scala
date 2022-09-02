package com.aimprosoft.service

import com.aimprosoft.model._

trait TEmployeeService[F[_], K] extends TBasicService[F, K, Employee[K]] {

  def getCompleteEmployeeById(id: K): F[Option[CompleteEmployee[K]]]

  def getEmployeesByDepartmentId(id: K): F[Seq[Employee[K]]]

}