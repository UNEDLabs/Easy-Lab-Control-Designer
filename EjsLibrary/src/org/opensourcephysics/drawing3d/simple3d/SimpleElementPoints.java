/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.Graphics2D;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPoints;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: SimpleElementPoints</p>
 * <p>Description: A group of points using the Painter's algorithm</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementPoints extends SimpleElement {
  private int aPoints[] = null, bPoints[] = null;
  private double[][] transformedCoordinates = new double[0][0];
  private double pixel[] = new double[3];       // Output of panel's projections
  private Stroke pointStroke[] = null; 

  public SimpleElementPoints(ElementPoints _element) { super(_element); }

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

  public void styleChanged(int _change) {
    switch(_change) {
      case Style.CHANGED_LINE_WIDTH:
        ElementPoints elP = (ElementPoints) element;
        if (elP.usesDifferentColors()) {
          int nPoints = elP.getData().length;
          if (pointStroke==null || pointStroke.length!=nPoints) pointStroke = new java.awt.Stroke[nPoints];
          for (int i=0; i<nPoints; i++) pointStroke[i] = new java.awt.BasicStroke(elP.getPointWidth(i)); 
        }
        else pointStroke = null;
        element.addChange(Element.CHANGE_POSITION); 
        break;
    }
    super.styleChanged(_change);
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------

  public void draw(Graphics2D _g2, int _index) {
    Color theColor = element.getPanel().projectColor(((ElementPoints) element).getPointColor(_index), objects[_index].getDistance()); // previously style.getLineColor()
    _g2.setStroke(pointStroke==null ? style.getLineStroke() : pointStroke[_index]);
    _g2.setColor(theColor);
    _g2.drawLine(aPoints[_index], bPoints[_index], aPoints[_index], bPoints[_index]); // a segment from it to itself
  }

  public void drawQuickly(Graphics2D _g2) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(style.getLineColor());
    ElementPoints el = (ElementPoints) element;
    for(int i = 0, n = aPoints.length; i<n; i++) {
      _g2.setColor(el.getPointColor(i));
      if (pointStroke!=null) _g2.setStroke(pointStroke[i]);
      _g2.drawLine(aPoints[i], bPoints[i], aPoints[i], bPoints[i]); // a segment from it to itself
    }
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void transformPoints() {
    double[][] coordinates = ((ElementPoints) element).getData();
    int n = coordinates.length;
    if (transformedCoordinates.length!=n) { // reallocate arrays
      transformedCoordinates = new double[n][3];
      aPoints = new int[n];
      bPoints = new int[n];
      objects = new Object3D[n];
      for (int i=0; i<n; i++) objects[i] = new Object3D(this,i);
    }
    for (int i=0; i<n; i++) {
      System.arraycopy(coordinates[i],0,transformedCoordinates[i],0,3);
      element.sizeAndToSpaceFrame(transformedCoordinates[i]); 
    }
  }
  
  private void projectPoints() {
    DrawingPanel3D panel = element.getPanel();
    for (int i = 0, n=transformedCoordinates.length; i<n; i++) {
      System.arraycopy(transformedCoordinates[i],0,pixel,0,3);
      panel.projectPosition(pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      objects[i].setDistance(pixel[2]*style.getDepthFactor());
    }
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
