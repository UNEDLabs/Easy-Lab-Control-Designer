package org.colos.ejs.library.server.utils;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

public class MetadataParam {
  private static final String NAME = "name";
  private static final String DESCRIPTION = "summary";
  private static final String TYPE = "type";
  private static final String REQUIRED = "required";
  private static final String ALLOWS = "allowMultiple";
  
  private String pName = "None";                //paramName: String name of the parameter
  private String pDescription = "Not defined";         //description: Purpose of this parameter
  private String pType = "Not defined";                //paramType: Java type of the parameter
  private boolean pRequired = true;         //required: Is needed in the method call?
  private boolean pAllowMultiple = false;   //allowMultiple: allows be read by multiple users?
  
  public MetadataParam() {
    pName = "None";
    pDescription = "Not defined";
    pType = "Not defined"; 
    pRequired = true;
    pAllowMultiple = false;
  }
  
  public MetadataParam(String name, Map<String, Object> info) {
    this.pName = name;
    load(info);
  }
  public MetadataParam(String name, JsonObject info) {
    this.pName = name;
    load(info);
  }
  public MetadataParam(String pName, String pDescription, String pType, boolean pRequired , boolean pAllowMultiple) {
    this.pName = pName;
    this.pDescription = pDescription;
    this.pType = pType;
    this.pRequired = pRequired;
    this.pAllowMultiple = pAllowMultiple;
  }
  
  public void load(JsonObject input){
    if (input.containsKey("parameter")){
      JsonObject inputParam = input.getJsonObject("parameter");
      if(inputParam.containsKey(NAME))          pName = inputParam.getString(NAME);
      if(inputParam.containsKey(DESCRIPTION))   pDescription = inputParam.getString(DESCRIPTION);
      if(inputParam.containsKey(TYPE))          pType = inputParam.getString(TYPE);
      if(inputParam.containsKey(REQUIRED))      pRequired = inputParam.getBoolean(REQUIRED);
      if(inputParam.containsKey(ALLOWS))        pAllowMultiple = inputParam.getBoolean(ALLOWS);
    }else{
      if(input.containsKey(NAME))           pName = input.getString(NAME);
      if(input.containsKey(DESCRIPTION))    pDescription = input.getString(DESCRIPTION);
      if(input.containsKey(TYPE))           pType = input.getString(TYPE);
      if(input.containsKey(REQUIRED))       pRequired = input.getBoolean(REQUIRED);
      if(input.containsKey(ALLOWS))         pAllowMultiple = input.getBoolean(ALLOWS);
    }
  }
  
  public void load(Map<String, Object> input){
    if(input.containsKey(NAME))   pName = (String) input.get(NAME);
    if(input.containsKey(DESCRIPTION))   pDescription = (String) input.get(DESCRIPTION);
    if(input.containsKey(TYPE))   pType = (String) input.get(TYPE);
    if(input.containsKey(REQUIRED))   pRequired = (Boolean) input.get(REQUIRED);
    if(input.containsKey(ALLOWS))   pAllowMultiple = (Boolean) input.get(ALLOWS);
  }
  
  public JsonObject getJSON(){
    JsonObject jsonParam = (JsonObject) Json.createObjectBuilder()
        .add(NAME, pName)
        .add(DESCRIPTION, pDescription)
        .add(TYPE,pType)
        .add(REQUIRED, pRequired)
        .add(ALLOWS, pAllowMultiple)
        .build();
    return jsonParam;
  }
   
  //Getters and Setters
  
  public String getName() {
    return pName;
  }
  public void setName(String pName) {
    this.pName = pName;
  }
  public String getDescription() {
    return pDescription;
  }
  public void setDescription(String pDescription) {
    this.pDescription = pDescription;
  }
  public String getType() {
    return pType;
  }
  public void setType(String pType) {
    this.pType = pType;
  }
  public boolean isRequired() {
    return pRequired;
  }
  public void setRequired(boolean pRequired) {
    this.pRequired = pRequired;
  }
  public boolean isAllowMultiple() {
    return pAllowMultiple;
  }
  public void setAllowMultiple(boolean pAllowMultiple) {
    this.pAllowMultiple = pAllowMultiple;
  }
  
  public String toString(){
    return "\n\t\t\t name:" + pName + "\n\t\t\t purpose: " + pDescription + "\n\t\t\t type: " + pType + "\n\t\t\t isRequired: " + pRequired + "\n\t\t\t allowsMultiple: " + pAllowMultiple;
  }

  public static void main (String [ ] args) {
    String pName = "isMyName", pDescription = "isDescription", pType = "send"; 
    boolean pRequired = true, pAllowMultiple = false;
    MetadataParam param1 = new MetadataParam(pName,pDescription,pType,pRequired ,pAllowMultiple);
    System.out.println("Message 1 is: \n" + param1.toString());
    
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("name", pName);
    data.put("summary", pDescription);
    data.put("type",pType);
    data.put("required", pRequired);
    data.put("allowMultiple", pAllowMultiple);
    MetadataParam param2 = new MetadataParam(pName,data);
    System.out.println("Message 2 is: \n" + param2.toString());
    
    JsonObject data2 = (JsonObject) Json.createObjectBuilder()
        .add("name", pName)
        .add("summary", pDescription)
        .add("type",pType)
        .add("required", pRequired)
        .add("allowMultiple", pAllowMultiple)
        .build();
    MetadataParam param3 = new MetadataParam(pName,data2);
    System.out.println("Message 3 is: \n" + param3.toString());
  }

}

