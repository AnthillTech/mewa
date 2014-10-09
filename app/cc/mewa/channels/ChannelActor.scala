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



object ChannelActor {
  
  case class RegisterDevice(deviceName: String, acceptEvents: List[String])
  case class UnRegisterDevice(deviceName: String)
  case class JoinedChannelEvent(deviceName:String, timestamp: String)
  case class LeftChannelEvent(deviceName:String, timestamp: String)
  
  case class SendToDevice(fromDevice: String, toDevice: String, message: Any, timestamp: String)
  case class Fanout(fromDevice: String, eventId: String, content: String, timestamp: String)
  
  case object GetConnectedDevices 
  case class ConnectedDevices(names: Seq[String], timestamp: String)
  
}


case class Subscriber(device: ActorRef, eventPrefixes: Seq[String])

/**
 * Channel actor
 */
class ChannelActor extends Actor with ActorLogging {

  import ChannelActor._
  
  def broadcaster(devices: Map[String, Subscriber]): Actor.Receive = {
    
    case RegisterDevice(name, eventIds) =>
      val event = JoinedChannelEvent(name, timeStamp)
      devices.values foreach (_.device ! event)
      context.become(broadcaster(devices + (name -> Subscriber(sender, eventIds))))
    
    case UnRegisterDevice(name) => 
      val event = LeftChannelEvent(name, timeStamp)
      devices.filterKeys(_ != name).values foreach (_.device ! event)
      context.become(broadcaster(devices - name))
    
    case msg @ SendToDevice(fromDevice, toDevice, message, ts) =>
      devices.get(toDevice) foreach (_.device ! SendToDevice(msg.fromDevice, msg.toDevice, msg.message, timeStamp))

    case Fanout(fromDevice, eventId, content, ts) =>
      val event = Fanout(fromDevice, eventId, content, timeStamp)
      devices.filter(isEventListener(fromDevice, eventId)).values.foreach(_.device forward event)
    
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
  
  def isEventListener(skipDevice: String, event: String): ((String, Subscriber)) => Boolean = { item: (String, Subscriber) =>
    val (device, sub) = item
    if(device == skipDevice){
      false
    }else{
      sub.eventPrefixes.filter(event.startsWith(_)).size > 0
    }
  }
}