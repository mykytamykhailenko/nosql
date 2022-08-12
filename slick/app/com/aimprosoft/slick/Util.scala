package com.aimprosoft.slick

import com.aimprosoft.common.model.Affected

object Util {

  implicit class StringOps(line: String) {
    def quote: String = '\"' + line + '\"'
  }
}
