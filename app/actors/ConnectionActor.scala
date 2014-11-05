package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json._
import cc.mewa.channels.ChannelManagerActor
import cc.mewa.channels.ChannelActor
import cc.mewa.api.Protocol._
import play.Logger



object ConnectionActor {
  def props(out: ActorRef) = Props(new ConnectionActor(out))
}


/**
 * WebSocket actor implementation
 */
class ConnectionActor(socket: ActorRef) extends Actor{
 
  import ConnectionActor._

  var connectedChannel : Option[ActorRef] = None
  var socketName: String = ""
  
  /** Disconnected from channel */
  def disconnected: Actor.Receive = {
    
    case SendEvent(_,_,_) =>
      socket ! NotConnectedError
    
    case SendMessage(_,_,_) =>
      socket ! NotConnectedError
    
    case ConnectToChannel(channel, device, password, listenTo) =>
      Logger.debug("Websocket: Connecting device " + device + " to channel " + channel)
      val manager = context.actorSelection("/user/channel-manager")
      manager ! ChannelManagerActor.GetChannel(channel, device, password)
      socketName = device
      context.become(connecting(listenTo))
  }

  /** Trying to connect */
  def connecting(listenTo: List[String]): Actor.Receive = {
    
    case ChannelManagerActor.ChannelFound(channel) =>
      Logger.debug("Connecting device " + socketName)
      channel ! ChannelActor.RegisterDevice(socketName, listenTo)
      socket ! ConnectedEvent
      connectedChannel = Some(channel)
      context.become(connected(channel))
    
    case ChannelManagerActor.AuthorizationError =>
      Logger.debug("WebSocket: Authorization error " + socketName)      
      socket ! AuthorizationError
      context.become(disconnected)
  }

  /** Process messages while connected to the channel */
  def connected(channel: ActorRef): Actor.Receive = {
    
    case SendEvent(eventId, value, ack) =>
      channel ! ChannelActor.Event(socketName, eventId, value, "")
      if(ack) sender ! Ack

    case ChannelActor.Event(from, eventId, value, ts) =>
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
      Logger.debug("Websocket: Disonnecting device " + socketName)
      channel ! ChannelActor.UnRegisterDevice(socketName)
      connectedChannel = None
      socket ! DisconnectedEvent
      context.become(disconnected)
    
    case ConnectToChannel(channel, device, password, listenTo) =>
      Logger.debug("Websocket: Already connected " + device)
      socket ! AlreadyConnectedError
  }
  
  def receive = disconnected
  
  override def postStop = {
    Logger.debug("WebSocket closed for device " + socketName)
    connectedChannel foreach {_ ! ChannelActor.UnRegisterDevice(socketName)}
  }
}