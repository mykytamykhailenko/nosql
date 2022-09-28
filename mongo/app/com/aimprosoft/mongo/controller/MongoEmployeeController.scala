package com.aimprosoft.mongo.controller

import com.aimprosoft.controllers.EmployeeController
import com.aimprosoft.mat.Materializer
import com.aimprosoft.service.TEmployeeService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MongoEmployeeController @Inject()(departmentService: TEmployeeService[Future, UUID],
                                        controllerComponents: ControllerComponents)
                                       (implicit mat: Materializer[Future], ec: ExecutionContext) extends
  EmployeeController[Future, UUID](departmentService, controllerComponents)