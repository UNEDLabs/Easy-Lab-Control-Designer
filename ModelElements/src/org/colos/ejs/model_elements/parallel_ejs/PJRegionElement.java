package org.colos.ejs.model_elements.parallel_ejs;

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

public class PJRegionElement extends AbstractModelElement 
                               implements ModelElementMultipageEditor {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/parallel_ejs/PJRegion.png");

//  static private final ImageIcon LINK_ICON = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
//  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

//  static private final String BEGIN_REPEAT_HEADER = "<Repeat>";
//  static private final String END_REPEAT_HEADER = "</Repeat>";

  static private final String[] mKeywords = {"Variables", "Preliminary", "Parallel", "Final"};
  static private String[] smBeginHeaders = new String[mKeywords.length];
  static private String[] smEndHeaders   = new String[mKeywords.length];

  static {
    for (int i=0, n=mKeywords.length; i<n; i++) {
      smBeginHeaders[i] = "<"  + mKeywords[i] + ">\n";
      smEndHeaders[i]   = "</" + mKeywords[i]+">\n";
    }
  }
  
  private ModelElementEditor variablesEditor, preliminaryEditor, parallelEditor, finalEditor;
//  private JTextField repeatField = new JTextField();  // needs to be created to avoid null references

  private JRadioButton[] buttons;
  private CardLayout cardLayout;
  private JPanel topPanel;

  {
    variablesEditor = new ModelElementEditor(this,this);
    variablesEditor.setName(mKeywords[0]);
    variablesEditor.readPlainCode("// "+ RES.getString("ParallelCode.Variables.ToolTip")+"\n\n");
    
    preliminaryEditor = new ModelElementEditor(this,this);
    preliminaryEditor.setName(mKeywords[1]);
    preliminaryEditor.readPlainCode("// "+ RES.getString("ParallelCode.Preliminary.ToolTip")+"\n\n");

    parallelEditor = new ModelElementEditor(this,this);
    parallelEditor.setName(mKeywords[2]);
    parallelEditor.addPragma("CRITICAL", "try { \n        region().critical (new edu.rit.pj.ParallelSection() { public void run() {\n","  }});\n      } catch (Exception _exc) { _exc.printStackTrace();}\n");
    parallelEditor.readPlainCode("// "+ RES.getString("ParallelCode.Parallel.ToolTip")+"\n\n");
    
    finalEditor = new ModelElementEditor(this,this);
    finalEditor.setName(mKeywords[3]);
    finalEditor.readPlainCode("// "+ RES.getString("ParallelCode.Final.ToolTip")+"\n\n");
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
  
  public String getGenericName() { return "pjRegion"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.parallel_ejs.EJSParallelCode"; }
  
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name + "!=null) "+_name+".release();\n");
//    buffer.append(_name + " = new " + getConstructorName() + "(\""+_name+"\",this, new org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion() {\n");
    buffer.append(_name + " = new " + getConstructorName() + "(\""+_name+"\",this) {\n");
    buffer.append("    public org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion createRegion() {\n");
    buffer.append("      return new org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion() {\n");

    // variables
    buffer.append(variablesEditor.generateCode(_name, "    "));
    buffer.append("\n");
    // preliminary
    buffer.append("    public void _preliminaryCode() {\n");
    buffer.append(preliminaryEditor.generateCode(_name, "      "));
    buffer.append("    }\n\n");
    // parallel
    buffer.append("    public void _parallelCode() throws Exception {\n");
    buffer.append("      int _threadCount = isExecutingInParallel() ? getThreadCount() : 1;\n");
    buffer.append("      int _threadIndex = isExecutingInParallel() ? getThreadIndex() : 0;\n");
    buffer.append(parallelEditor.generateCode(_name, "      "));
    buffer.append("    }\n\n");
    // final
    buffer.append("    public void _finalCode() {\n");
    buffer.append(finalEditor.generateCode(_name, "      "));
    buffer.append("    }\n\n");
    
    // repeat
//    String repeatCode = repeatField.getText().trim();
//    if (repeatCode.length()>0) {
//      buffer.append("  public boolean _shouldRepeat() {\n");
//      buffer.append("    return "+repeatCode+";\n");
//      buffer.append("  }\n\n");    
//    }
    buffer.append("    }; // end of EJSParallelRegion\n");
    buffer.append("  } // end of createRegion\n");
    buffer.append("}; // end of EJSParallelCode\n");
        return buffer.toString();
  }
  
  public String getDestructionCode(String _name) { return _name+".release();"; }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    // Variables
    buffer.append(smBeginHeaders[0]);
    buffer.append(variablesEditor.saveStringBuffer());
    buffer.append(smEndHeaders[0]);
    // Start
    buffer.append(smBeginHeaders[1]);
    buffer.append(preliminaryEditor.saveStringBuffer());
    buffer.append(smEndHeaders[1]);
    // Run
    buffer.append(smBeginHeaders[2]);
    buffer.append(parallelEditor.saveStringBuffer());
    buffer.append(smEndHeaders[2]);
    // Finish
    buffer.append(smBeginHeaders[3]);
    buffer.append(finalEditor.saveStringBuffer());
    buffer.append(smEndHeaders[3]);

//    buffer.append(BEGIN_REPEAT_HEADER+repeatField.getText()+END_REPEAT_HEADER + "\n");

    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    variablesEditor.readXmlString(OsejsCommon.getPiece(_inputXML,smBeginHeaders[0],smEndHeaders[0],false));
    preliminaryEditor.readXmlString (OsejsCommon.getPiece(_inputXML,smBeginHeaders[1],smEndHeaders[1],false));
    String parallelCode = OsejsCommon.getPiece(_inputXML,smBeginHeaders[2],smEndHeaders[2],false);
    if (parallelCode!=null) { 
      parallelEditor.readXmlString  (parallelCode);
    } 
    else { // Try old version. Backwards compatibility
      parallelCode = OsejsCommon.getPiece(_inputXML,"<RegionRun>","</RegionRun>",false);
      if (parallelCode!=null) {
        String code = OsejsCommon.getPiece(parallelCode,"<Variables><![CDATA[","]]></Variables>",false);
        String comment = OsejsCommon.getPiece(parallelCode,"<Run><![CDATA[","]]></Run>",false);
        if (code!=null) parallelEditor.readPlainCode(code,comment); 
      }
    }
    finalEditor.readXmlString  (OsejsCommon.getPiece(_inputXML,smBeginHeaders[3],smEndHeaders[3],false));
//    repeatField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_REPEAT_HEADER,END_REPEAT_HEADER,false));
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "defines a piece of code to be run in parallel to an EJS model"; }
  
  public void setFont(Font font) {
    variablesEditor.setFont(font);
    preliminaryEditor.setFont(font);
    parallelEditor.setFont(font);
    finalEditor.setFont(font);
//    repeatField.setFont(font);
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/parallel_ejs/PJRegion.html"; }

  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    cardLayout = new CardLayout ();
    topPanel = new JPanel(cardLayout);

    ActionListener al = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        cardLayout.show (topPanel,evt.getActionCommand());
      }
    };
    
