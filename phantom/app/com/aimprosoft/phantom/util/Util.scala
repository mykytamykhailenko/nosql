package com.aimprosoft.phantom.util

import com.aimprosoft.model.Affected

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, DurationInt, DurationLong}

object Util {

  val unaffected: Affected = 0

  val one: Affected = 1

  val aMicrosecond: Duration = 1.microsecond

  def currentMicro(): Duration = (Instant.now().toEpochMilli * 1000L).microseconds


}
