package es.uned.dia.softwarelinks.nodejs;

import javax.json.JsonObject;

public class RIPInfo {
  private String name;
  private String description;
  
  public RIPInfo(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public RIPInfo(JsonObject info) {
    load(info);
  }

  private void load(JsonObject info) {
    if(info.containsKey("name")) {
      this.name = info.getString("name");
    }
    if(info.containsKey("description")) {
      this.description = info.getString("description");
    }    
  }
  
  public String toString() {
    return "name:\n  " + name + "description:\n  " + description;
  }
  
  public String getName() {
    return name;
  }
  
  public String getDescription() {
    return description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}