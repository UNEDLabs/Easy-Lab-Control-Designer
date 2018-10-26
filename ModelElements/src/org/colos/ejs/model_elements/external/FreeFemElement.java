package org.colos.ejs.model_elements.external;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;

public class FreeFemElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/external/FreeFem.png");
  static private ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String sPragma = "$(";
  
  static private final String BEGIN_SCRIPT_HEADER = "<Script>";
  static private final String END_SCRIPT_HEADER = "</Script>";
  static private final String BEGIN_URL_HEADER = "<url>";
  static private final String END_URL_HEADER = "</url>";
  static private final String BEGIN_USERNAME_HEADER = "<username>";
  static private final String END_USERNAME_HEADER = "</username>";
  static private final String BEGIN_PASSWORD_HEADER = "<password>";
  static private final String END_PASSWORD_HEADER = "</password>";
 
  
  private ModelElementEditor mScriptEditor;
  private JTextField mURLField = new JTextField();  // needs to be created to avoid null references
  private JTextField mUserField = new JTextField();  // needs to be created to avoid null references
  private JPasswordField mPasswordField = new JPasswordField();  // needs to be created to avoid null references
 
  {
    mScriptEditor = new ModelElementEditor (this,null, true, "Code", "Comment");
//    mScriptEditor.addPragma("CRITICAL", "region().critical (new edu.rit.pj.ParallelSection() { public void run() {\n","}});\n");
    mScriptEditor.readPlainCode("// "+ RES.getString("FreeFem.Script.ToolTip"));
  }

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "FreeFem"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.external.FreeFemAdapter"; }
  
  public String getImportStatements() { return "org.colos.freefem.*"; }

  
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(_name + " = new " + getConstructorName() + "(this) {\n");
    buffer.append("  public String getScript() {\n");
    buffer.append("    java.lang.StringBuffer _buffer = new java.lang.StringBuffer();\n");
    buffer.append(processCode(mScriptEditor.getCode(), "    _buffer.append(",");\n"));
    buffer.append("    return _buffer.toString();\n");
    buffer.append("  }\n\n");
    buffer.append("}; // end of constructor\n");

    String url = mURLField.getText().trim();
    if (url.length()>0) buffer.append(_name + ".setServer("+ModelElementsUtilities.getQuotedValue(url)+",12345);\n");
    
    String username = mUserField.getText().trim();
    String password = mPasswordField.getText().trim();
    if (username.length()>0) buffer.append(_name + ".setUserAndPassword("+ModelElementsUtilities.getQuotedValue(username)+","+ModelElementsUtilities.getQuotedValue(password)+");\n");
    return buffer.toString();
  }
  
  static private StringBuffer processCode (String codeStr, String tabs, String suffix) {
    StringBuffer code = new StringBuffer ();
    
    
    StringTokenizer tkn = new StringTokenizer(codeStr, "\n");
    while (tkn.hasMoreTokens()) {
      code.append(tabs);
      String line = tkn.nextToken();
          
      int index = line.indexOf(sPragma);
      while (index>=0) {
//        System.out.println ("FreeFem element EJS_VALUE found in <"+line+">");
        int begin = line.indexOf('(', index);
        int end   = line.indexOf(')', index);
        if (begin>end) {
          JOptionPane.showMessageDialog(null, "syntax error in line: "+line,"FreeFem Model Element Error",JOptionPane.ERROR_MESSAGE);
          break;
        }
        code.append("\""+line.substring(0, index)+"\"");
        code.append("+"+line.substring(begin+1,end)+"+");
        line = line.substring(end+1);
//        System.out.println ("FreeFem element processing now <"+line+">");
        index = line.indexOf(sPragma);
      }
      //processing use of "" in the script, if they are not after a comment symbol //
     /* System.out.println(line);
      int begin;
      if(line.indexOf("//")<0) {
        System.out.println("no hay comentarios\n\n");
        begin = line.indexOf('"');        
      }
      else{
        System.out.println("hay comentarios\n\n");
        if((line.indexOf("//")<line.indexOf('"'))&&line.indexOf('"')>0){
          System.out.println("hay comillas despu�s del comentario\n\n");
          begin =-1;
        }
        else {
          System.out.println("las comillas est�n antes as� que hay que tratarlas o no hay comillas\n\n");
          begin = line.indexOf('"');
        }
      }*/
      int begin = line.indexOf('"');
      while(begin>=0){
        int end = line.indexOf('"',begin+1);
        code.append("\""+line.substring(0, begin)+"\\\"");
        code.append(line.substring(begin+1,end)+"\\\"\"+");
        line = line.substring(end+1);
        begin = line.indexOf('\"');        
      }
      
      code.append("\""+line+"\\n\"");
      code.append(suffix);
     // System.out.println(code);
    }
    return code;
  }
  
  public String getDisplayInfo() {
    String value = mURLField.getText().trim();
    if (value.length()<=0) return null;
    return "("+value+")";
  }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_URL_HEADER+mURLField.getText()+END_URL_HEADER + "\n");
    buffer.append(BEGIN_USERNAME_HEADER+mUserField.getText()+END_USERNAME_HEADER + "\n");
    buffer.append(BEGIN_PASSWORD_HEADER+mPasswordField.getText()+END_PASSWORD_HEADER + "\n");
    buffer.append(BEGIN_SCRIPT_HEADER);
    buffer.append(mScriptEditor.saveStringBuffer());
    buffer.append(END_SCRIPT_HEADER);
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    mURLField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_URL_HEADER,END_URL_HEADER,false));
    mUserField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_USERNAME_HEADER,END_USERNAME_HEADER,false));
    mPasswordField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_PASSWORD_HEADER,END_PASSWORD_HEADER,false));
    mScriptEditor.readXmlString(OsejsCommon.getPiece(_inputXML,BEGIN_SCRIPT_HEADER,END_SCRIPT_HEADER,false));
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "defines a FreeFem++ script to be run externally"; }
  
  public void setFont(Font font) {
    mURLField.setFont(font);
    mUserField.setFont(font);
    mPasswordField.setFont(font);
    mScriptEditor.setFont(font);
   }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/external/FreeFem.html"; }

  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(FreeFemElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(FreeFemElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(FreeFemElement.this); }
    };

    JLabel urlLabel = new JLabel(RES.getString("FreeFem.ServerURL"),SwingConstants.RIGHT);
    urlLabel.setForeground(COLOR);
    urlLabel.setBorder(LABEL_BORDER);
    
    mURLField.getDocument().addDocumentListener (documentListener);

    JButton urlLinkButton = new JButton(LINK_ICON);
    urlLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mURLField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mURLField,"String", value);
        if (variable!=null) mURLField.setText("%"+variable+"%");
      }
    });

    
    JLabel usernameLabel = new JLabel(RES.getString("FreeFem.Username"),SwingConstants.RIGHT);
    usernameLabel.setForeground(COLOR);
    usernameLabel.setBorder(LABEL_BORDER);
    
    mUserField.getDocument().addDocumentListener (documentListener);

    JButton usernameLinkButton = new JButton(LINK_ICON);
    usernameLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mUserField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mUserField,"String", value);
        if (variable!=null) mUserField.setText("%"+variable+"%");
      }
    });

    JLabel passwordLabel = new JLabel(RES.getString("FreeFem.Password"),SwingConstants.RIGHT);
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
    
