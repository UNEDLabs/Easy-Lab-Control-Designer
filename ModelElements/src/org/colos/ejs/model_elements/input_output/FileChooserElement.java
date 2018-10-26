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

public class FileChooserElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/FileChooser.png"); // This icon is included in this jar
  static ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  
  static private final String BEGIN_DESCRIPTION_HEADER = "<Description><![CDATA[";
  static private final String END_DESCRIPTION_HEADER = "]]></Description>"; 
  static private final String BEGIN_EXTENSIONS_HEADER = "<Extensions><![CDATA[";
  static private final String END_EXTENSIONS_HEADER = "]]></Extensions>";
  
  private JTextField mDescriptionField = new JTextField();  // needs to be created to avoid null references
  private JTextField mExtensionsField = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "FileChooser"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.FileChooser"; }
  
  public String getInitializationCode(String _name) {
    String description = mDescriptionField.getText().trim();
    String extensions = mExtensionsField.getText().trim();
    if (description.length()<=0) description = "XML files"; 
    if (extensions.length()<=0) extensions = "xml"; 
    return _name + " = new " + getConstructorName()+"(this," + ModelElementsUtilities.getQuotedValue(description)+","+ ModelElementsUtilities.getQuotedValue(extensions)+");";
  }
  
  public String getDisplayInfo() {
    String description = mDescriptionField.getText().trim();
    String extensions = mExtensionsField.getText().trim();
    if (description.length()<=0) description = "XML files"; 
    if (extensions.length()<=0) extensions = "xml"; 
    return "("+extensions + " - " + description+")";
  }

  public String savetoXML() {
    return BEGIN_DESCRIPTION_HEADER+mDescriptionField.getText()+END_DESCRIPTION_HEADER + "\n" +
           BEGIN_EXTENSIONS_HEADER +mExtensionsField.getText() +END_EXTENSIONS_HEADER;
  }

  public void readfromXML(String _inputXML) {
    int begin = _inputXML.indexOf(BEGIN_DESCRIPTION_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_DESCRIPTION_HEADER,begin);
      if (end>=0) mDescriptionField.setText(_inputXML.substring(begin+BEGIN_DESCRIPTION_HEADER.length(),end));
    }
    begin = _inputXML.indexOf(BEGIN_EXTENSIONS_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_EXTENSIONS_HEADER,begin);
      if (end>=0) this.mExtensionsField.setText(_inputXML.substring(begin+BEGIN_EXTENSIONS_HEADER.length(),end));
    }
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "encapsulates calls to OSPRuntime methods that let you choose a file for reading or writing";
  }

  public void setFont(Font font) { 
    mDescriptionField.setFont(font); 
    mExtensionsField.setFont(font); 
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/FileChooser.html"; }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JLabel descriptionLabel = new JLabel(" Description:",SwingConstants.RIGHT);
    JLabel extensionsLabel = new JLabel(" Extensions:",SwingConstants.RIGHT);
    // Make both labels the same dimension
    int maxWidth  = descriptionLabel.getPreferredSize().width;
    int maxHeight = descriptionLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  extensionsLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, extensionsLabel.getPreferredSize().height);
    Dimension dim = new Dimension (maxWidth,maxHeight);
    descriptionLabel.setPreferredSize(dim);
    extensionsLabel.setPreferredSize(dim);

    mDescriptionField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(FileChooserElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(FileChooserElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(FileChooserElement.this); }
    });
    mExtensionsField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(FileChooserElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(FileChooserElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(FileChooserElement.this); }
    });

    JButton descriptionLinkButton = new JButton(LINK_ICON);
    descriptionLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mDescriptionField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mDescriptionField,"String", value);
        if (variable!=null) mDescriptionField.setText("%"+variable+"%");
      }
    });

    JButton extensionsLinkButton = new JButton(LINK_ICON);
    extensionsLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mExtensionsField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mExtensionsField,"String", value);
        if (variable!=null) mExtensionsField.setText("%"+variable+"%");
      }
    });

    JPanel descriptionPanel = new JPanel(new BorderLayout());
    descriptionPanel.add(descriptionLabel, BorderLayout.WEST);
    descriptionPanel.add(mDescriptionField, BorderLayout.CENTER);
    descriptionPanel.add(descriptionLinkButton, BorderLayout.EAST);

    JPanel extensionsPanel = new JPanel(new BorderLayout());
    extensionsPanel.add(extensionsLabel, BorderLayout.WEST);
    extensionsPanel.add(mExtensionsField, BorderLayout.CENTER);
    extensionsPanel.add(extensionsLinkButton, BorderLayout.EAST);

    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.add(descriptionPanel);
    topPanel.add(extensionsPanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mDescriptionField,info,searchString,mode,this,name,collection);
    addToSearch(list,mExtensionsField,info,searchString,mode,this,name,collection);
    return list;
  }
  
}
