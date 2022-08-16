package com.aimprosoft.doobie.controllers

import com.aimprosoft.common.controllers.DepartmentController
import com.aimprosoft.common.lang.{BasicDAO, MatLang}
import com.aimprosoft.common.model.Department
import com.aimprosoft.common.service.DepartmentService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import zio.Task
import zio.interop.catz.monadErrorInstance

import scala.concurrent.ExecutionContext

class DoobieDepartmentController @Inject()(basicActionLang: BasicDAO[Task, Department],
                                           departmentService: DepartmentService[Task],
                                           langMat: MatLang[Task],
                                           controllerComponents: ControllerComponents)
                                          (implicit ec: ExecutionContext) extends
  DepartmentController[Task](departmentService, langMat, controllerComponents)
