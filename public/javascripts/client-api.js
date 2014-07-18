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
		/** Received event from channel */
		onEvent: function(id, content) {},
		/** send event to the channel */
		sendToChannel: function(id, content) {
			msg = {message: "send-to-channel", event:{id: id, content:content}}
	        _connection._sendMsg(msg);
		},
		/** send message to device */
		sendToDevice: function(destDevice, msg, params) {
			msg = {message: "send-to-device", event:{device: destDevice, id: msg, params: params}}
	        _connection._sendMsg(msg);
		},
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
	             _sendJoinMessage()
	        }
	 
	        _connection._socket.onclose = function(){
	        	_connection.onError("Connection closed. Trying to recoonect in 3 seconds.");
				window.setTimeout(_connect,3000)
	        }          
	 
	    } catch(exception){
	        _connection.onError(exception);
	    }	
	}
	
	function _sendJoinMessage(){
		msg = {message: "connect", channel:channelName, device:deviceName, password:password};
        _connection._sendMsg(msg);
        _connection._socket.onmessage = function(resp){
        	var event = JSON.parse(resp.data);
        	if(event.message == "connected"){
        		_connection.onConnected();
        		_connection._socket.onmessage = _processMessage;
        	}
        	else{
        		_connection.onError("Connection error " + event.message);
        	}
        }
	}
	
	function _processMessage(resp){
		var event = JSON.parse(resp.data);
		if(event.message == "channel-event"){
			_connection.onEvent(event.event.id, event.event.content);
		}
		else if(event.message == "message-event"){
			_connection.onMessage(event.event.device, event.event.id, event.event.params);
		}
	}
	
}