//    JPanel urlPanel = new JPanel(new BorderLayout());
//    urlPanel.add(urlLabel,BorderLayout.WEST);
//    urlPanel.add(mURLField,BorderLayout.CENTER);
//    urlPanel.add(urlLinkButton,BorderLayout.EAST);
//    
//    JPanel usernamePanel = new JPanel(new BorderLayout());
//    usernamePanel.add(usernameLabel,BorderLayout.WEST);
//    usernamePanel.add(mUserField,BorderLayout.CENTER);
//    usernamePanel.add(usernameLinkButton,BorderLayout.EAST);
//    
//    JPanel passwordPanel = new JPanel(new BorderLayout());
//    passwordPanel.add(passwordLabel,BorderLayout.WEST);
//    passwordPanel.add(mPasswordField,BorderLayout.CENTER);
//    passwordPanel.add(passwordLinkButton,BorderLayout.EAST);
    
    JPanel labelsGridPanel = new JPanel(new GridLayout(0,1));
    labelsGridPanel.add(urlLabel);
    labelsGridPanel.add(usernameLabel);
    labelsGridPanel.add(passwordLabel);
       
    JPanel linksGridPanel = new JPanel(new GridLayout(0,1));
    linksGridPanel.add(urlLinkButton);
    linksGridPanel.add(usernameLinkButton);
    linksGridPanel.add(passwordLinkButton);
   
    JPanel fieldsGridPanel = new JPanel(new GridLayout(0,1));
    fieldsGridPanel.add(mURLField);
    fieldsGridPanel.add(mUserField);
    fieldsGridPanel.add(mPasswordField);
      
    JPanel topPanel = new JPanel (new BorderLayout());
    topPanel.add(labelsGridPanel,BorderLayout.WEST);
    topPanel.add(fieldsGridPanel,BorderLayout.CENTER);
    topPanel.add(linksGridPanel,BorderLayout.EAST);
    
    JComponent rrComp = mScriptEditor.getComponent(collection);
    rrComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(rrComp,BorderLayout.CENTER);

    return mainPanel;

  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mURLField,info,searchString,mode,this,name,collection);
    addToSearch(list,mUserField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPasswordField,info,searchString,mode,this,name,collection);
    list.addAll(mScriptEditor.search(info, searchString, mode, name, collection));
    return list;
  }

  
}
