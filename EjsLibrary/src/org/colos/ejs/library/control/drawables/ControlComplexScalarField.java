/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables2D for inclusion in
 * a DrawingPanel
 * Copyright (c) Jan 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import java.awt.Color;
import javax.swing.*;

import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display2d.Plot2D;
import org.opensourcephysics.display2d.ArrayData;
import org.opensourcephysics.display2d.ComplexGridPlot;
import org.opensourcephysics.display2d.ComplexContourPlot;
import org.opensourcephysics.display2d.ComplexSurfacePlot;
import org.opensourcephysics.display2d.ComplexInterpolatedPlot;
import org.opensourcephysics.display2d.GridTableFrame;
import org.opensourcephysics.display2d.ColorMapper;
import org.opensourcephysics.display2d.SurfacePlotMouseController;

public class ControlComplexScalarField extends ControlDrawable2D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static private final int SCALAR_FIELD_ADDED = 18;

  static protected final int X_MINIMUM=0;
  static protected final int X_MAXIMUM=1;
  static protected final int Y_MINIMUM=2;
  static protected final int Y_MAXIMUM=3;
  static protected final int DATA=4;

  protected Plot2D plot;
  protected Plot2DWrapper wrapper;
  protected javax.swing.JFrame legendFrame;
  protected ArrayData pointdata;

  protected boolean auto;
  protected int levels,colormode;
  protected double minX, maxX, minY, maxY;
  protected double minZ, maxZ;
  protected Color floorColor, ceilingColor;
  protected int plotType;
  protected double minAbcise = Double.NaN, maxAbcise = Double.NaN;
  protected double minOrdinate = Double.NaN, maxOrdinate = Double.NaN;

  protected boolean mustUpdate,typeChanged,showGridLines,visibility,legendChanged, showLegend;
  protected double[][][] dataArray;
  protected Color[] palette=new Color[0];
  protected Color gridColor;
  protected double expandedFactor;

  private SurfacePlotMouseController controller;

  protected org.opensourcephysics.display.Drawable createDrawable () {
    pointdata = new ArrayData(30,30,3);
    pointdata.setComponentName(0, "magnitude");
    pointdata.setComponentName(1, "real");
    pointdata.setComponentName(2, "imaginary");

    minX = -1.0; maxX = 1.0; minY = -1.0; maxY = 1.0;
    pointdata.setCellScale(minX, maxX, minY, maxY);
    dataArray = null;
    plot = new ComplexGridPlot(pointdata);
    legendFrame = null;
    showLegend = legendChanged = false;
    plot.setAutoscaleZ(auto=true,minZ=-1.0,maxZ=1.0);
    plot.setPaletteType(colormode = ColorMapper.SPECTRUM);
    plot.setColorPalette(ColorMapper.getColorPalette (levels=16,colormode));
    plot.setFloorCeilColor(floorColor = Color.darkGray, ceilingColor=Color.lightGray);
    plot.setShowGridLines(showGridLines=true);
    plot.setGridLineColor(gridColor = Color.lightGray);
    plot.setExpandedZ(true,0.0);
    plot.setVisible(visibility=true);
    plotType=Plot2D.GRID_PLOT;
    expandedFactor = 0;
    mustUpdate=true;
    typeChanged=false;
    wrapper = new Plot2DWrapper();
    wrapper.setPlot2D(plot);
    preupdate();
    return wrapper;
  }

  // A particular feature of SurfacePlot, which requires a listener to be rotated with the mouse
  public void setParent (org.colos.ejs.library.control.swing.ControlParentOfDrawables _dp) {
    if (myParent!=null && controller!=null) { // Specific
      myParent.getDrawingPanel().removeMouseListener(controller);
      myParent.getDrawingPanel().removeMouseMotionListener(controller);
      controller = null;
    }
    if (_dp!=null && _dp instanceof ControlDrawablesParent && plot instanceof ComplexSurfacePlot) {
      DrawingPanel panel = ((ControlDrawablesParent)_dp).getDrawingPanel();
      controller = new SurfacePlotMouseController (panel,plot);
      panel.addMouseListener (controller);
      panel.addMouseMotionListener (controller);
    }
    super.setParent(_dp);
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
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))      return "int|double ";
    if (_property.equals("maximumX"))      return "int|double ";
    if (_property.equals("minimumY"))      return "int|double ";
    if (_property.equals("maximumY"))      return "int|double ";
    if (_property.equals("z"))             return "double[][][]";
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
    if (_property.equals("expandedZ"))      return "int|double ";
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
      case 4 : if (_value.getObject() instanceof double[][][]) {
          dataArray = (double[][][]) _value.getObject();
          mustUpdate = true;
          legendChanged = auto;
        }
        break;
      case 5 : if (auto!=_value.getBoolean()) { plot.setAutoscaleZ(auto=_value.getBoolean(),minZ,maxZ); legendChanged = true; } break;
      case 6 : if (minZ!=_value.getDouble())  { plot.setAutoscaleZ(auto,minZ=_value.getDouble(),maxZ);  legendChanged = true; } break;
      case 7 : if (maxZ!=_value.getDouble())  { plot.setAutoscaleZ(auto,minZ,maxZ=_value.getDouble());  legendChanged = true; } break;
      case 8 :
        if (plot instanceof ComplexContourPlot) {
          if (levels!=_value.getInteger()) {
            ((ComplexContourPlot)plot).getContour().setNumberOfLevels(levels = _value.getInteger());
            if (colormode>=0) {
              plot.setColorPalette(ColorMapper.getColorPalette(levels, colormode));
              plot.setFloorCeilColor(floorColor, ceilingColor);
            }
            legendChanged = true;
          }
        }
        else if (_value.getInteger()!=levels && colormode>=0) {
          plot.setColorPalette(ColorMapper.getColorPalette(levels = _value.getInteger(), colormode));
          plot.setFloorCeilColor(floorColor, ceilingColor);
          legendChanged = true;
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
          legendChanged = true;
        }
        else if (colormode!=_value.getInteger()) {
          plot.setPaletteType(colormode = _value.getInteger());
          plot.setFloorCeilColor(floorColor, ceilingColor);
          legendChanged = true;
        }
        break;
      case 10 : if (_value.getObject() instanceof Color && floorColor!=(Color)_value.getObject())
        plot.setFloorCeilColor(floorColor=(Color)_value.getObject(),ceilingColor);
        legendChanged = true;
        break;
      case 11 : if (_value.getObject() instanceof Color && ceilingColor!=(Color)_value.getObject())
        plot.setFloorCeilColor(floorColor,ceilingColor=(Color)_value.getObject());
        legendChanged = true;
        break;
      case 12 : plot.setShowGridLines (showGridLines=_value.getBoolean()); break;
      case 13 : if (_value.getObject() instanceof Color) plot.setGridLineColor(gridColor=(Color)_value.getObject()); break;
      case 14 : plot.setVisible(visibility=_value.getBoolean()); break;
      case 15 : if (_value.getInteger()!=plotType) { plotType = _value.getInteger(); typeChanged = true; } break;
      case 16 : if (showLegend!=_value.getBoolean()) { showLegend = _value.getBoolean(); legendChanged = true; }
                break;
      case 17 : if (_value.getDouble()!=expandedFactor) { plot.setExpandedZ(true,expandedFactor=_value.getDouble());  legendChanged = true; }
                      break;
      default: super.setValue(_index-SCALAR_FIELD_ADDED,_value);
    }
    if (isUnderEjs) plot.update();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : minX = -1.0; mustUpdate = true; break;
      case 1 : maxX =  1.0; mustUpdate = true; break;
      case 2 : minY = -1.0; mustUpdate = true; break;
      case 3 : maxY =  1.0; mustUpdate = true; break;
      case 4  :
        plot.setGridData(pointdata = new ArrayData(30,30,3));
        dataArray = null;
        mustUpdate = true;
        legendChanged = true;
        break;
    case 5  : plot.setAutoscaleZ(auto=true,minZ,maxZ); legendChanged = true; break;
      case 6  : plot.setAutoscaleZ(auto,minZ=-1.0,maxZ); legendChanged = true; break;
      case 7  : plot.setAutoscaleZ(auto,minZ,maxZ=1.0);  legendChanged = true; break;
      case 8  :
        levels = 16;
        if (plot instanceof ComplexContourPlot) ((ComplexContourPlot)plot).getContour().setNumberOfLevels(levels);
        else {
          if (colormode>=0) {
            plot.setColorPalette(ColorMapper.getColorPalette(levels,colormode));
            plot.setFloorCeilColor(floorColor, ceilingColor);
          }
        }
        legendChanged = true;
        break;
      case 9 :
        plot.setPaletteType(colormode=ColorMapper.SPECTRUM);
        plot.setFloorCeilColor(floorColor,ceilingColor);
        legendChanged = true;
        break;
      case 10 : plot.setFloorCeilColor(floorColor=Color.darkGray,ceilingColor); legendChanged = true; break;
      case 11 : plot.setFloorCeilColor(floorColor,ceilingColor=Color.lightGray); legendChanged = true; break;
      case 12 : plot.setShowGridLines (showGridLines=true); break;
      case 13 : plot.setGridLineColor(gridColor=Color.lightGray); break;
      case 14 : plot.setVisible(visibility=true); break;
      case 15 : plotType = Plot2D.GRID_PLOT; typeChanged = true; break;
      case 16 : showLegend = false; legendChanged = true; break;
      case 17 : plot.setExpandedZ(true,expandedFactor=0); legendChanged = true; break;
      default: super.setDefaultValue(_index-SCALAR_FIELD_ADDED); break;
    }
    if (isUnderEjs) plot.update();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "-1";
      case 1 : return "1";
      case 2 : return "-1";
      case 3 : return "1";
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
      case 15 : case 16 : case 17 :
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
      minAbcise = minX;
      maxAbcise = maxX;
      minOrdinate = minY;
      maxOrdinate = maxY;
      commonPreupdate();
  }


  public void commonPreupdate () {
    /// controller is a SurfacePlotMouseController for surface plots.
    if (typeChanged) {
      DrawingPanel panel = myParent.getDrawingPanel();
      if (controller!=null) { // remove SurfacePlotMouseController
        panel.removeMouseListener(controller);
        panel.removeMouseMotionListener(controller);
      }
      panel.setShowCoordinates(true);
      switch (plotType) {
        case Plot2D.INTERPOLATED_PLOT : plot = new ComplexInterpolatedPlot(pointdata); break;
        case Plot2D.CONTOUR_PLOT      : {
          plot = new ComplexContourPlot(pointdata);
          ((ComplexContourPlot)plot).getContour().setNumberOfLevels(levels);
        }
        break;
        case Plot2D.SURFACE_PLOT      : {
          plot = new ComplexSurfacePlot(pointdata);
          controller = new SurfacePlotMouseController (panel,plot);
          panel.addMouseListener (controller);
          panel.addMouseMotionListener (controller);
        }
        panel.setShowCoordinates(false);  // added
        break;
        default : plot = new ComplexGridPlot(pointdata); break;
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
      legendChanged = true;
      typeChanged = false;
      mustUpdate = true;
    }
    if (mustUpdate) {
      if (dataArray==null) { // Still demo data
        if (pointdata.getLeft()  !=minAbcise || pointdata.getRight()!=maxAbcise ||
            pointdata.getBottom()!=maxOrdinate || pointdata.getTop()  !=minOrdinate) {
          if (plot instanceof ComplexGridPlot) pointdata.setCellScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          else pointdata.setScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
        }
        double[][][] data = pointdata.getData();
        int nx = data[0].length, ny = data[0][0].length;
        for(int i = 0; i<nx; i++) {
          double x = minAbcise + i*(maxAbcise-minAbcise)/(nx-1);
          for(int j = 0; j<ny; j++) {
            double y = minOrdinate + j*(maxOrdinate-minOrdinate)/(ny-1);
            double r=Math.sqrt(x*x+y*y);
            data[0][i][j]=(r==0)?1:Math.exp(-r*r)*y/r; // real component
            data[1][i][j]=(r==0)?0:Math.exp(-r*r)*x/r; // complex component
            data[2][i][j]=Math.sqrt(data[0][i][j]*data[0][i][j]+data[1][i][j]*data[1][i][j]);
          }
        }
      }
      else { // Real data
        int nx = dataArray[0].length, ny = dataArray[0][0].length;
        if (pointdata.getNx()!=nx || pointdata.getNy()!=ny) {
          pointdata = new ArrayData(nx, ny, 3);
          if (plot instanceof ComplexGridPlot) pointdata.setCellScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          else pointdata.setScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          setData(dataArray);
          plot.setGridData(pointdata);
        }
        else {
          if (pointdata.getLeft()  !=minAbcise || pointdata.getRight()!=maxAbcise ||
              pointdata.getBottom()!=maxOrdinate || pointdata.getTop()  !=minOrdinate) {
            if (plot instanceof ComplexGridPlot) pointdata.setCellScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
            else pointdata.setScale(minAbcise, maxAbcise, maxOrdinate, minOrdinate);
          }
          setData(dataArray);
        }
      }
      mustUpdate = false;
    }
    if (legendChanged) {
     if (showLegend) {
//       System.out.println("Showing legend for "+plot.getClass().getName()+" with color mode = "+colormode);
       if (legendFrame!=null) legendFrame.setVisible(false);
       legendFrame = plot.showLegend();
     }
     else {
       if (legendFrame!=null) { legendFrame.setVisible(false); legendFrame = null; }
     }
    }
    //System.out.println("Updating the plot");
    plot.update();
  }

  /**
   * Sets the complex field's values.
   *
   * vals[0][][] is assumed to contain the real components of the field.
   * vals[1][][] is assumed to contain the imaginary components of the field.
   *
   * @param vals double[][][] complex field values
   */
  public void setData(double[][][] vals) {
    double[][][] data = pointdata.getData();
    double[][] mag = data[0];
    double[][] reData = data[1];
    double[][] imData = data[2];
    // current grid has correct size
    int ny = vals[0][0].length;
    for(int i = 0, nx = vals[0].length;i<nx;i++) {
      System.arraycopy(vals[0][i], 0, reData[i], 0, ny);
      System.arraycopy(vals[1][i], 0, imData[i], 0, ny);
      for(int j = 0;j<ny;j++) {
        mag[i][j] = Math.sqrt(vals[0][i][j]*vals[0][i][j]+vals[1][i][j]*vals[1][i][j]);
      }
    }
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
