/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import org.colos.ejs.osejs.edition.ode_editor.EquationEditor;
import org.colos.ejs.osejs.edition.variables.ElementsEditor;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.library.Animation;
import org.colos.ejs.library.control.value.DoubleValue;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.MODEL;
import org.opensourcephysics.tools.FontSizer;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

//--------------------

public class ModelEditor implements Editor {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private final ResourceUtil sysRes = new ResourceUtil("SystemResources");
  static private String[] keywords = {"Variables", "Initialization", "Evolution", "Constraints", "Library", "Elements"};
  static private final int DEFAULT_FPS = 20;
  static private String name="";
  static private Insets nullInsets = new Insets(0,0,0,0);

  private Editor[] editors;
  private final TabbedEditor initializationEditor, constraintsEditor;
  private final TabbedEvolutionEditor evolutionEditor;
  private final ElementsEditor elementsEditor;
  private final VariablesEditor variablesEditor;
  private final TabbedLibraryEditor libraryEditor;
  private CardLayout cardLayout = new CardLayout (); //, fpsCardLayout = new CardLayout();
  private JPanel panel = new JPanel (cardLayout); //, fpsPanel = new JPanel(fpsCardLayout);
  private JPanel mainPanel = new JPanel (new BorderLayout());
  private JRadioButton buttons[];
  private Osejs ejs = null;

//  private JComboBox<TwoStrings> runningModeCombo;
  private JCheckBox startCheckBox;
  private JSlider fpsSlider;
  private JTextField fpsField,spdField,realtimeField;
  private boolean changed = false , isMerging = false, visible=true;
  private String[] mPredefinedActions;

  public ModelEditor (org.colos.ejs.osejs.Osejs _ejs) {
    ejs = _ejs;
    Color color = InterfaceUtils.color(res.getString("Model.Color"));
    Font font   = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));

    // ----------------------------------
    // The execution control panel for the 'Evolution' panel

    JLabel topLabel = new JLabel (res.getString ("Model.Evolution.TopLabel"),SwingConstants.CENTER);
    topLabel.setForeground (color);
    topLabel.setFont(font.deriveFont(Font.BOLD));
    topLabel.setHorizontalAlignment (SwingConstants.CENTER);
    topLabel.setBorder(new EmptyBorder(4,0,4,0));

    JLabel fpsLabel = new JLabel (res.getString ("Model.Evolution.FpsLabel"),SwingConstants.LEFT);
    fpsLabel.setForeground (color);
    fpsLabel.setFont(font);
    fpsLabel.setToolTipText(res.getString ("Model.Evolution.FpsTooltip"));
    fpsLabel.setHorizontalAlignment (SwingConstants.CENTER);
