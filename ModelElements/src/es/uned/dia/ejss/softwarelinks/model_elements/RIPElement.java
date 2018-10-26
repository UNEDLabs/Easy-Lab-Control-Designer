package es.uned.dia.ejss.softwarelinks.model_elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URLEncoder;
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
public class RIPElement extends AbstractModelElement {
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
  private DefaultTableModel serverMethodsTableModel;
  private DefaultTableModel methodsTableModel;
  private DefaultTableModel indicatorsTableModel;
  private DefaultTableModel controlsTableModel;
  private JTextField serverText = new JTextField("localhost", 20);
  private JTextField expIdText = new JTextField("", 6);
  private JTextField evaluableText = new JTextField("", 50);
  private JComboBox labIdText = new JComboBox();
  private JTextField labDescriptionText = new JTextField();
  private JTextArea callbackText = new JTextArea("", 4 , 23);
  private JTextArea initialCodeText = new JTextArea("", 4 , 23);
  //private JTextField viFileText = new JTextField("ripTest.vi", 32);  
   
  private JPopupMenu popupMenuEJS;  
   
  private JTable serverMethodsTable;
  private JTable methodsTable;
  private JTable controlsTable;
  private JTable indicatorsTable;
  private JTable linksTable;
  private JPopupMenu popupMenuInd;
  private JPopupMenu popupMenuCon;
  
   // used to test the connection with the server
  RIPClient ripTest = null;
  private Component parent;
  private ModelEditor editor;
  private JPanel codePanel;
  private boolean showCodeTab;
  private JTabbedPane mainPanel;
  private JComboBox protocol;
  
