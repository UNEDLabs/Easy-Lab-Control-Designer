/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.ODE_EVENT;
import org.opensourcephysics.tools.FontSizer;
import org.w3c.dom.Element;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.*;
import javax.swing.event.*;
import com.cdsc.eje.gui.EJEArea;

public class EventEditor implements Editor {
  static public final int ZERO_CONDITION  = 501;
  static public final int ACTION          = 502;

  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  private Osejs ejs;
  private boolean changed=false, visible=true;
  private String name="";
  private JCheckBox stopCheckbox;
  private JTextComponent textAreaZeroCondition,textAreaAction;
  private JTextField commentField,toleranceField,iterationsField;
  private JComboBox<TwoStrings> typeCombo, methodCombo;
  private JSplitPane splitPanel;
  private JPanel mainPanel;
  protected TabbedEditor parentTabbedEditor;
  private EquationEditor equationEditor;
  protected ModelEditor modelEditor;
  private Set<Component> componentSet = new HashSet<Component>();
  private Set<JComponent> activeComponentSet = new HashSet<JComponent>(); // list of components to enable/disable when setActive is called

  public EventEditor (Osejs _ejs, TabbedEditor aParentEditor, EquationEditor anEquationEditor) {
    ejs = _ejs;
    modelEditor = _ejs.getModelEditor();
    parentTabbedEditor = aParentEditor;
    equationEditor = anEquationEditor;
    changed = false;
    MyDocumentListener MDL = new MyDocumentListener();

    ActionListener changeActionListener = new ActionListener (){
      public void actionPerformed(ActionEvent _evt) { changed = true; }
    };

    KeyListener keyListener = new KeyAdapter() {
      public void keyPressed  (java.awt.event.KeyEvent _e) { EquationEditor.checkValue(ejs,_e.getComponent(),false); }
      public void keyReleased (java.awt.event.KeyEvent _e) { EquationEditor.checkValue(ejs,_e.getComponent(),false); }
      public void keyTyped    (java.awt.event.KeyEvent _e) { 
        changed = true; 
        EquationEditor.checkValue(ejs,_e.getComponent(),false); 
      }
    };
    
    ComponentListener componentListener = new ComponentAdapter() {
      public void componentShown(ComponentEvent _e) { EquationEditor.checkValue(ejs,_e.getComponent(),false); }
    };

    Icon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("EquationEditor.Select.Icon"));

    // Type of event
    JLabel typeLabel = new JLabel(res.getString("VariableEditor.Type"));
    typeLabel.setBorder(EquationEditor.BORDER_SMALL);
    componentSet.add(typeLabel);
    
    typeCombo = new JComboBox<TwoStrings>();
    typeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Events.STATE_EVENT"),"STATE_EVENT"));
    typeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Events.CROSSING_EVENT"),"CROSSING_EVENT"));
    typeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Events.POSITIVE_EVENT"),"POSITIVE_EVENT"));
    typeCombo.setSelectedIndex(1);
    typeCombo.addActionListener(changeActionListener);

    JPanel typePanel =  new JPanel (new BorderLayout());
    typePanel.add(typeLabel,BorderLayout.WEST);
    typePanel.add(typeCombo,BorderLayout.CENTER);

    // method option
    JLabel methodLabel = new JLabel (res.getString("EquationEditor.Events.Method"));
    methodLabel.setBorder(EquationEditor.BORDER_SMALL);
    componentSet.add(methodLabel);

    methodCombo = new JComboBox<TwoStrings>();
    methodCombo.addItem(new TwoStrings(res.getString("EquationEditor.Events.Method.BISECTION"),"BISECTION"));
    methodCombo.addItem(new TwoStrings(res.getString("EquationEditor.Events.Method.SECANT"),"SECANT"));
    methodCombo.setSelectedIndex(0);
    methodCombo.addActionListener(changeActionListener);
    
    JPanel methodPanel = new JPanel (new BorderLayout());
    methodPanel.add(methodLabel, BorderLayout.WEST);
    methodPanel.add(methodCombo,BorderLayout.CENTER);

