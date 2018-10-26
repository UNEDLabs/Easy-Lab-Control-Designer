/**
 * XmlRpcProtocol
 * author: Jesús Chacón <jcsombria@gmail.com>
 *
 * Copyright (C) 2014 Jesús Chacón
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uned.dia.softwarelinks.labview.protocol.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;

import es.uned.dia.softwarelinks.labview.protocol.HighLevelProtocol;
import es.uned.dia.softwarelinks.labview.protocol.LowLevelProtocol;

/**
 * Class to interact with a JIL Server
 * @author Jesús Chacón <jcsombria@gmail.com> 
 */
public class XmlRpcProtocol implements HighLevelProtocol, LowLevelProtocol, Runnable {	
	// The XmlRpcClient object
	private XmlRpcClient client;
	private HashMap<String, String> controls;
	private HashMap<String, String> indicators;
	private HashMap<String, Object> results = new HashMap<String, Object>();
	private Queue<Object> queue = new LinkedList<Object>();

	// The names of the methods
    private static final String METHODNAME_CONNECT = "jil.connect";        
    private static final String METHODNAME_AUTHENTICATE = "jil.authenticate"; 
    private static final String METHODNAME_OPENVI = "jil.openvi"; 
    private static final String METHODNAME_RUNVI = "jil.runvi"; 
    private static final String METHODNAME_STOPVI = "jil.stopvi"; 
    private static final String METHODNAME_SERVERINFO = "jil.info";
    private static final String METHODNAME_CLOSEVI = "jil.closevi";
    private static final String METHODNAME_SYNCVI = "jil.syncvi"; 
    private static final String METHODNAME_DISCONNECT = "jil.disconnect";

    private enum State { IDLE, CONNECTED, OPENED, RUNNING };
    private State state = State.IDLE;
    
    /**
     * Create a new Labview Object 
     * @param url The url of the server
     */
	public XmlRpcProtocol(String url) throws MalformedURLException {
    	setServerAddress(url);
	}
	
    /**
     * Create a new Labview Object 
     * @param url The url of the server
     */
	public XmlRpcProtocol(URL url) {
    	setServerAddress(url);
	}
	
    /**
     * Set the <i>url</i> of the server 
     * @param url The url of the server
     */
	public boolean setServerAddress(String url) {
		boolean changed;
		try {
			URL address = new URL(url);
			changed = setServerAddress(address);
		} catch(Exception e) {
			changed = false;
		}
    	return changed;
	}

    /**
     * Set the <i>url</i> of the server 
     * @param url The url of the server
     */
	public boolean setServerAddress(URL url) {
		/* XmlRpcClient Configuration */
	    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(url);
		config.setConnectionTimeout(60000);
		config.setReplyTimeout(60000);
		config.setEnabledForExtensions(true);
	    config.setGzipRequesting(true);
		config.setEncoding("UTF-8");
		/* Create the XmlRpcClient */
	    client = new XmlRpcClient();
	    client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
	    client.setConfig(config);
	    return true;
	}

    /**
     * Enable or disable the Gzip compressing of the data   
     * @param	url	The url of the server
     * @return	<i>true</i> if the property has been correctly changed, false otherwise.	
     */
	public boolean setGzipCompressing(boolean enabled) {
		XmlRpcClientConfigImpl config = (XmlRpcClientConfigImpl)client.getConfig();
		if(config == null) {
			return false;
		}
	    config.setEnabledForExtensions(enabled);
		config.setGzipCompressing(enabled);
		return true;
	}
	
    /**
     * Open a connection with the server 
     * @return	<i>true</i> if the object is connected with the server as a result of the call, <i>false</i> otherwise.   	
     */
    public boolean connect() {
		try {
			Object[] params = new Object[]{};
			HashMap result = (HashMap)client.execute(METHODNAME_CONNECT, params);
			state = State.CONNECTED;
		} catch (XmlRpcException e) {
			state = State.IDLE;
			System.err.println(e);
			return false;
		}	
		return true;
    }
    
    public void authenticate() {
		try {
			Object[] params = new Object[]{};
			Object result = client.execute(METHODNAME_AUTHENTICATE, params);	
		} catch (XmlRpcException e) {
			System.err.println(e);
		}	
    }

    /**
     * Open a new VI 
     * @param name The path to the VI
     */
    public boolean openVI(String name) {
		try {
			Object[] params = new Object[]{name};
			Object[] result = (Object[])client.execute(METHODNAME_OPENVI, params);	
			controls = new HashMap<String, String>();
			indicators = new HashMap<String, String>();			
			for(Object o : result) {
				HashMap variable = (HashMap) o;
				if(variable != null) {
					String varname = (String)variable.get("name"), 
						   type = (String)variable.get("DataType"),
						   value = (String)variable.get("control_indicator");
					if(value.equals("control")) {
						controls.put(varname, type);
					} else if(value.equals("indicator")) {
						indicators.put(varname, type);
					}
				}
			}
			state = State.OPENED;
		} catch (XmlRpcException e) {
			state = State.IDLE;
			System.err.println(e);
			return false;
		}
		return true;
    }

