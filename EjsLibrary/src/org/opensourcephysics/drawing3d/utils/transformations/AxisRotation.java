/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils.transformations;

import org.opensourcephysics.drawing3d.Element;

/**
 * XAxisRotation implements a 3D rotation around the X axis.
 */
public abstract class AxisRotation extends org.opensourcephysics.numerics.Matrix3DTransformation {

  protected double angle;
  protected Element mElement;

  protected AxisRotation() {
    super(null);
    angle = 0; // matrix is then correct
    inverseMatrix = new double[][] {{1,0,0}, {0,1,0}, {0,0,1}};
  }

  /**
   * Sets the element the rotation applies to
   * @param element
   */
  public void setElement(Element element) {
    this.mElement = element;
  }
  
  @Override
  public double[] setOrigin(double[] origin) {
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    return super.setOrigin(origin);
  }

  @Override
  public void setOrigin(double ox, double oy, double oz) {
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    super.setOrigin(ox,oy,oz);
  }
    
  /**
   * Sets the angle of the rotation
   * @param theta
   * @returns true if there was a real change
   */
  public boolean setAngle(double theta) {
    if (angle==theta) return false;
    angle = theta;
    computeMatrix(Math.sin(angle),Math.cos(angle));
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    return true;
  }
  
  /**
   * Returns the angle of the rotation
   * @return
   */
  public double getAngle() { return this.angle; }
  
  /**
   * Computes the components of the matrix
   * @param sin
   * @param cos
   */
  abstract protected void computeMatrix(double sin, double cos);
  
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
