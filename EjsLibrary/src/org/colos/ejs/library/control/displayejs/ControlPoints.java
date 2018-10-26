/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import java.awt.Color;

import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive particle
 */
public class ControlPoints extends ControlInteractiveElement {
  static final private int POINTS_PROPERTIES_ADDED=1;
  static final private int MY_LINE_COLOR=ControlInteractiveElement.PRIMARY_COLOR+POINTS_PROPERTIES_ADDED;
  static final private int MY_STROKE=ControlInteractiveElement.STROKE+POINTS_PROPERTIES_ADDED;

  protected Drawable createDrawable () {
    return new InteractivePoints();
  }

  protected int getPropertiesDisplacement () { return 0; }

// ------------------------------------------------
// Properties
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
    if (_property.equals("data"))             return "double[][]";
    if (_property.equals("color"))          return "int|int[]|Color|Color[]|Object|Object[]";
    if (_property.equals("stroke"))         return "int|int[]|double|double[]|Object";

    if (_property.equals("z"))           return "int|double UNNECESSARY";
    if (_property.equals("sizez"))       return "int|double UNNECESSARY";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get values
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getObject() instanceof double[][]) ((InteractivePoints) myElement).setData((double[][])_value.getObject()); break;
      default : super.setValue(_index-POINTS_PROPERTIES_ADDED,_value); break;
      
      case MY_LINE_COLOR :
        if (_value.getObject() instanceof int[]) ((InteractivePoints) getDrawable()).setColors((int[]) _value.getObject());
        else if (_value.getObject() instanceof Object[]) ((InteractivePoints) getDrawable()).setColors((Color[]) _value.getObject());
        else super.setValue(_index-POINTS_PROPERTIES_ADDED, _value);
        break;

      case MY_STROKE :
        if (_value.getObject() instanceof int[]) ((InteractivePoints) getDrawable()).setWidths((int[]) _value.getObject());
        else if (_value.getObject() instanceof double[]) ((InteractivePoints) getDrawable()).setWidths((double[]) _value.getObject());
        else super.setValue(_index-POINTS_PROPERTIES_ADDED, _value);
        break;

    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractivePoints) myElement).setData(null); break;
      default : super.setDefaultValue(_index-POINTS_PROPERTIES_ADDED); break;
      
      case MY_LINE_COLOR : 
        ((InteractivePoints) getDrawable()).setColors((Color[]) null);
        super.setDefaultValue(_index-POINTS_PROPERTIES_ADDED);
        break;

      case MY_STROKE : 
        ((InteractivePoints) getDrawable()).setWidths((double[]) null);
        super.setDefaultValue(_index-POINTS_PROPERTIES_ADDED);
        break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 : return "<none>";
      default : return super.getDefaultValueString(_index-POINTS_PROPERTIES_ADDED);
    }
  }

} // End of class
