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
public class QuaternionRotation extends org.opensourcephysics.numerics.Quaternion {

  protected Element mElement;

  public QuaternionRotation(double q0, double q1, double q2, double q3) {
    super(q0,q1,q2,q3);
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
    
  @Override
  public void setCoordinates(double q0, double q1, double q2, double q3) {
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    super.setCoordinates(q0,q1,q2,q3);
  }

  @Override
  public double[] setCoordinates(double[] q) {
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    return super.setCoordinates(q);
  }
  
  @Override
  public Object clone() {
    QuaternionRotation q = new QuaternionRotation(q0, q1, q2, q3);
    q.setOrigin(ox, oy, oz);
    return q;
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
