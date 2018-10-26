/**
 * XmlRpc helper.
 */
function XmlRpc() {

}

/**
 * <p>
 * XML-RPC document prolog.
 * </p>
 */
XmlRpc.PROLOG = "<?xml version=\"1.0\"?>\n";

/**
 * <p>
 * XML-RPC methodCall node template.
 * </p>
 */
XmlRpc.REQUEST = "<methodCall>\n<methodName>${METHOD}</methodName>\n<params>\n${DATA}</params>\n</methodCall>";

/**
 * <p>
 * XML-RPC param node template.
 * </p>
 */
XmlRpc.PARAM = "<param>\n<value>\n${DATA}</value>\n</param>\n";

/**
 * <p>
 * XML-RPC array node template.
 * </p>
 */
XmlRpc.ARRAY = "<array>\n<data>\n${DATA}</data>\n</array>\n";

/**
 * <p>
 * XML-RPC struct node template.
 * </p>
 */
XmlRpc.STRUCT = "<struct>\n${DATA}</struct>\n";

/**
 * <p>
 * XML-RPC member node template.
 * </p>
 */
XmlRpc.MEMBER = "<member>\n${DATA}</member>\n";

/**
 * <p>
 * XML-RPC name node template.
 * </p>
 */
XmlRpc.NAME = "<name>${DATA}</name>\n";

/**
 * <p>
 * XML-RPC value node template.
 * </p>
 */
XmlRpc.VALUE = "<value>\n${DATA}</value>\n";

/**
 * <p>
 * XML-RPC scalar node template (int, i4, double, string, boolean, base64,
 * dateTime.iso8601).
 * </p>
 */
XmlRpc.SCALAR = "<${TYPE}>${DATA}</${TYPE}>\n";

/**
 * <p>
 * Get the tag name used to represent a JavaScript object in the XMLRPC
 * protocol.
 * </p>
 * 
 * @param data
 *            A JavaScript object.
 * @return tag|null
 *            String with XMLRPC object type.
 */
XmlRpc.getDataTag = function(data) {
	try {
		// Vars
		var tag = typeof data;
		
		switch (tag.toLowerCase()) {
		case "number":
			tag = (Math.round(data) == data) ? "int" : "double";
			break;
		case "object":
			if (data.constructor == Base64) {
				tag = "base64";
			} else if (data.constructor == String) {
				tag = "string";
			} else if (data.constructor == Boolean) {
				tag = "boolean";
			} else if (data.constructor == Array) {
				tag = "array";
			} else if (data.constructor == Date) {
				tag = "dateTime.iso8601";
			} else if (data.constructor == Number) {
				tag = (Math.round(data) == data) ? "int" : "double";
			} else {
				tag = "struct";
			}
			break;
		}
		return tag;
	} catch (e) {
		return null;
	}
};

/**
 * <p>
 * Get JavaScript object type represented by XMLRPC protocol tag.
 * <p>
 * 
 * @param tag
 *            A XMLRPC tag name.
 * @return data
 *            A JavaScript object.
 */
XmlRpc.getTagData = function(tag) {
	// Vars
	var data = null;
	
	switch (tag) {
	case "struct":
		data = new Object();
		break;
	case "array":
		data = new Array();
		break;
	case "datetime.iso8601":
		data = new Date();
		break;
	case "boolean":
		data = new Boolean();
		break;
	case "int":
	case "i4":
	case "double":
		data = new Number();
		break;
	case "string":
		data = new String();
		break;
	case "base64":
		data = new Base64();
		break;
	}
	return data;
};

/**
 * <p>
 * XmlRpcRequest.
 * </p>
 * 
 * @param url
 *            Server url.
 * @param method
 *            Server side method to call.
 */
function XmlRpcRequest(url, method) {
	this.serviceUrl = url;
	this.methodName = method;
	this.params = [];
	this.withCredentials = false;
    this.credentials = null;
    this.headers = {
        'Accept': 'text/xml',
        'Content-Type': 'text/plain', //instead of text/xml to avoid OPTIONS preflight request
    };
}

