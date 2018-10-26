/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.displayejs.*;
import java.awt.Stroke;
import java.awt.BasicStroke;

public abstract class ControlInteractiveElement extends ControlDrawable3D implements InteractionListener {
  static public final int IE_ADDED = 31;

  static public final int ACTION_PRESS   = 10;
  static public final int ACTION_ENTERED = 11;
  static public final int ACTION_EXITED = 12;

  static protected final int POSITION_X = 0;
  static protected final int POSITION_Y = 1;
  static protected final int POSITION_Z = 2;
  static protected final int SIZE_X = 3;
  static protected final int SIZE_Y = 4;
  static protected final int SIZE_Z = 5;
  static protected final int ENABLED = 7;
  static protected final int ENABLED_SECONDARY = 8;
  static protected final int STYLE = 14;
  static protected final int PRIMARY_COLOR = 18;
  static protected final int SECONDARY_COLOR = 19;
  static protected final int STROKE = 20;

  static protected final int POSITION = 28;
  static protected final int SIZE = 29;
  static protected final int MEASURED = 30;

  protected InteractiveElement myElement;
  protected double[] thePos=null, theSize=null;
  protected DoubleValue[] posValues ={ new DoubleValue(0.0), new DoubleValue(0.0), new DoubleValue(0.0)};
  protected DoubleValue[] sizeValues; // ={ new DoubleValue(0.1), new DoubleValue(0.1), new DoubleValue(0.1)};
  protected double scalex=1.0, scaley=1.0, scalez=1.0, lineWidth=1.0;
//  protected java.awt.Stroke stroke=null;
  protected java.awt.Font font, defaultFont;
  protected boolean enabledEjsEdit=false;

  private int[] posSpot, sizeSpot;
  private int fullPosition, fullSize;

