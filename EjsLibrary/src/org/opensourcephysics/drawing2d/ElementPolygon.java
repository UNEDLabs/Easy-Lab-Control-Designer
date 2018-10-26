/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;
import java.awt.event.ActionListener;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.drawing2d.interaction.InteractionTarget;

/**
 * <p>Title: ElementPolygon</p>
 * <p>Description: A Polygon 2D</p>
 * @author Francisco Esquembre
 * @version July 2008
 */
public class ElementPolygon extends Element implements Data {
  // Configuration variables
  private boolean closed = true;
  private double coordinates[][] = new double[0][0];
  private ActionListener listener=null;

  // Implementation variables
  private int aPoints[] = null, bPoints[] = null;

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  /**
   * Sets whether the polygon is closed
   * @param closed boolean
   */
  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  /**
   * Gets whether the polygon is closed
   * @return boolean
   */
  public boolean isClosed() {
    return this.closed;
  }

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
   * array do not affect the polygon, until the setData() method is invoked.
   * @param data double[][] the double[nPoints][2] array with the data
   */
  public void setData(double[][] data) {
    if (data==null) return;
    if (coordinates.length!=data.length) {
      int n = data.length;
      coordinates = new double[n][2];
      aPoints = new int[n];
      bPoints = new int[n];
    }
    for (int i = 0, n = data.length;i<n;i++) System.arraycopy(data[i], 0, coordinates[i], 0, 2);
    this.setElementChanged();
  }

  /**
   * Sets the data for the points of the polygon.
   * Each entry in the data array corresponds to one vertex.
   * If the polygon is closed, the last point will be connected
   * to the first one and the interior will be filled
   * (unless the fill color of the style is set to null).
   * The data array is copied, so subsequence changes to the original
   * array do not affect the polygon, until the setData() method is invoked.
   * If the arrays have different lengths, the last element of the shortest
   * array is repeated to match the longest array.
   * @param xArray double[] the double[nPoints] array with the X coordinates
   * @param yArray double[] the double[nPoints] array with the Y coordinates
   */
  public void setData(double[] xArray, double[] yArray) {
    int n = Math.max(xArray.length, yArray.length);
    if (coordinates.length!=n) {
      coordinates = new double[n][2];
      aPoints = new int[n];
      bPoints = new int[n];
    }
    if (xArray.length==yArray.length) {
      for (int i = 0; i<n; i++) {
        coordinates[i][0] = xArray[i];
        coordinates[i][1] = yArray[i];
      }
    }
    else {
      double lastX = xArray[xArray.length-1];
      double lastY = yArray[yArray.length-1];
      for (int i = 0; i<n; i++) {
        coordinates[i][0] = i<xArray.length ? xArray[i] : lastX;
        coordinates[i][1] = i<yArray.length ? yArray[i] : lastY;
      }
    }
    this.setElementChanged();
  }

  /**
   * Gets a copy of the data of the points for the polygon
   * @return double[][] the double[nPoints][2] array with the data
   */
  public double[][] getData() {
    double[][] data = new double[coordinates.length][2];
    for (int i = 0, n = coordinates.length;i<n;i++) System.arraycopy(coordinates[i], 0, data[i], 0, 2);
    return data;
  }

  /**
   * Gets the actual array with the data of the points for the polygon
   * @return double[][] the double[nPoints][2] array with the data
   */
  public double[][] getDataArray() { return coordinates; }

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------

  /** an integer ID that identifies this object */
  protected int datasetID = hashCode();
//  Dataset dataset = null;

  public void setID(int id) { datasetID = id; }

  public int getID() { return datasetID; }
  
  public double[][] getData2D() { 
    double[][] data = new double[2][coordinates.length];
    for (int i = 0, n = coordinates.length;i<n;i++) { 
      data[0][i] = coordinates[i][0];
      data[1][i] = coordinates[i][1];
    }
    return data;
  }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { return new String[]{"x","y"}; }

  public Color[] getLineColors() { 
    return new Color[] { Color.BLACK, getStyle().getLineColor()}; 
  }

  public Color[] getFillColors() { 
    Paint fill = getStyle().getFillColor();
    if (fill instanceof Color) return new Color[] { Color.BLACK, (Color) fill };
    return new Color[] { Color.BLACK, Color.BLUE };
  }

  public java.util.List<Data> getDataList() { return null; }

