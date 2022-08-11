package com.aimprosoft.common.controllers

import cats.Monad
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.{Id, TIdentity}
import com.google.inject.Inject
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

class BasicActionController[F[_] : Monad, M <: TIdentity : Format] @Inject()(basicActionLang: BasicActionLang[F, M],
                                                                             langMat: MatLang[F],
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
