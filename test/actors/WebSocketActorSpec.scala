package actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender


class WebSocketActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("WebSocketActorSpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  import WebSocketActor._
  
  "New socket" should {
 
    "not be connected to the channel" in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! SendToChannel("") 
      expectMsg(NotConnectedError)
    }
  }
  
  "Not connected socket" should {
 
    "throw error if got disconnect message." in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! DisconnectFromChannel
      expectMsg(NotConnectedError)
    }
  }
/*  
  "Correct channel name and password" should {
 
    "allow connection." in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! Connect("test", "test")
      expectMsg(ConnectedEvent)
    }
  }
*/ 
}