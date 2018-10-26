/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.tools.*;

import java.awt.Color;

/**
 * An interactive set of particles
 */
public class ControlTraceSet extends ControlElementSet implements org.colos.ejs.library.control.swing.NeedsPreUpdate,
                                                                  org.colos.ejs.library.control.Resetable,
                                                                  org.colos.ejs.library.control.DataCollector {
  static final protected int TRACESET_ADDED=16;
  static protected final int INPUT_X = POSITION_X+TRACESET_ADDED;
  static protected final int INPUT_Y = POSITION_Y+TRACESET_ADDED;
  static protected final int INPUT_Z = POSITION_Z+TRACESET_ADDED;
  static protected final int MY_STYLE = STYLE + TRACESET_ADDED;

  protected boolean norepeat, isSet=false, isSetZ = false;
  protected int elements=0;
  protected double[] x, y, z;
  protected double[] xOne, yOne, zOne;
  protected String name = "";
  private int[] myPosSpot, mySizeSpot;
  private int myFullPosition, myFullSize;

  public ControlTraceSet () {
    super ();
    int disp = getPropertiesDisplacement();
    myPosSpot  = new int[] {6+disp,7+disp,8+disp};
    mySizeSpot = new int[] {SIZE_X+TRACESET_ADDED+disp,SIZE_Y+TRACESET_ADDED+disp,SIZE_Z+TRACESET_ADDED+disp};
    myFullPosition = POSITION+TRACESET_ADDED+disp;
    myFullSize     = SIZE    +TRACESET_ADDED+disp;
  }

  protected int getPropertiesDisplacement () { return 0; }

  final public int[] getPosSpot () { return myPosSpot; }
  final public int[] getSizeSpot () { return mySizeSpot; }

  final public int getFullPositionSpot () { return myFullPosition; }
  final public int getFullSizeSpot () { return myFullSize; }


  protected Drawable createDrawable () {
    x = xOne = new double[]{0.0}; y = yOne = new double[]{0.0}; z = zOne = new double[]{0.0};
    norepeat = false;
    elementSet = new TraceSet(1);
    ((InteractiveTrace) elementSet.elementAt(0)).setAllowTable(true);
    return elementSet;
  }

  protected void setName (String _name) {
    elementSet.setName(_name);
  }

  public void initialize () { // Overwrites default initialize
    for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).initialize();
    xOne[0] = yOne[0] = zOne[0] = 0.0;
    x = xOne; y = yOne; z = zOne;
  }

  public void reset () { // Overwrites default reset
   //       System.out.println ("Resetting");
    for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
      ((InteractiveTrace)elementSet.elementAt(i)).clear();
    xOne[0] = yOne[0] = zOne[0] = 0.0;
    x = xOne; y = yOne; z = zOne;
  }

  public void onExit () { // free memory
    for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
      ((InteractiveTrace)elementSet.elementAt(i)).clear();
  }

