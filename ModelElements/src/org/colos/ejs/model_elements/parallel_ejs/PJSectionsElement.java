package org.colos.ejs.model_elements.parallel_ejs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.colos.ejs.osejs.utils.MenuUtils;
import org.colos.ejs.osejs.utils.TwoStrings;

public class PJSectionsElement extends AbstractModelElement 
                               implements ModelElementMultipageEditor {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/parallel_ejs/PJSections.png");
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));

  static private final String BEGIN_ACTION_MODE_HEADER = "<BarrierActionMode>";
  static private final String END_ACTION_MODE_HEADER = "</BarrierActionMode>";
  
  static private final String[] mKeywords = {"RegionRun", "Sections", "Barrier"};
  static private String[] smBeginHeaders = new String[mKeywords.length];
  static private String[] smEndHeaders   = new String[mKeywords.length];

  static private final org.colos.ejs.osejs.utils.ResourceUtil res = Osejs.getResources();

  static private final TwoStrings sSectionCode = new TwoStrings("try { \n  region().critical (new edu.rit.pj.ParallelSection() { public void run() {\n","}});\n} catch (Exception _exc) { _exc.printStackTrace();};\n");
  static private final String defaultRegionRunCode = 
    "// "+ RES.getString("ParallelCode.Parallel.ToolTip")+"\n\n" + 
