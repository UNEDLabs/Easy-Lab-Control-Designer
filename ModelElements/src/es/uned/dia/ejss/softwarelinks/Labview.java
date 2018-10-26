package es.uned.dia.ejss.softwarelinks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;

/**
 * Class to interact with a JIL Server
 * @author Jes�s Chac�n <jcsombria@gmail.com> 
 */
public class Labview {  
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
    private static final String METHODNAME_CLOSEVI = "jil.closevi"; 
    private static final String METHODNAME_SYNCVI = "jil.syncvi"; 
    private static final String METHODNAME_DISCONNECT = "jil.disconnect"; 

    private enum State { IDLE, CONNECTED, OPENED, RUNNING };
    private State state = State.IDLE;
    
    /**
     * Create a new Labview Object 
     * @param url The url of the server
     */
  public Labview(String url) {
      setServerAddress(url);
  }
  
    /**
     * Create a new Labview Object 
     * @param url The url of the server
     */
  public Labview(URL url) {
      setServerAddress(url);
  }
  
    /**
     * Set the <i>url</i> of the server 
     * @param url The url of the server
     */
  public void setServerAddress(String url) {
      try {
        setServerAddress(new URL(url));
      } catch(MalformedURLException e) {
        System.out.println(e.getMessage());
      }
  }

    /**
     * Set the <i>url</i> of the server 
     * @param url The url of the server
     */
  public void setServerAddress(URL url) {
    /* XmlRpcClient Configuration */
      XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setServerURL(url);
    config.setConnectionTimeout(30000);
    config.setReplyTimeout(30000);
    config.setEnabledForExtensions(true);
//      config.setGzipRequesting(true);
    /* Create the XmlRpcClient */    
      client = new XmlRpcClient();
      client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
      client.setConfig(config);
  }

    /**
     * Enable or disable the Gzip compressing of the data   
     * @param url The url of the server
     * @return    <i>true</i> if the property has been correctly changed, false otherwise.  
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
     * @return    <i>true</i> if the object is connected with the server as a result of the call, <i>false</i> otherwise.     
     */
    public boolean connect() {
      if(state != State.IDLE) return false;
    try {
      Object[] params = new Object[]{};
      HashMap result = (HashMap)client.execute(METHODNAME_CONNECT, params);
      System.out.println("Version: "+result.get("version")+", SessionID:"+ result.get("sessionID"));
      state = State.CONNECTED;
    } catch (XmlRpcException e) {
      System.err.println(e);
      return false;
    } 
    return true;
    }
    
    public void authenticate() {
    try {
      Object[] params = new Object[]{};
      Object result = client.execute(METHODNAME_AUTHENTICATE, params);  
      System.out.println(result.toString());
    } catch (XmlRpcException e) {
      System.err.println(e);
      //return false;
    } 
    }

    /**
     * Open a new VI 
     * @param name The path to the VI
     */
    public boolean openVI(String name) {
      if(state != State.CONNECTED) return false;    
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
      if(state != State.OPENED) return false;     
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
      if(state != State.RUNNING) return false;    
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
      switch(state) {
      case IDLE:
      case CONNECTED:
        return false;
      case RUNNING:
        stopVI();
      case OPENED:
      default:
      }       
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
      switch(state) {
      case IDLE:
      case CONNECTED:
        return false;
      case RUNNING:
      case OPENED:
        break;
      default:
      }       
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
      if(state == State.RUNNING) {
        stopVI();
      }
      if(state == State.OPENED) {
        closeVI();
      }
      if(state != State.CONNECTED) {
        return false;
      }
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
      for(Object o : result) {
        HashMap variable = (HashMap) o;
        if(variable != null) {
          System.out.println("[" + variable.get("name") + ", " + variable.get("value") + "]");
        }
      }
      return result;
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
    
    private Object getVarType(String name) {
      if(indicators == null || !indicators.containsKey(name)) {
        return null;
      }
      return typeToObject(indicators.get(name));
    }

    public boolean isConnected() {
      return (state != State.IDLE);
    }

    public boolean isRunning() {
      return (state == State.RUNNING);
    }
    
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
    
    public boolean openVI() {
    return false;
  };
}