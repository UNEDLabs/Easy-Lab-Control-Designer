/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;

/**
 * <p>Title: ElementTrail</p>
 * Description:<p>A trail of points on the screen.</p>
 * This object is often used to show the path of a moving object.
 * Points are added to a trail either with addPoint() or moveToPoint().
 * Trails can have many segments. A segment is a section of a Trail that has a starting point, 
 * a stopping point, and a color. The clear method removes all segments from the trail whereas 
 * the initialize method only removes data from the current segment.
 * @author Francisco Esquembre
 * @version June 2008/Revised December 2008
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
  
   /**
   * A null point to signal that no flush point is present
   */
  static private TrailPoint nullPoint = new TrailPoint(Double.NaN, Double.NaN,LINE_CONNECTION);

  // Configuration variables
  private int maximum = 0;
  private int connectionType = LINE_CONNECTION;

  private boolean active = true;
  private boolean noRepeat = false;
  private boolean clearAtInput = false;
  private int skip = 0;
  private String[] inputLabels = new String[] {"x", "y"};


  // Implementation variables
  private java.util.List<TrailPoint> currentList = new ArrayList<TrailPoint>(); // The current list of points
  private GeneralPath currentPath = new GeneralPath(); // The current general path
  private PathAndStyle currentSegment = new PathAndStyle(this,null,0,currentList);
  private java.util.List<PathAndStyle> segmentList = new ArrayList<PathAndStyle>(); // The list of past Segments, if any
  private TrailPoint flushPoint=nullPoint; // This is the last point which was not added because of the skip parameter
  private boolean isEmpty=true;
  private int counter = 0;
