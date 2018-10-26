/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.experiments;

import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.tools.FontSizer;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.ArrayList;

public class ScheduledConditionEditor implements Editor {
  static public final int BOOLEAN_CONDITION  = 501;
  static public final int ACTION             = 502;

  static private ResourceUtil res = new ResourceUtil ("Resources");

  private boolean changed=false, visible=true, advanced=false;
  private String name="";
  private JLabel labelBooleanCondition, labelAction;
  JTextComponent textAreaBooleanCondition,textAreaAction;
  JTextField commentField;
  private JSplitPane splitPanel;
  private JPanel mainPanel;

  public ScheduledConditionEditor (ModelEditor _modelEditor) {
    changed = false;
    MyDocumentListener MDL = new MyDocumentListener();

    labelBooleanCondition = new JLabel(res.getString("Experiment.ScheduledCondition.BooleanCondition"));
    labelBooleanCondition.setBorder (new EmptyBorder(0,5,0,5));

    textAreaBooleanCondition = 	new JTextArea ();
    textAreaBooleanCondition.getDocument().addDocumentListener(MDL);
    JScrollPane scrollPanel2 = new JScrollPane(textAreaBooleanCondition);
    Dimension dim = res.getDimension("EquationEditor.Events.DialogSize");
    scrollPanel2.setPreferredSize(new Dimension(100,dim.height/3));

    JPanel panel2 = new JPanel (new BorderLayout());
    panel2.add(labelBooleanCondition,BorderLayout.NORTH);
    panel2.add(scrollPanel2,BorderLayout.CENTER);

    labelAction = new JLabel(res.getString("EquationEditor.Events.Action"));
    labelAction.setBorder (new EmptyBorder(0,5,0,5));

    textAreaAction = 	new JTextArea ();
    textAreaAction.getDocument().addDocumentListener(MDL);
    JScrollPane scrollPanel3 = new JScrollPane(textAreaAction);

    JPanel panel3 = new JPanel (new BorderLayout());
    panel3.add(labelAction,BorderLayout.NORTH);
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
    new Undo2(textAreaBooleanCondition,_modelEditor);
    new Undo2(textAreaAction,_modelEditor);
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
    textAreaBooleanCondition.setText("return false;");
    textAreaAction.setText("");
    commentField.setText("");
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    labelBooleanCondition.setForeground (_color);
    labelAction.setForeground (_color);
  }

  public void setFont (Font _font) {
    textAreaBooleanCondition.setFont (_font);
    textAreaAction.setFont (_font);
  }

  public void setEditable (boolean _editable) {
    textAreaBooleanCondition.setEditable(_editable);
    textAreaAction.setEditable(_editable);
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
    textAreaBooleanCondition.setEnabled (_active);
    textAreaAction.setEnabled (_active);
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
  
  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (!isActive()) return code;
    if (_info==null) _info = "";
    String codeTxt,lastName;
    if (_type==BOOLEAN_CONDITION)  {
      codeTxt = textAreaBooleanCondition.getText();
      lastName = res.getString("Experiment.ScheduledCondition.BooleanCondition");
    }
    else if (_type==ACTION) {
      codeTxt = textAreaAction.getText();
      lastName = res.getString("EquationEditor.Events.Action");
    }
    else return code;
    code.append(CodeEditor.splitCode(lastName,codeTxt,_info + ":" + getName(),"      "));
    return code;
  }

  public void fillSimulationXML(SimulationXML _simXML) { // Do nothing, for the moment
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer txt = new StringBuffer();
    txt.append("<Comment><![CDATA["+commentField.getText()+"]]></Comment>\n");
    txt.append("<BooleanCondition><![CDATA[\n"+textAreaBooleanCondition.getText()+"\n]]></BooleanCondition>\n");
    txt.append("<Action><![CDATA[\n"+textAreaAction.getText()+"\n]]></Action>\n");
    return txt;
  }

  public void readString (String _input) {
    commentField.setText(OsejsCommon.getPiece(_input,"<Comment><![CDATA[","]]></Comment>",false));
    textAreaBooleanCondition.setText(OsejsCommon.getPiece(_input,"<BooleanCondition><![CDATA[\n","\n]]></BooleanCondition>",false));
    textAreaAction.setText( OsejsCommon.getPiece(_input,"<Action><![CDATA[\n","\n]]></Action>",false));
    textAreaBooleanCondition.setCaretPosition(0);
    textAreaAction.setCaretPosition(0);
  }

  class MyDocumentListener implements DocumentListener {
    public void changedUpdate (DocumentEvent evt)  { changed = true; }
    public void insertUpdate  (DocumentEvent evt)  { changed = true; }
    public void removeUpdate  (DocumentEvent evt)  { changed = true; }
  }

} // end of class
