package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsSuccess



object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}


/**
 * WebSocket actor implementation
 */
class WebSocketActor(socket: ActorRef) extends Actor {
  
  val log = Logging(context.system, this)
  
  /** Not connected state */
  def disconnected: Actor.Receive = {
    
    case SendToChannel(msg) =>
      socket ! NotConnectedError
    
    case DisconnectFromChannel =>
      socket ! NotConnectedError
    
    case ConnectToChannel(channel, pasword) =>
      log.info("Trying to connect to channel: " + channel)
  }

  /** Process messages while connected to the channel */
  def connected: Actor.Receive = {
    
    case SendToChannel(msg) =>
      log.info("Message received: " + msg)
    
    case DisconnectFromChannel =>
      log.info("disconnect")
    
    case ConnectToChannel(channel, pasword) =>
      log.info("Trying to connect while alreadyy connected .")
  }
  
  def receive = disconnected
  
}