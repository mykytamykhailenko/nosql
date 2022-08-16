
package controller

import com.aimprosoft.common.model
import controller.Util._
import play.api.mvc.Results
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class EmployeeControllerSpec extends PlaySpecification with Results {

  "employee controller" should {

    "read all employees from the same department" in {

      val employee = createEmployeeController().getEmployeeWithDepartmentById(1)(FakeRequest())

      (contentAsJson(employee) \ "id").as[model.Id] === 1
      (contentAsJson(employee) \ "name").as[String] === "Shon"
      (contentAsJson(employee) \ "surname").as[String] === "Crawler"

      (contentAsJson(employee) \ "department" \ "id").as[model.Id] === 0
      (contentAsJson(employee) \ "department" \ "name").as[String] === "Scala"
      (contentAsJson(employee) \ "department" \ "description").as[String] === ""
    }

  }

}
