package com.aimprosoft.model

import doobie.util.{Write => DoobieWrite, Read=> DoobieRead}
import play.api.libs.json.{Format, Json}

case class Employee(id: Option[Id],
                    departmentId: Id,
                    name: String,
                    surname: String) extends TIdentity

object Employee {

  implicit val employeeFormat: Format[Employee] = Json.format[Employee]

  implicit val employeeWrite: DoobieWrite[Employee] =
    DoobieWrite[(Id, String, String)].contramap { case Employee(_, departmentId, name, description) =>
      (departmentId, name, description)
    }

  implicit val employeeRead: DoobieRead[Employee] = DoobieRead[(Option[Id], Id, String, String)].map((Employee.apply _).tupled)

}
