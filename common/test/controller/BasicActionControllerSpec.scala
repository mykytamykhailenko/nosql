package controller

import cats.Id
import com.aimprosoft.common.controllers.BasicActionController
import com.aimprosoft.common.model
import com.aimprosoft.common.model.Employee
import controller.Util.createEmployeeMutableState
import inter.IdMatLang
import play.api.http.ContentTypes.JSON
import play.api.libs.json._
import play.api.mvc._
import play.api.test._

import scala.concurrent.ExecutionContext.Implicits.global

class BasicActionControllerSpec extends PlaySpecification with Results {

  "basic action controller" should {

    def createDefaultController(): BasicActionController[Id, Employee] =
      BasicActionController[Id, Employee](createEmployeeMutableState(), IdMatLang(), Helpers.stubControllerComponents())

    "read an entity by id" in {
      val worker = createDefaultController().readById(1)(FakeRequest())

      contentAsJson(worker).as[Employee].name === "Shon"
    }

    "read all entities" in {
      val workers = createDefaultController().readAll()(FakeRequest())

      contentAsJson(workers).as[Seq[Employee]].map(_.name).toSet === Set("Shon", "Sancho", "Marco")
    }

    "delete an entity by id" in {
      val confirmed = createDefaultController().deleteById(1)(FakeRequest())

      contentAsJson(confirmed).as[model.Id] === 1
    }

    "delete an entity, which doesn't exist" in {
      val confirmed = createDefaultController().deleteById(42)(FakeRequest())

      contentAsJson(confirmed).as[model.Id] === 0
    }

    "create an entity without id" in {

      val worker = JsObject(
        Seq(
          "department_id" -> JsNumber(0),
          "name" -> JsString("Shon"),
          "surname" -> JsString("Boreas")
        ))

      val confirmed =
        createDefaultController()
          .create()(
            FakeRequest(Helpers.POST, "/employee")
              .withBody(worker)
              .withHeaders(CONTENT_TYPE -> JSON))

      contentAsJson(confirmed).asOpt[model.Id] must beSome[model.Id]
    }

    "create an entity with id (must not be created)" in {

      val worker = JsObject(
        Seq(
          "id" -> JsNumber(42),
          "department_id" -> JsNumber(0),
          "name" -> JsString("Shon"),
          "surname" -> JsString("Boreas")
        ))

      val confirmed =
        createDefaultController()
          .create()(
            FakeRequest(Helpers.POST, "/employee")
              .withBody(worker)
              .withHeaders(CONTENT_TYPE -> JSON))

      contentAsJson(confirmed).asOpt[model.Id] must beNone
    }

    "update a record with id" in {

      val worker = JsObject(
        Seq(
          "id" -> JsNumber(1),
          "department_id" -> JsNumber(0),
          "name" -> JsString("Shon"),
          "surname" -> JsString("Boreas")
        ))

      val confirmed =
        createDefaultController()
          .update()(
            FakeRequest(Helpers.POST, "/employee")
              .withBody(worker)
              .withHeaders(CONTENT_TYPE -> JSON))

      contentAsJson(confirmed).asOpt[model.Affected] must beSome[model.Affected](1)
    }

    "update a record without id" in {

      val worker = JsObject(
        Seq(
          "department_id" -> JsNumber(0),
          "name" -> JsString("Shon"),
          "surname" -> JsString("Boreas")
        ))

      val confirmed =
        createDefaultController()
          .update()(
            FakeRequest(Helpers.POST, "/employee")
              .withBody(worker)
              .withHeaders(CONTENT_TYPE -> JSON))

      contentAsJson(confirmed).asOpt[model.Affected] must beNone
    }

  }
}
