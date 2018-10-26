/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import org.opensourcephysics.drawing3d.ElementSurface;

/**
 * <p>Title: SimpleElementSurface</p>
 * <p>Description: Painter's algorithm implementation of a surface</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementSurface extends SimpleAbstractTile {

  public SimpleElementSurface(ElementSurface _element) { super(_element); }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  
  @Override
  protected double[][][] computeTile() {
    double[][][] data = ((ElementSurface) element).getData();
    if (data==null || data.length==0) return new double[0][0][3];
    int nu = data.length-1, nv=data[0].length-1;
    double [][][] coord = new double[nu*nv][4][3];
    int tile = 0;
    for (int v = 0; v<nv; v++) {
      for (int u = 0; u<nu; u++, tile++) {
        for (int k = 0; k<3; k++) {
          coord[tile][0][k] = data[u][v][k];
          coord[tile][1][k] = data[u+1][v][k];
          coord[tile][2][k] = data[u+1][v+1][k];
          coord[tile][3][k] = data[u][v+1][k];
        }
      }
    }
    return coord;
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
