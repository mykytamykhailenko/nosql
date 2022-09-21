package com.aimprosoft.util

import play.api.libs.json.{Format, JsPath, Json, Reads, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

object DepartmentExceptions {

  abstract class DepartmentException(message: String) extends Throwable(message)

  implicit val departmentExceptionWrites: Writes[DepartmentException] = (JsPath \ "message").write[String].contramap(_.getMessage)

  implicit class FutureOps(f: Future[Result])(implicit ec: ExecutionContext) {

    def recoverDepartmentException(): Future[Result] = f.recover {
      case de: DepartmentException => BadRequest(Json.toJson(de))
    }

  }


  case class DepartmentIsNotEmpty(id: UUID) extends DepartmentException(s"Department '$id' is not empty")

  case class DepartmentDoesNotExist(id: UUID) extends DepartmentException(s"Department '$id' does not exist")

  case class DepartmentNameIsAlreadyTaken(name: String) extends DepartmentException(s"'$name' is already taken")

}
