/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import java.awt.*;

import org.opensourcephysics.display2d.VectorColorMapper;
import org.opensourcephysics.drawing2d.*;
import org.colos.ejs.library.control.value.*;

/**
 * A group of arrows that implements a simpler 2D vector field
 */
public class ControlVectorField2D extends ControlElement2D {
  static protected final int VECTORFIELD_ADDED=20;
  static protected final int X_COMPONENT=4;
  static protected final int Y_COMPONENT=5;
  static protected final int ANGLE_COMPONENT=7;
  static protected final int MAGNITUDE=11;

  static protected final int MY_LINE_WIDTH = LINE_WIDTH+VECTORFIELD_ADDED;

  protected VectorField field;

  protected org.opensourcephysics.display.Drawable createDrawable () {
    field = new VectorField();
    return field;
  }

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.VectorField"; }

  protected int getPropertiesDisplacement () { return VECTORFIELD_ADDED; }

//  public void initialize () { // Overwrites default initialize
////    System.out.println (this + " Initing");
////    field.echo();
//  }
//
//  public void reset () { // Overwrites default reset
////    System.out.println (this + " Resetting");
////    field.echo();
//  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");

      infoList.add ("xcomponent");
      infoList.add ("ycomponent");
      infoList.add ("length");
      infoList.add ("angles");

      infoList.add ("autoscale");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("magnitude");
      infoList.add ("levels");
      infoList.add ("invisibleLevel");
      infoList.add ("mincolor");
      infoList.add ("maxcolor");

      infoList.add ("style");
      infoList.add ("elementposition");

