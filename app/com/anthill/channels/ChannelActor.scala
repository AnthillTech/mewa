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
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Date



object ChannelActor {
  
  case class RegisterDevice(deviceName: String)
  case class UnRegisterDevice(deviceName: String)
  case class JoinedChannelEvent(deviceName:String, timestamp: String)
  case class LeftChannelEvent(deviceName:String, timestamp: String)
  
  case class SendToDevice(fromDevice: String, toDevice: String, message: Any, timestamp: String)
  case class Fanout(fromDevice: String, message: Any, timestamp: String)
  
  case object GetConnectedDevices 
  case class ConnectedDevices(names: Seq[String], timestamp: String)
  
}


/**
 * Channel actor
 */
class ChannelActor extends Actor with ActorLogging {

  import ChannelActor._
  
  def broadcaster(devices: Map[String,ActorRef]): Actor.Receive = {
    
    case RegisterDevice(name) =>
      val event = JoinedChannelEvent(name, timeStamp)
      devices.values foreach (_ ! event)
      context.become(broadcaster(devices + (name -> sender)))
    
    case UnRegisterDevice(name) => 
      val event = LeftChannelEvent(name, timeStamp)
      devices.filterKeys(_ != name).values foreach (_ ! event)
      context.become(broadcaster(devices - name))
    
    case msg @ SendToDevice(fromDevice, toDevice, message, ts) =>
      devices.get(toDevice) foreach (_ ! SendToDevice(msg.fromDevice, msg.toDevice, msg.message, timeStamp))

    case Fanout(fromDevice, message, ts) =>
      val event = Fanout(fromDevice, message, timeStamp)
      devices.filterKeys(_ != fromDevice).values.foreach(_ forward event)
    
    case GetConnectedDevices =>
      sender ! ConnectedDevices(devices.keys.toList, timeStamp)
  }
  
  def receive = broadcaster(Map.empty)
  
  def timeStamp: String = {
    dataFormat.format(new Date())
  }
  
  val dataFormat: SimpleDateFormat = {
    val tz = TimeZone.getTimeZone("UTC");
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    df.setTimeZone(tz);
    df
  }
}