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
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.tools.ToolForData;

import java.awt.Color;


/**
 * An interactive particle
 */
public class ControlPoligon extends ControlInteractiveElement {
  static protected final int POLIGON_ADDED = 17;
  static protected final int INPUT_X = POSITION_X+POLIGON_ADDED;
  static protected final int INPUT_Y = POSITION_Y+POLIGON_ADDED;
  static protected final int INPUT_Z = POSITION_Z+POLIGON_ADDED;
  static protected final int MY_SIZE_X = SIZE_X+POLIGON_ADDED;
  static protected final int MY_SIZE_Y = SIZE_Y+POLIGON_ADDED;
  static protected final int MY_SIZE_Z = SIZE_Z+POLIGON_ADDED;

  static protected final int MY_LINE_COLOR = SECONDARY_COLOR+POLIGON_ADDED;
  static protected final int MY_FILL_COLOR = PRIMARY_COLOR+POLIGON_ADDED;

  static protected final int INDEX_SELECTED = 12;

  protected InteractivePoligon poligon;
  protected ObjectValue[] coordinatesValues;
  protected IntegerValue pointValue = new IntegerValue(-1);
  protected int insensitiveCorner=-1;

  private int[] myPosSpot, mySizeSpot, coordinatesSpot;
  private int myFullPosition, myFullSize;

  public ControlPoligon () {
    super ();
    int disp = getPropertiesDisplacement();
    myPosSpot  = new int[] {4+disp,5+disp,6+disp};
    mySizeSpot = new int[] {MY_SIZE_X+disp,MY_SIZE_Y+disp,MY_SIZE_Z+disp};
    coordinatesSpot = new int[] {INPUT_X+disp,INPUT_Y+disp,INPUT_Z+disp};
    myFullPosition = POSITION+POLIGON_ADDED+disp;
    myFullSize     = SIZE    +POLIGON_ADDED+disp;
  }

  protected int getPropertiesDisplacement () { return 0; }

  final public int[] getMyPosSpot () { return myPosSpot; }
  final public int[] getMySizeSpot () { return mySizeSpot; }

  final public int getMyFullPositionSpot () { return myFullPosition; }
  final public int getMyFullSizeSpot () { return myFullSize; }

  final private int[] getCoordinatesSpot ()  { return coordinatesSpot; }

  protected void setName (String _name) { poligon.setName(_name); } // To be overwritten

