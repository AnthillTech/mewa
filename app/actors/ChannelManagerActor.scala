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
}


/**
 * Channel
 */
class ChannelManagerActor() extends Actor with ActorLogging{

  def receive = counter(1)
  
  def counter(n: Int = 1): Receive = {
    case "ok" => 
      sender() ! ("ok" + n)
      context.become(counter(n+1))
  }
}