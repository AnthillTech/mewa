package com.anthill.channels

import akka.actor.{ActorSystem, Actor, Props, ActorRef, ActorSelection}
import akka.testkit.TestKit
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfterAll}
import akka.testkit.ImplicitSender
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.anthill.channels.ChannelActor._
import akka.testkit.TestProbe



@RunWith(classOf[JUnitRunner])
class ChannelSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ChannelManagerActorSpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "Channel" should {
 
    "forward message to registered listeners" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
      val msg = DeviceEvent("testDevice2", "test", "my event")
      probe1.send(channel, msg)
      probe2.expectMsg(msg)
    }
 
    "not forward message to self" in {
      val channel = system.actorOf(Props[ChannelActor])
      val msg = DeviceEvent("testDevice", "test", "my event")
      channel ! RegisterDevice("testDevice")
      channel ! msg
      expectNoMsg
    }
 
    "send Join event when new actors adds listener" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
    }
 
    "send Left event when new actors removes listener" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
      probe2.send(channel, UnRegisterDevice("probe2"))
      probe1.expectMsgType[LeftChannelEvent]
    }
 
    "forward messages to target device" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
      val msg = Message("probe1", "probe2", "1", "")
      probe1.send(channel, msg)
      probe2.expectMsg(msg)
    }
  }
}