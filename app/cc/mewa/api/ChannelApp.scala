package cc.mewa.api

/**
 * aplication with this protocol will process events send to the channel
 */
object ChannelApp {
  
  sealed trait EventProcessor
  
  /** Channel received new event */
  case class EventReceived(timestamp: String, channel: String, device: String, id: String, params: String) extends EventProcessor
}
