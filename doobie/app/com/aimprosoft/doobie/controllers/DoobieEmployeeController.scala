package com.aimprosoft.doobie.controllers

import com.aimprosoft.common.controllers.EmployeeController
import com.aimprosoft.common.lang.MatLang
import com.aimprosoft.common.service.EmployeeService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import zio.Task
import zio.interop.catz.monadErrorInstance

import scala.concurrent.ExecutionContext

class DoobieEmployeeController @Inject()(employeeService: EmployeeService[Task],
                                         langMat: MatLang[Task],
                                         controllerComponents: ControllerComponents)
                                        (implicit ec: ExecutionContext) extends
  EmployeeController[Task](employeeService, langMat, controllerComponents)