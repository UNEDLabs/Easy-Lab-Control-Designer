package es.uned.dia.softwarelinks.utils;



  import java.io.IOException;
//import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.glassfish.json.JsonProviderImpl;
import org.colos.ejs.library.Model;
import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.value.BooleanValue;
import org.colos.ejs.library.control.value.DoubleValue;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.ObjectValue;
import org.colos.ejs.library.control.value.StringValue;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejs.library.server.utils.models.MetadataActionMetadataResponseModel;
import org.colos.ejs.library.server.DataMapExportable;
import org.colos.ejs.library.server.utils.models.MetadataActionDataResponseModel;
import org.colos.ejs.library.server.utils.models.MetadataActuatorMetadataResponseModel;
import org.colos.ejs.library.server.utils.models.MetadataModel;
import org.colos.ejs.library.server.utils.models.MetadataSensorDataResponseModel;
import org.colos.ejs.library.server.utils.models.MetadataSensorMetadataResponseModel;
import org.colos.ejs.library.server.utils.models.MetadataSensorResponseDataModel;
import org.colos.ejs.model_elements.input_output.WebSocketUtil;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
//import org.glassfish.json.JsonProviderImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A base interface for the graphical user interface of a simulation
 */

@SuppressWarnings("unused")
public class SDLinker {
  static private final int MAX_WAIT = 60;

  public enum Message { Reset, Initialize, Update, Collect, View_Method, Set_Property, Element_Method, Element_Map, Metadata_Info, SDS };  
  private JsonProviderImpl aux = new JsonProviderImpl();
  private WebSocketServer mCommSocket;
  private Simulation mSimulation;
  private Hashtable<String, String> mVariables = new Hashtable<String, String>();
//  private Hashtable<String, SocketViewElement> mElements = new Hashtable<String, SocketViewElement>();
  private List<String> mVariableList = new ArrayList<String>();
//  private List<String> mActionList = null;
  private WebSocketUtil mWebSocketUtil;
  private ObjectMapper mJSONmapper = new ObjectMapper();
  private Hashtable<WebSocket,Boolean> mAllowSendList = new Hashtable<WebSocket,Boolean>();
  private Hashtable<WebSocket,Boolean> mClientSmartDevice = new Hashtable<WebSocket,Boolean>();
//  private Hashtable<WebSocket,String> mSendPendingList = new Hashtable<WebSocket,String>();
  //private String modelMetadata = "";
  
  public SDLinker(Simulation simulation) {
    //mCommSocket = mComm.retrieveServer();
    //super(simulation);
    //mCommSocket = new WebSocketServer(new InetSocketAddress(port)) {
      //public void onMessage(WebSocket conn, String message) { processInputData(conn,message); }
    //};
    mSimulation = simulation;
    //connect();
  }
  
  public SDLinker(WebSocketUtil mComm,Simulation simulation) {
    mWebSocketUtil = mComm;
    mCommSocket = mWebSocketUtil.getServer();
    //super(simulation);
    //mCommSocket = new WebSocketServer(new InetSocketAddress(port)) {
      //public void onMessage(WebSocket conn, String message) { processInputData(conn,message); }
    //};
    mSimulation = simulation;
    //connect();
  }
  
  public void setWebSocket(WebSocketUtil mComm){
    mWebSocketUtil = mComm;
    mCommSocket = mWebSocketUtil.getServer();
    //connect();
  }

  public void release() {
    try { mCommSocket.stop(); } 
    catch (IOException e) { e.printStackTrace(); }
  }
  