/**
 * <p>
 * Add a new request parameter.
 * </p>
 * 
 * @param data
 *            New parameter value.
 */
XmlRpcRequest.prototype.addParam = function(data) {
	// Vars
	var type = typeof data;
	
	switch (type.toLowerCase()) {
	case "function":
		return;
	case "object":
		if (!data.constructor.name){
			return;
		}	
	}
	this.params.push(data);
};

/**
 * <p>
 * Set credentials for HTTP basic authentication.
 * </p>
 *
 * @param credentials
 *            Object with username and password.
 */
XmlRpcRequest.prototype.setCredentials = function(credentials) {
    if(credentials['username'] !== undefined && credentials['password'] !== undefined) {
        this.credentials = {
            'username': credentials.username,
            'password': credentials.password
        };
        this.withCredentials = true;
        this.headers = this.headers.push({
            'Authorization': 'Basic ' + btoa(this.credentials.username + ':' + this.credentials.password)
        });
    }
};

/**
 * <p>
 * Execute a synchronous XML-RPC request.
 * </p>
 *
 * @param payload
 *            Message payload to include in the POST request.
 * @param callback
 */
XmlRpcRequest.prototype.send = function(payload, callback) {
	var xhr = new XMLHttpRequest();
    xhr.open('POST', this.serviceUrl, true);
    for(var header in this.headers) {
        xhr.setRequestHeader(header, this.headers[header]);
    }
    if(this.withCredentials) {
        xhr.withCredentials = true;
    }
    xhr.onreadystatechange = function() {
        if(xhr.readyState == xhr.DONE && xhr.status == 200) {
            try {
                var response = xhr.responseXML;
                callback(response);
            } catch(error) {
                console.log(error);
            }
        }
    };
    xhr.send(payload);
};

/**
 * <p>
 * Create payload in XML format.
 * </p>
 *
 * return xml_call
 */
XmlRpcRequest.prototype.getXML = function() {
	var xml_params = "", xml_call;
	for (var i = 0; i < this.params.length; i++) {
		xml_params += XmlRpc.PARAM.replace("${DATA}", this.marshal(this.params[i]));
	}
	xml_call = XmlRpc.REQUEST.replace("${METHOD}", this.methodName);
	xml_call = XmlRpc.PROLOG + xml_call.replace("${DATA}", xml_params);
	return xml_call;
};


/**
 * <p>
 * Marshal request parameters.
 * </p>
 * 
 * @param data
 *            A request parameter.
 * @return xml
 *            String with XML-RPC element notation.
 */
XmlRpcRequest.prototype.marshal = function(data) {
	// Vars
	var type = XmlRpc.getDataTag(data), 
	    scalar_type = XmlRpc.SCALAR.replace(/\$\{TYPE\}/g, type), 
	    xml = "", 
	    value, i, member;

	switch (type) {
        case "struct":
            member = "";
            for (i in data) {
                value = "";
                value += XmlRpc.NAME.replace("${DATA}", i);
                value += XmlRpc.VALUE.replace("${DATA}", this.marshal(data[i]));
                member += XmlRpc.MEMBER.replace("${DATA}", value);
            }
            xml = XmlRpc.STRUCT.replace("${DATA}", member);
            break;
        case "array":
            value = "";
            for (i = 0; i < data.length; i++) {
                value += XmlRpc.VALUE.replace("${DATA}", this.marshal(data[i]));
            }
            xml = XmlRpc.ARRAY.replace("${DATA}", value);
            break;
        case "dateTime.iso8601":
            xml = scalar_type.replace("${DATA}", data.toIso8601());
            break;
        case "boolean":
            xml = scalar_type.replace("${DATA}", (data == true) ? 1 : 0);
            break;
        case "base64":
            xml = scalar_type.replace("${DATA}", data.encode());
            break;
        default:
            xml = scalar_type.replace("${DATA}", data);
            break;
	}
	return xml;
};

