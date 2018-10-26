/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A configurable panel. It has no internal value, nor can trigger
 * any action.
 */
public class ControlToolBar extends ControlContainer {
  static protected int TOOLBAR_ADDED = 6;

  protected JToolBar bar;
  private java.awt.LayoutManager myLayout=null, defLayout;
  private java.awt.Rectangle     myBorder=null;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    bar = new JToolBar();
    defLayout = bar.getLayout();
    return bar;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("borderPainted");
      infoList.add ("floatable");
      infoList.add ("layout");
      infoList.add ("border");
      infoList.add ("orientation");
      infoList.add ("rollOver");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("borderPainted"))  return "boolean";
    if (_property.equals("floatable"))      return "boolean";
    if (_property.equals("layout"))         return "Layout|Object NO_RESET";
    if (_property.equals("border"))         return "Margins|Object";
    if (_property.equals("orientation"))    return "Orientation|int";
    if (_property.equals("rollOver"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : bar.setBorderPainted(_value.getBoolean()); break;
      case 1 : bar.setFloatable(_value.getBoolean()); break;
      case 2 : // layout
        if (_value.getObject() instanceof java.awt.LayoutManager) {
          java.awt.LayoutManager layout = (java.awt.LayoutManager) _value.getObject();
          if (layout!=myLayout) {
            bar.setLayout(myLayout=layout);
            this.adjustChildren();
            bar.validate();
          }
        }
        break;
      case 3 : // border
        if (_value.getObject() instanceof java.awt.Rectangle) {
          java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
          if (rect!=myBorder) {
            bar.setBorder(new EmptyBorder(rect.x,rect.y,rect.width,rect.height));
            myBorder = rect;
          }
        }
        break;
      case 4 : if (bar.getOrientation()!=_value.getInteger()) bar.setOrientation(_value.getInteger()); break;
      case 5 : bar.setRollover(_value.getBoolean()); break;
      default: super.setValue(_index-TOOLBAR_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : bar.setBorderPainted(true); break;
      case 1 : bar.setFloatable(true); break;
      case 2 : bar.setLayout(myLayout = defLayout); this.adjustChildren(); bar.validate(); break;
      case 3 : bar.setBorder(null); myBorder = null; break;
      case 4 : bar.setOrientation(SwingConstants.HORIZONTAL); break;
      case 5 : bar.setRollover(false); break;
      default: super.setDefaultValue(_index-TOOLBAR_ADDED); break;
    }
  }
  
  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : case 1 : return "true";
      case 2 : case 3 : return "<none>";
      case 4 : return "HORIZONTAL";
      case 5 : return "false";
      default : return super.getDefaultValueString(_index-TOOLBAR_ADDED);
    }
  }
  

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 :
        return null;
      default: return super.getValue(_index-TOOLBAR_ADDED);
    }
  }

} // End of class