  public void onOpen(WebSocket conn) {
    System.out.println("Connected to "+ conn.getRemoteSocketAddress().getAddress().getHostAddress());
    mAllowSendList.put(conn, true);
    mClientSmartDevice.put(conn, true);
  }
  public void onClose(WebSocket conn) {
    System.out.println("Connection closed by "+conn.getRemoteSocketAddress().getAddress().getHostAddress());
    mAllowSendList.remove(conn);
    if (mCommSocket.connections().isEmpty()) { // Exit if no view connects in MAX_WAIT seconds 
      new Thread(new Runnable() {
        public void run() { waitForConnection(MAX_WAIT); }
      }).start();
    }
  }
  public void onError(WebSocket conn, Exception ex) {
    if (conn==null && mCommSocket.connections().isEmpty()) { 
      System.err.println("General communication error. Exiting!");
      ex.printStackTrace();
      System.exit(1);
    }
    System.err.println("Communication error with "+conn.getRemoteSocketAddress().getAddress().getHostAddress());
    ex.printStackTrace();
  }
  
    
  public void connect() {
    mCommSocket.start();
    System.out.println("Model server "+this.mSimulation.getJarName() +" started on " + mCommSocket.getAddress());
    waitForConnection(MAX_WAIT); // Exit if no view connects in MAX_WAIT seconds 
    //modelMetadata = getMetadataInfo();
  }

  // ----------------------------------------------------------------------
  // Communication interface
  //----------------------------------------------------------------------
  
  public void sendCommand(Message type, Object dataMap) {
    String output = null;
    
    //System.out.println("Type: " + type + "dataMap" + dataMap);
    switch (type) {
      case Reset          : output = "R"; break;
      case Initialize     : output = "I"; break;
      case Update         : output = "U"; break;
      case Collect        : output = "C"; break;
      case View_Method    : output = "M"; break;
      case Set_Property   : output = "P"; break;
      case Element_Method : output = "E"; break;
      case Element_Map    : output = "F"; break;
      case Metadata_Info  : output = "D"; break;
      case SDS            : output = "";  break;
      default : return; // Ignored
    }
    if (dataMap!=null) 
      try { 
        if(dataMap instanceof JsonObject){
          output += ((JsonObject) dataMap).toString();
        }else{
          //System.out.println("Not Json dataMap: " + mJSONmapper.writeValueAsString(dataMap));
          output += mJSONmapper.writeValueAsString(dataMap); 
        } 
      }catch (JsonProcessingException e) {
        System.err.println("SocketView update() : error parsing output!");
        System.out.println("Not Json : " + dataMap);
        e.printStackTrace();
        return;
      }catch (ClassCastException err){
        System.err.println("Casting Map into Json");
        return;
      }
    //System.out.println (" <-- Sending output: "+ output);
    if (type==Message.Update || type==Message.Collect) { // Send only to iddle connections
      Set<WebSocket> connectionList = mCommSocket.connections();
      synchronized (connectionList) {
        for (WebSocket conn : connectionList) {
          if (!mClientSmartDevice.get(conn)){
            if (mAllowSendList.get(conn)) conn.send(output);
            else System.out.println("Cannot send to "+conn.toString());
          }
        }
      }
    }else if(type==Message.SDS){
      Set<WebSocket> connectionList = mCommSocket.connections();
      synchronized (connectionList) {
        for (WebSocket conn : connectionList){
          if (mClientSmartDevice.get(conn)){
            conn.send(output);
          }
        }
      }
    }else{
      Set<WebSocket> connectionList = mCommSocket.connections();
      synchronized (connectionList) {
        for (WebSocket conn : connectionList){
          if (!mClientSmartDevice.get(conn)){
            conn.send(output);
          }
        }
      }
    }
    
  }

  // ----------------------------------------------------------------------
  // private methods
  //----------------------------------------------------------------------
  //JSV
  private boolean standalone = true;
  
  private void waitForConnection(int seconds) {
    try { Thread.sleep(1000); } catch (InterruptedException e1) { e1.printStackTrace(); }
    int counter=0;
    while (mCommSocket.connections().isEmpty()) {
      try {
        counter++;
        if (counter>MAX_WAIT) {
          if (!standalone){
            System.err.println("No view connected in "+seconds+" seconds. Exiting!");
            System.exit(1);
          }else{
            System.err.println("No view connected in last "+seconds+" seconds");
            counter = 0;
          }
        }
        if (!standalone)  System.err.println("Waiting for connection at port:" +mCommSocket.getPort()+" for "+counter+"/"+seconds+" seconds...");
        Thread.sleep(1000);
      } catch (InterruptedException e) { e.printStackTrace(); }
    }
  }

