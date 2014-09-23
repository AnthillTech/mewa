# HTTP API spec v.0.1

*This protocol is still under development. It means that it can change in the future in this way, that it will break backward compatibility*

If device for some reasons can't implemented WebSockets, then it can use HTTP API to communicate over channels.
For devices which want to send and receive packates over channel it is possible to register virtual device.
This device will send all events and messages to given URL. So the real device should have its own HTTP server.

It is also possible to send events and messages without connecting to the channel. It could be usefull for devices behind NAT which
can't provide public web server.


## Connecting

### Connect device

Connect to the channel and register URL where events and messages will be send

**URL**     /api/connect
**Method**  POST  
**POST Params:**  
* channel - Channel to which this device should be connected
* password - channel password
* device - device name
* url - All events and messages will be send to this URL

Events will be send to **url** + "event".
Messages will be send to **url** + "message".

If device want to send respons to received message it can send request with body:
* messageId
* params
This message will be send as response to the device which send first message.


### Disconnect device

**URL**     /api/disconnect
**Method**  POST  
**POST Params:**  
* channel - Channel to which this device should be connected
* device - device name


## Communication

### Send event

Event can be send without connecting to the channel. 
**URL**     /api/event  
**Method**  POST  
**POST Params:**  
* channel - Channel to which event should be send
* password - channel password
* device - device which sends event
* eventId - event ID
* params - event params



