/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of cones
 */
public class ControlConeSet3D extends ControlSet3D {
  static final private int CONESET_PROPERTIES_ADDED=7;

  protected int getPropertiesAddedToSet () { return CONESET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementCone();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementCone) newElement).setTruncationHeight(((ElementCone) oldElement).getTruncationHeight());
      ((ElementCone) newElement).setMinimumAngle(((ElementCone) oldElement).getMinimumAngle());
      ((ElementCone) newElement).setMaximumAngle(((ElementCone) oldElement).getMaximumAngle());
      ((ElementCone) newElement).setClosedTop(((ElementCone) oldElement).isClosedTop());
      ((ElementCone) newElement).setClosedBottom(((ElementCone) oldElement).isClosedBottom());
      ((ElementCone) newElement).setClosedLeft(((ElementCone) oldElement).isClosedLeft());
      ((ElementCone) newElement).setClosedRight(((ElementCone) oldElement).isClosedRight());
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("truncationHeight");
        infoList.add ("minimumAngle");
        infoList.add ("maximumAngle");
        infoList.add ("closedTop");
        infoList.add ("closedBottom");
        infoList.add ("closedLeft");
        infoList.add ("closedRight");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("truncationHeight")) return "int|double|double[]";
        if (_property.equals("minimumAngle")) return "int|int[]";
        if (_property.equals("maximumAngle")) return "int|int[]";
        if (_property.equals("closedTop"))    return "boolean|boolean[]";
        if (_property.equals("closedBottom")) return "boolean|boolean[]";
        if (_property.equals("closedLeft"))   return "boolean|boolean[]";
        if (_property.equals("closedRight"))  return "boolean|boolean[]";

      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------


    public void setValue (int _index, Value _value) {
      switch (_index) {
      case 0 :
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setTruncationHeight(val[i]);
          }
          else {
              double val = _value.getDouble();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setTruncationHeight(val);
          }
          break;
      case 1 :
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setMinimumAngle(val[i]);
          }
          else {
              int val = _value.getInteger();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setMinimumAngle(val);
          }
          break;
      case 2 :
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setMaximumAngle(val[i]);
          }
          else {
              int val = _value.getInteger();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setMaximumAngle(val);
          }
          break;
        case 3 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedTop(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedTop(val);
            }
            break;
        case 4 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedBottom(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedBottom(val);
            }
            break;
        case 5 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedLeft(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedLeft(val);
            }
            break;
        case 6 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedRight(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedRight(val);
            }
            break;
        default : super.setValue(_index-CONESET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setTruncationHeight(Double.NaN);    break;
        case 1 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setMinimumAngle(0); break;
        case 2 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setMaximumAngle(360); break;
        case 3 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedTop(true);    break;
        case 4 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedBottom(true); break;
        case 5 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedLeft(true);    break;
        case 6 : for (int i=0; i<numElements; i++) ((ElementCone) elements[i]).setClosedRight(true); break;
        default: super.setDefaultValue(_index-CONESET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "<none>"; 
        case 1 : return "0";
        case 2 : return "360";
        case 3 : 
        case 4 : 
        case 5 : 
        case 6 : 
          return "true";
        default : return super.getDefaultValueString(_index-CONESET_PROPERTIES_ADDED);
      }
    }
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 :
        case 4 : case 5 : case 6 :
            return null;
        default: return super.getValue (_index-CONESET_PROPERTIES_ADDED);
      }
    }

} // End of class
