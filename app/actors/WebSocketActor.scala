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
   *  Set property value on given device. Send from client to channel.  
   *  JSON format: 
   *  { "message": "set-device-property",  "device": "deviceName", "property": "propertyId", "value":"serialized property value"}
   */
  case class SetDeviceProperty(device: String, property: String, value: String) extends ClientMessage
  /** 
   *  Set property value. Send from channel to to device
   *  JSON format: 
   *  { "message": "set-property",  "property": "propertyId", "value":"serialized property value"}
   */
  case class SetPropertyEvent(property: String, value: String) extends ClientMessage
  
  /** 
   * Notify other devices that property changed
   *  JSON format: 
   *  { "message": "notify-property-changed",  "property": "propertyId", "value":"serialized property value"}
   */
  case class NotifyPropertyChanged(property: String, value: String) extends ClientMessage
  /** 
   * Receive event about property cahgne in one of the connected devices
   *  JSON format: 
   *  { "message": "property-changed", "device": "from device", "property": "propertyId", "value":"serialized property value"}
   */
  case class PropertyChangedEvent(device: String, property: String, value: String) extends ClientMessage

  /** 
   *  Get property value from given device. Send from client to channel.  
   *  JSON format: 
   *  { "message": "get-device-property", "device": "deviceName", "property": "propertyId"}
   */
  case class GetDeviceProperty(device: String, property: String) extends ClientMessage
  /** 
   *  Get property value. Send from channel to to device
   *  JSON format: 
   *  { "message": "get-property", "fromDevice": "asking device", "property": "propertyId"}
   */
  case class GetPropertyEvent(fromDevice: String, property: String) extends ClientMessage
  /** 
   *  Passes property value.
   *  JSON format: 
   *  { "message": "send-property-value", "toDevice": "device", "property": "propertyId", "value: "value"}
   */
  case class SendPropertyValue(toDevice: String, property: String, value: String) extends ClientMessage
  /** 
   *  Passes property value.
   *  JSON format: 
   *  { "message": "property-value", "device": "property device", "property": "propertyId", "value: "value"}
   */
  case class PropertyValue(device: String, property: String, value: String) extends ClientMessage
  
  
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
  implicit val messageToJson = Writes[ClientMessage]{
      
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

      case msg: SetDeviceProperty => Json.obj( "message" -> "set-device-property", "device" -> msg.device, "property" ->msg.property, "value" -> msg.value )
      case msg: SetPropertyEvent => Json.obj( "message" -> "set-property", "property" ->msg.property, "value" -> msg.value )
      case msg: NotifyPropertyChanged => Json.obj( "message" -> "notify-property-changed", "property" -> msg.property, "value" -> msg.value )
      case msg: PropertyChangedEvent => Json.obj( "message" -> "property-changed", "device" -> msg.device, "property" -> msg.property, "value" -> msg.value )
      case msg: GetDeviceProperty => Json.obj( "message" -> "get-device-property", "device" -> msg.device, "property" -> msg.property)
      case msg: GetPropertyEvent => Json.obj( "message" -> "get-property", "fromDevice" -> msg.fromDevice, "property" -> msg.property)
      case msg: SendPropertyValue => Json.obj( "message" -> "send-property-value", "toDevice" -> msg.toDevice, "property" ->msg.property, "value" -> msg.value )
      case msg: PropertyValue => Json.obj( "message" -> "property-value", "device" -> msg.device, "property" ->msg.property, "value" -> msg.value )

      case GetDevices => Json.obj("message" -> "get-devices")
      case msg:DevicesEvent => Json.obj("message" -> "devices-event", "devices" -> msg.names)                                                                                                                        
  }

  /**
   * Create message from JSON
   */
  implicit val messageFromJson = Reads[ClientMessage]{jsval => 
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
      case "set-device-property" => setDevicePropertyFromJson(jsval)
      case "set-property" => setPropertyFromJson(jsval)
      case "notify-property-changed" => notifyPropertyChangedFromJson(jsval)
      case "property-changed" => propertyChangedFromJson(jsval)
      case "get-device-property" => getDevicePropertyFromJson(jsval)
      case "get-property" => getPropertyFromJson(jsval)
      case "send-property-value" => sendPropertyValueFromJson(jsval)
      case "property-value" => propertyValueFromJson(jsval)
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

  def setDevicePropertyFromJson(jsval:JsValue): JsResult[SetDeviceProperty] = { 
    val device = (jsval \ "device").as[String]
    val property = (jsval \ "property").as[String]
    val value = (jsval \ "value").as[String]
    JsSuccess(SetDeviceProperty(device, property, value))
  }

  def setPropertyFromJson(jsval:JsValue): JsResult[SetPropertyEvent] = { 
    val property = (jsval \ "property").as[String]
    val value = (jsval \ "value").as[String]
    JsSuccess(SetPropertyEvent(property, value))
  }

  def notifyPropertyChangedFromJson(jsval:JsValue): JsResult[NotifyPropertyChanged] = { 
    val property = (jsval \ "property").as[String]
    val value = (jsval \ "value").as[String]
    JsSuccess(NotifyPropertyChanged(property, value))
  }

  def propertyChangedFromJson(jsval:JsValue): JsResult[PropertyChangedEvent] = { 
    val device = (jsval \ "device").as[String]
    val property = (jsval \ "property").as[String]
    val value = (jsval \ "value").as[String]
    JsSuccess(PropertyChangedEvent(device, property, value))
  }

  def getDevicePropertyFromJson(jsval:JsValue): JsResult[GetDeviceProperty] = { 
    val device = (jsval \ "device").as[String]
    val property = (jsval \ "property").as[String]
    JsSuccess(GetDeviceProperty(device, property))
  }

  def getPropertyFromJson(jsval:JsValue): JsResult[GetPropertyEvent] = { 
    val device = (jsval \ "fromDevice").as[String]
    val property = (jsval \ "property").as[String]
    JsSuccess(GetPropertyEvent(device, property))
  }

  def sendPropertyValueFromJson(jsval:JsValue): JsResult[SendPropertyValue] = { 
    val device = (jsval \ "toDevice").as[String]
    val property = (jsval \ "property").as[String]
    val value = (jsval \ "value").as[String]
    JsSuccess(SendPropertyValue(device, property, value))
  }

  def propertyValueFromJson(jsval:JsValue): JsResult[PropertyValue] = { 
    val device = (jsval \ "device").as[String]
    val property = (jsval \ "property").as[String]
    val value = (jsval \ "value").as[String]
    JsSuccess(PropertyValue(device, property, value))
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
    
    case SetDeviceProperty(device, property, value) =>
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
    
    case SetDeviceProperty(device, property, value) =>
      channel ! ChannelActor.SendToDevice(socketName, device, SetPropertyEvent(property, value))
      
    case ChannelActor.SendToDevice(from, to, msg @ SetPropertyEvent(property, value)) =>
      socket ! msg
    
    case GetDeviceProperty(device, property) =>
      channel ! ChannelActor.SendToDevice(socketName, device, GetPropertyEvent(socketName, property))
      
    case ChannelActor.SendToDevice(from, to, msg @ GetPropertyEvent(fromDevice, property)) =>
      socket ! msg
    
    case SendPropertyValue(device, property, value) =>
      channel ! ChannelActor.SendToDevice(socketName, device, PropertyValue(socketName, property, value))
      
    case ChannelActor.SendToDevice(from, to, msg @ PropertyValue(fromDevice, property, value)) =>
      socket ! msg
    
    case msg @ NotifyPropertyChanged(property, value) =>
      channel ! ChannelActor.Fanout(socketName, msg)
      
    case ChannelActor.Fanout(from, msg @ NotifyPropertyChanged(property, value)) =>
      socket ! PropertyChangedEvent(from, property, value)
            
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