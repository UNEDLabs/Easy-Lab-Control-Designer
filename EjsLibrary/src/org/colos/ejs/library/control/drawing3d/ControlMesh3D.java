/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import java.awt.Color;

import org.colos.ejs.library.control.value.*;
import org.colos.freefem.PDEData;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.MeshTools;
import org.opensourcephysics.display2d.ColorMapper;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementMesh;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlMesh3D extends ControlElement3D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int TILE_PROPERTIES_ADDED=19;
  static protected final int LINE_COLOR = ControlElement3D.LINE_COLOR + TILE_PROPERTIES_ADDED;
  static protected final int DRAWING_LINES = ControlElement3D.DRAWING_LINES + TILE_PROPERTIES_ADDED;

  private ElementMesh mMesh;
  private int levels,colormode;
  private double minZ, maxZ;
  private Color floorColor, ceilingColor;
  protected Color[] palette;
  protected double expandedFactor;
  private boolean showLegend, showLegendChanged;
  private javax.swing.JFrame legendFrame;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementMesh"; }

  protected Element createElement () { 
    mMesh = new ElementMesh();
    levels=16;
    colormode = ColorMapper.SPECTRUM;
    minZ=-1.0;
    maxZ=1.0;
    floorColor = Color.darkGray;
    ceilingColor=Color.lightGray;
    palette=new Color[0];
    expandedFactor = 0;
    legendFrame = null;
    showLegend = false;
    showLegendChanged = false;
    return mMesh;
  }

  protected int getPropertiesDisplacement () { return TILE_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("data");
      infoList.add ("field");
      
      infoList.add ("cells");
      infoList.add ("autoscaleZ");
      infoList.add ("minimumZ");
      infoList.add ("maximumZ");
      infoList.add ("levels");
      infoList.add ("expandedZ");
      infoList.add ("symmetricZ");
      
      infoList.add ("colormode");
      infoList.add ("floorcolor");
      infoList.add ("ceilingcolor");
      infoList.add ("showLegend");

      infoList.add("boundary");
      infoList.add("boundaryColors");
      infoList.add("boundaryWidth");
      infoList.add("drawingBoundary");
      infoList.add("boundaryZ");
      infoList.add("length");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))          return "double[][]|PDEData|org.colos.freefem.PDEData|Object";
    if (_property.equals("field"))         return "double[][]|double[][][]";

    if (_property.equals("cells"))          return "int[][]";
    if (_property.equals("autoscaleZ"))    return "boolean";
    if (_property.equals("minimumZ"))      return "int|double";
    if (_property.equals("maximumZ"))      return "int|double";
    if (_property.equals("levels"))        return "int";
    if (_property.equals("expandedZ"))     return "int|double ";
    if (_property.equals("symmetricZ"))    return "boolean";

    if (_property.equals("colormode"))     return "ColorMode|int|Object[]";
    if (_property.equals("floorcolor"))    return "Color|Object";
    if (_property.equals("ceilingcolor"))  return "Color|Object";
    if (_property.equals("showLegend"))    return "boolean";

    if (_property.equals("boundary"))        return "int[][]";
    if (_property.equals("boundaryColors"))  return "Color|Color[]|Object[]";
    if (_property.equals("boundaryWidth"))   return "int|double";
    if (_property.equals("drawingBoundary"))    return "boolean";
    if (_property.equals("boundaryZ"))   return "int|double";
    if (_property.equals("length"))      return "int|double";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ColorMode")>=0 || _propertyType.indexOf("PlotMode")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("spectrum"))     return new IntegerValue (ColorMapper.SPECTRUM);
      if (_value.equals("grayscale"))    return new IntegerValue (ColorMapper.GRAYSCALE);
      if (_value.equals("dualshade"))    return new IntegerValue (ColorMapper.DUALSHADE);
      if (_value.equals("red"))          return new IntegerValue (ColorMapper.RED);
      if (_value.equals("green"))        return new IntegerValue (ColorMapper.GREEN);
      if (_value.equals("blue"))         return new IntegerValue (ColorMapper.BLUE);
      if (_value.equals("black"))        return new IntegerValue (ColorMapper.BLACK);
      if (_value.equals("binary"))       return new IntegerValue (ColorMapper.BLACK);
      if (_value.equals("wireframe"))    return new IntegerValue (ColorMapper.WIREFRAME);
      if (_value.equals("norender"))     return new IntegerValue (ColorMapper.NORENDER);
      if (_value.equals("redblueshade")) return new IntegerValue (ColorMapper.REDBLUE_SHADE);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : 
        if (_value.getObject() instanceof PDEData) MeshTools.show((PDEData) _value.getObject(),mMesh);
        else mMesh.setPoints((double[][])_value.getObject()); 
        break;
      case 1 : 
        if (_value.getObject() instanceof double[][][]) mMesh.setFieldAtCells((double[][][])_value.getObject()); 
        else mMesh.setFieldAtPoints((double[][])_value.getObject()); 
        break;

      case 2 : mMesh.setCells((int[][])_value.getObject()); break;
      case 3 : if (mMesh.getTessellation().isAutoscaleZ()!=_value.getBoolean()) mMesh.getTessellation().setAutoscaleZ(_value.getBoolean(),minZ,maxZ); break;
      case 4 : if (minZ!=_value.getDouble()) mMesh.getTessellation().setAutoscaleZ(mMesh.getTessellation().isAutoscaleZ(),minZ=_value.getDouble(),maxZ);  break;
      case 5 : if (maxZ!=_value.getDouble()) mMesh.getTessellation().setAutoscaleZ(mMesh.getTessellation().isAutoscaleZ(),minZ,maxZ=_value.getDouble());  break;

      case 6:  
        if (levels!=_value.getInteger()  && colormode>=0) {
          mMesh.getTessellation().getDrawer().setColorPalette(ColorMapper.getColorPalette(levels = _value.getInteger(), colormode));
          mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor, ceilingColor);
        }
        break;
      case 7 : 
        if (_value.getDouble()!=expandedFactor) { 
          mMesh.getTessellation().getDrawer().setExpandedZ(true,expandedFactor=_value.getDouble());  
        }
        break;
      case 8 : mMesh.getTessellation().getDrawer().setSymmetricZ(_value.getBoolean()); break;
      
      case 9 :
        if (_value.getObject() instanceof Object[]) {
          colormode = -1;
          Object[] newPalette = (Object[]) _value.getObject();
          if (newPalette.length!=palette.length) palette = new Color[newPalette.length];
          boolean differentPalette = false;
          for (int i=0, n=palette.length; i<n; i++) {
            if (palette[i]!=(Color) newPalette[i]) { palette[i]=(Color) newPalette[i]; differentPalette = true; }
          }
          if (differentPalette) { 
            mMesh.getTessellation().getDrawer().setColorPalette(palette); 
            mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor, ceilingColor); 
          }
        }
        else if (colormode!=_value.getInteger()) {
          mMesh.getTessellation().getDrawer().setPaletteType(colormode = _value.getInteger());
          mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor, ceilingColor);
        }
        break;
      case 10 : 
        if (_value.getObject() instanceof Color && floorColor!=(Color)_value.getObject()) {
          mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor=(Color)_value.getObject(),ceilingColor);
        }
        break;
      case 11 : 
        if (_value.getObject() instanceof Color && ceilingColor!=(Color)_value.getObject()) {
          mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor,ceilingColor=(Color)_value.getObject());
        }
        break;
      case 12 : 
        if (showLegend!=_value.getBoolean()) { 
          showLegend = _value.getBoolean(); 
          showLegendChanged = true;
        }
        break;

      case 13 : mMesh.setBoundary((int[][])_value.getObject()); break;
      case 14 : 
        if (_value.getObject() instanceof Color[]) mMesh.setBoundaryColors((Color[]) _value.getObject());
        else if (_value.getObject() instanceof int[]) {
          int[] colorIndexes = (int[]) _value.getObject(); 
          Color[] colors = new Color[colorIndexes.length];
          for (int i=0; i<colorIndexes.length; i++) colors[i] = DisplayColors.getLineColor(colorIndexes[i]);
          mMesh.setBoundaryColors((Color[]) _value.getObject());
        }
        else if (_value.getObject() instanceof Object[]) mMesh.setBoundaryColors((Color[]) _value.getObject());
        break;
      case 15 : mMesh.getTrail().getStyle().setLineWidth((float) _value.getDouble()); break;
      case 16 : mMesh.getTrail().setVisible(_value.getBoolean()); break;
      case 17 : mMesh.getTrail().setZ(_value.getDouble()); break;
      case 18 : mMesh.setVectorLength(_value.getDouble()); break;

      default : super.setValue(_index-TILE_PROPERTIES_ADDED,_value); break;
      
      case LINE_COLOR :
        if (_value.getObject() instanceof Color) mMesh.getTessellation().getStyle().setLineColor((Color) _value.getObject()); 
        else mMesh.getTessellation().getStyle().setLineColor(DisplayColors.getLineColor(_value.getInteger()));
        super.setValue(_index-TILE_PROPERTIES_ADDED,_value);
        break;
      case DRAWING_LINES :
        mMesh.getTessellation().getStyle().setDrawingLines(_value.getBoolean()); 
        break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : mMesh.setPoints(null); break;
      case 1 : mMesh.setFieldAtPoints(null); break;

      case 2 : mMesh.setCells(null);break;
      case 3 : mMesh.getTessellation().setAutoscaleZ(true,minZ,maxZ); break;
      case 4 : mMesh.getTessellation().setAutoscaleZ(mMesh.getTessellation().isAutoscaleZ(),minZ=-1,maxZ);  break;
      case 5 : mMesh.getTessellation().setAutoscaleZ(mMesh.getTessellation().isAutoscaleZ(),minZ,maxZ=1);  break;

      case 6:  
        if (colormode>=0) {
          mMesh.getTessellation().getDrawer().setColorPalette(ColorMapper.getColorPalette(levels = 16, colormode));
          mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor, ceilingColor);
        }
        break;
      case 7 : mMesh.getTessellation().getDrawer().setExpandedZ(true,expandedFactor=0); break;
      case 8 : mMesh.getTessellation().getDrawer().setSymmetricZ(false); break;
      
      case 9 :
        mMesh.getTessellation().getDrawer().setPaletteType(colormode = ColorMapper.SPECTRUM);
        mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor, ceilingColor);
        break;
      case 10 : mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor=Color.DARK_GRAY,ceilingColor); break;
      case 11 : mMesh.getTessellation().getDrawer().setFloorCeilColor(floorColor,ceilingColor=Color.LIGHT_GRAY); break;
      case 12 : 
        showLegend = false; 
        showLegendChanged = true;
        break;

      case 13 : mMesh.setBoundary(null); break;
      case 14 : mMesh.setBoundaryColors(null); break;
      case 15 : mMesh.getTrail().getStyle().setLineWidth(1); break;
      case 16 : mMesh.getTrail().setVisible(true); break;
      case 17 : mMesh.getTrail().setZ(0); break;
      case 18 : mMesh.setVectorLength(0.1); break;

      default: super.setDefaultValue(_index-TILE_PROPERTIES_ADDED); break;
      
      case LINE_COLOR :
        mMesh.getTessellation().getStyle().setLineColor(defLines); 
        super.setDefaultValue(_index);
        break;
      case DRAWING_LINES :
        mMesh.getTessellation().getStyle().setDrawingLines(true); 
        break;

    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "<none>";

      case 2 : return "<none>";
      case 3: return "true";
      case 4: return "-1";
      case 5: return "1";
     
      case 6: return "16";
      case 7: return "0";
      case 8 : return "false";
     
      case 9  : return "SPECTRUM";
      case 10 : return "DARKGRAY";
      case 11 : return "LIGHTGRAY";
      case 12 : return "true";

      case 13 : return "<none>";
      case 14 : return "<none>";
      case 15 : return "1";
      case 16 : return "true";
      case 17 : return "0";
      case 18 : return "0.1";

      default : return super.getDefaultValueString(_index-TILE_PROPERTIES_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : 
      case 4 : case 5 : case 6 : case 7 : 
      case 8 : case 9 : case 10: case 11: 
      case 12: case 13: case 14: case 15:
      case 16: case 17 : case 18 :
        return null;
      default: return super.getValue (_index-TILE_PROPERTIES_ADDED);
    }
  }

  public void preupdate () {
    if (showLegendChanged) {
      if (showLegend) {
        if (legendFrame==null) legendFrame = mMesh.getTessellation().getDrawer().showLegend();
        legendFrame.setVisible(true);
      }
      else {
        if (legendFrame!=null) legendFrame.setVisible(false);
      }
      showLegendChanged = false;
    }
  }
  
} // End of class
