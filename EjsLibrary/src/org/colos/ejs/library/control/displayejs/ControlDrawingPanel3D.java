/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.dialogs.AutoScaleInspector;
import java.text.*;

/**
 * A configurable 3D panel from the displayejs package
 */
public class ControlDrawingPanel3D extends ControlDrawablesParent implements InteractionListener {
  static private final int DP3D_ADDED = 36;
  static final public double TO_RAD = Math.PI/180.0;
  static protected final int [] posIndexes = {9,10,11};
  static protected final int [] angleIndexes = {15,16};
  static protected final int ZOOM_INDEX=17;
  static protected final int [] panIndexes = {18,19};
  static private final int KEY_INDEX = 31;

  protected DrawingPanel3D drawingPanel3D;
  protected boolean reportDrag=false, reportViewPoint=false, reportKey=false;
  protected double minZ, maxZ;
  protected DoubleValue zoomValue;
  protected DoubleValue angleValues[];
  protected IntegerValue panValues[];
  protected DoubleValue[] posValues ={ new DoubleValue(0.0), new DoubleValue(0.0), new DoubleValue(0.0)};
  private IntegerValue keyPressedValue = new IntegerValue(-1);
  protected boolean autoZ;
  protected boolean zminSet=false, zmaxSet=false;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    drawingPanel3D = new DrawingPanel3D ();
    // drawingPanel3D.removeOptionController();
    minX = Double.NaN; // drawingPanel.getXMin();
    maxX = Double.NaN; // drawingPanel.getXMax();
    minY = Double.NaN; // drawingPanel.getYMin();
    maxY = Double.NaN; // drawingPanel.getYMax();
    minZ = Double.NaN; // drawingPanel.getYMin();
    maxZ = Double.NaN; // drawingPanel.getYMax();
    autoX = drawingPanel3D.isAutoscaleX();
    autoY = drawingPanel3D.isAutoscaleY();
    autoZ = drawingPanel3D.isAutoscaleZ();
/*
    minX = drawingPanel3D.getXMin();
    maxX = drawingPanel3D.getXMax();
    minY = drawingPanel3D.getYMin();
    maxY = drawingPanel3D.getYMax();
    minZ = drawingPanel3D.getZMin();
    maxZ = drawingPanel3D.getZMax();
    */
    angleValues = new DoubleValue[2];
    angleValues[0] = new DoubleValue(drawingPanel3D.getAlpha());
    angleValues[1] = new DoubleValue(drawingPanel3D.getBeta());
    zoomValue = new DoubleValue(drawingPanel3D.getZoom());
    panValues = new IntegerValue[2];
    panValues[0]  = new IntegerValue(drawingPanel3D.getPan().x);
    panValues[1]  = new IntegerValue(drawingPanel3D.getPan().y);
    drawingPanel3D.setAllowQuickRedraw(true);
    drawingPanel3D.addListener (this);
    drawingPanel3D.setFocusable(true);
    drawingPanel3D.render();

