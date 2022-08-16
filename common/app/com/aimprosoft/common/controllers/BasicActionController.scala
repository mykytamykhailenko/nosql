package com.aimprosoft.common.controllers

import cats.Monad
import com.aimprosoft.common.lang.MatLang
import com.aimprosoft.common.model.{Id, TIdentity}
import com.aimprosoft.common.service.TBasicService
import com.google.inject.Inject
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

case class BasicActionController[F[_] : Monad, M <: TIdentity : Format] @Inject()(basicActionLang: TBasicService[F, M],
                                                                                  langMat: MatLang[F],
                                                                                  controllerComponents: ControllerComponents)
                                                                                 (implicit val ec: ExecutionContext) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    val id = basicActionLang.create(request.body.as[M])
    langMat.materialize(id).map(v => Created(Json.toJson(v)))
  }

  def update(): Action[JsValue] = Action.async(parse.json) { request =>
    val affected = basicActionLang.update(request.body.as[M])
    langMat.materialize(affected).map(v => Ok(Json.toJson(v)))
  }

  def readAll(): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(basicActionLang.readAll()).map(v => Ok(Json.toJson(v)))
  }

  def readById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(basicActionLang.readById(id)).map(v => Ok(Json.toJson(v)))
  }

  def deleteById(id: Id): Action[AnyContent] = Action.async { _ =>
    langMat.materialize(basicActionLang.deleteById(id)).map(v => Ok(Json.toJson(v)))
  }

}
