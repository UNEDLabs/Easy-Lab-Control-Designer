function SarlabProxy (secure, ip, basepath, port, experience) {
  this.credentials = null;
  this.typeCredentials = null; // 'user' or 'key'
  this.experience = experience;
  this.wsSarlab = null;
  this.sessionTime = 1; // 1 minute by default

  SarlabProxy.prototype.closeScreen = './';
  SarlabProxy.prototype.sessionKey = null;

  this.setSarlabInfo(secure === 'true', ip, basepath, port, experience, SarlabProxy.prototype.closeScreen);

  this.get = function(url, callback) {
      var xmlHttp = new XMLHttpRequest();
      xmlHttp.onreadystatechange = function() {
          if(xmlHttp.readyState === xmlHttp.DONE && xmlHttp.status === 200) {
              callback(xmlHttp.responseText);
          }
      };
      xmlHttp.open('GET', url, true);
      xmlHttp.send(null);
  };

  this.post = function(url, payload, callback) {
      var xmlHttp = new XMLHttpRequest();
      xmlHttp.onreadystatechange = function() {
          if(xmlHttp.readyState === xmlHttp.DONE && xmlHttp.status === 200) {
              callback(xmlHttp.responseText);
          }
      };
      xmlHttp.open('POST', url, true);
      xmlHttp.withCredentials = true;
      xmlHttp.setRequestHeader('Content-Type', 'text/xml');
      xmlHttp.send(payload);
  }
}

//---------------------------------------------------------------------------------------------------------------------

SarlabProxy.prototype.setSarlabInfo = function(secure, host, basepath, port, experience, closescreen) {
    if (secure) {
        this.httpProtocol = 'https';
        this.wsProtocol = 'wss';
    } else {
        this.httpProtocol = 'http';
        this.wsProtocol = 'ws';
    }
    this.ip = host;
    this.basepath = basepath;
    this.port = port;
    this.experience = experience;
    SarlabProxy.prototype.closeScreen = closescreen;
};

SarlabProxy.prototype.setTimeout = function(timeout) {
    this.sessionTime = timeout;
};

SarlabProxy.prototype.setSarlabCredentials = function(credentials) {
    if (credentials['key'] !== undefined) {
        this.typeCredentials = 'key';
        this.credentials = {
            'key': credentials.key
        };
        this.withCredentials = true;
    } else if (credentials['username'] !== undefined && credentials['password'] !== undefined) {
        this.typeCredentials = 'user';
        this.credentials = {
            'username': credentials.username,
            'password': credentials.password
        };
        this.withCredentials = true;
        // this.headers.push('Authorization': 'Basic ' + btoa(this.credentials.username + ':' + this.credentials.password));
  }
};

SarlabProxy.prototype.getCredentialString = function () {
    if (SarlabProxy.prototype.sessionKey != null) {
        return 'keySession=' + SarlabProxy.prototype.sessionKey + '&timeOut=' + this.sessionTime;
    } else if (this.credentials['username'] != null) {
        return 'username=' + this.credentials.username + '&password=' + this.credentials.password;
    } else if (this.credentials['key'] != null) {
        return 'key=' + this.credentials.key;
    }
    return '';
};

SarlabProxy.prototype.getSessionKey = function () {
    return SarlabProxy.prototype.sessionKey;
};

SarlabProxy.prototype.getHTTPProxyUrl = function(url) {
    return this.getHTTPProxyPath(this.httpProtocol) + '?' + this.getCredentialString() + '&url=' + encodeURI(url);
};

SarlabProxy.prototype.getCamProxyUrl = function(url) {
    return this.getCamProxyPath(this.httpProtocol) + '?' + this.getCredentialString() + '&url=' + encodeURI(url);
};

SarlabProxy.prototype.getHTTPProxyPath = function() {
    return this.getBaseUrl(this.httpProtocol) + '/proxy';
};

SarlabProxy.prototype.getCamProxyPath = function() {
    return this.getBaseUrl(this.httpProtocol) + '/proxycam';
};

SarlabProxy.prototype.getBaseUrl = function(protocol) {
    if (this.port !== 80) {
        return protocol + '://' + this.ip + ':' + this.port + '/' + this.basepath;
    }
    return protocol + '://' + this.ip + '/' + this.basepath;
};

SarlabProxy.prototype.getHTTPUrlById = function(id, suffix) {
    var endURL = this.getHTTPEndUrlById(id, suffix);
    var result = this.getHTTPProxyPath();
    if(endURL !== undefined) {
        result += '?' + this.getCredentialString() + '&url=' + encodeURI(endURL);
    }
    return result;
};

SarlabProxy.prototype.getCamUrlById = function(id, suffix) {
    var endURL = this.getHTTPEndUrlById(id, suffix);
    var result = this.getCamProxyPath();
    if(endURL !== undefined) {
        result += '?' + this.getCredentialString() + '&url=' + encodeURI(endURL);
    }
    return result;
};

