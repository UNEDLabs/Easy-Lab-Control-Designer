/*
 * The contrib.control package contains non Open Source subclasses of
 * control.ControlElement
 * Copyright (c) August 2002 Fu-Kwun Hwang
 * @author Fu-Kwun Hwang
 */

package org.colos.ejs.library.control.swing;

import java.awt.Color;
import org.colos.ejs.library.control.value.*;

/**
 * Use cHotEqn to display mathematics equation as TeX syntax string values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlEquation extends ControlSwingElement {
  static private final int VARIABLE  = 0;
//  static private final int BACKGROUND  = ControlSwingElement.BACKGROUND+4;

  protected atp.cHotEqn equation;
  private StringValue internalValue;
  private boolean defaultValueSet;
  private String defaultValue;
  private Color defaultColor; //, editingColor, errorColor;
///  private StringValue selectedValue;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    if (equation==null) equation = new atp.cHotEqn ("");
    equation.setDebug(false);
    equation.setEditable(false);
    equation.setEquation ("");
// cHotEqn default action is write out selected area of equation to the console
//    equation.addActionListener (new MyActionListener());
//    equation.addKeyListener    (new MyKeyListener());
    defaultValue  = "";
    defaultValueSet = false;
    internalValue = new StringValue(defaultValue);
///    selectedValue=new StringValue("");
    decideColors (equation.getBackground());
    return equation;
  }

  public void reset() {
    if (defaultValueSet) {
      setTheValue (defaultValue);
      setInternalValue (defaultValue);
///      selectedValue.value="";
    }
  }

  private void setTheValue (String _value) {
//    System.out.println ("Setting eq to "+_value);
    if (internalValue.value.equals(_value)) return;
    equation.setEquation (internalValue.value = _value);
    setColor (defaultColor);
  }

  private void setInternalValue (String _value) {
    internalValue.value = _value;
    variableChanged (VARIABLE,internalValue);
    invokeActions ();
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;
  static private int nparam=3;
  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("variable");
      infoList.add ("value");
      infoList.add ("editable");
///      infoList.add ("action");
//      infoList.add ("selected");
/*      infoList.add("size");
      infoList.add("foreground");
      infoList.add("background");
      infoList.add("font");
      infoList.add("tooltip");
*/
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "String VARIABLE_EXPECTED TRANSLATABLE";
    if (_property.equals("value"))          return "String CONSTANT TRANSLATABLE";
    if (_property.equals("editable"))       return "boolean";
//    if (_property.equals("action"))         return "Action CONSTANT";
//    if (_property.equals("selected"))       return "String VARIABLE_EXPECTED";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case VARIABLE : setTheValue(_value.getString()); break;
      case 1 :
        defaultValueSet = true; defaultValue = _value.getString();
        setActive(false); reset(); setActive(true);
        break;
      case 2 : equation.setEditable(_value.getBoolean()); break;
/*      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
*/
//      case 4 :  break; // diaplay only selected TeX string
/*      case BACKGROUND :
        super.setValue (ControlSwingElement.BACKGROUND,_value);
        decideColors (getVisual().getBackground());
        break;*/
      default: super.setValue(_index-nparam,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : break; // Do nothing
      case 1 : defaultValueSet = false; break;
      case 2 : equation.setEditable(true); break;
///      case 3 : removeAction (ControlElement.ACTION,getProperty("action")); break;
//      case 4 : break; // Do nothing
/*      case BACKGROUND :
        super.setDefaultValue (ControlSwingElement.BACKGROUND);
        decideColors (getVisual().getBackground());
        break;*/
      default: super.setDefaultValue(_index-nparam); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case VARIABLE : return "<none>";
      case 1 : return "false";
      case 2 : return "true";
      default : return super.getDefaultValueString(_index-nparam);
    }
  }
  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case 1 : case 2 :/// case 3 :// case 4:
        return null;
      default: return super.getValue(_index-nparam);
    }
  }

// -------------------------------------
// Private methods and inner classes
// -------------------------------------

  private void setColor (Color aColor) {
    if (equation.isEditable()) getVisual().setBackground (aColor);
  }

  private void decideColors (Color aColor) {
    if (aColor==null) return;
    defaultColor = aColor;
//    if (defaultColor.equals(Color.yellow)) editingColor = Color.orange;
//    else editingColor = Color.yellow;
//    if (defaultColor.equals(Color.red)) errorColor = Color.magenta;
//    else errorColor = Color.red;
  }

} // End of class
