package org.colos.ejs.library.server.utils;

import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.colos.ejs.library.server.utils.models.MetadataModel;

/*
 {
    "apiVersion": "2.0.0",
    "swaggerVersion": "1.2",
    "basePath": "http://128.178.5.173:8000",
    "info": {
        "title": "Servo Motor Control",
        "description": "Control the position of a servo motor's shaft",
        "contact": "wissam.halimi@epfl.ch",
        "license": "Apache 2.0",
        "licenseUrl": "http://www.apache.org/licenses/LICENSE-2.0.html"
    },
    "authorizations": {},
    "concurrency": {
        "interactionMode": "synchronous",
        "concurrencyScheme": "roles",
        "roleSelectionMechanism": ["race", "fixed role"],
        "roles": [
            {
                "role": "controller",
                "selectionMechanism": ["race"]
            }
        ]
    },
    "apis": [],
    "models": {}
}
 
 * */

public class MetadataBuilder {
  
  protected static final String APIVERS = "apiVersion";
  protected static final String SWAGGERVER = "swaggerVersion";
  protected static final String BASEPATH = "basePath";
  protected static final String INFO = "info";
  protected static final String AUTHS = "authorizations";
  protected static final String CONCURRENCY = "concurrency";
  protected static final String APIS = "apis";
  protected static final String MODELS = "models";
  
  private String mMVersion;
  private String mSwaggerV;
  private String mBasePath;
  private JsonObject mInfo;
  private JsonObject mAuths;
  private JsonObject mConcurrency;
  private JsonArray mApis;
  private JsonObject mModels;
  //Map<String, Object> fullMetadata = new HashMap<String, Object>();
  
  public MetadataBuilder(){
    mMVersion = "1.0";
    mSwaggerV = "Not defined";
    mBasePath = "http://127.0.0.1:8800/";
    mInfo = Json.createObjectBuilder().build();
    mAuths = Json.createObjectBuilder().build();
    mConcurrency = Json.createObjectBuilder().build();
    mApis = Json.createArrayBuilder().build();
    mModels = Json.createObjectBuilder().build();
  }
  
  public MetadataBuilder(String version, String swaggerVer, String basePath){
    mMVersion = version;
    mSwaggerV = swaggerVer;
    mBasePath = basePath;
    mInfo = Json.createObjectBuilder().build();
    mAuths = Json.createObjectBuilder().build();
    mConcurrency = Json.createObjectBuilder().build();
    mApis = Json.createArrayBuilder().build();
    mModels = Json.createObjectBuilder().build();
  }
  
  public MetadataBuilder(String version, String swaggerVer, String basePath, JsonObject mInfo,
      JsonObject mAuths, JsonObject mConcurrency, JsonArray mApis, JsonObject mModels){
    this.mMVersion = version;
    this.mSwaggerV = swaggerVer;
    this.mBasePath = basePath;
    this.mInfo = mInfo;
    this.mAuths = mAuths;
    this.mConcurrency = mConcurrency;
    this.mApis = mApis;
    this.mModels = mModels;
  }
  
  public void load(JsonObject input){
    if(input.containsKey("metadata")){
      JsonObject inputOp = input.getJsonObject("metadata");
      if(inputOp.containsKey(APIVERS))      setVersion(inputOp.getString(APIVERS));
      if(inputOp.containsKey(SWAGGERVER))   setSwaggerV(inputOp.getString(SWAGGERVER));
      if(inputOp.containsKey(BASEPATH))     setBasePath(inputOp.getString(BASEPATH));
      if(inputOp.containsKey(INFO))         loadInfo(inputOp.getJsonObject(INFO));
      if(inputOp.containsKey(AUTHS))        loadAuths(inputOp.getJsonObject(AUTHS));
      if(inputOp.containsKey(CONCURRENCY))  loadConcurrency(inputOp.getJsonObject(CONCURRENCY));
      if(inputOp.containsKey(APIS))         loadApis(inputOp.getJsonArray(APIS));
      if(inputOp.containsKey(MODELS))       loadModels(inputOp.getJsonObject(MODELS));
    }else{
      if(input.containsKey(APIVERS))      setVersion(input.getString(APIVERS));
      if(input.containsKey(SWAGGERVER))   setSwaggerV(input.getString(SWAGGERVER));
      if(input.containsKey(BASEPATH))     setBasePath(input.getString(BASEPATH));
      if(input.containsKey(INFO))         loadInfo(input.getJsonObject(INFO));
      if(input.containsKey(AUTHS))        loadAuths(input.getJsonObject(AUTHS));
      if(input.containsKey(CONCURRENCY))  loadConcurrency(input.getJsonObject(CONCURRENCY));
      if(input.containsKey(APIS))         loadApis(input.getJsonArray(APIS));
      if(input.containsKey(MODELS))       loadModels(input.getJsonObject(MODELS));
    }
  }
 
