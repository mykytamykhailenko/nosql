package com.aimprosoft.common.controllers

import cats.Monad
import com.aimprosoft.common.lang.MatLang
import com.aimprosoft.common.model.{Employee, Id}
import com.aimprosoft.common.service.EmployeeService
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

// Those should be of a concrete effect type.
class EmployeeController[F[_] : Monad] @Inject()(employeeService: EmployeeService[F],
                                                 langMat: MatLang[F],
                                                 controllerComponents: ControllerComponents)
                                                (implicit ec: ExecutionContext) extends
  BasicActionController[F, Employee](employeeService, langMat, controllerComponents) {

  def getEmployeeWithDepartmentById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(employeeService.getEmployeeById(id)).map(v => Ok(Json.toJson(v)))
  }

}