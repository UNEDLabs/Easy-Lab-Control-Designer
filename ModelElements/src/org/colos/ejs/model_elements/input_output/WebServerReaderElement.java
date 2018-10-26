package org.colos.ejs.model_elements.input_output;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.library.utils.ModelElementsUtilities;

public class WebServerReaderElement extends AbstractModelElement {
  static private final ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/WebServerReader.png"); // This icon is included in this jar
  static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  
  static private final String BEGIN_FILE_HEADER = "<ServerAdress><![CDATA["; // Used to delimit my XML information
  static private final String END_FILE_HEADER = "]]></ServerAdress>";        // Used to delimit my XML information
  
  private JTextField mURLField = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "WebReader"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.WebServerReader"; }
  
  public String getInitializationCode(String _name) {
    String value = mURLField.getText().trim();
    if (value.length()<=0) return _name + " = new " + getConstructorName() + "(this,null); // Constructor with no filename";
    return _name + " = new " + getConstructorName()+"(this," + ModelElementsUtilities.getQuotedValue(value)+"); // Constructor with a filename";
  }
  
  public String getDisplayInfo() {
    String value = mURLField.getText().trim();
    if (value.length()<=0) return null;
    return "("+value+")";
  }

  public String savetoXML() {
    return BEGIN_FILE_HEADER+mURLField.getText()+END_FILE_HEADER;
  }

  public void readfromXML(String _inputXML) {
    int begin = _inputXML.indexOf(BEGIN_FILE_HEADER);
    if (begin<0) return; // A syntax error
    int end = _inputXML.indexOf(END_FILE_HEADER,begin);
    if (end<0) return; // Another syntax error
    String text = _inputXML.substring(begin+BEGIN_FILE_HEADER.length(),end);
    mURLField.setText(text);
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "sends commands to an HTTP web server and reads its response";
  }
  
  public void setFont(Font font) { mURLField.setFont(font); }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/WebServerReader.html"; }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    mURLField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(WebServerReaderElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(WebServerReaderElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(WebServerReaderElement.this); }
    });
    JLabel fieldLabel = new JLabel(" Server address:");

    JButton linkButton = new JButton(LINK_ICON);
    linkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mURLField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mURLField,"String", value);
        if (variable!=null) mURLField.setText("%"+variable+"%");
      }
    });

    JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
    buttonsPanel.add(linkButton);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(fieldLabel,BorderLayout.WEST);
    topPanel.add(mURLField,BorderLayout.CENTER);
    topPanel.add(buttonsPanel,BorderLayout.EAST);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    return addToSearch(new ArrayList<ModelElementSearch>(),mURLField,info,searchString,mode,this,name,collection);
  }
  
}
