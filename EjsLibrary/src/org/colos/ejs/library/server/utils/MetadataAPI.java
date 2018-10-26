package org.colos.ejs.library.server.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class MetadataAPI {
  private static final String PROTOCOL = "protocol";      //protocol: Which protocol is needed to use the API: websocket, html (By default: websocket)
  private static final String PRODUCES = "produce";       //produce: Type of response. (Default: application/json)
  private static final String OPERATIONS = "operations";  //operations: JSON structure with all the operations available

  private String aProtocol = "send";               
  private JsonArray aProduces;                   
  private JsonArray aOperations;     
  //new ArrayList<MetadataOperation>();
      
  public MetadataAPI(){
    aProtocol = "send";
    aProduces = Json.createArrayBuilder().build();
    aOperations = Json.createArrayBuilder().build();
  }
  
  public MetadataAPI(String aProtocol, JsonArray aProduces, JsonArray aOperations){
    this.aProtocol = aProtocol;
    this.aProduces = aProduces;
    this.aOperations = aOperations;
  }
  
  @SuppressWarnings("unchecked")
  public MetadataAPI(String aProtocol, JsonArray aProduces, List<?> aOperations,Class<?> className){
    this.aProtocol = aProtocol;
    this.aProduces = aProduces;
    if (className.equals(MetadataOperation.class)){
      loadOperations((List<MetadataOperation>) aOperations);
    }else if (className.equals(MetadataOperation.class)){
      loadOperationsFromMap((List<Map<String, Object>>) aOperations);
    }
  }
  
  public MetadataAPI(String aProtocol, JsonArray aProduces){
    this.aProtocol = aProtocol;
    this.aProduces = aProduces;
    //this.aOperations = aOperations;
  }
  
  public void load(JsonObject input){
    if(input.containsKey(PROTOCOL))     aProtocol = input.getString(PROTOCOL);
    if(input.containsKey(PRODUCES))     aProduces = input.getJsonArray(PRODUCES);
    if(input.containsKey(OPERATIONS))   aOperations = input.getJsonArray(OPERATIONS);
  }
  
  public void loadOperationsFromMap(List<Map<String, Object>> input){
    JsonArrayBuilder newops = Json.createArrayBuilder();
    MetadataOperation op;
    for(int i = 0;i<input.size(); i++){
      op = new MetadataOperation("",input.get(i));
      newops.add(op.getJSON());;
    }
    setOperations(newops.build());
  }
  
  public void loadOperations(List<MetadataOperation> input){
    JsonArrayBuilder newOps = Json.createArrayBuilder();
    for(int i = 0;i<input.size(); i++){
      newOps.add(input.get(i).getJSON());
    }
    setOperations(newOps.build());
  }
  
  public void loadProduces(List<String> input){
    JsonArrayBuilder newProd = Json.createArrayBuilder();
    String prod;
    for(int i = 0;i<input.size(); i++){
      prod = input.get(i);
      newProd.add(prod);;
    }
    setOperations(newProd.build());
  }
  
  public JsonObject getJSON(){
    JsonObject jsonApi = (JsonObject) Json.createObjectBuilder()
        .add(PROTOCOL, aProtocol)
        .add(PRODUCES, aProduces)
        .add(OPERATIONS, aOperations)
        .build();
    return jsonApi;
  }
  
  public String getProtocol() {
    return aProtocol;
  }
  public void setProtocol(String aProtocol) {
    this.aProtocol = aProtocol;
  }
  public JsonArray getProduces() {
    return aProduces;
  }
  public void setProduces(JsonArray aProduces) {
    this.aProduces = aProduces;
  }
  public JsonArray getOperations() {
    return aOperations;
  }
  
  public List<String> getProducesAsList() {
    List<String> paramArray = new ArrayList<String>();
    String param = "";
    for(int i = 0; i<aOperations.size(); i++){
      param = aOperations.getString(i);
      paramArray.add(param);
    }
    return paramArray;//opParams;
  }
  
  public List<MetadataOperation> getOperationsAsList() {
    List<MetadataOperation> paramArray = new ArrayList<MetadataOperation>();
    MetadataOperation param = new MetadataOperation();
    for(int i = 0; i<aOperations.size(); i++){
      param.load(Json.createObjectBuilder().add("parameter",aOperations.get(i)).build());
      paramArray.add(param);
    }
    return paramArray;//opParams;
  }
  
  public void setOperations(JsonArray opOperations) {
    this.aOperations = opOperations;
  }
  
  public String toString(){
    String infoComplete =  "\n\t Protocol :  " + aProtocol + "\n\t Produce: " + aProduces +
        "\n\t Operations : \n\t[\n";
    for (int i = 0; i<aOperations.size(); i++){
      infoComplete+= "\n\t\t {  " + aOperations.get(i).toString() + "\n\t\t }";
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
    msgList.add(msg1);
    JsonArray paramArray = Json.createArrayBuilder().add(param1.getJSON()).build();
    JsonArray msgArray = Json.createArrayBuilder().add(msg1.getJSON()).build();
    
    MetadataOperation op1 = new MetadataOperation(opTypeMethod, opMethodName, opRespType, opDescription,opAuths, paramList, msgList);
    MetadataAPI api = new MetadataAPI("JSON",Json.createArrayBuilder().add(op1.getJSON()).build(),Json.createArrayBuilder().add(op1.getJSON()).build());
    
    System.out.println("API: \n" + api.toString());
    
    
    MetadataOperation op2 = new MetadataOperation(opTypeMethod, opMethodName, opRespType, opDescription,opAuths, paramArray, msgArray);
    MetadataAPI api2 = new MetadataAPI("JSON",Json.createArrayBuilder().add(op2.getJSON()).build(),Json.createArrayBuilder().add(op2.getJSON()).build());
    
    System.out.println("API: \n" + api2.toString());
  }
}
