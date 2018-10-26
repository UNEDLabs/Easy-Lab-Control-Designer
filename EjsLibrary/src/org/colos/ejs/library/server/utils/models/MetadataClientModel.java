package org.colos.ejs.library.server.utils.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "Client": {
            "id": "Client",
            "properties": {
                "type": {
                    "type": "string",
                    "description": "The type of client application",
                    "enum": [
                        "OpenSocial Gadget",
                        "Web page"
                    ]
                },
                "url": {
                    "type": "string",
                    "description": "The URI where the client application resides"
                }
            }
        },
 * */

public class MetadataClientModel extends MetadataModel {

  private static final String TYPE = "type";
  private static final String URL = "url";
  
  public MetadataClientModel(){
    super("Client");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadClient("Web Page", "Not Defined");
  }
  
  public MetadataClientModel(String type){
    super("Client");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadClient(type, "Not Defined");
  }
  
  public MetadataClientModel(String type, String url){
    super("Client");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadClient(type, url);
  }
 
  
  public void loadClient(String type, String url){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(TYPE,type)
    .add(URL,url);
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
        .add(TYPE, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The type of client application")
            .add("enum", Json.createArrayBuilder()
                .add("OpenSocial Gadget")
                .add("Web page"))
            .build())
        .add(URL,Json.createObjectBuilder()
            .add("type","String")
            .add("description","The URI where the client application resides")
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataClientModel value1 = new MetadataClientModel();
    
    JsonObject description = Json.createObjectBuilder()
        .add(TYPE, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The type of client application")
            .add("enum", Json.createArrayBuilder()
                .add("OpenSocial Gadget")
                .add("Web page"))
            .build())
        .add(URL,Json.createObjectBuilder()
            .add("type","String")
            .add("description","The URI where the client application resides")
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "Motor")
        .add("required", Json.createArrayBuilder().add("name").build())
        .add("properties", Json.createObjectBuilder()
            .add(TYPE,"Web page")
            .add(URL,"www.noPage.com")
            .build())
        .build();
    value1.load(sens1);
    value1.setDefinitions(description);
    System.out.println("Client: \n" + value1);
    System.out.println("Client Model: \n" + value1.modelDescToString());
    //ID,isSensor, name, type, lastMeasurelastMeasured, uptFreq, unit, min, max
    MetadataValueModel value2 = new MetadataValueModel(true,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value2);
    MetadataValueModel value3 = new MetadataValueModel(false,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value3);
  }


}
