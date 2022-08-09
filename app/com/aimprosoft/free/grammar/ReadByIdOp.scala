package com.aimprosoft.free.grammar

import com.aimprosoft.model.{Id, TIdentity}

case class ReadByIdOp[M <: TIdentity](id: Id) extends Op[M, Option[M]]
