/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPlane;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlPlane3D extends ControlElement3D {
  static final private int PLANE_PROPERTIES_ADDED=4;

  private ElementPlane plane;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementPlane"; }

  protected Element createElement () { return plane = new ElementPlane(); }

  protected int getPropertiesDisplacement () { return PLANE_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("firstDirection");
      infoList.add ("secondDirection");
      infoList.add ("firstSize");
      infoList.add ("secondSize");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("firstDirection"))  return "double[]";
    if (_property.equals("secondDirection")) return "double[]";
    if (_property.equals("firstSize"))  return "int|double";
    if (_property.equals("secondSize")) return "int|double";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getObject() instanceof double[]) plane.setFirstDirection((double[]) _value.getObject());  break;
      case 1 : if (_value.getObject() instanceof double[]) plane.setSecondDirection((double[]) _value.getObject()); break;
      case 2 : plane.setSizeFirstDirection(_value.getDouble()); break;
      case 3 : plane.setSizeSecondDirection(_value.getDouble()); break;
      default : super.setValue(_index-PLANE_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : plane.setFirstDirection(new double[] {1,0,0});  break;
      case 1 : plane.setSecondDirection(new double[] {0,1,0}); break;
      case 2 : plane.setSizeFirstDirection(1.0); break;
      case 3 : plane.setSizeSecondDirection(1.0); break;
      default: super.setDefaultValue(_index-PLANE_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "new double[] {1,0,0}";
      case 1 : return "new double[] {0,1,0}";
      case 2 : return "1";
      case 3 : return "1";
      default : return super.getDefaultValueString(_index-PLANE_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : return null;
      default: return super.getValue (_index-PLANE_PROPERTIES_ADDED);
    }
  }

} // End of class
