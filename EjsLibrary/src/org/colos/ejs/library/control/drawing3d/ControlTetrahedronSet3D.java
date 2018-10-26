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
public class ControlTetrahedronSet3D extends ControlSet3D {
  static final private int TETRAHEDRONSET_PROPERTIES_ADDED=3;

  protected int getPropertiesAddedToSet () { return TETRAHEDRONSET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementTetrahedron();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementTetrahedron) newElement).setTruncationHeight(((ElementTetrahedron) oldElement).getTruncationHeight());
      ((ElementTetrahedron) newElement).setClosedTop(((ElementTetrahedron) oldElement).isClosedTop());
      ((ElementTetrahedron) newElement).setClosedBottom(((ElementTetrahedron) oldElement).isClosedBottom());
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("truncationHeight");
        infoList.add ("closedTop");
        infoList.add ("closedBottom");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("truncationHeight")) return "int|double|double[]";
        if (_property.equals("closedTop"))    return "boolean|boolean[]";
        if (_property.equals("closedBottom")) return "boolean|boolean[]";

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
            for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setTruncationHeight(val[i]);
          }
          else {
              double val = _value.getDouble();
              for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setTruncationHeight(val);
          }
          break;
        case 1 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setClosedTop(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setClosedTop(val);
            }
            break;
        case 2 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setClosedBottom(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setClosedBottom(val);
            }
            break;
        default : super.setValue(_index-TETRAHEDRONSET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setTruncationHeight(Double.NaN);    break;
        case 1 : for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setClosedTop(true);    break;
        case 2 : for (int i=0; i<numElements; i++) ((ElementTetrahedron) elements[i]).setClosedBottom(true); break;
        default: super.setDefaultValue(_index-TETRAHEDRONSET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "<none>"; 
        case 1 : 
        case 2 : 
          return "true";
        default : return super.getDefaultValueString(_index-TETRAHEDRONSET_PROPERTIES_ADDED);
      }
    }
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : 
            return null;
        default: return super.getValue (_index-TETRAHEDRONSET_PROPERTIES_ADDED);
      }
    }

} // End of class
