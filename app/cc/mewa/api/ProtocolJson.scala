package cc.mewa.api


import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import play.api.libs.json._
import cc.mewa.api.Protocol._



object ProtocolJson {

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
                                            , "password" -> msg.password
                                            , "subscribe" -> msg.subscribe)
      case DisconnectFromChannel => Json.obj("type" -> "disconnect")
      case AlreadyConnectedError => Json.obj("type" -> "already-connected-error")
      case AuthorizationError => Json.obj("type" -> "authorization-error")
      case NotConnectedError => Json.obj("type" -> "not-connected-error")
      case ConnectedEvent => Json.obj("type" -> "connected")
      case DisconnectedEvent => Json.obj("type" -> "disconnected")

      case msg:DeviceJoinedChannel => Json.obj("type" -> "joined-channel", "time" -> msg.timeStamp, "device" -> msg.device)                                                                                                                        
      case msg:DeviceLeftChannel => Json.obj("type" -> "left-channel", "time" -> msg.timeStamp, "device" -> msg.device)                                                                                                                        

      case GetDevices => Json.obj("type" -> "get-devices")
      case msg:DevicesEvent => Json.obj("type" -> "devices-event", "time" -> msg.timeStamp, "devices" -> msg.names)                                                                                                                        

      case msg: SendEvent => Json.obj( "type" -> "send-event", "id" -> msg.eventId, "params" -> msg.params, "ack" -> msg.ack )
      case msg: Event => Json.obj( "type" -> "event", "time" -> msg.timeStamp, "device" -> msg.fromDevice, "id" -> msg.eventId, "params" ->msg.params )
      case msg: SendMessage => Json.obj( "type" -> "send-message", "device" -> msg.targetDevice, "id" -> msg.messageId, "params" ->msg.params )
      case msg: Message => Json.obj( "type" -> "message", "time" -> msg.timeStamp, "device" -> msg.fromDevice, "id" -> msg.messageId, "params" ->msg.params )
      case Ack => Json.obj("type" -> "ack")

      case GetLastEvents(device, prefix) => Json.obj("type" -> "get-last-events", "device" -> device, "prefix" -> prefix)
      case msg: LastEvents => Json.obj( "type" -> "last-events"
                                      , "time" -> msg.timeStamp
                                      , "events" -> msg.events.map {e => Json.obj( "device" -> e.fromDevice
                                                                                 , "id" -> e.eventId
                                                                                 , "params" -> e.params
                                                                                 , "time" -> e.timeStamp)} )                                                                                                                        
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
      case "ack" => JsSuccess(Ack)
      
      case "get-devices" => JsSuccess(GetDevices)
      case "devices-event" => devicesEventFromJson(jsval)
      case "get-last-events" => getLastEventsFromJson(jsval)
      case "last-events" => lastEventsFromJson(jsval)
      case other => JsError("Unknown client message: <" + other + ">")
    }
  }

  def connectFromJson(jsval:JsValue): JsResult[ConnectToChannel] = { 
    val channel = (jsval \ "channel").as[String]
    val device : String= (jsval \ "device").as[String]
    val password : String= (jsval \ "password").as[String]
    val subscribe = (jsval \ "subscribe").asOpt[List[String]].getOrElse(List())
    JsSuccess(ConnectToChannel(channel, device, password, subscribe))
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
    val ack = (jsval \ "ack").asOpt[Boolean].getOrElse(false)
    JsSuccess(SendEvent(eventId, params, ack))
  }

  def eventFromJson(jsval:JsValue): JsResult[Event] = { 
    val device = (jsval \ "device").as[String]
    val eventId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(Event("", device, eventId, params))
  }

  def sendMessageFromJson(jsval:JsValue): JsResult[SendMessage] = { 
    val device = (jsval \ "device").as[String]
    val msgId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(SendMessage(device, msgId, params))
  }

  def messageFromJson(jsval:JsValue): JsResult[Message] = { 
    val device = (jsval \ "device").as[String]
    val msgId = (jsval \ "id").as[String]
    val params : String= (jsval \ "params").as[String]
    JsSuccess(Message("", device, msgId, params))
  }

  def devicesEventFromJson(jsval:JsValue): JsResult[DevicesEvent] = { 
    val deviceNames = (jsval \ "devices").as[List[String]]
    JsSuccess(DevicesEvent("", deviceNames))
  }

  def getLastEventsFromJson(jsval:JsValue): JsResult[GetLastEvents] = { 
    val device = (jsval \ "device").as[String]
    val prefix = (jsval \ "prefix").as[String]
    JsSuccess(GetLastEvents(device, prefix))
  }

  def lastEventsFromJson(jsval:JsValue): JsResult[LastEvents] = {
    val events: List[Event] = (jsval \ "events").as[List[JsValue]].map {v => 
      eventFromJson(v).getOrElse(Event("", "", "", ""))
    }
    JsSuccess(LastEvents("", events))
  }
}

