# API Messages specification

## Connecting

### Connect
```json
{ "message": "connect", 
  "channel":"channel name", 
  "device":"device1", 
  "password":"channel password" }
```
Connect device to the channel. Device can only be connected to one channel at the time. 

### Disconnect
```json
{"message": "disconnect"}
```
Disconnect device from channel

### Connected
```json
{"message": "connected"}
```
Notify device that it was connected to the channel

### Disconnected
```json
{"message": "disconnected"}
```
Notify device that it was disconnected from the channel

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

### Device joined channel event
```json
{ "message": "joined-channel", "device": "device name"}
```
Notify all already connected device that new device was connected to the channel.

### Device left channel event
```json
{ "message": "left-channel", "device": "device name"}
```
Notify all connected device that one of the devices left channel.


## Communication

### Send event
```json
{ "message": "send-event", 
  "id": "eventId", 
  "params":"json with params"}
```
Send event to the channel

### Event
```json
{"message": "event", 
 "device": "device1", 
 "id": "eventId", 
 "params":"json"}
```
Notify client about new event send by other clients to the connected channel 

### Send message
```json
{"message": "send-message", 
 "device": "device1", 
 "id": "messageId", 
 "params":"message parameters"}
```
Send message to specific device 

### Message
```json
{"message": "message", 
 "device": "source", 
 "id": "messageId", 
 "params":"message params"}
```
Notify client about message send from other device 


## Discovery

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


