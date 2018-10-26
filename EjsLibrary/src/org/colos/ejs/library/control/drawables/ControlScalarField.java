/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables2D for inclusion in
 * a DrawingPanel
 * Copyright (c) Jan 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display2d.*;

import java.awt.Color;
import javax.swing.*;

public class ControlScalarField extends ControlDrawable2D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static private final int SCALAR_FIELD_ADDED = 19;

  static protected final int X_MINIMUM=0;
  static protected final int X_MAXIMUM=1;
  static protected final int Y_MINIMUM=2;
  static protected final int Y_MAXIMUM=3;
  static protected final int DATA=4;

  private Plot2D plot;
  private Plot2DWrapper wrapper;
  private javax.swing.JFrame legendFrame;
  private GridPointData pointdata;

  private boolean auto;
  private int levels,colormode;
  protected double minX, maxX, minY, maxY;
  private double minZ, maxZ;
  private Color floorColor, ceilingColor;
  protected int plotType;

  protected double minAbcise, maxAbcise, minOrdinate, maxOrdinate;
  protected boolean mustUpdate,typeChanged,showGridLines,visibility;
  private boolean showLegend, showLegendChanged;
  protected double[][] dataArray;
  protected Color[] palette;
  protected Color gridColor;
  protected double expandedFactor;

  private SurfacePlotMouseController controller;

  @Override
  protected org.opensourcephysics.display.Drawable createDrawable () {
    plotType=Plot2D.GRID_PLOT;
    return doCreateTheDrawable();
  }

  final protected org.opensourcephysics.display.Drawable doCreateTheDrawable () {
    minX = maxX = minY = maxY = Double.NaN;
    dataArray = null;
    legendFrame = null;
    showLegend = false;
    showLegendChanged = false;
    auto=true;
    minZ=-1.0;
    maxZ=1.0;
    colormode = ColorMapper.SPECTRUM;
    levels=16;
    palette=new Color[0];
    floorColor = Color.darkGray;
    ceilingColor=Color.lightGray;
    showGridLines=true;
    gridColor = Color.lightGray;
    expandedFactor = 0.0;
    visibility=true;
    expandedFactor = 0;
    pointdata = new GridPointData(30,30,1);
    pointdata.setCellScale(-1, 1, -1, 1); // This is a common default in EJS panels
    wrapper = new Plot2DWrapper();
    mustUpdate=true;
    typeChanged=true;
    checkPlotType();
    return wrapper;
  }

  // A particular feature of SurfacePlot, which requires a listener to be rotated with the mouse
  public void setParent (org.colos.ejs.library.control.swing.ControlParentOfDrawables _dp) {
    if (myParent!=null && controller!=null) { // Specific
      myParent.getDrawingPanel().removeMouseListener(controller);
      myParent.getDrawingPanel().removeMouseMotionListener(controller);
      controller = null;
    }
    if (_dp!=null && _dp instanceof ControlDrawablesParent && plot instanceof SurfacePlot) {
      DrawingPanel panel = ((ControlDrawablesParent)_dp).getDrawingPanel();
      controller = new SurfacePlotMouseController (panel,plot);
      panel.addMouseListener (controller);
      panel.addMouseMotionListener (controller);
    }
    super.setParent(_dp);
    if (Double.isNaN(minX) || Double.isNaN(maxX) || Double.isNaN(minY) || Double.isNaN(maxY)) {
      mustUpdate = true;
      preupdate();
    }
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
      infoList.add ("z");

      infoList.add ("autoscaleZ");
      infoList.add ("minimumZ");
      infoList.add ("maximumZ");

      infoList.add ("levels");
      infoList.add ("colormode");
      infoList.add ("floorcolor");
      infoList.add ("ceilingcolor");
      infoList.add ("showgrid");
      infoList.add ("gridcolor");
      infoList.add ("visible");
      infoList.add ("plotType");
      infoList.add ("showLegend");
      infoList.add ("expandedZ");
      infoList.add ("symmetricZ");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))      return "int|double ";
    if (_property.equals("maximumX"))      return "int|double ";
    if (_property.equals("minimumY"))      return "int|double ";
    if (_property.equals("maximumY"))      return "int|double ";
    if (_property.equals("z"))             return "double[][]";
    if (_property.equals("autoscaleZ"))    return "boolean";
    if (_property.equals("minimumZ"))      return "int|double";
    if (_property.equals("maximumZ"))      return "int|double";

    if (_property.equals("levels"))        return "int";
    if (_property.equals("colormode"))     return "ColorMode|int|Object[]";
    if (_property.equals("floorcolor"))    return "Color|Object";
    if (_property.equals("ceilingcolor"))  return "Color|Object";
    if (_property.equals("showgrid"))      return "boolean";
    if (_property.equals("gridcolor"))     return "Color|Object";
    if (_property.equals("visible"))       return "boolean";
    if (_property.equals("plotType"))      return "Plot2DType|int";
    if (_property.equals("showLegend"))    return "boolean";
    if (_property.equals("expandedZ"))     return "int|double ";
    if (_property.equals("symmetricZ"))    return "boolean";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("Plot2DType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("grid"))         return new IntegerValue (Plot2D.GRID_PLOT);
      if (_value.equals("interpolated")) return new IntegerValue (Plot2D.INTERPOLATED_PLOT);
      if (_value.equals("contour"))      return new IntegerValue (Plot2D.CONTOUR_PLOT);
      if (_value.equals("surface"))      return new IntegerValue (Plot2D.SURFACE_PLOT);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (minX!=_value.getDouble()) { minX = _value.getDouble(); mustUpdate = true; } break;
      case 1 : if (maxX!=_value.getDouble()) { maxX = _value.getDouble(); mustUpdate = true; } break;
      case 2 : if (minY!=_value.getDouble()) { minY = _value.getDouble(); mustUpdate = true; } break;
      case 3 : if (maxY!=_value.getDouble()) { maxY = _value.getDouble(); mustUpdate = true; } break;
      case 4 : if (_value.getObject() instanceof double[][]) {
          dataArray = (double[][]) _value.getObject();
          double min = Double.POSITIVE_INFINITY;
          double max = Double.NEGATIVE_INFINITY;
          for (int i=0; i<dataArray.length; i++) for (int j=0; j<dataArray[0].length; j++) {
            min = Math.min(min, dataArray[i][j]);
            max = Math.max(max, dataArray[i][j]);
          }
//          System.out.println ("Data Extrema ="+min+","+max);
          mustUpdate = true;
        }
        break;
      case 5 : if (auto!=_value.getBoolean()) plot.setAutoscaleZ(auto=_value.getBoolean(),minZ,maxZ); break;
      case 6 : if (minZ!=_value.getDouble())  plot.setAutoscaleZ(auto,minZ=_value.getDouble(),maxZ);  break;
      case 7 : if (maxZ!=_value.getDouble())  plot.setAutoscaleZ(auto,minZ,maxZ=_value.getDouble());  break;
      case 8 :
        if (plot instanceof ContourPlot) {
          if (levels!=_value.getInteger()) {
            ((ContourPlot)plot).setNumberOfLevels(levels = _value.getInteger());
            if (colormode>=0) {
              plot.setColorPalette(ColorMapper.getColorPalette(levels, colormode));
              plot.setFloorCeilColor(floorColor, ceilingColor);
            }
          }
        }
        else if (_value.getInteger()!=levels && colormode>=0) {
          plot.setColorPalette(ColorMapper.getColorPalette(levels = _value.getInteger(), colormode));
          plot.setFloorCeilColor(floorColor, ceilingColor);
        }
        break;
      case 9 :
        if (_value.getObject() instanceof Object[]) {
          colormode = -1;
          Object[] newPalette = (Object[]) _value.getObject();
          if (newPalette.length!=palette.length) palette = new Color[newPalette.length];
          boolean differentPalette = false;
          for (int i=0, n=palette.length; i<n; i++) {
            if (palette[i]!=(Color) newPalette[i]) { palette[i]=(Color) newPalette[i]; differentPalette = true; }
          }
          if (differentPalette) { plot.setColorPalette(palette); plot.setFloorCeilColor(floorColor, ceilingColor); }
        }
        else if (colormode!=_value.getInteger()) {
          plot.setPaletteType(colormode = _value.getInteger());
          plot.setFloorCeilColor(floorColor, ceilingColor);
        }
        break;
      case 10 : 
        if (_value.getObject() instanceof Color && floorColor!=(Color)_value.getObject()) {
          plot.setFloorCeilColor(floorColor=(Color)_value.getObject(),ceilingColor);
        }
        break;
      case 11 : 
        if (_value.getObject() instanceof Color && ceilingColor!=(Color)_value.getObject()) {
          plot.setFloorCeilColor(floorColor,ceilingColor=(Color)_value.getObject());
        }
        break;
      case 12 : plot.setShowGridLines (showGridLines=_value.getBoolean()); break;
      case 13 : if (_value.getObject() instanceof Color) plot.setGridLineColor(gridColor=(Color)_value.getObject()); break;
      case 14 : plot.setVisible(visibility=_value.getBoolean()); break;
      case 15 : if (_value.getInteger()!=plotType) { plotType = _value.getInteger(); typeChanged = true; } break;
      case 16 : 
        if (showLegend!=_value.getBoolean()) { 
          showLegend = _value.getBoolean(); 
          showLegendChanged = true;
        }
        break;
      case 17 : 
        if (_value.getDouble()!=expandedFactor) { 
          plot.setExpandedZ(true,expandedFactor=_value.getDouble());  
        }
        break;
      case 18 : plot.setSymmetricZ(_value.getBoolean()); break;
      default: super.setValue(_index-SCALAR_FIELD_ADDED,_value);
    }
    if (isUnderEjs) plot.update();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : minX = Double.NaN; mustUpdate = true; break;
      case 1 : maxX = Double.NaN; mustUpdate = true; break;
      case 2 : minY = Double.NaN; mustUpdate = true; break;
      case 3 : maxY = Double.NaN; mustUpdate = true; break;
      case 4  :
        plot.setGridData(pointdata = new GridPointData(30,30,1));
        dataArray = null;
        mustUpdate = true;
        break;
      case 5  : plot.setAutoscaleZ(auto=true,minZ,maxZ); break;
      case 6  : plot.setAutoscaleZ(auto,minZ=-1.0,maxZ); break;
      case 7  : plot.setAutoscaleZ(auto,minZ,maxZ=1.0);  break;
      case 8  :
        levels = 16;
        if (plot instanceof ContourPlot) ((ContourPlot)plot).setNumberOfLevels(levels);
        else {
          if (colormode>=0) {
            plot.setColorPalette(ColorMapper.getColorPalette(levels,colormode));
            plot.setFloorCeilColor(floorColor, ceilingColor);
          }
        }
        break;
      case 9 :
        plot.setPaletteType(colormode=ColorMapper.SPECTRUM);
        plot.setFloorCeilColor(floorColor,ceilingColor);
        break;
      case 10 : plot.setFloorCeilColor(floorColor=Color.darkGray,ceilingColor);  break;
      case 11 : plot.setFloorCeilColor(floorColor,ceilingColor=Color.lightGray); break;
      case 12 : plot.setShowGridLines (showGridLines=true); break;
      case 13 : plot.setGridLineColor(gridColor=Color.lightGray); break;
      case 14 : plot.setVisible(visibility=true); break;
      case 15 : plotType = Plot2D.GRID_PLOT; typeChanged = true; break;
      case 16 : showLegend = false; showLegendChanged = true; break;
      case 17 : plot.setExpandedZ(true,expandedFactor=0); break;
      case 18 : plot.setSymmetricZ(false); break;
      default: super.setDefaultValue(_index-SCALAR_FIELD_ADDED); break;
    }
    if (isUnderEjs) preupdate();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : 
      case 1 : 
      case 2 : 
      case 3 : return "<none>";
      case 4 : return "<none>";
      case 5  : return "true";
      case 6  : return "-1.0";
      case 7  : return "1.0";
      case 8  : return "16";
      case 9 : return "SPECTRUM";
      case 10 : return "DARKGRAY";
      case 11 : return "LIGHTGRAY";
      case 12 : return "true";
      case 13 : return "LIGHTGRAY";
      case 14 : return "true";
      case 15 : return "GRID_PLOT";
      case 16 : return "false";
      case 17 : return "0";
      case 18 : return "false";
      default : return super.getDefaultValueString(_index-SCALAR_FIELD_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 :
      case 3 : case 4 : case 5 :
      case 6 : case 7 : case 8 :
      case 9 : case 10 : case 11 :
      case 12 : case 13 : case 14 :
      case 15 : case 16 : case 17 : case 18 :
        return null; // The element does not modify these
      default: return super.getValue(_index-SCALAR_FIELD_ADDED);
    }
  }

  public void destroy () {
    if (legendFrame!=null) legendFrame.setVisible(false);
    super.destroy();
  }

  // ---------------------------------------
  // Updating the data
  // ---------------------------------------

  public void preupdate () {
    if (!visibility) return;
    if (myParent==null) return;
    computeMinMax();
    commonPreupdate();
  }

  final protected void computeMinMax() {
    minAbcise = minX; maxAbcise = maxX;
    minOrdinate = minY; maxOrdinate = maxY;
    if (myParent!=null) {
      DrawingPanel panel = myParent.getDrawingPanel();
      if (Double.isNaN(minAbcise)) minAbcise = panel.getPreferredXMin();
      if (Double.isNaN(maxAbcise)) maxAbcise = panel.getPreferredXMax();
      if (Double.isNaN(minOrdinate)) minOrdinate = panel.getPreferredYMin();
      if (Double.isNaN(maxOrdinate)) maxOrdinate = panel.getPreferredYMax();
    }
  }
  
  private void checkPlotType() {
    if (typeChanged) {
      switch (plotType) {
        case Plot2D.INTERPOLATED_PLOT : plot = new InterpolatedPlot(pointdata); break;
        case Plot2D.CONTOUR_PLOT      : 
          plot = new ContourPlot(pointdata);
          ((ContourPlot)plot).setNumberOfLevels(levels);
          break;
        case Plot2D.SURFACE_PLOT      : 
          plot = new SurfacePlot(pointdata);
          if (myParent!=null) {
            DrawingPanel panel = myParent.getDrawingPanel();
            if (controller!=null) { // Specific
              panel.removeMouseListener(controller);
              panel.removeMouseMotionListener(controller);
            }
            controller = new SurfacePlotMouseController (myParent.getDrawingPanel(),plot);
            panel.addMouseListener (controller);
            panel.addMouseMotionListener (controller);
          }
          break;
        default : plot = new GridPlot(pointdata); break;
      }
      // Re-customize the plot
      plot.setAutoscaleZ(auto,minZ,maxZ);
      if (palette.length>0) plot.setColorPalette(palette);
      else plot.setColorPalette(ColorMapper.getColorPalette(levels, colormode));
      plot.setFloorCeilColor(floorColor, ceilingColor);
      plot.setShowGridLines (showGridLines);
      plot.setGridLineColor(gridColor);
      plot.setExpandedZ(true,expandedFactor);
      plot.setVisible(visibility);
      wrapper.setPlot2D(plot);
      showLegendChanged = true;
      if (legendFrame!=null) legendFrame.setVisible(false);
      legendFrame = null;
      typeChanged = false;
      mustUpdate = true;
      if (myParent!=null) myParent.getDrawingPanel().invalidateImage();
//      System.out.println("Type changed to "+plotType);
//      System.out.println("Extrema ="+minZ+","+maxZ);
    }
  }
  
  protected void commonPreupdate () {
    checkPlotType();
//    System.out.println ("Must update = "+mustUpdate);
    if (mustUpdate) {
      if (dataArray==null) { // Still demo data
        if (pointdata.getLeft()  !=minAbcise || pointdata.getRight()!=maxAbcise ||
            pointdata.getBottom()!=maxOrdinate || pointdata.getTop()  !=minOrdinate) {
          if (plot instanceof GridPlot) pointdata.setCellScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          else pointdata.setScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
        }
        double[][][] data = pointdata.getData();
        for(int i = 0, mx = data.length;i<mx;i++) {
          for(int j = 0, my = data[0].length;j<my;j++) {
            data[i][j][2] = TestData.gaussian(data[i][j][0], data[i][j][1], 0.1);
          }
        }
        //TestData.gaussianScalarField(pointdata);
      }
      else { // Real data
        int nx = dataArray.length, ny = dataArray[0].length;
        if (pointdata.getNx()!=nx || pointdata.getNy()!=ny) {
          pointdata = new GridPointData(nx, ny, 1);
          if (plot instanceof GridPlot) pointdata.setCellScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          else pointdata.setScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          double[][][] data = pointdata.getData();
          for (int i=0; i<nx; i++) for (int j=0; j<ny; j++) data[i][j][2] = dataArray[i][j];
          plot.setGridData(pointdata);
        }
        else {
          if (pointdata.getLeft()  !=minAbcise || pointdata.getRight()!=maxAbcise ||
              pointdata.getBottom()!=maxOrdinate || pointdata.getTop()  !=minOrdinate) {
            if (plot instanceof GridPlot) pointdata.setCellScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
            else pointdata.setScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          }
          double[][][] data = pointdata.getData();
          for (int i=0; i<nx; i++) for (int j=0; j<ny; j++) data[i][j][2] = dataArray[i][j];
        }
      }
      mustUpdate = false;
    }
    if (showLegendChanged) {
      if (showLegend) {
        if (legendFrame==null) legendFrame = plot.showLegend();
        legendFrame.setVisible(true);
      }
      else {
        if (legendFrame!=null) legendFrame.setVisible(false);
      }
      showLegendChanged = false;
    }
//    System.out.println("Updating the plot");
    plot.update();
  }

  public void addMenuEntries () {
    if (getMenuNameEntry()==null) return;
    java.util.List<Object> list = new java.util.ArrayList<Object> ();
    list.add(new AbstractAction(org.colos.ejs.library.Simulation.getEjsString("InteractiveTrace.ShowDataTable")){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(java.awt.event.ActionEvent e) { showDataTable(true); }
    });
    getSimulation().addElementMenuEntries (getMenuNameEntry(), list);
  }

  private GridTableFrame tableFrame=null;

  public synchronized void showDataTable(boolean show) {
     if(show) {
        if(tableFrame==null || !tableFrame.isDisplayable()) {
           if(plot.getGridData()==null) {
              return;
           }
           tableFrame = new GridTableFrame(plot.getGridData());
           tableFrame.setTitle(this.getProperty("name")+ " Data");
           tableFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        tableFrame.refreshTable();
        tableFrame.setVisible(true);
     } else {
        tableFrame.setVisible(false);
        tableFrame.dispose();
        tableFrame = null;
     }
   }
}
