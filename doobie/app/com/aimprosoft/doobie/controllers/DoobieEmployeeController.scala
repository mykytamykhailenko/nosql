package com.aimprosoft.doobie.controllers

import com.aimprosoft.controllers.EmployeeController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.EmployeeService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import zio.Task
import zio.interop.catz.monadErrorInstance

import scala.concurrent.ExecutionContext

class DoobieEmployeeController @Inject()(employeeService: EmployeeService[Task],
                                         controllerComponents: ControllerComponents)
                                        (implicit mat: Materializer[Task], ec: ExecutionContext) extends
  EmployeeController[Task](employeeService, controllerComponents)