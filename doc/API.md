# Channel API Messages specification

The idea of the communication channel is central to the followit24.com service. A few simple rules apply:

* Every device that wants to exchange information using followit24.com must connect to a channel
* Channels are created by users who have accounts with followit24.com. One user account can create and manage multiple channels. 
* Every channel must be given a name when it is created. The name must be unique within the scope of the user account
* Channels are identified globally by the account name and the channel name. The pair is called fully qualified channel name
* Access to each channel is protected by a password set by the user who creates and manages the channel

```
Example of a fully qualified channel name: 

john_smith.my_home_devices
```

* Every device that connects to a channel must have a name which is unique within the scope of the channel
* Devices are identified globally by the account name, the channel name and the device name. The triplet is called fully qualified device name

```
Example of a fully qualified device name: 

john_smith.my_home_devices.hallway_switch
```



Devices connect to channels using websocket protocol. All messages passed between the device and the channel are in JSON format. Below you will find the complete reference of all the messages that constitute the channel API

Note that the channel API only defines how information is exchanged between devices and the channel but it does not specify the actual information that devices may want to sent to each other. In other words - the channel provides efficient mechanism for data exchange, but the meaning of the data exchanged is defined on a higher level - the level of services exposed by the devices to each-other. Those services are specified in a separate reference document.



## Connecting to channel

### Connect

Connects a device to the channel  
**from:** any device wishing to connect to the channel  
**to:**   the channel  


```json
{ "message" : "connect", 
  "channel" : <fq_channel_name>, 
  "device"  : <dev_name>, 
  "password": <ch_pwd> }
```

```
<fq_channel_name> - fully qualified channel name
<dev_name> - device name (must be unique within the channel)
<ch_pwd> - channel access password
```



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


