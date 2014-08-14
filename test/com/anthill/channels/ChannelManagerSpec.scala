package com.anthill.channels

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.actor.ActorRef
import akka.actor.ActorSelection
import com.anthill.channels.ChannelManagerActor._



@RunWith(classOf[JUnitRunner])
class ChannelManagerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ChannelManagerActorSpec"))
 
  override def beforeAll {
    val channelManager = system.actorOf(ChannelManagerActor.props(None), "channel-manager")
  }
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  
  "ChannelManager" should {
 
    "refuse connection with invalid channel name" in {
      val manager = system.actorSelection("/user/channel-manager")
      manager ! GetChannel("", "", "")
      expectMsg(AuthorizationError)
    }
 
    "return new channel" in {
      val manager = system.actorSelection("/user/channel-manager")
      manager ! GetChannel("test1", "dev1", "pass1") 
      expectMsgType[ChannelFound]
    }
  }
}