/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.utils.*;

import atp.cHotEqn;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;
import org.w3c.dom.Element;
import org.colos.ejs.library.control.value.*;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.ODE;

public class EquationEditor implements Editor {
  
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");
  static private String solverNames[]=null, solverClassname[]=null, solverRes[]= null;
  static private Font DEFAULT_FONT = InterfaceUtils.font(null,res.getString("Osejs.DefaultFont"));
  static private final int COLUMNS = res.getInteger("EquationEditor.Parameters.FieldWidth");
  static private final String DEF_TOL = "0.00001";
  static private final String DEF_NUM_STEPS = "10000";
  static private Icon ADVANCED_ICON = ResourceLoader.getIcon("data/icons/Advanced.png");
  static private Icon ADVANCED_RED_ICON = ResourceLoader.getIcon("data/icons/AdvancedRED.png");

  static final Border BORDER_SMALL = new EmptyBorder(0,5,0,5);
  static private final EmptyBorder BORDER0202 = new EmptyBorder(0,2,0,2);
  static private final EmptyBorder BORDER0303 = new EmptyBorder(0,3,0,3);
  static final Insets NULL_INSETS = new Insets (0,0,0,0);
  
  static private final TwoStrings FALSE_VALUE = new TwoStrings(res.getString("EditorForBoolean.false"),"false");
  static private final TwoStrings TRUE_VALUE = new TwoStrings(res.getString("EditorForBoolean.true"),"true");

  private Color myColor=InterfaceUtils.color(res.getString("Model.Color")), defaultForeground=null;
  private Font myFont;
  private JComboBox<String> solverCombo;
  private JFrame eventDialog=null, prelimDialog = null, parametersDialog=null, zenoDialog=null;
  private Vector<String> emptyRow;
  private DefaultTableModel tableModel;
  private JTable table;
  private JPanel eventTopPanel, matrixFinalPanel;
  private JTabbedPane parametersFullPanel;
  private JTextField independentField, incrementField, eventField, commentField; //, toleranceField;
  private JButton prelimButton, zenoButton, eventButton, advancedButton;
  protected TabbedEventEditor eventEditor;
  private TabbedErrorEditor errorEditor;
  protected TabbedDiscontinuityEditor discontinuityEditor;
  protected CodeEditor prelimEditor; //, errorEditor;
  protected ZenoEditor zenoEditor; 
  private Set<JComponent> componentSet = new HashSet<JComponent>(); // list of components to which change color
  private Set<JComponent> activeComponentSet = new HashSet<JComponent>(); // list of components to enable/disable when setActive is called
  private Set<JComponent> activeAndNeedToleranceComponentSet = new HashSet<JComponent>(); // list of components to enable/disable when setActive is called
    
  private JTextComponent internalStepField, maximumStepField, maximumNumberOfStepsField, relativeToleranceField, absoluteToleranceField, eventStepField;
  private JTextArea directMatrixArea;
  private JComboBox<TwoStrings> forceSynchroCombo, estimateFirstStepCombo, useBestInterpolationCombo, accIndVelocityCombo;
  private JTextComponent memoryLengthField;

  // Delay differential equations
  private CodeEditor delayInitialConditionEditor; 
  private JTextComponent delayListField, maxDelayField;
  private JTextComponent delayAddDiscontField;
   
  private TitledBorder titleBorder;

  private int activeRow = -1;
  private boolean changed=false, advanced=false; //, reading=false;

  private String  name, title="",generateName;
  private JPanel fullPanel,mostOfAllPanel;
  private JPopupMenu popup, popup2;
  private Osejs ejs=null;
  protected TabbedEvolutionEditor parentTabbedEditor;
  private boolean isAlgebraBased;
  private JPanel solverAndTolerancePanel;

  public EquationEditor (Osejs _ejs, TabbedEvolutionEditor aParentEditor) {
    parentTabbedEditor = aParentEditor;
    ejs = _ejs;
    isAlgebraBased = ejs.getSimInfoEditor().isDeltaEquation();
    Icon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("EquationEditor.Select.Icon"));
    if (icon==null) {
      System.out.println ("Could not read icon " + sysRes.getString("EquationEditor.Select.Icon"));
    }
    if (solverNames==null) {
      String languagePrefix = ejs.supportsJava() ? "EquationEditor" : "JavascriptEquationEditor";
      solverNames = ResourceUtil.tokenizeString(sysRes.getString(languagePrefix+".ODESolvers"));
      solverClassname = new String[solverNames.length];
      solverRes       = new String[solverNames.length];
      for (int j=0; j<solverNames.length; j++) {
        solverClassname[j] = sysRes.getString(languagePrefix+"."+solverNames[j]);
        solverRes[j] = res.getString("EquationEditor."+solverNames[j]+".short");
      }
    }

    emptyRow = new Vector<String>();
    emptyRow.addElement(new String("")); //d / d  = "));
    emptyRow.addElement(new String(""));

    Vector<String> columnNames = new Vector<String>();
    columnNames.add (res.getString("EquationEditor.State"));
    columnNames.add (res.getString("EquationEditor.Rate"));

    tableModel  = new MyDefaultTableModel (columnNames,0);
    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener (new MLSL());
    if (OSPRuntime.isMac()) table.setGridColor(Color.LIGHT_GRAY);
    activeComponentSet.add(table);
    JScrollPane scrollPanel = new JScrollPane (table);

    ActionListener menuAL = new ActionListener() {
      public void actionPerformed (ActionEvent evt) {
        myActionPerformed (evt);
        changed = true;
      }
    };

    ActionListener changeActionListener = new ActionListener (){
      public void actionPerformed(ActionEvent _evt) { changed = true; checkAdvancedPanel(); }
    };

    KeyListener keyListener = new KeyAdapter() {
      public void keyPressed  (java.awt.event.KeyEvent _evt) { checkCorrectness(_evt.getComponent()); }
      public void keyReleased (java.awt.event.KeyEvent _evt) { checkCorrectness(_evt.getComponent()); }
      public void keyTyped    (java.awt.event.KeyEvent _evt) { 
        checkCorrectness(_evt.getComponent());
        changed = true;
      }
      private void checkCorrectness(Component comp) {
        boolean isAdvancedField = comp==internalStepField || comp==maximumStepField || comp==maximumNumberOfStepsField || comp==relativeToleranceField || comp==memoryLengthField;
        checkValue(ejs,comp, isAdvancedField || comp==eventStepField || comp==delayListField || comp==maxDelayField || comp==delayAddDiscontField);
        if (isAdvancedField) checkAdvancedPanel();
        if (comp==delayListField) checkDelaysSyntax();
      }
    };
      
    ComponentListener componentListener = new ComponentAdapter() {
      public void componentShown(ComponentEvent _evt) { 
        Component comp = _evt.getComponent(); 
        boolean isAdvancedField = comp==internalStepField || comp==maximumStepField || comp==maximumNumberOfStepsField || comp==relativeToleranceField || comp==memoryLengthField;
        checkValue(ejs,comp,isAdvancedField || comp==eventStepField); 
        if (isAdvancedField) checkAdvancedPanel();
      }
    };

    table.getColumnModel().getColumn(1).setPreferredWidth(res.getInteger("EquationEditor.ColumnSize"));
    for (int i=0; i<table.getColumnModel().getColumnCount(); i++) {
      JTextField textField = new JTextField();
      textField.getDocument().addDocumentListener (new StringDocumentListener(i));
      table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(textField));
    }
    table.getColumnModel().getColumn(0).setCellRenderer(new CustomRenderer());

    JMenuItem createRowButton = new JMenuItem (res.getString("EquationEditor.Create"));
    createRowButton.setActionCommand ("equationCreate");
    createRowButton.addActionListener (menuAL);

    JMenuItem insRowButton = new JMenuItem (res.getString("EquationEditor.Insert"));
    insRowButton.setActionCommand ("equationInsert");
    insRowButton.addActionListener (menuAL);

    JMenuItem addRowButton = new JMenuItem (res.getString("EquationEditor.Add"));
    addRowButton.setActionCommand ("equationAdd");
    addRowButton.addActionListener (menuAL);

    JMenuItem remRowButton = new JMenuItem (res.getString("EquationEditor.Remove"));
    remRowButton.setActionCommand ("equationRemove");
    remRowButton.addActionListener (menuAL);

    JMenuItem selectButton;
    if (icon!=null) selectButton = new JMenuItem (res.getString("EquationEditor.Select"),icon);
    else selectButton = new JMenuItem (res.getString("EquationEditor.Select"));
    selectButton.setActionCommand ("selectState");
    selectButton.addActionListener (menuAL);

    int width = res.getInteger("EquationEditor.FieldWidth");
    JLabel independentLabel = new JLabel (res.getString ("EquationEditor.IndependentVariable"));
    independentLabel.setBorder (BORDER0303);
    componentSet.add(independentLabel);

    independentField = new JTextField(width);
    independentField.setMargin(NULL_INSETS);
    independentField.setEditable (true);
    independentField.setActionCommand ("independentField"); // Added for d/dt
    independentField.addActionListener (menuAL);
    independentField.addKeyListener    (new MyKeyListener());
    independentField.setFont(DEFAULT_FONT);
    independentField.addComponentListener(new ComponentAdapter() {
      public void componentShown(ComponentEvent _evt) { checkIndependentVariable(); }
    });
    activeComponentSet.add(independentField);
    
    JButton independentButton;
    if (icon!=null) independentButton = new JButton (icon);
    else independentButton = new JButton ("...");
    independentButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) independentButton.setBorder(BORDER_SMALL);
    independentButton.setFocusPainted(false);
    independentButton.setActionCommand("selectVariable");
    independentButton.addActionListener(menuAL);
    defaultForeground = independentButton.getForeground();
    activeComponentSet.add(independentButton);

    JLabel incrementLabel   = new JLabel (res.getString ("EquationEditor.Increment"));
    incrementLabel.setBorder (BORDER0303);
    componentSet.add(incrementLabel);
    incrementField   = new JTextField(width);
    incrementField.setEditable (true);
    incrementField.setMargin(NULL_INSETS);
    incrementField.setFont(DEFAULT_FONT);
    incrementField.setActionCommand ("incrementField"); // Added for d/dt
    incrementField.addActionListener (menuAL);
    incrementField.addKeyListener (keyListener);
    incrementField.addComponentListener(componentListener);
    activeComponentSet.add(incrementField);

    JButton incrementButton;
    if (icon!=null) incrementButton = new JButton (icon);
    else incrementButton = new JButton ("...");
    incrementButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) incrementButton.setBorder(BORDER_SMALL);
    incrementButton.setFocusPainted(false);
    incrementButton.setActionCommand("selectIncrement");
    incrementButton.addActionListener(menuAL);
    activeComponentSet.add(incrementButton);

    JPanel row1Left = new JPanel (new BorderLayout());
    row1Left.add (independentLabel,BorderLayout.WEST);
    row1Left.add (independentField,BorderLayout.CENTER);
    row1Left.add (independentButton,BorderLayout.EAST);
    
    JPanel row1Right = new JPanel (new BorderLayout());
    row1Right.add (incrementLabel,BorderLayout.WEST);
    row1Right.add (incrementField,BorderLayout.CENTER);
    row1Right.add (incrementButton,BorderLayout.EAST);
    
    JPanel topCenterPanel = new JPanel (new GridLayout(1,0)); //BorderLayout()); //
    topCenterPanel.add (row1Left);
    topCenterPanel.add (row1Right);
    
    prelimButton = new JButton (res.getString("EquationEditor.PreliminaryCode"));
    prelimButton.setToolTipText(res.getString("EquationEditor.PreliminaryCode.Tooltip"));
