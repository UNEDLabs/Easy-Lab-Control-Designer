package org.colos.ejs.model_elements.input_output;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class ResourceFinderElement extends DataReaderElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/ResourceFinder.png");

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "ResourceFinder"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.ResourceFinder"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

//  protected void addListeners(JTextField field, final ModelElementsCollection collection) {
//    field.getDocument().addDocumentListener (new DocumentListener() {
//      public void changedUpdate(DocumentEvent e) { collection.reportChange(ResourceFinderElement.this); }
//      public void insertUpdate(DocumentEvent e)  { collection.reportChange(ResourceFinderElement.this); }
//      public void removeUpdate(DocumentEvent e)  { collection.reportChange(ResourceFinderElement.this); }
//    });
//  }

  public String getTooltip() {
    return "provides reading access to files in the simulation JAR file, on the applet server, or on disk";
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/ResourceFinder.html"; }

}
