/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) March 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import java.awt.Color;
import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Histogram;
import org.opensourcephysics.display.Drawable;

public class ControlHistogram extends ControlDrawable implements NeedsPreUpdate, Resetable, DataCollector {
  static private final int HISTOGRAM_ADDED = 14;

  private Histogram histogram;
  protected double x;
  protected double[] xArray;
  protected boolean enabled,clearAtInput;
  protected int occurences;
  protected Color lineColor, fillColor;
  protected int isSet = -1; // -1 not set, 0 = double, 1 = double[]
  protected String inputLabel = "x", occurrencesLabel = "occurrences";

  protected Drawable createDrawable () {
    x = 0.0;
    lineColor=Color.BLACK;
    fillColor=Color.BLUE;
    occurences = 1;
    enabled = true;
    clearAtInput = false;
    histogram = new Histogram();
    histogram.setBinColor(fillColor,lineColor);
    return histogram;
  }

  @Override
  protected void setName (String _name) { histogram.setName(_name); }

  @Override
  public void initialize () { // Overwrites default initialize
    histogram.clear();
  }

  @Override
  public void reset () { // Overwrites default reset
      histogram.clear();
  }

  @Override
  public void onExit () { // free memory
     histogram.clear();
  }

  public void preupdate () {
//    System.out.println ("Adding "+x);
    if (!enabled) return;
    switch (isSet) {
      case 0 :
        if (clearAtInput) histogram.clear();
        histogram.append(x,occurences);
        break;
      case 1 :
        if (clearAtInput) histogram.clear();
        histogram.append(xArray);
        break;
    }
    isSet = -1;
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !org.opensourcephysics.tools.ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),histogram));
  }

  public ControlElement setProperty(String _property, String _value) { // , inputLabel = "Input", occurrencesLabel="Ocurrences"
    _property = _property.trim();
    if      (_property.equals("input"))       { inputLabel = _value;       histogram.setXYColumnNames(inputLabel, occurrencesLabel); }
    else if (_property.equals("occurrences")) { occurrencesLabel = _value; histogram.setXYColumnNames(inputLabel, occurrencesLabel); }
    
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.ArrayList<String> infoList=null;

  public java.util.ArrayList<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("input");
      infoList.add ("occurences");
      infoList.add ("barOffset");
      infoList.add ("lineColor");
      infoList.add ("fillColor");
      infoList.add ("binOffset");
      infoList.add ("binStyle");
      infoList.add ("binWidth");
      infoList.add ("discrete");
      infoList.add ("normalized");
      infoList.add ("enabled");
      infoList.add ("clearAtInput");
      infoList.add ("visible");
      infoList.add ("measured");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("input"))         return "int|double|double[]";
    if (_property.equals("occurences"))    return "int|double";
    if (_property.equals("barOffset"))     return "int|double";
    if (_property.equals("lineColor"))     return "int|Color|Object";
    if (_property.equals("fillColor"))     return "int|Color|Object";
    if (_property.equals("binOffset"))     return "int|double";
    if (_property.equals("binStyle"))      return "BinStyle|int";
    if (_property.equals("binWidth"))      return "int|double";
    if (_property.equals("discrete"))      return "boolean";
    if (_property.equals("normalized"))    return "boolean";
    if (_property.equals("enabled"))       return "boolean";
    if (_property.equals("clearAtInput"))  return "boolean";
    if (_property.equals("visible"))  return "boolean";
    if (_property.equals("measured"))  return "boolean";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("BinStyle")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("point"))        return new IntegerValue (Histogram.DRAW_POINT);
      if (_value.equals("bin"))          return new IntegerValue (Histogram.DRAW_BIN);
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
      case 1 : occurences = _value.getInteger(); break;
      case 2 : histogram.setBarOffset (_value.getDouble()); break;
      case 3 : 
        if (_value.getObject() instanceof Color) histogram.setBinColor (fillColor,lineColor=(Color)_value.getObject());
        else histogram.setBinColor (fillColor,lineColor=DisplayColors.getLineColor(_value.getInteger()));
        break;
      case 4 : 
        if (_value.getObject() instanceof Color) histogram.setBinColor (fillColor=(Color)_value.getObject(),lineColor);
        else histogram.setBinColor (fillColor=DisplayColors.getLineColor(_value.getInteger()),lineColor);
        break;
      case 5 : histogram.setBinOffset (_value.getDouble()); break;
      case 6 : histogram.setBinStyle ((short) _value.getInteger()); break;
      case 7 : histogram.setBinWidth (_value.getDouble()); break;
      case 8 : histogram.setDiscrete (_value.getBoolean()); break;
      case 9 : histogram.setNormalizedToOne (_value.getBoolean()); break;
      case 10 : enabled = _value.getBoolean(); break;
      case 11 : clearAtInput = _value.getBoolean(); break;
      case 12 : histogram.setVisible(_value.getBoolean()); break;
      case 13 : histogram.setMeasured(_value.getBoolean()); break;
      default: super.setValue(_index-HISTOGRAM_ADDED,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :  x = 0; isSet = -1; break;
      case 1 : occurences = 1; break;
      case 2 : histogram.setBarOffset (0); break;
      case 3 : histogram.setBinColor (fillColor,lineColor=Color.BLACK); break;
      case 4 : histogram.setBinColor (fillColor=Color.BLUE,lineColor); break;
      case 5 : histogram.setBinOffset (0); break;
      case 6 : histogram.setBinStyle (Histogram.DRAW_BIN); break;
      case 7 : histogram.setBinWidth (1); break;
      case 8 : histogram.setDiscrete (true); break;
      case 9 : histogram.setNormalizedToOne (false); break;
      case 10 : enabled = true; break;
      case 11 : clearAtInput = false; break;
      case 12 : histogram.setVisible(true); break;
      case 13 : histogram.setMeasured(true); break;
      default: super.setDefaultValue(_index-HISTOGRAM_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "1";
      case 2 : return "0";
      case 3 : return "BLACK";
      case 4 : return "BLUE";
      case 5 : return "0";
      case 6 : return "BIN";
      case 7 : return "1";
      case 8 : return "true";
      case 9 : return "false";
      case 10 : return "true";
      case 11 : return "false";
      case 12 : return "true";
      case 13 : return "true";
      default : return super.getDefaultValueString(_index-HISTOGRAM_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 : case 8 : case 9 :
      case 10 : case 11 : case 12 : case 13 :
        return null;
      default: return super.getValue(_index-HISTOGRAM_ADDED);
    }
  }

}
