package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "SensorResponseData": {
      "id": "SensorResponseData",
      "required": [],
      "properties": {
          "valueNames": {
              "type": "array",
              "description": "An ordered array with all the value names of this sensor. The same order will be applied to the data array and lastMeasured array.",
              "items": {
                  "type": "string"
              }
          },
          "data": {
              "type": "array",
              "description": "An ordered array with all the data values of this sensor. Each data element in the array should be ordered in the same position of its corresponding value elements in the values array.",
              "items": {
                  "type": "any"
              }
          },
          "lastMeasured": {
              "type": "array",
              "description": "An ordered array with all the data values of this sensor. Each data element in the array should be ordered in the same position of its corresponding value elements in the values array.",
              "items": {
                  "type": "date-time"
              }
          }
      }
  },
 * */

public class MetadataSensorResponseDataModel extends MetadataModel {

  private static final String VALUENAMES = "valueNames";
  private static final String DATA = "data";
  private static final String LASTMEASURED = "lastMeasured";
  
  public MetadataSensorResponseDataModel(){
    super("SensorResponseData");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadSensorResp(Json.createArrayBuilder().build(), Json.createArrayBuilder().build(), Json.createArrayBuilder().build());
  }
  
  public MetadataSensorResponseDataModel(JsonArray valueNames, JsonArray data, JsonArray lastMeasured){
    super("SensorResponseData");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadSensorResp(valueNames, data, lastMeasured);
  }
 
  public void loadSensorResp(JsonArray valueNames, JsonArray data, JsonArray lastMeasured){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(VALUENAMES,valueNames)
    .add(DATA,data)
    .add(LASTMEASURED,lastMeasured);
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
        .add(VALUENAMES, Json.createObjectBuilder()
            .add("type","array")
            .add("description","An ordered array with all the value names of this sensor. The same order will be applied to the data array and lastMeasured array.")
            .add("items",Json.createObjectBuilder().add("type", "String"))
            .build())
        .add(DATA,Json.createObjectBuilder()
            .add("type","any")
            .add("description","An ordered array with all the data values of this sensor. Each data element in the array should be ordered in the same position of its corresponding value elements in the values array.")
            .add("items",Json.createObjectBuilder().add("type", "any"))
            .build())
        .add(DATA,Json.createObjectBuilder()
            .add("type","any")
            .add("description","An ordered array with all the data values of this sensor. Each data element in the array should be ordered in the same position of its corresponding value elements in the values array.")
            .add("items",Json.createObjectBuilder().add("type", "date-time"))
            .build())
        .build();
    this.setDefinitions(description);
  }
}
