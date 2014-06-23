package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsSuccess
import akka.actor.ActorLogging



object ChannelManagerActor {
  def props() = Props(new ChannelManagerActor())
  
    /** Channel manager messages */
  sealed trait ChannelManagerMessage
  /** Get channel. Requires correct authorization data */
  case class GetChannel(channel: String, device: String, password: String) extends ChannelManagerMessage
  /** Access to channel granted */
  case class ChannelFound(channel: ActorRef) extends ChannelManagerMessage
  /** Connection to the channel refused */
  case object AuthorizationError extends ChannelManagerMessage
}


/**
 * Channel
 */
class ChannelManagerActor() extends Actor with ActorLogging{

  import ChannelManagerActor._
  
  def receive = {
    
    case GetChannel(channel, device, password) => 
      if (canAccess(channel, device, password)){
        sender() ! ChannelFound(getOrCreateUserActor(channel))
      }
      else sender() ! AuthorizationError
  }
  
  /** Check if given credentials give access to the channel */
  def canAccess(channel: String, device: String, password: String): Boolean = {
    channel.length > 0 && device.length > 0 && password.length > 0
  } 
  
  /** Find or create channel actor */
  def getOrCreateUserActor(channelName: String): ActorRef = {
    context.child(channelName) match {
      case Some(child) => child
      case None => context.actorOf(Props(classOf[ChannelActor]), channelName)
    }
  }
}