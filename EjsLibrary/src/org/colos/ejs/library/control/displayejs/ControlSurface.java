/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;

import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;

public class ControlSurface extends ControlInteractiveTile {
  static final int PROPERTIES_SURFACE=PROPERTIES_ADDED+1;
  static final int MY_PRIMARY_COLOR=PRIMARY_COLOR+PROPERTIES_SURFACE;
  static final int MY_SECONDARY_COLOR=SECONDARY_COLOR+PROPERTIES_SURFACE;

  protected Drawable createDrawable () {
    InteractiveSurface surface = new InteractiveSurface();
      // setDemoData (surface);
    surface.setOrigin(0,0,0,true);
    sizeValues = new DoubleValue[] { new DoubleValue(1), new DoubleValue(1), new DoubleValue(1)};
    return surface;
  }

 protected int getPropertiesDisplacement () { return PROPERTIES_SURFACE; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("data");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))         return "double[][][]";
    return super.getPropertyInfo(_property);
  }

  // Backwards compatibility
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("linecolor")) return super.setProperty ("secondaryColor",_value);
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getObject() instanceof double[][][]) ((InteractiveSurface) myElement).setData((double[][][])_value.getObject()); break;
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
      default: super.setValue(_index-1,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractiveSurface) myElement).setData(null); /* setDemoData ((InteractiveSurface) myElement); */    break;
      case MY_PRIMARY_COLOR : myElement.getStyle().setFillPattern(java.awt.Color.blue); break;
      case MY_SECONDARY_COLOR : myElement.getStyle().setEdgeColor(java.awt.Color.black); break;
      default: super.setDefaultValue(_index-1); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case MY_PRIMARY_COLOR : return "BLUE";
      case MY_SECONDARY_COLOR : return "BLACK";
      default : return super.getDefaultValueString(_index-1);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return null;
      default: return getValue (_index-1);
    }
  }

// -------------------------------------
// Private methods
// -------------------------------------

//  static private void setDemoData (InteractiveSurface _surface) {
//    int nu = 16, nv = 16;
//    double[][][] data = new double[nu][nv][3];
//    for (int i = 0; i < nu; i++)
//      for (int j = 0; j < nv; j++) {
//        double r = i * 1.0 / (nu - 1);
//        double angle = j * 2.0 * Math.PI / (nv - 1);
//        data[i][j][0] = r * Math.cos(angle);
//        data[i][j][1] = r * Math.sin(angle);
//        if (r==0.0) data[i][j][2] = 0.5;
//        else if (r<0.5) data[i][j][2] = 0.5*Math.max(0.0, Math.sin(4*Math.PI*r)/(4*Math.PI*r));
//        else data[i][j][2] = 0.5*Math.max(0.0, Math.sin(4*Math.PI*r)/(4*r));
//      }
//    _surface.setData(data);
//  }

} // End of interface
