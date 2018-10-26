/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.experiments;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.edition.ode_editor.EquationEditor;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.tools.FontSizer;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.*;
import javax.swing.event.*;

public class ScheduledEventEditor implements Editor {
  static public final int ZERO_CONDITION  = 501;
  static public final int ACTION          = 502;

  static private ResourceUtil res = new ResourceUtil ("Resources");

  private boolean changed=false, visible=true, advanced=false;
  private String name="";
  private JLabel labelZeroCondition, labelAction, labelTolerance, labelOde;
  private JCheckBox stopCheckbox;
  private JTextComponent textAreaZeroCondition,textAreaAction;
  private JTextField commentField,toleranceField;
  private JSplitPane splitPanel;
  private JPanel mainPanel;
  private Osejs ejs;
  JComboBox<String> odeList;

  public ScheduledEventEditor (Osejs _ejs) {
    ejs=_ejs;
    changed = false;
    MyDocumentListener MDL = new MyDocumentListener();

    // Create OdeList
    odeList = new JComboBox<String>();
    odeList.setEnabled(false);
    updateOdeList();

    labelOde= new JLabel(res.getString("Experiment.ScheduledEvent.ODEPage"));
    labelOde.setBorder (new EmptyBorder(0,0,0,3));

    labelZeroCondition = new JLabel(res.getString("EquationEditor.Events.ZeroCondition"));
    labelZeroCondition.setBorder (new EmptyBorder(0,5,0,15));
    labelTolerance = new JLabel(res.getString("EquationEditor.Tolerance"));
    labelTolerance.setBorder (new EmptyBorder(0,5,0,5));
    toleranceField = new JTextField(6);
    toleranceField.setText("0.001");

    textAreaZeroCondition = 	new JTextArea ();
    textAreaZeroCondition.getDocument().addDocumentListener(MDL);
    JScrollPane scrollPanel2 = new JScrollPane(textAreaZeroCondition);
    Dimension dim = res.getDimension("EquationEditor.Events.DialogSize");
    scrollPanel2.setPreferredSize(new Dimension(100,dim.height/3));

    JPanel panel2UpEast =  new JPanel (new BorderLayout());
    panel2UpEast.add(labelTolerance,BorderLayout.CENTER);
    panel2UpEast.add(toleranceField,BorderLayout.EAST);

    JPanel panel2UpCenter = new JPanel (new BorderLayout());
    panel2UpCenter.add(odeList,BorderLayout.CENTER);
    panel2UpCenter.add(labelOde,BorderLayout.WEST);

    JPanel panel2Up =  new JPanel (new BorderLayout());
    panel2Up.add(labelZeroCondition,BorderLayout.WEST);
    panel2Up.add(panel2UpEast,BorderLayout.EAST);
    panel2Up.add(panel2UpCenter,BorderLayout.CENTER);

    JPanel panel2 = new JPanel (new BorderLayout());
    panel2.add(panel2Up,BorderLayout.NORTH);
    panel2.add(scrollPanel2,BorderLayout.CENTER);

    labelAction = new JLabel(res.getString("EquationEditor.Events.Action"));
    labelAction.setBorder (new EmptyBorder(0,5,0,5));
    stopCheckbox = new JCheckBox (res.getString("EquationEditor.Events.StopAtEvent"));
    stopCheckbox.setBorder (new EmptyBorder(0,5,0,5));
    stopCheckbox.setSelected(true);

    JPanel panel3Up = new JPanel (new BorderLayout());
    panel3Up.add(labelAction,BorderLayout.WEST);
    panel3Up.add(stopCheckbox,BorderLayout.EAST);

    textAreaAction = 	new JTextArea ();
    textAreaAction.getDocument().addDocumentListener(MDL);
    JScrollPane scrollPanel3 = new JScrollPane(textAreaAction);

    JPanel panel3 = new JPanel (new BorderLayout());
    panel3.add(panel3Up,BorderLayout.NORTH);
    panel3.add(scrollPanel3,BorderLayout.CENTER);

    commentField = new JTextField();
    commentField.setEditable (true);
    commentField.getDocument().addDocumentListener(MDL);
    commentField.setFont(InterfaceUtils.font(null,res.getString("Osejs.DefaultFont")));

    JLabel commentLabel = new JLabel (res.getString ("Editor.Comment"));
    commentLabel.setBorder(new EmptyBorder(0,0,0,3));
    commentLabel.setFont (InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    JPanel commentPanel = new JPanel (new BorderLayout());
    commentPanel.add (commentLabel,BorderLayout.WEST);
    commentPanel.add (commentField,BorderLayout.CENTER);

    splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT );
    splitPanel.setTopComponent(panel2);
    splitPanel.setBottomComponent(panel3);
    splitPanel.setOneTouchExpandable(true);

    mainPanel = new JPanel (new BorderLayout ());
    mainPanel.add(splitPanel,BorderLayout.CENTER);
    mainPanel.add (commentPanel,BorderLayout.SOUTH);

    mainPanel.setBorder (new EmptyBorder(5,2,0,2));
    mainPanel.validate();
    new Undo2(textAreaZeroCondition,ejs.getModelEditor());
    new Undo2(textAreaAction,ejs.getModelEditor());
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(mainPanel, level);
  }
  