/**
 * <p>
 * XmlRpcResponse.
 * </p>
 * 
 * @param xml
 *            Response XML document.
 */
function XmlRpcResponse(xml) {
	this.xmlData = xml;
}

/**
 * <p>
 * Indicate if response is a fault.
 * </p>
 *
 * @return faultValue
 *            Boolean flag indicating fault status.
 */
XmlRpcResponse.prototype.isFault = function() {
	return this.faultValue;
};

/**
 * <p>
 * Parse XML response to JavaScript.
 * </p>
 * 
 * @return params
 *            JavaScript object parsed from XML-RPC document.
 */
XmlRpcResponse.prototype.parseXML = function() {
	// Vars
	var i, nodesLength;

	nodesLength = this.xmlData.childNodes.length;
	this.faultValue = undefined;
	this.currentIsName = false;
	this.propertyName = "";
	this.params = [];
	for (i = 0; i < nodesLength; i++) {
		this.unmarshal(this.xmlData.childNodes[i], 0);
	}
	return this.params[0];
};

/**
 * <p>
 * Unmarshal response parameters.
 * </p>
 * 
 * @param node
 *            Current document node under processing.
 * @param parent
 *            Current node' parent node.
 */
XmlRpcResponse.prototype.unmarshal = function(node, parent) {
	// Vars
	var obj, tag, i, nodesLength;

	if (node.nodeType == 1) {
		obj = null;
		tag = node.tagName.toLowerCase();
		switch (tag) {
            case "fault":
                this.faultValue = true;
                break;
            case "name":
                this.currentIsName = true;
                break;
            default:
                obj = XmlRpc.getTagData(tag);
                break;
		}
		if (obj != null) {
			this.params.push(obj);
			if (tag == "struct" || tag == "array") {
				if (this.params.length > 1) {
					switch (XmlRpc.getDataTag(this.params[parent])) {
					case "struct":
						this.params[parent][this.propertyName] = this.params[this.params.length - 1];
						break;
					case "array":
						this.params[parent].push(this.params[this.params.length - 1]);
						break;
					}
				}
				parent = this.params.length - 1;
			}
		}
		nodesLength = node.childNodes.length;
		for (i = 0; i < nodesLength; i++) {
			this.unmarshal(node.childNodes[i], parent);
		}
	}
	if ((node.nodeType == 3) && (/[^\t\n\r ]/.test(node.nodeValue))) {
		if (this.currentIsName == true) {
			this.propertyName = node.nodeValue;
			this.currentIsName = false;
		} else {
			switch (XmlRpc.getDataTag(this.params[this.params.length - 1])) {
			case "dateTime.iso8601":
				this.params[this.params.length - 1] = Date.fromIso8601(node.nodeValue);
				break;
			case "boolean":
				this.params[this.params.length - 1] = (node.nodeValue == "1");
				break;
			case "int":
			case "double":
				this.params[this.params.length - 1] = new Number(node.nodeValue);
				break;
			case "string":
				this.params[this.params.length - 1] = new String(node.nodeValue);
				break;
			case "base64":
				this.params[this.params.length - 1] = new Base64(node.nodeValue);
				break;
			}
			if (this.params.length > 1) {
				switch (XmlRpc.getDataTag(this.params[parent])) {
				case "struct":
					this.params[parent][this.propertyName] = this.params[this.params.length - 1];
					break;
				case "array":
					this.params[parent].push(this.params[this.params.length - 1]);
					break;
				}
			}
		}
	}
};

/**
 * Date extensions.
 */

/**
 * <p>
 * Convert a GMT date to ISO8601.
 * </p>
 * 
 * @return String with an ISO8601 date.
 */
