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

public class WebSocketElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/input_output/WebSocket.png"); // This icon is included in this jar
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_SERVICE_HEADER = "<Service><![CDATA[";
  static private final String END_SERVICE_HEADER = "]]></Service>"; 
  static private final String BEGIN_PORT_HEADER = "<IPAndPort><![CDATA[";
  static private final String END_PORT_HEADER = "]]></IPAndPort>"; 
  static private final String DEFAULT_CODE = "function(message) { // What to do with the input message\n  // do nothing\n}";
  
  protected JTextField mServiceField = new JTextField();  // needs to be created to avoid null references
  protected JTextField mPortField = new JTextField();  // needs to be created to avoid null references
  protected ModelElementEditor mCodeEditor = new ModelElementEditor(this,null);  // The editor for the code

  {
    mCodeEditor.setName("WebSocketElement");
    mCodeEditor.readPlainCode(DEFAULT_CODE);  
  }
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "WebSocketClient"; }
  
  public String getConstructorName() { return "websocket"; }
  
  public String getInitializationCode(String _name) {
    return "var EJSS_INPUT_OUTPUT = EJSS_INPUT_OUTPUT || {};"; 
  }

  static protected String getValue(String _value) {
    if (_value.startsWith("%")) return ModelElementsUtilities.removeEnclosingString(_value, "%");
    if (_value.startsWith("\"")) return _value;
    return "\"" + _value +"\"";
  }
  
  public String getSourceCode(String name) { // Code that goes into the body of the model 
    String port = getValue(mPortField.getText().trim());
    String service = getValue(mServiceField.getText().trim());
    return "var " + name + " = EJSS_INPUT_OUTPUT."+getConstructorName()+" ("+service+","+port+",\n"+ 
        mCodeEditor.generateCode(name, "    ")+");\n";
  }  

  public String getImportStatements() { // Required for Lint
    return "InputOutput/websockets_utils.js"; 
  }

  public String getDisplayInfo() {
    String service = mServiceField.getText().trim();
    int l = service.length();
    if (l>0) {
      if (l>25) service = service.substring(0,25)+"..."; 
      return "("+service+")";
    }
    String port = mPortField.getText().trim();
    if (port.length()<=0) return "";
    return "("+port+")";
  }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_SERVICE_HEADER+mServiceField.getText()+END_SERVICE_HEADER + "\n");
    buffer.append(BEGIN_PORT_HEADER+mPortField.getText()+END_PORT_HEADER + "\n");
    buffer.append(mCodeEditor.saveStringBuffer());
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    mCodeEditor.readXmlString(_inputXML);
    String serviceStr = OsejsCommon.getPiece(_inputXML,BEGIN_SERVICE_HEADER,END_SERVICE_HEADER,false);
    if (serviceStr!=null) mServiceField.setText(serviceStr);
    String portStr = OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false);
    if (portStr!=null) mPortField.setText(portStr);
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "starts a WebSocket client";
  }

  public void setFont(Font font) { 
    mServiceField.setFont(font); 
    mPortField.setFont(font); 
    mCodeEditor.setFont(font); 
  }

  protected String getHtmlPage() { return "org/colos/ejss/model_elements/input_output/WebSocket.html"; }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JLabel serviceLabel = new JLabel(" Service:",SwingConstants.RIGHT);
    serviceLabel.setBorder(LABEL_BORDER);

    JLabel portLabel = new JLabel(" IP:portnumber:",SwingConstants.RIGHT);
    portLabel.setBorder(LABEL_BORDER);

    // Make both labels the same dimension
    int maxWidth  = serviceLabel.getPreferredSize().width;
    int maxHeight = serviceLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  portLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, portLabel.getPreferredSize().height);
    Dimension dim = new Dimension (maxWidth,maxHeight);
    serviceLabel.setPreferredSize(dim);
    portLabel.setPreferredSize(dim);

    mServiceField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(WebSocketElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(WebSocketElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(WebSocketElement.this); }
    });

    mPortField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(WebSocketElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(WebSocketElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(WebSocketElement.this); }
    });

    JPanel servicePanel = new JPanel(new BorderLayout());
    servicePanel.add(serviceLabel, BorderLayout.WEST);
    servicePanel.add(mServiceField, BorderLayout.CENTER);

    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel, BorderLayout.WEST);
    portPanel.add(mPortField, BorderLayout.CENTER);

    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.add(servicePanel);
    topPanel.add(portPanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(mCodeEditor.getComponent(collection),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mServiceField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPortField,info,searchString,mode,this,name,collection);
    list.addAll(mCodeEditor.search(info, searchString, mode, name, collection));
    return list;
  }
  
}
