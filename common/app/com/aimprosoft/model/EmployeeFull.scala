package com.aimprosoft.model

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Json, Reads, Writes}

case class EmployeeFull(id: Option[Id],
                        department: Department,
                        name: String,
                        surname: String) extends TIdentity

object EmployeeFull {

  implicit val employeeWriteFull: Writes[EmployeeFull] = (
    (JsPath \ "id").writeNullable[Id] and
      (JsPath \ "department").write[Department] and
      (JsPath \ "name").write[String] and
      (JsPath \ "surname").write[String]
    )(unlift(EmployeeFull.unapply))

}