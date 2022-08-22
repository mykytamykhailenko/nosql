package com.aimprosoft.phantom.util

import com.aimprosoft.model.Affected

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

object Util {

  val nothing: Affected = 0

  val one: Affected = 1

  def deptIsNotEmptyError(id: UUID) = Future.failed(new Throwable(s"Department '$id' is not empty"))

}
