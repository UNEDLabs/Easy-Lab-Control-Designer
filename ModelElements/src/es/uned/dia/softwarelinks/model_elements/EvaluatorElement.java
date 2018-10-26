package es.uned.dia.softwarelinks.model_elements;

import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.library.Model;

import java.io.IOException;
import java.io.StringReader;
import java.lang.String;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import org.w3c.dom.Document;


public class EvaluatorElement extends AbstractModelElement {
	public static final String ICON_FILE = "es/uned/dia/softwarelinks/resources/evaluator.png";
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon(ICON_FILE); // This icon is included in this jar

	private JTextField textInputs = new JTextField("");
	private JTextField textOutputs = new JTextField("");
	private JTextField inputsTable = new JTextField("");;	
	private JButton buttonAddInputs;
	private JButton buttonAddOutputs;
	private JButton buttonEraseInputs;
	private JRadioButton radioJava;
	private JRadioButton radioJS;
	private JRadioButton radioMATLAB;
	
	Model model;

	private int numInputs=0;
	private String numOutputs="";
	private String lang_code="";
	private String text_help_inputs="";
	private String text_help_outputs="";
	private String text_help="";
	
	/* XML Node labels for saving the state of the elements 
    */ 
   private static final String XML_NODE_LABEL_EVALUATOR = "evaluator";
   private static final String XML_NODE_LABEL_INPUTS = "inputs";
   private static final String XML_NODE_LABEL_OUTPUTS = "outputs";
   private static final String XML_NODE_LABEL_NOUTPUTS = "Noutputs";
   private static final String XML_NODE_LABEL_NINPUTS = "Ninputs";
   private static final String XML_NODE_LABEL_LANGCODE = "langCode";
   private static final String XML_NODE_LABEL_TEXTHELP_IN = "textHelpIn";
   private static final String XML_NODE_LABEL_TEXTHELP_OUT = "textHelpOut";

   private static final String JAVA = "Java";
   private static final String JAVASCRIPT = "JavaScript";
   private static final String MATLAB = "MATLAB";

	
	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------
	  
	public ImageIcon getImageIcon() { return ELEMENT_ICON; }
	  
	public String getGenericName() { return "ControllerEvaluator"; }
	
	public String getConstructorName() { return "es.uned.dia.softwarelinks.evaluator.CodeControllerEvaluator"; }
	
	public String getPackageList(){ return "bsh/++"; }
	
	public String getInitializationCode(String _name) {
	    StringBuffer buffer = new StringBuffer();
	    String state = "new String[]" + textInputs.getText();
	    String numIns = String.valueOf(numInputs);
	    String numOuts = numOutputs.toString();
	    text_help = text_help_inputs + text_help_outputs;
	    String help = text_help;
//	    System.out.println(text_help);
	    buffer.append(_name + " = new " + getConstructorName() + "("+numIns+",\""+numOuts+"\","+state+","+textOutputs.getText()+", _model, \""+lang_code+"\",\""+help+"\");\n");
//	    buffer.append("setUserData(\"_codeController\","+_name+");");
	    return buffer.toString();
	}

	public String getDestructionCode(String _name) {
		if(lang_code.equals(MATLAB)) {
			return _name+".close();\n";
		}
		return "";
	}

	
	 public String getDisplayInfo() {
         String inputs = textInputs.getText().trim();
         String outputs = textOutputs.getText().trim();
         String noutputs = this.numOutputs;
         String ninputs = String.valueOf(numInputs);
         String langcode = this.lang_code;
         String texthelp = this.text_help;
         return "("+ninputs+ "," +noutputs+ "," +inputs + "," + outputs+  "," + langcode+ "," + texthelp+")";
	 }
	
	
	public String savetoXML() {
		String result = "<" + XML_NODE_LABEL_EVALUATOR + ">" +
							"<" + XML_NODE_LABEL_INPUTS + ">" + textInputs.getText() + "</" + XML_NODE_LABEL_INPUTS + ">" +
							"<" + XML_NODE_LABEL_OUTPUTS + ">" + textOutputs.getText() + "</" + XML_NODE_LABEL_OUTPUTS + ">" +
							"<" + XML_NODE_LABEL_NOUTPUTS + ">" + this.numOutputs + "</" + XML_NODE_LABEL_NOUTPUTS + ">" +
							"<" + XML_NODE_LABEL_NINPUTS + ">" + String.valueOf(this.numInputs) + "</" + XML_NODE_LABEL_NINPUTS + ">" +
							"<" + XML_NODE_LABEL_LANGCODE + ">" + this.lang_code + "</" + XML_NODE_LABEL_LANGCODE + ">" +
							"<" + XML_NODE_LABEL_TEXTHELP_IN + ">" + this.text_help_inputs + "</" + XML_NODE_LABEL_TEXTHELP_IN + ">" +
							"<" + XML_NODE_LABEL_TEXTHELP_OUT + ">" + this.text_help_outputs + "</" + XML_NODE_LABEL_TEXTHELP_OUT + ">" +
						"</" + XML_NODE_LABEL_EVALUATOR + ">";
		return result;
      }

