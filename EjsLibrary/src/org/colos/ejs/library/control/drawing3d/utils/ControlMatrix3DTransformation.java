/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.utils.transformations.Matrix3DTransformation;
import org.opensourcephysics.numerics.Transformation;
import org.colos.ejs.library.control.value.*;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
public class ControlMatrix3DTransformation extends ControlTransformation3D {
  protected Matrix3DTransformation matrix3DTr;
  protected double[] origin;

  @Override
  protected Transformation createTransformation () {
    origin = new double[]{0.0,0.0,0.0};
    matrix3DTr = new Matrix3DTransformation();
    return matrix3DTr;
  }

  @Override
  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.utils.transformations.Matrix3DTransformation"; }

  @Override
  protected void setAffectedElement(Element element) {
    matrix3DTr.setElement(element);
  };

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("origin");
      infoList.add ("matrix");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("origin"))  return "double[]";
    if (_property.equals("matrix"))  return "double[]|double[][]";
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
        }
        break;
      case 1 : 
        if (_value.getObject() instanceof double[]) matrix3DTr.setMatrix((double[]) _value.getObject());
        else if (_value.getObject() instanceof double[][]) matrix3DTr.setMatrix((double[][]) _value.getObject());
        break;
      default : super.setValue(_index-2,_value); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : 
        matrix3DTr.setOrigin(origin = new double[]{0,0,0});
        break;
      case 1 : 
        matrix3DTr.setMatrix(new double[][]{{1,0,0}, {0,1,0}, {0,0,1}}); 
        break;
      default: super.setDefaultValue(_index-2); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "new double[]{0,0,0}";
      case 1 : return "new double[][]{{1,0,0}, {0,1,0}, {0,0,1}}";
      default : return super.getDefaultValueString(_index-2);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 :
        return null;
      default: return super.getValue (_index-2);
    }
  }
  
//------------------------------------------------
// Implementation of Transformation
//------------------------------------------------

  public Object clone() {
    ControlMatrix3DTransformation ct = new ControlMatrix3DTransformation();
    ct.enabled = this.enabled;
    ct.transformation = (Matrix3DTransformation) this.transformation.clone();
    ct.myParent = null;
    ct.matrix3DTr = (Matrix3DTransformation) ct.transformation;
    ct.origin = this.origin.clone();
    return ct;
  }
  
} // End of interface