//    prelimButton.setIcon(null);
    prelimButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) prelimButton.setBorder(BORDER_SMALL);
    prelimButton.setFocusPainted(false);
    prelimButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { prelimDialog.setVisible(true); }
    });

    JPanel topPanel = new JPanel (new BorderLayout());
    topPanel.add (topCenterPanel,BorderLayout.CENTER);
    topPanel.add (prelimButton,BorderLayout.EAST);

    prelimEditor = new CodeEditor (ejs, null);
    prelimEditor.setName(res.getString("EquationEditor.PreliminaryCode"));
    prelimEditor.setColor(myColor);
    prelimEditor.setFont (myFont);
    prelimEditor.getCommentField().setText(res.getString("EquationEditor.PrelimComment"));
    prelimEditor.addDocumentListener(new DocumentListener(){
      public void changedUpdate (DocumentEvent evt)  { checkContents(); }
      public void insertUpdate  (DocumentEvent evt)  { checkContents(); }
      public void removeUpdate  (DocumentEvent evt)  { checkContents(); }
      private void checkContents() {
        String text = prelimEditor.getTextComponent().getText().trim();
        if (text.length()>0) {
//          if (prelimButton.getIcon()==null) prelimButton.setIcon(NON_EMPTY_ICON);
          if (prelimButton.getForeground()==defaultForeground) prelimButton.setForeground(myColor);
        }
        else {
//          if (prelimButton.getIcon()!=null) prelimButton.setIcon(null);
          if (prelimButton.getForeground()!=defaultForeground) prelimButton.setForeground(defaultForeground);
        }
      }
    });
    
    prelimDialog = new JFrame(res.getString("EquationEditor.PreliminaryCodeFor")+" "+name);
    java.awt.Image image = org.opensourcephysics.tools.ResourceLoader.getImage("data/icons/EjsIcon.gif");
    if (image!=null) prelimDialog.setIconImage(image);
    prelimDialog.setSize (res.getDimension("EquationEditor.Events.DialogSize"));
    prelimDialog.setLocationRelativeTo (getComponent());
    prelimDialog.getContentPane().setLayout (new BorderLayout());
    prelimDialog.getContentPane().add(prelimEditor.getComponent(), BorderLayout.CENTER);
    prelimDialog.validate();
    ejs.setMenuBar(prelimDialog);

    // -----------------------------------
    // Method row
    // -----------------------------------
    {
      String languagePrefix = ejs.supportsJava() ? "EquationEditor" : "JavascriptEquationEditor";
      String def = sysRes.getString(languagePrefix+".DefaultSolver");
      ActionListener cal = new ActionListener() {
        public void actionPerformed(ActionEvent _e) {
          if (solverCombo.isEnabled()) {
            int index = solverCombo.getSelectedIndex();
            String solverType = solverClassname[index];
            boolean needsTolerance = needsTolerance(solverType);
            //          absoluteTolerancePanel.setVisible(needsTolerance);
            //          errorEditor.setActive(needsTolerance);
            for (JComponent comp : activeAndNeedToleranceComponentSet) {
              comp.setEnabled(needsTolerance);
              if (comp instanceof JTextField) ((JTextField) comp).setEditable(needsTolerance);
            }
            solverCombo.setToolTipText(res.getString("EquationEditor."+solverNames[index]));
            showQSSPanel(solverType.toLowerCase().startsWith("qss"));
            checkAdvancedPanel();
          }
          changed = true;
        }
      };
      solverCombo = new JComboBox<String>();
      for (int i=0; i<solverNames.length; i++) {
        solverCombo.addItem(solverRes[i]);
        if (solverNames[i].equals(def)) {
          solverCombo.setSelectedIndex(i);
          solverCombo.setToolTipText(res.getString("EquationEditor."+solverNames[i]));
        }
      }
      solverCombo.addActionListener(cal);
      activeComponentSet.add(solverCombo);
    }
    
    JLabel solverLabel = new JLabel (res.getString("EquationEditor.Method"));
    solverLabel.setBorder(BORDER0202);
    componentSet.add(solverLabel);
    
//    JPanel solverComboSmallPanel = new JPanel (new BorderLayout());
//    solverComboSmallPanel.add(solverCombo,BorderLayout.WEST);

//    JPanel solverLeftPanel = new JPanel (new BorderLayout()); //FlowLayout(FlowLayout.LEFT)); //
//    solverLeftPanel.add(solverLabel,BorderLayout.WEST);
//    solverLeftPanel.add(solverCombo,BorderLayout.CENTER);

    JLabel solverHelpButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Info")));   
    solverHelpButton.setToolTipText(res.getString("EquationEditor.Help"));
    solverHelpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    solverHelpButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) { ejs.openWikiPage("ModelODESolvers"); }
    });

    JPanel solverPanel = new JPanel (new BorderLayout()); //FlowLayout(FlowLayout.LEFT)); //
//    solverPanel.setBorder(new EmptyBorder(0,0,0,0));
    solverPanel.add(solverLabel,BorderLayout.WEST);
    solverPanel.add(solverCombo,BorderLayout.CENTER);
//    solverPanel.add(solverHelpButton,BorderLayout.EAST);

    // -----------------------------------
    // Advanced parameters window
    // -----------------------------------
    
    DocumentListener dl = new DocumentListener() {
      public void changedUpdate (DocumentEvent evt) { changed = true; }
      public void insertUpdate (DocumentEvent evt)  { changed = true; }
      public void removeUpdate (DocumentEvent evt)  { changed = true; }
    };

    Set<JComponent> labelSet = new HashSet<JComponent>();

    JPanel internalStepPanel = SimInfoEditor.makeLabel ("EquationEditor.InternalStep",labelSet);
    internalStepField = SimInfoEditor.makeField (internalStepPanel,dl,1,COLUMNS);
    internalStepField.addKeyListener (keyListener);
    internalStepField.addComponentListener(componentListener);
    internalStepField.setText("");
    activeComponentSet.add(internalStepField);
    
    JButton internalStepButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    internalStepButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) internalStepButton.setBorder(BORDER_SMALL);
    internalStepButton.setFocusPainted(false);
    internalStepButton.setActionCommand("selectInternalStep");
    internalStepButton.addActionListener(menuAL);
    activeComponentSet.add(internalStepButton);

    internalStepPanel.add(internalStepButton,BorderLayout.EAST);
    
    JPanel memoryLengthPanel = SimInfoEditor.makeLabel ("EquationEditor.MemoryLength",labelSet);
    memoryLengthField = SimInfoEditor.makeField (memoryLengthPanel,dl,1,COLUMNS);
    memoryLengthField.addKeyListener (keyListener);
    memoryLengthField.addComponentListener(componentListener);
    memoryLengthField.setText("");
    activeComponentSet.add(memoryLengthField);

    JButton memoryLengthButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    memoryLengthButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) memoryLengthButton.setBorder(BORDER_SMALL);
    memoryLengthButton.setFocusPainted(false);
    memoryLengthButton.setActionCommand("selectMemoryLength");
    memoryLengthButton.addActionListener(menuAL);
    activeComponentSet.add(memoryLengthButton);
    
    memoryLengthPanel.add(memoryLengthButton,BorderLayout.EAST);
        
    JPanel maximumStepPanel = SimInfoEditor.makeLabel ("EquationEditor.MaximumStep",labelSet);
    maximumStepField = SimInfoEditor.makeField (maximumStepPanel,dl,1,COLUMNS);
    maximumStepField.addKeyListener (keyListener);
    maximumStepField.addComponentListener(componentListener);
    maximumStepField.setText("");
    this.activeAndNeedToleranceComponentSet.add(maximumStepField);

    JButton maximumStepButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    maximumStepButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) maximumStepButton.setBorder(BORDER_SMALL);
    maximumStepButton.setFocusPainted(false);
    maximumStepButton.setActionCommand("selectMaximumStep");
    maximumStepButton.addActionListener(menuAL);
    this.activeAndNeedToleranceComponentSet.add(maximumStepButton);
    maximumStepPanel.add(maximumStepButton,BorderLayout.EAST);

    JPanel maximumNumberOfStepsPanel = SimInfoEditor.makeLabel ("EquationEditor.MaximumNumberOfSteps",labelSet);
    maximumNumberOfStepsField = SimInfoEditor.makeField (maximumNumberOfStepsPanel,dl,1,COLUMNS);
    maximumNumberOfStepsField.setText(DEF_NUM_STEPS);
    maximumNumberOfStepsField.addKeyListener (keyListener);
    maximumNumberOfStepsField.addComponentListener(componentListener);
    this.activeAndNeedToleranceComponentSet.add(maximumNumberOfStepsField);

    JButton maximumNumberOfStepsButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    maximumNumberOfStepsButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) maximumNumberOfStepsButton.setBorder(BORDER_SMALL);
    maximumNumberOfStepsButton.setFocusPainted(false);
    maximumNumberOfStepsButton.setActionCommand("selectMaximumNumberOfSteps");
    maximumNumberOfStepsButton.addActionListener(menuAL);
    this.activeAndNeedToleranceComponentSet.add(maximumNumberOfStepsButton);
    maximumNumberOfStepsPanel.add(maximumNumberOfStepsButton,BorderLayout.EAST);

    JPanel relativeTolerancePanel = SimInfoEditor.makeLabel ("EquationEditor.RelativeTolerance",labelSet);
    relativeToleranceField = SimInfoEditor.makeField (relativeTolerancePanel,dl,1,COLUMNS);
    relativeToleranceField.addKeyListener (keyListener);
    relativeToleranceField.addComponentListener(componentListener);
    relativeToleranceField.setText("");
    this.activeAndNeedToleranceComponentSet.add(relativeToleranceField);

    JButton relativeToleranceButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    relativeToleranceButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) relativeToleranceButton.setBorder(BORDER_SMALL);
    relativeToleranceButton.setFocusPainted(false);
    relativeToleranceButton.setActionCommand("selectRelativeTolerance");
    relativeToleranceButton.addActionListener(menuAL);
    this.activeAndNeedToleranceComponentSet.add(relativeToleranceButton);
    relativeTolerancePanel.add(relativeToleranceButton,BorderLayout.EAST);

    JLabel absoluteToleranceLabel = new JLabel(res.getString("EquationEditor.Tolerance"), SwingConstants.RIGHT);
    absoluteToleranceLabel.setToolTipText(res.getString("EquationEditor.AbsoluteTolerance"));
    absoluteToleranceLabel.setBorder(BORDER0202);
    componentSet.add(absoluteToleranceLabel);

    absoluteToleranceField = new JTextField(COLUMNS);
    absoluteToleranceField.getDocument().addDocumentListener(dl);
    absoluteToleranceField.addKeyListener (keyListener);
    absoluteToleranceField.addComponentListener(componentListener);
    absoluteToleranceField.setText(DEF_TOL);
    absoluteToleranceField.setFont(DEFAULT_FONT);
    absoluteToleranceField.setToolTipText(res.getString("EquationEditor.AbsoluteTolerance"));
    this.activeAndNeedToleranceComponentSet.add(absoluteToleranceField);

    absoluteToleranceField.addMouseListener(new MouseAdapter() {
      public void mouseClicked (MouseEvent _evt) {
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          String valueStr = org.colos.ejs.control.editors.EditorForString.edit(res.getString("EquationEditor.Tolerance"),absoluteToleranceField);
          if (valueStr==null) return; // Edition was canceled
          if ("<default>".equals(valueStr)) valueStr = "1.0e-5";
          absoluteToleranceField.setText(valueStr);
          checkValue(ejs,absoluteToleranceField,false);
        }
      }
    });

    
    JButton absoluteToleranceButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    absoluteToleranceButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) absoluteToleranceButton.setBorder(BORDER_SMALL);
    absoluteToleranceButton.setFocusPainted(false);
    absoluteToleranceButton.setActionCommand("selectAbsoluteTolerance");
    absoluteToleranceButton.addActionListener(menuAL);
    this.activeAndNeedToleranceComponentSet.add(absoluteToleranceButton);

    JPanel absoluteTolerancePanel = new JPanel(new BorderLayout());
    absoluteTolerancePanel.setBorder(new EmptyBorder(0,0,0,2));
    absoluteTolerancePanel.add(absoluteToleranceLabel,BorderLayout.WEST);
    absoluteTolerancePanel.add(absoluteToleranceField,BorderLayout.CENTER);
    absoluteTolerancePanel.add(absoluteToleranceButton,BorderLayout.EAST);

    JPanel estimateFirstStepPanel = SimInfoEditor.makeLabel ("EquationEditor.EstimateFirstStep",labelSet);
    estimateFirstStepCombo = new JComboBox<TwoStrings>();
    estimateFirstStepCombo.addItem(FALSE_VALUE);
    estimateFirstStepCombo.addItem(TRUE_VALUE);
    estimateFirstStepCombo.setSelectedIndex(0);
    estimateFirstStepCombo.addActionListener(changeActionListener);
    this.activeAndNeedToleranceComponentSet.add(estimateFirstStepCombo);
    JPanel estimateFirstStepComboSmallPanel = new JPanel (new BorderLayout());
    estimateFirstStepComboSmallPanel.add(estimateFirstStepCombo,BorderLayout.WEST);
    estimateFirstStepPanel.add(estimateFirstStepComboSmallPanel, BorderLayout.CENTER);

    JPanel accIndVelocityPanel = SimInfoEditor.makeLabel ("EquationEditor.AccelerationIndependentOfVelocity",labelSet);
    accIndVelocityCombo = new JComboBox<TwoStrings>();
    accIndVelocityCombo.addItem(FALSE_VALUE);
    accIndVelocityCombo.addItem(TRUE_VALUE);
    accIndVelocityCombo.setSelectedIndex(0);
    accIndVelocityCombo.addActionListener(changeActionListener);
    this.activeComponentSet.add(accIndVelocityCombo);
    
    JPanel accIndVelocityComboSmallPanel = new JPanel (new BorderLayout());
    accIndVelocityComboSmallPanel.add(accIndVelocityCombo,BorderLayout.WEST);
    accIndVelocityPanel.add(accIndVelocityComboSmallPanel, BorderLayout.CENTER);
    accIndVelocityPanel.add(solverHelpButton, BorderLayout.EAST);

    JPanel forceSynchroPanel = SimInfoEditor.makeLabel ("EquationEditor.ForceSynchronization",labelSet);
    forceSynchroCombo = new JComboBox<TwoStrings>();
    forceSynchroCombo.addItem(FALSE_VALUE);
    forceSynchroCombo.addItem(TRUE_VALUE);
    forceSynchroCombo.setSelectedIndex(0);
    forceSynchroCombo.addActionListener(changeActionListener);
    this.activeComponentSet.add(forceSynchroCombo);
    
    JPanel forceSynchroComboSmallPanel = new JPanel (new BorderLayout());
    forceSynchroComboSmallPanel.add(forceSynchroCombo,BorderLayout.WEST);
    forceSynchroPanel.add(forceSynchroComboSmallPanel, BorderLayout.CENTER);
