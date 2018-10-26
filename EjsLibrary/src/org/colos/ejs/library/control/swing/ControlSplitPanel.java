/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import javax.swing.JSplitPane;

/**
 * A configurable SplitPanel
 */
public class ControlSplitPanel extends ControlContainer {
  protected JSplitPane splitpanel;
  private boolean hasOne=false;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
      splitpanel = new JSplitPane ();
      splitpanel.setOneTouchExpandable(true);
      splitpanel.setDividerLocation(-1);
    return splitpanel;
  }

  public void reset() { splitpanel.setDividerLocation(-1); }

  public void add(ControlElement _child) {
    if (hasOne) {
      splitpanel.setBottomComponent(_child.getComponent());
      splitpanel.setDividerLocation(-1);
    }
    else {
      splitpanel.setTopComponent(_child.getComponent());
      splitpanel.setDividerLocation(-1);
      hasOne = true;
    }
    if (_child instanceof RadioButtonInterface) {
      radioButtons.add((ControlSwingElement)_child);
      ((RadioButtonInterface)_child).setControlParent(this);
    }
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("orientation");
      infoList.add ("expandable");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("orientation"))    return "Orientation|int";
    if (_property.equals("expandable"))     return "boolean";
    return super.getPropertyInfo(_property);
  }
  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("Orientation")>=0) {
      _value = _value.trim().toLowerCase();
      if      (_value.equals("vertical"))   return new IntegerValue(JSplitPane.VERTICAL_SPLIT);
      else if (_value.equals("horizontal")) return new IntegerValue(JSplitPane.HORIZONTAL_SPLIT);
    }
    return super.parseConstant(_propertyType, _value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if (splitpanel.getOrientation()!=_value.getInteger()) splitpanel.setOrientation(_value.getInteger());
        break;
      case 1 : splitpanel.setOneTouchExpandable(_value.getBoolean()); break;
      default: super.setValue(_index-2,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : splitpanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT); break;
      case 1 : splitpanel.setOneTouchExpandable(true); break;
      default: super.setDefaultValue(_index-2); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "HORIZONTAL";
      case 1 : return "true";
      default : return super.getDefaultValueString(_index-2);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 :
      case 1 :
        return null;
      default: return super.getValue(_index-2);
    }
  }

} // End of class


