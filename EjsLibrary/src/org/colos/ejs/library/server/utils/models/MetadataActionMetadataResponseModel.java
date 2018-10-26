package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "ActionMetadataResponse": {
            "id": "ActionMetadataResponse",
            "required": [
                "method", "actions"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "actions": {
                    "type": "array",
                    "items": {
                        "$ref": "Actions"
                    },
                    "description": "The list of actions from model"
                }
            }
        },
 * */

public class MetadataActionMetadataResponseModel extends MetadataModel {
  private static final String METHOD = "method";
  private static final String ACTIONS= "actions";
  
  public MetadataActionMetadataResponseModel(String method, JsonArray actions){
    super("ActionMetadataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(ACTIONS).build();
    setRequired(req);
    loadSensResp(method, actions);
  }
 
  public void loadSensResp(String method, JsonArray actions){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(ACTIONS,actions);
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
        .add(ACTIONS,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","Action")
                .build())
            .build())
        .build();
    this.setDefinitions(description);
  }
}
