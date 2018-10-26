package es.uned.dia.softwarelinks.model_elements;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.ModelEditor;
import org.colos.ejs.osejs.edition.variables.TableOfVariablesEditor;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.library.Model;
import org.colos.ejs.library.server.SocketView;
import org.colos.ejs.library.server.utils.MetadataBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.String;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opensourcephysics.display.OSPRuntime;
import org.w3c.dom.Document;

import es.uned.dia.ejss.softwarelinks.utils.RIPCodeBuilder;
import es.uned.dia.ejss.softwarelinks.utils.RIPConfigurationModel;
import es.uned.dia.softwarelinks.labview.protocol.xmlrpc.XmlRpcProtocol;
import es.uned.dia.softwarelinks.nodejs.RIPClient;
import es.uned.dia.softwarelinks.nodejs.RIPInfo;
//import es.uned.dia.softwarelinks.nodejs.RIPMetadata;
import es.uned.dia.softwarelinks.nodejs.RIPMethod;
import es.uned.dia.softwarelinks.nodejs.RIPClient.RIPException;
import es.uned.dia.softwarelinks.transport.HttpTransport;


public class SmartDeviceLinker extends AbstractModelElement {
  private static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uned/dia/ejss/softwarelinks/resources/rip.png"); // This icon is included in this jar
  
  protected static final Object CONNECTION_OK = "Connected to ";
  protected static final Object INVALID_PATH = "Invalid Path or nothing there";
  protected static final Object SERVER_KO = "Server is not working";
  
  
  
  private RIPConfigurationModel configuration = new RIPConfigurationModel();
  class LinksTableModel extends DefaultTableModel {
    public final String[] COLUMNS = {"Server", "EJS", "get", "set"};

      public void setDataVector(Object[][] dataVector) {
        setDataVector(dataVector, COLUMNS);
      }
  
