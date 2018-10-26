/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import java.awt.Color;
import java.awt.Paint;

import org.opensourcephysics.numerics.*;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.drawing3d.utils.Control3DChild;
import org.colos.ejs.library.control.drawing3d.utils.ControlTransformation3D;
import org.colos.ejs.library.control.NeedsFinalUpdate;
import org.colos.ejs.library.control.NeedsUpdate;
import org.colos.ejs.library.control.swing.ControlSwingElement;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.interaction.*;
import org.opensourcephysics.drawing3d.utils.*;


/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public abstract class ControlElement3D extends ControlElement implements InteractionListener, Control3DChild {
//  protected static final int ELEMENT3D_PROPERTIES_ADDED = 37;

  public static final int EL3D_NAME    = 0; // The name of the element
  public static final int PARENT  = 1; // The parent of the element

  static protected final int POSITION_X = 2;
  static protected final int POSITION_Y = 3;
  static protected final int POSITION_Z = 4;
  static protected final int SIZE_X = 5;
  static protected final int SIZE_Y = 6;
  static protected final int SIZE_Z = 7;
  static protected final int TRANSFORMATION   = 8;

  static protected final int VISIBLE = 9;
  static protected final int LINE_COLOR = 10;
  static protected final int LINE_WIDTH = 11;
  static protected final int FILL_COLOR = 12;
  static protected final int RESOLUTION = 13;
  static protected final int DRAWING_FILL = 14;
  static protected final int DRAWING_LINES = 15;

  static protected final int ENABLED_POSITION = 16;
  static protected final int MOVES_GROUP      = 17;
  static protected final int ENABLED_SIZE     = 18;
  static protected final int RESIZES_GROUP    = 19;
  static protected final int SENSITIVITY      = 20;

  static protected final int PRESS_ACTION     = 21;
  static protected final int DRAG_ACTION      = 22;
  static protected final int RELEASE_ACTION   = 23;
  static protected final int ENTERED_ACTION   = 24;
  static protected final int EXITED_ACTION    = 25;

  static protected final int POSITION = 26;
  static protected final int SIZE     = 27;
  static protected final int MENU_NAME = 28;
  static protected final int OFFSET = 29;
  static protected final int DEPTH_FACTOR = 30;
  static protected final int MEASURED    = 31;

  static protected final int EXTRA_COLOR  = 32;
  static protected final int TEXTURE  = 33;
  static protected final int SECOND_TEXTURE  = 34;
  static protected final int TEXTURE_TRANSPARENCY  = 35;
  static protected final int TEXTURE_COMBINE  = 36;

  static protected final int COLOR_ORIGIN  = 37;
  static protected final int COLOR_DIRECTION  = 38;
  static protected final int COLOR_LEVELS  = 39;
  static protected final int COLOR_FILLS  = 40;
  static protected final int COLOR_BELOW  = 41;

  static public final java.awt.Color NULL_COLOR = org.colos.ejs.library.control.ConstantParserUtil.NULL_COLOR;
  static public final double TO_RADIANS=Math.PI/180.0;

  protected String menuNameEntry=null;
//  protected Style myStyle; The style may change (as in a MultiTrail)

  protected DoubleValue[] posValues, sizeValues;
  protected double defaultX, defaultY, defaultZ, defaultSizeX, defaultSizeY, defaultSizeZ;
  protected Color defLines, defExtraColor;
  protected Paint defFill;
  protected Resolution defaultRes;
  protected int defaultOffset;
  protected double[] thePos, theSize;

  protected ControlParentOfElement3D myParent;
  private Element myElement = null; // change with care
  private int[] posSpot, sizeSpot;
  private int fullPosition, fullSize;
  
  /**
   * Constructor and utilities
   */
  public ControlElement3D() {
    super ();
    myElement = createElement();
    Style myStyle = myElement.getStyle();
    posValues  = new DoubleValue[] { new DoubleValue(defaultX=myElement.getX()),
                                     new DoubleValue(defaultY=myElement.getY()),
                                     new DoubleValue(defaultZ=myElement.getZ())};
    sizeValues = new DoubleValue[] { new DoubleValue(defaultSizeX=myElement.getSizeX()),
                                     new DoubleValue(defaultSizeY=myElement.getSizeY()),
                                     new DoubleValue(defaultSizeZ=myElement.getSizeZ())};
    defLines = myStyle.getLineColor();
    defFill  = myStyle.getFillColor();
    defExtraColor = myStyle.getExtraColor();
    defaultRes = myStyle.getResolution();
    defaultOffset = myStyle.getRelativePosition();
    int disp = getPropertiesDisplacement();
    posSpot  = new int[] {POSITION_X+disp,POSITION_Y+disp,POSITION_Z+disp};
    sizeSpot = new int[] {SIZE_X+disp,SIZE_Y+disp,SIZE_Z+disp};
    fullPosition = POSITION+disp;
    fullSize     = SIZE    +disp;
    myElement.addInteractionListener(this);
    myElement.setDataObject(this);
  }

  /**
   * This tells how many properties have been added on top of 
   * those of the basic ControlElement3D class
   */
  protected abstract int getPropertiesDisplacement ();

  /**
   * This actually creates the 3D Element
   */
  protected abstract Element createElement ();

  @Override
  public Object getObject() { return myElement; }
  
  abstract public String getObjectClassname ();

  public Element getElement () { return myElement; }

  /**
   * Returns the top window that contains the element. Needed by AddMenuEntries
   * @return
   */
  protected java.awt.Window getTopWindow() {
    if (myParent==null) return null;
    org.colos.ejs.library.control.EjsControl ejsControl = getEjsControl();
    ControlElement parent = ejsControl.getElement(getProperty("parent"));
    if (parent==null) return null;
    while (!(parent instanceof ControlSwingElement)) parent = ejsControl.getElement(parent.getProperty("parent"));
    return ((ControlSwingElement) parent).getTopWindow();
  }
  
  final public int[] getPosSpot () { return posSpot; }
  final public int[] getSizeSpot () { return sizeSpot; }

  final public int getFullPositionSpot () { return fullPosition; }
  final public int getFullSizeSpot () { return fullSize; }

  protected void setName (String _name) { myElement.setName(_name); }
  
  final public void setParent (ControlParentOfElement3D _dp) {
     // System.out.println ("Setting parent of "+this+" to "+_dp);
    Element el = myElement; //(myElement instanceof WrapsElement) ? ((WrapsElement)myElement).getElementWrapped() : myElement;
    if (myParent!=null) {
      if (this instanceof NeedsPreUpdate) myParent.removeFromPreupdateList((NeedsPreUpdate)this);
      myParent.removeElement(el);
    }
    if (_dp!=null) {
      if (this instanceof NeedsPreUpdate) _dp.addToPreupdateList((NeedsPreUpdate)this);
      _dp.addElement(el);
      if (_dp instanceof NeedsUpdate) ((NeedsUpdate)_dp).update();
      if (_dp instanceof NeedsFinalUpdate) ((NeedsFinalUpdate)_dp).finalUpdate();
      myParent = _dp;
    }
  }
  final public ControlParentOfElement3D getParent () { return myParent; }

  public void destroy () {
    super.destroy();
    if (myParent instanceof NeedsUpdate) ((NeedsUpdate)myParent).update();
    if (myParent instanceof NeedsFinalUpdate) ((NeedsFinalUpdate)myParent).finalUpdate();
  }

  public String getMenuNameEntry() {
    if (menuNameEntry!=null) {
      if (menuNameEntry.equals("null")) return null;
      return menuNameEntry;
    }
    return this.getProperty("name");
  }
  
  public boolean acceptsChild (ControlElement _child) {
    if (_child instanceof ControlTransformation3D) return true;
    return super.acceptsChild(_child);
  }


// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("name");
      infoList.add ("parent");

      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("z");
      infoList.add ("sizeX");
      infoList.add ("sizeY");
      infoList.add ("sizeZ");
      infoList.add ("transformation");

      infoList.add ("visible");
      infoList.add ("lineColor");
      infoList.add ("lineWidth");
      infoList.add ("fillColor");
      infoList.add ("resolution");
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
      
      infoList.add ("position");
      infoList.add ("size");
      infoList.add ("menuName");
      infoList.add ("elementposition");
      infoList.add ("depthFactor");

      infoList.add ("measured");
      infoList.add ("extraColor");

      infoList.add ("texture");
      infoList.add ("textureSecond");
      infoList.add ("textureTransparency");
      infoList.add ("textureCombine");

      infoList.add ("colorOrigin");
      infoList.add ("colorDirection");
      infoList.add ("colorLevels");
      infoList.add ("colorFills");
      infoList.add ("belowWhenEqual");
      
      // Does not inherit. Must match those of ControlSet3D
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("name"))    return "String CONSTANT";
    if (_property.equals("parent"))  return "ControlElement CONSTANT";
    if (_property.equals("x"))       return "int|double";
    if (_property.equals("y"))       return "int|double";
    if (_property.equals("z"))       return "int|double";
    if (_property.equals("sizeX"))   return "int|double";
    if (_property.equals("sizeY"))   return "int|double";
    if (_property.equals("sizeZ"))   return "int|double";
    if (_property.equals("transformation"))   return "3DTransformation|double[]|String|Object";

    if (_property.equals("visible"))      return "boolean";
    if (_property.equals("lineColor"))    return "int|Color|Object";
    if (_property.equals("lineWidth"))    return "int|double";
    if (_property.equals("fillColor"))    return "int|Color|Object";
    if (_property.equals("resolution"))   return "3DResolution|String|Object|double";
    if (_property.equals("drawingFill"))  return "boolean";
    if (_property.equals("drawingLines")) return "boolean";

    if (_property.equals("enabledPosition")) return "Interaction3D|int|boolean";
    if (_property.equals("movesGroup"))      return "boolean";
    if (_property.equals("enabledSize"))     return "Interaction3D|int|boolean";
    if (_property.equals("resizesGroup"))    return "boolean";
    if (_property.equals("sensitivity"))     return "int";

    if (_property.equals("pressAction"))     return "Action CONSTANT";
    if (_property.equals("dragAction"))      return "Action CONSTANT";
    if (_property.equals("releaseAction"))   return "Action CONSTANT";
    if (_property.equals("enteredAction"))   return "Action CONSTANT";
    if (_property.equals("exitedAction"))    return "Action CONSTANT";

    if (_property.equals("position")) return "double[]";
    if (_property.equals("size"))     return "double[]";
    if (_property.equals("menuName")) return "String TRANSLATABLE";
    if (_property.equals("elementposition"))return "ElementPosition|int";
    if (_property.equals("depthFactor")) return "double";

    if (_property.equals("measured"))        return "boolean";
    if (_property.equals("extraColor"))    return "int|Color|Object";

    if (_property.equals("texture"))  return "File|String TRANSLATABLE";
    if (_property.equals("textureSecond"))  return "File|String TRANSLATABLE";
    if (_property.equals("textureTransparency")) return "double";
    if (_property.equals("textureCombine"))      return "boolean";

    if (_property.equals("colorOrigin"))        return "double[]";
    if (_property.equals("colorDirection"))     return "double[]";
    if (_property.equals("colorLevels"))        return "double[]";
    if (_property.equals("colorFills"))         return "Object[]|Color[]|int[]";
    if (_property.equals("belowWhenEqual"))     return "boolean";
    
    return null;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("size")) return "sizeArray";
    return super.getPropertyCommonName(_property);
  }

  public void updatePanel() {
    DrawingPanel3D panel = myElement.getPanel();
    if (panel!=null) panel.update();
  }
  
// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case EL3D_NAME :
          setName(_value.getString());
          super.setValue (ControlElement.NAME,_value); 
          break;
      case PARENT :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if (parent!=null) setParent (null);
          parent = myGroup.getElement(_value.toString());
          if (parent==null) System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> not found for "+toString());
          else {
            if (parent instanceof ControlParentOfElement3D) setParent ((ControlParentOfElement3D) parent);
            else System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlElements3DParent");
          }
        }
        break;
      case POSITION_X : thePos = null; myElement.setX(posValues[0].value=_value.getDouble()); break;
      case POSITION_Y : thePos = null; myElement.setY(posValues[1].value=_value.getDouble()); break;
      case POSITION_Z : thePos = null; myElement.setZ(posValues[2].value=_value.getDouble()); break;
      case POSITION : 
        if (_value.getObject() instanceof double[]) {
          thePos = (double[]) _value.getObject();
          myElement.setXYZ(posValues[0].value=thePos[0],posValues[1].value=thePos[1],posValues[2].value=thePos[2]);
        }
        break;

      case SIZE_X : theSize = null; myElement.setSizeX(sizeValues[0].value=_value.getDouble()); break;
      case SIZE_Y : theSize = null; myElement.setSizeY(sizeValues[1].value=_value.getDouble()); break;
      case SIZE_Z : theSize = null; myElement.setSizeZ(sizeValues[2].value=_value.getDouble()); break;
      case SIZE :     
        if (_value.getObject() instanceof double[]) {
          theSize = (double[]) _value.getObject();
          myElement.setSizeXYZ(sizeValues[0].value=theSize[0],sizeValues[1].value=theSize[1],sizeValues[2].value=theSize[2]);
        }
        break;

      case TRANSFORMATION :
          if (_value==null) {
            System.err.println ("Trans is null");
            return;
          }
          if (_value.getObject() instanceof Transformation) myElement.setTransformation((Transformation)_value.getObject());
          else if (_value.getObject() instanceof double[]) {
            double[] array = (double[]) _value.getObject();
            if (array.length==6) {
              double[] v1 = new double[] { array[0], array[1], array[2] };
              double[] v2 = new double[] { array[3], array[4], array[5] };
              myElement.setTransformation(Matrix3DTransformation.createAlignmentTransformation(v1,v2));
            }
            else if (array.length==4) {
              double[] v = new double[] { array[1], array[2], array[3] };
              myElement.setTransformation(Matrix3DTransformation.rotation(array[0],v));
            }
          }
          else myElement.setTransformation(createTransformation(this,_value.getString()));
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
      case RESOLUTION :
          if (_value.getObject() instanceof Resolution) myElement.getStyle().setResolution((Resolution)_value.getObject());
          else {
            Resolution res = decodeResolution (_value.toString());
            if (res!=null) myElement.getStyle().setResolution(res);
          }
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

      case MENU_NAME : setName(menuNameEntry = _value.getString()); break;
      case OFFSET : if (_value.getInteger()!=myElement.getStyle().getRelativePosition()) myElement.getStyle().setRelativePosition(_value.getInteger()); break; 

      case DEPTH_FACTOR : if (_value.getDouble()!=myElement.getStyle().getDepthFactor()) myElement.getStyle().setDepthFactor(_value.getDouble()); break;
      case MEASURED : myElement.setCanBeMeasured(_value.getBoolean()); break;
      case EXTRA_COLOR :
        if (_value.getObject() instanceof Color) myElement.getStyle().setExtraColor((Color) _value.getObject()); 
        else myElement.getStyle().setExtraColor(DisplayColors.getLineColor(_value.getInteger()));
        break;

      case TEXTURE : myElement.getStyle().setTexture(_value.getString(),myElement.getStyle().getTextures()[1],myElement.getStyle().getTransparency(),myElement.getStyle().getCombine()); break; 
      case SECOND_TEXTURE : myElement.getStyle().setTexture(myElement.getStyle().getTextures()[0],_value.getString(),myElement.getStyle().getTransparency(),myElement.getStyle().getCombine()); break; 
      case TEXTURE_TRANSPARENCY : myElement.getStyle().setTexture(myElement.getStyle().getTextures()[0],myElement.getStyle().getTextures()[1],_value.getDouble(),myElement.getStyle().getCombine()); break; 
      case TEXTURE_COMBINE : myElement.getStyle().setTexture(myElement.getStyle().getTextures()[0],myElement.getStyle().getTextures()[1],myElement.getStyle().getTransparency(),_value.getBoolean()); break; 

      case  COLOR_ORIGIN :    myElement.setColorOrigin((double[]) _value.getObject()); break;
      case  COLOR_DIRECTION : myElement.setColorDirection((double[]) _value.getObject()); break;
      case  COLOR_LEVELS :    myElement.setColorRegions((double[])_value.getObject()); break;
      case  COLOR_FILLS : 
        if (_value.getObject() instanceof Color[]) myElement.setColorPalette((Color[])_value.getObject());
        else if (_value.getObject() instanceof int[]) myElement.setColorPalette((int[])_value.getObject());
        break;
      case  COLOR_BELOW : myElement.setColorBelowWhenEqual(_value.getBoolean()); break;


      default : break; // Do nothing. No inherited properties
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case EL3D_NAME : 
        super.setDefaultValue (ControlElement.NAME); 
        setName (""); 
        break;
      case PARENT : if (myGroup.getElement(getProperty("parent"))!=null) setParent (null); break;
      
      case POSITION_X : myElement.setX(posValues[0].value=defaultX);        break;
      case POSITION_Y : myElement.setY(posValues[1].value=defaultY);        break;
      case POSITION_Z : myElement.setZ(posValues[2].value=defaultZ);        break;
      case POSITION : thePos = null; break;

      case SIZE_X :     myElement.setSizeX(sizeValues[0].value=defaultSizeX); break;
      case SIZE_Y :     myElement.setSizeY(sizeValues[1].value=defaultSizeY); break;
      case SIZE_Z :     myElement.setSizeZ(sizeValues[2].value=defaultSizeZ); break;
      case SIZE : theSize = null; break;

      case TRANSFORMATION : myElement.setTransformation(new Matrix3DTransformation(null)); break;

      case VISIBLE : myElement.setVisible(true); break;
      case LINE_COLOR : myElement.getStyle().setLineColor(defLines); break;
      case LINE_WIDTH : myElement.getStyle().setLineWidth(1.0f); break;
      case FILL_COLOR : myElement.getStyle().setFillColor(defFill); break;
      case RESOLUTION : myElement.getStyle().setResolution(defaultRes); break;
      
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

      case MENU_NAME : menuNameEntry = null; break;
      case OFFSET : myElement.getStyle().setRelativePosition(Style.NORTH_EAST); break; 
      case DEPTH_FACTOR : myElement.getStyle().setDepthFactor(1.0); break;

      case MEASURED : myElement.setCanBeMeasured(true); break;
      case EXTRA_COLOR : myElement.getStyle().setExtraColor(defExtraColor); break;
      
      case TEXTURE : myElement.getStyle().setTexture(null,myElement.getStyle().getTextures()[1],myElement.getStyle().getTransparency(),myElement.getStyle().getCombine()); break; 
      case SECOND_TEXTURE : myElement.getStyle().setTexture(myElement.getStyle().getTextures()[0],null,myElement.getStyle().getTransparency(),myElement.getStyle().getCombine()); break; 
      case TEXTURE_TRANSPARENCY : myElement.getStyle().setTexture(myElement.getStyle().getTextures()[0],myElement.getStyle().getTextures()[1],Double.NaN,myElement.getStyle().getCombine()); break; 
      case TEXTURE_COMBINE : myElement.getStyle().setTexture(myElement.getStyle().getTextures()[0],myElement.getStyle().getTextures()[1],myElement.getStyle().getTransparency(),false); break; 


      case  COLOR_ORIGIN : myElement.setColorOrigin(new double[] {0.0,0.0,0.0}); break;
      case  COLOR_DIRECTION : myElement.setColorDirection(new double[] {1.0,0.0,0.0}); break;
      case  COLOR_LEVELS : myElement.setColorRegions(null); break;
      case  COLOR_FILLS : myElement.setColorRegions(null); break;
      case  COLOR_BELOW : myElement.setColorBelowWhenEqual(true); break;

      default : break; // Do nothing. No inherited properties
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case EL3D_NAME : 
      case PARENT : 
      case POSITION : 
      case SIZE : return "<none>";
      
      case POSITION_X : return ""+defaultX;
      case POSITION_Y : return ""+defaultY;
      case POSITION_Z : return ""+defaultZ;
      case SIZE_X : return ""+defaultSizeX;
      case SIZE_Y : return ""+defaultSizeY;
      case SIZE_Z : return ""+defaultSizeZ;

      case TRANSFORMATION : return "<none>";

      case VISIBLE : return "true";
      case LINE_COLOR : return "<none>";
      case LINE_WIDTH : return "1";
      case FILL_COLOR : return "<none>";
      case RESOLUTION : return "<none>";
      
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

      case MENU_NAME : return "<none>";
      case OFFSET : return ""+defaultOffset; 
      case DEPTH_FACTOR : return "1.0";
      case MEASURED : return "true";
      case EXTRA_COLOR : return defExtraColor.toString();

      case TEXTURE : 
      case SECOND_TEXTURE : 
      case TEXTURE_TRANSPARENCY : return "<none>";
      case TEXTURE_COMBINE : return "false";

      case  COLOR_ORIGIN : return "{0.0,0.0,0.0}";
      case  COLOR_DIRECTION : return "{1.0,0.0,0.0}";
      case  COLOR_LEVELS :
      case  COLOR_FILLS : return "<none>";
      case  COLOR_BELOW : return "true";
      
      default : return "<none>";
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case POSITION : return new ObjectValue (thePos); 
      case POSITION_X : return posValues[0];
      case POSITION_Y : return posValues[1];
      case POSITION_Z : return posValues[2];
      case SIZE : return new ObjectValue (theSize); 
      case SIZE_X : return sizeValues[0];
      case SIZE_Y : return sizeValues[1];
      case SIZE_Z : return sizeValues[2];
      default: return null; // Doesn't inherit
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  final ControlGroup3D getControlGroup() {
    if (myParent instanceof ControlGroup3D) return (ControlGroup3D) myParent;
    return null;
  }

  protected void propagatePosition (ControlElement3D origin) {
    posValues[0].value = myElement.getX();
    posValues[1].value = myElement.getY();
    posValues[2].value = myElement.getZ();
    if (thePos!=null) {
      thePos[0] = posValues[0].value;
      thePos[1] = posValues[1].value;
      thePos[2] = posValues[2].value;
      Value objVal = new ObjectValue(thePos);
      variableChanged (getFullPositionSpot(),objVal);
      if (origin!=this) origin.variableChanged (origin.getFullPositionSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullPositionSpot(),objVal);
    }
    else {
      variablesChanged (getPosSpot(),posValues);
      if (origin!=this) origin.variablesChanged (origin.getPosSpot(),posValues);
      if (isUnderEjs) setFieldListValues(getPosSpot(),posValues);
    }
  }

  protected void propagateSize (ControlElement3D origin) {
    sizeValues[0].value = myElement.getSizeX();
    sizeValues[1].value = myElement.getSizeY();
    sizeValues[2].value = myElement.getSizeZ();
    if (theSize!=null) {
      theSize[0] = sizeValues[0].value;
      theSize[1] = sizeValues[1].value;
      theSize[2] = sizeValues[2].value;
      Value objVal = new ObjectValue(theSize);
      variableChanged (getFullSizeSpot(),objVal);
      if (origin!=this) origin.variableChanged (origin.getFullSizeSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullSizeSpot(),objVal);
    }
    else {
      variablesChanged (getSizeSpot(),sizeValues);
      if (origin!=this) origin.variablesChanged (origin.getSizeSpot(),sizeValues);
      if (isUnderEjs) setFieldListValues(getSizeSpot(),sizeValues);
    }
  }

  private void reportMouseMotion (Object _info) {
    InteractionTarget target = (InteractionTarget) _info;
//    System.out.println ("Interaction with "+this);
    ControlGroup3D gr = getControlGroup();
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
        reportMouseMotion (_event.getInfo());
        invokeActions (ControlSwingElement.ACTION_PRESS);
        break;
      case InteractionEvent.MOUSE_DRAGGED  :
        reportMouseMotion (_event.getInfo());
        //invokeActions (ControlSwingElement.MOUSE_DRAGGED_ACTION);
        break;
      case InteractionEvent.MOUSE_RELEASED : invokeActions (ControlSwingElement.ACTION); break;
    }
  } // End of interaction method

