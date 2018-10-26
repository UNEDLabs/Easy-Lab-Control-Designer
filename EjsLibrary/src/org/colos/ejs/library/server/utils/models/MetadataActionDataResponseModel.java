package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/*
  "ActionDataResponse": {
        "id": "ActionDataResponse",
        "required": [
            "method"
        ],
        "properties": {
            "method": {
                "type": "string",
                "description": "The method should be equal to the nickname of one of the provided services."
            },
            "nickName": {
                "type": "string",
                "description": "The nickname of the answering method"
            },
            "payload": {
                "type": "any",
                "description": "The payload can be useful for describing a result that is returned, for instance by using the SensorResponseData model. Since results can differ from acknowledgements to result data, the field is optional and can contain any JSON object."
            },
            "observerMode": {
                "type": "ObserverMode",
                "description": "This field is only available if the accessRole field returns observer."
            }
        }
    },
 * */

public class MetadataActionDataResponseModel extends MetadataModel {
  private static final String METHOD = "method";
  private static final String NICKNAME = "payload"; 
  private static final String PAYLOAD = "payload"; 
  private static final String OBSERVERMODE = "observerMode";
  
 
  public MetadataActionDataResponseModel(String method,String nickname){
    super("actionDataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(NICKNAME).build();
    setRequired(req);
    loadActionDataResponse(method, nickname, Json.createObjectBuilder().build(), Json.createObjectBuilder().build());
  }
  
  public MetadataActionDataResponseModel(String method,String nickname, String lastmeasured, 
      String accessRole, JsonValue payload, JsonObject observerMode){
    super("actionDataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(NICKNAME).build();
    setRequired(req);
    loadActionDataResponse(method, nickname, payload, observerMode);
  }
 
  public void loadActionDataResponse(String method,String nickName, JsonValue payload, JsonObject observerMode){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(PAYLOAD,payload)
    .add(NICKNAME,nickName)
    .add(OBSERVERMODE,observerMode);
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
        .add(NICKNAME, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The nickname of the answering method")
            .build())
        .add(PAYLOAD,Json.createObjectBuilder()
            .add("type","any")
            .add("description","The payload can be useful for describing a result that is returned, for instance by using the SensorResponseData model. Since results can differ from acknowledgements to result data, the field is optional and can contain any JSON object.")
            .build())
        .add(OBSERVERMODE,Json.createObjectBuilder()
            .add("type","any")
            .add("description","This field is only available if the accessRole field returns observer.")
            .build())
        .build();
    this.setDefinitions(description);
  }

}
