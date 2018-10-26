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

public abstract class ControlGroupDrawable extends ControlDrawable3D {
  static protected final int GROUPDRAWABLE_ADDED= 12;

  static protected final int POSITION_X = 0;
  static protected final int POSITION_Y = 1;
  static protected final int POSITION_Z = 2;
  static protected final int SIZE_X = 3;
  static protected final int SIZE_Y = 4;
  static protected final int SIZE_Z = 5;
  static protected final int VISIBLE = 6;

  protected GroupDrawable myGroupDrawable;
  protected DoubleValue[] posValues;
  protected DoubleValue[] sizeValues;

  public ControlGroupDrawable () {
    super ();
    myGroupDrawable = (GroupDrawable) getDrawable();
    posValues = new DoubleValue[] { new DoubleValue(myGroupDrawable.getX()),
                                    new DoubleValue(myGroupDrawable.getY()),
                                    new DoubleValue(myGroupDrawable.getZ()) };
    sizeValues = new DoubleValue[] { new DoubleValue(myGroupDrawable.getSizeX()),
                                     new DoubleValue(myGroupDrawable.getSizeY()),
                                     new DoubleValue(myGroupDrawable.getSizeZ()) };
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

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

      infoList.add ("pressaction");
      infoList.add ("dragaction");
      infoList.add ("action");
      infoList.add ("enteredAction");
      infoList.add ("exitedAction");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("pressaction")) return "pressAction";
    if (_property.equals("dragaction"))  return "dragAction";
    if (_property.equals("action"))      return "releaseAction";
    if (_property.equals("sizex")) return "sizeX";
    if (_property.equals("sizey")) return "sizeY";
    if (_property.equals("sizez")) return "sizeZ";
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

    if (_property.equals("action"))      return "Action CONSTANT";
    if (_property.equals("pressaction")) return "Action CONSTANT";
    if (_property.equals("dragaction"))  return "Action CONSTANT";
    if (_property.equals("enteredAction"))  return "Action CONSTANT";
    if (_property.equals("exitedAction"))  return "Action CONSTANT";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  POSITION_X : myGroupDrawable.setX(posValues[0].value=_value.getDouble());  break;
      case  POSITION_Y : myGroupDrawable.setY(posValues[1].value=_value.getDouble());  break;
      case  POSITION_Z : myGroupDrawable.setZ(posValues[2].value=_value.getDouble());  break;
      case  SIZE_X : myGroupDrawable.setSizeX(sizeValues[0].value=_value.getDouble()); break;
      case  SIZE_Y : myGroupDrawable.setSizeY(sizeValues[1].value=_value.getDouble()); break;
      case  SIZE_Z : myGroupDrawable.setSizeZ(sizeValues[2].value=_value.getDouble()); break;
      case  VISIBLE : myGroupDrawable.setVisible(_value.getBoolean()); break;
      case 7 : // pressaction
        removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlInteractiveElement.ACTION_PRESS,_value.getString());
        return;
      case 8 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        return;
      case 9 : // releaseaction
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        return;
      case 10 : // enteredAction
        removeAction (ControlInteractiveElement.ACTION_ENTERED,getProperty("enteredAction"));
        addAction(ControlInteractiveElement.ACTION_ENTERED,_value.getString());
        return;
      case 11 : // exitedAction
        removeAction (ControlInteractiveElement.ACTION_EXITED,getProperty("exitedAction"));
        addAction(ControlInteractiveElement.ACTION_EXITED,_value.getString());
        return;

      default: super.setValue(_index-GROUPDRAWABLE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  POSITION_X : myGroupDrawable.setX(posValues[0].value=0.0);  break;
      case  POSITION_Y : myGroupDrawable.setY(posValues[1].value=0.0);  break;
      case  POSITION_Z : myGroupDrawable.setZ(posValues[2].value=0.0);  break;
      case  SIZE_X : myGroupDrawable.setSizeX(sizeValues[0].value=1.0); break;
      case  SIZE_Y : myGroupDrawable.setSizeY(sizeValues[1].value=1.0); break;
      case  SIZE_Z : myGroupDrawable.setSizeZ(sizeValues[2].value=1.0); break;
      case  VISIBLE : myGroupDrawable.setVisible(true); break;
      case 7 : removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction")); return;
      case 8 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));         return;
      case 9 : removeAction (ControlElement.ACTION,getProperty("action"));                       return;
      case 10 : removeAction (ControlInteractiveElement.ACTION_ENTERED,getProperty("enteredAction")); return;
      case 11 : removeAction (ControlInteractiveElement.ACTION_EXITED,getProperty("exitedAction")); return;

      default: super.setDefaultValue(_index-GROUPDRAWABLE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  POSITION_X :
      case  POSITION_Y :
      case  POSITION_Z : return "0.0";
      case  SIZE_X :
      case  SIZE_Y :
      case  SIZE_Z : return "1";
      case  VISIBLE : return "true";
      case 7 :
      case 8 :
      case 9 :
      case 10 :
      case 11 : return "<no_action>";
      default : return super.getDefaultValueString(_index-GROUPDRAWABLE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case POSITION_X : return posValues[0];
      case POSITION_Y : return posValues[1];
      case POSITION_Z : return posValues[2];
      case SIZE_X : return sizeValues[0];
      case SIZE_Y : return sizeValues[1];
      case SIZE_Z : return sizeValues[2];
      default: return null; // Doesn't inherit
    }
  }


} // End of interface
