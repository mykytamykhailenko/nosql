package com.aimprosoft.free.grammar

import com.aimprosoft.model.TIdentity

case class ReadAllOp[M <: TIdentity]() extends Op[M, Seq[M]]
