package org.colos.ejss.model_elements.input_output;

import javax.swing.*;

import org.colos.ejs.model_elements.*;

public class TinyBoxElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/input_output/TinyBox.png"); // This icon is included in this jar
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "TinyBox"; }
  
  public String getConstructorName() { return "TINY.box"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return "var TINY = {};"; 
  }

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    return "var " + name + " = TINY.box;";
  }
  
  public String getImportStatements() { 
    return "InputOutput/TinyBox2_packed.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "creates modal windows in Javascript";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/input_output/TinyBox.html"; 
  }
  
}
