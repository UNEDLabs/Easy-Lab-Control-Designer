/*
 * The control.automaticcontrol package contains subclasses of
 * control.ControlElement that are sueful for Automatic Control
 * Copyright (c) December 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.automaticcontrol;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.automaticcontrol.*;
import org.opensourcephysics.display.Drawable;

/**
 * A control valve
 */
public class ControlValve extends ControlPoligonsAndTexts {
  static protected final int VALVE_ADDED = 0;

  protected Valve valve;

  protected Drawable createDrawable () {
    return valve = new Valve();
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  public String getPropertyInfo(String _property) {
    if (_property.equals("type"))           return "int|ValveType";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ValveType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("continuous")) return new IntegerValue (Valve.CONTINUOUS);
      if (_value.equals("discrete"))   return new IntegerValue (Valve.DISCRETE);
      if (_value.equals("triple_continuous")) return new IntegerValue (Valve.TRIPLE_CONTINUOUS);
      if (_value.equals("triple_discrete"))   return new IntegerValue (Valve.TRIPLE_DISCRETE);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

// -------------------------
// Interaction
// -------------------------

} // End of class
