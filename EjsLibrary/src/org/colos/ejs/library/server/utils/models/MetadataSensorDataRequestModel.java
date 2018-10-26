package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "SensorDataRequest": {
            "id": "SensorDataRequest",
            "required": ["method", "sensorId"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "sensorId": {
                    "type": "string"
                },
                "configuration": {
                    "type": "array",
                    "items": {
                        "$ref": "ConfigurationItem"
                    }
                },
                "accessRole": {
                    "type": "string",
                    "description": "This field contains one of the roles defined in the concurrency roles list. If accessRole is not defined, the controller role is assumed."
                }
            }
        },
 */

public class MetadataSensorDataRequestModel extends MetadataModel {


  private static final String METHOD = "method";
  private static final String SENSORID = "sensorId";
  private static final String CONFIGURATION = "configuration";
  private static final String ACCESSROLE = "accessRole";
  
  /*public MetadataSensorDataRequestModel(){
    super("ClientResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORID).build();
    setRequired(req);
    loadSensorDataReq("None", "None");
  }*/
  
  public MetadataSensorDataRequestModel(String method, String sensorID){
    super("SensorDataRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORID).build();
    setRequired(req);
    loadSensorDataReq(method, sensorID, Json.createObjectBuilder().build(), "Not defined");
  }
  
  public MetadataSensorDataRequestModel(String method, String sensorID, JsonObject config, String access){
    super("SensorDataRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORID).build();
    setRequired(req);
    loadSensorDataReq(method, sensorID, config, access);
  }
 
  public void loadSensorDataReq(String method, String sensorId, JsonObject config, String access){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(SENSORID,sensorId)
    .add(CONFIGURATION,config)
    .add(ACCESSROLE,access);
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
        .add(SENSORID,Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(CONFIGURATION,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","ConfigurationItem")
                .build())
            .build())
        .add(ACCESSROLE,Json.createObjectBuilder()
            .add("type","String")
            .add("description","This field contains one of the roles defined in the concurrency roles list. If accessRole is not defined, the controller role is assumed.")
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  /*
  public static void main (String [ ] args) {
  }
  */



}
