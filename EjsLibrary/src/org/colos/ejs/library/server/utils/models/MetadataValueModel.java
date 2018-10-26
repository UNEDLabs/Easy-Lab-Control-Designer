package org.colos.ejs.library.server.utils.models;

//import java.util.ArrayList;
import java.util.Date;
//import java.util.List;


import javax.json.Json;
import javax.json.JsonArray;
//import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
//import javax.json.JsonValue;


/* Definition of the "value" model. Each value, from sensor or actuator must be created with this structure
 This is metadata not ready to use structure.
 * "Value": {
            "id": "Value",
            "required": [
                "name"
            ],
            "properties": {
                "name": {
                    "type": "string"
                },
                "unit": {
                    "type": "string"
                },
                "type": {
                    "type": "string",
                    "description": "The data type of this value",
                    "enum": [
                        "integer",
                        "long",
                        "float",
                        "double",
                        "string",
                        "byte",
                        "boolean",
                        "date",
                        "dateTime",
                        "object",
                        "array",
                        "any",
                        "binary"
                    ]
                },
                "rangeMinimum": {
                    "type": "number",
                    "format": "double"
                },
                "rangeMaximum": {
                    "type": "number",
                    "format": "double"
                },
                "rangeStep": {
                    "type": "number",
                    "format": "double"
                },
                "lastMeasured": {
                    "type": "date-time"
                },
                "updateFrequency": {
                    "type": "number",
                    "description": "The frequency in Hertz of which the sensor value updates",
                    "format": "int"
                }
            }
        },
*/
public class MetadataValueModel extends MetadataModel{
  private static final String NAME = "name";
  private static final String UNIT = "unit";
  private static final String TYPE = "type";
  private static final String RANGEMIN = "rangeMinimum";
  private static final String RANGEMAX = "rangeMaximum";
  private static final String LASTMEASURED = "lastMeasured";
  private static final String UPDATEFREQ = "updateFrequency";

  
  private boolean isSensor = true;
  
  public MetadataValueModel(boolean isSensor, String name){
    super("Value");
    this.isSensor = isSensor;
    JsonArray req = Json.createArrayBuilder().add("name").build();
    setRequired(req);
    if (isSensor){
      loadSensor(name,"Undefined","Undefined",0);
    }else{
      loadActuator(name,"Undefined", 0.0,0.0);
    }
    loadBasicDesc();
  }
  
  public MetadataValueModel(boolean isSensor, String name, String type, String lastMeasurelastMeasured,int uptFreq
      , String unit, double min, double max){
    super("Value");
    this.isSensor = isSensor;
    JsonArray req = Json.createArrayBuilder().add("name").build();
    setRequired(req);
    if (isSensor){
      loadSensor(name,type,lastMeasurelastMeasured,uptFreq);
    }else{
      loadActuator(name,unit, min,max);
    }
    loadBasicDesc();
  }
 
  
  public void loadSensor(String name, String type, String lastMeasurelastMeasured,int uptFreq){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(NAME,name)
    .add(TYPE,type)
    .add(LASTMEASURED,lastMeasurelastMeasured)
    .add(UPDATEFREQ,uptFreq);
    setProperties(propBuilder.build());
  }
  
  public void loadActuator(String name, String unit, double min, double max){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(NAME,name)
    .add(UNIT,unit)
    .add(RANGEMIN,min)
    .add(RANGEMAX,max);
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

  public boolean isSensor() {
    return isSensor;
  }

  public void setSensor(boolean isSensor) {
    this.isSensor = isSensor;
  }
  
  @Override
  protected void loadBasicDesc(){
    JsonObject description = Json.createObjectBuilder()
        .add("name", Json.createObjectBuilder()
            .add("type","String").build())
        .add("type", Json.createObjectBuilder()
            .add("type","number")
            .add("description","The data type of this value")
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
        .add("lastMeasured", (new Date()).toString())
        .add("updateFrequency",Json.createObjectBuilder()
            .add("type","number")
            .add("description","The frequency in Hertz of which the sensor value updates")
            .add("format","int")
            .build())
        .add("unit", Json.createObjectBuilder()
            .add("type","String").build())
        .add("rangeMin", Json.createObjectBuilder()
            .add("type","number")
            .add("format","double").build())
        .add("rangeMax", Json.createObjectBuilder()
            .add("type","number")
            .add("format","double").build())
        .build();
    this.setDefinitions(description);
  }
  
  public static void main (String [ ] args) {
    MetadataValueModel value1 = new MetadataValueModel(true,"Sensor1");
    
    JsonObject description = Json.createObjectBuilder()
      .add("name", Json.createObjectBuilder()
          .add("type","String").build())
      .add("type", Json.createObjectBuilder()
          .add("type","number")
          .add("description","The data type of this value")
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
      .add("lastMeasured", (new Date()).toString())
      .add("updateFrequency",Json.createObjectBuilder()
          .add("type","number")
          .add("description","The frequency in Hertz of which the sensor value updates")
          .add("format","int")
          .build())
      .add("unit", Json.createObjectBuilder()
          .add("type","String").build())
      .add("rangeMin", Json.createObjectBuilder()
          .add("type","number")
          .add("format","double").build())
      .add("rangeMax", Json.createObjectBuilder()
          .add("type","number")
          .add("format","double").build())
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
