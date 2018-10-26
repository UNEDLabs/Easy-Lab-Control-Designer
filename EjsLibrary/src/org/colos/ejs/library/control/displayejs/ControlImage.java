/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive particle
 */
public class ControlImage extends ControlInteractiveElement {
  static private final int IMAGE_ADDED=2;
  protected InteractiveImage image;
  private String imageFile = null;

  public ControlImage () { super (); enabledEjsEdit = true; }

  protected Drawable createDrawable () {
      image = new InteractiveImage();
      image.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
    return image;
  }

  protected int getPropertiesDisplacement () { return IMAGE_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("image");
      infoList.add ("trueSize");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("image"))    return "File|String TRANSLATABLE";
    if (_property.equals("trueSize")) return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  0 : setImage(_value.getString());   break;
      case  1 : image.setTrueSize(_value.getBoolean());   break;
      default: super.setValue(_index-IMAGE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 : setImage(null);   break;
      case  1 : image.setTrueSize(false);   break;
      default: super.setDefaultValue(_index-IMAGE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "false";
      default : return super.getDefaultValueString(_index-IMAGE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : return null;
      default: return super.getValue(_index-IMAGE_ADDED);
    }
  }

// -------------------------------------
// private methods
// -------------------------------------

  private void setImage (String _image) {
    if (imageFile!=null && imageFile.equals(_image)) return; // no need to do it again
    imageFile = _image;
    java.awt.Image theImage =  org.opensourcephysics.tools.ResourceLoader.getImage(_image);
    if (theImage!=null) image.getStyle().setDisplayObject(theImage);
  }

} // End of class
