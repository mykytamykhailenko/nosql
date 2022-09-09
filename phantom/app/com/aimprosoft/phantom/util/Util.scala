package com.aimprosoft.phantom.util

import com.aimprosoft.model.Affected

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, DurationInt, DurationLong}

object Util {

  val unaffected: Affected = 0

  val one: Affected = 1

  abstract class DepException(message: String) extends Throwable(message)

  case class DepartmentIsNotEmpty(id: UUID) extends DepException(s"Department '$id' is not empty")

  case class DepartmentDoesNotExist(id: UUID) extends DepException(s"Department '$id' does not exist")

  case class DepartmentNameIsAlreadyTaken(name: String) extends DepException(s"'$name' is already taken")

  val aMicrosecond: Duration = 1.microsecond

  def currentMicro(): Duration = (Instant.now().toEpochMilli * 1000L).microseconds


}
