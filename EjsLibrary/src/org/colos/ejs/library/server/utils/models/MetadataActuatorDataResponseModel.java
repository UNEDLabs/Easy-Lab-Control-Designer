package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/*
 "ActuatorDataResponse": {
            "id": "ActuatorDataResponse",
            "required": [
                "method"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "lastMeasured": {
                    "type": "date-time"
                },
                "accessRole": {
                    "type": "string",
                    "description": "This field contains one of the roles defined in the concurrency roles list. If no roles are defined controller is returned. If the observer is returned, the observerMode field will be available with extra info on the status of the lab."
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

public class MetadataActuatorDataResponseModel extends MetadataModel {
  private static final String METHOD = "method";
  private static final String LASTMEASURED = "lastmeasured";
  private static final String ACCESSROLE = "accessRole";
  private static final String PAYLOAD = "payload"; 
  private static final String OBSERVERMODE = "observerMode";
  
 
  public MetadataActuatorDataResponseModel(String method){
    super("ActuatorDataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).build();
    setRequired(req);
    loadSensorDataResponse(method, "Not Defined", "Not Defined", Json.createObjectBuilder().build(), Json.createObjectBuilder().build());
  }
  
  public MetadataActuatorDataResponseModel(String method, String lastmeasured, 
      String accessRole, JsonValue payload, JsonObject observerMode){
    super("ActuatorDataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).build();
    setRequired(req);
    loadSensorDataResponse(method, lastmeasured, accessRole, payload, observerMode);
  }
 
  public void loadSensorDataResponse(String method, String lastmeasured, 
      String accessRole, JsonValue payload, JsonObject observerMode){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(LASTMEASURED,lastmeasured)
    .add(ACCESSROLE,accessRole)
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
  
  @Override
  protected void loadBasicDesc(){
    JsonObject description = Json.createObjectBuilder()
        .add(METHOD, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .add(LASTMEASURED,Json.createObjectBuilder()
            .add("type","date-time")
            .build())
        .add(ACCESSROLE,Json.createObjectBuilder()
            .add("type","any")
            .add("description","This field contains one of the roles defined in the concurrency roles list. If no roles are defined controller is returned. If the observer is returned, the observerMode field will be available with extra info on the status of the lab.")
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
