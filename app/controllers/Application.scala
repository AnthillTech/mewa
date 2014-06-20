package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import actors.WebSocketActor
import play.api.libs.json.JsValue
import actors.ClientMessage


object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready.")).withSession(
        ("uuid" -> java.util.UUID.randomUUID.toString)
      )
  }

  def ws = WebSocket.acceptWithActor[ClientMessage, ClientMessage] { request => out =>
    WebSocketActor.props(out)
  }
  
}