      public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
      }
  }
  
  
  //GUI elements
  private LinksTableModel linksTableModel;
  private DefaultTableModel methodsTableModel;
  private DefaultTableModel indicatorsTableModel;
  private DefaultTableModel controlsTableModel;
  private JTextField serverText = new JTextField("localhost", 20);
  private JTextField portText = new JTextField("2055", 6);
  private JTextField evaluableText = new JTextField("", 50);
  private JTextField labIdText = new JTextField();
  private JTextField labDescriptionText = new JTextField();
  private JTextArea initialCodeText = new JTextArea("", 4 , 23);
  private JTextArea wrapperText = new JTextArea("", 4 , 23);
  private URI remoteServer = null; 
  private MetadataBuilder meta = null;
  
  private JPopupMenu popupMenuEJS;  
   
  private JTable methodsTable;
  private JTable controlsTable;
  private JTable indicatorsTable;
  private JTable linksTable;
  private JPopupMenu popupMenuInd;
  private JPopupMenu popupMenuCon;
  private JsonArray readable = Json.createArrayBuilder().build();
  private JsonArray writable = Json.createArrayBuilder().build();
  private TableOfVariablesEditor hojaSD;
  
   // used to test the connection with the server
  RIPClient ripTest = null;
  private Component parent;

  private boolean selectedLocalModel = true;
  
  private ModelEditor editor;

  private JPanel serverVariablesPanel;// = createServerVariablesPanel();
  
  private JScrollPane linksPanel;// = createLinksPanel();
  
  private JPanel codePanel;
  
  private JPanel wrapperPanel;

  private boolean showCodeTab;

  private JTabbedPane mainPanel;
  
  //private String elementName = "smartDeviceElementJ";
 
  public SmartDeviceLinker() { 
    // Server writable Table
    methodsTableModel = new DefaultTableModel();
    methodsTableModel.addColumn("Name");
    methodsTableModel.addColumn("Params");
    methodsTableModel.addColumn("Purpose");

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
    linksTableModel = new LinksTableModel();
    Object[][] data = {{"", "", new Boolean(false), new Boolean(true)}};
    linksTableModel.setDataVector(data);
  }
   
  //public String getElementName(){
   // return elementName;
  //}
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "SmartDeviceElementJ"; }
  
  public String getConstructorName() { return "es.uned.dia.softwarelinks.utils.SDLinker"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    //elementName = _name;
    String port = portText.getText().trim();
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name+"==null){\n "+ _name + " = new " + "es.uned.dia.softwarelinks.utils.SDLinker(_simulation);\n");
    buffer.append("org.colos.ejs.model_elements.input_output.WebSocketUtil webSocketServer" + " = new " + "org.colos.ejs.model_elements.input_output.WebSocketUtil" + "("+port+") {\n");
    buffer.append(getCodeWithName(_name));
    buffer.append("};\n");
    buffer.append(_name + ".setWebSocket(webSocketServer);\n}\n");
    
    return buffer.toString();
  } 

  private String getCodeWithName(String name) {
    return ""
        + "public void processInput (WebSocket conn,String message) { " + name +".processInputData(conn,message);}\n"
        + "public void onConnectionOpened (WebSocket conn) { " + name +".onOpen(conn);}\r\n" 
        + "public void onConnectionClosed (WebSocket conn) { " + name +".onClose(conn);}\n"
        + "public void onError (WebSocket conn, Exception ex) { " + name +".onError(conn,ex);}\n"
        + "\n";
  }
  
  public String getSourceCode(String name) { // Code that goes into the body of the model 
    getServerInfo();
    return "";
  }  

  
  public String getImportStatements() { // Required for Lint
    //return "/SoftwareLinks/smartDevice.js;/SoftwareLinks/wrapperSD.js"; 
    return "es.uned.dia.softwarelinks.utils.SDLinker; "
        + "org.colos.ejs.model_elements.input_output.WebSocketUtil;"
        + "org.java_websocket.WebSocket";
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access through Internet using the Remote Interoperability Protocol";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "es/uned/dia/ejss/softwarelinks/resources/rip.html"; 
  }
  
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    return createSDEditor(name, parentComponent, collection);
  }
  
  protected Component createSDEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    parent = parentComponent;
    editor = collection.getEJS().getModelEditor();

    mainPanel = new JTabbedPane();
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    mainPanel.setPreferredSize(new Dimension(600,300));

    JPanel serverPanel = createServerPanel(collection); 
    serverVariablesPanel = createServerVariablesPanel();
    linksPanel = createLinksPanel();
    codePanel = createCodePanel();
    mainPanel.addTab("Server", serverPanel);
    wrapperPanel = createWrapperPanel(collection);
    if(!selectedLocalModel){
      mainPanel.addTab("Reported Variables", serverVariablesPanel);
      mainPanel.addTab("Linked Variables", linksPanel);
      //mainPanel.addTab("Code", codePanel);
      mainPanel.addTab("Wrapper", wrapperPanel);
    }
    return mainPanel;
  }
  
  private JPanel createServerPanel(final ModelElementsCollection collection) {
    JPanel serverPanel = new JPanel();
    serverPanel.setBorder(new TitledBorder(null, "RIP server configuration", TitledBorder.LEADING, TitledBorder.TOP));
    serverPanel.setMinimumSize(new Dimension(620, 180));
    serverPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
    serverPanel.setPreferredSize(new Dimension(620, 180));
    SpringLayout sl_topPanel = new SpringLayout();
    serverPanel.setLayout(sl_topPanel);
    JLabel serverLabel = new JLabel("Server address:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, serverLabel, 7, SpringLayout.NORTH, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverLabel, 5, SpringLayout.WEST, serverPanel);
    serverPanel.add(serverLabel);
    
    JLabel portLabel = new JLabel("Port:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, portLabel, 0, SpringLayout.NORTH, serverLabel);
//    sl_topPanel.putConstraint(SpringLayout.EAST, portLabel, 126, SpringLayout.WEST, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.EAST, serverText);
    serverPanel.add(portLabel);
    
    JLabel labIdLabel = new JLabel("Lab Id:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, labIdLabel, 6, SpringLayout.SOUTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, labIdLabel, 0, SpringLayout.EAST, serverLabel);
    serverPanel.add(labIdLabel);
    
    JLabel labDescriptionLabel = new JLabel("Lab Description:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, labDescriptionLabel, 6, SpringLayout.SOUTH, labIdLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, labDescriptionLabel, 0, SpringLayout.EAST, labIdLabel);
    serverPanel.add(labDescriptionLabel);

    
    sl_topPanel.putConstraint(SpringLayout.NORTH, serverText, -2, SpringLayout.NORTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 6, SpringLayout.EAST, serverLabel);
    serverPanel.add(serverText);
    serverText.setColumns(10);

    sl_topPanel.putConstraint(SpringLayout.NORTH, portText, -2, SpringLayout.NORTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, portText, 6, SpringLayout.EAST, portLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, portText, -6, SpringLayout.EAST, serverPanel);
    serverPanel.add(portText);
    portText.setColumns(10);

    sl_topPanel.putConstraint(SpringLayout.NORTH, labIdText, 4, SpringLayout.SOUTH, serverText);
    sl_topPanel.putConstraint(SpringLayout.WEST, labIdText, 6, SpringLayout.EAST, labIdLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, labIdText, -6, SpringLayout.EAST, serverPanel);
    serverPanel.add(labIdText);
    labIdText.setColumns(10);
 
    sl_topPanel.putConstraint(SpringLayout.NORTH, labDescriptionText, 4, SpringLayout.SOUTH, labIdText);
    sl_topPanel.putConstraint(SpringLayout.WEST, labDescriptionText, 6, SpringLayout.EAST, labDescriptionLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, labDescriptionText, -6, SpringLayout.EAST, serverPanel);    
    serverPanel.add(labDescriptionText);
    
    //JPanel buttonPane = new JPanel();
    ButtonGroup thisRemoteGroup = new ButtonGroup();
    JRadioButton btnThis = new JRadioButton("Use this model");btnThis.setSelected(true);
    JRadioButton btnRemote = new JRadioButton("Use remote model");
    thisRemoteGroup.add(btnThis);
    thisRemoteGroup.add(btnRemote);

    
    JButton testButton = new JButton("Get metadata");
    sl_topPanel.putConstraint(SpringLayout.NORTH, testButton, 6, SpringLayout.SOUTH, labDescriptionText);
    sl_topPanel.putConstraint(SpringLayout.EAST, testButton, 0, SpringLayout.EAST, serverPanel);
    
    sl_topPanel.putConstraint(SpringLayout.NORTH, btnThis, 6, SpringLayout.SOUTH, labDescriptionText);
    sl_topPanel.putConstraint(SpringLayout.NORTH, btnRemote, 6, SpringLayout.SOUTH, labDescriptionText);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverPanel, 0, SpringLayout.WEST, btnRemote);
    sl_topPanel.putConstraint( SpringLayout.WEST, btnThis, 0, SpringLayout.EAST, btnRemote);
    
    
    methodsTable = new JTable(methodsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };      
    methodsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    
    JScrollPane methodsScrollPane = new JScrollPane(methodsTable);
    sl_topPanel.putConstraint(SpringLayout.NORTH, methodsScrollPane, 4, SpringLayout.SOUTH, testButton);
    sl_topPanel.putConstraint(SpringLayout.SOUTH, methodsScrollPane, 4, SpringLayout.SOUTH, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.EAST, methodsScrollPane, 4, SpringLayout.EAST, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, methodsScrollPane, 4, SpringLayout.WEST, serverPanel);
    
    serverPanel.add(methodsScrollPane);
    
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++    
    AbstractAction testServer = new AbstractAction("Get metadata"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        getServerInfo();
        addVariables(collection.getEJS());
      }
    };
    testButton.setAction(testServer);

    AbstractAction selectOwnModel = new AbstractAction("SelectLocalModel"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        Osejs ejs = collection.getEJS();
        File wrapperJsFile = new File(ejs.getBinDirectory(), "javascript/model_elements/SoftwareLinks/"+"wrapperSD.js");
        File wrapperDefault = new File(ejs.getBinDirectory(), "javascript/model_elements/SoftwareLinks/"+"wrapperSD_Default.js");
        //Charset charset = OsejsCommon.charsetOfFile(wrapperJsFile);
        FileUtils.copy(wrapperDefault, wrapperJsFile);

        selectedLocalModel = true;
        //wrapperPanel.setVisible(false);
        //serverVariablesPanel.setVisible(false);
        //linksPanel.setVisible(false);
        //mainPanel.getComponent(1).setVisible(false);
        //mainPanel.getComponent(2).setVisible(false);
        //mainPanel.getComponent(3).setVisible(false);
        //mainPanel.getComponent(4).setVisible(false);
        while(mainPanel.getTabCount()>1) mainPanel.remove(1);
      }
    };
    btnThis.setAction(selectOwnModel);
    
    AbstractAction selectRemoteModel = new AbstractAction("SelectRemoteModel"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        selectedLocalModel = false;
        //wrapperPanel.setVisible(true);
        //serverVariablesPanel.setVisible(true);
        //linksPanel.setVisible(true);
        //mainPanel.getComponent(1).setVisible(true);
        //mainPanel.getComponent(2).setVisible(true);
        //mainPanel.getComponent(3).setVisible(true);
        //mainPanel.getComponent(4).setVisible(true);
        mainPanel.addTab("Reported Variables", serverVariablesPanel);
        mainPanel.addTab("Linked Variables", linksPanel);
        mainPanel.addTab("Wrapper", wrapperPanel);
      }
    };
    btnRemote.setAction(selectRemoteModel);
    
    serverPanel.add(btnThis);
    serverPanel.add(btnRemote);   
    serverPanel.add(testButton);
    return serverPanel;
  }

  private boolean addVariables(Osejs ejs){
    VariablesEditor varEd = ejs.getModelEditor().getVariablesEditor();
    Vector<Editor> lista = varEd.getPages();
    hojaSD = new TableOfVariablesEditor(ejs);
    boolean needToOverwrite = false;
    int whichToOverwrite = -1;
    for(int i = 0; i<lista.size(); i++){
      TableOfVariablesEditor hoja = (TableOfVariablesEditor) lista.get(i);
      if(hoja.getName().contains("AUTO_SmartDevice")){
        needToOverwrite = true;
        whichToOverwrite = i;
      }
    }
    hojaSD.setName("AUTO_SmartDevice");
    //nuevaHoja.readString("<Variable><Name><![CDATA[s]]></Name><Value><![CDATA[23]]></Value><Type><![CDATA[double]]></Type><Dimension><![CDATA[]]></Dimension><Domain><![CDATA[public]]></Domain><Comment><![CDATA[]]></Comment></Variable>");
    //System.out.println("readable : " + readable.toString());
    for(int i = 0; i<readable.size();i++){
      createXMLVariable(readable.getJsonObject(i));
    }
    //System.out.println("writable : " + writable.toString());
    for(int i = 0; i<writable.size();i++){
      createXMLVariable(writable.getJsonObject(i));
    }
    if(needToOverwrite){
      lista.set(whichToOverwrite, hojaSD);
    }else{
      lista.add(hojaSD);
    }
    //varEd.updateControlValues(false);
    ejs.getModelEditor().setChanged(true);
    Object[] options = new Object[] {"Ok"};//,"No"};
    String message = "The element has Auto-generated methods and variables to use ";//, you want to reload?";
    JOptionPane.showOptionDialog(mainPanel, message, " SmartDeviceLinker Warning ", 
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
    //if(answer == 0){
      //ejs.checkChangesAndContinue(false);
      //ejs.readFile(ejs.getCurrentXMLFile(), false);
    //}
    return true;
  }
  
  
  private boolean getServerInfo() {
      try {
        remoteServer = new URI("ws://"+serverText.getText()+":"+portText.getText());
      } catch (URISyntaxException e1) {
        System.err.println("BAD URI, check server and port fields");
        e1.printStackTrace();
      }
      WebSocketClient mCommSocket = new WebSocketClient(remoteServer){

        @Override
        public void onOpen(ServerHandshake handshakedata) {
          System.out.println("Connected to "+ remoteServer.toString());
          send("{\"method\" : \"getMetadata\"}");
          send("getMetadata");
          send("{\"method\" : \"getSensorMetadata\"}");
          send("getSensorMetadata");
          send("{\"method\" : \"getActuatorMetadata\"}");
          send("getActuatorMetadata");
          send("{\"method\" : \"getSensorActuatorMetadata\"}");
          //send("getSensorActuatorMetadata");
        }

        @Override
        public void onMessage(String message) {
          //System.out.println("Receiving : "+ message + " from : " +remoteServer.toString());
          
          
          // TODO not handled messages
          if (message.substring(0, 1).equals("{")){
            JsonReader jsonReader = Json.createReader(new StringReader(message));
            JsonObject msgLab = jsonReader.readObject();
            jsonReader.close();
            if (msgLab.containsKey("apiVersion")){
              
              meta = new MetadataBuilder();
              meta.load(msgLab);
              processMetadata();
            }else if(msgLab.containsKey("method")){
              String methodName = msgLab.getString("method");
              if(methodName.contains("Metadata")){
                if(methodName.contains("Actions"))  processMetadataActions(msgLab);
                else{
                  //wrapperText.setText(wrapperText.getText() +"\n"+ methodName);
                  if(methodName.contains("Sensor"))  readable = Json.createArrayBuilder().build();
                  if(methodName.contains("Actuator"))  writable = Json.createArrayBuilder().build();
                  processMetadataVariables(msgLab);
                }
              }else{
                System.out.println("Not handling the method" + methodName);
              }
            }
          }
        }
        @Override
        public void onClose(int code, String reason, boolean remote) {
          System.out.println("Disconnected from "+ remoteServer.toString());
        }

        @Override
        public void onError(Exception ex) {
          ex.printStackTrace();
          System.out.println("Connection error with "+ remoteServer.toString());
        }
        
      };
      mCommSocket.connect();
    return false;
  }
  
  private boolean processMetadata(){
    //System.out.println("ProcessMetadata method, metadata is " + meta.toString());
    if(meta!= null) {
      //configuration.setMetadata(meta);
      //JsonObject fullApi = meta.getJSON();
      JsonObject info = meta.getInfo();
      labIdText.setText(info.getString("title"));
      labDescriptionText.setText(info.getString("description"));          
      methodsTableModel.setDataVector(new Object[][]{}, new Object[]{"Name", "Params", "Returns", "Purpose"});
      showCodeTab = false;        
      JsonArray apis = meta.getApis();
      //wrapperText.setText(apis.toString());
      JsonArrayBuilder opMethods = Json.createArrayBuilder();
      JsonArray apiOps; 
      for(int i = 0;i<apis.size();i++){
        JsonObject api = apis.getJsonObject(i);
        apiOps = api.getJsonArray("operations");
        for (int j = 0; j<apiOps.size();j++){
          opMethods.add(apiOps.getJsonObject(j));
        }
      }
      JsonArray methods = opMethods.build();
      for(int i = 0; i<methods.size(); i++) {
        JsonObject method = methods.getJsonObject(i);
        String name = method.getString("nickname");
        /*if(name.equals("step") || name.equals("eval")) {
          showCodeTab = true;
        }*/
        Object[] row = { name, method.getJsonArray("parameters").getJsonObject(0).getString("type").toString(), method.getString("type"), method.getString("summary")};
        methodsTableModel.addRow(row);
      }
      if(codePanel != null) {
        int tabs = mainPanel.getTabCount();
        if(showCodeTab && tabs == 3) {
          mainPanel.addTab("Code", codePanel);
        }
        if(!showCodeTab && tabs == 4) {
          mainPanel.remove(3);
        }
      }
      /*Vector<String> columns = new Vector<String>();
      columns.add("Name"); columns.add("Type");
      Vector<Vector<String>> controls = mapToVector(meta.getWritable());
      Vector<Vector<String>> indicators = mapToVector(meta.getReadable());
      controlsTableModel.setDataVector(controls, columns);
      indicatorsTableModel.setDataVector(indicators, columns);*/
      return true;
    }
    return false;
  }

  private boolean processMetadataActions(JsonObject input){
    // TODO
    return false;
  }
  
  
  
  private boolean processMetadataVariables(JsonObject input){
    String methodName = input.getString("method");
    Vector<String> columns = new Vector<String>();
    columns.add("Name"); columns.add("Type");
    JsonArrayBuilder tempVars = Json.createArrayBuilder();
    JsonArray inputVars = Json.createArrayBuilder().build();
    //System.out.println("Metadata Vars : " + input);
    if(methodName.equals("getSensorMetadata")){
      //wrapperText.setText(wrapperText.getText() +"\n"+ input.toString());
      //readable = input.getJsonArray("sensors");
      inputVars = input.getJsonArray("sensors");
      for(int i = 0;i<inputVars.size();i++) tempVars.add(inputVars.getJsonObject(i));
      for(int i = 0;i<readable.size();i++)  tempVars.add(readable.getJsonObject(i));
      readable = tempVars.build();
      Vector<Vector<String>> controls = jsonToVector(readable);
      controlsTableModel.setDataVector(controls, columns);
      //System.out.println("Metadata readable1 : " + readable);
    }else if(methodName.equals("getActuatorMetadata")){
      //wrapperText.setText(wrapperText.getText() +"\n"+ input.toString());
      //writable = input.getJsonArray("actuators");
      //writable.add(input.getJsonArray("actuators"));
      inputVars = input.getJsonArray("actuators");
      for(int i = 0;i<inputVars.size();i++) tempVars.add(inputVars.getJsonObject(i));
      for(int i = 0;i<writable.size();i++)  tempVars.add(writable.getJsonObject(i));
      //tempVars.add(inputVars);
      //tempVars.add(writable);
      writable = tempVars.build();
      Vector<Vector<String>> indicators = jsonToVector(writable);
      indicatorsTableModel.setDataVector(indicators, columns);
      //System.out.println("Metadata readable2 : " + readable);
    }else if(methodName.equals("getSensorActuatorMetadata")){
      //wrapperText.setText(wrapperText.getText() +"\n"+ input.toString());
      //readable.add(input.getJsonArray("sensorActuators"));
      //readable = input.getJsonArray("sensorActuators");
      //writable.add(input.getJsonArray("sensorActuators"));
      inputVars = input.getJsonArray("actuators");
      for(int i = 0;i<inputVars.size();i++) tempVars.add(inputVars.getJsonObject(i));
      for(int i = 0;i<writable.size();i++)  tempVars.add(writable.getJsonObject(i));
      //tempVars.add(inputVars);
      //tempVars.add(writable);
      writable = tempVars.build();
      
      tempVars = Json.createArrayBuilder();
      for(int i = 0;i<inputVars.size();i++) tempVars.add(inputVars.getJsonObject(i));
      for(int i = 0;i<readable.size();i++)  tempVars.add(readable.getJsonObject(i));
      //tempVars.add(inputVars);
      //tempVars.add(readable);
      readable = tempVars.build();
      
      Vector<Vector<String>> controls = jsonToVector(readable);
      controlsTableModel.setDataVector(controls, columns);
      Vector<Vector<String>> indicators = jsonToVector(writable);
      indicatorsTableModel.setDataVector(indicators, columns);
      //System.out.println("Metadata readable : " + readable);
    }else{
      System.out.println("Mehtod : " + methodName + " Not handled by this parser");
    }
    //System.out.println("Metadata readable3 : " + readable);
    return false;
  }
  
  private JPanel createServerVariablesPanel() {
    JPanel ripPanel = new JPanel();
    ripPanel.setLayout(new BoxLayout(ripPanel, BoxLayout.X_AXIS));
    ripPanel.setBorder(new TitledBorder(null, "Server writable and readable variables", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    ripPanel.setMinimumSize(new Dimension(620, 180));   
    ripPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    ripPanel.setPreferredSize(new Dimension(620, 180));

    controlsTable = new JTable(controlsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };      
    controlsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JScrollPane controlsScrollPane = new JScrollPane(controlsTable); //an scroll panel for the table
    ripPanel.add(controlsScrollPane);
    controlsScrollPane.setBorder(BorderFactory.createTitledBorder("Server Writable Variables"));
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++    
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
        String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(editor, "Variables", "", linksTable, varname, ""); 
        if (variable != null) {
          Vector<Object> row = new Vector<>();
          row.add(varname);
          row.add(variable + " : " + vartype);
          row.add(false);
          row.add(true);
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
    
    indicatorsTable = new JTable(indicatorsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    indicatorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane indicatorsScrollPane = new JScrollPane(indicatorsTable); //an scroll panel for the table
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
        String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(editor, "Variables", "", linksTable, varname, "");
        if (variable != null) {
          Vector<Object> row = new Vector<>();
          row.add(varname);
          row.add(variable + " : " + vartype);
          row.add(true);
          row.add(false);
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

    return ripPanel;
  }
  
  private JPanel createCodePanel() {
    JPanel topEvaluablePanel = new JPanel();
    topEvaluablePanel.setBorder(new TitledBorder(null, "Functions to evaluate in the server ", TitledBorder.LEADING, TitledBorder.TOP));
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

//    JLabel callbackLabel = new JLabel("Action after Evaluation (Callback):");
//    sl_topEvaluable.putConstraint(SpringLayout.NORTH, callbackLabel, 7, SpringLayout.NORTH, topEvaluablePanel);
//    sl_topEvaluable.putConstraint(SpringLayout.WEST, callbackLabel, 7, SpringLayout.EAST, evalLabel);
//    topEvaluablePanel.add(callbackLabel);
//      
//    sl_topEvaluable.putConstraint(SpringLayout.NORTH, callbackText, 4, SpringLayout.SOUTH, callbackLabel);
//    sl_topEvaluable.putConstraint(SpringLayout.WEST, callbackText, 24, SpringLayout.EAST, evaluableText);
//    topEvaluablePanel.add(callbackText);
//    callbackText.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      
      
    JLabel initialCodeLabel = new JLabel("Initial Code:");
    sl_topEvaluable.putConstraint(SpringLayout.NORTH, initialCodeLabel, 7, SpringLayout.NORTH, topEvaluablePanel);
    sl_topEvaluable.putConstraint(SpringLayout.WEST, initialCodeLabel, 87, SpringLayout.EAST, evalLabel);
    topEvaluablePanel.add(initialCodeLabel);
    sl_topEvaluable.putConstraint(SpringLayout.SOUTH, topEvaluablePanel, 4, SpringLayout.SOUTH, initialCodeText);
    sl_topEvaluable.putConstraint(SpringLayout.NORTH, initialCodeText, 4, SpringLayout.SOUTH, initialCodeLabel);
    sl_topEvaluable.putConstraint(SpringLayout.WEST, initialCodeText, 10, SpringLayout.EAST, evaluableText);
    sl_topEvaluable.putConstraint(SpringLayout.EAST, initialCodeText, -10, SpringLayout.EAST, topEvaluablePanel);
    topEvaluablePanel.add(initialCodeText);
//    initialCodeText.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    //callbackText.setColumns(15);
    //callbackText.setSize(15,50);

    sl_topEvaluable.putConstraint(SpringLayout.NORTH, portText, -2, SpringLayout.NORTH, evalLabel);
    
    //topEvaluablePanel.setVisible(showCodeTab);
    return topEvaluablePanel;
  }

  private JPanel createWrapperPanel(final ModelElementsCollection collection){
    SpringLayout sl_wrapper = new SpringLayout();
    JPanel auxPanel = new JPanel();
    auxPanel.setLayout(sl_wrapper);
    auxPanel.setBorder(BorderFactory.createTitledBorder("Client Wrapper"));
    JButton saveButton = new JButton("Save Wrapper");
    AbstractAction saveWrapper = new AbstractAction("Save Wrapper"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        Osejs ejs = collection.getEJS();
        File wrapperJsFile = new File(ejs.getBinDirectory(), "javascript/model_elements/SoftwareLinks/"+"wrapperSD.js");
        Charset charset = OsejsCommon.charsetOfFile(wrapperJsFile);
        try {
          FileUtils.saveToFile(wrapperJsFile, charset, wrapperText.getText());
          System.out.println("File saved -> " + wrapperJsFile.getPath().toString() + "/" + wrapperJsFile.getName());
        } catch (IOException e1) {
          System.err.println("Trying to save file: " + wrapperJsFile.getPath().toString() + 
              "/" + wrapperJsFile.getName() + " with Charset: " + charset + " and Text : " + wrapperText.getText());
          e1.printStackTrace();
        }
      }
    };
    JButton loadButton = new JButton("Load Wrapper");
    AbstractAction loadWrapper = new AbstractAction("Load Wrapper"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        Osejs ejs = collection.getEJS();
        JFileChooser chooser = new JFileChooser(ejs.getBinDirectory()+"javascript/model_elements/SoftwareLinks/");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Javascript Files", "js");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(parent);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           System.out.println("You chose to open this file: " +
                chooser.getSelectedFile().getName());
        }

      }
    };
    loadButton.setAction(loadWrapper);
    saveButton.setAction(saveWrapper);
    wrapperText.setText("\n"
        +"function wrapper(ws,data){}\n"
        +"wrapper.prototype = {},\n"
        +"  post: function(method, params) {},\n"
        +"  sync: function(callback) {},\n"
        +"  connect: function(callback) {},\n"
        +"  disconnect: function(callback) {},\n"
        +"  get: function(vars, eachStep, callback) {},\n"
        +"  set: function(vars, values, auth, callback) {},\n"
        +"  callAction: function(nickname, params, callback){   },\n"
        +"  extract: function(strMsg, callback){},\n"
        +"  eval: function(code, callback) {},\n"
        +"  open: function(model, callback) {},\n"
        +"  step: function(vars, callback) {},\n"
        +"  toEjssReadable: function(method,dataExtracted){}\n"
        +"}\n"
        );
    wrapperText.setMinimumSize(new Dimension(620, 180));   
    wrapperText.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    JScrollPane wrapperScroll = new JScrollPane(wrapperText); 
    wrapperScroll.setMinimumSize(new Dimension(620, 180));
    wrapperScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    wrapperScroll.setPreferredSize(new Dimension(620, 180));
    wrapperScroll.setBorder(BorderFactory.createLoweredBevelBorder());
    sl_wrapper.putConstraint(SpringLayout.NORTH, auxPanel, 4, SpringLayout.NORTH, saveButton);
    sl_wrapper.putConstraint(SpringLayout.NORTH, wrapperScroll, 4,SpringLayout.SOUTH, saveButton );
    sl_wrapper.putConstraint(SpringLayout.NORTH, auxPanel, 4, SpringLayout.NORTH, loadButton);
    sl_wrapper.putConstraint(SpringLayout.WEST, loadButton, 4, SpringLayout.EAST, saveButton);
    sl_wrapper.putConstraint(SpringLayout.NORTH, wrapperScroll, 4,SpringLayout.SOUTH, loadButton );
    sl_wrapper.putConstraint(SpringLayout.SOUTH, auxPanel, 4, SpringLayout.SOUTH, wrapperScroll);
    sl_wrapper.putConstraint(SpringLayout.EAST, auxPanel, 4, SpringLayout.EAST, wrapperScroll);
    
    auxPanel.add(loadButton);
    auxPanel.add(saveButton);
    auxPanel.add(wrapperScroll);
    return auxPanel;//wrapperPanel;
  }
  
  private JScrollPane createLinksPanel() {
    linksTable = new JTable(linksTableModel);
    linksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    linksTable.getModel().addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        if (TableModelEvent.UPDATE == e.getType() && linksTable.getRowCount() == e.getLastRow()+1) {
          Object[] emptyRow = new Object[]{"", "", new Boolean(false), new Boolean(false)};
          linksTableModel.addRow(emptyRow);
        }
      }
    });
    AbstractAction connectVariable = new AbstractAction("Connect variable"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        // Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"
        String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(editor, "Variables", null, linksTable, "", "");
        if (variable != null) {
            int row = linksTable.getSelectedRow(), column = 1;
          linksTableModel.setValueAt(variable, row, column);
        }
      }
    };
    AbstractAction deleteRow = new AbstractAction("Delete link"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
          int row = linksTable.getSelectedRow();
        linksTableModel.removeRow(row);
      }
    };
    popupMenuEJS = new JPopupMenu();
    popupMenuEJS.add(connectVariable);
    popupMenuEJS.add(deleteRow);
    linksTable.addMouseListener (new MouseAdapter() {       
      public void mousePressed (MouseEvent _evt) {
        if (OSPRuntime.isPopupTrigger(_evt) && linksTable.isEnabled ()) {
          int row = linksTable.rowAtPoint(_evt.getPoint()); 
          if(row != -1) { 
            linksTable.setRowSelectionInterval(row, row);
          }
          popupMenuEJS.show(_evt.getComponent(), _evt.getX(), _evt.getY());
        }
      }
    });    

    JScrollPane linksScrollPane = new JScrollPane(linksTable); //an scroll panel for the table
    linksScrollPane.setBorder(BorderFactory.createTitledBorder("Table of Linked Variables"));
    linksScrollPane.setMinimumSize(new Dimension(620, 180));
    linksScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    linksScrollPane.setPreferredSize(new Dimension(620, 180));

    TableModelListener ta = new TableModelListener(){ //Add a row to the table when last row is edited
      public void tableChanged(TableModelEvent e){
        if (TableModelEvent.UPDATE == e.getType() && linksTable.getRowCount() == e.getLastRow()+1){
          Object[] emptyRow = new Object[]{"", "", new Boolean(false), new Boolean(false)};
          linksTableModel.addRow(emptyRow);
        }       
      }       
    };
    linksTable.getModel().addTableModelListener(ta);

    return linksScrollPane;
  }
  
  public String getWrapperCode(){
    return this.wrapperText.getText();
  }
  
 /* private Vector<Vector<String>> mapToVector(Map<String, String> map) {
    Iterator<Map.Entry<String,String>> iter = map.entrySet().iterator();
    Vector<Vector<String>> result = new Vector<Vector<String>>();      
    while(iter.hasNext()) {
      Map.Entry<String,String> entry = iter.next();
      Vector<String> row = new Vector<String>();
      row.add(entry.getKey());
      row.add(entry.getValue());
      result.add(row);
    }
    return result;
  }*/
  private Vector<Vector<String>> jsonToVector(JsonArray map) {
    Vector<Vector<String>> result = new Vector<Vector<String>>();      
    for(int i = 0; i<map.size(); i++) {
      JsonObject entry = map.getJsonObject(i);
      Vector<String> row = new Vector<String>();
      //row.add(entry.getString("fullName"));
      JsonArray valuesJson = entry.getJsonArray("values");
      for(int j = 0; j<valuesJson.size();j++){
        row.add(valuesJson.getJsonObject(j).getString("name"));
        row.add(valuesJson.getJsonObject(j).getString("type"));
      }
      result.add(row);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public String savetoXML() {
    String server = serverText.getText().trim();
    String port = portText.getText().trim();
    String init = initialCodeText.getText();
    String step = evaluableText.getText();
    configuration.setInitCode((init != null) ? init : "");
    configuration.setStepCode((step != null) ? step : "");
    configuration.setServer(server, port);
    configuration.setData(linksTableModel.getDataVector());
    return configuration.dump();
  }

  /** 
   * Restores the states from an XML String
   */
  public void readfromXML(String inputXML) {
    configuration.restore(inputXML);
    serverText.setText(configuration.getServer());
    portText.setText(configuration.getPort());
    evaluableText.setText(configuration.getStepCode());
    initialCodeText.setText(configuration.getInitCode());
    linksTableModel.setDataVector(configuration.getData());
  }
  
  private boolean createXMLVariable(JsonObject variable){
    String name = "", type = "", initValue = "", domain = "", dimension = "", comment= "";
    if(variable.containsKey("fullName") && variable.containsKey("values")){
      JsonArray valuesJson = variable.getJsonArray("values");
      for(int j = 0; j<valuesJson.size();j++){
        name = valuesJson.getJsonObject(j).getString("name");
        type = valuesJson.getJsonObject(j).getString("type");
        if(type.equals("array")){
          dimension = "[10]";
          System.out.println("Warning: You must edit the dimension of the variable: " + variable + ", because "
              + "is not defined");
        }
        if(variable.containsKey("initValue")) initValue = variable.getString("initValue");
        if(variable.containsKey("sensorId"))    domain = "input";
        else if(variable.containsKey("actuatorId"))    domain = "output";
        else if(variable.containsKey("sensorActuatorId"))    domain = "public";
        if(variable.containsKey("comment"))   comment = variable.getString("comment");
        hojaSD.readString(createXMLVariable(name,type,initValue,domain,dimension,comment));
        System.out.println("createXMLVariable : " + createXMLVariable(name,type,initValue,domain,dimension,comment));
      }
      return true;
    }else{
      return false;
    }
  }
  
  private String createXMLVariable(String name, String type, String initValue, String domain, String dimension, String comment){
    if(type.equals("string")) type = "String";
    return "<Variable>"
        + "<Name><![CDATA["+ name +"]]></Name>"
        + "<Value><![CDATA["+ initValue +"]]></Value>"
        + "<Type><![CDATA["+ type +"]]></Type>"
        + "<Dimension><![CDATA["+ dimension +"]]></Dimension>"
        + "<Domain><![CDATA["+ domain +"]]></Domain>"
        + "<Comment><![CDATA["+ comment +"]]></Comment></Variable>";
    
  }
	
}