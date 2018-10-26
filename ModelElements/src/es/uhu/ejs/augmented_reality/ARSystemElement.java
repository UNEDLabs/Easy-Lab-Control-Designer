package es.uhu.ejs.augmented_reality;

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

public class ARSystemElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/augmented_reality/ARSystem.png"); // This icon is included in this jar

  static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_URL_HEADER = "<URL>";
  static private final String END_URL_HEADER = "</URL>";
  static private final String BEGIN_XRES_HEADER = "<XRes>";
  static private final String END_XRES_HEADER = "</XRes>";
  static private final String BEGIN_YRES_HEADER = "<YRes>";
  static private final String END_YRES_HEADER = "</YRes>";
  static private final String BEGIN_CONFIGURATION_HEADER = "<Configuration>";
  static private final String END_CONFIGURATION_HEADER = "</Configuration>";
  static private final String BEGIN_USER_HEADER = "<User><![CDATA[";
  static private final String END_USER_HEADER = "]]></User>";
  static private final String BEGIN_PASSWORD_HEADER = "<Password><![CDATA[";
  static private final String END_PASSWORD_HEADER = "]]></Password>";
  static private final String BEGIN_PANEL_HEADER = "<Panel3D>";
  static private final String END_PANEL_HEADER = "</Panel3D>";

  private JTextField mURLField = new JTextField();  // needs to be created to avoid null references
  private JTextField mXResField = new JTextField();  // needs to be created to avoid null references
  private JTextField mYResField = new JTextField();  // needs to be created to avoid null references
  private JTextField mConfigurationField = new JTextField();  // needs to be created to avoid null references
  private JTextField mUsernameField = new JTextField();  // needs to be created to avoid null references
  private JTextField mPasswordField = new JTextField();  // needs to be created to avoid null references
  private JTextField mPanelField = new JTextField();  // needs to be created to avoid null references

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "ARSystem"; }
  
  public String getConstructorName() { return "es.uhu.ejs.augmented_reality.ARSystem"; }

  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
