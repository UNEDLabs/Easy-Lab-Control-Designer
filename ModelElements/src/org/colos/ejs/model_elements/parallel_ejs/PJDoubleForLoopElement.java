package org.colos.ejs.model_elements.parallel_ejs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.colos.ejs.osejs.utils.MenuUtils;
import org.colos.ejs.osejs.utils.TwoStrings;

public class PJDoubleForLoopElement extends AbstractModelElement 
                              implements ModelElementMultipageEditor {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/parallel_ejs/PJDoubleForLoop.png");
  static private ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));

  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);
  
  static private final String BEGIN_ACTION_MODE_1_HEADER = "<BarrierActionMode1>"; // Used to delimit my XML information
  static private final String END_ACTION_MODE_1_HEADER = "</BarrierActionMode1>";  // Used to delimit my XML information
  static private final String BEGIN_MINIMUM_1_HEADER = "<MinimumIndex1>"; // Used to delimit my XML information
  static private final String END_MINIMUM_1_HEADER = "</MinimumIndex1>";  // Used to delimit my XML information
  static private final String BEGIN_MAXIMUM_1_HEADER = "<MaximumIndex1>"; // Used to delimit my XML information
  static private final String END_MAXIMUM_1_HEADER = "</MaximumIndex1>";  // Used to delimit my XML information
  static private final String BEGIN_SCHEDULE_MODE_1_HEADER = "<ScheduleMode1>"; // Used to delimit my XML information
  static private final String END_SCHEDULE_MODE_1_HEADER = "</ScheduleMode1>";  // Used to delimit my XML information
  static private final String BEGIN_SCHEDULE_CHUNK_1_HEADER = "<ScheduleChunk1><![CDATA["; // Used to delimit my XML information
  static private final String END_SCHEDULE_CHUNK_1_HEADER = "]]></ScheduleChunk1>";        // Used to delimit my XML information
  static private final String BEGIN_INDEX_TYPE_1_HEADER = "<IntegerType1>"; // Used to delimit my XML information
  static private final String END_INDEX_TYPE_1_HEADER = "</IntegerType1>";  // Used to delimit my XML information
    
  static private final String BEGIN_ACTION_MODE_2_HEADER = "<BarrierActionMode2>"; // Used to delimit my XML information
  static private final String END_ACTION_MODE_2_HEADER = "</BarrierActionMode2>";  // Used to delimit my XML information
  static private final String BEGIN_MINIMUM_2_HEADER = "<MinimumIndex2>"; // Used to delimit my XML information
  static private final String END_MINIMUM_2_HEADER = "</MinimumIndex2>";  // Used to delimit my XML information
  static private final String BEGIN_MAXIMUM_2_HEADER = "<MaximumIndex2>"; // Used to delimit my XML information
  static private final String END_MAXIMUM_2_HEADER = "</MaximumIndex2>";  // Used to delimit my XML information
  static private final String BEGIN_SCHEDULE_MODE_2_HEADER = "<ScheduleMode2>"; // Used to delimit my XML information
  static private final String END_SCHEDULE_MODE_2_HEADER = "</ScheduleMode2>";  // Used to delimit my XML information
  static private final String BEGIN_SCHEDULE_CHUNK_2_HEADER = "<ScheduleChunk2><![CDATA["; // Used to delimit my XML information
  static private final String END_SCHEDULE_CHUNK_2_HEADER = "]]></ScheduleChunk2>";        // Used to delimit my XML information
  static private final String BEGIN_INDEX_TYPE_2_HEADER = "<IntegerType2>"; // Used to delimit my XML information
  static private final String END_INDEX_TYPE_2_HEADER = "</IntegerType2>";  // Used to delimit my XML information
  
  static private final String[] mKeywords = {"RegionRun", "Loop1", "Barrier1", "Loop2", "Barrier2"};
  static private String[] smBeginHeaders = new String[mKeywords.length];
  static private String[] smEndHeaders   = new String[mKeywords.length];

  static private final TwoStrings sSectionCode = new TwoStrings("try { \n  region().critical (new edu.rit.pj.ParallelSection() { public void run() {\n","}});\n} catch (Exception _exc) { _exc.printStackTrace();};\n");
  static private final String defaultRegionRunCode = 
    "// "+ RES.getString("ParallelCode.Parallel.ToolTip")+"\n\n" + 
    "// "+ RES.getString("ParallelJava.DontTouchThis")+":\n"+
    "execute (); // same as the two lines: \n"+
    "            //   execute (_minimumIndex1, _maximumIndex1, new _Loop1(), _barrierAction1);\n"+
    "            //   execute (_minimumIndex2, _maximumIndex2, new _Loop2(), _barrierAction2);\n";
  static private final String defaultLoopRunCode = 
    "// "+ RES.getString("ParallelJava.Run.ToolTip")+"\n\n"+
    "// "+RES.getString("ParallelJava.Indexes")+"\n"+
    "for (_index=_firstIndex; _index<=_lastIndex; ++_index) {\n\n  // "+
    RES.getString("CodeWizard.WriteCodeHere")+"...\n\n" +
    "}";
    
  static {
    for (int i=0, n=mKeywords.length; i<n; i++) {
      smBeginHeaders[i] = "<"  + mKeywords[i] + ">\n";
      smEndHeaders[i]   = "</" + mKeywords[i]+">\n";
    }
  }
  
  private ModelElementCardEditor regionRunEditor;
  private ModelElementCardEditor loop1Editor;
  private ModelElementEditor barrier1Editor;
  private ModelElementCardEditor loop2Editor;
  private ModelElementEditor barrier2Editor;
  
  private JTextField minimum1Field, maximum1Field, scheduleChunk1Field;
  private JComboBox<String> indexType1Combo, scheduleMode1Combo;
  private JComboBox<String> barrierAction1Combo;

  private JTextField minimum2Field, maximum2Field, scheduleChunk2Field;
  private JComboBox<String> indexType2Combo, scheduleMode2Combo;
  private JComboBox<String> barrierAction2Combo;

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

    // First pair of loop + barrier
    loop1Editor = new ModelElementCardEditor (this,this,"ParallelJava", mKeywords[1], new String[]{"Variables","Start","Run", "Finish"});
    loop1Editor.getEditor(1).addPragma("CRITICAL", sSectionCode);
    loop1Editor.getEditor(2).addPragma("CRITICAL", sSectionCode);
    loop1Editor.getEditor(3).addPragma("CRITICAL", sSectionCode);
    String[] loopCode = new String[] {
        "// "+ RES.getString("ParallelJava.Variables.ToolTip"),
        "// "+ RES.getString("ParallelJava.Start.ToolTip"),
        defaultLoopRunCode,
        "// "+ RES.getString("ParallelJava.Finish.ToolTip")        
    };
    loop1Editor.readPlainCode(loopCode);

    barrier1Editor = new ModelElementEditor (this,this);
    barrier1Editor.setName(mKeywords[2]);
    barrier1Editor.addPragma("CRITICAL", sSectionCode);
    barrier1Editor.readPlainCode("// "+ RES.getString("ParallelJava.Barrier.ToolTip"));

    minimum1Field = new JTextField("0",8);
    maximum1Field = new JTextField(RES.getString("ParallelJava.MaximumIndexMessage"),8);
    
    indexType1Combo = new JComboBox<String>();
    indexType1Combo.addItem("int");
    indexType1Combo.addItem("long");
    indexType1Combo.setSelectedIndex(0);

    scheduleMode1Combo = new JComboBox<String>();
    scheduleMode1Combo.addItem("FIXED");
    scheduleMode1Combo.addItem("DYNAMIC");
    scheduleMode1Combo.addItem("GUIDED");
    scheduleMode1Combo.addItem("RUNTIME");
    scheduleMode1Combo.setSelectedIndex(0);
    
    scheduleChunk1Field = new JTextField("",8);
    
    barrierAction1Combo = new JComboBox<String>();
    barrierAction1Combo.addItem("WAIT");
    barrierAction1Combo.addItem("NO_WAIT");
    barrierAction1Combo.addItem("CUSTOM");

    // Second pair of loop + barrier
    loop2Editor = new ModelElementCardEditor (this,this,"ParallelJava", mKeywords[3], new String[]{"Variables","Start","Run", "Finish"});
    loop2Editor.getEditor(1).addPragma("CRITICAL", sSectionCode);
    loop2Editor.getEditor(2).addPragma("CRITICAL", sSectionCode);
    loop2Editor.getEditor(3).addPragma("CRITICAL", sSectionCode);
    loop2Editor.readPlainCode(loopCode);

    barrier2Editor = new ModelElementEditor (this,this);
    barrier2Editor.setName(mKeywords[4]);
    barrier2Editor.addPragma("CRITICAL", sSectionCode);
    barrier2Editor.readPlainCode("// "+ RES.getString("ParallelJava.Barrier.ToolTip"));

    minimum2Field = new JTextField("0",8);
    maximum2Field = new JTextField(RES.getString("ParallelJava.MaximumIndexMessage"),8);
    
    indexType2Combo = new JComboBox<String>();
    indexType2Combo.addItem("int");
    indexType2Combo.addItem("long");
    indexType2Combo.setSelectedIndex(0);

    scheduleMode2Combo = new JComboBox<String>();
    scheduleMode2Combo.addItem("FIXED");
    scheduleMode2Combo.addItem("DYNAMIC");
    scheduleMode2Combo.addItem("GUIDED");
    scheduleMode2Combo.addItem("RUNTIME");
    scheduleMode2Combo.setSelectedIndex(0);
    
    scheduleChunk2Field = new JTextField("",8);
    
    barrierAction2Combo = new JComboBox<String>();
    barrierAction2Combo.addItem("WAIT");
    barrierAction2Combo.addItem("NO_WAIT");
    barrierAction2Combo.addItem("CUSTOM");

  }
  
  // --- Implementation of ModelElementMultipageEditor
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
  
  public String getGenericName() { return "pjDoubleForLoop"; }
  
  public String getConstructorName() {  return "org.colos.ejs.model_elements.parallel_ejs.EJSParallelCode"; }
  
  public String getInitializationCode(String _name) {
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name + "!=null) "+_name+".release();\n");
    buffer.append(_name + " = new " + getConstructorName() + "(\""+_name+"\",this) {\n");
    buffer.append("    public org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion createRegion() {\n");
    buffer.append("      return new org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion() {\n");
    // Region run's variables
    buffer.append(regionRunEditor.getEditor(0).generateCode(_name, "      "));
    buffer.append("\n");

    // Loop 1
    boolean integerType1 = (indexType1Combo.getSelectedIndex()==0);
    String type1 = integerType1 ? "int" : "long";
    if (integerType1) buffer.append("      class _Loop1 extends edu.rit.pj.IntegerForLoop {\n");
    else              buffer.append("      class _Loop1 extends edu.rit.pj.LongForLoop {\n");
    // Variables of loop 1
    buffer.append(loop1Editor.getEditor(0).generateCode(_name, "        "));

    // Schedule of loop 1
    if (integerType1) buffer.append("        public edu.rit.pj.IntegerSchedule schedule() {\n");
    else              buffer.append("        public edu.rit.pj.LongSchedule schedule() {\n");
    if (integerType1) buffer.append("          return edu.rit.pj.IntegerSchedule.");
    else              buffer.append("          return edu.rit.pj.LongSchedule.");
    String scheduleChunkSize = scheduleChunk1Field.getText().trim();
    switch (scheduleMode1Combo.getSelectedIndex()) {
      case 0 : buffer.append("fixed();\n"); break;
      case 1 : 
        if (scheduleChunkSize.length()>0) buffer.append("dynamic("+scheduleChunkSize+"); // "+_name+" Schedule Chunk Size\n");
        else buffer.append("dynamic();\n");
        break;
      case 2 : 
        if (scheduleChunkSize.length()>0) buffer.append("guided("+scheduleChunkSize+"); // "+_name+" Schedule Chunk Size\n");
        else buffer.append("guided();\n");
        break;
      default : buffer.append("runtime();\n"); break;
    }
    buffer.append("        }\n\n");
    // Start of loop 1
    buffer.append("        public void start() throws Exception {\n");
    buffer.append(loop1Editor.getEditor(1).generateCode(_name, "          "));
    buffer.append("        }\n");
    // Run of loop 2
    buffer.append("        public void run("+type1+" _firstIndex, "+type1+" _lastIndex) throws Exception {\n");
    buffer.append("          "+type1+" _index;\n");
    buffer.append(loop1Editor.getEditor(2).generateCode(_name, "          "));
    buffer.append("        }\n");
    // Finish of loop 1
    buffer.append("        public void finish() throws Exception {\n");
    buffer.append(loop1Editor.getEditor(3).generateCode(_name, "          "));
    buffer.append("        }\n");
    buffer.append("      } // end of _Loop1 class\n\n");

    buffer.append("      edu.rit.pj.BarrierAction _barrierAction1 = ");
    // Barrier action 1
    switch (barrierAction1Combo.getSelectedIndex()) {
      case 0 : buffer.append("edu.rit.pj.BarrierAction.WAIT;\n\n"); break;
      case 1 : buffer.append("edu.rit.pj.BarrierAction.NO_WAIT;\n\n"); break;
      default :
        buffer.append("new edu.rit.pj.BarrierAction() {\n");
        buffer.append("        public void run() throws Exception {\n");
        buffer.append(barrier1Editor.generateCode(_name, "          "));
        buffer.append("        }\n");
        buffer.append("      };\n\n");
        break;
    }
    
    // Loop 2
    boolean integerType2 = (indexType2Combo.getSelectedIndex()==0);
    String type2 = integerType2 ? "int" : "long";

    if (integerType2) buffer.append("      class _Loop2 extends edu.rit.pj.IntegerForLoop {\n");
    else              buffer.append("      class _Loop2 extends edu.rit.pj.LongForLoop {\n");
    // Variables of loop 2
    buffer.append(loop2Editor.getEditor(0).generateCode(_name, "        "));

    // Schedule of loop 1
    if (integerType2) buffer.append("        public edu.rit.pj.IntegerSchedule schedule() {\n");
    else              buffer.append("        public edu.rit.pj.LongSchedule schedule() {\n");
    if (integerType2) buffer.append("          return edu.rit.pj.IntegerSchedule.");
    else              buffer.append("          return edu.rit.pj.LongSchedule.");
    String scheduleChunkSize2 = scheduleChunk2Field.getText().trim();
    switch (scheduleMode2Combo.getSelectedIndex()) {
      case 0 : buffer.append("fixed();\n"); break;
      case 1 : 
        if (scheduleChunkSize2.length()>0) buffer.append("dynamic("+scheduleChunkSize2+"); // "+_name+" Schedule Chunk Size\n");
        else buffer.append("dynamic();\n");
        break;
      case 2 : 
        if (scheduleChunkSize2.length()>0) buffer.append("guided("+scheduleChunkSize2+"); // "+_name+" Schedule Chunk Size\n");
        else buffer.append("guided();\n");
        break;
      default : buffer.append("runtime();\n"); break;
    }
    buffer.append("        }\n\n");
    // Start of loop 1
    buffer.append("        public void start() throws Exception {\n");
    buffer.append(loop2Editor.getEditor(1).generateCode(_name, "          "));
    buffer.append("        }\n");
    // Run of loop 2
    buffer.append("        public void run("+type2+" _firstIndex, "+type2+" _lastIndex) throws Exception {\n");
    buffer.append("          "+type2+" _index;\n");
    buffer.append(loop2Editor.getEditor(2).generateCode(_name, "          "));
    buffer.append("        }\n");
    // Finish of loop 1
    buffer.append("        public void finish() throws Exception {\n");
    buffer.append(loop2Editor.getEditor(3).generateCode(_name, "          "));
    buffer.append("        }\n");
    buffer.append("      } // end of _Loop2 class\n\n");

    buffer.append("      edu.rit.pj.BarrierAction _barrierAction2 = ");
    // Barrier action 2
    switch (barrierAction2Combo.getSelectedIndex()) {
      case 0 : buffer.append("edu.rit.pj.BarrierAction.WAIT;\n\n"); break;
      case 1 : buffer.append("edu.rit.pj.BarrierAction.NO_WAIT;\n\n"); break;
      default :
        buffer.append("new edu.rit.pj.BarrierAction() {\n");
        buffer.append("        public void run() throws Exception {\n");
        buffer.append(barrier2Editor.generateCode(_name, "          "));
        buffer.append("        }\n");
        buffer.append("      };\n\n");
        break;
    }
    
    // preliminary
    buffer.append("      public void _preliminaryCode() {\n");
    buffer.append(regionRunEditor.getEditor(1).generateCode(_name, "        "));
    buffer.append("      }\n\n");
    // execute method
    buffer.append("      public void execute() throws Exception {\n");
    buffer.append("        int _threadCount = isExecutingInParallel() ? getThreadCount() : 1;\n");
    buffer.append("        int _threadIndex = isExecutingInParallel() ? getThreadIndex() : 0;\n");
    buffer.append("        "+type2+" _minimumIndex1 = "+minimum1Field.getText()+"; // "+_name+" Minimum index for loop 1\n");
    buffer.append("        "+type2+" _maximumIndex1 = "+maximum1Field.getText()+"; // "+_name+" Maximum index for loop 1\n");
    buffer.append("        "+type2+" _minimumIndex2 = "+minimum2Field.getText()+"; // "+_name+" Minimum index for loop 2\n");
    buffer.append("        "+type2+" _maximumIndex2 = "+maximum2Field.getText()+"; // "+_name+" Maximum index for loop 2\n");
    buffer.append("        execute (_minimumIndex1, _maximumIndex1, new _Loop1(), _barrierAction1);\n");
    buffer.append("        execute (_minimumIndex2, _maximumIndex2, new _Loop2(), _barrierAction2);\n");
    buffer.append("      }\n\n");
    // parallel
    buffer.append("      public void _parallelCode() throws Exception {\n");
    buffer.append("        int _threadCount = isExecutingInParallel() ? getThreadCount() : 1;\n");
    buffer.append("        int _threadIndex = isExecutingInParallel() ? getThreadIndex() : 0;\n");
    buffer.append("        "+type2+" _minimumIndex1 = "+minimum1Field.getText()+"; // "+_name+" Minimum index\n");
    buffer.append("        "+type2+" _maximumIndex1 = "+maximum1Field.getText()+"; // "+_name+" Maximum index\n");
    buffer.append("        "+type2+" _minimumIndex2 = "+minimum2Field.getText()+"; // "+_name+" Minimum index for loop 2\n");
    buffer.append("        "+type2+" _maximumIndex2 = "+maximum2Field.getText()+"; // "+_name+" Maximum index for loop 2\n");
    buffer.append(regionRunEditor.getEditor(2).generateCode(_name, "        "));
    buffer.append("      }\n\n");
    // final
    buffer.append("      public void _finalCode() {\n");
    buffer.append(regionRunEditor.getEditor(3).generateCode(_name, "        "));
    buffer.append("      }\n\n");

    
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
    // Loop 1
    buffer.append(smBeginHeaders[1]);
    buffer.append(loop1Editor.saveStringBuffer());
    buffer.append(smEndHeaders[1]);
    // Barrier 1
    buffer.append(smBeginHeaders[2]);
    buffer.append(barrier1Editor.saveStringBuffer());
    buffer.append(smEndHeaders[2]);
    // Smaller fields 1
    buffer.append(BEGIN_ACTION_MODE_1_HEADER+barrierAction1Combo.getSelectedIndex()+END_ACTION_MODE_1_HEADER + "\n");
    buffer.append(BEGIN_MINIMUM_1_HEADER+minimum1Field.getText()+END_MINIMUM_1_HEADER + "\n");
    buffer.append(BEGIN_MAXIMUM_1_HEADER+maximum1Field.getText()+END_MAXIMUM_1_HEADER + "\n");
    buffer.append(BEGIN_SCHEDULE_MODE_1_HEADER+scheduleMode1Combo.getSelectedIndex()+END_SCHEDULE_MODE_1_HEADER + "\n");
    buffer.append(BEGIN_SCHEDULE_CHUNK_1_HEADER+scheduleChunk1Field.getText()+END_SCHEDULE_CHUNK_1_HEADER + "\n");
    buffer.append(BEGIN_INDEX_TYPE_1_HEADER+(indexType1Combo.getSelectedIndex()==0)+END_INDEX_TYPE_1_HEADER + "\n");
    // Loop 2
    buffer.append(smBeginHeaders[3]);
    buffer.append(loop2Editor.saveStringBuffer());
    buffer.append(smEndHeaders[3]);
    // Barrier 2
    buffer.append(smBeginHeaders[4]);
    buffer.append(barrier2Editor.saveStringBuffer());
    buffer.append(smEndHeaders[4]);
    // Smaller fields 2
    buffer.append(BEGIN_ACTION_MODE_2_HEADER+barrierAction2Combo.getSelectedIndex()+END_ACTION_MODE_2_HEADER + "\n");
    buffer.append(BEGIN_MINIMUM_2_HEADER+minimum2Field.getText()+END_MINIMUM_2_HEADER + "\n");
    buffer.append(BEGIN_MAXIMUM_2_HEADER+maximum2Field.getText()+END_MAXIMUM_2_HEADER + "\n");
    buffer.append(BEGIN_SCHEDULE_MODE_2_HEADER+scheduleMode2Combo.getSelectedIndex()+END_SCHEDULE_MODE_2_HEADER + "\n");
    buffer.append(BEGIN_SCHEDULE_CHUNK_2_HEADER+scheduleChunk2Field.getText()+END_SCHEDULE_CHUNK_2_HEADER + "\n");
    buffer.append(BEGIN_INDEX_TYPE_2_HEADER+(indexType2Combo.getSelectedIndex()==0)+END_INDEX_TYPE_2_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    regionRunEditor.readXmlString(OsejsCommon.getPiece(_inputXML,smBeginHeaders[0],smEndHeaders[0],false));
    
    // Loop 1
    loop1Editor.readXmlString     (OsejsCommon.getPiece(_inputXML,smBeginHeaders[1],smEndHeaders[1],false));
    barrier1Editor.readXmlString  (OsejsCommon.getPiece(_inputXML,smBeginHeaders[2],smEndHeaders[2],false));
    try {
      int index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_ACTION_MODE_1_HEADER,END_ACTION_MODE_1_HEADER,false));
      barrierAction1Combo.setSelectedIndex(index);
    }
    catch (NumberFormatException _e) { barrierAction1Combo.setSelectedIndex(0); }
    barrier1Editor.setVisible(barrierAction1Combo.getSelectedIndex()==2);
    minimum1Field.setText(OsejsCommon.getPiece(_inputXML,BEGIN_MINIMUM_1_HEADER,END_MINIMUM_1_HEADER,false));
    maximum1Field.setText(OsejsCommon.getPiece(_inputXML,BEGIN_MAXIMUM_1_HEADER,END_MAXIMUM_1_HEADER,false));
    try { 
      int index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_SCHEDULE_MODE_1_HEADER,END_SCHEDULE_MODE_1_HEADER,false)); 
      scheduleMode1Combo.setSelectedIndex(index); 
    }
    catch (NumberFormatException _e) { scheduleMode1Combo.setSelectedIndex(0); }
    scheduleChunk1Field.setText(OsejsCommon.getPiece(_inputXML,BEGIN_SCHEDULE_CHUNK_1_HEADER,END_SCHEDULE_CHUNK_1_HEADER,false));
    String integerTypeStr = OsejsCommon.getPiece(_inputXML,BEGIN_INDEX_TYPE_1_HEADER,END_INDEX_TYPE_1_HEADER,false);
    if ("false".equals(integerTypeStr)) indexType1Combo.setSelectedIndex(1); // long
    else indexType1Combo.setSelectedIndex(0); // int
    
    // Loop 2
    loop2Editor.readXmlString     (OsejsCommon.getPiece(_inputXML,smBeginHeaders[3],smEndHeaders[3],false));
    barrier2Editor.readXmlString  (OsejsCommon.getPiece(_inputXML,smBeginHeaders[4],smEndHeaders[4],false));
    try {
      int index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_ACTION_MODE_2_HEADER,END_ACTION_MODE_2_HEADER,false));
      barrierAction2Combo.setSelectedIndex(index);
    }
    catch (NumberFormatException _e) { barrierAction2Combo.setSelectedIndex(0); }
    barrier2Editor.setVisible(barrierAction2Combo.getSelectedIndex()==2);
    minimum2Field.setText(OsejsCommon.getPiece(_inputXML,BEGIN_MINIMUM_2_HEADER,END_MINIMUM_2_HEADER,false));
    maximum2Field.setText(OsejsCommon.getPiece(_inputXML,BEGIN_MAXIMUM_2_HEADER,END_MAXIMUM_2_HEADER,false));
    try { 
      int index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_SCHEDULE_MODE_2_HEADER,END_SCHEDULE_MODE_2_HEADER,false)); 
      scheduleMode2Combo.setSelectedIndex(index); 
    }
    catch (NumberFormatException _e) { scheduleMode2Combo.setSelectedIndex(0); }
    scheduleChunk2Field.setText(OsejsCommon.getPiece(_inputXML,BEGIN_SCHEDULE_CHUNK_2_HEADER,END_SCHEDULE_CHUNK_2_HEADER,false));
    integerTypeStr = OsejsCommon.getPiece(_inputXML,BEGIN_INDEX_TYPE_2_HEADER,END_INDEX_TYPE_2_HEADER,false);
    if ("false".equals(integerTypeStr)) indexType2Combo.setSelectedIndex(1); // long
    else indexType2Combo.setSelectedIndex(0); // int
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "defines a set of two consecutive integer indexed for loops to be executed (each) in parallel"; }
  
  public void setFont(Font font) {
    regionRunEditor.setFont(font);
    loop1Editor.setFont(font);
    barrier1Editor.setFont(font);
    minimum1Field.setFont(font);
    maximum1Field.setFont(font);
    scheduleChunk1Field.setFont(font);
    loop2Editor.setFont(font);
    barrier2Editor.setFont(font);
    minimum2Field.setFont(font);
    maximum2Field.setFont(font);
    scheduleChunk2Field.setFont(font);
  }
  
  protected String getHtmlPage() { return "org/colos/ejs/model_elements/parallel_ejs/PJDoubleForLoop.html"; }


  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(PJDoubleForLoopElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(PJDoubleForLoopElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(PJDoubleForLoopElement.this); }
    };

    ItemListener itemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        collection.reportChange(PJDoubleForLoopElement.this); 
      }
    };

    // Loop 1
    
    JLabel minimum1Label = new JLabel(RES.getString("ParallelJava.MinimumIndex"));
    minimum1Label.setForeground(COLOR);
    minimum1Label.setBorder(LABEL_BORDER);
    
    minimum1Field.getDocument().addDocumentListener (documentListener);

    JButton minimum1LinkButton = new JButton(LINK_ICON);
    minimum1LinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = minimum1Field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexType1Combo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(minimum1Field,integerType ? "int" : "long", value);
        if (variable!=null) minimum1Field.setText(variable);
      }
    });

    JPanel minimum1Panel = new JPanel(new BorderLayout());
    minimum1Panel.add(minimum1Label,BorderLayout.WEST);
    minimum1Panel.add(minimum1Field,BorderLayout.CENTER);
    minimum1Panel.add(minimum1LinkButton,BorderLayout.EAST);

    JLabel maximum1Label = new JLabel(RES.getString("ParallelJava.MaximumIndex"));
    maximum1Label.setForeground(COLOR);
    maximum1Label.setBorder(LABEL_BORDER);
    
    maximum1Field.getDocument().addDocumentListener (documentListener);

    JButton maximum1LinkButton = new JButton(LINK_ICON);
    maximum1LinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = maximum1Field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexType1Combo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(maximum1Field,integerType ? "int" : "long", value);
        if (variable!=null) maximum1Field.setText(variable);
      }
    });

    JPanel maximum1Panel = new JPanel(new BorderLayout());
    maximum1Panel.add(maximum1Label,BorderLayout.WEST);
    maximum1Panel.add(maximum1Field,BorderLayout.CENTER);
    maximum1Panel.add(maximum1LinkButton,BorderLayout.EAST);

    JLabel indexType1Label = new JLabel(RES.getString("ParallelJava.IndexType"));
    indexType1Label.setForeground(COLOR);
    indexType1Label.setBorder(LABEL_BORDER);
    indexType1Combo.addItemListener(itemListener);

    JPanel indexType1Panel = new JPanel(new BorderLayout());
    indexType1Panel.add(indexType1Label,BorderLayout.WEST);
    indexType1Panel.add(indexType1Combo,BorderLayout.CENTER);

    JLabel scheduleType1Label = new JLabel(RES.getString("ParallelJava.ScheduleType"));
    scheduleType1Label.setForeground(COLOR);
    scheduleType1Label.setBorder(LABEL_BORDER);
    scheduleMode1Combo.addItemListener(itemListener);

    JPanel scheduleType1Panel = new JPanel(new BorderLayout());
    scheduleType1Panel.add(scheduleType1Label,BorderLayout.WEST);
    scheduleType1Panel.add(scheduleMode1Combo,BorderLayout.CENTER);

    JLabel scheduleChunk1Label = new JLabel(RES.getString("ParallelJava.ChunkSize"));
    scheduleChunk1Label.setForeground(COLOR);
    scheduleChunk1Label.setBorder(LABEL_BORDER);
    scheduleChunk1Field.getDocument().addDocumentListener (documentListener);

    JButton scheduleChunk1LinkButton = new JButton(LINK_ICON);
    scheduleChunk1LinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = scheduleChunk1Field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexType1Combo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(scheduleChunk1Field,integerType ? "int" : "long", value);
        if (variable!=null) scheduleChunk1Field.setText(variable);
      }
    });

    JPanel scheduleChunck1Panel = new JPanel(new BorderLayout());
    scheduleChunck1Panel.add(scheduleChunk1Label,BorderLayout.WEST);
    scheduleChunck1Panel.add(scheduleChunk1Field,BorderLayout.CENTER);
    scheduleChunck1Panel.add(scheduleChunk1LinkButton,BorderLayout.EAST);

    JPanel schedule1LeftPanel = new JPanel(new BorderLayout());
    schedule1LeftPanel.add(scheduleType1Panel,BorderLayout.WEST);
    schedule1LeftPanel.add(scheduleChunck1Panel,BorderLayout.CENTER);

    JPanel indexes1CenterPanel = new JPanel(new GridLayout(1,0));
    indexes1CenterPanel.add(minimum1Panel);
    indexes1CenterPanel.add(maximum1Panel);

    JPanel run1TopBottomPanel = new JPanel(new BorderLayout());
    run1TopBottomPanel.add(indexType1Panel,BorderLayout.WEST);
    run1TopBottomPanel.add(schedule1LeftPanel,BorderLayout.CENTER);

    JPanel run1TopPanel = new JPanel(new GridLayout(0,1));
    run1TopPanel.add(indexes1CenterPanel);
    run1TopPanel.add(run1TopBottomPanel);
    run1TopPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

    JLabel barrierAction1Label = new JLabel(RES.getString("ParallelJava.BarrierAction"));
    barrierAction1Label.setForeground(COLOR);
    barrierAction1Label.setBorder(LABEL_BORDER);

    barrierAction1Combo.addItemListener(itemListener);
    barrierAction1Combo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        barrier1Editor.setVisible(barrierAction1Combo.getSelectedIndex()==2);
      }
    });

    JPanel barrierActionType1LeftPanel = new JPanel(new BorderLayout());
    barrierActionType1LeftPanel.add(barrierAction1Label,BorderLayout.WEST);
    barrierActionType1LeftPanel.add(barrierAction1Combo,BorderLayout.CENTER);

    JPanel barrierActionType1Panel = new JPanel(new BorderLayout());
    barrierActionType1Panel.add(barrierActionType1LeftPanel,BorderLayout.WEST);

    JPanel barrier1Panel = new JPanel(new BorderLayout());
    barrier1Panel.add(barrierActionType1Panel,BorderLayout.NORTH);
    barrier1Panel.add(barrier1Editor.getComponent (collection),BorderLayout.CENTER);
    
    barrier1Editor.setVisible(barrierAction1Combo.getSelectedIndex()==2);

    // Loop 2
    
    JLabel minimum2Label = new JLabel(RES.getString("ParallelJava.MinimumIndex"));
    minimum2Label.setForeground(COLOR);
    minimum2Label.setBorder(LABEL_BORDER);
    
    minimum2Field.getDocument().addDocumentListener (documentListener);

    JButton minimum2LinkButton = new JButton(LINK_ICON);
    minimum2LinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = minimum2Field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexType2Combo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(minimum2Field,integerType ? "int" : "long", value);
        if (variable!=null) minimum2Field.setText(variable);
      }
    });

    JPanel minimum2Panel = new JPanel(new BorderLayout());
    minimum2Panel.add(minimum2Label,BorderLayout.WEST);
    minimum2Panel.add(minimum2Field,BorderLayout.CENTER);
    minimum2Panel.add(minimum2LinkButton,BorderLayout.EAST);

    JLabel maximum2Label = new JLabel(RES.getString("ParallelJava.MaximumIndex"));
    maximum2Label.setForeground(COLOR);
    maximum2Label.setBorder(LABEL_BORDER);
    
    maximum2Field.getDocument().addDocumentListener (documentListener);

    JButton maximum2LinkButton = new JButton(LINK_ICON);
    maximum2LinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = maximum2Field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexType2Combo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(maximum2Field,integerType ? "int" : "long", value);
        if (variable!=null) maximum2Field.setText(variable);
      }
    });

    JPanel maximum2Panel = new JPanel(new BorderLayout());
    maximum2Panel.add(maximum2Label,BorderLayout.WEST);
    maximum2Panel.add(maximum2Field,BorderLayout.CENTER);
    maximum2Panel.add(maximum2LinkButton,BorderLayout.EAST);

    JLabel indexType2Label = new JLabel(RES.getString("ParallelJava.IndexType"));
    indexType2Label.setForeground(COLOR);
    indexType2Label.setBorder(LABEL_BORDER);
    indexType2Combo.addItemListener(itemListener);

    JPanel indexType2Panel = new JPanel(new BorderLayout());
    indexType2Panel.add(indexType2Label,BorderLayout.WEST);
    indexType2Panel.add(indexType2Combo,BorderLayout.CENTER);

    JLabel scheduleType2Label = new JLabel(RES.getString("ParallelJava.ScheduleType"));
    scheduleType2Label.setForeground(COLOR);
    scheduleType2Label.setBorder(LABEL_BORDER);
    scheduleMode2Combo.addItemListener(itemListener);

    JPanel scheduleType2Panel = new JPanel(new BorderLayout());
    scheduleType2Panel.add(scheduleType2Label,BorderLayout.WEST);
    scheduleType2Panel.add(scheduleMode2Combo,BorderLayout.CENTER);

    JLabel scheduleChunk2Label = new JLabel(RES.getString("ParallelJava.ChunkSize"));
    scheduleChunk2Label.setForeground(COLOR);
    scheduleChunk2Label.setBorder(LABEL_BORDER);
    scheduleChunk2Field.getDocument().addDocumentListener (documentListener);

    JButton scheduleChunk2LinkButton = new JButton(LINK_ICON);
    scheduleChunk2LinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = scheduleChunk2Field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexType2Combo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(scheduleChunk2Field,integerType ? "int" : "long", value);
        if (variable!=null) scheduleChunk2Field.setText(variable);
      }
    });

    JPanel scheduleChunck2Panel = new JPanel(new BorderLayout());
    scheduleChunck2Panel.add(scheduleChunk2Label,BorderLayout.WEST);
    scheduleChunck2Panel.add(scheduleChunk2Field,BorderLayout.CENTER);
    scheduleChunck2Panel.add(scheduleChunk2LinkButton,BorderLayout.EAST);

    JPanel schedule2LeftPanel = new JPanel(new BorderLayout());
    schedule2LeftPanel.add(scheduleType2Panel,BorderLayout.WEST);
    schedule2LeftPanel.add(scheduleChunck2Panel,BorderLayout.CENTER);

    JPanel indexes2CenterPanel = new JPanel(new GridLayout(1,0));
    indexes2CenterPanel.add(minimum2Panel);
    indexes2CenterPanel.add(maximum2Panel);

    JPanel run2TopBottomPanel = new JPanel(new BorderLayout());
    run2TopBottomPanel.add(indexType2Panel,BorderLayout.WEST);
    run2TopBottomPanel.add(schedule2LeftPanel,BorderLayout.CENTER);

    JPanel run2TopPanel = new JPanel(new GridLayout(0,1));
    run2TopPanel.add(indexes2CenterPanel);
    run2TopPanel.add(run2TopBottomPanel);
    run2TopPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

    JLabel barrierAction2Label = new JLabel(RES.getString("ParallelJava.BarrierAction"));
    barrierAction2Label.setForeground(COLOR);
    barrierAction2Label.setBorder(LABEL_BORDER);

    barrierAction2Combo.addItemListener(itemListener);
    barrierAction2Combo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        barrier2Editor.setVisible(barrierAction2Combo.getSelectedIndex()==2);
      }
    });

    JPanel barrierActionType2LeftPanel = new JPanel(new BorderLayout());
    barrierActionType2LeftPanel.add(barrierAction2Label,BorderLayout.WEST);
    barrierActionType2LeftPanel.add(barrierAction2Combo,BorderLayout.CENTER);

    JPanel barrierActionType2Panel = new JPanel(new BorderLayout());
    barrierActionType2Panel.add(barrierActionType2LeftPanel,BorderLayout.WEST);

    JPanel barrier2Panel = new JPanel(new BorderLayout());
    barrier2Panel.add(barrierActionType2Panel,BorderLayout.NORTH);
    barrier2Panel.add(barrier2Editor.getComponent (collection),BorderLayout.CENTER);
    
    barrier2Editor.setVisible(barrierAction2Combo.getSelectedIndex()==2);
    
    
    cardLayout = new CardLayout ();
    topPanel = new JPanel(cardLayout);

    ActionListener al = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        cardLayout.show (topPanel,evt.getActionCommand());
      }
    };
    Border buttonsBorder = BorderFactory.createEmptyBorder(0,6,0,6);
    Box toolbar = Box.createHorizontalBox();
    Insets inset = new java.awt.Insets(1,3,1,3);

    Font font = InterfaceUtils.font(null,Osejs.getResources().getString("Osejs.TitleFont"));

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
    
    JComponent loop1Comp = loop1Editor.getComponent(collection, new Component[] { null, null, run1TopPanel, null });
    loop1Comp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (loop1Comp,mKeywords[1]);

    barrier1Panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (barrier1Panel,mKeywords[2]);
        
    JComponent loop2Comp = loop2Editor.getComponent(collection, new Component[] { null, null, run2TopPanel, null });
    loop2Comp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (loop2Comp,mKeywords[3]);

    barrier2Panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (barrier2Panel,mKeywords[4]);
        
    cardLayout.show (topPanel,mKeywords[1]);
    buttons[1].setSelected(true);
    loop1Editor.showEditor(2); // Show the run panel
    loop2Editor.showEditor(2); // Show the run panel

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(toolbar,BorderLayout.NORTH);
    mainPanel.add(topPanel,BorderLayout.CENTER);

    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    list.addAll(regionRunEditor.search(info, searchString, mode, name, collection));
    list.addAll(loop1Editor.search(info, searchString, mode, name, collection));
    list.addAll(barrier1Editor.search(info, searchString, mode, name, collection));

    addToSearch(list,minimum1Field,info,searchString,mode,this,name,collection);
    addToSearch(list,maximum1Field,info,searchString,mode,this,name,collection);
    addToSearch(list,scheduleChunk1Field,info,searchString,mode,this,name,collection);

    list.addAll(loop2Editor.search(info, searchString, mode, name, collection));
    list.addAll(barrier2Editor.search(info, searchString, mode, name, collection));
    
    addToSearch(list,minimum2Field,info,searchString,mode,this,name,collection);
    addToSearch(list,maximum2Field,info,searchString,mode,this,name,collection);
    addToSearch(list,scheduleChunk2Field,info,searchString,mode,this,name,collection);
    return list;
  }
  
}
