package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.JsValue
import akka.event.Logging



object WebSocketActor {
 
  def props(out: ActorRef) = Props(new WebSocketActor(out))
  
  /** Commands send from client to the socket  */
  sealed trait SocketMsg

  /** Connect client to the channel  */
  case class Connect(channel: String, password: String) extends SocketMsg
  
  /** Disconnect from channel */
  case object Disconnect extends SocketMsg
  
  /** Send event to the channel */
  case class SendMessage(event: String) extends SocketMsg
  
  /** There is no connection to the channel. */
  case object NotConnectedError extends SocketMsg
  
  /** There is no connection to the channel. */
  case object ConnectedEvent extends SocketMsg
}


/**
 * WebSocket actor implementation
 */
class WebSocketActor(out: ActorRef) extends Actor {
  
  val log = Logging(context.system, this)
  
  import WebSocketActor._

  /** This state represents not connected to the channel socket */
  def beforeConnection: Actor.Receive = {
    
    case SendMessage(msg) =>
      out ! NotConnectedError
    
    case Disconnect =>
      out ! NotConnectedError
    
    case Connect(channel, pasword) =>
      log.info("Trying to connect to channel: " + channel)
  }
  
  def receive = beforeConnection
  
}