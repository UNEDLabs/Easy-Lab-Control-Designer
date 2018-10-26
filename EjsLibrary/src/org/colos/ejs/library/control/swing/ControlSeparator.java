/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * A configurable Label. It has no internal value, nor can trigger
 * any action.
 */
public class ControlSeparator extends ControlSwingElement {
  protected JSeparator sep;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    return sep = new JSeparator ();
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("orientation");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("orientation"))    return "Orientation|int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : sep.setOrientation(_value.getInteger()); break;
      default: super.setValue(_index-1,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : sep.setOrientation(SwingConstants.HORIZONTAL); break;
      default: super.setDefaultValue(_index-1); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "HORIZONTAL";
      default : return super.getDefaultValueString(_index-1);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 :
        return null;
      default: return super.getValue(_index-1);
    }
  }

} // End of class
