package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsSuccess



object ChannelActor {
}


/**
 * Channel
 */
class ChannelActor() extends Actor {

  def receive: Actor.Receive = {
    case "ok" => Console.println("channel")
  }
}