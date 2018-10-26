/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of ellipsoids
 */
public class ControlEllipsoidSet3D extends ControlSet3D {
  static final protected int ELSET_ROPERTIES_ADDED=8;

  protected int getPropertiesAddedToSet () { return ELSET_ROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementEllipsoid();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementEllipsoid) newElement).setMinimumAngleU(((ElementEllipsoid) oldElement).getMinimumAngleU());
      ((ElementEllipsoid) newElement).setMaximumAngleU(((ElementEllipsoid) oldElement).getMaximumAngleU());
      ((ElementEllipsoid) newElement).setMinimumAngleV(((ElementEllipsoid) oldElement).getMinimumAngleV());
      ((ElementEllipsoid) newElement).setMaximumAngleV(((ElementEllipsoid) oldElement).getMaximumAngleV());
      ((ElementEllipsoid) newElement).setClosedTop(((ElementEllipsoid) oldElement).isClosedTop());
      ((ElementEllipsoid) newElement).setClosedBottom(((ElementEllipsoid) oldElement).isClosedBottom());
      ((ElementEllipsoid) newElement).setClosedLeft(((ElementEllipsoid) oldElement).isClosedLeft());
      ((ElementEllipsoid) newElement).setClosedRight(((ElementEllipsoid) oldElement).isClosedRight());
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("minimumAngleU");
        infoList.add ("maximumAngleU");
        infoList.add ("minimumAngleV");
        infoList.add ("maximumAngleV");
        infoList.add ("closedTop");
        infoList.add ("closedBottom");
        infoList.add ("closedLeft");
        infoList.add ("closedRight");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("minimumAngleU")) return "int|int[]";
        if (_property.equals("maximumAngleU")) return "int|int[]";
        if (_property.equals("minimumAngleV")) return "int|int[]";
        if (_property.equals("maximumAngleV")) return "int|int[]";
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
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMinimumAngleU(val[i]);
          }
          else {
              int val = _value.getInteger();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMinimumAngleU(val);
          }
          break;
      case 1 :
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMaximumAngleU(val[i]);
          }
          else {
              int val = _value.getInteger();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMaximumAngleU(val);
          }
          break;
      case 2 :
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMinimumAngleV(val[i]);
          }
          else {
              int val = _value.getInteger();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMinimumAngleV(val);
          }
          break;
      case 3 :
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMaximumAngleV(val[i]);
          }
          else {
              int val = _value.getInteger();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMaximumAngleV(val);
          }
          break;
        case 4 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedTop(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedTop(val);
            }
            break;
        case 5 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedBottom(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedBottom(val);
            }
            break;
        case 6 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedLeft(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedLeft(val);
            }
            break;
        case 7 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedRight(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedRight(val);
            }
            break;
        default : super.setValue(_index-ELSET_ROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMinimumAngleU(0); break;
        case 1 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMaximumAngleU(360); break;
        case 2 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMinimumAngleV(-90); break;
        case 3 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setMaximumAngleV(90); break;
        case 4 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedTop(true);    break;
        case 5 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedBottom(true); break;
        case 6 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedLeft(true);    break;
        case 7 : for (int i=0; i<numElements; i++) ((ElementEllipsoid) elements[i]).setClosedRight(true); break;
        default: super.setDefaultValue(_index-ELSET_ROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "0";
        case 1 : return "360";
        case 2 : return "-90";
        case 3 : return "90";
        case 4 : 
        case 5 : 
        case 6 : 
        case 7 : 
          return "true";
        default : return super.getDefaultValueString(_index-ELSET_ROPERTIES_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 :
        case 4 : case 5 : case 6 : case 7 :
            return null;
        default: return super.getValue (_index-ELSET_ROPERTIES_ADDED);
      }
    }

} // End of class
