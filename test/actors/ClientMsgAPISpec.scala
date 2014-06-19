package actors

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{Json, JsSuccess}
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsSuccess

@RunWith(classOf[JUnitRunner])
class ClientMsgAPISpec extends Specification {

  import WebSocketActor._
/*  
  "Connect message" should {
    
    val json_msg = "{\"message\": \"connect\"}"
    
    "serialize to json" in {
      val msg = Connect("name", "password")
      val jsvalue = Json.toJson(msg) 
      Console.print("json: " + jsvalue)
      jsvalue must beEqualTo(json_msg)
    }

    "deserialize from json" in {
      val json_data = "{\"message\": \"connect\""

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
    }
  }
*/  
  
  "Disconnect message" should {
    
    val json_msg = "{\"message\":\"disconnect\"}"
    
    "serialize to json" in {
      val msg = Disconnect()
      val jsvalue = Json.toJson(msg).toString() 
      jsvalue must beEqualTo(json_msg)
    }

    "deserialize from json" in {
      val msg = Disconnect()
      val jsvalue = Json.toJson(msg)
      val msg2 = Json.fromJson[ClientMsg](jsvalue)
      val jsok = msg2.asInstanceOf[JsSuccess[ClientMsg]]
      jsok.get must beAnInstanceOf[Disconnect]
    }
  }
  
}