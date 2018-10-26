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
public class ControlShapeSet2D extends ControlSet2D {
  static final private int PROPERTIES_ADDED=3;
  
  protected int getPropertiesAddedToSet () { return PROPERTIES_ADDED; }

  protected Element createAnElement() {
    Element el = new ElementShape();
    return el;
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
    super.copyAnElement(oldElement, newElement);
    ((ElementShape)newElement).setShapeType(((ElementShape)oldElement).getShapeType());
    ((ElementShape)newElement).setPixelSize(((ElementShape)oldElement).isPixelSize());
  }

//------------------------------------------------
//Definition of Properties
//------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("style");
      infoList.add ("pixelSize");
      infoList.add ("elementposition");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("style")) return "MarkerShape|int|int[]";
    if (_property.equals("pixelSize")) return "boolean|boolean[]";
    if (_property.equals("elementposition"))return "ElementPosition|int|int[]";
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
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementShape)elements[i]).setShapeType(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0; i<elements.length; i++) ((ElementShape)elements[i]).setShapeType(val);
        }
        break;
      case 1 : 
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementShape)elements[i]).setPixelSize(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<elements.length; i++) ((ElementShape)elements[i]).setPixelSize(val);
        }
        break;
      case 2 : 
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
      case 0 : for (int i=0; i<elements.length; i++) ((ElementShape)elements[i]).setShapeType(ElementShape.ELLIPSE); break;
      case 1 : for (int i=0; i<elements.length; i++) ((ElementShape)elements[i]).setPixelSize(false); break;
      case 2 : for (int i=0; i<elements.length; i++) elements[i].getStyle().setRelativePosition(Style.CENTERED); break; 
      default: super.setDefaultValue(_index-PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "ELLIPSE";
      case 1 : return "false";
      case 2 : return "CENTERED";
      default : return super.getDefaultValueString(_index-PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 :  
        return null;
      default: return super.getValue (_index-PROPERTIES_ADDED);
    }
  }


} // End of class
