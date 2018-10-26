package es.uned.dia.softwarelinks.nodejs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class RIPExperienceInfo {
  private static final String WRITABLE = "writables";
  private static final String READABLE = "readables";
  private static final String METHODS = "methods";
  private static final String INFO = "info";
  private List<RIPMethod> methods = new ArrayList<>();
  private Map<String, String> readable = new HashMap<>();
  private Map<String, String> writable = new HashMap<>();
  private RIPInfo info;

  public static final String RIP_SSE = "RIP-LabVIEW";
  public static final String RIP_WEBSOCKETS = "RIP-Arduino";
  private String api = RIP_SSE;
  
  public RIPExperienceInfo() {
  }

  public RIPExperienceInfo(String input) {
    parse(input);
  }
  
  public void parse(String input) {
    InputStream stream;
    try {
      stream = new ByteArrayInputStream(input.getBytes("UTF-8"));
      JsonReader reader = Json.createReader(stream);
      JsonObject message = reader.readObject();
    
      loadInfo(message);
//      loadMethods(message);
      loadWritable(message);
      loadReadable(message);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("[WARNING] RIP Protocol not detected, switching to RIP-Websockets");
      api = RIP_WEBSOCKETS;
    }
  }  
  
  private void loadInfo(JsonObject message) {
    if(message.containsKey(INFO)) {
      JsonObject info = message.getJsonObject(INFO);
      this.info = new RIPInfo(info);
    } else {
      this.info = new RIPInfo(message);
    }
  }

//  private void loadMethods(JsonObject message) {
//    if(message.containsKey(METHODS)) {
//      JsonValue o = message.get(METHODS);
//      switch(o.getValueType()) {
//        case OBJECT:
//          JsonObject methods = (JsonObject)o;
//          Iterator<String> i = methods.keySet().iterator();
//          while(i.hasNext()) {
//            String method = i.next();
//            JsonObject info = methods.getJsonObject(method);
//            this.methods.add(new RIPMethod(info));
//          }
//          break;
//        case ARRAY:
//          JsonArray methodsList = (JsonArray)o;
//          Iterator<JsonValue> j = methodsList.iterator();
//          while(j.hasNext()) {
//            JsonObject method = (JsonObject)j.next();
//            System.out.println(method.toString());
//            try {
//              String name = method.getString("method");
//              RIPMethod newMethod = new RIPMethod(name, method);
//              this.methods.add(newMethod);
//            } catch(Exception e) {
//              System.out.println("Error");
//              System.out.println(e.getClass());
//            }
//          }
//          break;
//        default:                    
//      }
//
//    } else {
//      System.err.println("[WARNING] Methods not found");
//    }
//  }
  
  private void loadList(JsonObject message, String type) {
    String label;
    Map<String, String> store;
    switch(type) {
      case WRITABLE:
        label = WRITABLE;
        store = this.writable;
        break;
      case READABLE:
        label = READABLE;
        store = this.readable;
        break;
      default:
        return;
    }
    if(message.containsKey(label)) {
      JsonValue writable = message.get(label);
      switch(writable.getValueType()) {
        case OBJECT:
          JsonObject writableObject = (JsonObject)writable;
          JsonArray list = (JsonArray)writableObject.get("list");
          for(JsonValue o : list) {
            String varname = ((JsonObject)o).getString("name");
            String vartype = ((JsonObject)o).getString("type");
            if(varname != "") {
              store.put(varname, vartype);
            }
          }
          JsonArray methods = (JsonArray)writableObject.get("methods");
          for(JsonValue o : methods) {
            RIPMethod m = new RIPMethod((JsonObject)o);
            this.methods.add(m);
          }
          break;
        case ARRAY:
          store.clear();
          JsonArray writableArray = (JsonArray)writable,
              names = writableArray.getJsonArray(0),
              types = writableArray.getJsonArray(1);
          if((names != null) && (types != null) && (names.size() == types.size())) {
            for(int i = 0; i<names.size(); i++) {
              store.put(names.getString(i), types.getString(i));
            }
          }
          break;
        default:         
      }
    } else {
      String err = String.format("[WARNING] Field '%s' not found", label);
      System.err.println(err);
    }
  }

  private void loadWritable(JsonObject message) {
    loadList(message, WRITABLE);
  }

  private void loadReadable(JsonObject message) {
    loadList(message, READABLE);
  }

  public void setApi(String api) {
    this.api = api;
  }

  public String getApi() {
    return api;
  }

  public RIPInfo getInfo() {
    return info;
  }

  public List<RIPMethod> getMethods() {
    return methods;
  }
  
  public Map<String, String> getReadable() {
    return readable;
  }
  
  public Map<String, String> getWritable() {
    return writable;
  }
  
  public void setInfo(RIPInfo info) {
    this.info = info;
  }
  
  public String toString() {
    String toReturn = "";
    toReturn += "info:\n  " + info.getName() + "\n";
    toReturn += "description:\n  " + info.getDescription() + "\n";
    for(RIPMethod method : methods) {
      toReturn += method.toString() + "\n";
    }
    toReturn += "readable:\n  " + readable.toString() + "\n";
    toReturn += "writable:\n  " + writable.toString() + "\n";
    return toReturn;
  }
}