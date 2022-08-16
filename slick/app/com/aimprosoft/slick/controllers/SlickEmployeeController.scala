package com.aimprosoft.slick.controllers

import com.aimprosoft.common.controllers.EmployeeController
import com.aimprosoft.common.lang.{BasicDAO, MatLang}
import com.aimprosoft.common.model.Employee
import com.aimprosoft.common.service.EmployeeService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

class SlickEmployeeController @Inject()(employeeService: EmployeeService[DBIO],
                                        langMat: MatLang[DBIO],
                                        controllerComponents: ControllerComponents)
                                       (implicit ec: ExecutionContext) extends
  EmployeeController[DBIO](employeeService, langMat, controllerComponents)