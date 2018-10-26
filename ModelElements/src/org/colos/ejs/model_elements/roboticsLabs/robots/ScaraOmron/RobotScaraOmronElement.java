package org.colos.ejs.model_elements.roboticsLabs.robots.ScaraOmron;


import java.awt.GridLayout;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.roboticsLabs.robots.AbstractRobotElement;
import org.colos.roboticsLabs.robots.AbstractRobot;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class RobotScaraOmronElement extends AbstractRobotElement {
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/roboticsLabs/robots/ScaraOmron/Scara_Omron.gif"); // This icon is included in this jar
																					
	private JCheckBox mBasementCheckbox = new JCheckBox("Add Basement",true);
	private boolean mBasement;
	
	protected AbstractRobot createRobot() {
		return new org.colos.roboticsLabs.robots.RobotScaraOmronWithComponents(mBasement);
	}

	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------
	
	public String getPackageList() {
		if (mGroup3DField.getText().trim().length() > 0)
			return "org/colos/roboticsLabs/robots/obj_files/ScaraOmron/++";
		return "";
	}
	
	@Override
	  protected void addDeclaration(StringBuffer buffer, String _name) {
			mBasement = mBasementCheckbox.isSelected();
			buffer.append(_name + " = new " + getConstructorName() + "(this,");
			buffer.append(ModelElementsUtilities.getQuotedValue(mIPField.getText()
					.trim()) + ",");
			buffer.append(ModelElementsUtilities.getQuotedValue(mPortNumberField
					.getText().trim()) + ",");
			buffer.append(ModelElementsUtilities.getQuotedValue(mUserField.getText()
					.trim()) + ",");
			buffer.append(ModelElementsUtilities.getQuotedValue(mPasswordField
					.getText().trim()) + ",");
			buffer.append(mBasement + ");\n");
		}
	
	public ImageIcon getImageIcon() {
		return ELEMENT_ICON;
	}

	public String getGenericName() {
		return "RobotScaraOmron";
	}

	public String getConstructorName() {
		return "org.colos.ejs.model_elements.roboticsLabs.robots.ScaraOmron.RobotScaraOmronWithComponentsAdapter";
	}

	// -------------------------------
	// Help and edition
	// -------------------------------

	public String getTooltip() {
		return "provides access to a Scara Omron";
	}

	@Override
	protected String getHtmlPage() {
		return "org/colos/ejs/model_elements/roboticsLabs/robots/ScaraOmron/RobotScaraOmron.html";
	}

	@Override
	  protected JPanel createTopPanel(JPanel urlPanel, JPanel portPanel, JPanel userPanel, JPanel passwordPanel, JPanel group3DPanel) {
		  JPanel topPanel = new JPanel(new GridLayout(3, 2));
		  topPanel.add(urlPanel);
		  topPanel.add(portPanel);
		  topPanel.add(userPanel);
		  topPanel.add(passwordPanel);
		  topPanel.add(group3DPanel);
		  topPanel.add(mBasementCheckbox);
		  return topPanel;
	}

}
