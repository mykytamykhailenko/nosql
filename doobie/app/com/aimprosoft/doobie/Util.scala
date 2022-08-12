package com.aimprosoft.doobie

import com.aimprosoft.common.model.Affected

object Util {

  def boolToAffected(v: Boolean): Affected = if (v) 1 else 0

  def boolToAffectedOpt(v: Boolean): Option[Affected] = Option(boolToAffected(v))

}