//    forceSynchroPanel.add(solverHelpButton, BorderLayout.EAST);

    JPanel useBestInterpolationPanel = SimInfoEditor.makeLabel ("EquationEditor.UseBestInterpolation",labelSet);
    useBestInterpolationCombo = new JComboBox<TwoStrings>();
    useBestInterpolationCombo.addItem(FALSE_VALUE);
    useBestInterpolationCombo.addItem(TRUE_VALUE);
    useBestInterpolationCombo.setSelectedIndex(0);
    useBestInterpolationCombo.addActionListener(changeActionListener);
    this.activeComponentSet.add(useBestInterpolationCombo);
    
    JPanel useBestInterpolationComboSmallPanel = new JPanel (new BorderLayout());
    useBestInterpolationComboSmallPanel.add(useBestInterpolationCombo,BorderLayout.WEST);
    useBestInterpolationPanel.add(useBestInterpolationComboSmallPanel, BorderLayout.CENTER);
    
    errorEditor = new TabbedErrorEditor(_ejs,this);
    errorEditor.setName("EquationEditor.ErrorEditor");
    errorEditor.setContentDelim ("ErrorHandlingContent");

    discontinuityEditor = new TabbedDiscontinuityEditor(_ejs,this);
    discontinuityEditor.setName("EquationEditor.DiscontinuityEditor");
    discontinuityEditor.setContentDelim ("DiscontinuityContent");

    
//    JPanel eventLocationPanel = SimInfoEditor.makeLabel ("EquationEditor.EventLocationParameters",null); //componentSet);
//    eventLocationPanel.add(new JSeparator(JSeparator.HORIZONTAL),BorderLayout.SOUTH);

    JLabel eventStepLabel = new JLabel(" "+res.getString("EquationEditor.EventMaximumStep")+" ");
    componentSet.add(eventStepLabel);
    
    eventStepField = new JTextField(COLUMNS);
    eventStepField.getDocument().addDocumentListener(dl);
    eventStepField.addKeyListener (keyListener);
    eventStepField.addComponentListener(componentListener);
    eventStepField.setText("");
    activeComponentSet.add(eventStepField);

    JButton eventStepButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    eventStepButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) eventStepButton.setBorder(BORDER_SMALL);
    eventStepButton.setFocusPainted(false);
    eventStepButton.setActionCommand("selectEventStep");
    eventStepButton.addActionListener(menuAL);
    this.activeComponentSet.add(eventStepButton);

    JPanel eventStepPanel = new JPanel (new BorderLayout());
    eventStepPanel.add(eventStepLabel,BorderLayout.WEST);
    eventStepPanel.add(eventStepField,BorderLayout.CENTER);
    eventStepPanel.add(eventStepButton,BorderLayout.EAST);

    JLabel eventHelpButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Info")));   
    eventHelpButton.setToolTipText(res.getString("EquationEditor.Events.Help"));
    eventHelpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    eventHelpButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) { ejs.openWikiPage("ModelEvents"); }
    });
    
    zenoButton = new JButton (res.getString("EquationEditor.Events.ZenoEffect"));
    zenoButton.setIcon(null);
    zenoButton.setMargin(NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) zenoButton.setBorder(BORDER_SMALL);
    zenoButton.setFocusPainted(false);
    zenoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { editZenoEffect(true); }
    });
    
    eventTopPanel = new JPanel (new BorderLayout());
    eventTopPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    eventTopPanel.add(eventStepPanel,BorderLayout.CENTER);
    eventTopPanel.add(zenoButton,BorderLayout.WEST);
    eventTopPanel.add(eventHelpButton,BorderLayout.EAST);

    // --- Delay differential equations
    
    JPanel delayListPanel = SimInfoEditor.makeLabel ("EquationEditor.Delays",componentSet);
    
    delayListField = SimInfoEditor.makeField (delayListPanel,dl,1,COLUMNS);
    delayListField.addKeyListener (keyListener);
    delayListField.addComponentListener(componentListener);
    delayListField.setText("");
    activeComponentSet.add(delayListField);

    JPanel maxDelayPanel = SimInfoEditor.makeLabel ("EquationEditor.MaximumDelay",componentSet);

    maxDelayField = SimInfoEditor.makeField (maxDelayPanel,dl,1,COLUMNS);
    maxDelayField.addKeyListener (keyListener);
    maxDelayField.addComponentListener(componentListener);
    maxDelayField.setText("");
    activeComponentSet.add(maxDelayField);

    JPanel delayAddDiscontPanel = SimInfoEditor.makeLabel ("EquationEditor.DelaysInitialConditionDiscontinuities",componentSet);
    delayAddDiscontField = SimInfoEditor.makeField (delayAddDiscontPanel,dl,1,COLUMNS);
    delayAddDiscontField.addKeyListener (keyListener);
    delayAddDiscontField.addComponentListener(componentListener);
    delayAddDiscontField.setText("");
    activeComponentSet.add(delayAddDiscontField);
    
    delayInitialConditionEditor = new CodeEditor (ejs, null,true);
    delayInitialConditionEditor.setName(res.getString("EquationEditor.DelaysInitialConditionCode"));
    delayInitialConditionEditor.setColor(myColor);
    delayInitialConditionEditor.setFont (myFont);
    delayInitialConditionEditor.setEditable(true);
    delayInitialConditionEditor.addDocumentListener(new DocumentListener(){
      public void changedUpdate (DocumentEvent evt)  { checkAdvancedPanel(); }
      public void insertUpdate  (DocumentEvent evt)  { checkAdvancedPanel(); }
      public void removeUpdate  (DocumentEvent evt)  { checkAdvancedPanel(); }
    });
    
    JButton delayGuessButton = new JButton (res.getString("EquationEditor.Guess"));
    delayGuessButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        String txt = delayGuessInitialConditions();
        if (txt!=null) delayInitialConditionEditor.getTextComponent().setText(txt); 
      }
    });

    // --- QSS methods    
    
    JPanel matrixPanel = SimInfoEditor.makeLabel ("EquationEditor.QSSDirectIncidenceMatrix",componentSet);
    directMatrixArea = new JTextArea(1, 2*COLUMNS);
    directMatrixArea.setFont(DEFAULT_FONT);
    directMatrixArea.getDocument().addDocumentListener(dl);
    directMatrixArea.setLineWrap(false);
    directMatrixArea.setWrapStyleWord(false);
    new Undo2(directMatrixArea,_ejs.getModelEditor());

    matrixFinalPanel = new JPanel(new BorderLayout());
    matrixFinalPanel.add(matrixPanel, BorderLayout.NORTH);
    matrixFinalPanel.add(new JScrollPane (directMatrixArea), BorderLayout.CENTER);
    directMatrixArea.setText("");
    
    JButton directMatrixButton = new JButton (res.getString("EquationEditor.Guess"));
    directMatrixButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        String txt = guessIncidenceMatrix();
        if (txt!=null) directMatrixArea.setText(txt); 
      }
    });
    matrixFinalPanel.add(directMatrixButton, BorderLayout.SOUTH);
    
    // --- Final wrap-up
    
    // --- Make all labels in the set the same dimension
    int maxWidth = 0, maxHeight=0;
    for (JComponent label : labelSet) {
      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
    }
    Dimension dim = new Dimension (maxWidth,maxHeight);
    for (JComponent label : labelSet) label.setPreferredSize(dim);
    componentSet.addAll(labelSet);
    
//    errorEditor.getComponent().setPreferredSize(new Dimension(maxWidth,5*maxHeight)); // Only the height matters
//    errorEditor.getComponent().validate();
    
    // Standard parameters panel 
    Box parametersBox = Box.createVerticalBox();
    parametersBox.add(accIndVelocityPanel);
    parametersBox.add(forceSynchroPanel);
    parametersBox.add(useBestInterpolationPanel);
    parametersBox.add(estimateFirstStepPanel);
    parametersBox.add(memoryLengthPanel);
    parametersBox.add(internalStepPanel);
    parametersBox.add(maximumStepPanel);
    parametersBox.add(maximumNumberOfStepsPanel);
    parametersBox.add(relativeTolerancePanel);
//    parametersBox.add(Box.createVerticalStrut(5));
//    parametersBox.add(new JSeparator(SwingConstants.HORIZONTAL));

    JPanel parametersPanel = new JPanel (new BorderLayout());
    parametersPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
    parametersPanel.add(parametersBox,BorderLayout.NORTH);
//    parametersPanel.add(errorEditor.getComponent(),BorderLayout.CENTER);

    // DDE parameters panel
    Box delayBox = Box.createVerticalBox();
    delayBox.add(delayListPanel);
    delayBox.add(maxDelayPanel);
    delayBox.add(delayAddDiscontPanel);

    JPanel delayPanel = new JPanel (new BorderLayout());
    delayPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
    delayPanel.add(delayBox,BorderLayout.NORTH);
    delayPanel.add(delayInitialConditionEditor.getComponent(),BorderLayout.CENTER);
    delayPanel.add(delayGuessButton,BorderLayout.SOUTH);
    
    // main tabbed panel
    parametersFullPanel = new JTabbedPane();
    componentSet.add(parametersFullPanel);
    parametersFullPanel.addTab(res.getString("EquationEditor.OperationParameters"),parametersPanel);     
    parametersFullPanel.addTab(res.getString("EquationEditor.DiscontinuityEditor"),discontinuityEditor.getComponent());  
    parametersFullPanel.addTab(res.getString("EquationEditor.DDEparameters"),delayPanel);  
    parametersFullPanel.addTab(res.getString("EquationEditor.ErrorEditor"),errorEditor.getComponent());  
    
    // QSS parameters are included only if a QSS method is selected
