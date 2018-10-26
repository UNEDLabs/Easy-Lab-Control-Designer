/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;

import org.colos.ejs.library.control.value.*;

import es.uhu.augmented_reality.AbstractElementAR;
import es.uhu.augmented_reality.ElementRemoteAR;


/**
 * An WebCam image interactive
 */
public class ControlARSystem3D extends ControlARSystem3DAbstract {
  static protected final int AR_ADDED = 3; // Number of new properties
  
  static public final int AR_URL     = 0; // The URL of the element
  static public final int AR_USER    = 1; // The X resolution
  static public final int AR_PASS    = 2; // The X resolution

  private ElementRemoteAR mARRemoteSystem;
  
  protected AbstractElementAR createElementAR() {
    return mARRemoteSystem = new ElementRemoteAR(); // mARRemoteSystem = ElementRemoteAR.createConnection("");
  }

  @Override
  public Object getObject() { return mARRemoteSystem; }

  @Override
  public String getObjectClassname () { return "es.uhu.augmented_reality.ElementRemoteAR"; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("url");
      infoList.add ("username");
      infoList.add ("password");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("url"))      return "String";
    if (_property.equals("username")) return "String";
    if (_property.equals("password")) return "String";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case AR_URL  : 
        mARRemoteSystem.setURL(_value.getString()); 
        //mARSystem = ElementRemoteAR.createConnection(_value.getString());
        break;
      case AR_USER : mARRemoteSystem.setUsername(_value.getString()); break;
      case AR_PASS : mARRemoteSystem.setPassword(_value.getString()); break;
        
      default : super.setValue(_index-AR_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case AR_URL  : mARRemoteSystem.setURL(null);
      case AR_USER : mARRemoteSystem.setUsername(null); break;
      case AR_PASS : mARRemoteSystem.setPassword(null); break;

      default: super.setDefaultValue(_index-AR_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case AR_URL  : 
      case AR_USER : 
      case AR_PASS : 
        return "<none>";

      default : 
        return super.getDefaultValueString(_index-AR_ADDED);    
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case AR_URL  : 
      case AR_USER : 
      case AR_PASS : 
        return null;
      default: 
        return super.getValue (_index-AR_ADDED);
    }
  }

} // End of class


