/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.display.axes.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.drawing2d.PlottingPanel2D;

/**
 * A configurable plottingPanel.
 */
public class ControlPlottingPanel extends ControlDrawablesParent implements InteractiveMouseHandler, ActionListener {
  static final protected int ADDEDBYPLOTTINGPANEL = 48;
  static final int PANEL_FONT  = ControlSwingElement.FONT+ADDEDBYPLOTTINGPANEL;
  static private final int[] posIndex = {18,19};
//  static private final int[] axesPosIndex = {12,14,15, 13,16,17};
  
  static private final int KEY_INDEX = 31;
  static private final int ALIASING = 44;
  static private final int FONT_FACTOR = 45;
  static private final int AXES_ENABLED = 46;
  static private final int AXIS_DRAG_ACTION = 47;

  protected PlottingPanel2D plottingPanel;
  private String title, titleFontname;

  protected boolean reportDrag=false, reportKey=false;
  private java.awt.Rectangle myGutters;
  private int[] defaultGutters;
  // All about axes
  private DrawableAxes axes;
  private boolean axisGridX, axisGridY, xaxisLog, yaxisLog, axesEnabled;
  private int axesType;
  private double xaxisPos, yaxisPos;
  private double deltaR, deltaTheta;
  private String xLabel, yLabel, labelFontname;
  protected MyCoordinateStringBuilder strBuilder;
  private double fontFactor = 1.0;

  private DoubleValue[] posValues ={ new DoubleValue(0.0), new DoubleValue(0.0)};
  private IntegerValue keyPressedValue = new IntegerValue(-1);
//  private Value[] axesValues = { 
//      new BooleanValue(true), new DoubleValue(0.0), new DoubleValue(0.0), // X axis autoscale, min, and max
//      new BooleanValue(true), new DoubleValue(0.0), new DoubleValue(0.0)  // Y axis autoscale, min, and max
//  };

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    plottingPanel = new PlottingPanel2D ("", "", "");
    plottingPanel.enableInspector(false);
    plottingPanel.setSquareAspect (false);
    plottingPanel.setPreferredMinMax(-1,1,-1,1);
    plottingPanel.setAutoscaleX (true);
    plottingPanel.setAutoscaleY (true);
    plottingPanel.setBuffered(true);
    plottingPanel.removeOptionController();
    axes = plottingPanel.getAxes();
    axes.setVisible(true);
    axes.setShowMajorXGrid(axisGridX = true);
    axes.setShowMajorYGrid(axisGridY = true);
    xaxisLog = yaxisLog = false;
    axesEnabled = true;
    deltaR = 1; deltaTheta = Math.PI/8;
    if (axes instanceof CartesianAxes) {
      xaxisLog = ((CartesianAxes) axes).isXLog();
      yaxisLog = ((CartesianAxes) axes).isYLog();
      if      (axes instanceof CartesianType1) axesType = 1;
      else if (axes instanceof CartesianType2) axesType = 2;
      else if (axes instanceof CartesianType3) {
        axesType = 3;
        ((CartesianType3) axes).setEnabled(false);
      }
      else axesType = 1; // default for CartesianAxes
      if (axes instanceof CartesianInteractive) {
        CartesianInteractive iAxes = (CartesianInteractive) axes;
        iAxes.setEnabled(axesEnabled = false);
        iAxes.addAxisListener(this);
      }
    }
    else if (axes instanceof PolarAxes) {
      deltaR     = ((PolarAxes) axes).getDeltaR();
      deltaTheta = ((PolarAxes) axes).getDeltaTheta();
      if      (axes instanceof PolarType1) axesType = 4;
      else if (axes instanceof PolarType2) axesType = 5;
      else axesType = 4; // default for PolarAxes
    }
    else { // Don't touch anything
      axesType = 0;
    }
    minX = Double.NaN; // plottingPanel.getXMin();
    maxX = Double.NaN; // plottingPanel.getXMax();
    minY = Double.NaN; // plottingPanel.getYMin();
    maxY = Double.NaN; // plottingPanel.getYMax();
    autoX = plottingPanel.isAutoscaleX();
    autoY = plottingPanel.isAutoscaleY();

    plottingPanel.setInteractiveMouseHandler(this);
    plottingPanel.setCoordinateStringBuilder(strBuilder=new MyCoordinateStringBuilder());
    plottingPanel.setFocusable(true);
    defaultGutters = plottingPanel.getGutters();

    plottingPanel.addKeyListener (
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

    return plottingPanel;
  }

  protected int[] getPosIndex () { return posIndex; } // in case it should be overriden