SarlabProxy.prototype.getHTTPEndUrlById = function(id, suffix) {
	var proxies = this.getHTTPProxies();
	var result = undefined;
	var n = 0;
	if (proxies !== undefined) { n = proxies.length; }
	for (var i = 0; i < n; i++) {
		if (proxies[i].Description === id) {
			var url = proxies[i].IPInternal + ':' + proxies[i].PortInternal;
            if (!String.prototype.startsWith) { // polyfill for IE and Edge
                String.prototype.startsWith = function(searchString, position) {
                    position = position || 0;
                    return this.indexOf(searchString, position) === position;
                };
            }
			if (!url.startsWith('http')) {
				url = 'http://' + url;
			}
			if (suffix !== undefined) {
				if (!suffix.startsWith('/')) {
					url += '/';
				}
				url += suffix;
			}
			result = url;
		}
	}
	return result;
};

SarlabProxy.prototype.getHTTPProxies = function() {
    var result = undefined;
    try {
        result = this.info["ListConnectionProxyHTML"]["ConnectionProxyHTML"];
    } catch(error) {
        console.log('[ERROR] ' + error);
    }
    return result;
};

SarlabProxy.prototype.getWebsocketsUrlById = function(id) {
    var result = this.getWebsocketsProxyPath();
    var endURL = this.getEndWebsocketsUrlById(id);
    if (endURL !== undefined) {
        result += '?' + this.getCredentialString() + encodeURI(endURL);
    }
    return result;
};

SarlabProxy.prototype.getWebsocketsProxyPath = function() {
    return this.getBaseUrl(this.wsProtocol) + '/proxy1';
};

SarlabProxy.prototype.getEndWebsocketsUrlById = function(id) {
    var proxies = this.getWebsocketsProxies();
    var result = undefined;
    var n = 0;
    if(proxies !== undefined) { n = proxies.length; }
    for (var i = 0; i < n; i++) {
        if (proxies[i].Description === id) {
            result = "&ip=" + proxies[i].IPInternal + '&port=' + proxies[i].PortInternal;
        }
    }
    return result;
};

SarlabProxy.prototype.getWebsocketsProxies = function() {
    var result = undefined;
    try {
        result = this.info["ListConnectionProxyWebsocket"]["ConnectionProxyWebsocket"];
    } catch(error) {
        console.log('[ERROR] ' + error);
    }
    return result;
};

SarlabProxy.prototype.connectExperience = function(experience, callback) {
	var url = this.getServicePath() + '?idExp=' + encodeURI(experience);
	this.get(url, function(response) {
        this.info = JSON.parse(response);
        this.connectSession(this.info, callback);
	}.bind(this));
};

SarlabProxy.prototype.getServicePath = function() {
    return this.getBaseUrl(this.httpProtocol) + '/webresources/service';
};

SarlabProxy.prototype.connectSession = function(info, callback) {
    if (SarlabProxy.prototype.wsSarlab != null) return;
    var myObj = {
        'orden': {
            'mode': 1, 'idSecuent': Math.floor(Math.random() * 1000), 'command': 'access'
            }
    };
    switch(this.typeCredentials) {
        case 'key':
            myObj.orden.parameters=[this.experience, this.sessionTime, this.typeCredentials, this.credentials.key];
            break;
        case 'user':
            myObj.orden.parameters=[this.experience, this.sessionTime, this.typeCredentials, this.credentials.username, this.credentials.password];
            break;
    }

    // TODO: Delete credentials either from query (preferred option) or from myObj
    var wsSarlab = new WebSocket(this.getSarlabPath() + "?idExp=" + this.experience + '&' + this.getCredentialString());
    this.wsSarlab = wsSarlab;

    wsSarlab.onopen = function() {
        wsSarlab.send(JSON.stringify(myObj));
    };

    wsSarlab.onmessage = function (evt) {
        var obj = JSON.parse(evt.data);
        switch(obj.orden.command) {
            case "log":
                break;
            case "error":
                alert("Connection error..." + obj.orden.parameters[0]);
                SarlabProxy.prototype.closeSession();
                break;
            case "keySession":
                SarlabProxy.prototype.sessionKey = obj.orden.parameters[0];
                if(callback !== undefined) {
                    callback(info);
                }
                console.log("Command is received..." + obj.orden.command + ' keySession = ' + SarlabProxy.prototype.sessionKey);
                break;
            case "time":
                // TODO: Send time to an external agent to present remaining time info to users
                break;
        }
    };

    wsSarlab.onclose = function() {
        SarlabProxy.prototype.closeSession();
    };
};

SarlabProxy.prototype.getSarlabPath = function() {
    return this.getBaseUrl(this.wsProtocol) + '/sarlab';
};

SarlabProxy.prototype.sendTimeOut = function() {
    var myObj = {
        'orden': {
            'mode': 1, 'idSecuent': Math.floor(Math.random() * 1000), 'command': 'timeOut',
            'parameters': [this.sessionTime, SarlabProxy.prototype.sessionKey]
        }
    };
    this.wsSarlab.send(JSON.stringify(myObj));
};

SarlabProxy.prototype.closeSession = function() {
    window.location.href = SarlabProxy.prototype.closeScreen;
};