  public RIPElement() { 
    protocol = new JComboBox(new Object[] {"Guess", "RIP-Arduino", "RIP-LabVIEW"});
    // Server writable Table
    serverMethodsTableModel = new DefaultTableModel();
    serverMethodsTableModel.addColumn("Url");
    serverMethodsTableModel.addColumn("Type");
    serverMethodsTableModel.addColumn("Description");
    serverMethodsTableModel.addColumn("Params");
    serverMethodsTableModel.addColumn("Return");
    serverMethodsTableModel.addColumn("Example");

    // Server writable Table
    methodsTableModel = new DefaultTableModel();
    methodsTableModel.addColumn("Name");
    methodsTableModel.addColumn("Params");
    methodsTableModel.addColumn("Description");
          
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
    RIPCodeBuilder builder = new RIPCodeBuilder();
    
    builder.setName(name);
    builder.setModel(configuration);
    String code = builder.getCode();
    return code;
  }  

  
  public String getImportStatements() { // Required for Lint
    return "SoftwareLinks/RIP.js"; 
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access to remote hardware using the Remote Interoperability Protocol";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "es/uned/dia/ejss/softwarelinks/resources/rip.html"; 
  }
  
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    return createJilEditor(name, parentComponent, collection);
  }
  
  protected Component createJilEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    parent = parentComponent;
    editor = collection.getEJS().getModelEditor();

    mainPanel = new JTabbedPane();
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    mainPanel.setPreferredSize(new Dimension(600,300));

    JPanel serverPanel = createServerPanel(); 
    JPanel experiencePanel = createExperiencePanel(); 
    JPanel serverVariablesPanel = createVariablesPanel();
    codePanel = createCodePanel();
    mainPanel.addTab("Server Configuration", serverPanel);
    mainPanel.addTab("Experience", experiencePanel);
    mainPanel.addTab("Auto Update", serverVariablesPanel);

    return mainPanel;
  }
 
  private JPanel createServerPanel() {
    JPanel serverPanel = new JPanel();
    serverPanel.setBorder(new TitledBorder(null, "RIP server configuration", TitledBorder.LEADING, TitledBorder.TOP));
    serverPanel.setMinimumSize(new Dimension(620, 180));
    serverPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
    serverPanel.setPreferredSize(new Dimension(620, 180));
    SpringLayout sl_topPanel = new SpringLayout();
    serverPanel.setLayout(sl_topPanel);
    
    JLabel serverLabel = new JLabel("Server url:");
    JButton testButton = new JButton("Get metadata");
    serverText.setColumns(14);

    sl_topPanel.putConstraint(SpringLayout.NORTH, serverLabel, 7, SpringLayout.NORTH, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverLabel, 7, SpringLayout.WEST, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 0, SpringLayout.EAST, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, testButton, 6, SpringLayout.EAST, serverText);
    sl_topPanel.putConstraint(SpringLayout.EAST, testButton, 6, SpringLayout.EAST, serverPanel);
  
    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, serverText, 0, SpringLayout.VERTICAL_CENTER, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, testButton, 0, SpringLayout.VERTICAL_CENTER, serverText);

    serverPanel.add(serverLabel);
    serverPanel.add(serverText);
    serverPanel.add(testButton);

    AbstractAction testServer = new AbstractAction("Get Experiences") {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        boolean serverResponds = false;
        try {
          serverResponds = loadServerInfo();
        } catch(Exception e1) {
          if(serverResponds) {
            System.err.println("[WARNING] Can't parse server info.");
          } else {
            System.err.println("[WARNING] Server not responding.");
          }
        }
//        if(serverResponds) {
//          RIPMetadata meta = configuration.getMetadata();
//          System.out.println(meta.toString());
//          JOptionPane.showMessageDialog(parent, CONNECTION_OK + ((info != null) ? info.getName() : ""));
//        } else {
//          JOptionPane.showMessageDialog(parent, SERVER_KO);
//        }
      }
    };
    testButton.setAction(testServer);
    
    serverMethodsTable = new JTable(serverMethodsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };      
    serverMethodsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    
    JScrollPane methodsScrollPane = new JScrollPane(serverMethodsTable);
    sl_topPanel.putConstraint(SpringLayout.NORTH, methodsScrollPane, 4, SpringLayout.SOUTH, testButton);
    sl_topPanel.putConstraint(SpringLayout.SOUTH, methodsScrollPane, 4, SpringLayout.SOUTH, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.EAST, methodsScrollPane, 4, SpringLayout.EAST, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, methodsScrollPane, 4, SpringLayout.WEST, serverPanel);

    serverPanel.add(methodsScrollPane);
    
    return serverPanel;  
  }
  
  private JPanel createExperiencePanel() {
    JPanel experiencePanel = new JPanel();
    experiencePanel.setBorder(new TitledBorder(null, "RIP server configuration", TitledBorder.LEADING, TitledBorder.TOP));
    experiencePanel.setMinimumSize(new Dimension(620, 180));
    experiencePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
    experiencePanel.setPreferredSize(new Dimension(620, 180));
    SpringLayout sl_topPanel = new SpringLayout();
    experiencePanel.setLayout(sl_topPanel);

    JLabel expIdLabel = new JLabel("Choose an Experience:");
    //JLabel labIdLabel = new JLabel("Lab Id:");
    expIdText.setColumns(25);
    expIdText.setEditable(false);
    experiencePanel.add(expIdLabel);
//    experiencePanel.add(expIdText);
//    experiencePanel.add(labIdLabel);
    experiencePanel.add(labIdText);
    
    sl_topPanel.putConstraint(SpringLayout.NORTH, expIdLabel, 7, SpringLayout.NORTH, experiencePanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, labIdText, 10, SpringLayout.EAST, expIdLabel);

//    sl_topPanel.putConstraint(SpringLayout.WEST, labIdLabel, 0, SpringLayout.EAST, serverText);
//    sl_topPanel.putConstraint(SpringLayout.WEST, labIdText, 0, SpringLayout.EAST, labIdLabel);
//    sl_topPanel.putConstraint(SpringLayout.EAST, labIdText, 0, SpringLayout.EAST, experiencePanel);

//    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, labIdLabel, 0, SpringLayout.VERTICAL_CENTER, serverText);
//    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, labIdText, 0, SpringLayout.VERTICAL_CENTER, labIdLabel);

    //    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, expIdText, 0, SpringLayout.VERTICAL_CENTER, expIdLabel);
//  sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, portLabel, 0, SpringLayout.VERTICAL_CENTER, serverText);
//  sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, portText, 0, SpringLayout.VERTICAL_CENTER, portLabel);
//  sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, labIdLabel, 0, SpringLayout.VERTICAL_CENTER, portText);

    
    JLabel labDescriptionLabel = new JLabel("Lab Description:");
    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, labDescriptionText, 0, SpringLayout.VERTICAL_CENTER, labDescriptionLabel);
    sl_topPanel.putConstraint(SpringLayout.NORTH, labDescriptionLabel, 15, SpringLayout.SOUTH, expIdLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, labDescriptionText, 0, SpringLayout.EAST, labDescriptionLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, labDescriptionText, 0, SpringLayout.EAST, experiencePanel);
    experiencePanel.add(labDescriptionLabel);
    experiencePanel.add(labDescriptionText);
    labDescriptionText.setColumns(25);

