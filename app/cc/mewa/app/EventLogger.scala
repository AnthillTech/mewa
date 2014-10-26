package cc.mewa.app

import akka.actor.{Actor}
import cc.mewa.api.ChannelApp._
import java.io.FileWriter


/**
 * Serialize events to the file
 */
class EventLogger(logPath: String) extends Actor{

  def receive = {
    case EventReceived(timestamp: String, channel: String, device: String, id: String, params: String) =>
      val day = getDay(timestamp)
      val filePath = logPath + channel + "-" + day +".json"
      val fw = new FileWriter(filePath, true)
      try {
        val record = toJson(timestamp, channel, device, id, params)
        fw.write(record)
      }
      finally fw.close() 
  }
  
  def getDay(ts: String): String = {
    val a = ts.lastIndexOf('-')
    val b = ts.indexOf('T')
    if(a < b) ts.substring(a, b) 
    else ""
  }
  
  def toJson(timestamp: String, channel: String, device: String, id: String, params: String) = {
    "{\"ts\": \"" + timestamp + "\", \"channel\": \"" + channel + "\", \"device\": \"" + device +
    "\", \"id\": \"" + id + "\", \"params\": " + params + "}\n";
  }
}