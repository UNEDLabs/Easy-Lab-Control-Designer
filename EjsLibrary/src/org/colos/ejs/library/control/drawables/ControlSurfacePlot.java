/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables2D for inclusion in
 * a DrawingPanel
 * Copyright (c) Jan 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.swing.ControlDrawablesParent;
import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.display2d.*;

import java.awt.Color;

import org.opensourcephysics.display.DrawingPanel;

public class ControlSurfacePlot extends ControlDrawable2D {
  protected SurfacePlot plot;
  protected GridPointData pointdata;
  protected boolean auto;
  protected int colormode;
  protected double minZ, maxZ;
  protected Color floorColor, ceilingColor;

  private SurfacePlotMouseController controller;

  protected org.opensourcephysics.display.Drawable createDrawable () {
    plot = new SurfacePlot(pointdata = new GridPointData(30,30,1));
    plot.setAutoscaleZ(auto=true,minZ=0.0,maxZ=1.0);
    plot.setPaletteType(colormode = ColorMapper.SPECTRUM);
    plot.setFloorCeilColor(floorColor = Color.darkGray, ceilingColor=Color.lightGray);
    plot.setShowGridLines(true);
    plot.setGridLineColor(Color.lightGray);
    pointdata.setScale(-1.0,1.0,-1.0,1.0);
    TestData.gaussianScalarField(pointdata);
    plot.update();
    return plot;
  }

  public void setParent (org.colos.ejs.library.control.swing.ControlParentOfDrawables _dp) {
    if (myParent!=null && controller!=null) { // Specific
      myParent.getDrawingPanel().removeMouseListener(controller);
      myParent.getDrawingPanel().removeMouseMotionListener(controller);
      controller = null;
    }
    if (_dp!=null && _dp instanceof ControlDrawablesParent) {
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
      infoList.add ("data");
      infoList.add ("autoscaleZ");
      infoList.add ("minimumZ");
      infoList.add ("maximumZ");
      infoList.add ("colormode");
      infoList.add ("floorcolor");
      infoList.add ("ceilingcolor");
      infoList.add ("showgrid");
      infoList.add ("gridcolor");
      infoList.add ("visible");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))          return "double[][][]";
    if (_property.equals("autoscaleZ"))    return "boolean";
    if (_property.equals("minimumZ"))      return "int|double";
    if (_property.equals("maximumZ"))      return "int|double";
    if (_property.equals("colormode"))     return "PlotMode|int";
    if (_property.equals("floorcolor"))    return "Color|Object";
    if (_property.equals("ceilingcolor"))  return "Color|Object";
    if (_property.equals("showgrid"))      return "boolean";
    if (_property.equals("gridcolor"))     return "Color|Object";
    if (_property.equals("visible"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if (_value.getObject() instanceof double[][][]) {
          double[][][] array = (double[][][]) _value.getObject();
          int nx = array.length, ny = array[0].length;
          if (nx!= pointdata.getNx() || ny!=pointdata.getNy()) pointdata = new GridPointData(nx, ny, 1);
          synchronized (pointdata) {
            pointdata.setData(array);
            plot.setGridData(pointdata);
          }
          plot.update();
        }
        break;
      case 1 : if (auto!=_value.getBoolean()) plot.setAutoscaleZ(auto=_value.getBoolean(),minZ,maxZ); break;
      case 2 : if (_value.getDouble()!=minZ)  plot.setAutoscaleZ(auto,minZ=_value.getDouble(),maxZ);  break;
      case 3 : if (_value.getDouble()!=maxZ)  plot.setAutoscaleZ(auto,minZ,maxZ=_value.getDouble());  break;
      case 4 : if (colormode!=_value.getInteger()) {
          plot.setPaletteType(colormode = _value.getInteger());
          plot.setFloorCeilColor(floorColor, ceilingColor);
        }
        break;
      case 5 :
        if (_value.getObject() instanceof Color && floorColor!=(Color)_value.getObject())
          plot.setFloorCeilColor(floorColor=(Color)_value.getObject(),ceilingColor);
        break;
      case 6 :
        if (_value.getObject() instanceof Color && ceilingColor!=(Color)_value.getObject())
          plot.setFloorCeilColor(floorColor,ceilingColor=(Color)_value.getObject());
        break;
      case 7 : plot.setShowGridLines (_value.getBoolean()); break;
      case 8 :
        if (_value.getObject() instanceof Color) plot.setGridLineColor((Color)_value.getObject());
        break;
      case 9 : plot.setVisible(_value.getBoolean()); break;
      default: super.setValue(_index-10,_value);
    }
    if (isUnderEjs) plot.update();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : TestData.gaussianScalarField(pointdata); break;
      case 1 : plot.setAutoscaleZ(auto=true,minZ,maxZ); break;
      case 2 : plot.setAutoscaleZ(auto,minZ=-1.0,maxZ); break;
      case 3 : plot.setAutoscaleZ(auto,minZ,maxZ=1.0); break;
      case 4 : plot.setPaletteType(colormode=ColorMapper.SPECTRUM);
               plot.setFloorCeilColor(floorColor,ceilingColor); break;
      case 5 : plot.setFloorCeilColor(floorColor=Color.darkGray,ceilingColor); break;
      case 6 : plot.setFloorCeilColor(floorColor,ceilingColor=Color.lightGray); break;
      case 7 : plot.setShowGridLines (true); break;
      case 8 : plot.setGridLineColor(Color.lightGray); break;
      case 9 : plot.setVisible(true); break;
      default: super.setDefaultValue(_index-10); break;
    }
    if (isUnderEjs) plot.update();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "true";
      case 2 : return "-1.0";
      case 3 : return "1.0";
      case 4 : return "SPECTRUM";
      case 5 : return "DARKGRAY";
      case 6 : return "LIGHTGRAY";
      case 7 : return "true";
      case 8 : return "LIGHTGRAY";
      case 9 : return "true";
      default : return super.getDefaultValueString(_index-10);
    }
  }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 :
      case 3 : case 4 : case 5 :
      case 6 : case 7 : case 8 :
      case 9 :
        return null; // The element does not modify these
      default: return super.getValue(_index-10);
    }
  }

}
