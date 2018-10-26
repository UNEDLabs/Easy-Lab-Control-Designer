/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

public class ControlPlane extends ControlInteractiveTile {
  static final int PROPERTIES_PLANE=PROPERTIES_ADDED+2;

  protected Drawable createDrawable () {
    InteractivePlane plane = new InteractivePlane();
    plane.setOrigin(0,0,0,true);
    return plane;
  }

  protected int getPropertiesDisplacement () { return PROPERTIES_PLANE; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("direction1");
      infoList.add ("direction2");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("color"))          return "lineColor";
    if (_property.equals("secondaryColor")) return "fillColor";
    if (_property.equals("direction1")) return "firstDirection";
    if (_property.equals("direction2")) return "secondDirection";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("direction1"))       return "double[]"; // Vector3D|double[]";
    if (_property.equals("direction2"))       return "double[]"; // Vector3D|double[]";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    /*
    if (_propertyType.indexOf("Vector3D")>=0) {
      try {
        java.util.StringTokenizer t = new java.util.StringTokenizer(_value,",");
        double u1 = Double.parseDouble(t.nextToken());
        double u2 = Double.parseDouble(t.nextToken());
        double u3 = Double.parseDouble(t.nextToken());
        return new ObjectValue(new double[]{u1,u2,u3});
      } catch (Exception exc) {
        System.out.println ("Incorrect 3D vector:"+_value);
        exc.printStackTrace();
        return null;
      }
    }
    */
    return super.parseConstant (_propertyType,_value);
  }


// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          ( (InteractivePlane) myElement).setVectorU(val[0], val[1], val[2]);
        }
        break;
      case 1 :
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          ( (InteractivePlane) myElement).setVectorV(val[0], val[1], val[2]);
        }
        break;
      default : super.setValue(_index-2,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractivePlane) myElement).setVectorU(1.0,0.0,0.0); break;
      case 1 : ((InteractivePlane) myElement).setVectorV(0.0,1.0,0.0); break;
      default: super.setDefaultValue(_index-2); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "{1.0,0.0,0.0}";
      case 1 : return "{0.0,1.0,0.0}";
      default : return super.getDefaultValueString(_index-2);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : return null;
      default: return getValue (_index-2);
    }
  }

} // End of interface
