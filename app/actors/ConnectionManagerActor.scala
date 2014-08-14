package actors

import akka.actor.{Actor, ActorRef, Props, ActorLogging}
import cc.mewa.api.ConnectionManager._



/**
 * Manages connection actors
 */
class ConnectionManagerActor() extends Actor with ActorLogging{

  def receive = {
    
    case GetConnection =>
      val connection: ActorRef = context.actorOf(ConnectionActor.props(sender))
      sender ! Connection(connection)
  }

}