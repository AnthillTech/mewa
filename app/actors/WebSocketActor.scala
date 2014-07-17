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
   *  Send event to the channel 
   *  JSON format: 
   *  {"message": "send-to-channel", "event":{"id": "eventId", "content":"event content"}}  
   */
  case class SendToChannel(eventId: String, eventContent: String) extends ClientMessage
  /** 
   *  Notify client about new event send by other clients to the connected channel 
   *  JSON format: 
   *  {"message": "channel-event", "event":{"device": "source", "id": "eventId", "content":"event content"}}
   */
  case class ChannelEvent(deviceName: String, eventId: String, eventContent: String) extends ClientMessage
  /** 
   *  Send message to specific device 
   *  JSON format: 
   *  {"message": "send-to-device", "event":{"device": "deviceName", "id": "messageId", "params":"message parameters"}}
   */
  case class SendToDevice(targetDevice: String, messageId: String, params: String) extends ClientMessage
  /** 
   *  Notify client about message send from other device 
   *  JSON format: 
   *  {"message": "message-event", "event":{"device": "source", "id": "messageId", "params":"message params"}}
   */
  case class MessageEvent(fromDevice: String, messageId: String, params: String) extends ClientMessage
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
   *  Ask for info about device 
   *  JSON format: 
   *  {"message": "get-device-info", "device":"device1"}  
   */
  case class GetDeviceInfo(deviceName: String) extends ClientMessage
  /** 
   *  Information about device 
   *  JSON format: 
   *  {"message": "device-info", "device":{"name":"device1", "events":["event1", "event2"], "commands":["cmd1", "cmd2"]}}  
   */
  case class DeviceInfo(deviceName: String, supportedEvents: Seq[String], supportedCommands: Seq[String]) extends ClientMessage
  
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
      case msg: SendToDevice => Json.obj( "message" -> "send-to-device" 
                                        , "event" -> Json.obj("device" -> msg.targetDevice, "id" -> msg.messageId, "params" -> msg.params) )
      case GetDevices => Json.obj("message" -> "get-devices")
      case msg:GetDeviceInfo => Json.obj("message" -> "get-device-info", "device" -> msg.deviceName)

      case ConnectedEvent => Json.obj("message" -> "connected")
      case DisconnectedEvent => Json.obj("message" -> "disconnected")
      case msg:ChannelEvent => Json.obj( "message" -> "channel-event"
                                       , "event" -> Json.obj( "device" -> msg.deviceName
                                                            , "id" -> msg.eventId
                                                            , "content" -> msg.eventContent) )
      case msg:MessageEvent => Json.obj( "message" -> "message-event"
                                       , "event" -> Json.obj( "device" -> msg.fromDevice
                                                            , "id" -> msg.messageId
                                                            , "params" -> msg.params) )
      case msg:DevicesEvent => Json.obj("message" -> "devices-event", "devices" -> msg.names)                                                                                                                        
      case msg:DeviceInfo => Json.obj( "message" -> "device-info"
                                     , "device" -> Json.obj( "name" -> msg.deviceName
                                                            , "events" -> msg.supportedEvents
                                                            , "commands" -> msg.supportedCommands) )
      

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
      case "send-to-device" => sendToDeviceFromJson(jsval)
      case "get-devices" => JsSuccess(GetDevices)
      case "get-device-info" => getDeviceInfoFromJson(jsval)
      case "connected" => JsSuccess(ConnectedEvent)
      case "disconnected" => JsSuccess(DisconnectedEvent)
      case "channel-event" => channelEventFromJson(jsval)
      case "message-event" => messageEventFromJson(jsval)
      case "devices-event" => devicesEventFromJson(jsval)
      case "device-info" => deviceInfoFromJson(jsval)
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

  def sendToDeviceFromJson(jsval:JsValue): JsResult[SendToDevice] = { 
    val device = (jsval \ "event" \ "device").as[String]
    val messageId = (jsval \ "event" \ "id").as[String]
    val params = (jsval \ "event" \ "params").as[String]
    JsSuccess(SendToDevice(device, messageId, params))
  }

  def channelEventFromJson(jsval:JsValue): JsResult[ChannelEvent] = { 
    val deviceName = (jsval \ "event" \ "device").as[String]
    val eventId = (jsval \ "event" \ "id").as[String]
    val eventContent = (jsval \ "event" \ "content").as[String]
    JsSuccess(ChannelEvent(deviceName, eventId, eventContent))
  }

  def messageEventFromJson(jsval:JsValue): JsResult[MessageEvent] = { 
    val deviceName = (jsval \ "event" \ "device").as[String]
    val msgId = (jsval \ "event" \ "id").as[String]
    val params = (jsval \ "event" \ "params").as[String]
    JsSuccess(MessageEvent(deviceName, msgId, params))
  }

  def getDeviceInfoFromJson(jsval:JsValue): JsResult[GetDeviceInfo] = { 
    val deviceName = (jsval \ "device").as[String]
    JsSuccess(GetDeviceInfo(deviceName))
  }

  def devicesEventFromJson(jsval:JsValue): JsResult[DevicesEvent] = { 
    val deviceNames = (jsval \ "devices").as[List[String]]
    JsSuccess(DevicesEvent(deviceNames))
  }

  def deviceInfoFromJson(jsval:JsValue): JsResult[DeviceInfo] = { 
    val name = (jsval \ "device" \ "name").as[String]
    val events = (jsval \ "device" \ "events").as[List[String]]
    val commands = (jsval \ "device" \ "commands").as[List[String]]
    JsSuccess(DeviceInfo(name, events, commands))
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
      
    case msg @ SendToDevice(dest, msgId, params) =>
      channel ! ChannelActor.Message(deviceName, dest, msgId, params)
      
    case ChannelActor.Message(from, dest, msgId, params) =>
      socket ! MessageEvent(from, msgId, params)
      
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
    connectedChannel foreach {_ ! ChannelActor.UnRegisterDevice(deviceName)}
  }
}