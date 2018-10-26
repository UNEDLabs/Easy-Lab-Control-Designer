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

public class ControlSphere extends ControlCylinder {

  protected Drawable createDrawable () {
    InteractiveSphere sphere = new InteractiveSphere();
    sphere.setOrigin(0.5,0.5,0.5,true);
    return sphere;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  public String getPropertyInfo(String _property) {
    if (_property.equals("minanglev"))  return "int|double";
    if (_property.equals("maxanglev"))  return "int|double";
    return super.getPropertyInfo(_property);
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("minangle"))       return "minimumAngleU";
    if (_property.equals("maxangle"))       return "maximumAngleU";
    if (_property.equals("minanglev"))      return "minimumAngleV";
    if (_property.equals("maxanglev"))      return "maximumAngleV";
    return super.getPropertyCommonName(_property);
  }

  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("dx")) return super.setProperty ("sizex","2*"+_value);
    if (_property.equals("dy")) return super.setProperty ("sizey","2*"+_value);
    if (_property.equals("dz")) return super.setProperty ("sizez","2*"+_value);
    if (_property.equals("linecolor")) return super.setProperty ("secondaryColor",_value);
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 4 : ((InteractiveSphere) myElement).setMinAngleV(_value.getInteger());      break;
      case 5 : ((InteractiveSphere) myElement).setMaxAngleV(_value.getInteger());      break;
      default : super.setValue(_index,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 4 : ((InteractiveSphere) myElement).setMinAngleV(0);      break;
      case 5 : ((InteractiveSphere) myElement).setMaxAngleV(360);      break;
      default: super.setDefaultValue(_index); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 4 : return "0";
      case 5 : return "360";
      default : return super.getDefaultValueString(_index);
    }
  }

} // End of interface
