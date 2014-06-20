/**
 * API definition for comunication between client and server
 */
package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json._ //{__, Format, Writes, Reads, Json, JsError, JsSuccess, JsResult}
import akka.event.Logging
import play.api.libs.functional.syntax._


/** Commands send between client and socket actor. */
sealed trait ClientMessage

/** 
 *  Connect client to the channel  
 *  JSON format:
 *  {"message": "connect", "channel":"channel name", "password":"channel password"}
 */
case class ConnectToChannel(channel: String, password: String) extends ClientMessage
/** 
 *  Disconnect from channel.
 *  JSON format: 
 *  {"message": "disconnect"}  
 */
case class DisconnectFromChannel() extends ClientMessage
/** Send event to the channel */
case class SendToChannel(event: String) extends ClientMessage

/** Client was successfuk connected to the channel. */
case class ConnectedEvent() extends ClientMessage
/** client was disconnected from channel. */
case class DisconnectedEvent() extends ClientMessage

/** Client can be connected only to one channel at the time. */
case class AlreadyConnectedError() extends ClientMessage
/** There is no connection to the channel. */
case class NotConnectedError() extends ClientMessage
/** Wrong credentials. */
case class AuthorizationError() extends ClientMessage




object ClientMessage {

  /**
   * Formats WebSocket frames to be ClientMessages
   */
  implicit def jsonFrameFormatter: FrameFormatter[ClientMessage] = FrameFormatter.jsonFrame.transform(
    msg => Json.toJson(msg),
    json => Json.fromJson[ClientMessage](json).fold(
      invalid => throw new RuntimeException("Bad client message on WebSocket: " + invalid),
      valid => valid
    )
  )

  /**
   * JSON serialisers/deserialisers for the above messages
   */
  implicit def jsonFormat: Format[ClientMessage] = Format(
    (JsPath \ "message").read[String].flatMap {
      case "connect" => connectReads
      case "disconnect" => Reads(_  => JsSuccess(new DisconnectFromChannel()))
      case "already-connected-error" => Reads(_  => JsSuccess(new AlreadyConnectedError()))
      case "not-connected-error" => Reads(_  => JsSuccess(new NotConnectedError()))
      case "authorization-error" => Reads(_  => JsSuccess(new AuthorizationError()))
      case other => Reads(_ => JsError("Unknown client message: <" + other + ">"))
    },
    Writes {
      case msg: ConnectToChannel => connectWrites.writes(msg)
      case msg: DisconnectFromChannel => disconnectWrites.writes(msg)
      case msg: NotConnectedError => notConnectedWrites.writes(msg)
    }
  )

  /** Serialize Connect message */
  implicit val connectWrites = new Writes[ConnectToChannel] {
    def writes(msg: ConnectToChannel) = 
      Json.obj( "message" -> "connect" 
              , "channel" -> msg.channel 
              , "password" -> msg.password )
  }
  
  implicit val connectReads: Reads[ClientMessage] = (
      (JsPath \ "channel").read[String] and
      (JsPath \ "password").read[String] 
    )(ConnectToChannel.apply _)

    
  /** Serialize Disconnect message */
  implicit val disconnectWrites = new Writes[DisconnectFromChannel] {
    def writes(msg: DisconnectFromChannel) = Json.obj("message" -> "disconnect")
  }

    
  /** Serialize Disconnect message */
  implicit val notConnectedWrites = new Writes[NotConnectedError] {
    def writes(msg: NotConnectedError) = Json.obj("message" -> "not-connected-error")
  }
}  

