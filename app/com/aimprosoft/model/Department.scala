package com.aimprosoft.model

import doobie.util.{Write => DoobieWrite, Read => DoobieRead}
import play.api.libs.json.{Format, Json}

case class Department(id: Option[Id],
                      name: String,
                      description: String) extends TIdentity

object Department {

  implicit val departmentFormat: Format[Department] = Json.format[Department]

  implicit val departmentWrite: DoobieWrite[Department] =
    DoobieWrite[(String, String)].contramap { case Department(_, name, description) =>
      (name, description)
    }

  implicit val departmentRead: DoobieRead[Department] = DoobieRead[(Option[Id], String, String)].map((Department.apply _).tupled)

}
