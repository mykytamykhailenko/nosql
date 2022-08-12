package com.aimprosoft.doobie.controllers

import cats.effect.IO
import com.aimprosoft.common.controllers.DepartmentController
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Department
import com.aimprosoft.common.service.DepartmentService
import com.google.inject.Inject
import zio.Task
import play.api.mvc.ControllerComponents
import zio.interop.catz.monadErrorInstance

import scala.concurrent.ExecutionContext

class DoobieDepartmentController @Inject()(basicActionLang: BasicActionLang[Task, Department],
                                           departmentService: DepartmentService[Task],
                                           langMat: MatLang[Task],
                                           controllerComponents: ControllerComponents)
                                          (implicit ec: ExecutionContext) extends
  DepartmentController[Task](basicActionLang, departmentService, langMat, controllerComponents)
