# Channel API spec v.0.5

*This protocol is still under development. It means that it can change in the future in this way, that it will break backward compatibility*

The idea of the communication channel is central to the followit24.com service. A few simple principles apply:

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

*Note, that the devices need not be physical entities. They may be pure software constructs. Hence a single physical device may appear as a number of devices from the perspective of the channel*

Devices connect to channels using websocket protocol and exchanges packets. All packets passed between the device and the channel are in JSON format. Below you will find the complete reference of all the packets that constitute the channel API

Note that the channel API only defines how information is exchanged between devices and the channel but it does not specify the actual information that devices may want to sent to each other. In other words - the channel provides efficient mechanism for data exchange, but the meaning of the data exchanged is defined on a higher level - the level of services exposed by the devices to each-other. Those services are specified in a separate reference document.



## Connecting to channel

### Connect

Connects a device to the channel  
**from:** any device wishing to connect to the channel  
**to:**   the channel  


```json
{ "type" : "connect", 
  "channel" : <fq_channel_name>, 
  "device"  : <dev_name>, 
  "password": <ch_pwd> }
```

`fq_channel_name ::= string` - fully qualified channel name  
`dev_name ::= string` - device name (must be unique within the channel)  
`ch_pwd ::= string` - channel access password


If the device successfully connected to the channel it will receive `connected` packet. Other devices will receive `joined-channel` event from the channel announcing the arrival of a new device.
Alternatively the device may receive one of the following error packets: `already-connected-error`, `authorization-error`


### Disconnect

Disonnects a device from the channel  
**from:** any device wishing to disconnect from the channel  
**to:**   the channel  

```json
{"type": "disconnect"}

```
If the device successfully disconnected from the channel it will receive `disconnected` packet. Other devices will receive `left-channel` event from the channel announcing the diconnection of a device

### Connected
Confirms that the device has successfully connected to the channel  
**from:** the channel  
**to:**   the device  

```json
{"type": "connected"}
```

### Disconnected
Confirms that the device has successfully disconnected from the channel  
**from:** the channel  
**to:**   the device  
```json
{"type": "disconnected"}
```

### Already connected error
Indicates connection failure due to existing connection from the device to another channel. A device can only be connected to one channel at a time.  
**from:** the channel  
**to:**   the device  
```json
{"type": "already-connected-error"}
```


### Authorization error
Indicates connection failure due to wrong credentials  
**from:** the channel  
**to:**   the device  
```json
{"type": "authorization-error"}
```


### Not connected error
Indicates packet sending failure because the device is not connected to the channel  
**from:** the channel  
**to:**   the device  
```json
{"type": "not-connected-error"}
```

### Device joined channel event
Notifies all connected devices of the connection of a new device to the channel  
**from:** the channel  
**to:**   the device  
```json
{ "type": "joined-channel", 
"device": <device_name>}
```
`device_name` - name of the device who has joined the channel


### Device left channel event
Notifies all connected devices of the disconnection of another device from the channel  
**from:** the channel  
**to:**   the device  

```json
{ "type": "left-channel", 
"device": <device name>}
```
`device_name` - name of the device who has left the channel


## Communication

### Send event
Packet used by the device to notify all other devices connected to the channel about an event  
**from:** the device  
**to:**   all devices  

```json
{ "type": "send-event", 
  "id": <fq_event_id>, 
  "params":<params>}
```
`fq_event_id ::= string` - fully qualified event identifier *see service reference for definitions*  
`params ::= string` - parameters of the event 


### Event
Packet received by the device when another device sends out an event notification using **send-event** message  
**from:** a device  
**to:**   all devices  

```json
{"type": "event", 
 "device": <from_device>, 
 "id":  <fq_event_id>, 
 "params":<params>}
```
`from_device ::= string` - the name of the device that has sent the event message  
`fq_event_id ::= string` - fully qualified event identifier *see service reference for definitions*  
`params ::= string` - parameters of the event 


### Send message
Packet used by the device to send a message (e.g. a service request) to another device connected to the channel  
**from:** the device  
**to:**   another device  

```json
{"type": "send-message", 
 "device": <to_device>, 
 "id": <fq_message_id>,  
 "params": <params>}
```
`to_device ::= string` - the name of the device to which the message is directed  
`fq_message_id ::= string` - fully qualified message identifier *see service reference for definitions*  
`params ::= string` - parameters of the message


### Message
Packet received by the device when another device sends a message addressed to it using **send-message** message  
**from:** another device  
**to:**   the device  

```json
{"type": "message", 
 "device": <from_device>, 
 "id": <fq_message_id>, 
 "params": <params>}
```
`from_device ::= string` - the name of the device who has sent the message to this device  
`fq_message_id ::= string` - fully qualified message identifier *see service reference for definitions*  
`params ::= string` - parameters of the message



## Device Discovery

### GetDevices
Packet used by the device to find out about the names of all other devices connected to the channel  
**from:** the device  
**to:** the channel  

```json
{"type": "get-devices"}
```


### Device discovery event
Packet recived by the device, containing the list of names of all other devices connected to the channel. Sent by the channel in response to the **get-devices** packet  
**from:** the channel  
**to:** the device  

```json
{ "type": "devices-event", 
  "devices": [ <deviceA>, <deviceB>,...,<deviceN> ]
}
```
`deviceA, deviceB, deviceN ::= string` - names of devices connected to the channel


*document rev 0.4*
