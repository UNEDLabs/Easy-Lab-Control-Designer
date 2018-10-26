/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;

import org.colos.ejs.osejs.edition.CodeEditor;
import org.colos.ejs.osejs.edition.SearchResult;

class ZenoEditor extends CodeEditor {

  private JLabel zenoActionLabel;
  private JCheckBox zenoStopAfterEffect;

  public ZenoEditor (Osejs _ejs) {
    super (_ejs,null);
    zenoStopAfterEffect = new JCheckBox(res.getString("EquationEditor.Events.ZenoStopAtEffect"),true);
    zenoStopAfterEffect.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent arg0) { changed = true; }
    }); 

    zenoActionLabel = new JLabel(res.getString ("EquationEditor.Events.ZenoAction"));
    zenoActionLabel.setBorder (EquationEditor.BORDER_SMALL);

    JPanel actionRow = new JPanel(new BorderLayout());
    actionRow.add(zenoActionLabel,BorderLayout.WEST);
    actionRow.add(zenoStopAfterEffect,BorderLayout.EAST);

    textComponent.requestFocus();
    mainPanel.add(actionRow, BorderLayout.NORTH);
  }

  public void setColor (Color _color) {
    zenoActionLabel.setForeground(_color);
    zenoStopAfterEffect.setForeground(_color); 
    super.setColor(_color);
  }

  public void setEditable (boolean _editable) {
    zenoStopAfterEffect.setEnabled(_editable);
    super.setEditable(_editable);
  }

  public void setActive (boolean _active) {
    zenoStopAfterEffect.setEnabled(_active);
    super.setActive(_active);
  }

  public StringBuffer saveStringBuffer () {
    StringBuffer buffer = super.saveStringBuffer();
    buffer.append("<StopAfterEffect>"+zenoStopAfterEffect.isSelected()+"</StopAfterEffect>\n");
    return buffer;
  }

  public void readString (String _input) {
    if ("true".equals(OsejsCommon.getPiece(_input,"<StopAfterEffect>","</StopAfterEffect>",false))) zenoStopAfterEffect.setSelected(true);
    else zenoStopAfterEffect.setSelected(false);
    super.readString(_input);
    textComponent.requestFocus();
  }

  public boolean isSelected() { return zenoStopAfterEffect.isSelected(); }
  
  public java.util.List<SearchResult> search (EquationEditor _equationEditor, String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();

    int lineCounter=1,caretPosition=0;
    StringTokenizer t = new StringTokenizer(textComponent.getText(), "\n");
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(_searchString);
      else index = line.indexOf(_searchString);
      if (index>=0) list.add(new ZenoSearchResult(_equationEditor, _info,line.trim(),lineCounter,caretPosition+index));
      caretPosition += line.length();
      lineCounter++;
    }
    return list;
  }


//  public StringBuffer generateCode (int _type, String _info) {
//    if (_type==Editor.GENERATE_PLAINCODE) {
//      StringBuffer zenoCode = new StringBuffer();
//      zenoCode.append("    public boolean zenoEffectAction(org.opensourcephysics.numerics.GeneralStateEvent _event, double[] _state) {\n");
//      zenoCode.append(super.generateCode(Editor.GENERATE_PLAINCODE,_info));
//      zenoCode.append("      return "+zenoStopAfterEffect.isSelected()+";\n");
//      zenoCode.append("    }\n\n");
//      return zenoCode;
//    }
//    return super.generateCode(_type, _info);
//  }

  class ZenoSearchResult extends SearchResult {
    private EquationEditor equationEditor;
    public ZenoSearchResult (EquationEditor _equationEditor, String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation+"."+getName(),aText,textComponent,aLineNumber,aCaretPosition);
      equationEditor = _equationEditor;
    }

    public void show () {
      equationEditor.editZenoEffect (true);
      super.show();
    }
  }

} // end of class
