/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.utils.transformations.AxisRotation;
import org.colos.ejs.library.control.drawing3d.ControlElement3D;
import org.colos.ejs.library.control.value.*;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
public abstract class ControlRotation3DTransformation extends ControlTransformation3D {
  static public int ORIGIN = 0;
  
  protected AxisRotation rotation;
  protected double[] origin;

  @Override
  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.utils.transformations.AxisRotation"; }

  @Override
  protected void setAffectedElement(Element element) { rotation.setElement(element); }

  
// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("origin");
      infoList.add ("angle");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("origin"))  return "double[]";
    if (_property.equals("angle")) return "int|double";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : 
        double[] newOrigin = null;
        if (_value.getObject() instanceof double[]) newOrigin = (double[]) _value.getObject();
        else if (_value.getObject() instanceof int[]) {
          int[] newOriginInt = (int[]) _value.getObject();
          newOrigin = new double[]{newOriginInt[0],newOriginInt[1],newOriginInt[2]};
        }
        if (newOrigin!=null && !newOrigin.equals(origin)) {
          System.arraycopy(newOrigin,0,origin,0,3);
          rotation.setOrigin(newOrigin);
        }
        break;
      case 1 :
        if (_value instanceof IntegerValue) rotation.setAngle(_value.getInteger()*ControlElement3D.TO_RADIANS);
        else rotation.setAngle(_value.getDouble());
        break;
      default : super.setValue(_index-2,_value); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : rotation.setOrigin(origin = new double[]{0,0,0}); break;
      case 1 : rotation.setAngle(0); break;
      default: super.setDefaultValue(_index-2); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "new double[]{0,0,0}";
      case 1 : return "0";
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
  
} // End of interface
