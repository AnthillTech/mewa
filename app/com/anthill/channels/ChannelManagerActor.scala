package com.anthill.channels

import akka.actor.{Actor, ActorRef, Props, ActorLogging}
import dispatch._, Defaults._




object ChannelManagerActor {
  def props(authtUrl: Option[String]) = Props(new ChannelManagerActor(authtUrl))
  
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
class ChannelManagerActor(auth: Option[String]) extends Actor with ActorLogging{

  import ChannelManagerActor._
  
  def receive = {
    
    case GetChannel(channel, device, password) => 
      processGetChannel(sender, channel, device, password)
  }


  def processGetChannel(sender: ActorRef, channel: String, device: String, password: String): Unit = {
    
    auth match {
      case Some(authUrl) => 
        val authRequest = url(authUrl).POST
                            .addParameter("channel", channel)
                            .addParameter("password", password)
        for (response <- Http(authRequest OK as.String)){
          if(response == "ok")                    
            sender ! ChannelFound(getOrCreateChannel(channel))
          else
            sender ! AuthorizationError
        }
        
      case None =>
        if(isValidChannelName(channel)){
          sender ! ChannelFound(getOrCreateChannel(channel))
        }
        else{
          sender ! AuthorizationError
        }
    }
  }
  
  def isValidChannelName(name: String): Boolean = {
    name.length > 0
  }
  
  /** Find or create channel actor */
  def getOrCreateChannel(channelName: String): ActorRef = {
    context.child(channelName) match {
      case Some(child) => child
      case None => context.actorOf(Props(classOf[ChannelActor]), channelName)
    }
  }
}