  public ControlInteractiveElement () {
    super ();
    if (sizeValues==null)
      sizeValues = new DoubleValue[] { new DoubleValue(0.1), new DoubleValue(0.1), new DoubleValue(0.1)};
    myElement = (InteractiveElement) getDrawable();
    myElement.addListener(this);
    myElement.setDataObject(this);
    defaultFont = font = myElement.getStyle().getFont();
    int disp = getPropertiesDisplacement();
    posSpot  = new int[] {POSITION_X+disp,POSITION_Y+disp,POSITION_Z+disp};
    sizeSpot = new int[] {SIZE_X+disp,SIZE_Y+disp,SIZE_Z+disp};
    fullPosition = POSITION+disp;
    fullSize     = SIZE    +disp;
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

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
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

      infoList.add ("sensitivity");

      infoList.add ("position");
      infoList.add ("size");
      
      infoList.add ("measured");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
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
    if (_property.equals("x"))           return "int|double";
    if (_property.equals("y"))           return "int|double";
    if (_property.equals("z"))           return "int|double";
    if (_property.equals("sizex"))       return "int|double";
    if (_property.equals("sizey"))       return "int|double";
    if (_property.equals("sizez"))       return "int|double";

    if (_property.equals("visible"))        return "boolean";
    if (_property.equals("enabled"))        return "boolean";
    if (_property.equals("enabledSecondary")) return "boolean";

    if (_property.equals("scalex"))         return "int|double";
    if (_property.equals("scaley"))         return "int|double";
    if (_property.equals("scalez"))         return "int|double";

    if (_property.equals("style"))          return "MarkerShape|int";
    if (_property.equals("elementposition"))return "ElementPosition|int";
    if (_property.equals("angle"))          return "int|double";
    if (_property.equals("resolution"))     return "Resolution|String|Object";

    if (_property.equals("color"))          return "int|Color|Object";
    if (_property.equals("secondaryColor")) return "int|Color|Object";
    if (_property.equals("stroke"))         return "int|double|Object";
    if (_property.equals("font"))           return "Font|Object";

    if (_property.equals("action"))      return "Action CONSTANT";
    if (_property.equals("pressaction")) return "Action CONSTANT";
    if (_property.equals("dragaction"))  return "Action CONSTANT";
    if (_property.equals("enteredAction"))  return "Action CONSTANT";
    if (_property.equals("exitedAction"))  return "Action CONSTANT";

    if (_property.equals("sensitivity")) return "int";

    if (_property.equals("position"))    return "double[]";
    if (_property.equals("size"))        return "double[]";
    
    if (_property.equals("measured"))        return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  POSITION : 
        if (_value.getObject() instanceof double[]) {
          double[] val = thePos = (double[]) _value.getObject();
          myElement.setX(posValues[0].value=val[0]); 
          myElement.setY(posValues[1].value=val[1]); 
          if (val.length>2) myElement.setZ(posValues[2].value=val[2]); 
        }
        break;
      case  POSITION_X : thePos = null; myElement.setX(posValues[0].value=_value.getDouble()); break;
      case  POSITION_Y : thePos = null; myElement.setY(posValues[1].value=_value.getDouble()); break;
      case  POSITION_Z : thePos = null; myElement.setZ(posValues[2].value=_value.getDouble()); break;
      case  SIZE :
        if (_value.getObject() instanceof double[]) {
          double[] val = theSize = (double[]) _value.getObject();
          myElement.setSizeX((sizeValues[0].value=val[0])*scalex); 
          myElement.setSizeY((sizeValues[1].value=val[1])*scaley);
          if (val.length>2) myElement.setSizeZ((sizeValues[2].value=val[2])*scalez); 
        }
        break;
      case  SIZE_X : theSize = null; myElement.setSizeX((sizeValues[0].value=_value.getDouble())*scalex); break;
      case  SIZE_Y : theSize = null; myElement.setSizeY((sizeValues[1].value=_value.getDouble())*scaley); break;
      case  SIZE_Z : theSize = null; myElement.setSizeZ((sizeValues[2].value=_value.getDouble())*scalez); break;
      case  6 : myElement.setVisible(_value.getBoolean()); break;
        // Some elements may change this. For instance, arrows
      case  ENABLED           : myElement.setEnabled(InteractiveElement.TARGET_POSITION,_value.getBoolean()); break;
      case  ENABLED_SECONDARY : myElement.setEnabled(InteractiveElement.TARGET_SIZE,_value.getBoolean()); break;

      case  9 : scalex = _value.getDouble(); myElement.setSizeX(sizeValues[0].value*scalex); break;
      case 10 : scaley = _value.getDouble(); myElement.setSizeY(sizeValues[1].value*scaley); break;
      case 11 : scalez = _value.getDouble(); myElement.setSizeZ(sizeValues[2].value*scalez); break;

      case 12 : break; // Groups... how to implement this???
      case 13 : myElement.setGroupEnabled(_value.getBoolean()); break;

      case STYLE : break; // To be implemented by subclasses
      case 15 : myElement.getStyle().setPosition(_value.getInteger()); break;
      case 16 :
        if (_value instanceof IntegerValue) myElement.getStyle().setAngle(_value.getInteger()*ControlDrawingPanel3D.TO_RAD);
        else myElement.getStyle().setAngle(_value.getDouble());
        break;
      case 17 :
        if (_value.getObject() instanceof Resolution) myElement.setResolution((Resolution)_value.getObject());
        else {
          Resolution res = ControlDrawable3D.decodeResolution (_value.toString());
          if (res!=null) myElement.setResolution(res);
        }
        break;

      case PRIMARY_COLOR     : 
        if (_value instanceof IntegerValue) myElement.getStyle().setEdgeColor(DisplayColors.getLineColor(_value.getInteger()));
        else myElement.getStyle().setEdgeColor((java.awt.Color) _value.getObject()); 
        break;
      case SECONDARY_COLOR   :
        if (_value instanceof IntegerValue) myElement.getStyle().setFillPattern(DisplayColors.getLineColor(_value.getInteger()));
        else {
          java.awt.Paint fill = (java.awt.Paint) _value.getObject();
          if (fill==NULL_COLOR) fill = null;
          myElement.getStyle().setFillPattern(fill);
        }
        break;
      case STROKE :
        if (_value.getObject() instanceof Stroke) myElement.getStyle().setEdgeStroke((Stroke) _value.getObject());
        else if (lineWidth!=_value.getDouble()) {
          lineWidth = _value.getDouble();
          if (lineWidth<0) myElement.getStyle().setEdgeStroke(
              new java.awt.BasicStroke((float) -lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0) );
          else myElement.getStyle().setEdgeStroke(new java.awt.BasicStroke((float) lineWidth));
        }
        break;
      case 21 : if (_value.getObject() instanceof java.awt.Font) {
          java.awt.Font newFont = (java.awt.Font) _value.getObject();
          if (newFont!=font) myElement.getStyle().setFont(font=newFont);
        }
        break;
      case 22 : // pressaction
        removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlInteractiveElement.ACTION_PRESS,_value.getString());
        return;
      case 23 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        return;
      case 24 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        return;
      case 25 : // enteredAction
        removeAction (ControlInteractiveElement.ACTION_ENTERED,getProperty("enteredAction"));
        addAction(ControlInteractiveElement.ACTION_ENTERED,_value.getString());
        return;
      case 26 : // exitedAction
        removeAction (ControlInteractiveElement.ACTION_EXITED,getProperty("exitedAction"));
        addAction(ControlInteractiveElement.ACTION_EXITED,_value.getString());
        return;

