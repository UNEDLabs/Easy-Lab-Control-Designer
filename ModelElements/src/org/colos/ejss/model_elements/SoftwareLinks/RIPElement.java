package org.colos.ejss.model_elements.SoftwareLinks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.edition.ModelEditor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.java_websocket.client.WebSocketClient;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;



import javax.json.JsonReader;



//import org.json.JSONArray;
import org.json.JSONException;
//import org.json.JSONObject;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient;

import javax.xml.parsers.*;
/**
 * Class to interact with a JIL Server
 * @author Adapted to Javascript from the original code of Jesús Chacón <jcsombria@gmail.com> by Jacobo Sáenz 
 */
public class RIPElement extends AbstractModelElement {
  private static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/SoftwareLinks/rip.png"); // This icon is included in this jar
  
  private static final String XML_NODE_LABEL_RIP = "rip";
  private static final String XML_NODE_LABEL_SERVER = "server";
  private static final String XML_NODE_LABEL_PORT = "port";
  private static final String XML_NODE_LABEL_EVAL = "evalcode";
  private static final String XML_NODE_LABEL_CALLBACK = "callbackcode";
  private static final String XML_NODE_LABEL_PATH = "path";
  private static final String XML_NODE_LABEL_LINKS = "links";
  private static final String XML_NODE_LABEL_ROW = "row";
  private static final String XML_NODE_LABEL_JS = "jsclient";
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
   private JTextField evaluableText = new JTextField("", 50);
   private JTextField viFileText = new JTextField();  // needs to be created to avoid null references
   private JTextArea callbackText = new JTextArea("", 4 , 23);
   private JTextArea initialCodeText = new JTextArea("", 4 , 23);
   //private JTextField viFileText = new JTextField("ripTest.vi", 32);  
   
   private JPopupMenu popupMenuEJS;  
   private JPopupMenu popupMenuLV;
   
   private JTable controlsTable;
   private JTable indicatorsTable;
   private JTable linksTable;
   private JScrollPane scrollPaneControls;
   private JScrollPane scrollPaneIndicators;
   private JPopupMenu popupMenuInd;
   private JPopupMenu popupMenuCon;
  
