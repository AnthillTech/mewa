package actors

import akka.actor.{ActorSystem, Actor, Props, ActorRef, PoisonPill}
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfterAll}
import com.anthill.channels.ChannelManagerActor
import com.anthill.channels.ChannelActor._



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
  
  "Connected socket" should {
 
    "remove its listener from channel on disconnect" in {
      val socket1 = system.actorOf(WebSocketActor.props(self))
      socket1 ! ConnectToChannel("test1", "dev1", "pass1")
      expectMsg(ConnectedEvent)
      val channel = system.actorSelection("/user/channel-manager/test1")
      channel ! AddListener
      socket1 ! DisconnectFromChannel
      expectMsgType[LeftChannelEvent]
    }
 
    "remove its listener when stopped" in {
      val socket1 = system.actorOf(WebSocketActor.props(self))
      socket1 ! ConnectToChannel("test2", "dev1", "pass1")
      expectMsg(ConnectedEvent)
      val channel = system.actorSelection("/user/channel-manager/test2")
      channel ! AddListener
      system.stop(socket1)
      expectMsgType[LeftChannelEvent]
    }
  }
}