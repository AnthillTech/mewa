package com.anthill.channels

import akka.actor.{Actor, ActorRef, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsSuccess
import akka.actor.ActorLogging
import scala.collection.Set



object ChannelActor {
  
  case class RegisterDevice(deviceName: String)
  case class UnRegisterDevice(deviceName: String)
  case class JoinedChannelEvent(deviceName:String)
  case class LeftChannelEvent(deviceName:String)
  
  case class SendToDevice(fromDevice: String, toDevice: String, message: Any)
  case class Fanout(fromDevice: String, message: Any)
  
  case object GetConnectedDevices 
  case class ConnectedDevices(names: Seq[String])
  
}


/**
 * Channel actor
 */
class ChannelActor extends Actor with ActorLogging {

  import ChannelActor._
  
  def broadcaster(devices: Map[String,ActorRef]): Actor.Receive = {
    
    case RegisterDevice(name) =>
      val event = JoinedChannelEvent(name)
      devices.values foreach (_ ! event)
      context.become(broadcaster(devices + (name -> sender)))
    
    case UnRegisterDevice(name) => 
      val event = LeftChannelEvent(name)
      devices.filterKeys(_ != name).values foreach (_ ! event)
      context.become(broadcaster(devices - name))
    
    case msg @ SendToDevice(fromDevice, toDevice, message) =>
      devices.get(toDevice) foreach (_ ! SendToDevice(msg.fromDevice, msg.toDevice, msg.message))

    case event @ Fanout(fromDevice, message) =>
      devices.filterKeys(_ != fromDevice).values.foreach(_ forward event)
    
    case GetConnectedDevices =>
      sender ! ConnectedDevices(devices.keys.toList)
  }
  
  def receive = broadcaster(Map.empty)
}