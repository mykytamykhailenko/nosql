package com.aimprosoft.controllers

import cats.Monad
import com.aimprosoft.model.{Department, Employee, Id, TIdentity}
import com.aimprosoft.tagless.BasicActionLang
import com.aimprosoft.tagless.service.{DepartmentService, EmployeeService}
import com.google.inject.Inject
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

trait LangMat[F[_]] {
  def materialize[M](v: F[M]): Future[M]
}

import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.databaseConfig.db

class SlickMat extends LangMat[DBIO] {
  def materialize[M](v: DBIO[M]): Future[M] = db.run(v)
}

import slickeffect.implicits._

// Those should be of a concrete effect type.
class SlickEmployeeController @Inject()(basicActionLang: BasicActionLang[DBIO, Employee],
                                        employeeService: EmployeeService[DBIO],
                                        langMat: LangMat[DBIO],
                                        controllerComponents: ControllerComponents)
                                       (implicit ec: ExecutionContext) extends
  BasicActionController[DBIO, Employee](basicActionLang, langMat, controllerComponents) {

  def getEmployeeWithDepartmentById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(employeeService.getEmployeeById(id)).map(v => Ok(Json.toJson(v)))
  }

}

class SlickDepartmentController @Inject()(basicActionLang: BasicActionLang[DBIO, Department],
                                          departmentService: DepartmentService[DBIO],
                                          langMat: LangMat[DBIO],
                                          controllerComponents: ControllerComponents)
                                         (implicit ec: ExecutionContext) extends
  BasicActionController[DBIO, Department](basicActionLang, langMat, controllerComponents) {

  def getEmployeesByDepartmentId(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(departmentService.getEmployeesByDepartmentId(id)).map(v => Ok(Json.toJson(v)))
  }

}

class BasicActionController[F[_] : Monad, M <: TIdentity : Format] @Inject()(basicActionLang: BasicActionLang[F, M],
                                                                             langMat: LangMat[F],
                                                                             val controllerComponents: ControllerComponents)
                                                                            (implicit val ec: ExecutionContext) extends BaseController {

  def create(): Action[M] = Action.async(parse.json[M]) { request =>
    val id = basicActionLang.create(request.body)
    langMat.materialize(id).map(v => Created(Json.toJson(v)))
  }

  def update(): Action[M] = Action.async(parse.json[M]) { request =>
    val affected = basicActionLang.update(request.body)
    langMat.materialize(affected).map(v => Ok(Json.toJson(v)))
  }

  def readAll(): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(basicActionLang.readAll).map(v => Ok(Json.toJson(v)))
  }

  def readById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(basicActionLang.readById(id)).map(v => Ok(Json.toJson(v)))
  }

  def deleteById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(basicActionLang.deleteById(id)).map(v => Ok(Json.toJson(v)))
  }

}