  public JsonObject getJSON(){
    JsonObject jsonOp = (JsonObject) Json.createObjectBuilder()
        .add(APIVERS, mMVersion)
        .add(SWAGGERVER, mSwaggerV)
        .add(BASEPATH,mBasePath)
        .add(INFO, mInfo)
        .add(AUTHS, mAuths)
        .add(CONCURRENCY, mConcurrency)
        .add(APIS, mApis)
        .add(MODELS, mModels)
        .build();
    return jsonOp;
  }
  
  public void loadInfo(JsonObject input){
    JsonObjectBuilder info = Json.createObjectBuilder();
    if(input.containsKey("title"))        info.add("title",input.getString("title"));
    else                                  info.add("title","None");
    if(input.containsKey("description"))  info.add("description",input.getString("description"));
    else                                  info.add("description","Not defined");
    if(input.containsKey("contact"))      info.add("contact",input.getString("contact"));
    else                                  info.add("contact","jacobo.saenz@bec.uned.es");
    if(input.containsKey("license"))      info.add("license",input.getString("license"));
    else                                  info.add("license","Not defined");
    if(input.containsKey("licenseUrl"))   info.add("licenseUrl",input.getString("licenseUrl"));
    else                                  info.add("licenseUrl","Not defined");
    setInfo(info.build());
  }
  
  public void loadIfoFromMap(List<Map<String,Object>> input){}
  
  public void loadAuths(JsonObject input){
    setAuths(input);
  }
  
  public void loadAuthsFromMap(List<Map<String,Object>> input){}
   
  public void loadConcurrency(JsonObject input){
    if (!input.isEmpty()){
      JsonObjectBuilder info = Json.createObjectBuilder();
      if(input.containsKey("interactionMode"))        
              info.add("interactionMode",input.getString("interactionMode"));
      else    info.add("interactionMode","None");
      if(input.containsKey("concurrencyScheme"))  
              info.add("concurrencyScheme",input.getString("concurrencyScheme"));
      else    info.add("concurrencyScheme","None");
      if(input.containsKey("roleSelectionMechanism"))      
              info.add("roleSelectionMechanism",input.getJsonArray("roleSelectionMechanism"));
      else    info.add("roleSelectionMechanism",Json.createArrayBuilder().build());
      if(input.containsKey("roles"))      
              info.add("roles",input.getJsonArray("roles"));
      else    info.add("roles",Json.createArrayBuilder().build());
      setConcurrency(info.build());
    }
  }
  
  public void loadConcurrencyFromMap(List<Map<String,Object>> input){}
  
  public void loadApis(JsonArray input){
    setApis(input);
  }
  
  public void loadApisFromMap(List<MetadataAPI> input){
    JsonArrayBuilder newApis = Json.createArrayBuilder();
    for(int i = 0; i<input.size();i++){
      newApis.add(input.get(i).getJSON());
    }
    setApis(newApis.build());
  }
  
  public void loadModels(JsonObject input){
    setModels(input);
  }
  
  public void loadModelsFromMap(List<MetadataModel> input){
    JsonObjectBuilder newModels = Json.createObjectBuilder();
    for(int i = 0; i<input.size();i++){
      newModels.add(input.get(i).getName(),input.get(i).getJSON());
    }
    setModels(newModels.build());
  }
  
