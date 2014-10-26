/** 
 *  Specification
 * 
 *  type Timestamp = String 		-- UTC tima as string
 *  type Channel = String				-- Channel name	
 *  type Device = String				-- Device name
 *  type EventId = String
 *  type Event = Data						-- Event data
 *  type Message = Data  				-- Message data
 *  type Socket = Actor					-- Device socket actor
 *  type LastEventKey = String	-- Key for event cache. <device>-<eventId> 
 *  
 *  data ConnectedDevices = Map Device Socket 	-- Devices connected to the channel
 *  data LastEvents = Map String Event					-- Cache for last events
 *
 * 	function registerDevice : ConnectedDevices x Device x [EventId] -> (ConnectedDevices, JoinedChannelEvent)
 *  	-- Connected device to the channel. If device with given name already exists then it will be overriden.
 * 	function unregisterDevice : ConnectedDevices x Device -> (ConnectedDevices, LeftChannelEvent)
 *  	-- Discconnected device from the channel. 
 * 	function getConnectedDevices : ConnectedDevices -> [Device]
 *  	-- List of connected devices
 * 	function sendMessage : ConnectedDevices x Message -> Message
 *  	-- Redirect message to target device
 * 	function sendEvent : ConnectedDevices x Event x LastEvent-> ([Event], LastEvent)
 *  	-- Redirect event to all devices except sender
 * 	function lastEvent : LastEvents x Device x EventId -> [Event]
 *  	-- Return last events by event id send be given device.
 *  
 * Invariants:
 *   * Counting RegisterDevice and UnregisterDevice events should always get correct number of connected devices
 *   * Any device when asking for connected devices should get itself on the list.
 *   
 * @author Krzysztof Langner    
 */
package cc.mewa.channels

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
import play.Logger
import cc.mewa.api.ChannelApp.EventReceived


/** Channel protocol */
object ChannelActor {
  
  case class RegisterDevice(deviceName: String, acceptEvents: List[String])
  case class UnRegisterDevice(deviceName: String)
  case class JoinedChannelEvent(deviceName:String, timestamp: String)
  case class LeftChannelEvent(deviceName:String, timestamp: String)
  case object GetConnectedDevices 
  case class ConnectedDevices(names: Seq[String], timestamp: String)

  case class SendToDevice(fromDevice: String, toDevice: String, message: Any, timestamp: String)
  case class Event(fromDevice: String, eventId: String, content: String, timestamp: String)
  
  case class GetLastEvents(device: String, prefix: String)
  case class LastEvents(events: List[Event], timeStamp: String)
  
}

/** Subscription information */
case class Subscriber(device: ActorRef, eventPrefixes: Seq[String])

/**
 * Channel actor
 */
class ChannelActor extends Actor with ActorLogging {

  import ChannelActor._
  
  def broadcaster(devices: Map[String, Subscriber], lastEvents: Map[String, Event]): Actor.Receive = {
    
    case RegisterDevice(name, eventIds) =>
      Logger.debug("Channel: " + self.path + " register device: " + name)
      val event = JoinedChannelEvent(name, timeStamp)
      devices.values foreach (_.device ! event)
      context.become(broadcaster(devices + (name -> Subscriber(sender, eventIds)), lastEvents))
    
    case UnRegisterDevice(name) =>
      Logger.debug("Channel: " + self.path + " unregister device: " + name)
      val event = LeftChannelEvent(name, timeStamp)
      devices.filterKeys(_ != name).values foreach (_.device ! event)
      context.become(broadcaster(devices - name, lastEvents))
    
    case msg @ SendToDevice(fromDevice, toDevice, message, ts) =>
      devices.get(toDevice) foreach (_.device ! SendToDevice(msg.fromDevice, msg.toDevice, msg.message, timeStamp))

    case Event(fromDevice, eventId, content, ts) =>
      val event = Event(fromDevice, eventId, content, timeStamp)
      devices.filter(isEventListener(fromDevice, eventId)).values.foreach(_.device forward event)
      val newLastEvents = saveEvent(lastEvents, event)
      val e = EventReceived(event.timestamp, self.path.name, fromDevice, eventId, content)
      context.system.eventStream.publish(e)
      context.become(broadcaster(devices, newLastEvents))
    
    case GetConnectedDevices =>
      sender ! ConnectedDevices(devices.keys.toList, timeStamp)
    
    case GetLastEvents(device, prefix) =>
      val events = selectLastEvents(lastEvents, device, prefix)
      sender ! LastEvents(events, timeStamp)
  }
  
  def receive = broadcaster(Map.empty, Map.empty)
  
  def timeStamp: String = {
    dataFormat.format(new Date())
  }
  
  val dataFormat: SimpleDateFormat = {
    val tz = TimeZone.getTimeZone("UTC");
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    df.setTimeZone(tz);
    df
  }
  
  def isEventListener(skipDevice: String, event: String): ((String, Subscriber)) => Boolean = { item: (String, Subscriber) =>
    val (device, sub) = item
    if(device == skipDevice){
      false
    }else{
      sub.eventPrefixes.filter(event.startsWith(_)).size > 0
    }
  }

  def saveEvent(lastEvents: Map[String,Event], event: Event) = {
    val key: String = event.fromDevice + "-" + event.eventId
    lastEvents + ((key, event))
  }

  def selectLastEvents(lastEvents: Map[String,Event], device: String, prefix: String): List[Event] = {
    lastEvents.values.filter { e => e.fromDevice.startsWith(device) && e.eventId.startsWith(prefix) }.toList
  }
}