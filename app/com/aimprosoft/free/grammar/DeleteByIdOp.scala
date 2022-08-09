package com.aimprosoft.free.grammar

import com.aimprosoft.model.{Affected, Id, TIdentity}

case class DeleteByIdOp[M <: TIdentity](id: Id) extends Op[M, Affected]
