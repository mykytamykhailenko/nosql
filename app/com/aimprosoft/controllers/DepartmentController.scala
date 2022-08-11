package com.aimprosoft.controllers

import com.aimprosoft.lang.{BasicActionLang, MatLang}
import com.aimprosoft.model.{Department, Id}
import com.aimprosoft.service.DepartmentService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

class DepartmentController @Inject()(basicActionLang: BasicActionLang[DBIO, Department],
                                          departmentService: DepartmentService[DBIO],
                                          langMat: MatLang[DBIO],
                                          controllerComponents: ControllerComponents)
                                         (implicit ec: ExecutionContext) extends
  BasicActionController[DBIO, Department](basicActionLang, langMat, controllerComponents) {

  def getEmployeesByDepartmentId(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(departmentService.getEmployeesByDepartmentId(id)).map(v => Ok(Json.toJson(v)))
  }

}
