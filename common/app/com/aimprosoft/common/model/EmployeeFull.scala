package com.aimprosoft.common.model

import play.api.libs.json.{Format, Json}

case class EmployeeFull(id: Option[Id],
                        department: Department,
                        name: String,
                        surname: String) extends TIdentity

object EmployeeFull {

  implicit val employeeFullFormat: Format[EmployeeFull] = Json.format[EmployeeFull]

}