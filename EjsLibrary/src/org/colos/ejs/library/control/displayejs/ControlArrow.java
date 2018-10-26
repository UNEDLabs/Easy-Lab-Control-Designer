/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.displayejs.*;

public class ControlArrow extends ControlInteractiveElement {

  public ControlArrow () { super (); enabledEjsEdit = true; }

  protected Drawable createDrawable () {
      InteractiveArrow arrow = new InteractiveArrow(InteractiveArrow.ARROW);
      arrow.setEnabled(InteractiveElement.TARGET_SIZE,true); // Backwards compatibility
      return arrow;
  }

  protected int getPropertiesDisplacement () { return 0; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  public String getPropertyCommonName(String _property) {
    if (_property.equals("color"))            return "lineColor";
    if (_property.equals("secondaryColor"))   return "fillColor";
    if (_property.equals("enabled"))          return "enabledSize";
    if (_property.equals("enabledSecondary")) return "enabledPosition";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("style"))           return "ArrowStyle|int";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ArrowStyle")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("arrow"))       return new IntegerValue (InteractiveArrow.ARROW);
      if (_value.equals("segment"))     return new IntegerValue (InteractiveArrow.SEGMENT);
      if (_value.equals("box"))         return new IntegerValue (InteractiveArrow.BOX);
    }
    return super.parseConstant (_propertyType,_value);
  }

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case ENABLED           : myElement.setEnabled(InteractiveElement.TARGET_SIZE,_value.getBoolean()); break;
      case ENABLED_SECONDARY : myElement.setEnabled(InteractiveElement.TARGET_POSITION,_value.getBoolean()); break;
      case STYLE : ((InteractiveArrow) myElement).setArrowType(_value.getInteger()); break;
      default: super.setValue(_index,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case ENABLED           : myElement.setEnabled(InteractiveElement.TARGET_SIZE,true); break;
      case ENABLED_SECONDARY : myElement.setEnabled(InteractiveElement.TARGET_POSITION,false); break;
      case STYLE : ((InteractiveArrow) myElement).setArrowType(InteractiveArrow.ARROW); break;
      default: super.setDefaultValue(_index); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case ENABLED           : return "true";
      case ENABLED_SECONDARY : return "false";
      case STYLE : return "ARROW";
      default : return super.getDefaultValueString(_index);
    }
  }

} // End of class
