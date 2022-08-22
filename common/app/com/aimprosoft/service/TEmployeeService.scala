package com.aimprosoft.service

import com.aimprosoft.model._

trait TEmployeeService[F[_], K] extends TBasicService[F, K, Employee[K]] {

  def getEmployeeById(id: K): F[Option[CompleteEmployee[K]]]

}