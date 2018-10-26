package org.colos.freefem;

import java.util.HashMap;
import java.util.Map;

import org.colos.ejs.library.server.DataMapExportable;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author Mar�a Jos� Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

/**
 * A Mesh object contains information about the division of a region in the plane or space
 * into cells connecting points. The mesh includes information of:
 * <ul>
 *   <li>The coordinates of a number of points in the plane or in space.</li>
 *   <li>The cells of the region, i.e. how these points form cells as (planar) polygons or (space) polyhedra.</li>
 *   <li>The boundary of the region, i.e. how these points form (planar) segments or (space) polygons on the boundary of the region.</li>
 *   <li>A label for each boundary component (segment or polygon) that can be used to distinguish connected subsets of the boundary</li>
 * </ul>
 */
public class PDEMesh implements DataMapExportable {

  private double[][] mPoints;
  private int[][] mCells;
  private int[][] mBoundaryComponents;
  private int[] mBoundaryLabels;

  /**
   * Creates a PDEMesh with the given information
   * @param points the double[nPoints][2 or 3] array with the coordinates of all points in the cell 
   * @param cells the int[nCells][nPointsInCell] array with the indexes of the points that make each cell
   * @param boundaryComponents the int[nSegments][nPointsInSegment] array with the indexes of the points in each boundary segment
   * @param boundaryLabels the int[nSegments] array with the integer label for each boundary segment
   */
  public PDEMesh(double[][] points, int[][] cells, int[][]  boundaryComponents, int[] boundaryLabels) {
    mPoints = points;
    mCells = cells;
    mBoundaryComponents = boundaryComponents;	
    mBoundaryLabels = boundaryLabels;
  }

  /**
   * Provides the list of all the points in the mesh. Each point has 2 or 3 coordinates. 
   */
  public double[][] getPoints() { return mPoints; }

  /**
   * Provides the indexes of thepoints in each cell of the mesh
   * @return the int[nCells][nPointsInCell] array with the indexes (in array returned by getPoints())
   * of the points of each cell 
   */
  public int[][] getCells() { return mCells; }
  
  /**
   * Provides the indexes of the points in each boundary element of the mesh
   * @return the int[nSegment][nPointsInSegment] array with the indexes (in the array returned by getPoints())
   * of the points of each cell 
   */
  public int[][] getBoundaryElements() { return mBoundaryComponents; }

  /**
   * Returns an array with the label for each boundary element
   * @return
   */
  public int[] getBoundaryLabels() { return this.mBoundaryLabels; }

//-----------------------------
 // JSON utilities
 // -----------------------------
 
 
 public Map<String,Object> toDataMap () {
   Map<String, Object> dataMap = new HashMap<String, Object>();
   dataMap.put("points", mPoints);
   dataMap.put("cells", mCells);
   dataMap.put("boundary", mBoundaryComponents);
   dataMap.put("boundary_labels", mBoundaryLabels);
   return dataMap;
 }

  
  
}
