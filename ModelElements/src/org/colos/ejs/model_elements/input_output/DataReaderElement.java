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

public class DataReaderElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/DataReader.png"); // This icon is included in this jar
  static private ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private ImageIcon FILE_ICON = AbstractModelElement.createImageIcon("data/icons/openSmall.gif"); // This icon is bundled with EJS
  
  static private final String BEGIN_FILE_HEADER = "<Filename><![CDATA["; // Used to delimit my XML information
  static private final String END_FILE_HEADER = "]]></Filename>";        // Used to delimit my XML information
  
  private JTextField mFileField = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "DataReader"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.DataReader"; }
  
  public String getInitializationCode(String _name) {
    String value = mFileField.getText().trim();
    if (value.length()<=0) return _name + " = new " + getConstructorName() + "(this,null); // Constructor with no filename";
    return _name + " = new " + getConstructorName()+"(this," + ModelElementsUtilities.getQuotedValue(value)+"); // Constructor with a filename";
  }
  
  public String getResourcesRequired() {
    String value = mFileField.getText().trim();
    if (ModelElementsUtilities.isLinkedToVariable(value)) return null; // Cannot know!
    return ModelElementsUtilities.getPureValue(value); // Add the constant file to read
  }
  
  public String getDisplayInfo() {
    String value = mFileField.getText().trim();
    if (value.length()<=0) return null;
    return "("+value+")";
  }

  public String savetoXML() {
    return BEGIN_FILE_HEADER+mFileField.getText()+END_FILE_HEADER;
  }

  public void readfromXML(String _inputXML) {
    int begin = _inputXML.indexOf(BEGIN_FILE_HEADER);
    if (begin<0) return; // A syntax error
    int end = _inputXML.indexOf(END_FILE_HEADER,begin);
    if (end<0) return; // Another syntax error
    String text = _inputXML.substring(begin+BEGIN_FILE_HEADER.length(),end);
    mFileField.setText(text);
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

//  protected void addListeners(JTextField field, final ModelElementsCollection collection) {
//    field.getDocument().addDocumentListener (new DocumentListener() {
//      public void changedUpdate(DocumentEvent e) { collection.reportChange(DataReaderElement.this); }
//      public void insertUpdate(DocumentEvent e)  { collection.reportChange(DataReaderElement.this); }
//      public void removeUpdate(DocumentEvent e)  { collection.reportChange(DataReaderElement.this); }
//    });
//  }

  public String getTooltip() {
    return "encapsulates an OSP DataFile class to read a text file with a double[][] array of data";
  }
  
  public void setFont(Font font) { mFileField.setFont(font); }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/DataReader.html"; }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    mFileField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(DataReaderElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(DataReaderElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(DataReaderElement.this); }
    });
    JLabel fieldLabel = new JLabel(" File to read:");

    JButton fileButton = new JButton(FILE_ICON);
    fileButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String filename = collection.chooseFilename(mFileField, getGenericName()+" file chooser", "Data files", "xml,txt");
        if (filename!=null) mFileField.setText("\""+filename+"\"");
      }
    });
    JButton linkButton = new JButton(LINK_ICON);
    linkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mFileField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mFileField,"String", value);
        if (variable!=null) mFileField.setText("%"+variable+"%");
      }
    });

    JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
    buttonsPanel.add(fileButton);
    buttonsPanel.add(linkButton);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(fieldLabel,BorderLayout.WEST);
    topPanel.add(mFileField,BorderLayout.CENTER);
    topPanel.add(buttonsPanel,BorderLayout.EAST);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    return addToSearch(new ArrayList<ModelElementSearch>(),mFileField,info,searchString,mode,this,name,collection);
  }

}
