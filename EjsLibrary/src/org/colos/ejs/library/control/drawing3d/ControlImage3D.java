/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementImage;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlImage3D extends ControlElement3D {
  static final private int IMAGE_PROPERTIES_ADDED=3;

  private ElementImage image;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementImage"; }

  protected Element createElement () {
    image = new ElementImage();
    return image;
  }

  protected int getPropertiesDisplacement () { return IMAGE_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("imageFile");
      infoList.add ("trueSize");
      infoList.add ("rotationAngle");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("imageFile")) return "File|String TRANSLATABLE";
    if (_property.equals("trueSize")) return "boolean";
    if (_property.equals("rotationAngle")) return "int|double";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : image.setImageFile(_value.getString()); break;
      case 1 : image.setTrueSize(_value.getBoolean()); break;
      case 2 :
          if (_value instanceof IntegerValue) image.setRotationAngle(_value.getInteger()*ControlDrawingPanel3D.TO_RAD);
          else image.setRotationAngle(_value.getDouble());
          break;
      default : super.setValue(_index-IMAGE_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : image.setImageFile(null); break;
      case 1 : image.setTrueSize(false); break;
      case 2 : image.setRotationAngle(0); break;
      default: super.setDefaultValue(_index-IMAGE_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "false";
      case 2 : return "0.0";
      default : return super.getDefaultValueString(_index-IMAGE_PROPERTIES_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 :
          return null;
      default: return super.getValue (_index-IMAGE_PROPERTIES_ADDED);
    }
  }

} // End of class
