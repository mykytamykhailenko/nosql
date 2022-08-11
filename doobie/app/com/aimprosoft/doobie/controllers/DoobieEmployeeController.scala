package com.aimprosoft.doobie.controllers

import cats.effect.IO
import com.aimprosoft.common.controllers.EmployeeController
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Employee
import com.aimprosoft.common.service.EmployeeService
import com.google.inject.Inject
import monix.eval.Task
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class DoobieEmployeeController @Inject()(basicActionLang: BasicActionLang[Task, Employee],
                                         employeeService: EmployeeService[Task],
                                         langMat: MatLang[Task],
                                         controllerComponents: ControllerComponents)
                                        (implicit ec: ExecutionContext) extends
  EmployeeController[Task](basicActionLang, employeeService, langMat, controllerComponents)