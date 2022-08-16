package controller

import cats.Id
import com.aimprosoft.controllers.{DepartmentController, EmployeeController}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.{DepartmentService, EmployeeService}
import play.api.test.Helpers

import scala.concurrent.ExecutionContext

object Util {

  def createDepartmentController(departmentDAO: BasicDAO[Id, Department],
                                 employeeDAO: BasicDAO[Id, Employee])(implicit mat: Materializer[Id], ec: ExecutionContext): DepartmentController[Id] =
    new DepartmentController[Id](
      DepartmentService(departmentDAO, employeeDAO),
      Helpers.stubControllerComponents())

  def createEmployeeController(departmentDAO: BasicDAO[Id, Department],
                               employeeDAO: BasicDAO[Id, Employee])
                              (implicit mat: Materializer[Id], ec: ExecutionContext): EmployeeController[Id] = {
    new EmployeeController[Id](EmployeeService(departmentDAO, employeeDAO), Helpers.stubControllerComponents())
  }

}
