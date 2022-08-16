package com.aimprosoft.controllers

import cats.Monad
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Id, TIdentity}
import com.aimprosoft.service.TBasicService
import com.google.inject.Inject
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

case class BasicActionController[F[_] : Monad : Materializer, M <: TIdentity : Format] @Inject()(basicService: TBasicService[F, M],
                                                                                                 controllerComponents: ControllerComponents)
                                                                                                (implicit val mat: Materializer[F], ec: ExecutionContext) extends BaseController {

  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    val id = basicService.create(request.body.as[M])
    mat.materialize(id).map(v => Created(Json.toJson(v)))
  }

  def update(): Action[JsValue] = Action.async(parse.json) { request =>
    val affected = basicService.update(request.body.as[M])
    mat.materialize(affected).map(v => Ok(Json.toJson(v)))
  }

  def readAll(): Action[AnyContent] = Action.async { _ =>
    mat.materialize(basicService.readAll()).map(v => Ok(Json.toJson(v)))
  }

  def readById(id: Id): Action[AnyContent] = Action.async { _ =>
    mat.materialize(basicService.readById(id)).map(v => Ok(Json.toJson(v)))
  }

  def deleteById(id: Id): Action[AnyContent] = Action.async { _ =>
    mat.materialize(basicService.deleteById(id)).map(v => Ok(Json.toJson(v)))
  }

}
