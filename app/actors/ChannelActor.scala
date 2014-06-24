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
import scala.collection.Set



object ChannelActor {

  sealed trait ChannelMessage
  case object AddListener extends ChannelMessage
  case object RemoveListener extends ChannelMessage
  case class TextEvent(id:String, content:String) extends ChannelMessage
}


/**
 * Channel actor
 */
class ChannelActor() extends Actor with ActorLogging {

  import ChannelActor._
  
  def broadcaster(listeners: Set[ActorRef]): Actor.Receive = {
    
    case AddListener =>
      context.become(broadcaster(listeners + sender()))
    
    case RemoveListener => 
      context.become(broadcaster(listeners - sender()))
    
    case TextEvent(id, content) => 
      listeners.filter(_ != sender()).map(_ forward TextEvent(id, content))
  }
  
  def receive = broadcaster(Set.empty)
}