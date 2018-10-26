package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "ActuatorMetadataResponse": {
            "id": "ActuatorMetadataResponse",
            "required": [
                "method", "actuators"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "actuators": {
                    "type": "array",
                    "items": {
                        "$ref": "Actuator"
                    },
                    "description": "The list of actuator metadata elements"
                }
            }
        },
 * */

public class MetadataActuatorMetadataResponseModel extends MetadataModel {
  private static final String METHOD = "method";
  private static final String ACTUATORS= "actuators";
  
  public MetadataActuatorMetadataResponseModel(String method, JsonArray actuators){
    super("ActuatorMetadataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(ACTUATORS).build();
    setRequired(req);
    loadSensResp(method, actuators);
  }
 
  public void loadSensResp(String method, JsonArray actuators){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(ACTUATORS,actuators);
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
        .add(ACTUATORS,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","Actuator")
                .build())
            .build())
        .build();
    this.setDefinitions(description);
  }
}
