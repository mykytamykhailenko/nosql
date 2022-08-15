import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.common.model.{Department, Employee}
import com.aimprosoft.common.service.DepartmentService
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import util.inter.BasicOpStateInterp._

import scala.collection.mutable


class DepartmentServiceSpec extends Specification with Matchers {

  "department service" should {

    "find all employees belonging to the same department" in {

      val employees = mutable.Map(
        1 -> Employee(1.some, 1, "Mike", "Shen"),
        2 -> Employee(2.some, 1, "John", "Johnson"),
        3 -> Employee(3.some, 2, "Jessica", "London"))

      val departments = mutable.Map(
        1 -> Department(1.some, "Scala/ML", ""),
        2 -> Department(2.some, "Java", ""))

      val employeesOfDepartment = Set(
        Employee(1.some, 1, "Mike", "Shen"),
        Employee(2.some, 1, "John", "Johnson"))

      val employeeLang = MutableStateActionLang(employees, employeeAssigner)
      val departmentLang = MutableStateActionLang(departments, departmentAssigner)

      val service = DepartmentService(departmentLang, employeeLang)

      service.getEmployeesByDepartmentId(1).toSet === employeesOfDepartment
    }

  }

}
