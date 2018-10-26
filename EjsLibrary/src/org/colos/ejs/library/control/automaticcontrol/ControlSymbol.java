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
 * An interactive particle
 */
public class ControlSymbol extends ControlPoligonsAndTexts{
  static protected final int SYMBOL_ADDED = 18;

  protected Symbol symbol;

  protected Drawable createDrawable () {
    return symbol = new Symbol();
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  public String getPropertyInfo(String _property) {
    if (_property.equals("type"))      return "int|SymbolType";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("SymbolType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("circle_1")) return new IntegerValue (Symbol.CIRCLE_1);
      if (_value.equals("circle_2")) return new IntegerValue (Symbol.CIRCLE_2);
      if (_value.equals("circle_3")) return new IntegerValue (Symbol.CIRCLE_3);
      if (_value.equals("circle_4")) return new IntegerValue (Symbol.CIRCLE_4);
      if (_value.equals("circle_5")) return new IntegerValue (Symbol.CIRCLE_5);
      if (_value.equals("diamond_1")) return new IntegerValue (Symbol.DIAMOND_1);
      if (_value.equals("diamond_2")) return new IntegerValue (Symbol.DIAMOND_2);
      if (_value.equals("diamond_3")) return new IntegerValue (Symbol.DIAMOND_3);
      if (_value.equals("rectangle_1")) return new IntegerValue (Symbol.RECTANGLE_1);
      if (_value.equals("rectangle_2")) return new IntegerValue (Symbol.RECTANGLE_2);
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
