/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables2D for inclusion in
 * a DrawingPanel
 * Copyright (c) Dec 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.opensourcephysics.display2d.*;
import javax.swing.JFrame;
import java.awt.Color;
import org.opensourcephysics.display.DrawingPanel;
import java.awt.Graphics;

/**
 * A wrapper for a plot2D that changes in run-time
 */

public class Plot2DWrapper implements Plot2D {

  Plot2D plot;

  void setPlot2D (Plot2D _plot) { this.plot = _plot; }

// --------------------------------------
// Implementation of Plot2D
// --------------------------------------
  public void setAll(Object val) { plot.setAll(val); }
  public void setAll(Object obj, double xmin, double xmax, double ymin, double ymax) { plot.setAll(obj,xmin,xmax,ymin,ymax); }
  public void setGridData(GridData _griddata) { plot.setGridData(_griddata); }
  public GridData getGridData() { return plot.getGridData(); }
  public double indexToX(int i) { return plot.indexToX(i); }
  public double indexToY(int i) { return plot.indexToY(i); }
  public int xToIndex(double x) { return plot.xToIndex(x); }
  public int yToIndex(double y) { return plot.yToIndex(y); }
  public boolean isAutoscaleZ() { return plot.isAutoscaleZ(); }
  public double getFloor() { return plot.getFloor(); }
  public double getCeiling()  { return plot.getCeiling(); }
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) { plot.setAutoscaleZ(isAutoscale,floor,ceil); }
  public void setFloorCeilColor(Color floorColor, Color ceilColor) { plot.setFloorCeilColor(floorColor,ceilColor); }
  public void setColorPalette(Color[] colors) { plot.setColorPalette(colors); }
  public void setPaletteType(int type) { plot.setPaletteType(type); }
  public void setGridLineColor(Color c) { plot.setGridLineColor(c); }
  public void setShowGridLines(boolean showGrid) { plot.setShowGridLines(showGrid); }
  public JFrame showLegend() { return plot.showLegend(); }
  public void setVisible(boolean isVisible) { plot.setVisible(isVisible); }
  public void setIndexes(int[] indexes) { plot.setIndexes(indexes); }
  public void update() { plot.update(); }
  public void setExpandedZ (boolean expanded, double factor) { plot.setExpandedZ(expanded,factor); }
  public void setSymmetricZ(boolean symmetric) { plot.setSymmetricZ(symmetric); }
  public boolean isSymmetricZ() { return plot.isSymmetricZ(); }

// --------------------------------------
// Implementation of Measurable
// --------------------------------------

  public double getXMin() { return plot.getXMin(); }
  public double getXMax() { return plot.getXMax(); }
  public double getYMin() { return plot.getYMin(); }
  public double getYMax() { return plot.getYMax(); }
  public boolean isMeasured() { return plot.isMeasured(); }

// --------------------------------------
// Implementation of Drawable
// --------------------------------------

  public void draw(DrawingPanel panel, Graphics g) { plot.draw(panel,g); }

}
