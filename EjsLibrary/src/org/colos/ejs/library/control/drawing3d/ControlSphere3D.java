/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementSphere;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlSphere3D extends ControlEllipsoid3D {
  static final private int SPHERE_PROPERTIES_ADDED=1;

  private ElementSphere sphere;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementSphere"; }

  protected Element createElement () {
    return ellipsoid = sphere = new ElementSphere();
  }

  protected int getPropertiesDisplacement () { return SPHERE_PROPERTIES_ADDED+ControlEllipsoid3D.ELLIPSOID_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("radius");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("radius")) return "int|double";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : sphere.setRadius(_value.getDouble()); break;
      default : super.setValue(_index-SPHERE_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : sphere.setRadius(0.1); break;
      default: super.setDefaultValue(_index-SPHERE_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0.5";
      default : return super.getDefaultValueString(_index-SPHERE_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return null;
      default: return super.getValue (_index-SPHERE_PROPERTIES_ADDED);
    }
  }

} // End of class
