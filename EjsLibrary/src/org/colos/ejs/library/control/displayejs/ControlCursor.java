/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive cursor
 */
public class ControlCursor extends ControlInteractiveElement {
  static private final int CURSOR_ADDED = 0;
  static private final int MY_STYLE=STYLE+CURSOR_ADDED;
  static private final int MY_SECONDARY_COLOR=SECONDARY_COLOR+CURSOR_ADDED;

  public ControlCursor () { super (); enabledEjsEdit = true; }

  protected Drawable createDrawable () {
      InteractiveCursor cursor = new InteractiveCursor();
      cursor.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
      return cursor;
  }

  protected int getPropertiesDisplacement () { return CURSOR_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  public String getPropertyInfo(String _property) {
    if (_property.equals("style"))          return "CursorShape|int";
    if (_property.equals("z"))           return "int|double UNNECESSARY";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("CursorShape")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("crosshair"))   return new IntegerValue (InteractiveCursor.CROSSHAIR);
      if (_value.equals("horizontal"))  return new IntegerValue (InteractiveCursor.HORIZONTAL);
      if (_value.equals("vertical"))    return new IntegerValue (InteractiveCursor.VERTICAL);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get values
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case MY_STYLE : ((InteractiveCursor) myElement).setType (_value.getInteger()); break;
      case MY_SECONDARY_COLOR : 
        if (_value instanceof IntegerValue) myElement.getStyle().setEdgeColor(DisplayColors.getLineColor(_value.getInteger()));
        else myElement.getStyle().setEdgeColor((java.awt.Color) _value.getObject()); 
        break;
      default : super.setValue(_index-CURSOR_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case MY_STYLE : ((InteractiveCursor) myElement).setType (InteractiveCursor.CROSSHAIR); break;
      case MY_SECONDARY_COLOR : myElement.getStyle().setEdgeColor(java.awt.Color.black); break;
      default : super.setDefaultValue(_index-CURSOR_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case MY_STYLE : return "CROSSHAIR";
      case MY_SECONDARY_COLOR : return "BLACK";
      default : return super.getDefaultValueString(_index-CURSOR_ADDED);
    }
  }

  public Value getValue (int _index) {
  switch (_index) {
    default: return super.getValue(_index-CURSOR_ADDED);
  }
}


} // End of class