    // iterations option
    JLabel iterationsLabel = new JLabel (res.getString("EquationEditor.Events.Iterations"));
    iterationsLabel.setBorder(EquationEditor.BORDER_SMALL);
    componentSet.add(iterationsLabel);
    
    iterationsField = new JTextField(10);
    iterationsField.addKeyListener (keyListener);
    iterationsField.addComponentListener(componentListener);
    iterationsField.setText(""+100);

    JButton iterationsButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    iterationsButton.setMargin(EquationEditor.NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) iterationsButton.setBorder(EquationEditor.BORDER_SMALL);
    iterationsButton.setFocusPainted(false);
    iterationsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _event) {
        EquationEditor.selectVariableFor(ejs,iterationsField,"100","int",false);
        changed = true;
      }
    });

    JPanel iterationsPanel = new JPanel (new BorderLayout());
    iterationsPanel.add(iterationsLabel, BorderLayout.WEST);
    iterationsPanel.add(iterationsField,BorderLayout.CENTER);
    iterationsPanel.add(iterationsButton,BorderLayout.EAST);

    // Zero condition
    JLabel zeroLabel = new JLabel(res.getString("EquationEditor.Events.ZeroCondition"));
    zeroLabel.setBorder(EquationEditor.BORDER_SMALL);
    componentSet.add(zeroLabel);

    textAreaZeroCondition = new EJEArea(_ejs);
    textAreaZeroCondition.getDocument().addDocumentListener(MDL);
    JScrollPane zeroScrollPane = new JScrollPane(textAreaZeroCondition);

    // Tolerance
    JLabel toleranceLabel = new JLabel(res.getString("EquationEditor.Tolerance"));
    toleranceLabel.setBorder(EquationEditor.BORDER_SMALL);
    componentSet.add(toleranceLabel);

    toleranceField = new JTextField(10);
    toleranceField.setText("1.0e-5");

    JButton toleranceButton = (icon==null) ? new JButton ("...") : new JButton (icon);
    toleranceButton.setMargin(EquationEditor.NULL_INSETS);
    if (ejs.isNimbusLookAndFeel()) toleranceButton.setBorder(EquationEditor.BORDER_SMALL);
    toleranceButton.setFocusPainted(false);
    toleranceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _event) {
        EquationEditor.selectVariableFor(ejs,toleranceField,"1.e-5","double",false);
        changed = true;
      }
    });

    JPanel tolerancePanel =  new JPanel (new BorderLayout());
    tolerancePanel.add(toleranceLabel,BorderLayout.WEST);
    tolerancePanel.add(toleranceField,BorderLayout.CENTER);
    tolerancePanel.add(toleranceButton,BorderLayout.EAST);

    // Action
    JLabel actionLabel = new JLabel(res.getString("EquationEditor.Events.Action"));
    actionLabel.setBorder(EquationEditor.BORDER_SMALL);
    componentSet.add(actionLabel);

    stopCheckbox = new JCheckBox (res.getString("EquationEditor.Events.StopAtEvent"),true);
    stopCheckbox.setBorder (EquationEditor.BORDER_SMALL);
    stopCheckbox.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent arg0) { changed = true; }
    }); 

    textAreaAction = new EJEArea(_ejs);
    textAreaAction.getDocument().addDocumentListener(MDL);
    JScrollPane actionScrollPane = new JScrollPane(textAreaAction);

    // Comment
    JLabel commentLabel = new JLabel (res.getString ("Editor.Comment"));
    commentLabel.setBorder(new EmptyBorder(0,0,0,3));
    commentLabel.setFont (InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));

    commentField = new JTextField();
    commentField.setEditable (true);
    commentField.getDocument().addDocumentListener(MDL);
    commentField.setFont(InterfaceUtils.font(null,res.getString("Osejs.DefaultFont")));

    JPanel commentPanel = new JPanel (new BorderLayout());
    commentPanel.add (commentLabel,BorderLayout.WEST);
    commentPanel.add (commentField,BorderLayout.CENTER);


    // Put everything together

    
    JPanel firstRowRight = new JPanel(new BorderLayout());
    firstRowRight.add(iterationsPanel,BorderLayout.CENTER);
    firstRowRight.add(methodPanel,BorderLayout.EAST);

    JPanel firstRow = new JPanel(new BorderLayout());
    firstRow.add(typePanel,BorderLayout.WEST);
    firstRow.add(firstRowRight,BorderLayout.CENTER);
    firstRow.add(new JSeparator(),BorderLayout.SOUTH);

    JPanel secondRow =  new JPanel (new BorderLayout());
    secondRow.add(zeroLabel,BorderLayout.WEST);
    secondRow.add(tolerancePanel,BorderLayout.EAST);

    JPanel firstHalf =  new JPanel (new BorderLayout());
    firstHalf.add(secondRow,BorderLayout.NORTH);
    firstHalf.add(zeroScrollPane,BorderLayout.CENTER);

    JPanel topPanel =  new JPanel (new BorderLayout());
    topPanel.add(firstRow,BorderLayout.NORTH);
    topPanel.add(firstHalf,BorderLayout.CENTER);

    JPanel actionRow = new JPanel(new BorderLayout());
    actionRow.add(actionLabel,BorderLayout.WEST);
    actionRow.add(stopCheckbox,BorderLayout.EAST);
    
    JPanel secondHalf =  new JPanel (new BorderLayout());
    secondHalf.add(actionRow,BorderLayout.NORTH);
    secondHalf.add(actionScrollPane,BorderLayout.CENTER);

