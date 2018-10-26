/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.drawing2d.ControlElement2D;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.swing.Knob;

/**
 * A slider to display double values. When the value is changing it
 * invokes all VARIABLE_CHANGED actions. When the value is finally set,
 * it invokes all ACTION actions.
 */
public class ControlKnob extends ControlSwingElement implements ActionListener {
  static protected final int KNOB_ADDED = 8;
  static private final int VARIABLE  = 0;

  protected Knob knob;
  private DoubleValue internalValue;
  
// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    knob = new Knob();
    internalValue = new DoubleValue(knob.getValue());
    knob.addMouseListener  (new MyMouseListener());
    knob.addActionListener(this);
    return knob;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("variable");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("minimumAngle");
      infoList.add ("maximumAngle");
      infoList.add ("pressAction");
      infoList.add ("dragAction");
      infoList.add ("releaseAction");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "int|double";
    if (_property.equals("minimum"))        return "int|double";
    if (_property.equals("maximum"))        return "int|double";
    if (_property.equals("minimumAngle"))   return "int|double";
    if (_property.equals("maximumAngle"))   return "int|double";
    if (_property.equals("pressAction"))    return "Action CONSTANT";
    if (_property.equals("dragAction"))     return "Action CONSTANT";
    if (_property.equals("releaseAction"))  return "Action CONSTANT";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case VARIABLE : 
        knob.setValue (internalValue.value = _value.getDouble()); 
        break;
      case 1 : knob.setMinValue (_value.getDouble()); break;
      case 2 : knob.setMaxValue (_value.getDouble()); break;
      case 3 : 
        if (_value instanceof IntegerValue)  knob.setMinAngle (_value.getInteger()*ControlElement2D.TO_RADIANS); 
        else knob.setMinAngle (_value.getDouble()); 
        break;
      case 4 : 
        if (_value instanceof IntegerValue)  knob.setMaxAngle (_value.getInteger()*ControlElement2D.TO_RADIANS); 
        else knob.setMaxAngle (_value.getDouble()); 
        break;
      case 5 : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressAction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 6 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragAction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 7 : // releaseaction
        removeAction (ControlElement.ACTION,getProperty("releaseAction"));
        addAction(ControlElement.ACTION,_value.getString());
        break;

      default: super.setValue(_index-KNOB_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : break;
      case 1 : knob.setMinValue (-1); break;
      case 2 : knob.setMaxValue (1); break;
      case 3 : knob.setMinAngle (-Math.PI*0.4); break;
      case 4 : knob.setMaxAngle (Math.PI*0.4); break;
      case 5 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressAction")); break;
      case 6 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragAction")); break;
      case 7 : removeAction (ControlElement.ACTION,getProperty("releaseAction")); break;
      default: super.setDefaultValue(_index-KNOB_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "-1";
      case 2 : return "1";
      case 3 : return "-PI*0.4";
      case 4 : return "PI*0.4";
      case 5 : case 6 : case 7 : return "<no_action>";
      default : return super.getDefaultValueString(_index-KNOB_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case 1 :  case 2 :  case 3 :  case 4 : case 5 :
      case 6 :  case 7 : 
        return null;
      default: return super.getValue(_index-KNOB_ADDED);
    }
  }

// -------------------------------------
// Inner classes
// -------------------------------------

  
  public void actionPerformed (ActionEvent _evt) { // The knob changed its value
    internalValue.value = knob.getValue();
    variableChanged (VARIABLE,internalValue);
    if (isUnderEjs) setFieldListValue(VARIABLE, internalValue);
  }
  
  private class MyMouseListener extends java.awt.event.MouseAdapter {
    
    public void mousePressed (java.awt.event.MouseEvent evt) {
      if (knob.isEnabled()) invokeActions (ControlSwingElement.ACTION_PRESS);
    }

    public void mouseReleased (java.awt.event.MouseEvent evt) {
      if (knob.isEnabled()) invokeActions (ControlElement.ACTION);
    }
    
  }


} // End of class
