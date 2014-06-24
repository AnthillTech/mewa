package actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender


/**
 * Correct connection params for test  spec are:
 * channel:  test
 * password: pass
 */
class WebSocketActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("WebSocketActorSpec"))
 
  override def beforeAll {
    val channelManager = system.actorOf(Props[ChannelManagerActor], "channel-manager")
  }
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  import WebSocketActor._
  
  "New socket" should {
 
    "not be connected to the channel" in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! SendToChannel("","") 
      expectMsg(NotConnectedError)
    }
  }
  
  "Not connected socket" should {
 
    "refuse connection with wrong channel name" in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! ConnectToChannel("", "", "pass")
      expectMsg(AuthorizationError)
    }
 
    "refuse connection with wrong password" in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! ConnectToChannel("test", "", "")
      expectMsg(AuthorizationError)
    }
 
    "connect to the channel" in {
      val wsActor = system.actorOf(WebSocketActor.props(self))
      wsActor ! ConnectToChannel("test1", "dev1", "pass1")
      expectMsg(ConnectedEvent)
    }
  }
}