Date.prototype.toIso8601 = function() {
	// Vars	
	var year = this.getFullYear(),
	    month = this.getMonth() + 1,
	    day = this.getDate(),
	    time = this.toTimeString().substr(0, 8);
	
	// Normalization
	if (year < 1900) {
		year += 1900;
	}
	if (month < 10) {
		month = "0" + month;
	}
	if (day < 10) {
		day = "0" + day;
	}
	
	return year + month + day + "T" + time;
};

/**
 * <p>
 * Convert ISO8601 date to GMT.
 * </p>
 * 
 * @param value
 *            ISO8601 date.
 * @return Date in GMT format.
 */
Date.fromIso8601 = function(value) {
	// Vars	
	var year = value.substr(0, 4),
	    month = value.substr(4, 2),
	    day = value.substr(6, 2),
	    hour = value.substr(9, 2),
	    minute = value.substr(12, 2),
	    sec = value.substr(15, 2);
	    
	return new Date(year, month - 1, day, hour, minute, sec, 0);
};

/**
 * Base64 implementation.
 */
function Base64(value) {
	this.bytes = value;
}

/**
 * <p>
 * Base64 characters map.
 * </p>
 */
Base64.CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

/**
 * <p>
 * Encode the object bytes using base64 algorithm.
 * </p>
 * 
 * @return string encoded base64 string.
 */
Base64.prototype.encode = function() {
	if (typeof(btoa) === "function") {
		return btoa(this.bytes);
	} else {
		// Vars
		var _byte = [], 
		_char = [], 
		_result = [],
		j = 0;
		
		for (var i = 0; i < this.bytes.length; i += 3) {
			_byte[0] = this.bytes.charCodeAt(i);
			_byte[1] = this.bytes.charCodeAt(i + 1);
			_byte[2] = this.bytes.charCodeAt(i + 2);
			_char[0] = _byte[0] >> 2;
			_char[1] = ((_byte[0] & 3) << 4) | (_byte[1] >> 4);
			_char[2] = ((_byte[1] & 15) << 2) | (_byte[2] >> 6);
			_char[3] = _byte[2] & 63;
			if (isNaN(_byte[1])) {
				_char[2] = _char[3] = 64;
			} else if (isNaN(_byte[2])) {
				_char[3] = 64;
			}
			_result[j++] = Base64.CHAR_MAP.charAt(_char[0])
					     + Base64.CHAR_MAP.charAt(_char[1])
					     + Base64.CHAR_MAP.charAt(_char[2])
					     + Base64.CHAR_MAP.charAt(_char[3]);
		}
		return _result.join("");
	}
};

/**
 * <p>
 * Decode the object bytes using base64 algorithm.
 * </p>
 * 
 * @return string Decoded string.
 */
Base64.prototype.decode = function() {
	if (typeof(atob) === "function") {
		return atob(this.bytes);
	} else {
		// Vars
		var _byte = [], 
		_char = [], 
		_result = [], 
		j = 0;
		
		while ((this.bytes.length % 4) != 0) {
			this.bytes += "=";
		}
		for (var i = 0; i < this.bytes.length; i += 4) {
			_char[0] = Base64.CHAR_MAP.indexOf(this.bytes.charAt(i));
			_char[1] = Base64.CHAR_MAP.indexOf(this.bytes.charAt(i + 1));
			_char[2] = Base64.CHAR_MAP.indexOf(this.bytes.charAt(i + 2));
			_char[3] = Base64.CHAR_MAP.indexOf(this.bytes.charAt(i + 3));
			_byte[0] = (_char[0] << 2) | (_char[1] >> 4);
			_byte[1] = ((_char[1] & 15) << 4) | (_char[2] >> 2);
			_byte[2] = ((_char[2] & 3) << 6) | _char[3];
			_result[j++] = String.fromCharCode(_byte[0]);
			if (_char[2] != 64) {
				_result[j++] = String.fromCharCode(_byte[1]);
			}
			if (_char[3] != 64) {
				_result[j++] = String.fromCharCode(_byte[2]);
			}
		}
		return _result.join("");
	}
};

