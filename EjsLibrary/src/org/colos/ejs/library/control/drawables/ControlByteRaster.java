/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) July 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display2d.ByteRaster;
import org.opensourcephysics.display.Drawable;
import java.awt.Color;

public class ControlByteRaster extends ControlDrawable2D {
  static private final int BYTE_RASTER_ADDED = 8;
  
  protected ByteRaster raster;
  private double minX, maxX,minY, maxY;
  private int nx, ny;
  private Color[] palette=new Color[1];

  public String getServerClassname () { return "org.colos.ejs.library.server.drawables.ByteRaster"; }
  
  protected Drawable createDrawable () {
    raster = new ByteRaster(300,300);
    nx = raster.getNx();
    ny = raster.getNy();
    raster.setMinMax(minX=-1.0,maxX=1.0,minY=-1.0,maxY=1.0);
    raster.randomize();
    return raster;
  }

  @Override
  public ControlElement setProperty(String _property, String _value, boolean _storeIt) {
    _property = _property.trim();
//    if (_property.equals("_ejs_")) raster.setUnderEjs(true);
    return super.setProperty(_property, _value, _storeIt);
  }
  
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
      infoList.add ("data");
      infoList.add ("visible");
      infoList.add ("colorpalette");
      infoList.add ("allowRescale");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))      return "int|double";
    if (_property.equals("maximumX"))      return "int|double";
    if (_property.equals("minimumY"))      return "int|double";
    if (_property.equals("maximumY"))      return "int|double";
    if (_property.equals("data"))          return "int[][]|byte[][]";
    if (_property.equals("visible"))       return "boolean";
    if (_property.equals("colorpalette"))  return "Object[]";
    if (_property.equals("allowRescale"))       return "boolean";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getDouble()!=minX) raster.setXMin(minX=_value.getDouble()); break;
      case 1 : if (_value.getDouble()!=maxX) raster.setXMax(maxX=_value.getDouble()); break;
      case 2 : if (_value.getDouble()!=minY) raster.setYMin(minY=_value.getDouble()); break;
      case 3 : if (_value.getDouble()!=maxY) raster.setYMax(maxY=_value.getDouble()); break;
      case 4 :
        if (_value.getObject() instanceof byte[][]) {
          byte [][] data = (byte[][])_value.getObject();
          if (data.length!=nx || data[0].length!=ny) raster.resizeRaster(nx=data.length,ny=data[0].length);
          raster.setBlock(0,0,data);
        }
        else if (_value.getObject() instanceof int[][]) {
          int [][] data = (int[][])_value.getObject();
          if (data.length!=nx || data[0].length!=ny) raster.resizeRaster(nx=data.length,ny=data[0].length);
          raster.setBlock(0,0,data);
        }
        break;
      case 5 : raster.setVisible(_value.getBoolean()); break;
      case 6 :
          if (_value.getObject() instanceof Object[]) {
            Object[] newPalette = (Object[]) _value.getObject();
            if (newPalette.length!=palette.length) palette = new Color[newPalette.length];
            boolean differentPalette = false;
            for (int i=0, n=palette.length; i<n; i++) {
              if (palette[i]!=(Color) newPalette[i]) { palette[i]=(Color) newPalette[i]; differentPalette = true; }
            }
            if (differentPalette) raster.setColorPalette(palette);
          }
          break;
      case 7 : raster.setAllowRescale(_value.getBoolean()); break;
       
      default: super.setValue(_index-BYTE_RASTER_ADDED,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : raster.setXMin(minX=-1.0); break;
      case 1 : raster.setXMax(maxX=1.0); break;
      case 2 : raster.setYMin(minY=-1.0); break;
      case 3 : raster.setYMax(maxY=1.0); break;
      case 4 : raster.randomize(); break;
      case 5 : raster.setVisible(true); break;
      case 6 : raster.createDefaultColors(); break;
      case 7 : raster.setAllowRescale(false); break;

      default: super.setDefaultValue(_index-BYTE_RASTER_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "-1";
      case 1 : return "1";
      case 2 : return "-1";
      case 3 : return "1";
      case 4 : return "<none>";
      case 5 : return "true";
      case 6 : return "<none>";
      case 7 : return "false";

      default : return super.getDefaultValueString(_index-BYTE_RASTER_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 :
      case 3 : case 4 : case 5 :
      case 6 : case 7 :
        return null; // The element does not modify these
      default: return super.getValue(_index-BYTE_RASTER_ADDED);
    }
  }

}
