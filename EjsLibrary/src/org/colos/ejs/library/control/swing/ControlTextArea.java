/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

//import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import javax.swing.border.*;
import javax.swing.JTextArea;

/**
 * A configurable text. It has no internal value, nor can trigger
 * any action.
 */
public class ControlTextArea extends ControlSwingElement {
  static String _RETURN_ = "\n";

  private JTextArea textarea;
  private javax.swing.JScrollPane panel;
  private TitledBorder titledBorder;
  private EtchedBorder etchedBorder;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  static {
    try { _RETURN_ = System.getProperty("line.separator"); }
    catch (Exception e) { _RETURN_ = "\n"; }
  }

  protected java.awt.Component createVisual () {
      textarea = new JTextArea(5, 5);
      textarea.setEditable(true);
    panel = new javax.swing.JScrollPane(textarea);
    etchedBorder = new EtchedBorder(EtchedBorder.LOWERED);
    titledBorder = new TitledBorder (etchedBorder,"");
    titledBorder.setTitleJustification (TitledBorder.CENTER);
    panel.setBorder (etchedBorder);
    return textarea;
  }

  public String getObjectClassname () { return "javax.swing.JTextArea"; }

  public Object getObject () { return textarea; }

  public java.awt.Component getComponent () { return panel; }

  public void reset () { textarea.setText(""); }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("title");
      infoList.add ("editable");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("title"))          return "String TRANSLATABLE";
    if (_property.equals("editable"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : // title
        if (!_value.getString().equals(titledBorder.getTitle())) {
          titledBorder.setTitle (org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()));
          panel.setBorder (titledBorder);
          panel.repaint();
        }
        break;
      case 1 : // editable
        textarea.setEditable (_value.getBoolean());
        break;
      default: super.setValue(_index-2,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : // title
        panel.setBorder (etchedBorder);
        panel.repaint();
        break;
      case 1 : // editable
        textarea.setEditable (true);
        break;
      default: super.setDefaultValue(_index-2); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "true";
      default : return super.getDefaultValueString(_index-2);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 :
        return null;
      default: return super.getValue(_index-2);
    }
  }

// ------------------------------------------------
// Output
// ------------------------------------------------

  public void clear () {
    textarea.setText("");
    textarea.setCaretPosition (textarea.getText().length());
  }

  public void println (String s) { print (s+_RETURN_); }

  public void print (String s) {
    textarea.append(s);
    textarea.setCaretPosition (textarea.getText().length());
  }

} // End of class