    /**
     * Get info from the server
     */
    public boolean getServerInfo() {
		try {
			Object[] params = new Object[]{};
			HashMap result = (HashMap)client.execute(METHODNAME_SERVERINFO, params);
			System.out.println("getServerInfo: "+ result.toString());
			String serverState = (String) result.get("state");
			if(serverState.equals("opened")) {
				state = State.OPENED;
			} else if(serverState.equals("running")) {
				state = State.RUNNING;
			} else if(serverState.equals("connected")) {
				state = State.CONNECTED;
			} else {
				state = State.IDLE;
			}
		} catch (XmlRpcException e) {
			System.err.println(e);
			return false;
		}
		return true;
    }

    /**
     * Run the opened VI
     * @returns true if the VI is running as a result of the call 
     */
    public boolean runVI() {
		try {
			Object[] params = new Object[]{};
			client.execute(METHODNAME_RUNVI, params);
			state = State.RUNNING;
		} catch (XmlRpcException e) {
			System.err.println(e);
			return false;
		}
		return true;
    }
    
    /**
     * Stop the running VI
     */
    public boolean stopVI() {
		try {
			Object[] params = new Object[]{};
			client.execute(METHODNAME_STOPVI, params);
			state = State.OPENED;
		} catch (XmlRpcException e) {
			System.err.println(e);
			return false;
		}
		return true;
    }
    
    /**
     * Close the opened VI
     */
    public boolean closeVI() {
		try {
			Object[] params = new Object[]{};
			client.execute(METHODNAME_CLOSEVI, params);
			state = State.CONNECTED;
		} catch (XmlRpcException e) {
			System.err.println(e);
			return false;
		}
		return true;
    }

    /**
     * Sync the VI
     */
    public boolean syncVI() {
		try {
			Object[] params = new Object[]{queue.toArray()};
			queue.clear();
			Object[] result = (Object[])client.execute(METHODNAME_SYNCVI, params);			
			for(Object o : result) {
				HashMap<String, Object> variable =  (HashMap<String, Object>) o;
				if(variable != null) {
					String var = (String)variable.get("name");
					results.put(var, variable.get("value"));
				}
			}
		} catch (XmlRpcException e) {
			System.err.println(e);
			return false;
		}
		return true;
    }   
    
    /**
     * Disconnect from the server
     */
    public boolean disconnect() {
		try {
			Object[] params = new Object[]{};
			String result = (String)client.execute(METHODNAME_DISCONNECT, params);
			state = State.IDLE;
		} catch (XmlRpcException e) {
			System.err.println(e);
			return false;
		}
		return true;
    }

    /**
     * Obtain the value of an indicator
     * @params name The name of the indicator
     * @params value The new value
     */
    public Object getVariable(String name) {
		try {
			HashMap<String, Object> vars = getHashMapForGetVariable(name);
			Object[] params = new Object[]{new Object[]{vars}};
			Object[] result = (Object[])client.execute(METHODNAME_SYNCVI, params);
			Object value = null;
			for(Object o : result) {
				HashMap variable = (HashMap) o;
				if(variable != null && variable.get("name").equals(name)) {
					value = variable.get("value");
				}
			}
			return value;
		} catch (XmlRpcException e) {
			System.err.println(e);
		} catch (NullPointerException e) {
			return null;
		}
		return null;
    }
  
    /**
     * Update the value of a control
     * @params name The name of the control
     * @params value The new value
     */
    public void setVariable(String name, Object value) {
		try {
			HashMap<String, Object> vars = getHashMapForSetVariable(name, value);
			Object[] params = new Object[]{new Object[]{vars}};
			client.execute(METHODNAME_SYNCVI, params);
		} catch (XmlRpcException e) {
			System.err.println(e);
		} catch (NullPointerException e) {
			return;
		}
    }

    public Object getVariableResult(String name) {
    	return results.get(name);
    }
    
    /**
     * Update the value of a control
     * @params name The name of the control
     * @params value The new value
     */
    public boolean getVariableLater(String name) {
    	HashMap<String, Object> request = getHashMapForGetVariable(name);
    	if(request == null) {
    		return false;
    	}
		queue.add(request);
		return true;
    }
    
    /**
     * Update the value of a control
     * @params name The name of the control
     * @params value The new value
     */
    public boolean setVariableLater(String name, Object value) {
		HashMap<String, Object> request = getHashMapForSetVariable(name, value);
		if(request == null) {
			return false;
		}
		queue.add(request);
		return true;
    }

	/**
     * Returns a HashMap representing a request for an indicator
     * @params name The name of the indicator
     */
    private HashMap<String, Object> getHashMapForGetVariable(String name) {
		if(indicators == null || !indicators.containsKey(name)) {
			return null;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("name", name);
		result.put("action", "get");
		result.put("value", typeToObject(indicators.get(name)));
    	return result;
    }

    /**
     * Returns a HashMap representing a request for an indicator
     * @params name The name of the indicator
     */
    private HashMap<String, Object> getHashMapForGetVariable(String name, String type) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("name", name);
		result.put("action", "get");
		result.put("value", type);
    	return result;
    }

