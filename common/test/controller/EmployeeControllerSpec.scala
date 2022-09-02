
package controller

import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.controllers.{DepartmentController, EmployeeController}
import com.aimprosoft.model.{CompleteEmployee, Department, Employee}
import com.aimprosoft.service.{DepartmentService, EmployeeService}
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

      val service = mock[EmployeeService[Id, Int]]

      when(service.getCompleteEmployeeById(1)).thenReturn(CompleteEmployee(1.some, Department(0.some, "Scala", ""), "Shon", "Crawler").some)

      val employee = new EmployeeController[Id, Int](service, Helpers.stubControllerComponents()).getCompleteEmployeeById(1)(FakeRequest())

      (contentAsJson(employee) \ "id").as[Int] === 1
      (contentAsJson(employee) \ "name").as[String] === "Shon"
      (contentAsJson(employee) \ "surname").as[String] === "Crawler"

      (contentAsJson(employee) \ "department" \ "id").as[Int] === 0
      (contentAsJson(employee) \ "department" \ "name").as[String] === "Scala"
      (contentAsJson(employee) \ "department" \ "description").as[String] === ""
    }

    "read all employees from the same department" in {
      val service = mock[EmployeeService[Id, Int]]

      when(service.getEmployeesByDepartmentId(0)).thenReturn(Seq(Employee(None, 0, "Vel", "")))

      val employees = new EmployeeController[Id, Int](service, Helpers.stubControllerComponents()).getEmployeesByDepartmentId(0)(FakeRequest())

      (contentAsJson(employees) \ 0 \ "name").as[String] === "Vel"
    }

  }

}
