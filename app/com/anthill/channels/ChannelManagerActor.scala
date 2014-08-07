package com.anthill.channels

import akka.actor.{Actor, ActorRef, Props, ActorLogging}
import dispatch._, Defaults._
import play.api.Play




object ChannelManagerActor {
  def props() = Props(new ChannelManagerActor())
  
    /** Channel manager messages */
  sealed trait ChannelManagerMessage
  /** Get channel. Requires correct authorization data */
  case class GetChannel(channel: String, device: String, password: String) extends ChannelManagerMessage
  /** Access to channel granted */
  case class ChannelFound(channel: ActorRef) extends ChannelManagerMessage
  /** Connection to the channel refused */
  case object AuthorizationError extends ChannelManagerMessage
}


/**
 * Channel
 */
class ChannelManagerActor extends Actor with ActorLogging{

  import ChannelManagerActor._
  
  def receive = {
    
    case GetChannel(channel, device, password) => 
      processGetChannel(sender, channel, device, password)
  }

  val apiAuthtUrl = Play.current.configuration.getString("auth.url").getOrElse("")

  def processGetChannel(sender: ActorRef, channel: String, device: String, password: String): Unit = {
    if(apiAuthtUrl.length > 0){
      val authRequest = url(apiAuthtUrl).POST
                          .addParameter("channel", channel)
                          .addParameter("password", password)
      for (response <- Http(authRequest OK as.String)){
        if(response == "ok")                    
          sender ! ChannelFound(getOrCreateChannel(channel))
        else
          sender ! AuthorizationError
      }
    }
    else{
      sender ! ChannelFound(getOrCreateChannel(channel))
    }
  }
  
  /** Find or create channel actor */
  def getOrCreateChannel(channelName: String): ActorRef = {
    context.child(channelName) match {
      case Some(child) => child
      case None => context.actorOf(Props(classOf[ChannelActor]), channelName)
    }
  }
}