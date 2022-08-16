package com.aimprosoft.slick.controllers

import com.aimprosoft.controllers.DepartmentController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.DepartmentService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

class SlickDepartmentController @Inject()(departmentService: DepartmentService[DBIO],
                                          controllerComponents: ControllerComponents)
                                         (implicit mat: Materializer[DBIO], ec: ExecutionContext) extends
  DepartmentController[DBIO](departmentService, controllerComponents)
