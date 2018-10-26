package org.colos.ejs.model_elements.roboticsLabs.robots.TX60L;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.roboticsLabs.robots.AbstractRobotElement;
import org.colos.roboticsLabs.robots.AbstractRobot;

public class RobotTX60LElement extends AbstractRobotElement {
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/roboticsLabs/robots/TX60L/TX60L.gif"); // This icon is included in this jar
	private JCheckBox mBasementCheckbox = new JCheckBox("Add Basement", true);
//	private JCheckBox mBeltCheckbox = new JCheckBox("Add Belt", true);
	private JCheckBox mToolCheckbox = new JCheckBox("Add Tool", true);
	private boolean mBasement, mTool;//mBelt;

	protected AbstractRobot createRobot() {
		//return new org.colos.robots.RobotTX60LWithComponents(mBasement, mBelt, mTool);
		return new org.colos.roboticsLabs.robots.RobotTX60LWithComponents(mBasement, mTool);
	}

	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------
	
	public String getPackageList() {
		if (mGroup3DField.getText().trim().length() > 0)
			return "org/colos/roboticsLabs/robots/obj_files/TX60L/++";
		return "";
	}

	@Override
	protected void addDeclaration(StringBuffer buffer, String _name) {
		mBasement = mBasementCheckbox.isSelected();
		//mBelt = mBeltCheckbox.isSelected();
		mTool = mToolCheckbox.isSelected();
		buffer.append(_name + " = new " + getConstructorName() + "(this,");
		buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mUserField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mPasswordField.getText().trim()) + ",");
		buffer.append(mBasement + ",");
	//	buffer.append(mBelt + ",");
		buffer.append(mTool + ");\n");
	}

	public ImageIcon getImageIcon() {
		return ELEMENT_ICON;
	}

	public String getGenericName() {
		return "RobotTX60L";
	}

	public String getConstructorName() {
		return "org.colos.ejs.model_elements.roboticsLabs.robots.TX60L.RobotTX60LWithComponentsAdapter";
	}

	// -------------------------------
	// Help and edition
	// -------------------------------

	public String getTooltip() {
		return "provides access to a TX60L";
	}

	@Override
	protected String getHtmlPage() {
		return "org/colos/ejs/model_elements/roboticsLabs/robots/TX60L/RobotTX60L.html";
	}

	@Override
	protected JPanel createTopPanel(JPanel urlPanel, JPanel portPanel,
			JPanel userPanel, JPanel passwordPanel, JPanel group3DPanel) {
		JPanel topPanel = new JPanel(new GridLayout(4, 2));
		topPanel.add(urlPanel);
		topPanel.add(portPanel);
		topPanel.add(userPanel);
		topPanel.add(passwordPanel);
		topPanel.add(group3DPanel);
		topPanel.add(mBasementCheckbox);
		//topPanel.add(mBeltCheckbox);
		topPanel.add(mToolCheckbox);
		return topPanel;
	}

}
