package service

import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.DepartmentService
import dao.MutableStateDAO
import dao.StateDAO._
import org.mockito.Mockito.when
import org.specs2.matcher.Matchers
import org.specs2.mock.Mockito.mock
import org.specs2.mutable.Specification

import scala.collection.mutable


class DepartmentServiceSpec extends Specification with Matchers {

  "department service" should {

    "find all employees belonging to the same department" in {

      val departmentMock = mock[BasicDAO[Id, Department]]
      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.readAll()).thenReturn(Seq(Employee(None, 0, "Vel", "")))

      val service = DepartmentService(departmentMock, employeeMock)

      service.getEmployeesByDepartmentId(1) === Seq(Employee(None, 0, "Vel", ""))
    }

  }

}
