/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.utils.transformations.AxisRotation;
import org.opensourcephysics.drawing3d.utils.transformations.CustomAxisRotation;
import org.opensourcephysics.numerics.*;
import org.colos.ejs.library.control.value.*;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
public class ControlRotationAxis3DTransformation extends ControlRotation3DTransformation {
  private CustomAxisRotation axisRotation;

  @Override
  protected Transformation createTransformation () {
    origin = new double[]{0.0,0.0,0.0};
    rotation = axisRotation = new CustomAxisRotation();
    return rotation;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("position"); // backwards compatibility
      infoList.add ("axis");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("position"))  return "double[]";
    if (_property.equals("axis"))  return "int[]|double[]";
    return super.getPropertyInfo(_property);
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("position")) return "origin";
    return super.getPropertyCommonName(_property);
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : super.setValue(ControlRotation3DTransformation.ORIGIN, _value); break; // pass it over to "origin" 
      case 1 : 
        double[] newAxis = null;
        if (_value.getObject() instanceof double[]) {
          newAxis = (double[]) _value.getObject();
          axisRotation.setAxis(newAxis);
        }
        else if (_value.getObject() instanceof int[]) {
          int[] newAxisInt = (int[]) _value.getObject();
          newAxis = new double[]{newAxisInt[0],newAxisInt[1],newAxisInt[2]};
          axisRotation.setAxis(newAxis);
        }
        break;
      default : super.setValue(_index-2,_value); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : super.setDefaultValue(ControlRotation3DTransformation.ORIGIN); break;
      case 1 : axisRotation.setAxis(new double[]{1.0,0.0,0.0}); break;
      default: super.setDefaultValue(_index-2); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return super.getDefaultValueString(ControlRotation3DTransformation.ORIGIN);
      case 1 : return "new double[]{1,0,0}";
      default : return super.getDefaultValueString(_index-2);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : 
        return null;
      default: return super.getValue (_index-3);
    }
  }
  
//------------------------------------------------
// Implementation of Transformation
//------------------------------------------------

  public Object clone() {
    ControlRotationAxis3DTransformation ct = new ControlRotationAxis3DTransformation();
    ct.enabled = this.enabled;
    ct.transformation = (Matrix3DTransformation) this.transformation.clone();
    ct.myParent = null;
    ct.rotation = (AxisRotation) ct.transformation;
    ct.axisRotation = (CustomAxisRotation) ct.transformation;
    ct.origin = this.origin.clone();
    return ct;
  }
  
} // End of interface
