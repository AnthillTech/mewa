/*
 * Module : Connection API
 * Copyright : Copyright (C) 2014 Anthill Technology
 * License : BSD3
 * author: Krzysztof Langner <klangner@gmail.com>
 * Stability : alpha
 */

/* Connect to the channel 
 * Params:
 *   url - Server URL
 *   channelName - Channel to connect to
 *   deviceName - name of device. This device needs permission to access channel
 *   password - device password
 */
function channelConnect(url, channelName, deviceName, password){
	
	var _connection = {
		_socket: null,
		/** Connected to the channel */
		onConnected: function(){},
		/** Error */
		onError: function(reason){	console.log("Error: " + reason); },
		/** Device joined */
		onDeviceJoinedChannel: function(name) {},
		/** Device left */
		onDeviceLeftChannel: function(name) {},
		/** send event to all devices */
		sendEvent: function(eventId, params) {
			msg = {type: "send-event", id: eventId, params: params}
	        _connection._sendMsg(msg);
		},
		/** Received event */
		onEvent: function(device, eventId, params) {},
		/** send event to all devices */
		sendMessage: function(device, msgId, params) {
			msg = {type: "send-message", device: device, id: msgId, params: params}
	        _connection._sendMsg(msg);
		},
		/** Received message */
		onMessage: function(device, messageId, params) {},
		/** Get list of all connected to the channel devices */
		getDevices: function() {
			msg = {type: "get-devices"}
	        _connection._sendMsg(msg);
		},
		/** Received set property command */
		onDevicesEvent: function(devices) {},
		_sendMsg: function(msg){
	        try{
	            json = JSON.stringify(msg);
	            _connection._socket.send(json);
	        } catch(exception){
	            _connection.onError(exception);
	        }
	    }
	};
	
	// Implementation
	
	_connect();
	return _connection;
	
	
	function _connect(){
		_connection._socket = new WebSocket(url);
		try{
	        _connection._socket.onopen = function(){
	             _sendJoinPacket()
	        }
	 
	        _connection._socket.onclose = function(){
	        	_connection.onError("Connection closed. Trying to reconnect in 3 seconds.");
				window.setTimeout(_connect,3000)
	        }          
	 
	    } catch(exception){
	        _connection.onError(exception);
	    }	
	}
	
	function _sendJoinPacket(){
		msg = {type: "connect", channel:channelName, device:deviceName, password:password};
        _connection._sendMsg(msg);
        _connection._socket.onmessage = function(resp){
        	var event = JSON.parse(resp.data);
        	if(event.type == "connected"){
        		_connection.onConnected();
        		_connection._socket.onmessage = _processPacket;
        	}
        	else{
        		_connection.onError("Connection error " + event.type);
        	}
        }
	}
	
	function _processPacket(resp){
		var packet = JSON.parse(resp.data);
		if(packet.type == "joined-channel"){
			_connection.onDeviceJoinedChannel(packet.device, packet.time);
		}
		else if(packet.type == "left-channel"){
			_connection.onDeviceLeftChannel(packet.device, packet.time);
		}
		else if(packet.type == "event"){
			_connection.onEvent(packet.device, packet.id, packet.params, packet.time);
		}
		else if(packet.type == "message"){
			_connection.onMessage(packet.device, packet.id, packet.params, packet.time);
		}
		else if(packet.type == "devices-event"){
			_connection.onDevicesEvent(packet.devices, packet.time);
		}
	}
	
}
