package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "Actuator": {
            "id": "Actuator",
            "required": [
                "actuatorId", "fullName"
            ],
            "properties": {
                "actuatorId": {
                    "type": "string"
                },
                "fullName": {
                    "type": "string"
                },
                "description": {
                    "type": "string"
                },
                "webSocketType": {
                    "type": "string",
                    "description": "the type of websocket. Websockets can either be binary or textual.",
                    "enum": [
                        "text",
                        "binary"
                    ],
                    "defaultValue": "text"
                },
                "singleWebSocketRecommended": {
                    "type": "boolean",
                    "description": "If this field is set to true it means that the smart device expects that a client opens a dedicated websocket for to read from this value",
                    "defaultValue": false
                },
                "consumes": {
                    "type": "string",
                    "description": "The mime-type of the data that is consumed by this actuator. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type",
                    "defaultValue": "application/json"
                },
                "produces": {
                    "type": "string",
                    "description": "The mime-type of the data that is produced by this actuator. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type",
                    "defaultValue": "application/json"
                },
                "values": {
                    "type": "array",
                    "items": {
                        "$ref": "Value"
                    }
                },
                "configuration": {
                    "type": "array",
                    "description": "The configuration consists of an array of JSON objects that consist of parameter and type",
                    "items": {
                        "$ref": "ConfigurationMetadataItem"
                    }
                },
                "accessMode": {
                    "type": "AccessMode"
                }
            }
        },
 * */

public class MetadataActuatorModel extends MetadataModel {

  private static final String ACTUATORID = "actuatorId";
  private static final String FULLNAME = "fullName";
  private static final String DESCRIPTION = "description";
  private static final String WEBSOCKETTYPE = "webSocketType";
  private static final String SINGLEWEBSOCKET = "singleWebSocketRecommended";
  private static final String CONSUMES = "produces";
  private static final String PRODUCES = "produces";
  private static final String VALUES = "values";
  private static final String CONFIGURATION = "configuration";
  private static final String ACCESSMODE = "accessMode";
  
  public MetadataActuatorModel(String actuatorID, String fullName){
    super("Actuator");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(ACTUATORID).add(FULLNAME).build();
    setRequired(req);
    loadActuator(actuatorID, fullName, "Not Defined", "Not Defined", false, 
        "Not Defined", "Not Defined", Json.createArrayBuilder().build(), Json.createArrayBuilder().build(),
        Json.createObjectBuilder().build());
  }
  
  public MetadataActuatorModel(String actuatorID, String fullName, String description, String websocketType,
      boolean singleWS, String consumes, String produces){
    super("Actuator");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(ACTUATORID).add(FULLNAME).build();
    setRequired(req);
    loadActuator(actuatorID, fullName, description, websocketType, singleWS, consumes, produces, 
        Json.createArrayBuilder().build(), Json.createArrayBuilder().build(),
        Json.createObjectBuilder().build());
  }
  
  public void loadActuator(String actuatorID, String fullName, String description, String websocketType,
      boolean singleWS, String consumes, String produces, JsonArray values, JsonArray config, JsonObject access){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(ACTUATORID,actuatorID)
    .add(FULLNAME,fullName)
    .add(DESCRIPTION,description)
    .add(WEBSOCKETTYPE,websocketType)
    .add(SINGLEWEBSOCKET,singleWS)
    .add(CONSUMES,consumes)
    .add(PRODUCES,produces)
    .add(VALUES,values)
    .add(CONFIGURATION,config)
    .add(ACCESSMODE,access);
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
        .add(ACTUATORID, Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(FULLNAME, Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(DESCRIPTION,Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(WEBSOCKETTYPE, Json.createObjectBuilder()
            .add("type","String")
            .add("description","the type of websocket. Websockets can either be binary or textual.")
            .add("enum",Json.createArrayBuilder()
                .add("text")
                .add("binary")
                .build())
            .add("defaultValue","text")
            .build())
        .add(SINGLEWEBSOCKET, Json.createObjectBuilder()
            .add("type","boolean")
            .add("description","If this field is set to true it means that the smart device expects that a client opens a dedicated websocket for to read from this value")
            .add("defaultValue",false)
            .build())
        .add(CONSUMES, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The mime-type of the data that is consumed by this actuator. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type")
            .add("defaultValue","application/json")
            .build())
        .add(PRODUCES, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The mime-type of the data that is produced by this actuator. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type")
            .add("defaultValue","application/json")
            .build())
        .add(VALUES, Json.createObjectBuilder()
            .add("type","array")
            .add("items","$Value")
            .build())
        .add(CONFIGURATION, Json.createObjectBuilder()
            .add("type","array")
            .add("description","The configuration consists of an array of JSON objects that consist of parameter and type")
            .add("items","$ConfigurationMetadataItem")
            .build())
        .add(ACCESSMODE, Json.createObjectBuilder()
            .add("type","$AccessMode")
            .build())
        .build();
    this.setDefinitions(description);
  }

}
