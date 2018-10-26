/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;

import java.awt.Color;

/**
 * A Mesh object contains information about the division of a region in the plane or space
 * into cells connecting points. The mesh includes information of:
 * <ul>
 *   <li>The coordinates of a number of points in the plane or in space.</li>
 *   <li>The cells of the region, i.e. how these points form cells as (planar) polygons or (space) polyhedra.</li>
 *   <li>The boundary of the region, i.e. how these points form (planar) segments or (space) polygons on the boundary of the region.</li>
 *   <li>A label for each boundary component (segment or polygon) that can be used to distinguish connected subsets of the boundary</li>
 * </ul>
 * Each point has a field value associated to it. The field can be:
 * <ul>
 *   <li>real scalar: the last dimension is 1</li>
 *   <li>complex scalar: the last dimension is 2</li>
 *   <li>real 2D vector: the last dimension is 3, but the third coordinate is always 0</li>
 *   <li>complex 2D vector: the last dimension is 4: r1,c1, r2,c2</li>
 *   <li>real 3D vector: the last dimension is 3</li>
 *   <li>complex 3D vector: the last dimension is 6: r1,c1, r2,c2, r3,c3 </li>
 * </ul>   
 */
public interface Mesh {

  /**
   * Provides the array of points that conform the tile and its boundary
   * @param points The double[nPoints][dim] where:
   * <ul>
   * <li> nPoints : The number of points in the tile
   * <li> dim: 2 for two-dimensional tiles, 3 for 3D tiles
   * </ul>
   */
  public void setPoints(double[][] points);

  /**
   * Provides the geometry of the tile
   * @param cells the int[nCells][nPoints] array with the points in each cells.
   * For example,if the first cell is a cell joining points 0,3,7, one gets: cells[0] = { 0, 3, 7 }; 
   */
  public void setCells(int[][] cells);

  /**
   * Provides the data for the boundary.
   * @param tiles The int[nOfSegments][nPointsInSegment] array with the tile information, where:
   * <ul>
   * <li>First index = nOfSegments : number of segments in the boundary</li>
   * <li>Second index = nPointsInSegment : the points in this segment</li>
   * </ul>
   */
  public void setBoundary(int[][] boundary);
  
  /**
   * Provides the label for each boundary segment
   * @param values The int[nOfSegments] array with the label for each boundary segment
   */
  public void setBoundaryLabels(int[] labels);
  
  /**
   * The color to use for each boundary index 
   * There must be a color for each index 
   * If not enough colors are given, or colors is null, the element draw color is used
   * @param colors
   */
  public void setBoundaryColors(Color[] colors);
  
  /**
   * Provides the field value for each point in the tile or boundary
   * @param values The double[nPoints][] array with the value for each tile vertex
   */
  public void setFieldAtPoints(double[][] values);
  
  /**
   * Provides the field value for the points in each tile
   * @param values The double[nTiles][nPointsPerTile][] array with the value for each tile vertex
   */
  public void setFieldAtCells(double[][][] values);

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
