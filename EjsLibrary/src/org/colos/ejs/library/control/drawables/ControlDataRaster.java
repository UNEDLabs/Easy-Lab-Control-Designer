/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) July 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.ControlDrawablesParent;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display2d.DataRaster;
import org.opensourcephysics.display.Drawable;

public class ControlDataRaster extends ControlDrawable2D implements 
org.colos.ejs.library.control.Resetable,
org.colos.ejs.library.control.DataCollector{
  protected DataRaster raster;
  private double minX, maxX,minY, maxY;
  private double x, y;
  private double[] xArray=null, yArray=null;
  // z is the color index
  private int z;
  private int [] zArray=null;
  private boolean xIsConstant=true, xIsSet=false,
                  yIsConstant=true, yIsSet=false,
                  zIsConstant=true, zIsSet=false;

  protected Drawable createDrawable () {
    raster = new DataRaster (null,-1.0,1.0,-1.0,1.0);
    minX = raster.getXMin(); maxX = raster.getXMax();
    minY = raster.getYMin(); maxY = raster.getYMax();
    x = y = 0.0;
    z = 0;
    xIsConstant = yIsConstant = zIsConstant = true;
    xIsSet = yIsSet = zIsSet = false;
    return raster;
  }

  public void initialize () { // Overwrites default initialize
    raster.clear();
    xIsSet = yIsSet = zIsSet = false;
  }

  public void reset () { // Overwrites default reset
    raster.clear();
    xIsSet = yIsSet = zIsSet = false;
  }

  public void setParent (org.colos.ejs.library.control.swing.ControlParentOfDrawables _dp) {
    if (_dp instanceof ControlDrawablesParent) {
      raster.primaryDrawingPanel = ((ControlDrawablesParent)_dp).getDrawingPanel ();
      raster.primaryDrawingPanel.setPixelScale();
    }
    super.setParent(_dp);
  }

/* This was in EjsDataRaster
  public synchronized BufferedImage render() {
    if (primaryDrawingPanel==null) return null; // I could get a 0 width and height for the image
    return super.render();
  }
*/

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");
      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("index");
      infoList.add ("visible");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))      return "int|double";
    if (_property.equals("maximumX"))      return "int|double";
    if (_property.equals("minimumY"))      return "int|double";
    if (_property.equals("maximumY"))      return "int|double";
    if (_property.equals("x"))             return "int|double[]|double";
    if (_property.equals("y"))             return "int|double[]|double";
    if (_property.equals("index"))         return "int|int[]";
    if (_property.equals("visible"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("x")) {
      xArray = null;
      if (_value==null) xIsConstant = true;
      else {
        Value constant = Value.parseConstantOrArray(_value,true);
        xIsConstant = (constant!=null);
      }
    }
    else if (_property.equals("y")) {
      yArray = null;
      if (_value==null) yIsConstant = true;
      else {
        Value constant = Value.parseConstantOrArray(_value,true);
        yIsConstant = (constant!=null);
      }
    }
    else if (_property.equals("index")) {
      zArray = null;
      if (_value==null) zIsConstant = true;
      else {
        Value constant = Value.parseConstantOrArray(_value,true);
        zIsConstant = (constant!=null);
      }
    }
    return super.setProperty(_property,_value);
  }


  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getDouble()!=minX) raster.setXMin(minX=_value.getDouble()); break;
      case 1 : if (_value.getDouble()!=maxX) raster.setXMax(maxX=_value.getDouble()); break;
      case 2 : if (_value.getDouble()!=minY) raster.setYMin(minY=_value.getDouble()); break;
      case 3 : if (_value.getDouble()!=maxY) raster.setYMax(maxY=_value.getDouble()); break;
      case 4 :
        if (_value.getObject() instanceof double[]) xArray = (double[])_value.getObject();
        else x = _value.getDouble();
        if ((yIsConstant || yIsSet) && (zIsConstant || zIsSet)) appendData();
        else xIsSet = true;
        break;
      case 5 :
        if (_value.getObject() instanceof double[]) yArray = (double[])_value.getObject();
        else y = _value.getDouble();
        if ((xIsConstant || xIsSet) && (zIsConstant || zIsSet)) appendData();
        else yIsSet = true;
        break;
      case 6 :
        if (_value.getObject() instanceof int[]) zArray = (int[])_value.getObject();
        else z = _value.getInteger();
        if ((xIsConstant || xIsSet) && (yIsConstant || yIsSet)) appendData();
        else zIsSet = true;
        break;
      case 7 : raster.setVisible (_value.getBoolean()); break;
      default: super.setValue(_index-8,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : raster.setXMin(minX=-1.0); break;
      case 1 : raster.setXMax(maxX=1.0); break;
      case 2 : raster.setYMin(minY=-1.0); break;
      case 3 : raster.setYMax(maxY=1.0); break;
      case 4 : xIsConstant = true; break;
      case 5 : yIsConstant = true; break;
      case 6 : zIsConstant = true; break;
      case 7 : raster.setVisible (true); break;
      default: super.setDefaultValue(_index-8); break;
    }
  }

  public String getDefaultValueString (int _index) {
     switch (_index) {
       case 0 : return "-1";
       case 1 : return "1";
       case 2 : return "-1";
       case 3 : return "1";
       case 4 :
       case 5 :
       case 6 : return "<none>";
       case 7 : return "true";
       default : return super.getDefaultValueString(_index-8);
     }
   }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 :
      case 3 : case 4 : case 5 :
      case 6 : case 7 :
        return null; // The element does not modify these
      default: return super.getValue(_index-8);
    }
  }


// --------------------------------
// PRIVATE METHODS
// --------------------------------

  private void appendData () {
    if (zArray==null) {
      if (xArray==null) {
        if (yArray==null) raster.append(z,x,y);
        else for (int i=0, n=yArray.length; i<n; i++) raster.append(z, x, yArray[i]);
      }
      else {
        if (yArray==null) for (int i=0, n=xArray.length; i<n; i++) raster.append(z, xArray[i],y);
        else for (int i=0, n=Math.min(xArray.length,yArray.length); i<n; i++) raster.append(z, xArray[i],yArray[i]);
      }
    }
    else {
      if (xArray==null) {
        if (yArray==null) raster.append(zArray[0],x,y); // Absurd!
        else for (int i=0, n=Math.min(zArray.length,yArray.length); i<n; i++) raster.append(zArray[i],x,yArray[i]);
      }
      else {
        if (yArray==null) for (int i=0, n=Math.min(zArray.length,xArray.length); i<n; i++)
                            raster.append(zArray[i],xArray[i],y);
        else for (int i=0, n=Math.min(Math.min(zArray.length,xArray.length),yArray.length); i<n; i++)
               raster.append(zArray[i],xArray[i],yArray[i]);
      }
    }
    xIsSet = yIsSet = zIsSet = false;
  }

}
