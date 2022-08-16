package com.aimprosoft.common.lang

import com.aimprosoft.common.model.TIdentity
import com.aimprosoft.common.model._

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