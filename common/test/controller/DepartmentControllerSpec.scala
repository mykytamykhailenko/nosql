package controller

import com.aimprosoft.model.Employee
import controller.Util._
import inter.IdMaterializer
import play.api.mvc.Results
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class DepartmentControllerSpec extends PlaySpecification with Results {

  implicit val mat: IdMaterializer = IdMaterializer()

  "department controller" should {

    "read all employees from the same department" in {

      val employees = createDepartmentController().getEmployeesByDepartmentId(0)(FakeRequest())

      contentAsJson(employees).as[Seq[Employee]].map(_.surname).toSet === Set("Crawler")
    }

  }

}
