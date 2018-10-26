package org.colos.ejs.model_elements.roboticsLabs.components.Belt;


import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.roboticsLabs.components.AbstractComponentElement;

import org.colos.roboticsLabs.components.AbstractComponent;

public class BeltElement extends AbstractComponentElement{	
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/roboticsLabs/components/Belt/Belt.gif"); // This icon is included in this jar

	private JCheckBox mEncoderCheckbox = new JCheckBox("Add Encoder",true);
	private boolean mEncoder;
	protected AbstractComponent createComponent(){
		return new org.colos.roboticsLabs.components.Belt(mEncoder);
	}
	
	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------
	
	public String getPackageList() {
		//if (mGroup3DField.getText().trim().length() > 0)
		//	return "org/colos/ejs/robots/obj_files/TX60L/++";
		return "";
	}

	@Override
	protected void addDeclaration(StringBuffer buffer, String _name) {				
		mEncoder = mEncoderCheckbox.isSelected();
		buffer.append(_name + " = new " + getConstructorName() + "(this,");
		buffer.append(ModelElementsUtilities.getQuotedValue(mLengthField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mWidthField.getText().trim()) + ",");
		buffer.append(ModelElementsUtilities.getQuotedValue(mHighField.getText().trim()) + ",");
		buffer.append(mEncoder + ");\n");
	}

	public ImageIcon getImageIcon() {
		return ELEMENT_ICON;
	}

	public String getGenericName() {
		return "Belt";
	}

	public String getConstructorName() {
		return "org.colos.ejs.model_elements.roboticsLabs.components.Belt.BeltAdapter";
	}
	
	
	// -------------------------------
	// Help and edition
	// -------------------------------

	public String getTooltip() {
		return "Adds a belt to the laboratory";
	}
	
	@Override
	protected String getHtmlPage() {
		return "org/colos/ejs/model_elements/roboticsLabs/components/Belt/Belt.html";
	}

	
	@Override
	protected JPanel createTopPanel( JPanel group3DPanel, JPanel lengthPanel, JPanel widthPanel, JPanel highPanel) {
		JPanel topPanel = new JPanel(new GridLayout(5, 1));
		topPanel.add(lengthPanel);
		topPanel.add(widthPanel);
		topPanel.add(highPanel);	
		topPanel.add(group3DPanel);
		topPanel.add(mEncoderCheckbox);
		return topPanel;
	}
}