  public java.util.ArrayList<Dataset>  getDatasets() { return null; }

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
    if (listener!=null) listener.actionPerformed(null);
    if (!isReallyVisible() || coordinates.length==0) return;
    if (hasChanged() || needsToProject()) projectPoints(_panel);
    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(getStyle().getLineStroke());
    Color color = getStyle().getLineColor();
    if (closed) {
      Paint fill = getStyle().getFillColor();
      if (fill!=null && getStyle().isDrawingFill()) {
        g2.setPaint(fill);
        g2.fillPolygon(aPoints,bPoints, aPoints.length);
      }
      if (color!=null && getStyle().isDrawingLines()) {
        g2.setColor(color);
        g2.drawPolygon(aPoints,bPoints, aPoints.length);
      }
    }
    else {
      g2.setColor(color);
      g2.drawPolyline(aPoints, bPoints, aPoints.length);
    }
  }
  
  // -------------------------------------
  // Interaction
  // -------------------------------------

  private double[] getHotSpotOfPoint (int k) {
    double[] point = new double[] {coordinates[k][0], coordinates[k][1]};
    getTotalTransform().transform(point,0,point,0,1);
    return point;
  }
  
  @Override
  public void updateHotSpot(InteractionTarget target, double[] point) {
    if (target.getEnabled() == InteractionTarget.ENABLED_NO_MOVE) return; // No move
    Element gr = getGroup(); //getTopGroup();
    int k = ((Integer) target.getDataObject()).intValue();
    switch (target.getType()) {
      case Element.TARGET_POSITION :
        if (gr!=null && target.getAffectsGroup()) { // Move the whole group
          double[] origin = getHotSpotOfPoint(k);
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X :  gr.setX(gr.getX() + point[0] - origin[0]); break;
            case InteractionTarget.ENABLED_Y :  gr.setY(gr.getY() + point[1] - origin[1]); break;
            default :  gr.setXY(gr.getX() + point[0] - origin[0], gr.getY() + point[1] - origin[1]); break;
          }
        } 
        else { // Move only the element
          double[] thePoint = point.clone(); //new double[] {point[0], point[1], point[2]};
          groupInverseTransformations(thePoint);
          double[] origin = new double[] {coordinates[k][0], coordinates[k][1]};
          origin[0] *= getSizeX();
          origin[1] *= getSizeY();
          getTheTransformation().transform(origin,0,origin,0,1);
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X : setX(thePoint[0]-origin[0]); break;
            case InteractionTarget.ENABLED_Y : setY(thePoint[1]-origin[1]); break;
            default : setXY(thePoint[0]-origin[0],thePoint[1]-origin[1]); break;
          }
        }
        break;
      case Element.TARGET_SIZE :
        if (gr!=null && target.getAffectsGroup()) { // Resize the whole group
          double[] thePoint = point.clone();
          thePoint[0] -= gr.getX();
          thePoint[1] -= gr.getY();
          try { gr.getTheTransformation().inverseTransform(thePoint,0,thePoint,0,1); } 
          catch (Exception exc) {};
          double[] origin = new double[] {coordinates[k][0], coordinates[k][1]};
          elementDirectTransformations(origin);
          // If any of the dimensions is zero, a division by zero would occur.
          // Not dividing is not enough.
          if (origin[0]!=0) thePoint[0] /= origin[0];
          else thePoint[0] = gr.getX();
          if (origin[1]!=0) thePoint[1] /= origin[1];
          else thePoint[1] = gr.getY();
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X :  gr.setSizeX(thePoint[0]); break;
            case InteractionTarget.ENABLED_Y :  gr.setSizeY(thePoint[1]); break;
            default :  gr.setSize(thePoint); break;
          }
        } 
        else { // Resize only the element
          double[] thePoint = point.clone();
          groupInverseTransformations(thePoint);
          thePoint[0] -= this.getX();
          thePoint[1] -= this.getY();
          try { this.getTheTransformation().inverseTransform(thePoint,0,thePoint,0,1); } 
          catch (Exception exc) {};
          coordinates[k][0] = thePoint[0];
          coordinates[k][1] = thePoint[1];
          this.setNeedToProject(true);
          /*
          double[] origin = new double[] {coordinates[k][0], coordinates[k][1]};
          if (origin[0]!=0) thePoint[0] /= origin[0];
          if (origin[1]!=0) thePoint[1] /= origin[1];
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X : setSizeX(thePoint[0]); break;
            case InteractionTarget.ENABLED_Y : setSizeY(thePoint[1]); break;
            default : setSize(thePoint); break;
          }
          */
        }
        break;

    }
  }

  public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix) {
    if (! (targetPosition.isEnabled() || targetSize.isEnabled())) return null;
    if (!isReallyVisible() || coordinates.length==0) return null;
    if (hasChanged() || needsToProject()) projectPoints(_panel);
    int sensitivity = getStyle().getSensitivity();
    if (sensitivity<=0) sensitivity = 1;
    for (int k = 0, n = aPoints.length;k<n;k++) {
      if (Math.abs(aPoints[k]-_xpix)<sensitivity && Math.abs(bPoints[k]-_ypix)<sensitivity) {
        if (targetPosition.isEnabled()) {
          targetPosition.setDataObject(new Integer(k));
          return targetPosition;
        }
        if (targetSize.isEnabled()) {
          targetSize.setDataObject(new Integer(k));
          return targetSize;
        }
      }
    }

    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  
  private void projectPoints(org.opensourcephysics.display.DrawingPanel _panel) {
    java.awt.geom.AffineTransform tr = getPixelTransform(_panel);
    double[] point = new double[2];
    for(int k = 0, n = coordinates.length;k<n;k++) {
      System.arraycopy(coordinates[k], 0, point, 0, 2);
      tr.transform(point,0,point,0,1);
      aPoints[k] = (int) point[0];
      bPoints[k] = (int) point[1];
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
