package org.colos.ejs.model_elements.parallel_ejs;

import java.awt.Component;
import java.awt.Font;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.OsejsCommon;

public class EJSWorkerTaskElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/parallel_ejs/EJSWorkerTask.png");

  static private final String BEGIN_RUNCODE_HEADER = "<Run>";
  static private final String END_RUNCODE_HEADER = "</Run>";

  private ModelElementEditor runCodeEditor;
  
  {
    runCodeEditor = new ModelElementEditor (this,null, true);
    runCodeEditor.readPlainCode("// "+ RES.getString("CodeWizard.WriteCodeHere"));
  }

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "WorkerTask"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.parallel_ejs.EJSWorkerTask"; }
  
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name + "!=null) "+_name+".release();\n");
    buffer.append(_name + " = new " + getConstructorName() + "(this,new java.lang.Runnable() {\n");
    // Region run
    buffer.append("  public void run() { // thread's run method\n");
    buffer.append(runCodeEditor.generateCode(_name, "    "));
    buffer.append("  }\n\n");

    buffer.append("}); // end of EJSWorkerTask\n");
    return buffer.toString();
  }
  
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_RUNCODE_HEADER);
    buffer.append(runCodeEditor.saveStringBuffer());
    buffer.append(END_RUNCODE_HEADER);
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    runCodeEditor.readXmlString(OsejsCommon.getPiece(_inputXML,BEGIN_RUNCODE_HEADER,END_RUNCODE_HEADER,false));
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "defines a code that can be run in a separate thread"; }
  
  public void setFont(Font font) {
    runCodeEditor.setFont(font);
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/parallel_ejs/EJSWorkerTask.html"; }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JComponent rrComp = runCodeEditor.getComponent(collection);
    rrComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    return rrComp;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    list.addAll(runCodeEditor.search(info, searchString, mode, name, collection));
    return list;
  }
  
}