//  private int firstPoint = 0; // the first point of the current path 
  private double lastX = Double.NaN, lastY=Double.NaN;
  
  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  /**
   * Set/unset the active state of the trail. 
   * The trail does not use this state, but just keeps an internal value that
   * programs can consult in order not to add data to the trail.
   * In other words, a trail will always honor an addPoint() command.
   * But programs can consult this internal value to decide whether or not
   * to send data to a trail.
   * Default value is true.
   * @param _active
   */
  public void setActive (boolean _active) {
    this.active = _active;
  }
  
  /**
   * Whether the trail is in active mode.
   * @return
   */
  public boolean isActive () { return this.active; }
  
  /** 
   * Sets the no repeat state of the trail.
   * When set, a trail will ignore (x,y) points which equal the last added point.
   * @param _noRepeat
   */
  public void setNoRepeat (boolean _noRepeat) {
    this.noRepeat = _noRepeat;
  }
  
  /**
   * Whether the trail is in no repeat mode. Default value is false.
   * @return
   */
  public boolean isNoRepeat () { return this.noRepeat; }

  /**
   * Sets the trail to clear existing points when receiving 
   * a new point or array of points.
   * @param _clear
   */
  public void setClearAtInput (boolean _clear) {
    this.clearAtInput = _clear;
  }
  
  /**
   * Whether the trail is in clear at input mode.
   * @return
   */
  public boolean isClearAtInput () { return this.clearAtInput; }

  /**
   * Sets the skip parameter. When the skip parameter is larger than zero,
   * the trail only considers one of every 'skip' points. That is, if skip is 3, 
   * the trail will consider only every third point sent to it. 
   * The default is zero, meaning all points must be considered.
   * @param _skip
   */
  public void setSkipPoints (int _skip) {
    if (this.skip!=_skip) {
      this.skip = _skip;
      counter = 0;
    }
  }
  
  /**
   * Returns the skip parameter of the trail.
   * @return
   */
  public int getSkipPoints () { return this.skip; }

  /**
   * Sets the label of the X coordinate when the data is displayed in a table
   * @param _label
   */
  public void setXLabel (String _label) { inputLabels[0] = _label; }
  
  /**
   * Sets the label of the Y coordinate when the data is displayed in a table
   * @param _label
   */
  public void setYLabel (String _label) { inputLabels[1] = _label; }

  /**
   * Adds a new point to the trail.
   * @param x double The X coordinate of the point.
   * @param y double The Y coordinate of the point.
   */
  public void addPoint (double x, double y) {
    if (clearAtInput) initialize();
    addPoint (x,y,this.connectionType);
  }
  
  /**
   * Adds a new double[] point to the trail.
   * @param point double[] The double[2] array with the coordinates of the point.
   */
  public void addPoint(double[] point) {
    if (clearAtInput) initialize();
    addPoint(point[0], point[1], this.connectionType);
  }

  /**
   * Moves to the new point without drawing.
   * @param x double The X coordinate of the point.
   * @param y double The Y coordinate of the point.
   */
  public void moveToPoint(double x, double y) { 
    if (clearAtInput) initialize();
    addPoint(x,y, NO_CONNECTION);
  }

  /**
   * Moves to the new point without drawing. 
   * (Equivalent to setting the connection type
   * to NO_CONNECTION and adding one single point, then setting the 
   * type back to its previous value.)
   * @param point double[] The double[2] array with the coordinates of the point.
   */
  public void moveToPoint(double[] point) { 
    if (clearAtInput) initialize();
    addPoint(point[0], point[1], NO_CONNECTION);
  }
  
  /**
   * Adds an array of new double[] points to the trail.
   * @param point double[][] The double[nPoints][2] array with the coordinates of the points.
   * @param input
   */
  public void addPoints (double[][] input) {
    if (clearAtInput) initialize();
    for (int i=0,n=input.length; i<n; i++) addPoint (input[i][0],input[i][1],this.connectionType);
  }

  /**
   * Adds an array of points with the same Y coordinate to the trail.
   * @param x double The array of X coordinates of the point.
   * @param y double The Y coordinate of all the points.
   */
  public void addPoints (double[] x, double y) {
    if (clearAtInput) initialize();
    for (int i=0,n=x.length; i<n; i++) addPoint (x[i],y,this.connectionType);
  }
  
  /**
   * Adds an array of points with the same X coordinate to the trail
   * @param x double The X coordinate of all the points.
   * @param y double[] The array of Y coordinates of the point.
   */
  public void addPoints (double x, double[] y) {
    if (clearAtInput) initialize();
    for (int i=0,n=y.length; i<n; i++) addPoint (x,y[i],this.connectionType);
  }

  /**
   * Adds an array of points to the trail.
   * @param xInput double The double[] array with the X coordinates of the points.
   * @param yInput double The double[] array with the Y coordinates of the points.
   */
  public void addPoints (double[] xInput, double[] yInput) {
    if (clearAtInput) initialize();
    int n = Math.min(xInput.length,yInput.length);
    for (int i=0; i<n; i++) addPoint (xInput[i],yInput[i],this.connectionType);
  }

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
  public void setMaximumPoints(int maximum) {
    this.maximum = Math.max(maximum, 0);
  }

  /**
   * Returns the maximum number of points allowed for the trail
   * @return int
   */
  public int getMaximumPoints() {
    return this.maximum;
  }

  /**
   * Sets the type of connection for the next point.
   * @param type int
   */
  public void setConnectionType(int type) {
    this.connectionType = type;
  }

  /**
   * Gets the connection type.
   * @see #setConnectionType(int)
   */
  public int getConnectionType() {
    return this.connectionType;
  }

  /**
   * Clears all points from all segments of the trail.
   */
  public void clear() {
//    System.out.println ("clear");
    synchronized(segmentList) { segmentList.clear(); }
    currentSegment.index = 0;    
    initialize();
  }

  /**
   * Clears all points from the last segment of the trail, 
   * respecting previous segments.
   */
  public void initialize () {
    synchronized(currentList) {
      currentList.clear();
      currentPath.reset();
      flushPoint = nullPoint;
      isEmpty = true;
      counter = 0;
      lastX = Double.NaN;
      lastY = Double.NaN;
      setNeedToProject(true);
    }
  }

  /**
   * Creates a new segment of the trail.
   */
  public void newSegment() {
    TrailPoint extraPoint = null;
    synchronized(flushPoint) {
      if (flushPoint!=nullPoint) extraPoint = new TrailPoint (flushPoint.x,flushPoint.y,flushPoint.type);
    }
    if (extraPoint!=null) {
      currentList.add(extraPoint);
      int type = isEmpty ? NO_CONNECTION : extraPoint.type;
      switch(type) {
        default :
        case LINE_CONNECTION : 
          currentPath.lineTo((float)extraPoint.x, (float)extraPoint.y); 
          break;
        case NO_CONNECTION : 
          currentPath.moveTo((float)extraPoint.x, (float)extraPoint.y); 
          currentPath.lineTo((float)extraPoint.x, (float)extraPoint.y); 
          break;
      }
    }
    synchronized(currentList) {
      segmentList.add(new PathAndStyle(this,currentPath,segmentList.size(),currentList));
      currentList = new ArrayList<TrailPoint>();
      currentPath = new GeneralPath();
      currentSegment.pointsList = currentList;
      currentSegment.path = currentPath;
      currentSegment.index++;
      flushPoint = nullPoint;
      isEmpty = true;
      counter = 0;
      lastX = Double.NaN;
      lastY = Double.NaN;
    }
  }
  
  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------

  private int dataID = hashCode();

  public void setID(int id) { dataID = id; }

  public int getID() { return dataID; }

  public double[][] getData2D() { return null; }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { return inputLabels; }

  public Color[] getLineColors() { 
    return new Color[] { Color.BLACK, getStyle().getLineColor()}; 
  }

  public Color[] getFillColors() { 
    Paint fill = getStyle().getFillColor();
    if (fill instanceof Color) return new Color[] { Color.BLACK, (Color) fill };
    return new Color[] { Color.BLACK, Color.BLUE };
  }

  public java.util.List<Data> getDataList() {
    java.util.List<Data> dataList = new ArrayList<Data>();
    dataList.addAll(segmentList);
    // The current segment
    int n = segmentList.size();
    currentSegment.update(getCurrentSegmentPath(), n>0 ? n+1 : -1);
    dataList.add(currentSegment);
    return dataList; 
  }

  
  public java.util.ArrayList<Dataset>  getDatasets() { return null; }
  
  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------

  /**
   * Returns a GeneralPath with the last segment of the trail
   */
  private GeneralPath getCurrentSegmentPath() {
    TrailPoint extraPoint = null;
    synchronized(flushPoint) {
      if (flushPoint!=nullPoint) extraPoint = new TrailPoint (flushPoint.x,flushPoint.y,flushPoint.type);
    }
    if (extraPoint==null) return currentPath;
    GeneralPath path = new GeneralPath(currentPath);
    int type = isEmpty ? NO_CONNECTION : extraPoint.type;
    switch(type) {
      default :
      case LINE_CONNECTION : path.lineTo((float)extraPoint.x, (float)extraPoint.y); break;
      case NO_CONNECTION : 
        path.moveTo((float)extraPoint.x, (float)extraPoint.y); 
        path.lineTo((float)extraPoint.x, (float)extraPoint.y); 
        break;
    }
    return path;
  }

  @Override
  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible()) return;
    Graphics2D g2 = (Graphics2D) _g;

    // Draw past paths
    for (PathAndStyle pas : segmentList) {
      Color color = pas.style.getLineColor();
      if (color!=null) {
        g2.setStroke(pas.style.getLineStroke());
        g2.setColor(color);
        Shape trShape = getPixelTransform(_panel).createTransformedShape(pas.path);
        g2.draw(trShape);
      }
    }

    // Now draw the current path
    Color color = getStyle().getLineColor();
    if (color==null) return;
    g2.setStroke(getStyle().getLineStroke());
    g2.setColor(color);

    Shape trShape = getPixelTransform(_panel).createTransformedShape(getCurrentSegmentPath());
    g2.draw(trShape);
  }
  
  @Override
  public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix) {
    if (!targetPosition.isEnabled()) return null;
    if (!isReallyVisible()) return null;
    if (hasChanged() || needsToProject()) projectPoints(_panel);
    int sensitivity = getStyle().getSensitivity();
    if (targetPosition.isEnabled()) {
      for (PathAndStyle pas : segmentList) {
        for (TrailPoint point : pas.pointsList) {
          if (Math.abs(point.pixel[0]-_xpix)<sensitivity && Math.abs(point.pixel[1]-_ypix)<sensitivity) return this.targetPosition;
        }
      }
      for (TrailPoint point : currentList) {
        if (Math.abs(point.pixel[0]-_xpix)<sensitivity && Math.abs(point.pixel[1]-_ypix)<sensitivity) return this.targetPosition;
      }
      synchronized(flushPoint) {
        if (flushPoint!=nullPoint && Math.abs(flushPoint.pixel[0]-_xpix)<sensitivity && Math.abs(flushPoint.pixel[1]-_ypix)<sensitivity) return this.targetPosition;
      }
    }
    return null;
  }

  @Override
  public boolean isMeasured () { 
    return !(currentList.isEmpty() && segmentList.isEmpty()) && super.getCanBeMeasured();  
  }

  @Override
  protected void updateExtrema() {
    if (!hasChanged()) return;
    initExtrema();
    double[] aPoint = new double[2];
    
    for (PathAndStyle pas : segmentList) {
      for (TrailPoint point : pas.pointsList) {
        aPoint[0] = point.x;
        aPoint[1] = point.y;
        getTotalTransform().transform(aPoint,0,aPoint,0,1);
        if (Double.isNaN(aPoint[0]) || Double.isNaN(aPoint[1])) ; // ignore this point
        else compareToAllExtrema(aPoint[0],aPoint[1]);
      }
    }
    for (TrailPoint point : currentList) {
      aPoint[0] = point.x;
      aPoint[1] = point.y;
      getTotalTransform().transform(aPoint,0,aPoint,0,1);
      if (Double.isNaN(aPoint[0]) || Double.isNaN(aPoint[1])) ; // ignore this point
      else compareToAllExtrema(aPoint[0],aPoint[1]);
    }
    boolean hasFlush = false;
    synchronized(flushPoint) {
      if (flushPoint!=nullPoint) { // do the same with flushPoint
        aPoint[0] = flushPoint.x;
        aPoint[1] = flushPoint.y;
        hasFlush = true;
      }
    }
    if (hasFlush) {
      getTotalTransform().transform(aPoint,0,aPoint,0,1);
      if (Double.isNaN(aPoint[0]) || Double.isNaN(aPoint[1])) ; // ignore this point
      else compareToAllExtrema(aPoint[0],aPoint[1]);
    }
  }
  
  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void projectPoints(org.opensourcephysics.display.DrawingPanel _panel) {
    java.awt.geom.AffineTransform tr = getPixelTransform(_panel);
    for (PathAndStyle pas : segmentList) for (TrailPoint point : pas.pointsList) point.project(tr);
    for (TrailPoint point : currentList) point.project(tr);
    synchronized(flushPoint) {
      if (flushPoint!=nullPoint) flushPoint.project(tr);
    }
    setNeedToProject(false);
  }

  private void addPoint(double _x, double _y, int _type) {
    //if (!active) return;
//    System.out.println ("Add "+_x+","+_y);
    if (Double.isNaN(_x) || Double.isNaN(_y)) { isEmpty=true; return; }
    if (noRepeat && lastX==_x && lastY==_y) return;
//    System.out.println ("Added "+_x+","+_y);
    if (skip>0) { // Only if the counter is 0
      if (counter>0) { 
        counter++; 
        if (counter>=skip) counter = 0;
        flushPoint =  new TrailPoint(_x, _y, _type);
        lastX = _x;
        lastY = _y;
        return; 
      }
      counter++;
    }
    flushPoint = nullPoint;
    synchronized(currentList) {
      lastX = _x;
      lastY = _y;
      int size = currentList.size();
      if (maximum>2 && size>=maximum) {
        currentList.remove(0); // remove the firstPoint
        synchronized (currentPath) {
          currentPath.reset();
          TrailPoint initialPoint = currentList.get(0); //tmpPoints[firstPoint];
          currentPath.moveTo((float)initialPoint.x, (float)initialPoint.y);
          currentPath.lineTo((float)initialPoint.x, (float)initialPoint.y); 
          for (TrailPoint point : currentList) {
            switch(point.type) {
              default :
              case LINE_CONNECTION : 
                currentPath.lineTo((float)point.x, (float)point.y); 
                break;
              case NO_CONNECTION : 
                currentPath.moveTo((float)point.x, (float)point.y); 
                currentPath.lineTo((float)point.x, (float)point.y); 
                break;
            }
          }
        } // end of synchro currentPath
      }
      TrailPoint point = new TrailPoint(_x, _y, _type);
      currentList.add(point);
      synchronized (currentPath) {
        if (isEmpty) {
          currentPath.moveTo((float)_x, (float)_y);
          currentPath.lineTo((float)_x, (float)_y); 
        }
        else switch(point.type) {
          default :
          case LINE_CONNECTION : 
            currentPath.lineTo((float)_x, (float)_y); 
            break;
          case NO_CONNECTION : 
            currentPath.moveTo((float)_x, (float)_y); 
            currentPath.lineTo((float)_x, (float)_y); 
            break;
        }
      }
      isEmpty = false;
      setElementChanged();
    }
  }

  //----------------------------------------------------
  // A class for GeneralPath and Color
  // ----------------------------------------------------

  static private class PathAndStyle implements org.opensourcephysics.display.Data {
    private ElementTrail trail;
    private GeneralPath path;
    private Style style;
    private java.util.List<TrailPoint> pointsList;

    @SuppressWarnings("unused")
    private int index;
    private int id=hashCode();
    
    PathAndStyle (ElementTrail _trail, GeneralPath _path, int _index, java.util.List<TrailPoint> _pointsList) {
      this.trail = _trail;
      this.path = _path;
      this.style = _trail.getStyle().clone();
      this.index = _index;
      this.pointsList = _pointsList;
    }
    
    void update (GeneralPath _path, int _index) { 
      this.path = _path; 
      this.style = trail.getStyle().clone();
//      this.index = _index;
    }
    
//    public String getName() { return trail.segmentList.size()<=0 ? trail.getName() : trail.getName()+"_"+index; }

    public String getName() { return trail.getName(); }

    public void setID(int _id) { id = _id; }

    public int getID() { return id; }
    
    public double[][] getData2D() {
      int n = pointsList.size();
      double[][] data = new double[2][n];
      for (int i = 0; i<n; i++) {
        TrailPoint point = pointsList.get(i);
        data[0][i] = point.x;
        data[1][i] = point.y;
      }
//      PathIterator pi = path.getPathIterator(null);
//      double[] coords = new double [6];
//      ArrayList<double[]> coordList = new ArrayList<double[]>();
//      while (!pi.isDone()) {
//        int type = pi.currentSegment(coords);
//        if (type!=PathIterator.SEG_MOVETO) coordList.add(new double[] { coords[0], coords[1] } );
//        pi.next();
//      }
//      double[][] data = new double[2][coordList.size()];
//      for (int i=0,n=coordList.size(); i<n; i++) {
//        double[] point = coordList.get(i);
//        data[0][i] = point[0];
//        data[1][i] = point[1];
//      }
      return data;
    }

    public double[][][] getData3D() { return null; }

    public String[] getColumnNames() { return trail.inputLabels; }

    public Color[] getLineColors() { 
      return new Color[] { Color.BLACK, style.getLineColor()}; 
    }

    public Color[] getFillColors() { 
      Paint fill = style.getFillColor();
      if (fill instanceof Color) return new Color[] { Color.BLACK, (Color) fill };
      return new Color[] { Color.BLACK, Color.BLUE };
    }

    public java.util.List<Data> getDataList() { return null; }
    
    public java.util.ArrayList<Dataset> getDatasets() { return null; }

  }
  
  // ----------------------------------------------------
  // A class for the individual points of the trail
  // ----------------------------------------------------

  static private class TrailPoint {
    private int type;
    private double x, y;
    private double pixel[];

    TrailPoint(double _x, double _y, int _type) {
      this.pixel = new double[2];
      this.x = _x;
      this.y = _y;
      this.type = _type;
    }
    
    void project(AffineTransform tr) {
      pixel[0] = x;
      pixel[1] = y;
      tr.transform(pixel,0,pixel,0,1);
    }

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
