/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import  java.awt.Color;
import javax.swing.JTextField;

/**
 * A textfield to display String values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlTextField extends ControlSwingElement {
  static final int TEXTFIELD_ADDED = 5;
  static protected final int VARIABLE  = 0;
  static protected final int VALUE  = 1;
  static private final int FIELD_BACKGROUND  = ControlSwingElement.BACKGROUND+TEXTFIELD_ADDED;
  static private final int FIELD_FOREGROUND  = ControlSwingElement.FOREGROUND+TEXTFIELD_ADDED;

  protected boolean inputError = false;
  protected JTextField textfield;
  protected StringValue internalValue;
  protected boolean defaultValueSet;
  protected String defaultValue;
  protected boolean foregroundSet;
  protected Color defaultColor, editingColor, errorColor;
  protected int defaultColumns;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    textfield = new JTextField();
    textfield.setText ("");
    defaultValue = textfield.getText();
    defaultColumns = textfield.getColumns();
    textfield.addActionListener (new MyActionListener());
    textfield.addKeyListener    (new MyKeyListener());
    defaultValueSet = false;
    internalValue = new StringValue(defaultValue);
    textfield.setBackground(Color.WHITE);
    textfield.setForeground(Color.BLACK);
    foregroundSet = false;
    decideColors (textfield.getBackground());
    return textfield;
  }

  protected int getVariableIndex () { return ControlTextField.VARIABLE; }
  protected int getValueIndex () { return ControlTextField.VALUE; }

  public void reset() {
    if (defaultValueSet) {
      setTheValue (defaultValue);
      setInternalValue (defaultValue);
    }
  }

  private void setTheValue (String _value) {
    if (internalValue.value!=null && internalValue.value.equals(_value)) return;
    textfield.setText (internalValue.value = _value);
    if (inputError) setColor (errorColor);
    else setColor (defaultColor);
  }

  protected void setInternalValue (String _value) {
    internalValue.value = _value;
    variableChanged (getVariableIndex(),internalValue);
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
      infoList.add ("value");
      infoList.add ("editable");
      infoList.add ("action");
      infoList.add ("columns");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "String VARIABLE_EXPECTED";
    if (_property.equals("value"))          return "String CONSTANT TRANSLATABLE DEPRECATED";
    if (_property.equals("editable"))       return "boolean";
    if (_property.equals("action"))         return "Action CONSTANT";
    if (_property.equals("columns"))        return "int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case VARIABLE : setTheValue(_value.getString()); break;
      case VALUE :
        defaultValueSet = true; defaultValue = _value.getString();
        setActive (false); reset (); setActive(true);
        break;
      case 2 : 
        if (_value.getBoolean()) {
          textfield.setEditable(true);
          if (!foregroundSet) textfield.setForeground(Color.BLACK);
        }
        else {
          textfield.setEditable(false);
          if (!foregroundSet) textfield.setForeground(Color.GRAY);
        }
//        setColor(defaultColor);
        break;
      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 4 : 
        if (_value.getInteger()!=textfield.getColumns()) {
          textfield.setColumns(_value.getInteger());
          if (textfield.getParent()!=null) textfield.getParent().validate();
        }
        break;

      default: super.setValue(_index-TEXTFIELD_ADDED,_value); break;
      case FIELD_BACKGROUND :
        super.setValue (ControlSwingElement.BACKGROUND,_value);
        decideColors (getVisual().getBackground());
        setColor(defaultColor);
        break;
      case FIELD_FOREGROUND :
        super.setValue (ControlSwingElement.FOREGROUND,_value);
        foregroundSet = true;
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : break; // Do nothing
      case VALUE : defaultValueSet = false; break;
      case 2 : 
        textfield.setEditable(true); 
        if (!foregroundSet) textfield.setForeground(Color.BLACK);
//        setColor(defaultColor);
        break;
      case 3 : removeAction (ControlElement.ACTION,getProperty("action")); break;
      case 4 : 
        textfield.setColumns(defaultColumns); 
        if (textfield.getParent()!=null) textfield.getParent().validate();
        break;
      default: super.setDefaultValue(_index-TEXTFIELD_ADDED); break;
      case FIELD_BACKGROUND :
        super.setDefaultValue (ControlSwingElement.BACKGROUND);
        decideColors (getVisual().getBackground());
        setColor(defaultColor);
        break;
      case FIELD_FOREGROUND :
        super.setDefaultValue (ControlSwingElement.FOREGROUND);
        foregroundSet = false;
        break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case VARIABLE : case VALUE : return "<none>"; 
      case 2 : return "true";
      case 3 : return "<no_action>";
      case 4 : return Integer.toString(defaultColumns); 
      default : return super.getDefaultValueString(_index-TEXTFIELD_ADDED);
      case FIELD_BACKGROUND : return "WHITE";
      case FIELD_FOREGROUND : return "BLACK/GRAY";
    }
  }
  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case 1 : case 2 : case 3 : case 4 :
        return null;
      default: return super.getValue(_index-TEXTFIELD_ADDED);
    }
  }

// -------------------------------------
// Private methods and inner classes
// -------------------------------------

  protected final void setColor (Color aColor) {
    textfield.setBackground (aColor);
//    if (textfield.isEditable()) textfield.setBackground (aColor);
//    else textfield.setBackground(noneditableColor);
  }

  protected void decideColors (Color aColor) {
    if (aColor==null) return;
    defaultColor = aColor;
    if (defaultColor.equals(Color.yellow)) editingColor = Color.orange;
    else editingColor = Color.yellow;
    if (defaultColor.equals(Color.red)) errorColor = Color.magenta;
    else errorColor = Color.red;
//    if (defaultColor.equals(Color.LIGHT_GRAY)) noneditableColor = Color.GRAY;
//    else noneditableColor = Color.LIGHT_GRAY;
  }

  private void acceptValue () {
    setInternalValue (textfield.getText());
    if (isUnderEjs) setFieldListValueWithAlternative(getVariableIndex(), getValueIndex(),internalValue);
    if (inputError) setColor (errorColor);
    else setColor (defaultColor);
  }

  protected class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed (java.awt.event.ActionEvent _e) { if (textfield.isEditable()) acceptValue(); }
  }

  protected class MyKeyListener implements java.awt.event.KeyListener {
    public void keyPressed  (java.awt.event.KeyEvent _e) { processKeyEvent (_e,0); }
    public void keyReleased (java.awt.event.KeyEvent _e) { processKeyEvent (_e,1); }
    public void keyTyped    (java.awt.event.KeyEvent _e) { processKeyEvent (_e,2); }
    private void processKeyEvent (java.awt.event.KeyEvent _e, int _n) {
      if (!textfield.isEditable()) return;
//      System.out.println("Keyword is "+_e.getKeyCode());
//      if (_e.getKeyChar()=='\t') { System.out.println("accepting value "); acceptValue(); return; }
      if (_e.getKeyChar()!='\n') setColor (editingColor);
      if (_e.getKeyCode()==27)   { textfield.setText (internalValue.value); setColor (defaultColor); }
    }
  }

} // End of class
