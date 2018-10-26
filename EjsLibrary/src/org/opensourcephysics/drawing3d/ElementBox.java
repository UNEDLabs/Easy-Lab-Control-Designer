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
 * <p>Title: ElementBox</p>
 * <p>Description: A 3D Box</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementBox extends Element {

	//Configuration variables
	private boolean closedBottom = true;
	private boolean closedTop = true;
	private double sizeZreduction=1.0;
  
  {
    getStyle().setResolution(new Resolution(3, 3, 3));
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementBox(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementBox(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Set the relative reduction in size along the Z axis. Default is 1.0.
   * @param reduction A number between 0 and 1 that multiplies the size of the element along the Z axis
   */
  public void setSizeZReduction(double reduction){
    if (this.sizeZreduction==reduction) return;
    sizeZreduction = reduction;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the relative reduction in size along the Z axis.
   */
  public double getSizeZReduction() { return this.sizeZreduction; }

  /**
   * Whether the box should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close){
    if (closedBottom==close) return;
	  closedBottom = close;
	  addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the box is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom() { return closedBottom; }

  /**
   * Whether the box should be closed at its top.
   * @param close the desired value
   */
  public void setClosedTop(boolean close){
    if (closedTop==close) return;
	closedTop = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the box is closed at its top.
   * @return the value
   */
  public boolean isClosedTop() { return closedTop; }

  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------
  
  // ------------------------------------
  // Utility methods
  // -------------------------------------

  /**
   * Returns the data for a standard box (from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) )
   * with the given parameters
   */
  static public double[][][] createStandardBox(int nx, int ny, int nz, boolean top, boolean bottom, double rz) {
    int nTotal = 2*nx*nz+2*ny*nz;
    if (bottom) nTotal += nx*ny;
    if (top)    nTotal += nx*ny;
    if (rz==0) nTotal -= ny*nz;
    double[][][] data = new double[nTotal][4][3];
    int tile = 0;
    double dx = 1.0/nx, dy = 1.0/ny;
    for (int i = 0; i<nx; i++) { // x-y sides
      double theX = i*dx;
      double topZ = 1-theX*(1-rz)-0.5;
      double topZdx = topZ-dx*(1-rz);
      theX -= 0.5;
      for (int j = 0; j<ny; j++) {
        double theY = j*dy-0.5;
        if (bottom) {
          data[tile][0][0] = theX;
          data[tile][0][1] = theY;
          data[tile][0][2] = -0.5;
          data[tile][1][0] = theX+dx;
          data[tile][1][1] = theY;
          data[tile][1][2] = -0.5;
          data[tile][2][0] = theX+dx;
          data[tile][2][1] = theY+dy;
          data[tile][2][2] = -0.5;
          data[tile][3][0] = theX;
          data[tile][3][1] = theY+dy;
          data[tile][3][2] = -0.5;
          tile++;
        }
        if (top) {             // The upper side
          data[tile][0][0] = theX;
          data[tile][0][1] = theY;
          data[tile][0][2] = topZ;
          data[tile][1][0] = theX+dx;
          data[tile][1][1] = theY;
          data[tile][1][2] = topZdx;
          data[tile][2][0] = theX+dx;
          data[tile][2][1] = theY+dy;
          data[tile][2][2] = topZdx;
          data[tile][3][0] = theX;
          data[tile][3][1] = theY+dy;
          data[tile][3][2] = topZ;
          tile++;
        }
      }
    }
    for (int i = 0; i<nx; i++) { // x-z sides
      double theX = i*dx;
      double topZ = 1-theX*(1-rz), topZdx = topZ - dx*(1-rz);
      double dz = topZ/nz, dzdx = topZdx/nz;
      theX -= 0.5;
      for (int k = 0; k<nz; k++) {
        double theZ = k*dz-0.5;
        double theZdx = k*dzdx-0.5;
        data[tile][0][0] = theX;
        data[tile][0][2] = theZ;
        data[tile][0][1] = -0.5;
        data[tile][1][0] = theX+dx;
        data[tile][1][2] = theZdx;
        data[tile][1][1] = -0.5;
        data[tile][2][0] = theX+dx;
        data[tile][2][2] = theZdx+dzdx;
        data[tile][2][1] = -0.5;
        data[tile][3][0] = theX;
        data[tile][3][2] = theZ+dz;
        data[tile][3][1] = -0.5;
        tile++;               // The upper side
        data[tile][0][0] = theX;
        data[tile][0][2] = theZ;
        data[tile][0][1] = 0.5;
        data[tile][1][0] = theX+dx;
        data[tile][1][2] = theZdx;
        data[tile][1][1] = 0.5;
        data[tile][2][0] = theX+dx;
        data[tile][2][2] = theZdx+dzdx;
        data[tile][2][1] = 0.5;
        data[tile][3][0] = theX;
        data[tile][3][2] = theZ+dz;
        data[tile][3][1] = 0.5;
        tile++;
      }
    }
    double dz = 1.0/nz, dzend = rz/nz;
    for (int k = 0; k<nz; k++) { // y-z sides
      double theZ = k*dz-0.5;
      double theZend = k*dzend-0.5;
      for (int j = 0; j<ny; j++) {
        double theY = j*dy-0.5;
        data[tile][0][2] = theZ;
        data[tile][0][1] = theY;
        data[tile][0][0] = -0.5;
        data[tile][1][2] = theZ+dz;
        data[tile][1][1] = theY;
        data[tile][1][0] = -0.5;
        data[tile][2][2] = theZ+dz;
        data[tile][2][1] = theY+dy;
        data[tile][2][0] = -0.5;
        data[tile][3][2] = theZ;
        data[tile][3][1] = theY+dy;
        data[tile][3][0] = -0.5;
        tile++;               // The upper side
        if (rz!=0.0) {
          data[tile][0][2] = theZend;
          data[tile][0][1] = theY;
          data[tile][0][0] = 0.5;
          data[tile][1][2] = theZend+dzend;
          data[tile][1][1] = theY;
          data[tile][1][0] = 0.5;
          data[tile][2][2] = theZend+dzend;
          data[tile][2][1] = theY+dy;
          data[tile][2][0] = 0.5;
          data[tile][3][2] = theZend;
          data[tile][3][1] = theY+dy;
          data[tile][3][0] = 0.5;
          tile++;
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
