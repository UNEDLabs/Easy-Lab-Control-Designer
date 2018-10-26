/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementTetrahedron;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlTetrahedron3D extends ControlElement3D {
  static final private int TETRAHEDRON_PROPERTIES_ADDED=3;

  private ElementTetrahedron tetrahedron;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementTetrahedron"; }

  protected Element createElement () { return tetrahedron = new ElementTetrahedron(); }

  protected int getPropertiesDisplacement () { return TETRAHEDRON_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("truncationHeight");
      infoList.add ("closedTop");
      infoList.add ("closedBottom");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("truncationHeight")) return "int|double";
    if (_property.equals("closedTop"))    return "boolean";
    if (_property.equals("closedBottom")) return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : tetrahedron.setTruncationHeight(_value.getDouble()); break;
      case 1 : tetrahedron.setClosedTop(_value.getBoolean());    break;
      case 2 : tetrahedron.setClosedBottom(_value.getBoolean()); break;
      default : super.setValue(_index-TETRAHEDRON_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : tetrahedron.setTruncationHeight(Double.NaN); break;
      case 1 : tetrahedron.setClosedTop(true);    break;
      case 2 : tetrahedron.setClosedBottom(true); break;
      default: super.setDefaultValue(_index-TETRAHEDRON_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>"; 
      case 1 : 
      case 2 : 
        return "true";
      default : return super.getDefaultValueString(_index-TETRAHEDRON_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : 
        return null;
      default: return super.getValue (_index-TETRAHEDRON_PROPERTIES_ADDED);
    }
  }

} // End of class
