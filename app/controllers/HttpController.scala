package controllers

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.Play.current
import actors.HttpEventActor
import play.api.libs.json.JsValue
import cc.mewa.api.Protocol.MewaMessage
import play.api.data.Form
import play.api.data.Forms._
import akka.util.Timeout
import play.libs.Akka
import akka.pattern.ask
import cc.mewa.channels.ChannelManagerActor
import akka.actor.ActorRef
import akka.actor.Props
import actors.HttpEventActor.SendEvent


object HttpController extends Controller {

  val eventForm = Form(
    tuple(
      "channel" -> text,
      "password" -> text,
      "device" -> text,
      "eventId" -> text,
      "params" -> text
    )
  )

  def sendEvent = Action { implicit request =>
    val (channel, password, device, id, params) = eventForm.bindFromRequest.get
    val actor = Akka.system.actorOf(HttpEventActor.props(channel, password, device))
    actor ! SendEvent(id, params)
    Ok("ok")
  }
}