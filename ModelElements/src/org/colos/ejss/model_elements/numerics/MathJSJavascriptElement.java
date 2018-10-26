package org.colos.ejss.model_elements.numerics;

import javax.swing.*;

import org.colos.ejs.model_elements.*;

public class MathJSJavascriptElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/numerics/mathjs.png"); // This icon is included in this jar
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "MathJS"; }
  
  public String getConstructorName() { return "MathJS"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return "";
  }

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    return "";
  }

  public String getPackageList() { 
    return "";
  } 

  public String getImportStatements() { 
    return "Numerics/math-3.16.4.min.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "extensive math library for JavaScript";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/numerics/mathjs.html"; 
  }
  
}