      case 27 : myElement.setSensitivity(_value.getInteger()); break;
      
      case MEASURED : myElement.canBeMeasured(_value.getBoolean()); break;
      default: super.setValue(_index-IE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  POSITION : thePos = null; break;
      case  POSITION_X : myElement.setX(posValues[0].value=0.0);        break;
      case  POSITION_Y : myElement.setY(posValues[1].value=0.0);        break;
      case  POSITION_Z : myElement.setZ(posValues[2].value=0.0);        break;
      case  SIZE : theSize = null; break;
      case  SIZE_X : myElement.setSizeX((sizeValues[0].value=0.1)*scalex); break;
      case  SIZE_Y : myElement.setSizeY((sizeValues[1].value=0.1)*scaley); break;
      case  SIZE_Z : myElement.setSizeZ((sizeValues[2].value=0.1)*scalez); break;
      case  6 : myElement.setVisible(true); break;

        // Some elements may change this. For instance, arrows
      case  ENABLED           : myElement.setEnabled(InteractiveElement.TARGET_POSITION,true); break;
      case  ENABLED_SECONDARY : myElement.setEnabled(InteractiveElement.TARGET_SIZE,false); break;

      case  9 : scalex = 1.0; myElement.setSizeX(sizeValues[0].value); break;
      case 10 : scaley = 1.0; myElement.setSizeY(sizeValues[1].value); break;
      case 11 : scalez = 1.0; myElement.setSizeZ(sizeValues[2].value); break;

      case 12 : break; // Groups... how to implement this???
      case 13 : myElement.setGroupEnabled(true); break;

      case STYLE : break; // To be implemented by subclasses
      case 15 : myElement.getStyle().setPosition(Style.CENTERED); break;
      case 16 : myElement.getStyle().setAngle(0.0); break;
      case 17 : myElement.setResolution(null); break;

      case PRIMARY_COLOR     : myElement.getStyle().setEdgeColor(java.awt.Color.black); break;
      case SECONDARY_COLOR   : myElement.getStyle().setFillPattern(java.awt.Color.blue); break;
      case STROKE : myElement.getStyle().setEdgeStroke(new java.awt.BasicStroke()); break;
      case 21 : myElement.getStyle().setFont(font=defaultFont); break;

      case 22 : removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction")); return;
      case 23 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));         return;
      case 24 : removeAction (ControlElement.ACTION,getProperty("action"));                       return;
      case 25 : removeAction (ControlInteractiveElement.ACTION_ENTERED,getProperty("enteredAction")); return;
      case 26 : removeAction (ControlInteractiveElement.ACTION_EXITED,getProperty("exitedAction")); return;

      case 27 : myElement.setSensitivity(AbstractInteractiveElement.SENSIBILITY); break;
      case MEASURED : myElement.canBeMeasured(true); break;
      default: super.setDefaultValue(_index-IE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  POSITION : 
      case SIZE :
        return "<none>";
      case  POSITION_X :
      case  POSITION_Y :
      case  POSITION_Z : return "0";
      case  SIZE_X :
      case  SIZE_Y :
      case  SIZE_Z : return "0.1";
      case  6 : return "true";

        // Some elements may change this. For instance, arrows
      case  ENABLED           : return "true";
      case  ENABLED_SECONDARY : return "false";

      case  9 :
      case 10 :
      case 11 : return "1";

      case 12 : return "<none>";
      case 13 : return "true";

      case STYLE : return "<none>";
      case 15 : return "CENTERED";
      case 16 : return "0.0";
      case 17 : return "<none>";

      case PRIMARY_COLOR     : return "BLACK";
      case SECONDARY_COLOR   : return "BLUE";
      case STROKE : return "1.0";
      case 21 : return defaultFont.toString();

      case 22 :
      case 23 :
      case 24 :
      case 25 :
      case 26 : return "<no_action>";

      case 27 : return ""+AbstractInteractiveElement.SENSIBILITY;
      case MEASURED : return "true";
      default : return super.getDefaultValueString(_index-IE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case POSITION : return new ObjectValue(thePos);
      case POSITION_X : return posValues[0];
      case POSITION_Y : return posValues[1];
      case POSITION_Z : return posValues[2];
      case SIZE : return new ObjectValue(theSize);
      case SIZE_X : return sizeValues[0];
      case SIZE_Y : return sizeValues[1];
      case SIZE_Z : return sizeValues[2];
      default: return null; // Doesn't inherit
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
        break;
      case InteractionEvent.MOUSE_ENTERED :
        invokeActions (ControlInteractiveElement.ACTION_ENTERED);
        break;
      case InteractionEvent.MOUSE_PRESSED :
        if (myParent instanceof ControlDrawingPanel3D) ((ControlDrawingPanel3D) myParent).setSelectedDrawable (this);
        invokeActions (ControlInteractiveElement.ACTION_PRESS);
        // Do not break!
      case InteractionEvent.MOUSE_DRAGGED :
        if (_event.getTarget().getClass()==InteractionTargetElementSize.class) {
          if (scalex!=0.0) sizeValues[0].value = myElement.getSizeX()/scalex; else sizeValues[0].value = myElement.getSizeX();
          if (scaley!=0.0) sizeValues[1].value = myElement.getSizeY()/scaley; else sizeValues[1].value = myElement.getSizeY();
          if (scalez!=0.0) sizeValues[2].value = myElement.getSizeZ()/scalez; else sizeValues[2].value = myElement.getSizeZ();
// System.out.println("Size is now "+sizeValues[0].value+", "+sizeValues[1].value);
          if (theSize!=null) {
            theSize[0] = sizeValues[0].value;
            theSize[1] = sizeValues[1].value;
            if (theSize.length>2) theSize[2] = sizeValues[2].value;
            Value objVal = new ObjectValue(theSize);
            variableChanged (getFullSizeSpot(),objVal);
            if (isUnderEjs && enabledEjsEdit) setFieldListValue(getFullSizeSpot(),objVal);
          }
          else {
            variablesChanged (getSizeSpot(),sizeValues);
            if (isUnderEjs && enabledEjsEdit) setFieldListValues(getSizeSpot(),sizeValues);
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
            variableChanged (getFullPositionSpot(),objVal);
            if (isUnderEjs && enabledEjsEdit) setFieldListValue(getFullPositionSpot(),objVal);
          }
          else {
            variablesChanged (getPosSpot(),posValues);
            if (isUnderEjs && enabledEjsEdit) setFieldListValues(getPosSpot(),posValues);
          }
        }
        break;
      case InteractionEvent.MOUSE_RELEASED :
        if (myParent instanceof ControlDrawingPanel3D) ((ControlDrawingPanel3D) myParent).setSelectedDrawable (null);
        invokeActions (ControlElement.ACTION);
        break;
    }
  } // End of interaction method


} // End of interface
