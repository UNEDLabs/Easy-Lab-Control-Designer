package es.uhu.ejs.slavemodbussarlab;

//import es.uhu.ejs.sarlab.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version December 2012
 */

public class SlaveModbusSarlabElement extends AbstractModelElement{
  static private final ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/slavemodbussarlab/SlaveModbusSarlabElement.png"); // This icon is included in this jar
  static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_SERIAL_NUMBER = "<SerialNumber>"; // Used to delimit my XML information
  static private final String END_SERIAL_NUMBER = "</SerialNumber>";  // Used to delimit my XML information
  static private final String BEGIN_URL_HEADER = "<IPaddress>";
  static private final String END_URL_HEADER = "</IPaddress>";
  static private final String BEGIN_PORT_HEADER = "<PortNumber>";
  static private final String END_PORT_HEADER = "</PortNumber>";


  private JTextField mSerialNumberField = new JTextField();  // needs to be created to avoid null references
  private JTextField mIPField = new JTextField();  // needs to be created to avoid null references
  private JTextField mPortNumberField = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------

    
   @Override 
   public ImageIcon getImageIcon() { return ELEMENT_ICON; }  
   @Override 
   public String getGenericName() { return "slaveModbus"; }

  @Override 
  public String getConstructorName() { return "modbussarlab.SlaveModbusInterfaceEJS"; }  
  @Override
  public String getInitializationCode(String _name) {   
    /*StringBuffer buffer = new StringBuffer();    
    buffer.append(_name + " = new " + getConstructorName() + "(");    
    buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mSerialNumberField.getText().trim())+");\n");
    return buffer.toString();  */   
    StringBuffer buffer = new StringBuffer();    
    buffer.append(_name + " = " + getConstructorName() + ".createConnection(");    
    buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mSerialNumberField.getText().trim())+");\n");
    return buffer.toString();
  } 
   
  
  
  @Override
  public String getDestructionCode(String _name) { return null; } // This element requires no destruction code

  @Override
  public String getResourcesRequired() { return null; }

  @Override
  public String getPackageList() { return  "modbussarlab/++"; } // No non-class file from my jar is required to package this element
 
  public String getDisplayInfo() {    
    String ipAddress = mIPField.getText().trim();
    String portNumber =mPortNumberField.getText().trim();
    String serialNumber = mSerialNumberField.getText().trim();
    return  "(" +ipAddress+ ":"+portNumber+"-"+serialNumber+")" ;
  } // Nothing to add

    @Override
  public String savetoXML() { 
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_URL_HEADER+mIPField.getText()+END_URL_HEADER + "\n");
    buffer.append(BEGIN_PORT_HEADER+mPortNumberField.getText()+END_PORT_HEADER + "\n");
    buffer.append(BEGIN_SERIAL_NUMBER+mSerialNumberField.getText()+END_SERIAL_NUMBER + "\n");    
    return buffer.toString(); } // Nothing to save

    @Override
  public void readfromXML(String _inputXML) { 
    mIPField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_URL_HEADER,END_URL_HEADER,false));
    mPortNumberField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false));
    mSerialNumberField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_SERIAL_NUMBER,END_SERIAL_NUMBER,false));
  } // Nothing to read
    
  // -------------------------------
  // Help and edition
  // -------------------------------    

 @Override
  protected String getHtmlPage() { return "es/uhu/ejs/slavemodbussarlab/SlaveModbusSarlabElement.html"; }
 
    @Override
  public String getTooltip() {
    return "Provides connectivity between the student and a remote laboratory experience";
  }
  
    @Override
    public void setFont(Font font) {
    mIPField.setFont(font);
    mPortNumberField.setFont(font);
    mSerialNumberField.setFont(font);   
  }
      
    @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(SlaveModbusSarlabElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(SlaveModbusSarlabElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(SlaveModbusSarlabElement.this); }
    }; 
    
    JLabel urlLabel = new JLabel("Slave modbus IP  : ",SwingConstants.RIGHT);
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

    
    JLabel portLabel = new JLabel("Slave modbus port : ",SwingConstants.RIGHT);
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

    JLabel serialNumberLabel = new JLabel("Slave identifier : ",SwingConstants.RIGHT);
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
    labelSet.add(serialNumberLabel);


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

    
    JPanel line1Panel = new JPanel(new GridLayout(1,2));
    line1Panel.add(urlPanel);
    line1Panel.add(portPanel);
    
    JPanel topPanel = new JPanel (new GridLayout(2,1));
    topPanel.add(line1Panel);
    topPanel.add(serialNumberPanel);

    
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

   @Override
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mIPField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPortNumberField,info,searchString,mode,this,name,collection);
    addToSearch(list,mSerialNumberField,info,searchString,mode,this,name,collection);
    return list;
  }
   
}
