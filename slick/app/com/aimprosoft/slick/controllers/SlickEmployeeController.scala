package com.aimprosoft.slick.controllers

import com.aimprosoft.controllers.EmployeeController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.EmployeeService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

class SlickEmployeeController @Inject()(employeeService: EmployeeService[DBIO, Int],
                                        controllerComponents: ControllerComponents)
                                       (implicit mat: Materializer[DBIO], ec: ExecutionContext) extends
  EmployeeController[DBIO, Int](employeeService, controllerComponents)