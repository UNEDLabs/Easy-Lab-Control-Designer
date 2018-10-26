package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/*
 "ConfigurationItem": {
    "id": "ConfigurationItem",
    "required": [
        "parameter", "value"
    ],
    "properties": {
        "parameter": {
            "type": "string",
            "description": "The name of the configuration parameter"
        },
        "value": {
            "type": "any",
            "description": "The value to set the configuration parameter to. The type should equal the type given in the metadata for this sensor."
        }
    }
},
 * */

public class MetadataConfigurationItemModel extends MetadataModel {

  private static final String PARAMETER = "parameter";
  private static final String VALUE = "value";
  
  /*public MetadataConfigurationItemModel(){
    super("SensorMetadataResponse");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORS).build();
    setRequired(req);
    loadResp("None", Json.createArrayBuilder().build());
  }*/
  
  /*public MetadataConfigurationItemModel(String method){
    super("ConfigurationItem");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(METHOD).add(SENSORS).build();
    setRequired(req);
    loadResp(method, Json.createArrayBuilder().build());
  }*/
  
  public MetadataConfigurationItemModel(String method, JsonValue value){
    super("ConfigurationItem");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().add(PARAMETER).add(VALUE).build();
    setRequired(req);
    loadConfigItem(method, value);
  }
 
  public void loadConfigItem(String method, JsonValue value){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(PARAMETER,method)
    .add(VALUE,value);
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
        .add(VALUE,Json.createObjectBuilder()
            .add("type","any")
            .add("description","The value to set the configuration parameter to. The type should equal the type given in the metadata for this sensor.")
            .build())
        .build();
    this.setDefinitions(description);
  }
  

}
