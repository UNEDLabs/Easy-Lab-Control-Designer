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

public class WebSocketServerElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/WebSocketServer.png"); // This icon is included in this jar
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_PORT_HEADER = "<Port><![CDATA[";
  static private final String END_PORT_HEADER = "]]></Port>"; 
  static private final String DEFAULT_CODE = "public void processInput (String message) { // What to do with an input message from a client\n  // do nothing\n}";
  
  protected JTextField mPortField = new JTextField();  // needs to be created to avoid null references
  protected ModelElementEditor mCodeEditor = new ModelElementEditor(this,null);  // The editor for the code

  {
    mCodeEditor.setName("WebSocketServerElement");
    mCodeEditor.readPlainCode(DEFAULT_CODE);  
  }
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "WebSocketServer"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.WebSocketUtil"; }
  
  public String getInitializationCode(String _name) {
    String port = mPortField.getText().trim();
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name+"==null) "+ _name + " = new " + getConstructorName()+"("+port+") {\n");
    buffer.append(mCodeEditor.generateCode(_name, "  "));
    buffer.append("};\n");
    return buffer.toString();
  }
  
  public String getDestructionCode(String _name) { 
    return "if ("+_name+"!=null) "+ _name + ".stop();\n"; 
  }

  
  public String getDisplayInfo() {
    String port = mPortField.getText().trim();
    if (port.length()<=0) return null; 
    return "("+port+")";
  }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_PORT_HEADER+mPortField.getText()+END_PORT_HEADER + "\n");
    buffer.append(mCodeEditor.saveStringBuffer());
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    mCodeEditor.readXmlString(_inputXML);
    String portStr = OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false);
    if (portStr!=null) mPortField.setText(portStr);
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "starts a WebSocket service";
  }

  public void setFont(Font font) { 
    mPortField.setFont(font); 
    mCodeEditor.setFont(font); 
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/WebSocketServer.html"; }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JLabel portLabel = new JLabel(" Port:",SwingConstants.RIGHT);
    portLabel.setBorder(LABEL_BORDER);

    JLabel processLabel = new JLabel(" Processing method:",SwingConstants.RIGHT);
    processLabel.setBorder(LABEL_BORDER);

    mPortField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(WebSocketServerElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(WebSocketServerElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(WebSocketServerElement.this); }
    });

    JPanel descriptionPanel = new JPanel(new BorderLayout());
    descriptionPanel.add(portLabel, BorderLayout.WEST);
    descriptionPanel.add(mPortField, BorderLayout.CENTER);

    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.add(descriptionPanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(mCodeEditor.getComponent(collection),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mPortField,info,searchString,mode,this,name,collection);
    list.addAll(mCodeEditor.search(info, searchString, mode, name, collection));
    return list;
  }
  
}
