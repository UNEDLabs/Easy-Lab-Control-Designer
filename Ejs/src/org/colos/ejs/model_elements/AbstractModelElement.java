package org.colos.ejs.model_elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A base abstract class that can simplify the creation of ModelElements
 * @author Paco
 *
 */
public abstract class AbstractModelElement implements ModelElement {
  static protected final ResourceUtil RES = new ResourceUtil ("Resources");

  static public ImageIcon createImageIcon(String iconFilename) {
    return new ImageIcon(ResourceLoader.getImage(iconFilename));
  }

  private JDialog mHelpDialog; // The dialog for help
  private JDialog mEditorDialog; // The dialog for edition

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  abstract public ImageIcon getImageIcon();
  
  abstract public String getGenericName();
  
  abstract public String getConstructorName();

  public String getInitializationCode(String _name) {
    return _name + " = new " + getConstructorName() + "();";
  }
  
  public String getDestructionCode(String _name) { return null; } // This element requires no destruction code

  public String getSourceCode(String name) { return null; } // Code that goes into the body of the model 

  public String getResourcesRequired() { return null; } // This element requires no resources 

  public String getImportStatements() { return null; }
  
  public String getReadFromView(String _name) { return null; } // Code to read from view after it has been updated

  public String getPackageList() { return null; } // No non-class file from my jar is required to package this element

  public String getDisplayInfo() { return null; } // Nothing to add

  public String savetoXML() { return null; } // Nothing to save

  public void readfromXML(String _inputXML) { } // Nothing to read

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "encapsulates an element of class "+getConstructorName(); }
  
  public void clear() {
    if (mEditorDialog!=null) {
      mEditorDialog.setVisible(false);
      mEditorDialog.dispose();
      mEditorDialog = null;
    }
    if (mHelpDialog!=null) {
      mHelpDialog.setVisible(false);
      mHelpDialog.dispose();
      mHelpDialog = null;
    }
  }
  
  public void setFont(Font font) { } // No special font requirements
  
  abstract protected String getHtmlPage();

  public void showHelp(Component _parentComponent) {
    if (mHelpDialog==null) { // create the dialog
      mHelpDialog = new JDialog((JFrame) null,RES.getString("ModelElementHelpFor")+" "+ getGenericName());
      mHelpDialog.getContentPane().setLayout(new BorderLayout());
      mHelpDialog.getContentPane().add(createHelpComponent(),BorderLayout.CENTER);
      mHelpDialog.setModal(false);
      mHelpDialog.pack();
    }
    java.awt.Rectangle bounds = EjsControl.getDefaultScreenBounds();
    if (_parentComponent==null) mHelpDialog.setLocation(bounds.x + (bounds.width - mHelpDialog.getWidth())/2, bounds.y + (bounds.height - mHelpDialog.getHeight())/2);
    else mHelpDialog.setLocationRelativeTo(_parentComponent);
    mHelpDialog.setVisible(true);
  }

  protected Component createEditor(String name, Component parentComponent, ModelElementsCollection collection) {
    return null; // A null editor will display the help
  }

  public void showEditor(String _name, Component _parentComponent, ModelElementsCollection _collection) {
    if (mEditorDialog==null) {
      Component editor = createEditor(_name,_parentComponent, _collection);
      if (editor==null) {
        showHelp(_parentComponent);
        return;
      }
      mEditorDialog = new JDialog((JFrame) null,RES.getString("ModelElementEditorFor")+ " "+ _name+ " ("+ getGenericName() + ")");
      mEditorDialog.getContentPane().setLayout(new BorderLayout());
      mEditorDialog.getContentPane().add(editor,BorderLayout.CENTER);
      mEditorDialog.setModal(false);
      mEditorDialog.pack();
    }
    java.awt.Rectangle bounds = EjsControl.getDefaultScreenBounds();
    if (_parentComponent==null) mEditorDialog.setLocation(bounds.x + (bounds.width - mEditorDialog.getWidth())/2, bounds.y + (bounds.height - mEditorDialog.getHeight())/2);
    else mEditorDialog.setLocationRelativeTo(_parentComponent);
    mEditorDialog.setVisible(true);
  }
  
  public void refreshEditor(String _name) {
    if (mEditorDialog!=null) mEditorDialog.setTitle(RES.getString("ModelElementEditorFor")+ " "+ _name+ " ("+ getGenericName() + ")");
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, 
      String name, ModelElementsCollection collection) {
    return null;
  }
  
  // -------------------------------
  // Utilities
  // -------------------------------

  /**
   * Standardized way of saving XML information
   * @param keyword
   * @param textComponent
   * @return
   */
  static protected String writeField(String keyword, JTextField textComponent) {
    String startHeader = "<"+keyword+"><![CDATA["; // Used to delimit my XML information
    String endHeader   = "]]></"+keyword+">";     // Used to delimit my XML information
    return startHeader + textComponent.getText() + endHeader;
  }

  /**
   * Standardized way of reading XML information
   * @param xmlInput
   * @param keyword
   * @param textComponent
   */
  static protected void readInput(String xmlInput, String keyword, JTextField textComponent) {
    String startHeader = "<"+keyword+"><![CDATA["; // Used to delimit my XML information
    String endHeader   = "]]></"+keyword+">";     // Used to delimit my XML information
    int begin = xmlInput.indexOf(startHeader);
    if (begin>=0) {
      int end = xmlInput.indexOf(endHeader,begin);
      if (end>=0) textComponent.setText(xmlInput.substring(begin+startHeader.length(),end));
    }
  }

  
  /**
   * Searches for a string in a given component and adds results to the list
   * @return
   */
  static public java.util.List<ModelElementSearch> addToSearch (java.util.List<ModelElementSearch> resultList, JTextComponent textComponent, 
      String info, String searchString, int mode, 
      ModelElement modelElement, String elementName, ModelElementsCollection collection) {
    boolean toLower = (mode & SearchResult.CASE_INSENSITIVE) !=0;
    int lineCounter=1,caretPosition=0;
    StringTokenizer t = new StringTokenizer(textComponent.getText(), "\n",true);
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(searchString);
      else index = line.indexOf(searchString);
      if (index>=0) resultList.add(new ModelElementSearch(collection, modelElement, elementName, info, line.trim(), textComponent, lineCounter, caretPosition+index));
      caretPosition += line.length();
      lineCounter++;
    }
    return resultList;
  }

  /**
   * Creates an HTML viewer with information about the class
   */
  protected Component createHelpComponent() {
    JEditorPane htmlArea = new JEditorPane ();
    htmlArea.setContentType ("text/html");
    htmlArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    htmlArea.setEditable(false);
    htmlArea.addHyperlinkListener(new HyperlinkListener() { // Make hyperlinks work
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
          OSPDesktop.displayURL(e.getURL().toString());
      }
    });
    JScrollPane helpComponent = new JScrollPane(htmlArea);
    helpComponent.setPreferredSize(new Dimension(600,500));
    
    try { // read the help for this element
      java.net.URL htmlURL = ResourceLoader.getResource(getHtmlPage()).getURL();
      htmlArea.setPage(htmlURL);
    } catch(Exception exc) { exc.printStackTrace(); }
    return helpComponent;
  }

}
