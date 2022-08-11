package com.aimprosoft.slick.controllers

import com.aimprosoft.common.controllers.EmployeeController
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Employee
import com.aimprosoft.common.service.EmployeeService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

class SlickEmployeeController @Inject()(basicActionLang: BasicActionLang[DBIO, Employee],
                                        employeeService: EmployeeService[DBIO],
                                        langMat: MatLang[DBIO],
                                        controllerComponents: ControllerComponents)
                                       (implicit ec: ExecutionContext) extends
  EmployeeController[DBIO](basicActionLang, employeeService, langMat, controllerComponents)