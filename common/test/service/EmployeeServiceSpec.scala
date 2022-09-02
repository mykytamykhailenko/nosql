package service

import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{CompleteEmployee, Department, Employee}
import com.aimprosoft.service.EmployeeService
import org.mockito.Mockito.when
import org.specs2.matcher.Matchers
import org.specs2.mock.Mockito.mock
import org.specs2.mutable.Specification

class EmployeeServiceSpec extends Specification with Matchers {

  "employee service" should {

    "find all employees belonging to the same department" in {

      val departmentMock = mock[BasicDAO[Id, Int, Department[Int]]]
      val employeeMock = mock[BasicDAO[Id, Int, Employee[Int]]]

      when(departmentMock.readById(0)).thenReturn(Some(Department(0.some, "Scala", "")))
      when(employeeMock.readById(1)).thenReturn(Some(Employee(1.some, 0, "Shon", "Crawler")))

      val service = EmployeeService(departmentMock, employeeMock)

      service.getCompleteEmployeeById(1) === Some(CompleteEmployee(1.some, Department(0.some, "Scala", ""), "Shon", "Crawler"))
    }

    "find all employees belonging to the same department" in {

      val departmentMock = mock[BasicDAO[Id, Int, Department[Int]]]
      val employeeMock = mock[BasicDAO[Id, Int, Employee[Int]]]

      when(employeeMock.readAll()).thenReturn(Seq(Employee(None, 1, "Vel", "")))

      val service = EmployeeService(departmentMock, employeeMock)

      service.getEmployeesByDepartmentId(1) === Seq(Employee(None, 1, "Vel", ""))
    }

  }

}
