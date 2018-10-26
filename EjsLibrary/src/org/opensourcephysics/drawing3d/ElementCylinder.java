/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Resolution;

/**
 * <p>Title: ElementCylinder</p>
 * <p>Description: A 3D cylinder</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementCylinder extends Element {

  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private boolean closedLeft = true, closedRight = true;
  private int minAngle = 0, maxAngle = 360;
  
  {
    getStyle().setResolution(new Resolution(3, 12, 5));
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementCylinder(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementCylinder(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Whether the cylinder should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close){
    if (closedBottom==close) return;
	  closedBottom = close;
	  addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the cylinder is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom() { return closedBottom; }

  /**
   * Whether the (truncated) cylinder should be closed at its top.
   * @param close the desired value
   */
  public void setClosedTop(boolean close){
    if (closedTop==close) return;
	  closedTop = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (truncated) cylinder is closed at its top.
   * @return the value
   */
  public boolean isClosedTop() { return closedTop; }

  /**
   * Whether the (non-360 degrees) cylinder should be closed at its left side.
   * @param close the desired value
   */
  public void setClosedLeft(boolean close) {
    if (closedLeft==close) return;
    closedLeft = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (non-360 degrees) cylinder is closed at its left side.
   * @return the value
   */
  public boolean isClosedLeft() { return closedLeft; }

  /**
   * Whether the (non-360 degrees) cylinder should be closed at its right side.
   * @param close the desired value
   */
  public void setClosedRight(boolean close) {
    if (closedRight==close) return;
    closedRight = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (non-360 degrees) cylinder is closed at its right side.
   * @return the value
   */
  public boolean isClosedRight() { return closedRight; }

  /**
   * Sets the starting angle for a non-360 degrees cylinder.
   * @param angle the start angle in degrees, between 0 and 360
   */
  public void setMinimumAngle(int angle) {
    if (this.minAngle==angle) return;
    this.minAngle = Math.max(0,Math.min(360,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the starting angle for a possible non-360 degrees cylinder.
   * @return int the minimum angle
   */
  public int getMinimumAngle() { return this.minAngle; }

  /**
   * Sets the end angle for a non-360 degrees cylinder.
   * @param angle the end angle in degrees, between 0 and 360, 
   * and usually bigger than the minimum angle
   */
  public void setMaximumAngle(int angle) {
    if (this.maxAngle==angle) return;
    this.maxAngle = Math.max(0,Math.min(360,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the end angle for a possible non-360 degrees cylinder.
   * @return int the maximum angle
   */
  public int getMaximumAngle() { return this.maxAngle; }
  
  
  /**
   * Returns true if the cylinder is a complete primitive shape.
   * @return boolean whether the cylinder is complete
   */
  public boolean checkStandarCylinder(){
	 if(closedBottom && closedTop && minAngle==0 && maxAngle==360) return true;
	 return false;
  }
  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------
  
  // ------------------------------------
  // Utility methods
  // -------------------------------------

  /**
   * Returns the data for a standard cylinder (from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) )
   * with the given parameters
   */
  static public double[][][] createStandardCylinder(int nr, int nu, int nz, 
      double angle1, double angle2, 
      boolean top, boolean bottom, boolean left, boolean right) {
	  int totalN = nu*nz;
	  if (bottom) totalN += nr*nu;
	  if (top)  totalN += nr*nu;
	  if (Math.abs(angle2-angle1)<360) {
      if (left)  totalN += nr*nz;
      if (right) totalN += nr*nz;
    }
    double[][][] data = new double[totalN][4][3];
    // Compute sines and cosines
    double[] cosu = new double[nu+1], sinu = new double[nu+1];
    for(int u = 0;u<=nu;u++) {     // compute sines and cosines
      double angle = ((nu-u)*angle1+u*angle2)*Element.TO_RADIANS/nu;
      cosu[u] = Math.cos(angle)/2; // The /2 is because the element is centered
      sinu[u] = Math.sin(angle)/2;
    }
    // Now compute the tiles
    int tile = 0;
    double[] vectorx = Element.X_UNIT_VECTOR;
    double[] vectory = Element.Y_UNIT_VECTOR;
    double[] vectorz = Element.Z_UNIT_VECTOR;

    double[] center = new double[] {-vectorz[0]/2, -vectorz[1]/2, -vectorz[2]/2};
    {                                     // Tiles along the z axis
      double aux = 1.0/nz;
      for(int j = 0;j<nz;j++) {
        for(int u = 0;u<nu;u++, tile++) { // This ordering is important for the computations below (see ref)
          for(int k = 0;k<3;k++) {
            data[tile][0][k] = center[k]+cosu[u]*vectorx[k]+sinu[u]*vectory[k]+j*aux*vectorz[k];
            data[tile][1][k] = center[k]+cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k]+j*aux*vectorz[k];
            data[tile][2][k] = center[k]+cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k]+(j+1)*aux*vectorz[k];
            data[tile][3][k] = center[k]+cosu[u]*vectorx[k]+sinu[u]*vectory[k]+(j+1)*aux*vectorz[k];
          }
        }
      }
    }
    if (bottom) { // Tiles at bottom
      for(int u = 0;u<nu;u++) {
        for(int i = 0;i<nr;i++, tile++) {
          for(int k = 0;k<3;k++) {
            data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr; // should be ref+u
            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
            data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr; // should be ref+u
          }
        }
      }
    }
    if (top) { // Tiles at top
      int ref = nu*(nz-1);
      center[0] = vectorz[0];
      center[1] = vectorz[1];
      center[2] = vectorz[2]-0.5;
      for(int u = 0;u<nu;u++) {
        for(int i = 0;i<nr;i++, tile++) {
          for(int k = 0;k<3;k++) {
            data[tile][0][k] = ((nr-i)*center[k]+i*data[ref+u][3][k])/nr;
            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][3][k])/nr;
            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][2][k])/nr;
            data[tile][3][k] = ((nr-i)*center[k]+i*data[ref+u][2][k])/nr;
          }
        }
      }
    }
    if (Math.abs(angle2-angle1)<360) { // No need to close left or right if the Cylinder is 'round' enough
      center[0] = -vectorz[0]/2;
      center[1] = -vectorz[1]/2;
      center[2] = -vectorz[2]/2;
      if(right) { // Tiles at right
        double aux = 1.0/nz;
        for(int j = 0;j<nz;j++) {
          for(int i = 0;i<nr;i++, tile++) {
            for(int k = 0;k<3;k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[0][0][k])/nr+j*aux*vectorz[k];
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[0][0][k])/nr+j*aux*vectorz[k];
              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[0][0][k])/nr+(j+1)*aux*vectorz[k];
              data[tile][3][k] = ((nr-i)*center[k]+i*data[0][0][k])/nr+(j+1)*aux*vectorz[k];
            }
          }
        }
      }
      if(left) { // Tiles at left
        double aux = 1.0/nz;
        int ref = nu-1;
        for(int j = 0;j<nz;j++) {
          for(int i = 0;i<nr;i++, tile++) {
            for(int k = 0;k<3;k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr+j*aux*vectorz[k];
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr+j*aux*vectorz[k];
              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr+(j+1)*aux*vectorz[k];
              data[tile][3][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr+(j+1)*aux*vectorz[k];
            }
          }
        }
      }
    }
    return data;
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
