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



public class RIPServerInfo {
  private static final String EXPID = "id";
  private static final String EXPLIST = "list";
  private static final String EXPERIENCES = "experiences";
  private static final String METHODS = "methods";
  
  private Map<String, RIPExperienceInfo> experiences = new HashMap<>();
  private List<RIPMethod> methods = new ArrayList<>();
  
  public RIPServerInfo(String[] input) {
    for(String exp : input) {
      experiences.put(exp, null);
    }
  }

  public RIPServerInfo(String input) {
    parse(input);
  }
  
  public void parse(String input) {
    InputStream stream;
    try {
      stream = new ByteArrayInputStream(input.getBytes("UTF-8"));
      JsonReader reader = Json.createReader(stream);
      JsonObject message = reader.readObject();

      if(message.containsKey("info")) { // UHU
        JsonValue info = message.get("info");
        switch(info.getValueType()) {
          case OBJECT:
            JsonObject infoAsObject = (JsonObject)info;
            if(infoAsObject.containsKey("Register")) {
              JsonArray list = infoAsObject.getJsonArray("Register");
              Iterator<JsonValue> i = (Iterator<JsonValue>)list.iterator();
              while(i.hasNext()) {
                JsonObject o = (JsonObject)i.next();
                if(o.containsKey("key") && o.containsKey("value")) {
                  String name = o.getString("key");
                  RIPExperienceInfo expInfo = new RIPExperienceInfo();
                  expInfo.parse(o.get("value").toString());
                  experiences.put(name, expInfo);
                }
              }
            } 
            break;
          case ARRAY:
            JsonArray infoAsArray = (JsonArray)info;
            loadExperiences(infoAsArray);
            break;
          default:
          }
        }
    
      if(message.containsKey(EXPERIENCES)) { //UNED
        JsonObject experiences = (JsonObject)message.get(EXPERIENCES);
        JsonArray list = (JsonArray)experiences.get(EXPLIST);
        List<String> experiencesId = new ArrayList<>();
        if(list != null) {
          Iterator<JsonValue> iter = list.iterator();
          while (iter.hasNext()) {
            JsonValue exp = iter.next();
            switch(exp.getValueType()) {
              case STRING:
                experiencesId.add(exp.toString());
                this.experiences.put(exp.toString(), null);
                break;
              case OBJECT:
                JsonObject o = (JsonObject)exp;
                if(o.containsKey(EXPID)) {
                  String id = o.getString(EXPID);
                  experiencesId.add(id);
                  this.experiences.put(id, null);
                }
                break;
              default:
            }
          }
        }
        if(experiences.containsKey(METHODS)) {
          JsonArray methods = (JsonArray)experiences.get(METHODS);
          Iterator<JsonValue> iter = methods.iterator();
          while(iter.hasNext()) {
            JsonObject m = (JsonObject)iter.next();
            try {
              RIPMethod method = new RIPMethod(m);              
              this.getMethods().add(method);
            }catch(Exception e) {
              System.out.println("dfasdfasfd");
            }
          }
        }
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("[ERROR] Unexpected error parsing server info");
    }
  }
  
  private void loadExperiences(JsonArray list) {
    Iterator<JsonValue> i = (Iterator<JsonValue>)list.iterator();
    while(i.hasNext()) {
      JsonObject o = (JsonObject)i.next();
//      if(o.containsKey("key") && o.containsKey("value")) {
//        String name = o.getString("key");
        RIPExperienceInfo expInfo = new RIPExperienceInfo();
        expInfo.parse(o.toString());
//        expInfo.parse(o.get("value").toString());
        experiences.put(o.getString("name"), expInfo);
//      }
    }
  }

  public Map<String, RIPExperienceInfo> getExperiences() {
    return experiences;
  }
  
  public RIPExperienceInfo getExperience(String name) {
    return experiences.get(name);
  }

  public String toString() {
    String toReturn = "";
    for(String expid : experiences.keySet()) {
      toReturn += expid + ":\n" + experiences.get(expid).toString();
    }
    return toReturn;
  }

  public List<RIPMethod> getMethods() {
    return methods;
  }

  public void setMethods(List<RIPMethod> methods) {
    this.methods = methods;
  }
}