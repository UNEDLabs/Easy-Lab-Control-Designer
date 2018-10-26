/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.drawing3d.utils.Resolution;

/**
 * <p>Title: SimpleElementObject</p>
 * <p>Description: Painter's algorithm implementation of an Object</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementObject extends SimpleAbstractTile {

  public SimpleElementObject(ElementObject _element) { super(_element); }

  @Override
  protected double[][][] computeTile() {
    int nx = 1, ny = 1, nz = 1;
    Resolution res = style.getResolution();
    if (res!=null) {
      switch (res.getType()) {
        case Resolution.DIVISIONS :
          nx = Math.max(res.getN1(), 1);
          ny = Math.max(res.getN2(), 1);
          nz = Math.max(res.getN3(), 1);
          break;
        case Resolution.MAX_LENGTH :
          nx = Math.max((int) Math.round(0.49+Math.abs(element.getSizeX())/res.getMaxLength()), 1);
          ny = Math.max((int) Math.round(0.49+Math.abs(element.getSizeY())/res.getMaxLength()), 1);
          nz = Math.max((int) Math.round(0.49+Math.abs(element.getSizeZ())/res.getMaxLength()), 1);
          break;
      }
    }
    // reallocate arrays
    return ElementBox.createStandardBox(nx, ny, nz, true, true,1);
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
