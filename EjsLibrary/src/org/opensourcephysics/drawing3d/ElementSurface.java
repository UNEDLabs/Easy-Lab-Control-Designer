/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.event.ActionListener;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementSurface</p>
 * <p>Description: A surface</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementSurface  extends Element {

  // Configuration variables
  protected double[][][] data;
  private ActionListener listener=null;

  // Implementation variables
  protected int nu = -1, nv = -1; // Make sure arrays are allocated

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementSurface(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementSurface(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets the data of the surface.
   * @param data the double[nu][nv][3] array of coordinates for the surface.
   */
  public void setData(double[][][] data) {
    this.data = data;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Gets the data of the surface.
   * @return the double[nu][nv][3] the actual array of coordinates of the surface (not a copy)
   */
  public double[][][] getData() { return this.data; }

  /**
   * Sets an action listener that will be called just before drawing.
   * This is used by ControlAnalyticCurve to make sure the extremes are correct
   * when read from the parent drawing panel.
   * @param _listener
   */
  public void setActionListener(ActionListener _listener) { this.listener = _listener; }

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
    //int theNu = data.length-1, theNv = data[0].length-1;
    for(int i = 0, n1 = data.length; i<n1; i++) {
      for(int j = 0, n2 = data[0].length; j<n2; j++) {
        System.arraycopy(data[i][j], 0, aPoint, 0, 3);
        sizeAndToSpaceFrame(aPoint);
        minX = Math.min(minX, aPoint[0]);
        maxX = Math.max(maxX, aPoint[0]);
        minY = Math.min(minY, aPoint[1]);
        maxY = Math.max(maxY, aPoint[1]);
        minZ = Math.min(minZ, aPoint[2]);
        maxZ = Math.max(maxZ, aPoint[2]);
      }
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
