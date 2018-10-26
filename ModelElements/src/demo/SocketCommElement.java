package demo;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.colos.ejs.osejs.utils.MenuUtils;

public class SocketCommElement extends AbstractModelElement 
                               implements ModelElementMultipageEditor {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/input_output/SocketComm.png");
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));

  static private final String[] mKeywords = {"Input", "Output"};
  static private String[] smBeginHeaders = new String[mKeywords.length];
  static private String[] smEndHeaders   = new String[mKeywords.length];

  static {
    for (int i=0, n=mKeywords.length; i<n; i++) {
      smBeginHeaders[i] = "<"  + mKeywords[i] + ">\n";
      smEndHeaders[i]   = "</" + mKeywords[i]+">\n";
    }
  }
  
  private ModelElementEditor inputEditor, outputEditor;
  
  private JRadioButton[] buttons;
  private CardLayout cardLayout;
  private JPanel topPanel;

  {
    inputEditor = new ModelElementEditor(this,this);
    inputEditor.setName(mKeywords[0]);
    inputEditor.readPlainCode("// "+ RES.getString("External.Input.ToolTip")+" :_input");
    
    outputEditor = new ModelElementEditor(this,this);
    outputEditor.setName(mKeywords[1]);
    outputEditor.readPlainCode("// "+ RES.getString("External.Output.ToolTip")+"\n\nreturn \"\";\n");
  }

  public void showPanel(ModelElementsCollection collection, String elementName, String keyword) {
    showEditor(elementName, null, collection);
    for (int i=0; i<mKeywords.length; i++) {
      if (mKeywords[i].equals(keyword)) {
        cardLayout.show (topPanel,keyword);
        buttons[i].setSelected(true);
        return;
      }
    }
  }

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "SocketComm"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.SocketComm"; }
  
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name + "!=null) "+_name+".release();\n");
    buffer.append("try {\n");
    buffer.append("  "+_name + " = new " + getConstructorName() + "(this,8887) {\n");
    // variables
    buffer.append("    public void processInputData(String _input) {\n");
    buffer.append(inputEditor.generateCode(_name, "    "));
    buffer.append("    }\n\n");
    // start
    buffer.append("    public String getOutputData() {\n");
    buffer.append(outputEditor.generateCode(_name, "      "));
    buffer.append("    }\n\n");

    buffer.append("  }; // end of SocketCommn\n");
    buffer.append("  "+_name + ".connect();\n");
    buffer.append("} catch (Exception _exc) {\n");
    buffer.append("  _exc.printStackTrace();\n");
    buffer.append("  "+_name + " = null;\n");
    buffer.append("}\n");
    return buffer.toString();
  }
  
  public String getDestructionCode(String _name) { return _name+".release();"; }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    // Variables
    buffer.append(smBeginHeaders[0]);
    buffer.append(inputEditor.saveStringBuffer());
    buffer.append(smEndHeaders[0]);
    // Start
    buffer.append(smBeginHeaders[1]);
    buffer.append(outputEditor.saveStringBuffer());
    buffer.append(smEndHeaders[1]);

    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    inputEditor.readXmlString(OsejsCommon.getPiece(_inputXML,smBeginHeaders[0],smEndHeaders[0],false));
    outputEditor.readXmlString (OsejsCommon.getPiece(_inputXML,smBeginHeaders[1],smEndHeaders[1],false));
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "creates a socket communication mechanism"; }
  
  public void setFont(Font font) {
    inputEditor.setFont(font);
    outputEditor.setFont(font);
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/input_output/SocketComm.html"; }

  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    cardLayout = new CardLayout ();
    final JPanel topPanel = new JPanel(cardLayout);

    ActionListener al = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        cardLayout.show (topPanel,evt.getActionCommand());
      }
    };
    Border buttonsBorder = BorderFactory.createEmptyBorder(0,6,0,6);
    Box toolbar = Box.createHorizontalBox();
    Insets inset = new java.awt.Insets(0,3,0,3);

    Font font = InterfaceUtils.font(null,Osejs.getResources().getString("Model.TitleFont"));

    buttons = MenuUtils.createRadioGroup (mKeywords,"External.",al,false);
    for (int i=0; i<buttons.length; i++) {
      buttons[i].setBorder(buttonsBorder);
      buttons[i].setFont(font);
      buttons[i].setForeground(COLOR);
      buttons[i].setMargin(inset);
      toolbar.add (buttons[i]);
    }

    JComponent varComp = inputEditor.getComponent(collection);
    varComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (varComp,mKeywords[0]);
    
    JComponent startComp = outputEditor.getComponent(collection);
    startComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (startComp,mKeywords[1]);

    cardLayout.show (topPanel,mKeywords[0]);
    buttons[0].setSelected(true);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(toolbar,BorderLayout.NORTH);
    mainPanel.add(topPanel,BorderLayout.CENTER);

    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    list.addAll(inputEditor.search(info, searchString, mode, name, collection));
    list.addAll(outputEditor.search(info, searchString, mode, name, collection));
    return list;
  }
  
}
