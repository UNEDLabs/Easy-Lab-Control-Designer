/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) July 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display2d.CellLattice;
import org.opensourcephysics.display2d.ColorMapper;
import org.opensourcephysics.display.Drawable;
import java.awt.Color;

public class ControlCellLattice extends ControlDrawable2D {
  private CellLattice lattice;
  private double minX, maxX,minY, maxY;
  private int nx, ny, numColors, paletteType;
  private Color[] palette=new Color[1];

  protected Drawable createDrawable () {
      lattice = new CellLattice(30,30);
      lattice.setShowGridLines (true);
      lattice.setGridLineColor (Color.lightGray);
    nx = lattice.getNx();
    ny = lattice.getNy();
    lattice.setMinMax(minX=-1.0,maxX=1.0,minY=-1.0,maxY=1.0);
    lattice.randomize();
    setColorMode (numColors=256,paletteType=ColorMapper.SPECTRUM);
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
      infoList.add ("numcolors");
      infoList.add ("colormode");
      infoList.add ("showgrid");
      infoList.add ("gridcolor");
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
    if (_property.equals("numcolors"))     return "int";
    if (_property.equals("colormode"))     return "int|ColorMode|Object[]";
    if (_property.equals("showgrid"))      return "boolean";
    if (_property.equals("gridcolor"))     return "Color|Object";
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
          if (data.length!=nx || data[0].length!=ny)
            lattice.resizeLattice(nx=data.length,ny=data[0].length);
          lattice.setBlock(0,0,data);
        }
        break;
      case 5 : if (numColors!=_value.getInteger()) setColorMode(numColors=_value.getInteger(),paletteType); break;
      case 6 :
          if (_value.getObject() instanceof Object[]) {
            paletteType = -1;
            Object[] newPalette = (Object[]) _value.getObject();
            if (newPalette.length!=palette.length) palette = new Color[newPalette.length];
            // System.out.println("Setting color palette to a Color[] array of size "+palette.length);
            boolean differentPalette = false;
            for (int i=0, n=palette.length; i<n; i++) {
              if (palette[i]!=(Color) newPalette[i]) { palette[i]=(Color) newPalette[i]; differentPalette = true; }
            }
            if (differentPalette) lattice.setColorPalette(palette);
          }
          else if (paletteType!=_value.getInteger()) setColorMode(numColors,paletteType=_value.getInteger()); break;
      case 7 : lattice.setShowGridLines (_value.getBoolean()); break;
      case 8 :
        if (_value.getObject() instanceof Color) lattice.setGridLineColor((Color)_value.getObject());
        break;
      case 9 : lattice.setVisible (_value.getBoolean()); break;
      default: super.setValue(_index-10,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : lattice.setXMin(minX=-1.0); break;
      case 1 : lattice.setXMax(maxX=1.0); break;
      case 2 : lattice.setYMin(minY=-1.0); break;
      case 3 : lattice.setYMax(maxY=1.0); break;
      case 4 : lattice.randomize(); break;
      case 5 : setColorMode(numColors=256,paletteType); break;
      case 6 : setColorMode(numColors,paletteType=ColorMapper.SPECTRUM); break;
      case 7 : lattice.setShowGridLines(true); break;
      case 8 : lattice.setGridLineColor (Color.lightGray); break;
      case 9 : lattice.setVisible (true); break;
      default: super.setDefaultValue(_index-10); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "-1";
      case 1 : return "1";
      case 2 : return "-1";
      case 3 : return "1";
      case 4 : return "<none>";
      case 5 : return "256";
      case 6 : return "SPECTRUM";
      case 7 : return "true";
      case 8 : return "LIGHTGRAY";
      case 9 : return "true";
      default : return super.getDefaultValueString(_index-10);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 : case 8 : case 9 :
        return null;
      default: return super.getValue(_index-8);
    }
  }

  // Private methods
  protected void setColorMode (int _n, int _palette) {
    if (paletteType<0) return;
    if (_n>256) _n = 256;
    Color[] colors = ColorMapper.getColorPalette(_n,_palette);
    if (colors==null) lattice.createDefaultColors();
    else {
      colors[0] = new java.awt.Color(0,0,0,0);
      lattice.setColorPalette(colors);
    }
  }


}
