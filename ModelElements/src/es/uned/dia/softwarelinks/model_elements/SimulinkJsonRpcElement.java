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
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.edition.ModelEditor;
import org.opensourcephysics.display.OSPRuntime;

import es.uned.dia.softwarelinks.utils.RIPConfigurationModel;
import es.uned.dia.softwarelinks.utils.SimulinkCodeBuilder;

public class SimulinkJsonRpcElement extends AbstractModelElement { 
	private static final String LOCAL_OR_REMOTE_LABEL = "Local or Remote:";
	private static final String GENERIC_NAME = "Simulink";
	private static final String VARIABLES_LABEL = "Matlab and EJS variables";
	private static final String TRANSPORT_LABEL = "Transport:";
	private static final String PORT_LABEL = "Port:";
	private static final String SERVER_ADDRESS_LABEL = "Server address:";
	private static final String RPC_SIMULINK_CONFIGURATION = "Connection";
	//private ModelElementsCollection elementsCollection; // A provider of services for edition under EJS
	private static final long serialVersionUID = 1L;
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uned/dia/softwarelinks/resources/simulink.png"); // This icon is included in this jar
	// GUI elements
	LinksTableModel linksTableModel;
	private JTextField serverText = new JTextField("localhost", 20);
	private JTextField portText = new JTextField("2055", 6);
	private JTable linksTable;
	private JPopupMenu popupMenuCon;
	private ModelEditor editor;
	private JComboBox<String> transport = new JComboBox<>(new String[]{"tcp", "http"});
	private JComboBox<String> localOrRemote = new JComboBox<>(new String[]{"local", "remote"});

	RIPConfigurationModel configuration = new RIPConfigurationModel();
	
	class LinksTableModel extends DefaultTableModel {
		public final String[] COLUMNS = {"Matlab", "EJS", "get", "set"};

    	public void setDataVector(Object[][] dataVector) {
    		setDataVector(dataVector, COLUMNS);
    	}
	
	    public Class<?> getColumnClass(int c) {
	    	return getValueAt(0, c).getClass();
	    }
	}
	
	/**
	 * Class Constructor
	 */
	public SimulinkJsonRpcElement() {
		linksTableModel = new LinksTableModel();
		Object[][] data = {{"", "", new Boolean(false), new Boolean(true)}};
		linksTableModel.setDataVector(data);
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
		return GENERIC_NAME;
	}
  
	/**
	 * Returns the name of the constructor
	 */
	public String getConstructorName() {
		return "es.uned.dia.softwarelinks.matlab.client.RemoteSimulinkConnector";
	}

	/** 
	 * Returns the initialization code
	 */
	public String getInitializationCode(String _name) {
		SimulinkCodeBuilder builder = new SimulinkCodeBuilder();
		builder.setName(_name);
		builder.setModel(configuration);
		return builder.getCode();
	}

	/**
	 * Return a string to show next to the instance name 
	 */
	public String getDisplayInfo() {
		String server = serverText.getText().trim();
		String port = portText.getText().trim();
		if (server.length()<=0) server = "localhost"; 
		if (port.length()<=0) port = "2055";
		return "("+configuration.getServer() + ":" + configuration.getPort()+")";
	}

	/** 
	 * Write the state into an XML String  
	 */
	public String savetoXML() {
		String mode = (String)localOrRemote.getSelectedItem();
		String server = serverText.getText().trim();
		String port = portText.getText().trim();
		String protocol = transport.getSelectedItem().toString();
		configuration.setMode(mode);
		configuration.setServer(server, port, protocol);
		configuration.setData(linksTableModel.getDataVector());
		return configuration.dump();
	}

	/** 
	 * Restore the states from an XML String
	 */
	public void readfromXML(String inputXML) {
		configuration.restore(inputXML);
		serverText.setText(configuration.getServer());
		portText.setText(configuration.getPort());
		transport.setSelectedItem(configuration.getProtocol());
		localOrRemote.setSelectedItem(configuration.getMode());
		linksTableModel.setDataVector(configuration.getData());
	}
  
	/**
	 * Return the tooltip of the element
	 */
	public String getTooltip() {
		return "Encapsulates an object that connects EJS to Simulink";
	}

	/**
	 * Return the html help page
	 */
	protected String getHtmlPage() {
		return "es/uned/dia/softwarelinks/resources/simulink.html";
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
		JLabel modeLabel = new JLabel(LOCAL_OR_REMOTE_LABEL);
		JLabel serverLabel = new JLabel(SERVER_ADDRESS_LABEL);
		JLabel portLabel = new JLabel(PORT_LABEL);
		JLabel transportLabel = new JLabel(TRANSPORT_LABEL);
		SpringLayout sl_topPanel = new SpringLayout();	
		portText.setColumns(10);
		serverText.setColumns(10);
		topPanel.setBorder(new TitledBorder(null, RPC_SIMULINK_CONFIGURATION, TitledBorder.LEADING, TitledBorder.TOP));
		topPanel.setMinimumSize(new Dimension(420, 120));
		topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		topPanel.setPreferredSize(new Dimension(420, 120));
		topPanel.add(modeLabel);
		topPanel.add(localOrRemote);
		topPanel.add(serverLabel);
		topPanel.add(portLabel);
		topPanel.add(serverText);
		topPanel.add(portText);
		topPanel.add(transportLabel);
		topPanel.add(transport);
		topPanel.setLayout(sl_topPanel);
		sl_topPanel.putConstraint(SpringLayout.NORTH, modeLabel, 7, SpringLayout.NORTH, topPanel);
		sl_topPanel.putConstraint(SpringLayout.WEST, modeLabel, 5, SpringLayout.WEST, topPanel);
		sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, localOrRemote, 0, SpringLayout.VERTICAL_CENTER, modeLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, localOrRemote, 5, SpringLayout.EAST, modeLabel);
		sl_topPanel.putConstraint(SpringLayout.NORTH, serverLabel, 7, SpringLayout.SOUTH, localOrRemote);
		sl_topPanel.putConstraint(SpringLayout.WEST, serverLabel, 5, SpringLayout.WEST, topPanel);
		sl_topPanel.putConstraint(SpringLayout.NORTH, portLabel, 0, SpringLayout.NORTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.EAST, serverText);
		sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, serverText, 0, SpringLayout.VERTICAL_CENTER, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 0, SpringLayout.WEST, localOrRemote);
		sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, portText, 0, SpringLayout.VERTICAL_CENTER, serverText);
		sl_topPanel.putConstraint(SpringLayout.WEST, portText, 6, SpringLayout.EAST, portLabel);
		sl_topPanel.putConstraint(SpringLayout.EAST, portText, -6, SpringLayout.EAST, topPanel);
		sl_topPanel.putConstraint(SpringLayout.NORTH, transportLabel, 15, SpringLayout.SOUTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, transportLabel, 0, SpringLayout.WEST, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, transport, 0, SpringLayout.VERTICAL_CENTER, transportLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, transport, 0, SpringLayout.WEST, serverText);
		return topPanel;
	}

	private JPanel createVariablesPanel() {
		JPanel variablesPanel = new JPanel();
		variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.X_AXIS));
		variablesPanel.setBorder(new TitledBorder(null, VARIABLES_LABEL, TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
    	popupMenuCon = new JPopupMenu();
    	popupMenuCon.add(connectVariable);
    	popupMenuCon.add(deleteRow);
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
