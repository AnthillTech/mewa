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

For more details check [API Messages Specification](doc/API.md)


## Instalation
* Install [Typesafe activator](http://www.playframework.com/download)
* Clone application to local dysk
* Run: activator run
* The application will start listening on port 9000


## Redistributing
This code is distributed under BSD3 License. It may be freely redistributed, subject to the provisions of this license.
