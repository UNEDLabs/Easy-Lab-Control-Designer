/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlLight3D extends ControlElement3D {
  protected static final int LIGHT_PROPERTIES_ADDED = 6;

  static protected final int TYPE = 0;
  static protected final int ON = 1;
  static protected final int AMBIENT_FACTOR = 2;
  static protected final int ATTENUATION = 3;
  static protected final int CONCENTRATION = 4;
  static protected final int ANGLE = 5;
  
  static protected final int LIGHT_VISIBLE = ControlElement3D.VISIBLE + LIGHT_PROPERTIES_ADDED;

  protected int defType;
  protected double defAmbientFactor, defConcentration, defAngle;
  protected double[] defAttenuation;

  private ElementLight light; 

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementLight"; }

  protected Element createElement () { 
    light = new ElementLight();
    defType = light.getType();
    defAmbientFactor = light.getAmbientFactor();
    defConcentration = light.getConcentrationLight();
    defAngle = light.getAngleLight();
    defAttenuation = light.getAttenuationLight();
    return light;
  }

  protected int getPropertiesDisplacement () { return LIGHT_PROPERTIES_ADDED; }
  
//------------------------------------------------
//Definition of Properties
//------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("type");
      infoList.add ("on");
      infoList.add ("ambientFactor");
      infoList.add ("attenuation");
      infoList.add ("concentration");
      infoList.add ("angle");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("type"))  return "3DLightType|int";
    if (_property.equals("on"))    return "boolean";
    if (_property.equals("ambientFactor"))  return "int|double";
    if (_property.equals("attenuation"))    return "int|double|double[]";
    if (_property.equals("concentration")) return "int|double";
    if (_property.equals("angle")) return "int|double";
    return super.getPropertyInfo(_property);
  }

  public void updatePanel() {
    DrawingPanel3D panel = light.getPanel();
    if (panel!=null) panel.update();
  }
  
  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("3DLightType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("ambient"))     return new IntegerValue (ElementLight.TYPE_AMBIENT);
      if (_value.equals("directional")) return new IntegerValue (ElementLight.TYPE_DIRECTIONAL);
      if (_value.equals("point"))       return new IntegerValue (ElementLight.TYPE_POINT);
      if (_value.equals("spot"))        return new IntegerValue (ElementLight.TYPE_SPOT);
    }
    return super.parseConstant (_propertyType,_value);
  }
// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case TYPE : light.setType(_value.getInteger());	   break;
      case ON :   light.setOn(_value.getBoolean()); break;
      case AMBIENT_FACTOR: light.setAmbientFactor(_value.getDouble()); break;
      case ATTENUATION:
        if (_value.getObject() instanceof double[]) {
          light.setAttenuationLight((double[]) _value.getObject());
        }
        else {
          double value = _value.getDouble();
          light.setAttenuationLightXYZ(value,value,value);
        }
        break;
      case CONCENTRATION: light.setConcentrationLight(_value.getDouble()); break;
      case ANGLE: light.setAngleLight(_value.getDouble()); break;
      default : super.setValue(_index-LIGHT_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case TYPE: light.setType(defType); break;
      case ON : light.setOn(true); break;
      case AMBIENT_FACTOR: light.setAmbientFactor(defAmbientFactor); break;
      case ATTENUATION:    light.setAttenuationLight(defAttenuation); break;
      case CONCENTRATION:  light.setConcentrationLight(defConcentration); break;
      case ANGLE:          light.setAngleLight(defAngle); break;
      default: super.setDefaultValue(_index-LIGHT_PROPERTIES_ADDED); break;
    
      case LIGHT_VISIBLE : light.setVisible(false);
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case TYPE : 
        switch (defType) {
          case ElementLight.TYPE_AMBIENT : return "AMBIENT";
          case ElementLight.TYPE_DIRECTIONAL : return "DIRECTIONAL";
          case ElementLight.TYPE_SPOT : return "SPOT";
          default : return "POINT";
        }
      case ON :            return "true";
      case AMBIENT_FACTOR: return ""+defAmbientFactor;
      case ATTENUATION:    return "new double[]{"+defAttenuation[0]+","+defAttenuation[1]+","+defAttenuation[2]+"}";
      case CONCENTRATION:  return ""+defConcentration;
      case ANGLE:          return ""+defAngle;
      default : return super.getDefaultValueString(_index-LIGHT_PROPERTIES_ADDED);

      case LIGHT_VISIBLE : return "false";
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case TYPE : 
      case ON : 
      case AMBIENT_FACTOR: 
      case ATTENUATION: 
      case CONCENTRATION: 
      case ANGLE: 
        return null; 
      default: return super.getValue (_index-LIGHT_PROPERTIES_ADDED);
    }
  }

  
} // End of interface
