/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Color;

import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementPoints</p>
 * <p>Description: A 3D cloud of points</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementPoints extends Element {
  // Configuration variables
  private double coordinates[][] = new double[0][0];
  private Color pointColor[] = null; 
  private float pointWidth[] = null;

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementPoints(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementPoints(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets the data for the coordinates of the points.
   * Each entry in the data array corresponds to one point.
   * @param data double[][] the double[nPoints][3] array with the data
   */
  public void setData(double[][] data) {
    int n = data.length;
    if (coordinates.length!=n) {
      coordinates = new double[n][3];
      styleChanged(org.opensourcephysics.drawing3d.utils.Style.CHANGED_LINE_WIDTH); // so that the array of strokes is resized
    }
    for (int i = 0; i<n; i++) System.arraycopy(data[i], 0, coordinates[i], 0, 3);
    addChange(Element.CHANGE_POSITION);
  }

  /**
   * Gets the data of the coordinates of the points
   * @return double[][] the double[nPoints][3] original array with the data (not a copy)
   */
  public double[][] getData() { return coordinates; }

  /**
   * Allow for setting individual colors to each point
   * @param colors null if all points should be of the same color (given by style) 
   */
  public void setColors(Color[] colors) {
    pointColor = colors;
    addChange(Element.CHANGE_COLOR);
  }

  /**
   * Allow for setting individual colors to each point
   * @param colors null if all points should be of the same color (given by style) 
   */
  public void setColors(int[] colors) {
    if (colors==null) pointColor = null;
    else {
      if (pointColor==null || pointColor.length!=colors.length) pointColor = new Color[colors.length];
      for (int i=0; i<colors.length; i++) pointColor[i] = DisplayColors.getLineColor(colors[i]);
    }
    addChange(Element.CHANGE_COLOR);
  }

  /**
   * Returns the color of the point with that index
   * @param index
   * @return
   */
  public Color getPointColor(int index) {
    if (pointColor==null) return getStyle().getLineColor();
    if (index<0 || index>=pointColor.length) return getStyle().getLineColor();
    return pointColor[index];
  }

  public boolean usesDifferentColors() {
    return pointColor!=null;
  }
  
  /**
   * Allow for setting individual widths to each point
   * @param widths null if all points should be of the same width (given by style) 
   */
  public void setWidths(int[] widths) {
    if (widths==null) pointWidth = null;
    else {
      if (pointWidth==null || pointWidth.length!=widths.length) pointWidth = new float[widths.length];
      for (int i=0; i<widths.length; i++) pointWidth[i] = widths[i];
    }
    styleChanged(org.opensourcephysics.drawing3d.utils.Style.CHANGED_LINE_WIDTH);
  }

  /**
   * Allow for setting individual widths to each point
   * @param widths null if all points should be of the same width (given by style) 
   */
  public void setWidths(double[] widths) {
    if (widths==null) pointWidth = null;
    else {
      if (pointWidth==null || pointWidth.length!=widths.length) pointWidth = new float[widths.length];
      for (int i=0; i<widths.length; i++) pointWidth[i] = (float) widths[i];
    }
    styleChanged(org.opensourcephysics.drawing3d.utils.Style.CHANGED_LINE_WIDTH);
  }

  /**
   * Returns the color of the point with that index
   * @param index
   * @return
   */
  public float getPointWidth(int index) {
    if (pointWidth==null) return getStyle().getLineWidth();
    if (index<0 || index>=pointWidth.length) return getStyle().getLineWidth();
    return pointWidth[index];
  }

  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------

  @Override
  protected void getExtrema(double[] min, double[] max) {
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
