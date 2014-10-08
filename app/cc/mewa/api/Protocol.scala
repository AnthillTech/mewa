package cc.mewa.api

import akka.actor.ActorRef


object ConnectionManager{
  /** Get channel connection actor */
  case object GetConnection
  /** Channel connection actor */
  case class Connection(connection: ActorRef)
}


object Protocol {
  
  /** Commands send between client and socket actor. */
  sealed trait MewaMessage
  
  /** Connect client to the channel */
  case class ConnectToChannel(channel: String, device: String, password: String, subscribe: List[String]) extends MewaMessage
  /** Notify client that it was successfully connected to the channel. */
  case object ConnectedEvent extends MewaMessage
  
  /** Disconnect from channel. */
  case object DisconnectFromChannel extends MewaMessage
  /** Notify client that it was disconnected from channel. */
  case object DisconnectedEvent extends MewaMessage
  
  /** Client can be connected only to one channel at the time. */
  case object AlreadyConnectedError extends MewaMessage
  /** Wrong credentials. */
  case object AuthorizationError extends MewaMessage
  /** There is no connection to the channel. */
  case object NotConnectedError extends MewaMessage
  
  /** Device joined channel event */
  case class DeviceJoinedChannel(timeStamp: String, device: String) extends MewaMessage
  /** Device left channel event */
  case class DeviceLeftChannel(timeStamp: String, device: String) extends MewaMessage
  
  /** Send event to the channel */
  case class SendEvent(eventId: String, params: String, ack: Boolean) extends MewaMessage
  /** Notify client about new event send by other clients to the connected channel */
  case class Event(timeStamp: String, fromDevice: String, eventId: String, params: String) extends MewaMessage
  /** Send message to specific device */
  case class SendMessage(targetDevice: String, messageId: String, params: String) extends MewaMessage
  /** Notify client about message send from other device */
  case class Message(timeStamp: String, fromDevice: String, messageId: String, params: String) extends MewaMessage  
  /** Acknowledge that server received event or message */
  case object Ack extends MewaMessage  
  
  /** Ask for list of all connect to the channel devices. */
  case object GetDevices extends MewaMessage
  /** Event with list of all connected devices */
  case class DevicesEvent(timeStamp: String, names: Seq[String]) extends MewaMessage

}
