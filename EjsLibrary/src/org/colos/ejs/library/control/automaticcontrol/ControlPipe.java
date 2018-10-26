/*
 * The control.automaticcontrol package contains subclasses of
 * control.ControlElement that are sueful for Automatic Control
 * Copyright (c) December 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.automaticcontrol;

import org.colos.ejs.library.control.displayejs.ControlPoligon;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.automaticcontrol.*;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive particle
 */
public class ControlPipe extends ControlPoligon {
  static protected final int PIPE_ADDED = 4;

  protected Pipe pipe;

  protected Drawable createDrawable () {
      pipe = new Pipe();
      pipe.setData(new double[][] { { 0,0}, {0.2,0} });
    poligon = pipe;
    double[][]data = poligon.getData();
    coordinatesValues     = new ObjectValue[3];
    if (data!=null) {
      coordinatesValues[0] = new ObjectValue(data[0]);
      coordinatesValues[1] = new ObjectValue(data[1]);
      coordinatesValues[2] = new ObjectValue(data[2]);
    }
    return pipe;
  }

  protected int getPropertiesDisplacement () { return PIPE_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("width");
      infoList.add ("filled");
      infoList.add ("endClosed");
      infoList.add ("emptyColor");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("filled"))          return "drawingFill";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("width"))     return "int|double";
    if (_property.equals("filled"))    return "boolean";
    if (_property.equals("endClosed")) return "boolean";
    if (_property.equals("emptyColor"))return "Color|Object";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : pipe.setWidth(_value.getDouble()); break;
      case 1 : pipe.setFilled(_value.getBoolean()); break;
      case 2 : pipe.setEndClosed(_value.getBoolean()); break;
      case 3 :
        {
          java.awt.Paint fill = (java.awt.Paint) _value.getObject();
          if (fill==NULL_COLOR) fill = null;
          pipe.setEmptyPattern(fill);
        }
        break;
      default: super.setValue(_index-PIPE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : pipe.setWidth(0.025); break;
      case 1 : pipe.setFilled(false); break;
      case 2 : pipe.setEndClosed(false); break;
      case 3 : pipe.setEmptyPattern(null); break;
      default: super.setDefaultValue(_index-PIPE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0.025";
      case 1 : return "false";
      case 2 : return "false";
      case 3 : return "<none>";
      default : return super.getDefaultValueString(_index-PIPE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : return null;
      default: return super.getValue (_index-PIPE_ADDED);
    }
  }


} // End of class
