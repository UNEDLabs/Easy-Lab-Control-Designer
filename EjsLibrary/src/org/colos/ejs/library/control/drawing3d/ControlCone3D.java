/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementCone;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlCone3D extends ControlElement3D {
  static final private int CONE_PROPERTIES_ADDED=7;

  private ElementCone cone;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementCone"; }

  protected Element createElement () { return cone = new ElementCone(); }

  protected int getPropertiesDisplacement () { return CONE_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("truncationHeight");
      infoList.add ("minimumAngle");
      infoList.add ("maximumAngle");
      infoList.add ("closedTop");
      infoList.add ("closedBottom");
      infoList.add ("closedLeft");
      infoList.add ("closedRight");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("truncationHeight")) return "int|double";
    if (_property.equals("minimumAngle")) return "int";
    if (_property.equals("maximumAngle")) return "int";
    if (_property.equals("closedTop"))    return "boolean";
    if (_property.equals("closedBottom")) return "boolean";
    if (_property.equals("closedLeft"))   return "boolean";
    if (_property.equals("closedRight"))  return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : cone.setTruncationHeight(_value.getDouble()); break;
      case 1 : cone.setMinimumAngle(_value.getInteger()); break;
      case 2 : cone.setMaximumAngle(_value.getInteger()); break;
      case 3 : cone.setClosedTop(_value.getBoolean());    break;
      case 4 : cone.setClosedBottom(_value.getBoolean()); break;
      case 5 : cone.setClosedLeft(_value.getBoolean()); break;
      case 6 : cone.setClosedRight(_value.getBoolean()); break;
      default : super.setValue(_index-CONE_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : cone.setTruncationHeight(Double.NaN); break;
      case 1 : cone.setMinimumAngle(0); break;
      case 2 : cone.setMaximumAngle(360); break;
      case 3 : cone.setClosedTop(true);    break;
      case 4 : cone.setClosedBottom(true); break;
      case 5 : cone.setClosedLeft(true); break;
      case 6 : cone.setClosedRight(true); break;
      default: super.setDefaultValue(_index-CONE_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>"; 
      case 1 : return "0";
      case 2 : return "360";
      case 3 : 
      case 4 : 
      case 5 : 
      case 6 : 
        return "true";
      default : return super.getDefaultValueString(_index-CONE_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 :
        return null;
      default: return super.getValue (_index-CONE_PROPERTIES_ADDED);
    }
  }

} // End of class
