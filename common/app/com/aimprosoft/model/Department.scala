package com.aimprosoft.model

import play.api.libs.json.{Format, Json}

case class Department[K](id: Option[K],
                         name: String,
                         description: String) extends Id[K]

object Department {

  implicit def departmentFormat[K : Format]: Format[Department[K]] = Json.format[Department[K]]

}
