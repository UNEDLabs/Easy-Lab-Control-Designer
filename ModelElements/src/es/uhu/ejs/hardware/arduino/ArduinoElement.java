package es.uhu.ejs.hardware.arduino;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.opensourcephysics.tools.ResourceLoader;

public class ArduinoElement extends AbstractModelElement {
  static private final ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/arduino/Arduino.png"); // This icon is included in this jar
  static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
  static private final Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_URL_HEADER = "<IPaddress>";
  static private final String END_URL_HEADER = "</IPaddress>";
  static private final String BEGIN_PORT_HEADER = "<PortNumber>";
  static private final String END_PORT_HEADER = "</PortNumber>";

  private JTextField mIPField = new JTextField();  // needs to be created to avoid null references
  private JTextField mPortNumberField = new JTextField();  // needs to be created to avoid null references

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getPackageList() { return "gnu/io/++;es/uhu/hardware/utils/++"; }

  public String getGenericName() { return "Arduino"; }
  
  public String getConstructorName() { return "es.uhu.ejs.hardware.arduino.ArduinoAdapter"; }

  public String getDestructionCode(String _name) { return _name+".close();"; }

  public String getInitializationCode(String _name) {
    String ipField = mIPField.getText().trim();
    StringBuffer buffer = new StringBuffer();
//    buffer.append("if ("+_name+"!=null) "+_name+".close();\n");
    if (ipField.length()>0) {
      buffer.append(_name + " = new " + getConstructorName() + "(this,");
      buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText().trim())+",");
      buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField.getText().trim())+");");
    }
    else buffer.append(_name + " = new " + getConstructorName() + "(this);");
    return buffer.toString();
  }
  
  public String getDisplayInfo() {
    String ipAddress = mIPField.getText().trim();
    if (ipAddress.length()>0) {
      String portNumber = mPortNumberField.getText().trim();
      return (portNumber.length()>0) ? "("+ipAddress+":"+portNumber+")" : "(" + ipAddress+ ")";
    }
    return null;
  }
  
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_URL_HEADER+mIPField.getText()+END_URL_HEADER + "\n");
    buffer.append(BEGIN_PORT_HEADER+mPortNumberField.getText()+END_PORT_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    mIPField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_URL_HEADER,END_URL_HEADER,false));
    mPortNumberField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false));
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides an interface to control and Arduino card";
  }
  
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/arduino/Arduino.html"; }

  public void setFont(Font font) { 
    mIPField.setFont(font);
    mPortNumberField.setFont(font);
  }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(ArduinoElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(ArduinoElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(ArduinoElement.this); }
    };
    
    JLabel urlLabel = new JLabel(RES.getString("Phidget.ServerIP"),SwingConstants.RIGHT);
    urlLabel.setForeground(COLOR);
    urlLabel.setBorder(LABEL_BORDER);
    
    mIPField.getDocument().addDocumentListener (documentListener);

    JButton urlLinkButton = new JButton(LINK_ICON);
    urlLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mIPField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mIPField,"String", value);
        if (variable!=null) mIPField.setText("%"+variable+"%");
      }
    });

    
    JLabel portLabel = new JLabel(RES.getString("Phidget.PortNumber"),SwingConstants.RIGHT);
    portLabel.setForeground(COLOR);
    portLabel.setBorder(LABEL_BORDER);
    
    mPortNumberField.getDocument().addDocumentListener (documentListener);

    JButton portLinkButton = new JButton(LINK_ICON);
    portLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mPortNumberField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mPortNumberField,"int", value);
        if (variable!=null) mPortNumberField.setText("%"+variable+"%");
      }
    });

    Set<JLabel> labelSet = new HashSet<JLabel>(); // list of labels to adjust size
    labelSet.add(urlLabel);
    labelSet.add(portLabel);

    JPanel urlPanel = new JPanel(new BorderLayout());
    urlPanel.add(urlLabel,BorderLayout.WEST);
    urlPanel.add(mIPField,BorderLayout.CENTER);
    urlPanel.add(urlLinkButton,BorderLayout.EAST);
    
    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel,BorderLayout.WEST);
    portPanel.add(mPortNumberField,BorderLayout.CENTER);
    portPanel.add(portLinkButton,BorderLayout.EAST);

    JPanel topPanel = new JPanel (new GridLayout(1,2));
    topPanel.add(urlPanel);
    topPanel.add(portPanel);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);

    // --- Make all labels in the set the same dimension
//    int maxWidth = 0, maxHeight=0;
//    for (JLabel label : labelSet) {
//      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
//      maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
//    }
//    Dimension dim = new Dimension (maxWidth,maxHeight);
//    for (JLabel label : labelSet) label.setPreferredSize(dim);
    
    return mainPanel;
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mIPField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPortNumberField,info,searchString,mode,this,name,collection);
    return list;
  }

}
