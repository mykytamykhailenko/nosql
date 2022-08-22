package com.aimprosoft.dao

import com.aimprosoft.model._

/**
 * Represents a set of basic actions.
 *
 * @tparam F An effectful type
 * @tparam M Model type
 */
trait BasicDAO[F[_], K, M <: Id[K]] {

  def create(value: M): F[Option[K]]

  def update(value: M): F[Option[Affected]]

  def readAll(): F[Seq[M]]

  def readById(id: K): F[Option[M]]

  def deleteById(id: K): F[Affected]

}
