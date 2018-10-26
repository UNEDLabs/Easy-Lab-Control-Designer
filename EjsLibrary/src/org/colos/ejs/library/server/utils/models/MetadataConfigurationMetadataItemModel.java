package org.colos.ejs.library.server.utils.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 * "ConfigurationMetadataItem": {
            "id": "ConfigurationMetadataItem",
            "required": [
                "parameter", "type"
            ],
            "properties": {
                "parameter": {
                    "type": "string",
                    "description": "The name of the configuration parameter"
                },
                "description": {
                    "type": "string",
                    "description": "This field can provide some more information on how this parameter should be used."
                },
                "type": {
                    "type": "string",
                    "description": "The data type of that this configuration parameters expects, e.g. number or string",
                    "enum": [
                        "integer","long","float","double","string","byte","boolean","date","dateTime","object","array","any","binary"
                    ]
                },
                "items": {
                    "type": "string",
                    "description": "This field should only be used when the type is 'array'. It describes which types are present within the array",
                    "enum": [
                        "integer","long","float","double","string","byte","boolean","date","dateTime","object","any","binary"
                    ]
                }
            }
        },
 * */

public class MetadataConfigurationMetadataItemModel extends MetadataModel {

  private static final String PARAMETER = "parameter";
  private static final String DESCRIPTION = "description";
  private static final String TYPE = "type";
  private static final String ITEMS = "items";
  
  public MetadataConfigurationMetadataItemModel(String parameter, String type){
    super("ConfigurationMetadataItem");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(PARAMETER).add(TYPE).build();
    setRequired(req);
    loadConfig(parameter, "Not Defined", type, "Not Defined");
  }
  
  public MetadataConfigurationMetadataItemModel(String parameter, String description, String type,String items){
    super("ConfigurationMetadataItem");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(PARAMETER).add(TYPE).build();
    setRequired(req);
    loadConfig(parameter, description, type, items);
  }
 
  
  public void loadConfig(String parameter, String description, String type,String items){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(PARAMETER,parameter)
    .add(TYPE,type)
    .add(DESCRIPTION,description)
    .add(ITEMS,items);
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
        .add(PARAMETER, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The name of the configuration parameter")
            .build())
        .add(TYPE, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The data type of that this configuration parameters expects, e.g. number or string")
            .add("enum",Json.createArrayBuilder()
                .add("integer")
                .add("long")
                .add("float")
                .add("double")
                .add("string")
                .add("byte")
                .add("boolean")
                .add("date")
                .add("dateTime")
                .add("object")
                .add("array")
                .add("any")
                .add("binary")
                .build())
            .build())
        .add(DESCRIPTION,Json.createObjectBuilder()
            .add("type","String")
            .add("description","This field can provide some more information on how this parameter should be used.")
            .build())
        .add(ITEMS, Json.createObjectBuilder()
            .add("type","String")
            .add("description","This field should only be used when the type is 'array'. It describes which types are present within the array")
            .add("enum",Json.createArrayBuilder()
                .add("integer")
                .add("long")
                .add("float")
                .add("double")
                .add("string")
                .add("byte")
                .add("boolean")
                .add("date")
                .add("dateTime")
                .add("object")
                .add("any")
                .add("binary")
                .build())
            .build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataConfigurationMetadataItemModel value1 = new MetadataConfigurationMetadataItemModel("radius","array");
    
    JsonObject description = Json.createObjectBuilder()
        .add(PARAMETER, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The name of the configuration parameter")
            .build())
        .add(TYPE, Json.createObjectBuilder()
            .add("type","String")
            .add("description","The data type of that this configuration parameters expects, e.g. number or string")
            .add("enum",Json.createArrayBuilder()
                .add("integer")
                .add("long")
                .add("float")
                .add("double")
                .add("string")
                .add("byte")
                .add("boolean")
                .add("date")
                .add("dateTime")
                .add("object")
                .add("array")
                .add("any")
                .add("binary")
                .build())
            .build())
        .add(DESCRIPTION,Json.createObjectBuilder()
            .add("type","String")
            .add("description","This field can provide some more information on how this parameter should be used.")
            .build())
        .add(ITEMS, Json.createObjectBuilder()
            .add("type","String")
            .add("description","This field should only be used when the type is 'array'. It describes which types are present within the array")
            .add("enum",Json.createArrayBuilder()
                .add("integer")
                .add("long")
                .add("float")
                .add("double")
                .add("string")
                .add("byte")
                .add("boolean")
                .add("date")
                .add("dateTime")
                .add("object")
                .add("any")
                .add("binary")
                .build())
            .build())
        .build();
    
    JsonObject sens1 = Json.createObjectBuilder()
        .add("name", "Motor")
        .add("required", Json.createArrayBuilder().add("name").build())
        .add("properties", Json.createObjectBuilder()
            .add("name", "Sensor1")
            .add("type", "float")
            .add("lastMeasured", (new Date()).toString())
            .add("updateFrequency",3).build()
            ).build();
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