  protected List<Object> getExtraMenuOptions() {
    
    JMenu fontMenu = new JMenu(DisplayRes.getString("DrawingFrame.Font_menu_title")); //$NON-NLS-1$
    JMenuItem sizeUpItem = new JMenuItem(DisplayRes.getString("DrawingFrame.IncreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeUpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fontFactor += 0.2;
        axes.resizeFonts(fontFactor, plottingPanel);
        plottingPanel.setFontFactor(fontFactor);
        if (getSimulation()==null || getSimulation().isPaused()) plottingPanel.render();
      }

    });
    fontMenu.add(sizeUpItem);
    final JMenuItem sizeDownItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DecreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeDownItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fontFactor -= 0.2;
        if (fontFactor<0.1) fontFactor = 0.1;
        axes.resizeFonts(fontFactor, plottingPanel);
        plottingPanel.setFontFactor(fontFactor);
        if (getSimulation()==null || getSimulation().isPaused()) plottingPanel.render();
      }

    });
    fontMenu.add(sizeDownItem);
    
    List<Object> extraOptions = super.getExtraMenuOptions();
    extraOptions.add(fontMenu);   
    return extraOptions;
  }
  
// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("title");
      infoList.add ("titleFont");

      infoList.add ("axesType"); // cartesian1, cartesian2, cartesian3, polar1, polar2
      infoList.add ("titleX");
      infoList.add ("titleY");
      infoList.add ("xaxisType");  // log10 or linear for cartesian types
      infoList.add ("yaxisType");  // log10 or linear for cartesian types
      infoList.add ("deltaR");     // In the case of polar coordinates
      infoList.add ("deltaTheta");
      infoList.add ("interiorBackground");
      infoList.add ("majorTicksX");
      infoList.add ("majorTicksY");

      infoList.add ("autoscaleX");
      infoList.add ("autoscaleY");
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");
      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("pressaction");
      infoList.add ("dragaction");
      infoList.add ("action");
      infoList.add ("square");
      infoList.add ("showCoordinates");
      infoList.add ("gutters");

      infoList.add ("xaxisPos");
      infoList.add ("yaxisPos");

      infoList.add ("xFormat");
      infoList.add ("yFormat");

      infoList.add ("keyAction");
      infoList.add ("keyPressed");
      infoList.add ("enteredAction");
      infoList.add ("exitedAction");

      infoList.add ("xyExpression");
      infoList.add ("xyFormat");
      infoList.add ("xMarginPercentage");
      infoList.add ("yMarginPercentage");

      infoList.add ("TLmessage");
      infoList.add ("TRmessage");
      infoList.add ("BLmessage");
      infoList.add ("BRmessage");

      infoList.add ("showAxes");
      infoList.add ("fixedGutters");
      
      infoList.add ("aliasing");
      infoList.add ("fontFactor");

      infoList.add ("axesEnabled");
      infoList.add ("axisDragAction");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("pressaction")) return "pressAction";
    if (_property.equals("dragaction"))  return "dragAction";
    if (_property.equals("action"))      return "releaseAction";
    if (_property.equals("square"))      return "squareAspect";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("title"))          return "String TRANSLATABLE";
    if (_property.equals("titleFont"))      return "Font|Object";

    if (_property.equals("axesType"))       return "int|AxesType";
    if (_property.equals("titleX"))         return "String TRANSLATABLE";
    if (_property.equals("titleY"))         return "String TRANSLATABLE";
    if (_property.equals("xaxisType"))      return "int|CartesianAxisType";
    if (_property.equals("yaxisType"))      return "int|CartesianAxisType";
    if (_property.equals("deltaR"))         return "int|double";
    if (_property.equals("deltaTheta"))     return "int|double";
    if (_property.equals("interiorBackground")) return "Color|Object";
    if (_property.equals("majorTicksX"))     return "boolean";
    if (_property.equals("majorTicksY"))     return "boolean";

    if (_property.equals("autoscaleX"))     return "boolean";
    if (_property.equals("autoscaleY"))     return "boolean";
    if (_property.equals("minimumX"))       return "int|double";
    if (_property.equals("maximumX"))       return "int|double";
    if (_property.equals("minimumY"))       return "int|double";
    if (_property.equals("maximumY"))       return "int|double";
    if (_property.equals("x"))              return "int|double";
    if (_property.equals("y"))              return "int|double";
    if (_property.equals("action"))         return "Action CONSTANT";
    if (_property.equals("pressaction"))    return "Action CONSTANT";
    if (_property.equals("dragaction"))     return "Action CONSTANT";
    if (_property.equals("square"))         return "boolean";
    if (_property.equals("showCoordinates"))return "boolean";
    if (_property.equals("gutters"))        return "Gutters|Object";

    if (_property.equals("xaxisPos"))          return "int|double";
    if (_property.equals("yaxisPos"))          return "int|double";

    if (_property.equals("xFormat"))        return "Format|Object|String TRANSLATABLE";
    if (_property.equals("yFormat"))        return "Format|Object|String TRANSLATABLE";

    if (_property.equals("keyAction"))      return "Action CONSTANT";
    if (_property.equals("keyPressed"))     return "int";
    if (_property.equals("enteredAction"))  return "Action CONSTANT";
    if (_property.equals("exitedAction"))   return "Action CONSTANT";

    if (_property.equals("xyExpression"))  return "Object|String";
    if (_property.equals("xyFormat"))        return "Format|Object|String TRANSLATABLE";
    if (_property.equals("xMarginPercentage"))       return "int|double";
    if (_property.equals("yMarginPercentage"))       return "int|double";

    if (_property.equals("TLmessage"))   return "String TRANSLATABLE";
    if (_property.equals("TRmessage"))   return "String TRANSLATABLE";
    if (_property.equals("BLmessage"))   return "String TRANSLATABLE";
    if (_property.equals("BRmessage"))   return "String TRANSLATABLE";

    if (_property.equals("showAxes"))     return "boolean";
    if (_property.equals("fixedGutters")) return "boolean";
    if (_property.equals("aliasing"))     return "boolean";

    if (_property.equals("fontFactor"))   return "int|double";

    if (_property.equals("axesEnabled"))    return "boolean";
    if (_property.equals("axisDragAction")) return "Action CONSTANT";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("CartesianAxisType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("linear")) return new IntegerValue (0);
      if (_value.equals("log10"))  return new IntegerValue (1);
    }
    if (_propertyType.indexOf("AxesType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("null") || _value.equals("none")) return new IntegerValue (0);
      if (_value.equals("cartesian1")) return new IntegerValue (1);
      if (_value.equals("cartesian2")) return new IntegerValue (2);
      if (_value.equals("cartesian3")) return new IntegerValue (3);
      if (_value.equals("polar1"))     return new IntegerValue (4);
      if (_value.equals("polar2"))     return new IntegerValue (5);
    }
    return super.parseConstant (_propertyType,_value);
  }

  // Backwards compatibility
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("x") || _property.equals("y") || _property.equals("dragaction")) {
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        reportDrag = (constant == null);
      }
    }
    else if (_property.equals("keyAction") || _property.equals("keyPressed")) { // All key properties set the reportKey boolean
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportKey) reportKey = (constant == null);
      }
    }
    else if (_property.equals("xaxis")) return super.setProperty ("xaxisPos",_value);
    else if (_property.equals("yaxis")) return super.setProperty ("yaxisPos",_value);
    return super.setProperty(_property,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------


  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : plottingPanel.setTitle(title=_value.getString(),titleFontname); break;
      case 1 :
        if (_value.getObject() instanceof Font) {
          Font font = (Font) _value.getObject();
          titleFontname = font.getFamily();
          if (font.isPlain()) titleFontname += "-PLAIN";
          else if (font.isItalic()) {
            if (font.isBold()) titleFontname += "-BOLDITALIC";
            else titleFontname += "-ITALIC";
          }
          else if (font.isBold()) titleFontname += "-BOLD";
          titleFontname += "-" + font.getSize();
          if (title!=null) axes.setTitle(title,titleFontname);
        }
        break;
      case 2 :
        if (axesType!=_value.getInteger()) { // Create new axes
          switch (axesType = _value.getInteger()) {
            default :
            case 1 : 
              axes = AxisFactory.createAxesType1(plottingPanel); 
              ((CartesianType1)axes).setXLog(xaxisLog); ((CartesianType1)axes).setYLog(yaxisLog); break;
            case 2 : 
              axes = AxisFactory.createAxesType2(plottingPanel); 
              ((CartesianType2)axes).setXLog(xaxisLog); ((CartesianType2)axes).setYLog(yaxisLog); break;
            case 3 : 
              axes = AxisFactory.createAxesType3(plottingPanel); 
              ((CartesianType3) axes).setEnabled(false); 
              ((CartesianType3)axes).setXLog(xaxisLog); ((CartesianType3)axes).setYLog(yaxisLog); break;
            case 4 : 
              axes = new PolarType1(plottingPanel); 
              ((PolarAxes)axes).setDeltaR(deltaR); ((PolarAxes)axes).setDeltaTheta(deltaTheta); break;
            case 5 : 
              axes = new PolarType2(plottingPanel); 
              ((PolarAxes)axes).setDeltaR(deltaR); ((PolarAxes)axes).setDeltaTheta(deltaTheta); break;
          }
          if (xLabel!=null) axes.setXLabel(xLabel, labelFontname);
          if (yLabel!=null) axes.setYLabel(yLabel, labelFontname);
          if (title!=null)  axes.setTitle(title, titleFontname);
          axes.setShowMajorXGrid(axisGridX);
          axes.setShowMajorYGrid(axisGridY);
          if (axes instanceof CartesianAxes) {
            ((CartesianAxes) axes).setX(xaxisPos);
            ((CartesianAxes) axes).setY(yaxisPos);
            if (axes instanceof CartesianInteractive) {
              CartesianInteractive iAxes = (CartesianInteractive) axes;
              iAxes.setEnabled(axesEnabled);
              iAxes.addAxisListener(this);
            }
          }
          if (axesType==0) plottingPanel.setAxes(null);
          else plottingPanel.setAxes(axes);
          axes.resizeFonts(fontFactor, plottingPanel);
          plottingPanel.setPreferredMinMax(minX,maxX,minY,maxY);
          plottingPanel.setCoordinateStringBuilder(strBuilder);
        }
        break;

      case 3 : 
        if (_value.getString()!=null && !_value.getString().equals(xLabel)) {
          plottingPanel.setXLabel(xLabel=_value.getString(),labelFontname);
          axes.resizeFonts(fontFactor, plottingPanel);
        }
        break;
      case 4 : 
        if (_value.getString()!=null && !_value.getString().equals(yLabel)) {
          plottingPanel.setYLabel(yLabel=_value.getString(),labelFontname); 
          axes.resizeFonts(fontFactor, plottingPanel);
        }
        break;

      case 5 : if (xaxisLog && _value.getInteger()!=1) plottingPanel.setLogScale(xaxisLog = false, yaxisLog);
               else if (!xaxisLog && _value.getInteger()==1) plottingPanel.setLogScale(xaxisLog = true, yaxisLog);
               break;
      case 6 : if (yaxisLog && _value.getInteger()!=1) plottingPanel.setLogScale(xaxisLog, yaxisLog = false);
               else if (!yaxisLog && _value.getInteger()==1) plottingPanel.setLogScale(xaxisLog, yaxisLog = true);
               break;
      case 7 : if (_value.getDouble()!=deltaR) {
                 deltaR = _value.getDouble();
                 if (axes instanceof PolarAxes) ((PolarAxes)axes).setDeltaR(deltaR);
               }
               break;
      case 8 : if (_value.getDouble()!=deltaTheta) {
                 deltaTheta = _value.getDouble();
                 if (axes instanceof PolarAxes) ((PolarAxes)axes).setDeltaTheta(deltaTheta);
              }
              break;
      case 9 : if (_value.getObject() instanceof Color) axes.setInteriorBackground((Color) _value.getObject()); break;

      case 10 : axes.setShowMajorXGrid(_value.getBoolean()); break;
      case 11 : axes.setShowMajorYGrid(_value.getBoolean()); break;

      case 12 : autoX = _value.getBoolean(); updateAutoscale(); break;
      case 13 : autoY = _value.getBoolean(); updateAutoscale(); break;
      case 14 : if (_value.getDouble()!=minX || !xminSet) { minX=_value.getDouble(); xminSet = true; updateExtrema(); } break;
      case 15 : if (_value.getDouble()!=maxX || !xmaxSet) { maxX=_value.getDouble(); xmaxSet = true; updateExtrema(); } break;
      case 16 : if (_value.getDouble()!=minY || !yminSet) { minY=_value.getDouble(); yminSet = true; updateExtrema(); } break;
      case 17 : if (_value.getDouble()!=maxY || !ymaxSet) { maxY=_value.getDouble(); ymaxSet = true; updateExtrema(); } break;

      case 18 : posValues[0].value = _value.getDouble(); break;
      case 19 : posValues[1].value = _value.getDouble(); break;
      case 20 : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 21 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 22 : // pressaction
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;

      case 23 : plottingPanel.setSquareAspect(_value.getBoolean()); break;
      case 24 : plottingPanel.setShowCoordinates(_value.getBoolean()); break;
      case 25 :
        if (_value.getObject() instanceof java.awt.Rectangle) {
          java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
          if (rect!=myGutters) {
            plottingPanel.setPreferredGutters(rect.x,rect.y,rect.width,rect.height);
            myGutters = rect;
          }
        }
        break;
      case 26 : if (xaxisPos!=_value.getDouble() && axes instanceof CartesianAxes) ((CartesianAxes)axes).setX(xaxisPos=_value.getDouble()); break;
      case 27 : if (yaxisPos!=_value.getDouble() && axes instanceof CartesianAxes) ((CartesianAxes)axes).setY(yaxisPos=_value.getDouble()); break;

      case 28 :
        if (_value.getObject() instanceof java.text.DecimalFormat) strBuilder.setXFormat((java.text.DecimalFormat) _value.getObject());
        else strBuilder.setXFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;
      case 29 :
        if (_value.getObject() instanceof java.text.DecimalFormat)
          strBuilder.setYFormat((java.text.DecimalFormat) _value.getObject());
        else strBuilder.setYFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;

      case 30 : // keyaction
        removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction"));
        addAction(ControlSwingElement.KEY_ACTION,_value.getString());
        break;
      case KEY_INDEX : keyPressedValue.value = _value.getInteger(); break;
      case 32 : // mouse entered action
        removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction"));
        addAction(ControlSwingElement.MOUSE_ENTERED_ACTION,_value.getString());
        break;
      case 33 : // mouse exited action
        removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction"));
        addAction(ControlSwingElement.MOUSE_EXITED_ACTION,_value.getString());
        break;

      case 34 : strBuilder.setExpression(_value.getString()); break;
      case 35 :
        if (_value.getObject() instanceof java.text.DecimalFormat) strBuilder.setExpressionFormat((java.text.DecimalFormat) _value.getObject());
        else strBuilder.setExpressionFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;
      case 36 : plottingPanel.setXMarginPercentage(_value.getDouble()); break;
      case 37 : plottingPanel.setYMarginPercentage(_value.getDouble()); break;

      case 38 : plottingPanel.setMessage(_value.getString(),DrawingPanel.TOP_LEFT); break;
      case 39 : plottingPanel.setMessage(_value.getString(),DrawingPanel.TOP_RIGHT); break;
      case 40 : plottingPanel.setMessage(_value.getString(),DrawingPanel.BOTTOM_LEFT); break;
      case 41 : plottingPanel.setMessage(_value.getString(),DrawingPanel.BOTTOM_RIGHT); break;

      case 42 :
        {
          boolean visible = _value.getBoolean();
          plottingPanel.setClipAtGutter(visible);
          plottingPanel.setAxesVisible(visible);
        }
        break;
      case 43 : plottingPanel.setAdjustableGutter(!_value.getBoolean()); break;
      case ALIASING : 
        boolean on = _value.getBoolean();
        plottingPanel.setAntialiasTextOn(on);
        plottingPanel.setAntialiasShapeOn(on);
        break;
      
      case FONT_FACTOR :
        if (fontFactor!=_value.getDouble()) {
          axes.resizeFonts(fontFactor=_value.getDouble(), plottingPanel);
          plottingPanel.setFontFactor(fontFactor);
        }
        break;

      case AXES_ENABLED : if (axes instanceof CartesianInteractive) ((CartesianInteractive)axes).setEnabled(axesEnabled = _value.getBoolean()); break;
      case AXIS_DRAG_ACTION : // axis drag action
        removeAction (ControlSwingElement.AXIS_DRAGGED_ACTION,getProperty("axisDragAction"));
        addAction(ControlSwingElement.AXIS_DRAGGED_ACTION,_value.getString());
        break;
      
      default: super.setValue(_index-ADDEDBYPLOTTINGPANEL,_value); break;
      case PANEL_FONT :
        if (_value.getObject() instanceof Font) {
          Font font = (Font) _value.getObject();
          labelFontname = font.getFamily();
          if (font.isPlain()) labelFontname += "-PLAIN";
          else if (font.isItalic()) {
            if (font.isBold()) labelFontname += "-BOLDITALIC";
            else labelFontname += "-ITALIC";
          }
          else if (font.isBold()) labelFontname += "-BOLD";
          labelFontname += "-" + font.getSize();
          if (xLabel!=null) axes.setXLabel(xLabel, labelFontname);
          if (yLabel!=null) axes.setYLabel(yLabel, labelFontname);
          axes.resizeFonts(fontFactor, plottingPanel);
          plottingPanel.setFontFactor(fontFactor);
        }
        super.setValue (ControlSwingElement.FONT,_value);
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {

      case 0 : plottingPanel.setTitle(title="",titleFontname); break;
      case 1 : titleFontname = "Helvetica-BOLD-14"; if (title!=null) axes.setTitle(title,titleFontname); break;

      case 2 :
        axesType = 1;
        axes = AxisFactory.createAxesType1(plottingPanel); ((CartesianType1)axes).setXLog(xaxisLog); ((CartesianType1)axes).setYLog(yaxisLog);
        if (xLabel!=null) axes.setXLabel(xLabel, labelFontname);
        if (yLabel!=null) axes.setYLabel(yLabel, labelFontname);
        if (title!=null)  axes.setTitle(title, titleFontname);
        axes.setShowMajorXGrid(axisGridX);
        axes.setShowMajorYGrid(axisGridY);
        if (axes instanceof CartesianInteractive) {
          CartesianInteractive iAxes = (CartesianInteractive) axes;
          iAxes.setEnabled(axesEnabled);
          iAxes.addAxisListener(this);
        }
        axes.resizeFonts(fontFactor, plottingPanel);
        // plottingPanel.setAxes(axes);
        break;

      case 3 : plottingPanel.setXLabel(xLabel="",null); break;
      case 4 : plottingPanel.setYLabel(yLabel="",null); break;
      case 5 : plottingPanel.setLogScale(xaxisLog = false, yaxisLog); break;
      case 6 : plottingPanel.setLogScale(xaxisLog, yaxisLog = false); break;
      case 7 : deltaR = 1; if (axes instanceof PolarAxes) ((PolarAxes)axes).setDeltaR(deltaR); break;
      case 8 : deltaTheta = Math.PI/8; if (axes instanceof PolarAxes) ((PolarAxes)axes).setDeltaTheta(deltaTheta); break;
      case 9 : axes.setInteriorBackground(Color.white); break;

      case 10 : axes.setShowMajorXGrid(true); break;
      case 11 : axes.setShowMajorYGrid(true); break;

      case 12 : autoX = false; updateAutoscale(); break;
      case 13 : autoY = false; updateAutoscale(); break;
      case 14 : minX=Double.NaN; break;// ; xminSet = false; updateExtrema(); break;
      case 15 : maxX=Double.NaN; break;// ; xmaxSet = false; updateExtrema(); break;
      case 16 : minY=Double.NaN; break;// ; yminSet = false; updateExtrema(); break;
      case 17 : maxY=Double.NaN; break;// ; ymaxSet = false; updateExtrema(); break;

      case 18 : posValues[0].value = (minX+maxX)/2.0; break; // x
      case 19 : posValues[1].value = (minY+maxY)/2.0; break; // y
      case 20 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction")); break;
      case 21 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));   break;
      case 22 : removeAction (ControlElement.ACTION,getProperty("action"));                break;

      case 23 : plottingPanel.setSquareAspect(false);  break;
      case 24 : plottingPanel.setShowCoordinates(true); break;
      case 25 : plottingPanel.setPreferredGutters(defaultGutters[0],defaultGutters[1],defaultGutters[2],defaultGutters[3]); myGutters = null; break;

      case 26 : if (axes instanceof CartesianAxes) ((CartesianAxes)axes).setX(xaxisPos=Double.NaN); break;
      case 27 : if (axes instanceof CartesianAxes) ((CartesianAxes)axes).setY(yaxisPos=Double.NaN); break;

      case 28 : strBuilder.setXFormat(new java.text.DecimalFormat("x=0.000;x=-0.000")); break;
      case 29 : strBuilder.setYFormat(new java.text.DecimalFormat("y=0.000;y=-0.000")); break;

      case 30 : removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction")); break;
      case KEY_INDEX : keyPressedValue.value = -1; break;
      case 32 : removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction")); break;
      case 33 : removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction")); break;

      case 34 : strBuilder.setExpression(null); break;
      case 35 : strBuilder.setXFormat(new java.text.DecimalFormat("0.000;-0.000")); break;

      case 36 : plottingPanel.setXMarginPercentage(0.0); break;
      case 37 : plottingPanel.setYMarginPercentage(0.0); break;

      case 38 : plottingPanel.setMessage("",DrawingPanel.TOP_LEFT); break;
      case 39 : plottingPanel.setMessage("",DrawingPanel.TOP_RIGHT); break;
      case 40 : plottingPanel.setMessage("",DrawingPanel.BOTTOM_LEFT); break;
      case 41 : plottingPanel.setMessage("",DrawingPanel.BOTTOM_RIGHT); break;

      case 42 : plottingPanel.setClipAtGutter(true); plottingPanel.setAxesVisible(true); break;
      case 43 : plottingPanel.setAdjustableGutter(false); break;

      case ALIASING : 
        plottingPanel.setAntialiasTextOn(false);
        plottingPanel.setAntialiasShapeOn(false);
        break;

      case FONT_FACTOR :
        axes.resizeFonts(fontFactor=1.0, plottingPanel);
        plottingPanel.setFontFactor(fontFactor);
        break;

      case AXES_ENABLED : if (axes instanceof CartesianInteractive) ((CartesianInteractive)axes).setEnabled(axesEnabled = false); break;
      case AXIS_DRAG_ACTION : removeAction (ControlSwingElement.AXIS_DRAGGED_ACTION,getProperty("axisDragAction")); break;

      default: super.setDefaultValue(_index-ADDEDBYPLOTTINGPANEL); break;
      case PANEL_FONT :
        labelFontname = "Helvetica-PLAIN-12";
        if (xLabel!=null) axes.setXLabel(xLabel, labelFontname);
        if (yLabel!=null) axes.setYLabel(yLabel, labelFontname);
        axes.resizeFonts(fontFactor, plottingPanel);
        plottingPanel.setFontFactor(fontFactor);
        super.setDefaultValue (ControlSwingElement.FONT);
        break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "Helvetica-BOLD-14";
      case 2 : return "CARTESIAN1";
      case 3 : case 4 : return "<none>";
      case 5 : case 6 : return "LINEAR";
      case 7 : return "1";
      case 8 : return "PI/8";
      case 9 : return "WHITE";
      case 10 : case 11 : return "true";
      case 12 : case 13 : return "false";
      case 14 : case 15 : case 16 : case 17 : case 18 : case 19 : return "<none>";
      case 20 : case 21 : case 22 : return "<no_action>";
      case 23 : return "false";
      case 24 : return "true";
      case 25 : return ""+defaultGutters[0]+","+defaultGutters[1]+","+defaultGutters[2]+","+defaultGutters[3];
      case 26 : case 27 : return "<none>";
      case 28 : return "x=0.000;x=-0.000";
      case 29 : return "y=0.000;y=-0.000";
      case 30 : case 32 : case 33 : return "<no_action>";
      case KEY_INDEX : return "<none>";
      case 34 : return "<none>";
      case 35 : return "0.000;-0.000";
      case 36 : case 37 : return "0.0";
      case 38 : case 39 : case 40 : case 41 : return "<none>";
      case 42 : return "true";
      case 43 : return "false";
      case ALIASING : return "false";

      case FONT_FACTOR :return "1";
      
      case AXES_ENABLED : return "false";
      case AXIS_DRAG_ACTION : return "<no_action>";

      default : return super.getDefaultValueString(_index-ADDEDBYPLOTTINGPANEL);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case  0 : case  1 : case  2 : case  3 : case  4 : case  5 :
      case  6 : case  7 : case  8 : case  9 : case 10 : case 11 :
      case 12 : case 13 : case 14 : case 15 : case 16 : case 17 :
        return null;
      case 18 : return posValues[0];
      case 19 : return posValues[1];
      case KEY_INDEX : return keyPressedValue;
      case 20 : case 21 : case 22 : case 23 : case 24 : case 25 :
      case 26 : case 27 : case 28 : case 29 : case 30 : case 32 :
      case 33 : case 34 : case 35 : case 36 : case 37 : case 38 :
      case 39 : case 40 : case 41 : case 42 : case 43 : 
      case ALIASING : case FONT_FACTOR : case AXES_ENABLED :
      case AXIS_DRAG_ACTION : 
        return null;
      default: return super.getValue(_index-ADDEDBYPLOTTINGPANEL);
    }
  }

