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
import org.colos.ejss.xml.SimulationXML.ODE_DISCONTINUITY;
import org.w3c.dom.Element;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.*;
import javax.swing.event.*;

import com.cdsc.eje.gui.EJEArea;

public class DiscontinuityEditor implements Editor {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  private Osejs ejs;
  private boolean changed=false, visible=true;
  private String name="";
  private JCheckBox stopCheckbox;
  private JTextComponent textAreaZeroCondition,textAreaAction;
  private JTextField commentField,toleranceField;
  private JSplitPane splitPanel;
  private JPanel mainPanel;
  protected TabbedEditor parentTabbedEditor;
  private EquationEditor equationEditor;
  protected ModelEditor modelEditor;
  private Set<Component> componentSet = new HashSet<Component>();
  private Set<JComponent> activeComponentSet = new HashSet<JComponent>(); // list of components to enable/disable when setActive is called

  public DiscontinuityEditor (Osejs _ejs, TabbedEditor aParentEditor, EquationEditor anEquationEditor) {
    ejs = _ejs;
    modelEditor = _ejs.getModelEditor();
    parentTabbedEditor = aParentEditor;
    equationEditor = anEquationEditor;
    changed = false;
    MyDocumentListener MDL = new MyDocumentListener();

    Icon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("EquationEditor.Select.Icon"));

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
    
    JPanel firstRow =  new JPanel (new BorderLayout());
    firstRow.add(zeroLabel,BorderLayout.WEST);
    firstRow.add(tolerancePanel,BorderLayout.EAST);

    JPanel firstHalf =  new JPanel (new BorderLayout());
    firstHalf.add(firstRow,BorderLayout.NORTH);
    firstHalf.add(zeroScrollPane,BorderLayout.CENTER);

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
    splitPanel.setTopComponent(firstHalf);
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
    activeComponentSet.add(stopCheckbox);
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
      if (index>=0) list.add(new DiscontinuitySearchResult(_info,line.trim(),textComponent,lineCounter,caretPosition+index));
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
    toleranceField.setFont(_font);
  }
  
  public void setZoomLevel (int level) {}

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public void refresh (boolean _hiddensToo) {
    splitPanel.setVisible (visible || _hiddensToo);
  }

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
  
  public boolean getStopAtDiscontinuity () { return stopCheckbox.isSelected(); }

  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (_info==null) _info = "";
    String codeTxt,lastName;
    if (_type==EventEditor.ZERO_CONDITION)  {
      codeTxt = textAreaZeroCondition.getText();
      lastName = res.getString("EquationEditor.Events.ZeroCondition");
    }
    else if (_type==EventEditor.ACTION) {
      codeTxt = textAreaAction.getText();
      lastName = res.getString("EquationEditor.Events.Action");
    }
    else return code;
    code.append(CodeEditor.splitCode(lastName,codeTxt,_info + ":" + getName(),"        "));
    return code;
  }

  public void fillSimulationXML(SimulationXML _simXML) { } // Do nothing, for the moment
  
  public void fillSimulationXML(SimulationXML _simXML, Element _ode) {
    Element discontinuity = _simXML.addODEDiscontinuity(_ode,getName(),commentField.getText().trim());
    _simXML.setEnabled(discontinuity, isActive());
    _simXML.setDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.ZERO_CONDITION, textAreaZeroCondition.getText().trim());
    _simXML.setDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.TOLERANCE, toleranceField.getText().trim());
    _simXML.setDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.ACTION, textAreaAction.getText().trim());
    _simXML.setDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.END_AT_DISCONTINUITY, stopCheckbox.isSelected() ? "true" : "false");
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer txt = new StringBuffer();
    txt.append("<Tolerance><![CDATA["+toleranceField.getText()+"]]></Tolerance>\n");
    txt.append("<StopAtDiscontinuity>"+stopCheckbox.isSelected()+"</StopAtDiscontinuity>\n");
    txt.append("<ZeroCondition><![CDATA[\n"+textAreaZeroCondition.getText()+"\n]]></ZeroCondition>\n");
    txt.append("<Action><![CDATA[\n"+textAreaAction.getText()+"\n]]></Action>\n");
    txt.append("<Comment><![CDATA["+commentField.getText()+"]]></Comment>\n");
    return txt;
  }

  public void readString (String _input) {
    toleranceField.setText(OsejsCommon.getPiece(_input,"<Tolerance><![CDATA[","]]></Tolerance>",false));

    if ("true".equals(OsejsCommon.getPiece(_input,"<StopAtDiscontinuity>","</StopAtDiscontinuity>",false))) stopCheckbox.setSelected(true);
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

  class DiscontinuitySearchResult extends SearchResult {
    public DiscontinuitySearchResult (String anInformation, String aText, JTextComponent textComponent, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,textComponent,aLineNumber,aCaretPosition);
    }

    public String toString () {
      if (this.containerTextComponent == toleranceField) return information+": "+textFound;
      return information+"("+lineNumber+"): "+textFound;
    }

    public void show () {
      equationEditor.showDiscontinuityTab();
      parentTabbedEditor.showPage(DiscontinuityEditor.this);
      equationEditor.showDialogEvent();
      super.show();
    }
  }

} // end of class
