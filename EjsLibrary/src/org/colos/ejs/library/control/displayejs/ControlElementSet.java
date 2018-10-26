/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.Function;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.displayejs.*;
import java.awt.*;

/**
 * An interactive set of particles
 */
public abstract class ControlElementSet extends ControlDrawable3D implements InteractionListener {
  static protected final int SET_ADDED = 36;

  static protected final int IMAGE = 1;
  static protected final int TEXT = 2;
  static protected final int RADIUS = 3;

  static protected final int POSITION = 33;
  static protected final int POSITION_X = 4;
  static protected final int POSITION_Y = 5;
  static protected final int POSITION_Z = 6;

  static protected final int SIZE = 34;
  static protected final int SIZE_X = 7;
  static protected final int SIZE_Y = 8;
  static protected final int SIZE_Z = 9;

  static protected final int ENABLED = 11;
  static protected final int ENABLED_SECONDARY = 12;
  static protected final int STYLE = 18;
  static protected final int PRIMARY_COLOR = 22;
  static protected final int SECONDARY_COLOR = 23;

  static protected final int ELEMENT_SELECTED=31;
  static protected final int MEASURED=35;

  protected ElementSet elementSet;

  protected ObjectValue[] allposValues, allsizesValue;
  protected IntegerValue selectedValue = new IntegerValue(-1);

  protected double[] theXs, theYs, theZs;
  protected double[][] thePos=null, theSize=null;
  protected double[] theSizeXs, theSizeYs, theSizeZs;
  protected double scalex = 1.0, scaley = 1.0, scalez = 1.0, lineWidth = 1.0;
  protected java.awt.Font font, defaultFont;
  protected int sensitivity = AbstractInteractiveElement.SENSIBILITY;
  protected boolean numberOfElements_isSet=false;

  private int[] posSpot, sizeSpot;
  private int fullPosition, fullSize;
  private int elementSelectedSpot;

  public ControlElementSet () {
    super ();
    allposValues     = new ObjectValue[3];
    allposValues[0]  = new ObjectValue(theXs = elementSet.getXs());
    allposValues[1]  = new ObjectValue(theYs = elementSet.getYs());
    allposValues[2]  = new ObjectValue(theZs = elementSet.getZs());
    allsizesValue    = new ObjectValue[3];
    allsizesValue[0] = new ObjectValue(theSizeXs = elementSet.getSizeXs());
    allsizesValue[1] = new ObjectValue(theSizeYs = elementSet.getSizeYs());
    allsizesValue[2] = new ObjectValue(theSizeZs = elementSet.getSizeZs());
    elementSet.addListener(this);
    defaultFont = font = elementSet.elementAt(0).getStyle().getFont();

    int disp = getPropertiesDisplacement();
    posSpot  = new int[] {POSITION_X+disp,POSITION_Y+disp,POSITION_Z+disp};
    sizeSpot = new int[] {SIZE_X+disp,SIZE_Y+disp,SIZE_Z+disp};
    fullPosition = POSITION+disp;
    fullSize     = SIZE    +disp;
    elementSelectedSpot = ELEMENT_SELECTED + disp;
  }

  /**
   * This tells how many properties have been added on top of those of the basic ControlElement3D class
   */
  protected abstract int getPropertiesDisplacement ();

  // Next four methods are not final because ControlTrace redefines them
  protected int[] getPosSpot () { return posSpot; }
  protected int[] getSizeSpot () { return sizeSpot; }
  protected int getFullPositionSpot () { return fullPosition; }
  protected int getFullSizeSpot () { return fullSize; }

  protected int getElementSpot()  { return elementSelectedSpot; }

// ------------------------------------------------
// Definition of functions
// ------------------------------------------------

  protected Function elementFunction = new MyFunction();

  private class MyFunction extends Function {
    public double eval () { return elementSet.getInteractedIndex(); }
  }

