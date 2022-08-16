package controller

import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model
import com.aimprosoft.model._
import controller.Util.createEmployeeController
import mat.IdMaterializer
import org.mockito.Mockito.when
import org.specs2.mock.Mockito.mock
import play.api.http.ContentTypes.JSON
import play.api.libs.json._
import play.api.mvc._
import play.api.test._

import scala.concurrent.ExecutionContext.Implicits.global

class BasicActionControllerSpec extends PlaySpecification with Results {

  implicit val mat: IdMaterializer = IdMaterializer()

  "basic action controller" should {

    val departmentMock = mock[BasicDAO[Id, Department]]

    "read an entity by id" in {
      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.readById(1)).thenReturn(Employee(None, 0, "Shon", "Boreas").some)

      val worker = createEmployeeController(departmentMock, employeeMock).readById(1)(FakeRequest())

      (contentAsJson(worker) \ "name").as[String] === "Shon"
      (contentAsJson(worker) \ "surname").as[String] === "Boreas"
      (contentAsJson(worker) \ "department_id").as[model.Id] === 0
      (contentAsJson(worker) \ "id").asOpt[model.Id] must beNone
    }

    "read all entities" in {
      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.readAll()).thenReturn(Seq(Employee(None, 0, "Shon", "Boreas")))

      val workers = createEmployeeController(departmentMock, employeeMock).readAll()(FakeRequest())

      (contentAsJson(workers) \ 0 \ "name").as[String] === "Shon"
      (contentAsJson(workers) \ 0 \ "surname").as[String] === "Boreas"
      (contentAsJson(workers) \ 0 \ "department_id").as[model.Id] === 0
      (contentAsJson(workers) \ 0 \ "id").asOpt[model.Id] must beNone
    }

    "delete an entity by id" in {
      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.deleteById(1)).thenReturn(1)

      val confirmed = createEmployeeController(departmentMock, employeeMock).deleteById(1)(FakeRequest())

      contentAsJson(confirmed).as[model.Id] === 1
    }

    "create an entity" in {

      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.create(Employee(None, 0, "Shon", "Boreas"))).thenReturn(0.some)

      val worker = JsObject(
        Seq(
          "department_id" -> JsNumber(0),
          "name" -> JsString("Shon"),
          "surname" -> JsString("Boreas")
        ))

      val confirmed =
        createEmployeeController(departmentMock, employeeMock)
          .create()(
            FakeRequest(Helpers.POST, "/employee")
              .withBody(worker)
              .withHeaders(CONTENT_TYPE -> JSON))

      contentAsJson(confirmed).asOpt[model.Id] must beSome[model.Id]
    }

    "update a record with id" in {

      val employeeMock = mock[BasicDAO[Id, Employee]]

      when(employeeMock.update(Employee(Some(1), 0, "Shon", "Boreas"))).thenReturn(1.some)

      val worker = JsObject(
        Seq(
          "id" -> JsNumber(1),
          "department_id" -> JsNumber(0),
          "name" -> JsString("Shon"),
          "surname" -> JsString("Boreas")
        ))

      val confirmed =
        createEmployeeController(departmentMock, employeeMock)
          .update()(
            FakeRequest(Helpers.POST, "/employee")
              .withBody(worker)
              .withHeaders(CONTENT_TYPE -> JSON))

      contentAsJson(confirmed).asOpt[model.Affected] must beSome[model.Affected](1)
    }

  }
}