  @SuppressWarnings("rawtypes")
  protected JsonObject jsonizeObject(Object value){
    JsonObjectBuilder outJson = Json.createObjectBuilder();
    //System.out.println("jsonizing Object " + value.getClass().toString());
    if(value instanceof Double){
      outJson.add("value", (Double) value);
    }else if(value instanceof Integer){
      outJson.add("value", (Integer) value);
    }else if(value instanceof String){
      outJson.add("value", (String) value);
    }else if(value instanceof Boolean){
      outJson.add("value", (Boolean) value);
    }else if(value instanceof Array){
      JsonArrayBuilder arrayOut = Json.createArrayBuilder();
      for(int i = 0; i<((ArrayList) value).size();i++){
        arrayOut.add(jsonizeObject(((ArrayList) value).get(i)));
      }
      outJson.add("value", arrayOut.build());
    }else{
      System.out.println("jsonizeObject : " + value.getClass().toString() +  "  -> No Idea of what to do with you");
    }
    return outJson.build();
  }
  
  protected JsonObject getOutputData(boolean isSDS) {
    if (mVariableList == null) return null;
    JsonObjectBuilder dataMap = Json.createObjectBuilder();
    Model model = mSimulation.getModel();
    Class<?> theClass = model.getClass();
    JsonArrayBuilder varNames = Json.createArrayBuilder();
    JsonArrayBuilder varValues = Json.createArrayBuilder();
    for (String variable : mVariableList) {
      try { // Only non expressions
        Field varField = theClass.getDeclaredField(variable);
        Object val = varField.get(model);
        JsonValue value = jsonizeObject(val).get("value");
        if (value==null) continue; // ignore null values
//        System.out.println("Value of " + variable + " = " + value);
        if (value instanceof DataMapExportable) {
          if(!isSDS)  dataMap.add(variable, ((DataMapExportable)value).toDataMap().toString());
          else{
            varNames.add(variable);
            varValues.add((JsonValue) ((DataMapExportable)value).toDataMap());
          }
        }else{
          if(!isSDS)  dataMap.add(variable, value);
          else{
            varNames.add(variable);
            varValues.add(value);
          }
        }
      } catch (Exception exc) {
        System.err.println("Field " + variable + " does not exist in this model. Will be ignored"); 
      }
    }
    Map<String, Object> auxMap = new HashMap<String, Object>();
    model._addToHTMLOutputData(auxMap);
    for(String key : auxMap.keySet()){
      if(!isSDS)  dataMap.add(key, auxMap.get(key).toString());
      else{
        varNames.add(key);
        varValues.add(jsonizeObject(auxMap.get(key)).get("value"));
      }
    }
    if(isSDS){
      dataMap.add("method","getSensorData");
      dataMap.add("sensorId","modelVars");  
      dataMap.add("responseData",Json.createObjectBuilder()
          .add("valueNames", varNames.build())
          .add("data", varValues.build())
          .build());
      //dataMap.add("valueNames", varNames.build());
      //dataMap.add("data", varValues.build());   
    }
    return dataMap.build();
  }

  /*protected Map<String, Object> getOutputData() {
    if (mVariableList == null) return null;
    Map<String, Object> dataMap = new HashMap<String, Object>();
    Model model = mSimulation.getModel();
    Class<?> theClass = model.getClass();
    for (String variable : mVariableList) {
      try { // Only non expressions
        Field varField = theClass.getDeclaredField(variable);
        Object value = varField.get(model);
        if (value==null) continue; // ignore null values
//        System.out.println("Value of " + variable + " = " + value);
        if (value instanceof DataMapExportable) {
          dataMap.put(variable, ((DataMapExportable)value).toDataMap());
        }
        else dataMap.put(variable, value);
      } catch (Exception exc) {
        System.err.println("Field " + variable + " does not exist in this model. Will be ignored"); 
      }
    }
    model._addToHTMLOutputData(dataMap);
    return dataMap;
  }*/
  
