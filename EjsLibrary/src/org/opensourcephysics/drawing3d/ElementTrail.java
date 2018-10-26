/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DisplayColors;

/**
 * <p>Title: ElementTrail</p>
 * <p>Description: A single (and simple) trail of 3D points</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementTrail extends Element implements org.opensourcephysics.display.Data {
  /**
   * The next point will not be connected to the previous one
   */
  static public final int NO_CONNECTION = 0;
  /**
   * The next point will be connected to the previous one by a segment
   */
  static public final int LINE_CONNECTION = 1;

  // Configuration variables
  private int maximum = 0;
  private String[] inputLabels = new String[] {"x", "y", "z"}; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

  // Implementation variables
  private TrailPoint ghostPoint = null;
  private java.util.List<TrailPoint> list = new ArrayList<TrailPoint>(); // The list of points
  private java.util.List<TrailPoint> displayPoints = new ArrayList<TrailPoint>(); // The list of points plus (possibly) the ghostpoint


  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementTrail(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementTrail(this);
    }
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Sets the maximum number of points for the trail.
   * Once the maximum is reached, adding a new point will cause
   * remotion of the first one. This is useful to keep trails
   * down to a reasonable size, since very long trails can slow
   * down the rendering (in certain implementations).
   * If the value is 0 (the default) the trail grows forever
   * without discarding old points.
   * @param maximum int
   */
  public void setMaximumPoints(int maximum) { this.maximum = maximum; }

  
  /**
   * Clears all points from the trail.
   */
  public synchronized void clear() {
    synchronized(list) { list.clear(); }
    synchronized(displayPoints) { displayPoints.clear(); }
    ghostPoint = null;
    addChange(Element.CHANGE_SHAPE);
  }

  public void setLabels(String[] _labels) {
    for (int i=0; i<_labels.length; i++) inputLabels[i] = _labels[i];
  }
  
  public void setXLabel(String _label) { inputLabels[0] = _label; }

  public void setYLabel(String _label) { inputLabels[1] = _label; }

  public void setZLabel(String _label) { inputLabels[2] = _label; }

  
  /**
   * Adds a new point to the trace
   * @param _x double The X coordinate of the new point
   * @param _y double The Y coordinate of the new point
   * @param _z double The Z coordinate of the new point
   * @param _type int The type of connection. One of:
   * <ul>
   *   <li>LINE_CONNECTION : for a connected trail point</li>
   *   <li>NO_CONNECTION : for a disconected trail point</li>
   * </ul>
   */
  public void addPoint(double _x, double _y, double _z, int _type) {
    TrailPoint point = new TrailPoint(_x, _y, _z, _type);
    synchronized(list) {
      if (maximum>0 && list.size()>=maximum) list.remove(0);
      list.add(point);
    }
    addChange(Element.CHANGE_SHAPE);
  }

  public void addPoint(double _x, double _y, double _z) { addPoint(_x,_y,_z,LINE_CONNECTION); }
  
  public void addPoint(double[] _point) { addPoint(_point[0],_point[1],_point[2],LINE_CONNECTION); }

  public void addPoint(TrailPoint trailPoint) {
    TrailPoint point = new TrailPoint(trailPoint);
    synchronized(list) {
      if (maximum>0 && list.size()>=maximum) list.remove(0);
      list.add(point);
    }
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Sets a temporary point used only for display, but that is not
   * meant to stay in the trail
   * @param _point
   */
  public void setGhostPoint(TrailPoint _point) {
    ghostPoint = _point;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Closes the trail by adding the ghost point, if not null
   */
  public void closeTrail(int _type) {
    if (ghostPoint!=null) {
      ghostPoint.type = _type;
      addPoint(ghostPoint);
    }
  }
  
  // -------------------------------------
  // Parent methods overwritten
  // -------------------------------------

  @Override
  public void processChanges(int _cummulativeChange) {
    if ((getChange() & Element.CHANGE_SHAPE)!=0) {
      displayPoints.clear();
      synchronized (list) { displayPoints.addAll(list); }
      if (ghostPoint!=null) synchronized (ghostPoint) { displayPoints.add(new TrailPoint(ghostPoint)); }
    }
    super.processChanges(_cummulativeChange);
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  
  @Override
  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    synchronized(list) { if (list.isEmpty()) return; }
    synchronized(displayPoints) {
      for (TrailPoint trailPoint : displayPoints) {
        System.arraycopy(trailPoint.coordinates,0,aPoint,0,3);
        sizeAndToSpaceFrame(aPoint);
        minX = Math.min(minX, aPoint[0]);
        maxX = Math.max(maxX, aPoint[0]);
        minY = Math.min(minY, aPoint[1]);
        maxY = Math.max(maxY, aPoint[1]);
        minZ = Math.min(minZ, aPoint[2]);
        maxZ = Math.max(maxZ, aPoint[2]);
      }
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
  }

  // -------------------------------------
  // Private or utility methods
  // -------------------------------------
  
  /**
   * Returns the actual display points, including the ghost point, if any.
   * @return java.util.List<TrailPoint>
   */
  public java.util.List<TrailPoint> getDisplayPoints() { return displayPoints; }
  
//  /**
//   * Returns the number of points
//   * @return
//   */
//  public int getNumberOfPoints() { return displayPoints.size(); }
//  

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------
  
  protected int datasetID = hashCode();

  public void setID(int id) { datasetID = id; }

  public int getID() { return datasetID; }

  public double[][] getData2D() {
    synchronized(displayPoints) {
      int n = displayPoints.size();
      double[][] data = new double[3][n];
      for(int i = 0; i<n; i++) {
        TrailPoint point = displayPoints.get(i);
        data[0][i] = point.coordinates[0];
        data[1][i] = point.coordinates[1];
        data[2][i] = point.coordinates[2];
      }
      return data;
    }
  }
  
  public double[] getData1D(){
	  int n = getData2D()[0].length;
	  double[] data = new double[3*n];
	  for(int i=0; i<n; i++){
		  data[i] = getData2D()[0][i];
		  data[i+1] = getData2D()[1][i];
		  data[i+2] = getData2D()[2][i];
	  }
	  return data;
  }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { return inputLabels; }

  public Color[] getLineColors() {
    return new Color[] {DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), DisplayColors.getLineColor(2)};
  }

  public Color[] getFillColors() { 
    Paint fill = getStyle().getFillColor();
    if (fill instanceof Color) return new Color[] { DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), (Color) fill };
    return new Color[] { DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), DisplayColors.getLineColor(2)};
  }

  public java.util.List<Data> getDataList() { return null; }
  
  public java.util.ArrayList<Dataset> getDatasets() { return null; }

  public int getMaximum(){return maximum;}
  // ----------------------------------------------------
  // A class for the individual points of the trail
  // ----------------------------------------------------
  
  static public class TrailPoint {
    private int type;
    private double[] coordinates;

    public TrailPoint(double _x, double _y, double _z, int _type) {
      coordinates =  new double[] {_x, _y, _z};
      this.type = _type;
    }

    public TrailPoint(TrailPoint point) {
      coordinates =  point.coordinates.clone();
      this.type = point.type;
    }

    public void setCoordinates( double _x, double _y, double _z, int _type) {
      this.coordinates[0] = _x;
      this.coordinates[1] = _y;
      this.coordinates[2] = _z;
      this.type = _type;
    }
    
    public int getType() { return type; }
    
//    public void setType(int _type) { type = _type; }
    
    public double[] getPoint() { return coordinates; }

  } // End of class TrailPoint

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
