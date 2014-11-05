package cc.mewa.api

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{Json, JsSuccess}
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsSuccess
import cc.mewa.api.Protocol._
import cc.mewa.api.ProtocolJson._


object ProtocolJsonSpec extends Specification{
  
  /*
   * Convert to json
   * Read from json
   * compare to original message
   */
  def assertFromJson(msg:MewaMessage) = {
      val jsvalue = Json.toJson(msg)
      val msg2 = Json.fromJson[MewaMessage](jsvalue)
      val jsok = msg2.asInstanceOf[JsSuccess[MewaMessage]]
      jsok.get must beEqualTo(msg)
  }
}

@RunWith(classOf[JUnitRunner])
class ConnectMessage1Spec extends Specification {

  import ProtocolJsonSpec._

  "Connect message ver 1" should {
    
    "deserialize from json" in {
      val jmsg = Json.obj( "type" -> "connect" 
                         , "channel" -> "name" 
                         , "device" -> "device1"
                         , "password" -> "pass")
      val msg = ConnectToChannel("name", "device1", "pass", List.empty)
      val msg2 = Json.fromJson[MewaMessage](jmsg).get
      msg2 must beEqualTo(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class ConnectMessage2Spec extends Specification {

  import ProtocolJsonSpec._

  "Connect message ver 2" should {
    
    val expected = """{ "type":"connect",
												"channel":"name",
												"device":"device1",
												"password":"pass",
												"subscribe": ["ala", "ola"] }
									 """
    
    "serialize to json" in {
      val msg :MewaMessage = ConnectToChannel("name", "device1", "pass", List("ala", "ola"))
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected.replaceAll("\\s+", ""))
    }

    "deserialize from json" in {
      val msg = ConnectToChannel("name", "device1", "pass", List("ala", "ola"))
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class DisconnectMessageSpec extends Specification {

  import ProtocolJsonSpec._

  "Disconnect message" should {
    
    val expected = """{"type":"disconnect"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = DisconnectFromChannel
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
class ConnectedEventSpec extends Specification {

  import ProtocolJsonSpec._

  "ConnectedEvent message" should {
    
    val expected = """{"type":"connected"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = ConnectedEvent
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
class AlreadyConnectedErrorSpec extends Specification {

  import ProtocolJsonSpec._

  "AlreadyConnectedError message" should {
    
    val expected = """{"type":"already-connected-error"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = AlreadyConnectedError
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

  import ProtocolJsonSpec._

  "AuthorizationError message" should {
    
    val expected = """{"type":"authorization-error"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = AuthorizationError
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

  import ProtocolJsonSpec._

  "NotConnectedError message" should {
    
    val expected = """{"type":"not-connected-error"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = NotConnectedError
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = NotConnectedError
      assertFromJson(msg)
    }
  }
}

@RunWith(classOf[JUnitRunner])
class DisconnectedEventSpec extends Specification {

  import ProtocolJsonSpec._

  "DisconnectedEvent message" should {
    
    val expected = """{"type":"disconnected"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = DisconnectedEvent
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
class DeviceJonedChannelSpec extends Specification {

  import ProtocolJsonSpec._

  "DeviceJonedChannel message" should {
    
    val expected = """{ "type": "joined-channel", "time": "", "device": "device1"}""".replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg: MewaMessage = DeviceJoinedChannel("", "device1")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DeviceJoinedChannel("", "device1")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class DeviceLeftChannelSpec extends Specification {

  import ProtocolJsonSpec._

  "DeviceJonedLeft message" should {
    
    val expected = """{ "type": "left-channel", "time": "", "device": "device1"}""".replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :MewaMessage = DeviceLeftChannel("", "device1")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DeviceLeftChannel("", "device1")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class SendEventSpec extends Specification {

  import ProtocolJsonSpec._

  "SendEvent message" should {
    
    val expected = """{"type": "send-event", "id": "eventId", "params":"params1", "ack":false}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :MewaMessage = SendEvent("eventId", "params1", false)
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = SendEvent("eventId", "params1", false)
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class EventSpec extends Specification {

  import ProtocolJsonSpec._

  "Event message" should {
    
    val expected = """{"type": "event", "time": "", "device": "device1", "id": "service.event1", "params":"json"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :MewaMessage = Event("", "device1", "service.event1", "json")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = Event("", "device1", "service.event1", "json")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class SendMessageSpec extends Specification {

  import ProtocolJsonSpec._

  "SendMessage message" should {
    
    val expected = """{"type": "send-message", "device": "device1", "id": "messageId", "params":"json"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :MewaMessage = SendMessage("device1", "messageId", "json")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = SendMessage("device1", "messageId", "json")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class MessageSpec extends Specification {

  import ProtocolJsonSpec._

  "Message message" should {
    
    val expected = """{"type": "message", "time": "", "device": "source", "id": "messageId", "params":"json"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :MewaMessage = Message("", "source", "messageId", "json")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = Message("", "source", "messageId", "json")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class GetDevicesSpec extends Specification {

  import ProtocolJsonSpec._

  "GetDevices message" should {
    
    val expected = """{"type":"get-devices"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = GetDevices
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = GetDevices
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class DevicesEventSpec extends Specification {

  import ProtocolJsonSpec._

  "DevicesEvent message" should {
    
    val expected = """{ "type":"devices-event", 
												"time": "",
												"devices": ["device1", "device2"] }
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg : MewaMessage = DevicesEvent("", List("device1", "device2"))
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DevicesEvent("", List("device1", "device2"))
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class AckSpec extends Specification {

  import ProtocolJsonSpec._

  "Ack message" should {
    
    val expected = """{"type":"ack"}"""
    
    "serialize to json" in {
      val msg : MewaMessage = Ack
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = Ack
      assertFromJson(msg)
    }
  }
}
