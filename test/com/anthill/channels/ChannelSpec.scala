package com.anthill.channels

import akka.actor.{ActorSystem, Actor, Props, ActorRef, ActorSelection}
import akka.testkit.TestKit
import org.scalatest.{WordSpecLike, Matchers, BeforeAndAfterAll}
import akka.testkit.ImplicitSender
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.anthill.channels.ChannelActor._
import akka.testkit.TestProbe
import cc.mewa.api.Protocol.GetDevices



@RunWith(classOf[JUnitRunner])
class ChannelSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ChannelManagerActorSpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "Channel" should {
 
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
 
    "send message from one device to the target device" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
      val msg = SendToDevice("probe1", "probe2", "msg", "")
      probe1.send(channel, msg)
      probe2.expectMsgType[SendToDevice]
    }
 
    "fan-out message " in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
      val msg = Fanout("probe1", "msg", "")
      probe1.send(channel, msg)
      probe2.expectMsgType[Fanout]
    }
 
    "return list of all connected devices" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val channel = system.actorOf(Props[ChannelActor])
      probe1.send(channel, RegisterDevice("probe1"))
      probe2.send(channel, RegisterDevice("probe2"))
      probe1.expectMsgType[JoinedChannelEvent]
      probe1.send(channel, GetConnectedDevices)
      probe1.expectMsgType[ConnectedDevices]
    }
  }
}