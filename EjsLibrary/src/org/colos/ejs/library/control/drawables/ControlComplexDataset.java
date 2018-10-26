/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) March 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.swing.ControlDrawable;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.ComplexDataset;
import org.opensourcephysics.display.Drawable;
import java.awt.Color;

public class ControlComplexDataset extends ControlDrawable implements org.colos.ejs.library.control.swing.NeedsPreUpdate,
                                                                 org.colos.ejs.library.control.Resetable,
                                                                 org.colos.ejs.library.control.DataCollector {
  static private final int COMPLEXDATASET_ADDED = 14;

  private ComplexDataset dataset;
  protected double x, re, im;
  protected double[] xArray, reArray, imArray;
  protected boolean enabled, clearAtInput;
  protected Color reColor, imColor;
  protected int isSet = -1; // -1 not set, 0 = double, 1 = double[]

  protected Drawable createDrawable () {
    x = re = im = 0.0;
    reColor=Color.BLACK;
    imColor= Color.BLUE;
    enabled = true;
    clearAtInput = true;
    dataset = new ComplexDataset();
    dataset.setLineColor(reColor,imColor);
    return dataset;
  }

  public void initialize () { // Overwrites default initialize
    dataset.clear();
  }

  public void reset () { // Overwrites default reset
      dataset.clear();
  }

  public void onExit () { // free memory
     dataset.clear();
  }

  public void preupdate () {
    if (!enabled) return;
    switch (isSet) {
      case 0 :
        if (clearAtInput) dataset.clear();
        dataset.append(x,re,im);
        break;
      case 1 :
        if (clearAtInput) dataset.clear();
        dataset.append(xArray,reArray,imArray);
        break;
    }
    isSet = -1;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("x");
      infoList.add ("real");
      infoList.add ("imag");
      infoList.add ("realColor");
      infoList.add ("imagColor");
      infoList.add ("enabled");

      infoList.add ("centered");
      infoList.add ("connected");
      infoList.add ("markersize");
      infoList.add ("markershape");
      infoList.add ("sorted");
      infoList.add ("visible");
      infoList.add ("measured");
      infoList.add ("clearAtInput");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("x"))       return "int|double|double[]";
    if (_property.equals("real"))    return "int|double|double[]";
    if (_property.equals("imag"))    return "int|double|double[]";
    if (_property.equals("realColor"))    return "Color|Object";
    if (_property.equals("imagColor"))    return "Color|Object";
    if (_property.equals("enabled"))      return "boolean";

    if (_property.equals("centered"))     return "boolean";
    if (_property.equals("connected"))    return "boolean";
    if (_property.equals("markersize"))   return "int";
    if (_property.equals("markershape"))  return "ComplexMarkerShape|int";
    if (_property.equals("sorted"))       return "boolean";

    if (_property.equals("visible"))      return "boolean";
    if (_property.equals("measured"))     return "boolean";
    if (_property.equals("clearAtInput"))  return "boolean";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ComplexMarkerShape")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("amp_curve"))   return new IntegerValue (ComplexDataset.AMP_CURVE);
      if (_value.equals("re_im_curve")) return new IntegerValue (ComplexDataset.RE_IM_CURVE);
      if (_value.equals("phase_curve")) return new IntegerValue (ComplexDataset.PHASE_CURVE);
      if (_value.equals("phase_bar"))   return new IntegerValue (ComplexDataset.PHASE_BAR);
      if (_value.equals("phase_post"))  return new IntegerValue (ComplexDataset.PHASE_POST);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
          if (_value.getObject() instanceof double[]) { xArray = (double[]) _value.getObject(); isSet = 1; }
          else { x = _value.getDouble(); isSet = 0; }
          break;
      case 1 :
          if (_value.getObject() instanceof double[]) { reArray = (double[]) _value.getObject(); isSet = 1; }
          else { re = _value.getDouble(); isSet = 0; }
          break;
      case 2 :
          if (_value.getObject() instanceof double[]) { imArray = (double[]) _value.getObject(); isSet = 1; }
          else { im = _value.getDouble(); isSet = 0; }
          break;
      case 3 : dataset.setLineColor (reColor=(Color)_value.getObject(),imColor); break;
      case 4 : dataset.setLineColor (reColor,imColor=(Color)_value.getObject()); break;
      case 5 : enabled = _value.getBoolean(); break;

      case  6 : dataset.setCentered(_value.getBoolean()); break;
      case  7 : dataset.setConnected(_value.getBoolean()); break;
      case  8 : dataset.setMarkerSize(_value.getInteger()); break;
      case  9 : dataset.setMarkerShape(_value.getInteger()); break;
      case 10 : dataset.setSorted(_value.getBoolean()); break;
      
      case 11 : dataset.setVisible(_value.getBoolean()); break;
      case 12 : dataset.setMeasurable(_value.getBoolean()); break;
      case 13 : clearAtInput = _value.getBoolean(); break;

      default: super.setValue(_index-COMPLEXDATASET_ADDED,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
    case 0 : x  = 0.0; isSet = -1; break;
    case 1 : re = 0.0; isSet = -1; break;
    case 2 : im = 0.0; isSet = -1; break;
    case 3 : dataset.setLineColor (reColor=Color.BLACK,imColor); break;
    case 4 : dataset.setLineColor (reColor,imColor=Color.BLUE); break;
    case 5 : enabled = true; break;

    case  6 : dataset.setCentered(true); break;
    case  7 : dataset.setConnected(true); break;
    case  8 : dataset.setMarkerSize(5); break;
    case  9 : dataset.setMarkerShape(ComplexDataset.PHASE_CURVE); break;
    case 10 : dataset.setSorted(false); break;

    case 11 : dataset.setVisible(true); break;
    case 12 : dataset.setMeasurable(true); break;
    case 13 : clearAtInput = true; break;

      default: super.setDefaultValue(_index-COMPLEXDATASET_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 :
      case 1 :
      case 2 : return "<none>";
      case 3 : return "BLACK";
      case 4 : return "BLUE";
      case 5 : return "true";

      case  6 : return "true";
      case  7 : return "true";
      case  8 : return "5";
      case  9 : return "PHASE_CURVE";
      case 10 : return "false";
      case 11 : return "true";
      case 12 : return "true";
      case 13 : return "true";
      default : return super.getDefaultValueString(_index-COMPLEXDATASET_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 : case 8 : case 9 :
      case 10 : case 11 : case 12 : case 13 :
        return null;
      default: return super.getValue(_index-COMPLEXDATASET_ADDED);
    }
  }

}
