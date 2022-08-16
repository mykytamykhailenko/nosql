package com.aimprosoft.dao

import com.aimprosoft.model._
import com.aimprosoft.model.TIdentity

/**
 * Represents a set of basic actions.
 *
 * @tparam F An effectful type
 * @tparam M Model type
 */
trait BasicDAO[F[_], M <: TIdentity] {

  def create(value: M): F[Option[Id]]

  def update(value: M): F[Option[Affected]]

  def readAll(): F[Seq[M]]

  def readById(id: Id): F[Option[M]]

  def deleteById(id: Id): F[Affected]

}
