package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json._
import com.anthill.channels.ChannelManagerActor
import com.anthill.channels.ChannelActor
import cc.mewa.api.Protocol._



object ConnectionActor {
  def props(out: ActorRef) = Props(new ConnectionActor(out))
  

  /**
   * Formats WebSocket frames to be MewaMessages
   */
  implicit def jsonFrameFormatter: FrameFormatter[MewaMessage] = FrameFormatter.jsonFrame.transform(
    msg => Json.toJson(msg),
    json => Json.fromJson[MewaMessage](json).fold(
      invalid => throw new RuntimeException("Bad client message on WebSocket: " + invalid),
      valid => valid
    )
  )

  /**
   * Convert message to JSON
   */
  implicit val msgToJson = Writes[MewaMessage]{
      
      case msg: ConnectToChannel => Json.obj( "type" -> "connect" 
                                            , "channel" -> msg.channel 
                                            , "device" -> msg.device
                                            , "password" -> msg.password )
      case DisconnectFromChannel => Json.obj("type" -> "disconnect")
      case AlreadyConnectedError => Json.obj("type" -> "already-connected-error")
      case AuthorizationError => Json.obj("type" -> "authorization-error")
      case NotConnectedError => Json.obj("type" -> "not-connected-error")
      case ConnectedEvent => Json.obj("type" -> "connected")
      case DisconnectedEvent => Json.obj("type" -> "disconnected")

      case msg:DeviceJoinedChannel => Json.obj("type" -> "joined-channel", "time" -> msg.timeStamp, "device" -> msg.device)                                                                                                                        
      case msg:DeviceLeftChannel => Json.obj("type" -> "left-channel", "time" -> msg.timeStamp, "device" -> msg.device)                                                                                                                        

      case msg: SendEvent => Json.obj( "type" -> "send-event", "id" -> msg.eventId, "params" ->msg.params )
      case msg: Event => Json.obj( "type" -> "event", "time" -> msg.timeStamp, "device" -> msg.fromDevice, "id" -> msg.eventId, "params" ->msg.params )
      case msg: SendMessage => Json.obj( "type" -> "send-message", "device" -> msg.targetDevice, "id" -> msg.messageId, "params" ->msg.params )
      case msg: Message => Json.obj( "type" -> "message", "time" -> msg.timeStamp, "device" -> msg.fromDevice, "id" -> msg.messageId, "params" ->msg.params )

      case GetDevices => Json.obj("type" -> "get-devices")
      case msg:DevicesEvent => Json.obj("type" -> "devices-event", "time" -> msg.timeStamp, "devices" -> msg.names)                                                                                                                        
  }

  /**
   * Create message from JSON
   */
  implicit val msgFromJson = Reads[MewaMessage]{jsval => 
    (jsval \ "type").as[String] match {
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
    JsSuccess(DeviceJoinedChannel("", deviceName))
  }

  def leftChannelFromJson(jsval:JsValue): JsResult[DeviceLeftChannel] = { 
    val deviceName = (jsval \ "device").as[String]
    JsSuccess(DeviceLeftChannel("", deviceName))
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
    JsSuccess(Event("", device, eventId, params))
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
    JsSuccess(Message("", device, msgId, params))
  }

  def devicesEventFromJson(jsval:JsValue): JsResult[DevicesEvent] = { 
    val deviceNames = (jsval \ "devices").as[List[String]]
    JsSuccess(DevicesEvent("", deviceNames))
  }
}


/**
 * WebSocket actor implementation
 */
class ConnectionActor(socket: ActorRef) extends Actor with ActorLogging {
 
  import ConnectionActor._

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
      channel ! ChannelActor.Fanout(socketName, msg, "")

    case ChannelActor.Fanout(from, msg @ SendEvent(eventId, value), ts) =>
      socket ! Event(ts, from, eventId, value)
      
    case msg @ SendMessage(targetDevice, messageId, params) =>
      channel ! ChannelActor.SendToDevice(socketName, targetDevice, msg, "")
            
    case ChannelActor.SendToDevice(from, toDevice, msg @ SendMessage(device, msgId, params), ts) =>
      socket ! Message(ts, from, msgId, params)
      
    case GetDevices =>
      channel ! ChannelActor.GetConnectedDevices
      
    case ChannelActor.ConnectedDevices(devices, ts) =>
      socket ! DevicesEvent(ts, devices)
      
    case ChannelActor.JoinedChannelEvent(deviceName, ts) =>
      socket ! DeviceJoinedChannel(ts, deviceName)
      
    case ChannelActor.LeftChannelEvent(deviceName, ts) =>
      socket ! DeviceLeftChannel(ts, deviceName)
    
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