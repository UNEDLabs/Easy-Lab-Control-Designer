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
import org.opensourcephysics.drawing3d.ElementTrail;

/**
 * <p>Title: SimpleElementTrail</p>
 * <p>Description: A single (and simple) trail of 3D points the painter's algorithm </p>
 * @author Francisco Esquembre
 * @version September 2009
*/

public class SimpleElementTrail extends SimpleElement {
  static private final int DEFAULT_SIZE = 100;
  
//  private int numPoints=0;
  private int connected[]; // The connection type between points
  private int aCoord[], bCoord[]; // The integer pixel of the projected points
  private double points[][];    // pixel for the 3D points of the segment and its subdivisions
  private double[] pixel = new double[3]; // the point for all projections
  private java.util.List<ElementTrail.TrailPoint> trailPointsList = new java.util.ArrayList<ElementTrail.TrailPoint>();
  
  public SimpleElementTrail(ElementTrail _element) { 
    super(_element); 
    java.util.List<ElementTrail.TrailPoint> displayPoints = ((ElementTrail) element).getDisplayPoints();
    allocateArrays(Math.max(DEFAULT_SIZE,displayPoints.size()));
  }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & Element.CHANGE_SHAPE)!=0) {
      // Copy the list of points, just in case they change
      synchronized(trailPointsList) {
        trailPointsList.clear();
        java.util.List<ElementTrail.TrailPoint> displayPoints = ((ElementTrail) element).getDisplayPoints();
        synchronized(displayPoints) { trailPointsList.addAll(displayPoints); }
        // reallocate arrays, if needed
        int numPoints = trailPointsList.size();
        if (numPoints>=objects.length) allocateArrays(numPoints+DEFAULT_SIZE); // allocate another chunk
        else if (numPoints<=0 && objects.length>DEFAULT_SIZE) allocateArrays(DEFAULT_SIZE); // save memory
        //      reallocateArrays();
        computePositions();
        projectPoints();
      }
    }
    if ((_cummulativeChange & FORCE_RECOMPUTE)!=0) {
      computePositions();
      projectPoints();
    }
    else if ((_cummulativeChange & Element.CHANGE_PROJECTION)!=0) projectPoints();
  }
  
  // -------------------------------------
  // Implementation of SimpleElement
  // -------------------------------------

  public void draw(Graphics2D _g2, int _index) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(element.getPanel().projectColor(style.getLineColor(), objects[_index].getDistance()));
    if ((_index==0) || connected[_index]==ElementTrail.NO_CONNECTION) _g2.drawLine(aCoord[_index], bCoord[_index], aCoord[_index], bCoord[_index]);
    else _g2.drawLine(aCoord[_index], bCoord[_index], aCoord[_index-1], bCoord[_index-1]);
  }

  public void drawQuickly(Graphics2D _g2) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(style.getLineColor());
    int numPoints = trailPointsList.size();
    if (numPoints==0) return;
    int aPrev = aCoord[0], bPrev = bCoord[0];
    _g2.drawLine(aPrev, bPrev, aPrev, bPrev);
    for (int i=1; i<numPoints; i++) {
      int aCurrent = aCoord[i], bCurrent = bCoord[i];
      switch(connected[i]) {
        default :
        case ElementTrail.LINE_CONNECTION : 
          _g2.drawLine(aCurrent, bCurrent, aPrev, bPrev);
          break;
        case ElementTrail.NO_CONNECTION : 
          _g2.drawLine(aCurrent, bCurrent, aCurrent, bCurrent);
          break;
      }
      aPrev = aCurrent;
      bPrev = bCurrent;
    }
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  
  private void allocateArrays(int _length) {
//    System.err.println ("Allocating arrays for points = "+_length);
    objects = new Object3D[_length];
    points = new double[_length][3];
    connected = new int[_length];
    aCoord = new int[_length];
    bCoord = new int[_length];
    for (int i=0; i<_length; i++) objects[i] = new Object3D(this,i);
  }

//  private void reallocateArrays() {
//    java.util.List<ElementTrail.TrailPoint> displayPoints = ((ElementTrail) element).getDisplayPoints();
//    trailPointsList.clear();
//    synchronized(displayPoints) { trailPointsList.addAll(displayPoints); }
//    int n = trailPointsList.size();
//    if (n>objects.length) allocateArrays(2*n);
//  }

  private void computePositions() {
    int numPoints = trailPointsList.size();

    for (int i=0; i<numPoints; i++) {
      ElementTrail.TrailPoint trailPoint = trailPointsList.get(i);
      System.arraycopy(trailPoint.getPoint(), 0, points[i], 0, 3);
      element.sizeAndToSpaceFrame(points[i]);
      connected[i] = trailPoint.getType();
    }
  }

  private void projectPoints() {
    DrawingPanel3D panel = element.getPanel();
    int numPoints = trailPointsList.size();
    for (int i = 0; i<numPoints; i++) {
      System.arraycopy(points[i],0,pixel,0,3);
      panel.projectPosition(pixel);
      aCoord[i] = (int) pixel[0];
      bCoord[i] = (int) pixel[1];
      objects[i].setDistance(pixel[2]*style.getDepthFactor());
    }
    for (int i=numPoints; i<objects.length; i++) objects[i].setDistance(Double.NaN);
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
