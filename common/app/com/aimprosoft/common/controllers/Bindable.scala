package com.aimprosoft.common.controllers

import com.aimprosoft.common.model.Id
import play.api.mvc.QueryStringBindable.Parsing

object Bindable {

  implicit object bindableId
    extends Parsing[Id](Integer.parseInt, _.toString, (s, e) => s"Cannot parse parameter $s as Id (Int): ${e.getMessage}")

}
