
package controller

import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.controllers.{DepartmentController, EmployeeController}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model
import com.aimprosoft.model.{Department, Employee, EmployeeFull}
import com.aimprosoft.service.{DepartmentService, EmployeeService}
import controller.Util._
import mat.IdMaterializer
import org.mockito.Mockito.when
import org.specs2.mock.Mockito.mock
import play.api.mvc.Results
import play.api.test.{FakeRequest, Helpers, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class EmployeeControllerSpec extends PlaySpecification with Results {

  implicit val mat: IdMaterializer = IdMaterializer()

  "employee controller" should {

    "read all employees from the same department" in {

      val service = mock[EmployeeService[Id]]


      when(service.getEmployeeById(1)).thenReturn(EmployeeFull(1.some, Department(0.some, "Scala", ""), "Shon", "Crawler").some)

      val employee = new EmployeeController[Id](service, Helpers.stubControllerComponents()).getEmployeeWithDepartmentById(1)(FakeRequest())

      (contentAsJson(employee) \ "id").as[model.Id] === 1
      (contentAsJson(employee) \ "name").as[String] === "Shon"
      (contentAsJson(employee) \ "surname").as[String] === "Crawler"

      (contentAsJson(employee) \ "department" \ "id").as[model.Id] === 0
      (contentAsJson(employee) \ "department" \ "name").as[String] === "Scala"
      (contentAsJson(employee) \ "department" \ "description").as[String] === ""
    }

  }

}
