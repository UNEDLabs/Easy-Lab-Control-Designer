/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.border.EmptyBorder;

/**
 * A configurable Menubar
 */
public class ControlMenuBar extends ControlContainer {
  protected JMenuBar menubar;
  private java.awt.Rectangle     myBorder=null;

  protected java.awt.Component createVisual () {
    return menubar = new JMenuBar();
  }

  public boolean acceptsChild (ControlElement _child) {
    if (_child.getVisual() instanceof javax.swing.JMenu) return true;
    return false;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("borderPainted");
      infoList.add ("border");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("borderPainted"))  return "boolean";
    if (_property.equals("border"))         return "Margins|Object";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : menubar.setBorderPainted(_value.getBoolean()); break;
      case 1 : // border
        if (_value.getObject() instanceof java.awt.Rectangle) {
          java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
          if (rect!=myBorder) {
            menubar.setBorder(new EmptyBorder(rect.x,rect.y,rect.width,rect.height));
            myBorder = rect;
          }
        }
        break;
      default: super.setValue(_index-2,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : menubar.setBorderPainted(true); break;
      case 1 : menubar.setBorder(null); myBorder = null; break;
      default: super.setDefaultValue(_index-2); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "true";
      case 1 : return "<none>";
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
// Own methods
// ------------------------------------------------

 /**
  * adds a child control
  * @param _child the child control
  */
  public void add(ControlElement _child) {
    if (! (_child instanceof ControlMenu)) return;
    children.add(_child);
    menubar.add ((JMenu) _child.getVisual());
    if (getControlWindow()!=null) getControlWindow().adjustSize();
    // Now propagate my own font, foreground and background;
    propagateProperty (_child,"font"      ,getPropagatedProperty("font"));
    propagateProperty (_child,"foreground",getPropagatedProperty("foreground"));
    propagateProperty (_child,"background",getPropagatedProperty("background"));
  }

 /**
  * removes a child control
  * @param _child the child control
  */
  public void remove(ControlElement _child) {
    if (! (_child instanceof ControlMenu)) return;
    children.remove(_child);
    menubar.remove(_child.getVisual());
    if (getControlWindow()!=null) getControlWindow().adjustSize();
  }

} // End of class
