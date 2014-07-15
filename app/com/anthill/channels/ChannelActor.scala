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
  
  //type Id = String
//  case class DeviceInfo(name: String, supportedMessages: [String], supportedEvents: [String])

  case class RegisterDevice(deviceName: String)
  case class UnRegisterDevice(deviceName: String)
  case class DeviceEvent(deviceName: String, eventId:String, content:String)
  case class Message(fromDevice: String, targetDevice: String, messageId: String, params: String)
//  case object GetConnectedDevices  // Returns ConnectedDevices
//  case class ConnectedDevices(names: [String])
//  case class GetDevice(deviceName: String)
  
  case class JoinedChannelEvent(deviceName:String)
  case class LeftChannelEvent(deviceName:String)
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
    
    case event @ DeviceEvent(name, id, content) =>
      devices.filterKeys(_ != name).values.foreach(_ forward event)
    
    case event @ Message(from, target, messageId, params) =>
      devices.get(target) foreach (_ forward event)
  }
  
  def receive = broadcaster(Map.empty)
}