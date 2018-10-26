package org.colos.ejs.library.server.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/*
 {
    "method": "Send",
    "nickname": "getActuatorMetadata",
    "summary": "List all actuators and their metadata",
    "type": "ActuatorMetadataResponse",
    "parameters": [
        {
            "name": "message",
            "description": "The payload for the getActuatorMetadata service",
            "required": true,
            "paramType": "message",
            "type": "SimpleRequest",
            "allowMultiple": false
        }
    ],
    "responseMessages": [
        {
            "code": 404,
            "message": "No actuators found"
        },
        {
            "code": 405,
            "message": "Method not allowed. The requested method is not allowed by this server."
        },
        {
            "code": 422,
            "message": "The request body is unprocessable"
        }
    ],
    "authorizations": {}
},
 * */

public class MetadataOperation {
  private static final String METHODT = "method";           //typeMethod: Send, get...(By default "send")
  private static final String METHODNAME = "nickname";      //methodName: Nickname to needs to match with the actual methods
  private static final String DESCRIPTION = "summary";      //typeResponse: Name of JSON structure which describes the response
  private static final String TYPEOUT = "type";             //description: Purpose of the method
  private static final String AUTH = "authorizations";      //auth: If needed, key to read/write/call the resource
  private static final String PARAMS = "parameters";        //parameters: List of JSON structures with the parameters of the method
  private static final String RESPMSG = "responseMessages"; //responseMsgs: Possible error msgs to receive
  
  private String opTypeMethod = "send";               
  private String opMethodName = "None";                   
  private String opRespType = "Not defined";                     
  private String opDescription = "Not defined";                  
  private String opAuths = "Not defined";                        
  private JsonArray opParams= Json.createArrayBuilder().build();;                                 
  private JsonArray opResponseMessages= Json.createArrayBuilder().build();;                             
  //private List<MetadataParam> opParams = new ArrayList<MetadataParam>();                    //parameters: List of JSON structures with the parameters of the method
  //private List<MetadataRespMsg> opResponseMessages = new ArrayList<MetadataRespMsg>();      //responseMsgs: Possible error msgs to receive
  
  public MetadataOperation() {
    opTypeMethod = "send"; 
    opMethodName = "None";
    opRespType = "Not defined";
    opDescription = "Not defined";
    opAuths = "Not defined";
    opParams = Json.createArrayBuilder().build();
    opResponseMessages = Json.createArrayBuilder().build();
  }
  
  public MetadataOperation(String name, Map<String, Object> info) {
    this.opMethodName = name;
    load(info);
  }
  
  public MetadataOperation(String opTypeMethod, String opMethodName, String opRespType, String opDescription,
      String opAuths, JsonArray opParams, JsonArray opResponseMessages) {
    this.opTypeMethod = opTypeMethod;
    this.opMethodName = opMethodName;
    this.opRespType = opRespType;
    this.opDescription = opDescription;
    this.opAuths = opAuths;
    this.opParams = opParams;
    this.opResponseMessages = opResponseMessages;
  }
  
  public MetadataOperation(String opTypeMethod, String opMethodName, String opRespType, String opDescription,
      String opAuths, List<MetadataParam> opParams, List<MetadataRespMsg> opResponseMessages) {
    this.opTypeMethod = opTypeMethod;
    this.opMethodName = opMethodName;
    this.opRespType = opRespType;
    this.opDescription = opDescription;
    this.opAuths = opAuths;
    loadParameters(opParams);
    loadMessages(opResponseMessages);
  }
  
  public void load(JsonObject input, JsonArray opParams, JsonArray opResponseMessages){
    if(input.containsKey(METHODT))   opTypeMethod = input.getString(METHODT);
    if(input.containsKey(METHODNAME))   opMethodName = input.getString(METHODNAME);
    if(input.containsKey(DESCRIPTION))    opDescription = input.getString(DESCRIPTION);
    if(input.containsKey(TYPEOUT))    opRespType = input.getString(TYPEOUT);
    if(input.containsKey(AUTH))   opAuths = input.getString(AUTH);
    if(input.containsKey(PARAMS))   setParams(opParams);
    if(input.containsKey(RESPMSG))   setResponseMessages(opResponseMessages);
  } 
  
  public void load(JsonObject input){
    if(input.containsKey("operation")){
      JsonObject inputOp = input.getJsonObject("operation");
      if(inputOp.containsKey(METHODT))    opTypeMethod        = inputOp.getString(METHODT);
      if(inputOp.containsKey(METHODNAME)) opMethodName        = inputOp.getString(METHODNAME);
      if(inputOp.containsKey(DESCRIPTION))opDescription       = inputOp.getString(DESCRIPTION);
      if(inputOp.containsKey(TYPEOUT))    opRespType          = inputOp.getString(TYPEOUT);
      if(inputOp.containsKey(AUTH))       opAuths             = inputOp.getString(AUTH);
      if(inputOp.containsKey(PARAMS))     opParams            = inputOp.getJsonArray(PARAMS);
      if(inputOp.containsKey(RESPMSG))    opResponseMessages  = inputOp.getJsonArray(RESPMSG);
    }else{
      if(input.containsKey(METHODT))   opTypeMethod = input.getString(METHODT);
      if(input.containsKey(METHODNAME))   opMethodName = input.getString(METHODNAME);
      if(input.containsKey(DESCRIPTION))    opDescription = input.getString(DESCRIPTION);
      if(input.containsKey(TYPEOUT))    opRespType = input.getString(TYPEOUT);
      if(input.containsKey(AUTH))   opAuths = input.getString(AUTH);
      if(input.containsKey(PARAMS))   opParams =  input.getJsonArray(PARAMS);
      if(input.containsKey(RESPMSG))   opResponseMessages=  input.getJsonArray(RESPMSG);
    }
    
  } 
  
