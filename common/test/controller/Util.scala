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

  def createDepartmentController(departmentDAO: BasicDAO[Id, Int, Department[Int]],
                                 employeeDAO: BasicDAO[Id, Int, Employee[Int]])(implicit mat: Materializer[Id], ec: ExecutionContext): DepartmentController[Id, Int] =
    new DepartmentController[Id, Int](
      DepartmentService(departmentDAO, employeeDAO),
      Helpers.stubControllerComponents())

  def createEmployeeController(departmentDAO: BasicDAO[Id, Int, Department[Int]],
                               employeeDAO: BasicDAO[Id, Int, Employee[Int]])
                              (implicit mat: Materializer[Id], ec: ExecutionContext): EmployeeController[Id, Int] = {
    new EmployeeController[Id, Int](EmployeeService(departmentDAO, employeeDAO), Helpers.stubControllerComponents())
  }

}
