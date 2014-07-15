package com.anthill.channels

import akka.actor.{ActorSystem, Actor, Props, ActorRef, ActorSelection}
import akka.testkit.TestKit
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfterAll}
import akka.testkit.ImplicitSender
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.anthill.channels.ChannelActor._



@RunWith(classOf[JUnitRunner])
class ChannelSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ChannelManagerActorSpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  /* Test actor for sending events */
  class TestClientActor(channel: ActorRef) extends Actor{
    def receive = {
      case msg => channel ! msg
    }
  }

  
  "Channel" should {
 
    "forward message to registered listeners" in {
      val channel = system.actorOf(Props[ChannelActor])
      val testActor = system.actorOf(Props(new TestClientActor(channel)))
      channel ! RegisterDevice("testDevice")
      val msg = DeviceEvent("testDevice2", "test", "my event")
      testActor ! msg
      expectMsg(msg)
    }
 
    "not forward message to self" in {
      val channel = system.actorOf(Props[ChannelActor])
      val msg = DeviceEvent("testDevice", "test", "my event")
      channel ! RegisterDevice("testDevice")
      channel ! msg
      expectNoMsg
    }
 
    "send Join event when new actors adds listener" in {
      val channel = system.actorOf(Props[ChannelActor])
      val testActor = system.actorOf(Props(new TestClientActor(channel)))
      channel ! RegisterDevice("testDevice")
      testActor ! RegisterDevice("testDevice2")
      expectMsgType[JoinedChannelEvent]
    }
 
    "send Left event when new actors removes listener" in {
      val channel = system.actorOf(Props[ChannelActor])
      val testActor = system.actorOf(Props(new TestClientActor(channel)))
      channel ! RegisterDevice("testDevice")
      testActor ! RegisterDevice("testDevice2")
      expectMsgType[JoinedChannelEvent]
      testActor ! UnRegisterDevice("testDevice2")
      expectMsgType[LeftChannelEvent]
    }
 
    "send forward messages to devices" in {
      val channel = system.actorOf(Props[ChannelActor])
      val testActor = system.actorOf(Props(new TestClientActor(channel)))
      channel ! RegisterDevice("testDevice")
      testActor ! RegisterDevice("testActor")
      expectMsgType[JoinedChannelEvent]
      val msg = Message("testActor", "testDevice", "1", "")
      testActor ! msg
      expectMsg(msg)
    }
  }
}