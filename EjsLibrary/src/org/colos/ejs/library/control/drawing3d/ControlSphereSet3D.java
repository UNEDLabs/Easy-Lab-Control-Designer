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
 * A set of Spheres
 */
public class ControlSphereSet3D extends ControlEllipsoidSet3D {
  static final private int SPHERESET_PROPERTIES_ADDED=1;

  protected int getPropertiesAddedToSet () { return SPHERESET_PROPERTIES_ADDED + ControlEllipsoidSet3D.ELSET_ROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementSphere();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementSphere) newElement).setRadius(((ElementSphere) oldElement).getRadius());
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("radius");
        
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("radius")) return "int|double|double[]";

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
              for (int i=0; i<numElements; i++) ((ElementSphere) elements[i]).setRadius(val[i]);
            }
            else {
                double val = _value.getDouble();
                for (int i=0; i<numElements; i++) ((ElementSphere) elements[i]).setRadius(val);
            }
            break;
        default : super.setValue(_index-SPHERESET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementSphere) elements[i]).setRadius(0.2); break;
        default: super.setDefaultValue(_index-SPHERESET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
   }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "0.1";
        default : return super.getDefaultValueString(_index-SPHERESET_PROPERTIES_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : 
            return null;
        default: return super.getValue (_index-SPHERESET_PROPERTIES_ADDED);
      }
    }

} // End of class
