package org.colos.ejs.model_elements.input_output;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.server.DataMapExportable;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opensourcephysics.controls.OSPLog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulates access to a WebSocket server
 * @author Francisco Esquembre
 * @version 1.0, October 2015
 *
 */
public class EjsSServerSimulation extends WebSocketUtil {
  
  private Model mModel;
  private ObjectMapper mJSONmapper = new ObjectMapper();

  /**
   * Standard constructor to be called by the simulation
   * Connection must be started explicitly by calling start()
   */
  public EjsSServerSimulation(Model model) {
    mModel = model;
  }
    /**
   * Standard constructor to be called by the simulation
   * @param _model
   */
  public EjsSServerSimulation(Model model, int _portNumber) {
    mModel = model;
    start(_portNumber);
  }

  // Keep remote view informed of status
  synchronized public boolean sendStatus() {
    if (!isConnected()) return false;
    Map<String, Object> messageMap = new HashMap<String, Object>();
    if (mModel._isPlaying()) messageMap.put("_command", "_isPlaying");
    else messageMap.put("_command", "_isPaused");
    try { 
      String message = "_"+mJSONmapper.writeValueAsString(messageMap); 
//      Set<WebSocket> connections = mCommSocket.connections().clone();
      for (WebSocket conn : mCommSocket.connections()) conn.send(message);
    } 
    catch (JsonProcessingException e) {
      System.err.println("SocketView update() : error parsing output!");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  // Keep remote view informed of status
  synchronized public boolean sendConectionList(WebSocket socket) {
    if (!isConnected()) return false;
    Map<String, Object> messageMap = new HashMap<String, Object>();
    messageMap.put("_command", "_connectionList");
    try { 
      String[] list = super.getConnectionsIDs();      
      messageMap.put("list", list);
      socket.send("_"+mJSONmapper.writeValueAsString(messageMap));
    } 
    catch (JsonProcessingException e) {
      System.err.println("SocketView sendConnectionList() : error parsing output!");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  // ---------------------------------------
  // EjsS output API
  //---------------------------------------

  synchronized public boolean sendCommand(WebSocket socket, String keyword, Object... args) {
    if (!isConnected()) return false;
    //System.err.println("Server sending command "+keyword);
    Map<String, Object> messageMap = new HashMap<String, Object>();
    messageMap.put("_command", keyword);
    int n = 0;
    for (Object value : args) {
      n++;
      if (value instanceof DataMapExportable) {
        System.out.println("Warning: Argument["+n+"] = " + value + " is DataMapExportable");
        messageMap.put("arg"+n, ((DataMapExportable)value).toDataMap());
      }
      else messageMap.put("arg"+n, value);
    }
    try { 
      String message = "_"+mJSONmapper.writeValueAsString(messageMap); 
      socket.send(message);
    } 
    catch (JsonProcessingException e) {
      System.err.println("SocketView update() : error parsing output!");
      e.printStackTrace();
      return false;
    }
    sendStatus();
    return true;    
  }

  synchronized public boolean sendCommand(String keyword, Object... args) {
    if (!isConnected()) return false;
    //System.err.println("Server sending command "+keyword);
    Map<String, Object> messageMap = new HashMap<String, Object>();
    messageMap.put("_command", keyword);
    int n = 0;
    for (Object value : args) {
      n++;
      if (value instanceof DataMapExportable) {
        System.out.println("Warning: Argument["+n+"] = " + value + " is DataMapExportable");
        messageMap.put("arg"+n, ((DataMapExportable)value).toDataMap());
      }
      else messageMap.put("arg"+n, value);
    }
    try { 
      String message = "_"+mJSONmapper.writeValueAsString(messageMap); 
      for (WebSocket conn : mCommSocket.connections()) conn.send(message);
    } 
    catch (JsonProcessingException e) {
      System.err.println("SocketView update() : error parsing output!");
      e.printStackTrace();
      return false;
    }
    sendStatus();
    return true;    
  }

  synchronized public boolean sendVariables(String keyword, String... variables) {
    if (!isConnected()) return false;
    Map<String, Object> messageMap = new HashMap<String, Object>();
    messageMap.put("_command", keyword);
    Class<?> theClass = mModel.getClass();
    for (String variable : variables) {
      try { // Only non expressions
        Field varField = theClass.getDeclaredField(variable);
        Object value = varField.get(mModel);
        if (value==null) continue; // ignore null values
        //System.out.println("Value of " + variable + " = " + value);
        if (value instanceof DataMapExportable) {
          System.out.println("Warning: Variable " + variable + " is DataMapExportable");
          messageMap.put(variable, ((DataMapExportable)value).toDataMap());
        }
        else messageMap.put(variable, value);
      } catch (Exception exc) {
        System.err.println("Field " + variable + " does not exist in this model.");
      }
    }
    try { 
      String message = "_"+mJSONmapper.writeValueAsString(messageMap); 
      for (WebSocket conn : mCommSocket.connections()) conn.send(message);
    } 
    catch (JsonProcessingException e) {
      System.err.println("SocketView update() : error parsing output!");
      e.printStackTrace();
      return false;
    }
    sendStatus();
    return true;    
  }
  
  public boolean client_play()  { return sendCommand("_play"); }
  public boolean client_pause() { return sendCommand("_pause"); }
  public boolean client_step()  { return sendCommand("_step"); }
  public boolean client_reset() { return sendCommand("_reset"); }

  public boolean client_update()       { return sendCommand("_upate"); }
  public boolean client_initialize()   { return sendCommand("_initialize"); }
  public boolean client_resetSolvers() { return sendCommand("_resetSolvers"); }

  public boolean client_alert(String message)   { return sendCommand("_alert",message); }
  public boolean client_println(String message) { return sendCommand("_println",message); }
  
  // ---------------------------------------
  // EjsS input API
  //---------------------------------------
  
  public void processCommand (WebSocket socket, String keyword, Map<String,Object> data) {
    processCommand (keyword, data);
  }

  public void processCommand (String keyword, Map<String,Object> data) {
    System.out.println("EjsSServerSimulation : Command received = "+keyword + " with data = "+data); 
  }

  public boolean processDefaultAPI(WebSocket socket, String command, Map<String,Object> data) {
    if (command.charAt(0)!='_') return false; 
    if      (command.equals("_play"))  { mModel._play(); return true; }
    else if (command.equals("_pause")) { mModel._pause(); mModel.getSimulation().update(); return true; }
    else if (command.equals("_step"))  { mModel._step(); return true; }
    else if (command.equals("_reset")) { mModel._reset(); return true; }
    else if (command.equals("_update"))       { mModel.getSimulation().update(); return true; }
    else if (command.equals("_initialize"))   { mModel._initialize(); return true; }
    else if (command.equals("_resetSolvers")) { mModel._resetSolvers(); return true; }
    else if (command.equals("_sendConnectionList")) { sendConectionList(socket); return true; }
    else if (command.equals("_setID")) { setID(socket, data.get("id").toString()); return true; }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  synchronized public void processInput (WebSocket socket, String input) { 
    //System.err.println("Message received: "+input);
    if (input.charAt(0)=='_') { 
      try {
        Map<String,Object> messageMap = (Map<String,Object>) mJSONmapper.readValue(input.substring(1), Map.class);
        String keyword = (String) messageMap.get("_command");
        if (keyword==null) processCommand(socket,input,null);
        else {
          Map<String,Object> data = (Map<String,Object>) messageMap.get("_data");
          if (!processDefaultAPI(socket,keyword,data)) // Not a standard message
            processCommand(socket,keyword, data);
        }
      } catch (Exception exc) {
        System.err.println("Warning '_' input is not a command: " + input);
        exc.printStackTrace();
        processCommand(socket,input,null);
      }
    }
    else processCommand(socket,input,null);
    sendStatus();
  }
  
  /**
   * Utility function to extract a double variable from the map
   * @param dataMap
   * @param variable
   * @return
   */
  synchronized public double getDouble(Map<String,Object> dataMap, String variable) {
    Object object = dataMap.get(variable);
    try { return (double) object; } 
    catch (Exception exc) { 
      try { return (int) object; }
      catch (Exception exc2) { return Double.NaN; }
    }
  }
  
  /**
   * Utility function to extract a String variable from the map
   * @param dataMap
   * @param variable
   * @return
   */
  synchronized public String getString(Map<String,Object> dataMap, String variable) {
    if (dataMap==null) return null;
    Object object = dataMap.get(variable);
    if (object==null) return null;
    return object.toString();
  }  

}
