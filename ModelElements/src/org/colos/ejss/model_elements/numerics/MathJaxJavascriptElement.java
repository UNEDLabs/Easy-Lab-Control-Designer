package org.colos.ejss.model_elements.numerics;

import javax.swing.*;

import org.colos.ejs.model_elements.*;

public class MathJaxJavascriptElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/numerics/mathjax.png"); // This icon is included in this jar
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "MathJax"; }
  
  public String getConstructorName() { return "MathJax"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return "";
  }

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    return "";
  }

  public String getPackageList() { 
    return "Numerics/_MathJax-2.7.2/";
  } 

  public String getImportStatements() { 
    return "Numerics/_MathJax-2.7.2/MathJax.ejsS.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "display engine for LaTeX, MathML, and AsciiMath notation";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/numerics/mathjax.html"; 
  }
  
}
