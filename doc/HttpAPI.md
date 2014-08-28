# HTTP API spec v.0.1

*This protocol is still under development. It means that it can change in the future in this way, that it will break backward compatibility*

If device wants to send only events every few secons, then instead of using WebSockets it can use HTTP API.


## Communication

### Send event
**URL**     /api/event
**Method**  POST
**POST Params:**
* channel - Channel to which event should be send
* password - channel password
* device - device which sends event
* eventId - event ID
* params - event params

