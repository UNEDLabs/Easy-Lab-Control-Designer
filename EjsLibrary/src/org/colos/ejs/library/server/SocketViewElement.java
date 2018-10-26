package org.colos.ejs.library.server;

//import java.util.Date;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
//import javax.json.JsonObject;
import javax.json.JsonReader;

import org.colos.ejs.library.ConfigurableElement;
import org.colos.ejs.library.server.SocketView.Message;
//import org.colos.ejs.library.server.utils.models.MetadataActionDataResponseModel;
import org.colos.ejs.library.server.utils.models.MetadataActionRequestModel;
//import org.colos.ejs.library.server.utils.models.MetadataModel;
//import org.colos.ejs.library.server.utils.models.MetadataSensorDataResponseModel;
//import org.colos.ejs.library.server.utils.models.MetadataSensorResponseDataModel;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SocketViewElement extends DummyViewElement {

  private SocketView mView;
  private String mName;
  
  void setView(SocketView view) {
    mView = view;
  }
  
  void setName(String name) {
    mName = name;
  }

  public String getName() { return mName; }
  
  public ConfigurableElement setProperty(String property, String value) {
    System.out.println(mName+": wants to set property <"+property+"> to <"+value+">");
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.put("element", mName);
    dataMap.put("property", property);
    dataMap.put("value", value);
    mView.sendCommand(Message.SDS,dataMap);
    return this;
  }
  
  public void executeMethodVoid(String method) {
    executeMethodWithObject(method,null);
  }

  public void executeMethodWithObject(String method, Object data) {
    MetadataActionRequestModel resp = new MetadataActionRequestModel("callAction","viewElement");
    try {
      JsonReader jsonReader = Json.createReader(new StringReader((new ObjectMapper()).writeValueAsString(data)));
      JsonArray params = jsonReader.readArray();
      resp.loadActionRequest(
          "callAction", 
          mName,
          method,
          params
          );
      jsonReader.close();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    //System.out.println("executeMethodWithObject calling : " + resp.getJSONModel().toString());
    mView.sendCommand(Message.SDS,resp.getJSONModel());
  }
  
  public void executeMethodWithMap(String method, Map<String, Object> dataMap) {
    Map<String, Object> messageMap = new HashMap<String, Object>();
    messageMap.put("element", mName);
    messageMap.put("method", method);
    messageMap.put("data", dataMap);
    mView.sendCommand(Message.SDS, messageMap);
  }
}
