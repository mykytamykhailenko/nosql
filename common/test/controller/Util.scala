package controller

import cats.Id
import com.aimprosoft.common.lang.BasicActionLang
import com.aimprosoft.common.model.{Department, Employee}
import inter.BasicOpStateInterp.{MutableStateActionLang, departmentAssigner, employeeAssigner}

import scala.collection.mutable

object Util {

  val employees: Seq[(Int, Employee)] = Seq(
    1 -> Employee(Some(1), 0, "Shon", "Crawler"),
    2 -> Employee(Some(2), 0, "Sancho", "Crawler"),
    3 -> Employee(Some(3), 1, "Marco", "Italy"))

  val department: Seq[(Int, Department)] = Seq(
    0 -> Department(Some(0), "Scala", ""),
    1 -> Department(Some(1), "ML", ""))

  def createEmployeeMutableState(): BasicActionLang[Id, Employee] =
    MutableStateActionLang[Employee](mutable.Map(employees: _*), employeeAssigner)

  def createDepartmentMutableState(): BasicActionLang[Id, Department] =
    MutableStateActionLang[Department](mutable.Map(department: _*), departmentAssigner)

}
