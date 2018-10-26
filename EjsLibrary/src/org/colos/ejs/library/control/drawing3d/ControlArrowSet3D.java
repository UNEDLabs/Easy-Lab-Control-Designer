/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of arrows
 */
public class ControlArrowSet3D extends ControlSet3D {
  static final private int ARROWSET_PROPERTIES_ADDED=1;

  protected int getPropertiesAddedToSet () { return ARROWSET_PROPERTIES_ADDED; }

  protected Element createAnElement() {
    Element el = new ElementArrow();
    return el;
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
    super.copyAnElement(oldElement, newElement);
    ((ElementArrow)newElement).setArrowType(((ElementArrow)oldElement).getArrowType());
  }

//------------------------------------------------
//Definition of Properties
//------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("style");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("style")) return "ArrowStyle|int|int[]";
    if (_property.equals("elementposition"))return "ArrowPosition|int|int[]";
    return super.getPropertyInfo(_property);
  }

  //------------------------------------------------
  //Set and Get the values of the properties
  //------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : 
        if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((ElementArrow)elements[i]).setArrowType(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0; i<numElements; i++) ((ElementArrow)elements[i]).setArrowType(val);
        }
        break;
      default : super.setValue(_index-ARROWSET_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : for (int i=0; i<numElements; i++) ((ElementArrow)elements[i]).setArrowType(ElementArrow.ARROW); break;
      default: super.setDefaultValue(_index-ARROWSET_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "ARROW";

      default : return super.getDefaultValueString(_index-ARROWSET_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : 
        return null;
      default: return super.getValue (_index-ARROWSET_PROPERTIES_ADDED);
    }
  }

} // End of class
