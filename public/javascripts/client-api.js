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
		/** Set device property */
		setDeviceProperty: function(device, property, value) {
			msg = {message: "set-device-property", device: device, property: property, value: value}
	        _connection._sendMsg(msg);
		},
		/** Received set property command */
		onSetProperty: function(property, value) {},
		/** Notify devices in channel that property changed */
		notifyPropertyChanged: function(property, value) {
			msg = {message: "notify-property-changed", property: property, value: value}
	        _connection._sendMsg(msg);
		},
		/** Received notiication about property change */
		onPropertyChanged: function(device, property, value) {},
		/** Set device property */
		getDeviceProperty: function(device, property) {
			msg = {message: "get-device-property", device: device, property: property}
	        _connection._sendMsg(msg);
		},
		/** Received set property command */
		onGetProperty: function(fromDevice, property) {},
		/** Send property value to another device*/
		sendPropertyValue: function(device, property, value) {
			msg = {message: "send-property-value", toDevice: device, property: property, value: value}
	        _connection._sendMsg(msg);
		},
		/** Received set property command */
		onPropertyValue: function(fromDevice, property, value) {},
		
		/** Get list of all connected to the channel devices */
		getDevices: function() {
			msg = {message: "get-devices"}
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
	        	_connection.onError("Connection closed. Trying to reconnect in 3 seconds.");
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
		if(event.message == "joined-channel"){
			_connection.onDeviceJoinedChannel(event.device);
		}
		else if(event.message == "left-channel"){
			_connection.onDeviceLeftChannel(event.device);
		}
		else if(event.message == "set-property"){
			_connection.onSetProperty(event.property, event.value);
		}
		else if(event.message == "get-property"){
			_connection.onGetProperty(event.fromDevice, event.property);
		}
		else if(event.message == "property-value"){
			_connection.onPropertyValue(event.device, event.property, event.value);
		}
		else if(event.message == "property-changed"){
			_connection.onPropertyChanged(event.device, event.property, event.value);
		}
		else if(event.message == "devices-event"){
			_connection.onDevicesEvent(event.devices);
		}
	}
	
}