//    DocumentListener documentListener = new DocumentListener() {
//      public void changedUpdate(DocumentEvent e) { collection.reportChange(EJSParallelCodeElement.this); }
//      public void insertUpdate(DocumentEvent e)  { collection.reportChange(EJSParallelCodeElement.this); }
//      public void removeUpdate(DocumentEvent e)  { collection.reportChange(EJSParallelCodeElement.this); }
//    };
    
    Border buttonsBorder = BorderFactory.createEmptyBorder(0,6,0,6);
    Box toolbar = Box.createHorizontalBox();
    Insets inset = new java.awt.Insets(0,3,0,3);

    Font font = InterfaceUtils.font(null,Osejs.getResources().getString("Model.TitleFont"));

    buttons = MenuUtils.createRadioGroup (mKeywords,"ParallelCode.",al,false);
    for (int i=0; i<buttons.length; i++) {
      buttons[i].setBorder(buttonsBorder);
      buttons[i].setFont(font);
      buttons[i].setForeground(COLOR);
      buttons[i].setMargin(inset);
      toolbar.add (buttons[i]);
    }

    JComponent varComp = variablesEditor.getComponent(collection);
    varComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (varComp,mKeywords[0]);
    
    JComponent preliminaryComp = preliminaryEditor.getComponent(collection);
    preliminaryComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (preliminaryComp,mKeywords[1]);

    JComponent parallelComp = parallelEditor.getComponent(collection);
    parallelComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (parallelComp,mKeywords[2]);

//    repeatField.getDocument().addDocumentListener (documentListener);
//    OneLine repeatLine = new OneLine (collection, null, repeatField, "Repeat", "boolean");

//    JComponent finalComp = new JPanel(new BorderLayout()); 
//    finalComp.add(finalEditor.getComponent(collection),BorderLayout.CENTER);
//    finalComp.add(repeatLine,BorderLayout.SOUTH);
    JComponent finalComp = finalEditor.getComponent(collection);
    finalComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (finalComp,mKeywords[3]);

    cardLayout.show (topPanel,mKeywords[2]);
    buttons[2].setSelected(true);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(toolbar,BorderLayout.NORTH);
    mainPanel.add(topPanel,BorderLayout.CENTER);

    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    list.addAll(variablesEditor.search(info, searchString, mode, name, collection));
    list.addAll(preliminaryEditor.search(info, searchString, mode, name, collection));
    list.addAll(parallelEditor.search(info, searchString, mode, name, collection));
    list.addAll(finalEditor.search(info, searchString, mode, name, collection));
    return list;
  }
  
//  static private class OneLine extends JPanel {
//    JLabel label;
//    JButton linkButton;
//
//    OneLine (final ModelElementsCollection collection, Set<JLabel> labelSet, 
//            final JTextField field, String labelText, final String type) {
//      label = new JLabel(RES.getString("ParallelCode."+labelText),SwingConstants.RIGHT);
//      label.setForeground(COLOR);
//      label.setBorder(LABEL_BORDER);
//      label.setToolTipText(RES.getString("ParallelCode."+labelText+".ToolTip"));
//      if (labelSet!=null) labelSet.add(label);
//
//      linkButton = new JButton(LINK_ICON);
//      linkButton.addActionListener(new ActionListener(){
//        public void actionPerformed(ActionEvent e) {
//          String value = field.getText().trim();
//          if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
//          else value = ModelElementsUtilities.getPureValue(value);
//          String variable = collection.chooseVariable(field,type, value);
//          if (variable!=null) field.setText(variable);
//        }
//      });
//      
//      setLayout(new BorderLayout());
//      add(label,BorderLayout.WEST);
//      add(field,BorderLayout.CENTER);
//      add(linkButton,BorderLayout.EAST);
//    }
//  }
  
}