//    parametersFullPanel.addTab(res.getString("EquationEditor.QSSInformation"),matrixFinalPanel);

    // main parameters window
    parametersDialog = new JFrame(res.getString("EquationEditor.ParametersFor")+" "+name);
    if (image!=null) parametersDialog.setIconImage(image);
    parametersDialog.getContentPane().setLayout (new BorderLayout());
    parametersDialog.getContentPane().add(parametersFullPanel, BorderLayout.CENTER);
    parametersDialog.validate();
    parametersDialog.pack();
    parametersDialog.setLocationRelativeTo (getComponent());
    ejs.setMenuBar(parametersDialog);


    advancedButton = new JButton(ADVANCED_ICON); //res.getString("EquationEditor.Parameters"));
    advancedButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { parametersDialog.setVisible(true); }
    });
    advancedButton.setMargin (NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) advancedButton.setBorder(BORDER_SMALL);
    advancedButton.setToolTipText (res.getString("EquationEditor.ParametersTooltip"));
    
    // -----------------------------------
    // Events row
    // -----------------------------------

    eventButton = new JButton(res.getString("EquationEditor.Events.Defined"));
    eventButton.setActionCommand("editEvents");
    eventButton.addActionListener(menuAL);
    eventButton.setMargin (NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) eventButton.setBorder(BORDER_SMALL);
    eventButton.setToolTipText (res.getString("EquationEditor.Events.EditEvents"));

    eventField = new JTextField (2);
    eventField.setFont(eventField.getFont().deriveFont(Font.BOLD));
    eventField.setEditable(false);
    eventField.setText("0");
    eventField.setFont(DEFAULT_FONT);
    eventField.setMargin (NULL_INSETS);
    eventField.setHorizontalAlignment(SwingConstants.RIGHT);

    JPanel eventPanel = new JPanel (new BorderLayout());
    eventPanel.setBorder (new EmptyBorder(0,2,0,0));
    eventPanel.add(eventButton,BorderLayout.WEST);
    eventPanel.add(eventField,BorderLayout.CENTER);

    JPanel eventAndParametersPanel = new JPanel (new BorderLayout());
    eventAndParametersPanel.add(eventPanel,BorderLayout.EAST);
    eventAndParametersPanel.add(advancedButton,BorderLayout.WEST);
    

    // -----------------------------------
    // Comment Panel
    // -----------------------------------

    commentField = new JTextField();
    commentField.setFont(DEFAULT_FONT);
    JLabel commentLabel = new JLabel (res.getString ("Editor.Comment"));
    commentLabel.setBorder(new EmptyBorder(0,0,0,3));
    commentLabel.setFont (InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    activeComponentSet.add(commentField);
    
    JPanel commentPanel = new JPanel (new BorderLayout());
    commentPanel.add (commentLabel,BorderLayout.WEST);
    commentPanel.add (commentField,BorderLayout.CENTER);

    // -----------------------------------
    // Putting everything together
    // -----------------------------------

    solverAndTolerancePanel = new JPanel (new BorderLayout());
    solverAndTolerancePanel.add(solverPanel,BorderLayout.WEST);
    solverAndTolerancePanel.add(absoluteTolerancePanel,BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel (new BorderLayout());
    bottomPanel.add(solverAndTolerancePanel,BorderLayout.CENTER);
    bottomPanel.add(eventAndParametersPanel,BorderLayout.EAST);

    mostOfAllPanel = new JPanel (new BorderLayout());
    mostOfAllPanel.add (topPanel,BorderLayout.NORTH);
    mostOfAllPanel.add (scrollPanel,BorderLayout.CENTER);
    mostOfAllPanel.add (bottomPanel,BorderLayout.SOUTH);

    fullPanel = new JPanel (new BorderLayout());
    fullPanel.add (mostOfAllPanel,BorderLayout.CENTER);
    fullPanel.add (commentPanel,BorderLayout.SOUTH);

    titleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),title+name);
    titleBorder.setTitleJustification (TitledBorder.LEFT);
    titleBorder.setTitleFont (InterfaceUtils.font(null,res.getString("Editor.TitleFont")));
    if (title.trim().length()>0) fullPanel.setBorder (titleBorder);
    else fullPanel.setBorder (new EmptyBorder(10,0,0,0));

    popup = new JPopupMenu();
    popup.add(selectButton);
    popup.add(insRowButton);
    popup.add(addRowButton);
    popup.add(remRowButton);
    table.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent e) {
        if (OSPRuntime.isPopupTrigger(e) //SwingUtilities.isRightMouseButton(e) 
          && table.isEnabled () && activeRow>=0) popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });

    popup2 = new JPopupMenu();
    popup2.add(createRowButton);
    scrollPanel.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent e) {
        if (OSPRuntime.isPopupTrigger(e) //SwingUtilities.isRightMouseButton(e) 
          && table.isEnabled ()) popup2.show(e.getComponent(), e.getX(), e.getY());
      }
    });
    appendEmptyRow();

    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(fullPanel, level);
    FontSizer.setFonts(popup, level);
    FontSizer.setFonts(eventDialog, level);
    FontSizer.setFonts(prelimDialog, level);
    FontSizer.setFonts(parametersDialog, level);
    FontSizer.setFonts(zenoDialog, level);
  }
  
  /**
   * Sets the algebra-based style for ODE rates
   */
  public void setAlgebraBasedEquations(boolean algebra) {
    isAlgebraBased = algebra;
    solverAndTolerancePanel.setVisible(!algebra);
    advancedButton.setVisible(!algebra);
    table.repaint(table.getBounds(null));
  }
  
  /**
   * Checks the syntax in the editors
   */
  public void checkSyntax () {
    checkIndependentVariable();
    checkValue(ejs,incrementField,false);
    checkDelaysSyntax();
  }
  
  /**
   * Checks if the delays are correctly defined 
   */
  private void checkDelaysSyntax() {
    delayListField.setBackground(Color.WHITE);
    String text = delayListField.getText().trim();
    if (text.length()<=0) {
      delayListField.setBackground(Color.WHITE);
      return;
    }
    String[] splitText = text.split(",");
    for (int i=0; i<splitText.length;i++) {
      if (!ejs.getModelEditor().getVariablesEditor().isVariableDefined(splitText[i],"double")) {
        Value value = ejs.getModelEditor().getVariablesEditor().checkExpression(splitText[i],"double");
        if (!(value instanceof DoubleValue)) {
          delayListField.setBackground(TabbedEditor.ERROR_COLOR);
          return;
        }
      }
    }
  }

  public void showDialogEvent() {
    eventDialog.setVisible(true);
  }
  
  public void showErrorTab() {
    parametersDialog.setVisible(true);
    parametersFullPanel.setSelectedComponent(errorEditor.getComponent());
    errorEditor.getComponent().requestFocus();
  }

  public void showDiscontinuityTab() {
    parametersDialog.setVisible(true);
    parametersFullPanel.setSelectedComponent(discontinuityEditor.getComponent());
    discontinuityEditor.getComponent().requestFocus();
  }

  protected StringBuffer getErrorHandlingCode() {
    return errorEditor.generateCode(Editor.GENERATE_PLAINCODE, getName());
  }
  
  private void showQSSPanel(boolean _show) {
    if (_show) {
      if (parametersFullPanel.indexOfComponent(matrixFinalPanel)<0) 
        parametersFullPanel.addTab(res.getString("EquationEditor.QSSInformation"),matrixFinalPanel);
    }
    else {
      int index = parametersFullPanel.indexOfComponent(matrixFinalPanel);
      if (index>=0) parametersFullPanel.remove(index);
    }
  }

  private static boolean needsTolerance(String _type) {
    if (_type.endsWith("+")) return true;
    if (_type.endsWith("*")) return true;
    return false;
  }

  public void setName (String _name) {
    name = _name;
    if (title.trim().length()>0) {
      titleBorder.setTitle (title+name);
      fullPanel.setBorder (titleBorder);
      fullPanel.repaint();
    }
    setCodeName (name);
    String plusName = " " + name;
    if (eventDialog!=null) eventDialog.setTitle(res.getString("EquationEditor.Events.EventsFor")+plusName);
    if (zenoDialog!=null) zenoDialog.setTitle(res.getString("EquationEditor.Events.ZenoEffectFor")+plusName);
    prelimDialog.setTitle(res.getString("EquationEditor.PreliminaryCodeFor")+plusName);
    prelimEditor.setName(res.getString("EquationEditor.PreliminaryCode")+plusName);
//    errorEditor.setName(res.getString("EquationEditor.ErrorCode")+plusName);
    delayInitialConditionEditor.setName(res.getString("EquationEditor.DelaysInitialConditionCode")+plusName);
    parametersDialog.setTitle(res.getString("EquationEditor.ParametersFor")+plusName);
    changed = true;
  }

  public String getName () { return name; }

  public void clear () {
    independentField.setText (" ");
    incrementField.setText ("0.05");
    commentField.setText("");
//    toleranceField.setText(DEF_TOL);
    eventField.setText("0");
    eventButton.setForeground(defaultForeground);
    tableModel.setNumRows (0);
    activeRow = -1;
    table.repaint(table.getBounds(null));
    
    
    if (eventDialog!=null) {
      eventDialog.dispose();
      eventDialog = null;
      eventEditor.clear();
      eventEditor = null;
    }
    if (zenoDialog!=null) {
      zenoDialog.dispose();
      zenoDialog = null;
      zenoEditor.clear();
      zenoEditor = null;
    }
//    zenoButton.setIcon(null);
    zenoButton.setForeground(defaultForeground);
    prelimDialog.setVisible(false);
    prelimEditor.clear();
    prelimEditor.getCommentField().setText(res.getString("EquationEditor.PrelimComment"));
    errorEditor.clear();
    discontinuityEditor.clear();
    prelimButton.setIcon(null);
    prelimButton.setForeground(defaultForeground);
    parametersDialog.setVisible(false);
    relativeToleranceField.setText("");
    absoluteToleranceField.setText(DEF_TOL);
    internalStepField.setText("");
    memoryLengthField.setText("");
    maximumStepField.setText("");
    maximumNumberOfStepsField.setText(DEF_NUM_STEPS);
    eventStepField.setText("");
    directMatrixArea.setText("");
    estimateFirstStepCombo.setSelectedIndex(0);
    accIndVelocityCombo.setSelectedIndex(0);
    forceSynchroCombo.setSelectedIndex(0);
    useBestInterpolationCombo.setSelectedIndex(0);
    // Delays
    delayListField.setText("");
    maxDelayField.setText("");
    delayAddDiscontField.setText("");
    delayInitialConditionEditor.clear();

    appendEmptyRow();
    checkAdvancedPanel();
  }

  public Component getComponent () { return fullPanel; }

  public void setColor (java.awt.Color _color) {
    myColor = _color;
    titleBorder.setTitleColor (_color);
    for (JComponent comp : componentSet) comp.setForeground(_color); 
//    prelimButton.setForeground(_color);
//    zenoButton.setForeground(_color);
    if (eventEditor!=null) eventEditor.setColor(_color);
    if (zenoEditor!=null) zenoEditor.setColor(_color);
    prelimEditor.setColor(_color);
    errorEditor.setColor(_color);
    discontinuityEditor.setColor(_color);
    delayInitialConditionEditor.setColor(_color);
  }

  public void setFont (Font _font) {
    myFont = _font;
    for (int i=0; i<2; i++) {
      TableCellEditor editor = table.getColumnModel().getColumn(i).getCellEditor();
      if (editor==null) ;
      else if (editor instanceof DefaultCellEditor) {
        DefaultCellEditor defEditor = (DefaultCellEditor) editor;
        if (defEditor.getComponent()!=null) defEditor.getComponent().setFont(_font);
      }
    }
    table.setFont(_font);
    table.setRowHeight(table.getFontMetrics(_font).getHeight());
    independentField.setFont (_font);
    incrementField.setFont (_font);
    absoluteToleranceField.setFont (_font);
    eventField.setFont(_font);
    eventStepField.setFont(_font);
    //commentField.setFont (_font);
    if (eventEditor!=null) eventEditor.setFont(_font);
    if (zenoEditor!=null) zenoEditor.setFont(_font);
    prelimEditor.setFont(_font);
    errorEditor.setFont(_font);
    discontinuityEditor.setFont(_font);
    delayInitialConditionEditor.setFont(_font);
    directMatrixArea.setFont(_font);

    TableCellRenderer renderer = table.getColumnModel().getColumn(0).getCellRenderer();
    ((CustomRenderer) renderer).equationRenderer.setFontsizes(_font.getSize(),_font.getSize(),_font.getSize(),_font.getSize());
    int cellHeight = (table.getFontMetrics(_font).getHeight()*8)/3;
//    System.out.println ("H = "+cellHeight);
    table.setRowHeight(cellHeight);
    table.repaint();
  }

  public boolean isChanged () {
    if (eventEditor!=null && eventEditor.isChanged()) return true;
    if (zenoEditor!=null && zenoEditor.isChanged()) return true;
    if (prelimEditor.isChanged()) return true;
    if (errorEditor.isChanged()) return true;
    if (discontinuityEditor.isChanged()) return true;
    if (delayInitialConditionEditor.isChanged()) return true;
    return changed;
  }

  public void setChanged (boolean _changed) {
    if (eventEditor!=null) eventEditor.setChanged(_changed);
    if (zenoEditor!=null) zenoEditor.setChanged(_changed);
    prelimEditor.setChanged(_changed);
    errorEditor.setChanged(_changed);
    discontinuityEditor.setChanged(_changed);
    delayInitialConditionEditor.setChanged(_changed);
    changed = _changed;
  }

  public void setActive (boolean _active) {
    boolean needsTolerance = needsTolerance(solverClassname[solverCombo.getSelectedIndex()]);
    activeEditor=_active;
    if (eventEditor!=null) eventEditor.setActive(_active);
    if (zenoEditor!=null) zenoEditor.setActive(_active);
    prelimEditor.setActive(_active);
    
    for (JComponent comp : activeComponentSet) {
      comp.setEnabled(_active);
      if (comp instanceof JTextField) ((JTextField) comp).setEditable(_active);
    }
    errorEditor.setActive(_active);
    discontinuityEditor.setActive(_active);
    delayInitialConditionEditor.setActive(_active);

    _active = _active && needsTolerance;
    for (JComponent comp : activeAndNeedToleranceComponentSet) {
      comp.setEnabled(_active);
      if (comp instanceof JTextComponent) ((JTextComponent) comp).setEditable(_active);
    }
     checkAdvancedPanel();
  }

  private boolean activeEditor=true;//FKH 021024
  public boolean isActive () { return activeEditor;}
  public boolean isInternal() {
    return advanced;
  }

  public void setInternal(boolean _advanced) {
    advanced = _advanced;
  }
  
 /**
  * Sets the name for code generation
  */
  public void setCodeName (String _name) { generateName = _name.trim().replace(' ','_'); }

  public StringBuffer generateCode (int _type, String _info) {
    if (_type==Editor.GENERATE_ENABLED_CONDITION) {
      StringBuffer code = new StringBuffer();
      code.append("  private boolean _isEnabled_" + generateName + " = "+isActive()+"; // Enabled condition for " + _info + "." + getName()+ "\n");
      if (eventEditor!=null) {
        Vector<Editor> eventList = eventEditor.getPages();
        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
          EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
          code.append("  private boolean _isEnabled_"+generateName+"_Event"+counter+" = "+anEvent.isActive()+"; // Enabled condition for event "+ anEvent.getName()+" \n");
        }
      }
      {
        Vector<Editor> discList = discontinuityEditor.getPages();
        for (int counter=1,n=discList.size(); counter<=n; counter++) {
          DiscontinuityEditor aDiscontinuity = (DiscontinuityEditor) discList.elementAt(counter-1);
          code.append("  private boolean _isEnabled_"+generateName+"_Discontinuity"+counter+" = "+aDiscontinuity.isActive()+"; // Enabled condition for discontinuity "+ aDiscontinuity.getName()+" \n");
        }
      }
//      if (errorEditor!=null) {
//        Vector<Editor> errorList = errorEditor.getPages();
//        for (int counter=1,n=errorList.size(); counter<=n; counter++) {
//          ErrorEditor anErrorPage = (ErrorEditor) errorList.elementAt(counter-1);
//          code.append("  private boolean _isEnabled_"+generateName+"_ErrorHandling"+counter+" = "+anErrorPage.isActive()+"; // Enabled condition for error "+ anErrorPage.getName()+" \n");
//        }
//      }
      return code;
    }

    if (_type==Editor.GENERATE_CHANGE_ENABLED_CONDITION) {
      StringBuffer code = new StringBuffer();
      code.append("    if (\""+getName()+"\".equals(_pageName)) { _pageFound = true; _isEnabled_" + generateName + " = _enabled; _automaticResetSolvers(); } // Change enabled condition for " + _info + "." + getName()+ "\n");
//      if (eventEditor!=null) code.append(eventEditor.generateCode(_type, _info));
      if (eventEditor!=null) {
        Vector<Editor> eventList = eventEditor.getPages();
        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
          EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
          code.append("    if (\""+anEvent.getName()+"\".equals(_pageName)) {"+
              " _pageFound = true; _isEnabled_"+generateName+"_Event"+counter+" = _enabled;"+
              " _ODEi_"+generateName+".initializeSolver(); "+
              "} // Change enabled condition for event "+ anEvent.getName()+" \n");
        }
      }
      {
        Vector<Editor> discList = discontinuityEditor.getPages();
        for (int counter=1,n=discList.size(); counter<=n; counter++) {
          DiscontinuityEditor aDiscontinuity = (DiscontinuityEditor) discList.elementAt(counter-1);
          code.append("    if (\""+aDiscontinuity.getName()+"\".equals(_pageName)) {"+
              " _pageFound = true; _isEnabled_"+generateName+"_Discontinuity"+counter+" = _enabled;"+
              " _ODEi_"+generateName+".initializeSolver(); "+
              "} // Change enabled condition for discontinuity "+ aDiscontinuity.getName()+" \n");
        }
      }
      return code;
    }

    if (_type==Editor.GENERATE_RESET_ENABLED_CONDITION) {
      StringBuffer code = new StringBuffer();
      code.append("    _isEnabled_" + generateName + " = "+isActive()+"; // Reset enabled condition for " + _info + "." + getName()+ "\n");
//      if (eventEditor!=null) code.append(eventEditor.generateCode(_type, _info));
      if (eventEditor!=null) {
        Vector<Editor> eventList = eventEditor.getPages();
        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
          EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
          code.append("    _isEnabled_"+generateName+"_Event"+counter+" = "+anEvent.isActive()+"; // reset enabled condition for event "+ anEvent.getName()+" \n");
        }
      }
      {
        Vector<Editor> discList = discontinuityEditor.getPages();
        for (int counter=1,n=discList.size(); counter<=n; counter++) {
          DiscontinuityEditor anEvent = (DiscontinuityEditor) discList.elementAt(counter-1);
          code.append("    _isEnabled_"+generateName+"_Discontinuity"+counter+" = "+anEvent.isActive()+"; // reset enabled condition for discontinuity "+ anEvent.getName()+" \n");
        }
      }
      return code;
    }

