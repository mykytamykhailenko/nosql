package service

import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.model.{Department, Employee, EmployeeFull}
import com.aimprosoft.service.EmployeeService
import dao.MutableStateDAO
import dao.StateDAO.{departmentAssigner, employeeAssigner}
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification

import scala.collection.mutable

class EmployeeServiceSpec extends Specification with Matchers {

  "employee service" should {

    "find all employees belonging to the same department" in {

      val employees = mutable.Map(1 -> Employee(1.some, 1, "Mike", "Shen"))
      val departments = mutable.Map(1 -> Department(1.some, "Scala/ML", ""))

      val completeEmployee = EmployeeFull(Some(1), Department(Some(1), "Scala/ML", ""), "Mike", "Shen")

      val employeeLang = MutableStateDAO(employees, employeeAssigner)
      val departmentLang = MutableStateDAO(departments, departmentAssigner)

      val service = EmployeeService(departmentLang, employeeLang)

      service.getEmployeeById(1) === Some(completeEmployee)
    }

  }

}
