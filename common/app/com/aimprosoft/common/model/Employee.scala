package com.aimprosoft.common.model

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Reads, Writes}

case class Employee(id: Option[Id],
                    departmentId: Id,
                    name: String,
                    surname: String) extends TIdentity

object Employee {

  implicit val employeeWrite: Writes[Employee] = (
    (JsPath \ "id").writeNullable[Id] and
      (JsPath \ "department_id").write[Id] and
      (JsPath \ "name").write[String] and
      (JsPath \ "surname").write[String]
    ) (unlift(Employee.unapply))

  implicit val employeeReads: Reads[Employee] = (
    (JsPath \ "id").readNullable[Id] and
      (JsPath \ "department_id").read[Id] and
      (JsPath \ "name").read[String] and
      (JsPath \ "surname").read[String]
    )(Employee.apply _)

  implicit val employeeFormat: Format[Employee] = Format(employeeReads, employeeWrite)

}