//    if (!isActive()) return new StringBuffer();

    String myInfo = res.getOptionalString(_info);
    if (myInfo==null) myInfo = _info;
    if (myInfo==null) myInfo = getName();
    else myInfo = myInfo + ":" + getName();

    String indVar = independentField.getText().trim();
    String inc = incrementField.getText().trim();
    if (indVar.length()<=0 || inc.length()<=0) return new StringBuffer();
    if (_type==Editor.GENERATE_DECLARATION) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("    if (_wasEnabled_" + generateName+") _ODEi_" + generateName + ".step();\n");
      buffer.append("    if (__shouldBreak) return;\n");
      return buffer;
    }
    if (_type==Editor.GENERATE_ENABLED_MEMORY) { 
      return new StringBuffer("    boolean _wasEnabled_" + generateName+" = _isEnabled_" + generateName + ";\n");
    }
    if (_type==Editor.GENERATE_SOURCECODE) {
      return new StringBuffer("    _ODEi_"+generateName+ " = new _ODE_"+generateName+"();\n");
    }
    if (_type==Editor.GENERATE_RESET_SOLVER) {
      return new StringBuffer("    _ODEi_" + generateName + ".resetSolver();\n");
    }
    if (_type==Editor.GENERATE_AUTOMATIC_RESET_SOLVER) {
      return new StringBuffer("    _ODEi_" + generateName + ".automaticResetSolver();\n");
    }
    if (_type==Editor.GENERATE_DESTRUCTION) {
      return new StringBuffer("    _ODEi_" + generateName + "=null;\n");
    }
    if (_type==Editor.GENERATE_CODE) {
      if (isActive()) parentTabbedEditor.checkIndependentVariable(independentField.getText().trim());
      return EquationCode.generateCode (ejs, this, generateName,myInfo,indVar);
    }
    return new StringBuffer();
  }

  public void addToInterpreter () {
    String indVar = independentField.getText().trim();
    String inc = incrementField.getText().trim();
    if (indVar.length()<=0 || inc.length()<=0) return;
    EquationCode.addToInterpreter(ejs,this,indVar);
  }

  Vector<?> getDataVector() { return tableModel.getDataVector(); }
  
  String getSolverType() {
    String solverType = solverClassname[solverCombo.getSelectedIndex()];
    if (needsTolerance(solverType)) return solverType.substring(0,solverType.length()-1); // Adaptive
    return solverType;
  }
  
  String getReadStepSize() { return incrementField.getText().trim(); }

  String getInternalStepSize() { return internalStepField.getText().trim(); }

  String getMemoryLength() { return memoryLengthField.getText().trim(); }

  String getMaximumStepSize() { return maximumStepField.getText().trim(); }

  String getMaximumNumberOfSteps() { return maximumNumberOfStepsField.getText().trim(); }

  String getAbsoluteToleranceStr() { return absoluteToleranceField.getText().trim(); }

  String getRelativeToleranceStr() {
    String relTol = relativeToleranceField.getText().trim();
    if (relTol.length()<=0) return getAbsoluteToleranceStr();
    return relTol;
  }

  boolean getEstimateFirstStep() {
    TwoStrings ts = (TwoStrings) this.estimateFirstStepCombo.getSelectedItem();
    return ts.getSecondString().endsWith("true");
  }

  boolean getAccelerationIndependentOfVelocity() { 
    TwoStrings ts = (TwoStrings) this.accIndVelocityCombo.getSelectedItem();
    return ts.getSecondString().endsWith("true");
  }
  
  boolean getForceSynchro() {
    TwoStrings ts = (TwoStrings) this.forceSynchroCombo.getSelectedItem();
    return ts.getSecondString().endsWith("true");
  }

  boolean getUseBestInterpolation() {
    TwoStrings ts = (TwoStrings) this.useBestInterpolationCombo.getSelectedItem();
    return ts.getSecondString().endsWith("true");
  }

  // Event steps
  String getEventMaximumStep() {
    return eventStepField.getText().trim();
  }

  // Delays

  String getDelays() { return delayListField.getText().trim(); }

  String getMaximumDelay() { return maxDelayField.getText().trim(); }
  
  String getDelayAddDiscont() { return delayAddDiscontField.getText().trim(); }

  String getDelayInitCond() { return delayInitialConditionEditor.getTextComponent().getText().trim(); }
  
  // QSS 
  
  String getIncidenceMatrix() { return this.directMatrixArea.getText().trim(); }
  
  private String guessIncidenceMatrix() {
    Vector<EquationVariable> variableList = new Vector<EquationVariable>();
    StringAndInteger  sai = EquationCode.getEquationVariables(ejs,this,independentField.getText().trim(),variableList);
    
    if (sai.getInteger()>0) {  // there are arrays
      String dimStr = sai.getString();
      StringBuffer txt = new StringBuffer();
      txt.append("int _dim = "+dimStr+";\n");
      txt.append("boolean[][] _boolMatrix = new boolean[_dim][_dim];\n\n");
      txt.append("int  _row=0,_col=0;\n");
      for (int i=0,num=variableList.size(); i<num; i++) { // Rate equations
        EquationVariable eqnVar = variableList.get(i);
        String indexStr=null;
        if (eqnVar.isArray()) { // find the index string, if any
          indexStr = OsejsCommon.getPiece(eqnVar.getStateString(),"[","]",false);
          if (indexStr==null || indexStr.length()<=0) indexStr = null;
        }
        txt.append("// Dependence of "+eqnVar.getName()+"\n");
        txt.append("_col=0;\n");
        String[] vars = ParserSuryono.getVariableList (eqnVar.getRateString());
//        System.out.println ("Expression = "+rate);
//        for (int k=0; k<vars.length; k++) System.out.println ("Var["+k+"] = "+vars[k]);
        for (int j=0; j<num; j++) {
          EquationVariable secondEqnVar = variableList.get(j);
          String secondVarName = secondEqnVar.getName();
          if (secondEqnVar.isArray()) {
            String secondIndexStr=null;
            if (indexStr!=null) secondIndexStr = secondVarName+"["+indexStr+"]";
            for (int k=0; k<vars.length; k++) if (vars[k].equals(secondIndexStr==null ? secondVarName : secondIndexStr)) {
              if (eqnVar.isArray()) {
                if (indexStr==null) {
                  txt.append("for (int _i=0; _i<"+eqnVar.getName()+".length; _i++)\n");
                  txt.append("  for (int _j=0; _j<"+secondVarName+".length; _j++) _boolMatrix[_row+_i][_col+_j] = true;\n");
                }
                else {
                  txt.append("for (int _i=0; _i<"+eqnVar.getName()+".length; _i++) _boolMatrix[_row+_i][_col+_i] = true;\n");
                }
              }
              else txt.append("  for (int _j=0; _j<"+secondVarName+".length; _j++) _boolMatrix[_row][_col+_j] = true;\n");
            }
            txt.append("_col+="+secondVarName+".length;\n");
          }
          else {
            for (int k=0; k<vars.length; k++) if (vars[k].equals(secondVarName)) {
              if (eqnVar.isArray()) txt.append("for (int _i=0; _i<"+eqnVar.getName()+".length; _i++)  _boolMatrix[_row+_i][_col] = true;\n");
              else txt.append("_boolMatrix[_row][_col] = true;\n");
            }
            if (j!=num-1) txt.append("_col++;\n");
          }
        }
        if (eqnVar.isArray()) txt.append("_row+="+eqnVar.getName()+".length;\n\n");
        else txt.append("_row++;\n\n");
      }
      txt.append("// The next method computes the matrix from the boolean array\n");
      txt.append("return org.opensourcephysics.numerics.qss.MultirateUtils.getIncidenceMatrix(_boolMatrix);\n");
      return txt.toString();
    }
    
    // There are NO arrays
    StringBuffer txt = new StringBuffer();
    txt.append("return new int[][] {\n");

    for (int i=0,num=variableList.size(); i<num; i++) { // Rate equations
      EquationVariable eqnVar = variableList.get(i);
      String[] vars = ParserSuryono.getVariableList (eqnVar.getRateString());
//      System.out.println ("Expression = "+rate);
//      for (int k=0; k<vars.length; k++) System.out.println ("Var["+k+"] = "+vars[k]);
      txt.append("  { ");
      boolean added = false;
      for (int j=0; j<num; j++) {
        EquationVariable secondEqnVar = variableList.get(j);
        String secondVarName = secondEqnVar.getName();
        for (int k=0; k<vars.length; k++) if (vars[k].equals(secondVarName)) {
          if (added) txt.append(", "+j);
          else { txt.append(""+j); added = true; }
        }
      }
      txt.append(" },\n");
    }
    txt.append("  { }\n");
    txt.append("};");
    return txt.toString();
  }
  
  /**
   * Creates a default initial condition for the current ODE to work as a delay differential equation
   * @return
   */
  private String delayGuessInitialConditions () {
    String valueComment = " // "+res.getString("EquationEditor.DelaysInitialConditionValue")+" ";
    Vector<EquationVariable> variableList = new Vector<EquationVariable>();
    StringAndInteger  sai = EquationCode.getEquationVariables(ejs,this,independentField.getText().trim(),variableList);
    boolean javaVersion = ejs.supportsJava();
    
    if (sai.getInteger()>0) {  // there are arrays
      String dimStr = sai.getString();
      StringBuffer txt = new StringBuffer();
      HashSet<String> indexSet = new HashSet<String>();
      if (javaVersion) {
        txt.append("int _dim = "+dimStr+";\n");
        txt.append("double[] _doubleArray = new double[_dim];\n\n");
        txt.append("int  _c=0;\n");
        txt.append("int  _i=0;\n");
      }
      else {
        txt.append("var _dim = "+dimStr+";\n");
        txt.append("var _doubleArray = [];\n\n");
        txt.append("var  _c=0;\n");
        txt.append("var  _i;\n");
        indexSet.add("_i");
      }
      for (int i=0,num=variableList.size(); i<num; i++) { // Rate equations
        EquationVariable eqnVar = variableList.get(i);
        String varName = variableList.get(i).getName();
        String varValue = ejs.getModelEditor().getVariablesEditor().getInitialValue(varName);
        String indexStr=null;
        if (eqnVar.isArray()) { // find the index string, if any
          indexStr = OsejsCommon.getPiece(eqnVar.getStateString(),"[","]",false);
          if (indexStr==null || indexStr.length()<=0) indexStr = null;
          if (indexStr==null) {
            txt.append(" for (_i=0; _i<"+varName+".length;_i++) _doubleArray[__c++] = "+varValue+";");
          }
          else {
            if (!indexSet.contains(indexStr)) {
              txt.append( javaVersion ? " int "+indexStr+";\n" : " var "+indexStr+";\n");
              indexSet.add(indexStr);
            }
            txt.append(" for ("+indexStr+"=0; "+indexStr+"<"+varName+".length; "+indexStr+"++) _doubleArray[__c++] = "+varValue+";");
          }
          txt.append(valueComment+ varName+"\n");
          txt.append("_row += "+varName+".length;\n");
        }
        else {
          txt.append("_doubleArray[_c++] = "+varValue+";");
          txt.append(valueComment + varName+"\n");
        }
      }
      txt.append("return _doubleArray;\n");
      return txt.toString();
    }
    // There are NO arrays
    if (variableList.size()>=2) {
      StringBuffer txt = new StringBuffer();
      if (javaVersion) txt.append("return new double[] {\n");
      else txt.append("return [\n");
      for (int i=0,num=variableList.size()-1; i<num;i++) {
        String varName = variableList.get(i).getName();
        String varValue = ejs.getModelEditor().getVariablesEditor().getInitialValue(varName);
        txt.append("  " + ((varValue!=null) ? varValue : "0.0"));
        if (i<num-1) txt.append(",");
        txt.append(valueComment+ varName+"\n");
      }
      if (javaVersion) txt.append("};\n");
      else txt.append("];\n");
      return txt.toString();
    }
    return null;
  }
  
  
  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();
    if (_info==null) _info = "";
    else _info=res.getString(_info);//FKH 021020
    String addInfo;
    addInfo = getName()+" ("+res.getString ("EquationEditor.IndependentVariable")+")";
    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,independentField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.Increment")+")";
    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,incrementField));
//    addInfo = getName()+" ("+res.getString ("EquationEditor.Tolerance")+")";
//    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,toleranceField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.PreliminaryCode")+")";
    for (SearchResult result : prelimEditor.search(null, _searchString, _mode))
      list.add(new PreliminaryCodeSearchResult(_info+"."+addInfo,result.getTextFound(),result.getLineNumber(),result.getCaretPosition()));
    
   