      infoList.add ("colormode");
      infoList.add ("showLegend");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))   return "int|double";
    if (_property.equals("maximumX"))   return "int|double";
    if (_property.equals("minimumY"))   return "int|double";
    if (_property.equals("maximumY"))   return "int|double";

    if (_property.equals("xcomponent"))      return "int|double|double[][]";
    if (_property.equals("ycomponent"))      return "int|double|double[][]";
    if (_property.equals("length"))     return "int|double";
    if (_property.equals("angles"))      return "int|double|double[][]";

    if (_property.equals("autoscale"))     return "boolean";
    if (_property.equals("minimum"))       return "int|double";
    if (_property.equals("maximum"))       return "int|double";
    if (_property.equals("magnitude"))     return "int|double|double[][]";
    if (_property.equals("levels"))        return "int";
    if (_property.equals("invisibleLevel"))return "int";
    if (_property.equals("mincolor"))      return "Color|Object";
    if (_property.equals("maxcolor"))      return "Color|Object";

    if (_property.equals("style")) return "NewArrowStyle|int";
    if (_property.equals("elementposition"))return "ArrowPosition|int";
    
    if (_property.equals("colormode"))     return "VectorColorMapper|int";
    if (_property.equals("showLegend"))    return "boolean";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("VectorColorMapper")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("spectrum"))     return new IntegerValue (VectorColorMapper.SPECTRUM);
      if (_value.equals("grayscale"))    return new IntegerValue (VectorColorMapper.GRAY);
      if (_value.equals("red"))          return new IntegerValue (VectorColorMapper.RED);
      if (_value.equals("green"))        return new IntegerValue (VectorColorMapper.GREEN);
      if (_value.equals("blue"))         return new IntegerValue (VectorColorMapper.BLUE);
      if (_value.equals("black"))        return new IntegerValue (VectorColorMapper.BLACK);
      if (_value.equals("binary"))       return new IntegerValue (VectorColorMapper.BLACK);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : field.setMinimumX(_value.getDouble()); break;
      case 1 : field.setMaximumX(_value.getDouble()); break;
      case 2 : field.setMinimumY(_value.getDouble()); break;
      case 3 : field.setMaximumY(_value.getDouble()); break;
      
      case 4 :
        if (_value.getObject() instanceof double[][]) field.setVectorSizeXData((double[][]) _value.getObject());
        else field.setVectorSizeX(_value.getDouble());
        break;
      case 5 :
        if (_value.getObject() instanceof double[][]) field.setVectorSizeYData((double[][]) _value.getObject());
        else field.setVectorSizeY(_value.getDouble());
        break;
      case 6 : field.setConstantLength(_value.getDouble()); break;
      case 7 :
        if (_value.getObject() instanceof double[][]) field.setVectorAngleData((double[][]) _value.getObject());
        else field.setVectorAngle(_value.getDouble());
        break;

      case 8 : field.setAutoscaleMagnitude(_value.getBoolean()); break;
      case 9 : field.setMagnitudeExtrema(_value.getDouble(), field.getMagnitudeMaximum()); break;
      case 10 : field.setMagnitudeExtrema(field.getMagnitudeMinimum(),_value.getDouble()); break;
      case 11 :
        if (_value.getObject() instanceof double[][]) field.setMagnitudeData((double[][]) _value.getObject());
        else field.setMagnitude(_value.getDouble());
        break;
      case 12 : field.setNumberOfLevels(_value.getInteger());  break;
      case 13 : field.setInvisibleLevel(_value.getInteger()); break;
      case 14 : field.setMinColor ((Color)_value.getObject()); break;
      case 15 : field.setMaxColor ((Color)_value.getObject()); break;

      case 16 : field.setArrowType(_value.getInteger()); break;
      case 17 : field.setRelativePosition(_value.getInteger()); break;

      case 18 :
        field.setMapperPaletteType(_value.getInteger());
        field.setUseColorMapper(true);
        break;
      case 19 : field.setShowLegend(_value.getBoolean()); break;

      default : super.setValue(_index-VECTORFIELD_ADDED,_value); break;
      case MY_LINE_WIDTH :
        float newLineWidth = (float) _value.getDouble();
        for (Element element : field.getElements()) element.getStyle().setLineWidth(newLineWidth);
        break;
    }
  }

  public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : field.setMinimumX(Double.NaN); break;
        case 1 : field.setMaximumX(Double.NaN); break;
        case 2 : field.setMinimumY(Double.NaN); break;
        case 3 : field.setMaximumY(Double.NaN); break;
        
        case 4 : field.setVectorSizeX(1); break;
        case 5 : field.setVectorSizeY(1);  break;
        case 6 : field.setConstantLength(Double.NaN); break;
        case 7 : field.setVectorAngle(Double.NaN); break;

        case 8 : field.setAutoscaleMagnitude(true); break;
        case 9 : field.setMagnitudeExtrema(0.0, field.getMagnitudeMaximum()); break;
        case 10 : field.setMagnitudeExtrema(field.getMagnitudeMinimum(),1.0); break;
        case 11 : field.setMagnitude(Double.NaN); break;
        case 12 : field.setNumberOfLevels(16);  break;
        case 13 : field.setInvisibleLevel(-1); break;
        case 14 : field.setMinColor (Color.BLUE); break;
        case 15 : field.setMaxColor (Color.RED); break;

        case 16 : field.setArrowType(ElementArrow.ARROW); break; 
        case 17 : field.setRelativePosition(Style.NORTH_EAST); break;

        case 18 : field.setUseColorMapper(false); break;
        case 19 : field.setShowLegend(false); break;
        
        default: super.setDefaultValue(_index-VECTORFIELD_ADDED); break;
        case MY_LINE_WIDTH : for (Element element : field.getElements()) element.getStyle().setLineWidth(1); break;
      }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : 
      case 1 : 
      case 2 : 
      case 3 : return "<none>";

      case 4 :
      case 5 : return "1";
      case 6 : return "<none>";
      case 7 : return "<none>";

      case 8 : return "true";
      case 9 : return "0";
      case 10 : return "1";
      case 11 : return "<none>";
      case 12 : return "16";
      case 13 : return "-1";
      case 14 : return "BLUE";
      case 15 : return "RED";

      case 16 : return "ARROW";
      case 17 : return "NORTH_EAST";
      case 18 : return "<none>";
      case 19 : return "false";
      default : return super.getDefaultValueString(_index-VECTORFIELD_ADDED);
      case MY_LINE_WIDTH : return "1";
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 : case 10 : case 11 :
      case 12 : case 13 : case 14 : case 15 :
      case 16 : case 17 : case 18 : case 19 :
      return null;
      default: return super.getValue (_index-VECTORFIELD_ADDED);
      case MY_LINE_WIDTH : return null;
    }
  }

} // End of class
