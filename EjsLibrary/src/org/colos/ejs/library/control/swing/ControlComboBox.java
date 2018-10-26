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
import java.util.StringTokenizer;
import javax.swing.JComboBox;

/**
 * A combobox to display string options. When the selected option changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlComboBox extends ControlSwingElement {
  static private final int COMBOBOX_ADDED  = 6;
  static private final int VARIABLE  = 0;
  static private final int VALUE  = 1;
//  static private final int BACKGROUND  = ControlSwingElement.BACKGROUND+COMBOBOX_ADDED;
  static private final int COMBO_FOREGROUND1  = ControlSwingElement.FOREGROUND+COMBOBOX_ADDED;

  protected JComboBox combo;
  private java.awt.Component editorComponent;
  private String optionsString;
  private StringValue internalValue;
  private boolean defaultValueSet, defaultEditable,doNotUpdate=false;
  private String defaultValue;
  private Color defaultColor, editingColor;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    combo = new JComboBox();
    defaultEditable = combo.isEditable();
    combo.addActionListener (new MyActionListener());
    editorComponent = combo.getEditor().getEditorComponent();
    editorComponent.addKeyListener    (new MyKeyListener());
    defaultValue  = "";
    defaultValueSet = false;
    internalValue = new StringValue(defaultValue);
    decideColors (editorComponent.getBackground());
    return combo;
  }

  public void reset() {
    if (defaultValueSet) {
      setTheValue (defaultValue);
      internalValue.value = defaultValue;
      variableChanged (VARIABLE,internalValue);
      invokeActions ();
    }
  }

  private void setTheValue (final String _value) {
    if (internalValue.value!=null && internalValue.value.equals(_value)) return;
    doNotUpdate = true;
    combo.setSelectedItem (internalValue.value = _value);
    setColor (defaultColor);
    variableChanged (VARIABLE,internalValue);
    invokeActions ();
    doNotUpdate = false;
  }

  private void setTheOptions(String _options)  {
    if (_options==null) {
      if (optionsString!=null) {
        combo.removeAllItems();
        optionsString = null;
      }
      return;
    }
    if (_options.equals(optionsString)) return;
    doNotUpdate = true;
    combo.removeAllItems();
    StringTokenizer tkn = new StringTokenizer (_options,";");
    while (tkn.hasMoreTokens()) combo.addItem(org.opensourcephysics.display.TeXParser.parseTeX(tkn.nextToken()));
    optionsString = _options;
    doNotUpdate = false;
    //if (combo.getItemCount()>0) setTheValue (combo.getItemAt(0).toString());
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
      infoList.add ("editBackground");
      infoList.add ("action");
      infoList.add ("options");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("options"))        return "String PREVIOUS TRANSLATABLE";
    if (_property.equals("variable"))       return "String VARIABLE_EXPECTED";
    if (_property.equals("value"))          return "String CONSTANT TRANSLATABLE";
    if (_property.equals("editable"))       return "boolean";
    if (_property.equals("editBackground")) return "Color|Object";
    if (_property.equals("action"))         return "Action CONSTANT";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
//     System.err.println (getProperty("name")+" setting value of "+_index+ " to "+_value.toString());
    switch (_index) {
      case VARIABLE : setTheValue(_value.getString()); break;
      case VALUE :
        defaultValueSet = true; defaultValue = _value.getString();
        setActive (false); reset (); setActive(true);
        break;
      case 2 : combo.setEditable(_value.getBoolean()); break;
      case 3 :
        if (_value.getObject() instanceof Color) editorComponent.setBackground((Color) _value.getObject());
        decideColors (editorComponent.getBackground());
        break;
      case 4 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 5 : setTheOptions(_value.getString()); break;
      default: super.setValue(_index-COMBOBOX_ADDED,_value); break;
      case COMBO_FOREGROUND1 :
        super.setValue (ControlSwingElement.FOREGROUND,_value);
        if (_value.getObject() instanceof Color) editorComponent.setForeground((Color) _value.getObject());
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : break; // Do nothing
      case VALUE : defaultValueSet = false; break;
      case 2 : combo.setEditable(defaultEditable); break;
      case 3 :
        editorComponent.setBackground(Color.white);
        decideColors (editorComponent.getBackground());
        break;
      case 4 : removeAction (ControlElement.ACTION,getProperty("action")); break;
      case 5 : setTheOptions(null); break;
      default: super.setDefaultValue(_index-COMBOBOX_ADDED); break;
      case COMBO_FOREGROUND1 :
        super.setDefaultValue (ControlSwingElement.FOREGROUND);
        editorComponent.setForeground(Color.black);
        break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case VALUE : case 2 : case 3 : case 4 : case 5 :
        return null;
      default: return super.getValue(_index-COMBOBOX_ADDED);
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case VARIABLE : return "<none>";
      case VALUE : return "<none>";
      case 2 : return ""+defaultEditable;
      case 3 : return "WHITE";
      case 4 : return "<no_action>";
      case 5 : return "<none>";
      default : return super.getDefaultValueString(_index-COMBOBOX_ADDED);
      case COMBO_FOREGROUND1 : return super.getDefaultValueString(ControlSwingElement.FOREGROUND);
    }
  }
// -------------------------------------
// Private methods and inner classes
// -------------------------------------

  private void setColor (Color aColor) {
    if (combo.isEditable()) editorComponent.setBackground (aColor);
  }

  private void decideColors (Color aColor) {
    if (aColor==null) return;
    defaultColor = aColor;
    if (defaultColor.equals(Color.yellow)) editingColor = Color.orange;
    else editingColor = Color.yellow;
//    if (defaultColor.equals(Color.red)) errorColor = Color.magenta;
//    else errorColor = Color.red;
  }

  private void acceptValue () {
    if (doNotUpdate) return;
    internalValue.value = (String) combo.getSelectedItem();
    variableChanged (VARIABLE,internalValue);
    invokeActions ();
    if (isUnderEjs && internalValue.value!=null) setFieldListValueWithAlternative(VARIABLE, VALUE,internalValue);
    setColor (defaultColor);
  }

  private class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed (java.awt.event.ActionEvent _e) { acceptValue(); }
  }

  private class MyKeyListener implements java.awt.event.KeyListener {
    public void keyPressed  (java.awt.event.KeyEvent _e) { processKeyEvent (_e,0); }
    public void keyReleased (java.awt.event.KeyEvent _e) { processKeyEvent (_e,1); }
    public void keyTyped    (java.awt.event.KeyEvent _e) { processKeyEvent (_e,2); }
    private void processKeyEvent (java.awt.event.KeyEvent _e, int _n) {
      if (!combo.isEditable()) return;
//      if (_e.getKeyChar()=='\t') { acceptValue(); return; }
      if (_e.getKeyChar()!='\n') setColor (editingColor);
      if (_e.getKeyCode()==27)   setValue (VARIABLE,internalValue);
    }
  }


} // End of class
