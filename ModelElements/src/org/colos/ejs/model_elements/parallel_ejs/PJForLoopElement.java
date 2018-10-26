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

public class PJForLoopElement extends AbstractModelElement 
                              implements ModelElementMultipageEditor {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/parallel_ejs/PJForLoop.png");
  static private ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));

  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);
  
  static private final String BEGIN_ACTION_MODE_HEADER = "<BarrierActionMode>"; // Used to delimit my XML information
  static private final String END_ACTION_MODE_HEADER = "</BarrierActionMode>";  // Used to delimit my XML information
  static private final String BEGIN_MINIMUM_HEADER = "<MinimumIndex>"; // Used to delimit my XML information
  static private final String END_MINIMUM_HEADER = "</MinimumIndex>";  // Used to delimit my XML information
  static private final String BEGIN_MAXIMUM_HEADER = "<MaximumIndex>"; // Used to delimit my XML information
  static private final String END_MAXIMUM_HEADER = "</MaximumIndex>";  // Used to delimit my XML information
  static private final String BEGIN_SCHEDULE_MODE_HEADER = "<ScheduleMode>"; // Used to delimit my XML information
  static private final String END_SCHEDULE_MODE_HEADER = "</ScheduleMode>";  // Used to delimit my XML information
  static private final String BEGIN_SCHEDULE_CHUNK_HEADER = "<ScheduleChunk><![CDATA["; // Used to delimit my XML information
  static private final String END_SCHEDULE_CHUNK_HEADER = "]]></ScheduleChunk>";        // Used to delimit my XML information
  static private final String BEGIN_INDEX_TYPE_HEADER = "<IntegerType>"; // Used to delimit my XML information
  static private final String END_INDEX_TYPE_HEADER = "</IntegerType>";  // Used to delimit my XML information
    
  static private final String[] mKeywords = {"RegionRun", "Loop", "Barrier"};
  static private String[] smBeginHeaders = new String[mKeywords.length];
  static private String[] smEndHeaders   = new String[mKeywords.length];

  static private final TwoStrings sSectionCode = new TwoStrings("try { \n  region().critical (new edu.rit.pj.ParallelSection() { public void run() {\n","}});\n} catch (Exception _exc) { _exc.printStackTrace();};\n");
  static private final String defaultRegionRunCode = 
    "// "+ RES.getString("ParallelCode.Parallel.ToolTip")+"\n\n" + 
    "// "+ RES.getString("ParallelJava.DontTouchThis")+":\n"+
    "execute (); // same as execute (_minimumIndex, _maximumIndex, new _Loop(), _barrierAction);\n";
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
  private ModelElementCardEditor loopEditor;
  private ModelElementEditor barrierEditor;
  
  private JTextField minimumField, maximumField, scheduleChunkField;
  private JComboBox<String> indexTypeCombo, scheduleModeCombo;
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

    loopEditor = new ModelElementCardEditor (this,this,"ParallelJava", mKeywords[1], new String[]{"Variables","Start","Run", "Finish"});
    loopEditor.getEditor(1).addPragma("CRITICAL", sSectionCode);
    loopEditor.getEditor(2).addPragma("CRITICAL", sSectionCode);
    loopEditor.getEditor(3).addPragma("CRITICAL", sSectionCode);
    String[] loopCode = new String[] {
        "// "+ RES.getString("ParallelJava.Variables.ToolTip"),
        "// "+ RES.getString("ParallelJava.Start.ToolTip"),
        defaultLoopRunCode,
        "// "+ RES.getString("ParallelJava.Finish.ToolTip")        
    };
    loopEditor.readPlainCode(loopCode);

    barrierEditor = new ModelElementEditor (this,this);
    barrierEditor.setName(mKeywords[2]);
    barrierEditor.addPragma("CRITICAL", sSectionCode);
    barrierEditor.readPlainCode("// "+ RES.getString("ParallelJava.Barrier.ToolTip"));

    minimumField = new JTextField("0",8);
    maximumField = new JTextField(RES.getString("ParallelJava.MaximumIndexMessage"),8);
    
    indexTypeCombo = new JComboBox<String>();
    indexTypeCombo.addItem("int");
    indexTypeCombo.addItem("long");
    indexTypeCombo.setSelectedIndex(0);

    scheduleModeCombo = new JComboBox<String>();
    scheduleModeCombo.addItem("FIXED");
    scheduleModeCombo.addItem("DYNAMIC");
    scheduleModeCombo.addItem("GUIDED");
    scheduleModeCombo.addItem("RUNTIME");
    scheduleModeCombo.setSelectedIndex(0);
    
    scheduleChunkField = new JTextField("",8);
    
    barrierActionCombo = new JComboBox<String>();
    barrierActionCombo.addItem("WAIT");
    barrierActionCombo.addItem("NO_WAIT");
    barrierActionCombo.addItem("CUSTOM");
    
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
  
  public String getGenericName() { return "pjForLoop"; }
  
  public String getConstructorName() {  return "org.colos.ejs.model_elements.parallel_ejs.EJSParallelCode"; }
  
  public String getInitializationCode(String _name) {
    boolean integerType = (indexTypeCombo.getSelectedIndex()==0);
    String type = integerType ? "int" : "long";
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+_name + "!=null) "+_name+".release();\n");
    buffer.append(_name + " = new " + getConstructorName() + "(\""+_name+"\",this) {\n");
    buffer.append("    public org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion createRegion() {\n");
    buffer.append("      return new org.colos.ejs.model_elements.parallel_ejs.EJSParallelRegion() {\n");
    // Region run's variables
    buffer.append(regionRunEditor.getEditor(0).generateCode(_name, "      "));
    buffer.append("\n");

    // Loop
    if (integerType) buffer.append("      class _Loop extends edu.rit.pj.IntegerForLoop {\n");
    else             buffer.append("      class _Loop extends edu.rit.pj.LongForLoop {\n");
    // Variables of the loop
    buffer.append(loopEditor.getEditor(0).generateCode(_name, "        "));

    // Schedule of the loop
    if (integerType) buffer.append("        public edu.rit.pj.IntegerSchedule schedule() {\n");
    else             buffer.append("        public edu.rit.pj.LongSchedule schedule() {\n");
    if (integerType) buffer.append("          return edu.rit.pj.IntegerSchedule.");
    else             buffer.append("          return edu.rit.pj.LongSchedule.");
    String scheduleChunkSize = scheduleChunkField.getText().trim();
    switch (scheduleModeCombo.getSelectedIndex()) {
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
    // Start of the loop
    buffer.append("        public void start() throws Exception {\n");
    buffer.append(loopEditor.getEditor(1).generateCode(_name, "          "));
    buffer.append("        }\n");
    // Run of the loop
    buffer.append("        public void run("+type+" _firstIndex, "+type+" _lastIndex) throws Exception {\n");
    buffer.append("          "+type+" _index;\n");
    buffer.append(loopEditor.getEditor(2).generateCode(_name, "          "));
    buffer.append("        }\n");
    // Finish of the loop
    buffer.append("        public void finish() throws Exception {\n");
    buffer.append(loopEditor.getEditor(3).generateCode(_name, "          "));
    buffer.append("        }\n");
    buffer.append("      } // end of _Loop class\n\n");

    // Barrier action
    buffer.append("      edu.rit.pj.BarrierAction _barrierAction = ");
    switch (barrierActionCombo.getSelectedIndex()) {
      case 0 : buffer.append("edu.rit.pj.BarrierAction.WAIT;\n\n"); break;
      case 1 : buffer.append("edu.rit.pj.BarrierAction.NO_WAIT;\n\n"); break;
      default :
        buffer.append("new edu.rit.pj.BarrierAction() {\n");
        buffer.append("        public void run() throws Exception {\n");
        buffer.append(barrierEditor.generateCode(_name, "          "));
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
    buffer.append("        "+type+" _minimumIndex = "+minimumField.getText()+"; // "+_name+" Minimum index\n");
    buffer.append("        "+type+" _maximumIndex = "+maximumField.getText()+"; // "+_name+" Maximum index\n");
    buffer.append("        execute (_minimumIndex, _maximumIndex, new _Loop(), _barrierAction);\n");
    buffer.append("      }\n\n");
    // parallel
    buffer.append("      public void _parallelCode() throws Exception {\n");
    buffer.append("        int _threadCount = isExecutingInParallel() ? getThreadCount() : 1;\n");
    buffer.append("        int _threadIndex = isExecutingInParallel() ? getThreadIndex() : 0;\n");
    buffer.append("        "+type+" _minimumIndex = "+minimumField.getText()+"; // "+_name+" Minimum index\n");
    buffer.append("        "+type+" _maximumIndex = "+maximumField.getText()+"; // "+_name+" Maximum index\n");
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
    // Loop
    buffer.append(smBeginHeaders[1]);
    buffer.append(loopEditor.saveStringBuffer());
    buffer.append(smEndHeaders[1]);
    // Barrier
    buffer.append(smBeginHeaders[2]);
    buffer.append(barrierEditor.saveStringBuffer());
    buffer.append(smEndHeaders[2]);
    // Smaller fields
    buffer.append(BEGIN_ACTION_MODE_HEADER+barrierActionCombo.getSelectedIndex()+END_ACTION_MODE_HEADER + "\n");
    buffer.append(BEGIN_MINIMUM_HEADER+minimumField.getText()+END_MINIMUM_HEADER + "\n");
    buffer.append(BEGIN_MAXIMUM_HEADER+maximumField.getText()+END_MAXIMUM_HEADER + "\n");
    buffer.append(BEGIN_SCHEDULE_MODE_HEADER+scheduleModeCombo.getSelectedIndex()+END_SCHEDULE_MODE_HEADER + "\n");
    buffer.append(BEGIN_SCHEDULE_CHUNK_HEADER+scheduleChunkField.getText()+END_SCHEDULE_CHUNK_HEADER + "\n");
    buffer.append(BEGIN_INDEX_TYPE_HEADER+(indexTypeCombo.getSelectedIndex()==0)+END_INDEX_TYPE_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    String rrCode = OsejsCommon.getPiece(_inputXML,smBeginHeaders[0],smEndHeaders[0],false);
    if (rrCode==null) {
      // backwards compatibility
      regionRunEditor.readXmlString(OsejsCommon.getPiece(_inputXML,"<RegionRun>","</RegionRun>",false));
      loopEditor.getEditor(0).readXmlString(OsejsCommon.getPiece(_inputXML,"<Variables>","</Variables>",false));
      loopEditor.getEditor(1).readXmlString(OsejsCommon.getPiece(_inputXML,"<Start>","</Start>",false));
      loopEditor.getEditor(2).readXmlString(OsejsCommon.getPiece(_inputXML,"<Run>","</Run>",false));
      loopEditor.getEditor(3).readXmlString(OsejsCommon.getPiece(_inputXML,"<Finish>","</Finish>",false));
      barrierEditor.readXmlString(OsejsCommon.getPiece(_inputXML,"<Barrier>","</Barrier>",false));
    }
    else {
      int index = rrCode.indexOf("<Name>Run</Name>");
      if (index>=0) { // Backwards compatibility with version 1
        rrCode = rrCode.substring(0, index) + "<Name>Parallel</Name>" + rrCode.substring(index+16);
      }
      regionRunEditor.readXmlString(rrCode);
      loopEditor.readXmlString     (OsejsCommon.getPiece(_inputXML,smBeginHeaders[1],smEndHeaders[1],false));
      barrierEditor.readXmlString  (OsejsCommon.getPiece(_inputXML,smBeginHeaders[2],smEndHeaders[2],false));
    }
    try {
      int index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_ACTION_MODE_HEADER,END_ACTION_MODE_HEADER,false));
      barrierActionCombo.setSelectedIndex(index);
    }
    catch (NumberFormatException _e) { barrierActionCombo.setSelectedIndex(0); }
    barrierEditor.setVisible(barrierActionCombo.getSelectedIndex()==2);
    minimumField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_MINIMUM_HEADER,END_MINIMUM_HEADER,false));
    maximumField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_MAXIMUM_HEADER,END_MAXIMUM_HEADER,false));
    try { 
      int index = Integer.parseInt(OsejsCommon.getPiece(_inputXML,BEGIN_SCHEDULE_MODE_HEADER,END_SCHEDULE_MODE_HEADER,false)); 
      scheduleModeCombo.setSelectedIndex(index); 
    }
    catch (NumberFormatException _e) { scheduleModeCombo.setSelectedIndex(0); }
    scheduleChunkField.setText(OsejsCommon.getPiece(_inputXML,BEGIN_SCHEDULE_CHUNK_HEADER,END_SCHEDULE_CHUNK_HEADER,false));
    String integerTypeStr = OsejsCommon.getPiece(_inputXML,BEGIN_INDEX_TYPE_HEADER,END_INDEX_TYPE_HEADER,false);
    if ("false".equals(integerTypeStr)) indexTypeCombo.setSelectedIndex(1); // long
    else indexTypeCombo.setSelectedIndex(0); // int
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "defines an integer indexed for loop to be executed in parallel"; }
  
  public void setFont(Font font) {
    regionRunEditor.setFont(font);
    loopEditor.setFont(font);
    barrierEditor.setFont(font);
    minimumField.setFont(font);
    maximumField.setFont(font);
    scheduleChunkField.setFont(font);
  }
  
  protected String getHtmlPage() { return "org/colos/ejs/model_elements/parallel_ejs/PJForLoop.html"; }


  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(PJForLoopElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(PJForLoopElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(PJForLoopElement.this); }
    };

    ItemListener itemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        collection.reportChange(PJForLoopElement.this); 
      }
    };

    JLabel minimumLabel = new JLabel(RES.getString("ParallelJava.MinimumIndex"));
    minimumLabel.setForeground(COLOR);
    minimumLabel.setBorder(LABEL_BORDER);
    
    minimumField.getDocument().addDocumentListener (documentListener);

    JButton minimumLinkButton = new JButton(LINK_ICON);
    minimumLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = minimumField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexTypeCombo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(minimumField,integerType ? "int" : "long", value);
        if (variable!=null) minimumField.setText(variable);
      }
    });

    JPanel minimumPanel = new JPanel(new BorderLayout());
    minimumPanel.add(minimumLabel,BorderLayout.WEST);
    minimumPanel.add(minimumField,BorderLayout.CENTER);
    minimumPanel.add(minimumLinkButton,BorderLayout.EAST);

    JLabel maximumLabel = new JLabel(RES.getString("ParallelJava.MaximumIndex"));
    maximumLabel.setForeground(COLOR);
    maximumLabel.setBorder(LABEL_BORDER);
    
    maximumField.getDocument().addDocumentListener (documentListener);

    JButton maximumLinkButton = new JButton(LINK_ICON);
    maximumLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = maximumField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexTypeCombo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(maximumField,integerType ? "int" : "long", value);
        if (variable!=null) maximumField.setText(variable);
      }
    });

    JPanel maximumPanel = new JPanel(new BorderLayout());
    maximumPanel.add(maximumLabel,BorderLayout.WEST);
    maximumPanel.add(maximumField,BorderLayout.CENTER);
    maximumPanel.add(maximumLinkButton,BorderLayout.EAST);

    JLabel indexTypeLabel = new JLabel(RES.getString("ParallelJava.IndexType"));
    indexTypeLabel.setForeground(COLOR);
    indexTypeLabel.setBorder(LABEL_BORDER);
    indexTypeCombo.addItemListener(itemListener);

    JPanel indexTypePanel = new JPanel(new BorderLayout());
    indexTypePanel.add(indexTypeLabel,BorderLayout.WEST);
    indexTypePanel.add(indexTypeCombo,BorderLayout.CENTER);

    JLabel scheduleTypeLabel = new JLabel(RES.getString("ParallelJava.ScheduleType"));
    scheduleTypeLabel.setForeground(COLOR);
    scheduleTypeLabel.setBorder(LABEL_BORDER);
    scheduleModeCombo.addItemListener(itemListener);

    JPanel scheduleTypePanel = new JPanel(new BorderLayout());
    scheduleTypePanel.add(scheduleTypeLabel,BorderLayout.WEST);
    scheduleTypePanel.add(scheduleModeCombo,BorderLayout.CENTER);

    JLabel scheduleChunkLabel = new JLabel(RES.getString("ParallelJava.ChunkSize"));
    scheduleChunkLabel.setForeground(COLOR);
    scheduleChunkLabel.setBorder(LABEL_BORDER);
    scheduleChunkField.getDocument().addDocumentListener (documentListener);

    JButton scheduleChunkLinkButton = new JButton(LINK_ICON);
    scheduleChunkLinkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = scheduleChunkField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        boolean integerType = (indexTypeCombo.getSelectedIndex()==0);
        String variable = collection.chooseVariable(scheduleChunkField,integerType ? "int" : "long", value);
        if (variable!=null) scheduleChunkField.setText(variable);
      }
    });

    JPanel scheduleChunckPanel = new JPanel(new BorderLayout());
    scheduleChunckPanel.add(scheduleChunkLabel,BorderLayout.WEST);
    scheduleChunckPanel.add(scheduleChunkField,BorderLayout.CENTER);
    scheduleChunckPanel.add(scheduleChunkLinkButton,BorderLayout.EAST);

    JPanel scheduleLeftPanel = new JPanel(new BorderLayout());
    scheduleLeftPanel.add(scheduleTypePanel,BorderLayout.WEST);
    scheduleLeftPanel.add(scheduleChunckPanel,BorderLayout.CENTER);

    JPanel indexesCenterPanel = new JPanel(new GridLayout(1,0));
    indexesCenterPanel.add(minimumPanel);
    indexesCenterPanel.add(maximumPanel);

    JPanel runTopBottomPanel = new JPanel(new BorderLayout());
    runTopBottomPanel.add(indexTypePanel,BorderLayout.WEST);
    runTopBottomPanel.add(scheduleLeftPanel,BorderLayout.CENTER);

    JPanel runTopPanel = new JPanel(new GridLayout(0,1));
    runTopPanel.add(indexesCenterPanel);
    runTopPanel.add(runTopBottomPanel);
    runTopPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

    JLabel barrierActionLabel = new JLabel(RES.getString("ParallelJava.BarrierAction"));
    barrierActionLabel.setForeground(COLOR);
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
    barrierPanel.add(barrierEditor.getComponent (collection),BorderLayout.CENTER);
    
    barrierEditor.setVisible(barrierActionCombo.getSelectedIndex()==2);

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
    
    JComponent loopComp = loopEditor.getComponent(collection, new Component[] { null, null, runTopPanel, null });
    loopComp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (loopComp,mKeywords[1]);

    barrierPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    topPanel.add (barrierPanel,mKeywords[2]);
        
    cardLayout.show (topPanel,mKeywords[1]);
    buttons[1].setSelected(true);
    loopEditor.showEditor(2); // Show the run panel

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(600,500));
    mainPanel.add(toolbar,BorderLayout.NORTH);
    mainPanel.add(topPanel,BorderLayout.CENTER);

    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    list.addAll(regionRunEditor.search(info, searchString, mode, name, collection));
    list.addAll(loopEditor.search(info, searchString, mode, name, collection));
    list.addAll(barrierEditor.search(info, searchString, mode, name, collection));

    addToSearch(list,minimumField,info,searchString,mode,this,name,collection);
    addToSearch(list,maximumField,info,searchString,mode,this,name,collection);
    addToSearch(list,scheduleChunkField,info,searchString,mode,this,name,collection);
    return list;
  }
  
}
