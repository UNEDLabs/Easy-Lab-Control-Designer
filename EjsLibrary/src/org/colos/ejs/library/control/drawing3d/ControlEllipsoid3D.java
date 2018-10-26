/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementEllipsoid;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlEllipsoid3D extends ControlElement3D {
  static final protected int ELLIPSOID_PROPERTIES_ADDED=8;

  protected ElementEllipsoid ellipsoid;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementEllipsoid"; }

  protected Element createElement () { return ellipsoid = new ElementEllipsoid(); }

  protected int getPropertiesDisplacement () { return ELLIPSOID_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("minimumAngleU");
      infoList.add ("maximumAngleU");
      infoList.add ("minimumAngleV");
      infoList.add ("maximumAngleV");
      infoList.add ("closedTop");
      infoList.add ("closedBottom");
      infoList.add ("closedLeft");
      infoList.add ("closedRight");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumAngleU")) return "int";
    if (_property.equals("maximumAngleU")) return "int";
    if (_property.equals("minimumAngleV")) return "int";
    if (_property.equals("maximumAngleV")) return "int";
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
      case 0 : ellipsoid.setMinimumAngleU(_value.getInteger()); break;
      case 1 : ellipsoid.setMaximumAngleU(_value.getInteger()); break;
      case 2 : ellipsoid.setMinimumAngleV(_value.getInteger()); break;
      case 3 : ellipsoid.setMaximumAngleV(_value.getInteger()); break;
      case 4 : ellipsoid.setClosedTop(_value.getBoolean());    break;
      case 5 : ellipsoid.setClosedBottom(_value.getBoolean()); break;
      case 6 : ellipsoid.setClosedLeft(_value.getBoolean()); break;
      case 7 : ellipsoid.setClosedRight(_value.getBoolean()); break;
      default : super.setValue(_index-ELLIPSOID_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ellipsoid.setMinimumAngleU(0); break;
      case 1 : ellipsoid.setMaximumAngleU(360); break;
      case 2 : ellipsoid.setMinimumAngleV(-90); break;
      case 3 : ellipsoid.setMaximumAngleV(90); break;
      case 4 : ellipsoid.setClosedTop(true);    break;
      case 5 : ellipsoid.setClosedBottom(true); break;
      case 6 : ellipsoid.setClosedLeft(true); break;
      case 7 : ellipsoid.setClosedRight(true); break;
      default: super.setDefaultValue(_index-ELLIPSOID_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "360";
      case 2 : return "-90";
      case 3 : return "90";
      case 4 : 
      case 5 : 
      case 6 : 
      case 7 : 
        return "true";
      default : return super.getDefaultValueString(_index-ELLIPSOID_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
        return null;
      default: return super.getValue (_index-ELLIPSOID_PROPERTIES_ADDED);
    }
  }

} // End of class
