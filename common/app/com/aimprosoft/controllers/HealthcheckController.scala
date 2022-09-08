package com.aimprosoft.controllers

import com.google.inject.Inject
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, EssentialAction}


class HealthcheckController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {

  EssentialAction

  def healthCheck(): Action[AnyContent] = Action(Ok("hi"))

}
