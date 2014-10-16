package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json._
import cc.mewa.channels.ChannelManagerActor
import cc.mewa.channels.ChannelActor
import cc.mewa.api.Protocol._
import akka.actor.PoisonPill


object HttpEventActor {
  def props(channel: String, password: String, device: String) = Props(new HttpEventActor(channel, password, device))
  
  case class SendEvent(eventId: String, content: String)
}

/**
 * Http event actor implementation
 */
class HttpEventActor(channel: String, password: String, device: String) extends Actor with ActorLogging {
 
  import HttpEventActor.SendEvent
  
  
  def receive: Actor.Receive = {
    
    case SendEvent(id, content) =>
      val manager = context.actorSelection("/user/channel-manager")
      manager ! ChannelManagerActor.GetChannel(channel, device, password)
      context.become(receiveEvent(id, content))
  }

  def receiveEvent(id: String, content: String): Actor.Receive = {
    
    case ChannelManagerActor.ChannelFound(channel) =>
      channel ! ChannelActor.Event(device, id, content, "")
      self ! PoisonPill
    
    case _ => self ! PoisonPill
  }
}