      /** 
       * Modified by Jes�s Chac�n <jchacon@bec.uned.es> - 27/10/2012
       * Restores the states from an XML String
       */
      public void readfromXML(String _inputXML) {
    	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	  DocumentBuilder db = null;
    	  InputSource is = new InputSource(new StringReader(_inputXML));        
    	  try {
    		  db = dbf.newDocumentBuilder();
    		  Document doc = db.parse(is);
    		  // Configuration
    		  this.textInputs.setText(doc.getElementsByTagName(XML_NODE_LABEL_INPUTS).item(0).getTextContent());
    		  this.textOutputs.setText(doc.getElementsByTagName(XML_NODE_LABEL_OUTPUTS).item(0).getTextContent());
    		  this.numOutputs = doc.getElementsByTagName(XML_NODE_LABEL_NOUTPUTS).item(0).getTextContent();
    		  this.numInputs = Integer.valueOf(doc.getElementsByTagName(XML_NODE_LABEL_NINPUTS).item(0).getTextContent());
    		  this.text_help_inputs = doc.getElementsByTagName(XML_NODE_LABEL_TEXTHELP_IN).item(0).getTextContent();
    		  this.text_help_outputs = doc.getElementsByTagName(XML_NODE_LABEL_TEXTHELP_OUT).item(0).getTextContent();
    		  this.lang_code = doc.getElementsByTagName(XML_NODE_LABEL_LANGCODE).item(0).getTextContent();
    	  } catch (ParserConfigurationException e1) {
    		  System.out.println("Error al configurar el interprete de XML.");
    	  } catch (SAXException | IOException e) {
    		  System.out.println("Error al leer el documento XML.");
    	  } catch (Exception e) {
    		  System.err.println("Fallo raro");
    	  }           
      }
	
	// -------------------------------
	// Help and edition
	// -------------------------------

	public String getTooltip() {
	    return "Code Controller";
	}

	protected String getHtmlPage() { 
		return "es/uned/dia/softwarelinks/resources/evaluator.html";
	}
	
	protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {    
		  JPanel mainPanel = new JPanel();
		  mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		  mainPanel.setBounds(100, 100, 450, 300);
		  mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		  mainPanel.setPreferredSize(new Dimension(370, 270));
		  
		  //Entradas
		  JPanel labelPanel = new JPanel();
	      JLabel textLabel = new JLabel("ENTRADAS (ej.: {\"x\",\"t\"})");
	      textLabel.setLocation(0, 0);
	      textLabel.setSize(50, 40);
	      textLabel.setHorizontalAlignment(0);
	      labelPanel.add(textLabel);
	      //mainPanel.add(labelPanel);
	      
	      JPanel contentPanel = new JPanel();
	      contentPanel.setLayout(new BorderLayout());
	      contentPanel.setBorder(new TitledBorder(null, "ENTRADAS (ej.: {\"x\",\"t\"})", TitledBorder.LEADING, TitledBorder.TOP));

	      JPanel buttonPanel = new JPanel();
	      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
	      buttonAddInputs = new JButton("+");
	      buttonPanel.add(buttonAddInputs);
	      buttonEraseInputs = new JButton("-");
	      buttonPanel.add(buttonEraseInputs);
	      buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
	      
	      JPanel textPanel = new JPanel();
	      textPanel.setLayout(new BorderLayout());
//	      textInputs.setColumns(30);
//	      textInputs.setSize(200, 30);
//	      textInputs.setPreferredSize(new Dimension(200,30));
	      textInputs.setEditable(false);
	      textPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
	      textPanel.add(textInputs, BorderLayout.CENTER);
	      
	      contentPanel.add(buttonPanel, BorderLayout.WEST);
	      contentPanel.add(textPanel, BorderLayout.CENTER);
	      
	      mainPanel.add(contentPanel);
	      
	      //Salidas
	      JPanel labelPanel2 = new JPanel();
	      JLabel textLabel2 = new JLabel("VECTOR SALIDA (ej.: u)");
	      textLabel2.setLocation(0, 0);
	      textLabel2.setSize(50, 40);
	      textLabel2.setHorizontalAlignment(0);
	      labelPanel2.add(textLabel2);
	      //mainPanel.add(labelPanel2);
	      
	      JPanel contentPanel2 = new JPanel();
	      contentPanel2.setLayout(new BorderLayout());
	      contentPanel2.setBorder(new TitledBorder(null, "VECTOR SALIDA (ej.: \"u\")", TitledBorder.LEADING, TitledBorder.TOP));
	      
	      JPanel buttonPanel2 = new JPanel();
	      buttonAddOutputs = new JButton("Select");
	      buttonPanel2.setBorder(new EmptyBorder(10, 10, 10, 10));
	      buttonPanel2.add(buttonAddOutputs);	      
	      
	      JPanel textPanel2 = new JPanel();
	      textPanel2.setLayout(new BorderLayout());
//	      textPanel2.setSize(155, 45);
//	      textOutputs.setPreferredSize(new Dimension(200,30));
//	      textOutputs.setEditable(false);
	      textPanel2.setBorder(new EmptyBorder(10, 10, 10, 10));
//	      textPanel2.add(textOutputs);
	      textPanel2.add(textOutputs, BorderLayout.CENTER);
	      
	      contentPanel2.add(buttonPanel2, BorderLayout.WEST);
	      contentPanel2.add(textPanel2, BorderLayout.CENTER);
	      
	      mainPanel.add(contentPanel2);
	      
	      /*JPanel prueba=new JPanel();
	     inputsTable.setPreferredSize(new Dimension(150,20));
	      prueba.add(inputsTable);*/
	      
	      //mainPanel.add(prueba);
	      
	      
	    //Lenguaje	      
	      JPanel contentPanel3=new JPanel();
	      contentPanel3.setLayout(new BoxLayout(contentPanel3, BoxLayout.LINE_AXIS));
	      contentPanel3.setBorder(new TitledBorder(null, "LENGUAJE INTÉRPRETE", TitledBorder.LEADING, TitledBorder.TOP));
	      
	      JPanel radioPanel1 = new JPanel();
	      JPanel radioPanel2 = new JPanel();
	      JPanel radioPanel3 = new JPanel();
	      ButtonGroup radioGroup = new ButtonGroup();
	      radioJava = new JRadioButton(JAVA);
	      radioJS = new JRadioButton(JAVASCRIPT);
	      radioMATLAB = new JRadioButton(MATLAB);
	      radioPanel1.setBorder(new EmptyBorder(5, 5, 5, 5));
	      radioPanel2.setBorder(new EmptyBorder(5, 5, 5, 5));
	      radioPanel3.setBorder(new EmptyBorder(5, 5, 5, 5));
	      
	      radioGroup.add(radioJava);
	      radioGroup.add(radioJS);
	      radioGroup.add(radioMATLAB);
	      radioJava.setSelected(true);
	      
	      radioPanel1.add(radioJava);
	      radioPanel2.add(radioJS);
	      radioPanel3.add(radioMATLAB);

	      contentPanel3.add(radioPanel1);
	      contentPanel3.add(radioPanel2);
	      contentPanel3.add(radioPanel3);
	      
	      mainPanel.add(contentPanel3);
	      
	      //Gesti�n eventos del bot�n
	            //Inputs   
          AbstractAction selectVariable = new AbstractAction("+") {
                  private static final long serialVersionUID = 1L;
                  public void actionPerformed(ActionEvent e) {
                          String vartype="double";
                          String varname="";
                          Osejs ejs = collection.getEJS();
                          // Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"                           
                          String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", vartype, inputsTable, varname, "");
                          if (variable != null) {
                        	  String textin = textInputs.getText();
                    		  int length = textin.length();
                    		  String commentVar = getVariableComment(ejs,variable);
                        	  if (length>0) {
                        		  if(!textin.contains("\"" + variable + "\"")) { //If the variable is not in the list yet...
                        			  textInputs.setText(textin.substring(0, length-1) + ", \"" + variable + "\"}");
                        			  numInputs += 1;
                        			  text_help_inputs += variable + ": " + commentVar+"$";
                        		  }
                        	  }
                        	  else {
                        		  textInputs.setText("{\""+variable+"\"}");
                        		  numInputs=1;
                        		  text_help_inputs+="$INPUTS$"+variable+": "+commentVar+"$";
                        	  }
                          }
                  }
          };

          buttonAddInputs.setAction(selectVariable);
          
          AbstractAction selectVariable1 = new AbstractAction("-"){
              private static final long serialVersionUID = 1L;
              public void actionPerformed(ActionEvent e) {
                      String vartype="double";
                      String varname="";
                      Osejs ejs = collection.getEJS();
                      // Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"                           
                      String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", vartype, inputsTable, varname, "");
                      if (variable != null) {
                    	  String textin=textInputs.getText();
                		  int length=textin.length();
                		  // Search for the variable name in the string, and deletes it
                    	  if (length>0) {
                    		  if(textin.contains("\""+variable+"\"")) { //Search for the String "variable"
                				  int index=textin.indexOf("\""+variable+"\"")+1;
                				  int lengthVar=variable.length();
                    			  if(numInputs>1) {
                    				  String aux2 = textin.substring(index+1+lengthVar,length);
                    				  if(index>2) {
                    					  String aux1 = textin.substring(0, index-3);
                    					  textInputs.setText(aux1+aux2);
                    				  }
                    				  else {
                    					  textInputs.setText("{"+aux2.substring(2));
                    				  }
                    			  }
                    			  else 
                    				  textInputs.setText("");
                    			  //Delete the variable from the help text
                    			  if(text_help_inputs.contains("$"+variable+":")) {
                    				  String commentVar=getVariableComment(ejs,variable);
                    				  text_help_inputs=text_help_inputs.replace(variable+": "+commentVar+"$","");
                    			  }
                    			  numInputs-=1;
                    		  }
                    	  }
                    	  else {
                    		  numInputs=0; 
                    	  }
                      }
              }
      };
      buttonEraseInputs.setAction(selectVariable1);

      //Outputs
      AbstractAction selectVariable2 = new AbstractAction("Select"){
    	  private static final long serialVersionUID = 1L;
    	  public void actionPerformed(ActionEvent e) {
    		  String vartype="double[]";
    		  String varname="";
    		  // Passing an empty String as the last parameter hides the default methods "_isPlaying, _isPaused, _isApplet"                           
    		  String variable = org.colos.ejs.osejs.utils.EditorForVariables.edit(collection.getEJS().getModelEditor(), "Variables", vartype, inputsTable, varname, "");
    		  if (variable != null) {
    			  textOutputs.setText("\""+variable+"\"");
    			  Osejs ejs = collection.getEJS();
    			  numOutputs = getVariableType(ejs, variable);
    			  String commentVar = getVariableComment(ejs, variable);
    			  text_help_outputs = "$OUTPUTS$" + variable + ": " + commentVar;
    		  }
    	  }
      };
      buttonAddOutputs.setAction(selectVariable2);
      //radio button Java
      AbstractAction selectRadio1 = new AbstractAction(JAVA){
    	  private static final long serialVersionUID = 1L;
    	  public void actionPerformed(ActionEvent e) {
    		  if(radioJava.isSelected()) {
    			  lang_code = JAVA;
    		  }
    	  }
      };
      radioJava.setAction(selectRadio1);
      //radio button JavaScript
      AbstractAction selectRadio2 = new AbstractAction(JAVASCRIPT){
    	  private static final long serialVersionUID = 1L;
    	  public void actionPerformed(ActionEvent e) {
    		  if(radioJS.isSelected()) {
    			  lang_code = JAVASCRIPT;
    		  }
    	  }
      };
      radioJS.setAction(selectRadio2);
      //radio button MATLAB
      AbstractAction selectMatlab = new AbstractAction(MATLAB) {
    	  private static final long serialVersionUID = 1L;
    	  public void actionPerformed(ActionEvent e) {
    		  if(radioMATLAB.isSelected()) {
    			  lang_code = MATLAB;
    		  }
    	  }
      };
      radioMATLAB.setAction(selectMatlab);
	  switch(lang_code) {
	  default:
	  case JAVA:
		  radioJava.setSelected(true);
		  break;
	  case JAVASCRIPT:
		  radioJS.setSelected(true);
		  break;
	  case MATLAB:
		  radioMATLAB.setSelected(true);
		  break;              
	  }
      
      return mainPanel;   
	} 
	
