/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing2d.*;
import org.opensourcephysics.drawing2d.interaction.*;
import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;

public abstract class ControlElement2D extends org.colos.ejs.library.control.swing.ControlDrawable 
                                       implements InteractionListener {
  static public final int E2D_ADDED = 27;

  static protected final int POSITION_X = 0;
  static protected final int POSITION_Y = 1;
  static protected final int POSITION   = 2;
  static protected final int SIZE_X     = 3;
  static protected final int SIZE_Y     = 4;
  static protected final int SIZE       = 5;
  static protected final int SCALE_X    = 6;
  static protected final int SCALE_Y    = 7;
  static protected final int TRANSFORMATION  = 8;

  static protected final int VISIBLE       = 9;
  static protected final int LINE_COLOR    = 10;
  static protected final int LINE_WIDTH    = 11;
  static protected final int FILL_COLOR    = 12;
  static protected final int DRAWING_FILL  = 13;
  static protected final int DRAWING_LINES = 14;

  static protected final int ENABLED_POSITION = 15;
  static protected final int MOVES_GROUP      = 16;
  static protected final int ENABLED_SIZE     = 17;
  static protected final int RESIZES_GROUP    = 18;
  static protected final int SENSITIVITY      = 19;

  static protected final int PRESS_ACTION     = 20;
  static protected final int DRAG_ACTION      = 21;
  static protected final int RELEASE_ACTION   = 22;
  static protected final int ENTERED_ACTION   = 23;
  static protected final int EXITED_ACTION    = 24;

  static protected final int MEASURED    = 25;
  static protected final int EXTRA_COLOR    = 26;

  static public final java.awt.Color NULL_COLOR = org.colos.ejs.library.control.ConstantParserUtil.NULL_COLOR;
  static public final double TO_RADIANS=Math.PI/180.0;

  protected DoubleValue[] posValues, sizeValues;
  protected double defaultX, defaultY, defaultSizeX, defaultSizeY;
  protected double scalex=1.0, scaley=1.0, lineWidth=1.0;
  protected Color defLines, defExtraColor;
  protected Paint defFill;
  protected double[] thePos, theSize;

  private Element myElement = null; // change with care
  private int[] posSpot, sizeSpot;
  private int fullPosition, fullSize;

  /**
   * Constructor and utilities
   */
  public ControlElement2D() {
    super ();
    myElement = (Element) getDrawable();
    posValues  = new DoubleValue[] { new DoubleValue(defaultX=myElement.getX()),
                                     new DoubleValue(defaultY=myElement.getY())};
    sizeValues = new DoubleValue[] { new DoubleValue(defaultSizeX=myElement.getSizeX()),
                                     new DoubleValue(defaultSizeY=myElement.getSizeY())};
    defLines = myElement.getStyle().getLineColor();
    defFill  = myElement.getStyle().getFillColor();
    defExtraColor = myElement.getStyle().getExtraColor();
    int disp = getPropertiesDisplacement();
    posSpot  = new int[] {POSITION_X+disp,POSITION_Y+disp};
    sizeSpot = new int[] {SIZE_X+disp,SIZE_Y+disp};
    fullPosition = POSITION+disp;
    fullSize     = SIZE    +disp;
    myElement.addInteractionListener(this);
    myElement.setDataObject(this);
  }

  /**
   * This tells how many properties have been added on top of 
   * those of the basic ControlElement2D class
   */
  protected abstract int getPropertiesDisplacement ();

  public Object getObject() { return myElement; }

  abstract public String getObjectClassname ();

  final public Element getElement () { return myElement; }

  final public int[] getPosSpot () { return posSpot; }
  final public int[] getSizeSpot () { return sizeSpot; }

  final public int getFullPositionSpot () { return fullPosition; }
  final public int getFullSizeSpot () { return fullSize; }

  protected void setName (String _name) { myElement.setName(_name); }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();

      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("position");
      infoList.add ("sizeX");
      infoList.add ("sizeY");
      infoList.add ("size");
      infoList.add ("scalex");
      infoList.add ("scaley");
      infoList.add ("transformation");

      infoList.add ("visible");
      infoList.add ("lineColor");
      infoList.add ("lineWidth");
      infoList.add ("fillColor");
      infoList.add ("drawingFill");
      infoList.add ("drawingLines");

      infoList.add ("enabledPosition");
      infoList.add ("movesGroup");
      infoList.add ("enabledSize");
      infoList.add ("resizesGroup");
      infoList.add ("sensitivity"); 

      infoList.add ("pressAction");
      infoList.add ("dragAction");
      infoList.add ("releaseAction");
      infoList.add ("enteredAction");
      infoList.add ("exitedAction");
      
      infoList.add ("measured");
      infoList.add ("extraColor");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("x"))       return "int|double";
    if (_property.equals("y"))       return "int|double";
    if (_property.equals("position")) return "double[]";
    if (_property.equals("sizeX"))   return "int|double";
    if (_property.equals("sizeY"))   return "int|double";
    if (_property.equals("size"))     return "double[]";
    if (_property.equals("scalex"))         return "int|double";
    if (_property.equals("scaley"))         return "int|double";
    if (_property.equals("transformation"))   return "AffineTransform|int|double|double[]|double[][]|Object";

    if (_property.equals("visible"))      return "boolean";
    if (_property.equals("lineColor"))    return "int|Color|Object";
    if (_property.equals("lineWidth"))    return "int|double";
    if (_property.equals("fillColor"))    return "int|Color|Object";
    if (_property.equals("drawingFill"))  return "boolean";
    if (_property.equals("drawingLines")) return "boolean";

    if (_property.equals("enabledPosition")) return "Interaction2D|int|boolean";
    if (_property.equals("movesGroup"))      return "boolean";
    if (_property.equals("enabledSize"))     return "Interaction2D|int|boolean";
    if (_property.equals("resizesGroup"))    return "boolean";
    if (_property.equals("sensitivity"))     return "int";

    if (_property.equals("pressAction"))     return "Action CONSTANT";
    if (_property.equals("dragAction"))      return "Action CONSTANT";
    if (_property.equals("releaseAction"))   return "Action CONSTANT";
    if (_property.equals("enteredAction"))   return "Action CONSTANT";
    if (_property.equals("exitedAction"))    return "Action CONSTANT";

    if (_property.equals("measured"))        return "boolean";
    if (_property.equals("extraColor"))    return "int|Color|Object";

    return super.getPropertyInfo(_property);
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("size")) return "sizeArray";
    return super.getPropertyCommonName(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case POSITION_X : thePos = null; myElement.setX(posValues[0].value=_value.getDouble()); break;
      case POSITION_Y : thePos = null; myElement.setY(posValues[1].value=_value.getDouble()); break;
      case POSITION : 
        if (_value.getObject() instanceof double[]) {
          thePos = (double[]) _value.getObject();
          myElement.setXY(posValues[0].value=thePos[0],posValues[1].value=thePos[1]);
        }
        break;

      case SIZE_X : theSize = null; myElement.setSizeX((sizeValues[0].value=_value.getDouble())*scalex); break;
      case SIZE_Y : theSize = null; myElement.setSizeY((sizeValues[1].value=_value.getDouble())*scaley); break;
      case SIZE :     
        if (_value.getObject() instanceof double[]) {
          theSize = (double[]) _value.getObject();
          myElement.setSizeXY((sizeValues[0].value=theSize[0])*scalex,(sizeValues[1].value=theSize[1])*scaley);
        }
        break;
      case SCALE_X : scalex = _value.getDouble(); myElement.setSizeX(sizeValues[0].value*scalex); break;
      case SCALE_Y : scaley = _value.getDouble(); myElement.setSizeY(sizeValues[1].value*scaley); break;

      case TRANSFORMATION :
        Object transform = _value.getObject();
        if (transform instanceof AffineTransform ||  transform instanceof org.opensourcephysics.numerics.Matrix2DTransformation ||
            transform instanceof double[] || transform instanceof double[][]) myElement.setTransformation (_value.getObject());
        else if (_value instanceof IntegerValue) myElement.setTransformation(AffineTransform.getRotateInstance(_value.getInteger()*TO_RADIANS));
        else if (_value instanceof DoubleValue || _value instanceof InterpretedValue) 
          myElement.setTransformation(AffineTransform.getRotateInstance(_value.getDouble()));
        else myElement.setTransformation(decodeAffineTransform(_value.getString()));
        break;

      case VISIBLE : myElement.setVisible(_value.getBoolean()); break;
      case LINE_COLOR :
        if (_value.getObject() instanceof Color) myElement.getStyle().setLineColor((Color) _value.getObject()); 
        else myElement.getStyle().setLineColor(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case LINE_WIDTH : myElement.getStyle().setLineWidth((float) _value.getDouble()); break;
      case FILL_COLOR : 
        if (_value.getObject() instanceof Paint) myElement.getStyle().setFillColor((Paint) _value.getObject());
        else myElement.getStyle().setFillColor(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case DRAWING_FILL : myElement.getStyle().setDrawingFill(_value.getBoolean()); break;
      case DRAWING_LINES : myElement.getStyle().setDrawingLines(_value.getBoolean()); break;

      case ENABLED_POSITION :
        if (_value instanceof BooleanValue) myElement.getInteractionTarget(Element.TARGET_POSITION).setEnabled(_value.getBoolean());  
        else myElement.getInteractionTarget(Element.TARGET_POSITION).setEnabled(_value.getInteger()); 
        break;
      case MOVES_GROUP      : myElement.getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(_value.getBoolean()); break;
      case ENABLED_SIZE     : 
        if (_value instanceof BooleanValue) myElement.getInteractionTarget(Element.TARGET_SIZE).setEnabled(_value.getBoolean());  
        else myElement.getInteractionTarget(Element.TARGET_SIZE).setEnabled(_value.getInteger()); 
        break;
      case RESIZES_GROUP    : myElement.getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(_value.getBoolean()); break;
      case SENSITIVITY      : myElement.getStyle().setSensitivity(_value.getInteger()); break;

      case PRESS_ACTION : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressAction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case DRAG_ACTION : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragAction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case RELEASE_ACTION : // releaseaction
        removeAction (ControlElement.ACTION,getProperty("releaseAction"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case ENTERED_ACTION : // mouse entered action
        removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction"));
        addAction(ControlSwingElement.MOUSE_ENTERED_ACTION,_value.getString());
        break;
      case EXITED_ACTION : // mouse exited action
        removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction"));
        addAction(ControlSwingElement.MOUSE_EXITED_ACTION,_value.getString());
        break;

      case MEASURED : myElement.setCanBeMeasured(_value.getBoolean()); break;
      case EXTRA_COLOR :
        if (_value.getObject() instanceof Color) myElement.getStyle().setExtraColor((Color) _value.getObject()); 
        else myElement.getStyle().setExtraColor(DisplayColors.getLineColor(_value.getInteger()));
        break;

      default : super.setValue(_index-E2D_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case POSITION_X : myElement.setX(posValues[0].value=defaultX);        break;
      case POSITION_Y : myElement.setY(posValues[1].value=defaultY);        break;
      case POSITION : thePos = null; break;

      case SIZE_X :     myElement.setSizeX((sizeValues[0].value=defaultSizeX)*scalex); break;
      case SIZE_Y :     myElement.setSizeY((sizeValues[1].value=defaultSizeY)*scaley); break;
      case SIZE : theSize = null; break;
      
      case SCALE_X : scalex = 1; myElement.setSizeX(sizeValues[0].value); break;
      case SCALE_Y : scaley = 1; myElement.setSizeY(sizeValues[1].value); break;

      case TRANSFORMATION : myElement.setTransformation(null); break;

      case VISIBLE : myElement.setVisible(true); break;
      case LINE_COLOR : myElement.getStyle().setLineColor(defLines); break;
      case LINE_WIDTH : myElement.getStyle().setLineWidth(1.0f); break;
      case FILL_COLOR : myElement.getStyle().setFillColor(defFill); break;

      case DRAWING_FILL : myElement.getStyle().setDrawingFill(true); break;
      case DRAWING_LINES : myElement.getStyle().setDrawingLines(true); break;

      case ENABLED_POSITION : myElement.getInteractionTarget(Element.TARGET_POSITION).setEnabled(false); break;
      case MOVES_GROUP      : myElement.getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(false); break;
      case ENABLED_SIZE     : myElement.getInteractionTarget(Element.TARGET_SIZE).setEnabled(false); break;
      case RESIZES_GROUP    : myElement.getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(false); break;
      case SENSITIVITY      : myElement.getStyle().setSensitivity(Style.DEFAULT_SENSITIVITY); break;

      case PRESS_ACTION : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressAction")); break;
      case DRAG_ACTION : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragAction")); break;
      case RELEASE_ACTION : removeAction (ControlElement.ACTION,getProperty("releaseAction")); break;
      case ENTERED_ACTION : removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction")); break;
      case EXITED_ACTION  : removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction")); break;

      case MEASURED : myElement.setCanBeMeasured(true); break;
      case EXTRA_COLOR : myElement.getStyle().setExtraColor(defExtraColor); break;

      default : super.setDefaultValue(_index-E2D_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case POSITION_X : return Double.toString(defaultX);
      case POSITION_Y : return Double.toString(defaultY);
      case POSITION : return "<none>"; 

      case SIZE_X : return Double.toString(defaultSizeX);
      case SIZE_Y : return Double.toString(defaultSizeY);
      case SIZE : return "<none>";

      case SCALE_X : return "1";
      case SCALE_Y : return "1";

      case TRANSFORMATION : return "<none>";

      case VISIBLE : return "true";
      case LINE_COLOR : return defLines.toString();
      case LINE_WIDTH : return "1";
      case FILL_COLOR : return defFill.toString();

      case DRAWING_FILL : 
      case DRAWING_LINES : return "true";

      case ENABLED_POSITION : return "ENABLED_NONE";
      case MOVES_GROUP      : return "false";
      case ENABLED_SIZE     : return "ENABLED_NONE";
      case RESIZES_GROUP    : return "false";
      case SENSITIVITY      : return Integer.toString(Style.DEFAULT_SENSITIVITY);

      case PRESS_ACTION :
      case DRAG_ACTION :
      case RELEASE_ACTION :
      case ENTERED_ACTION :
      case EXITED_ACTION  : return "<no_action>";

      case MEASURED : return "true";
      case EXTRA_COLOR : return defExtraColor.toString();

      default : return super.getDefaultValueString(_index-E2D_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case POSITION : return new ObjectValue (thePos); 
      case POSITION_X : return posValues[0];
      case POSITION_Y : return posValues[1];
      case SIZE : return new ObjectValue (theSize); 
      case SIZE_X : return sizeValues[0];
      case SIZE_Y : return sizeValues[1];
      default: return super.getValue(_index-E2D_ADDED);
    }
  }

  //-------------------------------------
  //Respond to interaction
  //-------------------------------------

  /*
  final ControlGroup2D getTopGroup() {
    ControlElement2D el = this;
    while (el.myParent instanceof ControlGroup2D) {
      el = (ControlGroup2D) el.myParent;
    }
    if (el instanceof ControlGroup2D) return (ControlGroup2D) el;
    return null;
  }
*/
  final ControlGroup2D getControlGroup() {
    if (myParent instanceof ControlGroup2D) return (ControlGroup2D) myParent;
    return null;
  }

  protected void propagatePosition (ControlElement2D origin) {
    posValues[0].value = myElement.getX();
    posValues[1].value = myElement.getY();
    if (thePos!=null) {
      thePos[0] = posValues[0].value;
      thePos[1] = posValues[1].value;
      Value objVal = new ObjectValue(thePos);
      variableChanged (getFullPositionSpot(),objVal);
      if (this!=origin) origin.variableChanged (origin.getFullPositionSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullPositionSpot(),objVal);
    }
    else {
      variablesChanged (getPosSpot(),posValues);
      if (this!=origin) origin.variablesChanged (origin.getPosSpot(),posValues);
      if (isUnderEjs) setFieldListValues(getPosSpot(),posValues);
    }
  }

  protected void propagateSize (ControlElement2D origin) {
    if (scalex!=0.0) sizeValues[0].value = myElement.getSizeX()/scalex; 
    else             sizeValues[0].value = myElement.getSizeX();
    if (scaley!=0.0) sizeValues[1].value = myElement.getSizeY()/scaley; 
    else             sizeValues[1].value = myElement.getSizeY();
    if (theSize!=null) {
      theSize[0] = sizeValues[0].value;
      theSize[1] = sizeValues[1].value;
      Value objVal = new ObjectValue(theSize);
      variableChanged (getFullSizeSpot(),objVal);
      if (this!=origin) origin.variableChanged (origin.getFullSizeSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullSizeSpot(),objVal);
    }
    else {
      variablesChanged (getSizeSpot(),sizeValues);
      if (this!=origin) origin.variablesChanged (origin.getSizeSpot(),sizeValues);
      if (isUnderEjs) setFieldListValues(getSizeSpot(),sizeValues);
    }
  }

  protected final void reportMouseMotion (Object _info) {
    InteractionTarget target = (InteractionTarget) _info;
    //   System.out.println ("Interaction with "+this);
    ControlGroup2D gr = getControlGroup(); // getTopGroup()
    if (target==myElement.getInteractionTarget(Element.TARGET_POSITION)) {
      if (target.getAffectsGroup() && gr!=null) gr.propagatePosition(this);
      else propagatePosition(this); 
    }
    else if (target==myElement.getInteractionTarget(Element.TARGET_SIZE)) {
      if (target.getAffectsGroup()&& gr!=null) gr.propagateSize(this);
      else propagateSize(this);
    }
  }

  public void interactionPerformed(InteractionEvent _event) {
    // System.out.println("Event ID "+_event.getID());
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_ENTERED  : invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION); break;
      case InteractionEvent.MOUSE_EXITED   : invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);  break;
      case InteractionEvent.MOUSE_PRESSED  :
        invokeActions (ControlSwingElement.ACTION_PRESS);
//        reportMouseMotion (_event.getInfo()); This calls On drag unneccessarily
        break;
      case InteractionEvent.MOUSE_DRAGGED  :
        reportMouseMotion (_event.getInfo()); 
        //invokeActions (ControlElement.VARIABLE_CHANGED);
        break;
      case InteractionEvent.MOUSE_RELEASED : invokeActions (ControlElement.ACTION); break;
    }
  } // End of interaction method

  //------------------------------------------------
  //Parsing constants
  //------------------------------------------------

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;

    if (_propertyType.indexOf("AffineTransform")>=0) {
      AffineTransform transf = decodeAffineTransform (_value);
      if (transf!=null) return new ObjectValue(transf);
    }
    else if (_propertyType.indexOf("ElementPosition")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("centered"))         return new IntegerValue (Style.CENTERED);
      if (_value.equals("north"))            return new IntegerValue (Style.NORTH);
      if (_value.equals("south"))            return new IntegerValue (Style.SOUTH);
      if (_value.equals("east"))             return new IntegerValue (Style.EAST);
      if (_value.equals("west"))             return new IntegerValue (Style.WEST);
      if (_value.equals("north_east"))       return new IntegerValue (Style.NORTH_EAST);
      if (_value.equals("north_west"))       return new IntegerValue (Style.NORTH_WEST);
      if (_value.equals("south_east"))       return new IntegerValue (Style.SOUTH_EAST);
      if (_value.equals("south_west"))       return new IntegerValue (Style.SOUTH_WEST);
    }
    else if (_propertyType.indexOf("ArrowPosition")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("centered"))         return new IntegerValue (Style.CENTERED);
      if (_value.equals("north_east"))       return new IntegerValue (Style.NORTH_EAST);
      if (_value.equals("south_west"))       return new IntegerValue (Style.SOUTH_WEST);
    }
    else if (_propertyType.indexOf("ArrowStyle")>=0 || _propertyType.indexOf("NewArrowStyle")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("arrow"))       return new IntegerValue (ElementArrow.ARROW);
      if (_value.equals("segment"))     return new IntegerValue (ElementArrow.SEGMENT);
      if (_value.equals("box"))         return new IntegerValue (ElementArrow.BOX);
      if (_value.equals("triangle"))    return new IntegerValue (ElementArrow.TRIANGLE);
      if (_value.equals("rhombus"))    return new IntegerValue (ElementArrow.RHOMBUS);
    }
    else if (_propertyType.indexOf("MarkerShape")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("ellipse"))         return new IntegerValue(ElementShape.ELLIPSE);
      if (_value.equals("rectangle"))       return new IntegerValue(ElementShape.RECTANGLE);
      if (_value.equals("round_rectangle")) return new IntegerValue(ElementShape.ROUND_RECTANGLE);
      if (_value.equals("wheel"))           return new IntegerValue(ElementShape.WHEEL);
      if (_value.equals("none"))            return new IntegerValue(ElementShape.NONE);
    }
    else if (_propertyType.indexOf("Interaction2D")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("enabled_none")) return new IntegerValue(InteractionTarget.ENABLED_NONE);
      if (_value.equals("enabled_any"))  return new IntegerValue(InteractionTarget.ENABLED_ANY);
      if (_value.equals("enabled_x"))    return new IntegerValue(InteractionTarget.ENABLED_X);
      if (_value.equals("enabled_y"))    return new IntegerValue(InteractionTarget.ENABLED_Y);
      if (_value.equals("enabled_no_move")) return new IntegerValue(InteractionTarget.ENABLED_NO_MOVE);
    }
    return super.parseConstant (_propertyType,_value);
  }

  static public AffineTransform createAffineTransform (String _value) {
    AffineTransform transformation = new AffineTransform();
    if (_value==null || _value.equals("null")) return transformation;
    if (_value.indexOf(':')<0 && _value.indexOf(',')<0) return transformation; // coincides with ControlEditorFor3DTransformation
    AffineTransform newTransf;
    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(_value,"&");
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      String keyword = token.toLowerCase();
      try {
        if (keyword.startsWith("ro:")) {
          String angleStr = token.substring(3).trim();
          boolean degrees = false;
          if (angleStr.endsWith("�") || angleStr.endsWith("d") ) { 
            angleStr = angleStr.substring(0,angleStr.length()-1).trim(); 
            degrees = true; 
          }
          double angle = Double.parseDouble(angleStr);
          if (degrees) angle = angle*TO_RADIANS;
          newTransf = AffineTransform.getRotateInstance(angle);
        }
        else if (keyword.startsWith("sc:")) {
          String angleStr = token.substring(3).trim();
          java.util.StringTokenizer t = new java.util.StringTokenizer(angleStr,",");
          double sx = Double.parseDouble(t.nextToken());
          double sy = Double.parseDouble(t.nextToken());
          newTransf = AffineTransform.getScaleInstance(sx, sy);
        }
        else if (keyword.startsWith("sh:")) {
          String angleStr = token.substring(3).trim();
          java.util.StringTokenizer t = new java.util.StringTokenizer(angleStr,",");
          double sx = Double.parseDouble(t.nextToken());
          double sy = Double.parseDouble(t.nextToken());
          newTransf = AffineTransform.getShearInstance(sx, sy);
        }
        else if (keyword.startsWith("tr:")) {
          String angleStr = token.substring(3).trim();
          java.util.StringTokenizer t = new java.util.StringTokenizer(angleStr,",");
          double sx = Double.parseDouble(t.nextToken());
          double sy = Double.parseDouble(t.nextToken());
          newTransf = AffineTransform.getTranslateInstance(sx, sy);
        }
        else { // A sequence of doubles m00, m10, m01, m11, m02, m12
          String angleStr = token;
          java.util.StringTokenizer t = new java.util.StringTokenizer(angleStr,",");
          double m00 = Double.parseDouble(t.nextToken());
          double m10 = Double.parseDouble(t.nextToken());
          double m01 = Double.parseDouble(t.nextToken());
          double m11 = Double.parseDouble(t.nextToken());
          double m02 = Double.parseDouble(t.nextToken());
          double m12 = Double.parseDouble(t.nextToken());
          newTransf = new AffineTransform (m00,m10,m01,m11,m02,m12);
        }
        newTransf.concatenate(transformation);
        transformation = newTransf;
      } catch (Exception exc) {
        System.out.println("Incorrect value for transformation: "+token);
        exc.printStackTrace();
      }
    }  // end of while
    return transformation;
}

  static public AffineTransform decodeAffineTransform (String _value) {
//    if (_value==null) return null;
    if (_value.indexOf('"')>=0) return null; // Seems to be an expression
    if (_value.indexOf('%')>=0) return null; // Seems to be a variable
    if (_value.indexOf('{')>=0 || _value.indexOf('}')>=0 ) return null; 
    if (_value.indexOf(':')<0 && _value.indexOf(',')<0) return null; 
    return createAffineTransform(_value.trim());
  }

}