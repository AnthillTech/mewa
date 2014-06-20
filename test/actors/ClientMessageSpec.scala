package actors

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{Json, JsSuccess}
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsSuccess

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
												"password":"pass" }
									 """
    
    "serialize to json" in {
      val msg :ClientMessage = ConnectToChannel("name", "pass")
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected.replaceAll("\\s+", ""))
    }

    "deserialize from json" in {
      val msg = ConnectToChannel("name", "pass")
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
      val msg : ClientMessage= DisconnectFromChannel()
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(expected)
    }

    "deserialize from json" in {
      val msg = DisconnectFromChannel()
      assertFromJson(msg)
    }
  }
  
}