//    Dimension dim = res.getDimension("EquationEditor.Events.AreaSize");
//    zeroScrollPane.setPreferredSize(dim);
//    actionScrollPane.setPreferredSize(dim);

    splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT );
    splitPanel.setTopComponent(topPanel);
    splitPanel.setBottomComponent(secondHalf);
    splitPanel.setOneTouchExpandable(true);
    splitPanel.setResizeWeight(0.5);

    mainPanel = new JPanel (new BorderLayout ());
    mainPanel.add(splitPanel,BorderLayout.CENTER);
    mainPanel.add (commentPanel,BorderLayout.SOUTH);

    mainPanel.setBorder (new EmptyBorder(5,2,0,2));
    mainPanel.validate();
    new Undo2(textAreaZeroCondition,modelEditor);
    new Undo2(textAreaAction,modelEditor);
    
    activeComponentSet.add(textAreaZeroCondition);
    activeComponentSet.add(textAreaAction);
    activeComponentSet.add(toleranceField);
    activeComponentSet.add(toleranceButton);
    activeComponentSet.add(commentField);
    activeComponentSet.add(typeCombo);
    activeComponentSet.add(iterationsField);
    activeComponentSet.add(iterationsButton);
    activeComponentSet.add(methodCombo);
    activeComponentSet.add(stopCheckbox);

    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(mainPanel, level);
  }
  
  public String toString() { return name; }

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();
    if (_info==null) _info = "";
    else _info=res.getString(_info);//FKH 021020
    String addInfo;
    addInfo = getName()+" ("+res.getString ("EquationEditor.Tolerance");
    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,toleranceField));
    addInfo = getName()+" ("+res.getString ("EquationEditor.Events.Iterations");
    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,iterationsField));
    addInfo = getName()+"."+res.getString ("EquationEditor.Events.ZeroCondition");
    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,textAreaZeroCondition));
    addInfo = getName()+"."+res.getString ("EquationEditor.Events.Action");
    list.addAll(searchInComponent(_info+"."+addInfo,_searchString,toLower,textAreaAction));
    return list;
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
      if (index>=0) list.add(new EventSearchResult(_info,line.trim(),textComponent,lineCounter,caretPosition+index));
      caretPosition += line.length()+1;
      lineCounter++;
    }
    return list;
  }

  public void setName (String _newName) {
    name = _newName;
    changed = true;
  }

  public String getName() { return name; }

  public void clear () {
    textAreaZeroCondition.setText("return 1.0;");
    textAreaAction.setText("");
    toleranceField.setText("1.0e-5");
    typeCombo.setSelectedIndex(1);
    iterationsField.setText("100");
    methodCombo.setSelectedIndex(0);
    stopCheckbox.setSelected(true);
    commentField.setText("");
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    stopCheckbox.setForeground (_color);
    for (Component comp : componentSet) comp.setForeground(_color);
  }

  public void setFont (Font _font) {
    textAreaZeroCondition.setFont (_font);
    textAreaAction.setFont (_font);
    iterationsField.setFont(_font);
    toleranceField.setFont(_font);
  }

