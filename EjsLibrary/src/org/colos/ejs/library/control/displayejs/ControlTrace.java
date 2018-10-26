/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.tools.*;

/**
 * An interactive particle
 */
public class ControlTrace extends ControlInteractiveElement implements org.colos.ejs.library.control.swing.NeedsPreUpdate,
                                                                       org.colos.ejs.library.control.Resetable,
                                                                       org.colos.ejs.library.control.DataCollector {
  static final protected int TRACE_ADDED=16;
  static protected final int INPUT_X = POSITION_X+TRACE_ADDED;
  static protected final int INPUT_Y = POSITION_Y+TRACE_ADDED;
  static protected final int INPUT_Z = POSITION_Z+TRACE_ADDED;
  static protected final int MY_STYLE = STYLE + TRACE_ADDED;

  private InteractiveTrace trace;
  private double x, y, z;
  private double[] xArray=null, yArray=null, zArray=null;
  private boolean isSet = false, isSetZ = false;

  private int[] myPosSpot, mySizeSpot;
  private int myFullPosition, myFullSize;

  public ControlTrace () {
    super ();
    enabledEjsEdit = true; 
    
    int disp = getPropertiesDisplacement();
    myPosSpot  = new int[] {6+disp,7+disp,8+disp};
    mySizeSpot = new int[] {SIZE_X+TRACE_ADDED+disp,SIZE_Y+TRACE_ADDED+disp,SIZE_Z+TRACE_ADDED+disp};
    myFullPosition = POSITION+TRACE_ADDED+disp;
    myFullSize     = SIZE    +TRACE_ADDED+disp;
  }

  protected int getPropertiesDisplacement () { return 0; }

  final public int[] getPosSpot () { return myPosSpot; }
  final public int[] getSizeSpot () { return mySizeSpot; }

  final public int getFullPositionSpot () { return myFullPosition; }
  final public int getFullSizeSpot () { return myFullSize; }

  public String getServerClassname () { return "org.colos.ejs.library.server.drawing2d.ElementTrail"; }

  protected void setName (String _name) { trace.setName(_name); } // To be overwritten

  protected Drawable createDrawable () {
    x = y = z = 0.0;
    trace = new InteractiveTrace();
//    trace.createTableFrame();
    trace.setAllowTable(true);
    return trace;
  }

  public void initialize () { // Overwrites default initialize
//    System.out.println ("Initing");
    trace.initialize();
  }

  public void reset () { // Overwrites default reset
//    System.out.println ("Resetting");
    trace.clear();
  }


  public void onExit () { // free memory
    trace.clear();
  }

  public void preupdate () {
    //System.out.println ("Adding "+x+" " + y);
    if (!trace.isActive()) return;
    if (isSetZ) {
      if (xArray==null) {
        if (yArray==null) {
          if (zArray == null) trace.addPoint(x, y, z);
          else for (int i=0,n=zArray.length; i<n; i++) trace.addPoint(x,y,zArray[i]);
        }
        else {
          if (zArray == null) for (int i=0,n=yArray.length; i<n; i++) trace.addPoint(x,yArray[i],z);
          else for (int i=0,n=Math.min(yArray.length,zArray.length); i<n; i++) trace.addPoint(x,yArray[i],zArray[i]);
        }
      }
      else {
        if (yArray==null) {
          if (zArray == null) for (int i=0,n=xArray.length; i<n; i++) trace.addPoint(xArray[i], y, z);
          else for (int i=0,n=Math.min(xArray.length,zArray.length); i<n; i++) trace.addPoint(xArray[i],y,zArray[i]);
        }
        else {
          if (zArray == null) for (int i=0,n=Math.min(xArray.length,yArray.length); i<n; i++) trace.addPoint(xArray[i],yArray[i],z);
          else trace.addPoints(xArray,yArray,zArray);
        }
      }
    }
    else if (isSet) {
      if (xArray==null) {
        if (yArray==null) trace.addPoint(x, y);
        else for (int i=0,n=yArray.length; i<n; i++) trace.addPoint(x,yArray[i]);
      }
      else {
        if (yArray == null) for (int i=0,n=xArray.length; i<n; i++) trace.addPoint(xArray[i],y);
        else trace.addPoints(xArray,yArray);
      }
    }
    isSet = isSetZ = false;
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),trace));
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.ArrayList<String> infoList=null;

  public java.util.ArrayList<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("maxpoints");
      infoList.add ("skippoints");
      infoList.add ("active");
      infoList.add ("norepeat");
      infoList.add ("connected");
      infoList.add ("markersize");
      infoList.add ("positionx");
      infoList.add ("positiony");
      infoList.add ("positionz");
      infoList.add ("memory");
      infoList.add ("drivenby");
      infoList.add ("memorycolor");
      infoList.add ("clearAtInput");
      
      infoList.add ("xLabel");
      infoList.add ("yLabel");
      infoList.add ("zLabel");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("x")) return "inputX";
    if (_property.equals("y")) return "inputY";
    if (_property.equals("z")) return "inputZ";
    if (_property.equals("color"))            return "lineColor";
    if (_property.equals("secondaryColor"))   return "fillColor";
    if (_property.equals("maxpoints")) return "maximumPoints";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("x"))   return "int|double|double[]";
    if (_property.equals("y"))   return "int|double|double[]";
    if (_property.equals("z"))   return "int|double|double[] UNNECESSARY";

    if (_property.equals("positionz"))   return "int|double|double[] UNNECESSARY";
    if (_property.equals("sizez"))    return "int|double|double[] UNNECESSARY";

    if (_property.equals("maxpoints"))   return "int PREVIOUS";
    if (_property.equals("skippoints"))  return "int";
    if (_property.equals("active"))      return "boolean";
    if (_property.equals("norepeat"))    return "boolean";
    if (_property.equals("connected"))   return "boolean";
    if (_property.equals("style"))          return "TraceMarkerShape|int";
    if (_property.equals("markersize"))  return "int";
    if (_property.equals("positionx"))   return "int|double";
    if (_property.equals("positiony"))   return "int|double";
    if (_property.equals("positionz"))   return "int|double";

    if (_property.equals("memory"))      return "int";
    if (_property.equals("drivenby"))    return "DrivenBy|int";
    if (_property.equals("memorycolor")) return "int|Color|Object";
    if (_property.equals("clearAtInput"))return "boolean";

    if (_property.equals("xLabel"))    return "String TRANSLATABLE";
    if (_property.equals("yLabel"))    return "String TRANSLATABLE";
    if (_property.equals("zLabel"))    return "String TRANSLATABLE UNNECESSARY";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("DrivenBy")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("show_all")) return new IntegerValue (InteractiveTrace.SHOW_ALL);
      if (_value.equals("as_added")) return new IntegerValue (InteractiveTrace.ORDER_OF_APPEARANCE);
      if (_value.equals("x_order")) return new IntegerValue (InteractiveTrace.X_COORDINATE);
      if (_value.equals("y_order")) return new IntegerValue (InteractiveTrace.Y_COORDINATE);
      if (_value.equals("z_order")) return new IntegerValue (InteractiveTrace.Z_COORDINATE);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  @Override
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    // Backwards compatibility
    if (_property.equals("makershape"))  return super.setProperty ("style",_value);
    if (_property.equals("linecolor"))   return super.setProperty ("color",_value);
    if (_property.equals("markercolor")) return super.setProperty ("secondaryColor",_value);
    
    if (_value!=null) {
      boolean useDefaultTitle = _value.startsWith("%_model.") && _value.endsWith("()%");
      if      (_property.equals("x")) trace.setXLabel(useDefaultTitle ? "Input X" : _value);
      else if (_property.equals("y")) trace.setYLabel(useDefaultTitle ? "Input Y" : _value);
      else if (_property.equals("z")) trace.setYLabel(useDefaultTitle ? "Input Z" : _value);
    }

    return super.setProperty(_property,_value);
  }

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case INPUT_X :
        if (_value.getObject() instanceof double[]) xArray = (double[]) _value.getObject();
        else { x = _value.getDouble(); xArray = null; }
        isSet = true;
        break;
      case INPUT_Y :
        if (_value.getObject() instanceof double[]) yArray = (double[]) _value.getObject();
        else { y = _value.getDouble(); yArray = null; }
        isSet = true;
        break;
      case INPUT_Z :
        if (_value.getObject() instanceof double[]) zArray = (double[]) _value.getObject();
        else { z = _value.getDouble(); zArray = null; }
        isSetZ = true;
        break;

      case 0 : trace.setMaximumPoints(_value.getInteger());     break;
      case 1 : trace.setSkip(_value.getInteger());              break;
      case 2 : trace.setActive(_value.getBoolean());            break;
      case 3 : trace.setIgnoreEqualPoints(_value.getBoolean()); break;
      case 4 : trace.setConnected(_value.getBoolean());         break;
      case 5 : trace.setShapeSize(_value.getInteger());         break;
      case 6 : super.setValue(POSITION_X,_value); break; // These affect the whole trace,
      case 7 : super.setValue(POSITION_Y,_value); break; // not just the added point
      case 8 : super.setValue(POSITION_Z,_value); break;
      case 9 : trace.setMemorySets(_value.getInteger()); break;
      case 10 : trace.setMemoryDrivenBy(_value.getInteger()); break;
      case 11 : 
        if (_value instanceof IntegerValue) trace.setMemoryColor(DisplayColors.getLineColor(_value.getInteger()));
        else trace.setMemoryColor((java.awt.Color) _value.getObject()); 
        break;
      case 12 : trace.setClearAtInput(_value.getBoolean()); break;
      
      case 13 : trace.setXLabel(_value.getString()); break;
      case 14 : trace.setYLabel(_value.getString()); break;
      case 15 : trace.setZLabel(_value.getString()); break;

      case MY_STYLE : trace.setShapeType(_value.getInteger());         break;
      default: super.setValue(_index-TRACE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case INPUT_X : x = 0.0; xArray = null; break;
      case INPUT_Y : y = 0.0; yArray = null; break;
      case INPUT_Z : z = 0.0; zArray = null; break;

      case 0 : trace.setMaximumPoints(0);         break;
      case 1 : trace.setSkip(0);                  break;
      case 2 : trace.setActive(true);             break;
      case 3 : trace.setIgnoreEqualPoints(false); break;
      case 4 : trace.setConnected(true);          break;
      case 5 : trace.setShapeSize(5);             break;
      case 6 : super.setDefaultValue(POSITION_X); break;
      case 7 : super.setDefaultValue(POSITION_Y); break;
      case 8 : super.setDefaultValue(POSITION_Z); break;
      case 9 : trace.setMemorySets(1);            break;
      case 10 : trace.setMemoryDrivenBy(InteractiveTrace.SHOW_ALL);        break;
      case 11 : trace.setMemoryColor(java.awt.Color.BLACK); break;
      case 12 : trace.setClearAtInput(false); break;

      case 13 : trace.setXLabel(null); break;
      case 14 : trace.setYLabel(null); break;
      case 15 : trace.setZLabel(null); break;

      case MY_STYLE : trace.setShapeType(InteractiveParticle.NONE); break;
      default: super.setDefaultValue(_index-TRACE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case INPUT_X :
      case INPUT_Y :
      case INPUT_Z : return "0";

      case 0 :
      case 1 : return "0";
      case 2 : return "true";
      case 3 : return "false";
      case 4 : return "true";
      case 5 : return "5";
      case 6 : return super.getDefaultValueString(POSITION_X);
      case 7 : return super.getDefaultValueString(POSITION_Y);
      case 8 : return super.getDefaultValueString(POSITION_Z);
      case 9 : return "1";
      case 10 : return "SHOW_ALL";
      case 11 : return "BLACK";
      case 12 : return "false";

      case 13 : return "<none>";
      case 14 : return "<none>";
      case 15 : return "<none>";

      case MY_STYLE : return "NONE";
      default : return super.getDefaultValueString(_index-TRACE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case INPUT_X : case INPUT_Y : case INPUT_Z :
      case 0 : case 1 : case 2 : case 3 :  case 4 :
      case 5 :
        return null;

      case 6 : return super.getValue(POSITION_X);
      case 7 : return super.getValue(POSITION_Y);
      case 8 : return super.getValue(POSITION_Z);

      case 9 : case 10 : case 11 : case 12 : 
      case 13 : case 14 : case 15 : return null;

      default: return super.getValue (_index-TRACE_ADDED);
    }
  }

} // End of class
