package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import actors.ConnectionActor._
import actors.ConnectionActor
import play.api.libs.json.JsValue
import cc.mewa.api.Protocol.MewaMessage


object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready.")).withSession(
        ("uuid" -> java.util.UUID.randomUUID.toString)
      )
  }

  def ws = WebSocket.acceptWithActor[MewaMessage, MewaMessage] { request => out =>
    ConnectionActor.props(out)
  }
  
}