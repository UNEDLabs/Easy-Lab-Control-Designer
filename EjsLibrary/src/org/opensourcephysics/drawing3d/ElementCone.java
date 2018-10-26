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
 * <p>Title: ElementCone</p>
 * <p>Description: A 3D cone</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementCone extends Element {

  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private boolean closedLeft = true, closedRight = true;
  private int minAngle = 0, maxAngle = 360;
  private double truncationHeight = Double.NaN;
  
  {
    getStyle().setResolution(new Resolution(3, 12, 5));
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementCone(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementCone(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Whether the cone should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close){
    if (closedBottom==close) return;
	  closedBottom = close;
	  addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the cone is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom() { return closedBottom; }

  /**
   * Whether the (truncated) cone should be closed at its top.
   * @param close the desired value
   */
  public void setClosedTop(boolean close){
    if (closedTop==close) return;
	  closedTop = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (truncated) cone is closed at its top.
   * @return the value
   */
  public boolean isClosedTop() { return closedTop; }

  /**
   * Whether the (non-360 degrees) cone should be closed at its left side.
   * @param close the desired value
   */
  public void setClosedLeft(boolean close) {
    if (closedLeft==close) return;
    closedLeft = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (non-360 degrees) cone is closed at its left side.
   * @return the value
   */
  public boolean isClosedLeft() { return closedLeft; }

  /**
   * Whether the (non-360 degrees) cone should be closed at its right side.
   * @param close the desired value
   */
  public void setClosedRight(boolean close) {
    if (closedRight==close) return;
    closedRight = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (non-360 degrees) cone is closed at its right side.
   * @return the value
   */
  public boolean isClosedRight() { return closedRight; }

  /**
   * Sets the height at which to truncate the cone
   * @param height The desired height of the truncated cone. 
   * Double.NaN or negative for a non truncated cone.
   */
  public void setTruncationHeight(double height) {
    if (truncationHeight==height) return;
    if (height<0) height = Double.NaN;
    truncationHeight = height;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the height at which to truncate the cone
   * @return The height of the truncated cone. Double.NaN for a non truncated cone.
   */
  public double getTruncationHeight() { return truncationHeight; }

  /**
   * Sets the starting angle for a non-360 degrees cone.
   * @param angle the start angle in degrees, between 0 and 360
   */
  public void setMinimumAngle(int angle) {
    if (this.minAngle==angle) return;
    this.minAngle = Math.max(0,Math.min(360,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the starting angle for a possible non-360 degrees cone.
   * @return int the minimum angle
   */
  public int getMinimumAngle() { return this.minAngle; }

  /**
   * Sets the end angle for a non-360 degrees cone.
   * @param angle the end angle in degrees, between 0 and 360, 
   * and usually bigger than the minimum angle
   */
  public void setMaximumAngle(int angle) {
    if (this.maxAngle==angle) return;
    this.maxAngle = Math.max(0,Math.min(360,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the end angle for a possible non-360 degrees cone.
   * @return int the maximum angle
   */
  public int getMaximumAngle() { return this.maxAngle; }
  
  
  /**
   * Returns true if the cone is a complete primitive shape.
   * @return boolean whether the cone is complete
   */
  public boolean checkStandarCone(){
	if(closedBottom && minAngle==0 && maxAngle==360 && Double.isNaN(truncationHeight)) return true;
	return false;
  }
  
  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------
  
  // ------------------------------------
  // Utility methods
  // -------------------------------------

  /**
   * Returns the data for a standard cone (from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) )
   * with the given parameters
   */
  static public double[][][] createStandardCone(int nr, int nu, int nz, 
    double angle1, double angle2, 
    boolean top, boolean bottom, boolean left, boolean right, double height) 
  {
    int totalN = nu*nz;
    if (bottom)  totalN += nr*nu;
    if (!Double.isNaN(height)&& top) totalN += nr*nu;
    if (Math.abs(angle2-angle1)<360) {
      if (left) totalN += nr*nz;
      if (right) totalN += nr*nz;
    }
    double[][][] data = new double[totalN][4][3];
    // Compute sines and cosines
    double[] cosu = new double[nu+1], sinu = new double[nu+1];
    for(int u = 0;u<=nu;u++) {     // compute sines and cosines
      double angle = ((nu-u)*angle1+u*angle2)*TO_RADIANS/nu;
      cosu[u] = Math.cos(angle)/2; // The /2 is because the element is centered
      sinu[u] = Math.sin(angle)/2;
    }
    // Now compute the tiles
    int tile = 0;
    double[] vectorx = Element.X_UNIT_VECTOR;
    double[] vectory = Element.Y_UNIT_VECTOR;
    double[] vectorz = Element.Z_UNIT_VECTOR;
    double[] center = new double[] {-vectorz[0]/2, -vectorz[1]/2, -vectorz[2]/2 };
    double theNz;
    if (Double.isNaN(height)) theNz = nz;
    else if(height==0) theNz = Integer.MAX_VALUE;
    else theNz = nz/height;
    double auxZ = 1.0/theNz;
    for (int j = 0; j<nz; j++) {
      for (int u = 0; u<nu; u++, tile++) { // This ordering is important for the computations below (see ref)
        for (int k = 0; k<3; k++) {
          data[tile][0][k] = center[k]+(cosu[u]*vectorx[k]+sinu[u]*vectory[k])*(theNz-j)/theNz+j*auxZ*vectorz[k];
          data[tile][1][k] = center[k]+(cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*(theNz-j)/theNz+j*auxZ*vectorz[k];
          data[tile][2][k] = center[k]+(cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*(theNz-j-1)/theNz+(j+1)*auxZ*vectorz[k];
          data[tile][3][k] = center[k]+(cosu[u]*vectorx[k]+sinu[u]*vectory[k])*(theNz-j-1)/theNz+(j+1)*auxZ*vectorz[k];
        }
      }
    }

    if (bottom) {  // Tiles at bottom
      for (int u = 0; u<nu; u++) {
        for (int i = 0; i<nr; i++, tile++) {
          for (int k = 0; k<3; k++) {
            data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr; // should be ref+u
            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
            data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr; // should be ref+u
          }
        }
      }
    }
    if (!Double.isNaN(height) && top) { // Tiles at top
      int ref = nu*(nz-1);
      center[0] = vectorz[0];
      center[1] = vectorz[1];
      if (Double.isNaN(height)) center[2] = vectorz[2]-0.5;
      else center[2] = height*vectorz[2]-0.5;
      for (int u = 0; u<nu; u++) {
        for (int i = 0; i<nr; i++, tile++) {
          for (int k = 0; k<3; k++) {
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
      if (right) { // Tiles at right
        int ref = 0;
        double N;
        double[] nextCenter = new double[3];
        if (Double.isNaN(height)) N = nz;
        else if (height==0)  N = Integer.MAX_VALUE;
        else N = nz/height;
        double aux = 1.0/N;
        for (int j = 0; j<nz; j++, ref += nu) {
          center[0] = j*aux*vectorz[0];
          center[1] = j*aux*vectorz[1];
          center[2] = j*aux*vectorz[2]-0.5;
          nextCenter[0] = (j+1)*aux*vectorz[0];
          nextCenter[1] = (j+1)*aux*vectorz[1];
          nextCenter[2] = (j+1)*aux*vectorz[2]-0.5;
          for (int i = 0; i<nr; i++, tile++) {
            for (int k = 0; k<3; k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][0][k])/nr;
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][0][k])/nr;
              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][3][k])/nr;
              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][3][k])/nr;
            }
          }
        }
      }
      if (left) { // Tiles at left
        int ref = nu-1;
        double N;
        double[] nextCenter = new double[3];
        if (Double.isNaN(height)) N = nz;
        else if (height==0) N = Integer.MAX_VALUE;
        else N = nz/height;
        double aux = 1.0/N;
        for (int j = 0; j<nz; j++, ref += nu) {
          center[0] = j*aux*vectorz[0];
          center[1] = j*aux*vectorz[1];
          center[2] = j*aux*vectorz[2]-0.5;
          nextCenter[0] = (j+1)*aux*vectorz[0];
          nextCenter[1] = (j+1)*aux*vectorz[1];
          nextCenter[2] = (j+1)*aux*vectorz[2]-0.5;
          for (int i = 0; i<nr; i++, tile++) {
            for (int k = 0; k<3; k++) {
              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr;
              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr;
              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][2][k])/nr;
              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][2][k])/nr;
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
