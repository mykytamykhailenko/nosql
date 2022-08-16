
package controller

import cats.Id
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model
import com.aimprosoft.model.{Department, Employee}
import controller.Util._
import mat.IdMaterializer
import org.mockito.Mockito.when
import org.specs2.mock.Mockito.mock
import play.api.mvc.Results
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class EmployeeControllerSpec extends PlaySpecification with Results {

  implicit val mat: IdMaterializer = IdMaterializer()

  "employee controller" should {

    "read all employees from the same department" in {

      val departmentMock = mock[BasicDAO[Id, Department]]
      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(departmentMock.readById(0)).thenReturn(Some(Department(Some(0), "Scala", "")))
      when(employeeMock.readById(1)).thenReturn(Some(Employee(Some(1), 0, "Shon", "Crawler")))

      val employee = createEmployeeController(departmentMock, employeeMock).getEmployeeWithDepartmentById(1)(FakeRequest())

      (contentAsJson(employee) \ "id").as[model.Id] === 1
      (contentAsJson(employee) \ "name").as[String] === "Shon"
      (contentAsJson(employee) \ "surname").as[String] === "Crawler"

      (contentAsJson(employee) \ "department" \ "id").as[model.Id] === 0
      (contentAsJson(employee) \ "department" \ "name").as[String] === "Scala"
      (contentAsJson(employee) \ "department" \ "description").as[String] === ""
    }

  }

}
