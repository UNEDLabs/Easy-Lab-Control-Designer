/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

public class ControlBox extends ControlInteractiveTile {
  static final int PROPERTIES_BOX=PROPERTIES_ADDED+2;

  private InteractiveBox box;
  
  protected Drawable createDrawable () {
    box = new InteractiveBox();
    box.setOrigin(0,0,0,true);
    return box;
  }

 protected int getPropertiesDisplacement () { return PROPERTIES_BOX; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("closedBottom");
      infoList.add ("closedTop");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("color"))          return "lineColor";
    if (_property.equals("secondaryColor")) return "fillColor";
    return super.getPropertyCommonName(_property);
  }


  public String getPropertyInfo(String _property) {
    if (_property.equals("closedBottom"))  return "boolean";
    if (_property.equals("closedTop"))     return "boolean";
    return super.getPropertyInfo(_property);
  }

  // Backwards compatibility
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("dx")) return super.setProperty ("sizex",_value);
    if (_property.equals("dy")) return super.setProperty ("sizey",_value);
    if (_property.equals("dz")) return super.setProperty ("sizez",_value);
    if (_property.equals("linecolor")) return super.setProperty ("secondaryColor",_value);
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  0 : box.setClosedBottom(_value.getBoolean()); break;
      case  1 : box.setClosedTop(_value.getBoolean());    break;
      default: super.setValue(_index-2,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 : box.setClosedBottom(true); break;
      case  1 : box.setClosedTop(true);    break;
      default: super.setDefaultValue(_index-2); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 :
      case  1 : return "true";
      default : return super.getDefaultValueString(_index-2);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : return null;
      default: return getValue (_index-2);
    }
  }

} // End of interface