  protected JsonObject getOutputData(String name) {
    //Map<String, Object> dataMap = new HashMap<String, Object>();
    JsonObjectBuilder dataMap = Json.createObjectBuilder();
    Model model = mSimulation.getModel();
    Class<?> theClass = model.getClass();
    try { // Only non expressions
      Field varField = theClass.getDeclaredField(name);
      Object value = varField.get(model);
      if (value==null) dataMap.build(); // ignore null values
      if (value instanceof DataMapExportable) {
        dataMap.add(name, ((DataMapExportable)value).toDataMap().toString());
      }
      else dataMap.add(name,value.toString());
    } catch (Exception exc) {
      System.err.println("Field " + name + " does not exist in this model. Will be ignored"); 
    }
    //model._addToHTMLOutputData(dataMap);
    return dataMap.build();
  }
  //JSV_Changes

  public void processInputData(WebSocket conn, String input) {
    System.out.println (" --> Processing input: "+ input); 
    switch(input.charAt(0)) {
      case '{' : //Metadata is sent using the SDS, then I can process the message directly
        JsonReader jsonReader = Json.createReader(new StringReader(input));
        JsonObject msgLab = jsonReader.readObject();
        jsonReader.close();
        mClientSmartDevice.put(conn, true);
        String methodInfo = "";
        MetadataModel resp;
        MetadataModel respData;
        if (msgLab.containsKey("method")){
          methodInfo = msgLab.getString("method");
          //System.out.println("Method: " + methodInfo);
          //Now this structure will be useful, may be the next step could be use reflection 
          if(methodInfo.equals("getSensorMetadata")){
            JsonArray dataVarList = getModelVarsList("out");
            resp = new MetadataSensorMetadataResponseModel("getSensorMetadata",dataVarList);
            sendCommand(Message.SDS, resp.getJSONModel());
          }else if(methodInfo.equals("getActuatorMetadata")){
            JsonArray dataVarList = getModelVarsList("in");
            resp = new MetadataActuatorMetadataResponseModel("getActuatorMetadata",dataVarList);
            sendCommand(Message.SDS, resp.getJSONModel());
          }else if(methodInfo.equals("getSensorActuatorMetadata")){
            JsonArray dataVarList = getModelVarsList("public");
            resp = new MetadataActuatorMetadataResponseModel("getSensorActuatorMetadata",dataVarList);
            sendCommand(Message.SDS, resp.getJSONModel());
          }else if(methodInfo.equals("getMetadata")){
            JsonObject metadata = getMetadata();
            sendCommand(Message.SDS, metadata);
          }else if(methodInfo.equals("getActionsMetadata")){
            JsonArray dataVarList = getModelMethList();
            resp = new MetadataActionMetadataResponseModel("ActionMetadataResponse",dataVarList);
            sendCommand(Message.SDS, resp.getJSONModel());
          }else if(methodInfo.equals("callAction")){
            //JsonArray dataVarList = getModelMethList();
            String nickName = msgLab.getString("nickName").trim();
            if(msgLab.containsKey("params"))
              invokeMethod(nickName, msgLab.getJsonArray("params").toArray());
            else{
              invokeMethod(nickName,null);
            }
            resp = new MetadataActionDataResponseModel("callAction",nickName);
            sendCommand(Message.SDS, resp.getJSONModel());
          
          }else if(methodInfo.equals("getSensorData")){
            String sensorId = msgLab.getString("sensorId");
            if(msgLab.containsKey("configuration")){
              //System.out.println("Full getSensorData : " + msgLab);
              JsonArray configurations = msgLab.getJsonArray("configuration");
              for(int i = 0; i<configurations.size();i++){
                if(mVariableList==null)   mVariableList = new ArrayList<String>();
                JsonObject config = configurations.getJsonObject(i);
                if((config.getString("parameter").equals("updateFrequency")
                    && ((double) config.getJsonNumber("value").doubleValue()) > 0.0)){
                  mVariableList.add(sensorId);
                }
              }
            }
            resp = new MetadataSensorDataResponseModel("getSensorData",sensorId);
            respData = new MetadataSensorResponseDataModel();
            ((MetadataSensorResponseDataModel) respData).loadSensorResp(
                Json.createArrayBuilder().add(sensorId).build(),
                Json.createArrayBuilder().add(getOutputData(sensorId)).build(),
                Json.createArrayBuilder().add(new Date().toString()).build()
                );
            ((MetadataSensorDataResponseModel)resp).loadSensorDataResponse(
                "getSensorData", 
                sensorId, 
                "", 
                respData.getJSONModel(), 
                Json.createObjectBuilder().build(), 
                Json.createObjectBuilder().build());
            sendCommand(Message.SDS,resp.getJSONModel());
            
          }else if(methodInfo.equals("sendActuatorData")){
            //original = 
            //JsonArray nameVars = msgLab.getJsonArray("valueNames");
            JsonArray nameVars = msgLab.getJsonObject("payload").getJsonArray("valueNames");
            JsonArray valueVars = msgLab.getJsonObject("payload").getJsonArray("data"); 
            for (int i = 0; i<nameVars.size();i++){
              try {
                System.out.println("nameVars : " + nameVars.toString() + " valueVars: " + valueVars.toString());
                setVariable((String) nameVars.getString(i),valueVars.get(i));
                mSimulation.updateAfterModelAction();
              } catch (Exception exc) {
                System.err.println("Error processing interaction command: " + input);
                exc.printStackTrace();
              }
            }
          }
        }
        break;
      default:
        mClientSmartDevice.put(conn, false);
        processNotSmartData(conn,input);
        break;
    }
  }
 
