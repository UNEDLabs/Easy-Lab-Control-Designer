/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils.transformations;

/**
 * YAxisRotation implements a 3D rotation around the Y axis.
 */
public class YAxisRotation extends AxisRotation {

  @Override
  protected void computeMatrix(double sin, double cos) {
    // matrix elements not listed are zero
    matrix[0][0] = cos;
    matrix[0][2] = sin;
    matrix[2][0] = -sin;
    matrix[2][2] = cos;
    inverseMatrix[0][0] = cos;
    inverseMatrix[0][2] = -sin;
    inverseMatrix[2][0] = sin;
    inverseMatrix[2][2] = cos;
  }

  /**
   * Provides a copy of this transformation.
   */
  public Object clone() {
    YAxisRotation m = new YAxisRotation();
    m.setAngle(angle);
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
