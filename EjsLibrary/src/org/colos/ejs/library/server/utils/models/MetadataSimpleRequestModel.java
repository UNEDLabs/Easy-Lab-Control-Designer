package org.colos.ejs.library.server.utils.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 * "SimpleRequest": {
            "id": "SimpleRequest",
            "required": [
                "method"
            ],
            "properties": {
                "authToken": {
                    "type": "string"
                },
                "method": {
                    "type": "string",
                    "description": "The method should be equal to the nickname of one of the provided services."
                }
            }
        },
*/

public class MetadataSimpleRequestModel extends MetadataModel {
  private static final String AUTHTOKEN = "authToken";
  private static final String METHOD = "method";
  
  public MetadataSimpleRequestModel(String method){
    super("SimpleRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).build();
    setRequired(req);
    loadAccess(method, "Not Defined");
  }
  
  public MetadataSimpleRequestModel(String method, String auth){
    super("SimpleRequest");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).build();
    setRequired(req);
    loadAccess(method, auth);
  }
 
  
  public void loadAccess(String method, String auth){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(AUTHTOKEN,auth)
    .add(METHOD,method);
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
        .add(METHOD,Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataSimpleRequestModel value1 = new MetadataSimpleRequestModel("callSomeone");
    
    JsonObject description = Json.createObjectBuilder()
        .add(AUTHTOKEN, Json.createObjectBuilder()
            .add("type","String")
            .build())
        .add(METHOD,Json.createObjectBuilder()
            .add("type","String")
            .add("description","The method should be equal to the nickname of one of the provided services.")
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "Motor")
        .add("required", Json.createArrayBuilder().add("name").build())
        .add("properties", Json.createObjectBuilder()
            .add(AUTHTOKEN,"1234567890")
            .add(METHOD,"callSomeone")
            .build())
        .build();
    value1.load(sens1);
    value1.setDefinitions(description);
    System.out.println("SimpleRequest: \n" + value1);
    System.out.println("SimpleRequest: \n" + value1.modelDescToString());
    //ID,isSensor, name, type, lastMeasurelastMeasured, uptFreq, unit, min, max
    MetadataValueModel value2 = new MetadataValueModel(true,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value2);
    MetadataValueModel value3 = new MetadataValueModel(false,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value3);
  }

}