  @SuppressWarnings("unchecked")
  private void processNotSmartData(WebSocket conn, String input){
   // System.out.println (" --> Processing no smart input: "+ input); 
    switch(input.charAt(0)) {
      case 'A' : // API
        try {
          Map<String,Object> dataMap = mJSONmapper.readValue(input.substring(1), Map.class);
          mVariableList = (List<String>) dataMap.get("variables");
          //System.out.println("VariablesList : " + mVariableList);
//          mActionList = (List<String>) dataMap.get("actions");
          //System.out.println ("variables class = "+variableList.getClass());
        } catch (Exception exc) {
          System.err.println("Error processing api: " + input);
          exc.printStackTrace();
        }
        if (mCommSocket.connections().isEmpty()) mSimulation.reset();
        update(); // Send the value of the variables for the first time
        break;
      case 'I' : // Interaction
        try {
          //System.out.println("Interaction" + mJSONmapper.readValue(input.substring(1), Map.class));
          Map<String,Object> dataMap = mJSONmapper.readValue(input.substring(1), Map.class);
          List<Map<String,Object>> propertiesList = (List<Map<String, Object>>) dataMap.get("properties");
          for (Map<String,Object> propertyChange : propertiesList) setVariable((String) propertyChange.get("name"), propertyChange.get("value"));
          List<Map<String,Object>> actionsList = (List<Map<String, Object>>) dataMap.get("actions");
          for (Map<String,Object> actionInvocation : actionsList) invokeMethod((String) actionInvocation.get("name"), actionInvocation.get("argument"));
          mSimulation.updateAfterModelAction();
        } catch (Exception exc) {
          System.err.println("Error processing interaction command: " + input);
          exc.printStackTrace();
        }
        break;
      case 'O' : // Ok = communication iddle
        mAllowSendList.put(conn, true);
//      String pendingOutput = mSendPendingList.get(conn);
//      if (pendingOutput!=null) {
//        System.out.println("Trying to resend to "+conn.toString());
//        sendOutputIfAllowed(conn,pendingOutput);
//      }
        break;
      case 'D' : // Metadata info as a response to a petition 
        Map<String, Object> dataMap = new HashMap<String, Object>();
        JsonArray dataMapMeth = getModelMethList();
        JsonArray varArray = getModelVarsList("public");
        dataMap.put("variables" , varArray);
        dataMap.put("methods",dataMapMeth);
        sendCommand(Message.Metadata_Info, dataMap);
        break;
      default:
        break;
    }
  }
  
