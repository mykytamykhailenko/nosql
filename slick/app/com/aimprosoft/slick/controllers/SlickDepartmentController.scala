package com.aimprosoft.slick.controllers

import com.aimprosoft.common.controllers.DepartmentController
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Department
import com.aimprosoft.common.service.DepartmentService
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.google.inject.Inject
import play.api.mvc.ControllerComponents
import slickeffect.implicits._

import scala.concurrent.ExecutionContext

class SlickDepartmentController @Inject()(basicActionLang: BasicActionLang[DBIO, Department],
                                          departmentService: DepartmentService[DBIO],
                                          langMat: MatLang[DBIO],
                                          controllerComponents: ControllerComponents)
                                         (implicit ec: ExecutionContext) extends
  DepartmentController[DBIO](basicActionLang, departmentService, langMat, controllerComponents)