/**
 * JiL definition.
 */
var JIL = {};

/**
 * JiL implementation.
 */
JIL.Jil = function() {
	this.messages = "";
	this.controlModified = []; // Boolean array indicating if the control has been modified (whether it must be updated or not)
	this.status = "OK"; // Was the last execution successful?
	this.host = ""; // Host to connect to
	this.filePath = ""; // Path of the .vi file to open

	this.controls = [];
	this.indicators = [];
	this.connected = false;
	this.running = false;
};

/**
 * <p>
 * Get the XML payload and run the JiL or the Sarlab proxy send() method
 * </p>
 *
 * @param method
 *            RIP-type message.
 * @return xml
 */
JIL.Jil.prototype.invoke = function(method) {
	var request = new XmlRpcRequest(this.host, method.id);
	if(method.params != undefined) {
		for (var i=0; i<method.params.length; i++) {
			request.addParam(method.params[i]);
		}
	}

	var xmlrpc = request.getXML();

	if(this.proxy != undefined) {
		var response = this.proxy.send(xmlrpc, returnResponse);
	} else {
		var response = request.send(xmlrpc, returnResponse);
	}
    function returnResponse(response) {
        var xml = new XmlRpcResponse(response);
        var message = xml.parseXML();
        if(method.callback != undefined) {
            method.callback(message);
        }
        return xml;
    }
};

/**
 * <p>
 * Run the JiL or the Sarlab SSE method to receive data from the server
 * </p>
 *
 * @return xml
 */
JIL.Jil.prototype.sse = function() {
    var sse;
    if (this.proxy != undefined) {
        sse = this.proxy.sse(this.host);
    } else {
        // Register to the SSE:
        if(typeof(EventSource) !== "undefined") {
            sse = new EventSource(this.host);
            //source = new EventSource('http://62.204.199.224/SARLABV8.0/proxy?url=http://10.192.38.68:8080/RIP/SSE&key=123');
        } else {
            // Poly fil (TODO)
        }
    }

    sse.onopen = function(e) {
        // Connection was opened.
        console.log('Open SSE');
    };

    sse.onmessage = function(e) {
        console.log('data:'+e.data);
        if (e.id === "CLOSE") {
            console.log('Close SSE');
            sse.close();
        } else {
        	//this.onGet(e.data); // TODO: this.onGet() is not a accessible from here!!
			console.log(e.data);
		}
    }
};

/**
 * <p>
 * Is the client connected to the server?
 * </p>
 *
 * return boolean connected
 */
JIL.Jil.prototype.isConnected = function() {
	return this.connected;
};

/**
 * <p>
 * Is the vi in the server running?
 * </p>
 *
 * @return boolean running
 */
JIL.Jil.prototype.isRunning = function() {
	return this.running;
};

/**
 * <p>
 * Connect the client to the server
 * </p>
 *
 * @param callback
 * @return status
 */
JIL.Jil.prototype.connect = function(callback) {
	try {
		var method = {
			id: "jil.connect",
			params: undefined,
			callback: this.onResponse(this.onConnect.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status = "COMMUNICATION FAIL";
	}
	return this.status;
};

/**
 * <p>
 * onResponse callback for the connect() method
 * </p>
 *
 * @param callback
 * @param userCallback
 */
JIL.Jil.prototype.onResponse = function(callback, userCallback) {
	return function(response) {
		if(callback != undefined) {
			callback(response);
		}
		if(userCallback != undefined) {
			userCallback(response);
		}	
	}
};

/**
 * <p>
 * onConnect callback for the connect() method
 * </p>
 *
 * @param response
 */
JIL.Jil.prototype.onConnect = function(response) {
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode + "-->" + response.faultString);
		this.status = "FAIL CONNECTING";
		this.connected = false;
	} else {
		this.messageAdd("Version: " + response.version + " SessionID: " + response.sessionID);
		this.status = "OK";
		this.connected = true;
	}
};