//  public void preupdate () {
//    if (!isSet) return;
//    int maxa = x.length-1, maxb = y.length-1, maxc = z.length-1;
//    for (int i=0,ia=0,ib=0,ic=0,n = elementSet.getNumberOfElements(); i<n; i++) {
//      InteractiveTrace trace = (InteractiveTrace) elementSet.elementAt(i);
//      if (trace.isActive()) trace.addPoint(x[ia], y[ib], z[ic]);
//      if (ia<maxa) ia++;
//      if (ib<maxb) ib++;
//      if (ic<maxc) ic++;
//    }
//    isSet = isSetZ = false;
//  }

  public void preupdate () {
    if (!isSet) return;
    if (isSetZ) {
      int maxa = x.length-1, maxb = y.length-1, maxc = z.length-1;
      for (int i=0,ia=0,ib=0,ic=0,n = elementSet.getNumberOfElements(); i<n; i++) {
        InteractiveTrace trace = (InteractiveTrace) elementSet.elementAt(i);
        if (trace.isActive()) trace.addPoint(x[ia], y[ib], z[ic]);
        if (ia<maxa) ia++;
        if (ib<maxb) ib++;
        if (ic<maxc) ic++;
      }
    }
    else {
      int maxa = x.length-1, maxb = y.length-1;
      for (int i=0,ia=0,ib=0,n = elementSet.getNumberOfElements(); i<n; i++) {
        InteractiveTrace trace = (InteractiveTrace) elementSet.elementAt(i);
        if (trace.isActive()) trace.addPoint(x[ia], y[ib]);
        if (ia<maxa) ia++;
        if (ib<maxb) ib++;
      }
    }
    isSet = isSetZ = false;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

static private java.util.ArrayList<String> infoList=null;

  public java.util.ArrayList<String>  getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>  ();
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
    if (_property.equals("maxpoints"))   return "int|int[] PREVIOUS";
    if (_property.equals("skippoints"))  return "int|int[]";
    if (_property.equals("active"))      return "boolean|boolean[]";
    if (_property.equals("norepeat"))    return "boolean|boolean[]";
    if (_property.equals("connected"))   return "boolean|boolean[]";
    if (_property.equals("style"))       return "TraceMarkerShape|int|int[]";
    if (_property.equals("markersize"))  return "int|int[]";

    if (_property.equals("positionx"))   return "int|double|double[]";
    if (_property.equals("positiony"))   return "int|double|double[]";
    if (_property.equals("positionz"))   return "int|double|double[]";

    if (_property.equals("memory"))      return "int|int[]";
    if (_property.equals("drivenby"))    return "DrivenBy|int|int[]";
    if (_property.equals("memorycolor")) return "Color|Object|Object[]";
    if (_property.equals("clearAtInput"))return "boolean|boolean[]";

    if (_property.equals("xLabel"))    return "String|String[] TRANSLATABLE";
    if (_property.equals("yLabel"))    return "String|String[] TRANSLATABLE";
    if (_property.equals("zLabel"))    return "String|String[] TRANSLATABLE";

    return super.getPropertyInfo(_property);
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),(TraceSet)elementSet));
  }

