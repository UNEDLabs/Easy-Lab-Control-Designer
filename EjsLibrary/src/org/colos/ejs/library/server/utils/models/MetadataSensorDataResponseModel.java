package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/*
 "SensorDataResponse": {
            "id": "SensorDataResponse",
            "required": [
                "method", "sensorId"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "sensorId": {
                    "type": "string"
                },
                "accessRole": {
                    "type": "string",
                    "description": "This field contains one of the roles defined in the concurrency roles list. If no roles are defined controller is returned. If the observer is returned, the observerMode field will be available with extra info on the status of the lab."
                },
                "responseData": {
                    "type": "SensorResponseData",
                    "description": "The data as measured by this sensor"
                },
                "payload": {
                    "type": "any",
                    "description": "This optional payload field can contain any JSON object that provides extra information on this sensor or the current measurement."
                },
                "observerMode": {
                    "type": "ObserverMode",
                    "description": "This field is only available if the accessRole field returns observer."
                }
            }
        },
 * */

public class MetadataSensorDataResponseModel extends MetadataModel {

  private static final String METHOD = "method";
  private static final String SENSORID = "sensorId";
  private static final String ACCESSROLE = "accessRole";
  private static final String RESPONSEDATA = "responseData";
  private static final String PAYLOAD = "payload";
  private static final String OBSERVERMODE = "observerMode";
  
 
  public MetadataSensorDataResponseModel(String method, String sensorID){
    super("SensorDataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORID).build();
    setRequired(req);
    loadSensorDataResponse(method, sensorID, "Not Defined", Json.createObjectBuilder().build(), Json.createObjectBuilder().build(), Json.createObjectBuilder().build());
  }
  
  public MetadataSensorDataResponseModel(String method, String sensorID, 
      String accessRole, JsonObject respData, JsonValue payload, JsonObject observerMode){
    super("SensorDataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORID).build();
    setRequired(req);
    loadSensorDataResponse(method, sensorID, accessRole, respData, payload, observerMode);
  }
 
  public void loadSensorDataResponse(String method, String sensorID, 
      String accessRole, JsonObject respData, JsonValue payload, JsonObject observerMode){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(SENSORID,sensorID)
    .add(ACCESSROLE,accessRole)
    .add(RESPONSEDATA,respData)
    .add(PAYLOAD,payload)
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
  
  /*
   "method": {
      "type": "string",
      "description": "The method should be equal to the nickname of one of the provided services."
  },
  "sensorId": {
      "type": "string"
  },
  "accessRole": {
      "type": "string",
      "description": "This field contains one of the roles defined in the concurrency roles list. If no roles are defined controller is returned. If the observer is returned, the observerMode field will be available with extra info on the status of the lab."
  },
  "responseData": {
      "type": "SensorResponseData",
      "description": "The data as measured by this sensor"
  },
  "payload": {
      "type": "any",
      "description": "This optional payload field can contain any JSON object that provides extra information on this sensor or the current measurement."
  },
  "observerMode": {
      "type": "ObserverMode",
      "description": "This field is only available if the accessRole field returns observer."
  }
   * */
  
  @Override
  protected void loadBasicDesc(){
    JsonObject description = Json.createObjectBuilder()
        .add(METHOD, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .add(SENSORID,Json.createObjectBuilder()
            .add("type","any")
            .build())
        .add(ACCESSROLE,Json.createObjectBuilder()
            .add("type","any")
            .add("description","This field contains one of the roles defined in the concurrency roles list. If no roles are defined controller is returned. If the observer is returned, the observerMode field will be available with extra info on the status of the lab.")
            .build())
        .add(RESPONSEDATA,Json.createObjectBuilder()
            .add("type","any")
            .add("description","The data as measured by this sensor")
            .build())
        .add(PAYLOAD,Json.createObjectBuilder()
            .add("type","any")
            .add("description","This optional payload field can contain any JSON object that provides extra information on this sensor or the current measurement.")
            .build())
        .add(OBSERVERMODE,Json.createObjectBuilder()
            .add("type","any")
            .add("description","This field is only available if the accessRole field returns observer.")
            .build())
        .build();
    this.setDefinitions(description);
  }

}
