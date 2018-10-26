package org.colos.ejs.model_elements.roboticsLabs.robots;

import java.awt.*;






import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.EJSAware;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementMultipageEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementTabbedEditor;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.colos.roboticsLabs.robots.AbstractRobot;
import org.opensourcephysics.drawing3d.Group;

/**
 * A base abstract class to simplify the creation of RobotElements
 * @author Almudena Ruiz 
 */

public abstract class AbstractRobotElement extends AbstractModelElement implements EJSAware {

	static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif"); // This icon is bundled with EJS
	static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
	static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0, 4, 0, 2);

	static protected final String BEGIN_URL_HEADER = "<IPaddress>"; // Used to delimit my XML information
	static protected final String END_URL_HEADER = "</IPaddress>"; // Used to delimit my XML information
	static protected final String BEGIN_PORT_HEADER = "<PortNumber>";
	static protected final String END_PORT_HEADER = "</PortNumber>";
	static protected final String BEGIN_USER_HEADER = "<User><![CDATA[";
	static protected final String END_USER_HEADER = "</User>";
	static protected final String BEGIN_PASSWORD_HEADER = "<Password><![CDATA[";
	static protected final String END_PASSWORD_HEADER = "]]></Password>";
	static protected final String BEGIN_GROUP3D_HEADER = "<Group3D><![CDATA[";
	static protected final String END_GROUP3D_HEADER = "</Group3D>";
	static protected final String BEGIN_RESTRICTIONS_HEADER = "<Restrictions>\n";
	static protected final String END_RESTRICTIONS_HEADER = "</Restrictions>\n";

	protected JTextField mIPField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mPortNumberField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mUserField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mPasswordField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mGroup3DField = new JTextField(); // needs to be created to avoid null references
	protected ModelElementTabbedEditor restrictionsEditor = new MyModelElementTabbedEditor(this, null, "Restriction");
	private AbstractRobot mRobot=null;
	private Osejs mEjs=null;

	{
		mGroup3DField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed (java.awt.event.ActionEvent _e) { 
				String groupName = mGroup3DField.getText().trim();
				System.out.println ("Action to view group = <"+groupName+">");
				setViewGroup();
				mGroup3DField.setBackground (Color.WHITE);
			}
		});
		mGroup3DField.addKeyListener(new java.awt.event.KeyListener() {
			public void keyPressed  (java.awt.event.KeyEvent _e) { processKeyEvent (_e,0); }
			public void keyReleased (java.awt.event.KeyEvent _e) { processKeyEvent (_e,1); }
			public void keyTyped    (java.awt.event.KeyEvent _e) { processKeyEvent (_e,2); }
			private void processKeyEvent (java.awt.event.KeyEvent _e, int _n) {
				if (_e.getKeyChar()!='\n')     mGroup3DField.setBackground (Color.YELLOW);
				//			  if (_e.getKeyCode()==27)   { mGroup3DField.setText (internalValue.value); mGroup3DField.setBackground (Color.WHITE); }
			}
		});
	}

	// -------------------------------
	// Implementation of EJSAware
	// -------------------------------

	public void setEJS(Osejs ejs) {
		mEjs = ejs;
		setViewGroup();
	}

	public void readCompleted() {
		setViewGroup();
	}

	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------

	public String getPackageList() {
		if (mGroup3DField.getText().trim().length() > 0)
			return "gnu/io/++";
		return "";
	}

	public String getDestructionCode(String _name) {
		return "try { " + _name + ".disconnect(); } catch(Exception _exc) {};";
	}

	protected void addDeclaration(StringBuffer buffer, String _name) {
		buffer.append(_name + " = new " + getConstructorName() + "(this,");
		buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText()
				.trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField
				.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mUserField.getText()
				.trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mPasswordField
				.getText().trim()) + ");\n");
	}

	public String getInitializationCode(String _name) {
		StringBuffer buffer = new StringBuffer();
		String groupName = mGroup3DField.getText().trim();
		if (groupName.length() > 0) {
			if (!groupName.startsWith("_view.")) groupName = "_view." + groupName;
		}
		else groupName = null;

		buffer.append("if (" + _name + "!=null) {\n");
		buffer.append("  " + getDestructionCode(_name) + "\n");
		if (groupName!=null) buffer.append("  " + _name + ".removeFromViewGroup();\n");
		buffer.append("  }\n");
		addDeclaration(buffer,_name);
		if (groupName!=null) buffer.append(_name + ".addToViewGroup(" + groupName + ");\n");		
	    // Restrictions
	    java.util.List<ModelElementEditor> pList = restrictionsEditor.getEditorList();
	    int nRestrictions = pList.size();
	    // for (int i=0, c=0; i<nRestrictions; i++) {
	    for (int i = 0; i < nRestrictions; i++) {
	      ModelElementEditor editor = pList.get(i);
	      if (!editor.isEditable()) continue;
	      buffer.append("    " + _name + ".addRestriction(new org.colos.roboticsLabs.robots.utils.restrictions.Restriction() {\n");
	      buffer.append(editor.generateCode(_name + "." + editor.getName(),"      "));
	      buffer.append("  });\n\n");
	    }	    
		return buffer.toString();
	}

	public String getDisplayInfo() {
		String ipAddress = mIPField.getText().trim();
		String portNumber = mPortNumberField.getText().trim();
		if (ipAddress.length() > 0) {
			return (portNumber.length() > 0) ? "(" + portNumber + "-" + ipAddress
					+ ")" : "(" + ipAddress + ")";
		}
		if (portNumber.length() > 0)
			return "(" + portNumber + ")";
		return null;
	}

	public String savetoXML() {
		StringBuffer buffer = new StringBuffer();
		buffer
		.append(BEGIN_URL_HEADER + mIPField.getText() + END_URL_HEADER + "\n");
		buffer.append(BEGIN_PORT_HEADER + mPortNumberField.getText()
				+ END_PORT_HEADER + "\n");
		buffer.append(BEGIN_USER_HEADER + mUserField.getText() + END_USER_HEADER
				+ "\n");
		buffer.append(BEGIN_PASSWORD_HEADER + mPasswordField.getText()
				+ END_PASSWORD_HEADER + "\n");
		buffer.append(BEGIN_GROUP3D_HEADER + mGroup3DField.getText()
				+ END_GROUP3D_HEADER + "\n");
	
		buffer.append(BEGIN_RESTRICTIONS_HEADER+ restrictionsEditor.saveStringBuffer() + END_RESTRICTIONS_HEADER + "\n");
		return buffer.toString();
	}

	public void readfromXML(String _inputXML) {
		mIPField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_URL_HEADER, END_URL_HEADER, false));
		mPortNumberField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_PORT_HEADER, END_PORT_HEADER, false));
		mUserField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_USER_HEADER, END_USER_HEADER, false));
		mPasswordField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_PASSWORD_HEADER, END_PASSWORD_HEADER, false));
		mGroup3DField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_GROUP3D_HEADER, END_GROUP3D_HEADER, false));
		restrictionsEditor.readXmlString(OsejsCommon.getPiece(_inputXML,BEGIN_RESTRICTIONS_HEADER, END_RESTRICTIONS_HEADER, false));
		setViewGroup();
	}

	abstract protected AbstractRobot createRobot();

	private void setViewGroup() {
		String groupName = mGroup3DField.getText().trim();
		//	  System.out.println("Group = "+groupName);
		//	  System.out.println("EJS is = "+mEjs);
		if (groupName.length()<=0) {
			if (mRobot!=null) mRobot.addToViewGroup(null);
			return;
		}
		if (mRobot==null) mRobot = createRobot();
		if (mEjs!=null) {
			Object object = mEjs.getViewElement(groupName);
			//		System.out.println("Object is = "+object);
			if (object instanceof Group) {
				//			System.out.println("Adding to robot = "+mRobot);
				mRobot.addToViewGroup((Group) object);
			}
		}
		else System.err.println ("Error: "+groupName+" is not a valid 3D Group element");
	}

	// -------------------------------
	// Help and edition
	// -------------------------------

	public void setFont(Font font) {
		mIPField.setFont(font);
		mPortNumberField.setFont(font);
		mUserField.setFont(font);
		mPasswordField.setFont(font);
		mGroup3DField.setFont(font);
		restrictionsEditor.setFont(font);
	}

	protected JPanel createTopPanel(JPanel urlPanel, JPanel portPanel, JPanel userPanel, JPanel passwordPanel, JPanel group3DPanel) {
		JPanel mainPanel = new JPanel(new GridLayout(3, 2));
		mainPanel.add(urlPanel);
		mainPanel.add(portPanel);
		mainPanel.add(userPanel);
		mainPanel.add(passwordPanel);
		mainPanel.add(group3DPanel);		
		return mainPanel;
	}

	protected Component createEditor(String name, Component parentComponent,
			final ModelElementsCollection collection) {
		DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				collection.reportChange(AbstractRobotElement.this);
			}

			public void insertUpdate(DocumentEvent e) {
				collection.reportChange(AbstractRobotElement.this);
			}

			public void removeUpdate(DocumentEvent e) {
				collection.reportChange(AbstractRobotElement.this);
			}
		};

		JLabel urlLabel = new JLabel(RES.getString("ServerIP"),
				SwingConstants.RIGHT);
		urlLabel.setForeground(COLOR);
		urlLabel.setBorder(LABEL_BORDER);

		mIPField.getDocument().addDocumentListener(documentListener);

		JButton urlLinkButton = new JButton(LINK_ICON);
		urlLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mIPField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseVariable(mIPField, "String", value);
				if (variable != null)
					mIPField.setText("%" + variable + "%");
			}
		});

		JLabel portLabel = new JLabel(RES.getString("PortNumber"),
				SwingConstants.RIGHT);
		portLabel.setForeground(COLOR);
		portLabel.setBorder(LABEL_BORDER);

		mPortNumberField.getDocument().addDocumentListener(documentListener);

		JLabel userLabel = new JLabel(RES.getString("Username"),
				SwingConstants.RIGHT);
		userLabel.setForeground(COLOR);
		userLabel.setBorder(LABEL_BORDER);

		mUserField.getDocument().addDocumentListener(documentListener);

		JButton userLinkButton = new JButton(LINK_ICON);
		userLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mUserField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection
				.chooseVariable(mUserField, "String", value);
				if (variable != null)
					mUserField.setText("%" + variable + "%");
			}
		});

		JLabel passwordLabel = new JLabel(RES.getString("Password"),
				SwingConstants.RIGHT);
		passwordLabel.setForeground(COLOR);
		passwordLabel.setBorder(LABEL_BORDER);

		mPasswordField.getDocument().addDocumentListener(documentListener);

		JButton passwordLinkButton = new JButton(LINK_ICON);
		passwordLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mPasswordField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseVariable(mPasswordField, "String",
						value);
				if (variable != null)
					mPasswordField.setText("%" + variable + "%");
			}
		});

		JLabel group3DLabel = new JLabel(RES.getString("Robots.Group3D"),
				SwingConstants.RIGHT);
		group3DLabel.setForeground(COLOR);
		group3DLabel.setBorder(LABEL_BORDER);

		mGroup3DField.getDocument().addDocumentListener(documentListener);

		JButton group3DLinkButton = new JButton(LINK_ICON);
		group3DLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mGroup3DField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseViewElement(mGroup3DField, Group.class, value);
				if (variable != null) {
					mGroup3DField.setText(variable);
					setViewGroup();
				}
			}
		});

		Set<JLabel> labelSet = new HashSet<JLabel>(); // list of labels to adjust size

		labelSet.add(urlLabel);
		labelSet.add(portLabel);
		labelSet.add(userLabel);
		labelSet.add(passwordLabel);
		labelSet.add(group3DLabel);

		JPanel urlPanel = new JPanel(new BorderLayout());
		urlPanel.add(urlLabel, BorderLayout.WEST);
		urlPanel.add(mIPField, BorderLayout.CENTER);
		urlPanel.add(urlLinkButton, BorderLayout.EAST);

		JPanel portPanel = new JPanel(new BorderLayout());
		portPanel.add(portLabel, BorderLayout.WEST);
		portPanel.add(mPortNumberField, BorderLayout.CENTER);
		JButton portLinkButton = new JButton(LINK_ICON);
		portLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mPortNumberField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseVariable(mPortNumberField, "int",
						value);
				if (variable != null)
					mPortNumberField.setText("%" + variable + "%");
			}
		});
		portPanel.add(portLinkButton, BorderLayout.EAST);

		JPanel userPanel = new JPanel(new BorderLayout());
		userPanel.add(userLabel, BorderLayout.WEST);
		userPanel.add(mUserField, BorderLayout.CENTER);
		userPanel.add(userLinkButton, BorderLayout.EAST);

		JPanel passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.add(passwordLabel, BorderLayout.WEST);
		passwordPanel.add(mPasswordField, BorderLayout.CENTER);
		passwordPanel.add(passwordLinkButton, BorderLayout.EAST);

		JPanel group3DPanel = new JPanel(new BorderLayout());
		group3DPanel.add(group3DLabel, BorderLayout.WEST);
		group3DPanel.add(mGroup3DField, BorderLayout.CENTER);
		group3DPanel.add(group3DLinkButton, BorderLayout.EAST);

		JPanel topPanel = createTopPanel(urlPanel,portPanel,userPanel,passwordPanel,group3DPanel);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(600, 500));
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(restrictionsEditor.getComponent(collection),BorderLayout.CENTER);

		// --- Make all labels in the set the same dimension
		int maxWidth = 0, maxHeight = 0;
		for (JLabel label : labelSet) {
			maxWidth = Math.max(maxWidth, label.getPreferredSize().width);
			maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
		}
		Dimension dim = new Dimension(maxWidth, maxHeight);
		for (JLabel label : labelSet)
			label.setPreferredSize(dim);

		return mainPanel;
	}

	public java.util.List<ModelElementSearch> search(String info,
			String searchString, int mode, String name,
			ModelElementsCollection collection) {
		java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
		addToSearch(list, mIPField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mPortNumberField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mUserField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mPasswordField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mGroup3DField, info, searchString, mode, this, name,
				collection);
		list.addAll(restrictionsEditor.search(info, searchString, mode, name,collection));
		return list;
	}

	static protected class MyModelElementTabbedEditor extends ModelElementTabbedEditor {

		public MyModelElementTabbedEditor(ModelElement _element,ModelElementMultipageEditor _parentEditor, String _name) {
			super(_element, _parentEditor, _name);
		}
		
		static private String options[] = {"Plane", "Box", "Block", "Sphere", "Custom" };
			
		@Override
		protected String getDefaultCode() {
												
			int option = JOptionPane.showOptionDialog(null,"Choose the retriction type:","Restriction Editor",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[4]);		
		
		StringBuffer buffer = new StringBuffer();
			buffer.append("<Code><![CDATA[");
			buffer.append("public boolean allowsPoint(double x, double y, double z) {\n");
			switch(option) {
			case 0:
				buffer.append("  // Plane is Ax+By+Cz = D\n");
				buffer.append("  double A = 1;\n");
				buffer.append("  double B = 1;\n");
				buffer.append("  double C = 1;\n");
				buffer.append("  double D = 1;\n");
				buffer.append("  return A*x+B*y+C*z-D<0;\n"); 
				break;
			case 1 : 
				buffer.append("  // BoxRestriciton. Define the bounds:\n");
				buffer.append("  double xmin = -1;\n");
				buffer.append("  double xmax = 1;\n");
				buffer.append("  double ymin = -1;\n");
				buffer.append("  double ymax = 1;\n");
				buffer.append("  double zmin = 0;\n");
				buffer.append("  double zmax = 1;\n");
				buffer.append("  return (x >= xmin && x <= xmax && " +
						"y >= ymin && y <= ymax && \n " + 
						"z >= zmin && z <= zmax);\n");				
				break;
			case 2 : 
				buffer.append("  // BlockRestriciton. Define the bounds:\n");
				buffer.append("  double xmin = -1;\n");
				buffer.append("  double xmax = 1;\n");
				buffer.append("  double ymin = -1;\n");
				buffer.append("  double ymax = 1;\n");
				buffer.append("  double zmin = 0;\n");
				buffer.append("  double zmax = 1;\n");
				buffer.append("  return !(x >= xmin && x <= xmax && " +
						"y >= ymin && y <= ymax && \n " + 
						"z >= zmin && z <= zmax);\n");	
				break;
			case 3 : 
				buffer.append("  // Sphere is (x-x0)^2 + (y-y0)^2 + (z-z0)^2 = r^2 \n");
				buffer.append("  // with centre (x0, y0, z0) and radius r \n");
				buffer.append("  double x0 = 0;\n");
				buffer.append("  double y0 = 0;\n");
				buffer.append("  double z0 = 0;\n");
				buffer.append("  double r = 1;\n");
				buffer.append("  return ((x-x0)*(x-x0) + (y-y0)*(y-y0) + " +
						"(z-z0)*(z-z0)) < (r * r);\n");
				break;
			default:
				buffer.append("  return true;\n");
				break;

			}
			buffer.append("}\n");
			buffer.append("public void action(double x, double y, double z){\n");
			buffer.append("}\n]]></Code>\n");
			return buffer.toString();
		}
		}
	}



