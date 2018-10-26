/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display2d.TriangularBinaryLattice;
import org.opensourcephysics.display.Drawable;
import java.awt.Color;

public class ControlTriangularBinaryLattice extends ControlDrawable2D {
  static private final int TRIANGULAR_BINARY_LATTICE_ADDED = 8;
  private TriangularBinaryLattice lattice;
  private Color[] palette;
  private double minX, maxX,minY, maxY;
  private int nx, ny;

  protected Drawable createDrawable () {
    nx = 30;
    ny = 30;
    lattice = new TriangularBinaryLattice(nx,ny);
    lattice.setMinMax(minX=-1.0,maxX=1.0,minY=-1.0,maxY=1.0);
    lattice.randomize();
    palette = new Color[] { Color.red, Color.blue };
    lattice.setColorPalette(palette);
    return lattice;
  }

// ------------------------------------------------
// Properties
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
      infoList.add ("deadcolor");
      infoList.add ("color");
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
    if (_property.equals("data"))          return "int[][]";
    if (_property.equals("color"))         return "Color|Object";
    if (_property.equals("deadcolor"))     return "Color|Object";
    if (_property.equals("visible"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getDouble()!=minX) lattice.setMinMax(minX=_value.getDouble(),maxX,minY,maxY); break;
      case 1 : if (_value.getDouble()!=maxX) lattice.setMinMax(minX,maxX=_value.getDouble(),minY,maxY); break;
      case 2 : if (_value.getDouble()!=minY) lattice.setMinMax(minX,maxX,minY=_value.getDouble(),maxY); break;
      case 3 : if (_value.getDouble()!=maxY) lattice.setMinMax(minX,maxX,minY,maxY=_value.getDouble()); break;
      case 4 :
        if (_value.getObject() instanceof int[][]) {
          int [][] data = (int[][])_value.getObject();
          if (data.length!=nx || data[0].length!=ny) {
            lattice.resizeLattice(nx=data.length,ny=data[0].length);
            lattice.setMinMax(minX,maxX,minY,maxY);
          }
          lattice.setBlock(0,0,data);
        }
        break;
      case 5 :
        if (_value.getObject() instanceof Color && palette[0] != (Color) _value.getObject()) {
          palette[0] = (Color) _value.getObject();
          lattice.setColorPalette(palette);
        }
        break;
      case 6 :
        if (_value.getObject() instanceof Color && palette[1] != (Color) _value.getObject()) {
          palette[1] = (Color) _value.getObject();
          lattice.setColorPalette(palette);
        }
        break;
      case 7 : lattice.setVisible(_value.getBoolean()); break;
      default: super.setValue(_index-TRIANGULAR_BINARY_LATTICE_ADDED,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : lattice.setMinMax(minX=-1.0,maxX,minY,maxY); break;
      case 1 : lattice.setMinMax(minX,maxX=1.0,minY,maxY); break;
      case 2 : lattice.setMinMax(minX,maxX,minY=-1.0,maxY); break;
      case 3 : lattice.setMinMax(minX,maxX,minY,maxY=1.0); break;
      case 4 : lattice.randomize(); break;
      case 5 : palette[0] = Color.red; lattice.setColorPalette(palette); break;
      case 6 : palette[1] = Color.blue; lattice.setColorPalette(palette); break;
      case 7 : lattice.setVisible(true); break;
      default: super.setDefaultValue(_index-TRIANGULAR_BINARY_LATTICE_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "-1";
      case 1 : return "1";
      case 2 : return "-1";
      case 3 : return "1";
      case 4 : return "<none>";
      case 5 : return "RED";
      case 6 : return "BLUE";
      case 7 : return "true";
      default : return super.getDefaultValueString(_index-TRIANGULAR_BINARY_LATTICE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 :
        return null;
      default: return super.getValue(_index-TRIANGULAR_BINARY_LATTICE_ADDED);
    }
  }

}
