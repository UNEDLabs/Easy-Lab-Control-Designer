package org.colos.ejs.model_elements.input_output;

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

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;

public class EjsSServerSimulationElement extends WebSocketServerElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/EjsSServerSimulation.png"); // This icon is included in this jar
  
  static private final String DEFAULT_CODE = "public void processCommand (String keyword, Map<String,Object> data) { // What to do with an input command from a client\n  // do nothing\n}";
  
  {
    mCodeEditor.setName("EjsSServerSimulation");
    mCodeEditor.readPlainCode(DEFAULT_CODE);  
  }
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  @Override 
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  @Override 
  public String getGenericName() { return "ejssServer"; }
  
  @Override 
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.EjsSServerSimulation"; }
  
  @Override 
  public String getInitializationCode(String _name) {
    String port = mPortField.getText().trim();
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name+"==null) "+ _name + " = new " + getConstructorName());
    if (port.length()>0) buffer.append("(this,"+port+") {\n");
    else                 buffer.append("(this) {\n");
    buffer.append(mCodeEditor.generateCode(_name, "  "));
    buffer.append("};\n");
    return buffer.toString();
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  @Override 
  public String getTooltip() {
    return "encapsulates a WebSocket server to send/receive data to/from a remote EjsS WebSocket client.";
  }

  @Override 
  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/EjsSServerSimulation.html"; }
  
}
