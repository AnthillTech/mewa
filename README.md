# Channel server

## About
This is server application written in SCala and Play framework. 
The server creates channels which can be accessed by devices to send and receive messages and events.
The API is based on WebSocket communication and consists of JSON structures.

## Useful links

* Discussion group
* Commercial support

## Server API overview
Server API offer the following functionality:
* Connecting to the channel
* Send message
* Receive message
* Send event
* Receive event
* Get list of connected to the channel devices

## API Messages specification
### Connect
```json
{ "message": "connect", 
  "channel":"channel name", 
  "device":"device1", 
  "password":"channel password" }
```
Connect device to the channel. Device can only be connected to one channel at the time. 

### Connected
```json
{"message": "connected"}  
```
Notify device that it was connected to the channel

### Disconnect
```json
{"message": "disconnect"}  
```
Disconnect device from channel

### Connected
```json
{"message": "disconnected"}  
```
Notify device that it was disconnected from the channel

### SendToChannel
```json
{ "message": "send-to-channel", 
  "event":{ "id": "eventId", 
            "content":"event content"}}  
```
Send event to all connected devices.

### Channel event
```json
{ "message": "channel-event", 
  "event":{ "device": "source", 
            "id": "eventId", 
            "content":"event content"}}
```            
Notify device about new event send by other clients to the connected channel 

### SendToDevice
```json
{ "message": "send-to-device", 
  "event":{ "device": "deviceName", 
            "id": "messageId", 
            "params":"message parameters"}}
```
Send message to specific device 

### Device message
```json
{ "message": "message-event", 
  "event":{ "device": "source", 
            "id": "messageId", 
            "params":"message params"}}
```
Notify client about message send from other device 

### GetDevices
```json
{"message": "get-devices"}  
```
Ask for list of all connect to the channel devices. 

### Device list
```json
{ "message": "devices-event", 
  "devices":["device1", "device2"]}  
```
Event with list of all connected devices 

### Already connected error
```json
{"message": "already-connected-error"}  
```
Device can be connected only to one channel at the time. 

### Authorization error
```json
{"message": "authorization-error"}  
```
Wrong credentials. 

### Not connected error
```json
{"message": "not-connected-error"}  
```
Device is not connected to the channel. 


## Instalation
* Install [Typesafe activator](http://www.playframework.com/download)
* Clone application to local dysk
* Run: activator run
* The application will start listening on port 9000


## Redistributing
This code is distributed under BSD3 License. It may be freely redistributed, subject to the provisions of this license.
