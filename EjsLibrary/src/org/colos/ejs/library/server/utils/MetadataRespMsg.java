package org.colos.ejs.library.server.utils;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

public class MetadataRespMsg {
  private static final String CODE = "code";
  private static final String MESSAGE = "message";
  private String mCode = "None";            //code: Alphanumeric ID to identify the error
  private String mMessage = "Not defined";         //msg2Show: Message to show to the user
  
  public MetadataRespMsg() {
    this.mCode = "None";
    this.mMessage = "Not defined";
  }
  
  public MetadataRespMsg(String code, String message) {
    this.mCode = code;
    this.mMessage = message;
  }
  public MetadataRespMsg(Map<String, Object> info) {
    load(info);
  }
  
  public MetadataRespMsg(JsonObject info) {
    load(info);
  }
  
  public void load(Map<String, Object> input){
    if(input.containsKey(CODE))   mCode = (String) input.get(CODE);
    if(input.containsKey(MESSAGE))   mMessage = (String) input.get(MESSAGE);
  }
  
  public void load(JsonObject input){
    if (input.containsKey("respMsg")){
      JsonObject inputRespMsg = input.getJsonObject("respMsg");
      if(inputRespMsg.containsKey(CODE))      mCode = inputRespMsg.getString(CODE);
      if(inputRespMsg.containsKey(MESSAGE))   mMessage = inputRespMsg.getString(MESSAGE);
    }else{
      if(input.containsKey(CODE))       mCode = input.getString(CODE);
      if(input.containsKey(MESSAGE))    mMessage = input.getString(MESSAGE);
    }
  }
  
  public JsonObject getJSON(){
    JsonObject jsonRespMsg = (JsonObject) Json.createObjectBuilder()
        .add(CODE, mCode)
        .add(MESSAGE, mMessage)
        .build();
    return jsonRespMsg;
  }
  
//Getters and Setters
  
  public String getCode() {
    return mCode;
  }
  public void setCode(String mCode) {
    this.mCode = mCode;
  }
  public String getMessage() {
    return mMessage;
  }
  public void setMessage(String mMessage) {
    this.mMessage = mMessage;
  }
  
  public String toString(){
    return "\n\t\t\t Code : " +  mCode + "\n\t\t\t Message : " + mMessage;
  }
  
  public static void main (String [ ] args) {
    MetadataRespMsg msg1 = new MetadataRespMsg("Err1","No connection");
    System.out.println("Message 1 is: \n" + msg1.toString());
  }
  
}
