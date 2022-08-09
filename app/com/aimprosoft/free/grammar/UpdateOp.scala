package com.aimprosoft.free.grammar

import com.aimprosoft.model.{Affected, TIdentity}

/**
 * Updates this record and tells if the record has been affected.
 * This value must have an [[com.aimprosoft.model.Id Id]], otherwise no records will be affected and you will get [[None]].
 *
 * @param value Value with identity
 * @tparam M Model type
 */
case class UpdateOp[M <: TIdentity](value: M) extends Op[M, Option[Affected]]