   // used to test the connection with the server
   RemoteMatlabConnectorClient ripTest = null;
   private Component parent;
   private JsonArray methodsArray;
   private JsonArray controlsArray;
   private JsonArray indicatorsArray;
   
  
   public RIPElement() { 
     // Server writable Table
     controlsTableModel = new DefaultTableModel();
     Vector<String> controlsColumns = new Vector<String>();
     controlsColumns.add("Name");
     controlsColumns.add("Type");
     Vector<Vector<String>> controlsVector = new Vector<Vector<String>>();
     controlsTableModel.setDataVector(controlsVector, controlsColumns);
     // Server readable Table
     indicatorsTableModel = new DefaultTableModel();
     Vector<String> indicatorsColumns = new Vector<String>();
     indicatorsColumns.add("Name");
     indicatorsColumns.add("Type");
     Vector<Vector<String>> indicatorsVector = new Vector<Vector<String>>();
     indicatorsTableModel.setDataVector(indicatorsVector, indicatorsColumns);
     // Links Table
       linksTableModel = new DefaultTableModel();
       Vector<String> namesVector = new Vector<String>();
       namesVector.add("Server Variable");
       namesVector.add("Model Variable");
       namesVector.add("Type");
       Vector<Vector<String>> linksVector = new Vector<Vector<String>>();      
       linksTableModel.setDataVector(linksVector, namesVector);
   }
   
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "RIP_Element"; }
  
  public String getConstructorName() { return "RIP"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    StringBuffer buffer = new StringBuffer();
    buffer.append("var RIP = {};");  
    return buffer.toString(); 
  } 

  public String getSourceCode(String name) { // Code that goes into the body of the model 
    StringBuffer buffer = new StringBuffer();
    buffer.append("var " + name + " = new wrapper(\"" + serverText.getText() + "\"," + portText.getText() + ");\n");
    if (serverText != null && portText != null && serverText.getText() != "" && portText.getText() != ""){
      buffer.append(name +".host = " + "\"" +"http://" + serverText.getText().trim()+":"+ portText.getText().trim()  +"\"" + ";\n");
    }
    else buffer.append(name +".host = NONE");
    //JSV Changes -> // adding a new function "step" to the RIP object called *name*, 
    //using the links established in the element editor  
    buffer.append(buildStepCode(name));
    buffer.append(buildInitCode(name));
    buffer.append(buildDefaultCallback());
    return buffer.toString();
  }  

  public String buildStepCode(String name){
    StringBuffer buffer = new StringBuffer();
    //JSV Changes -> // adding a new function "step" to the RIP object called *name*, 
    //using the links established in the element editor
    buffer.append(name + ".step = function(callback){\n");
    buffer.append("\t" + "// === Step function ===\n");
    buffer.append(appendListAsCalls(name,"con"));   //Linked controls as function call
    buffer.append(appendEvalCalls(name,"eva"));     //Eval expressions as function call
    buffer.append(appendListAsCalls(name,"ind"));   //Linked indicators as function call
    buffer.append(buildSyncBody("response"));
    //buffer.append("\t" +"return;\n");
    buffer.append("}\n");
    return buffer.toString();
    
  }
  
  public String buildInitCode(String name){
    StringBuffer buffer = new StringBuffer();
    //buffer.append(name +".filePath = NONE");
    //JSV Changes -> // adding a new function "step" to the RIP object called *name*, 
    //using the links established in the element editor
    buffer.append(name + ".init = function(){\n");
    buffer.append("\t" + "// === Init function ===\n");
    buffer.append("\t"+initialCodeText.getText()); 
    buffer.append("\t");
    buffer.append("\n");
    buffer.append("}\n");
    return buffer.toString();
  }
  
  public String buildSyncBody(String callBackParam){
    StringBuffer buffer = new StringBuffer();
    if (callBackParam.equals(""))
      buffer.append("\tthis.sync(function(){\n");
    else      
      buffer.append("\tthis.sync(function("+ callBackParam +"){\n");
    buffer.append("\t\tif(callback != \"undefined\"){\n");
    if (!callBackParam.equals("")){
      buffer.append("\t\t\tvar result = response[2].result;\n");
      buffer.append("\t\t\t__callback(result);\n\t\t}");
      buffer.append("\n");
    }else
      buffer.append("\t\t\t__callback();\n\t\t}");
    buffer.append("\t});\n");
    buffer.append("\t\n");
    return buffer.toString();
  }
  
  public String buildDefaultCallback(){
    //If callback function is not defined in the step function at evolution page, we can use one like this, to update
    //update the values of the changed variables
    StringBuffer buffer = new StringBuffer();
    buffer.append("var __callback = function(response){\n");
    buffer.append("\t" + "// === Default callback function ===\n");
    buffer.append("\t"+callbackText.getText()); 
    buffer.append("\t");
    buffer.append("\n}");
    return buffer.toString();
  }
  
  public String appendEvalCalls(String name,String type){
    StringBuffer buffer = new StringBuffer();
    if (type.equals("eva")){
      // It is not a link, its a eval function
      buffer.append("\t" +name + ".post(\"eval\",[\""+ evaluableText.getText() + "\"]);"); //First Parameter
      //buffer.append("\t" +name + ".post(\"eval\",[\""+ evaluableText.getText() + "\"]);"); //First Parameter
      buffer.append("\n");
      //this.post('eval', ['y=sin(2*pi*f*t);']);
    }
    return buffer.toString();
  }
  
  
  public String appendListAsCalls(String name,String type){
    StringBuffer controlsBuffer = new StringBuffer(), indicatorsBuffer = new StringBuffer();
    String serverVar = "";      //Complete name of the variable that comes from the server
    String modelVar = "";       //Just the name of the variable at the client side
    String serverVarType = "";  //Only the name of the variable from the server
    int firstIdxModel = 0, lastIdxModel = 0;   //First and last indexes of model vars
    int idxVi = 0;              //First index of vi vars
    boolean firstLine = true;
    for(Vector<?> v : (Iterable<Vector>)(linksTableModel.getDataVector())) {
      serverVar = v.elementAt(0).toString();              //Picks up the indicator/control name(type) of the vi
      modelVar = v.elementAt(1).toString();               //Picks up the Type:Name of the model variable
      idxVi = serverVar.indexOf("(");
      serverVarType = serverVar.substring(idxVi+1,serverVar.indexOf(")")); //Type of serverVar
      firstIdxModel = modelVar.indexOf(":")+1;           //To isolate the modelVar name
      lastIdxModel = modelVar.lastIndexOf(":");          //To isolate the modelVar name
      //System.out.println("ServerVar : "+ serverVar + ", modelVar: "+ modelVar + ", serverVarType: " + serverVarType);
      if (serverVarType.contains(type)){ 
        if (serverVarType.contains("ind")){        //If and indicator -> we need to make a modelVar = name.get(serverVar)
          //System.out.println("ind: ServerVar : "+ serverVar.substring(0,idxVi) + ", modelVar: "+ modelVar.substring(firstIdxModel,lastIdxModel));
          if (firstLine){
            indicatorsBuffer.append("\t" + modelVar.substring(firstIdxModel,lastIdxModel) + "="); //Left side of expression
            indicatorsBuffer.append("\t" +name + ".post(\"get\",[[\""+ serverVar.substring(0,idxVi)+"\"]"); //First Parameter
            firstLine = false;
          }else{
            indicatorsBuffer.append("," + "[\""+ serverVar.substring(0,idxVi)+"\"]"); //First Parameter
          }
        }else if (serverVarType.contains("con")) {  //If and control -> we need to make a name.set(serverVar,modelVar)
          //System.out.println("con: ServerVar : "+ serverVar.substring(0,idxVi) + ", modelVar: "+ modelVar.substring(firstIdxModel,lastIdxModel));
          if (firstLine){
            controlsBuffer.append("\t" +name + ".post(\"set\",[[[\""+ serverVar.substring(0,idxVi)+"\"]," + "["+modelVar.substring(firstIdxModel,lastIdxModel) + "]]"); //First Parameter
            firstLine = false;
          }else{
            controlsBuffer.append("," +"[[\""+ serverVar.substring(0,idxVi)+"\"]," + "["+modelVar.substring(firstIdxModel,lastIdxModel) + "]]");
          }
          //System.out.println("writed: " + serverVar.substring(0,idxVi));

        }else{  // It is not a link, its a eval function
          System.err.println("Error in appendListAsCalls with parameters");
        }
      }
    }
    if (controlsBuffer.length()>1) controlsBuffer.append("]);\n");
    if (indicatorsBuffer.length()>1) indicatorsBuffer.append("]);\n");
    if (type.equals("ind"))    return indicatorsBuffer.toString();
    else                                return controlsBuffer.toString();
  }
  
  public String getImportStatements() { // Required for Lint
    return "SoftwareLinks/RIP.js";
    //return "Input_output/JiL.js;Input_output/mimic.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access to Remote Interoperability Protocol through internet using XML-RPC";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejss/model_elements/SoftwareLinks/RIP.html"; 
  }
  
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    //Using the GUI developed by Jesús Chacón Sombría: 
    return createJilEditor(name,parentComponent,collection);
  }
  
  protected Component createJilEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    parent = parentComponent;
    
    JPanel mainPanel = new JPanel(); 
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    mainPanel.setPreferredSize(new Dimension(620, 900));

    // ------------------------------
    // RIP server configuration panel
    // ------------------------------
    JPanel topPanel = new JPanel();
    topPanel.setBorder(new TitledBorder(null, "RIP server configuration", TitledBorder.LEADING, TitledBorder.TOP));
    mainPanel.add(topPanel);
    topPanel.setMinimumSize(new Dimension(620, 180));
    topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
    topPanel.setPreferredSize(new Dimension(620, 180));
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
    
    JLabel viFileLabel = new JLabel("File path:");
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
   
    JButton testButtonAux = new JButton("Get metadata as JSON");
    sl_topPanel.putConstraint(SpringLayout.NORTH, testButtonAux, 6, SpringLayout.SOUTH, viFileText);
    sl_topPanel.putConstraint(SpringLayout.WEST, testButtonAux, 0, SpringLayout.WEST, topPanel);
    //JSV_Temporal change
    topPanel.add(testButtonAux);
    
    JButton testButton = new JButton("Get metadata Test");
    sl_topPanel.putConstraint(SpringLayout.NORTH, testButton, 6, SpringLayout.SOUTH, viFileText);
    sl_topPanel.putConstraint(SpringLayout.EAST, testButton, 0, SpringLayout.EAST, topPanel);
    //JSV_Temporal change
    topPanel.add(testButton);
    
    JPanel ripPanel = new JPanel();
    mainPanel.add(ripPanel);
    ripPanel.setLayout(new BoxLayout(ripPanel, BoxLayout.X_AXIS));
    //JSV_Temporal change
    ripPanel.setBorder(new TitledBorder(null, "Server writable and readable variables", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    ripPanel.setMinimumSize(new Dimension(620, 180));   
    ripPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    ripPanel.setPreferredSize(new Dimension(620, 180));
   
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
    ripPanel.add(controlsScrollPane);
    controlsScrollPane.setBorder(BorderFactory.createTitledBorder("Server Writable Variables"));
    
      AbstractAction connectControlToVariable = new AbstractAction("Connect Writable to variable "){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
          if(controlsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Please, select a Writable Var.");
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
    // RIP Server Readable Variables Table
    // ------------------------
    indicatorsTable = new JTable(indicatorsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    indicatorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane indicatorsScrollPane = new JScrollPane(indicatorsTable); //an scroll panel for the table
    //JSV_Temporal change
    ripPanel.add(indicatorsScrollPane);
    indicatorsScrollPane.setBorder(BorderFactory.createTitledBorder("Server Readable variables"));

      AbstractAction connectIndicatorToVariable = new AbstractAction("Connect Readable to Model Variable"){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
          if(indicatorsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Please, select an Readable Var.");
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

    // ------------------------
    // RIP Server Evaluable Expressions
    // ------------------------
      
      JPanel topEvaluablePanel = new JPanel();
      topEvaluablePanel.setBorder(new TitledBorder(null, "Functions to evaluate in the server ", TitledBorder.LEADING, TitledBorder.TOP));
      mainPanel.add(topEvaluablePanel);
      topEvaluablePanel.setMinimumSize(new Dimension(620, 180));
      topEvaluablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
      topEvaluablePanel.setPreferredSize(new Dimension(620, 180));
      SpringLayout sl_topEvaluable = new SpringLayout();
      topEvaluablePanel.setLayout(sl_topEvaluable);
      
      JLabel evalLabel = new JLabel("Expression to Evaluate:");
      sl_topEvaluable.putConstraint(SpringLayout.NORTH, evalLabel, 7, SpringLayout.NORTH, topEvaluablePanel);
      sl_topEvaluable.putConstraint(SpringLayout.WEST, evalLabel, 7, SpringLayout.WEST, topEvaluablePanel);
      topEvaluablePanel.add(evalLabel);
      
      sl_topEvaluable.putConstraint(SpringLayout.NORTH, evaluableText, 4, SpringLayout.SOUTH, evalLabel);
      sl_topEvaluable.putConstraint(SpringLayout.WEST, evaluableText, 0, SpringLayout.WEST, evalLabel);
      topEvaluablePanel.add(evaluableText);
      evaluableText.setColumns(10);

      JLabel callbackLabel = new JLabel("Action after Evaluation (Callback):");
      sl_topEvaluable.putConstraint(SpringLayout.NORTH, callbackLabel, 7, SpringLayout.NORTH, topEvaluablePanel);
      sl_topEvaluable.putConstraint(SpringLayout.WEST, callbackLabel, 7, SpringLayout.EAST, evalLabel);
      topEvaluablePanel.add(callbackLabel);
      
      sl_topEvaluable.putConstraint(SpringLayout.NORTH, callbackText, 4, SpringLayout.SOUTH, callbackLabel);
      sl_topEvaluable.putConstraint(SpringLayout.WEST, callbackText, 24, SpringLayout.EAST, evaluableText);
      topEvaluablePanel.add(callbackText);
      callbackText.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      
      
      JLabel initialCodeLabel = new JLabel("Initial Code:");
      sl_topEvaluable.putConstraint(SpringLayout.NORTH, initialCodeLabel, 7, SpringLayout.NORTH, topEvaluablePanel);
      sl_topEvaluable.putConstraint(SpringLayout.WEST, initialCodeLabel, 87, SpringLayout.EAST, callbackLabel);
      topEvaluablePanel.add(initialCodeLabel);
      //sl_topEvaluable.putConstraint(SpringLayout.SOUTH, topEvaluablePanel, 4, SpringLayout.SOUTH, initialCodeText);
      sl_topEvaluable.putConstraint(SpringLayout.NORTH, initialCodeText, 4, SpringLayout.SOUTH, initialCodeLabel);
      sl_topEvaluable.putConstraint(SpringLayout.WEST, initialCodeText, 24, SpringLayout.EAST, callbackText);
      topEvaluablePanel.add(initialCodeText);
      initialCodeText.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      //callbackText.setColumns(15);
      //callbackText.setSize(15,50);

      sl_topEvaluable.putConstraint(SpringLayout.NORTH, portText, -2, SpringLayout.NORTH, evalLabel);
      
      
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
    linksScrollPane.setMinimumSize(new Dimension(620, 180));
    linksScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    linksScrollPane.setPreferredSize(new Dimension(620, 180));

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
      // Button to test the connection with the RIP server and to obtain the metadata object
      // -------------------------------------------------------------------------------   
      
      //JSV_Temporal Change Disable test button
      //Obtain a  metadata block: Like the next one: 
      /*{"methods":["connect","set","get","disconnect"],"readable":["ball_heigh
      t","setpoint","kp","ki","kd"],"writable":["fan_control","setpoint","kp"
      ,"ki","kd"]}*/
          
      AbstractAction testServerAux = new AbstractAction("Get variables from JSON Metadata"){
        boolean connected = false;
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {  
          System.out.println("http://"+serverText.getText()+":"+portText.getText());
          try {
            //if(ripTest == null) {
              es.uned.dia.softwarelinks.transport.HttpTransport transport = new es.uned.dia.softwarelinks.transport.HttpTransport("http://"+serverText.getText()+":"+portText.getText());
              ripTest = new es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient(transport);
              connected = ripTest.connect();
            //}
            if(connected) {
              //if(ripTest.isOpened()) {
                Vector<String> columns = new Vector<String>();
                columns.add("Name"); columns.add("Type");
                String result = ripTest.getMetadata();
                InputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
                JsonReader reader = Json.createReader(stream);
                JsonObject message = reader.readObject();
                System.err.println(result);
                System.err.println(message);
                if(message.containsKey("methods")) {
                  methodsArray  = message.getJsonArray("methods");            
                } else {
                  System.err.println("methods not found");
                }
                if(message.containsKey("writable")) {
                  controlsArray  = message.getJsonArray("writable");            
                } else {
                  System.err.println("writable not found");
                }
                if(message.containsKey("readable")) {
                  indicatorsArray  = message.getJsonArray("readable");            
                } else {
                  System.err.println("readable not found");
                }

                //JSONArray controlsArray = message.getJSONArray("writeable");
                //JSONArray indicatorsArray  = message.getJSONArray("readable");
                
                System.out.println("Array de writeable Obtenido: " + controlsArray.toString());
                System.out.println("Array de readable Obtenido: " + indicatorsArray.toString());
                System.out.println(result);
                Vector<Vector<String>> controls = hashmapToVector(getControls());
                Vector<Vector<String>> indicators = hashmapToVector(getIndicators());
                controlsTableModel.setDataVector(controls, columns);
                indicatorsTableModel.setDataVector(indicators, columns);
                JOptionPane.showMessageDialog(parent, CONNECTION_OK);
              //} else {
               // JOptionPane.showMessageDialog(parent, INVALID_PATH);
              //}
            } else {
              JOptionPane.showMessageDialog(parent, SERVER_KO);
            }
            //ripTest.disconnect();
            ripTest = null;
          } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
          
      };
      testButtonAux.setAction(testServerAux);
      
      AbstractAction testServer = new AbstractAction("Get VI Test variables"){
        boolean connected = false;
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {  
          System.out.println("http://"+serverText.getText()+":"+portText.getText());
          try {
            if(ripTest == null) {
              //es.uned.dia.softwarelinks.transport.HttpTransport transport = new es.uned.dia.softwarelinks.transport.HttpTransport("http://"+serverText.getText()+":"+portText.getText());
              //ripTest = new es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient(transport);
              
              connected = true;//ripTest.connect();
            }
            if(connected) {
              //if(ripTest.isOpened()) {
                Vector<String> columns = new Vector<String>();
                columns.add("Name"); columns.add("Type");
                String result = "{\"methods\":[\"connect\",\"set\",\"get\",\"disconnect\"],\"readable\":[\"ball_height\",\"setpoint\",\"kp\",\"ki\",\"kd\"],\"writable\":[\"fan_control\",\"setpoint\",\"kp\",\"ki\",\"kd\"]}\")";//; ripTest.getMetadata();
                InputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
                JsonReader reader = Json.createReader(stream);
                JsonObject message = reader.readObject();
                System.err.println(result);
                System.err.println(message);
                if(message.containsKey("methods")) {
                  methodsArray  = message.getJsonArray("methods");            
                } else {
                  System.err.println("methods not found");
                }
                if(message.containsKey("writable")) {
                  controlsArray  = message.getJsonArray("writable");            
                } else {
                  System.err.println("writable not found");
                }
                if(message.containsKey("readable")) {
                  indicatorsArray  = message.getJsonArray("readable");            
                } else {
                  System.err.println("readable not found");
                }

                //JSONArray controlsArray = message.getJSONArray("writeable");
                //JSONArray indicatorsArray  = message.getJSONArray("readable");
                
                System.out.println("Array de writeable Obtenido: " + controlsArray.toString());
                System.out.println("Array de readable Obtenido: " + indicatorsArray.toString());
                System.out.println(result);
                Vector<Vector<String>> controls = hashmapToVector(getControls());
                Vector<Vector<String>> indicators = hashmapToVector(getIndicators());
                controlsTableModel.setDataVector(controls, columns);
                indicatorsTableModel.setDataVector(indicators, columns);
                JOptionPane.showMessageDialog(parent, CONNECTION_OK);
              //} else {
               // JOptionPane.showMessageDialog(parent, INVALID_PATH);
              //}
            } else {
              JOptionPane.showMessageDialog(parent, SERVER_KO);
            }
            //ripTest.disconnect();
          ripTest = null;
          } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
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

      mainPanel.setPreferredSize(new Dimension(800,750));

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
  
  @SuppressWarnings("unchecked")
  private HashMap<String,String> getControls() throws JSONException{
    HashMap<String, String> controls = new HashMap<String, String>();
    for(int i = 0; i<controlsArray.size();i++) {      
      controls.put(controlsArray.getString(i), "double");
    }
    return (HashMap<String,String>)controls.clone();
  }
  
  @SuppressWarnings("unchecked")
  private HashMap<String,String> getIndicators() throws JSONException{
    HashMap<String, String> indicators = new HashMap<String, String>();
    for(int i = 0; i<indicatorsArray.size();i++) {
      indicators.put(indicatorsArray.getString(i), "double");
    }
    return (HashMap<String,String>)indicators.clone();
  }
  
  @SuppressWarnings("unchecked")
  private HashMap<String,String> getmethods() throws JSONException{
    HashMap<String, String> methods = new HashMap<String, String>();
    for(int i = 0; i<methodsArray.size();i++) {
        methods.put(methodsArray.getString(i), "double");
    }
    
    return (HashMap<String,String>)methods.clone();
  }
  
  public String savetoXML() {
    String result = "<" + XML_NODE_LABEL_RIP + ">" +
        "<" + XML_NODE_LABEL_SERVER + ">" + serverText.getText() + "</" + XML_NODE_LABEL_SERVER + ">" +
              "<" + XML_NODE_LABEL_PORT + ">" + portText.getText() + "</" + XML_NODE_LABEL_PORT + ">"+
              "<" + XML_NODE_LABEL_EVAL + ">" + evaluableText.getText() + "</" + XML_NODE_LABEL_EVAL + ">"+
              "<" + XML_NODE_LABEL_CALLBACK + ">" + callbackText.getText() + "</" + XML_NODE_LABEL_CALLBACK + ">"+
              "<" + XML_NODE_LABEL_PATH + ">" + viFileText.getText() + "</" + XML_NODE_LABEL_PATH + ">";

    if(linksTableModel != null) {
      result += "<" + XML_NODE_LABEL_LINKS + ">";
      for(Vector v : (Iterable<Vector>)(linksTableModel.getDataVector())) {
        result += "<" + XML_NODE_LABEL_ROW + ">" + 
                "<" + XML_NODE_LABEL_JS + ">" + v.elementAt(0) + "</" + XML_NODE_LABEL_JS + ">" +  
                "<" + XML_NODE_LABEL_MODEL + ">" + v.elementAt(1) + "</" + XML_NODE_LABEL_MODEL + ">" + 
                "<" + XML_NODE_LABEL_TYPE + ">" + v.elementAt(2) + "</" + XML_NODE_LABEL_TYPE + ">" + 
                "</" + XML_NODE_LABEL_ROW + ">" + "\n";
      }
      result += "</" + XML_NODE_LABEL_LINKS + ">";
    }
    result += "</" + XML_NODE_LABEL_RIP + ">";
    
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
      this.evaluableText.setText(doc.getElementsByTagName(XML_NODE_LABEL_EVAL).item(0).getTextContent());
      this.callbackText.setText(doc.getElementsByTagName(XML_NODE_LABEL_CALLBACK).item(0).getTextContent());
      //this.viFileText.setText(doc.getElementsByTagName(XML_NODE_LABEL_PATH).item(0).getTextContent());

      // The links between server variables and model variables 
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
