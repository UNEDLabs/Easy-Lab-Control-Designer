/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;

import org.colos.ejs.library.control.value.*;

import es.uhu.augmented_reality.AbstractElementAR;
import es.uhu.augmented_reality.ElementLocalAR;


/**
 * An WebCam image interactive
 */
public class ControlARLocalSystem3D extends ControlARSystem3DAbstract {
  static protected final int AR_LOCAL_ADDED = 1; // Number of new properties
  
  static public final int AR_PORT  = 0; // The device to which the camera is attached to

  private ElementLocalAR mARLocalSystem;

  protected AbstractElementAR createElementAR() {
    return mARLocalSystem = new ElementLocalAR();
  }

  @Override
  public Object getObject() { return mARLocalSystem; }

  @Override
  public String getObjectClassname () { return "es.uhu.augmented_reality.ElementLocalAR"; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("port");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("port"))   return "String";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case AR_PORT : mARLocalSystem.setPort(_value.getString()); break;
        
      default : super.setValue(_index-AR_LOCAL_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case AR_PORT : mARLocalSystem.setPort(null); break;

      default: super.setDefaultValue(_index-AR_LOCAL_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case AR_PORT : 
        return "<none>";

      default : 
        return super.getDefaultValueString(_index-AR_LOCAL_ADDED);    
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case AR_PORT  : 
        return null;
      default: 
        return super.getValue (_index-AR_LOCAL_ADDED);
    }
  }

} // End of class


