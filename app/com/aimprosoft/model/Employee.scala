package com.aimprosoft.model

import play.api.libs.json.{Format, Json}

case class Employee(id: Option[Id],
                    departmentId: Id,
                    name: String,
                    surname: String) extends TIdentity

object Employee {

  implicit val employeeFormat: Format[Employee] = Json.format[Employee]

}
