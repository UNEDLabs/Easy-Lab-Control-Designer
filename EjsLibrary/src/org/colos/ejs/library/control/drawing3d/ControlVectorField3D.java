/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import java.awt.*;
//import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display2d.VectorColorMapper;
import org.opensourcephysics.drawing2d.Style;
import org.opensourcephysics.drawing3d.VectorField;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementArrow;

/**
 * A simpler API for ControlVectorField3D
 */
public class ControlVectorField3D extends ControlElement3D { //implements NeedsPreUpdate {
  static protected final int VECTORFIELD_ADDED=24;
  static protected final int MY_LINE_WIDTH = LINE_WIDTH+VECTORFIELD_ADDED;

  static protected final int X_MINIMUM=0;
  static protected final int X_MAXIMUM=1;
  static protected final int Y_MINIMUM=2;
  static protected final int Y_MAXIMUM=3;
  static protected final int X_COMPONENT=4;
  static protected final int Y_COMPONENT=5;
  static protected final int ALPHA_COMPONENT=7;
  static protected final int MAGNITUDE=11;
  static protected final int Z_MINIMUM=18;
  static protected final int Z_MAXIMUM=19;
  static protected final int Z_COMPONENT=20;
  static protected final int BETA_COMPONENT=21;

  // Configuration variables
  protected VectorField field;

  @Override
  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.VectorField"; }

  @Override
  protected Element createElement () {
    field = new VectorField();
    return field;
  }

  @Override
  protected int getPropertiesDisplacement () { return VECTORFIELD_ADDED; }

//  public void preupdate () { field.prepareField();  }

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

      infoList.add ("minimumZ");
      infoList.add ("maximumZ");
      infoList.add ("zcomponent");
      infoList.add ("betas");

      infoList.add ("colormode");
      infoList.add ("showLegend");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }


