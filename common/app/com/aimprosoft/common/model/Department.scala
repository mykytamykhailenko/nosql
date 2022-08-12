package com.aimprosoft.common.model

import play.api.libs.json.{Format, Json}

case class Department(id: Option[Id],
                      name: String,
                      description: String) extends TIdentity

object Department {

  implicit val departmentFormat: Format[Department] = Json.format[Department]

}