// ------------------------------------------------
// Parsing constants
// ------------------------------------------------

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;

    if (_propertyType.indexOf("3DResolution")>=0) {
      Resolution res = decodeResolution (_value);
      if (res!=null) return new ObjectValue(res);
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
    else if (_propertyType.indexOf("3DTransformation")>=0) {
      Transformation transf = decodeTransformation (this,_value);
      if (transf!=null) return new ObjectValue(transf);
    }
    else if (_propertyType.indexOf("ArrowPosition")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("centered"))         return new IntegerValue (Style.CENTERED);
      if (_value.equals("north_east"))       return new IntegerValue (Style.NORTH_EAST);
      if (_value.equals("south_west"))       return new IntegerValue (Style.SOUTH_WEST);
    }
    else if (_propertyType.indexOf("MarkerShape")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("ellipse"))         return new IntegerValue(ElementShape.ELLIPSE);
      if (_value.equals("rectangle"))       return new IntegerValue(ElementShape.RECTANGLE);
      if (_value.equals("round_rectangle")) return new IntegerValue(ElementShape.ROUND_RECTANGLE);
      if (_value.equals("wheel"))           return new IntegerValue(ElementShape.WHEEL);
      if (_value.equals("none"))            return new IntegerValue(ElementShape.NONE);
    }
    else if (_propertyType.indexOf("Interaction3D")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("enabled_none")) return new IntegerValue(InteractionTarget.ENABLED_NONE);
      if (_value.equals("enabled_any"))  return new IntegerValue(InteractionTarget.ENABLED_ANY);
      if (_value.equals("enabled_x"))    return new IntegerValue(InteractionTarget.ENABLED_X);
      if (_value.equals("enabled_y"))    return new IntegerValue(InteractionTarget.ENABLED_Y);
      if (_value.equals("enabled_z"))    return new IntegerValue(InteractionTarget.ENABLED_Z);
      if (_value.equals("enabled_xy"))    return new IntegerValue(InteractionTarget.ENABLED_XY);
      if (_value.equals("enabled_xz"))    return new IntegerValue(InteractionTarget.ENABLED_XZ);
      if (_value.equals("enabled_yz"))    return new IntegerValue(InteractionTarget.ENABLED_YZ);
      if (_value.equals("enabled_no_move")) return new IntegerValue(InteractionTarget.ENABLED_NO_MOVE);
    }
    return super.parseConstant (_propertyType,_value);
  }

  static public Resolution decodeResolution (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.indexOf('.')>=0) { // A double value
      try { return new Resolution (Double.parseDouble(_value)); }
      catch (Exception exc) {
        System.out.println("Incorrect double value for resolution");
        exc.printStackTrace();
        return null;
      }
    }
    else if (_value.indexOf(',')<0) { // A single integer
      try { return new Resolution (Integer.parseInt(_value),1,1); }
      catch (Exception e) { } // Do not complain, could be a variable
      return null;
    }
    else { // A sequence of integers n1,n2,n3
      try {
        java.util.StringTokenizer t = new java.util.StringTokenizer(_value,"\",");
        int n1 = Integer.parseInt(t.nextToken());
        if (!t.hasMoreTokens()) return new Resolution (n1,1,1);
        int n2 = Integer.parseInt(t.nextToken());
        if (!t.hasMoreTokens()) return new Resolution (n1,n2,1);
        return new Resolution (n1,n2,Integer.parseInt(t.nextToken()));
      } catch (Exception exc) {
        System.out.println("Incorrect integer values for resolution");
        exc.printStackTrace();
        return null;
      }
    }
  }

  static private Transformation createTransformation (ControlElement _element, String _value) {
//    System.err.println ("Create transformation from "+_value);
      Matrix3DTransformation transformation = new Matrix3DTransformation(null); // Identity matrix
      if (_value==null || _value.equals("null")) return transformation;
      if (_value.indexOf(':')<0 && _value.indexOf(',')<0) return transformation; // coincides with ControlEditorFor3DTransformation
      // Paco introduced next check because setting the property to, say, "z:"+variable produces an initial call
      // with "z: void", just before the variables are created and initialized. 090817
      if (_element.editorIsReading() && _value.indexOf("void")>=0) return transformation;
      Matrix3DTransformation newTransf;
      java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(_value,"&");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken().trim();
        String keyword = token.toLowerCase();
        try {
          if (keyword.startsWith("x:") || keyword.startsWith("y:") || keyword.startsWith("z:") ) {
            String angleStr = token.substring(2).trim();
            boolean degrees = false;
            if (angleStr.endsWith("�") || angleStr.endsWith("d") ) { 
              angleStr = angleStr.substring(0,angleStr.length()-1).trim(); 
              degrees = true; 
            }
            double angle = Double.parseDouble(angleStr);
            if (degrees) angle = angle*TO_RADIANS;
            if      (keyword.startsWith("x")) newTransf = Matrix3DTransformation.rotationX(angle);
            else if (keyword.startsWith("y")) newTransf = Matrix3DTransformation.rotationY(angle);
            else                              newTransf = Matrix3DTransformation.rotationZ(angle);
          }
          else if (keyword.startsWith("q:")) { // quaternion
            String angleStr = token.substring(2).trim();
            java.util.StringTokenizer t = new java.util.StringTokenizer(angleStr,",");
            double q1 = Double.parseDouble(t.nextToken());
            double q2 = Double.parseDouble(t.nextToken());
            double q3 = Double.parseDouble(t.nextToken());
            double q4 = Double.parseDouble(t.nextToken());
            newTransf = Matrix3DTransformation.Quaternion (q1,q2,q3,q4);
          }
          else { // A sequence of doubles n1,n2,n3 m1,m2,m3  or n1,n2,n3, angle
            String angleStr = token;
            java.util.StringTokenizer t = new java.util.StringTokenizer(angleStr,",");
            String firstToken = t.nextToken();
            double x2 = Double.parseDouble(t.nextToken());
            double x3 = Double.parseDouble(t.nextToken());
            double x4 = Double.parseDouble(t.nextToken());
            if (!t.hasMoreTokens()) {
              boolean degrees = false;
              if (firstToken.endsWith("�") || firstToken.endsWith("d")) { 
                firstToken = firstToken.substring(0,firstToken.length()-1); 
                degrees = true;
              }
              double angle = Double.parseDouble(firstToken);
              if (degrees) angle = angle*TO_RADIANS;
              newTransf = Matrix3DTransformation.rotation (angle,new double[]{x2,x3,x4});
            }
            else {
              double x1 = Double.parseDouble(firstToken);
              double x5 = Double.parseDouble(t.nextToken());
              double x6 = Double.parseDouble(t.nextToken());
              newTransf = Matrix3DTransformation.createAlignmentTransformation (new double[]{x1,x2,x3},new double[]{x4,x5,x6});
            }
          }
          newTransf.multiply(transformation);
          transformation = newTransf;
        } catch (Exception exc) {
          System.out.println("Incorrect value for transformation: "+token);
          exc.printStackTrace();
        }
      }  // end of while
      return transformation;
  }

  static private Transformation decodeTransformation (ControlElement _element, String _value) {
    if (_value.indexOf('"')>=0) return null; // Seems to be an expression
    if (_value.indexOf('%')>=0) return null; // Seems to be a variable
    if (_value.indexOf('{')>=0 || _value.indexOf('}')>=0 ) return null; // coincides with ControlEditorFor3DTransformation
    if (_value.indexOf(':')<0 && _value.indexOf(',')<0) return null; // coincides with ControlEditorFor3DTransformation
    return createTransformation(_element,_value.trim());
  }

} // End of interface
