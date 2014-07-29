package actors

import akka.actor.{ActorSystem, Actor, Props, ActorRef, PoisonPill}
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfterAll}
import com.anthill.channels.ChannelManagerActor
import akka.util.Timeout
import akka.testkit.TestProbe
import scala.concurrent.duration._
import akka.actor.Identify
import akka.actor.ActorIdentity
import com.anthill.channels.ChannelActor


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
      wsActor ! SetDeviceProperty("","","") 
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
      val probe = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe.ref))
      probe.send(socket1, ConnectToChannel("test1", "dev1", "pass1"))
      probe.expectMsg(ConnectedEvent)
      val channel = system.actorSelection("/user/channel-manager/test1")
      channel ! ChannelActor.RegisterDevice("testDevice")
      probe.send(socket1, DisconnectFromChannel)
      fishForMessage() {
        case ChannelActor.LeftChannelEvent("dev1") => true
        case _ => false
      }
    }
 
    "remove its listener when stopped" in {
      val probe = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe.ref))
      probe.send(socket1, ConnectToChannel("test2", "dev1", "pass1"))
      probe.expectMsg(ConnectedEvent)
      val channel = system.actorSelection("/user/channel-manager/test2")
      channel ! ChannelActor.RegisterDevice("testDevice")
      system.stop(socket1)
      fishForMessage() {
        case ChannelActor.LeftChannelEvent("dev1") => true
        case _ => false
      }
    }
 
    "send set property event" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe1.ref))
      val socket2 = system.actorOf(WebSocketActor.props(probe2.ref))
      probe1.send(socket1, ConnectToChannel("test3", "probe1", "pass1"))
      probe2.send(socket2, ConnectToChannel("test3", "probe2", "pass1"))
      probe2.expectMsg(ConnectedEvent)
      probe1.send(socket1, SetDeviceProperty("probe2", "prop1", "12"))
      probe2.fishForMessage() {
        case SetPropertyEvent("prop1", "12") => true
        case m => false
      }
    }
 
    "fan out notification about property change" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe1.ref))
      val socket2 = system.actorOf(WebSocketActor.props(probe2.ref))
      probe1.send(socket1, ConnectToChannel("test3", "probe1", "pass1"))
      probe2.send(socket2, ConnectToChannel("test3", "probe2", "pass1"))
      probe2.expectMsg(ConnectedEvent)
      probe1.send(socket1, NotifyPropertyChanged("prop1", "12"))
      probe2.fishForMessage() {
        case PropertyChangedEvent("probe1", "prop1", "12") => true
        case m => false
      }
    }
 
    "send get property event" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe1.ref))
      val socket2 = system.actorOf(WebSocketActor.props(probe2.ref))
      probe1.send(socket1, ConnectToChannel("test3", "probe1", "pass1"))
      probe2.send(socket2, ConnectToChannel("test3", "probe2", "pass1"))
      probe2.expectMsg(ConnectedEvent)
      
      probe1.send(socket1, GetDeviceProperty("probe2", "prop1"))
      probe2.fishForMessage() {
        case GetPropertyEvent("probe1", "prop1") => true
        case m => false
      }
    }
 
    "send property value" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe1.ref))
      val socket2 = system.actorOf(WebSocketActor.props(probe2.ref))
      probe1.send(socket1, ConnectToChannel("test3", "probe1", "pass1"))
      probe2.send(socket2, ConnectToChannel("test3", "probe2", "pass1"))
      probe2.expectMsg(ConnectedEvent)
      
      probe1.send(socket1, SendPropertyValue("probe2", "prop1", "13"))
      probe2.fishForMessage() {
        case PropertyValue("probe1", "prop1", "13") => true
        case m => false
      }
    }
 
    "return list of all connected devices" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val socket1 = system.actorOf(WebSocketActor.props(probe1.ref))
      val socket2 = system.actorOf(WebSocketActor.props(probe2.ref))
      probe1.send(socket1, ConnectToChannel("test3", "probe1", "pass1"))
      probe2.send(socket2, ConnectToChannel("test3", "probe2", "pass1"))
      probe1.expectMsg(ConnectedEvent)
      probe1.send(socket1, GetDevices)
      probe1.fishForMessage() {
        case DevicesEvent(List("probe1", "probe2")) => true
        case DevicesEvent(List("probe2", "probe1")) => true
        case m =>
          println(m)
          false
      }
    }
  }
}