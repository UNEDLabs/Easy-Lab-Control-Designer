/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementShape;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlShape3D extends ControlElement3D {
  static final private int SHAPE_PROPERTIES_ADDED=3;

  private ElementShape shape;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementShape"; }

  protected Element createElement () {
    shape = new ElementShape();
    return shape;
  }

  protected int getPropertiesDisplacement () { return SHAPE_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

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
    if (_property.equals("style")) return "MarkerShape|int";
    if (_property.equals("pixelSize")) return "boolean";
    if (_property.equals("rotationAngle")) return "int|double";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : shape.setShapeType(_value.getInteger()); break;
      case 1 : shape.setPixelSize(_value.getBoolean()); break;
      case 2 :
          if (_value instanceof IntegerValue) shape.setRotationAngle(_value.getInteger()*ControlDrawingPanel3D.TO_RAD);
          else shape.setRotationAngle(_value.getDouble());
          break;
      default : super.setValue(_index-SHAPE_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : shape.setShapeType(ElementShape.ELLIPSE); break;
      case 1 : shape.setPixelSize(false); break;
      case 2 : shape.setRotationAngle(0); break;
      default: super.setDefaultValue(_index-SHAPE_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "ELLIPSE";
      case 1 : return "false";
      case 2 : return "0.0";
      default : return super.getDefaultValueString(_index-SHAPE_PROPERTIES_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 :
          return null;
      default: return super.getValue (_index-SHAPE_PROPERTIES_ADDED);
    }
  }

} // End of class
