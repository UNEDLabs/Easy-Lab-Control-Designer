package org.colos.ejs.library.server.utils.models;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
 "ObserverMode": {
            "id": "ObserverMode",
            "required": [],
            "properties": {
                "queueSize": {
                    "type": "integer",
                    "description": "Provides the length of the user waiting queue that want to get control of the lab"
                },
                "queuePosition": {
                    "type": "integer",
                    "description": "Provides the position of the client who made this call in the user waiting queue. This value should be positive and smaller or equal to queueSize."
                },
                "estimatedTimeUntilControl": {
                    "type": "integer",
                    "description": "The estimated waiting time from now on until the client will get controllerMode access. The time is expressed in seconds."
                }
            }
        }
 */

public class MetadataObserverModeModel extends MetadataModel {

  private static final String QUEUESIZE = "queueSize";
  private static final String QUEUEPOS = "queuePosition";
  private static final String ESTIMATEDTIME = "estimatedTimeUntilControl";
  
  public MetadataObserverModeModel(String method){
    super("ObserverMode");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadObserverMode(0,0,0);
  }
  
  public MetadataObserverModeModel(int queueSize, int queuePosition, int estTime){
    super("ObserverMode");
    loadBasicDesc();
    JsonArray req = Json.createArrayBuilder().build();
    setRequired(req);
    loadObserverMode(queueSize, queuePosition, estTime);
  }
 
  public void loadObserverMode(int queueSize, int queuePos, int estTime){
    JsonObjectBuilder propBuilder = Json.createObjectBuilder();
    propBuilder
    .add(QUEUESIZE,queueSize)
    .add(QUEUEPOS,queuePos)
    .add(ESTIMATEDTIME,estTime);
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
        .add(QUEUESIZE, Json.createObjectBuilder()
            .add("type","integer")
            .add("description","Provides the length of the user waiting queue that want to get control of the lab.")
            .build())
        .add(QUEUEPOS,Json.createObjectBuilder()
            .add("type","integer")
            .add("description","Provides the position of the client who made this call in the user waiting queue. This value should be positive and smaller or equal to queueSize.")
            .build())
        .add(ESTIMATEDTIME, Json.createObjectBuilder()
            .add("type","integer")
            .add("description","The estimated waiting time from now on until the client will get controllerMode access. The time is expressed in seconds.")
            .build())
        .build();
    this.setDefinitions(description);
  }

}
