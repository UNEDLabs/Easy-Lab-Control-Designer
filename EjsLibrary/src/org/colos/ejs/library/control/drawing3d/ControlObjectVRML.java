/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementObjectVRML;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlObjectVRML extends ControlElement3D {
  static final private int OBJECTVRML_PROPERTIES_ADDED=1;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementObjectVRML"; }

  protected Element createElement () { return new ElementObjectVRML(); }

  protected int getPropertiesDisplacement () { return OBJECTVRML_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("VRMLfile");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("VRMLfile")) return "File|String TRANSLATABLE";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : ((ElementObjectVRML)getElement()).setObjectFile(_value.getString()); break;
      default : super.setValue(_index-OBJECTVRML_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((ElementObjectVRML)getElement()).setObjectFile(null); break;
      default: super.setDefaultValue(_index-OBJECTVRML_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      default : return super.getDefaultValueString(_index-OBJECTVRML_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return null;
      default: return super.getValue (_index-OBJECTVRML_PROPERTIES_ADDED);
    }
  }

} // End of class
