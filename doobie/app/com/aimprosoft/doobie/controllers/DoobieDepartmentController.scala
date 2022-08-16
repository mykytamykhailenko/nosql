package com.aimprosoft.doobie.controllers

import com.aimprosoft.controllers.DepartmentController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.DepartmentService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import zio.Task
import zio.interop.catz.monadErrorInstance

import scala.concurrent.ExecutionContext

class DoobieDepartmentController @Inject()(departmentService: DepartmentService[Task],
                                           controllerComponents: ControllerComponents)
                                          (implicit mat: Materializer[Task], ec: ExecutionContext) extends
  DepartmentController[Task](departmentService, controllerComponents)
