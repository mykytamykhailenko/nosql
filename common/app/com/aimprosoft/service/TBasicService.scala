package com.aimprosoft.service

import com.aimprosoft.model._

trait TBasicService[F[_], K, M <: Id[K]] {

  def create(value: M): F[Option[K]]

  def update(value: M): F[Option[Affected]]

  def readAll(): F[Seq[M]]

  def readById(id: K): F[Option[M]]

  def deleteById(id: K): F[Affected]

}