  protected Drawable createDrawable () {
    poligon = new InteractivePoligon();
    double[][]data = poligon.getData();
    coordinatesValues     = new ObjectValue[3];
    if (data!=null) {
      coordinatesValues[0] = new ObjectValue(data[0]);
      coordinatesValues[1] = new ObjectValue(data[1]);
      coordinatesValues[2] = new ObjectValue(data[2]);
    }
    poligon.setAllowTable(true);
    return poligon;
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),poligon));
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("maxpoints");
      infoList.add ("connected");
      infoList.add ("closed");
      infoList.add ("fixed");
      infoList.add ("positionx");
      infoList.add ("positiony");
      infoList.add ("positionz");
      infoList.add ("startType");
      infoList.add ("startSize");
      infoList.add ("endType");
      infoList.add ("endSize");
      infoList.add ("neumatic");
      infoList.add ("indexSelected");

      infoList.add ("markershape");
      infoList.add ("markersize");
      infoList.add ("markerColor");
      infoList.add ("markerFill");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("maxpoints"))        return "points";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("maxpoints")) return "int PREVIOUS";
    if (_property.equals("x"))         return "int|double|double[]";
    if (_property.equals("y"))         return "int|double|double[]";
    if (_property.equals("z"))         return "int|double|double[]";
    if (_property.equals("connected")) return "boolean[]";
    if (_property.equals("closed"))    return "boolean";
    if (_property.equals("startType")) return "int|ExtremeType";
    if (_property.equals("startSize")) return "int|double";
    if (_property.equals("endType"))   return "int|ExtremeType";
    if (_property.equals("endSize"))   return "int|double";
    if (_property.equals("neumatic"))      return "int|double";

    if (_property.equals("fixed"))       return "int|boolean[]";

    if (_property.equals("positionx"))   return "int|double";
    if (_property.equals("positiony"))   return "int|double";
    if (_property.equals("positionz"))   return "int|double";

    if (_property.equals("secondaryColor")) return "int|int[]|Color|Object|Object[]";
    if (_property.equals("indexSelected")) return "int";

    if (_property.equals("markershape")) return "MarkerShape|int|int[]";
    if (_property.equals("markersize")) return "int|int[]";
    if (_property.equals("markerColor")) return "Color|Object|Object[]";
    if (_property.equals("markerFill")) return "Color|Object|Object[]";

    return super.getPropertyInfo(_property);
  }

  // Backwards compatibility
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("linecolor")) return super.setProperty ("secondaryColor",_value);
    return super.setProperty(_property,_value);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ExtremeType")>=0 || _propertyType.indexOf("PoligonShape")>=0 ) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("plain"))    return new IntegerValue (InteractivePoligon.PLAIN);
      if (_value.equals("circle"))   return new IntegerValue (InteractivePoligon.CIRCLE);
      if (_value.equals("diamond"))  return new IntegerValue (InteractivePoligon.DIAMOND);
      if (_value.equals("square"))   return new IntegerValue (InteractivePoligon.SQUARE);
      if (_value.equals("line"))     return new IntegerValue (InteractivePoligon.LINE);
      if (_value.equals("arrow"))    return new IntegerValue (InteractivePoligon.ARROW);

      if (_value.equals("filled_circle"))   return new IntegerValue (InteractivePoligon.FILLED_CIRCLE);
      if (_value.equals("filled_diamond"))  return new IntegerValue (InteractivePoligon.FILLED_DIAMOND);
      if (_value.equals("filled_square"))   return new IntegerValue (InteractivePoligon.FILLED_SQUARE);
      if (_value.equals("filled_arrow"))    return new IntegerValue (InteractivePoligon.FILLED_ARROW);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if (poligon.getNumberOfPoints()!=_value.getInteger()) {
          poligon.setNumberOfPoints (_value.getInteger());
          double[][] data = poligon.getData();
          if (data!=null) {
            coordinatesValues[0] = new ObjectValue(data[0]);
            coordinatesValues[1] = new ObjectValue(data[1]);
            coordinatesValues[2] = new ObjectValue(data[2]);
          }
          if (insensitiveCorner >-1 && insensitiveCorner<poligon.getNumberOfPoints()) poligon.setPointSizeEnabled(insensitiveCorner,false);
          else insensitiveCorner = -1;
        }
        break;
      case 1 : if (_value.getObject() instanceof boolean[]) poligon.setConnections((boolean[])_value.getObject()); break;
      case 2 : poligon.setClosed (_value.getBoolean());               break;
      case 3 : // fixed corner(s)
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(val.length,poligon.getNumberOfPoints()); i<n; i++)  poligon.setPointSizeEnabled(i,!val[i]);
          insensitiveCorner = -1;
        }
        else {
          if (insensitiveCorner<0) for (int i=0, n=poligon.getNumberOfPoints(); i<n; i++) poligon.setPointSizeEnabled(i,true);
          else poligon.setPointSizeEnabled(insensitiveCorner,true);
          poligon.setPointSizeEnabled(insensitiveCorner=_value.getInteger(),false);
        }
      break;
      case 4 : super.setValue(POSITION_X,_value);              break;
      case 5 : super.setValue(POSITION_Y,_value);              break;
      case 6 : super.setValue(POSITION_Z,_value);              break;
      case  7 : poligon.setStartType (_value.getInteger());   break;
      case  8 : poligon.setStartSize (_value.getDouble());   break;
      case  9 : poligon.setEndType (_value.getInteger());   break;
      case 10 : poligon.setEndSize (_value.getDouble());   break;
      case 11 : poligon.setNeumatic (_value.getDouble());   break;

      case INDEX_SELECTED : pointValue.value = _value.getInteger(); break;

      case 13 : // shape type
        if (_value.getObject() instanceof int[]) poligon.setShapesType((int[]) _value.getObject());
        else poligon.setShapesType(_value.getInteger());
        break;
      case 14 : // shape size
        if (_value.getObject() instanceof int[]) poligon.setShapesSize((int[]) _value.getObject());
        else poligon.setShapesSize(_value.getInteger());
        break;
      case 15 : // shape edge color
        if (_value.getObject() instanceof Object[]) poligon.setShapesEdgeColor((Object[]) _value.getObject());
        else if (_value.getObject() instanceof Color) poligon.setShapesEdgeColor((Color) _value.getObject());
        break;
      case 16 : // shape fill color
        if (_value.getObject() instanceof Object[]) poligon.setShapesFillColor((Object[]) _value.getObject());
        else if (_value.getObject() instanceof Color) poligon.setShapesFillColor((Color) _value.getObject());
        break;



      case INPUT_X :
        if (_value.getObject() instanceof double[]) poligon.setXs((double[])_value.getObject());
        else poligon.setXs(_value.getDouble());
        // if (insensitiveCorner>-1) polygon.setPointSizeEnabled(insensitiveCorner,false);
        break;
      case INPUT_Y :
        if (_value.getObject() instanceof double[]) poligon.setYs((double[])_value.getObject());
        else poligon.setYs(_value.getDouble());
        // if (insensitiveCorner>-1) polygon.setPointSizeEnabled(insensitiveCorner,false);
        break;
      case INPUT_Z :
        if (_value.getObject() instanceof double[]) poligon.setZs((double[])_value.getObject());
        else poligon.setZs(_value.getDouble());
        // if (insensitiveCorner>-1) polygon.setPointSizeEnabled(insensitiveCorner,false);
        break;

      case MY_FILL_COLOR : super.setValue(SECONDARY_COLOR,_value) ; break;
      case MY_LINE_COLOR :
        if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          int n = val.length;
          Color[] colors = new Color[n];
          for (int i = 0; i < n; i++) colors[i] = DisplayColors.getLineColor(val[i]);
          poligon.setColors(colors);
        }
        else if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          int n = val.length;
          Color[] colors = new Color[n];
          for (int i = 0; i < n; i++) colors[i] = (Color) val[i];
          poligon.setColors(colors);
        }
        else {
          super.setValue(PRIMARY_COLOR, _value);
          poligon.setColors(null);
        }
        break;
      default: super.setValue(_index-POLIGON_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : poligon.setNumberOfPoints (0); break;
      case 1 : poligon.setConnections(null);  break;
      case 2 : poligon.setClosed (true);      break;
      case 3 : for (int i=0, n=poligon.getNumberOfPoints(); i<n; i++) poligon.setPointSizeEnabled(i,true); insensitiveCorner = -1; break;
      case 4 : super.setDefaultValue(POSITION_X);     break;
      case 5 : super.setDefaultValue(POSITION_Y);     break;
      case 6 : super.setDefaultValue(POSITION_Z);     break;

      case  7 : poligon.setStartType (InteractivePoligon.PLAIN);   break;
      case  8 : poligon.setStartSize (Double.NaN);   break;
      case  9 : poligon.setEndType (InteractivePoligon.PLAIN);   break;
      case 10 : poligon.setEndSize (Double.NaN);   break;
      case 11 : poligon.setNeumatic (0.0);   break;

      case INDEX_SELECTED : pointValue.value = -1; break;

      case 13 : poligon.setShapesType(null); break;
      case 14 : poligon.setShapesSize(null); break;
      case 15 : poligon.setShapesEdgeColor((Object[])null); break;
      case 16 : poligon.setShapesFillColor((Object[])null); break;

      case INPUT_X : poligon.setXs(0.0); break;
      case INPUT_Y : poligon.setYs(0.0); break;
      case INPUT_Z : poligon.setZs(0.0); break;

      case  MY_SIZE_X : myElement.setSizeX((sizeValues[0].value=1.0)*scalex); break;
      case  MY_SIZE_Y : myElement.setSizeY((sizeValues[1].value=1.0)*scaley); break;
      case  MY_SIZE_Z : myElement.setSizeZ((sizeValues[2].value=1.0)*scalez); break;

      case MY_FILL_COLOR : super.setDefaultValue(SECONDARY_COLOR) ; break;
      case MY_LINE_COLOR :
        super.setDefaultValue(PRIMARY_COLOR);
        poligon.setColors(null);
        break;
      default: super.setDefaultValue(_index-POLIGON_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "<none>";
      case 2 : return "true";
      case 3 : return "<none>";
      case 4 : return super.getDefaultValueString(POSITION_X);
      case 5 : return super.getDefaultValueString(POSITION_Y);
      case 6 : return super.getDefaultValueString(POSITION_Z);

      case  7 : return "PLAIN";
      case  8 : return "<none>";
      case  9 : return "PLAIN";
      case 10 : return "<none>";
      case 11 : return "0";

      case INDEX_SELECTED : return "<none>";

      case 13 :
      case 14 :
      case 15 :
      case 16 : return "<none>";

      case INPUT_X :
      case INPUT_Y :
      case INPUT_Z : return "<none>";

      case  MY_SIZE_X :
      case  MY_SIZE_Y :
      case  MY_SIZE_Z : return "1";

      case MY_FILL_COLOR : return super.getDefaultValueString(SECONDARY_COLOR) ;
      case MY_LINE_COLOR : return super.getDefaultValueString(PRIMARY_COLOR);
      default: return super.getDefaultValueString(_index-POLIGON_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case INPUT_X : return coordinatesValues[0];
      case INPUT_Y : return coordinatesValues[1];
      case INPUT_Z : return coordinatesValues[2];

      case 0 : case 1 : case 2 : case 3 :
      case 7 : case 8 : case 9 : case 10 :
      case 11 :
      case 13 : case 14 : case 15 : case 16 :
        return null;

      case 4 : return super.getValue(POSITION_X);
      case 5 : return super.getValue(POSITION_Y);
      case 6 : return super.getValue(POSITION_Z);

      case INDEX_SELECTED : return pointValue;

      default: return super.getValue (_index-POLIGON_ADDED);
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  private int getIndex (Object _target) {
    if (_target instanceof InteractionTargetPoligonPoint) return ((InteractionTargetPoligonPoint) _target).getPointIndex();
    if (_target instanceof InteractionTargetPoligonMovingPoint) return ((InteractionTargetPoligonMovingPoint) _target).getPointIndex();
    return -1;
  }

  @SuppressWarnings("fallthrough")
  public void interactionPerformed(InteractionEvent _event) {
  switch (_event.getID()) {
    case InteractionEvent.MOUSE_EXITED :
      invokeActions (ControlInteractiveElement.ACTION_EXITED);
//      pointValue.value = -1; This caused problem with delayed actions
      variableChanged (INDEX_SELECTED,pointValue);
      break;
    case InteractionEvent.MOUSE_ENTERED :
      pointValue.value = getIndex (_event.getTarget());
      variableChanged (INDEX_SELECTED,pointValue);
      invokeActions (ControlInteractiveElement.ACTION_ENTERED);
      break;
    case InteractionEvent.MOUSE_PRESSED :
      pointValue.value = getIndex (_event.getTarget());
      variableChanged (INDEX_SELECTED,pointValue);
      invokeActions (ControlInteractiveElement.ACTION_PRESS);
      // Do not break!
    case InteractionEvent.MOUSE_DRAGGED :
      if (_event.getTarget().getClass()==InteractionTargetPoligonPoint.class) {
        variablesChanged (getCoordinatesSpot(),coordinatesValues);
        if (isUnderEjs) setFieldListValues(getCoordinatesSpot(),coordinatesValues);
      }
      else if (_event.getTarget().getClass()==InteractionTargetElementSize.class) {
        if (scalex!=0.0) sizeValues[0].value = myElement.getSizeX()/scalex; else sizeValues[0].value = myElement.getSizeX();
        if (scaley!=0.0) sizeValues[1].value = myElement.getSizeY()/scaley; else sizeValues[1].value = myElement.getSizeY();
        if (scalez!=0.0) sizeValues[2].value = myElement.getSizeZ()/scalez; else sizeValues[2].value = myElement.getSizeZ();
// System.out.println("Size is now "+sizeValues[0].value+", "+sizeValues[1].value);
        if (theSize!=null) {
          theSize[0] = sizeValues[0].value;
          theSize[1] = sizeValues[1].value;
          if (theSize.length>2) theSize[2] = sizeValues[2].value;
          Value objVal = new ObjectValue(theSize);
          variableChanged (getMyFullSizeSpot(),objVal);
          if (isUnderEjs && enabledEjsEdit) setFieldListValue(getMyFullSizeSpot(),objVal);
        }
        else {
          variablesChanged (getMySizeSpot(),sizeValues);
          if (isUnderEjs && enabledEjsEdit) setFieldListValues(getMySizeSpot(),sizeValues);
        }
      }
      else {
        posValues[0].value = myElement.getX();
        posValues[1].value = myElement.getY();
        posValues[2].value = myElement.getZ();
        if (thePos!=null) {
          thePos[0] = posValues[0].value;
          thePos[1] = posValues[1].value;
          if (thePos.length>2) thePos[2] = posValues[2].value;
          Value objVal = new ObjectValue(thePos);
          variableChanged (getMyFullPositionSpot(),objVal);
          if (isUnderEjs && enabledEjsEdit) setFieldListValue(getMyFullPositionSpot(),objVal);
        }
        else {
          variablesChanged (getMyPosSpot(),posValues);
          if (isUnderEjs && enabledEjsEdit) setFieldListValues(getMyPosSpot(),posValues);
        }
      }
      break;
    case InteractionEvent.MOUSE_RELEASED :
      invokeActions (ControlElement.ACTION);
//      pointValue.value = -1; This caused problem with delayed actions
      variableChanged (INDEX_SELECTED,pointValue);
      break;
    }
  } // End of interaction method


} // End of class
