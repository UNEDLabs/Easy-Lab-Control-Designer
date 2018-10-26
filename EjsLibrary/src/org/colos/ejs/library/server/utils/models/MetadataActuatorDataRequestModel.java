package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 * "ActuatorDataRequest": {
            "id": "ActuatorDataRequest",
            "required": [
                "method", "actuatorId"
            ],
            "properties": {
                "authToken": {
                    "type": "string"
                },
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "actuatorId": {
                    "type": "string"
                },
                "valueNames": {
                    "type": "array",
                    "description": "An ordered array with all the value names of this sensor. The same order will be applied to the data array and lastMeasured array.",
                    "items": {
                        "type": "string"
                    }
                },
                "data": {
                    "type": "array",
                    "description": "An ordered array with all the data values of this sensor. Each data element in the array should be ordered in the same position of its corresponding value elements in the valueNames array.",
                    "items": {
                        "type": "any"
                    }
                },
                "configuration": {
                    "type": "array",
                    "items": {
                        "$ref": "ConfigurationItem"
                    }
                },
                "accessRole": {
                    "type": "string",
                    "description": "This field contains one of the roles defined in the concurrency roles list. If accessRole is not defined the controller role is assumed."
                }
            }
        },
 * */

public class MetadataActuatorDataRequestModel extends MetadataModel {
  private static final String AUTHTOKEN = "authToken";
  private static final String METHOD = "method";
  private static final String ACTUATORID = "actuatorId";
  private static final String VALUENAMES = "valueNames";
  private static final String DATA = "data";
  private static final String CONFIGURATION = "configuration";
  private static final String ACCESSROLE = "accessRole";
  
  public MetadataActuatorDataRequestModel(String method, String actuatorID){
    super("ActuatorDataRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(ACTUATORID).build();
    setRequired(req);
    loadActuatorDataReq("Not Defined", method, actuatorID, Json.createArrayBuilder().build(), Json.createArrayBuilder().build(), Json.createArrayBuilder().build(), "Not defined");
  }
  
  public MetadataActuatorDataRequestModel(String authToken,String method, String actuatorID, JsonArray valueNames, JsonArray data,  JsonArray config, String access){
    super("ActuatorDataRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(ACTUATORID).build();
    setRequired(req);
    loadActuatorDataReq(authToken, method, actuatorID, valueNames, data, config, access);
  }
 
  public void loadActuatorDataReq(String authToken,String method, String actuatorId, JsonArray valueNames, JsonArray data,  JsonArray config, String access){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(AUTHTOKEN,authToken)
    .add(METHOD,method)
    .add(ACTUATORID,actuatorId)
    .add(VALUENAMES,valueNames)
    .add(DATA, data)
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
        .add(AUTHTOKEN, Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(METHOD, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .add(ACTUATORID,Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(VALUENAMES,Json.createObjectBuilder()
            .add("type","array")
            .add("description","An ordered array with all the value names of this actuator. The same order will be applied to the data array and lastMeasured array.")
            .add("items",Json.createObjectBuilder()
                .add("type","String")
                .build())
            .build())
        .add(DATA,Json.createObjectBuilder()
            .add("type","array")
            .add("description","An ordered array with all the data values of this actuator. Each data element in the array should be ordered in the same position of its corresponding value elements in the valueNames array.")
            .add("items",Json.createObjectBuilder()
                .add("type","any")
                .build())
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