//    for (SearchResult result : errorEditor.search(null, _searchString, _mode))
//      list.add(new ParametersSearchResult(_info+"."+addInfo,result.getTextFound(),errorEditor.getTextComponent(), result.getLineNumber(),result.getCaretPosition()));
    if (errorEditor!=null) list.addAll(errorEditor.search(_info,_searchString,_mode));

    list.addAll(discontinuityEditor.search(_info,_searchString,_mode));

    addInfo = getName()+" ("+res.getString ("EquationEditor.InternalStep")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,internalStepField));

    addInfo = getName()+" ("+res.getString ("EquationEditor.MemoryLength")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,memoryLengthField));

    addInfo = getName()+" ("+res.getString ("EquationEditor.MaximumStep")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,maximumStepField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.MaximumNumberOfSteps")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,maximumNumberOfStepsField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.RelativeTolerance")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,relativeToleranceField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.AbsoluteTolerance")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,absoluteToleranceField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.EventMaximumStep")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,eventStepField));

    // Delays
    addInfo = getName()+" ("+res.getString ("EquationEditor.Delays")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,delayListField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.MaximumDelay")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,maxDelayField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.DelaysInitialConditionDiscontinuities")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,delayAddDiscontField));
    
    for (SearchResult result : delayInitialConditionEditor.search(null, _searchString, _mode))
      list.add(new ParametersSearchResult(_info+"."+addInfo,result.getTextFound(),delayInitialConditionEditor.getTextComponent(), result.getLineNumber(),result.getCaretPosition()));

    // QSS
    addInfo = getName()+" ("+res.getString ("EquationEditor.QSSDirectIncidenceMatrix")+")";
    list.addAll(searchInParameter(_info+"."+addInfo,_searchString,toLower,directMatrixArea));
    
    
    // to do : search in the table
    Vector<?> data = tableModel.getDataVector();
    addInfo = getName()+" ("+res.getString ("EquationEditor.State")+")";
    String addInfo2 = getName()+" ("+res.getString ("EquationEditor.Rate")+")";
    for (int i=0; i<data.size(); i++) {
      Vector<?> row = (Vector<?>) data.get(i);
      SearchResult sr = searchInString(_info+"."+addInfo, _searchString,toLower,row.get(0).toString(),i,0);
      if (sr!=null) list.add(sr);
      sr = searchInString(_info+"."+addInfo2,_searchString,toLower,row.get(1).toString(),i,1);
      if (sr!=null) list.add(sr);
    }
    if (eventEditor!=null) list.addAll(eventEditor.search(_info,_searchString,_mode));
    if (zenoEditor!=null) list.addAll(zenoEditor.search(this,_info,_searchString,_mode));
    return list;
  }

  private SearchResult searchInString(String _info, String _searchString, boolean toLower, String line, int row, int column)  {
    int index;
    if (toLower) index = line.toLowerCase().indexOf(_searchString);
    else index = line.indexOf(_searchString);
    if (index>=0) return new RowSearchResult(_info,line.trim(),row,column);
    return null;
  }

  private java.util.List<SearchResult> searchInComponent(String _info, String _searchString, boolean toLower, JTextComponent textComponent)  {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    int lineCounter=1,caretPosition=0;
    StringTokenizer t = new StringTokenizer(textComponent.getText(), "\n");
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(_searchString);
      else index = line.indexOf(_searchString);
      if (index>=0) list.add(new EquationSearchResult(_info,line.trim(),textComponent,lineCounter,caretPosition+index));
      caretPosition += line.length()+1;
      lineCounter++;
    }
    return list;
  }

  private java.util.List<SearchResult> searchInParameter(String _info, String _searchString, boolean toLower, JTextComponent textComponent)  {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    int lineCounter=1,caretPosition=0;
    String line = textComponent.getText();
    int index;
    if (toLower) index = line.toLowerCase().indexOf(_searchString);
    else index = line.indexOf(_searchString);
    if (index>=0) list.add(new ParametersSearchResult(_info,line.trim(),textComponent,lineCounter,caretPosition+index));
    return list;
  }

  public void fillSimulationXML(SimulationXML _simXML) {
    Element ode = _simXML.addModelEvolutionODE(getName(), commentField.getText());
    _simXML.setEnabled(ode, isActive());
    _simXML.setODEConfiguration(ode, ODE.INDEPENDENT_VARIABLE, independentField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.INCREMENT, incrementField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.PRELIMINARY_CODE, prelimEditor.getTextComponent().getText().trim());
    _simXML.setODEConfiguration(ode, ODE.PRELIMINARY_CODE_COMMENT, prelimEditor.getCommentField().getText().trim());
    
    Vector<?> data = tableModel.getDataVector();
    for (int i=0; i<data.size(); i++) {
      Vector<?> row = (Vector<?>) data.get(i);
      String rateString = row.get(1).toString().trim();
      if (rateString.length()<=0) continue; 
      if (rateString.endsWith(";")) rateString.substring(0, rateString.length()-1);
      String stateString = row.get(0).toString().trim();
      if (stateString.length()<=0) continue;
      int index = stateString.indexOf("["); 
      boolean isArray = true;
      if (index<0) { // If is has no [] or [i], it could still be an array.
        isArray = ejs.getModelEditor().getVariablesEditor().isDoubleArray(stateString); // ask the variables editor
        //        if (isArray) stateString = stateString+"[]"; // Allow for specifying arrays as x instead of x[]
      }
//      System.out.println("State "+stateString+" is Array = "+isArray);
      if (isArray) {
        if (index<0) {
          // Check if the rate is an array, too.
          // Changed to the line above, to be Java EJS compliant
          boolean isRateAnArray = ejs.getModelEditor().getVariablesEditor().isDoubleArray(rateString);
          if (isRateAnArray) _simXML.addODEEquation(ode,stateString,"__i", rateString+"[__i]"); // Standard index + add index to rate
          else _simXML.addODEEquation(ode,stateString,"__i", rateString); // Standard index
        }
        else { // get the index
          StringTokenizer tkn = new StringTokenizer(stateString,"[ ]");
          String theState = tkn.nextToken();
          if (!tkn.hasMoreTokens()) _simXML.addODEEquation(ode,theState,null, rateString); // No index
          else _simXML.addODEEquation(ode,theState,tkn.nextToken(), rateString); // f.i. x[i]
        }
      }
      else _simXML.addODEEquation(ode,stateString,null, rateString); // No index
    }

    _simXML.setODEConfiguration(ode, ODE.SOLVER_METHOD, getSolverType()); //solverClassname[solverCombo.getSelectedIndex()]);
    _simXML.setODEConfiguration(ode, ODE.ABSOLUTE_TOLERANCE, absoluteToleranceField.getText().trim());
    
    _simXML.setODEConfiguration(ode, ODE.ACCELERATION_INDEPENDENT_OF_VELOCITY, getAccelerationIndependentOfVelocity() ? "true" : "false");
    _simXML.setODEConfiguration(ode, ODE.FORCE_SYNCHRONIZATION, getForceSynchro() ? "true" : "false");
    _simXML.setODEConfiguration(ode, ODE.USE_BEST_INTERPOLATION, getUseBestInterpolation() ? "true" : "false");
    _simXML.setODEConfiguration(ode, ODE.ESTIMATE_FIRST_STEP, getEstimateFirstStep() ? "true" : "false");

    _simXML.setODEConfiguration(ode, ODE.HISTORY_LENGTH, memoryLengthField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.INTERNAL_STEP, internalStepField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.MAXIMUM_STEP, maximumStepField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.MAXIMUM_NUMBER_OF_STEPS, maximumNumberOfStepsField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.RELATIVE_TOLERANCE, relativeToleranceField.getText().trim());
    
    _simXML.setODEConfiguration(ode, ODE.DELAY_LIST, delayListField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.DELAY_MAXIMUM, maxDelayField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.DELAY_DISCONTINUITIES, delayAddDiscontField.getText().trim());
    _simXML.setODEConfiguration(ode, ODE.DELAY_INITIAL_CONDITION, delayInitialConditionEditor.getTextComponent().getText().trim());
    _simXML.setODEConfiguration(ode, ODE.DELAY_COMMENT, delayInitialConditionEditor.getCommentField().getText().trim());
    
    _simXML.setODEConfiguration(ode, ODE.MAXIMUM_EVENT_STEP, getEventMaximumStep());

    // events
    if (eventEditor!=null) eventEditor.fillSimulationXML(_simXML,ode);

    if (zenoEditor!=null) {
      _simXML.setODEConfiguration(ode, ODE.ZENO_ACTION, zenoEditor.getTextComponent().getText().trim());
      _simXML.setODEConfiguration(ode, ODE.ZENO_END_STEP, zenoEditor.isSelected() ? "true" : "false");
      _simXML.setODEConfiguration(ode, ODE.ZENO_COMMENT, zenoEditor.getCommentField().getText().trim());
    }
    
    // error handling
    errorEditor.fillSimulationXML(_simXML,ode);
    discontinuityEditor.fillSimulationXML(_simXML,ode);
    
  }
  
  public void fillSimulationXMLForHtmlView(SimulationXML _simXML) {
    if (!isActive()) return;
    Element ode = _simXML.addModelEvolutionODE(getName(), "");

    Vector<?> data = tableModel.getDataVector();
    for (int i=0; i<data.size(); i++) {
      Vector<?> row = (Vector<?>) data.get(i);
      String rateString = row.get(1).toString().trim();
      if (rateString.length()<=0) continue; 
      if (rateString.endsWith(";")) rateString.substring(0, rateString.length()-1);
      String stateString = row.get(0).toString().trim();
      if (stateString.length()<=0) continue;
      int index = stateString.indexOf("["); 
      boolean isArray = true;
      if (index<0) { // If is has no [] or [i], it could still be an array.
        isArray = ejs.getModelEditor().getVariablesEditor().isDoubleArray(stateString); // ask the variables editor
        //        if (isArray) stateString = stateString+"[]"; // Allow for specifying arrays as x instead of x[]
      }
//      System.out.println("State "+stateString+" is Array = "+isArray);
      if (isArray) {
        if (index<0) {
          _simXML.addODEEquation(ode,stateString,"__i", rateString+"[__i]"); // Standard index
          // Check if the rate is an array, too.
          // Changed to the line above, to be Java EJS compliant
//          if (ejs.getModelEditor().getVariablesEditor().isDoubleArray(rateString))  _simXML.addODEEquation(ode,stateString,"__i", rateString+"[__i]"); // Standard index + add index to rate
//          else _simXML.addODEEquation(ode,stateString,"__i", rateString+"[__i]"); // Standard index
        }
        else { // get the index
          StringTokenizer tkn = new StringTokenizer(stateString,"[ ]");
          String theState = tkn.nextToken();
          if (!tkn.hasMoreTokens()) _simXML.addODEEquation(ode,theState,null, rateString); // No index
          else _simXML.addODEEquation(ode,theState,tkn.nextToken(), rateString); // f.i. x[i]
        }
      }
      else _simXML.addODEEquation(ode,stateString,null, rateString); // No index
    }

  }
  
  public StringBuffer saveStringBuffer () {
    // ORDER IS VERY IMPORTANT HERE!!!!
    StringBuffer txt = new StringBuffer();
    // Editors included in this tag must be saved (and read) first because they contain their own "Comment" tag
    txt.append("<PreliminaryCode>\n"); 
    txt.append(prelimEditor.saveStringBuffer());
    txt.append("</PreliminaryCode>\n");
    txt.append("<ErrorHandling>\n"); // Same for error code
    txt.append(errorEditor.saveStringBuffer());
    txt.append("</ErrorHandling>\n");
    txt.append("<DelayInitialCondition>\n");
    txt.append(delayInitialConditionEditor.saveStringBuffer());
    txt.append("</DelayInitialCondition>\n");
    txt.append("<Discontinuities>\n"); // Same for error code
    txt.append(discontinuityEditor.saveStringBuffer());
    txt.append("</Discontinuities>\n");
    if (eventEditor!=null) {
      txt.append("<Events>\n");
      txt.append(eventEditor.saveStringBuffer());
      txt.append("</Events>\n");
    }
    if (zenoEditor!=null) {
      txt.append("<ZenoEffect>\n");
      txt.append(zenoEditor.saveStringBuffer());
      txt.append("</ZenoEffect>\n");
    }
    
    txt.append("<IndependentVariable>" + independentField.getText().trim()+"</IndependentVariable>\n");
    txt.append("<Increment>" + incrementField.getText().trim()+"</Increment>\n");
    Vector<?> data = tableModel.getDataVector();
    for (int i=0; i<data.size(); i++) {
      Vector<?> row = (Vector<?>) data.get(i);
//      txt += "<Rate state=\""+row.get(0)+"\">"+row.get(1)+"</Rate>\n";
      txt.append("<Rate state=\""+row.get(0).toString().trim()+"\">"+row.get(1)+"</Rate>\n");
    }
    txt.append("<Method>"+solverNames[solverCombo.getSelectedIndex()]+"</Method>\n");
    txt.append("<Tolerance>"+relativeToleranceField.getText()+"</Tolerance>\n");
    txt.append("<InternalStep>"+internalStepField.getText()+"</InternalStep>\n");
    txt.append("<MemoryLength>"+memoryLengthField.getText()+"</MemoryLength>\n");
    txt.append("<MaximumStep>"+maximumStepField.getText()+"</MaximumStep>\n");
    txt.append("<MaximumNumberOfSteps>"+maximumNumberOfStepsField.getText()+"</MaximumNumberOfSteps>\n");
    txt.append("<AbsoluteTolerance>"+absoluteToleranceField.getText()+"</AbsoluteTolerance>\n");
    txt.append("<RelativeTolerance>"+relativeToleranceField.getText()+"</RelativeTolerance>\n");
    txt.append("<EstimateFirstStep>"+getEstimateFirstStep()+"</EstimateFirstStep>\n");
    txt.append("<AccelerationIndependentOfVelocity>"+getAccelerationIndependentOfVelocity()+"</AccelerationIndependentOfVelocity>\n");
    txt.append("<ForceSynchronization>"+getForceSynchro()+"</ForceSynchronization>\n");
    txt.append("<UseBestInterpolation>"+getUseBestInterpolation()+"</UseBestInterpolation>\n");
    txt.append("<EventMaximumStep>"+getEventMaximumStep()+"</EventMaximumStep>\n");

    // Delays
    txt.append("<DelayList><![CDATA["+delayListField.getText()+"]]></DelayList>\n");
    txt.append("<DelayMaximum><![CDATA["+maxDelayField.getText()+"]]></DelayMaximum>\n");
    txt.append("<DelayAddDiscont><![CDATA["+delayAddDiscontField.getText()+"]]></DelayAddDiscont>\n");
    // QSS
    txt.append("<DirectIncidenceMatrix><![CDATA["+directMatrixArea.getText()+"]]></DirectIncidenceMatrix>\n");
    txt.append("<Comment><![CDATA["+commentField.getText()+"]]></Comment>\n");
    return txt;
  }

