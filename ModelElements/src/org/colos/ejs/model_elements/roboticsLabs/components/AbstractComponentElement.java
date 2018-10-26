package org.colos.ejs.model_elements.roboticsLabs.components;

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
//import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.EJSAware;
//import org.colos.ejs.model_elements.ModelElement;
//import org.colos.ejs.model_elements.ModelElementEditor;
//import org.colos.ejs.model_elements.ModelElementMultipageEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
//import org.colos.ejs.model_elements.ModelElementTabbedEditor;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.colos.roboticsLabs.components.AbstractComponent;
import org.opensourcephysics.drawing3d.Group;

/**
 * A base abstract class to simplify the creation of components Elements
 * @author Almudena Ruiz 
 */

public abstract class AbstractComponentElement extends AbstractModelElement implements EJSAware {

	static private final ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif"); // This icon is bundled with EJS
	static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
	static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0, 4, 0, 2);

	static protected final String BEGIN_LENGTH_HEADER = "<Length>";
	static protected final String END_LENGTH_HEADER = "</Length>";
	static protected final String BEGIN_WIDTH_HEADER = "<Width>";
	static protected final String END_WIDTH_HEADER = "</Width>";
	static protected final String BEGIN_HIGH_HEADER = "<High>";
	static protected final String END_HIGH_HEADER = "</High>";
	static protected final String BEGIN_GROUP3D_HEADER = "<Group3D><![CDATA[";
	static protected final String END_GROUP3D_HEADER = "</Group3D>";
	
	protected JTextField mLengthField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mWidthField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mHighField = new JTextField(); // needs to be created to avoid null references
	protected JTextField mGroup3DField = new JTextField(); // needs to be created to avoid null references
	
	private AbstractComponent mComponent=null;
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

	public void readCompleted() { setViewGroup();}

	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------

	public String getPackageList() {
		if (mGroup3DField.getText().trim().length() > 0)
			return "gnu/io/++";
		return "";
	}

	public String getDestructionCode(String _name) { return ""; }

	protected void addDeclaration(StringBuffer buffer, String _name) {
		buffer.append(_name + " = new " + getConstructorName() + "(this,");
		buffer.append(ModelElementsUtilities.getQuotedValue(mLengthField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mWidthField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mHighField.getText().trim()) + ");\n");
				//.trim()) + ",");
		//buffer.append(ModelElementsUtilities.getQuotedValue(mPasswordField
		//		.getText().trim()) + ");\n");
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
		return buffer.toString();
	}

	public String getDisplayInfo() {
		//String length = mLengthField.getText().trim();
		//String width= mWidthField.getText().trim();
		//String high= mHighField.getText().trim();
		/*if (length.length() > 0) {
			return (portNumber.length() > 0) ? "(" + portNumber + "-" + length
					+ ")" : "(" + ipAddress + ")";
		}
		if (portNumber.length() > 0)
			return "(" + portNumber + ")";*/
		return "";
	}

	public String savetoXML() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(BEGIN_LENGTH_HEADER + mLengthField.getText() + END_LENGTH_HEADER + "\n");
		buffer.append(BEGIN_WIDTH_HEADER + mWidthField.getText() + END_WIDTH_HEADER + "\n");
		buffer.append(BEGIN_HIGH_HEADER + mHighField.getText() + END_HIGH_HEADER + "\n");
		buffer.append(BEGIN_GROUP3D_HEADER + mGroup3DField.getText() + END_GROUP3D_HEADER + "\n");
		return buffer.toString();
	}

	public void readfromXML(String _inputXML) {
		mLengthField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_LENGTH_HEADER, END_LENGTH_HEADER, false));
		mWidthField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_WIDTH_HEADER, END_WIDTH_HEADER, false));
		mHighField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_HIGH_HEADER, END_HIGH_HEADER, false));
		mGroup3DField.setText(OsejsCommon.getPiece(_inputXML, BEGIN_GROUP3D_HEADER, END_GROUP3D_HEADER, false));
		setViewGroup();
	}

	abstract protected AbstractComponent createComponent();

	protected void setViewGroup() {
		String groupName = mGroup3DField.getText().trim();
		//	  System.out.println("Group = "+groupName);
		//	  System.out.println("EJS is = "+mEjs);
		if (groupName.length()<=0) {
			if (mComponent!=null) mComponent.addToViewGroup(null); 
			return;
		}
		if (mComponent==null) mComponent = createComponent();
		if (mEjs!=null) {
			Object object = mEjs.getViewElement(groupName);
			//		System.out.println("Object is = "+object);
			if (object instanceof Group) {
				//			System.out.println("Adding to robot = "+mRobot);
				mComponent.addToViewGroup((Group) object);
			}
		}
		else System.err.println ("Error: "+groupName+" is not a valid 3D Group element");
	}

	// -------------------------------
	// Help and edition
	// -------------------------------

	public void setFont(Font font) {
		mLengthField.setFont(font);
		mWidthField.setFont(font);
		mHighField.setFont(font);
		mGroup3DField.setFont(font);
	}

	protected JPanel createTopPanel(JPanel group3DPanel, JPanel lengthPanel, JPanel widthPanel, JPanel highPanel) {
		JPanel mainPanel = new JPanel(new GridLayout(4, 1));
		mainPanel.add(group3DPanel);
		mainPanel.add(lengthPanel);
		mainPanel.add(widthPanel);
		mainPanel.add(highPanel);		
		return mainPanel;
	}

	protected Component createEditor(String name, Component parentComponent,
			final ModelElementsCollection collection) {
		DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				collection.reportChange(AbstractComponentElement.this);
			}

			public void insertUpdate(DocumentEvent e) {
				collection.reportChange(AbstractComponentElement.this);
			}

			public void removeUpdate(DocumentEvent e) {
				collection.reportChange(AbstractComponentElement.this);
			}
		};

		JLabel lengthLabel = new JLabel(RES.getString("RoboticsLab.Length"),
				SwingConstants.RIGHT);
		lengthLabel.setForeground(COLOR);
		lengthLabel.setBorder(LABEL_BORDER);

		mLengthField.getDocument().addDocumentListener(documentListener);

		JButton lengthLinkButton = new JButton(LINK_ICON);
		lengthLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mLengthField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseVariable(mLengthField, "double",
						value);
				if (variable != null)
					mLengthField.setText("%" + variable + "%");
			}
		});
		
		
		JLabel widthLabel = new JLabel(RES.getString("RoboticsLab.Width"),
				SwingConstants.RIGHT);
		widthLabel.setForeground(COLOR);
		widthLabel.setBorder(LABEL_BORDER);

		mWidthField.getDocument().addDocumentListener(documentListener);
		
		JButton widthLinkButton = new JButton(LINK_ICON);
		widthLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mWidthField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseVariable(mWidthField, "double",
						value);
				if (variable != null)
					mWidthField.setText("%" + variable + "%");
			}
		});
		
		JLabel highLabel = new JLabel(RES.getString("RoboticsLab.High"),
				SwingConstants.RIGHT);
		highLabel.setForeground(COLOR);
		highLabel.setBorder(LABEL_BORDER);

		mHighField.getDocument().addDocumentListener(documentListener);

		JButton highLinkButton = new JButton(LINK_ICON);
		highLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = mHighField.getText().trim();
				if (!ModelElementsUtilities.isLinkedToVariable(value))
					value = "";
				else
					value = ModelElementsUtilities.getPureValue(value);
				String variable = collection.chooseVariable(mHighField, "double",
						value);
				if (variable != null)
					mHighField.setText("%" + variable + "%");
			}
		});
				
		JLabel group3DLabel = new JLabel(RES.getString("RoboticsLab.Group3D"),
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
		labelSet.add(lengthLabel);
		labelSet.add(widthLabel);
		labelSet.add(highLabel);
		labelSet.add(group3DLabel);

		JPanel lengthPanel = new JPanel(new BorderLayout());
		lengthPanel.add(lengthLabel, BorderLayout.WEST);
		lengthPanel.add(mLengthField, BorderLayout.CENTER);	
		lengthPanel.add(lengthLinkButton, BorderLayout.EAST);

		JPanel widthPanel = new JPanel(new BorderLayout());
		widthPanel.add(widthLabel, BorderLayout.WEST);
		widthPanel.add(mWidthField, BorderLayout.CENTER);		
		widthPanel.add(widthLinkButton, BorderLayout.EAST);
		
		
		JPanel highPanel = new JPanel(new BorderLayout());
		highPanel.add(highLabel, BorderLayout.WEST);
		highPanel.add(mHighField, BorderLayout.CENTER);	
		highPanel.add(highLinkButton, BorderLayout.EAST);

		JPanel group3DPanel = new JPanel(new BorderLayout());
		group3DPanel.add(group3DLabel, BorderLayout.WEST);
		group3DPanel.add(mGroup3DField, BorderLayout.CENTER);
		group3DPanel.add(group3DLinkButton, BorderLayout.EAST);

		JPanel topPanel = createTopPanel(group3DPanel,lengthPanel,widthPanel,highPanel);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(300, 300));
		mainPanel.add(topPanel, BorderLayout.NORTH);
		//mainPanel.add(restrictionsEditor.getComponent(collection),BorderLayout.CENTER);

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
		addToSearch(list, mLengthField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mWidthField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mHighField, info, searchString, mode, this, name,
				collection);
		addToSearch(list, mGroup3DField, info, searchString, mode, this, name,
				collection);
		return list;

	}
}



