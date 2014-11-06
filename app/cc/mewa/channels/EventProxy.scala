/** 
 *  Specification
 * 
 * @author Krzysztof Langner    
 */
package cc.mewa.channels

import akka.actor.{Actor, ActorRef}
import cc.mewa.api.ChannelApp._
import java.io.FileWriter


/**
 * Allows to register event listeners from other processes
 */
class EventProxy() extends Actor{

  def broadcaster(apps: Map[String, ActorRef]): Actor.Receive = {
    case event@ EventReceived(timestamp: String, channel: String, device: String, id: String, params: String) =>
      apps.values.foreach(_ ! event)
      
    case RegisterApp(name: String) =>
      context.become(broadcaster(apps + (name -> sender)))
  }
  
  def receive = broadcaster(Map.empty)
}