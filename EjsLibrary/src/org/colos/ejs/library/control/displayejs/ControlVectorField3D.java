/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import java.awt.Color;
import org.opensourcephysics.display.Drawable;
import java.awt.BasicStroke;
import java.awt.Stroke;

public class ControlVectorField3D extends ControlDrawable3D {
  static private final int VFIELD_ADDED = 13;

  protected VectorField3D vectorfield3d;
  private boolean auto;
  private double minC, maxC, zoom, lineWidth = 1.0;
  private int levels;
  private Color mincolor, maxcolor;

  protected Drawable createDrawable () {
    vectorfield3d = new VectorField3D();
    minC = 0.0;
    maxC = 1.0;
    zoom = 1.0;
    levels = 0;
    vectorfield3d.setAutoscaleMagnitude(auto=true);
    vectorfield3d.setInvisibleLevel(-1);
    return vectorfield3d;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("data");
      infoList.add ("autoscale");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("levels");
      infoList.add ("mincolor");
      infoList.add ("maxcolor");
      infoList.add ("zoom");
      infoList.add ("visible");
      infoList.add ("invisibleLevel");
      infoList.add ("style");
      infoList.add ("stroke");
      infoList.add ("measured");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))          return "double[][][][]|double[][][]";
    if (_property.equals("autoscale"))     return "boolean";
    if (_property.equals("minimum"))       return "int|double";
    if (_property.equals("maximum"))       return "int|double";
    if (_property.equals("levels"))        return "int";
    if (_property.equals("mincolor"))      return "Color|Object";
    if (_property.equals("maxcolor"))      return "Color|Object";
    if (_property.equals("zoom"))          return "int|double";
    if (_property.equals("visible"))       return "boolean";
    if (_property.equals("invisibleLevel"))return "int";
    if (_property.equals("style"))         return "ArrowStyle|int";
    if (_property.equals("stroke"))         return "int|double|Object";
    if (_property.equals("measured"))        return "boolean";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ArrowStyle")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("arrow"))       return new IntegerValue (InteractiveArrow.ARROW);
      if (_value.equals("segment"))     return new IntegerValue (InteractiveArrow.SEGMENT);
      if (_value.equals("box"))         return new IntegerValue (InteractiveArrow.BOX);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if      (_value.getObject() instanceof double[][][][]) vectorfield3d.setDataArray((double[][][][])_value.getObject());
        else if (_value.getObject() instanceof double[][][])   vectorfield3d.setDataArray((double[][][])_value.getObject());
        break;
      case 1 : if (auto!=_value.getBoolean()) vectorfield3d.setAutoscaleMagnitude(auto=_value.getBoolean()); break;
      case 2 : if (_value.getDouble()!=minC)  vectorfield3d.setColorExtrema(minC=_value.getDouble(),maxC);   break;
      case 3 : if (_value.getDouble()!=maxC)  vectorfield3d.setColorExtrema(minC,maxC=_value.getDouble());   break;
      case 4 : if (_value.getInteger()!=levels) vectorfield3d.setNumberOfLevels(levels=_value.getInteger()); break;
      case 5 : if (mincolor!=(Color) _value.getObject()) vectorfield3d.setMinColor(mincolor=(Color)_value.getObject()); break;
      case 6 : if (maxcolor!=(Color) _value.getObject()) vectorfield3d.setMaxColor(maxcolor=(Color)_value.getObject()); break;
      case 7 : if (_value.getDouble()!=zoom) vectorfield3d.setZoom (zoom=_value.getDouble()); break;
      case 8 : vectorfield3d.setVisible(_value.getBoolean()); break;
      case 9 : vectorfield3d.setInvisibleLevel(_value.getInteger()); break;
      case 10 : // ArrowStyle
        if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(vectorfield3d.getNumberOfElements(),val.length); i<n; i++) ((InteractiveArrow) vectorfield3d.elementAt(i)).setArrowType(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) ((InteractiveArrow)vectorfield3d.elementAt(i)).setArrowType(val);
        }
        break;
      case 11 :
        if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          for (int i=0, n=Math.min(vectorfield3d.getNumberOfElements(),val.length); i<n; i++) vectorfield3d.elementAt(i).getStyle().setEdgeStroke((Stroke)val[i]);
        }
        else if (_value.getObject() instanceof Stroke) {
          Stroke val = (Stroke) _value.getObject();
          for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) vectorfield3d.elementAt(i).getStyle().setEdgeStroke(val);
        }
        else if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          for (int i=0, n=Math.min(vectorfield3d.getNumberOfElements(),val.length); i<n; i++) {
            BasicStroke stroke;
            if (val[i]<0) stroke = new BasicStroke((float) -val[i], BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0);
            else stroke = new BasicStroke((float) val[i]);
            vectorfield3d.elementAt(i).getStyle().setEdgeStroke(stroke);
          }
        }
        else if (lineWidth!=_value.getDouble()) {
          lineWidth = _value.getDouble();
          BasicStroke stroke;
          if (lineWidth<0) stroke = new BasicStroke((float) -lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0);
          else stroke = new BasicStroke((float) lineWidth);
          for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) vectorfield3d.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        break;

      case 12 : for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) vectorfield3d.elementAt(i).canBeMeasured(_value.getBoolean()); break;

      default: super.setValue(_index-VFIELD_ADDED,_value);
    }
  }


  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : vectorfield3d.setDataArray((double[][][])null); break;
      case 1 : vectorfield3d.setAutoscaleMagnitude(auto=true); break;
      case 2 : vectorfield3d.setColorExtrema(minC=0.0,maxC);   break;
      case 3 : vectorfield3d.setColorExtrema(minC,maxC=1.0);   break;
      case 4 : vectorfield3d.setNumberOfLevels(levels=16);     break;
      case 5 : vectorfield3d.setMinColor(mincolor=Color.blue); break;
      case 6 : vectorfield3d.setMaxColor(maxcolor=Color.red);  break;
      case 7 : vectorfield3d.setZoom (zoom=1.0);  break;
      case 8 : vectorfield3d.setVisible(true); break;
      case 9 : vectorfield3d.setInvisibleLevel(-1); break;
      case 10 :
        for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) ((InteractiveArrow)vectorfield3d.elementAt(i)).setArrowType(InteractiveArrow.ARROW);
        break;
      case 11 :
        {
          BasicStroke stroke = new java.awt.BasicStroke((float) (lineWidth=1.0));
          for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) vectorfield3d.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        break;
      case 12 : for (int i=0, n=vectorfield3d.getNumberOfElements(); i<n; i++) vectorfield3d.elementAt(i).canBeMeasured(true); break;

      default: super.setDefaultValue(_index-VFIELD_ADDED); break;
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 : case 10 : case 11 :
      case 12 : 
        return null;
      default: return super.getValue(_index-VFIELD_ADDED);
    }
  }

}