//  public void setEditable (boolean _editable) {
//    textAreaZeroCondition.setEditable(_editable);
//    textAreaAction.setEditable(_editable);
//    toleranceField.setEditable(_editable);
//    typeCombo.setEnabled(_editable);
//    iterationsField.setEnabled(_editable);
//    methodCombo.setEnabled(_editable);
//    stopCheckbox.setEnabled(_editable);
//    commentField.setEditable(_editable);
//  }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public void refresh (boolean _hiddensToo) {
    splitPanel.setVisible (visible || _hiddensToo);
  }

//  public void adjust () {
//    if (adjusted || splitPanel.getSize().height==0) return;
//    splitPanel.setDividerLocation(0.5);
//    splitPanel.validate();
//    adjusted = true;
//  }

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _ch) { changed = _ch; }

  private boolean activeEditor=true, advanced=false;
  public boolean isActive () { return activeEditor;}
  public boolean isInternal() {
    return advanced;
  }

  public void setInternal(boolean _advanced) {
    advanced = _advanced;
  }
  public void setActive (boolean _active) {
//    textAreaZeroCondition.setEnabled (_active);
//    textAreaAction.setEnabled (_active);
//    toleranceField.setEnabled (_active);
//    commentField.setEnabled(_active);
//    typeCombo.setEnabled(_active);
//    iterationsField.setEnabled(_active);
//    methodCombo.setEnabled(_active);
//    stopCheckbox.setEnabled (_active);
    
    activeEditor=_active;
    changed = true;
    setActiveComponents();
  }
  
  public void setActiveComponents() {
    boolean active = activeEditor && parentTabbedEditor.isActive();
    for (JComponent comp : activeComponentSet) {
      comp.setEnabled(active);
      if (comp instanceof JTextField) ((JTextField) comp).setEditable(active);
    }
  }

  public String getTolerance () { return toleranceField.getText(); }

  public String getIterations() { return iterationsField.getText(); }
  
  public String getMethod() {
    TwoStrings ts = (TwoStrings) methodCombo.getSelectedItem();
    return ts.getSecondString(); 
  }
  
  public String getEventType () {
    TwoStrings ts = (TwoStrings) typeCombo.getSelectedItem();
    return ts.getSecondString(); 
  }

  public boolean getStopAtEvent () { return stopCheckbox.isSelected(); }

  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (_info==null) _info = "";
    String codeTxt,lastName;
    if (_type==ZERO_CONDITION)  {
      codeTxt = textAreaZeroCondition.getText();
      lastName = res.getString("EquationEditor.Events.ZeroCondition");
    }
    else if (_type==ACTION) {
      codeTxt = textAreaAction.getText();
      lastName = res.getString("EquationEditor.Events.Action");
    }
    else return code;
    code.append(CodeEditor.splitCode(lastName,codeTxt,_info + ":" + getName(),"        "));
    return code;
  }

  public void fillSimulationXML(SimulationXML _simXML) { } // Do nothing, for the moment
  
  public void fillSimulationXML(SimulationXML _simXML, Element _ode) {
    Element event = _simXML.addODEEvent(_ode,getName(),commentField.getText().trim());
    _simXML.setEnabled(event,isActive());

    _simXML.setEventConfiguration(event, ODE_EVENT.TYPE, getEventType());
    _simXML.setEventConfiguration(event, ODE_EVENT.ITERATIONS, iterationsField.getText().trim());
    _simXML.setEventConfiguration(event, ODE_EVENT.METHOD, getMethod());
    
    _simXML.setEventConfiguration(event, ODE_EVENT.ZERO_CONDITION, textAreaZeroCondition.getText().trim());
    _simXML.setEventConfiguration(event, ODE_EVENT.TOLERANCE, toleranceField.getText().trim());
    
    _simXML.setEventConfiguration(event, ODE_EVENT.ACTION, textAreaAction.getText().trim());
    _simXML.setEventConfiguration(event, ODE_EVENT.END_AT_EVENT, stopCheckbox.isSelected() ? "true" : "false");
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer txt = new StringBuffer();
    txt.append("<EventType>"+this.getEventType()+"</EventType>\n");
    txt.append("<Method>"+getMethod()+"</Method>\n");
    txt.append("<Iterations>"+iterationsField.getText()+"</Iterations>\n");
    txt.append("<Tolerance><![CDATA["+toleranceField.getText()+"]]></Tolerance>\n");
    txt.append("<StopAtEvent>"+stopCheckbox.isSelected()+"</StopAtEvent>\n");
    txt.append("<ZeroCondition><![CDATA[\n"+textAreaZeroCondition.getText()+"\n]]></ZeroCondition>\n");
    txt.append("<Action><![CDATA[\n"+textAreaAction.getText()+"\n]]></Action>\n");
    txt.append("<Comment><![CDATA["+commentField.getText()+"]]></Comment>\n");
    return txt;
  }

  public void readString (String _input) {
    
    String txt = OsejsCommon.getPiece(_input,"<EventType>","</EventType>",false);
    if (txt==null) typeCombo.setSelectedIndex(0);
    else {
      for (int i=0,n=typeCombo.getItemCount(); i<n; i++) {
        TwoStrings ts = (TwoStrings) typeCombo.getItemAt(i);
        if (txt.equals(ts.getSecondString())) { typeCombo.setSelectedIndex(i); break; }
      }
    }

    txt = OsejsCommon.getPiece(_input,"<Method>","</Method>",false);
    if (txt==null) methodCombo.setSelectedIndex(0); 
    else for (int i=0,n=methodCombo.getItemCount(); i<n; i++) {
      TwoStrings ts = (TwoStrings) methodCombo.getItemAt(i);
      if (txt.equals(ts.getSecondString())) { methodCombo.setSelectedIndex(i); break; }
    }

    txt = OsejsCommon.getPiece(_input,"<Iterations>","</Iterations>",false);
    if (txt!=null) iterationsField.setText(txt);
    else iterationsField.setText("100");

    toleranceField.setText(OsejsCommon.getPiece(_input,"<Tolerance><![CDATA[","]]></Tolerance>",false));

    if ("true".equals(OsejsCommon.getPiece(_input,"<StopAtEvent>","</StopAtEvent>",false))) stopCheckbox.setSelected(true);
    else stopCheckbox.setSelected(false);
    
    textAreaZeroCondition.setText(OsejsCommon.getPiece(_input,"<ZeroCondition><![CDATA[\n","\n]]></ZeroCondition>",false));
    textAreaZeroCondition.setCaretPosition(0);

    textAreaAction.setText(OsejsCommon.getPiece(_input,"<Action><![CDATA[\n","\n]]></Action>",false));
    textAreaAction.setCaretPosition(0);

    commentField.setText(  OsejsCommon.getPiece(_input,"<Comment><![CDATA[","]]></Comment>",false));
  }

  // --- private methods and classes

  class MyDocumentListener implements DocumentListener {
    public void changedUpdate (DocumentEvent evt)  { changed = true; }
    public void insertUpdate  (DocumentEvent evt)  { changed = true; }
    public void removeUpdate  (DocumentEvent evt)  { changed = true; }
  }

  class EventSearchResult extends SearchResult {
    public EventSearchResult (String anInformation, String aText, JTextComponent textComponent, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,textComponent,aLineNumber,aCaretPosition);
    }

    public String toString () {
      if (this.containerTextComponent == toleranceField) return information+": "+textFound;
      return information+"("+lineNumber+"): "+textFound;
    }

    public void show () {
      parentTabbedEditor.showPage(EventEditor.this);
      equationEditor.showDialogEvent();
      super.show();
    }
  }

} // end of class