// ---------- Implementation of InteractiveMouseHandler -------------------

  private InteractionTarget targetHit=null;
  private InteractionSource sourceLingered=null;
  private org.opensourcephysics.drawing2d.interaction.InteractionTarget target2D=null, target2DEntered=null;

  public  ControlDrawable getSelectedDrawable() {
    if (targetHit!=null && (targetHit.getSource() instanceof HasDataObjectInterface) ) {
      Object data = ((HasDataObjectInterface) targetHit.getSource()).getDataObject();
      if (data instanceof ControlDrawable) return (ControlDrawable) data;
    }
    else if (target2D!=null) {
      Object data = target2D.getElement().getDataObject();
      if (data instanceof ControlDrawable) return (ControlDrawable) data;
    }
    return null;
  }

  final private void invokeTheAction (org.opensourcephysics.drawing2d.interaction.InteractionTarget _target2D, int _action, MouseEvent _evt) {
    org.opensourcephysics.drawing2d.Element element = _target2D.getElement();
    element.invokeActions(new org.opensourcephysics.drawing2d.interaction.InteractionEvent(element, _action, 
        _target2D.getActionCommand(),_target2D, _evt));
  }
  
  public void handleMouseAction(final InteractivePanel _panel, MouseEvent _evt) {
    switch (_panel.getMouseAction ()) {
      case InteractivePanel.MOUSE_PRESSED :
        Interactive interactiveDrawable=_panel.getInteractive ();
        if (interactiveDrawable instanceof InteractionTarget) {
          targetHit = (InteractionTarget) interactiveDrawable;
          targetHit.getSource().invokeActions (new InteractionEvent (targetHit.getSource(),InteractionEvent.MOUSE_PRESSED,null,targetHit));
        }
        else if (interactiveDrawable instanceof org.opensourcephysics.drawing2d.interaction.InteractionTarget) {
          target2D = (org.opensourcephysics.drawing2d.interaction.InteractionTarget) interactiveDrawable;
          invokeTheAction(target2D,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_PRESSED,_evt);
        }
        else {
          targetHit = null;
          target2D = null;
          mousePressed(_panel.getMouseX (),_panel.getMouseY ());
        }
        break;
      case InteractivePanel.MOUSE_DRAGGED :
        if (targetHit!=null) {
          Point3D trackerPoint = new Point3D (_panel.getMouseX (),_panel.getMouseY (),0.0);
          Simulation sim = getSimulation();
          if (sim!=null) {
            sim.invokeMethodWhenIdle(new UpdateHotSpotDelayedAction(targetHit,_panel,trackerPoint));
            if (sim.isPaused()) _panel.render();
          }
          else {
            targetHit.updateHotspot(_panel,trackerPoint);
            _panel.render();
          }
        }
        else if (target2D!=null) {
          double [] point = new double[] {_panel.getMouseX (),_panel.getMouseY (),0.0};
          Simulation sim = getSimulation();
          if (sim!=null) {
            sim.invokeMethodWhenIdle(new UpdateHotSpot2DDelayedAction(target2D,point.clone(),_evt));
            if (sim.isPaused()) _panel.render();
          }
          else {
            target2D.getElement().updateHotSpot(target2D, point);
            invokeTheAction(target2D,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_DRAGGED,_evt);
            _panel.render();
          }
        }
        else mouseDragged(_panel.getMouseX (),_panel.getMouseY ());
        break;
      case InteractivePanel.MOUSE_RELEASED :
        if (targetHit!=null) {
          targetHit.getSource().invokeActions (new InteractionEvent (targetHit.getSource(),InteractionEvent.MOUSE_RELEASED,null,targetHit));
          Simulation sim = getSimulation();
          if (sim==null || sim.isPaused()) {
            _panel.invalidateImage(); // this is needed because of the peculiar behavior of InteractivePanel scaleX and scaleY methods
            _panel.repaint();
          }
        }
        else if (target2D!=null) {
          invokeTheAction(target2D,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_RELEASED,_evt);
          Simulation sim = getSimulation();
          if (sim==null || sim.isPaused()) {
            _panel.invalidateImage(); // this is needed because of the peculiar behavior of InteractivePanel scaleX and scaleY methods
            _panel.repaint();
          }
        }
        else mouseReleased(_panel.getMouseX (),_panel.getMouseY ());
        targetHit = null;
        target2D = null;
        break;
      case InteractivePanel.MOUSE_ENTERED :
        invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION);
        break;
      case InteractivePanel.MOUSE_EXITED :
        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);
        targetHit = null;
        target2D = null;
        if (sourceLingered!=null) {
          sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_EXITED,null,sourceLingered));
        }
        else if (target2DEntered!=null) {
          invokeTheAction(target2DEntered,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_EXITED,_evt);
        }
        sourceLingered = null;
        target2DEntered = null;
        break;
      case InteractivePanel.MOUSE_MOVED :
        Interactive interactiveDrawableLingered=_panel.getInteractive ();
        if (interactiveDrawableLingered!=null) {
          _panel.setMouseCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
          if (interactiveDrawableLingered instanceof InteractionTarget) {
            if (sourceLingered!=((InteractionTarget) interactiveDrawableLingered).getSource()) {
              if (sourceLingered!=null) sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_EXITED,null,sourceLingered));
              sourceLingered = ((InteractionTarget) interactiveDrawableLingered).getSource();
              sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_ENTERED,null,interactiveDrawableLingered));
            }
          }
          else if (interactiveDrawableLingered instanceof org.opensourcephysics.drawing2d.interaction.InteractionTarget) {
            if (target2DEntered==null ||
                target2DEntered!=interactiveDrawableLingered) {
              if (target2DEntered!=null) invokeTheAction(target2DEntered,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_EXITED,_evt);
              target2DEntered = (org.opensourcephysics.drawing2d.interaction.InteractionTarget) interactiveDrawableLingered;
              invokeTheAction(target2DEntered,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_ENTERED,_evt);
            }
          }
        }
        else {
          _panel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
          if (sourceLingered!=null) {
            sourceLingered.invokeActions (new InteractionEvent (sourceLingered,InteractionEvent.MOUSE_EXITED,null,sourceLingered));
          }
          else if (target2DEntered!=null) {
            invokeTheAction(target2DEntered,org.opensourcephysics.drawing2d.interaction.InteractionEvent.MOUSE_EXITED,_evt);
          }
          sourceLingered = null;
          target2DEntered = null;
        }
        break;
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  public void mousePressed (double _x, double _y) {
    plottingPanel.requestFocus();
    invokeActions (ControlSwingElement.ACTION_PRESS);
    mouseDragged (_x,_y);
  }

  public void mouseDragged (double _x, double _y) {
    posValues[0].value = _x;
    posValues[1].value = _y;
//    System.out.println("dragged at "+_x+","+_y);
    if (reportDrag) {
      variablesChanged (getPosIndex(),posValues); // Report only is someone is interested
      if (isUnderEjs) setFieldListValues(getPosIndex(), posValues);
    }
  }

  public void mouseReleased (double _x, double _y) {
    invokeActions (ControlElement.ACTION);
  }

  /*
   * used as listener by CartesianInteractive axes 
   */
  public void actionPerformed(ActionEvent evt) {
    isZoomed = true;
    invokeActions (ControlSwingElement.AXIS_DRAGGED_ACTION);
  }

} // End of class
