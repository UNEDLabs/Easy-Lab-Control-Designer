/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.utils.transformations.QuaternionRotation;
import org.opensourcephysics.numerics.*;
import org.colos.ejs.library.control.value.*;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
public class ControlQuaternionTransformation extends ControlTransformation3D {
  static private final int QUATERNION_ADDED = 6;
  
  protected QuaternionRotation quaternion;
  protected double[] origin;
  private double q0, q1, q2, q3;

  @Override
  protected Transformation createTransformation () {
    origin = new double[]{0.0,0.0,0.0};
    q0 = 1;
    q1 =  q2 = q3 = 0;
    quaternion = new QuaternionRotation(q0,q1,q2,q3);
    return quaternion;
  }

  @Override
  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.utils.transformations.QuaternionRotation"; }

  @Override
  protected void setAffectedElement(Element element) { quaternion.setElement(element); }
  
// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("origin");
      infoList.add ("q");
      infoList.add ("q0");
      infoList.add ("q1");
      infoList.add ("q2");
      infoList.add ("q3");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("origin"))  return "double[]";
    if (_property.equals("q"))   return "double[]";
    if (_property.equals("q0"))  return "double";
    if (_property.equals("q1"))  return "double";
    if (_property.equals("q2"))  return "double";
    if (_property.equals("q3"))  return "double";
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
          quaternion.setOrigin(newOrigin);
        }
        break;
      case 1 :
        boolean changed = false;
        if (_value.getObject() instanceof double[]) {
          double[] q = (double[]) _value.getObject();
//          double[] cQ = quaternion.getCoordinates();
//          System.err.println ("Qnew view = "+q[0]+","+q[1]+","+q[2]+","+q[3]);
//          System.err.println ("Qcurrent view = "+cQ[0]+","+cQ[1]+","+cQ[2]+","+cQ[3]);
          if (q0!=q[0]) { q0 = q[0]; changed = true; }
          if (q1!=q[1]) { q1 = q[1]; changed = true; }
          if (q2!=q[2]) { q2 = q[2]; changed = true; }
          if (q3!=q[3]) { q3 = q[3]; changed = true; }
        }
        else if (_value.getObject() instanceof int[]) {
          int[] q = (int[]) _value.getObject();
          if (q0!=q[0]) { q0 = q[0]; changed = true; }
          if (q1!=q[1]) { q1 = q[1]; changed = true; }
          if (q2!=q[2]) { q2 = q[2]; changed = true; }
          if (q3!=q[3]) { q3 = q[3]; changed = true; }
        }
        if (changed) {
          quaternion.setCoordinates(q0,q1,q2,q3);
//          System.err.println ("Qchanged view = "+q0+","+q1+","+q2+","+q3);
        }
        break;
      case 2 : if (_value.getDouble()!=q0) quaternion.setCoordinates(q0 = _value.getDouble(),q1,q2,q3); break;
      case 3 : if (_value.getDouble()!=q1) quaternion.setCoordinates(q0,q1 = _value.getDouble(),q2,q3); break;
      case 4 : if (_value.getDouble()!=q2) quaternion.setCoordinates(q0,q1,q2 = _value.getDouble(),q3); break;
      case 5 : if (_value.getDouble()!=q3) quaternion.setCoordinates(q0,q1,q2,q3 = _value.getDouble());break;
      default : super.setValue(_index-QUATERNION_ADDED,_value); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : quaternion.setOrigin(origin = new double[]{0,0,0}); break;
      case 1 : // do nothing 
        break;
      case 2 : quaternion.setCoordinates(q0=1,q1,q2,q3); break;
      case 3 : quaternion.setCoordinates(q0,q1=0,q2,q3); break;
      case 4 : quaternion.setCoordinates(q0,q1,q2=0,q3); break;
      case 5 : quaternion.setCoordinates(q0,q1,q2,q3=0); break;
      default: super.setDefaultValue(_index-QUATERNION_ADDED); break;
    }
    if (isUnderEjs && myParent!=null) myParent.updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "new double[]{0,0,0}";
      case 1 : return "<none>";
      case 2 : return "1";
      case 3 : return "0";
      case 4 : return "0";
      case 5 : return "0";
      default : return super.getDefaultValueString(_index-QUATERNION_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 :
        return null;
      default: return super.getValue (_index-QUATERNION_ADDED);
    }
  }
  
//------------------------------------------------
// Implementation of Transformation
//------------------------------------------------

  public Object clone() {
    ControlQuaternionTransformation ct = new ControlQuaternionTransformation();
    ct.enabled = this.enabled;
    ct.transformation = (Transformation) this.transformation.clone();
    ct.myParent = null;
    ct.quaternion = (QuaternionRotation) ct.transformation;
    ct.origin = this.origin.clone();
    ct.q0 = this.q0;
    ct.q1 = this.q1;
    ct.q2 = this.q2;
    ct.q3 = this.q3;
    return ct;
  }
  
} // End of interface
