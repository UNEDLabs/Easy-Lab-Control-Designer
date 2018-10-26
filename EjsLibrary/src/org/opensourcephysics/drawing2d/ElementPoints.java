/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;

import org.opensourcephysics.display.DisplayColors;

/**
 * <p>Title: ElementPoints</p>
 * <p>Description: A cloud of points.</p>
 * @author Francisco Esquembre
 * @version June 2008
 */
public class ElementPoints extends Element {
  // Configuration variables
  private double coordinates[][] = new double[0][0];
  private Color pointColor[] = null; 
  private Stroke pointStroke[] = null; 

  // Implementation variables
  private int aPoints[] = null, bPoints[] = null;
  private double[][] transformedCoordinates = new double[0][0];
  private double origin[] = new double[2];      // Origin coordinates, required for interaction
  private double pixel[] = new double[2];       // Output of panel's projections
  private double originpixel[] = new double[2]; // Projection of the origin, required for interaction

  // -------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Sets the data for the coordinates of the points.
   * Each entry in the data array corresponds to one point.
   * @param data double[][] the double[nPoints][2] array with the data
   */
  public void setData(double[][] data) {
    if (coordinates.length!=data.length) {
      int n = data.length;
      coordinates = new double[n][2];
      transformedCoordinates = new double[n][2];
      aPoints = new int[n];
      bPoints = new int[n];
    }
    for(int i = 0, n = data.length;i<n;i++) System.arraycopy(data[i], 0, coordinates[i], 0, 2);
    setElementChanged();
  }

  /**
   * Gets the data of the coordinates of the points
   * @return double[][] the double[nPoints][3] array with the data
   */
  public double[][] getData() {
    double[][] data = new double[coordinates.length][2];
    for (int i = 0, n = coordinates.length;i<n;i++) {
      System.arraycopy(coordinates[i], 0, data[i], 0, 2);
    }
    return data;
  }

  /**
   * Allow for setting individual colors to each point
   * @param colors null if all points should be of the same color (given by style) 
   */
  public void setColors(Color[] colors) {
    pointColor = colors;
    setElementChanged();
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
    setElementChanged();
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

  /**
   * Allow for setting individual widths to each point
   * @param widths null if all points should be of the same width (given by style) 
   */
  public void setWidths(int[] widths) {
    if (widths==null) pointStroke = null;
    else {
      if (pointStroke==null || pointStroke.length!=widths.length) pointStroke = new Stroke[widths.length];
      for (int i=0; i<widths.length; i++) pointStroke[i] = new BasicStroke(widths[i]);
    }
    setElementChanged();
  }

  /**
   * Allow for setting individual widths to each point
   * @param widths null if all points should be of the same width (given by style) 
   */
  public void setWidths(double[] widths) {
    if (widths==null) pointStroke = null;
    else {
      if (pointStroke==null || pointStroke.length!=widths.length) pointStroke = new Stroke[widths.length];
      for (int i=0; i<widths.length; i++) pointStroke[i] = new BasicStroke((float) widths[i]);
    }
    setElementChanged();
  }
  
  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  
  @Override
  protected void updateExtrema() {
    if (!hasChanged()) return;
    initExtrema();
    double[] aPoint = new double[2];
    for(int k = 0, n = coordinates.length;k<n;k++) {
      System.arraycopy(coordinates[k], 0, aPoint, 0, 2);
      getTotalTransform().transform(aPoint,0,aPoint,0,1);
      compareToAllExtrema(aPoint[0],aPoint[1]);
    }
  }

  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible() || coordinates.length==0) return;
    if (hasChanged()) transformAndProject();
    else if (needsToProject()) project();
    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(getStyle().getLineStroke());
//    g2.setColor(getStyle().getLineColor());
    for (int i = 0, n = coordinates.length;i<n;i++) {
      g2.setColor(getPointColor(i));
      if (pointStroke!=null) g2.setStroke(pointStroke[Math.min(i,pointStroke.length-1)]);
      g2.drawLine(aPoints[i], bPoints[i], aPoints[i], bPoints[i]); // a segment from it to itself
    }
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  
  public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix) {
    if (!targetPosition.isEnabled()) return null;
    if (!isReallyVisible() || coordinates.length==0) return null;
    if (hasChanged()) transformAndProject();
    else if (needsToProject()) project();
    int sensitivity = getStyle().getSensitivity();
    if (Math.abs(originpixel[0]-_xpix)<sensitivity && Math.abs(originpixel[1]-_ypix)<sensitivity) {
      return this.targetPosition;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void transformAndProject() {
    // Compute the origin projection. Reuse center
    for (int i=0; i<2; i++) origin[i] = 0.0;
    getTotalTransform().transform(origin,0,origin,0,1);
    getPanel().projectPosition(origin, originpixel);
    for (int i = 0, n = coordinates.length; i<n; i++) {
      System.arraycopy(coordinates[i],0,transformedCoordinates[i],0,2);
      getTotalTransform().transform(transformedCoordinates[i],0,transformedCoordinates[i],0,1);
      getPanel().projectPosition(transformedCoordinates[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
    }
    //setElementChanged(false);
    setNeedToProject(false);
  }

  private void project() {
    for( int i = 0, n = coordinates.length;i<n;i++) {
      getPanel().projectPosition(transformedCoordinates[i], pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
    }
    setNeedToProject(false);
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
