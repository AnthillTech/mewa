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
  
  /** Commands send between client and socket actor. */
  sealed trait ClientMsg

  /** Connect client to the channel  */
  case class Connect(channel: String, password: String) extends ClientMsg
  /** 
   *  Disconnect from channel.
   *  JSON format: 
   *  {"message": "disconnect"}  
   */
  case class Disconnect() extends ClientMsg
  /** Send event to the channel */
  case class SendMessage(event: String) extends ClientMsg
  /** There is no connection to the channel. */
  case object NotConnectedError extends ClientMsg
  /** There is no connection to the channel. */
  case object ConnectedEvent extends ClientMsg
  
  /**
   * JSON serialisers/deserialisers for the above messages
   */
  object ClientMsg {
    implicit def clientEventFormat: Format[ClientMsg] = Format(
      (__ \ "message").read[String].flatMap {
        case "disconnect" => Reads(_  => JsSuccess(new Disconnect()))
        case other => Reads(_ => JsError("Unknown client message: <" + other + ">"))
      },
      Writes {
        case msg: Disconnect => Disconnect.disconnectWrites.writes(msg)
      }
    )

    /**
     * Formats WebSocket frames to be ClientMsg.
     */
    implicit def clientEventFrameFormatter: FrameFormatter[ClientMsg] = FrameFormatter.jsonFrame.transform(
      ClientMsg => Json.toJson(ClientMsg),
      json => Json.fromJson[ClientMsg](json).fold(
        invalid => throw new RuntimeException("Bad client event on WebSocket: " + invalid),
        valid => valid
      )
    )
    
    object Disconnect {
      implicit val disconnectWrites = new Writes[Disconnect] {
        def writes(location: Disconnect) = Json.obj("message" -> "disconnect")
      }
    }    
  }  
}


/**
 * WebSocket actor implementation
 */
class WebSocketActor(socket: ActorRef) extends Actor {
  
  val log = Logging(context.system, this)
  
  import WebSocketActor._

  /** Not connected state */
  def disconnected: Actor.Receive = {
    
    case SendMessage(msg) =>
      socket ! NotConnectedError
    
    case Disconnect =>
      socket ! NotConnectedError
    
    case Connect(channel, pasword) =>
      log.info("Trying to connect to channel: " + channel)
  }

  /** Process messages while connected to the channel */
  def connected: Actor.Receive = {
    
    case SendMessage(msg) =>
      log.info("Message received: " + msg)
    
    case Disconnect =>
      log.info("disconnect")
    
    case Connect(channel, pasword) =>
      log.info("Trying to connect while alreadyy connected .")
  }
  
  def receive = disconnected
  
}