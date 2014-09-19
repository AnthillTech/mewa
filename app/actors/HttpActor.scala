package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import play.api.mvc.WebSocket.FrameFormatter
import play.api.libs.json.{__, Format, Writes, Reads, Json, JsError}
import akka.event.Logging
import play.api.libs.json._
import cc.mewa.channels.ChannelManagerActor
import cc.mewa.channels.ChannelActor
import cc.mewa.api.Protocol._
import akka.actor.PoisonPill
import cc.mewa.channels.ChannelActor.{RegisterDevice, UnRegisterDevice}
import cc.mewa.channels.ChannelActor.{Fanout, SendToDevice}
import dispatch._, Defaults._


object HttpActor {
  def props(channel: String, password: String, device: String, url: String) = Props(new HttpActor(channel, password, device, url))
  
  case object Connect
  case object Disconnect;
}

/**
 * Http event actor implementation
 */
class HttpActor(channel: String, password: String, device: String, deviceUrl: String) extends Actor with ActorLogging {
 
  import HttpActor._
  
  
  def receive: Actor.Receive = {
    
    case Connect =>
      val manager = context.actorSelection("/user/channel-manager")
      manager ! ChannelManagerActor.GetChannel(channel, device, password)
      
    case ChannelManagerActor.ChannelFound(channel) =>
      channel ! RegisterDevice(device)
      context.become(receiveConnected(channel))
  }

  def receiveConnected(channel: ActorRef): Actor.Receive = {
    
    case Disconnect =>
      channel ! UnRegisterDevice(device)
      self ! PoisonPill
      
    case ChannelActor.SendToDevice(from, toDevice, msg @ SendMessage(device, msgId, params), ts) =>
      sendMessage(from, msgId, params)

    case Fanout(fromDevice, eventId, content, ts) =>
      sendEvent(fromDevice, eventId, content)
  }

  def sendEvent(fromDevice: String, eventId: String, params: String): Unit = {
    val request = url(deviceUrl + "event").POST
                            .addParameter("channel", channel)
                            .addParameter("device", fromDevice)
                            .addParameter("eventId", eventId)
                            .addParameter("params", params)
    Http(request)
  }

  def sendMessage(fromDevice: String, msgId: String, params: String): Unit = {
    val request = url(deviceUrl + "message").POST
                            .addParameter("channel", channel)
                            .addParameter("device", fromDevice)
                            .addParameter("messageId", msgId)
                            .addParameter("params", params)
    Http(request)
  }
}