  public void load(Map<String, Object> input){
    if(input.containsKey(METHODT))   opTypeMethod = (String) input.get(METHODT);
    if(input.containsKey(METHODNAME))   opMethodName = (String) input.get(METHODNAME);
    if(input.containsKey(DESCRIPTION))   opDescription = (String) input.get(DESCRIPTION);
    if(input.containsKey(TYPEOUT))   opRespType = (String) input.get(TYPEOUT);
    if(input.containsKey(AUTH))   opAuths = (String) input.get(AUTH);
    if(input.containsKey(PARAMS))   opParams = (JsonArray) input.get(PARAMS);
    if(input.containsKey(RESPMSG))   opResponseMessages = (JsonArray) input.get(RESPMSG);
  }
  
  public JsonObject getJSON(){
    JsonObject jsonOp = (JsonObject) Json.createObjectBuilder()
        .add(METHODT, opTypeMethod)
        .add(METHODNAME, opMethodName)
        .add(DESCRIPTION,opDescription)
        .add(TYPEOUT, opRespType)
        .add(AUTH, opAuths)
        .add(PARAMS, opParams)
        .add(RESPMSG, opResponseMessages)
        .build();
    
    return jsonOp;
  }
  
  public void loadParametersFromMap(List<Map<String, Object>> input){
    JsonArrayBuilder newParams = Json.createArrayBuilder();
    MetadataParam param;
    for(int i = 0;i<input.size(); i++){
      param = new MetadataParam("",input.get(i));
      newParams.add(param.getJSON());
    }
    setParams(newParams.build());
  }
  
  public void loadParameters(List<MetadataParam> input){
    JsonArrayBuilder newParams = Json.createArrayBuilder();
    for(int i = 0;i<input.size(); i++){
      newParams.add(input.get(i).getJSON());
    }
    setParams(newParams.build());
  }
  
  
  public void loadMessagesFromMap(List<Map<String, Object>> input){
    JsonArrayBuilder newMsgs = Json.createArrayBuilder();
    MetadataRespMsg Msg;
    for(int i = 0;i<input.size(); i++){
      Msg = new MetadataRespMsg(input.get(i));
      newMsgs.add(Msg.getJSON());;
    }
    setResponseMessages(newMsgs.build());
  }
  
  public void loadMessages(List<MetadataRespMsg> input){
    JsonArrayBuilder newMsgs = Json.createArrayBuilder();
    for(int i = 0;i<input.size(); i++){
      newMsgs.add(input.get(i).getJSON());
    }
    setResponseMessages(newMsgs.build());
  }
  
//Getters and Setters
  
  public String getTypeMethod() {
    return opTypeMethod;
  }
  public void setTypeMethod(String opTypeMethod) {
    this.opTypeMethod = opTypeMethod;
  }
  public String getMethodName() {
    return opMethodName;
  }
  public void setMethodName(String opMethodName) {
    this.opMethodName = opMethodName;
  }
  public String getRespType() {
    return opRespType;
  }
  public void setRespType(String opRespType) {
    this.opRespType = opRespType;
  }
  public String getDescription() {
    return opDescription;
  }
  public void setDescription(String opDescription) {
    this.opDescription = opDescription;
  }
  public String getAuths() {
    return opAuths;
  }
  public void setAuths(String opAuths) {
    this.opAuths = opAuths;
  }
  public JsonArray getParams() {
    return opParams;
  }
  public List<MetadataParam> getParamsAsList() {
    List<MetadataParam> paramArray = new ArrayList<MetadataParam>();
    MetadataParam param = new MetadataParam();
    for(int i = 0; i<opParams.size(); i++){
      param.load(Json.createObjectBuilder().add("parameter",opParams.get(i)).build());
      paramArray.add(param);
    }
    return paramArray;//opParams;
  }
  public void setParams(JsonArray opParams) {
    this.opParams = opParams;
  } 
  public JsonArray getResponseMessages() {
    return opResponseMessages;
  }
  public List<MetadataRespMsg> getResponseMessagesAsList() {
    List<MetadataRespMsg> msgArray = new ArrayList<MetadataRespMsg>();
    MetadataRespMsg param = new MetadataRespMsg();
    for(int i = 0; i<opResponseMessages.size(); i++){
      param.load(Json.createObjectBuilder().add("respMsg",opResponseMessages.get(i)).build());
      msgArray.add(param);
    }
    return msgArray;//opParams;
  }
  
  public void setResponseMessages(JsonArray opResponseMessages) {
    this.opResponseMessages = opResponseMessages;
  }  
  
  public String toString(){
    String infoComplete =  "\n\t\t Method :  " + opTypeMethod + "\n\t\t Nickname: " + opMethodName + "\n\t\t Summary: " +
        opDescription + "\n\t\t Type: " + opRespType + "\n\t\t parameters : \n\t\t[\n";
    for (int i = 0; i<opParams.size(); i++){
      infoComplete+= "\n\t\t\t {  " + opParams.get(i).toString() + "\n\t\t\t }";
    }
    infoComplete = infoComplete + "\n\t\t]\n\t\t  Response Messages : \n\t\t[\n"; 
    for (int i = 0; i<opParams.size(); i++){
      infoComplete+= "\n\t\t\t {  " + opResponseMessages.get(i).toString()+ "\n\t\t\t }";
    }
    infoComplete = infoComplete + "\n\t\t]\n";
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
    System.out.println("Operation: \n" + op1.toString());
  }
}




