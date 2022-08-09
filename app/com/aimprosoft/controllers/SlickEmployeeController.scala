package com.aimprosoft.controllers

import com.aimprosoft.lang.{BasicActionLang, MatLang}
import com.aimprosoft.model.{Employee, Id}
import com.aimprosoft.service.EmployeeService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

// Those should be of a concrete effect type.
class SlickEmployeeController @Inject()(basicActionLang: BasicActionLang[DBIO, Employee],
                                        employeeService: EmployeeService[DBIO],
                                        langMat: MatLang[DBIO],
                                        controllerComponents: ControllerComponents)
                                       (implicit ec: ExecutionContext) extends
  BasicActionController[DBIO, Employee](basicActionLang, langMat, controllerComponents) {

  def getEmployeeWithDepartmentById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(employeeService.getEmployeeById(id)).map(v => Ok(Json.toJson(v)))
  }

}