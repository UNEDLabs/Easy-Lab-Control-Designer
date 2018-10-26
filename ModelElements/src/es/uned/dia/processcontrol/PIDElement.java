package es.uned.dia.processcontrol;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.edition.ModelEditor;
import org.opensourcephysics.display.OSPRuntime;

import es.uned.dia.ejss.softwarelinks.utils.RIPCodeBuilder;
import es.uned.dia.ejss.softwarelinks.utils.RIPConfigurationModel;
import es.uned.dia.softwarelinks.nodejs.RIPClient;
import es.uned.dia.softwarelinks.nodejs.RIPServerInfo;
import es.uned.dia.softwarelinks.nodejs.RIPClient.RIPException;
import es.uned.dia.softwarelinks.nodejs.RIPInfo;
import es.uned.dia.softwarelinks.nodejs.RIPExperienceInfo;
import es.uned.dia.softwarelinks.nodejs.RIPMethod;
import es.uned.dia.softwarelinks.transport.HttpTransport;

/**
 * Class to interact with a JIL Server
 * @author Adapted to Javascript from the original code of Jesús Chacón <jcsombria@gmail.com> by Jacobo Sáenz 
 */
public class PIDElement extends AbstractModelElement {
  private static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uned/dia/ejss/softwarelinks/resources/rip.png"); // This icon is included in this jar
   
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "PID"; }
  
  public String getConstructorName() { return "PIDController"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    StringBuffer buffer = new StringBuffer();
    buffer.append("var RIP = {};");  
    return buffer.toString(); 
  } 

  public String getSourceCode(String name) { // Code that goes into the body of the model
    String code = String.format("%s = new %s();", name, getConstructorName());
    return code;
  }  
  
  public String getImportStatements() { // Required for Lint
    return "ProcessControl/processcontrol.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "Process Control Elements";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "es/uned/dia/ejss/softwarelinks/resources/rip.html"; 
  }
  
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    return null;
  }
  
  
  public String savetoXML() {
    String dump = "";
//    // Retrieve values from the dialog
//    try {
//    } catch(NullPointerException e) {
//      System.out.println("[Error] Cannot serialize configuration of RIP Element");
//      dump = "";
//    }
    return dump;
  }

  /** 
   * Restores the states from an XML String
   */
  public void readfromXML(String inputXML) {
//    try {
//    } catch(Exception e) {
//      System.err.println("[ERROR] Element configuration not loaded.");
//    }
  } 
}