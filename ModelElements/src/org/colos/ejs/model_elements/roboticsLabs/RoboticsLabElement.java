package org.colos.ejs.model_elements.roboticsLabs;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import org.colos.roboticsLabs.RoboticsLab;

public class RoboticsLabElement extends AbstractModelElement implements EJSAware {
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/roboticsLabs/RoboticsLab.gif"); // This icon is included in this jar

	static protected final String BEGIN_RESTRICTIONS_HEADER = "<Restrictions>\n";
	static protected final String END_RESTRICTIONS_HEADER = "</Restrictions>\n";
	protected ModelElementTabbedEditor restrictionsEditor = new MyModelElementTabbedEditor(this, null, "LabRestriction");
	
	private Osejs mEjs=null;

	protected RoboticsLab createComponent(){ return new org.colos.roboticsLabs.RoboticsLab();}
	
	// -------------------------------
	// Implementation of EJSAware
	// -------------------------------

	public void setEJS(Osejs ejs) {
		mEjs = ejs;
		//setViewGroup();
	}

	public void readCompleted() {
		//setViewGroup();}
		}

	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------
	
	public String getPackageList() {return "";}
	
	public String getDestructionCode(String _name) { return ""; }
	
	
	protected void addDeclaration(StringBuffer buffer, String _name) {				
		buffer.append(_name + " = new " + getConstructorName() + "(this)"+";\n");
	}
	
	public String getInitializationCode(String _name) {
		StringBuffer buffer = new StringBuffer();	
		addDeclaration(buffer,_name);
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
	
	public String getDisplayInfo() {return "";}
	
	public String savetoXML() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(BEGIN_RESTRICTIONS_HEADER+ restrictionsEditor.saveStringBuffer() + END_RESTRICTIONS_HEADER + "\n");
		return buffer.toString();
	}

	public void readfromXML(String _inputXML) {
		restrictionsEditor.readXmlString(OsejsCommon.getPiece(_inputXML,BEGIN_RESTRICTIONS_HEADER, END_RESTRICTIONS_HEADER, false));
	}

	public ImageIcon getImageIcon() {return ELEMENT_ICON;}

	public String getGenericName() {return "RoboticsLab";}

	public String getConstructorName() {return "org.colos.ejs.model_elements.roboticsLabs.RoboticsLabAdapter";}
	
	
	// -------------------------------
	// Help and edition
	// -------------------------------

	public void setFont(Font font) {restrictionsEditor.setFont(font);}
	
	public String getTooltip() {return "Defining a new Robotics Lab";}

	protected String getHtmlPage() {return "org/colos/ejs/model_elements/roboticsLabs/RoboticsLab.html";}
	
	
	protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
	//Borrar esto 	
	/*	DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				collection.reportChange(RoboticsLabElement.this);
			}

			public void insertUpdate(DocumentEvent e) {
				collection.reportChange(RoboticsLabElement.this);
			}

			public void removeUpdate(DocumentEvent e) {
				collection.reportChange(RoboticsLabElement.this);
			}
		};
		*/
	//hasta aqui	
		
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(500, 400));
	    mainPanel.add(restrictionsEditor.getComponent(collection),BorderLayout.CENTER);    
		return mainPanel;
	}

	public java.util.List<ModelElementSearch> search(String info,
			String searchString, int mode, String name, ModelElementsCollection collection) {
		java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
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

