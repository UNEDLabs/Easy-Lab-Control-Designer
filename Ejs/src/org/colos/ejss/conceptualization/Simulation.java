package org.colos.ejss.conceptualization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Simulation {
  static private final String sEJSS_TAG             = "EJS";
  static private final String sEJSS_VERSION         = "1.1";

  private String tag;
  private String name;
  private String version;
  
  private Information information;
  private Description description;
  private Model model;
  private View view;
  
  public Simulation(String name) {
    this.tag = sEJSS_TAG;
    this.name = name;
    this.version = sEJSS_VERSION;
    
    this.information = new Information();
    this.description = new Description();
    this.model = new Model();
    this.view = new View();
  }

  public String toJSON() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();  
    return gson.toJson(this); 
  }

}

