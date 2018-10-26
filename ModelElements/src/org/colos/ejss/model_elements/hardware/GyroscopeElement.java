package org.colos.ejss.model_elements.hardware;

import javax.swing.*;

import org.colos.ejs.model_elements.*;

public class GyroscopeElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/hardware/Gyroscope.png"); // This icon is included in this jar
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Gyroscope"; }
  
  public String getConstructorName() { return "gyroscope"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return "var EJSS_HARDWARE = EJSS_HARDWARE || {};"; 
  }

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    return "var " + name + " = EJSS_HARDWARE.gyroscope();";
  }  

  public String getImportStatements() { // Required for Lint
    return "Hardware/window_sensors.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access to the built-in gyroscope";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/hardware/Gyroscope.html"; 
  }
  
}
