package service

import cats.Id
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.DepartmentService
import org.mockito.Mockito.when
import org.specs2.matcher.Matchers
import org.specs2.mock.Mockito.mock
import org.specs2.mutable.Specification


class DepartmentServiceSpec extends Specification with Matchers {

  "department service" should {

    "find all employees belonging to the same department" in {

      val departmentMock = mock[BasicDAO[Id, Int, Department[Int]]]
      val employeeMock = mock[BasicDAO[Id, Int, Employee[Int]]]

      when(employeeMock.readAll()).thenReturn(Seq(Employee(None, 1, "Vel", "")))

      val service = DepartmentService(departmentMock, employeeMock)

      service.getEmployeesByDepartmentId(1) === Seq(Employee(None, 1, "Vel", ""))
    }

  }

}