/**
 * <p>
 * Opens the vi in the server
 * </p>
 *
 * @param vi
 * @param callback
 * @return status
 */
JIL.Jil.prototype.openVi = function(vi, callback) {  
	try {
		if (vi == null)	{
			vi = this.filePath;
		}
		var method = {
			id: "jil.openvi",
			params: [vi],
			callback: this.onResponse(this.onOpen.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status = "COMMUNICATION FAIL";
	}
	return this.status;
};

/**
 * <p>
 * onOpen callback for the open() method
 * </p>
 *
 * @param response
 */
JIL.Jil.prototype.onOpen = function(response) {
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode + "-->" + response.faultString);
		this.status="FAIL OPENING VI";
	} else {
		for(var n=0; n<response.length; n++) {
			if (response[n].control_indicator == "control") {
				this.controls[response[n].name] = {
					datatype: response[n].DataType,
					type: this.getRepresentativeValue(response[n].DataType),
					modified: false
				};
				this.controlModified.push(false);
			} else {
					this.indicators[response[n].name] = {
						datatype: response[n].DataType,
						type: this.getRepresentativeValue(response[n].DataType),
						modified: false
					}
			}
		}
		this.messageAdd("VI Opened");
		this.status = "OK";
	}
};

/**
 * <p>
 * returns a value that represents the different variable types
 * </p>
 *
 * @param type
 * @return types
 */
JIL.Jil.prototype.getRepresentativeValue = function(type) {
	var types = {
		'int': 0,
		'boolean': false,
		'double': 0.1,
		'string': 'a'
	};
	return types[type];
};

/**
 * <p>
 * runs the vi in the server
 * </p>
 *
 * @param callback
 * @return status
 */
JIL.Jil.prototype.runVi = function(callback) {  
	try {
		var method = {
			id: "jil.runvi",
			params: undefined,
			callback: this.onResponse(this.onRun.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status="COMMUNICATION FAIL";
	}
	return this.status;
};

/**
 * <p>
 * onRun callback for the runVi() method
 * </p>
 *
 * @param response
 */
JIL.Jil.prototype.onRun = function(response) {
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode +"-->" + response.faultString);
		this.status = "FAIL RUNNING VI";
	} else {
		this.messageAdd("VI running");
		this.status = "OK";
		this.running = true;
		// Once the Vi is running, we can start the SSE connection to receive indicators
        this.sse();
	}
};

/**
 * <p>
 * runs the connect(), openVi() and runVi() methods
 * </p>
 *
 * @param callback
 */
JIL.Jil.prototype.start = function(callback) {
    this.connect(function() {
        this.openVi(this.filePath, function() {
            this.runVi(callback)
        }.bind(this))
    }.bind(this))
};

/**
 * <p>
 * sends a syncvi method with empty parameters to the server to keep the connection alive
 * </p>
 *
 * @return status
 */
JIL.Jil.prototype.stayConnected = function() {  
	try {
		var response = this.invoke("jil.syncvi", []);
		if(response.faultValue) {
			this.messageAdd("Error: " + response.faultCode +"-->"+ response.faultString);
			this.status="SYNC FAIL2";
		} else {
			this.status="OK";
		}
		return this.status;
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status="COMMUNICATION FAIL";
		return this.status;
	}
};

/**
 * <p>
 * sends the syncvi method with one control variable value as parameter
 * </p>
 *
 * @param variable
 * @param value
 * @param callback
 * @return status
 */
JIL.Jil.prototype.setVariable = function(variable, value, callback) {  
	return this.set([variable], [value], callback);
};

/**
 * <p>
 * sends the syncvi method with several control variable values as parameters
 * </p>
 *
 * @param variables
 * @param values
 * @param callback
 * @return status
 */
