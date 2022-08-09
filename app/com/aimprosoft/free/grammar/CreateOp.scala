package com.aimprosoft.free.grammar

import com.aimprosoft.model.{Id, TIdentity}

/**
 * Stores this record in the database and returns its newly created [[com.aimprosoft.model.Id Id]].
 * The [[com.aimprosoft.model.Id Id]] will be created on the server side, so you must not provide it yourself.
 * If you provide the [[com.aimprosoft.model.Id Id]] manually, it won't be created and you will get [[None]].
 *
 * @param value Value without identity
 * @tparam M Model type
 */
case class CreateOp[M<: TIdentity](value: M) extends Op[M, Option[Id]]

