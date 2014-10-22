package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import actors.ConnectionActor
import play.api.libs.json.JsValue
import cc.mewa.api.Protocol.MewaMessage
import cc.mewa.api.ProtocolJson._


object WebSocketController extends Controller {

  def ws = WebSocket.acceptWithActor[MewaMessage, MewaMessage] { request => out =>
    ConnectionActor.props(out)
  }
}