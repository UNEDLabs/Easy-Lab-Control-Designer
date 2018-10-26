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
public class ControlTextSet extends ControlElementSet {

  protected Drawable createDrawable () {
      elementSet = new ElementSet(1, InteractiveText.class);
      elementSet.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
    return elementSet;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  public String getPropertyInfo(String _property) {
    if (_property.equals("text"))             return "String|String[] TRANSLATABLE";
    return super.getPropertyInfo(_property);
  }

  protected int getPropertiesDisplacement () { return 0; }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case TEXT :
        if (_value instanceof ObjectValue) {
          String[] val = (String[])_value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setDisplayObject(val[i]);
        }
        else {
          String val = _value.toString();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setDisplayObject(val);
        }
        break;
      default: super.setValue(_index,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  TEXT : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setDisplayObject(""); break;
      default: super.setDefaultValue(_index); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case TEXT : return "<none>";
      default : return super.getDefaultValueString(_index);
    }
  }


} // End of class
