package com.aimprosoft.phantom.util

import com.aimprosoft.model.Affected

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, DurationInt, DurationLong}

object Util {

  val unaffected: Affected = 0

  val one: Affected = 1

  def departmentIsNotEmpty(id: UUID) = new Throwable(s"Department '$id' is not empty")

  def departmentDoesNotExist(id: UUID) = new Throwable(s"Department '$id' does not exist")

  def nameIsAlreadyTaken(name: String) = new Throwable(s"'$name' is already taken")

  def employeeBelongsToUnrealDepartment(id: UUID) = new Throwable(s"This employee belongs to department ('$id'), which does not exist")


  val aMicrosecond: Duration = 1.microsecond

  def currentMicro(): Duration = (Instant.now().toEpochMilli * 1000L).microseconds


}