SarlabProxy.prototype.isConnected = function () {
	return this.sessionKey != null;
};

SarlabProxy.prototype.getHTTPProxy = function(id, suffix) {
	var url = this.getHTTPUrlById(id, suffix);
	var transport = new Transport(url, this.credentials);
	transport.configurePostRequest();
	return transport;
};

SarlabProxy.prototype.getWebSocketProxy = function(command, IP, port, idExp, expTime, user, password, jarPath) {
	return new SarlabWebSocket(command, IP, port, idExp, expTime, user, password, jarPath);
};

//---------------------------------------------------------------------------------------------------------------------

function Transport(url, credentials) {
	this.url = url;
	this.credentials = credentials;
	this.asynchronous = true;

	this.send = function(payload, callback) {
		var xhr = new XMLHttpRequest();
		xhr.open(this.method, this.url, this.asynchronous);
		for(var header in this.headers) {
			xhr.setRequestHeader(header, this.headers[header]);
		}
		if(this.withCredentials) {
			xhr.withCredentials = true;
		}
		if(this.asynchronous) {
			xhr.onreadystatechange = function() {
				if(xhr.readyState === xhr.DONE && xhr.status === 200) {
					try {
						if(xhr.getResponseHeader('Content-Type') === 'text/xml') {
							callback(xhr.responseXML);
						} else {
							callback(xhr.responseText);
						}
					} catch(error) {
						console.log(error);
					}
				} 
			}
		}
		xhr.send(payload);
	};

	this.configureGetRequest = function() {
		this.method = 'GET';
	};

	this.configurePostRequest = function() {
		this.method = 'POST';
		this.headers = {
			'Accept': 'text/xml',
			'Content-Type': 'text/xml' //use text/plain instead to avoid OPTIONS preflight request
		};
	};

    this.sse = function() {
        var source;
        // this.url = this.url + '/RIP/SSE?expId=TestOK';
        // Register to the SSE:
        if(typeof(EventSource) !== "undefined") {
            console.log("Opened SSE to " + this.url);
            source = new EventSource(this.url);
        } else {
            alert("SSE are not supported by your browser!");
        }
        /*source.onerror = function(event) {
            if (event.eventPhase == EventSource.CLOSED) {
                that.eventSource.close();
                console.log("SSE Closed");
            }
        };
        source.onmessage = function(event) {
            if(!event) {
                source.close();
            } else {
                // Update EjsS variables
            }
        };*/
        return source;
    };
}

//---------------------------------------------------------------------------------------------------------------------

function SarlabWebSocket(command, IP, port, idExp, expTime, user, password, jarPath) {
	var ws = new WebSocket("ws://127.0.0.1:8887");
	this.ws = ws;

	ws.onopen = function() {
		ws.send("Message to send \r\n");
		var obj = '{'
			+ '"command":"' + command + '",'
			+ '"ip_server":"' + IP + '",'
			+ '"port_server":"' + port + '",'
			+ '"id_exp":"' + idExp + '",'
			+ '"expiration_time":"' + expTime + '",'
			+ '"user":"' + user + '",'
			+ '"password":"' + password + '"';
		if (command === 'execjar') {
			obj += ',' + '"jar_file":"' + jarPath + '"';
		}
		obj += '}';
		ws.send(obj);
		console.log("Connected to Sarlab experience: " + idExp);
	};

	ws.onmessage = function (evt) {
		console.log("Message from Sarlab server: " + evt.data);
	};

	ws.onerror = function() {
		console.log('This: ' + this);
		if (ws.readyState === 1 || ws.readyState === 2) {
			ws.send("exit");
		}
		alert("You need to download, install and/or run the Sarlab service.");
		var a = document.createElement("a");
		a.download = "installsarlabservice_win64.exe";
		a.href = "http://sarlab2.uhu.es/downloads/installsarlabservice_win64.exe";
		a.target = "_blank";
		document.body.appendChild(a);
		a.click();
		document.body.removeChild(a);
	};

	ws.onclose = function() {
		console.log("Connection has been closed.");
	};

	window.onbeforeunload = function() {
		ws.send('{"command":"reset"}');
		ws.send('{"command":"exit"}');
	};
}

SarlabWebSocket.prototype.connectExperience = function(command, IP, port, idExp, expTime, user, password, jarPath) {
	var obj = '{'
		+ '"command":"' + command + '",'
		+ '"ip_server":"' + IP + '",'
		+ '"port_server":"' + port + '",'
		+ '"id_exp":"' + idExp + '",'
		+ '"expiration_time":"' + expTime + '",'
		+ '"user":"' + user + '",'
		+ '"password":"' + password + '"';
	if (command === 'execjar') {
		obj += ',' + '"jar_file":"' + jarPath + '"';
	}
	obj += '}';
	this.ws.send(obj);
};

SarlabWebSocket.prototype.stopExperience = function() {
	this.ws.send('{"command":"exit"}');
};

SarlabWebSocket.prototype.resetExperience = function() {
	this.ws.send('{"command":"reset"}');
};