//    buffer.append("if ("+_name+"!=null) "+_name+".dispose();\n");
    buffer.append(_name + " = new es.uhu.ejs.augmented_reality.ARSystemAdapter(this,");
    buffer.append(ModelElementsUtilities.getQuotedValue(mURLField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mXResField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mYResField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mConfigurationField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mUsernameField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mPasswordField.getText().trim())+",");
    buffer.append(ModelElementsUtilities.getQuotedValue(mPanelField.getText().trim())+");\n");
    return buffer.toString();
  }
  
  public String getPackageList() { return "es/uhu/augmented_reality/data/++;org/gstreamer/++;com/sun/jna/++"; }

  public String getDestructionCode(String _name) { return _name+".dispose();"; }

  public String getDisplayInfo() {
    String url = mURLField.getText().trim();
    if (url.length()>0) return "("+url+")";
    return null;
  }
  
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_URL_HEADER+mURLField.getText()+END_URL_HEADER + "\n");
    buffer.append(BEGIN_XRES_HEADER+mXResField.getText()+END_XRES_HEADER + "\n");
    buffer.append(BEGIN_YRES_HEADER+mYResField.getText()+END_YRES_HEADER + "\n");
    buffer.append(BEGIN_CONFIGURATION_HEADER+mConfigurationField.getText()+END_CONFIGURATION_HEADER + "\n");
    buffer.append(BEGIN_USER_HEADER+mUsernameField.getText()+END_USER_HEADER + "\n");
    buffer.append(BEGIN_PASSWORD_HEADER+mPasswordField.getText()+END_PASSWORD_HEADER + "\n");
    buffer.append(BEGIN_PANEL_HEADER+mPanelField.getText()+END_PANEL_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    mURLField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_URL_HEADER,END_URL_HEADER,false));
    mXResField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_XRES_HEADER,END_XRES_HEADER,false));
    mYResField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_YRES_HEADER,END_YRES_HEADER,false));
    mConfigurationField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_CONFIGURATION_HEADER,END_CONFIGURATION_HEADER,false));
    mUsernameField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_USER_HEADER,END_USER_HEADER,false));
    mPasswordField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PASSWORD_HEADER,END_PASSWORD_HEADER,false));
    mPanelField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PANEL_HEADER,END_PANEL_HEADER,false));
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------
  
  public String getTooltip() {
    return "provides 3D tracking of fiducial markers for augmented reality";
  }
  
  protected String getHtmlPage() { return "es/uhu/ejs/augmented_reality/ARSystem.html"; }

  public void setFont(Font font) { 
    mURLField.setFont(font);
    mXResField.setFont(font);
    mYResField.setFont(font); 
    mConfigurationField.setFont(font);
    mUsernameField.setFont(font);
    mPasswordField.setFont(font);
    mPanelField.setFont(font);
  }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(ARSystemElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(ARSystemElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(ARSystemElement.this); }
    };
    
    Set<JLabel> labelSet = new HashSet<JLabel>(); // list of labels to adjust size

    mURLField.getDocument().addDocumentListener (documentListener);
    OneLine urlLine = new OneLine (collection,labelSet, mURLField, "URL", "String");
        
    mXResField.getDocument().addDocumentListener (documentListener);
    OneLine xresLine = new OneLine (collection,labelSet, mXResField, "XRes", "int");

    mYResField.getDocument().addDocumentListener (documentListener);
    OneLine yresLine = new OneLine (collection,labelSet, mYResField, "YRes", "int");

    mConfigurationField.getDocument().addDocumentListener (documentListener);
    OneLine configurationLine = new OneLine (collection,labelSet, mConfigurationField, "Configuration", "String");

    mUsernameField.getDocument().addDocumentListener (documentListener);
    OneLine usernameLine = new OneLine (collection,labelSet, mUsernameField, "Username", "String");

    mPasswordField.getDocument().addDocumentListener (documentListener);
    OneLine passwordLine = new OneLine (collection,labelSet, mPasswordField, "Password", "String");

    mPanelField.getDocument().addDocumentListener (documentListener);
    OneLine panelLine = new OneLine (collection,labelSet, mPanelField, "Panel3D", "String");

    JPanel resPanel = new JPanel (new GridLayout(1,0));
    resPanel.add(xresLine);
    resPanel.add(yresLine);

    JPanel userPanel = new JPanel (new GridLayout(1,0));
    userPanel.add(usernameLine);
    userPanel.add(passwordLine);

    JPanel topPanel = new JPanel (new GridLayout(0,1));
    topPanel.add(urlLine);
    topPanel.add(resPanel);
    topPanel.add(configurationLine);
    topPanel.add(userPanel);
    topPanel.add(panelLine);

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
    addToSearch(list,mURLField,info,searchString,mode,this,name,collection);
    addToSearch(list,mXResField,info,searchString,mode,this,name,collection);
    addToSearch(list,mYResField,info,searchString,mode,this,name,collection);
    addToSearch(list,mConfigurationField,info,searchString,mode,this,name,collection);
    addToSearch(list,mUsernameField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPasswordField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPanelField,info,searchString,mode,this,name,collection);
    return list;
  }
  
  static private class OneLine extends JPanel {
    private static final long serialVersionUID = 1L;

    JLabel label;
    JButton linkButton;

    OneLine (final ModelElementsCollection collection, Set<JLabel> labelSet, 
            final JTextField field, String labelText, final String type) {
      label = new JLabel(RES.getString("ARSystem."+labelText),SwingConstants.RIGHT);
      label.setForeground(COLOR);
      label.setBorder(LABEL_BORDER);
      labelSet.add(label);

      linkButton = new JButton(LINK_ICON);
      linkButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          String value = field.getText().trim();
          if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
          else value = ModelElementsUtilities.getPureValue(value);
          String variable = collection.chooseVariable(field,type, value);
          if (variable!=null) field.setText("%"+variable+"%");
        }
      });
      
      setLayout(new BorderLayout());
      add(label,BorderLayout.WEST);
      add(field,BorderLayout.CENTER);
      add(linkButton,BorderLayout.EAST);
    }
    
  }
  
}
