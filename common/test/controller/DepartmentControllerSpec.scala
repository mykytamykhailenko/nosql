package controller

import cats.Id
import com.aimprosoft.common.controllers.DepartmentController
import com.aimprosoft.common.model.Employee
import com.aimprosoft.common.service.DepartmentService
import controller.Util._
import inter.IdMatLang
import play.api.mvc.Results
import play.api.test.{FakeRequest, Helpers, PlaySpecification}

import scala.concurrent.ExecutionContext.Implicits.global

class DepartmentControllerSpec extends PlaySpecification with Results {

  "department controller" should {

    def createDefaultController(): DepartmentController[Id] = {
      val lang = createDepartmentMutableState()
      new DepartmentController[Id](
        lang,
        DepartmentService(lang, createEmployeeMutableState()),
        IdMatLang(),
        Helpers.stubControllerComponents())
    }

    "read all employees from the same department" in {

      val employees = createDefaultController().getEmployeesByDepartmentId(0)(FakeRequest())

      contentAsJson(employees).as[Seq[Employee]].map(_.surname).toSet === Set("Crawler")
    }

  }

}
