package com.aimprosoft.free

import cats.free.Free
import com.aimprosoft.free.grammar._
import com.aimprosoft.model._

/**
 * Contains smart constructors for [[cats.free.Free Free]].
 */
object FreeOp {

  def create[M <: TIdentity](v: M): Free[Op[M, *], Option[Id]] = Free.liftF[Op[M, *], Option[Id]](CreateOp(v))

  def update[M <: TIdentity](v: M): Free[Op[M, *], Option[Affected]] = Free.liftF[Op[M, *], Option[Affected]](UpdateOp(v))

  def readAll[M <: TIdentity](): Free[Op[M, *], Seq[M]] = Free.liftF[Op[M, *], Seq[M]](ReadAllOp())

  def readById[M <: TIdentity](id: Id): Free[Op[M, *], Option[M]] = Free.liftF[Op[M, *], Option[M]](ReadByIdOp(id))

  def deleteById[M <: TIdentity](id: Id): Free[Op[M, *], Affected] = Free.liftF[Op[M, *], Affected](DeleteByIdOp(id))

}