package com.aimprosoft.doobie.controllers

import cats.effect.IO
import com.aimprosoft.common.controllers.EmployeeController
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Employee
import com.aimprosoft.common.service.EmployeeService
import com.google.inject.Inject
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class DoobieEmployeeController @Inject()(basicActionLang: BasicActionLang[IO, Employee],
                                         employeeService: EmployeeService[IO],
                                         langMat: MatLang[IO],
                                         controllerComponents: ControllerComponents)
                                        (implicit ec: ExecutionContext) extends
  EmployeeController[IO](basicActionLang, employeeService, langMat, controllerComponents)