    drawingPanel3D.addKeyListener (
      new java.awt.event.KeyAdapter() {
        public void keyPressed  (java.awt.event.KeyEvent _e) {
          keyPressedValue.value = _e.getKeyCode();
          if (reportKey) {
            variableChanged (KEY_INDEX,keyPressedValue);
            invokeActions (ControlSwingElement.KEY_ACTION);
          }
        }
        public void keyReleased (java.awt.event.KeyEvent _e) {
//          keyPressedValue.value = -1;
          if (reportKey) variableChanged (KEY_INDEX,keyPressedValue);
        }
      }
    );
    return drawingPanel3D;
  }

  @Override
  public void addMenuEntries() {} // Override its parent and do nothing: 3D panels can't zoom
    
  protected void updateAutoscale () {
    if (!isZoomed) {
      DrawingPanel dPanel = ((DrawingPanel) getVisual());
      dPanel.setAutoscaleX(autoX);
      dPanel.setAutoscaleY(autoY);
      drawingPanel3D.setAutoscaleZ(autoZ);
      updateExtrema(); 
    }
  }

  protected void updateExtrema () {
    if (!isZoomed) {
      DrawingPanel dPanel = ((DrawingPanel) getVisual());
      if (dPanel.isAutoscaleX()) {
        if (xminSet || xmaxSet) dPanel.limitAutoscaleX(minX, maxX);
      }
      else dPanel.setPreferredMinMaxX(minX,maxX);
      if (dPanel.isAutoscaleY()) {
        if (yminSet || ymaxSet) dPanel.limitAutoscaleY(minY, maxY);
      }
      else dPanel.setPreferredMinMaxY(minY,maxY);
      if (drawingPanel3D.isAutoscaleZ()) {
        if (zminSet || zmaxSet) drawingPanel3D.limitAutoscaleZ(minZ, maxZ);
      }
      else drawingPanel3D.setPreferredMinMaxZ(minZ,maxZ);
    }
  }

  protected void checkAutoscaling () {
    DrawingPanel dPanel = ((DrawingPanel) getVisual());
    if (dPanel.isAutoscaleX() || dPanel.isAutoscaleY() || drawingPanel3D.isAutoscaleZ()) {
      isZoomed = true;
      AutoScaleInspector plotInspector = new AutoScaleInspector(dPanel);
      plotInspector.setLocationRelativeTo(dPanel);
      plotInspector.updateDisplay();
      plotInspector.setVisible(true);
    }
  }

  // --------------------------
  // End of zooming and scaling (See ZoomControler class below)
  // --------------------------

  /*
  public void update () { // Ensure it will be updated
    // First prepare children that need to do something
    for (Enumeration e=preupdateList.elements(); e.hasMoreElements(); ) ((NeedsPreUpdate) e.nextElement()).preupdate();
    if (myGroup!=null && myGroup.isCollectingData()) return;
    // Now render
    drawingPanel3D.render();
  }
*/
  
// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("autoscaleX");
      infoList.add ("autoscaleY");
      infoList.add ("autoscaleZ");
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");
      infoList.add ("minimumZ");
      infoList.add ("maximumZ");
      infoList.add ("x"); // 9
      infoList.add ("y");
      infoList.add ("z");
      infoList.add ("pressaction");
      infoList.add ("dragaction");
      infoList.add ("action");

      infoList.add ("alpha"); // 15
      infoList.add ("beta");
      infoList.add ("zoom");
      infoList.add ("panx");
      infoList.add ("pany");

      infoList.add ("displayMode");
      infoList.add ("decoration");
      infoList.add ("square");
      infoList.add ("cursorMode");
      infoList.add ("showCoordinates");
      infoList.add ("hideLines");
      infoList.add ("quickRedraw");

      infoList.add ("xFormat");
      infoList.add ("yFormat");
      infoList.add ("zFormat");

      infoList.add ("keyAction");
      infoList.add ("keyPressed");
      infoList.add ("enteredAction");
      infoList.add ("exitedAction");

      infoList.add ("colorDepth");
      infoList.add ("axesLabels");

      infoList.addAll(super.getPropertyList()); // Inherit from ControlSwingElement
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("angle")) return "rotationAngle";
    if (_property.equals("displayMode")) return "projectionMode";
    if (_property.equals("hideLines"))  return "removeHiddenLines";
    if (_property.equals("quickRedraw"))      return "allowQuickRedraw";
    if (_property.equals("colorDepth"))      return "useColorDepth";
    if (_property.equals("decoration"))      return "decorationType";
    if (_property.equals("cursorMode"))      return "cursorType";
    if (_property.equals("quickRedraw"))      return "allowQuickRedraw";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("autoscaleX"))     return "boolean";
    if (_property.equals("autoscaleY"))     return "boolean";
    if (_property.equals("autoscaleZ"))     return "boolean";
    if (_property.equals("minimumX"))       return "int|double";
    if (_property.equals("maximumX"))       return "int|double";
    if (_property.equals("minimumY"))       return "int|double";
    if (_property.equals("maximumY"))       return "int|double";
    if (_property.equals("minimumZ"))       return "int|double";
    if (_property.equals("maximumZ"))       return "int|double";
    if (_property.equals("x"))              return "int|double";
    if (_property.equals("y"))              return "int|double";
    if (_property.equals("z"))              return "int|double";
    if (_property.equals("action"))         return "Action CONSTANT";
    if (_property.equals("pressaction"))    return "Action CONSTANT";
    if (_property.equals("dragaction"))     return "Action CONSTANT";

    if (_property.equals("alpha"))          return "int|double";
    if (_property.equals("beta"))           return "int|double";
    if (_property.equals("zoom"))           return "int|double";
    if (_property.equals("panx"))           return "int|double";
    if (_property.equals("pany"))           return "int|double";

    if (_property.equals("displayMode"))    return "int|DisplayMode";
    if (_property.equals("decoration"))     return "int|Decoration";
    if (_property.equals("square"))         return "boolean";
    if (_property.equals("cursorMode"))     return "int|CursorMode";
    if (_property.equals("showCoordinates"))return "boolean";
    if (_property.equals("hideLines"))      return "boolean";
    if (_property.equals("quickRedraw"))    return "boolean";

    if (_property.equals("xFormat"))        return "Format|Object|String TRANSLATABLE";
    if (_property.equals("yFormat"))        return "Format|Object|String TRANSLATABLE";
    if (_property.equals("zFormat"))        return "Format|Object|String TRANSLATABLE";

    if (_property.equals("keyAction"))      return "Action CONSTANT";
    if (_property.equals("keyPressed"))     return "int";
    if (_property.equals("enteredAction"))  return "Action CONSTANT";
    if (_property.equals("exitedAction"))   return "Action CONSTANT";

    if (_property.equals("colorDepth"))     return "boolean";
    if (_property.equals("axesLabels"))     return "String TRANSLATABLE";

    if (_property.equals("name"))       return "String CONSTANT";

    return super.getPropertyInfo(_property);
  }

  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("x") || _property.equals("y") || _property.equals("z") || _property.equals("dragaction")) {
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportDrag) reportDrag = (constant == null);
      }
    }
    else if (_property.equals("alpha") || _property.equals("beta") || _property.equals("zoom")) {
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportViewPoint) reportViewPoint = (constant == null);
      }
    }
    else if (_property.equals("keyAction") || _property.equals("keyPressed")) { // All key properties set the reportKey boolean
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportKey) reportKey = (constant == null);
      }
    }
    return super.setProperty(_property,_value);
  }


  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("DisplayMode")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("perspective")) return new IntegerValue (DrawingPanel3D.DISPLAY_PERSPECTIVE);
      if (_value.equals("no_perspective")) return new IntegerValue (DrawingPanel3D.DISPLAY_NO_PERSPECTIVE);
      if (_value.equals("planar_xy")) return new IntegerValue (DrawingPanel3D.DISPLAY_PLANAR_XY);
      if (_value.equals("planar_xz")) return new IntegerValue (DrawingPanel3D.DISPLAY_PLANAR_XZ);
      if (_value.equals("planar_yz")) return new IntegerValue (DrawingPanel3D.DISPLAY_PLANAR_YZ);
    }
    if (_propertyType.indexOf("Decoration")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("none")) return new IntegerValue (DrawingPanel3D.DECORATION_NONE);
      if (_value.equals("cube")) return new IntegerValue (DrawingPanel3D.DECORATION_CUBE);
      if (_value.equals("axes")) return new IntegerValue (DrawingPanel3D.DECORATION_AXES);
    }
    if (_propertyType.indexOf("CursorMode")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("none")) return new IntegerValue (DrawingPanel3D.CURSOR_NONE);
      if (_value.equals("cube")) return new IntegerValue (DrawingPanel3D.CURSOR_CUBE);
      if (_value.equals("crosshair")) return new IntegerValue (DrawingPanel3D.CURSOR_CROSSHAIR);
      if (_value.equals("xyz")) return new IntegerValue (DrawingPanel3D.CURSOR_XYZ);
    }
    return super.parseConstant (_propertyType,_value);
  }

  private void setAxesLabels (String _labelsStr) {
    String[] labels = new String[]{"X","Y","Z"};
    if (_labelsStr!=null) {
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(_labelsStr,",;");
      if (tkn.hasMoreTokens()) labels[0] = tkn.nextToken();
      if (tkn.hasMoreTokens()) labels[1] = tkn.nextToken();
      if (tkn.hasMoreTokens()) labels[2] = tkn.nextToken();
    }
    drawingPanel3D.setAxesLabels (labels);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    double angle;
    switch (_index) {
      case 0 :  autoX = _value.getBoolean(); updateAutoscale(); break;
      case 1 :  autoY = _value.getBoolean(); updateAutoscale(); break;
      case 2 :  autoZ = _value.getBoolean(); updateAutoscale(); break;
      case 3 : if (_value.getDouble()!=minX || !xminSet) { minX=_value.getDouble(); xminSet = true; updateExtrema(); } break;
      case 4 : if (_value.getDouble()!=maxX || !xmaxSet) { maxX=_value.getDouble(); xmaxSet = true; updateExtrema(); } break;
      case 5 : if (_value.getDouble()!=minY || !yminSet) { minY=_value.getDouble(); yminSet = true; updateExtrema(); } break;
      case 6 : if (_value.getDouble()!=maxY || !ymaxSet) { maxY=_value.getDouble(); ymaxSet = true; updateExtrema(); } break;
      case 7 : if (_value.getDouble()!=minZ || !zminSet) { minZ=_value.getDouble(); zminSet = true; updateExtrema(); } break;
      case 8 : if (_value.getDouble()!=maxZ || !zmaxSet) { maxZ=_value.getDouble(); zmaxSet = true; updateExtrema(); } break;
/*
      case 0 :
        if (_value.getBoolean()==false) { drawingPanel3D.setAutoscaleX(false); drawingPanel3D.setPreferredMinMaxX(minX,maxX); }
        else drawingPanel3D.setAutoscaleX(true);
        break;
      case 1 :
        if (_value.getBoolean()==false) { drawingPanel3D.setAutoscaleY(false); drawingPanel3D.setPreferredMinMaxY(minY,maxY); }
        else drawingPanel3D.setAutoscaleY(true);
        break;
      case 2 :
        if (_value.getBoolean()==false) { drawingPanel3D.setAutoscaleZ(false); drawingPanel3D.setPreferredMinMaxZ(minZ,maxZ); }
        else drawingPanel3D.setAutoscaleZ(true);
        break;
  
      case 3 : if (_value.getDouble()!=minX) drawingPanel3D.setPreferredMinMaxX(minX=_value.getDouble(),maxX); break;
      case 4 : if (_value.getDouble()!=maxX) drawingPanel3D.setPreferredMinMaxX(minX,maxX=_value.getDouble()); break;
      case 5 : if (_value.getDouble()!=minY) drawingPanel3D.setPreferredMinMaxY(minY=_value.getDouble(),maxY); break;
      case 6 : if (_value.getDouble()!=maxY) drawingPanel3D.setPreferredMinMaxY(minY,maxY=_value.getDouble()); break;
      case 7 : if (_value.getDouble()!=minZ) drawingPanel3D.setPreferredMinMaxZ(minZ=_value.getDouble(),maxZ); break;
      case 8 : if (_value.getDouble()!=maxZ) drawingPanel3D.setPreferredMinMaxZ(minZ,maxZ=_value.getDouble()); break;
*/
      case  9 : posValues[0].value = _value.getDouble(); break;
      case 10 : posValues[1].value = _value.getDouble(); break;
      case 11 : posValues[2].value = _value.getDouble(); break;
      case 12 : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 13 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 14 : // pressaction
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;

      case 15 :
        if (_value instanceof IntegerValue) angle = _value.getInteger()*TO_RAD;
        else angle = _value.getDouble();
        if (angle!=angleValues[0].value) drawingPanel3D.setAlpha(angleValues[0].value=angle);
        break;
      case 16 :
        if (_value instanceof IntegerValue) angle = _value.getInteger()*TO_RAD;
        else angle = _value.getDouble();
        if (angle!=angleValues[1].value) drawingPanel3D.setBeta(angleValues[1].value=angle);
        break;
      case 17 : if (_value.getDouble()!=zoomValue.value) drawingPanel3D.setZoom(zoomValue.value=_value.getDouble()); break;
      case 18 : if (_value.getInteger()!=panValues[0].value) drawingPanel3D.setPan(panValues[0].value=_value.getInteger(),panValues[1].value); break;
      case 19 : if (_value.getInteger()!=panValues[1].value) drawingPanel3D.setPan(panValues[0].value,panValues[1].value=_value.getInteger()); break;

      case 20 : if (_value.getInteger()!=drawingPanel3D.getDisplayMode())    drawingPanel3D.setDisplayMode   (_value.getInteger()); break;
      case 21 : if (_value.getInteger()!=drawingPanel3D.getDecorationType()) drawingPanel3D.setDecorationType(_value.getInteger()); break;
      case 22 : if (_value.getBoolean()!=drawingPanel3D.isSquareAspect())    drawingPanel3D.setSquareAspect  (_value.getBoolean()); break;
      case 23 : if (_value.getInteger()!=drawingPanel3D.getCursorMode())     drawingPanel3D.setCursorMode    (_value.getInteger()); break;
      case 24 : drawingPanel3D.setShowCoordinates(_value.getBoolean()); break;
      case 25 : drawingPanel3D.setRemoveHiddenLines(_value.getBoolean()); break;
      case 26 : drawingPanel3D.setAllowQuickRedraw(_value.getBoolean()); break;

      case 27 : if (_value.getObject() instanceof DecimalFormat) drawingPanel3D.setXFormat((DecimalFormat) _value.getObject());
                else drawingPanel3D.setXFormat((DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
                break;

      case 28 : if (_value.getObject() instanceof DecimalFormat) drawingPanel3D.setYFormat((DecimalFormat) _value.getObject());
                else drawingPanel3D.setYFormat((DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
                break;
      case 29 : if (_value.getObject() instanceof DecimalFormat) drawingPanel3D.setZFormat((DecimalFormat) _value.getObject());
                else drawingPanel3D.setZFormat((DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
                break;

      case 30 : // keyaction
                removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction"));
                addAction(ControlSwingElement.KEY_ACTION,_value.getString());
                break;
      case 31 : keyPressedValue.value = _value.getInteger(); break;
      case 32 : // mouse entered action
        removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction"));
        addAction(ControlSwingElement.MOUSE_ENTERED_ACTION,_value.getString());
        break;
      case 33 : // mouse exited action
        removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction"));
        addAction(ControlSwingElement.MOUSE_EXITED_ACTION,_value.getString());
        break;

      case 34 : drawingPanel3D.setUseColorDepth  (_value.getBoolean()); break;
      case 35 : setAxesLabels (_value.toString()); break;
      default: super.setValue(_index-DP3D_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 : drawingPanel3D.setAutoscaleX(false); drawingPanel3D.setPreferredMinMaxX(minX,maxX); break;
      case  1 : drawingPanel3D.setAutoscaleY(false); drawingPanel3D.setPreferredMinMaxY(minY,maxY); break;
      case  2 : drawingPanel3D.setAutoscaleZ(false); drawingPanel3D.setPreferredMinMaxZ(minZ,maxZ); break;
      case  3 : drawingPanel3D.setPreferredMinMaxX(minX=0.0,maxX); break;
      case  4 : drawingPanel3D.setPreferredMinMaxX(minX,maxX=1.0); break;
      case  5 : drawingPanel3D.setPreferredMinMaxY(minY=0.0,maxY); break;
      case  6 : drawingPanel3D.setPreferredMinMaxY(minY,maxY=1.0); break;
      case  7 : drawingPanel3D.setPreferredMinMaxZ(minZ=0.0,maxZ); break;
      case  8 : drawingPanel3D.setPreferredMinMaxZ(minZ,maxZ=1.0); break;

      case  9 : posValues[0].value = (minX+maxX)/2.0; break;
      case 10 : posValues[1].value = (minY+maxY)/2.0; break;
      case 11 : posValues[2].value = (minZ+maxZ)/2.0; break;
      case 12 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction")); break;
      case 13 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));   break;
      case 14 : removeAction (ControlElement.ACTION,getProperty("action"));                break;

      case 15 : drawingPanel3D.setAlpha(angleValues[0].value=0.0); break;
      case 16 : drawingPanel3D.setBeta(angleValues[1].value=0.0);  break;
      case 17 : drawingPanel3D.setZoom(zoomValue.value=1.0);     break;
      case 18 : drawingPanel3D.setPan(panValues[0].value=0,panValues[1].value);  break;
      case 19 : drawingPanel3D.setPan(panValues[0].value,panValues[1].value=0);  break;

      case 20 : drawingPanel3D.setDisplayMode   (DrawingPanel3D.DISPLAY_PERSPECTIVE); break;
      case 21 : drawingPanel3D.setDecorationType(DrawingPanel3D.DECORATION_CUBE); break;
      case 22 : drawingPanel3D.setSquareAspect  (true); break;
      case 23 : drawingPanel3D.setCursorMode    (DrawingPanel3D.CURSOR_CROSSHAIR); break;
      case 24 : drawingPanel3D.setShowCoordinates(true); break;
      case 25 : drawingPanel3D.setRemoveHiddenLines(true); break;
      case 26 : drawingPanel3D.setAllowQuickRedraw(true); break;

      case 27 : drawingPanel3D.setXFormat(new DecimalFormat("x=0.000;x=-0.000")); break;
      case 28 : drawingPanel3D.setYFormat(new DecimalFormat("y=0.000;y=-0.000")); break;
      case 29 : drawingPanel3D.setZFormat(new DecimalFormat("z=0.000;z=-0.000")); break;
      case 30 : removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction")); break;
      case 31 : keyPressedValue.value = -1; break;
      case 32 : removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction")); break;
      case 33 : removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction")); break;
      case 34 : drawingPanel3D.setUseColorDepth  (true); break;
      case 35 : setAxesLabels (null); break;
      default: super.setDefaultValue(_index-DP3D_ADDED); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case  0 : case  1 : case  2 : case  3 :
      case  4 : case  5 : case  6 : case  7 :
      case  8 :
      case 12 : case 13 : case 14 :
      case 20 : case 21 : case 22 : case 23 :
      case 24 : case 25 : case 26 :
      case 27 : case 28 : case 29 : case 30 :
      case 32 : case 33 : case 34 : case 35 :
        return null;

      case  9 : return posValues[0];
      case 10 : return posValues[1];
      case 11 : return posValues[2];

      case 15 : return angleValues[0];
      case 16 : return angleValues[1];
      case 17 : return zoomValue;
      case 18 : return panValues[0];
      case 19 : return panValues[1];
      case KEY_INDEX : return keyPressedValue;

      default: return super.getValue(_index-DP3D_ADDED);
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  private ControlDrawable selectedDrawable = null;

  public ControlDrawable getSelectedDrawable() { return selectedDrawable; }

  public void setSelectedDrawable (ControlDrawable _dr) { selectedDrawable = _dr; }

  private InteractionSource sourceLingered=null;

  @SuppressWarnings("fallthrough")
  public void interactionPerformed(InteractionEvent _event) {
    if (_event.getTarget()==null) {
      if (_event.getID()==InteractionEvent.MOUSE_ENTERED) {
         invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION);
      }
      else if (_event.getID()==InteractionEvent.MOUSE_EXITED) {
        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);
        if (sourceLingered!=null) sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_EXITED,null,sourceLingered));
        sourceLingered = null;
      }
      else if (_event.getID()==InteractionEvent.MOUSE_MOVED) {
        Interactive interactiveDrawableLingered=drawingPanel3D.getInteractive ();
        if (interactiveDrawableLingered!=null) {
          if (interactiveDrawableLingered instanceof InteractionTarget) {
            if (sourceLingered!=((InteractionTarget) interactiveDrawableLingered).getSource()) {
              if (sourceLingered!=null) sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_EXITED,null,sourceLingered));
              sourceLingered = ((InteractionTarget) interactiveDrawableLingered).getSource();
              sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_ENTERED,null,interactiveDrawableLingered));
            }
          }
        }
        else {
          if (sourceLingered!=null) sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_EXITED,null,sourceLingered));
          sourceLingered = null;
        }
      }
      // Just changing the view point
      else if (_event.getID()==InteractionEvent.MOUSE_PRESSED) {
        invokeActions (ControlSwingElement.ACTION_PRESS);
      }
      else if (_event.getID()==InteractionEvent.MOUSE_RELEASED) {
        invokeActions (ControlElement.ACTION);
      }
      else if (_event.getID()==InteractionEvent.MOUSE_DRAGGED) { // Update projection data
        if (angleValues[0].value != drawingPanel3D.getAlpha() ||
          angleValues[1].value != drawingPanel3D.getBeta()) {
          angleValues[0].value = drawingPanel3D.getAlpha();
          angleValues[1].value = drawingPanel3D.getBeta();
          if (reportViewPoint) variablesChanged(angleIndexes, angleValues);
          if (isUnderEjs) setFieldListValues(angleIndexes,angleValues);
        }
        if (zoomValue.value != drawingPanel3D.getZoom()) {
          zoomValue.value = drawingPanel3D.getZoom();
          if (reportViewPoint) variableChanged(ZOOM_INDEX, zoomValue);
          if (isUnderEjs) setFieldListValue(ZOOM_INDEX, zoomValue);
        }
        if (panValues[0].value != drawingPanel3D.getPan().x ||
          panValues[1].value != drawingPanel3D.getPan().y) {
          panValues[0].value = drawingPanel3D.getPan().x;
          panValues[1].value = drawingPanel3D.getPan().y;
          if (reportViewPoint) variablesChanged(panIndexes, panValues);
          if (isUnderEjs) setFieldListValues(panIndexes, panValues);
        }
      }
      return;
    }
    if (! (_event.getTarget() instanceof Point3D) ) return; // Not a real click on the panel
    Point3D point = (Point3D) _event.getTarget();
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_PRESSED :
        drawingPanel3D.requestFocus();
        invokeActions (ControlSwingElement.ACTION_PRESS);
        // Do not break!
      case InteractionEvent.MOUSE_DRAGGED :
        posValues[0].value = point.x;
        posValues[1].value = point.y;
        posValues[2].value = point.z;
        if (reportDrag) variablesChanged (posIndexes,posValues);
        if (isUnderEjs) setFieldListValues(posIndexes,posValues);
        break;
      case InteractionEvent.MOUSE_RELEASED :
        invokeActions (ControlElement.ACTION);
        break;
    }
  } // End of interaction method


} // End of class
