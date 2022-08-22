package com.aimprosoft.controllers

import cats.Monad
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.Employee
import com.aimprosoft.service.TEmployeeService
import com.google.inject.Inject
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

// Those should be of a concrete effect type.
class EmployeeController[F[_] : Monad, K: Format] @Inject()(employeeService: TEmployeeService[F, K],
                                                            controllerComponents: ControllerComponents)
                                                           (implicit mat: Materializer[F], ec: ExecutionContext) extends
  BasicActionController[F, K, Employee[K]](employeeService, controllerComponents) {

  def getEmployeeWithDepartmentById(id: K): Action[AnyContent] = Action.async { _ =>
    mat.materialize(employeeService.getEmployeeById(id)).map(v => Ok(Json.toJson(v)))
  }

}