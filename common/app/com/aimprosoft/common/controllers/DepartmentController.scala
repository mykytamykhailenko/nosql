package com.aimprosoft.common.controllers

import cats.Monad
import com.aimprosoft.common.lang.MatLang
import com.aimprosoft.common.model.{Department, Id}
import com.aimprosoft.common.service.DepartmentService
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class DepartmentController[F[_] : Monad] @Inject()(departmentService: DepartmentService[F],
                                                   langMat: MatLang[F],
                                                   controllerComponents: ControllerComponents)
                                                  (implicit ec: ExecutionContext) extends
  BasicActionController[F, Department](departmentService, langMat, controllerComponents) {

  def getEmployeesByDepartmentId(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(departmentService.getEmployeesByDepartmentId(id)).map(v => Ok(Json.toJson(v)))
  }

}
