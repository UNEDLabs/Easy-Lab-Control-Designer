/*
 * The control.automaticcontrol package contains subclasses of
 * control.ControlElement that are sueful for Automatic Control
 * Copyright (c) December 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.automaticcontrol;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.displayejs.ControlDrawingPanel3D;
import org.colos.ejs.library.control.displayejs.ControlGroupDrawable;
import org.colos.ejs.library.control.displayejs.ControlInteractiveElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.automaticcontrol.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.displayejs.*;
import java.awt.BasicStroke;

/**
 * An interactive particle
 */
public class ControlTank extends ControlGroupDrawable implements InteractionListener {
  static protected final int TANK_ADDED = 14;

  protected Tank tank;
  private double lineWidth=Double.NaN;
  private Value[] magnitudesValue;

  public ControlTank () {
    super ();
    magnitudesValue = new Value[7];
    magnitudesValue[0] = new DoubleValue(tank.getHeight());
    magnitudesValue[1] = new DoubleValue(tank.getWidth());
    magnitudesValue[2] = new DoubleValue(tank.getLevel());
    magnitudesValue[3] = new ObjectValue(tank.getProfile());
    magnitudesValue[4] = posValues[0];
    magnitudesValue[5] = posValues[1];
    magnitudesValue[6] = posValues[2];
    tank.addListener(this);
  }

  protected Drawable createDrawable () {
      tank = new Tank();
      tank.setEnabled(true);
      tank.setMovable(true);
      tank.setResizable(true);
      tank.setProfilable(true);
      tank.setShowProfiles(false);
    return tank;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("height");
      infoList.add ("width");
      infoList.add ("level");
      infoList.add ("closedOnTop");
      infoList.add ("lineColor");
      infoList.add ("stroke");
      infoList.add ("fillColor");
      infoList.add ("enabled");
      infoList.add ("resizable");
      infoList.add ("profilable");
      infoList.add ("profileColor");
      infoList.add ("profile");
      infoList.add ("movable");
      infoList.add ("showProfiles");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }


  public String getPropertyCommonName(String _property) {
    if (_property.equals("movable"))     return "enabledPosition";
    if (_property.equals("resizable"))   return "enabledSize";
    if (_property.equals("closedOnTop"))   return "closedTop";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("height"))    return "int|double";
    if (_property.equals("width"))     return "int|double";
    if (_property.equals("level"))     return "int|double";
    if (_property.equals("closedOnTop")) return "boolean";
    if (_property.equals("lineColor")) return "Color|Object";
    if (_property.equals("stroke"))    return "int|double|Object";
    if (_property.equals("fillColor")) return "Color|Object";
    if (_property.equals("enabled")) return "boolean";
    if (_property.equals("resizable")) return "boolean";
    if (_property.equals("profilable")) return "boolean";
    if (_property.equals("profileColor")) return "Color|Object";
    if (_property.equals("profile")) return "double[]";
    if (_property.equals("movable")) return "boolean";
    if (_property.equals("showProfiles")) return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public Object getObject (String _name) {
    if (_name.equals("diameter")) return tank.diameterFunction;
    return null;
  }

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : tank.setHeight(_value.getDouble()); break;
      case 1 : tank.setWidth(_value.getDouble()); break;
      case 2 : tank.setLevel(_value.getDouble()); break;
      case 3 : tank.setClosedOnTop(_value.getBoolean()); break;
      case 4 : tank.setLineColor((java.awt.Color)_value.getObject()); break;
      case 5 :
        if (_value.getObject() instanceof java.awt.Stroke) tank.setLineStroke((java.awt.Stroke) _value.getObject());
        else if (lineWidth!=_value.getDouble()) {
          lineWidth = _value.getDouble();
          if (lineWidth<0) tank.setLineStroke(new java.awt.BasicStroke((float) -lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0) );
          else tank.setLineStroke(new java.awt.BasicStroke((float) lineWidth));
        }
        break;
      case 6 : tank.setFillColor((java.awt.Color)_value.getObject()); break;
      case 7 : tank.setEnabled(_value.getBoolean()); break;

      case 8 : tank.setResizable(_value.getBoolean()); break;
      case 9 : tank.setProfilable(_value.getBoolean()); break;
      case 10 : tank.setProfileColor((java.awt.Color)_value.getObject()); break;
      case 11 : if (_value.getObject() instanceof double[]) tank.setProfile((double[]) _value.getObject()); break;
      case 12 : tank.setMovable(_value.getBoolean()); break;
      case 13 : tank.setShowProfiles(_value.getBoolean()); break;
      default: super.setValue(_index-TANK_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : tank.setHeight(0.5); break;
      case 1 : tank.setWidth(0.2); break;
      case 2 : tank.setLevel(0.0); break;
      case 3 : tank.setClosedOnTop(false); break;
      case 4 : tank.setLineColor(java.awt.Color.BLACK); break;
      case 5 : tank.setLineStroke(new java.awt.BasicStroke()); break;
      case 6 : tank.setFillColor(java.awt.Color.BLUE); break;
      case 7 : tank.setEnabled(true); break;
      case 8 :  tank.setResizable(true); break;
      case 9 : tank.setProfilable(true); break;
      case 10 : tank.setProfileColor(java.awt.Color.GRAY); break;
      case 11 : tank.setProfile(null); break;
      case 12 : tank.setMovable(true); break;
      case 13 : tank.setShowProfiles(false); break;
      default: super.setDefaultValue(_index-TANK_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0.5";
      case 1 : return "0.2";
      case 2 : return "0";
      case 3 : return "false";
      case 4 : return "BLACK";
      case 5 : return "1";
      case 6 : return "BLUE";
      case 7 : return "true";
      case 8 : return "true";
      case 9 : return "true";
      case 10 : return "GRAY";
      case 11 : return "<none>";
      case 12 : return "true";
      case 13 : return "false";
      default : return super.getDefaultValueString(_index-TANK_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return magnitudesValue[0];
      case 1 : return magnitudesValue[1];
      case 2 : return magnitudesValue[2];
      case 11 : return magnitudesValue[3];
      case 3 : case 4 : case 5 : case 6 :
      case 7 : case 8 : case 9 : case 10:
      case 12 : case 13 :
        return null;
      default: return super.getValue (_index-TANK_ADDED);
    }
  }

  static private final int[] magnitudesIndex = {0,1,2,11,
      TANK_ADDED+ControlGroupDrawable.POSITION_X,
      TANK_ADDED+ControlGroupDrawable.POSITION_Y,
      TANK_ADDED+ControlGroupDrawable.POSITION_Z};

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
        ((DoubleValue)magnitudesValue[0]).value = tank.getHeight();
        ((DoubleValue)magnitudesValue[1]).value = tank.getWidth();
        ((DoubleValue)magnitudesValue[2]).value = tank.getLevel();
        ((ObjectValue)magnitudesValue[3]).value = tank.getProfile();
        ((DoubleValue)magnitudesValue[4]).value = tank.getX();
        ((DoubleValue)magnitudesValue[5]).value = tank.getY();
        ((DoubleValue)magnitudesValue[6]).value = tank.getZ();
        variablesChanged (magnitudesIndex,magnitudesValue);
        if (isUnderEjs) setFieldListValues (magnitudesIndex,magnitudesValue);
        break;
      case InteractionEvent.MOUSE_RELEASED :
        if (myParent instanceof ControlDrawingPanel3D) ((ControlDrawingPanel3D) myParent).setSelectedDrawable (null);
        invokeActions (ControlElement.ACTION);
        break;
    }
  } // End of interaction method



} // End of class
