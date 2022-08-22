package com.aimprosoft.model

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Reads, Writes}

case class Employee[K](id: Option[K],
                       departmentId: K,
                       name: String,
                       surname: String) extends Id[K]

object Employee {

  implicit def employeeWrite[K: Writes]: Writes[Employee[K]] = (
    (JsPath \ "id").writeNullable[K] and
      (JsPath \ "department_id").write[K] and
      (JsPath \ "name").write[String] and
      (JsPath \ "surname").write[String]
    ) (unlift(Employee.unapply[K]))

  implicit def employeeReads[K: Reads]: Reads[Employee[K]] = (
    (JsPath \ "id").readNullable[K] and
      (JsPath \ "department_id").read[K] and
      (JsPath \ "name").read[String] and
      (JsPath \ "surname").read[String]
    ) (Employee.apply[K] _)

}
