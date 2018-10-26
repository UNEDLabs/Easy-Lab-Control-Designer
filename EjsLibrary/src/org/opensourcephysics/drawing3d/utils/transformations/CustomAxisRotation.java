/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils.transformations;

import org.opensourcephysics.drawing3d.Element;

/**
 * AxisRotation implements a 3D rotation around a given axis.
 */
public class CustomAxisRotation extends AxisRotation {

  private double[] axis = new double[]{1,0,0};
  private double x=1,y=0,z=0; // normalized components of the axis
  private double cos=1, sin=0; // sine and cosine of the angle

  @Override
  public boolean setAngle(double theta) {
    return setAngleAndAxis(theta, axis);
  }
  
  /**
   * Sets the axis of the rotation
   * @param axis
   * @returns true if there was a real change
   */
  public boolean setAxis(double[] newAxis) {
    return setAngleAndAxis(angle, newAxis);
  }
  
  /**
   * Returns a copy of the axis of the rotation
   * @return
   */
  public double[] getAxis() { return axis.clone(); }

  /**
   * Sets the axis and angle of the rotation
   * @param theta
   * @param axis
   * @returns true if there was a real change
   */
  public boolean setAngleAndAxis(double theta, double[] newAxis) {
    boolean changed=false;
    if (!axis.equals(newAxis)) {
      System.arraycopy(newAxis, 0, axis, 0, 3);
      // normalize the components
      x = axis[0]; 
      y = axis[1];
      z = axis[2];
      double norm = x*x+y*y+z*z;
      if (norm!=1) { // this usually doesn't happen because of roundoff but is worth a try
        norm = 1/Math.sqrt(norm);
        x *= norm;
        y *= norm;
        z *= norm;
      }
      changed = true;
    }
    if (angle!=theta) {
      angle = theta;
      sin = Math.sin(angle);
      cos = Math.cos(angle);
      changed = true;
    }
    if (changed) {
      computeMatrix(sin,cos);
      if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    }
    return changed;
  }
  
  protected void computeMatrix(double s, double c) {
    double t = 1-c;
    // matrix elements not listed are zero
    matrix[0][0] = t*x*x+c;
    matrix[0][1] = t*x*y-s*z;
    matrix[0][2] = t*x*z+s*y;
    matrix[1][0] = t*x*y+s*z;
    matrix[1][1] = t*y*y+c;
    matrix[1][2] = t*y*z-s*x;
    matrix[2][0] = t*x*z-s*y;
    matrix[2][1] = t*y*z+s*x;
    matrix[2][2] = t*z*z+c;
    inverseMatrix[0][0] = matrix[0][0];
    inverseMatrix[1][0] = matrix[0][1];
    inverseMatrix[2][0] = matrix[0][2];
    inverseMatrix[0][1] = matrix[1][0];
    inverseMatrix[1][1] = matrix[1][1];
    inverseMatrix[2][1] = matrix[1][2];
    inverseMatrix[0][2] = matrix[2][0];
    inverseMatrix[1][2] = matrix[2][1];
    inverseMatrix[2][2] = matrix[2][2];
  }

  /**
   * Provides a copy of this transformation.
   */
  public Object clone() {
    CustomAxisRotation m = new CustomAxisRotation();
    m.setAngleAndAxis(this.getAngle(),this.getAxis());
    m.origin = origin.clone();
    return m;
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