  //All These function will be overwritten by *serverView when the Java files are generated
  //Then the final definition depends of some user-edition constraints.
  //JSV_Changes
  /***This function will be overwritten when the simulation begins!
   * Inspect the declared variables and returns all of them with no "_" in their names
   * @return ArrayList<String> easy to map into json
   */
  public JsonArray getModelVarsList(String type){
    JsonArray varArray = Json.createArrayBuilder().build();
    return varArray;
  }
  public JsonArray getModelMethList(){
    JsonArray Methods = Json.createArrayBuilder().build();
    return Methods;
  }
  public JsonObject getMetadata(){
    JsonObject Methods = Json.createObjectBuilder().build();
    return Methods;
  }
  
  private Object checkCast(Object value){
    if(value instanceof JsonValue){
      switch(((JsonValue) value).getValueType()){
        case ARRAY:
          value = ((JsonArray) value).toArray();
          //System.out.println("Class of the Value Array");
          break;
        case FALSE:
          value = false;
          System.out.println("FALSE value, casting to false boolean");
          break;
        case TRUE:
          value = true;
          System.out.println("TRUE value, casting to true boolean");
          //System.out.println("Class of the Value Boolean");
          break;
        case NUMBER:
          value = (((JsonNumber) value));
          try{
            value = (((JsonNumber) value)).doubleValue();
            //System.out.println("Class of the Value double");
          }catch(Exception e){
              try{
              value = (((JsonNumber) value)).intValue();
              //System.out.println("Class of the Value int");
            }catch(Exception e2){
              System.out.println("Not int, not double");
            }
          }
          break;
        case STRING:
          value = ((JsonString) value).getString();
          //System.out.println("Class of the Value String");
          break;
        case OBJECT:
          //System.out.println("Class of the Value Object");
          break;
        case NULL:
          value = null;
          break;
        default:
          System.out.println("JsonValue : " + value + " is not a basic type, ignoring");
          break;
      }
    }
    return value;
    
  }

  private void setVariable(String name, Object value) {
    value = checkCast(value);
    Model model = mSimulation.getModel();
    Class<?> theClass = model.getClass();
    Field varField;
    try {
      varField = theClass.getDeclaredField(name);
      //System.out.println("Class of the Variable to define: " + varField.getType());
      //System.out.println("Class of the Value to use: " + value.getClass());
      if(value instanceof String){
        if(varField.getType().getName().equals("double")){
          //System.out.println("Parsing double from string ");
          try{
            value = Double.parseDouble((String)value);
          }catch(Exception e){
            value = theClass.getDeclaredField(name).get(model);
            //Recover from crash
            this.sendCommand(Message.Update,(new HashMap<String,Object>()).put(name,value));
            //System.out.println("Ignoring setting model variable");
          }
        }else if(varField.getType().getName().equals("int")){
          //System.out.println("Parsing double from int ");
          try{
            value = Integer.parseInt((String)value);
          }catch(Exception e){
            value = theClass.getDeclaredField(name).get(model);
            //Recover from crash
            this.sendCommand(Message.Update,(new HashMap<String,Object>()).put(name,value));
            //System.out.println("Ignoring setting model variable");
          }
        }
      }
      //System.err.println("Setting model variable: "+name+ " = "+value);
      varField.set(model, value);
    } catch (SecurityException e) {
      System.err.println ("SocketView setVariable security exception error : Variable : "+name+" ( "+value+")");
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      System.err.println ("SocketView setVariable no such field error : Variable : "+name+" ( "+value+")");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      System.err.println ("SocketView setVariable illegal argument error : Variable : "+name+" ( "+value+")");
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      System.err.println ("SocketView setVariable illegal access error : Variable : "+name+" ( "+value+")");
      e.printStackTrace();
    }
  }