//  public Value parseConstant (String _propertyType, String _value) {
//    if (_value==null) return null;
//    if (_propertyType.indexOf("VectorFieldStyle")>=0) {
//      _value = _value.trim().toLowerCase();
//      if (_value.equals("arrow"))       return new IntegerValue (ElementArrow.ARROW);
//      if (_value.equals("segment"))     return new IntegerValue (ElementArrow.SEGMENT);
//    }
//    else if (_propertyType.indexOf("ArrowPosition")>=0) {
//      _value = _value.trim().toLowerCase();
//      if (_value.equals("centered"))         return new IntegerValue (Style.CENTERED);
//      if (_value.equals("north_east"))       return new IntegerValue (Style.NORTH_EAST);
//      if (_value.equals("south_west"))       return new IntegerValue (Style.SOUTH_WEST);
//    }
//    return super.parseConstant (_propertyType,_value);
//  }

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

  public String getPropertyInfo(String _property) {
    
    if (_property.equals("minimumX"))   return "int|double";
    if (_property.equals("maximumX"))   return "int|double";
    if (_property.equals("minimumY"))   return "int|double";
    if (_property.equals("maximumY"))   return "int|double";

    if (_property.equals("xcomponent"))      return "int|double|double[][][]";
    if (_property.equals("ycomponent"))      return "int|double|double[][][]";
    if (_property.equals("length"))     return "int|double";
    if (_property.equals("angles"))      return "int|double|double[][][]";

    if (_property.equals("autoscale"))     return "boolean";
    if (_property.equals("minimum"))       return "int|double";
    if (_property.equals("maximum"))       return "int|double";
    if (_property.equals("magnitude"))     return "int|double|double[][][]";
    if (_property.equals("levels"))        return "int";
    if (_property.equals("invisibleLevel"))return "int";
    if (_property.equals("mincolor"))      return "Color|Object";
    if (_property.equals("maxcolor"))      return "Color|Object";

    if (_property.equals("style"))         return "NewArrowStyle|int";
    if (_property.equals("elementposition"))return "ArrowPosition|int";

    if (_property.equals("minimumZ"))   return "int|double";
    if (_property.equals("maximumZ"))   return "int|double";
    if (_property.equals("zcomponent")) return "int|double|double[][][]";
    if (_property.equals("betas"))      return "int|double|double[][][]";

    if (_property.equals("colormode"))     return "VectorColorMapper|int";
    if (_property.equals("showLegend"))    return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case X_MINIMUM : field.setMinimumX(_value.getDouble()); break;
      case X_MAXIMUM : field.setMaximumX(_value.getDouble()); break;
      case Y_MINIMUM : field.setMinimumY(_value.getDouble()); break;
      case Y_MAXIMUM : field.setMaximumY(_value.getDouble()); break;
      
      case X_COMPONENT :
        if (_value.getObject() instanceof double[][][]) field.setVectorSizeXData((double[][][]) _value.getObject());
        else field.setVectorSizeX(_value.getDouble());
        break;
      case Y_COMPONENT :
        if (_value.getObject() instanceof double[][][]) field.setVectorSizeYData((double[][][]) _value.getObject());
        else field.setVectorSizeY(_value.getDouble());
        break;
      case 6 : field.setConstantLength(_value.getDouble()); break;
      case ALPHA_COMPONENT :
        if (_value.getObject() instanceof double[][][]) field.setVectorAlphaData((double[][][]) _value.getObject());
        else field.setVectorAlpha(_value.getDouble());
        break;

      case 8 : field.setAutoscaleMagnitude(_value.getBoolean()); break;
      case 9 : field.setMagnitudeExtrema(_value.getDouble(), field.getMagnitudeMaximum()); break;
      case 10 : field.setMagnitudeExtrema(field.getMagnitudeMinimum(),_value.getDouble()); break;
      case MAGNITUDE :
        if (_value.getObject() instanceof double[][][]) field.setMagnitudeData((double[][][]) _value.getObject());
        else field.setMagnitude(_value.getDouble());
        break;
      case 12 : field.setNumberOfLevels(_value.getInteger());  break;
      case 13 : field.setInvisibleLevel(_value.getInteger()); break;
      case 14 : field.setMinColor ((Color)_value.getObject()); break;
      case 15 : field.setMaxColor ((Color)_value.getObject()); break;

      case 16 : field.setArrowType(_value.getInteger()); break;
      case 17 : field.setRelativePosition(_value.getInteger()); break;

      case Z_MINIMUM : field.setMinimumZ(_value.getDouble()); break;
      case Z_MAXIMUM : field.setMaximumZ(_value.getDouble()); break;
      case Z_COMPONENT :
        if (_value.getObject() instanceof double[][][]) field.setVectorSizeZData((double[][][]) _value.getObject());
        else field.setVectorSizeZ(_value.getDouble());
        break;
      case BETA_COMPONENT :
        if (_value.getObject() instanceof double[][][]) field.setVectorBetaData((double[][][]) _value.getObject());
        else field.setVectorBeta(_value.getDouble());
        break;
        
      case 22 :
        field.getColorMapper().setPaletteType(_value.getInteger());
        field.setUseColorMapper(true);
        break;
      case 23 : field.setShowLegend(_value.getBoolean()); break;


      default : super.setValue(_index-VECTORFIELD_ADDED,_value); break;
      case MY_LINE_WIDTH : 
        float lineWidth = (float) _value.getDouble();
        for (Element element : field.getElements()) element.getStyle().setLineWidth(lineWidth);
        break;
    }
//    if (this.isUnderEjs) field.prepareField();
    if (isUnderEjs) updatePanel();
  
  }

  public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : field.setMinimumX(-1.0); break;
        case 1 : field.setMaximumX(1.0); break;
        case 2 : field.setMinimumY(-1.0); break;
        case 3 : field.setMaximumY(1.0); break;
        
        case 4 : field.setVectorSizeX(1); break;
        case 5 : field.setVectorSizeY(1);  break;
        case 6 : field.setConstantLength(Double.NaN); break;
        case 7 : field.setVectorAlpha(Double.NaN); break;

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
        
        case 18 : field.setMinimumZ(-1.0); break;
        case 19 : field.setMaximumZ(1.0); break;
        case 20 : field.setVectorSizeZ(1);; break;
        case 21 : field.setVectorBeta(Double.NaN); break;

        case 22 : field.setUseColorMapper(false); break;
        case 23 : field.setShowLegend(false); break;

        default: super.setDefaultValue(_index-VECTORFIELD_ADDED); break;
        case MY_LINE_WIDTH : for (Element element : field.getElements()) element.getStyle().setLineWidth(1); break;
      }
//      if (this.isUnderEjs) field.prepareField();
      if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "-1";
      case 1 : return "1";
      case 2 : return "-1";
      case 3 : return "1";

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

      case 18 : return "-1";
      case 19 : return "1";
      case 20 : return "<none>";
      case 21 : return "<none>";

      case 22 : return "<none>";
      case 23 : return "false";

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
      case 20 : case 21 : case 22 : case 23 :
        return null;
      default: return super.getValue (_index-VECTORFIELD_ADDED);
      case MY_LINE_WIDTH : return null;
    }
  }

} // End of class
