var EJSS_INPUT_OUTPUT = EJSS_INPUT_OUTPUT || {};

EJSS_INPUT_OUTPUT.ejsSClientSimulation = function (model, serviceUrl, fixedUrl, processCommandFunction) {
	  var self = EJSS_INPUT_OUTPUT.websocket(serviceUrl, fixedUrl,function(input) { 	  
          // Override super processInput function
		  // console.log ("input = "+input);

	      if (input.charAt(0)=='_') {
		    var message = JSON.parse(input.substring(1));
		    if (message._command) {
		      if (processDefaultAPI(message)) return; // Standard API message
		      // call user-defined processing function
			  // console.log ("Command = "+message._command);
			  return processCommandFunction (message._command, message);
		    }
	   	  }
		  // Otherwise
	      processCommandFunction(input);
		});
	  
	  var mIsServerPlaying = false;
	  var mConnectionListListener = null;

	  self.isServerPlaying = function() { return mIsServerPlaying; }

	  self.isServerPaused  = function() { return !mIsServerPlaying; }
	  
	  // ---------------------------------------
	  // EjsS input API
	  //---------------------------------------


	  function processDefaultAPI(message) {
		  var command = message._command;
		  if (command.charAt(0)!=='_') return false;
		  switch (command) {
		    case "_isPlaying" : mIsServerPlaying = true;  model.update(); return true;  
		    case "_isPaused"  : mIsServerPlaying = false; model.update(); return true;  

		    case "_play"  : model._play();  return true;  
		    case "_pause" : model._pause(); model.update(); return true;  
		    case "_step"  : model._step();  return true;  
		    case "_reset" : model._reset(); return true;
		    
		    case "_update"       : model.update();       return true;  
		    case "_initialize"   : model.initialize();   return true;  
		    case "_resetSolvers" : if (_model.resetSolvers) model.resetSolvers(); return true;  
		   
		    case "_alert"  : alert(message.arg1);    return true;  
		    case "_println": console.log (message.arg1); return true;
		    
		    case "_connectionList" : if (mConnectionListListener) mConnectionListListener(message.list); return true;
		  }
		  return false;
	  }
	  
	  // ---------------------------------------
	  // EjsS output API
	  //---------------------------------------

    /**
     * Creates a JSON string from an object
     */
    self.toJSON = function(data) {
    	return JSON.stringify(data);
    }

    /**
     * Returns an object from a JSON string
     */
    self.fromJSON = function(data) {
    	return JSON.parse(data);
    }

    self.sendCommand = function(keyword, data) {
      // console.log ("Sending command "+keyword+" with data:"+data);
	  if (typeof data == "undefined") return self.sendMessage(keyword);
	  var message = { "_command" : keyword, "_data" : data };
	  return self.sendMessage("_" + JSON.stringify(message));
	};
		  
	self.server_play  = function() { self.sendCommand("_play",null); }
	self.server_pause = function() { self.sendCommand("_pause",null); }
	self.server_step  = function() { self.sendCommand("_step",null); }
	self.server_reset = function() { self.sendCommand("_reset",null); }
	
	self.server_update       = function() { self.sendCommand("_update",null); }
	self.server_initialize   = function() { self.sendCommand("_initialize",null); }
	self.server_resetSolvers = function() { self.sendCommand("_resetSolvers",null); }

	self.setID  = function(anId) { self.sendCommand("_setID",{ "id" : anId }); }

	self.getConnectionList = function(listener) {
		mConnectionListListener = listener;
        self.sendCommand("_sendConnectionList",null);
    }

	// ---------------------------------------
	// utils
	//---------------------------------------

	/*
	 * Parses the search string of the page, if any, and returns the desired server URL and portnumber
	 * To specify a server and/or port load the HTML page as http://wahtever/page.html?url=aURL & portnumber=aPort
	 * returns an object describing the server { url, port} 
	*/
	self.getServiceInformation = function() {
      var serverInfo = { url : "", port : 0 , found : false };
      var search = location.search;
	  if ((typeof search === "undefined") || search.trim().length<=0) return serverInfo;
	  search = search.trim();
	  if (search[0]==='?') search = search.substring(1);

	  var entries = search.split('&');
	  for (var i = 0; i < entries.length; i++) {
	    var pair = entries[i].split('=');
	    switch(pair[0].trim()) {
	      case "url" :
	      case "server" : serverInfo.url = decodeURIComponent(pair[1].trim()); serverInfo.found=true; break;
	      case "port" :
	      case "portnumber" : serverInfo.port = parseInt(pair[1].trim()); serverInfo.found=true; break;
	      default : serverInfo[pair[0].trim()] = pair[1].trim(); break;
	    }
	  }
	  return serverInfo;
	}

	/*
	 * Parses the search string of the page, if any, and returns true if there is any server information there
	 * To specify a server and/or port load the HTML page as http://wahtever/page.html?url=aURL & portnumber=aPort
	 * returns true if there is any server information
	*/
	self.isServiceSpecifiedByPage = function() {
	  return self.getServiceInformation().found;
	}

	/*
	 * Returns a serviceProvides string that can be called to run a jar
	 * @param portnumber Optional: a default port number in case the page does not specifiy it. Default = 8800
	 * @param jarFile Optional: the name of the jarFile to run as service. Default = "ejs_model_ClassServer.jar"
	 * @param modelServiceName Optional: the name of the string that identifies, in the server, 
	 * The jarfile must be in the folder under a "model_service" subfolder, and TWO LEVELS UP the HTML page 
	 */
	self.getServiceProvider = function(portnumber,jarFile, modelServiceName) {
      var DEFAULT_PORT  = (typeof portnumber       !== 'undefined') ? portnumber : 8800;
	  var JAR_FILE      = (typeof jarFile          !== 'undefined') ? jarFile          : "ejs_model_ClassServer.jar";
	  var MODEL_SERVICE = (typeof modelServiceName !== 'undefined') ? modelServiceName : "model_service";
	  
	  var serverInfo = self.getServiceInformation();
	  if (!serverInfo.found) return "";
	  var index;
	  var serviceProvider;
	  
	  if (serverInfo.url.length>0) { // The page specified a URL
	    if (serverInfo.url.indexOf("//")<0) serverInfo.url = "http://"+serverInfo.url;
	    // Add the broker
	    if      (serverInfo.url.endsWith("/"+MODEL_SERVICE+"/")) serviceProvider = serverInfo.url+"broker.php?";
	    else if (serverInfo.url.endsWith("/"+MODEL_SERVICE))     serviceProvider = serverInfo.url+"/broker.php?";
	    else serviceProvider = serverInfo.url + "/"+MODEL_SERVICE+"/broker.php?";
	  }
	  else { // the page did not specify a URL, so take the URL from the page itself
	    var url = location.href;
	    index = url.lastIndexOf("/"+MODEL_SERVICE+"/");
	    if (index>0) serviceProvider = url.substring(0,index) + "/"+MODEL_SERVICE+"/broker.php?";
	    else serviceProvider = "http://10.1.1.1/model_service/broker.php?"; // Try the Raspberry PI
	  }
	  
	  // First add the port
	  if (serverInfo.port) serviceProvider += "port="+serverInfo.port;
	  else serviceProvider += "port="+DEFAULT_PORT;

	  // Now add the address for the jar file
	  var address = location.href;
	  // remove the search
	  index = address.lastIndexOf("?");
	  if (index>0) address = address.substring(0,index); // Sure this is the case
	  // remove the part up to the "model_service" 
	  index = address.lastIndexOf("/"+MODEL_SERVICE+"/");
	  if (index>0) address = address.substring(index+MODEL_SERVICE.length+2);
	  // Now, go two levels up
	  index = address.lastIndexOf("/");
	  if (index>0) { // Sure this is the case
	    address = address.substring(0,index);
	    index = address.lastIndexOf("/"); // go up one more level, if there is one
	    if (index>0) address = address.substring(0,index);
	    serviceProvider += "&model="+address+"/"+JAR_FILE;
	  }
	  else { // fallback, just in case (but VERY unlikely)
	    serviceProvider += "&model=ClassService/ejs_model_ClassServer.jar"; // The Raspberry PI server is configured like this
	  }
	  if (serverInfo.sudo) serviceProvider += "&sudo=true";
	  
	  return serviceProvider;
	}
	
	// ---------------------------------------
	// final start up
	//---------------------------------------

	return self;
}

