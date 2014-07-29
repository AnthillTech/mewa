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
class DeviceJonedChannelSpec extends Specification {

  import ClientMessageSpec._

  "DeviceJonedChannel message" should {
    
    val expected = """{ "message": "joined-channel", "device": "device1"}""".replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = DeviceJoinedChannel("device1")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DeviceJoinedChannel("device1")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class DeviceLeftChannelSpec extends Specification {

  import ClientMessageSpec._

  "DeviceJonedLeft message" should {
    
    val expected = """{ "message": "left-channel", "device": "device1"}""".replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = DeviceLeftChannel("device1")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DeviceLeftChannel("device1")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class SetDevicePropertySpec extends Specification {

  import ClientMessageSpec._

  "SetDeviceProperty message" should {
    
    val expected = """{ "message": "set-device-property",  
												"device": "device1", 
												"property": "property1", 
												"value":"12"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = SetDeviceProperty("device1", "property1", "12")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = SetDeviceProperty("device1", "property1", "12")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class SetPropertySpec extends Specification {

  import ClientMessageSpec._

  "SetProperty message" should {
    
    val expected = """{ "message": "set-property",  
												"property": "property1", 
												"value":"12"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = SetPropertyEvent("property1", "12")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = SetPropertyEvent("property1", "12")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class GetDevicePropertySpec extends Specification {

  import ClientMessageSpec._

  "GetDeviceProperty message" should {
    
    val expected = """{ "message": "get-device-property",  
												"device": "device1", 
												"property": "property1"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = GetDeviceProperty("device1", "property1")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = GetDeviceProperty("device1", "property1")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class GetPropertyEventSpec extends Specification {

  import ClientMessageSpec._

  "GetProperty message" should {
    
    val expected = """{ "message": "get-property",  "fromDevice": "dev2", "property": "property1"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = GetPropertyEvent("dev2", "property1")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = GetPropertyEvent("dev2", "property1")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class SendPropertyValueSpec extends Specification {

  import ClientMessageSpec._

  "SendPropertyValue message" should {
    
    val expected = """{ "message": "send-property-value",  
												"toDevice": "device1", 
												"property": "property1", 
												"value":"12"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = SendPropertyValue("device1", "property1", "12")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = SendPropertyValue("device1", "property1", "12")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class PropertyValueSpec extends Specification {

  import ClientMessageSpec._

  "PropertyValue message" should {
    
    val expected = """{ "message": "property-value",  
												"device": "device1", 
												"property": "property1", 
												"value":"12"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = PropertyValue("device1", "property1", "12")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = PropertyValue("device1", "property1", "12")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class NotifyPropertyChangedSpec extends Specification {

  import ClientMessageSpec._

  "NotifyPropertyChanged message" should {
    
    val expected = """{ "message": "notify-property-changed", "property": "prop1", "value":"12"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = NotifyPropertyChanged("prop1", "12")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = NotifyPropertyChanged("prop1", "12")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class PropertyChangedEventSpec extends Specification {

  import ClientMessageSpec._

  "SetProperty message" should {
    
    val expected = """{ "message": "property-changed", "device": "dev1", "property": "prop1", "value":"12"}
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg :ClientMessage = PropertyChangedEvent("dev1", "prop1", "12")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = PropertyChangedEvent("dev1", "prop1", "12")
      assertFromJson(msg)
    }
  }
}


@RunWith(classOf[JUnitRunner])
class GetDevicesSpec extends Specification {

  import ClientMessageSpec._

  "GetDevices message" should {
    
    val expected = """{"message":"get-devices"}"""
    
    "serialize to json" in {
      val msg : ClientMessage = GetDevices
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

  import ClientMessageSpec._

  "DevicesEvent message" should {
    
    val expected = """{ "message":"devices-event",
												"devices": ["device1", "device2"] }
									 """.replaceAll("\\s+", "")
    
    "serialize to json" in {
      val msg : ClientMessage = DevicesEvent(List("device1", "device2"))
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DevicesEvent(List("device1", "device2"))
      assertFromJson(msg)
    }
  }
}