//    JLabel protocolLabel = new JLabel("RIP Subprotocol:");
//    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, protocolLabel, 0, SpringLayout.VERTICAL_CENTER, labDescriptionLabel);
//    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, protocol, 0, SpringLayout.VERTICAL_CENTER, protocolLabel);
//    sl_topPanel.putConstraint(SpringLayout.WEST, protocolLabel, 0, SpringLayout.EAST, labDescriptionText);
//    sl_topPanel.putConstraint(SpringLayout.WEST, protocol, 0, SpringLayout.EAST, protocolLabel);
//    sl_topPanel.putConstraint(SpringLayout.EAST, protocol, 0, SpringLayout.EAST, serverPanel);
//    serverPanel.add(protocolLabel);
//    serverPanel.add(protocol);
//  
    methodsTable = new JTable(methodsTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };      
    methodsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    
    JScrollPane methodsScrollPane = new JScrollPane(methodsTable);
    sl_topPanel.putConstraint(SpringLayout.NORTH, methodsScrollPane, 4, SpringLayout.SOUTH, labDescriptionText);
    sl_topPanel.putConstraint(SpringLayout.SOUTH, methodsScrollPane, 4, SpringLayout.SOUTH, experiencePanel);
    sl_topPanel.putConstraint(SpringLayout.EAST, methodsScrollPane, 4, SpringLayout.EAST, experiencePanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, methodsScrollPane, 4, SpringLayout.WEST, experiencePanel);
    
    experiencePanel.add(methodsScrollPane);
    
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++    
    AbstractAction getExpInfo = new AbstractAction("Get Experience Info") {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        try {
          loadExperience();
        } catch(Exception e1) {
        }
      }
    };
    labIdText.setAction(getExpInfo);
    
    return experiencePanel;
  }

  private boolean loadServerInfo() {
    try {
      String url = getServerURL();
      HttpTransport transport = new HttpTransport(url);
      String info = transport.get("RIP").toString();
      // Get info     
      RIPServerInfo explist = new RIPServerInfo(info);
      labIdText.removeAllItems();
      for(String expid : explist.getExperiences().keySet()) {
        labIdText.addItem(expid);
      }
      serverMethodsTableModel.getDataVector().clear();
      for(RIPMethod method : explist.getMethods()) {
        serverMethodsTableModel.addRow(new Object[] {
            method.getUrl(),
            method.getType(),
            method.getDescription(),
            method.getParams().toString(),
            method.getResponse(),
            method.getExample()
        });
      }
      configuration.setApi(RIPConfigurationModel.RIP_SSE);
    } catch (NullPointerException | RIPException e) {
      System.err.println("[ERROR] Cannot parse RIP Server info, falling back to Websockets+RIPArduino");
      configuration.setApi(RIPConfigurationModel.RIP_WEBSOCKETS);
    } catch (Exception e) {
      System.err.println("[WARNING] Cannot retrieve server metadata, falling back to Websockets+RIPArduino");
      configuration.setApi(RIPConfigurationModel.RIP_WEBSOCKETS);
    }
    return false;
  }

  private String getServerURL() {
    String host = serverText.getText(); 
    return host;
  }

  private boolean loadExperience() {
    try {
      // Get server channel
      String url = getServerURL();
      if(!url.endsWith("/")) { url += '/'; }
      HttpTransport transport = new HttpTransport(url + "RIP");
      
      // Get experience info     
      String id = labIdText.getSelectedItem().toString();
      String query = String.format("?expId=%s", URLEncoder.encode(id, "utf8"));
      String info = transport.get(query).toString();
      RIPExperienceInfo meta = new RIPExperienceInfo(info);
      // Update configuration
      if(meta != null) {        
        configuration.setMetadata(meta);
        expIdText.setText(meta.getInfo().getName());
        labDescriptionText.setText(meta.getInfo().getDescription());
        labDescriptionText.setEditable(false);
        methodsTableModel.setDataVector(new Object[][]{}, new Object[]{"Name", "Params", "Returns", "Description"});
        showCodeTab = false;
        for(RIPMethod method : meta.getMethods()) {
          String name = method.getName();
          if(name.equals("step") || name.equals("eval")) {
            showCodeTab = true;
          }
          Object[] row = { method.getName(), method.getParams().toString(), method.getResponse().toString(), method.getDescription()};
          methodsTableModel.addRow(row);
        }
        Vector<String> columns = new Vector<String>();
        columns.add("Name"); columns.add("Type");
        Vector<Vector<String>> controls = mapToVector(meta.getWritable());
        Vector<Vector<String>> indicators = mapToVector(meta.getReadable());
        controlsTableModel.setDataVector(controls, columns);
        indicatorsTableModel.setDataVector(indicators, columns);
        return true;
      }     
    } catch (NullPointerException | RIPException e) {
      System.err.println("[ERROR] Cannot parse experience info");
    } catch (Exception e) {
      System.err.println("[ERROR] Cannot retrieve experience info");
    }
    return false;
  }

  private JPanel createVariablesPanel() {
    JPanel variablesPanel = new JPanel();
    JPanel serverVariablesPanel = createServerVariablesPanel();
    JScrollPane linksPanel = createLinksPanel();
    variablesPanel.setBorder(new TitledBorder(null, "Variables configuration", TitledBorder.LEADING, TitledBorder.TOP));
    variablesPanel.add(serverVariablesPanel);
    variablesPanel.add(linksPanel);
    return variablesPanel;
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
        String[] parts = variable.split(":");
        variable = parts[parts.length-1];
        if (variable != null) {
          Vector<Object> row = new Vector<>();
          row.add(varname);
          row.add(variable);
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
        String[] parts = variable.split(":");
        variable = parts[parts.length-1];
        if (variable != null) {
          Vector<Object> row = new Vector<>();
          row.add(varname);
          row.add(variable);
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
    AbstractAction deleteRow = new AbstractAction("Delete link"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
          int row = linksTable.getSelectedRow();
        linksTableModel.removeRow(row);
      }
    };
    popupMenuEJS = new JPopupMenu();
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
          Object[] emptyRow = new Object[]{"", "", false, false};
          linksTableModel.addRow(emptyRow);
        }       
      }       
    };
    linksTable.getModel().addTableModelListener(ta);

    return linksScrollPane;
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
    
    topEvaluablePanel.setVisible(showCodeTab);
    return topEvaluablePanel;
  }
  
  private Vector<Vector<String>> mapToVector(Map<String, String> map) {
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
  }

  public String savetoXML() {
    String dump = "";
    // Retrieve values from the dialog
    try {
      String server = serverText.getText().trim();
      String init = initialCodeText.getText();
      String step = evaluableText.getText();
      try { 
        RIPInfo info = configuration.getMetadata().getInfo();
        info.setName(labIdText.getSelectedItem().toString());
        info.setDescription(labDescriptionText.getText());
      } catch(NullPointerException e) {
        System.err.println("[WARNING] Server info not found.");
      }
      configuration.setInitCode((init != null) ? init : "");
      configuration.setStepCode((step != null) ? step : "");
      configuration.setServer(server);
      Vector<Vector> data = linksTableModel.getDataVector();
      configuration.setData(data);
      dump = configuration.dump();
    } catch(NullPointerException e) {
      System.out.println("[Error] Cannot serialize configuration of RIP Element");
      dump = "";
    }
    return dump;
  }

  /** 
   * Restores the states from an XML String
   */
  public void readfromXML(String inputXML) {
    try {
      configuration.restore(inputXML);
      serverText.setText(configuration.getServer());
      try {
        RIPExperienceInfo meta = configuration.getMetadata();
        RIPInfo info = meta.getInfo();
        labIdText.addItem(info.getName());
        labDescriptionText.setText(info.getDescription());
      } catch(Exception e) {
        System.err.println("[ERROR] Restoring Server Info");
      }
      linksTableModel.setDataVector(configuration.getData());
    } catch(Exception e) {
      System.err.println("[ERROR] Element configuration not loaded.");
    }
  } 
}