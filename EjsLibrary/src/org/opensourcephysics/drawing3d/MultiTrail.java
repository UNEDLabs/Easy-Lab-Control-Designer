/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;



/**
 * <p>Title: MultiTrail</p>
 * <p>Description: MultiTrail is a class that uses a group of ElementTrails to create
 * a more sophisticated trail with segments</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class MultiTrail extends Group {

  // Configuration variables
  private int maximum = 0;
  private int connectionType = ElementTrail.LINE_CONNECTION;
  private boolean active = true;
  private boolean noRepeat = false;
  private boolean clearAtInput = false;
  private int skip = 0;
  private String[] inputLabels = new String[] {"x", "y", "z"}; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

  // Implementation variables
  private ElementTrail currentSegment; // The current active segment
  private ElementTrail.TrailPoint flushPoint=new ElementTrail.TrailPoint(0,0,0,ElementTrail.LINE_CONNECTION);
  private int counter = 0; // The counter for the skip parameter
  private double lastX = Double.NaN, lastY=Double.NaN, lastZ=Double.NaN;
  

  public MultiTrail() {
    super();
    createNewTrail();
  }
  
  public org.opensourcephysics.drawing3d.utils.Style getStyle() {
    return currentSegment.getStyle();
  }
  
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
  public void setActive (boolean _active) { this.active = _active; }
  
  /**
   * Whether the trail is in active mode.
   * @return boolean
   */
  public boolean isActive () { return this.active; }
  
  /** 
   * Sets the no repeat state of the trail.
   * When set, a trail will ignore (x,y) points which equal the last added point.
   * @param _noRepeat
   */
  public void setNoRepeat (boolean _noRepeat) { this.noRepeat = _noRepeat; }
  
  /**
   * Whether the trail is in no repeat mode. Default value is false.
   * @return boolean
   */
  public boolean isNoRepeat () { return this.noRepeat; }

  /**
   * Sets the trail to clear existing points when receiving 
   * a new point or array of points.
   * @param _clear
   */
  public void setClearAtInput (boolean _clear) { this.clearAtInput = _clear; }
  
  /**
   * Whether the trail is in clear at input mode.
   * @return boolean
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
   * @return int
   */
  public int getSkipPoints () { return this.skip; }

  /**
   * Sets the label of the X coordinate when the data is displayed in a table
   * @param _label
   */
  public void setXLabel (String _label) { currentSegment.setXLabel(inputLabels[0] = _label); }
  
  /**
   * Sets the label of the Y coordinate when the data is displayed in a table
   * @param _label
   */
  public void setYLabel (String _label) { currentSegment.setYLabel(inputLabels[1] = _label); }

  /**
   * Sets the label of the Z coordinate when the data is displayed in a table
   * @param _label
   */
  public void setZLabel (String _label) { currentSegment.setZLabel(inputLabels[2] = _label); }

  /**
   * Adds a new point to the trail.
   * @param x double The X coordinate of the point.
   * @param y double The Y coordinate of the point.
   */
  public void addPoint (double x, double y, double z) {
    if (clearAtInput) initialize();
    addPoint (x,y,z,this.connectionType);
  }
  
  /**
   * Adds a new double[] point to the trail.
   * @param point double[] The double[2] array with the coordinates of the point.
   */
  public void addPoint(double[] point) {
    if (clearAtInput) initialize();
    addPoint(point[0], point[1], point[2], this.connectionType);
  }
  
  /**
   * Moves to the new point without drawing.
   * @param x double The X coordinate of the point.
   * @param y double The Y coordinate of the point.
   */
  public void moveToPoint(double x, double y, double z) { 
    if (clearAtInput) initialize();
    addPoint(x,y,z, ElementTrail.NO_CONNECTION);
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
    addPoint(point[0], point[1], point[2], ElementTrail.NO_CONNECTION);
  }
  
  /**
   * Adds an array of new double[] points to the trail.
   * @param input double[][] The double[nPoints][2] array with the coordinates of the points.
   */
  public void addPoints (double[][] input) {
    if (clearAtInput) initialize();
    for (int i=0,n=input.length; i<n; i++) addPoint (input[i][0],input[i][1],input[i][2],this.connectionType);
  }

  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The X coordinate of all the points.
   * @param yInput double The Y coordinate of all the points.
   * @param zInput double The double[] array with the Z coordinates of the points.
   */
  public void addPoints (double xInput, double yInput, double[] zInput) {
    if (clearAtInput) initialize();
    for (int i=0, n=zInput.length ; i<n; i++) addPoint (xInput,yInput,zInput[i],this.connectionType);
  }
  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The X coordinate of all the points.
   * @param yInput double The double[] array with the Y coordinates of the points. 
   * @param zInput double The Z coordinate of all the points.
   */
  public void addPoints (double xInput, double[] yInput, double zInput) {
    if (clearAtInput) initialize();
    for (int i=0, n=yInput.length ; i<n; i++) addPoint (xInput,yInput[i],zInput,this.connectionType);
  }
  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The double[] array with the X coordinates of the points.
   * @param yInput double The Y coordinate of all the points.
   * @param zInput double The Z coordinate of all the points.
   */
  public void addPoints (double[] xInput, double yInput, double zInput) {
    if (clearAtInput) initialize();
    for (int i=0, n=xInput.length ; i<n; i++) addPoint (xInput[i],yInput,zInput,this.connectionType);
  }
  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The X coordinate of all the points.
   * @param yInput double The double[] array with the Y coordinates of the points.
   * @param zInput double The double[] array with the Z coordinates of the points.

   */
  public void addPoints (double xInput, double[] yInput, double[] zInput) {
    if (clearAtInput) initialize();
    int n = Math.min(yInput.length,zInput.length);
    for (int i=0; i<n; i++) addPoint (xInput,yInput[i],zInput[i],this.connectionType);
  }
  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The double[] array with the X coordinates of the points.
   * @param yInput double The Y coordinate of all the points.
   * @param zInput double The double[] array with the Z coordinates of the points.

   */
  public void addPoints (double[] xInput, double yInput, double[] zInput) {
    if (clearAtInput) initialize();
    int n = Math.min(xInput.length,zInput.length);
    for (int i=0; i<n; i++) addPoint (xInput[i],yInput,zInput[i],this.connectionType);
  }
  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The double[] array with the X coordinates of the points.
   * @param yInput double The double[] array with the Y coordinates of the points.
   * @param zInput double The Z coordinate of all the points.

   */
  public void addPoints (double[] xInput, double[] yInput, double zInput) {
    if (clearAtInput) initialize();
    int n = Math.min(xInput.length,yInput.length);
    for (int i=0; i<n; i++) addPoint (xInput[i],yInput[i],zInput,this.connectionType);
  }
  
  /**
   * Adds an array of points to the trail.
   * @param xInput double The double[] array with the X coordinates of the points.
   * @param yInput double The double[] array with the Y coordinates of the points.
   * @param zInput double The double[] array with the Z coordinates of the points.
   */
  public void addPoints (double[] xInput, double[] yInput, double[] zInput) {
    if (clearAtInput) initialize();
    int n = Math.min(xInput.length,Math.min(yInput.length,zInput.length));
    for (int i=0; i<n; i++) addPoint (xInput[i],yInput[i],zInput[i],this.connectionType);
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
    currentSegment.setMaximumPoints(maximum);
  }

  /**
   * Returns the maximum number of points allowed for the trail
   * @return int
   */
  public int getMaximumPoints() { return this.maximum; }

  /**
   * Sets the type of connection for the next point.
   * @param type int
   */
  public void setConnectionType(int type) { this.connectionType = type; }

  /**
   * Gets the connection type.
   * @see #setConnectionType(int)
   */
  public int getConnectionType() { return this.connectionType; }

  /**
   * Clears all points from all segments of the trail.
   */
  public void clear() {
    for (Element el : getElements()) ((ElementTrail) el).clear();
    removeAllElements();
    addElement(currentSegment);
    initialize();
  }

  /**
   * Clears all points from the last segment of the trail, 
   * respecting previous segments.
   */
  public void initialize () {
    currentSegment.clear();
    currentSegment.setGhostPoint(null);
    counter = 0;
    lastX = lastY = lastZ = Double.NaN;
  }

  /**
   * Creates a new segment of the trail.
   */
  public void newSegment() {
    currentSegment.closeTrail(Double.isNaN(lastX) ? ElementTrail.NO_CONNECTION : flushPoint.getType());
    createNewTrail(); // close the previous trail and open a new one
    counter = 0;
    lastX = lastY = lastZ = Double.NaN;
  }
  
  private void createNewTrail() {
    ElementTrail oldTrail = currentSegment;
    currentSegment = new ElementTrail();
    currentSegment.setMaximumPoints(maximum);
    currentSegment.setLabels(inputLabels);
    if (oldTrail!=null) oldTrail.getStyle().copyTo(currentSegment.getStyle());
    addElement(currentSegment);
  }
  
  private void addPoint(double _x, double _y, double _z, int _type) {
    if (Double.isNaN(_x) || Double.isNaN(_y)  || Double.isNaN(_z)) { 
      lastX = lastY = lastZ = Double.NaN; 
      return; 
    }
    if (noRepeat && lastX==_x && lastY==_y  && lastZ==_z) return;
    if (skip>0) { // Only if the counter is 0
      if (counter>0) { 
        counter++; 
        if (counter>=skip) counter = 0;
        flushPoint.setCoordinates(_x, _y, _z, _type);
//        flushPoint.setType(_type);
        currentSegment.setGhostPoint(flushPoint);
        lastX = _x;
        lastY = _y;
        lastZ = _z;
        return; 
      }
      counter++;
    }
    currentSegment.setGhostPoint(null);
    lastX = _x;
    lastY = _y;
    lastZ = _z;
    currentSegment.addPoint(_x, _y, _z, Double.isNaN(lastX) ? ElementTrail.NO_CONNECTION : _type);
  }

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------

//  public String getName() { 
//    String theName = super.getName();
//    int index = 1;
//    int n = getElements().size();
//    for (Element el : getElements()) { // Make sure trails have the right name
//      el.setName(n>1 ? theName+"_"+index : theName);
//      index++;
//    }
//    return theName; 
//  }
  
//  public String[] getColumnNames() { return inputLabels; }

//  public java.util.List<Data> getDataList() { 
//    getName(); // make sure trails have the right name
//    return super.getDataList();
//    return dataList;
//  }

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
