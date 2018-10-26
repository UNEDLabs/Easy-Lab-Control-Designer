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

/**
 * An interactive set of particles
 */
public class ControlArrowSet extends ControlElementSet {

  protected Drawable createDrawable () {
      elementSet = new ElementSet(1, InteractiveArrow.class);
      elementSet.setEnabled(InteractiveElement.TARGET_SIZE, true); // Backwards compatibility
    return elementSet;
  }

  protected int getPropertiesDisplacement () { return 0; }

// ------------------------------------------------
// Definition of Properties
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

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case STYLE :
        if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveArrow)elementSet.elementAt(i)).setArrowType(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveArrow)elementSet.elementAt(i)).setArrowType(val);
          }
        break;

      case  ENABLED :
        if (_value.getObject() instanceof boolean[]) elementSet.setEnableds(InteractiveElement.TARGET_SIZE,(boolean[]) _value.getObject());
        else elementSet.setEnabled(InteractiveElement.TARGET_SIZE,_value.getBoolean());
        break;
      case  ENABLED_SECONDARY :
        if (_value.getObject() instanceof boolean[]) elementSet.setEnableds(InteractiveElement.TARGET_POSITION,(boolean[]) _value.getObject());
        else elementSet.setEnabled(InteractiveElement.TARGET_POSITION,_value.getBoolean());
        break;

      default: super.setValue(_index,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  ENABLED : elementSet.setEnabled(InteractiveElement.TARGET_SIZE,true); break;
      case  ENABLED_SECONDARY : elementSet.setEnabled(InteractiveElement.TARGET_POSITION,false); break;

      case STYLE :
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveArrow)elementSet.elementAt(i)).setArrowType(InteractiveArrow.ARROW);
        break;
      default: super.setDefaultValue(_index); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  ENABLED : return "true";
      case  ENABLED_SECONDARY : return "false";

      case STYLE :return "ARROW";
      default : return super.getDefaultValueString(_index);
    }
  }

} // End of class
