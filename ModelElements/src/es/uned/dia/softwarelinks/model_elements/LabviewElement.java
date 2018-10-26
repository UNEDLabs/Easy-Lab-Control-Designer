/**
 * Labview Element
 * author: Jesús Chacón <jcsombria@gmail.com>
 *
 * Copyright (C) 2014 Jesús Chacón
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uned.dia.softwarelinks.model_elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.opensourcephysics.display.OSPRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import es.uned.dia.softwarelinks.labview.protocol.xmlrpc.XmlRpcProtocol;

import javax.xml.parsers.*;

public class LabviewElement extends AbstractModelElement {  
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uned/dia/softwarelinks/resources/Labview.png"); // This icon is included in this jar

	private static final String CONNECTION_OK = "The VI controls and indicator have been loaded.";
	private static final String INVALID_PATH = "The VI couldn't be opened, but the server is OK. Please, check the VI path.";
	private static final String SERVER_KO = "The server is not responding. Please, check the server address and port.";

	private static final String XML_NODE_LABEL_JIL = "jil";
	private static final String XML_NODE_LABEL_SERVER = "server";
	private static final String XML_NODE_LABEL_PORT = "port";
	private static final String XML_NODE_LABEL_PATH = "path";
	private static final String XML_NODE_LABEL_LINKS = "links";
	private static final String XML_NODE_LABEL_ROW = "row";
	private static final String XML_NODE_LABEL_LABVIEW = "labview";
	private static final String XML_NODE_LABEL_MODEL = "model";
	private static final String XML_NODE_LABEL_TYPE = "type";
  
	//private ModelElementsCollection elementsCollection; // A provider of services for edition under EJS
	private static final long serialVersionUID = 1L;
  
	// GUI elements
	private DefaultTableModel linksTableModel;
	private DefaultTableModel indicatorsTableModel;
	private DefaultTableModel controlsTableModel;
	private JTextField serverText = new JTextField("localhost", 20);
	private JTextField portText = new JTextField("2055", 6);
	private JTextField viFileText = new JTextField("jiltest/JiLTest.vi", 32);  
	
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
	XmlRpcProtocol jilTest = null;
	private Component parent;

	/**
	 * Class Constructor
	 */
	public LabviewElement() {	
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
  
	/**
	 * Returns the ImageIcon representing the element
	 */
	public ImageIcon getImageIcon() {
		return ELEMENT_ICON;
	}
  
	/**
	 * Returns the generic name of the element
	 */
	public String getGenericName() {
		return "LabVIEW_v2.0";
	}
  
	/**
	 * Returns the name of the constructor
	 */
	public String getConstructorName() {
		return "es.uned.dia.softwarelinks.labview.protocol.xmlrpc.XmlRpcProtocol";
	}
  
	/**
	 * Returns the name of the classes to be packaged with the element
	 */
	public String getPackageList() {
		return "org/apache/ws/++;org/apache/xmlrpc/++";
	}
	
	/** 
	 * Returns the initialization code
	 */
	public String getInitializationCode(String _name) {
		String server = serverText.getText().trim();
		String port = portText.getText().trim();
		if (server.length()<=0) server = "localhost"; 
		if (port.length()<=0) port = "2055";
		
		// Generate code for setValues() and getValues()		
		String setValuesCode = "public void setValues() { " +
							   "  if (isConnected() && isRunning()) {" +
							   "    setValuesLater();" +
							   "    syncVI();" +
							   "  }" +
							   "}";
		String setValuesLaterCode = "public void setValuesLater() { ";
		String getValuesCode = "public void getValues() { " +							   
							   "  if (isConnected() && isRunning()) {" +
							   "    getValuesLater();" +
							   "    syncVI();" +
		  					   "  }";
		String getValuesLaterCode = "public void getValuesLater() { ";
		String stepCode = "public boolean step() {" +
						  "  boolean result = false;" +
						  "  if (isConnected() && isRunning()) {" +
						  "    result = true;" +
						  "    setValuesLater();" +
						  "    getValues();" +
						  "  }" +
						  "  return result;" +
						  "}";
	
		String openVICode = "public boolean openVI() { return openVI(\"" + viFileText.getText() + "\", \"";
		
		@SuppressWarnings("unchecked")
		Iterator<Vector<String>> iter = (Iterator<Vector<String>>)linksTableModel.getDataVector().iterator();
		while(iter.hasNext()) {
			Vector<String> row = iter.next();
			
			String[] lvvar = row.get(0).split(":", 2), modelvar = row.get(1).split(":", 2);
			String type = row.get(2);
			if(lvvar != null && lvvar.length == 2) {
				boolean isIndicator = type.contains("indicator");
				if(isIndicator) {
					getValuesLaterCode += "getVariableLater(\""+lvvar[0]+"\");";
					String typeCast;
    				if(modelvar[1].compareTo("boolean") == 0){
    					typeCast = "(Boolean)";
    				} else if(modelvar[1].compareTo("int") == 0) {
    					typeCast = "(Integer)";
    				} else if(modelvar[1].compareTo("float") == 0){
    					typeCast = "(Float)";
    				} else if(modelvar[1].compareTo("double") == 0) {
    					typeCast = "(Double)";
    				} else if(modelvar[1].compareTo("String") == 0) {
    					typeCast = "(String)";
    				} else {
    					typeCast = "(Object)";
    				}
					getValuesCode += modelvar[0] + "= " + typeCast + "getVariableResult(\""+lvvar[0]+"\");";
				} else {
					setValuesLaterCode += "setVariableLater(\""+lvvar[0]+"\", " + modelvar[0] + ");";
				}
				openVICode += lvvar[0];	
				if(iter.hasNext()) openVICode += ",";
			}
		}
		openVICode = "public boolean openVI() { return openVI(\"" + viFileText.getText() + "\"); }";
		setValuesLaterCode += "}";
		getValuesLaterCode += "}";
		getValuesCode += "}";
		String labviewCode = "try {" + _name + " = new " + getConstructorName()+ "(\"http://" +
							 ModelElementsUtilities.getPureValue(server) + ":" + 
							 ModelElementsUtilities.getPureValue(port) +"\") {" + 
							 	setValuesCode + 
							 	setValuesLaterCode + 
							 	getValuesCode + 
							 	getValuesLaterCode +
							 	stepCode +
								openVICode +
							 "};" + "} catch(Exception e) {e.printStackTrace();}";
		return labviewCode;
	}
  
	/**
	 * Return a string to show with the instance of the element 
	 */
	public String getDisplayInfo() {
		String server = serverText.getText().trim();
		String port = portText.getText().trim();
		if (server.length()<=0) server = "localhost"; 
		if (port.length()<=0) port = "2055";
		return "("+server + ":" + port+")";
	}

	/** 
	 * Write the state into an XML String  
	 */
	public String savetoXML() {
	  // Save the connection: server, port and path to vi
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
  
	// -------------------------------
	// Help and edition
	// -------------------------------

	/**
	 * Return the tooltip of the element
	 */
	public String getTooltip() {
		return "encapsulates an object that connects EJS to LabVIEW";
	}

	/**
	 * Return the html help page
	 */
	protected String getHtmlPage() {
		return "es/uned/dia/softwarelinks/resources/Labview.html";
	}

	// Create the editor to configure the connection with JIL server and the links between LabVIEW and EJS variables 
	protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
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
//		sl_topPanel.putConstraint(SpringLayout.EAST, portLabel, 126, SpringLayout.WEST, serverLabel);
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
		topPanel.add(testButton);		
		
		JPanel labviewPanel = new JPanel();
		mainPanel.add(labviewPanel);
		labviewPanel.setLayout(new BoxLayout(labviewPanel, BoxLayout.X_AXIS));
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
		labviewPanel.add(controlsScrollPane);
		controlsScrollPane.setBorder(BorderFactory.createTitledBorder("VI Controls"));
		
    	AbstractAction connectControlToVariable = new AbstractAction("Connect control to variable"){
    		private static final long serialVersionUID = 1L;
    		public void actionPerformed(ActionEvent e) {
    			if(controlsTable.getSelectedRow() == -1) {
    				JOptionPane.showMessageDialog(null, "Please, select a control.");
    				return;    				
    			}
    				String varname = (String)controlsTable.getValueAt(controlsTable.getSelectedRow(), 0),
    					   vartype = (String)controlsTable.getValueAt(controlsTable.getSelectedRow(), 1);    			

    			// Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"   			
    			String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", XmlRpcProtocol.XmlRpcToJavaType(vartype), linksTable, varname, "");
    			if (variable != null) {
    				Vector<String> row = new Vector<String>();
    				row.add(varname + ":" + vartype);
    				row.add(variable + ":" + XmlRpcProtocol.XmlRpcToJavaType(vartype));
    				row.add("control");
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
    			String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", XmlRpcProtocol.XmlRpcToJavaType(vartype), linksTable, varname, "");
    			if (variable != null) {
    				Vector<String> row = new Vector<String>();
    				row.add(varname + ":" + vartype);
    				row.add(variable + ":" + XmlRpcProtocol.XmlRpcToJavaType(vartype));
    				row.add("indicator");
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
		JScrollPane	linksScrollPane = new JScrollPane(linksTable); //an scroll panel for the table
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
    	AbstractAction testServer = new AbstractAction("Get VI variables"){
    		private static final long serialVersionUID = 1L;
    		public void actionPerformed(ActionEvent e) {				
    			if(jilTest == null) {    				
					try {
						jilTest = new XmlRpcProtocol("http://"+serverText.getText()+":"+portText.getText());
					} catch (MalformedURLException e1) {					
//						e1.printStackTrace();
					}
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
	 * Return the search info
	 */
	public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
		java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
		//    addToSearch(list,mDescriptionField,info,searchString,mode,this,name,collection);
		//    addToSearch(list,mExtensionsField,info,searchString,mode,this,name,collection);
		return list;
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
}