  public Object getObject (String _name) {
    if (_name.equals("elementSelected")) return elementFunction;
    return null;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String>  getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>  ();
      infoList.add ("elementnumber");

      // Next two must be here because nothing can appear before elementnumber!!!

      infoList.add ("image");
      infoList.add ("text");
      infoList.add ("radius");

      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("z");
      infoList.add ("sizex");
      infoList.add ("sizey");
      infoList.add ("sizez");

      infoList.add ("visible");
      infoList.add ("enabled");
      infoList.add ("enabledSecondary");

      infoList.add ("scalex");
      infoList.add ("scaley");
      infoList.add ("scalez");

      infoList.add ("group");
      infoList.add ("groupEnabled");

      infoList.add ("style");
      infoList.add ("elementposition");
      infoList.add ("angle");
      infoList.add ("resolution");

      infoList.add ("color");
      infoList.add ("secondaryColor");
      infoList.add ("stroke");
      infoList.add ("font");

      infoList.add ("pressaction");
      infoList.add ("dragaction");
      infoList.add ("action");
      infoList.add ("enteredAction");
      infoList.add ("exitedAction");

      infoList.add ("elementSelected");
      infoList.add ("sensitivity");
      
      infoList.add ("position");
      infoList.add ("size");

      infoList.add ("measured");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("elementnumber")) return "numberOfElements";
    if (_property.equals("angle")) return "rotationAngle";
    if (_property.equals("color"))          return "fillColor";
    if (_property.equals("secondaryColor")) return "lineColor";
    if (_property.equals("action"))      return "releaseAction";
    if (_property.equals("pressaction")) return "pressAction";
    if (_property.equals("dragaction"))  return "dragAction";
    if (_property.equals("enabled"))          return "enabledPosition";
    if (_property.equals("enabledSecondary")) return "enabledSize";
    if (_property.equals("sizex")) return "sizeX";
    if (_property.equals("sizey")) return "sizeY";
    if (_property.equals("sizez")) return "sizeZ";
    if (_property.equals("size")) return "sizeArray";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("elementnumber")) return "int PREVIOUS";

    if (_property.equals("x"))           return "int|double|double[]";
    if (_property.equals("y"))           return "int|double|double[]";
    if (_property.equals("z"))           return "int|double|double[]";
    if (_property.equals("sizex"))       return "int|double|double[]";
    if (_property.equals("sizey"))       return "int|double|double[]";
    if (_property.equals("sizez"))       return "int|double|double[]";

    if (_property.equals("visible"))        return "boolean|boolean[]";
    if (_property.equals("enabled"))        return "boolean|boolean[]";
    if (_property.equals("enabledSecondary")) return "boolean|boolean[]";

    if (_property.equals("scalex"))         return "int|double";
    if (_property.equals("scaley"))         return "int|double";
    if (_property.equals("scalez"))         return "int|double";

    if (_property.equals("style"))          return "MarkerShape|int|int[]";
    if (_property.equals("elementposition"))return "ElementPosition|int|int[]";
    if (_property.equals("angle"))          return "int|int[]|double|double[]";
    if (_property.equals("resolution"))     return "Resolution";

    if (_property.equals("color"))          return "int|int[]|Color|Object|Object[]";
    if (_property.equals("secondaryColor")) return "int|int[]|Color|Object|Object[]";
    if (_property.equals("stroke"))         return "int|double|Object";
    if (_property.equals("font"))           return "Font|Object ";

    if (_property.equals("action"))      return "Action CONSTANT";
    if (_property.equals("pressaction")) return "Action CONSTANT";
    if (_property.equals("dragaction"))  return "Action CONSTANT";
    if (_property.equals("enteredAction"))  return "Action CONSTANT";
    if (_property.equals("exitedAction"))  return "Action CONSTANT";

    if (_property.equals("elementSelected")) return "int";
    if (_property.equals("sensitivity")) return "int";
    
    if (_property.equals("position"))    return "double[][]";
    if (_property.equals("size"))        return "double[][]";

    if (_property.equals("measured"))        return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  protected void checkNumberOfElements(int newNumber, boolean force) {
    if (elementSet.getNumberOfElements()==newNumber && !force) return;
    elementSet.setNumberOfElements(newNumber);
    allposValues[0].value  = theXs = elementSet.getXs();
    allposValues[1].value  = theYs = elementSet.getYs();
    allposValues[2].value  = theZs = elementSet.getZs();
    allsizesValue[0].value = theSizeXs = elementSet.getSizeXs();
    allsizesValue[1].value = theSizeYs = elementSet.getSizeYs();
    allsizesValue[2].value = theSizeZs = elementSet.getSizeZs();
    for (int i=0; i<newNumber; i++) {
      InteractiveElement element = elementSet.elementAt(i);
      if (scalex!=0.0) theSizeXs[i] = element.getSizeX()/scalex; else theSizeXs[i] = element.getSizeX();
      if (scaley!=0.0) theSizeYs[i] = element.getSizeY()/scaley; else theSizeYs[i] = element.getSizeY();
      if (scalez!=0.0) theSizeZs[i] = element.getSizeZ()/scalez; else theSizeZs[i] = element.getSizeZ();
      element.initializeMemberOfSet();
      element.setSensitivity (sensitivity);
    }
    org.colos.ejs.library.control.EjsControl group = getGroup();
    // Required in case the number of elements is set AFTER other values, such as the position
    // But not if this is caused by a report of a change, this would revert the change
    if (group!=null && !group.isReportingChange()) group.propagateValues(); 
  }

  public void setValue (int _index, Value _value) {
//    System.out.println ("Setting "+_index+" to "+_value);
    switch (_index) {
      case 0 :
        if (_value.getInteger()!=elementSet.getNumberOfElements()) checkNumberOfElements(_value.getInteger(),true);
        numberOfElements_isSet = true;
        break;
      case IMAGE : break; // Subclasses will use this
      case TEXT : break; // Subclasses will use this
      case RADIUS : break; // Subclasses will use this

      case POSITION :
        if (_value.getObject() instanceof double[][]) {
          double[][] val = thePos = (double[][]) _value.getObject();
          if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
          for (int i=0, n=Math.min(theXs.length,val.length); i<n; i++) elementSet.elementAt(i).setX(theXs[i] = val[i][0]);
          if (val[0].length>1) 
            for (int i=0, n=Math.min(theYs.length,val.length); i<n; i++) elementSet.elementAt(i).setY(theYs[i] = val[i][1]);
          if (val[0].length>2) 
            for (int i=0, n=Math.min(theZs.length,val.length); i<n; i++) elementSet.elementAt(i).setZ(theZs[i] = val[i][2]);
        }
        break;
      case POSITION_X :
        thePos = null;
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
          for (int i=0, n=Math.min(theXs.length,val.length); i<n; i++) elementSet.elementAt(i).setX(theXs[i] = val[i]);
        }
        else {
          double val = _value.getDouble();
          for (int i=0, n=theXs.length; i<n; i++) elementSet.elementAt(i).setX(theXs[i]=val);
        }
        break;
      case POSITION_Y :
        thePos = null;
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theYs.length,val.length); i<n; i++) elementSet.elementAt(i).setY(theYs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theYs.length; i<n; i++) elementSet.elementAt(i).setY(theYs[i]=val);
          }
        break;
      case POSITION_Z :
        thePos = null;
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theZs.length,val.length); i<n; i++) elementSet.elementAt(i).setZ(theZs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theZs.length; i<n; i++) elementSet.elementAt(i).setZ(theZs[i]=val);
          }
        break;
      case SIZE :
        if (_value.getObject() instanceof double[][]) {
          double[][] val = theSize = (double[][]) _value.getObject();
          if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
          for (int i=0, n=Math.min(theSizeXs.length,val.length); i<n; i++) elementSet.elementAt(i).setSizeX(theSizeXs[i] = val[i][0]);
          if (val[0].length>1) 
            for (int i=0, n=Math.min(theSizeYs.length,val.length); i<n; i++) elementSet.elementAt(i).setSizeY(theSizeYs[i] = val[i][1]);
          if (val[0].length>2) 
            for (int i=0, n=Math.min(theSizeZs.length,val.length); i<n; i++) elementSet.elementAt(i).setSizeZ(theSizeZs[i] = val[i][2]);
        }
        break;
      case SIZE_X :
        theSize = null;
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theSizeXs.length,val.length); i<n; i++) elementSet.elementAt(i).setSizeX((theSizeXs[i]=val[i])*scalex);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theSizeXs.length; i<n; i++) elementSet.elementAt(i).setSizeX((theSizeXs[i]=val)*scalex);
          }
        break;
      case SIZE_Y :
        theSize = null;
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theSizeYs.length,val.length); i<n; i++) elementSet.elementAt(i).setSizeY((theSizeYs[i]=val[i])*scaley);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theSizeYs.length; i<n; i++) elementSet.elementAt(i).setSizeY((theSizeYs[i]=val)*scaley);
          }
        break;
      case SIZE_Z :
        theSize = null;
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theSizeZs.length,val.length); i<n; i++) elementSet.elementAt(i).setSizeZ((theSizeZs[i]=val[i])*scalez);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theSizeZs.length; i<n; i++) elementSet.elementAt(i).setSizeZ((theSizeZs[i]=val)*scalez);
          }
        break;
      case 10 :
        if (_value.getObject() instanceof boolean[]) elementSet.setVisibles((boolean[]) _value.getObject());
        else elementSet.setVisible(_value.getBoolean());
        break;
        // Some elements may change this. For instance, arrows
      case  ENABLED :
        if (_value.getObject() instanceof boolean[]) elementSet.setEnableds(InteractiveElement.TARGET_POSITION,(boolean[]) _value.getObject());
        else elementSet.setEnabled(InteractiveElement.TARGET_POSITION,_value.getBoolean());
        break;
      case  ENABLED_SECONDARY :
        if (_value.getObject() instanceof boolean[]) elementSet.setEnableds(InteractiveElement.TARGET_SIZE,(boolean[]) _value.getObject());
        else elementSet.setEnabled(InteractiveElement.TARGET_SIZE,_value.getBoolean());
        break;

      case 13 :
        scalex = _value.getDouble();
        for (int i=0, n=theSizeXs.length; i<n; i++) elementSet.elementAt(i).setSizeX(theSizeXs[i]*scalex);
        break;
      case 14 :
        scaley = _value.getDouble();
        for (int i=0, n=theSizeYs.length; i<n; i++) elementSet.elementAt(i).setSizeY(theSizeYs[i]*scaley);
        break;
      case 15 :
        scalez = _value.getDouble();
        for (int i=0, n=theSizeZs.length; i<n; i++) elementSet.elementAt(i).setSizeZ(theSizeZs[i]*scalez);
        break;

      case 16 : break; // Groups... how to implement this???
      case 17 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).setGroupEnabled(_value.getBoolean()); break;

      case STYLE : break; // To be implemented by subclasses

      case 19 :
        if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setPosition(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setPosition(val);
          }
        break;

      case 20 :
        if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++)
              elementSet.elementAt(i).getStyle().setAngle(val[i]);
          }
        else if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++)
            elementSet.elementAt(i).getStyle().setAngle(val[i]*ControlDrawingPanel3D.TO_RAD);
        }
        else if (_value instanceof IntegerValue) {
          double val = _value.getInteger()*ControlDrawingPanel3D.TO_RAD;
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setAngle(val);
        }
        else {
          double val = _value.getDouble();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setAngle(val);
        }
        break;

      case 21 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).setResolution((Resolution)_value.getObject()); break;

      case PRIMARY_COLOR :
        if (_value instanceof IntegerValue) {
          Color col = DisplayColors.getLineColor(_value.getInteger());
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(col);
        }
        else if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(DisplayColors.getLineColor(val[i]));
        }
        else if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor((Color)val[i]);
        }
        else if (_value.getObject() instanceof Color) {
          Color val = (Color) _value.getObject();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(val);
        }
        break;

      case SECONDARY_COLOR :
        if (_value instanceof IntegerValue) {
          Color col = DisplayColors.getLineColor(_value.getInteger());
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(col);
        }
        else if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(DisplayColors.getLineColor(val[i]));
        }
        else if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern((Paint)val[i]);
        }
        else if (_value.getObject() instanceof Color) {
          java.awt.Paint fill = (java.awt.Paint) _value.getObject();
          if (fill==NULL_COLOR) fill = null;
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(fill);
        }
        break;

      case 24 :
        if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke((Stroke)val[i]);
        }
        else if (_value.getObject() instanceof Stroke) {
          Stroke val = (Stroke) _value.getObject();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke(val);
        }
        else if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) {
            BasicStroke stroke;
            if (val[i]<0) stroke = new java.awt.BasicStroke((float) -val[i], BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0);
            else stroke = new java.awt.BasicStroke((float) val[i]);
            elementSet.elementAt(i).getStyle().setEdgeStroke(stroke);
          }
        }
        else if (lineWidth!=_value.getDouble()) {
          lineWidth = _value.getDouble();
          BasicStroke stroke;
          if (lineWidth<0) stroke = new java.awt.BasicStroke((float) -lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0);
          else stroke = new java.awt.BasicStroke((float) lineWidth);
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        break;

      case 25 : if (_value.getObject() instanceof java.awt.Font) {
          Font newFont = (Font) _value.getObject();
          if (newFont!=font) {
            font = newFont;
            for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFont(newFont);
          }
        }
        break;

      case 26 : // pressaction
        removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlInteractiveElement.ACTION_PRESS,_value.getString());
        return;
      case 27 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        return;
      case 28 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        return;
      case 29 : // enteredAction
        removeAction (ControlInteractiveElement.ACTION_ENTERED,getProperty("enteredAction"));
        addAction(ControlInteractiveElement.ACTION_ENTERED,_value.getString());
        return;
      case 30 : // exitedAction
        removeAction (ControlInteractiveElement.ACTION_EXITED,getProperty("exitedAction"));
        addAction(ControlInteractiveElement.ACTION_EXITED,_value.getString());
        return;
      case ELEMENT_SELECTED : selectedValue.value = _value.getInteger(); break;

      case 32 :
        sensitivity = _value.getInteger();
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).setSensitivity(sensitivity); break;

      case MEASURED : elementSet.canBeMeasured(_value.getBoolean()); break;

      default: super.setValue(_index-SET_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :
        checkNumberOfElements(1,true);
        numberOfElements_isSet = false;
        break;

      case IMAGE : break; // Subclasses will use this
      case TEXT : break; // Subclasses will use this
      case RADIUS : break; // Subclasses will use this

      case POSITION : thePos = null; break;
      case POSITION_X : for (int i=0, n=theXs.length; i<n; i++) elementSet.elementAt(i).setX(theXs[i]=0.0);  break;
      case POSITION_Y : for (int i=0, n=theYs.length; i<n; i++) elementSet.elementAt(i).setY(theYs[i]=0.0);  break;
      case POSITION_Z : for (int i=0, n=theZs.length; i<n; i++) elementSet.elementAt(i).setZ(theZs[i]=0.0);  break;

      case SIZE : theSize = null; break;
      case SIZE_X : for (int i=0, n=theSizeXs.length; i<n; i++) elementSet.elementAt(i).setSizeX((theSizeXs[i]=0.1)*scalex); break;
      case SIZE_Y : for (int i=0, n=theSizeYs.length; i<n; i++) elementSet.elementAt(i).setSizeY((theSizeYs[i]=0.1)*scaley); break;
      case SIZE_Z : for (int i=0, n=theSizeZs.length; i<n; i++) elementSet.elementAt(i).setSizeZ((theSizeZs[i]=0.1)*scalez); break;

      case 10 : elementSet.setVisible(true); break;
        // Some elements may change this. For instance, arrows
      case  ENABLED : elementSet.setEnabled(InteractiveElement.TARGET_POSITION,true); break;
      case  ENABLED_SECONDARY : elementSet.setEnabled(InteractiveElement.TARGET_SIZE,false); break;

      case 13 : scalex = 1.0; for (int i=0, n=theSizeXs.length; i<n; i++) elementSet.elementAt(i).setSizeX(theSizeXs[i]); break;
      case 14 : scaley = 1.0; for (int i=0, n=theSizeYs.length; i<n; i++) elementSet.elementAt(i).setSizeY(theSizeYs[i]); break;
      case 15 : scalez = 1.0; for (int i=0, n=theSizeZs.length; i<n; i++) elementSet.elementAt(i).setSizeZ(theSizeZs[i]); break;

      case 16 : break; // Groups... how to implement this???
      case 17 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).setGroupEnabled(true); break;

      case STYLE : break; // To be implemented by subclasses

      case 19 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setPosition(Style.CENTERED); break;
      case 20 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setAngle(0.0); break;
      case 21 : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).setResolution(null); break;

      case PRIMARY_COLOR : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(Color.black); break;
      case SECONDARY_COLOR : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(Color.blue); break;

      case 24 :
        {
          BasicStroke stroke = new java.awt.BasicStroke((float) (lineWidth=1.0));
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        break;

      case 25 :
        {
          font = defaultFont;
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFont(font);
        }
        break;

      case 26 : removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction")); return;
      case 27 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));         return;
      case 28 : removeAction (ControlElement.ACTION,getProperty("action"));                       return;
      case 29 : removeAction (ControlInteractiveElement.ACTION_ENTERED,getProperty("enteredAction")); return;
      case 30 : removeAction (ControlInteractiveElement.ACTION_EXITED,getProperty("exitedAction")); return;

      case ELEMENT_SELECTED : selectedValue.value = -1; break;
      case 32 :
        sensitivity = AbstractInteractiveElement.SENSIBILITY;
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).setSensitivity(sensitivity); break;

      case MEASURED : elementSet.canBeMeasured(true); break;

      default : super.setDefaultValue (_index-SET_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "1";
      case IMAGE :
      case TEXT :
      case RADIUS : return "<none>";

      case POSITION : return "<none>";
      case POSITION_X :
      case POSITION_Y :
      case POSITION_Z : return "0";

      case SIZE : return "<none>";
      case SIZE_X :
      case SIZE_Y :
      case SIZE_Z : return "0.1";

      case 10 : return "true";
        // Some elements may change this. For instance, arrows
      case  ENABLED : return "true";
      case  ENABLED_SECONDARY : return "false";

      case 13 :
      case 14 :
      case 15 : return "1";

      case 16 : return "<none>";
      case 17 : return "true";

      case STYLE : return "<none>";

      case 19 : return "CENTERED";
      case 20 : return "0";
      case 21 : return "<none>";

      case PRIMARY_COLOR : return "BLACK";
      case SECONDARY_COLOR : return "BLUE";

      case 24 : return "1";
      case 25 : return "<none>";

      case 26 :
      case 27 :
      case 28 :
      case 29 :
      case 30 : return "<no_action>";

      case ELEMENT_SELECTED : return "-1";
      case 32 : return ""+AbstractInteractiveElement.SENSIBILITY;

      case MEASURED : return "true";

      default : return super.getDefaultValueString(_index-SET_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case POSITION : return new ObjectValue(thePos);
      case POSITION_X : return allposValues[0];
      case POSITION_Y : return allposValues[1];
      case POSITION_Z : return allposValues[2];
      case SIZE : return new ObjectValue(theSize);
      case SIZE_X : return allsizesValue[0];
      case SIZE_Y : return allsizesValue[1];
      case SIZE_Z : return allsizesValue[2];
      case ELEMENT_SELECTED : return selectedValue;
      default: 
        if (_index<SET_ADDED) return null;
        return super.getValue(_index-SET_ADDED);
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  @SuppressWarnings("fallthrough")
  public void interactionPerformed(InteractionEvent _event) {
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_EXITED :
        invokeActions (ControlInteractiveElement.ACTION_EXITED);
//        selectedValue.value = -1; This caused problem with delayed actions
        variableExtraChanged (getElementSpot(),selectedValue);
        break;
      case InteractionEvent.MOUSE_ENTERED :
        selectedValue.value = elementSet.getInteractedIndex();
        variableExtraChanged (getElementSpot(),selectedValue);
        invokeActions (ControlInteractiveElement.ACTION_ENTERED);
        break;
      case InteractionEvent.MOUSE_PRESSED :
        selectedValue.value = elementSet.getInteractedIndex();
        variableExtraChanged (getElementSpot(),selectedValue);
        invokeActions (ControlInteractiveElement.ACTION_PRESS);
        // Do not break!
      case InteractionEvent.MOUSE_DRAGGED :
        InteractionTargetSetElement elementTarget = (InteractionTargetSetElement) _event.getTarget();
        int i = elementTarget.getElementIndex();
        if (elementTarget.getElementTarget().getClass()==InteractionTargetElementSize.class) { // Size
          if (scalex!=0.0) theSizeXs[i] = elementSet.elementAt(i).getSizeX()/scalex; else theSizeXs[i] = elementSet.elementAt(i).getSizeX();
          if (scaley!=0.0) theSizeYs[i] = elementSet.elementAt(i).getSizeY()/scaley; else theSizeYs[i] = elementSet.elementAt(i).getSizeY();
          if (scalez!=0.0) theSizeZs[i] = elementSet.elementAt(i).getSizeZ()/scalez; else theSizeZs[i] = elementSet.elementAt(i).getSizeZ();
          if (theSize!=null) {
            theSize[i][0] = theSizeXs[i];
            theSize[i][1] = theSizeYs[i];
            if (theSize[i].length>2) theSize[i][2] = theSizeZs[i];
            Value objVal = new ObjectValue(theSize);
            variableChanged (getFullSizeSpot(),objVal);
            if (isUnderEjs) setFieldListValue(getFullSizeSpot(),objVal);
          }
          else {
            variablesChanged (getSizeSpot(),allsizesValue);
            if (isUnderEjs) setFieldListValues(getSizeSpot(),allsizesValue);
          }
        }
        else { // Position
          theXs[i] = elementSet.elementAt(i).getX();
          theYs[i] = elementSet.elementAt(i).getY();
          theZs[i] = elementSet.elementAt(i).getZ();
          if (thePos!=null) {
            thePos[i][0] = theXs[i];
            thePos[i][1] = theYs[i];
            if (thePos[i].length>2) thePos[i][2] = theZs[i];
            Value objVal = new ObjectValue(thePos);
            variableChanged (getFullPositionSpot(),objVal);
            if (isUnderEjs) setFieldListValue(getFullPositionSpot(),objVal);
          }
          else {
            variablesChanged (getPosSpot(),allposValues);
            if (isUnderEjs) setFieldListValues(getPosSpot(),allposValues);
          }
        }
        break;
      case InteractionEvent.MOUSE_RELEASED :
        invokeActions (ControlElement.ACTION);
//        selectedValue.value = -1; This caused problem with delayed actions
        variableExtraChanged (getElementSpot(),selectedValue);
        break;
    }
  } // End of interaction method

} // End of class
