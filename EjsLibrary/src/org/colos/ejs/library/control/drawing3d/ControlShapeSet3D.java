/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of circles
 */
public class ControlShapeSet3D extends ControlSet3D {
  static final private int SHAPESET_PROPERTIES_ADDED=3;

  protected int getPropertiesAddedToSet () { return SHAPESET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementShape();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementShape) newElement).setRotationAngle(((ElementShape) oldElement).getRotationAngle());
      ((ElementShape) newElement).setShapeType(((ElementShape) oldElement).getShapeType());
      ((ElementShape) newElement).setPixelSize(((ElementShape) oldElement).isPixelSize());
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("style");
        infoList.add ("pixelSize");
        infoList.add ("rotationAngle");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
      if (_property.equals("style")) return "MarkerShape|int|int[]";
      if (_property.equals("pixelSize")) return "boolean|boolean[]";
      if (_property.equals("rotationAngle")) return "int|double|int[]|double[]";

      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :             
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setShapeType(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setShapeType(val);
          }
          break;
        case 1 : 
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setPixelSize(val[i]);
          }
          else {
            boolean val = _value.getBoolean();
            for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setPixelSize(val);
          }
          break;
        case 2 :
            if (_value.getObject() instanceof double[]) {
              double[] val = (double[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setRotationAngle(val[i]);
            }
            else if (_value.getObject() instanceof int[]) {
              int[] val = (int[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setRotationAngle(val[i]*ControlDrawingPanel3D.TO_RAD);
            }
            else if (_value instanceof IntegerValue) {
              double angle = _value.getInteger()*ControlDrawingPanel3D.TO_RAD;
              for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setRotationAngle(angle);
            }
            else {
              double angle = _value.getDouble();
              for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setRotationAngle(angle);
            }
            break;
        default : super.setValue(_index-SHAPESET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setShapeType(ElementShape.ELLIPSE); break;
        case 1 : for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setPixelSize(false); break;
        case 2 : for (int i=0; i<numElements; i++) ((ElementShape) elements[i]).setRotationAngle(0.0); break;
        default: super.setDefaultValue(_index-SHAPESET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "ELLIPSE";
        case 1 : return "false";
        case 2 : return "0.0";
        default : return super.getDefaultValueString(_index-SHAPESET_PROPERTIES_ADDED);
      }
    }
    
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : return null;
        default: return super.getValue (_index-SHAPESET_PROPERTIES_ADDED);
      }
    }

} // End of class
