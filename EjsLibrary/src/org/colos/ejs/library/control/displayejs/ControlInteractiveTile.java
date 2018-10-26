/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.AbstractInteractiveTile;
import java.awt.Paint;

public abstract class ControlInteractiveTile extends ControlInteractiveElement {
  static final public int PROPERTIES_ADDED=7;

  protected double colorLevels[]=null;
  protected Paint colorFills[]=null;
  protected double colorOrigin[] = new double[]{0.0,0.0,0.0};
  protected double colorDirection[] = new double[]{1.0,0.0,0.0};

  public ControlInteractiveTile () { super (); enabledEjsEdit = true; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("displacementFactor");
      infoList.add ("colorOrigin");
      infoList.add ("colorDirection");
      infoList.add ("colorLevels");
      infoList.add ("colorFills");
      infoList.add ("belowWhenEqual");
      infoList.add ("interiorTransparency");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("displacementFactor")) return "int|double";
    if (_property.equals("colorOrigin"))        return "double[]";
    if (_property.equals("colorDirection"))     return "double[]";
    if (_property.equals("colorLevels"))        return "double[]";
    if (_property.equals("colorFills"))         return "Object[]";
    if (_property.equals("belowWhenEqual"))     return "boolean";
    if (_property.equals("interiorTransparency"))     return "int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  0 : ((AbstractInteractiveTile) myElement).setDisplacementFactor(_value.getDouble()); break;
      case  1 :
        if (_value.getObject() instanceof double[]) {
            double val[] = (double[]) _value.getObject();
            if (val.length>2) {
              ((AbstractInteractiveTile) myElement).setColorOriginAndDirection(colorOrigin[0]=val[0],colorOrigin[1]=val[1],colorOrigin[2]=val[2],
                  colorDirection[0],colorDirection[1],colorDirection[2]);
            }
        }
        break;
      case  2 :
        if (_value.getObject() instanceof double[]) {
          double val[] = (double[]) _value.getObject();
          if (val.length>2) {
            ((AbstractInteractiveTile) myElement).setColorOriginAndDirection(colorOrigin[0],colorOrigin[1],colorOrigin[2],
                colorDirection[0]=val[0],colorDirection[1]=val[1],colorDirection[2]=val[2]);
          }
        }
        break;
      case  3 :
        if (_value.getObject() instanceof double[]) {
          colorLevels = (double[]) _value.getObject();
          if (colorFills!=null && colorFills.length==(colorLevels.length+1)) {
            ((AbstractInteractiveTile) myElement).setColorRegions(colorLevels,colorFills);
          }
        }
        break;
      case  4 :
        if (_value.getObject() instanceof Object[]) {
          Object[] colorObjects = (Object[]) _value.getObject();
          colorFills = new Paint[colorObjects.length];
          for (int i=0; i<colorObjects.length; i++) colorFills[i] = (Paint) colorObjects[i];
          if (colorLevels!=null && colorLevels.length==(colorFills.length-1)) {
            ((AbstractInteractiveTile) myElement).setColorRegions(colorLevels,colorFills);
          }
        }
        break;
      case  5 : ((AbstractInteractiveTile) myElement).setColorBelowWhenEqual(_value.getBoolean()); break;
      case  6 : quickInterior(_value.getInteger()); break;

      default: super.setValue(_index-7,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 : ((AbstractInteractiveTile) myElement).setDisplacementFactor(1.0); break;
      case  1 :
        colorOrigin = new double[] {0.0,0.0,0.0};
        ((AbstractInteractiveTile) myElement).setColorOriginAndDirection(colorOrigin[0],colorOrigin[1],colorOrigin[2],
            colorDirection[0],colorDirection[1],colorDirection[2]);
        break;
      case  2 :
        colorDirection = new double[] {1.0,0.0,0.0};
        ((AbstractInteractiveTile) myElement).setColorOriginAndDirection(colorOrigin[0],colorOrigin[1],colorOrigin[2],
            colorDirection[0],colorDirection[1],colorDirection[2]);
        break;
      case  3 : ((AbstractInteractiveTile) myElement).setColorRegions(colorLevels=null,null); break;
      case  4 : ((AbstractInteractiveTile) myElement).setColorRegions(null,colorFills=null); break;
      case  5 : ((AbstractInteractiveTile) myElement).setColorBelowWhenEqual(true); break;
      case  6 : quickInterior(-1); break;

      default: super.setDefaultValue(_index-7); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 : return "1";
      case  1 : return "{0.0,0.0,0.0}";
      case  2 : return "{1.0,0.0,0.0}";
      case  3 :
      case  4 : return "<none>";
      case  5 : return "true";
      case  6 : return "-1";
      default : return super.getDefaultValueString(_index-7);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 : case 5 : case 6 : return null;
      default: return getValue (_index-7);
    }
  }

  protected void quickInterior (int _trans) {
    if (_trans<0 || _trans>255) ((AbstractInteractiveTile) myElement).setDrawQuickInterior(false,0);
    else ((AbstractInteractiveTile) myElement).setDrawQuickInterior(true,_trans);
  }

} // End of interface
