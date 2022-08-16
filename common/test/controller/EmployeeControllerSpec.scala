
package controller

import cats.Id
import com.aimprosoft.common.controllers.EmployeeController
import com.aimprosoft.common.model
import com.aimprosoft.common.service.EmployeeService
import controller.Util._
import inter.IdMatLang
import play.api.mvc.Results
import play.api.test.{FakeRequest, Helpers, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class EmployeeControllerSpec extends PlaySpecification with Results {

  "employee controller" should {

    def createDefaultController(): EmployeeController[Id] = {
      val lang = createEmployeeMutableState()
      new EmployeeController[Id](
        lang,
        EmployeeService(createDepartmentMutableState(), lang),
        IdMatLang(),
        Helpers.stubControllerComponents())
    }

    "read all employees from the same department" in {

      val employee = createDefaultController().getEmployeeWithDepartmentById(1)(FakeRequest())

      (contentAsJson(employee) \ "id").as[model.Id] === 1
      (contentAsJson(employee) \ "name").as[String] === "Shon"
      (contentAsJson(employee) \ "surname").as[String] === "Crawler"

      (contentAsJson(employee) \ "department" \ "id").as[model.Id] === 0
      (contentAsJson(employee) \ "department" \ "name").as[String] === "Scala"
      (contentAsJson(employee) \ "department" \ "description").as[String] === ""
    }

  }

}
