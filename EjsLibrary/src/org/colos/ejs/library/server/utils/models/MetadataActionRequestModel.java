package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
//import javax.json.JsonValue;

/*
  "ActionRequest": {
            "id": "ActionRequest",
            "required": [
                "method"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "callerName": {
                            "type": "string",
                            "description": "The caller if its not the model or the view, e.g. a graph"
                        },
                "nickName": {
                            "type": "string",
                            "description": "The action to be triggered"
                        },
                "params": {
                            "type": "array",
                            "description": "Structure with all named parameters to call the method",
                            "items": {
                                "type": "Any"
                            }
                }
            }
        },
 * */

public class MetadataActionRequestModel extends MetadataModel {
  private static final String METHOD = "method";
  private static final String CALLERNAME = "callerName";
  private static final String NICKNAME = "nickName"; 
  private static final String PARAMS = "params"; 
  
 
  public MetadataActionRequestModel(String method,String nickname){
    super("ActionRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).build();
    setRequired(req);
    loadActionRequest(method, "", nickname, Json.createArrayBuilder().build());
  }
  
  public MetadataActionRequestModel(String method,String callerName,String nickname, String lastmeasured, 
      String accessRole, JsonArray params){
    super("ActionRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).build();
    setRequired(req);
    loadActionRequest(method, callerName, nickname, params);
  }
 
  public void loadActionRequest(String method,String callerName,String nickName, JsonArray params){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(CALLERNAME,callerName)
    .add(NICKNAME,nickName)
    .add(PARAMS,params);
    setProperties(propBuilder.build());
  }
  
  @Override
  public String toString() {
    return "\n\t\t " + ID + " : " + mID +
        "\n\t\t " + REQUIRED + " : " + mRequired +
        "\n\t\t " + PROPERTIES + " : " + mProperties;
  }
  
  public String modelDescToString() {
    return "\n\t\t " + ID + " : " + mID +
        "\n\t\t " + REQUIRED + " : " + mRequired +
        "\n\t\t " + PROPDEFS + " : " + mPropDefinitions;
  }
  
  @Override
  protected void loadBasicDesc(){
    JsonObject description = Json.createObjectBuilder()
        .add(METHOD, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .add(CALLERNAME, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The caller if its not the model or the view, e.g. a graph")
            .build())
        .add(NICKNAME, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The nickname of the answering method")
            .build())
        .add(PARAMS,Json.createObjectBuilder()
            .add("type","any")
            .add("description","The payload can be useful for describing a result that is returned, for instance by using the SensorResponseData model. Since results can differ from acknowledgements to result data, the field is optional and can contain any JSON object.")
            .build())
        .build();
    this.setDefinitions(description);
  }

}
