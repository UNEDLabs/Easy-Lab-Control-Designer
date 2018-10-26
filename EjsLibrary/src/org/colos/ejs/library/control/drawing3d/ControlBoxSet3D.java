/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of boxes
 */
public class ControlBoxSet3D extends ControlSet3D {
  static final private int BOXSET_PROPERTIES_ADDED=2;

  protected int getPropertiesAddedToSet () { return BOXSET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    return new ElementBox();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementBox) newElement).setClosedTop(((ElementBox) oldElement).isClosedTop());
      ((ElementBox) newElement).setClosedBottom(((ElementBox) oldElement).isClosedBottom());
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("closedTop");
        infoList.add ("closedBottom");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
      if (_property.equals("closedTop")) return "boolean|boolean[]";
      if (_property.equals("closedBottom")) return "boolean|boolean[]";

      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementBox) elements[i]).setClosedTop(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementBox) elements[i]).setClosedTop(val);
            }
            break;
        case 1 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementBox) elements[i]).setClosedBottom(val[i]);
            }
            else {
                boolean val = _value.getBoolean();
                for (int i=0; i<numElements; i++) ((ElementBox) elements[i]).setClosedBottom(val);
            }
            break;
        default : super.setValue(_index-BOXSET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementBox) elements[i]).setClosedTop(true);    break;
        case 1 : for (int i=0; i<numElements; i++) ((ElementBox) elements[i]).setClosedBottom(true); break;
        default: super.setDefaultValue(_index-BOXSET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : 
        case 1 : return "true";
        default : return super.getDefaultValueString(_index-BOXSET_PROPERTIES_ADDED);
      }
    }
    
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : return null;
        default: return super.getValue (_index-BOXSET_PROPERTIES_ADDED);
      }
    }

} // End of class
