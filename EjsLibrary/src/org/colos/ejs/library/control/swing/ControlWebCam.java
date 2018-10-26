/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import com.charliemouse.cambozola.Viewer;

import org.colos.ejs.library.control.value.*;

/**
 * A configurable panel. It has no internal value, nor can trigger
 * any action.
 */
public class ControlWebCam extends ControlSwingElement {
  protected Viewer viewer;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    return viewer = new Viewer ();
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("url");
      infoList.add ("mjpeg");
      infoList.add ("delay");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("url")) return "String";
    if (_property.equals("mjpeg")) return "boolean";
    if (_property.equals("delay")) return "int";
    return super.getPropertyInfo(_property);
  }

  private void setURL (String _url) {
      viewer.stop();
      viewer.setParameterValue("url", "");
      if (_url!=null && _url.trim().length()>0) {
          viewer.setParameterValue("url", _url);
          viewer.init();
          viewer.start();
      }
      //else viewer.init();
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : setURL (_value.getString());  break;
      case 1 : viewer.setMJPEGFormat (_value.getBoolean());  break;
      case 2 : viewer.setImageDelay (_value.getInteger());  break;
      default: super.setValue(_index-3,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : setURL (null); break;
      case 1 : viewer.setMJPEGFormat (false);  break;
      case 2 : viewer.setImageDelay (0);  break;
      default: super.setDefaultValue(_index-3); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "false";
      case 2 : return "0";
      default : return super.getDefaultValueString(_index-3);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 :
        return null;
      default: return super.getValue(_index-3);
    }
  }

} // End of class
