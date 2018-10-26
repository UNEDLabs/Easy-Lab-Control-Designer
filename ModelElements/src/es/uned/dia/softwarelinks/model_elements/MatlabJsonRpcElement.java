/**
 * RHI JSON-RPC Element
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
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.edition.ModelEditor;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MatlabJsonRpcElement extends AbstractModelElement { 
	//private ModelElementsCollection elementsCollection; // A provider of services for edition under EJS
	private static final long serialVersionUID = 1L;
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uned/dia/softwarelinks/resources/matlabicon.png"); // This icon is included in this jar
	// XML Node labels for saving the state of the elements 
	private static final String XML_NODE_LABEL_MATLAB = "rpcmatlab";
	private static final String XML_NODE_LABEL_SERVER = "server";
	private static final String XML_NODE_LABEL_PORT = "port";
	private static final String XML_NODE_LABEL_PATH = "path";
	private static final String XML_NODE_LABEL_LINKS = "links";
	private static final String XML_NODE_LABEL_ROW = "row";
	private static final String XML_NODE_LABEL_LABVIEW = "matlab";
	private static final String XML_NODE_LABEL_MODEL = "model";
	private static final String XML_NODE_LABEL_CODE = "code";  
	// GUI elements
	private DefaultTableModel linksTableModel;
	private JTextField serverText = new JTextField("localhost", 20);
	private JTextField portText = new JTextField("2055", 6);
	private JTable linksTable;
	private JPopupMenu popupMenuCon;
	private ModelEditor editor;
	private JComboBox transport = new JComboBox(new String[]{"tcp", "http"});

	/**
	 * Class Constructor
	 */
	public MatlabJsonRpcElement() {
		linksTableModel = new DefaultTableModel();
		Vector<String> controlsColumns = new Vector<String>();
		controlsColumns.add("Matlab");
		controlsColumns.add("EJS");
		Vector<Vector<String>> controlsVector = new Vector<Vector<String>>();
		linksTableModel.setDataVector(controlsVector, controlsColumns);
		linksTableModel.addRow(new String[]{"", ""});
	}
	 
	/**
	 * Returns the name of the classes to be packaged with the element
	 */
	public String getPackageList() {
		return "org/apache/++;org/apache/commons/logging/impl/++;org/glassfish/json/++";
	}
	
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
		return "Matlab";
	}
  
	/**
	 * Returns the name of the constructor
	 */
	public String getConstructorName() {
		return "es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient";
	}
  
	/** 
	 * Returns the initialization code
	 */
	public String getInitializationCode(String _name) {
		String init = getInstanceCode(_name);
		String getVariables = getCodeForGetVariables();
		String step = getCodeForStep();
		String setVariables = getCodeForSetVariables();
		String code = init;
		return code;
	}

	private String getInstanceCode(String name) {
		String server = serverText.getText().trim();
		String port = portText.getText().trim();
		if (server.length()<=0) { server = "localhost";	}
		if (port.length()<=0) { port = "2055"; }
		String protocol = transport.getSelectedItem().toString();
		String transportClass = "";
		switch(protocol) {
		default:
			protocol = "tcp";
		case "tcp":
			transportClass = "TcpTransport";
			break;
		case "http":
			transportClass = "HttpTransport";
			break;
		}
		String prefix = "es.uned.dia.softwarelinks.transport.";
		String url = protocol + "://" + server + ":" + port;
		return "try {" +
			prefix + "Transport transport = new "+ prefix + transportClass + "(\"" + url + "\");" +
			name + " = new " + getConstructorName() + "(transport);" +
		"} catch (Exception e) { e.printStackTrace(); }";		
	}
  
	private String getCodeForGetVariables() {
		final String function = "public void getValues() {\n %s \n}\n"; 
		final String assignment = "%s = get(\"%s\");\n"; 
		StringBuilder body = new StringBuilder();
		Vector<Vector<String>> data = (Vector<Vector<String>>)linksTableModel.getDataVector();
		for(Vector<String> row : data) {
			String matlabVariable = row.get(0);
			String ejsVariable = row.get(1);
			if(isValidGet(matlabVariable, ejsVariable)) {
				String code = String.format(assignment, ejsVariable, matlabVariable);
				System.out.println(code);
				body.append(code);
			}
		}
		return String.format(function, body.toString());
	}

	private boolean isValidGet(String matlab, String ejs) {
		return matlab != "" && matlab != null && ejs != "" && ejs != null;
	}
	
	private String getCodeForStep() {
		return "";
	}
		
	private String getCodeForSetVariables() {
		return "";
	}

	/**
	 * Return a string to show next to the instance name 
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
		String result = "<" + XML_NODE_LABEL_MATLAB + ">" +
						"<" + XML_NODE_LABEL_SERVER + ">" + serverText.getText() + "</" + XML_NODE_LABEL_SERVER + ">" +
	    	   		  	"<" + XML_NODE_LABEL_PORT + ">" + portText.getText() + "</" + XML_NODE_LABEL_PORT + ">";
		if(linksTableModel != null) {
			result += "<" + XML_NODE_LABEL_LINKS + ">";
			Iterable<Vector> links = (Iterable<Vector>)linksTableModel.getDataVector();
			for(Vector v : links) {
				result += "<" + XML_NODE_LABEL_ROW + ">" + 
					      "<" + XML_NODE_LABEL_LABVIEW + ">" + v.elementAt(0) + "</" + XML_NODE_LABEL_LABVIEW + ">" +  
					      "<" + XML_NODE_LABEL_MODEL + ">" + v.elementAt(1) + "</" + XML_NODE_LABEL_MODEL + ">" + 
					      "</" + XML_NODE_LABEL_ROW + ">" + "\n";
			}
			result += "</" + XML_NODE_LABEL_LINKS + ">";
		}
		result += "</" + XML_NODE_LABEL_MATLAB + ">";
	  
		return result;
	}

	/** 
	 * Restore the states from an XML String
	 */
	public void readfromXML(String _inputXML) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			InputSource is = new InputSource(new StringReader(_inputXML));        
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			// Server configuration
			this.serverText.setText(doc.getElementsByTagName(XML_NODE_LABEL_SERVER).item(0).getTextContent());
			this.portText.setText(doc.getElementsByTagName(XML_NODE_LABEL_PORT).item(0).getTextContent());
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
						Node node1 = node.getFirstChild(), node2 = node1.getNextSibling(), node3 = node2.getNextSibling();
						// The node labels are *NOT* checked
						row.add(node1.getTextContent());
						row.add(node2.getTextContent());
						linksTableModel.addRow(row);
					}
					node = linksList.item(++i);				
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Error al restaurar el estado del elemento.");
		}
	}
  
	/**
	 * Return the tooltip of the element
	 */
	public String getTooltip() {
		return "encapsulates an object that connects EJS to Matlab";
	}

	/**
	 * Return the html help page
	 */
	protected String getHtmlPage() {
		return "es/uned/dia/softwarelinks/resources/matlab.html";
	}

	/**
	 * Create the editor to configure the element
	 */
	protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
		editor = collection.getEJS().getModelEditor();
		JPanel topPanel = createTopPanel();
		JPanel variablesPanel = createVariablesPanel(); 
    	JButton closeButton = createCloseButton();
    	JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    	mainPanel.setPreferredSize(new Dimension(430, 400));
		mainPanel.add(topPanel);
		mainPanel.add(variablesPanel);
    	mainPanel.add(closeButton);
    	
		return mainPanel;
	}

	private JPanel createTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setBorder(new TitledBorder(null, "RpcMatlab configuration", TitledBorder.LEADING, TitledBorder.TOP));
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
		sl_topPanel.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.EAST, serverText);
		topPanel.add(portLabel);

		sl_topPanel.putConstraint(SpringLayout.NORTH, serverText, -2, SpringLayout.NORTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 6, SpringLayout.EAST, serverLabel);
		topPanel.add(serverText);
		serverText.setColumns(10);

		sl_topPanel.putConstraint(SpringLayout.NORTH, portText, -2, SpringLayout.NORTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, portText, 6, SpringLayout.EAST, portLabel);
		sl_topPanel.putConstraint(SpringLayout.EAST, portText, -6, SpringLayout.EAST, topPanel);
		topPanel.add(portText);
		portText.setColumns(10);

		JLabel transportLabel = new JLabel("Transport:");
		sl_topPanel.putConstraint(SpringLayout.NORTH, transportLabel, 15, SpringLayout.SOUTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, transportLabel, 0, SpringLayout.WEST, serverLabel);	
		topPanel.add(transportLabel);

		sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, transport, 0, SpringLayout.VERTICAL_CENTER, transportLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, transport, 5, SpringLayout.WEST, serverText);	
		topPanel.add(transport);
		
		return topPanel;
	}

	private JPanel createVariablesPanel() {
		JPanel variablesPanel = new JPanel();
		variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.X_AXIS));
		variablesPanel.setBorder(new TitledBorder(null, "Matlab and EJS variables", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		variablesPanel.setMinimumSize(new Dimension(420, 120));		
		variablesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		variablesPanel.setPreferredSize(new Dimension(420, 120));
		createTable();
		JScrollPane controlsScrollPane = new JScrollPane(linksTable);
		variablesPanel.add(controlsScrollPane);
		return variablesPanel;
	}

	private void createTable() {
		linksTable = new JTable(linksTableModel);
		linksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		linksTable.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (TableModelEvent.UPDATE == e.getType() && linksTable.getRowCount() == e.getLastRow()+1) {
					String[] emptyRow = new String[]{"", ""};
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
    	popupMenuCon = new JPopupMenu();
    	popupMenuCon.add(connectVariable);
    	linksTable.addMouseListener (new MouseAdapter() {    		
    		public void mousePressed (MouseEvent _evt) {
    			if (OSPRuntime.isPopupTrigger(_evt) && linksTable.isEnabled ()) {
    				int row = linksTable.rowAtPoint(_evt.getPoint()); 
    				if(row != -1) { 
    					linksTable.setRowSelectionInterval(row, row);
    				}
    				popupMenuCon.show(_evt.getComponent(), _evt.getX(), _evt.getY());
    			}
    		}
    	});    
	}
	
	private JButton createCloseButton() {
    	JButton closeButton = new JButton(new AbstractAction("Close") {
    		private static final long serialVersionUID = 1L;
    		public void actionPerformed(ActionEvent e) {
    			JButton button = ((JButton)e.getSource());
    			JDialog dialog = null;
    			if(button != null) {
    				dialog = (JDialog)button.getTopLevelAncestor();
    			}
    			if(dialog != null) {
    				dialog.setVisible(false);
    			}
    		}    		
    	});
    	closeButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
    	return closeButton;
	}
	
	/**
	 * Return search info
	 */
	public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
		java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
		return list;
	}

}
