package org.colos.ejss.model_elements.numerics;

import javax.swing.*;

import org.colos.ejs.model_elements.*;

public class NumericJavascriptElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/numerics/NumericJavascript.png"); // This icon is included in this jar
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "NumericJS"; }
  
  public String getConstructorName() { return "numeric"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return "var numeric = {};"; 
  }

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    return "var " + name + " = numeric;";
  }
  
  public String getImportStatements() { 
    return "Numerics/numeric-1.2.6.min.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "performs numerical computations in pure javascript";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/numerics/NumericJavascript.html"; 
  }
  
}
