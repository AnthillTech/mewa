# Channel server

## About
This is server application written in SCala and Play framework. 
The server creates channels which can be accessed by devices to send and receive messages and events.
The API is based on WebSocket communication and consists of JSON structures.

## Useful links

* Discussion group

## Server API specyfication
  /** 
   *  Connect client to the channel  
   *  JSON format:
   *  {"message": "connect", "channel":"channel name", "device":"device1", "password":"channel password"}
   */
  case class ConnectToChannel(channel: String, device: String, password: String) extends ClientMessage
  /** 
   *  Notify client that it was successfully connected to the channel. 
   *  JSON format: 
   *  {"message": "connected"}  
   */
  case object ConnectedEvent extends ClientMessage
  
  /** 
   *  Disconnect from channel.
   *  JSON format: 
   *  {"message": "disconnect"}  
   */
  case object DisconnectFromChannel extends ClientMessage
  /** 
   *  Notify client that it was disconnected from channel. 
   *  JSON format: 
   *  {"message": "disconnected"}  
   */
  case object DisconnectedEvent extends ClientMessage

  /** 
   *  Send event to the channel 
   *  JSON format: 
   *  {"message": "send-to-channel", "event":{"id": "eventId", "content":"event content"}}  
   */
  case class SendToChannel(eventId: String, eventContent: String) extends ClientMessage
  /** 
   *  Notify client about new event send by other clients to the connected channel 
   *  JSON format: 
   *  {"message": "channel-event", "event":{"device": "source", "id": "eventId", "content":"event content"}}
   */
  case class ChannelEvent(deviceName: String, eventId: String, eventContent: String) extends ClientMessage
  /** 
   *  Send message to specific device 
   *  JSON format: 
   *  {"message": "send-to-device", "event":{"device": "deviceName", "id": "messageId", "params":"message parameters"}}
   */
  case class SendToDevice(targetDevice: String, messageId: String, params: String) extends ClientMessage
  /** 
   *  Notify client about message send from other device 
   *  JSON format: 
   *  {"message": "message-event", "event":{"device": "source", "id": "messageId", "params":"message params"}}
   */
  case class MessageEvent(fromDevice: String, messageId: String, params: String) extends ClientMessage
  /** 
   *  Ask for list of all connect to the channel devices. 
   *  JSON format: 
   *  {"message": "get-devices"}  
   */
  case object GetDevices extends ClientMessage
  /** 
   *  Event with list of all connected devices 
   *  JSON format: 
   *  {"message": "devices-event", "devices":["device1", "device2"]}  
   */
  case class DevicesEvent(names: Seq[String]) extends ClientMessage
  
  /** 
   *  Client can be connected only to one channel at the time. 
   *  JSON format: 
   *  {"message": "already-connected-error"}  
   */
  case object AlreadyConnectedError extends ClientMessage
  /** 
   *  Wrong credentials. 
   *  JSON format: 
   *  {"message": "authorization-error"}  
   */
  case object AuthorizationError extends ClientMessage
  /** 
   *  There is no connection to the channel. 
   *  JSON format: 
   *  {"message": "not-connected-error"}  
   */
  case object NotConnectedError extends ClientMessage


## Instalation
* Install [Typesafe activator](http://www.playframework.com/download)
* Clone application to local dysk
* Run: activator run
* The application will start listening on port 9000


## Redistributing
This code is distributed under BSD3 License. It may be freely redistributed, subject to the provisions of this license.
