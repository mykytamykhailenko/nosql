package com.aimprosoft.controllers

import play.api.mvc.QueryStringBindable.Parsing

import java.util.UUID

object Bindable {

  implicit object bindableUUID
    extends Parsing[UUID](UUID.fromString, _.toString, (s, e) => s"Cannot parse parameter $s as UUID (String): ${e.getMessage}")

}
