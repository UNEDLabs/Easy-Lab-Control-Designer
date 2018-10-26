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

/**
 * An interactive set of particles
 */
public class ControlSpringSet extends ControlElementSet {

  protected Drawable createDrawable () {
      elementSet = new ElementSet(1, InteractiveSpring.class);
      elementSet.setEnabled(InteractiveElement.TARGET_SIZE, true); // Backwards compatibility
    return elementSet;
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
    if (_property.equals("radius"))          return "int|double|double[]";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case RADIUS :
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveSpring)elementSet.elementAt(i)).setRadius(val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveSpring)elementSet.elementAt(i)).setRadius(val);
          }
        break;
      case ENABLED :
        if (_value.getObject() instanceof boolean[]) elementSet.setEnableds(InteractiveElement.TARGET_SIZE,(boolean[]) _value.getObject());
        else elementSet.setEnabled(InteractiveElement.TARGET_SIZE,_value.getBoolean());
        break;
      case ENABLED_SECONDARY :
        if (_value.getObject() instanceof boolean[]) elementSet.setEnableds(InteractiveElement.TARGET_POSITION,(boolean[]) _value.getObject());
        else elementSet.setEnabled(InteractiveElement.TARGET_POSITION,_value.getBoolean());
        break;

      default: super.setValue(_index,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case RADIUS : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveSpring)elementSet.elementAt(i)).setRadius(0.1); break;
      case ENABLED : elementSet.setEnabled(InteractiveElement.TARGET_SIZE,true); break;
      case ENABLED_SECONDARY : elementSet.setEnabled(InteractiveElement.TARGET_POSITION,false); break;
      default: super.setDefaultValue(_index); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case RADIUS : return "0.1";
      case ENABLED : return "true";
      case ENABLED_SECONDARY : return "false";
      default : return super.getDefaultValueString(_index);
    }
  }

} // End of class
