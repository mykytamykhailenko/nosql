package com.aimprosoft.mongo.controller

import com.aimprosoft.controllers.DepartmentController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.TDepartmentService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MongoDepartmentController @Inject()(departmentService: TDepartmentService[Future, UUID],
                                          controllerComponents: ControllerComponents)
                                         (implicit mat: Materializer[Future], ec: ExecutionContext) extends
  DepartmentController[Future, UUID](departmentService, controllerComponents)