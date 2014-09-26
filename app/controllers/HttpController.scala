package controllers

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.Play.current
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
import actors.HttpEventActor
import actors.HttpEventActor.SendEvent
import actors.HttpActor
import actors.HttpActor.{Connect, Disconnect}


object HttpController extends Controller {

  /** Send event to the channel */
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
  

  /** Send message to the channel */
  val askForm = Form(
    tuple(
      "channel" -> text,
      "password" -> text,
      "device" -> text,
      "messageId" -> text,
      "params" -> text
    )
  )
  
  /** Send message to the device and return received response */
  def askMessage = Action { implicit request =>
    val (channel, password, device, id, params) = eventForm.bindFromRequest.get
    val actor = Akka.system.actorOf(HttpEventActor.props(channel, password, device))
    actor ! SendEvent(id, params)
    Ok("ok")
  }
  

  /** Connect persistent device to the channel.
   *  Params: 
   *  url - Messages and events will be send there
   */
  val connectForm = Form(
    tuple(
      "channel" -> text,
      "password" -> text,
      "device" -> text,
      "url" -> text
    )
  )

  def connect = Action { implicit request =>
    val (channel, password, device, url) = connectForm.bindFromRequest.get
    val actorName = channel + "-" + device
    val actor = Akka.system.actorOf(HttpActor.props(channel, password, device, url), actorName)
    actor ! Connect
    Ok("ok")
  }

  
  /** Disconnect persistent device from the channel.
   */
  val disconnectForm = Form(
    tuple(
      "channel" -> text,
      "device" -> text
    )
  )

  def disconnect = Action { implicit request =>
    val (channel, device) = disconnectForm.bindFromRequest.get
    val actorName = channel + "-" + device
    val actor = Akka.system.actorSelection("/user/" + actorName)
    actor ! Disconnect
    Ok("ok")
  }
  
  
  /** Send event to the channel */
  val logEventForm = Form(
    tuple(
      "channel" -> text,
      "device" -> text,
      "eventId" -> text,
      "params" -> text
    )
  )
  
  def logEvent = Action { implicit request =>
    val (channel, device, id, params) = logEventForm.bindFromRequest.get
    Logger.info("Received event '" + id + "' on channel '" + channel + "' from " + device)
    Ok("")
  }
  
  
  /** Send event to the channel */
  val logMessageForm = Form(
    tuple(
      "channel" -> text,
      "device" -> text,
      "messageId" -> text,
      "params" -> text
    )
  )
  
  def logMessage = Action { implicit request =>
    val (channel, device, id, params) = logMessageForm.bindFromRequest.get
    Logger.info("Received message '" + id + "' on channel '" + channel + "' from " + device)
    // Echo message
    Ok(id + "\n" + params)
  }
}