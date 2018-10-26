package org.colos.ejs.library.server.utils.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "ClientResponse": {
            "id": "ClientResponse",
            "properties": {
                "method": {
                    "type": "string"
                },
                "clients": {
                    "type": "array",
                    "items": {
                        "$ref": "Client"
                    }
                }
            }
        },
 * */

public class MetadataClientResponseModel extends MetadataModel {

  private static final String METHOD = "method";
  private static final String CLIENTS = "clients";
  
  public MetadataClientResponseModel(){
    super("ClientResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadResp("None", Json.createArrayBuilder().build());
  }
  
  public MetadataClientResponseModel(String method){
    super("ClientResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadResp(method, Json.createArrayBuilder().build());
  }
  
  public MetadataClientResponseModel(String method, JsonArray clients){
    super("ClientResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadResp(method, clients);
  }
 
  public void loadResp(String method, JsonArray clients){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(METHOD,method)
    .add(CLIENTS,clients);
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
            .build())
        .add(CLIENTS,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","Client")
                .build())
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataClientResponseModel value1 = new MetadataClientResponseModel();
    MetadataClientModel client1 = new MetadataClientModel();
    
    JsonObject description = Json.createObjectBuilder()
        .add(METHOD, Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(CLIENTS,Json.createObjectBuilder()
            .add("type","array")
            .add("items",Json.createObjectBuilder()
                .add("$ref","Client")
                .build())
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "ClientResponse")
        .add("required", Json.createArrayBuilder().build())
        .add("properties", Json.createObjectBuilder()
            .add("method", "whoEverCallMe")
            .add("clients", client1.getJSON())
            .build())
        .build();
    value1.load(sens1);
    value1.setDefinitions(description);
    System.out.println("ClientResponse: \n" + value1);
    System.out.println("ClientResponse: \n" + value1.modelDescToString());
    //ID,isSensor, name, type, lastMeasurelastMeasured, uptFreq, unit, min, max
    MetadataValueModel value2 = new MetadataValueModel(true,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value2);
    MetadataValueModel value3 = new MetadataValueModel(false,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value3);
    
    System.out.println("Classname : " + value1.getClass().getSimpleName().substring(8).replace("model", ""));
    System.out.println("Classname : " + value3.getName());
  }


}
