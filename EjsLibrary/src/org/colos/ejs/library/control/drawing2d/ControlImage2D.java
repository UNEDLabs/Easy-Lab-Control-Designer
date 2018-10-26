/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.opensourcephysics.drawing2d.*;
import org.colos.ejs.library.control.value.*;

/**
 * A 2D mImage
 */
public class ControlImage2D extends ControlElement2D {
  static final private int PROPERTIES_ADDED=3;

  protected ElementImage mImage;

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementImage"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    mImage = new ElementImage();
    return mImage;
  }

  protected int getPropertiesDisplacement () { return PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("imageFile");
      infoList.add ("trueSize");
      infoList.add ("elementposition");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("imageFile")) return "File|String TRANSLATABLE";
    if (_property.equals("trueSize")) return "boolean";
    if (_property.equals("elementposition"))return "ElementPosition|int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : mImage.setImageFile(_value.getString()); break;
      case 1 : if (_value.getBoolean()!=mImage.isTrueSize()) mImage.setTrueSize(_value.getBoolean()); break;
      case 2 : if (_value.getInteger()!=mImage.getStyle().getRelativePosition()) mImage.getStyle().setRelativePosition(_value.getInteger()); break; 
      default : super.setValue(_index-PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : mImage.setImageFile(null); break;
      case 1 : mImage.setTrueSize(false); break;
      case 2 : mImage.getStyle().setRelativePosition(Style.CENTERED); break; 
      default: super.setDefaultValue(_index-PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
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
