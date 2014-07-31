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
   *  Notify client that it was successfully connected to the channel. 
   *  JSON format: 
   *  {"message": "connected"}  
   */
  case object ConnectedEvent extends ClientMessage
  
  /** 
   *  Disconnect from channel.
   *  JSON format: 
   *  {"message": "disconnect"}  
   */
  case object DisconnectFromChannel extends ClientMessage
  /** 
   *  Notify client that it was disconnected from channel. 
   *  JSON format: 
   *  {"message": "disconnected"}  
   */
  case object DisconnectedEvent extends ClientMessage
  
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
   *  Device joined channel event
   *  JSON format: 
   *  { "message": "joined-channel", "device": "device name"}  
   */
  case class DeviceJoinedChannel(device: String) extends ClientMessage
  
  /** 
   *  Device left channel event
   *  JSON format: 
   *  { "message": "left-channel", "device": "device name"}  
   */
  case class DeviceLeftChannel(device: String) extends ClientMessage
  

  /** 
   *  Send event to the channel 
   *  JSON format: 
   *  {"message": "send-event", "id": "eventId", "params":"json with params"}  
   */
  case class SendEvent(eventId: String, params: String) extends ClientMessage
  /** 
   *  Notify client about new event send by other clients to the connected channel 
   *  JSON format: 
   *  {"message": "event", "device": "device1", "id": "eventId", "params":"json"}
   */
  case class Event(fromDevice: String, eventId: String, params: String) extends ClientMessage
  /** 
   *  Send message to specific device 
   *  JSON format: 
   *  {"message": "send-message", "device": "device1", "id": "messageId", "params":"message parameters"}
   */
  case class SendMessage(targetDevice: String, messageId: String, params: String) extends ClientMessage
  /** 
   *  Notify client about message send from other device 
   *  JSON format: 
   *  {"message": "message", "device": "source", "id": "messageId", "params":"message params"}
   */
  case class Message(fromDevice: String, messageId: String, params: String) extends ClientMessage  
  
  /** 
   *  Ask for list of all connect to the channel devices. 
   *  JSON format: 
   *  {"message": "get-devices"}  
   */
  case object GetDevices extends ClientMessage
  /** 
   *  Event with list of all connected devices 
   *  JSON format: 
   *  {"message": "devices-event", "devices":["device1", "device2"]}  
   */
  case class DevicesEvent(names: Seq[String]) extends ClientMessage

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
  implicit val msgToJson = Writes[ClientMessage]{
      
      case msg: ConnectToChannel => Json.obj( "message" -> "connect" 
                                            , "channel" -> msg.channel 
                                            , "device" -> msg.device
                                            , "password" -> msg.password )
      case DisconnectFromChannel => Json.obj("message" -> "disconnect")
      case AlreadyConnectedError => Json.obj("message" -> "already-connected-error")
      case AuthorizationError => Json.obj("message" -> "authorization-error")
      case NotConnectedError => Json.obj("message" -> "not-connected-error")
      case ConnectedEvent => Json.obj("message" -> "connected")
      case DisconnectedEvent => Json.obj("message" -> "disconnected")

      case msg:DeviceJoinedChannel => Json.obj("message" -> "joined-channel", "device" -> msg.device)                                                                                                                        
      case msg:DeviceLeftChannel => Json.obj("message" -> "left-channel", "device" -> msg.device)                                                                                                                        

      case msg: SendEvent => Json.obj( "message" -> "send-event", "id" -> msg.eventId, "params" ->msg.params )
      case msg: Event => Json.obj( "message" -> "event", "device" -> msg.fromDevice, "id" -> msg.eventId, "params" ->msg.params )
      case msg: SendMessage => Json.obj( "message" -> "send-message", "device" -> msg.targetDevice, "id" -> msg.messageId, "params" ->msg.params )
      case msg: Message => Json.obj( "message" -> "message", "device" -> msg.fromDevice, "id" -> msg.messageId, "params" ->msg.params )

      case GetDevices => Json.obj("message" -> "get-devices")
      case msg:DevicesEvent => Json.obj("message" -> "devices-event", "devices" -> msg.names)                                                                                                                        
  }

  /**
   * Create message from JSON
   */
  implicit val msgFromJson = Reads[ClientMessage]{jsval => 
    (jsval \ "message").as[String] match {
      case "connect" => connectFromJson(jsval)
      case "disconnect" => JsSuccess(DisconnectFromChannel)
      case "connected" => JsSuccess(ConnectedEvent)
      case "disconnected" => JsSuccess(DisconnectedEvent)
      case "already-connected-error" => JsSuccess(AlreadyConnectedError)
      case "authorization-error" => JsSuccess(AuthorizationError)
      case "not-connected-error" => JsSuccess(NotConnectedError)
      case "joined-channel" => joinedChannelFromJson(jsval)
      case "left-channel" => leftChannelFromJson(jsval)
      
      case "send-event" => sendEventFromJson(jsval)
      case "event" => eventFromJson(jsval)
      case "send-message" => sendMessageFromJson(jsval)
      case "message" => messageFromJson(jsval)
      
      case "get-devices" => JsSuccess(GetDevices)
      case "devices-event" => devicesEventFromJson(jsval)
      case other => JsError("Unknown client message: <" + other + ">")
    }
  }

  def connectFromJson(jsval:JsValue): JsResult[ConnectToChannel] = { 
    val channel = (jsval \ "channel").as[String]
    val device : String= (jsval \ "device").as[String]
    val password : String= (jsval \ "password").as[String]
    JsSuccess(ConnectToChannel(channel, device, password))
  }

  def joinedChannelFromJson(jsval:JsValue): JsResult[DeviceJoinedChannel] = { 
    val deviceName = (jsval \ "device").as[String]
    JsSuccess(DeviceJoinedChannel(deviceName))
  }

  def leftChannelFromJson(jsval:JsValue): JsResult[DeviceLeftChannel] = { 
    val deviceName = (jsval \ "device").as[String]
    JsSuccess(DeviceLeftChannel(deviceName))
  }

  def sendEventFromJson(jsval:JsValue): JsResult[SendEvent] = { 
    val eventId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(SendEvent(eventId, params))
  }

  def eventFromJson(jsval:JsValue): JsResult[Event] = { 
    val device : String= (jsval \ "device").as[String]
    val eventId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(Event(device, eventId, params))
  }

  def sendMessageFromJson(jsval:JsValue): JsResult[SendMessage] = { 
    val device : String= (jsval \ "device").as[String]
    val msgId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(SendMessage(device, msgId, params))
  }

  def messageFromJson(jsval:JsValue): JsResult[Message] = { 
    val device : String= (jsval \ "device").as[String]
    val msgId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(Message(device, msgId, params))
  }

  def devicesEventFromJson(jsval:JsValue): JsResult[DevicesEvent] = { 
    val deviceNames = (jsval \ "devices").as[List[String]]
    JsSuccess(DevicesEvent(deviceNames))
  }
}


