package com.aimprosoft.doobie.controllers

import com.aimprosoft.controllers.EmployeeController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.TEmployeeService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import zio.Task
import zio.interop.catz.monadErrorInstance

import scala.concurrent.ExecutionContext

class DoobieEmployeeController @Inject()(employeeService: TEmployeeService[Task, Int],
                                         controllerComponents: ControllerComponents)
                                        (implicit mat: Materializer[Task], ec: ExecutionContext) extends
  EmployeeController[Task, Int](employeeService, controllerComponents)