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



@RunWith(classOf[JUnitRunner])
class ChannelManagerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
            with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ChannelManagerActorSpec"))
 
  override def beforeAll {
    val channelManager = system.actorOf(Props[ChannelManagerActor], "channel-manager")
    Console.println("channelManager: " + channelManager)
  }
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "ChannelManager" should {
 
    "be creatable" in {
      val manager = system.actorSelection("/user/channel-manager")
      Console.println("actorSelection: " + manager)
      manager ! "ok" 
      expectMsg("ok1")
    }
 
    "accessible by path" in {
      val manager = system.actorSelection("/user/channel-manager")
      manager ! "ok" 
      expectMsg("ok2")
    }
  }
}