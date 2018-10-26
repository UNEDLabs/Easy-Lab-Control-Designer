package org.colos.ejs.library.server.utils.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 * "SensorMetadataResponse": {
            "id": "SensorMetadataResponse",
            "required": [
                "method", "sensors"
            ],
            "properties": {
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                },
                "sensors": {
                    "type": "array",
                    "items": {
                        "$ref": "Sensor"
                    }
                }
            }
        },
 * */

public class MetadataSensorMetadataResponseModel extends MetadataModel {
  private static final String METHOD = "method";
  private static final String SENSORS = "sensors";
  
  /*public MetadataSensorMetadataResponseModel(){
    super("SensorMetadataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORS).build();
    setRequired(req);
    loadResp("None", Json.createArrayBuilder().build());
  }*/
  
  /*public MetadataSensorMetadataResponseModel(String method, ){
    super("SensorMetadataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORS).build();
    setRequired(req);
    loadResp(method, Json.createArrayBuilder().build());
  }*/
  
  public MetadataSensorMetadataResponseModel(String method, JsonArray sensors){
    super("SensorMetadataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORS).build();
    setRequired(req);
    loadSensResp(method, sensors);
  }
 
  public void loadSensResp(String method, JsonArray sensors){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(SENSORS,sensors);
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
        .add(SENSORS,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","Sensor")
                .build())
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataSensorMetadataResponseModel value1 = new MetadataSensorMetadataResponseModel("getClientMetadata",Json.createArrayBuilder().build());
    MetadataSensorModel sensor1 = new MetadataSensorModel("Sensor3112","MotorShaft");
    
    
    
    JsonObject description = Json.createObjectBuilder()
        .add(METHOD, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .add(SENSORS,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","Sensor")
                .build())
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "ClientResponse")
        .add("required", Json.createArrayBuilder().build())
        .add("properties", Json.createObjectBuilder()
            .add("method", "getSensorMetadata")
            .add("sensors", sensor1.getJSON())
            .build())
        .build();
    value1.load(sens1);
    value1.setDefinitions(description);
    System.out.println("SensrResponse: \n" + value1);
    System.out.println("SensorResponse: \n" + value1.modelDescToString());
    //ID,isSensor, name, type, lastMeasurelastMeasured, uptFreq, unit, min, max
    MetadataValueModel value2 = new MetadataValueModel(true,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value2);
    MetadataValueModel value3 = new MetadataValueModel(false,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value3);
    
    System.out.println("Classname : " + value1.getClass().getSimpleName().substring(8).replace("model", ""));
    System.out.println("Classname : " + value3.getName());
  }



}