EJSS_INPUT_OUTPUT.websocket = function (serviceUrl, fixedUrl, processInputFunction) {
  var self = {};
  
  var mWebsocket;
  var mMustWait = 30;
  var mOpenTry = 0;  
  var mConnected = false;
  var mShouldReconnect = true;
  
  self.isConnected = function() {
  	return mConnected;
  };

  self.setWaitTime = function(seconds) { mMustWait = seconds; }

  /**
   * Start a WS client listening to the given WS server 
   * @param {String} url The url of the WS server
   */
  self.start = function(url) {
    try {
      mWebsocket = new WebSocket(url);
      console.log('Connecting... (readyState ' + mWebsocket.readyState + ') to '+url);
      mWebsocket.onopen = function(message) {
        mOpenTry = 0;
        mConnected = true;
    	mShouldReconnect = true;
    	if (self.onConnectionOpened) self.onConnectionOpened(message);
    	console.log("Openhd Event: " + message.type + " - Message: " + message.data);
      };
      mWebsocket.onclose = function(evt) {
        mConnected = false;
        if (self.onConnectionClosed) self.onConnectionClosed(evt);
        if (mShouldReconnect) {
      	  if (mMustWait<=0 || mOpenTry < mMustWait) {
      		window.setTimeout(function(){ self.start(url); }, 1000); // try to connect again in 1000 mseg
      		mOpenTry++;
      	  }
        }
        console.log("Connection closed: " + evt.type + " - Message: " + evt.data);
      };
      mWebsocket.onerror = function(evt) {
        mConnected = false;
        console.log("Error Event: " + evt.type + " - Message: " + evt.data);
      };
      mWebsocket.onmessage = function(evt) {
        processInputFunction(evt.data);
      };
    } 
    catch(exception) {
      console.log(exception);
    }
  };

  /**
   * Call a service that (optionally) starts a WS server and returns the url. 
   * Then, start a WS client listening to that url. 
   * @param {String} url The url of the service that will start the WS server
   */
  self.startFromService= function(url) {
  	if (mConnected) return;
    // run server model first
    var xmlhttp = new XMLHttpRequest();
    // send command to start the service and get the socket url
    xmlhttp.open("GET", url, true);

    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        var response = JSON.parse(xmlhttp.responseText);
        console.log("Server responded with service available at : "+response.webserver);
        self.start(response.webserver);
      }
      else console.log("WARNING: Service provider did not respond!:"+url);
    };
    xmlhttp.send();
  };

  self.stop = function() {
    try { 
      mShouldReconnect = false;
      mWebsocket.close(); 
      mConnected = false;
    } 
    catch(exception) {
      console.log(exception);
    }
  };

  self.sendMessage = function (message) {
	if (!mConnected) return false;
    mWebsocket.send(message);
    return true;
  };
  
  // ---------------------------------------
  // Utility functions
  //---------------------------------------

  function startsWith(fullStr, str) {
      return (fullStr.match("^"+str)==str);
   }

  // ---------------------------------------
  // Final start up
  //---------------------------------------
  
  // backwards compatibility
  if (serviceUrl.length>0) self.startFromService(serviceUrl);
  else if (fixedUrl.length>0) {
	  if (startsWith(fixedUrl.toLowerCase(),"ws://")) self.start(fixedUrl);
	  else self.start("ws://"+fixedUrl);
  }

  return self;

};