JIL.Jil.prototype.set = function(variables, values, callback) {  
	try {
		var toSet = this.checkValidToSet(variables, values);
		if(toSet.length < 1) {
			this.status = "No Variables To Set";
			return this.status;
		}
		var method = {
			id: "jil.syncvi",
			params: [toSet],
			callback: this.onResponse(this.onSet.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status = "COMMUNICATION FAIL";
	}
	return this.status;
};

/**
 * <p>
 * checks whether the variables are really controls in the vi notified by the server
 * </p>
 *
 * @param variables
 * @param values
 * @return valid
 */
JIL.Jil.prototype.checkValidToSet = function(variables, values) {
	var valid = [];
	for(var i = 0; i<variables.length; i++) {
		var name = variables[i];
		var control = this.controls[name]; 
		if(control != undefined) {
			var set = {
				name: name,
				action: "set",
				value: this.checkType(control.datatype, values[i])
			};
			valid.push(set);
		}
	}
	return valid;
};

/**
 * <p>
 * onSet callback for the set() method
 * </p>
 *
 * @param response
 * @return status
 */
JIL.Jil.prototype.onSet = function(response) {
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode +"-->"+ response.faultString);
		this.status="SYNC FAIL2";
		return this.status;
	} else {
		this.status = "OK";
	}
	return this.status;	
};

/**
 * <p>
 * sends the syncvi method with one indicator variable as parameter
 * </p>
 *
 * @param variable
 * @param callback
 * @return status
 */
JIL.Jil.prototype.getVariable = function(variable, callback) {  
	return this.get([variable], callback);
};

/**
 * <p>
 * sends the syncvi method with several indicator variables as parameters
 * </p>
 *
 * @param variables
 * @param callback
 * @return status
 */
JIL.Jil.prototype.get = function(variables, callback) {  
	try {
		var toGet = this.checkValidToGet(variables);
		var method = {
			id: "jil.syncvi",
			params: [toGet],
			callback: this.onResponse(this.onGet.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status = "COMMUNICATION FAIL";
		return this.status;
	}
};

/**
 * <p>
 * checks whether the variables are really indicators in the vi notified by the server
 * </p>
 *
 * @param variables
 * @return valid
 */
JIL.Jil.prototype.checkValidToGet = function(variables) {
	var valid = [];
	for(var i = 0; i<variables.length; i++) {
		var name = variables[i];
		var indicator = this.indicators[name];
		if(indicator != undefined) {
			var get = {
				name: name,
				action: "get",
				value: indicator.type
			};
			valid.push(get);
		}
	}
	return valid;
};

/**
 * <p>
 * onGet callback for the get() method; also called by the onmessage event of the see client
 * </p>
 *
 * @param response
 * @return status
 */
JIL.Jil.prototype.onGet = function(response) {
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode +"-->"+ response.faultString);
		this.status = "SYNC FAIL2";
	} else {
		for (var n=0; n<response.length; n++) {
			var name = response[n].name;
			var indicator = this.indicators[name];
			var value = response[n].value;
			indicator.value = this.checkType(indicator.datatype, value);
			this.messageAdd(indicator.name + " " + indicator.value);
		}
		this.status = "OK";
	}
};

/**
 * <p>
 * sends the stopvi method to the server to stop the execution of the vi
 * </p>
 *
 * @param callback
 * @return status
 */
JIL.Jil.prototype.stopVi = function(callback) {
	try {
		var method = {
			id: "jil.stopvi",
			params: undefined,
			callback: this.onResponse(this.onStop.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status="COMMUNICATION FAIL";
		return this.status;
	}
};

/**
 * <p>
 * onStop callback for the stopVi() method
 * </p>
 *
 * @param response
 */
JIL.Jil.prototype.onStop = function(response) {
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode +"-->"+ response.faultString);
		this.status = "FAIL STOPPING VI";
	} else {
		this.messageAdd("VI Stopped");
		this.status = "OK";
		this.running = false;
	}
};

/**
 * <p>
 * sends the closevi method to the server to close the vi
 * </p>
 *
 * @param callback
 * @return status
 */
