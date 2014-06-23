package actors

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
import actors.ChannelActor._


/* Test actor for sending events */
class TestClientActor(channel: ActorRef) extends Actor{
  def receive = {
    case msg => channel ! msg
  }
}


@RunWith(classOf[JUnitRunner])
class ChannelSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ChannelManagerActorSpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "Channel" should {
 
    "forward message to registered listeners" in {
      val channel = system.actorOf(Props[ChannelActor], name="channelA")
      val testActor = system.actorOf(Props(new TestClientActor(channel)))
      val msg = Event("my event")
      channel ! AddListener
      testActor ! msg
      expectMsg(msg)
    }
 
    "not forward message to self" in {
      val channel = system.actorOf(Props[ChannelActor], name="channelB")
      val testActor = system.actorOf(Props(new TestClientActor(channel)))
      val msg = Event("my event")
      channel ! AddListener
      channel ! msg
      expectNoMsg
    }
  }
}