  public String toString() { return name; }

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
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
    toleranceField.setText("0.001");
    stopCheckbox.setSelected(true);
    commentField.setText("");
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    labelOde.setForeground (_color);
    labelZeroCondition.setForeground (_color);
    labelAction.setForeground (_color);
    labelTolerance.setForeground (_color);
    stopCheckbox.setForeground (_color);
  }

  public void setFont (Font _font) {
    textAreaZeroCondition.setFont (_font);
    textAreaAction.setFont (_font);
  }

  public void setEditable (boolean _editable) {
    textAreaZeroCondition.setEditable(_editable);
    textAreaAction.setEditable(_editable);
    toleranceField.setEditable(_editable);
    stopCheckbox.setEnabled(_editable);
    commentField.setEditable(_editable);
  }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public void refresh (boolean _hiddensToo) {
    splitPanel.setVisible (visible || _hiddensToo);
  }

  public void adjust () {
    splitPanel.setDividerLocation(0.5);
    splitPanel.validate();
  }

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _ch) { changed = _ch; }

  public void setActive (boolean _active) {
    textAreaZeroCondition.setEnabled (_active);
    textAreaAction.setEnabled (_active);
    toleranceField.setEnabled (_active);
    stopCheckbox.setEnabled (_active);
    changed = true;
    activeEditor=_active;
  }
  private boolean activeEditor=true;//FKH 021024
  public boolean isActive () { return activeEditor;}
  public boolean isInternal() {
    return advanced;
  }

  public void setInternal(boolean _advanced) {
    advanced = _advanced;
  }
  public String getTolerance () { return toleranceField.getText(); }

  public boolean getStopAtEvent () { return stopCheckbox.isSelected(); }

  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (!isActive()) return code;
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

  public void fillSimulationXML(SimulationXML _simXML) { // Do nothing, for the moment
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer txt = new StringBuffer();
    txt.append("<Comment><![CDATA["+commentField.getText()+"]]></Comment>\n");
    txt.append("<ZeroCondition><![CDATA[\n"+textAreaZeroCondition.getText()+"\n]]></ZeroCondition>\n");
    txt.append("<Tolerance><![CDATA["+toleranceField.getText()+"]]></Tolerance>\n");
    txt.append("<Action><![CDATA[\n"+textAreaAction.getText()+"\n]]></Action>\n");
    txt.append("<OdeIndex><![CDATA[\n"+odeList.getSelectedItem()+"\n]]></OdeIndex>\n");
    txt.append("<StopAtEvent>"+stopCheckbox.isSelected()+"</StopAtEvent>\n");
    return txt;
  }

  public void readString (String _input) {
    commentField.setText(  OsejsCommon.getPiece(_input,"<Comment><![CDATA[","]]></Comment>",false));
    textAreaZeroCondition.setText(     OsejsCommon.getPiece(_input,"<ZeroCondition><![CDATA[\n","\n]]></ZeroCondition>",false));
    toleranceField.setText(OsejsCommon.getPiece(_input,"<Tolerance><![CDATA[","]]></Tolerance>",false));
    textAreaAction.setText(     OsejsCommon.getPiece(_input,"<Action><![CDATA[\n","\n]]></Action>",false));
    odeList.setSelectedItem((OsejsCommon.getPiece(_input,"<OdeIndex><![CDATA[\n","\n]]></OdeIndex>",false)));
    if ("true".equals(OsejsCommon.getPiece(_input,"<StopAtEvent>","</StopAtEvent>",false))) stopCheckbox.setSelected(true);
    else stopCheckbox.setSelected(false);
    textAreaZeroCondition.setCaretPosition(0);
    textAreaAction.setCaretPosition(0);
  }

  public void updateOdeList(){
    if (ejs==null) return;
    TabbedEditor tabbedEditor = ejs.getModelEditor().getEvolutionEditor();
    odeList.removeAllItems();
    odeList.setEnabled(false);
    for (Iterator<Editor> it = tabbedEditor.getPages().iterator(); it.hasNext(); ) {
      odeList.setEnabled(true);
      Editor editor = it.next();
      if (editor instanceof EquationEditor) {
        odeList.addItem(editor.getName());
      }
    }
  }

  public String getOdePage() {
    if (ejs!=null) return ((String)odeList.getSelectedItem());
    return "";
  }

  // --- private methods and classes

  class MyDocumentListener implements DocumentListener {
    public void changedUpdate (DocumentEvent evt)  { changed = true; }
    public void insertUpdate  (DocumentEvent evt)  { changed = true; }
    public void removeUpdate  (DocumentEvent evt)  { changed = true; }
  }

} // end of class