  public String getVersion() {
    return mMVersion;
  }
  public void setVersion(String mMVersion) {
    this.mMVersion = mMVersion;
  }
  public String getSwaggerV() {
    return mSwaggerV;
  }
  public void setSwaggerV(String mSwaggerV) {
    this.mSwaggerV = mSwaggerV;
  }
  public String getBasePath() {
    return mBasePath;
  }
  public void setBasePath(String mBasePath) {
    this.mBasePath = mBasePath;
  }
  public JsonObject getInfo() {
    return mInfo;
  }
  public void setInfo(JsonObject mInfo) {
    this.mInfo = mInfo;
  }
  public JsonObject getAuths() {
    return mAuths;
  }
  public void setAuths(JsonObject mAuths) {
    this.mAuths = mAuths;
  }
  public JsonObject getConcurrency() {
    return mConcurrency;
  }
  public void setConcurrency(JsonObject mConcurrency) {
    this.mConcurrency = mConcurrency;
  }
  public JsonArray getApis() {
    return mApis;
  }
  public void setApis(JsonArray mApis) {
    this.mApis = mApis;
  }
  public JsonObject getModels() {
    return mModels;
  }
  public void setModels(JsonObject mModels) {
    this.mModels = mModels;
  }
 
  public String toString(){
    String infoComplete =  
        "\n\t {  \n" + 
        "\n\t"+ APIVERS   + " : " + mMVersion +
        "\n\t"+ SWAGGERVER+ " : " + mSwaggerV +
        "\n\t"+ BASEPATH  + " : " + mBasePath + 
        "\n\t"+ INFO  + " : " +
        "[\n";
    List<String> keySet = new ArrayList<String>(mInfo.keySet());
    for (int i = 0; i<keySet.size(); i++){
      infoComplete+= "\n\t\t {  " + keySet.get(i) + ":" + mInfo.get(keySet.get(i)).toString() + "\n\t\t }";
    }
    infoComplete+= "\n\t]\n";
    infoComplete+="\n\t"+ CONCURRENCY + " : " + "[\n";
    keySet = new ArrayList<String>(mConcurrency.keySet());
    for (int i = 0; i<keySet.size(); i++){
      infoComplete+= "\n\t\t {  " + keySet.get(i) + ":" + mConcurrency.get(keySet.get(i)).toString() + "\n\t\t }";
    }
    infoComplete+= "\n\t]\n";
    
    infoComplete+="\n\t"+ APIS + " : " + "[\n";
    for (int i = 0; i<mApis.size(); i++){
      infoComplete+= "\n\t\t {  " + mApis.get(i).toString() + "\n\t\t }";
    }
    infoComplete+= "\n\t]\n";
    
    infoComplete+="\n\t"+ MODELS + " : " + "[\n";
    keySet = new ArrayList<String>(mModels.keySet());
    for (int i = 0; i<keySet.size(); i++){
      infoComplete+= "\n\t\t {  " + mModels.get(keySet.get(i)).toString() + "\n\t\t }";
    }
    infoComplete+= "\n\t]\n";
    return infoComplete;
  }
  
  public static void main (String [ ] args) {
    String opTypeMethod = "send", opMethodName = "getProp", opRespType = "JSON Msg", opDescription="I do nothing", opAuths = "112"; 
    String pName = "isMyName", pDescription = "isDescription", pType = "send"; 
    boolean pRequired = true, pAllowMultiple = false;
    MetadataParam param1 = new MetadataParam(pName,pDescription,pType,pRequired ,pAllowMultiple);
    MetadataRespMsg msg1 = new MetadataRespMsg("Err1","No connection");
    List<MetadataParam> paramList = new ArrayList<MetadataParam>();
    List<MetadataRespMsg> msgList = new ArrayList<MetadataRespMsg>();
    paramList.add(param1);
    paramList.add(param1);
    msgList.add(msg1);
    msgList.add(msg1);
    //JsonArray paramArray = Json.createArrayBuilder().add(param1.getJSON()).build();
    //JsonArray msgArray = Json.createArrayBuilder().add(msg1.getJSON()).build();
    
    MetadataOperation op1 = new MetadataOperation(opTypeMethod, opMethodName, opRespType, opDescription,opAuths, paramList, msgList);
    List<MetadataOperation> opList = new ArrayList<MetadataOperation>();
    opList.add(op1);
    MetadataAPI api1 = new MetadataAPI();
    List<MetadataAPI> apiList = new ArrayList<MetadataAPI>();
    apiList.add(api1);
    api1.loadOperations(opList);
    //System.out.println("Operation: \n" + op1.toString());
    MetadataBuilder fullMeta = new MetadataBuilder("1.0","0.0","http://127.0.0.1/");
    System.out.println("Operation: \n" + fullMeta.toString());
    
    
    fullMeta.loadApis(Json.createArrayBuilder().add(api1.getJSON()).build());;
    System.out.println("Operation: \n" + fullMeta.toString());
  }
}
