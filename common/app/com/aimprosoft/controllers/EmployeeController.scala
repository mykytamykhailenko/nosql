package com.aimprosoft.controllers

import cats.Monad
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Employee, Id}
import com.aimprosoft.service.EmployeeService
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

// Those should be of a concrete effect type.
class EmployeeController[F[_] : Monad] @Inject()(employeeService: EmployeeService[F],
                                                 controllerComponents: ControllerComponents)
                                                (implicit mat: Materializer[F], ec: ExecutionContext) extends
  BasicActionController[F, Employee](employeeService, controllerComponents) {

  def getEmployeeWithDepartmentById(id: Id): Action[AnyContent] = Action.async { _ =>
    mat.materialize(employeeService.getEmployeeById(id)).map(v => Ok(Json.toJson(v)))
  }

}