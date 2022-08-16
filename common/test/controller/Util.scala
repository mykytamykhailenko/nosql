package controller

import cats.Id
import com.aimprosoft.controllers.{DepartmentController, EmployeeController}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.{DepartmentService, EmployeeService}
import inter.BasicOpStateInterp.{MutableStateActionLang, departmentAssigner, employeeAssigner}
import play.api.test.Helpers

import scala.collection.mutable
import scala.concurrent.ExecutionContext

object Util {

  val employees: Seq[(Int, Employee)] = Seq(
    1 -> Employee(Some(1), 0, "Shon", "Crawler"),
    2 -> Employee(Some(2), 0, "Sancho", "Crawler"),
    3 -> Employee(Some(3), 1, "Marco", "Italy"))

  val department: Seq[(Int, Department)] = Seq(
    0 -> Department(Some(0), "Scala", ""),
    1 -> Department(Some(1), "ML", ""))

  def createEmployeeMutableState(): BasicDAO[Id, Employee] =
    MutableStateActionLang[Employee](mutable.Map(employees: _*), employeeAssigner)

  def createDepartmentMutableState(): BasicDAO[Id, Department] =
    MutableStateActionLang[Department](mutable.Map(department: _*), departmentAssigner)

  def createDepartmentController()(implicit mat: Materializer[Id], ec: ExecutionContext): DepartmentController[Id] =
    new DepartmentController[Id](
      DepartmentService(createDepartmentMutableState(), createEmployeeMutableState()),
      Helpers.stubControllerComponents())

  def createEmployeeController()(implicit mat: Materializer[Id], ec: ExecutionContext): EmployeeController[Id] = {
    new EmployeeController[Id](
      EmployeeService(createDepartmentMutableState(), createEmployeeMutableState()),
      Helpers.stubControllerComponents())
  }

}
