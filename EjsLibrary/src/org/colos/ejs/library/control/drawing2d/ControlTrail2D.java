/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.opensourcephysics.drawing2d.ElementTrail;
import org.opensourcephysics.tools.ToolForData;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlTrail2D extends ControlElement2D  implements org.colos.ejs.library.control.swing.NeedsPreUpdate,
                                                                 org.colos.ejs.library.control.Resetable,
                                                                 org.colos.ejs.library.control.DataCollector {
  static final private int TRAIL2D_PROPERTIES_ADDED=10;

  private ElementTrail trail;
  private volatile boolean isSet = false;
  private volatile double x, y;
  private double[] xArray=null, yArray=null;

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementTrail"; }

  public String getServerClassname () { return "org.colos.ejs.library.server.drawing2d.ElementTrail"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    trail = new ElementTrail();
    return trail;
  }

  protected int getPropertiesDisplacement () { return TRAIL2D_PROPERTIES_ADDED; }

  public void reset () { // Overwrites default reset
    trail.clear();
  }

  public void initialize () { // Overwrites default initialize
    trail.initialize();
  }

  public void preupdate () {
    if (!trail.isActive()) return;
    if (isSet) {
      if (xArray==null) {
        if (yArray==null) trail.addPoint(x, y);
        else trail.addPoints(x,yArray);
      }
      else {
        if (yArray == null) trail.addPoints(xArray,y);
        else trail.addPoints(xArray,yArray);
      }
    }
    isSet = false;
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),trail));
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("maximumPoints");
      infoList.add ("connected");
      infoList.add ("inputX");
      infoList.add ("inputY");

      infoList.add ("skippoints");
      infoList.add ("active");
      infoList.add ("norepeat");
      infoList.add ("clearAtInput");
      
      infoList.add ("xLabel");
      infoList.add ("yLabel");
      
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("maximumPoints")) return "int";
    if (_property.equals("connected")) return "boolean";
    if (_property.equals("inputX")) return "int|double|double[]";
    if (_property.equals("inputY")) return "int|double|double[]";

    if (_property.equals("skippoints"))  return "int";
    if (_property.equals("active"))      return "boolean";
    if (_property.equals("norepeat"))    return "boolean";
    if (_property.equals("clearAtInput"))return "boolean";

    if (_property.equals("xLabel"))    return "String TRANSLATABLE";
    if (_property.equals("yLabel"))    return "String TRANSLATABLE";

    return super.getPropertyInfo(_property);
  }

  @Override
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_value!=null) {
      boolean useDefaultTitle = _value.startsWith("%_model.") && _value.endsWith("()%");
      if      (_property.equals("inputX")) trail.setXLabel(useDefaultTitle ? "Input X" : _value);
      else if (_property.equals("inputY")) trail.setYLabel(useDefaultTitle ? "Input Y" : _value);
    }
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------


  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : trail.setMaximumPoints(_value.getInteger()); break;
      case 1 : trail.setConnectionType(_value.getBoolean() ? ElementTrail.LINE_CONNECTION : ElementTrail.NO_CONNECTION); break;
      case 2 :
        if (_value.getObject() instanceof double[]) xArray = (double[]) _value.getObject();
        else { x = _value.getDouble(); xArray = null; }
        isSet = true;
        break;
      case 3 :
        if (_value.getObject() instanceof double[]) yArray = (double[]) _value.getObject();
        else { y = _value.getDouble(); yArray = null; }
        isSet = true;
        break;
      case 4 : trail.setSkipPoints(_value.getInteger()); break;
      case 5 : trail.setActive(_value.getBoolean()); break;
      case 6 : trail.setNoRepeat(_value.getBoolean()); break;
      case 7 : trail.setClearAtInput(_value.getBoolean()); break;
      case 8 : trail.setXLabel(_value.getString()); break;
      case 9 : trail.setYLabel(_value.getString()); break;
      default : super.setValue(_index-TRAIL2D_PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : trail.setMaximumPoints(0);    break;
      case 1 : trail.setConnectionType(ElementTrail.LINE_CONNECTION); break;
      case 2 : x = 0.0; xArray = null; break;
      case 3 : y = 0.0; yArray = null; break;
      case 4 : trail.setSkipPoints(0); break;
      case 5 : trail.setActive(true); break;
      case 6 : trail.setNoRepeat(false); break;
      case 7 : trail.setClearAtInput(false); break;
      case 8 : trail.setXLabel(null); break;
      case 9 : trail.setYLabel(null); break;
      default: super.setDefaultValue(_index-TRAIL2D_PROPERTIES_ADDED); break;
    }
  }
  
  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "true";
      case 2 : return "0.0";
      case 3 : return "0.0";
      case 4 : return "0";
      case 5 : return "true";
      case 6 : return "false";
      case 7 : return "false";
      case 8 : return "<none>";
      case 9 : return "<none>";

      default : return super.getDefaultValueString(_index-TRAIL2D_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 :
          return null;
      default: return super.getValue (_index-TRAIL2D_PROPERTIES_ADDED);
    }
  }

} // End of class