//      "// "+ res.getString("ParallelJava."+mKeywords[0]+".ToolTip")+"\n\n"+
    "// "+RES.getString("ParallelJava.DontTouchThis")+":\n"+
    "execute (_sections, _barrierAction);\n";
    
  static {
    for (int i=0, n=mKeywords.length; i<n; i++) {
      smBeginHeaders[i] = "<"  + mKeywords[i] + ">\n";
      smEndHeaders[i]   = "</" + mKeywords[i]+">\n";
    }
  }
  
  private ModelElementCardEditor regionRunEditor;
  private ModelElementTabbedEditor sectionsEditor;
  private ModelElementEditor barrierEditor;
  
  private JComboBox<String> barrierActionCombo;
  
  private JRadioButton[] buttons;
  private CardLayout cardLayout;
  private JPanel topPanel;

  {
    regionRunEditor = new ModelElementCardEditor (this,this, "ParallelCode", mKeywords[0], new String[]{"Variables","Preliminary", "Parallel", "Final"});
    regionRunEditor.getEditor(0).readPlainCode("// "+ RES.getString("ParallelCode.Variables.ToolTip")+"\n\n");
    regionRunEditor.getEditor(1).readPlainCode("// "+ RES.getString("ParallelCode.Preliminary.ToolTip")+"\n\n");
    regionRunEditor.getEditor(2).readPlainCode(defaultRegionRunCode);
    regionRunEditor.getEditor(2).addPragma("CRITICAL", sSectionCode);
    regionRunEditor.getEditor(3).readPlainCode("// "+ RES.getString("ParallelCode.Final.ToolTip")+"\n\n");

    sectionsEditor = new ModelElementTabbedEditor (this, this, mKeywords[1]);
    sectionsEditor.addPragma("CRITICAL", sSectionCode);
    
    barrierEditor = new ModelElementEditor (this,this);
    barrierEditor.setName(mKeywords[2]);
    barrierEditor.readPlainCode("// "+ RES.getString("ParallelJava.Barrier.ToolTip"));
    
    barrierActionCombo = new JComboBox<String>();
    barrierActionCombo.addItem("WAIT");
    barrierActionCombo.addItem("NO_WAIT");
    barrierActionCombo.addItem("CUSTOM");
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
  
  public String getGenericName() { return "pjSections"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.parallel_ejs.EJSParallelCode"; }
  
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name + "!=null) "+_name+".release();\n");
//    buffer.append(_name + " = new " + getConstructorName() + "(\""+_name+"\",this, new org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion() {\n");
    buffer.append(_name + " = new " + getConstructorName() + "(\""+_name+"\",this) {\n");
    buffer.append("    public org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion createRegion() {\n");
    buffer.append("      return new org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion() {\n");
    // Region run's variables
    buffer.append(regionRunEditor.getEditor(0).generateCode(_name, "    "));
    buffer.append("\n");

    // Barrier action
    buffer.append("    edu.rit.pj.BarrierAction _barrierAction = ");
    switch (barrierActionCombo.getSelectedIndex()) {
      case 0 : buffer.append("edu.rit.pj.BarrierAction.WAIT;\n\n"); break;
      case 1 : buffer.append("edu.rit.pj.BarrierAction.NO_WAIT;\n\n"); break;
      default :
        buffer.append("new edu.rit.pj.BarrierAction() {\n");
        buffer.append("      public void run() throws Exception {\n");
        buffer.append(barrierEditor.generateCode(_name, "        "));
        buffer.append("      }\n");
        buffer.append("    };\n\n");
        break;
    }
    
    // preliminary
    buffer.append("    public void _preliminaryCode() {\n");
    buffer.append(regionRunEditor.getEditor(1).generateCode(_name, "      "));
    buffer.append("    }\n\n");
    // parallel
    buffer.append("    public void _parallelCode() throws Exception {\n");
    buffer.append("      edu.rit.pj.ParallelSection[] _sections = new edu.rit.pj.ParallelSection["+sectionsEditor.getActivePageCount()+"];\n");

    // Sections
    java.util.List<ModelElementEditor> pList = sectionsEditor.getEditorList();
    int nSections =  pList.size();
    for (int i=0, c=0; i<nSections; i++)  {
      ModelElementEditor editor = pList.get(i);
      if (!editor.isEditable()) continue;
      buffer.append("      _sections["+(c++)+"] = new edu.rit.pj.ParallelSection() {\n");
      buffer.append("        public void run() throws Exception { // Section's run method\n");
      buffer.append(editor.generateCode(_name+"."+editor.getName(), "          "));
      buffer.append("        }\n");
      buffer.append("      };\n\n");
    }
    
    buffer.append(regionRunEditor.getEditor(2).generateCode(_name, "      "));
    buffer.append("    }\n\n");
    // final
    buffer.append("    public void _finalCode() {\n");
    buffer.append(regionRunEditor.getEditor(3).generateCode(_name, "      "));
    buffer.append("    }\n\n");

    buffer.append("    }; // end of EJSParallelRegion\n");
    buffer.append("  } // end of createRegion\n");
    buffer.append("}; // end of EJSParallelCode\n");
    return buffer.toString();
  }
  
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    // Region run
    buffer.append(smBeginHeaders[0]);
    buffer.append(regionRunEditor.saveStringBuffer());
    buffer.append(smEndHeaders[0]);
    // Sections
    buffer.append(smBeginHeaders[1]);
    buffer.append(sectionsEditor.saveStringBuffer());
    buffer.append(smEndHeaders[1]);
    // Barrier
    buffer.append(smBeginHeaders[2]);
    buffer.append(barrierEditor.saveStringBuffer());
    buffer.append(smEndHeaders[2]);
    // Smaller fields
    buffer.append(BEGIN_ACTION_MODE_HEADER+barrierActionCombo.getSelectedIndex()+END_ACTION_MODE_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    String rrCode = OsejsCommon.getPiece(_inputXML,smBeginHeaders[0],smEndHeaders[0],false);
    int index = rrCode.indexOf("<Name>Run</Name>");
    if (index>=0) { // Backwards compatibility with version 1
      rrCode = rrCode.substring(0, index) + "<Name>Parallel</Name>" + rrCode.substring(index+16);
    }
    regionRunEditor.readXmlString(rrCode);
    sectionsEditor.readXmlString (OsejsCommon.getPiece(_inputXML,smBeginHeaders[1],smEndHeaders[1],false));
    barrierEditor.readXmlString  (OsejsCommon.getPiece(_inputXML,smBeginHeaders[2],smEndHeaders[2],false));
    try {
      index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_ACTION_MODE_HEADER,END_ACTION_MODE_HEADER,false));
      barrierActionCombo.setSelectedIndex(index); 
    }
    catch (NumberFormatException _e) { barrierActionCombo.setSelectedIndex(0); }
    barrierEditor.setVisible(barrierActionCombo.getSelectedIndex()==2);
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "defines number of sections of code to be run in parallel"; }
  
  public void setFont(Font font) {
    regionRunEditor.setFont(font);
    sectionsEditor.setFont(font);
    barrierEditor.setFont(font);
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/parallel_ejs/PJSections.html"; }

  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    ItemListener itemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        collection.reportChange(PJSectionsElement.this); 
      }
    };

    JLabel barrierActionLabel = new JLabel(res.getString("ParallelJava.BarrierAction"));
    barrierActionLabel.setBorder(LABEL_BORDER);

    barrierActionCombo.addItemListener(itemListener);
    barrierActionCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        barrierEditor.setVisible(barrierActionCombo.getSelectedIndex()==2);
      }
    });
    
    JPanel barrierActionTypeLeftPanel = new JPanel(new BorderLayout());
    barrierActionTypeLeftPanel.add(barrierActionLabel,BorderLayout.WEST);
    barrierActionTypeLeftPanel.add(barrierActionCombo,BorderLayout.CENTER);

    JPanel barrierActionTypePanel = new JPanel(new BorderLayout());
    barrierActionTypePanel.add(barrierActionTypeLeftPanel,BorderLayout.WEST);

    JPanel barrierPanel = new JPanel(new BorderLayout());
    barrierPanel.add(barrierActionTypePanel,BorderLayout.NORTH);
    barrierPanel.add(barrierEditor.getComponent(collection),BorderLayout.CENTER);

    barrierEditor.setVisible(barrierActionCombo.getSelectedIndex()==2);
    
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

    buttons = MenuUtils.createRadioGroup (mKeywords,"ParallelJava.",al,false);
    for (int i=0; i<buttons.length; i++) {
      buttons[i].setBorder(buttonsBorder);
      buttons[i].setFont(font);
      buttons[i].setForeground(COLOR);
      buttons[i].setMargin(inset);
      toolbar.add (buttons[i]);
    }

    JComponent rrComp = regionRunEditor.getComponent(collection,null);
    rrComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (rrComp,mKeywords[0]);
    
    JComponent sectComp = sectionsEditor.getComponent(collection);
    sectComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (sectComp,mKeywords[1]);

    barrierPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (barrierPanel,mKeywords[2]);
        
    cardLayout.show (topPanel,mKeywords[1]);
    buttons[1].setSelected(true);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(toolbar,BorderLayout.NORTH);
    mainPanel.add(topPanel,BorderLayout.CENTER);

    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    list.addAll(regionRunEditor.search(info, searchString, mode, name, collection));
    list.addAll(sectionsEditor.search(info, searchString, mode, name, collection));
    list.addAll(barrierEditor.search(info, searchString, mode, name, collection));
    return list;
  }
  
}
