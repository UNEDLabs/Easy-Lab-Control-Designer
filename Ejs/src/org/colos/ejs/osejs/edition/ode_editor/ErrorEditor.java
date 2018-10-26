/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.colos.ejs.osejs.edition.CodeEditor;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejs.osejs.utils.TwoStrings;
import org.colos.ejss.xml.SimulationXML;
import org.w3c.dom.Element;

/**
 * Creates an editor for code that handles error conditions for ODEs
 * @author Paco
 *
 */
public class ErrorEditor extends CodeEditor {

  static final public String ANY_ERROR = "ANY_ERROR";
  
  private JLabel errorTypeLabel;
  private JComboBox<TwoStrings> errorTypeCombo;

  private EquationEditor equationEditor;

//  private JCheckBox zenoStopAfterEffect;

  public ErrorEditor (Osejs _ejs, TabbedEditor aParentEditor, EquationEditor anEquationEditor) {
    super (_ejs,aParentEditor);
    equationEditor = anEquationEditor;

    errorTypeLabel = new JLabel(res.getString ("EquationEditor.ErrorType"));
    errorTypeLabel.setBorder (EquationEditor.BORDER_SMALL);

    ActionListener changeActionListener = new ActionListener (){
      public void actionPerformed(ActionEvent _evt) { changed = true; }
    };

    errorTypeCombo = new JComboBox<TwoStrings>();
    errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.ANY_ERROR"),ANY_ERROR)); // This must be the first one!
    errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.INTERNAL_SOLVER_ERROR"),"INTERNAL_SOLVER_ERROR"));
    errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.EVENT_NOT_FOUND"),"EVENT_NOT_FOUND"));
    errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.ILLEGAL_EVENT_STATE"),"ILLEGAL_EVENT_STATE"));
//    errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.ZENO_EFFECT"),"ZENO_EFFECT"));
    errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.TOO_MANY_STEPS_ERROR"),"TOO_MANY_STEPS_ERROR"));
    
    if (_ejs.supportsHtml()) {
//      errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.DISCONTINUITY_PRODUCED_ERROR"),"DISCONTINUITY_PRODUCED_ERROR"));
      errorTypeCombo.addItem(new TwoStrings(res.getString("EquationEditor.Error.DID_NOT_CONVERGE"),"DID_NOT_CONVERGE"));
    }
    
    errorTypeCombo.setSelectedIndex(0);
    errorTypeCombo.addActionListener(changeActionListener);

    JPanel typePanel = new JPanel(new BorderLayout());
    typePanel.add(errorTypeLabel,BorderLayout.WEST);
    typePanel.add(errorTypeCombo,BorderLayout.CENTER);

    JPanel typeRow = new JPanel(new BorderLayout());
    typeRow.add(typePanel,BorderLayout.WEST);

    mainPanel.add(typeRow, BorderLayout.NORTH);
    textComponent.requestFocus();
  }
  
  public String getErrorType() {
    TwoStrings ts = (TwoStrings) errorTypeCombo.getSelectedItem();
    return ts.getSecondString(); 
  }

  public void clear() {
    errorTypeCombo.setSelectedIndex(0);
    super.clear();
  }
  
  public void setColor (Color _color) {
    errorTypeLabel.setForeground(_color);
    super.setColor(_color);
  }

  public void setEditable (boolean _editable) {
    errorTypeCombo.setEnabled(_editable);
    super.setEditable(_editable);
  }

  public void setActive (boolean _active) {
    errorTypeCombo.setEnabled(_active);
    super.setActive(_active);
  }

  public void fillSimulationXML(SimulationXML _simXML, Element _ode) {
    _simXML.addODEErrorHandler(_ode,getName(),getErrorType(),textComponent.getText(),commentField.getText(), isActive());
  }

  public StringBuffer saveStringBuffer () {
    StringBuffer buffer = super.saveStringBuffer();
    buffer.append("<ErrorType>"+getErrorType()+"</ErrorType>\n");
    return buffer;
  }

  public void readString (String _input) {
    String txt = OsejsCommon.getPiece(_input,"<ErrorType>","</ErrorType>",false);
    if (txt==null) errorTypeCombo.setSelectedIndex(0);
    else {
      for (int i=0,n=errorTypeCombo.getItemCount(); i<n; i++) {
        TwoStrings ts = (TwoStrings) errorTypeCombo.getItemAt(i);
        if (txt.equals(ts.getSecondString())) { errorTypeCombo.setSelectedIndex(i); break; }
      }
    }
    super.readString(_input);
    textComponent.requestFocus();
  }

  public StringBuffer generateCode (int _type, String _info) {
    if (!isActive()) return new StringBuffer();
    if (_type==Editor.GENERATE_PLAINCODE) {
      StringBuffer errorCode = new StringBuffer();
      String code = textComponent.getText().trim();
      if (code.length()<=0) return errorCode;
      if (errorTypeCombo.getSelectedIndex()!=0) {
        TwoStrings ts = (TwoStrings) errorTypeCombo.getSelectedItem();
        errorCode.append("      if (__eventSolver.getErrorCode()==org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR."+ts.getSecondString()+") { // "+getCommentField().getText()+"\n");
      }
      else errorCode.append("      { // For any error: "+getCommentField().getText()+"\n");
      errorCode.append(super.generateCode(Editor.GENERATE_PLAINCODE,_info));
      errorCode.append("      }\n\n");
      return errorCode;
    }
    return super.generateCode(_type, _info);
  }

  
  // --- private methods and classes

  @Override
  protected SearchResult createSearchResult(String _info, String _line, int _lineCounter, int _pos) {
    return new ErrorSearchResult(_info,_line,_lineCounter,_pos);
  }

  class ErrorSearchResult extends SearchResult {
    public ErrorSearchResult (String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation+"."+getName(),aText,textComponent,aLineNumber,aCaretPosition);
    }

    public void show () {
      equationEditor.showErrorTab();
      parentTabbedEditor.showPage(ErrorEditor.this);
      super.show();
    }
  }

  
} // end of class
