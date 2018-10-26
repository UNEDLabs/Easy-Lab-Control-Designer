package org.colos.ejs.library.server.utils.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 * "AccessMode": {
            "id": "AccessMode",
            "properties": {
                "type": {
                    "type": "string",
                    "enum": [
                        "push",
                        "pull",
                        "stream"
                    ]
                },
                "nominalUpdateInterval": {
                    "type": "number",
                    "format": "float"
                },
                "userModifiableFrequency": {
                    "type": "boolean",
                    "defaultValue": false
                }
            }
        },
 * */

public class MetadataAccessModeModel extends MetadataModel {
  private static final String TYPE = "type";
  private static final String NOMINTERVAL = "nominalUpdateInterval";
  private static final String MODIFFREQ = "userModifiableFrequency";
  
  public MetadataAccessModeModel(){
    super("AccessMode");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadAccess("push", 0.0, false);
  }
  
  public MetadataAccessModeModel(String type){
    super("AccessMode");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadAccess(type, 0.0, false);
  }
  
  public MetadataAccessModeModel(String type, double nomInterval, boolean modifFreq){
    super("AccessMode");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadAccess(type, nomInterval, modifFreq);
  }
 
  
  public void loadAccess(String type, double nomInterval, boolean modifFreq){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(TYPE,type)
    .add(NOMINTERVAL,nomInterval)
    .add(MODIFFREQ,modifFreq);
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
            .add("enum",Json.createArrayBuilder()
                .add("push")
                .add("pull")
                .add("stream")
                .build())
            .build())
        .add(NOMINTERVAL,Json.createObjectBuilder()
            .add("type","number")
            .add("format","float")
            .build())
        .add(MODIFFREQ, Json.createObjectBuilder()
            .add("type","boolean")
            .add("defaultValue",false)
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataAccessModeModel value1 = new MetadataAccessModeModel();
    
    JsonObject description = Json.createObjectBuilder()
        .add(TYPE, Json.createObjectBuilder()
            .add("type","String")
            .add("enum",Json.createArrayBuilder()
                .add("push")
                .add("pull")
                .add("stream")
                .build())
            .build())
        .add(NOMINTERVAL,Json.createObjectBuilder()
            .add("type","number")
            .add("format","float")
            .build())
        .add(MODIFFREQ, Json.createObjectBuilder()
            .add("type","boolean")
            .add("defaultValue",false)
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "Motor")
        .add("required", Json.createArrayBuilder().add("name").build())
        .add("properties", Json.createObjectBuilder()
            .add("type", "push")
            .add("nominalInterval", 0.1)
            .add("userModifiableFrequency", false)
            .build())
        .build();
    value1.load(sens1);
    value1.setDefinitions(description);
    System.out.println("Value: \n" + value1);
    System.out.println("Value: \n" + value1.modelDescToString());
    //ID,isSensor, name, type, lastMeasurelastMeasured, uptFreq, unit, min, max
    MetadataValueModel value2 = new MetadataValueModel(true,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value2);
    MetadataValueModel value3 = new MetadataValueModel(false,"Sensor1", "float", (new Date()).toString() ,10,"",-10.5,10.5);
    System.out.println("Value: \n" + value3);
  }

}
