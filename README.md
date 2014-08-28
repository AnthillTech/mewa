# Mewa server

## About
Mewa is server application written in Scala and Play framework. 
The server creates channels which can be accessed by devices to send and receive messages and events.
The API is based on WebSocket communication and consists of JSON structures.

## Useful links

* [WebSocket API Messages Specification](doc/API.md)
* [HTTP API Specification](doc/HttpAPI.md)
* [Discussion group](http://groups.google.com/d/forum/followit24?hl=en)


## Server API overview
Server API offer the following functionality:
* Connecting to the channel
* Send message
* Receive message
* Send event
* Receive event
* Get list of connected to the channel devices


## Instalation
* Install Java JDK if you don't already have it on the system.
* Clone application to local dysk
* Run: `activator run`
* The application will start listening on port 9000


## Redistributing
This code is distributed under BSD3 License. It may be freely redistributed, subject to the provisions of this license.
