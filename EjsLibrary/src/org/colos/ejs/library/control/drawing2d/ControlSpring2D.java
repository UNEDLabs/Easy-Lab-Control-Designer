/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.drawing2d.*;


/**
 * A 2D spring
 */
public class ControlSpring2D extends ControlElement2D {
  static final private int SPRING2D_PROPERTIES_ADDED=5;
  
  ElementSpring spring;
  
  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementSpring"; }

  protected org.opensourcephysics.display.Drawable createDrawable () { 
    spring = new ElementSpring();
    return spring;
  }

  protected int getPropertiesDisplacement () { return SPRING2D_PROPERTIES_ADDED; }
  
//------------------------------------------------
//Definition of Properties
//------------------------------------------------

 static java.util.List<String> infoList=null;

 public java.util.List<String> getPropertyList() {
   if (infoList==null) {
     infoList = new java.util.ArrayList<String> ();
     infoList.add ("radius");
     infoList.add ("solenoid");
     infoList.add ("thinExtremes");
     infoList.add ("loops");
     infoList.add ("pointsPerLoop");
     infoList.addAll(super.getPropertyList());
   }
   return infoList;
 }

 
 public String getPropertyInfo(String _property) {
   if (_property.equals("radius"))          return "int|double";
   if (_property.equals("solenoid"))        return "int|double";
   if (_property.equals("thinExtremes"))    return "boolean";
   if (_property.equals("loops"))           return "int";
   if (_property.equals("pointsPerLoop"))   return "int";

   return super.getPropertyInfo(_property);
 }

//------------------------------------------------
//Set and Get the values of the properties
//------------------------------------------------

 public void setValue (int _index, Value _value) {
   switch (_index) {
     case 0 : spring.setRadius(_value.getDouble()); break;
     case 1 : spring.setSolenoid(_value.getDouble()); break;
     case 2 : spring.setThinExtremes(_value.getBoolean()); break;
     case 3 : spring.setResolution(_value.getInteger(),spring.getPointsPerLoop()); break; 
     case 4 : spring.setResolution(spring.getLoops(),_value.getInteger()); break; 
     default : super.setValue(_index-SPRING2D_PROPERTIES_ADDED,_value); break;
   }
 }

 public void setDefaultValue (int _index) {
   switch (_index) {
     case 0 : spring.setRadius(ElementSpring.DEF_RADIUS); break;
     case 1 : spring.setSolenoid(0.0); break;
     case 2 : spring.setThinExtremes(true); break;
     case 3 : spring.setResolution(ElementSpring.DEF_LOOPS,spring.getPointsPerLoop()); break; 
     case 4 : spring.setResolution(spring.getLoops(),ElementSpring.DEF_PPL); break; 
     default: super.setDefaultValue(_index-SPRING2D_PROPERTIES_ADDED); break;
   }
 }

 public String getDefaultValueString (int _index) {
   switch (_index) {
     case 0 : return Double.toString(ElementSpring.DEF_RADIUS);
     case 1 : return "0.0";
     case 2 : return "true";
     case 3 : return Integer.toString(ElementSpring.DEF_LOOPS);
     case 4 : return Integer.toString(ElementSpring.DEF_PPL);
     default : return super.getDefaultValueString(_index-SPRING2D_PROPERTIES_ADDED);
   }
 }

 public Value getValue (int _index) {
   switch (_index) {
     case 0 : case 1 : case 2 : case 3 : case 4 : 
       return null;
     default: return super.getValue (_index-SPRING2D_PROPERTIES_ADDED);
   }
 }


} // End of class
