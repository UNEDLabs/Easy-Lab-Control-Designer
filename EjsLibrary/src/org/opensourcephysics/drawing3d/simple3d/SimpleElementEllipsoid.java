/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementEllipsoid;
import org.opensourcephysics.drawing3d.utils.Resolution;

/**
 * <p>Title: SimpleElementEllipsoid</p>
 * <p>Description: Painter's algorithm implementation of a ellipsoid</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementEllipsoid extends SimpleAbstractTile {

  public SimpleElementEllipsoid(ElementEllipsoid _element) { super(_element); }

  @Override
  protected double[][][] computeTile() {
    ElementEllipsoid ellipsoid = (ElementEllipsoid) element;
    int nr = 1, nu = 1, nv = 1;
    double angleu1 = ellipsoid.getMinimumAngleU(), angleu2 = ellipsoid.getMaximumAngleU();
    if (Math.abs(angleu2-angleu1)>360) angleu2 = angleu1+360;
    double anglev1 = ellipsoid.getMinimumAngleV(), anglev2 = ellipsoid.getMaximumAngleV();
    if (Math.abs(anglev2-anglev1)>180) anglev2 = anglev1+180;
    Resolution res = style.getResolution();
    if(res!=null) {
      switch(res.getType()) {
      case Resolution.DIVISIONS :
        nr = Math.max(res.getN1(), 1);
        nu = Math.max(res.getN2(), 1);
        nv = Math.max(res.getN3(), 1);
        break;
      case Resolution.MAX_LENGTH :
        double maxRadius = Math.max(Math.max(Math.abs(element.getSizeX()), Math.abs(element.getSizeY())), Math.abs(element.getSizeZ()))/2;
        nr = Math.max((int) Math.round(0.49+maxRadius/res.getMaxLength()), 1);
        nu = Math.max((int) Math.round(0.49+Math.abs(angleu2-angleu1)*Element.TO_RADIANS*maxRadius/res.getMaxLength()), 1);
        nv = Math.max((int) Math.round(0.49+Math.abs(anglev2-anglev1)*Element.TO_RADIANS*maxRadius/res.getMaxLength()), 1);
        break;
      }
    }
    return ElementEllipsoid.createStandardEllipsoid(nr, nu, nv, 
    	angleu1, angleu2, anglev1, anglev2, 
        ellipsoid.isClosedTop(), ellipsoid.isClosedBottom(), ellipsoid.isClosedLeft(), ellipsoid.isClosedRight());
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