/**
 * WebSocket actor implementation
 */
class WebSocketActor(socket: ActorRef) extends Actor with ActorLogging{
 
  import WebSocketActor._

  var connectedChannel : Option[ActorRef] = None
  var socketName: String = ""
  
  /** Disconnected from channel */
  def disconnected: Actor.Receive = {
    
    case SendEvent(_,_) =>
      socket ! NotConnectedError
    
    case SendMessage(_,_,_) =>
      socket ! NotConnectedError
    
    case ConnectToChannel(channel, device, password) =>
      val manager = context.actorSelection("/user/channel-manager")
      manager ! ChannelManagerActor.GetChannel(channel, device, password)
      socketName = device
      context.become(connecting())
  }

  /** Trying to connect */
  def connecting(): Actor.Receive = {
    
    case ChannelManagerActor.ChannelFound(channel) =>
      channel ! ChannelActor.RegisterDevice(socketName)
      socket ! ConnectedEvent
      connectedChannel = Some(channel)
      context.become(connected(channel))
    
    case ChannelManagerActor.AuthorizationError =>
      socket ! AuthorizationError
      context.become(disconnected)
  }

  /** Process messages while connected to the channel */
  def connected(channel: ActorRef): Actor.Receive = {
    
    case msg @ SendEvent(eventId, value) =>
      channel ! ChannelActor.Fanout(socketName, msg)

    case ChannelActor.Fanout(from, msg @ SendEvent(eventId, value)) =>
      socket ! Event(from, eventId, value)
      
    case msg @ SendMessage(targetDevice, messageId, params) =>
      channel ! ChannelActor.SendToDevice(socketName, targetDevice, msg)
            
    case ChannelActor.SendToDevice(from, toDevice, msg @ SendMessage(device, msgId, params)) =>
      socket ! Message(from, msgId, params)
      
    case GetDevices =>
      channel ! ChannelActor.GetConnectedDevices
      
    case ChannelActor.ConnectedDevices(devices) =>
      socket ! DevicesEvent(devices)
      
    case ChannelActor.JoinedChannelEvent(deviceName) =>
      socket ! DeviceJoinedChannel(deviceName)
      
    case ChannelActor.LeftChannelEvent(deviceName) =>
      socket ! DeviceLeftChannel(deviceName)
    
    case DisconnectFromChannel =>
      channel ! ChannelActor.UnRegisterDevice(socketName)
      connectedChannel = None
      context.become(disconnected)
      socket ! DisconnectedEvent
  }
  
  def receive = disconnected
  
  override def postStop = {
    connectedChannel foreach {_ ! ChannelActor.UnRegisterDevice(socketName)}
  }
}