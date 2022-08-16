package controller

import cats.Id
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Department, Employee}
import controller.Util._
import mat.IdMaterializer
import org.mockito.Mockito.when
import org.specs2.mock.Mockito.mock
import play.api.mvc.Results
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class DepartmentControllerSpec extends PlaySpecification with Results {

  implicit val mat: IdMaterializer = IdMaterializer()

  "department controller" should {

    "read all employees from the same department" in {

      val departmentMock = mock[BasicDAO[Id, Department]]
      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.readAll()).thenReturn(Seq(Employee(None, 0, "Vel", "")))

      val employees = createDepartmentController(departmentMock, employeeMock).getEmployeesByDepartmentId(0)(FakeRequest())

      (contentAsJson(employees) \ 0 \ "name").as[String] === "Vel"
    }

  }

}
