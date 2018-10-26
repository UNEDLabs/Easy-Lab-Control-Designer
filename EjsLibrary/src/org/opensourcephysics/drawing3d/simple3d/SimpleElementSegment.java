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
import org.opensourcephysics.drawing3d.ElementSegment;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: SimpleElementSegment</p>
 * <p>Description: A Segment using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementSegment extends SimpleElement {
  protected int div = -1; // divisions of the segment. -1 to make sure new arrays are allocated
  protected int aCoord[] = null, bCoord[] = null; // The integer pixel of the projected points
  protected double points[][] = null;    // pixel for the 3D points of the segment and its subdivisions
  private double[] pixel = new double[3]; // the point for all projections
  
  public SimpleElementSegment(ElementSegment _element) { super(_element); }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & FORCE_RECOMPUTE)!=0) {
      computeDivisions();
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
    _g2.drawLine(aCoord[_index], bCoord[_index], aCoord[_index+1], bCoord[_index+1]);
  }

  public void drawQuickly(Graphics2D _g2) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(style.getLineColor());
    _g2.drawLine(aCoord[0], bCoord[0], aCoord[div], bCoord[div]);
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  
  private final void computeDivisions() {
    int theDiv = 1;
    Resolution res = style.getResolution();
    if (res!=null) {
      switch (res.getType()) {
        case Resolution.MAX_LENGTH :
          theDiv = Math.max((int) Math.round(0.49+element.getDiagonalSize()/res.getMaxLength()), 1);
          break;
        case Resolution.DIVISIONS :
          theDiv = Math.max(res.getN1(), 1);
          break;
      }
    }
    if (div!=theDiv) { // Reallocate arrays
      div = theDiv;
      points = new double[div+1][3];
      aCoord = new int[div+1];
      bCoord = new int[div+1];
      objects = new Object3D[div];
      for (int i = 0; i<div; i++) objects[i] = new Object3D(this, i);
    }
    double first = 0, last = 1; 
    switch (style.getRelativePosition()) {
      case Style.NORTH_EAST : 
        first = 0;
        last = 1;
        break;
      default :
      case Style.CENTERED : 
        first = -0.5;
        last = 0.5;
        break;
      case Style.SOUTH_WEST : 
        first = 1;
        last = 0;
        break;
    }
    points[0][0] = points[0][1] = points[0][2] = first;
    points[div][0] = points[div][1] = points[div][2] = last;
    double delta = (last-first)/div;
    for (int i = 1;i<div;i++) points[i][0] = points[i][1] = points[i][2] = first + i*delta;
    for (int i = 0;i<=div;i++) element.sizeAndToSpaceFrame(points[i]); // apply the transformation(s)
  }

  protected void projectPoints() {
    DrawingPanel3D panel = element.getPanel();
    for (int i = 0; i<div; i++) {
      System.arraycopy(points[i],0,pixel,0,3);
      panel.projectPosition(pixel);
      aCoord[i] = (int) pixel[0];
      bCoord[i] = (int) pixel[1];
      for (int j = 0; j<3; j++) pixel[j] = (points[i][j]+points[i+1][j])/2; // The middle point
      panel.projectPosition(pixel);
      objects[i].setDistance(pixel[2]*style.getDepthFactor());
    }
    // Project last point
    System.arraycopy(points[div],0,pixel,0,3);
    panel.projectPosition(pixel);
    aCoord[div] = (int) pixel[0];
    bCoord[div] = (int) pixel[1];
    
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