// ------------------------------------------------
// Variable properties
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
      if      (_property.equals("x")) for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setXLabel(useDefaultTitle ? "Input X" : _value);
      else if (_property.equals("y")) for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setYLabel(useDefaultTitle ? "Input Y" : _value);
      else if (_property.equals("z")) for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setYLabel(useDefaultTitle ? "Input Z" : _value);
    }

    return super.setProperty(_property,_value);
  }

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case INPUT_X :
        if (_value.getObject() instanceof double[]) x = (double[]) _value.getObject();
        else { xOne[0] = _value.getDouble(); x = xOne; }
        isSet = true;
        break;
      case INPUT_Y :
        if (_value.getObject() instanceof double[]) y = (double[]) _value.getObject();
        else { yOne[0] = _value.getDouble(); y = yOne; }
        isSet = true;
        break;
      case INPUT_Z :
        if (_value.getObject() instanceof double[]) z = (double[]) _value.getObject();
        else { zOne[0] = _value.getDouble(); z = zOne; }
        isSet = true;
        isSetZ = true;
        break;

    case 0 :
      if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setMaximumPoints(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setMaximumPoints(val);
        }
      break;
    case 1 :
      if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setSkip(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setSkip(val);
        }
      break;

    case 2 :
      if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setActive(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setActive(val);
        }
      break;
    case 3 :
      if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setIgnoreEqualPoints(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setIgnoreEqualPoints(val);
        }
      break;
    case 4 :
      if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setConnected(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setConnected(val);
        }
      break;
    case 5 :
      if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setShapeSize(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setShapeSize(val);
        }
      break;
    case 6 : super.setValue(POSITION_X,_value);              break;
    case 7 : super.setValue(POSITION_Y,_value);              break;
    case 8 : super.setValue(POSITION_Z,_value);              break;

    case 9 :
      if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++)
            ((InteractiveTrace)elementSet.elementAt(i)).setMemorySets(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
            ((InteractiveTrace)elementSet.elementAt(i)).setMemorySets(val);
        }
      break;
    case 10 :
      if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++)
            ((InteractiveTrace)elementSet.elementAt(i)).setMemoryDrivenBy(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
            ((InteractiveTrace)elementSet.elementAt(i)).setMemoryDrivenBy(val);
        }
      break;
    case 11 :
      if (_value.getObject() instanceof Object[]) {
        Object[] val = (Object[]) _value.getObject();
        for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++)
          ((InteractiveTrace)elementSet.elementAt(i)).setMemoryColor((Color)val[i]);
      }
      else if (_value.getObject() instanceof Color) {
        Color val = (Color) _value.getObject();
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
          ((InteractiveTrace)elementSet.elementAt(i)).setMemoryColor(val);
      }
      break;
    case 12 :
      if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setClearAtInput(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setClearAtInput(val);
        }
      break;

    case 13 :
      if (_value.getObject() instanceof String[]) {
        String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setXLabel(val[i]);
        }
        else {
          String val = _value.getString();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setXLabel(val);
        }
      break;
    case 14 :
      if (_value.getObject() instanceof String[]) {
        String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setYLabel(val[i]);
        }
        else {
          String val = _value.getString();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setYLabel(val);
        }
      break;
    case 15 :
      if (_value.getObject() instanceof String[]) {
        String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setZLabel(val[i]);
        }
        else {
          String val = _value.getString();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setZLabel(val);
        }
      break;

    case MY_STYLE :
      if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setShapeType(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setShapeType(val);
        }
      break;
    default: super.setValue(_index-TRACESET_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case INPUT_X : xOne[0] = 0.0; x = xOne; break;
      case INPUT_Y : yOne[0] = 0.0; y = yOne; break;
      case INPUT_Z : zOne[0] = 0.0; z = zOne; break;

      case 0 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setMaximumPoints(0); break;
      case 1 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setSkip(0);          break;
      case 2 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setActive(true);     break;
      case 3 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setIgnoreEqualPoints(false); break;
      case 4 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setConnected(true);          break;
      case 5 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setShapeSize(5); break;
      case 6 : super.setDefaultValue(POSITION_X);  break;
      case 7 : super.setDefaultValue(POSITION_Y);  break;
      case 8 : super.setDefaultValue(POSITION_Z);  break;
      case 9 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
          ((InteractiveTrace)elementSet.elementAt(i)).setMemorySets(1); break;
      case 10 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
          ((InteractiveTrace)elementSet.elementAt(i)).setMemoryDrivenBy(InteractiveTrace.SHOW_ALL); break;
      case 11 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) {
          InteractiveElement el = elementSet.elementAt(i);
          ((InteractiveTrace)el).setMemoryColor(Color.BLACK);
        }
        break;
      case 12 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setClearAtInput(false); break;

      case 13 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setXLabel(null); break;
      case 14 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setYLabel(null); break;
      case 15 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setZLabel(null); break;

      case MY_STYLE : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveTrace)elementSet.elementAt(i)).setShapeType(InteractiveParticle.NONE); break;
      default: super.setDefaultValue(_index-TRACESET_ADDED); break;
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
      default : return super.getDefaultValueString(_index-TRACESET_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case INPUT_X : case INPUT_Y : case INPUT_Z :
      case 0 : case 1 : case 2 : case 3 :  case 4 :
      case 5 :
      case 9 : case 10 : case 11 : case 12 :
      case 13 : case 14 : case 15 : 
        return null;

      case 6 : return super.getValue(POSITION_X);
      case 7 : return super.getValue(POSITION_Y);
      case 8 : return super.getValue(POSITION_Z);

      default: return super.getValue (_index-TRACESET_ADDED);
    }
  }

} // End of class
