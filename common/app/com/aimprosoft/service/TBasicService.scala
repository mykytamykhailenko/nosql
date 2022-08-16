package com.aimprosoft.service

import com.aimprosoft.model._
import com.aimprosoft.model.TIdentity

trait TBasicService[F[_], M <: TIdentity] {

  def create(value: M): F[Option[Id]]

  def update(value: M): F[Option[Affected]]

  def readAll(): F[Seq[M]]

  def readById(id: Id): F[Option[M]]

  def deleteById(id: Id): F[Affected]

}



