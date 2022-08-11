package com.aimprosoft.doobie.controllers

import cats.effect.IO
import com.aimprosoft.common.controllers.DepartmentController
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Department
import com.aimprosoft.common.service.DepartmentService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class DoobieDepartmentController @Inject()(basicActionLang: BasicActionLang[IO, Department],
                                           departmentService: DepartmentService[IO],
                                           langMat: MatLang[IO],
                                           controllerComponents: ControllerComponents)
                                          (implicit ec: ExecutionContext) extends
  DepartmentController[IO](basicActionLang, departmentService, langMat, controllerComponents)