	static private String getVariableType (Osejs ejs, String varName) {
		  String info = ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
		  StringTokenizer tkn = new StringTokenizer (info,"\n");
		  while (tkn.hasMoreTokens()) {
		    String line = tkn.nextToken();
		    StringTokenizer tkn2 = new StringTokenizer (line,":");
		    String type = tkn2.nextToken().trim();
		    String name = tkn2.nextToken().trim();
		    if (name.equals(varName)) {
		    	type = tkn2.nextToken().trim();
		    	type = type.substring(type.indexOf("[") + 1);
		    	type = type.substring(0, type.indexOf("]"));
		    	return type;
		    }
		  }
		  return null; // variable not found
		}
	
	static private String getVariableComment (Osejs ejs, String varName) {
		  String info = ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
		  StringTokenizer tkn = new StringTokenizer (info,"\n");
		  while (tkn.hasMoreTokens()) {
		    String line = tkn.nextToken();
		    StringTokenizer tkn2 = new StringTokenizer (line,":");
		    String type = tkn2.nextToken().trim();
		    String name = tkn2.nextToken().trim();
		    String comment ="";
		    if (name.equals(varName)) {
		    	type=tkn2.nextToken().trim();
		    	comment =tkn2.nextToken().trim();
		    	return comment;
		    }
		  }
		  return null; // variable not found
		}
}