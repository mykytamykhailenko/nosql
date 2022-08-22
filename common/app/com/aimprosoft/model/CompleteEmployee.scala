package com.aimprosoft.model

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Writes}

case class CompleteEmployee[K](id: Option[K],
                               department: Department[K],
                               name: String,
                               surname: String) extends Id[K]

object CompleteEmployee {

  implicit def completeEmployeeWrite[K: Writes](implicit dp: Writes[Department[K]]): Writes[CompleteEmployee[K]] = (
    (JsPath \ "id").writeNullable[K] and
      (JsPath \ "department").write[Department[K]] and
      (JsPath \ "name").write[String] and
      (JsPath \ "surname").write[String]
    ) (unlift(CompleteEmployee.unapply[K]))

}