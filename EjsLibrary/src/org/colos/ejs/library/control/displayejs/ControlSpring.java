/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

public class ControlSpring extends ControlInteractiveElement {
  static private final int SPRING_ADDED = 3;
  static private final int MY_ENABLED = ENABLED+SPRING_ADDED;
  static private final int MY_ENABLED_SECONDARY = ENABLED_SECONDARY+SPRING_ADDED;

  public ControlSpring () { super (); enabledEjsEdit = true; }

  protected Drawable createDrawable () {
      InteractiveSpring spring = new InteractiveSpring();
      spring.setEnabled(InteractiveElement.TARGET_SIZE,true); // Backwards compatibility
      return  spring;
  }

  protected int getPropertiesDisplacement () { return SPRING_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("radius");
      infoList.add ("solenoid");
      infoList.add ("thinExtremes");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("color"))            return "lineColor";
    if (_property.equals("secondaryColor"))   return "fillColor";
    if (_property.equals("enabled"))          return "enabledSize";
    if (_property.equals("enabledSecondary")) return "enabledPosition";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("radius"))          return "int|double";
    if (_property.equals("solenoid"))        return "int|double";
    if (_property.equals("thinExtremes"))    return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : ((InteractiveSpring) myElement).setRadius(_value.getDouble()); break;
      case 1 : ((InteractiveSpring) myElement).setSolenoid(_value.getDouble()); break;
      case 2 : ((InteractiveSpring) myElement).setThinExtremes(_value.getBoolean()); break;
      case MY_ENABLED           : myElement.setEnabled(InteractiveElement.TARGET_SIZE,_value.getBoolean()); break;
      case MY_ENABLED_SECONDARY : myElement.setEnabled(InteractiveElement.TARGET_POSITION,_value.getBoolean()); break;
      default: super.setValue(_index-SPRING_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractiveSpring) myElement).setRadius(0.1); break;
      case 1 : ((InteractiveSpring) myElement).setSolenoid(0.0); break;
      case 2 : ((InteractiveSpring) myElement).setThinExtremes(true); break;
      case MY_ENABLED           : myElement.setEnabled(InteractiveElement.TARGET_SIZE,true); break;
      case MY_ENABLED_SECONDARY : myElement.setEnabled(InteractiveElement.TARGET_POSITION,false); break;
      default: super.setDefaultValue(_index-SPRING_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0.1";
      case 1 : return "0";
      case 2 : return "true";
      case MY_ENABLED           : return "true";
      case MY_ENABLED_SECONDARY : return "false";
      default : return super.getDefaultValueString(_index-SPRING_ADDED);
    }
  }

} // End of class
