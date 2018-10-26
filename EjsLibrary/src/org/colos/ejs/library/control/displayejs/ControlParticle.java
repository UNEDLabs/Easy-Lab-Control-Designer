/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive particle
 */
public class ControlParticle extends ControlInteractiveElement {
  static private final int PARTICLE_ADDED = 1;
  static private final int MY_STYLE=STYLE+PARTICLE_ADDED;
  static private final int MY_PRIMARY_COLOR=PRIMARY_COLOR+PARTICLE_ADDED;
  static private final int MY_SECONDARY_COLOR=SECONDARY_COLOR+PARTICLE_ADDED;


  public ControlParticle () { super (); enabledEjsEdit = true; }

  protected Drawable createDrawable () {
      InteractiveParticle particle = new InteractiveParticle();
      particle.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
      return particle;
  }

  protected int getPropertiesDisplacement () { return PARTICLE_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("pixelSize");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
      if (_property.equals("pixelSize")) return "boolean";
      return super.getPropertyInfo(_property);
    }


// ------------------------------------------------
// Set and Get values
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : ((InteractiveParticle) myElement).setPixelSize(_value.getBoolean()); break;

      case MY_STYLE           : ((InteractiveParticle) myElement).setShapeType (_value.getInteger()); break;
      case MY_PRIMARY_COLOR   :
        if (_value instanceof IntegerValue) myElement.getStyle().setFillPattern(DisplayColors.getLineColor(_value.getInteger()));
        else {
          java.awt.Paint fill = (java.awt.Paint) _value.getObject();
          if (fill==NULL_COLOR) fill = null;
          myElement.getStyle().setFillPattern(fill);
        }
      break;
      case MY_SECONDARY_COLOR : 
        if (_value instanceof IntegerValue) myElement.getStyle().setEdgeColor(DisplayColors.getLineColor(_value.getInteger()));
        else myElement.getStyle().setEdgeColor((java.awt.Color) _value.getObject()); break;
      default : super.setValue(_index-PARTICLE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractiveParticle) myElement).setPixelSize(false); break;

      case MY_STYLE         : ((InteractiveParticle) myElement).setShapeType (InteractiveParticle.ELLIPSE); break;
      case MY_PRIMARY_COLOR : myElement.getStyle().setFillPattern(java.awt.Color.blue); break;
      case MY_SECONDARY_COLOR : myElement.getStyle().setEdgeColor(java.awt.Color.black); break;
      default : super.setDefaultValue(_index-PARTICLE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "false";

      case MY_STYLE         : return "ELLIPSE";
      case MY_PRIMARY_COLOR : return "BLUE";
      case MY_SECONDARY_COLOR : return "BLACK";
      default : return super.getDefaultValueString(_index-PARTICLE_ADDED);
    }
  }

  public Value getValue (int _index) {
  switch (_index) {
    case 0 : return null;
    default: return super.getValue(_index-PARTICLE_ADDED);
  }
}


} // End of class
