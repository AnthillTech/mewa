# Channel API Messages specification
*rev 0.2*

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


`fq_channel_name` - fully qualified channel name
`dev_name` - device name (must be unique within the channel)
`ch_pwd` - channel access password


If the device successfully connected to the channel it will receive `connected` message. Other devices will receive `joined-channel` event from the channel announcing the arrival of a new device.
Alternatively the device may receive one of the following error messages: `already-connected-error`, `authorization-error`


### Disconnect

Disonnects a device from the channel  
**from:** any device wishing to disconnect from the channel  
**to:**   the channel  

```json
{"message": "disconnect"}

```
If the device successfully disconnected from the channel it will receive `disconnected` message. Other devices will receive `left-channel` event from the channel announcing the diconnection of a device

### Connected
Confirms that the device has successfully connected to the channel  
**from:** the channel  
**to:**   the device  

```json
{"message": "connected"}
```

### Disconnected
Confirms that the device has successfully disconnected from the channel  
**from:** the channel  
**to:**   the device  
```json
{"message": "disconnected"}
```

### Already connected error
Indicates connection failure due to existing connection from the device to another channel. A device can only be connected to one channel at a time.  
**from:** the channel  
**to:**   the device  
```json
{"message": "already-connected-error"}
```


### Authorization error
Indicates connection failure due to wrong credentials  
**from:** the channel  
**to:**   the device  
```json
{"message": "authorization-error"}
```


### Not connected error
Indicates message sending failure because the device is not connected to the channel  
**from:** the channel  
**to:**   the device  
```json
{"message": "not-connected-error"}
```

### Device joined channel event
Notifies all connected devices of the connection of a new device to the channel  
**from:** the channel  
**to:**   the device  
```json
{ "message": "joined-channel", "device": <device_name>}
```
`device_name` - name of the device who has joined the channel


### Device left channel event
Notifies all connected devices of the disconnection of another device from the channel  
**from:** the channel  
**to:**   the device  

```json
{ "message": "left-channel", "device": <device name>}
```
`device_name` - name of the device who has left the channel


## Communication

### Send event
Message used by the device to notify all other devices connected to the channel about an event  
**from:** the device  
**to:**   all devices  

```json
{ "message": "send-event", 
  "id": <event_id>, 
  "params":<json_params>}
```
`event_id` - fully qualified event identifier *see service reference for definitions*  
`json_params` - parameters of the event, expressed in JSON format  


### Event
Message received by the device when another device sends out an event notification using **send-event** message  
**from:** a device  
**to:**   all devices  

```json
{"message": "event", 
 "device": <from_device>, 
 "id":  <event_id>, 
 "params":<json_params>}
```
`from_device` - the name of the device that has sent the event message  
`event_id` - fully qualified event identifier *see service reference for definitions*  
`json_params` - parameters of the event, expressed in JSON format  


### Send message
Message used by the device to send a message (e.g. a service request) to another device connected to the channel  
**from:** the device  
**to:**   another device  

```json
{"message": "send-message", 
 "device": <to_device>, 
 "id": <message_id>,  
 "params": <json_params>}
```
`to_device` - the name of the device to which the message is directed  
`message_id` - fully qualified message identifier *see service reference for definitions*  
`json_params` - parameters of the message, expressed in JSON format  


### Message
Message received by the device when another device sends a message addressed to it using **send-message** message  
**from:** another device  
**to:**   the device  

```json
{"message": "message", 
 "device": <from_device>, 
 "id": <message_id>, 
 "params": <json_params>}
```
`from_device` - the name of the device who has sent the message to this device  
`message_id` - fully qualified message identifier *see service reference for definitions*  
`json_params` - parameters of the message, expressed in JSON format  



## Device Discovery

### GetDevices
Message used by the device to find out about the names of all other devices connected to the channel  
**from:** the device  
**to:** the channel  

```json
{"message": "get-devices"}
```


### Device discovery event
Event recived by the device, containing the list of names of all other devices connected to the channel. Sent by the channel in response to the **get-devices** message  
**from:** the channel  
**to:** the device  

```json
{ "message": "devices-event", 
  "devices": <device_name_list>}
```
`device_name_list` - list of device names expressed as JSON list (i.e. [dev_1, dev_2, dev_3....]




