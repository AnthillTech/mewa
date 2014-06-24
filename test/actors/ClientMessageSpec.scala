package actors

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{Json, JsSuccess}
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsSuccess
import actors.WebSocketActor._

object ClientMessageSpec extends Specification{
  
  /*
   * Convert to json
   * Read from json
   * compare to original message
   */
  def assertFromJson(msg:ClientMessage) = {
      val jsvalue = Json.toJson(msg)
      val msg2 = Json.fromJson[ClientMessage](jsvalue)
      val jsok = msg2.asInstanceOf[JsSuccess[ClientMessage]]
      jsok.get must beEqualTo(msg)
  }
}

@RunWith(classOf[JUnitRunner])
class ConnectMessageSpec extends Specification {

  import ClientMessageSpec._

  "Connect message" should {
    
    val expected = """{ "message":"connect",
												"channel":"name",
												"device":"device1",
												"password":"pass" }
									 """
    
    "serialize to json" in {
      val msg :ClientMessage = ConnectToChannel("name", "device1", "pass")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected.replaceAll("\\s+", ""))
    }

    "deserialize from json" in {
      val msg = ConnectToChannel("name", "device1", "pass")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class DisconnectMessageSpec extends Specification {

  import ClientMessageSpec._

  "Disconnect message" should {
    
    val expected = """{"message":"disconnect"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = DisconnectFromChannel
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DisconnectFromChannel
      assertFromJson(msg)
    }
  }
  
}


@RunWith(classOf[JUnitRunner])
class SendToChannelSpec extends Specification {

  import ClientMessageSpec._

  "SendToChannel message" should {
    
    val expected = """{ "message":"send-to-channel",
												"event": { "id":"eventId", "content": "eventContent"} }
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = SendToChannel("eventId", "eventContent")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = SendToChannel("eventId", "eventContent")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class ConnectedEventSpec extends Specification {

  import ClientMessageSpec._

  "ConnectedEvent message" should {
    
    val expected = """{"message":"connected"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = ConnectedEvent
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = ConnectedEvent
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class DisconnectedEventSpec extends Specification {

  import ClientMessageSpec._

  "DisconnectedEvent message" should {
    
    val expected = """{"message":"disconnected"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = DisconnectedEvent
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DisconnectedEvent
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class ChannelEventSpec extends Specification {

  import ClientMessageSpec._

  "ChannelEvent message" should {
    
    val expected = """{ "message":"channel-event",
												"event": { "id":"eventId", "content": "eventContent"} }
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg : ClientMessage = ChannelEvent("eventId", "eventContent")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = ChannelEvent("eventId", "eventContent")
      assertFromJson(msg)
    }
  }
}

@RunWith(classOf[JUnitRunner])
class AlreadyConnectedErrorSpec extends Specification {

  import ClientMessageSpec._

  "AlreadyConnectedError message" should {
    
    val expected = """{"message":"already-connected-error"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = AlreadyConnectedError
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = AlreadyConnectedError
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class AuthorizationErrorSpec extends Specification {

  import ClientMessageSpec._

  "AuthorizationError message" should {
    
    val expected = """{"message":"authorization-error"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = AuthorizationError
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = AuthorizationError
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class NotConnectedErrorSpec extends Specification {

  import ClientMessageSpec._

  "NotConnectedError message" should {
    
    val expected = """{"message":"not-connected-error"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = NotConnectedError
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = NotConnectedError
      assertFromJson(msg)
    }
  }
}