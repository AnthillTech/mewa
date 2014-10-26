/** 
 *  Specification
 * 
 *  type Timestamp = String 	-- UTC tima as string
 *  type ChannelName = String	
 *  type Filename = String
 *  
 *  data LogFile 	-- File on the disk with log records written as JSON data
 *  data Event		-- Event which should be saved to the log file
 *
 * 	function eventReceived : Event x LogFile -> LogFile
 *  	-- Save event to the log file
 *  
 * Implementation:
 * 
 * 	function logFilename : Timestamp x ChannelName -> FileName
 * 		-- Event is saved to file: <channel>-yyyy-mm-dd.json
 * 
 * @author Krzysztof Langner    
 */
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
      val fw = new FileWriter(logFilename(timestamp, channel), true)
      try {
        val record = toJson(timestamp, channel, device, id, params)
        fw.write(record)
      }
      finally fw.close() 
  }
  
  /** Creates log filename based on timestamp and channel name */
  def logFilename(timestamp: String, channel: String): String = {
    val b = timestamp.indexOf('T')
    val day = timestamp.substring(0, b)
    logPath + channel + "-" + day +".json"
  }
  
  def toJson(timestamp: String, channel: String, device: String, id: String, params: String) = {
    "{\"ts\": \"" + timestamp + "\", \"channel\": \"" + channel + "\", \"device\": \"" + device +
    "\", \"id\": \"" + id + "\", \"params\": " + params + "}\n";
  }
}