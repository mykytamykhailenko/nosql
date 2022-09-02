package com.aimprosoft.controllers

import cats.Monad
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.Department
import com.aimprosoft.service.TDepartmentService
import com.google.inject.Inject
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class DepartmentController[F[_] : Monad, K: Format] @Inject()(departmentService: TDepartmentService[F, K],
                                                              controllerComponents: ControllerComponents)
                                                             (implicit mat: Materializer[F], ec: ExecutionContext) extends
  BasicActionController[F, K, Department[K]](departmentService, controllerComponents)
