package es.uhu.ejs.hardware.phidgets;

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

public abstract class AbstractPhidgetElement extends AbstractModelElement {
  static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_SERIAL_NUMBER = "<SerialNumber>"; // Used to delimit my XML information
  static private final String END_SERIAL_NUMBER = "</SerialNumber>";        // Used to delimit my XML information
  static private final String BEGIN_URL_HEADER = "<IPaddress>";
  static private final String END_URL_HEADER = "</IPaddress>";
  static private final String BEGIN_PORT_HEADER = "<PortNumber>";
  static private final String END_PORT_HEADER = "</PortNumber>";
  static private final String BEGIN_PASSWORD_HEADER = "<Password><![CDATA[";
  static private final String END_PASSWORD_HEADER = "]]></Password>";

  private JTextField mSerialNumberField = new JTextField();  // needs to be created to avoid null references
  private JTextField mIPField = new JTextField();  // needs to be created to avoid null references
  private JTextField mPortNumberField = new JTextField();  // needs to be created to avoid null references
  private JTextField mPasswordField = new JTextField();  // needs to be created to avoid null references

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
    
  public String getPackageList() { return "com/phidgets/++"; }

  public String getDestructionCode(String _name) { return _name+".close();"; }

  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name+"!=null) "+_name+".close();\n");
    buffer.append(_name + " = new " + getConstructorName() + "(this,");
    buffer.append(ModelElementsUtilities.getQuotedValue(mSerialNumberField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mPasswordField.getText().trim())+");\n");
    return buffer.toString();
  }

  public String getDisplayInfo() {
    String ipAddress = mIPField.getText().trim();
    String serialNumber = mSerialNumberField.getText().trim();
    if (ipAddress.length()>0) {
      return (serialNumber.length()>0) ? "(" + serialNumber+ "-"+ipAddress+")" : "(" + ipAddress+ ")";
    }
    if (serialNumber.length()>0) return "("+serialNumber+")";
    return null;
  }
  
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_URL_HEADER+mIPField.getText()+END_URL_HEADER + "\n");
    buffer.append(BEGIN_PORT_HEADER+mPortNumberField.getText()+END_PORT_HEADER + "\n");
    buffer.append(BEGIN_SERIAL_NUMBER+mSerialNumberField.getText()+END_SERIAL_NUMBER + "\n");
    buffer.append(BEGIN_PASSWORD_HEADER+mPasswordField.getText()+END_PASSWORD_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    mIPField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_URL_HEADER,END_URL_HEADER,false));
    mPortNumberField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false));
    mSerialNumberField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_SERIAL_NUMBER,END_SERIAL_NUMBER,false));
    mPasswordField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PASSWORD_HEADER,END_PASSWORD_HEADER,false));
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public void setFont(Font font) { 
    mIPField.setFont(font);
    mPortNumberField.setFont(font);
    mSerialNumberField.setFont(font); 
    mPasswordField.setFont(font);
  }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(AbstractPhidgetElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(AbstractPhidgetElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(AbstractPhidgetElement.this); }
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

    JLabel passwordLabel = new JLabel(RES.getString("Phidget.Password"),SwingConstants.RIGHT);
    passwordLabel.setForeground(COLOR);
    passwordLabel.setBorder(LABEL_BORDER);
    
    mPasswordField.getDocument().addDocumentListener (documentListener);

    JButton passwordLinkButton = new JButton(LINK_ICON);
    passwordLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mPasswordField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mPasswordField,"String", value);
        if (variable!=null) mPasswordField.setText("%"+variable+"%");
      }
    });

    JLabel serialNumberLabel = new JLabel(RES.getString("Phidget.SerialNumber"),SwingConstants.RIGHT);
    serialNumberLabel.setForeground(COLOR);
    serialNumberLabel.setBorder(LABEL_BORDER);
    
    mSerialNumberField.getDocument().addDocumentListener (documentListener);

    JButton serialNumberLinkButton = new JButton(LINK_ICON);
    serialNumberLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mSerialNumberField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mSerialNumberField,"int", value);
        if (variable!=null) mSerialNumberField.setText("%"+variable+"%");
      }
    });

    Set<JLabel> labelSet = new HashSet<JLabel>(); // list of labels to adjust size
    labelSet.add(urlLabel);
    labelSet.add(portLabel);
    labelSet.add(passwordLabel);
    labelSet.add(serialNumberLabel);

//    JPanel labelsGridPanel = new JPanel(new GridLayout(0,1));
//    labelsGridPanel.add(urlLabel);
//    labelsGridPanel.add(portLabel);
//    labelsGridPanel.add(passwordLabel);
//    labelsGridPanel.add(serialNumberLabel);
//    
//    JPanel linksGridPanel = new JPanel(new GridLayout(0,1));
//    linksGridPanel.add(urlLinkButton);
//    linksGridPanel.add(portLinkButton);
//    linksGridPanel.add(passwordLinkButton);
//    linksGridPanel.add(serialNumberLinkButton);
//
//    JPanel fieldsGridPanel = new JPanel(new GridLayout(0,1));
//    fieldsGridPanel.add(mIPField);
//    fieldsGridPanel.add(mPortNumberField);
//    fieldsGridPanel.add(mPasswordField);
//    fieldsGridPanel.add(mSerialNumberField);

    JPanel urlPanel = new JPanel(new BorderLayout());
    urlPanel.add(urlLabel,BorderLayout.WEST);
    urlPanel.add(mIPField,BorderLayout.CENTER);
    urlPanel.add(urlLinkButton,BorderLayout.EAST);
    
    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel,BorderLayout.WEST);
    portPanel.add(mPortNumberField,BorderLayout.CENTER);
    portPanel.add(portLinkButton,BorderLayout.EAST);

    JPanel serialNumberPanel = new JPanel(new BorderLayout());
    serialNumberPanel.add(serialNumberLabel,BorderLayout.WEST);
    serialNumberPanel.add(mSerialNumberField,BorderLayout.CENTER);
    serialNumberPanel.add(serialNumberLinkButton,BorderLayout.EAST);

    JPanel passwordPanel = new JPanel(new BorderLayout());
    passwordPanel.add(passwordLabel,BorderLayout.WEST);
    passwordPanel.add(mPasswordField,BorderLayout.CENTER);
    passwordPanel.add(passwordLinkButton,BorderLayout.EAST);

    JPanel topPanel = new JPanel (new GridLayout(2,2));
    topPanel.add(serialNumberPanel);
    topPanel.add(portPanel);
    topPanel.add(urlPanel);
    topPanel.add(passwordPanel);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);

    // --- Make all labels in the set the same dimension
    int maxWidth = 0, maxHeight=0;
    for (JLabel label : labelSet) {
      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
    }
    Dimension dim = new Dimension (maxWidth,maxHeight);
    for (JLabel label : labelSet) label.setPreferredSize(dim);
    
    return mainPanel;
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mIPField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPortNumberField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPasswordField,info,searchString,mode,this,name,collection);
    addToSearch(list,mSerialNumberField,info,searchString,mode,this,name,collection);
    return list;
  }

}
