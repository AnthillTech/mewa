package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import actors.ConnectionActor._
import actors.ConnectionActor
import play.api.libs.json.JsValue
import cc.mewa.api.Protocol.MewaMessage


object HttpController extends Controller {

  def sendEvent(channel: String) = Action { implicit request =>
    Ok("error")
  }
}