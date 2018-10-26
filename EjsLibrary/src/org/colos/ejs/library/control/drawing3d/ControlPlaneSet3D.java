/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of planes
 */
public class ControlPlaneSet3D extends ControlSet3D {
  static final private int PLANESET_PROPERTIES_ADDED=4;

  protected int getPropertiesAddedToSet () { return PLANESET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementPlane();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementPlane) newElement).setFirstDirection(((ElementPlane) oldElement).getFirstDirection());
      ((ElementPlane) newElement).setSecondDirection(((ElementPlane) oldElement).getSecondDirection());
  }

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
        if (_property.equals("firstDirection"))  return "double[]|double[][]";
        if (_property.equals("secondDirection")) return "double[]|double[][]";
        if (_property.equals("firstSize"))  return "int|double|double[]";
        if (_property.equals("secondSize")) return "int|double|double[]";

      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------
    
    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :
            if (_value.getObject() instanceof double[][]) {
              double[][] val = (double[][]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setFirstDirection(val[i]);
            }
            else if (_value.getObject() instanceof double[]) {
              double[] val = (double[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setFirstDirection(val);
            }
            break;
        case 1 :
            if (_value.getObject() instanceof double[][]) {
              double[][] val = (double[][]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSecondDirection(val[i]);
            }
            else if (_value.getObject() instanceof double[]) {
              double[] val = (double[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSecondDirection(val);
            }
            break;
        case 2 :
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSizeFirstDirection(val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSizeFirstDirection(val);
          }
          break;
        case 3 :
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSizeSecondDirection(val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSizeSecondDirection(val);
          }
          break;
        default : super.setValue(_index-PLANESET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 :
          {
            double[] dir = new double[] {1,0,0};
            for (int i = 0; i < numElements; i++) ((ElementPlane) elements[i]).setFirstDirection(dir);
          }
          break;
        case 1 :
        {
          double[] dir = new double[] {0,1,0};
          for (int i = 0; i < numElements; i++) ((ElementPlane) elements[i]).setSecondDirection(dir);
        }
        break;
        case 2 :
          for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSizeFirstDirection(1.0);
          break;
        case 3 :
          for (int i=0; i<numElements; i++) ((ElementPlane) elements[i]).setSizeSecondDirection(1.0);
          break;

        default: super.setDefaultValue(_index-PLANESET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "new double[] {1,0,0}";
        case 1 : return "new double[] {0,1,0}";
        case 2 : return "1";
        case 3 : return "1";
        default : return super.getDefaultValueString(_index-PLANESET_PROPERTIES_ADDED);
      }
    }
    
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 : return null;
        default: return super.getValue (_index-PLANESET_PROPERTIES_ADDED);
      }
    }

} // End of class
