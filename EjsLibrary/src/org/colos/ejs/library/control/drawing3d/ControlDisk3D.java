/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementDisk;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlDisk3D extends ControlElement3D {
  static final private int DISK_PROPERTIES_ADDED=2;

  private ElementDisk circle;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementCylinder"; }

  protected Element createElement () { return circle = new ElementDisk(); }

  protected int getPropertiesDisplacement () { return DISK_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("minimumAngle");
      infoList.add ("maximumAngle");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumAngle")) return "int";
    if (_property.equals("maximumAngle")) return "int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : circle.setMinimumAngle(_value.getInteger()); break;
      case 1 : circle.setMaximumAngle(_value.getInteger()); break;
      default : super.setValue(_index-DISK_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : circle.setMinimumAngle(0); break;
      case 1 : circle.setMaximumAngle(360); break;
      default: super.setDefaultValue(_index-DISK_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "360";
      default : return super.getDefaultValueString(_index-DISK_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 :
      return null;
      default: return super.getValue (_index-DISK_PROPERTIES_ADDED);
    }
  }

} // End of class
