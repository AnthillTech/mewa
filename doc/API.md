# API Messages specification

## Connecting Protocol

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


## Attribute protocol

### Write property
```json
{ "message": "set-device-property", 
  "device": "deviceName", 
  "property": "propertyId", 
  "value":"serialized property value"}
```
Set property on device to given value. No message is returned. This message is send from device to channel.

```json
{ "message": "set-property", 
  "property": "propertyId", 
  "value":"serialized property value"}
```
Set property to given value. No message is returned. This message is send from channel to device with given property.

### Read property value
```json
{ "message": "get-device-property", 
  "device": "deviceName", 
  "property": "propertyId"}
```
Read property value from given device. Send from device to channel

```json
{ "message": "get-property", 
  "fromDevice": "deviceName", 
  "property": "propertyId"}
```
Read property value from given device. Send from channel to device

### Send property value
```json
{ "message": "send-property-value", 
  "toDevice": "deviceName", 
  "property": "propertyId",
  "value": "property value"}
```
Send property value to given device. Send from device to channel

```json
{ "message": "property-value", 
  "device": "deviceName", 
  "property": "propertyId",
  "value": "property value"}
```
Send property value to given device. Send from channel to device

### Property value
```json
{ "message": "property-value", 
  "device": "deviceName", 
  "property": "propertyId",
  "value":"serialized property value"}
```
Message with property value

### Notify that property on the device was changed
```json
{ "message": "notify-property-changed", 
  "property": "propertyId",
  "value":"serialized property value"}
```
Let channel know that property was changed

### Property changed event
```json
{ "message": "property-changed", 
  "device": "deviceName", 
  "property": "propertyId",
  "value":"serialized property value"}
```
Send information about property change to the devices.



## Discovery protocol

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


