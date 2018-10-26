/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementCylinder;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlCylinder3D extends ControlElement3D {
  static final private int CYLINDER_PROPERTIES_ADDED=6;

  private ElementCylinder cylinder;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementCylinder"; }

  protected Element createElement () { return cylinder = new ElementCylinder(); }

  protected int getPropertiesDisplacement () { return CYLINDER_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
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
      case 0 : cylinder.setMinimumAngle(_value.getInteger()); break;
      case 1 : cylinder.setMaximumAngle(_value.getInteger()); break;
      case 2 : cylinder.setClosedTop(_value.getBoolean());    break;
      case 3 : cylinder.setClosedBottom(_value.getBoolean()); break;
      case 4 : cylinder.setClosedLeft(_value.getBoolean()); break;
      case 5 : cylinder.setClosedRight(_value.getBoolean()); break;
      default : super.setValue(_index-CYLINDER_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : cylinder.setMinimumAngle(0); break;
      case 1 : cylinder.setMaximumAngle(360); break;
      case 2 : cylinder.setClosedTop(true);    break;
      case 3 : cylinder.setClosedBottom(true); break;
      case 4 : cylinder.setClosedLeft(true); break;
      case 5 : cylinder.setClosedRight(true); break;
      default: super.setDefaultValue(_index-CYLINDER_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "360";
      case 2 : 
      case 3 : 
      case 4 : 
      case 5 : 
        return "true";
      default : return super.getDefaultValueString(_index-CYLINDER_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 :
        return null;
      default: return super.getValue (_index-CYLINDER_PROPERTIES_ADDED);
    }
  }

} // End of class
