package controller

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.testkit.NoMaterializer
import akka.util.Timeout
import cats.Id
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.common.controllers.BasicActionController
import com.aimprosoft.common.lang.BasicActionLang
import com.aimprosoft.common.model
import com.aimprosoft.common.model.Employee
import inter.BasicOpStateInterp.{MutableStateActionLang, employeeAssigner}
import inter.IdMatLang
import play.api.libs.json._
import play.api.mvc.{BodyParser, _}
import play.api.test.Helpers.stubBodyParser
import play.api.test._
import play.api.data.FormBinding.Implicits._
import play.api.http.ContentTypes.JSON

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class BasicActionControllerSpec extends PlaySpecification with Results {

  implicit val actorSystem = ActorSystem("test")
  implicit val materializer = Materializer.matFromSystem

  "basic action controller" should {


    val employee = Employee(Some(1), 0, "Shon", "Crawler")

    def createMutableState(): BasicActionLang[Id, Employee] =
      MutableStateActionLang[Employee](mutable.Map(employee.id.get -> employee), employeeAssigner)

    def createDefaultController(): BasicActionController[Id, Employee] =
      BasicActionController[Id, Employee](createMutableState(), IdMatLang(), Helpers.stubControllerComponents())

    def createControllerWithBody(body: JsValue): BasicActionController[Id, Employee] =
      BasicActionController[Id, Employee](
        createMutableState(),
        IdMatLang(),
        Helpers.stubControllerComponents(
          stubBodyParser(AnyContentAsJson(body))
        )
      )

    "read an entity by id" in {
      val worker = createDefaultController().readById(1)(FakeRequest())

      contentAsJson(worker).as[Employee] === employee
    }

    "delete an entity by id" in {
      val confirmed = createDefaultController().deleteById(1)(FakeRequest())

      contentAsJson(confirmed).as[model.Id] === 1
    }

    "create an entity without id" in {

      val confirmed =
        createDefaultController()
          .create()(FakeRequest(
            Helpers.POST,
            "/employee",
            FakeHeaders(Seq(CONTENT_TYPE -> JSON)),
            Employee(None, 0, "Shon", "Boreas")))

      contentAsJson(confirmed).asOpt[model.Id] must beSome[model.Id]
    }

  }
}