//  static private int[] indexesFor (String _input, String _tag) {
//    int beginIndex = _input.indexOf("<"+_tag+">");
//    if (beginIndex<0) return null;
//    int endIndex = _input.indexOf("</"+_tag+">");
//    if (endIndex<beginIndex) return null;
//    int l = _tag.length();
//    return new int[] {beginIndex+l,endIndex, endIndex+l};
//  }
//  int[] indexes = indexesFor(_input,"PreliminaryCode");
//  if (indexes!=null) {
//    prelimEditor.readString(_input.substring(indexes[0],indexes[1]));
//    _input = _input.substring(0,indexes[0]) + _input.substring(indexes[2]);
//  }
  
  public void readString (String _input) {
    // ORDER IS VERY IMPORTANT HERE!!!!
    // Read the embedded editors and remove them because they contain their own "Comment" tags
    String txt = OsejsCommon.getPiece(_input,"<PreliminaryCode>","</PreliminaryCode>",false);
    if (txt!=null) {
      prelimEditor.readString(txt);
      int index = _input.indexOf("</PreliminaryCode>");
      if (index>=0) _input = _input.substring(index+18);
    }
    // Same for error code
    txt = OsejsCommon.getPiece(_input,"<ErrorCode>","</ErrorCode>",false);
    if (txt!=null) { // backwards compatibility
      errorEditor.readBackwardsString(txt);
      int index = _input.indexOf("</ErrorCode>");
      if (index>=0) _input = _input.substring(index+12);
    }
    else { // Modern form
      txt = OsejsCommon.getPiece(_input,"<ErrorHandling>","</ErrorHandling>",false);
      if (txt!=null) {
        errorEditor.readString(txt);
        int index = _input.indexOf("</ErrorHandling>");
        if (index>=0) _input = _input.substring(index+16);
      }
    } 

    txt = OsejsCommon.getPiece(_input,"<DelayInitialCondition>","</DelayInitialCondition>",false);
    if (txt!=null) {
      delayInitialConditionEditor.readString(txt);
      int index = _input.indexOf("</DelayInitialCondition>");
      if (index>=0) _input = _input.substring(index+24);
    }
    // read discontinuities, if any
    txt = OsejsCommon.getPiece(_input,"<Discontinuities>","</Discontinuities>",false);
    if (txt!=null) {
      discontinuityEditor.readString(txt);
      int index = _input.indexOf("<Discontinuities>");
      _input = _input.substring(0,index)+_input.substring(index+txt.length()+35); // Or the fields in events can conflict with those of the solver (Tolerance, etc...)
    }
    
    // read events, if any
    txt = OsejsCommon.getPiece(_input,"<Events>","</Events>",false);
    if (txt!=null) {
      editEvents(false); // create the editor and window
      eventEditor.readString(txt);
      int index = _input.indexOf("<Events>");
      _input = _input.substring(0,index)+_input.substring(index+txt.length()+17); // Or the fields in events can conflict with those of the solver (Method, Tolerance, etc...)
    }
    // read the zeno effect code, if any
    txt = OsejsCommon.getPiece(_input,"<ZenoEffect>","</ZenoEffect>",false);
    if (txt!=null) {
      editZenoEffect(false); // create the editor and window
      zenoEditor.readString(txt);
    }
    
    // Now, read the rest
    txt = OsejsCommon.getPiece(_input,"<IndependentVariable>","</IndependentVariable>",false);
    independentField.setText(txt);
    txt = OsejsCommon.getPiece(_input,"<Increment>","</Increment>",false);
    incrementField.setText(txt);

    txt = OsejsCommon.getPiece(_input,"<Comment><![CDATA[","]]></Comment>",false);
    commentField.setText(txt);
    txt = OsejsCommon.getPiece(_input,"<Method>","</Method>",false);
    for (int i=0; i<solverNames.length; i++) {
      if (solverNames[i].equals(txt)) {
        solverCombo.setSelectedIndex(i);
        solverCombo.setToolTipText(res.getString("EquationEditor."+solverNames[i]));
        break;
      }
    }

    txt = OsejsCommon.getPiece(_input,"<AbsoluteTolerance>","</AbsoluteTolerance>",false);
    if (txt==null) { // Backwards compatibility
//    toleranceField.setText(txt);
      txt = OsejsCommon.getPiece(_input,"<Tolerance>","</Tolerance>",false);
      absoluteToleranceField.setText(txt);
      relativeToleranceField.setText("");
      internalStepField.setText("");
      memoryLengthField.setText("");
           
      maximumStepField.setText("");
      maximumNumberOfStepsField.setText(DEF_NUM_STEPS);
      eventStepField.setText("");
      estimateFirstStepCombo.setSelectedIndex(0);
      accIndVelocityCombo.setSelectedIndex(0);
      forceSynchroCombo.setSelectedIndex(0);
      useBestInterpolationCombo.setSelectedIndex(0);
      // Delays
      delayListField.setText("");
      maxDelayField.setText("");
      delayAddDiscontField.setText("");
    }
    else { // New solver parameters
      absoluteToleranceField.setText(txt);
      txt = OsejsCommon.getPiece(_input,"<RelativeTolerance>","</RelativeTolerance>",false);
      relativeToleranceField.setText(txt);

      txt = OsejsCommon.getPiece(_input,"<InternalStep>","</InternalStep>",false);
      internalStepField.setText(txt);

      txt = OsejsCommon.getPiece(_input,"<MemoryLength>","</MemoryLength>",false);
      if (txt!=null) memoryLengthField.setText(txt);
  
      txt = OsejsCommon.getPiece(_input,"<MaximumStep>","</MaximumStep>",false);
      if (txt!=null) maximumStepField.setText(txt); // This parameter was introduced later

      txt = OsejsCommon.getPiece(_input,"<MaximumNumberOfSteps>","</MaximumNumberOfSteps>",false);
      if (txt!=null) maximumNumberOfStepsField.setText(txt); // This parameter was introduced later

      txt = OsejsCommon.getPiece(_input,"<EventMaximumStep>","</EventMaximumStep>",false);
      eventStepField.setText(txt);
      
      txt = OsejsCommon.getPiece(_input,"<EstimateFirstStep>","</EstimateFirstStep>",false);
      for (int i=0,n=estimateFirstStepCombo.getItemCount(); i<n; i++) {
        TwoStrings ts = (TwoStrings) estimateFirstStepCombo.getItemAt(i);
        if (txt.equals(ts.getSecondString())) { estimateFirstStepCombo.setSelectedIndex(i); break; }
      }
          
      txt = OsejsCommon.getPiece(_input,"<AccelerationIndependentOfVelocity>","</AccelerationIndependentOfVelocity>",false);
      if (txt!=null) for (int i=0,n=accIndVelocityCombo.getItemCount(); i<n; i++) {
        TwoStrings ts = (TwoStrings) accIndVelocityCombo.getItemAt(i);
        if (txt.equals(ts.getSecondString())) { accIndVelocityCombo.setSelectedIndex(i); break; }
      }
      
      txt = OsejsCommon.getPiece(_input,"<ForceSynchronization>","</ForceSynchronization>",false);
      for (int i=0,n=forceSynchroCombo.getItemCount(); i<n; i++) {
        TwoStrings ts = (TwoStrings) forceSynchroCombo.getItemAt(i);
        if (txt.equals(ts.getSecondString())) { forceSynchroCombo.setSelectedIndex(i); break; }
      }

      txt = OsejsCommon.getPiece(_input,"<UseBestInterpolation>","</UseBestInterpolation>",false);
      if (txt!=null) for (int i=0,n=useBestInterpolationCombo.getItemCount(); i<n; i++) { // This parameter was introduced later
        TwoStrings ts = (TwoStrings) useBestInterpolationCombo.getItemAt(i);
        if (txt.equals(ts.getSecondString())) { useBestInterpolationCombo.setSelectedIndex(i); break; }
      }
      
      txt = OsejsCommon.getPiece(_input,"<DelayList><![CDATA[","]]></DelayList>",false);
      delayListField.setText(txt);
      txt = OsejsCommon.getPiece(_input,"<DelayMaximum><![CDATA[","]]></DelayMaximum>",false);
      if (txt!=null) maxDelayField.setText(txt);
      txt = OsejsCommon.getPiece(_input,"<DelayAddDiscont><![CDATA[","]]></DelayAddDiscont>",false);
      delayAddDiscontField.setText(txt);
      
      // QSS
      txt = OsejsCommon.getPiece(_input,"<DirectIncidenceMatrix><![CDATA[","]]></DirectIncidenceMatrix>",false);
      directMatrixArea.setText(txt);
    }

    tableModel.setNumRows (0);
//    int counter=0;
    String line = OsejsCommon.getPiece(_input,"<Rate state=","</Rate>",true);
    while (line!=null) {
      String state = OsejsCommon.getPiece(line,"<Rate state=\"","\">",false);
      String rate  = OsejsCommon.getPiece(line,"\">","</Rate>",false);
      Vector<String> row = new Vector<String>();
      row.add (state);
      row.add (rate);
      tableModel.addRow(row);
      _input = _input.substring(_input.indexOf("</Rate>")+7);
      line = OsejsCommon.getPiece(_input,"<Rate state=","</Rate>",true);
    }
    activeRow = -1;
    correctIndependentVariable(-1);
    checkAdvancedPanel();
//    table.repaint(table.getBounds(null));
//    reading = false;
  }

  // ---------- This ends the implementation of the abstract superclass

  static public void checkValue(Osejs _ejs,Component _field,boolean canBeEmpty) {
    if (! (_field instanceof JTextComponent)) {
      System.err.println ("Component "+_field+" is not a text component");
      return;
    }
    JTextComponent textComponent = (JTextComponent) _field;
    String variable = textComponent.getText().trim();
    if (canBeEmpty && variable.length()<=0) { // This one can be empty
      textComponent.setBackground(Color.WHITE);
      return;
    }
    if (_ejs.getModelEditor().getVariablesEditor().isVariableDefined(variable,"double")) textComponent.setBackground(Color.WHITE);
    else {
      Value value = _ejs.getModelEditor().getVariablesEditor().checkExpression(variable,"double");
      if (value instanceof DoubleValue) textComponent.setBackground(Color.WHITE);
      else textComponent.setBackground(TabbedEditor.ERROR_COLOR);
    }
  }
  
  /**
   * Checks if there is any non-default value in the advanced panel and sets the foreground of the advanced button accordingly
   */
  public void checkAdvancedPanel() {
    if (hasDefaultAdvancedValues()) {
      if (advancedButton.getIcon()!=ADVANCED_ICON) advancedButton.setIcon(ADVANCED_ICON);
//      if (advancedButton.getForeground()!=defaultForeground) advancedButton.setForeground(defaultForeground);
    }
    else {
      if (advancedButton.getIcon()!=ADVANCED_RED_ICON) advancedButton.setIcon(ADVANCED_RED_ICON);
//      if (advancedButton.getForeground()==defaultForeground) advancedButton.setForeground(myColor);
    }
  }
   
  private boolean hasDefaultAdvancedValues() {
    if (accIndVelocityCombo.getSelectedIndex()!=0) return false;
    if (forceSynchroCombo.getSelectedIndex()!=0) return false;
    if (useBestInterpolationCombo.getSelectedIndex()!=0) return false;
    if (estimateFirstStepCombo.getSelectedIndex()!=0) return false;
    if (internalStepField.getText().trim().length()>0) return false;
    if (memoryLengthField.getText().trim().length()>0) return false;
    if (maximumStepField.getText().trim().length()>0) return false;
    String noSteps = getMaximumNumberOfSteps();
    if (noSteps.length()>0 && !DEF_NUM_STEPS.equals(noSteps)) return false;
    if (relativeToleranceField.getText().trim().length()>0) return false;
    if (!errorEditor.getPages().isEmpty()) return false;
    if (!discontinuityEditor.getPages().isEmpty()) return false;
    // Delays
    if (delayListField.getText().trim().length()>0) return false;
    if (maxDelayField.getText().trim().length()>0) return false;
    if (delayAddDiscontField.getText().trim().length()>0) return false;
    if (delayInitialConditionEditor.getTextComponent().getText().trim().length()>0) return false;
    // QSS
    if (directMatrixArea.getText().trim().length()>0) return false;
    return true;
  }
  
  private void checkIndependentVariable() {
    String variable = independentField.getText().trim();
    if (variable.length()<=0 ||  ejs.getModelEditor().getVariablesEditor().isVariableDefined(variable,"double")) 
      independentField.setBackground(Color.WHITE);
    else independentField.setBackground(TabbedEditor.ERROR_COLOR);
  }

  private void correctIndependentVariable (int row) {
    table.repaint(table.getBounds(null));
    checkIndependentVariable();
  }

  static public void selectVariableFor (Osejs _ejs, JTextComponent _textComp, String _default, String _type, boolean _canBeEmpty) {
    String option = EditorForVariables.edit (_ejs.getModelEditor(), "Variables", _type, _textComp,_textComp.getText().trim(),_default);
    if (option!=null) {
      int index = option.indexOf(':');
      if (index>0) option = option.substring(0,index);
      _textComp.setText(option);
      checkValue(_ejs,_textComp,_canBeEmpty);
    }
  }
  
  private void myActionPerformed (ActionEvent evt) {
    String aCmd=evt.getActionCommand();
    if (aCmd.equals("equationAdd") || aCmd.equals("equationCreate")) addEmptyRow(false);
    else if (aCmd.equals("equationInsert")) addEmptyRow(true);
    else if (aCmd.equals("equationRemove")) removeRow();
    else if (aCmd.equals("independentField")) correctIndependentVariable (-1);
    else if (aCmd.equals("selectVariable")) {
      String option = EditorForVariables.edit (ejs.getModelEditor(), "Variables", "double", independentField, independentField.getText().trim(),"");
      if (option!=null) {
        independentField.setText(option);
        correctIndependentVariable (-1);
      }
    }
    else if (aCmd.equals("selectIncrement")) selectVariableFor(ejs,incrementField,"0.1","double",false); 
    else if (aCmd.equals("selectInternalStep")) selectVariableFor(ejs,internalStepField,"","double",true);
    else if (aCmd.equals("selectMemoryLength")) selectVariableFor(ejs,memoryLengthField,"","int|double",true);
    else if (aCmd.equals("selectMaximumStep")) selectVariableFor(ejs,maximumStepField,"","double",true);
    else if (aCmd.equals("selectMaximumNumberOfSteps")) selectVariableFor(ejs,maximumNumberOfStepsField,"","int",true);
    else if (aCmd.equals("selectAbsoluteTolerance")) selectVariableFor(ejs,absoluteToleranceField,DEF_TOL,"double",false);
    else if (aCmd.equals("selectRelativeTolerance")) selectVariableFor(ejs,relativeToleranceField,"","double",true);
    else if (aCmd.equals("selectEventStep")) selectVariableFor(ejs,eventStepField,DEF_TOL,"double",true);
    else if (aCmd.equals("selectState")) {
      String option;
      int field = 0;
      if (table.isColumnSelected(1)) {
        field = 1;
        option = EditorForVariables.edit (ejs.getModelEditor(), "VariablesAndCustomMethods", "double|double[]", 
            table, (String) table.getValueAt(activeRow,field),null);
      }
      else option = EditorForVariables.edit (ejs.getModelEditor(), "Variables", "double|double[]", table, (String) table.getValueAt(activeRow,field),"");
      if (option!=null) {
        table.setValueAt(EditorForVariables.finalDimensionedValue(),activeRow,field);
        if (field==0) correctIndependentVariable (activeRow);
      }
    }
    else if (aCmd.equals("editEvents")) { editEvents(true); }
  }

  private void editEvents (boolean show) {
    if (eventDialog==null) {  // The first time

      eventEditor = new TabbedEventEditor (ejs,this);
      eventEditor.setPageCounterField(eventField,myColor, eventButton);
      eventEditor.setName("Event");
      eventEditor.setContentDelim ("EventContent");
      eventEditor.setColor(myColor);
      eventEditor.setFont (myFont);
      eventEditor.setActive(prelimEditor.isActive());
      
      eventDialog = new JFrame (); //(Frame)owner);
      java.awt.Image image = org.opensourcephysics.tools.ResourceLoader.getImage("data/icons/EjsIcon.gif");
      if (image!=null) eventDialog.setIconImage(image);
      eventDialog.setTitle(res.getString("EquationEditor.Events.EventsFor")+" "+this.name);
      eventDialog.getContentPane().setLayout (new BorderLayout());
      eventDialog.getContentPane().add(eventTopPanel, BorderLayout.SOUTH);
      eventDialog.getContentPane().add(eventEditor.getComponent(), BorderLayout.CENTER);
//      eventDialog.getContentPane().addComponentListener (new ComponentAdapter() {
//        public void componentResized(ComponentEvent evt) {
//          eventEditor.adjust();
//        }
//      });
      eventDialog.validate();
      eventDialog.setSize (res.getDimension("EquationEditor.Events.DialogSize"));
      eventDialog.setLocationRelativeTo (this.getComponent());
      ejs.setMenuBar(eventDialog);
    }
    if (show) {
      eventDialog.setVisible(true);
    }
    else {
      eventDialog.setVisible(true);
      eventDialog.setVisible(false);
    }
  }

  public void editZenoEffect (boolean show) {
    if (zenoDialog==null) {  // The first time
      zenoEditor = new ZenoEditor (ejs);
      zenoEditor.setName("ZenoEffect");
      zenoEditor.setColor(myColor);
      zenoEditor.setFont (myFont);
      zenoEditor.setActive(prelimEditor.isActive());

      zenoEditor.addDocumentListener(new DocumentListener(){
        public void changedUpdate (DocumentEvent evt)  { checkContents(); }
        public void insertUpdate  (DocumentEvent evt)  { checkContents(); }
        public void removeUpdate  (DocumentEvent evt)  { checkContents(); }
        private void checkContents() {
          String text = zenoEditor.getTextComponent().getText().trim();
          if (text.length()>0) {
//            if (zenoButton.getIcon()==null) zenoButton.setIcon(NON_EMPTY_ICON);
            if (zenoButton.getForeground()==defaultForeground) zenoButton.setForeground(myColor);

          }
          else {
            if (zenoButton.getForeground()!=defaultForeground) zenoButton.setForeground(defaultForeground);
//            if (zenoButton.getIcon()!=null) zenoButton.setIcon(null);
          }
        }
      });

      zenoDialog = new JFrame (res.getString("EquationEditor.Events.ZenoEffectFor")+" "+this.name);
//      zenoDialog.setModal(false);
      java.awt.Image image = org.opensourcephysics.tools.ResourceLoader.getImage("data/icons/EjsIcon.gif");
      if (image!=null) zenoDialog.setIconImage(image);
//      zenoDialog.setTitle(res.getString("EquationEditor.Events.ZenoEffectFor")+" "+this.name);
      zenoDialog.getContentPane().setLayout (new BorderLayout());
      zenoDialog.getContentPane().add(zenoEditor.getComponent(), BorderLayout.CENTER);
      zenoDialog.validate();
      Dimension size = res.getDimension("EquationEditor.Events.DialogSize");
      zenoDialog.setSize ((size.width*3)/4,(size.height*3)/4);
      zenoDialog.setLocationRelativeTo (eventDialog); //this.getComponent());
      ejs.setMenuBar(zenoDialog);
    }
    if (show) {
      zenoDialog.setVisible(true);
    }
    else {
      zenoDialog.setVisible(true);
      zenoDialog.setVisible(false);
    }
  }

  private  void addEmptyRow (boolean before) {
    Vector<String> newRow = new Vector<String>(emptyRow);
    if (before) {
      if (activeRow<0) tableModel.insertRow (0,newRow);
      else tableModel.insertRow (activeRow,newRow);
    }
    else {
      if (activeRow<0) tableModel.addRow (newRow);
      else tableModel.insertRow (activeRow+1,newRow);
    }
    changed = true;
  }

  private  void appendEmptyRow () {
    Vector<String> newRow = new Vector<String>(emptyRow);
    tableModel.addRow (newRow);
    changed = true;
  }

  private void removeRow () {
    if (activeRow==-1) return;
    tableModel.removeRow (activeRow);
    activeRow = -1;
    changed = true;
  }

  class MyDefaultTableModel extends DefaultTableModel {
    public MyDefaultTableModel (Vector<String> colNames, int columns) { super (colNames, columns); }
    public Class<String> getColumnClass (int c) { return String.class;   }
    public void setValueAt(Object value, int row, int col) {
      if (col>0 && row==(tableModel.getRowCount()-1)) appendEmptyRow();
//      if (col==0) correctRow (row, independentField.getText().trim());
      super.setValueAt (value,row,col);
//      if (col==0) { I want to modify the way the table traverses when hitting return, But it doesn't work
//        table.setEditingRow(row);
//        table.setEditingColumn(0);
//      }
//      else if (col==1) {
//        table.setEditingRow(row+1);
//        table.setEditingColumn(0);
//      }
    }

    public boolean isCellEditable(int row, int col) {
//      if (col==0) return false;
      return true;
    }

  } // end of class MyDefaultTableModel

  class MLSL implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent lse) {
      ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
      if (lsm.getMinSelectionIndex()>tableModel.getRowCount()) activeRow = -1;
      else activeRow = lsm.getMinSelectionIndex();
    }
  } // end of class MLSL

  // This is to get WYSIWYG
  private class StringDocumentListener implements DocumentListener {
    private int column;
    StringDocumentListener (int _column) { column = _column; }
    public void changedUpdate(DocumentEvent e) { reflectChange(e); }
    public void insertUpdate(DocumentEvent e)  { reflectChange(e); }
    public void removeUpdate(DocumentEvent e)  { reflectChange(e); }
    private void reflectChange(DocumentEvent e) {
      changed = true;
      try {
        String txt = e.getDocument().getText(0,e.getDocument().getLength());
//        System.out.println("Pos "+activeRow+","+column+" : Update to "+txt);
        tableModel.setValueAt (txt,activeRow,column);
      } catch (Exception exc) { exc.printStackTrace(); }
    }
  }

  private class MyKeyListener implements java.awt.event.KeyListener {
    public void keyPressed  (java.awt.event.KeyEvent _e) { processKeyEvent (_e,0); }
    public void keyReleased (java.awt.event.KeyEvent _e) { processKeyEvent (_e,1); }
    public void keyTyped    (java.awt.event.KeyEvent _e) { processKeyEvent (_e,2); }
    private void processKeyEvent (java.awt.event.KeyEvent _e, int _n) {
      correctIndependentVariable(-1);
    }
  }
 
  static private String processTeXString(String _input) {
    int index = _input.indexOf('_');
    while (index>=0) {
      _input = _input.substring(0,index) + "-" +_input.substring(index+1);
      index = _input.indexOf('_');
    }
    return _input;
  }

  private class CustomRenderer implements TableCellRenderer {
    protected cHotEqn equationRenderer;

    public CustomRenderer() {
      equationRenderer = new cHotEqn();
      equationRenderer.setDebug(false);
      equationRenderer.setEditable(false);
    }

    public Component getTableCellRendererComponent(JTable _table, Object value,
                     boolean isSelected,boolean hasFocus, int row,int column) {
      if (isSelected) {
         equationRenderer.setForeground(_table.getSelectionForeground());
         equationRenderer.setBackground(_table.getSelectionBackground());
      } else {
         equationRenderer.setForeground(_table.getForeground());
         equationRenderer.setBackground(_table.getBackground());
      }
      if (value==null)  equationRenderer.setEquation("");
      else {
//        if (ejs.getSimInfoEditor().isDeltaEquation()) 
        if (isAlgebraBased)  equationRenderer.setEquation("\\frac{\\Delta \\; "+processTeXString(value.toString())+"}"
            + "{\\Delta \\; "+processTeXString(independentField.getText().trim())+"} \\; = ");
        else equationRenderer.setEquation("\\frac{d \\; "+processTeXString(value.toString())+"}"
            + "{d \\; "+processTeXString(independentField.getText().trim())+"} \\; = ");
      }
      return equationRenderer;
    }
  }

  class EquationSearchResult extends SearchResult {

    public EquationSearchResult (String anInformation, String aText, JTextComponent textComponent, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,textComponent,aLineNumber,aCaretPosition);
    }

    public String toString () {
      return information+": "+textFound;
    }

    public void show () {
      parentTabbedEditor.showPage(EquationEditor.this);
      super.show();
    }
  }

  class PreliminaryCodeSearchResult extends SearchResult {

    public PreliminaryCodeSearchResult (String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,prelimEditor.getTextComponent(),aLineNumber,aCaretPosition);
    }

    public String toString () {
      return information+": "+textFound;
    }

    public void show () {
      prelimDialog.setVisible(true);
      super.show();
    }
  }
  
  class ParametersSearchResult extends SearchResult {

    public ParametersSearchResult (String anInformation, String aText, JTextComponent textComponent, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,textComponent,aLineNumber,aCaretPosition);
    }

    public String toString () {
      return information+": "+textFound;
    }

    public void show () {
      parametersDialog.setVisible(true);
      super.show();
    }
  }
  class RowSearchResult extends SearchResult {
    // lineNumber is the row number
    // caretPosition is the column
    public RowSearchResult (String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,null,aLineNumber,aCaretPosition);
    }

    public void show () {
      parentTabbedEditor.showPage(EquationEditor.this);
      table.requestFocusInWindow();
      table.setRowSelectionInterval(lineNumber,lineNumber);
      table.setColumnSelectionInterval(caretPosition,caretPosition);
    }
  }

} // end of class CodeTableDisplay
