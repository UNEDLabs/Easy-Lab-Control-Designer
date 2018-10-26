/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of Surfaces
 */
public class ControlSurfaceSet3D extends ControlSet3D {
  static final private int SURFACESET_PROPERTIES_ADDED=1;

  protected int getPropertiesAddedToSet () { return SURFACESET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementSurface();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementSurface) newElement).setData(((ElementSurface) oldElement).getData());
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("data");
        
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("data"))    return "double[][][]|double[][][][]";

      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :
            if (_value.getObject() instanceof double[][][][]) {
              double[][][][] val = (double[][][][]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementSurface) elements[i]).setData(val[i]);
            }
            else if (_value.getObject() instanceof double[][][]) {
              double[][][] val = (double[][][]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementSurface) elements[i]).setData(val);
            }
            break;
        default : super.setValue(_index-SURFACESET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : break;
        default: super.setDefaultValue(_index-SURFACESET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "<none>";
        default : return super.getDefaultValueString(_index-SURFACESET_PROPERTIES_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : return null;
        default: return super.getValue (_index-SURFACESET_PROPERTIES_ADDED);
      }
    }

} // End of class
