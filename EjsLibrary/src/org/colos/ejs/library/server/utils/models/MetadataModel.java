package org.colos.ejs.library.server.utils.models;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;


//import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;

/* Main Structure of a Model. Must contain method to load and get Json models. Also getters and setters,
 * and in order to obtain use it an add function to Lists is required. Finally, the properties must have 
 * as well an add method
This is metadata not ready to use structure, is just an abstract class.
*{
     "id": "",
     "required": [],
     "properties": {}
 }
*/
//@SuppressWarnings("unused")
public abstract class MetadataModel {
  
  protected static final String ID = "id";
  protected static final String REQUIRED = "required";
  protected static final String PROPERTIES = "properties";
  protected static final String PROPDEFS = "propertiesDefinitions";
  
  protected String mID = "None";           
  //protected List<String> mRequired = new ArrayList<String>(); 
  protected JsonArray mRequired;
  protected JsonObject mProperties; 
  protected JsonObject mPropDefinitions;
  
  public void load(JsonObject input) {
    if(input.containsKey(ID))   mID = input.getString(ID);
    if(input.containsKey(REQUIRED))   setRequired(input.getJsonArray(REQUIRED));
    if(input.containsKey(PROPERTIES))   setProperties((input.getJsonObject(PROPERTIES)));
    if(input.containsKey(PROPDEFS))   setDefinitions((input.getJsonObject(PROPDEFS)));
  }
  
  public JsonObject getJSON() {
    JsonObject jsonRespMsg = (JsonObject) Json.createObjectBuilder()
        .add(ID, mID)
        .add(REQUIRED, mRequired)
        .add(PROPERTIES, mProperties)
        .build();
    return jsonRespMsg;
  }
  
  public JsonObject getJSONModel() {
    return getProperties();
  }
  
  public MetadataModel(String ID, boolean isSensor, List<String> required, JsonObject properties){
    mID = ID;
    setRequired(required);
    setProperties(properties);
  }
  
  public MetadataModel(){
    this.mID = "None";
    this.mRequired = Json.createArrayBuilder().build();
    this.mProperties = Json.createObjectBuilder().build();  
    this.mPropDefinitions= Json.createObjectBuilder().build();
  }
  
  public MetadataModel(String ID){
    this.mID = ID;
    this.mRequired = Json.createArrayBuilder().build();
    this.mProperties = Json.createObjectBuilder().build();  
    this.mPropDefinitions= Json.createObjectBuilder().build();
  }
  
  public MetadataModel(String ID, JsonArray req, JsonObject prop, JsonObject propDef){
    this.mID = ID;
    this.mRequired = req;
    this.mProperties = prop;  
    this.mPropDefinitions= propDef;
  }
  
  public String getID(){
    return mID;
  }
  
  public void setID(String ID){
    mID = ID;
  }
  
  public JsonArray getRequired(){
    return mRequired;
  }
  
  public JsonObject getJSONRequired() {
    JsonArrayBuilder reqJSON = Json.createArrayBuilder();
    for(int i = 0; i<mRequired.size(); i++){
      reqJSON.add(mRequired.get(i));
    }
    JsonObject req = Json.createObjectBuilder().add(REQUIRED,reqJSON.build()).build();
    return req;
  }
  
  public void setRequired(JsonArray req){
    mRequired = req;
  }
  
  public void setRequired(List<String> req){
    JsonArrayBuilder reqJSON = Json.createArrayBuilder();
    for(int i = 0; i< req.size(); i++){
      addRequired(req.get(i));
    }
    setRequired(reqJSON.build());
  }
  
  public void addRequired(String req) {
    if(getPropertiesNames().contains(req)){
      JsonArrayBuilder reqJSON = Json.createArrayBuilder();
      if (mRequired.contains(req)){
        System.out.println("Warning! The String: " + req + " is also in the required variables List");
      }else{
        mRequired.add(reqJSON.add(req).build());
      }
    }else   System.err.println("This required name is not one of the given properties, will be ignored");
  }
  
  public JsonObject getProperties(){
    return mProperties;
  }
  
  public List<String> getPropertiesNames(){
    return new ArrayList<String>(mProperties.keySet());
  }
  
  public void setProperties(JsonObject properties){
    mProperties = properties;
  }
  
  public void addProperty(JsonObject value) {
    //JsonArrayBuilder valJson = Json.createArrayBuilder();
    List<String> iterateJSON = new ArrayList<String>(value.keySet());
    for(String iteraKey : iterateJSON){
      if (mProperties.containsKey(iteraKey)){
        System.out.println("Warning! The JSON key: " + iteraKey + " is also in the structure. Will be overwrintten");
      }
      this.mProperties.put(iteraKey, value.get(iteraKey));
    }
  }
  
  public JsonObject getDefinition(String key){
    if (mPropDefinitions.containsKey(key)) return Json.createObjectBuilder().add(key,mPropDefinitions.get(key)).build();
    else{
      System.out.println("Warning! The JSON key: " + key +" not exists, ignoring it");
      return null;
    }
  }
  public JsonObject getDefinition(){
    return mPropDefinitions;
  }
  
  public void setDefinitions(JsonObject newDef){
    mPropDefinitions = newDef;
  }
  
  public void addDefinition(JsonObject value){
    List<String> iterateJSON = new ArrayList<String>(value.keySet());
    for(String iteraKey : iterateJSON){
      if (mProperties.containsKey(iteraKey)){
        System.out.println("Warning! The JSON key: " + iteraKey + " is also in the structure. Will be overwrintten");
      }
      this.mPropDefinitions.put(iteraKey, value.get(iteraKey));
    }
  }
  
  public JsonObject getModelDescription(){
    JsonObject jsonRespMsg = (JsonObject) Json.createObjectBuilder()
        .add(ID, mID)
        .add(REQUIRED, mRequired)
        .add(PROPERTIES, mPropDefinitions)
        .build();
    return jsonRespMsg;
  }
  
  public String getName(){
    return getClass().getSimpleName().substring(8).replace("model", ""); 
  }
  
  protected abstract void loadBasicDesc();
  
  public abstract String toString();
  
}
