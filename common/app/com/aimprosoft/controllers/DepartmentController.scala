package com.aimprosoft.controllers

import cats.Monad
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Id}
import com.aimprosoft.service.DepartmentService
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class DepartmentController[F[_] : Monad] @Inject()(departmentService: DepartmentService[F],
                                                   controllerComponents: ControllerComponents)
                                                  (implicit mat: Materializer[F], ec: ExecutionContext) extends
  BasicActionController[F, Department](departmentService, controllerComponents) {

  def getEmployeesByDepartmentId(id: Id): Action[AnyContent] = Action.async { _ =>
    mat.materialize(departmentService.getEmployeesByDepartmentId(id)).map(v => Ok(Json.toJson(v)))
  }

}
