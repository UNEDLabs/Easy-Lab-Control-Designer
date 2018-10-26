/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.opensourcephysics.drawing2d.*;
import org.colos.ejs.library.control.value.*;

/**
 * A 2D arrow
 */
public class ControlArrow2D extends ControlElement2D {
  static final private int ARROW_PROPERTIES_ADDED=2;

  private ElementArrow arrow;

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementArrow"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    arrow = new ElementArrow();
    return arrow;
  }

  protected int getPropertiesDisplacement () { return ARROW_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("style");
      infoList.add ("elementposition");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("style")) return "NewArrowStyle|int";
    if (_property.equals("elementposition"))return "ArrowPosition|int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getInteger()!=arrow.getArrowType()) arrow.setArrowType(_value.getInteger()); break;
      case 1 : if (_value.getInteger()!=arrow.getStyle().getRelativePosition()) arrow.getStyle().setRelativePosition(_value.getInteger()); break; 
      default : super.setValue(_index-ARROW_PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : arrow.setArrowType(ElementArrow.ARROW); break;
      case 1 : arrow.getStyle().setRelativePosition(Style.NORTH_EAST); break; 
      default: super.setDefaultValue(_index-ARROW_PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "ARROW";
      case 1 : return "NORTH_EAST";
      default : return super.getDefaultValueString(_index-ARROW_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 :
        return null;
      default: return super.getValue (_index-ARROW_PROPERTIES_ADDED);
    }
  }

} // End of class
