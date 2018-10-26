/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.numerics.*;
import org.colos.ejs.library.control.value.*;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
public class ControlAlignment3DTransformation extends ControlTransformation3D {
  protected Matrix3DTransformation matrix3DTr;
  protected double[] origin;
  private double[] v1;
  private double[] v2;

  @Override
  protected Transformation createTransformation () {
    origin = new double[]{0.0,0.0,0.0};
    v1 = new double[]{1.0,0.0,0.0};
    v2 = new double[]{1.0,0.0,0.0};
    matrix3DTr = Matrix3DTransformation.createAlignmentTransformation(v1, v2);
    return matrix3DTr;
  }

  @Override
  public String getObjectClassname () { return "org.opensourcephysics.numerics.Matrix3DTransformation"; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("origin");
      infoList.add ("v1");
      infoList.add ("v2");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("origin"))  return "double[]";
    if (_property.equals("v1"))  return "double[]";
    if (_property.equals("v2"))  return "double[]";
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
          matrix3DTr.setOrigin(newOrigin);
          if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION);
        }
        break;
      case 1 : 
        double[] newV1 = null;
        if (_value.getObject() instanceof double[]) newV1 = (double[]) _value.getObject();
        if (newV1!=null && !newV1.equals(v1)) {
          System.arraycopy(newV1,0,v1,0,3);
          transformation = matrix3DTr = Matrix3DTransformation.createAlignmentTransformation(v1,v2);
          if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION);
        }
        break;
      case 2 :
        double[] newV2 = null;
        if (_value.getObject() instanceof double[]) newV2 = (double[]) _value.getObject();
        if (newV2!=null && !newV2.equals(v2)) {
          System.arraycopy(newV2,0,v2,0,3);
          transformation = matrix3DTr = Matrix3DTransformation.createAlignmentTransformation(v1,v2);
          if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION);
        }
        break;
      default : super.setValue(_index-3,_value); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : 
        matrix3DTr.setOrigin(origin = new double[]{0,0,0});
        if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION); 
        break;
      case 1 : 
        v1 = new double[]{1.0,0.0,0.0}; 
        transformation = matrix3DTr = Matrix3DTransformation.createAlignmentTransformation(v1,v2);
        if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION);
        break;
      case 2 : 
        v2 = new double[]{1.0,0.0,0.0}; 
        transformation = matrix3DTr = Matrix3DTransformation.createAlignmentTransformation(v1,v2);
        if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION);
        break;
      default: super.setDefaultValue(_index-3); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "new double[]{0,0,0}";
      case 1 : return "new double[]{1,0,0}";
      case 2 : return "new double[]{1,0,0}";
      default : return super.getDefaultValueString(_index-3);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : 
        return null;
      default: return super.getValue (_index-3);
    }
  }
  
//------------------------------------------------
// Implementation of Transformation
//------------------------------------------------

  public Object clone() {
    ControlAlignment3DTransformation ct = new ControlAlignment3DTransformation();
    ct.enabled = this.enabled;
    ct.transformation = (Matrix3DTransformation) this.transformation.clone();
    ct.myParent = null;
    ct.matrix3DTr = (Matrix3DTransformation) ct.transformation;
    ct.origin = this.origin.clone();
    ct.v1 = this.v1.clone();
    ct.v2 = this.v2.clone();
    return ct;
  }
  
} // End of interface
