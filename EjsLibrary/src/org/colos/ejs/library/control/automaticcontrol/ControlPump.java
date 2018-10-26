/*
 * The control.automaticcontrol package contains subclasses of
 * control.ControlElement that are sueful for Automatic Control
 * Copyright (c) December 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.automaticcontrol;

import org.colos.ejs.library.control.displayejs.ControlDrawingPanel3D;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.automaticcontrol.*;
import org.opensourcephysics.display.Drawable;

/**
 * A control valve
 */
public class ControlPump extends ControlPoligonsAndTexts {
  static protected final int PUMP_ADDED = 1;
  static protected final int MY_FILLCOLOR2 = FILLCOLOR2+PUMP_ADDED;

  protected Pump pump;

  protected Drawable createDrawable () {
    return pump = new Pump();
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      // For the poligons
      infoList.add ("rotorAngle");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("rotorAngle"))     return "int|double";
    if (_property.equals("type"))           return "int|PumpType";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("PumpType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("left"))    return new IntegerValue (Pump.LEFT);
      if (_value.equals("right"))   return new IntegerValue (Pump.RIGHT);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :
          if (_value instanceof IntegerValue) pump.setRotorAngle(_value.getInteger()*ControlDrawingPanel3D.TO_RAD);
          else pump.setRotorAngle(_value.getDouble());
          break;
        default: super.setValue(_index-PUMP_ADDED,_value); break;
      }
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : pump.setRotorAngle(Double.NaN); break;
        default: super.setDefaultValue(_index-PUMP_ADDED); break;
        case MY_FILLCOLOR2 : pump.setFillColor2(java.awt.Color.BLACK); break;
      }
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "NaN";
        default : return super.getDefaultValueString(_index-PUMP_ADDED);
        case MY_FILLCOLOR2 : return "BLACK";
      }
   }

    public Value getValue (int _index) {
      switch (_index) {
        case  0 :
          return null;
        default: return super.getValue (_index-PUMP_ADDED);
      }
    }

// -------------------------
// Interaction
// -------------------------

    static private final int[] posSpot = {POSITION_X+PAT_ADDED+PUMP_ADDED,
                                          POSITION_Y+PAT_ADDED+PUMP_ADDED,
                                          POSITION_Z+PAT_ADDED+PUMP_ADDED};
    int[] getPosSpot ()  { return posSpot; }
    int getValueIndex ()  { return VALUE+PUMP_ADDED; }

} // End of class
