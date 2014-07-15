package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json._
import com.anthill.channels.ChannelManagerActor
import com.anthill.channels.ChannelActor



object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
  
  /** Commands send between client and socket actor. */
  sealed trait ClientMessage
  
  /** 
   *  Connect client to the channel  
   *  JSON format:
   *  {"message": "connect", "channel":"channel name", "device":"device1", "password":"channel password"}
   */
  case class ConnectToChannel(channel: String, device: String, password: String) extends ClientMessage
  /** 
   *  Disconnect from channel.
   *  JSON format: 
   *  {"message": "disconnect"}  
   */
  case object DisconnectFromChannel extends ClientMessage
  /** 
   *  Send event to the channel 
   *  JSON format: 
   *  {"message": "send-to-channel", "event":{"id": "eventId", "content":"event content"}}  
   */
  case class SendToChannel(eventId: String, eventContent: String) extends ClientMessage
  
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
   *  Notify client about new event send by other clients to the connected channel 
   *  JSON format: 
   *  {"message": "channel-event", "event":{"device": "source", "id": "eventId", "content":"event content"}}
   */
  case class ChannelEvent(deviceName: String, eventId: String, eventContent: String) extends ClientMessage
  
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
                                            , "device" -> msg.device
                                            , "password" -> msg.password )
      case DisconnectFromChannel => Json.obj("message" -> "disconnect")
      case msg: SendToChannel => Json.obj( "message" -> "send-to-channel" 
                                         , "event" -> Json.obj("id" -> msg.eventId, "content" -> msg.eventContent) )

      case ConnectedEvent => Json.obj("message" -> "connected")
      case DisconnectedEvent => Json.obj("message" -> "disconnected")
      case msg:ChannelEvent => Json.obj( "message" -> "channel-event"
                                       , "event" -> Json.obj( "device" -> msg.deviceName
                                                            , "id" -> msg.eventId
                                                            , "content" -> msg.eventContent) )

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
    val device : String= (jsval \ "device").as[String]
    val password : String= (jsval \ "password").as[String]
    JsSuccess(ConnectToChannel(channel, device, password))
  }

  def sendToChannelFromJson(jsval:JsValue): JsResult[SendToChannel] = { 
    val eventId = (jsval \ "event" \ "id").as[String]
    val eventContent = (jsval \ "event" \ "content").as[String]
    JsSuccess(SendToChannel(eventId, eventContent))
  }

  def channelEventFromJson(jsval:JsValue): JsResult[ChannelEvent] = { 
    val deviceName = (jsval \ "event" \ "device").as[String]
    val eventId = (jsval \ "event" \ "id").as[String]
    val eventContent = (jsval \ "event" \ "content").as[String]
    JsSuccess(ChannelEvent(deviceName, eventId, eventContent))
  }
}


/**
 * WebSocket actor implementation
 */
class WebSocketActor(socket: ActorRef) extends Actor with ActorLogging{
 
  import WebSocketActor._

  var connectedChannel : Option[ActorRef] = None
  var deviceName: String = ""
  
  /** Disconnected from channel */
  def disconnected: Actor.Receive = {
    
    case SendToChannel(eventId, eventContent) =>
      socket ! NotConnectedError
    
    case ConnectToChannel(channel, device, password) =>
      val manager = context.actorSelection("/user/channel-manager")
      manager ! ChannelManagerActor.GetChannel(channel, device, password)
      deviceName = device
      context.become(connecting())
  }

  /** Trying to connect */
  def connecting(): Actor.Receive = {
    
    case ChannelManagerActor.ChannelFound(channel) =>
      channel ! ChannelActor.RegisterDevice(deviceName)
      socket ! ConnectedEvent
      connectedChannel = Some(channel)
      context.become(connected(channel))
    
    case ChannelManagerActor.AuthorizationError =>
      socket ! AuthorizationError
      context.become(disconnected)
  }

  /** Process messages while connected to the channel */
  def connected(channel: ActorRef): Actor.Receive = {
    
    case SendToChannel(eventId, eventContent) =>
      channel ! ChannelActor.DeviceEvent(deviceName, eventId, eventContent)
      
    case ChannelActor.DeviceEvent(deviceName, eventId, eventContent) =>
      socket ! ChannelEvent(deviceName, eventId, eventContent)
      
    case ChannelActor.JoinedChannelEvent(deviceName) =>
      socket ! ChannelEvent(deviceName, "Device joined channel", "")
      
    case ChannelActor.LeftChannelEvent(deviceName) =>
      socket ! ChannelEvent(deviceName, "Device left channel", "")
    
    case DisconnectFromChannel =>
      channel ! ChannelActor.UnRegisterDevice(deviceName)
      connectedChannel = None
      context.become(disconnected)
  }
  
  def receive = disconnected
  
  override def postStop = {
    connectedChannel.map(_ ! ChannelActor.UnRegisterDevice(deviceName))
  }
}