  private void invokeMethod(String methodName, Object arg1) {
    Method method=null;
    Object[] parameterList = {};
    Model model = mSimulation.getModel();
    Class<?> theClass = model.getClass();
    try { // try a void method
      Class<?>[] argList = {};
      method = theClass.getMethod(methodName, argList);
    }
    catch (Exception exc) { // Try with a single parameter
      try {
        Class<?>[] argList = new Class[] { Object.class };
        method = theClass.getMethod(methodName, argList);
        parameterList = new Object[] { arg1 };
      } catch (Exception exc2) {
        System.err.println ("SocketView invokeMethod error : Could not find or run method : "+methodName+" () nor "+methodName+"( "+arg1+")");
        exc2.printStackTrace();
        return;
      }
    }
    // Once here, invoke the method
    try {
      method.invoke(model, parameterList);
    } catch (IllegalAccessException e) {
      System.err.println ("SocketView invokeMethod error : Illegal access for method: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      System.err.println ("SocketView invokeMethod error : Illegal argument for method: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      System.err.println ("SocketView invokeMethod error : Invocation target error for method: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  private void invokeMethodVoid(String methodName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
  //System.err.println("Calling model method: "+methodName+ " with no argument");
  Class<?>[] argList = {};
  Object[] parameterList = {};
  Model model = mSimulation.getModel();
  Class<?> theClass = model.getClass();
  Method method = theClass.getMethod(methodName, argList);
  method.invoke(model, parameterList);
}

  @SuppressWarnings("unused")
  private void invokeMethodWithArgument(String methodName, Object arg1) {
//    System.err.println("Calling model method: "+methodName+ " with argument: class = "+arg1.getClass()+" = "+ arg1);
    Class<?>[] argList = new Class[] { Object.class };
    Object[] parameterList = new Object[] { arg1 };
    Model model = mSimulation.getModel();
    Class<?> theClass = model.getClass();
    Method method;
    try {
      method = theClass.getMethod(methodName, argList);
      method.invoke(model, parameterList);
    } catch (SecurityException e) {
      System.err.println ("SocketView invokeMethod security exception error : Method : "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      System.err.println ("SocketView invokeMethod error : Method not found: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      System.err.println ("SocketView invokeMethod error : Illegal argument for method: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      System.err.println ("SocketView invokeMethod error : Illegal access for method: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      System.err.println ("SocketView invokeMethod error : Invocation target error for method: "+methodName+" ( "+arg1+")");
      e.printStackTrace();
    }
  }
  
  // ----------------------------------------------------------------------
  // 
  //----------------------------------------------------------------------
  
  /**
   * Adds an element to the view
   * 
   * @param name
   * @return
   */
  /*public SocketViewElement addElement(SocketViewElement element, String name) {
    super.addElement(element, name);
    element.setView(this);
    element.setName(name);
//    mElements.put(name, element);
    return element;
  }*/

  protected void addListener(String variable) {
//    mVariables.put(variable, "0");
  }

  // ---------------------------------------
  // Implementation of View
  // ----------------------------------------

  /**
   * Clearing any previous data
   */
  public void reset() { 
    sendCommand(Message.Reset,(Map<String,Object>) null); 
    sendCommand(Message.SDS,Json.createObjectBuilder().add("method", "reset").build());
  }

  /**
   * A softer reset. Calling reset makes initialize unnecessary
   */
  public void initialize() { 
    if(aux != null) aux = null;
    sendCommand(Message.Initialize,(Map<String,Object>)null); 
    sendCommand(Message.SDS,Json.createObjectBuilder().add("method", "initialize").build());
    }

  /**
   * Does the final update which makes the drawing complete. Typically used by
   * drawing panels for rendering
   */
  public void finalUpdate() { }

  /**
   * Accept data sent and graphic work
   */
  public void update() {
    //Map<String, Object> dataMap = getOutputData();
    JsonObject dataMapSDS= getOutputData(true);
    JsonObject dataMap= getOutputData(false);
    if (dataMap!=null){
      sendCommand(Message.Update,dataMap);
      sendCommand(Message.SDS,dataMapSDS);
    }
  }
  
  /**
   * Accept data sent but do not graphic work
   */
  public void collectData() { 
  //Map<String, Object> dataMap = getOutputData();
    JsonObject dataMapSDS= getOutputData(true);
    JsonObject dataMap= getOutputData(false);
    if (dataMap!=null){
      sendCommand(Message.Collect,dataMap);
      sendCommand(Message.SDS,dataMapSDS);
    }
  }

  // ------------------------------------------
  // Setting and getting values
  // ------------------------------------------

  // For the custom methods
  private BooleanValue booleanValue = new BooleanValue(false);
  private IntegerValue integerValue = new IntegerValue(0);
  private DoubleValue doubleValue = new DoubleValue(0.0);
  private StringValue stringValue = new StringValue("");
  private ObjectValue objectValue = new ObjectValue(null);

  // --------- Setting different types of values ------

  /**
   * A convenience method to set a value to a boolean
   * 
   * @param _name
   * @param _value
   */
  public void setValue(String _name, boolean _value) {
    booleanValue.value = _value;
    setVariable(_name, booleanValue);
  }

  /**
   * A convenience method to set a value to an int
   * 
   * @param _name
   * @param _value
   */
  public void setValue(String _name, int _value) {
    integerValue.value = _value;
    setVariable(_name, integerValue);
  }

  /**
   * A convenience method to set a value to a double
   * 
   * @param _name
   * @param _value
   */
  public void setValue(String _name, double _value) {
    doubleValue.value = _value;
    setVariable(_name, doubleValue);
  }

  /**
   * A convenience method to set a value to a String
   * 
   * @param _name
   * @param _value
   */
  public void setValue(String _name, String _value) {
    stringValue.value = _value;
    setVariable(_name, stringValue);
  }

  /**
   * A convenience method to set a value to any Object
   * 
   * @param _name
   * @param _value
   */
  public void setValue(String _name, Object _value) {
    if (_value instanceof String)
      setValue(_name, (String) _value);
    else {
      objectValue.value = _value;
      setVariable(_name, objectValue);
    }
  }

  // --------- Getting different types of values ------

  /**
   * A convenience method to get a value as a boolean
   * 
   * @param _name
   */
  public Value getValue(String _name) {
    String value = mVariables.get(_name);
    if (value == null)
      return null;
    return new StringValue(value);
  }

  /**
   * A convenience method to get a value as a boolean
   * 
   * @param _name
   */
  public boolean getBoolean(String _name) {
    String value = mVariables.get(_name);
    if (value == null)
      return false;
    if (value.toLowerCase().equals("true"))
      return true;
    return false;
  }

  /**
   * A convenience method to get a value as an int
   * 
   * @param _name
   */
  public int getInt(String _name) {
    try {
      return Integer.parseInt(mVariables.get(_name));
    } catch (Exception exc) {
      return 0;
    }
  }

  /**
   * A convenience method to get a value as a double
   * 
   * @param _name
   */
  public double getDouble(String _name) {
    try {
      return Double.parseDouble(mVariables.get(_name));
    } catch (Exception exc) {
      return 0;
    }
  }

  /**
   * A convenience method to get a value as a String
   * 
   * @param _name
   */
  public String getString(String _name) {
    String value = mVariables.get(_name);
    if (value == null)
      return "";
    return value;
  }

  /**
   * A convenience method to get a value as an Object
   * 
   * @param _name
   */
  public Object getObject(String _name) {
    String value = mVariables.get(_name);
    if (value == null)
      return null;
    return value;
  }


 
}
