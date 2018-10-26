/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import org.opensourcephysics.displayejs.InteractionTarget;
import org.opensourcephysics.displayejs.Point3D;
import org.opensourcephysics.displayejs.InteractionEvent;
import org.opensourcephysics.displayejs.HasDataObjectInterface;
import org.opensourcephysics.displayejs.InteractionSource;

import org.opensourcephysics.drawing2d.DrawingPanel2D;

import org.opensourcephysics.display.*;

import java.awt.Cursor;
import java.awt.event.*;


/**
 * A panel to hold Drawables
 */
public class ControlDrawingPanel extends ControlDrawablesParent implements InteractiveMouseHandler {
  static public final int DP_ADDED = 30;
  static private final int[] posIndex = {6,7};
  static private final int KEY_INDEX = 17;
  static private final int ALIASING = 28;
  static private final int FONT_FACTOR = 29;

  protected boolean reportDrag=false, reportKey=false;
  protected DrawingPanel2D drawingPanel;
  private java.awt.Rectangle myGutters=null;
  private DoubleValue[] posValues ={ new DoubleValue(0.0), new DoubleValue(0.0)};
  private IntegerValue keyPressedValue = new IntegerValue(-1);
  protected MyCoordinateStringBuilder strBuilder;
  private double fontFactor = 1.0;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    drawingPanel = new DrawingPanel2D ();
    drawingPanel.enableInspector (false); // OSP Update
    drawingPanel.setSquareAspect (false);
    drawingPanel.setBuffered(true);
    drawingPanel.removeOptionController();
    minX = Double.NaN; // drawingPanel.getXMin();
    maxX = Double.NaN; // drawingPanel.getXMax();
    minY = Double.NaN; // drawingPanel.getYMin();
    maxY = Double.NaN; // drawingPanel.getYMax();
    autoX = drawingPanel.isAutoscaleX();
    autoY = drawingPanel.isAutoscaleY();
    drawingPanel.render();
    drawingPanel.setInteractiveMouseHandler(this);
    drawingPanel.setCoordinateStringBuilder(strBuilder=new MyCoordinateStringBuilder());
    drawingPanel.setFocusable(true);

    drawingPanel.addKeyListener (
      new KeyAdapter() {
        public void keyPressed  (KeyEvent _e) {
          keyPressedValue.value = _e.getKeyCode();
          if (reportKey) {
            variableChanged (KEY_INDEX,keyPressedValue);
            invokeActions (ControlSwingElement.KEY_ACTION);
          }
        }
        public void keyReleased (KeyEvent _e) {
//          keyPressedValue.value = -1;
          if (reportKey) variableChanged (KEY_INDEX,keyPressedValue);
        }
      }
    );
    return drawingPanel;
  }

  protected int[] getPosIndex () { return posIndex; } // in case it should be overriden

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
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

      infoList.add ("aliasing");
      infoList.add ("fontFactor");

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
    if (_property.equals("aliasing")) return "boolean";
    if (_property.equals("fontFactor"))   return "int|double";

    return super.getPropertyInfo(_property);
  }

  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("x") || _property.equals("y") || _property.equals("dragaction")) {
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportDrag) reportDrag = (constant == null);
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


// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    //System.out.println ("setting avlue of "+_index+" to "+_value.getDouble());
    switch (_index) {
      case 0 :  autoX = _value.getBoolean(); updateAutoscale(); break;
      case 1 :  autoY = _value.getBoolean(); updateAutoscale(); break;
      case 2 : if (_value.getDouble()!=minX || !xminSet) { minX=_value.getDouble(); xminSet = true; updateExtrema(); } break;
      case 3 : if (_value.getDouble()!=maxX || !xmaxSet) { maxX=_value.getDouble(); xmaxSet = true; updateExtrema(); } break;
      case 4 : if (_value.getDouble()!=minY || !yminSet) { minY=_value.getDouble(); yminSet = true; updateExtrema(); } break;
      case 5 : if (_value.getDouble()!=maxY || !ymaxSet) { maxY=_value.getDouble(); ymaxSet = true; updateExtrema(); } break;
      case 6 : posValues[0].value = _value.getDouble(); break;
      case 7 : posValues[1].value = _value.getDouble(); break;
      case 8 : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 9 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 10 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 11 : drawingPanel.setSquareAspect(_value.getBoolean());    break;
      case 12 : drawingPanel.setShowCoordinates(_value.getBoolean()); break;
      case 13 :
        if (_value.getObject() instanceof java.awt.Rectangle) {
          java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
          if (rect!=myGutters) {
            drawingPanel.setGutters(rect.x,rect.y,rect.width,rect.height);
            myGutters = rect;
          }
        }
        break;
      case 14 :
        if (_value.getObject() instanceof java.text.DecimalFormat) strBuilder.setXFormat((java.text.DecimalFormat) _value.getObject());
        else strBuilder.setXFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;
      case 15 :
        if (_value.getObject() instanceof java.text.DecimalFormat) strBuilder.setYFormat((java.text.DecimalFormat) _value.getObject());
        else strBuilder.setYFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;
      case 16 : // keyaction
        removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction"));
        addAction(ControlSwingElement.KEY_ACTION,_value.getString());
        break;
      case KEY_INDEX : keyPressedValue.value = _value.getInteger(); break;
      case 18 : // mouse entered action
        removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction"));
        addAction(ControlSwingElement.MOUSE_ENTERED_ACTION,_value.getString());
        break;
      case 19 : // mouse exited action
        removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction"));
        addAction(ControlSwingElement.MOUSE_EXITED_ACTION,_value.getString());
        break;
      case 20 : strBuilder.setExpression(_value.getString()); break;
      case 21 :
        if (_value.getObject() instanceof java.text.DecimalFormat) strBuilder.setExpressionFormat((java.text.DecimalFormat) _value.getObject());
        else strBuilder.setExpressionFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;
      case 22 : drawingPanel.setXMarginPercentage(_value.getDouble()); break;
      case 23 : drawingPanel.setYMarginPercentage(_value.getDouble()); break;

      case 24 : drawingPanel.setMessage(_value.getString(),DrawingPanel.TOP_LEFT); break;
      case 25 : drawingPanel.setMessage(_value.getString(),DrawingPanel.TOP_RIGHT); break;
      case 26 : drawingPanel.setMessage(_value.getString(),DrawingPanel.BOTTOM_LEFT); break;
      case 27 : drawingPanel.setMessage(_value.getString(),DrawingPanel.BOTTOM_RIGHT); break;
      
      case ALIASING : 
        boolean on = _value.getBoolean();
        drawingPanel.setAntialiasTextOn(on);
        drawingPanel.setAntialiasShapeOn(on);
        break;
        
      case FONT_FACTOR :
        if (fontFactor!=_value.getDouble()) {
          drawingPanel.setFontFactor(fontFactor=_value.getDouble());
        }
        break;
        
      default: super.setValue(_index-DP_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :  autoX = false; updateAutoscale(); break;
      case 1 :  autoY = false; updateAutoscale(); break;
      case 2 : minX=Double.NaN; xminSet = false; updateExtrema(); break;
      case 3 : maxX=Double.NaN; xmaxSet = false; updateExtrema(); break;
      case 4 : minY=Double.NaN; yminSet = false; updateExtrema(); break;
      case 5 : maxY=Double.NaN; ymaxSet = false; updateExtrema(); break;
      case 6 : posValues[0].value = (minX+maxX)/2.0; break; // x
      case 7 : posValues[1].value = (minY+maxY)/2.0; break; // y
      case 8 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction")); break;
      case 9 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));   break;
      case 10 : removeAction (ControlElement.ACTION,getProperty("action"));                break;
      case 11 : drawingPanel.setSquareAspect(false);   break;
      case 12 : drawingPanel.setShowCoordinates(true); break;
      case 13 : drawingPanel.setGutters(0,0,0,0); myGutters = null; break;
      case 14 : strBuilder.setXFormat(new java.text.DecimalFormat("x=0.000;x=-0.000")); break;
      case 15 : strBuilder.setYFormat(new java.text.DecimalFormat("y=0.000;y=-0.000")); break;
      case 16 : removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction")); break;
      case KEY_INDEX : keyPressedValue.value = -1; break;
      case 18 : removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction")); break;
      case 19 : removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction")); break;
      case 20 : strBuilder.setExpression(null); break;
      case 21 : strBuilder.setXFormat(new java.text.DecimalFormat("0.000;-0.000")); break;
      case 22 : drawingPanel.setXMarginPercentage(0.0); break;
      case 23 : drawingPanel.setYMarginPercentage(0.0); break;

      case 24 : drawingPanel.setMessage("",DrawingPanel.TOP_LEFT); break;
      case 25 : drawingPanel.setMessage("",DrawingPanel.TOP_RIGHT); break;
      case 26 : drawingPanel.setMessage("",DrawingPanel.BOTTOM_LEFT); break;
      case 27 : drawingPanel.setMessage("",DrawingPanel.BOTTOM_RIGHT); break;
      case ALIASING : 
        drawingPanel.setAntialiasTextOn(false);
        drawingPanel.setAntialiasShapeOn(false);
        break;
        
      case FONT_FACTOR :
        drawingPanel.setFontFactor(fontFactor=1.0);
        break;

      default: super.setDefaultValue(_index-DP_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : case 1 : return "false";
      case 2 : case 3 : case 4 : case 5 : return "<none>";
      case 6 : case 7 : return "<none>";
      case 8 : case 9 : case 10 : return "<no_action>";
      case 11 : return "false";
      case 12 : return "true";
      case 13 : return "0,0,0,0";
      case 14 : return "x=0.000;x=-0.000";
      case 15 : return "y=0.000;y=-0.000";
      case 16 : return "<no_action>";
      case 17 : return "<none>";
      case 18 : case 19 : return "<no_action>";
      case 20 : return "<none>";
      case 21 : return "0.000;-0.000";
      case 22 : case 23 : return "0.0";
      case 24 : case 25 : case 26 : case 27 : return "<none>";
      case ALIASING : return "false";
      default : return super.getDefaultValueString(_index-DP_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case  2 : case  3 : case  4 : case  5 :
      case 8 : case 9 : case 10 : case 11 : case 12 : case 13 :
      case 14 : case 15 : case 16 :
      case 18 : case 19 : case 20 : case 21 : case 22 :
      case 23 : case 24 :
      case 25 : case 26 : case 27 : case ALIASING :
        return null;
      case 6 : return posValues[0];
      case 7 : return posValues[1];
      case KEY_INDEX : return keyPressedValue;
      default: return super.getValue(_index-DP_ADDED);
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
    drawingPanel.requestFocus();
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


} // End of class
