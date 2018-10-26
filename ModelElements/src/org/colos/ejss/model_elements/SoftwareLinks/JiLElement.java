package org.colos.ejss.model_elements.SoftwareLinks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;

import org.opensourcephysics.display.OSPRuntime;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
/**
 * Class to interact with a JIL Server
 * @author Adapted to Javascript from the original code of Jes�s Chac�n <jcsombria@gmail.com> by Jacobo S�enz 
 */
public class JiLElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/SoftwareLinks/JiL.png"); // This icon is included in this jar
  //private JTextField serverText = new JTextField();  // needs to be created to avoid null references
  //private JTextField portText = new JTextField(); // needs to be created to avoid null references
  //private JTextField viFileText = new JTextField();  // needs to be created to avoid null references
  
  private static final String XML_NODE_LABEL_JIL = "jil";
  private static final String XML_NODE_LABEL_SERVER = "server";
  private static final String XML_NODE_LABEL_PORT = "port";
  private static final String XML_NODE_LABEL_PATH = "path";
  private static final String XML_NODE_LABEL_LINKS = "links";
  private static final String XML_NODE_LABEL_ROW = "row";
  private static final String XML_NODE_LABEL_LABVIEW = "labview";
  private static final String XML_NODE_LABEL_MODEL = "model";
  private static final String XML_NODE_LABEL_TYPE = "type";
  protected static final Object CONNECTION_OK = "Connected Ok";
  protected static final Object INVALID_PATH = "Invalid Path or nothing there";
  protected static final Object SERVER_KO = "Server Its not working";
  
  //GUI elements
   private DefaultTableModel linksTableModel;
   private DefaultTableModel indicatorsTableModel;
   private DefaultTableModel controlsTableModel;
   private JTextField serverText = new JTextField("localhost", 20);
   private JTextField portText = new JTextField("2055", 6);
   private JTextField viFileText = new JTextField("JiLTest.vi", 32);  
   
   private JPopupMenu popupMenuEJS;  
   
   private JTable controlsTable;
   private JTable indicatorsTable;
   private JTable linksTable;
   private JPopupMenu popupMenuInd;
   private JPopupMenu popupMenuCon;
  
   // used to test the connection with the server
   Labview jilTest = null;
   private Component parent;
  
   public JiLElement() { 
     // LabVIEW Controls Table
     controlsTableModel = new DefaultTableModel();
     Vector<String> controlsColumns = new Vector<String>();
     controlsColumns.add("Name");
     controlsColumns.add("Type");
     Vector<Vector<String>> controlsVector = new Vector<Vector<String>>();
     controlsTableModel.setDataVector(controlsVector, controlsColumns);
     // LabVIEW Indicators Table
     indicatorsTableModel = new DefaultTableModel();
     Vector<String> indicatorsColumns = new Vector<String>();
     indicatorsColumns.add("Name");
     indicatorsColumns.add("Type");
     Vector<Vector<String>> indicatorsVector = new Vector<Vector<String>>();
     indicatorsTableModel.setDataVector(indicatorsVector, indicatorsColumns);
     // Links Table
       linksTableModel = new DefaultTableModel();
       Vector<String> namesVector = new Vector<String>();
       namesVector.add("LabVIEW Variable");
       namesVector.add("Model Variable");
       namesVector.add("Type");
       Vector<Vector<String>> linksVector = new Vector<Vector<String>>();      
       linksTableModel.setDataVector(linksVector, namesVector);
   }
   
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "LabVIEW_v2.0"; }
  
  public String getConstructorName() { return "JiL"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    StringBuffer buffer = new StringBuffer();
    buffer.append("var JIL = {};");  
    return buffer.toString(); 
  } 

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    StringBuffer buffer = new StringBuffer();
    buffer.append("var " + name + " = new JIL.Jil();\n");
    if (serverText != null && portText != null && serverText.getText() != "" && portText.getText() != ""){
      buffer.append(name +".host = " + "\"" +"http://" + serverText.getText().trim()+":"+ portText.getText().trim()  +"\"" + ";\n");
    }
    else buffer.append(name +".host = NONE");
    if (viFileText != null && viFileText.getText() != "")   buffer.append(name +".filePath = " + "\"" + viFileText.getText().trim() + "\"" + ";\n");
    else buffer.append(name +".filePath = NONE");
    //JSV Changes -> // adding a new function "step" to the JIL object called *name*, 
    //using the links established in the element editor  
    if (linksTableModel != null){
      buffer.append(name + ".step = function(){\n");
      buffer.append("\t" + "// === Get and Set function calls ===\n");
      String viVar = "", modelVar = "", viVarType = "";
      int fIdxModel = 0, lIdxModel = 0;   //First and last indexes of model vars
      int idxVi = 0;   //First index of vi vars
      for(Vector v : (Iterable<Vector>)(linksTableModel.getDataVector())) {
        viVar = v.elementAt(0).toString();              //Picks up the indicator/control name(type) of the vi
        idxVi = viVar.indexOf("(");
        viVarType = viVar.substring(idxVi+1,viVar.indexOf(")")); //Type of viVar
        modelVar = v.elementAt(1).toString();           //Picks up the Type:Name of the model variable
        fIdxModel = modelVar.indexOf(":")+1;           //To isolate the modelVar name
        lIdxModel = modelVar.lastIndexOf(":");          //To isolate the modelVar name
        if (viVarType.equals("ind")){        //If and indicator -> we need to make a modelVar = name.get(viVar)
          buffer.append("\t" + modelVar.substring(fIdxModel,lIdxModel) + "="); //Left side of expression
          buffer.append("\t" + name + ".getVariable(\"" + viVar.substring(0,idxVi) +"\");");
          buffer.append("\n");
        }else{                              //If and control -> we need to make a name.set(viVar,modelVar)
          buffer.append("\t" +name + ".setVariable(\""+ viVar.substring(0,idxVi) + "\","); //First Parameter
          buffer.append("\t" +modelVar.substring(fIdxModel,lIdxModel) + ");"); 
          buffer.append("\n");
        }
      }
      buffer.append("\t" +"return;\n");
      buffer.append("}\n");
    }
    
    return buffer.toString();
  }  

  public String getImportStatements() { // Required for Lint
    return "SoftwareLinks/JiL.js";
    //return "Input_output/JiL.js;Input_output/mimic.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access to Labview through internet using XML-RPC";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/SoftwareLinks/JiL.html"; 
  }
  
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    //Using the GUI developed by Jes�s Chac�n Sombr�a: 
    return createJilEditor(name,parentComponent,collection);
    //All the code commented below is the simple GUI for the JiLElement, if you want to recover it, just comment the
    //previous line and uncomment the next lines. 
    
    /*JLabel serverLabel = new JLabel(" Server :",SwingConstants.RIGHT);
    JLabel portLabel = new JLabel(" Port :",SwingConstants.RIGHT);
    JLabel fileLabel = new JLabel(" File Path:",SwingConstants.RIGHT);
    // Make both labels the same dimension
    int maxWidth  = serverLabel.getPreferredSize().width;
    int maxHeight = serverLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  fileLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, fileLabel.getPreferredSize().height);
    Dimension dim = new Dimension (maxWidth,maxHeight);
    //Dimension dimHalf = new Dimension (maxWidth/2,maxHeight/2);
    //serverLabel.setPreferredSize(dimHalf);
    //portLabel.setPreferredSize(dimHalf);
    fileLabel.setPreferredSize(dim);
    serverText.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(JiLElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(JiLElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(JiLElement.this); }
    });
    portText.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(JiLElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(JiLElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(JiLElement.this); }
    });
    viFileText.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(JiLElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(JiLElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(JiLElement.this); }
    });

    JButton serverLinkButton = new JButton(ELEMENT_ICON);
    serverLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = serverText.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(serverText,"String", value);
        if (variable!=null) serverText.setText("%"+variable+"%");
      }
    });

    JButton portLinkButton = new JButton(ELEMENT_ICON);
    portLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = portText.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(portText,"String", value);
        if (variable!=null) portText.setText("%"+variable+"%");
      }
    });
    
    JButton fileLinkButton = new JButton(ELEMENT_ICON);
    fileLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = viFileText.getText().trim();
        value = "\"" + value +"\"";
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(viFileText,"String", value);
        if (variable!=null) viFileText.setText("%"+variable+"%");
      }
    });

    JPanel serverPanel = new JPanel(new BorderLayout());
    serverPanel.add(serverLabel, BorderLayout.WEST);
    serverPanel.add(serverText, BorderLayout.CENTER);
    serverPanel.add(serverLinkButton, BorderLayout.EAST);
    
    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel, BorderLayout.WEST);
    portPanel.add(portText, BorderLayout.CENTER);
    portPanel.add(portLinkButton, BorderLayout.EAST);

    JPanel filePanel = new JPanel(new BorderLayout());
    filePanel.add(fileLabel, BorderLayout.WEST);
    filePanel.add(viFileText, BorderLayout.CENTER);
    filePanel.add(fileLinkButton, BorderLayout.EAST);

    JPanel hostPanel = new JPanel(new GridLayout(1,2));
    hostPanel.add(serverPanel);
    hostPanel.add(portPanel);
    
    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.add(hostPanel);
    topPanel.add(filePanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;*/
  }
  
  protected Component createJilEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    parent = parentComponent;
    
    JPanel mainPanel = new JPanel(); 
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    mainPanel.setPreferredSize(new Dimension(430, 600));

    // ------------------------------
    // JiL server configuration panel
    // ------------------------------
    JPanel topPanel = new JPanel();
    topPanel.setBorder(new TitledBorder(null, "JiL server configuration", TitledBorder.LEADING, TitledBorder.TOP));
    mainPanel.add(topPanel);
    topPanel.setMinimumSize(new Dimension(420, 120));
    topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
    topPanel.setPreferredSize(new Dimension(420, 120));
    SpringLayout sl_topPanel = new SpringLayout();
    topPanel.setLayout(sl_topPanel);
    
    JLabel serverLabel = new JLabel("Server address:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, serverLabel, 7, SpringLayout.NORTH, topPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverLabel, 5, SpringLayout.WEST, topPanel);
    topPanel.add(serverLabel);
    
    JLabel portLabel = new JLabel("Port:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, portLabel, 0, SpringLayout.NORTH, serverLabel);
//    sl_topPanel.putConstraint(SpringLayout.EAST, portLabel, 126, SpringLayout.WEST, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.EAST, serverText);
    topPanel.add(portLabel);
    
    JLabel viFileLabel = new JLabel("Vi file path:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, viFileLabel, 6, SpringLayout.SOUTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, viFileLabel, 0, SpringLayout.EAST, serverLabel);
    topPanel.add(viFileLabel);
    
    sl_topPanel.putConstraint(SpringLayout.NORTH, serverText, -2, SpringLayout.NORTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 6, SpringLayout.EAST, serverLabel);
    topPanel.add(serverText);
    serverText.setColumns(10);

    sl_topPanel.putConstraint(SpringLayout.NORTH, portText, -2, SpringLayout.NORTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, portText, 6, SpringLayout.EAST, portLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, portText, -6, SpringLayout.EAST, topPanel);
    topPanel.add(portText);
    portText.setColumns(10);

    sl_topPanel.putConstraint(SpringLayout.NORTH, viFileText, 4, SpringLayout.SOUTH, serverText);
    sl_topPanel.putConstraint(SpringLayout.WEST, viFileText, 6, SpringLayout.EAST, viFileLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, viFileText, -6, SpringLayout.EAST, topPanel);
    topPanel.add(viFileText);
    viFileText.setColumns(10);
    
    JButton testButton = new JButton("Get metadata");
    sl_topPanel.putConstraint(SpringLayout.NORTH, testButton, 6, SpringLayout.SOUTH, viFileText);
    sl_topPanel.putConstraint(SpringLayout.EAST, testButton, -6, SpringLayout.EAST, topPanel);
    //JSV_Temporal change
    topPanel.add(testButton);   
    
    JPanel labviewPanel = new JPanel();
    mainPanel.add(labviewPanel);
    labviewPanel.setLayout(new BoxLayout(labviewPanel, BoxLayout.X_AXIS));
    //JSV_Temporal change
    labviewPanel.setBorder(new TitledBorder(null, "LabVIEW controls and indicators", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    labviewPanel.setMinimumSize(new Dimension(420, 120));   
    labviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    labviewPanel.setPreferredSize(new Dimension(420, 120));
   
    // ----------------------
    // LabVIEW Controls Table
    // ----------------------
    controlsTable = new JTable(controlsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };      
    controlsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JScrollPane controlsScrollPane = new JScrollPane(controlsTable); //an scroll panel for the table
    //JSV_Temporal change
    labviewPanel.add(controlsScrollPane);
    controlsScrollPane.setBorder(BorderFactory.createTitledBorder("VI Controls"));
    
      AbstractAction connectControlToVariable = new AbstractAction("Connect control to variable "){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
          if(controlsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Please, select a control.");
            return;           
          }
            String varname = (String)controlsTable.getValueAt(controlsTable.getSelectedRow(), 0),
                 vartype = (String)controlsTable.getValueAt(controlsTable.getSelectedRow(), 1);         

          // Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"         
          //String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", vartype, linksTable, varname, "");
          String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", "", linksTable, varname, ""); 
          if (variable != null) {
            Vector<String> row = new Vector<String>();
            row.add(varname+"(con)");
            row.add(variable + " : " + vartype);
            row.add("setValue(\"" + varname + "(con)\", " + variable + ");");
            linksTableModel.addRow(row);
          }
        }
      };
      
      popupMenuCon = new JPopupMenu();
      popupMenuCon.add(connectControlToVariable);
      controlsTable.addMouseListener (new MouseAdapter() {        
        public void mousePressed (MouseEvent _evt) {
          if (OSPRuntime.isPopupTrigger(_evt) && controlsTable.isEnabled ()) {
            int row = controlsTable.rowAtPoint(_evt.getPoint()); 
            if(row != -1) controlsTable.setRowSelectionInterval(row, row);
            popupMenuCon.show(_evt.getComponent(), _evt.getX(), _evt.getY());
          }
        }
      });    
    
    // ------------------------
    // LabVIEW Indicators Table
    // ------------------------
    indicatorsTable = new JTable(indicatorsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    indicatorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane indicatorsScrollPane = new JScrollPane(indicatorsTable); //an scroll panel for the table
    //JSV_Temporal change
    labviewPanel.add(indicatorsScrollPane);
    indicatorsScrollPane.setBorder(BorderFactory.createTitledBorder("VI indicators"));

      AbstractAction connectIndicatorToVariable = new AbstractAction("Connect to Model Variable"){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
          if(indicatorsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Please, select an indicator.");
            return;           
          }
            String varname = (String)indicatorsTable.getValueAt(indicatorsTable.getSelectedRow(), 0),
                 vartype = (String)indicatorsTable.getValueAt(indicatorsTable.getSelectedRow(), 1);         
        
          // Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"         
          //String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", vartype, linksTable, varname, "");
          String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", "", linksTable, varname, "");
          if (variable != null) {
            Vector<String> row = new Vector<String>();
            row.add(varname+"(ind)");
            row.add(variable + " : " + vartype);            
            row.add(variable + " = get" + vartype.substring(0,1).toUpperCase() + vartype.substring(1) + "(\"" + varname + "(ind)\");");
            linksTableModel.addRow(row);
          }
        }
      };

      popupMenuInd = new JPopupMenu();
      popupMenuInd.add(connectIndicatorToVariable);
      indicatorsTable.addMouseListener (new MouseAdapter() {
        public void mousePressed (MouseEvent _evt) {
          if (OSPRuntime.isPopupTrigger(_evt) && indicatorsTable.isEnabled ()) {
            int row = indicatorsTable.rowAtPoint(_evt.getPoint()); 
            if(row != -1) indicatorsTable.setRowSelectionInterval(row, row);
            popupMenuInd.show(_evt.getComponent(), _evt.getX(), _evt.getY());
          }
        }
      });

    // -----------
    // Links Table
    // -----------
      linksTable = new JTable(linksTableModel) {  
        private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    JScrollPane linksScrollPane = new JScrollPane(linksTable); //an scroll panel for the table
    //JSV_Temporal change
    mainPanel.add(linksScrollPane);
    linksScrollPane.setBorder(BorderFactory.createTitledBorder("Table of Linked Variables"));
    linksScrollPane.setMinimumSize(new Dimension(420, 100));
    linksScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    linksScrollPane.setPreferredSize(new Dimension(420, 120));

      TableModelListener ta = new TableModelListener(){ //Add a row to the table when last row is edited
        public void tableChanged(TableModelEvent e){
          if (TableModelEvent.UPDATE == e.getType() && linksTable.getRowCount() == e.getLastRow()+1){
            linksTableModel.addRow(new String[]{"",""});
          }       
        }       
      };
      linksTable.getModel().addTableModelListener(ta);
      
      popupMenuEJS = new JPopupMenu(); // The popup menu for the table

      AbstractAction delete = new AbstractAction("Delete link(s)"){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
          int next = linksTable.getSelectedRow();
          while(next != -1) {
            linksTableModel.removeRow(next);
            next = linksTable.getSelectedRow();
          }
        }
      };
          
      popupMenuEJS.add(delete);
      
      linksTable.addMouseListener ( new MouseAdapter() {
        public void mousePressed (MouseEvent _evt) {
          if (OSPRuntime.isPopupTrigger(_evt) && linksTable.isEnabled ()) {
            popupMenuEJS.show(_evt.getComponent(), _evt.getX(), _evt.getY());
          }
        }
      });    

   // -------------------------------------------------------------------------------
      // Button to test the connection with the JIL server and to obtain the VI metadata
      // -------------------------------------------------------------------------------   
      
      //JSV_Temporal Change Disable test button
      
      AbstractAction testServer = new AbstractAction("Get VI variables"){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {        
          if(jilTest == null) {           
            jilTest = new Labview("http://"+serverText.getText()+":"+portText.getText());
            jilTest.connect();
          }
      
          if(jilTest.isConnected()) {
            jilTest.openVI(viFileText.getText());
            if(jilTest.isOpened()) {
              Vector<String> columns = new Vector<String>();
              columns.add("Name"); columns.add("Type");
              Vector<Vector<String>> controls = hashmapToVector(jilTest.getControls());
              Vector<Vector<String>> indicators = hashmapToVector(jilTest.getIndicators());
              controlsTableModel.setDataVector(controls, columns);
              indicatorsTableModel.setDataVector(indicators, columns);
              JOptionPane.showMessageDialog(parent, CONNECTION_OK);
            } else {
              JOptionPane.showMessageDialog(parent, INVALID_PATH);
            }
          } else {
            JOptionPane.showMessageDialog(parent, SERVER_KO);
          }
          jilTest.disconnect();
        jilTest = null;
        }
      };
      testButton.setAction(testServer);
      
      // --------------------------
      // Button to close the dialog
      // --------------------------
      JButton closeButton = new JButton(new AbstractAction("Close") {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
          JButton button = ((JButton)e.getSource());
          JDialog dialog = null;          
          if(button != null) dialog = (JDialog)button.getTopLevelAncestor();        
          if(dialog != null) dialog.setVisible(false);        
        }       
      });     
      closeButton.setAlignmentX(JButton.CENTER_ALIGNMENT);

      mainPanel.add(closeButton);
      
// -------------------------------------------------------------------------------------------------
// Adds the components to the main panel
// -------------------------------------------------------------------------------------------------

      mainPanel.setPreferredSize(new Dimension(430,400));

      return mainPanel;
  }
  
  /**
   * 
   * @param hashmap
   * @return
   */
  private Vector<Vector<String>> hashmapToVector(HashMap<String,String> hashmap) {
      Iterator<Map.Entry<String,String>> iter = hashmap.entrySet().iterator();
      Vector<Vector<String>> result = new Vector<Vector<String>>();
      
      while(iter.hasNext()) {
        Map.Entry<String,String> entry = iter.next();
        Vector<String> row = new Vector<String>();
        row.add(entry.getKey());
        row.add(entry.getValue());
        result.add(row);
      }
      return result;
  }
  
  public String savetoXML() {
    String result = "<" + XML_NODE_LABEL_JIL + ">" +
        "<" + XML_NODE_LABEL_SERVER + ">" + serverText.getText() + "</" + XML_NODE_LABEL_SERVER + ">" +
              "<" + XML_NODE_LABEL_PORT + ">" + portText.getText() + "</" + XML_NODE_LABEL_PORT + ">" +
              "<" + XML_NODE_LABEL_PATH + ">" + viFileText.getText() + "</" + XML_NODE_LABEL_PATH + ">";

    if(linksTableModel != null) {
      result += "<" + XML_NODE_LABEL_LINKS + ">";
      for(Vector v : (Iterable<Vector>)(linksTableModel.getDataVector())) {
        result += "<" + XML_NODE_LABEL_ROW + ">" + 
                "<" + XML_NODE_LABEL_LABVIEW + ">" + v.elementAt(0) + "</" + XML_NODE_LABEL_LABVIEW + ">" +  
                "<" + XML_NODE_LABEL_MODEL + ">" + v.elementAt(1) + "</" + XML_NODE_LABEL_MODEL + ">" + 
                "<" + XML_NODE_LABEL_TYPE + ">" + v.elementAt(2) + "</" + XML_NODE_LABEL_TYPE + ">" + 
                "</" + XML_NODE_LABEL_ROW + ">" + "\n";
      }
      result += "</" + XML_NODE_LABEL_LINKS + ">";
    }
    result += "</" + XML_NODE_LABEL_JIL + ">";
    
    return result;
        
  }

  /** 
   * Restores the states from an XML String
   */
  public void readfromXML(String _inputXML) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    InputSource is = new InputSource(new StringReader(_inputXML));        
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e1) {
      System.out.println("Error al configurar el interprete de XML.");
    }     

    try {
      Document doc = db.parse(is);
      // Server configuration

      this.serverText.setText(doc.getElementsByTagName(XML_NODE_LABEL_SERVER).item(0).getTextContent());
      this.portText.setText(doc.getElementsByTagName(XML_NODE_LABEL_PORT).item(0).getTextContent());
      this.viFileText.setText(doc.getElementsByTagName(XML_NODE_LABEL_PATH).item(0).getTextContent());

      // The links between labview variables and model variables 
      Node links = doc.getElementsByTagName(XML_NODE_LABEL_LINKS).item(0);
      if (links != null) {
        NodeList linksList = links.getChildNodes();

        int i = 0; 
        Node node = linksList.item(i);
        while(node != null) {       
          if(node.getNodeName() == XML_NODE_LABEL_ROW) { 
            Vector<String> row = new Vector<String>();
            int j = 0;                  
            Node node1 = node.getFirstChild(), 
               node2 = node1.getNextSibling(), 
               node3 = node2.getNextSibling();
            // The node labels are NOT checked
            row.add(node1.getTextContent());
            row.add(node2.getTextContent());
            row.add(node3.getTextContent());
            linksTableModel.addRow(row);
          }

          node = linksList.item(++i);       
        }
      }
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
