/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import java.awt.Color;
import javax.swing.JPasswordField;

/**
 * A password field to enter passwords. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlPasswordField extends ControlSwingElement {
  static protected final int PASSWORD_ADDED = 4;
  static protected final int VARIABLE = 0;
  static protected final int PASSWORDFIELD_BACKGROUND  = ControlSwingElement.BACKGROUND+PASSWORD_ADDED;

  protected JPasswordField textfield;
  protected StringValue internalValue;
  private Color defaultColor, editingColor;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    textfield = new JPasswordField();
    textfield.setText ("");
    textfield.addActionListener (new MyActionListener());
    textfield.addKeyListener    (new MyKeyListener());
    internalValue = new StringValue(new String(textfield.getPassword()));
    decideColors (textfield.getBackground());
    return textfield;
  }

  private void setTheValue (String _value) {
    if (internalValue.value!=null && internalValue.value.equals(_value)) return;
    textfield.setText (internalValue.value = _value);
    textfield.setCaretPosition(Math.max(_value.length()-1,0));
    setColor (defaultColor);
  }

  protected void setInternalValue (String _value) {
    internalValue.value = _value;
    variableChanged (VARIABLE,internalValue);
    invokeActions ();
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("variable");
      infoList.add ("echoCharacter");
      infoList.add ("editable");
      infoList.add ("action");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "String VARIABLE_EXPECTED";
    if (_property.equals("echoCharacter"))  return "String CONSTANT";
    if (_property.equals("editable"))       return "boolean";
    if (_property.equals("action"))         return "Action CONSTANT";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case VARIABLE : setTheValue(_value.getString()); break;
      case 1 : textfield.setEchoChar(_value.getString().charAt(0)); break;
      case 2 : textfield.setEditable(_value.getBoolean()); break;
      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      default: super.setValue(_index-PASSWORD_ADDED,_value); break;
      case PASSWORDFIELD_BACKGROUND :
        super.setValue (ControlSwingElement.BACKGROUND,_value);
        decideColors (getVisual().getBackground());
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : setTheValue(""); break;
      case 1 : textfield.setEchoChar('*'); break;
      case 2 : textfield.setEditable(true); break;
      case 3 : removeAction (ControlElement.ACTION,getProperty("action")); break;
      default: super.setDefaultValue(_index-PASSWORD_ADDED); break;
      case PASSWORDFIELD_BACKGROUND :
        super.setDefaultValue (ControlSwingElement.BACKGROUND);
        decideColors (getVisual().getBackground());
        break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case VARIABLE : return "<none>"; 
      case 1 : return "*"; 
      case 2 : return "true";
      case 3 : return "<no_action>";
      default : return super.getDefaultValueString(_index-PASSWORD_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case 1 : case 2 : case 3 :
        return null;
      default: return super.getValue(_index-PASSWORD_ADDED);
    }
  }

// -------------------------------------
// Private methods and inner classes
// -------------------------------------

  private void setColor (Color aColor) {
    if (textfield.isEditable()) getVisual().setBackground (aColor);
  }

  private void decideColors (Color aColor) {
    if (aColor==null) return;
    defaultColor = aColor;
    if (defaultColor.equals(Color.yellow)) editingColor = Color.orange;
    else editingColor = Color.yellow;
  }

  private void acceptValue () {
    setInternalValue (new String(textfield.getPassword()));
    if (isUnderEjs) setFieldListValue(VARIABLE, internalValue);
    setColor (defaultColor);
  }

  private class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed (java.awt.event.ActionEvent _e) { if (textfield.isEditable()) acceptValue();  }
  }

  private class MyKeyListener implements java.awt.event.KeyListener {
    public void keyPressed  (java.awt.event.KeyEvent _e) { processKeyEvent (_e,0); }
    public void keyReleased (java.awt.event.KeyEvent _e) { processKeyEvent (_e,1); }
    public void keyTyped    (java.awt.event.KeyEvent _e) { processKeyEvent (_e,2); }
    private void processKeyEvent (java.awt.event.KeyEvent _e, int _n) {
      if (!textfield.isEditable()) return;
//      if (_e.getKeyChar()=='\t') { acceptValue(); return; }
      if (_e.getKeyChar()!='\n') setColor (editingColor);
      if (_e.getKeyCode()==27)   { textfield.setText (internalValue.value); setColor (defaultColor); }
    }
  }

} // End of class
