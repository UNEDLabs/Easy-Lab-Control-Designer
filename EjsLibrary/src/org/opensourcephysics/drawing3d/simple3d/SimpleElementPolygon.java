/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.Graphics2D;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPolygon;

/**
 * <p>Title: SimpleElementPolygon</p>
 * <p>Description: A Polygon using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class SimpleElementPolygon extends SimpleElement {
  // Implementation variables
  private ElementPolygon polygon;
  private int aPoints[] = null, bPoints[] = null;
  private double[][] transformedCoordinates = new double[0][0];
  private double center[] = new double[3];                                     // The center of the poligon
  private double pixel[] = new double[3];                                      // Output of panel's projections
  private Object3D[] closedObject = new Object3D[] {new Object3D(this, -1)}; // A special object for a closed poligon

  public SimpleElementPolygon(ElementPolygon _element) { 
    super(_element); 
    polygon = _element;
  }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & FORCE_RECOMPUTE)!=0) {
      transformPoints();
      projectPoints();
    }
    else if ((_cummulativeChange & Element.CHANGE_PROJECTION)!=0) projectPoints();
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  
  @Override
  public Object3D[] getObjects3D() {
    if (polygon.isClosed() && style.isDrawingFill()) return closedObject;
    return objects;
  }

  public void draw(Graphics2D _g2, int _index) {
    DrawingPanel3D panel = element.getPanel();
    if (_index<0) { // Interior ==> closed = true and fillPattern!=null
      if (style.isDrawingFill()) {
        _g2.setPaint(panel.projectPaint(style.getFillColor(), closedObject[0].getDistance()));
        _g2.fillPolygon(aPoints, bPoints, aPoints.length);
      }
      if (style.isDrawingLines()) {
        _g2.setStroke(style.getLineStroke());
        _g2.setColor(panel.projectColor(style.getLineColor(), closedObject[0].getDistance()));
        int n = aPoints.length-1;
        for (int i = 0; i<n; i++) _g2.drawLine(aPoints[i], bPoints[i], aPoints[i+1], bPoints[i+1]);
        _g2.drawLine(aPoints[n], bPoints[n], aPoints[0], bPoints[0]);
      }
      return;
    } // end of filled polygon
    
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(panel.projectColor(style.getLineColor(), objects[_index].getDistance()));
    int sides = aPoints.length-1;
    if (_index<sides) _g2.drawLine(aPoints[_index], bPoints[_index], aPoints[_index+1], bPoints[_index+1]); // regular segment
    else _g2.drawLine(aPoints[sides], bPoints[sides], aPoints[0], bPoints[0]);                 // if (_index==sides) { // Last closing segment
  }

  public void drawQuickly(Graphics2D _g2) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(style.getLineColor());
    _g2.drawPolyline(aPoints, bPoints, aPoints.length);
    if (polygon.isClosed()) {
      int sides = aPoints.length-1;
      _g2.drawLine(aPoints[sides], bPoints[sides], aPoints[0], bPoints[0]);
    }
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  
  private void transformPoints() {
    double[][] coordinates = polygon.getDataArray();
    int n = coordinates.length;
    if (transformedCoordinates.length!=n) { // reallocate arrays
      transformedCoordinates = new double[n][3];
      aPoints = new int[n];
      bPoints = new int[n];
      objects = new Object3D[n];
      for(int i = 0; i<n; i++) objects[i] = new Object3D(this, i);
    }
    System.arraycopy(Element.STD_ORIGIN,0,center,0,3);
    for (int i=0; i<n; i++) {
      System.arraycopy(coordinates[i],0,transformedCoordinates[i],0,3);
      element.sizeAndToSpaceFrame(transformedCoordinates[i]); 
      for (int k = 0; k<3; k++) center[k] += transformedCoordinates[i][k];
    }
    for (int k = 0; k<3; k++) center[k] /= n;
  }
  
  private void projectPoints() {
    DrawingPanel3D panel = element.getPanel();
    int n = transformedCoordinates.length;
    for (int i = 0; i<n; i++) {
      System.arraycopy(transformedCoordinates[i],0,pixel,0,3);
      panel.projectPosition(pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      objects[i].setDistance(pixel[2]*style.getDepthFactor());
    }
    // last Segment
    if (polygon.isClosed()) {
      if (style.isDrawingFill()) {
        System.arraycopy(center,0,pixel,0,3);
        panel.projectPosition(pixel);
        closedObject[0].setDistance(pixel[2]*style.getDepthFactor());
      } 
      else closedObject[0].setDistance(Double.NaN); // Will not be drawn
    }
    else objects[n-1].setDistance(Double.NaN);
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
