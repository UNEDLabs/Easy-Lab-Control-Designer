package org.colos.ejss.model_elements.input_output;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;

public class EjsSClientSimulationElement extends WebSocketElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/input_output/EjsSClientSimulation.png"); // This icon is included in this jar

  static private final String DEFAULT_CODE = "function(keyword,data) { // What to do with the input message\n  // do nothing\n}";
  
  {
    mCodeEditor.setName("EjsSClientSimulation");
    mCodeEditor.readPlainCode(DEFAULT_CODE);  
  }
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "EjssClient"; }
  
  public String getConstructorName() { return "ejsSClientSimulation"; }
  
  public String getInitializationCode(String _name) {
    return "var EJSS_INPUT_OUTPUT = EJSS_INPUT_OUTPUT || {};"; 
  }
  
  public String getSourceCode(String name) { // Code that goes into the body of the model 
    String port = getValue(mPortField.getText().trim());
    String service = getValue(mServiceField.getText().trim());
    return "var " + name + " = EJSS_INPUT_OUTPUT."+getConstructorName()+" (_model,"+service+","+port+",\n"+ 
        mCodeEditor.generateCode(name, "    ")+");\n";
  }  

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return  "encapsulates a WebSocket client to send/receive data to/from a remote EjsS WebSocket server.";
  }

  protected String getHtmlPage() { return "org/colos/ejss/model_elements/input_output/EjsSClientSimulation.html"; }
  
}
