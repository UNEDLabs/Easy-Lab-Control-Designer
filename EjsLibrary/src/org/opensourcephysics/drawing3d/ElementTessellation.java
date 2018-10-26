/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing2d.utils.ColorCodedDrawer;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementTesselation</p>
 * <p>Description: A Tesselation is a collection of planar tiles</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementTessellation extends Element {
  // Configuration variables
  private double[][][] mTileData = new double[0][0][3];
  private double[][] mTileZ = new double[0][0];
  private boolean mAutoscaleZ=true; 

  // Implementation variables
  private ColorCodedDrawer mDrawer = new ColorCodedDrawer();

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementTessellation(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementTessellation(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets the mTileData of the tiles. Each tile is a polygon with possible different number of vertex.
   * @param tiles the double[][][3] array of coordinates for the surface.
   */
  public void setTiles(double[][][] tiles) {
    this.mTileData = tiles;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Gets the mTileData of the surface.
   * @return the double[nu][nv][3] the actual array of coordinates of the surface (not a copy)
   */
  public double[][][] getTiles() { return this.mTileData; }

  /**
   * Provides the value for each vertex of the tile
   * @param values The double[nOfTiles][nPointsInTile] array with the value for each tile vertex
   */
  public void setValues(double[][] values) {
    mTileZ = values;
    if (mTileZ!=null) addChange(Element.CHANGE_COLOR);
  }
  
  public double[][] getValues() { return this.mTileZ; }
  
  /**
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z are span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param floor
   * @param ceil
   */
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) {
    mAutoscaleZ = isAutoscale;
    if (!mAutoscaleZ) mDrawer.setScale(floor, ceil);
    addChange(Element.CHANGE_COLOR);
  }
  
  /**
   * Gets the autoscale flag for z.
   *
   * @return boolean
   */
  public boolean isAutoscaleZ() {
    return mAutoscaleZ;
  }
  
  /**
   * Returns the ColorCodedDrawer for customization
   * @return
   */
  public ColorCodedDrawer getDrawer() {
    return mDrawer;
  }
  
  public void checkScales() {
    if (mTileData==null) {
      return;
    }
    if (mAutoscaleZ && mTileZ!=null) {
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;
      for (int i=0, n=mTileZ.length; i<n; i++) {
        double[] tile = mTileZ[i];
        for (int j=0,m=tile.length; j<m; j++) {        
          double value = tile[j];
          max = Math.max (max, value);
          min = Math.min (min, value);            
        }
      }
      mDrawer.setAutoscale(min,max);
    }
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------

  public void getExtrema(double[] min, double[] max) {
    if (mTileData==null) {
      super.getExtrema(min, max);
      return;
    }
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] aPoint = new double[3];
    for(int i = 0,n=mTileData.length; i<n; i++) {
      for(int j = 0,sides=mTileData[i].length; j<sides; j++) {
        System.arraycopy(mTileData[i][j], 0, aPoint, 0, 3);
        sizeAndToSpaceFrame(aPoint);
        minX = Math.min(minX, aPoint[0]);
        maxX = Math.max(maxX, aPoint[0]);
        minY = Math.min(minY, aPoint[1]);
        maxY = Math.max(maxY, aPoint[1]);
        minZ = Math.min(minZ, aPoint[2]);
        maxZ = Math.max(maxZ, aPoint[2]);
      }
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
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