    /**
     * Returns a HashMap representing a request to update a control
     * @params name The name of the control
     * @params value The new value
     */
    private HashMap<String, Object> getHashMapForSetVariable(String name, Object value) {
		if(controls == null || !controls.containsKey(name)) {
			return null;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("name", name);
		result.put("action", "set");
		result.put("value", value);
    	return result;
    }

    /**
     * Returns a new object of type described by <i>type</i>  
     * @params type The name of the type
     * @params value The new value
     */
    private Object typeToObject(String type) {
    	if(type.compareTo("boolean")==0) { 
    		return false;
    	} else if(type.compareTo("i4")==0 || type.compareTo("int")==0) {    		
    		return new Integer(0);
    	} else if(type.compareTo("double")==0) {
    		return new Double(0.0);
    	} else if(type.compareTo("string")==0) {
    		return "";
    	} else if(type.compareTo("base64")==0) {
    		return new byte[1];
    	} else if(type.compareTo("dateTime.iso8601")==0) {
    		return new java.util.Date();
    	} else {
    		return null;
    	}
    }

    public static String XmlRpcToJavaType(String type) {
    	if(type.compareTo("boolean")==0) {     		
    		return "boolean";
    	} else if(type.compareTo("i4")==0 || type.compareTo("int")==0) {    		
    		return "int";
    	} else if(type.compareTo("double")==0) {
    		return "double";
    	} else if(type.compareTo("string")==0) {
    		return "String";
    	} else if(type.compareTo("base64")==0) {
    		return "base64";
    	} else if(type.compareTo("dateTime.iso8601")==0) {
    		return "java.util.Date";
    	} else {
    		return "null";
    	}
    }
    
    private Object getVarType(String name) {
    	if(indicators == null || !indicators.containsKey(name)) {
    		return null;
    	}
    	return typeToObject(indicators.get(name));
    }

    /**
     * Reports whether is connected with JIL server.
     * @return A boolean indicating whether it is connected.
     */
    public boolean isConnected() {
    	return (state != State.IDLE);
    }

    /**
     * Reports whether target VI is running.
     * @return A boolean indicating whether the target VI is running.
     */
    public boolean isRunning() {
    	return (state == State.RUNNING);
    }

    /**
     * Reports whether target VI is opened.
     * @return A boolean indicating whether the target VI is opened.
     */
    public boolean isOpened() {
    	return (state == State.OPENED) || (state == State.RUNNING);
    }

   	public HashMap<String,String> getControls() {
   		if(controls == null) return null;
   		return (HashMap<String,String>)controls.clone();
   	}

   	public HashMap<String,String> getIndicators() {
   		if(indicators == null) return null;
   		return (HashMap<String,String>)indicators.clone();
   	}

	public boolean getBoolean(String name) {
		return (Boolean)getVariable(name);
	}

	public int getInt(String name) {
		return (Integer)getVariable(name);
	}

	public float getFloat(String name) {
		return (Float)getVariable(name);
	}

	public double getDouble(String name) {
		return (Double)getVariable(name);
	}

	public String getString(String name) {
		return (String)getVariable(name);		
	}

	public void setValue(String name, boolean value) { 
		setVariable(name, value);	
	}

	public void setValue(String name, int value) {
		setVariable(name, value);	
	}

	public void setValue(String name, float value) {
		setVariable(name, value);	
	}

	public void setValue(String name, double value) {
		setVariable(name, value);	
	}

	public void setValue(String name, String value) {
		setVariable(name, value);			
	}   

	// To Do: Mover a otra parte?
   public boolean openVI() { return false; };
   
   public void setValuesLater() {}

   public void getValuesLater() {}
   
   public void setValues() {}

   public void getValues() {}
   
   public boolean step() {
	   boolean result = false;
	   if (isConnected() && isRunning()) {
		   result = true;
		   setValuesLater();
		   getValues();
	   }
	   return result;
   }

   // Nanoseconds time
   private long time = 0;
   private boolean stop = false;
   private Thread thread = null;
   private long period = 100; // milliseconds
   private java.util.concurrent.ScheduledThreadPoolExecutor scheduler;
   
   /**
    * Call the method step every <i>Ts</i> milliseconds
    */
   @Override
   public void run() {
	   long currentTime = System.nanoTime();
	   time = currentTime;
	   step();
   }

   public void setPeriod(long period) {
	   this.period = period;
   }
   
   /**
    * Start the thread 
    */
   public void start() {
	   stop = false;
	   if(scheduler == null ) {
		   scheduler = new java.util.concurrent.ScheduledThreadPoolExecutor(1);
		   scheduler.scheduleAtFixedRate(this, 0L, period, TimeUnit.MILLISECONDS);
	   }
   }

   /**
    * Stop the thread 
    */
   public void stop() {
	   stop = true;
	   scheduler.remove(this);
   }
	
}