JIL.Jil.prototype.closeVi = function(callback) {  
	try {
		var method = {
			id: "jil.closevi",
			params: undefined,
			callback: this.onResponse(this.onClose.bind(this), callback)
		};
		this.invoke(method);
	} catch(error) {
		this.xmlErrorHandler(error);
		this.status="COMMUNICATION FAIL";
	} finally {
        this.controls = [];
		this.indicators = [];
		return this.status;
	}
};

/**
 * <p>
 * onClose callback for the closeVi() method
 * </p>
 *
 * @param response
 */
JIL.Jil.prototype.onClose = function(response) {  
	if(response.faultValue) {
		this.messageAdd("Error: " + response.faultCode +"-->"+ response.faultString);
		this.status="FAIL CLOSING VI"
	} else {
		this.messageAdd("VI Closed");
		this.status="OK";
		this.running = false;
	}
};

/**
 * <p>
 * sends the disconnect method to the server to close the http socket connection
 * </p>
 *
 * @param callback
 */
JIL.Jil.prototype.disconnect = function(callback) {
	this.stopVi(function() {
		this.closeVi(function() {
			try {
				var method = {
					id: "jil.disconnect",
					params: undefined,
					callback: this.onResponse(this.onDisconnect.bind(this), callback)
				};
				this.invoke(method);
				return this.status;
			} catch(error) {
				this.xmlErrorHandler(error);
				this.status = "COMMUNICATION FAIL";
				this.connected = false;
				return this.status;
			}
		}.bind(this));
	}.bind(this));
};

/**
 * <p>
 * onDisconnect callback for the disconnect() method
 * </p>
 *
 * @param response
 */
JIL.Jil.prototype.onDisconnect = function(response) {  
		if(response.faultValue) {
			this.messageClear();
			this.messageAdd("Error: " + response.faultCode +"-->"+ response.faultString);
			this.status="FAIL DISCONNECTING";
			return this.status;
		} else {
			this.messageClear();
		}
		this.status="OK";
};

/**
 * <p>
 * obtain data and to write the error messages
 * </p>
 *
 * @param message
 */
JIL.Jil.prototype.messageAdd = function(message) {
	this.messages+=message +"\n";
};

/**
 * <p>
 * clear the error messages
 * </p>
 */
JIL.Jil.prototype.messageClear = function() {
	this.messages = "";
};

/**
 * <p>
 * manages the errors with the HTTP sockets
 * </p>
 *
 * @param error
 */
JIL.Jil.prototype.xmlErrorHandler = function (error) {
	if (error == "Error: NetworkError: DOM Exception 19") {
		this.messageAdd("Cannot connect to host...")
	} else {
		console.log(error); // Other errors?
	}
};

/**
 * <p>
 * checks the types and makes the conversion if possible. Otherwise, it returns null
 * </p>
 *
 * @param type
 * @param value
 */
JIL.Jil.prototype.checkType = function(type, value) {
	if(!value) { // Check it is not a wrong value: false, null NaN...
		if ((value == "") && (type == "string")) {
			return ""; // if type is string and it is not empty, then its fine
		} else if ((value == false) && (type == "boolean")) {
			return false; // if it is false, then its fine
		} else {
			return null;
		}
	} else { // In this case, the value is ok, so we check its type and convert
		switch (String(type)) {
			case "int":
				if ((value % 1) != 0) {
					return null;
				} else {
					return parseInt(value);
				}
			case "double":
                value = Number(value);
				if ( isNaN(value) || !isFinite(value) || (typeof value == "undefined") ) {
					return null;
				} else {
					return value;
				}
			case "boolean":
				if (value == "true" || value == 1) {
					return true;
				}	else if (value == "false" || value == 0 ) {
					return false;
				} else {
					return null;
				} // Not true nor false
			case "string":
                value=String(value);
                value=value.replace("&","&amp;");
                value=value.replace("<","&lt;");
				return value;
			default:
				return null; // Not recognized type
			}
	}
};
