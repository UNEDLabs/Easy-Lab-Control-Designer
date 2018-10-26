/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionListener;

import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementPolygon</p>
 * <p>Description: A 3D polygon</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementPolygon extends Element implements Data {
  // Configuration variables
  private boolean closed = true;
  private double coordinates[][] = new double[0][0];
  private ActionListener listener=null;

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementPolygon(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementPolygon(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets whether the polygon is closed
   * @param _closed boolean
   */
  public void setClosed(boolean _closed) {
    if (this.closed==_closed) return;
    this.closed = _closed;
    addChange(Element.CHANGE_PROJECTION);
  }

  /**
   * Gets whether the polygon is closed
   * @return boolean
   */
  public boolean isClosed() { return this.closed; }

  /**
   * Sets an action listener that will be called just before drawing.
   * This is used by ControlAnalyticCurve to make sure the extremes are correct
   * when read from the parent drawing panel.
   * @param _listener
   */
  public void setActionListener(ActionListener _listener) { this.listener = _listener; }
  
  /**
   * Sets the data for the points of the polygon.
   * Each entry in the data array corresponds to one vertex.
   * If the polygon is closed, the last point will be connected
   * to the first one and the interior will be filled
   * (unless the fill color of the style is set to null).
   * The data array is copied, so subsequence changes to the original
   * array do not affect the polygon, until this setData() method is invoked.
   * @param data double[][] the double[nPoints][3] array with the data
   */
  public void setData(double[][] data) {
	if(data==null) return;
    int n = data.length;
    if (coordinates.length!=n) coordinates = new double[n][3];
    for (int i = 0; i<n; i++) System.arraycopy(data[i], 0, coordinates[i], 0, 3);
    addChange(Element.CHANGE_POSITION);
  }

  /**
   * Sets the data for the points of the polygon.
   * Each entry in the data arrays corresponds to one vertex.
   * If the polygon is closed, the last point will be connected
   * to the first one and the interior will be filled
   * (unless the fill color of the style is set to null).
   * The input arrays are copied. If any array is shorter than the others,
   * the last point is repeated.
   * @param xArray double[] an array with the X coordinates of the vertex
   * @param yArray double[] an array with the Y coordinates of the vertex
   * @param zArray double[] an array with the Z coordinates of the vertex
   */
  public void setData(double[] xArray, double[] yArray, double[] zArray) {
    if (xArray==null || yArray==null || zArray==null) return;
    int n = Math.max(xArray.length, Math.max(yArray.length, zArray.length));
    if (coordinates.length!=n) coordinates = new double[n][3];
    if (xArray.length==yArray.length && xArray.length==zArray.length) {
      for (int i = 0; i<n; i++) {
        coordinates[i][0] = xArray[i];
        coordinates[i][1] = yArray[i];
        coordinates[i][2] = zArray[i];
      }
    } 
    else {
      double lastX = xArray[xArray.length-1];
      double lastY = yArray[yArray.length-1];
      double lastZ = zArray[zArray.length-1];
      for (int i = 0; i<n; i++) {
        coordinates[i][0] = (i<xArray.length) ? xArray[i] : lastX;
        coordinates[i][1] = (i<yArray.length) ? yArray[i] : lastY;
        coordinates[i][2] = (i<zArray.length) ? zArray[i] : lastZ;
      }
    }
    addChange(Element.CHANGE_POSITION);
  }

  /**
   * Gets the data of the coordinates of the points
   * @return double[][] the double[nPoints][3] original array with the data (not a copy)
   */
  public double[][] getData() { return coordinates; }

  /**
   * Same as getData()
   */
  public double[][] getDataArray() { return coordinates; }

  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------

  @Override
  public void processChanges(int _cummulativeChange) {
    if (listener!=null) listener.actionPerformed(null);
    super.processChanges(_cummulativeChange);
  }

  @Override
  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    for(int i = 0, n = coordinates.length; i<n; i++) {
      System.arraycopy(coordinates[i], 0, aPoint, 0, 3);
      sizeAndToSpaceFrame(aPoint);
      minX = Math.min(minX, aPoint[0]);
      maxX = Math.max(maxX, aPoint[0]);
      minY = Math.min(minY, aPoint[1]);
      maxY = Math.max(maxY, aPoint[1]);
      minZ = Math.min(minZ, aPoint[2]);
      maxZ = Math.max(maxZ, aPoint[2]);
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
  }

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------

  /** an integer ID that identifies this object */
  protected int datasetID = hashCode();
//  Dataset dataset = null;

  public void setID(int id) { datasetID = id; }

  public int getID() { return datasetID; }
  
  public double[][] getData2D() { 
    double[][] data = new double[3][coordinates.length];
    for (int i = 0, n = coordinates.length;i<n;i++) { 
      data[0][i] = coordinates[i][0];
      data[1][i] = coordinates[i][1];
      data[2][i] = coordinates[i][2];
    }
    return data;
  }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { return new String[]{"x","y","z"}; }

  public Color[] getLineColors() { 
    return new Color[] { Color.BLACK, Color.BLUE, getStyle().getLineColor()}; 
  }

  public Color[] getFillColors() { 
    Paint fill = getStyle().getFillColor();
    if (fill instanceof Color) return new Color[] { Color.BLACK, Color.BLUE, (Color) fill };
    return new Color[] { Color.BLACK, Color.BLUE, Color.RED };
  }

  public java.util.List<Data> getDataList() { return null; }

  public java.util.ArrayList<Dataset>  getDatasets() { return null; }

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
