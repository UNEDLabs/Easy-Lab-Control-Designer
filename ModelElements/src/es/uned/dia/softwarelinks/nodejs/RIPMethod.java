package es.uned.dia.softwarelinks.nodejs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class RIPMethod {
  private static final String RETURNS = "returns";
  private static final String EXAMPLE = "example";
  private static final String DESCRIPTION = "description";
  private static final String PARAMS = "params";
  private static final String TYPE = "type";
  private static final String URL = "url";
  private static final String NAME = "name";
  private String name = "method";
  private String description = "without purpose";
  private Map<String, String> params = new HashMap<>();
  private String response;
  private String url;
  private String type;
  private String example;
  
  public RIPMethod(JsonObject info) {
    load(info);
  }

  @Deprecated
  public RIPMethod(String name, JsonObject info) {
    this.setName(name);
    load(info);
  }

  private void load(JsonObject info) {
    if(info.containsKey(NAME)) { setName(info.getString(NAME)); }
    if(info.containsKey(URL)) { setUrl(info.getString(URL)); }
    if(info.containsKey(TYPE)) { setType(info.getString(TYPE)); }
    if(info.containsKey(DESCRIPTION)) { setDescription(info.getString(DESCRIPTION)); }
    if(info.containsKey(PARAMS)) {
      switch(info.get(PARAMS).getValueType()) {
        case OBJECT:
          loadParams((JsonObject)info.get(PARAMS));
          break;
        case ARRAY:
          loadParams((JsonArray)info.get(PARAMS));
          break;
        default:
      }
    }
    if(info.containsKey(RETURNS)) { setResponse(info.getString(RETURNS)); }
    if(info.containsKey(EXAMPLE)) { setExample(info.get(EXAMPLE).toString()); }
  }

  private void loadParams(JsonArray input) {
    Iterator<JsonValue> iter = input.iterator();
    while(iter.hasNext()) {
      JsonObject o = (JsonObject)iter.next();
      this.params.put(o.getString(NAME), o.toString());
    }
  }

  private void loadParams(JsonObject input) {
    String[] params = input.keySet().toArray(new String[]{});
    this.params.clear();
    if(input.containsKey(NAME) && input.containsKey(TYPE)) {
      Iterator<JsonValue> names = (Iterator<JsonValue>)input.getJsonArray(NAME).iterator();
      Iterator<JsonValue> types = (Iterator<JsonValue>)input.getJsonArray(TYPE).iterator();
      while(names.hasNext() && types.hasNext()) {
        this.params.put(names.next().toString(), types.next().toString());
      }
    } else {
      for(String key : params) {
        this.params.put(key, input.get(key).toString());
      }
    }
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getParams() {
    return params;
  }
  
  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getExample() {
    return example;
  }

  public void setExample(String example) {
    this.example = example;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String toString() {
    return "Method " + getName() + ":\n  purpose: " + getDescription() + "\n  params: " + params.toString() + "\n  returns: " + response;
  }

}