//    fpsLabel.setBorder(new EmptyBorder(0,0,0,2));

    fpsField = new JTextField (4);
    fpsField.setFont(fpsField.getFont().deriveFont(Font.BOLD));
    fpsField.setEditable (true);
    fpsField.setHorizontalAlignment (SwingConstants.RIGHT);
    fpsField.setMargin (nullInsets);
    fpsField.setText(""+DEFAULT_FPS);
    fpsField.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent _e) {
        changed = true;
        try {
          int value = Integer.parseInt(fpsField.getText());
          if (value >= Animation.MAXIMUM_FPS) {
            fpsField.setText(res.getString("Model.Evolution.MaximumFPS"));
            fpsSlider.setValue(Animation.MAXIMUM_FPS);
          }
          else if (value <= Animation.MINIMUM_FPS) {
            fpsField.setText(res.getString("Model.Evolution.MinimumFPS"));
            fpsSlider.setValue(Animation.MINIMUM_FPS);
          }
          else fpsSlider.setValue(value);
        }
        catch (Exception exc) {
          fpsField.setText(""+DEFAULT_FPS);
          fpsSlider.setValue(DEFAULT_FPS);
        }
      }
     });

    fpsSlider = new JSlider(SwingConstants.VERTICAL,Animation.MINIMUM_FPS,Animation.MAXIMUM_FPS,DEFAULT_FPS);
    fpsSlider.setInverted (false);
    fpsSlider.setMinorTickSpacing (1);
    fpsSlider.setSnapToTicks(true);
    fpsSlider.setPaintTicks (true);
    fpsSlider.setPaintLabels (true);
    fpsSlider.setBorder(new EmptyBorder(2,5,5,0));
    fpsSlider.setFont(font);
    java.util.Hashtable<Integer,JComponent> table = new java.util.Hashtable<Integer,JComponent> ();
    table.put (new Integer(Animation.MINIMUM_FPS),new JLabel(res.getString("Model.Evolution.MinimumFPS")));
    for (int i=5; i<(Animation.MAXIMUM_FPS-1); i+=5) table.put (new Integer(i),new JLabel(""+i));
    table.put (new Integer(Animation.MAXIMUM_FPS),new JLabel(res.getString("Model.Evolution.MaximumFPS")));
    fpsSlider.setLabelTable (table);

    fpsSlider.addChangeListener (new ChangeListener() {
      public void stateChanged(ChangeEvent _e) {
        changed = true;
        int value = fpsSlider.getValue();
        if      (value==Animation.MAXIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MaximumFPS"));
        else if (value==Animation.MINIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MinimumFPS"));
        else fpsField.setText (""+value);
      }
     });

//     JLabel spdLabel = new JLabel (res.getString ("Model.Evolution.SpdLabel"),SwingConstants.LEFT);
     JButton spdLabel = new JButton (res.getString ("Model.Evolution.SpdLabel"));
     spdLabel.setForeground (color);
     spdLabel.setFont(font);
     spdLabel.setToolTipText(res.getString ("Model.Evolution.SpdTooltip"));
     spdLabel.setBorder(new EmptyBorder(0,0,0,2));
     spdLabel.setHorizontalAlignment (SwingConstants.CENTER);
     spdLabel.addActionListener (new ActionListener() {
       public void actionPerformed(ActionEvent _e) {
         String option = EditorForVariables.edit (ejs.getModelEditor(), "Variables", "int", spdField, spdField.getText().trim(),"");
         if (option!=null) {
           spdField.setText(option);
           changed = true;
           checkSPD();
         }
       }
     });

     spdField = new JTextField (4);
     spdField.setFont(spdField.getFont().deriveFont(Font.BOLD));
     spdField.setEditable (true);
     spdField.setHorizontalAlignment (SwingConstants.RIGHT);
     spdField.setMargin (nullInsets);
     spdField.setText("1");
     spdField.addActionListener (new ActionListener() {
       public void actionPerformed(ActionEvent _e) {
         changed = true;
         checkSPD();
       }
     });

//     JLabel realtimeLabel = new JLabel (res.getString ("Model.Evolution.RealTimeLabel"),SwingConstants.LEFT);
     JButton realtimeLabel = new JButton (res.getString ("Model.Evolution.RealTimeLabel"));
     realtimeLabel.setForeground (color);
     realtimeLabel.setFont(font);
     realtimeLabel.setToolTipText(res.getString ("Model.Evolution.RealTimeTooltip"));
     realtimeLabel.setBorder(new EmptyBorder(0,0,0,2));
     realtimeLabel.setHorizontalAlignment (SwingConstants.CENTER);
     realtimeLabel.addActionListener (new ActionListener() {
       public void actionPerformed(ActionEvent _e) {
         String option = EditorForVariables.edit (ejs.getModelEditor(), "Variables", "double", realtimeField, realtimeField.getText().trim(),"");
         if (option!=null) {
           realtimeField.setText(option);
           changed = true;
           checkRTV(); 
         }
       }
     });

     realtimeField = new JTextField (4);
     realtimeField.setFont(realtimeField.getFont().deriveFont(Font.BOLD));
     realtimeField.setEditable (true);
     realtimeField.setHorizontalAlignment (SwingConstants.RIGHT);
     realtimeField.setMargin (nullInsets);
     realtimeField.setText("");
     realtimeField.addActionListener (new ActionListener() {
       public void actionPerformed(ActionEvent _e) {
         changed = true;
         checkRTV(); 
       }
     });
     realtimeField.addKeyListener (new KeyListener(){
       public void keyPressed  (java.awt.event.KeyEvent _e) {
         if (_e.getKeyCode()==27)   {
           realtimeField.setText("");
           changed = true;
           checkRTV();
         }
       }
       public void keyReleased (java.awt.event.KeyEvent _e) { }
       public void keyTyped    (java.awt.event.KeyEvent _e) {
         if (_e.getKeyChar()!='\n') _e.getComponent().setBackground (Color.yellow);
         else if (_e.getKeyCode()==27) {
           realtimeField.setText("");
           changed = true;
           checkRTV();
         }
       }
     });
     realtimeField.addFocusListener (new FocusAdapter() {
       public void focusLost(java.awt.event.FocusEvent _e) {
         changed = true;
         checkRTV();       }
     });
     
    JLabel label = new JLabel (res.getString ("Model.Evolution.FpsLabel1"),SwingConstants.CENTER);
    label.setForeground (color);
    label.setFont(font);

    JPanel labelPanel = new JPanel(new GridLayout(0,1,0,0));
    labelPanel.add(label);

    if (res.getString ("Model.Evolution.FpsLabel2").trim().length()>0) {
      label = new JLabel (res.getString ("Model.Evolution.FpsLabel2"),SwingConstants.CENTER);
      label.setForeground (color);
      label.setFont(font);
      labelPanel.add(label);
    }
    if (res.getString ("Model.Evolution.FpsLabel3").trim().length()>0) {
      label = new JLabel (res.getString ("Model.Evolution.FpsLabel3"),SwingConstants.CENTER);
      label.setForeground (color);
      label.setFont(font);
      labelPanel.add(label);
    }

    startCheckBox = new JCheckBox (res.getString ("Model.Evolution.Autostart"),true);
    startCheckBox.setForeground (color);
    startCheckBox.setFont(font);
    startCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
    startCheckBox.setMargin(nullInsets);
    startCheckBox.setToolTipText(res.getString ("Model.Evolution.AutostartTooltip"));
    startCheckBox.addItemListener (new ItemListener() {
      public void itemStateChanged(ItemEvent _e) { changed = true; }
    });

//    JLabel runningModeLabel = new JLabel(res.getString("Model.Evolution.RunningMode"),SwingConstants.LEFT);
//    runningModeLabel.setForeground (color);
//    runningModeLabel.setFont(font);
//    runningModeLabel.setToolTipText(res.getString ("Model.Evolution.RunningModeTip"));
//
//    runningModeCombo = new JComboBox<TwoStrings> ();
////    runningModeCombo.addItem(new TwoStrings(res.getString("Model.Evolution.RunningModeRespectTime"),"MODE_RESPECT_TIME"));
////    runningModeCombo.addItem(new TwoStrings(res.getString("Model.Evolution.RunningModeRespectDelay"),"MODE_RESPECT_DELAY"));
//    runningModeCombo.addItem(new TwoStrings("1","MODE_JAVA_STYLE"));
//    runningModeCombo.addItem(new TwoStrings("2","MODE_RESPECT_TIME"));
//    runningModeCombo.addItem(new TwoStrings("3","MODE_RESPECT_DELAY"));
//    runningModeCombo.setForeground (color);
//    runningModeCombo.setFont(font);
//    runningModeCombo.setToolTipText(res.getString ("Model.Evolution.RunningModeTip"));
//    runningModeCombo.addItemListener(new ItemListener() {
//      public void itemStateChanged(ItemEvent _e) { changed = true; }
//    });
//    runningModeCombo.setSelectedIndex(0); // Respect time by default
//
//    JPanel runningModePanel = new JPanel(new BorderLayout());
//    runningModePanel.add(runningModeLabel,BorderLayout.WEST);
//    runningModePanel.add(runningModeCombo,BorderLayout.CENTER);

    
    
//    realtimeCheckBox = new JCheckBox (res.getString ("Model.Evolution.Realtime"),false);
//    realtimeCheckBox.setForeground (color);
//    realtimeCheckBox.setFont(font);
//    realtimeCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
//    realtimeCheckBox.setMargin(nullInsets);
//    realtimeCheckBox.setToolTipText(res.getString ("Model.Evolution.RealtimeTooltip"));
//    realtimeCheckBox.addItemListener (new ItemListener() {
//      public void itemStateChanged(ItemEvent _e) { 
//        changed = true;
//        if (realtimeCheckBox.isSelected()) fpsCardLayout.show(fpsPanel, "fps_realtime");
//        else fpsCardLayout.show(fpsPanel, "fps_direct");
//      }
//    });

    Set<JComponent> labelSet = new HashSet<JComponent>();
    labelSet.add(fpsLabel);
    if (_ejs.supportsJava()) labelSet.add(realtimeLabel);
    labelSet.add(spdLabel);
    labelSet.add(spdLabel);
//    if (_ejs.supportsJavascript()) labelSet.add(runningModeLabel);
    
        
    JPanel fpsDownPanel = new JPanel (new BorderLayout());
    fpsDownPanel.add (fpsLabel,BorderLayout.WEST);
    fpsDownPanel.add (fpsField,BorderLayout.CENTER);

    JPanel fpsDirectPanel = new JPanel (new BorderLayout());
    fpsDirectPanel.setBorder(new EmptyBorder(5,0,0,0));
    fpsDirectPanel.add (labelPanel,BorderLayout.NORTH);
    fpsDirectPanel.add (fpsSlider,BorderLayout.CENTER);
    fpsDirectPanel.add (fpsDownPanel,BorderLayout.SOUTH);

    JPanel fpsRTPanel = new JPanel (new BorderLayout());
    fpsRTPanel.add (realtimeLabel,BorderLayout.WEST);
    fpsRTPanel.add (realtimeField,BorderLayout.CENTER);
    
//    JPanel fpsRTFullPanel = new JPanel (new BorderLayout());
//    fpsRTFullPanel.add (fpsRTPanel,BorderLayout.NORTH);

    JPanel spdPanel = new JPanel (new BorderLayout());
    spdPanel.add (spdLabel,BorderLayout.WEST);
    spdPanel.add (spdField,BorderLayout.CENTER);
    spdPanel.setBorder (new EmptyBorder(1,0,0,0));
    
//    fpsPanel.add(fpsDirectPanel,"fps_direct");
//    fpsPanel.add(fpsRTFullPanel,"fps_realtime");
//    fpsCardLayout.show(fpsPanel, "fps_direct");
    
//    JPanel fpsSelectionPanel = new JPanel (new BorderLayout());
//    fpsSelectionPanel.add (fpsPanel,BorderLayout.CENTER);
//    fpsSelectionPanel.add (realtimeCheckBox,BorderLayout.NORTH);
    
    JPanel topPanel = new JPanel (new BorderLayout());
    topPanel.add (topLabel,BorderLayout.CENTER);
//    topPanel.add (new JSeparator(JSeparator.HORIZONTAL),BorderLayout.NORTH);
//    topPanel.add (new JSeparator(JSeparator.HORIZONTAL),BorderLayout.SOUTH);
//    topPanel.setBorder (new LineBorder(Color.BLACK));
    topPanel.setBorder (new EmptyBorder(5,0,0,0));

    JPanel southMiddlePanel = new JPanel (new GridLayout(0,1));
    if (_ejs.supportsJava()) southMiddlePanel.add (fpsRTPanel);
//    if (_ejs.supportsJavascript()) southMiddlePanel.add (runningModePanel);
    southMiddlePanel.add (spdPanel);

    JPanel southPanel = new JPanel (new BorderLayout());
    southPanel.add (fpsDirectPanel,BorderLayout.NORTH);
    southPanel.add (southMiddlePanel,BorderLayout.CENTER);
    southPanel.add (startCheckBox,BorderLayout.SOUTH);

    
    JPanel leftPanel = new JPanel (new BorderLayout());
    leftPanel.add (fpsDirectPanel,BorderLayout.CENTER);
    leftPanel.add (southPanel,BorderLayout.SOUTH);
    leftPanel.setBorder (new EmptyBorder(0,3,5,3));

//    JPanel southPanel = new JPanel (new BorderLayout());
//    southPanel.add (topPanel,BorderLayout.NORTH);
//    southPanel.add (spdPanel,BorderLayout.CENTER);
//    southPanel.add (startCheckBox,BorderLayout.SOUTH);
//    
//    JPanel leftPanel = new JPanel (new BorderLayout());
//    leftPanel.add (fpsSelectionPanel,BorderLayout.CENTER);
//    leftPanel.add (southPanel,BorderLayout.NORTH);
//    leftPanel.setBorder (new EmptyBorder(0,3,0,3));

    JPanel evolutionPanel = new JPanel (new BorderLayout());
    evolutionPanel.add(leftPanel,BorderLayout.WEST);

    // --- Now, create the editors
    editors = new Editor[6];
    editors[0] = variablesEditor = new VariablesEditor (_ejs);
    editors[1] = initializationEditor = new TabbedEditor (_ejs, Editor.CODE_EDITOR,"Model.Initialization");
    editors[2] = evolutionEditor = new TabbedEvolutionEditor (_ejs);
    editors[3] = constraintsEditor = new TabbedEditor (_ejs, Editor.CODE_EDITOR,"Model.Constraints");
    editors[4] = libraryEditor = new TabbedLibraryEditor (_ejs);
    editors[5] = elementsEditor = new ElementsEditor (_ejs);

    ActionListener al = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        cardLayout.show (panel,evt.getActionCommand());
      }
    };
    Border buttonsBorder = BorderFactory.createEmptyBorder(0,6,0,6);
    JPanel toolbar = new JPanel (new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridy = 0;
    Insets inset = new java.awt.Insets(0,3,0,3);
    
    font = InterfaceUtils.font(null,res.getString("Model.TitleFont"));
    buttons = MenuUtils.createRadioGroup (keywords,"Model.",al,_ejs.getOptions().useShortHeaders());
    for (int i=0; i<buttons.length; i++) {
      buttons[i].setBorder(buttonsBorder);
      gbc.gridx = i;
      buttons[i].setFont(font);
      buttons[i].setMargin(inset);
      toolbar.add (buttons[i],gbc);
    }

    JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.add(toolbar,BorderLayout.WEST);

    panel.add (variablesEditor.getComponent(),keywords[0]);
    panel.add (initializationEditor.getComponent(),keywords[1]);
    evolutionPanel.add (evolutionEditor.getComponent(),BorderLayout.CENTER);
    panel.add (evolutionPanel,keywords[2]);
    panel.add (constraintsEditor.getComponent(),keywords[3]);
    JPanel libraryPanel = new JPanel (new BorderLayout());
    libraryPanel.add(libraryEditor.getComponent(),BorderLayout.CENTER);
    //libraryPanel.add(jarsPanel,BorderLayout.SOUTH);
    panel.add (libraryPanel,keywords[4]);
    panel.add (elementsEditor.getComponent(),keywords[5]);

    cardLayout.show (panel,keywords[0]);
    buttons[0].setSelected(true);

    int maxWidth = 0, maxHeight=0;
    for (JComponent comp : labelSet) {
      maxWidth  = Math.max(maxWidth,  comp.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, comp.getPreferredSize().height);
    }
    Dimension dim = new Dimension (maxWidth,maxHeight);
    for (JComponent comp : labelSet) comp.setPreferredSize(dim);

    mainPanel.add (toolbarPanel,BorderLayout.NORTH);
    mainPanel.add (panel  ,BorderLayout.CENTER);
    setName("Model");
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
//    FontSizer.setFonts(mainPanel, level);
  }

  public Osejs getEJS () { return ejs; }
  public ElementsEditor getElementsEditor() { return this.elementsEditor; }
  public VariablesEditor getVariablesEditor() { return this.variablesEditor; }
  public TabbedEditor getInitializationEditor() { return this.initializationEditor; }
  public TabbedEvolutionEditor getEvolutionEditor() { return this.evolutionEditor; }
  public TabbedEditor getConstraintsEditor() { return this.constraintsEditor; }
  public TabbedLibraryEditor getLibraryEditor() { return this.libraryEditor; }

  public String[] getPredefinedActions() {
    if (mPredefinedActions==null) {
      String key = ejs.supportsJava() ? "EditorForVariables.ActionList" : "EditorForVariables.JavascriptActionList";
      mPredefinedActions = ResourceUtil.tokenizeString(sysRes.getString(key));
    }
    return mPredefinedActions;
  }

  public void checkSyntax() {
    checkSPD();
    checkRTV();
  }

  public String getRealtimeVariable() { 
    String variable = realtimeField.getText().trim();
    if (variable.length()<=0) return null;
    return variable;
  }

  /**
   * Checks for a valid value of SPD
   * @return true if the value is a constant
   */
  public boolean checkSPD() { return checkField(spdField,"int",true); }
  
  /**
   * Checks for a valid value of RTV
   * @return true if the value is empty
   */
  public boolean checkRTV() { 
    boolean empty = checkField(realtimeField,"double",false); 
    fpsSlider.setEnabled(empty);
    fpsField.setEnabled(empty);
    return empty; 
  }
  
  /**
   * Checks for a valid value of SPD
   * @return true if the value is a constant
   */
  private boolean checkField(JTextField _field, String _type, boolean _allowConstants) {
    String variable = _field.getText().trim();
    if (_allowConstants) {
      try {
        int value = Integer.parseInt(variable);
        if (value <= 1) _field.setText("1");
        _field.setBackground(Color.WHITE);
        return true;
      }
      catch (Exception exc) { }
    }
    else if (variable.length()<=0) {
      _field.setBackground(Color.WHITE);
      return true;
    }
    if (variablesEditor.isVariableDefined(variable,_type)) _field.setBackground(Color.WHITE);
    else {
      Value value = variablesEditor.checkExpression(variable,_type);
      if (_type.equals("int") && value instanceof IntegerValue) _field.setBackground(Color.WHITE);
      else if (_type.equals("double") && value instanceof DoubleValue) _field.setBackground(Color.WHITE);
      else _field.setBackground(TabbedEditor.ERROR_COLOR);
    }
    return false;
  }

  public void refreshHeaders(boolean _useShortForm) {
    for (int i=0; i<buttons.length; i++) {
      String key=buttons[i].getActionCommand();
      String text=null;
      if (_useShortForm) text = res.getOptionalString("Model."+key+".Short");
      if (text==null) text = res.getString("Model."+key);
      if (text==null) text = key;
      buttons[i].setText(text);
    }
  }
  
  
  // Start of implementation of Editor

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    for (int i=0; i<editors.length; i++) list.addAll(editors[i].search(_info,_searchString,_mode));
    return list;
  }

  public void setName (String _name) {
    name = _name;
    for (int i=0; i<editors.length; i++) editors[i].setName(_name+"."+keywords[i]);
  }

  public String getName() { return name; }

  public void clear () {
    for (int i=0; i<editors.length; i++) editors[i].clear();
    fpsSlider.setValue(DEFAULT_FPS);
    fpsField.setText(""+DEFAULT_FPS);
    spdField.setText("1");
    spdField.setBackground(Color.WHITE);
    realtimeField.setText("");
    realtimeField.setBackground(Color.WHITE);
    fpsSlider.setEnabled(true);
    fpsField.setEnabled(true);
    startCheckBox.setSelected(true);
//    runningModeCombo.setSelectedIndex(0);
    setChanged(false);
    isMerging = false;
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    for (int i=0; i<editors.length; i++) editors[i].setColor(_color);
    for (int i=0; i<buttons.length; i++) buttons[i].setForeground(_color);
  }

  public void setFont (Font _font) {
    for (int i=0; i<editors.length; i++) editors[i].setFont(_font);
    realtimeField.setFont(_font);
    spdField.setFont(_font);
  }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public boolean isChanged () {
    if (changed) return true;
    for (int i=0; i<editors.length; i++) if (editors[i].isChanged()) return true;
    return false;
  }

  public void setChanged (boolean _ch) {
    changed = _ch;
    for (int i=0; i<editors.length; i++) editors[i].setChanged(_ch);
  }

  public boolean isActive () {
    for (int i=0; i<editors.length; i++) if (!editors[i].isActive()) return false;
    return true;
  }
  
  public boolean isInternal() {
    return false;
  }

  public void setInternal(boolean _advanced) {  }

  public void setActive (boolean _active) {
    for (int i=0; i<editors.length; i++) editors[i].setActive(_active);
  }

  public String getStepsPerDisplay() { return spdField.getText(); }
  
  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (_type==Editor.GENERATE_SIMULATION_STATE) {
      boolean autoplay = startCheckBox.isSelected() && evolutionEditor.getActivePageCount()>0;
      code.append("    setFPS("+fpsSlider.getValue()+");\n");
      code.append("    setStepsPerDisplay(_model._getPreferredStepsPerDisplay()); \n");
      code.append("    if (_allowAutoplay) { setAutoplay("+autoplay+"); reset(); }\n");
      code.append("    else { reset(); setAutoplay("+autoplay+"); }\n");
    }
    else if (_type==Editor.GENERATE_RESOURCES_NEEDED) {
      code.append(ejs.getSimInfoEditor().getJarsStringBuffer());
      for (int i=0; i<editors.length; i++) code.append(editors[i].generateCode(_type,""));
    }

    else for (int i=0; i<editors.length; i++) code.append(editors[i].generateCode(_type,""));
    return code;
  }

  public void fillSimulationXML(SimulationXML _simXML) {
    int fps = fpsSlider.getValue();
    if (fps>= Animation.MAXIMUM_FPS) _simXML.setModelConfiguration(MODEL.FRAMES_PER_SECOND,res.getString("Model.Evolution.MaximumFPS"));
    else _simXML.setModelConfiguration(MODEL.FRAMES_PER_SECOND,""+fps);
    _simXML.setModelConfiguration(MODEL.STEPS_PER_DISPLAY,spdField.getText());
    _simXML.setModelConfiguration(MODEL.REAL_TIME_VARIABLE,realtimeField.getText());
    _simXML.setModelConfiguration(MODEL.AUTOSTART, startCheckBox.isSelected() ? "true" : "false");
//    _simXML.setModelConfiguration(MODEL.RUNNING_MODE,  ((TwoStrings) runningModeCombo.getSelectedItem()).getSecondString());
    variablesEditor.fillSimulationXML(_simXML);
    initializationEditor.fillSimulationXML(_simXML);
    evolutionEditor.fillSimulationXML(_simXML);
    constraintsEditor.fillSimulationXML(_simXML);
    libraryEditor.fillSimulationXML(_simXML);
    elementsEditor.fillSimulationXML(_simXML);
  }

  public void fillSimulationXMLForHtmlView(SimulationXML _simXML) {
    _simXML.setInsideEJS();
    variablesEditor.fillSimulationXML(_simXML);
    libraryEditor.fillSimulationXML(_simXML);
    for (EquationEditor eqnEditor : evolutionEditor.getODEpages()) {
      eqnEditor.fillSimulationXMLForHtmlView(_simXML);
    }
    elementsEditor.fillSimulationXML(_simXML);
  }

  /**
   * Sets the algebra-based style for all rates in ODE pages
   */
  public void setAlgebraBasedEquations(boolean algebraStyle) {
    for (EquationEditor eqnEditor : evolutionEditor.getODEpages()) {
      eqnEditor.setAlgebraBasedEquations(algebraStyle);
    }
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer save = new StringBuffer (
      "<"+name+".FramesPerSecond>"+fpsSlider.getValue()+"</"+name+".FramesPerSecond>\n" +
      "<"+name+".StepsPerDisplay>"+spdField.getText()+"</"+name+".StepsPerDisplay>\n" +
      "<"+name+".RealTimeVariable>"+realtimeField.getText()+"</"+name+".RealTimeVariable>\n" +
      "<"+name+".Autostart>"+startCheckBox.isSelected()+"</"+name+".Autostart>\n");
//      "<"+name+".RunningMode>"+runningModeCombo.getSelectedIndex()+"</"+name+".RunningMode>\n");
    for (int i=0; i<editors.length; i++)
      save.append("<"+name+"."+keywords[i]+">\n"+editors[i].saveStringBuffer()+"</"+name+"."+keywords[i]+">\n");
    return save;
  }

  public void setMerging (boolean _value) { isMerging = _value; }

  public void readString (String _input) {
    int fps = 0;
    if (!isMerging) {
      try {
        fps = Integer.parseInt(OsejsCommon.getPiece(_input,
         "<"+name+".FramesPerSecond>","</"+name+".FramesPerSecond>",false));
      }
      catch (NumberFormatException _e) { fps = DEFAULT_FPS; }
      fpsSlider.setValue(fps);
      if (fps>=Animation.MAXIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MaximumFPS"));
      else if (fps <= Animation.MINIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MinimumFPS"));
      else fpsField.setText(""+fps);

      String spd = OsejsCommon.getPiece(_input,"<"+name+".StepsPerDisplay>","</"+name+".StepsPerDisplay>",false);
      if (spd!=null) spdField.setText(spd);
      else spdField.setText("1");

      String rtv = OsejsCommon.getPiece(_input,"<"+name+".RealTimeVariable>","</"+name+".RealTimeVariable>",false);
      if (rtv!=null) realtimeField.setText(rtv);
      else realtimeField.setText("");
      checkRTV(); 

      String start = OsejsCommon.getPiece(_input,"<"+name+".Autostart>","</"+name+".Autostart>",false);
      if ("false".equals(start)) startCheckBox.setSelected(false);
      else startCheckBox.setSelected(true);

//      String runningModeStr = OsejsCommon.getPiece(_input,"<"+name+".RunningMode>","</"+name+".RunningMode>",false);
//      if (runningModeStr==null) runningModeCombo.setSelectedIndex(0);
//      else {
//        try { runningModeCombo.setSelectedIndex(Integer.parseInt(runningModeStr)); }
//        catch (Exception exc) { runningModeCombo.setSelectedIndex(0); }
//      }

    }
    
    // --- Backwards compatibility. Libraries and imports will be saved on SimInfoEditor
    // Extract the additional libraries, if any
    String libs = OsejsCommon.getPiece(_input,"<"+name+".AdditionalLibraries>","</"+name+".AdditionalLibraries>",false);
    if (libs!=null) {
      Set<String> libsToAdd = new HashSet<String>();
      int begin = libs.indexOf("<Library>");
      while (begin>=0) {
        int end = libs.indexOf("</Library>\n");
        String oneLib = libs.substring(begin+9,end);
        libsToAdd.add(oneLib);
        libs = libs.substring(end+11);
        begin = libs.indexOf("<Library>");
      }
      if (!libsToAdd.isEmpty()) OsejsCommon.warnAboutFiles(ejs.getMainPanel(),libsToAdd,"SimInfoEditor.RequiredFileNotFound");
    }
    // Extract the import statements, if any
    String imports = OsejsCommon.getPiece(_input,"<"+name+".ImportStatements>","</"+name+".ImportStatements>",false);
    if (imports!=null) {
      int begin = imports.indexOf("<Import>");
      while (begin>=0) {
        int end = imports.indexOf("</Import>\n");
        String oneImport = imports.substring(begin+8,end);
        ejs.getSimInfoEditor().addToImportsCombo(oneImport);
        imports = imports.substring(end+10);
        begin = imports.indexOf("<Import>");
      }
      ejs.getSimInfoEditor().updateImportStatements();
    }
    // End of backwards compatibility
    
    // Pass over the rest of the work to the editors
    for (int i=0; i<editors.length; i++) {
      int begin = _input.indexOf("<"+name+"."+keywords[i]+">\n");
      if (begin<0) continue;
      int end = _input.indexOf("</"+name+"."+keywords[i]+">\n");
      editors[i].readString(_input.substring(begin+keywords[i].length()+name.length()+4,end));
    }
    setChanged(false);
  }

// ------ End of implementation of Editor

// ------ Other public methods

  /**
   * Make sure a given panel is shown
   */
  public void showPanel (String subpanelStr) {
   for (int i=0; i<keywords.length; i++) {
      if (keywords[i].equals(subpanelStr)) {
        cardLayout.show(panel, keywords[i]);
        buttons[i].setSelected(true);
        return;
      }
    }
  }

  /**
   * Returns the list of all variables
   */
  public java.util.List<String> getAllVariables () {
    ArrayList<String> list = new ArrayList<String>();
    String listStr = variablesEditor.generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
    StringTokenizer lineTkn = new StringTokenizer (listStr,"\n");
    while (lineTkn.hasMoreTokens()) {
      String line = lineTkn.nextToken();
      StringTokenizer tkn = new StringTokenizer (line,":");
      String type = tkn.nextToken().trim();
      list.add(tkn.nextToken().trim()+":"+type);
    }
    return list;
  }

  /**
   * Returns the list of variables of one of the allowed types
   * @param _types the desired types, null for any type
   */
  private java.util.List<String> getVariables (String _types) {
    java.util.List<String> possibleTypes = new ArrayList<String>();
    if (_types!=null) {
      StringTokenizer typeTkn = new StringTokenizer(_types,"|");
      while (typeTkn.hasMoreTokens()) possibleTypes.add(typeTkn.nextToken());
    }
    java.util.List<String> list = new ArrayList<String>();
    String listStr = variablesEditor.generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
    StringTokenizer lineTkn = new StringTokenizer (listStr,"\n");
    while (lineTkn.hasMoreTokens()) {
      String line = lineTkn.nextToken();
      StringTokenizer tkn = new StringTokenizer (line,":");
      String type = tkn.nextToken().trim();
      if (_types==null) list.add(tkn.nextToken().trim());
      else for (String aType : possibleTypes) if (aType.equals(type)) { list.add(tkn.nextToken().trim()); break; }
    }
    return list;
  }

  /**
   * 
   * @param _variable
   * @param _types the desired types, null for any type
   * @return
   */
  public boolean isVariableDefinedOfType (String _variable, String _types) {
    for (String aVariable : getVariables(_types)) {
      if (_variable.equals(aVariable)) return true;
    }
    return false;
  }

  public java.util.List<String> getCustomMethods (String _type) {
    ArrayList<String> list = new ArrayList<String>();
    String txt = libraryEditor.generateCode(Editor.GENERATE_LIST_ACTIONS,_type).toString();
    StringTokenizer tkn = new StringTokenizer (txt,"\n");
    while (tkn.hasMoreTokens()) list.add(tkn.nextToken());
    return list;
  }

  /*
  public Vector getActions () {
    Vector actions= new Vector();
    for (Enumeration e = libraryEditor.getPageEnumeration(); e.hasMoreElements();)
      actions.addAll(((LibraryEditor) e.nextElement()).getActions());
    return actions;
  }
*/


} // end of class
