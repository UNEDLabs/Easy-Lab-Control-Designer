/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.displayejs.*;

public class ControlCylinder extends ControlInteractiveTile {
  static final int PROPERTIES_CYLINDER=PROPERTIES_ADDED+10;
  static final int MY_PRIMARY_COLOR=PRIMARY_COLOR+PROPERTIES_CYLINDER;
  static final int MY_SECONDARY_COLOR=SECONDARY_COLOR+PROPERTIES_CYLINDER;

  protected Drawable createDrawable () {
    InteractiveCylinder cylinder = new InteractiveCylinder();
    cylinder.setOrigin(0.5,0.5,0,true);
    return cylinder;
  }

 protected int getPropertiesDisplacement () { return PROPERTIES_CYLINDER; }

 // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("direction");
      infoList.add ("axes");
      infoList.add ("minangle");
      infoList.add ("maxangle");
      infoList.add ("minanglev");
      infoList.add ("maxanglev");

      infoList.add ("closedBottom");
      infoList.add ("closedTop");
      infoList.add ("closedLeft");
      infoList.add ("closedRight");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("minangle")) return "minimumAngle";
    if (_property.equals("maxangle")) return "maximumAngle";
    return super.getPropertyCommonName(_property);
  }


  public String getPropertyInfo(String _property) {
    if (_property.equals("direction"))  return "Axis|int";
    if (_property.equals("axes"))       return "double[]";
    if (_property.equals("minangle"))   return "int|double";
    if (_property.equals("maxangle"))   return "int|double";
    if (_property.equals("closedBottom"))  return "boolean";
    if (_property.equals("closedTop"))     return "boolean";
    if (_property.equals("closedRight"))   return "boolean";
    if (_property.equals("closedLeft"))    return "boolean";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("Axis")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.startsWith("x"))      return new IntegerValue (InteractiveCylinder.X_AXIS);
      if (_value.startsWith("y"))      return new IntegerValue (InteractiveCylinder.Y_AXIS);
      if (_value.startsWith("z"))      return new IntegerValue (InteractiveCylinder.Z_AXIS);
      if (_value.equals("custom"))     return new IntegerValue (InteractiveCylinder.USER_DEFINED);
    }
    return super.parseConstant (_propertyType,_value);
  }


  // Backwards compatibility
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("dx")) return super.setProperty ("sizex","2*"+_value);
    if (_property.equals("dy")) return super.setProperty ("sizey","2*"+_value);
    if (_property.equals("dz")) return super.setProperty ("sizez",_value);
    if (_property.equals("linecolor")) return super.setProperty ("secondaryColor",_value);
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : ((InteractiveCylinder) myElement).setDirection(_value.getInteger());      break;
      case 1 : if (_value.getObject() instanceof double[]) ((InteractiveCylinder) myElement).setCustomAxes((double[])_value.getObject()); break;
      case 2 : ((InteractiveCylinder) myElement).setMinAngleU(_value.getInteger());      break;
      case 3 : ((InteractiveCylinder) myElement).setMaxAngleU(_value.getInteger());      break;
      case 4 : break;
      case 5 : break;
      case 6 : ((InteractiveCylinder) myElement).setClosedBottom(_value.getBoolean()); break;
      case 7 : ((InteractiveCylinder) myElement).setClosedTop(_value.getBoolean());    break;
      case 8 : ((InteractiveCylinder) myElement).setClosedLeft(_value.getBoolean());   break;
      case 9 : ((InteractiveCylinder) myElement).setClosedRight(_value.getBoolean());  break;
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
      default : super.setValue(_index-10,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractiveCylinder) myElement).setDirection(InteractiveCylinder.Z_AXIS);      break;
      case 1 : break; // do nothing
      case 2 : ((InteractiveCylinder) myElement).setMinAngleU(0);      break;
      case 3 : ((InteractiveCylinder) myElement).setMaxAngleU(360);      break;
      case 4 : break;
      case 5 : break;
      case 6 : ((InteractiveCylinder) myElement).setClosedBottom(true); break;
      case 7 : ((InteractiveCylinder) myElement).setClosedTop(true);    break;
      case 8 : ((InteractiveCylinder) myElement).setClosedLeft(true);   break;
      case 9 : ((InteractiveCylinder) myElement).setClosedRight(true);  break;
      case MY_PRIMARY_COLOR : myElement.getStyle().setFillPattern(java.awt.Color.blue); break;
      case MY_SECONDARY_COLOR : myElement.getStyle().setEdgeColor(java.awt.Color.black); break;
      default: super.setDefaultValue(_index-10); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "Z_AXIS";
      case 1 : return "<none>";
      case 2 : return "0";
      case 3 : return "360";
      case 4 : return "<none>";
      case 5 : return "<none>";
      case 6 :
      case 7 :
      case 8 :
      case 9 : return "true";
      case MY_PRIMARY_COLOR : return "BLUE";
      case MY_SECONDARY_COLOR : return "BLACK";
      default : return super.getDefaultValueString(_index-10);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 : case 8 : case 9 :
        return null;
      default: return getValue (_index-10);
    }
  }

} // End of interface
