package org.colos.ejs.library.server.utils.models;

//import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 * "Action":{
        "id": "Action",
            "required": [
                "actionId", "fullName","params"
            ],
            "properties": {
                "sensorId": {
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
                "produces": {
                    "type": "string",
                    "description": "The mime-type of the data that is produced by this sensor. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type",
                    "defaultValue": "application/json"
                },
                "params": {
                    "type": "array",
                    "items": {
                        "$ref": "Value"
                    }
                },
                "accessMode": {
                    "type": "AccessMode"
                }
            }
    }
 * */

public class MetadataActionModel extends MetadataModel {
  private static final String ACTIONID = "actionId";
  private static final String FULLNAME = "fullName";
  private static final String DESCRIPTION = "description";
  private static final String WEBSOCKETTYPE = "webSocketType";
  private static final String SINGLEWEBSOCKET = "singleWebSocketRecommended";
  private static final String PRODUCES = "produces";
  private static final String PARAMS = "params";
  private static final String ACCESSMODE = "accessMode";
  
  public MetadataActionModel(String actionID, String fullName){
    super("Action");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(ACTIONID).add(FULLNAME).build();
    setRequired(req);
    loadAction(actionID, fullName, "Not Defined", "Not Defined", false, 
        "Not Defined", Json.createArrayBuilder().build(),Json.createObjectBuilder().build());
  }
  
  public MetadataActionModel(String actionID, String fullName, String description, String websocketType,
      boolean singleWS, String produces){
    super("Action");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(ACTIONID).add(FULLNAME).build();
    setRequired(req);
    loadAction(actionID, fullName, description, websocketType, singleWS, produces, 
        Json.createArrayBuilder().build(),
        Json.createObjectBuilder().build());
  }
  
  public void loadAction(String actionID, String fullName, String description, String websocketType,
      boolean singleWS, String produces, JsonArray params, JsonObject access){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(ACTIONID,actionID)
    .add(FULLNAME,fullName)
    .add(DESCRIPTION,description)
    .add(WEBSOCKETTYPE,websocketType)
    .add(SINGLEWEBSOCKET,singleWS)
    .add(PRODUCES,produces)
    .add(PARAMS,params)
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
        .add(ACTIONID, Json.createObjectBuilder()
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
        .add(PRODUCES, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The mime-type of the data that is produced by this sensor. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type")
            .add("defaultValue","application/json")
            .build())
        .add(PARAMS, Json.createObjectBuilder()
            .add("type","array")
            .add("items","$Value")
            .build())
        .add(ACCESSMODE, Json.createObjectBuilder()
            .add("type","$AccessMode")
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    /*MetadataValueModel value2 = new MetadataValueModel(true,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value2);
    MetadataValueModel value3 = new MetadataValueModel(false,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value3);
    MetadataConfigurationMetadataItemModel config1 = new MetadataConfigurationMetadataItemModel("radius","array");
    
    
    MetadataActionModel value1 = new MetadataActionModel("Sensor3112","MotorShaft");
    
    JsonObject description = Json.createObjectBuilder()
        .add(ACTIONID, Json.createObjectBuilder()
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
        .add(PRODUCES, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The mime-type of the data that is produced by this sensor. A list of mime types can be found at http://en.wikipedia.org/wiki/Internet_media_type")
            .add("defaultValue","application/json")
            .build())
        .add(PARAMS, Json.createObjectBuilder()
            .add("type","array")
            .add("items","$Value")
            .build())
        .add(ACCESSMODE, Json.createObjectBuilder()
            .add("type","$AccessMode")
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "Motor")
        .add("required", Json.createArrayBuilder().add("name").build())
        .add("properties", Json.createObjectBuilder()
            .add(ACTIONID,"Sensor3112")
            .add(FULLNAME,"MotorShaft")
            .add(DESCRIPTION,"Not defined yet")
            .add(WEBSOCKETTYPE,"Not defined yet")
            .add(SINGLEWEBSOCKET,"text")
            .add(PRODUCES,"application/json")
            .add(PARAMS,value2.getJSON())
            .add(ACCESSMODE,Json.createObjectBuilder().build()))
            .build();
    value1.load(sens1);
    value1.setDefinitions(description);
    System.out.println("Sensor: \n" + value1);
    System.out.println("Sensor Description: \n" + value1.modelDescToString());
    //ID,isSensor, name, type, lastMeasurelastMeasured, uptFreq, unit, min, max
   */ 
  }
}
