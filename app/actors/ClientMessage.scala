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
case object DisconnectFromChannel extends ClientMessage
/** 
 *  Send event to the channel 
 *  JSON format: 
 *  {"message": "send-to-channel", "event":"event content"}  
 */
case class SendToChannel(event: String) extends ClientMessage

/** 
 *  Notify client that it was successfully connected to the channel. 
 *  JSON format: 
 *  {"message": "connected"}  
 */
case object ConnectedEvent extends ClientMessage
/** 
 *  Notify client that it was disconnected from channel. 
 *  JSON format: 
 *  {"message": "disconnected"}  
 */
case object DisconnectedEvent extends ClientMessage
/** 
 *  Notify client that it was successfully connected to the channel. 
 *  JSON format: 
 *  {"message": "channel-event", "event":"event content"}  
 */
case class ChannelEvent(event: String) extends ClientMessage

/** 
 *  Client can be connected only to one channel at the time. 
 *  JSON format: 
 *  {"message": "already-connected-error"}  
 */
case object AlreadyConnectedError extends ClientMessage
/** 
 *  Wrong credentials. 
 *  JSON format: 
 *  {"message": "authorization-error"}  
 */
case object AuthorizationError extends ClientMessage
/** 
 *  There is no connection to the channel. 
 *  JSON format: 
 *  {"message": "not-connected-error"}  
 */
case object NotConnectedError extends ClientMessage




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
   * Convert message to JSON
   */
  implicit val messageToJson = Writes[ClientMessage]{
      
      case msg: ConnectToChannel => Json.obj( "message" -> "connect" 
                                            , "channel" -> msg.channel 
                                            , "password" -> msg.password )
      case DisconnectFromChannel => Json.obj("message" -> "disconnect")
      case msg: SendToChannel => Json.obj( "message" -> "send-to-channel" 
                                         , "event" -> msg.event )

      case ConnectedEvent => Json.obj("message" -> "connected")
      case DisconnectedEvent => Json.obj("message" -> "disconnected")
      case msg:ChannelEvent => Json.obj( "message" -> "channel-event"
                                       , "event" -> msg.event )

      case AlreadyConnectedError => Json.obj("message" -> "already-connected-error")
      case AuthorizationError => Json.obj("message" -> "authorization-error")
      case NotConnectedError => Json.obj("message" -> "not-connected-error")
  }

  /**
   * Create message from JSON
   */
  implicit val messageFromJson = Reads[ClientMessage]{jsval => 
    (jsval \ "message").as[String] match {
      case "connect" => connectFromJson(jsval)
      case "disconnect" => JsSuccess(DisconnectFromChannel)
      case "send-to-channel" => sendToChannelFromJson(jsval)
      case "connected" => JsSuccess(ConnectedEvent)
      case "disconnected" => JsSuccess(DisconnectedEvent)
      case "channel-event" => channelEventFromJson(jsval)
      case "already-connected-error" => JsSuccess(AlreadyConnectedError)
      case "authorization-error" => JsSuccess(AuthorizationError)
      case "not-connected-error" => JsSuccess(NotConnectedError)
      case other => JsError("Unknown client message: <" + other + ">")
    }
  }

  def connectFromJson(jsval:JsValue): JsResult[ConnectToChannel] = { 
    val channel = (jsval \ "channel").as[String]
    val password : String= (jsval \ "password").as[String]
    JsSuccess(ConnectToChannel(channel, password))
  }

  def sendToChannelFromJson(jsval:JsValue): JsResult[SendToChannel] = { 
    val event = (jsval \ "event").as[String]
    JsSuccess(SendToChannel(event))
  }

  def channelEventFromJson(jsval:JsValue): JsResult[ChannelEvent] = { 
    val event = (jsval \ "event").as[String]
    JsSuccess(ChannelEvent(event))
  }
  
}  

