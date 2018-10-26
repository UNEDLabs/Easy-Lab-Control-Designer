/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.drawing2d.*;


/**
 * A set of arrows
 */
public class ControlSegmentSet2D extends ControlSet2D {
  static final private int PROPERTIES_ADDED=1;
  
  protected int getPropertiesAddedToSet () { return PROPERTIES_ADDED; }

  protected Element createAnElement() {
    Element el = new ElementSegment();
    return el;
  }
  
//------------------------------------------------
//Definition of Properties
//------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("elementposition");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
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
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setRelativePosition(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0; i<elements.length; i++) elements[i].getStyle().setRelativePosition(val);
        }
        break;

      default : super.setValue(_index-PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : for (int i=0; i<elements.length; i++) elements[i].getStyle().setRelativePosition(Style.NORTH_EAST); break; 
      default: super.setDefaultValue(_index-PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "NORTH_EAST";

      default : return super.getDefaultValueString(_index-PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 :
        return null;
      default: return super.getValue (_index-PROPERTIES_ADDED);
    }
  }


} // End of class
