/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;

import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

import org.opensourcephysics.drawing2d.utils.ColorCodedDrawer;

/**
 * <p>Title: ElementMesh</p> A 2D implementation of a org.opensourcephysics.display.Mesh
 */
public class ElementMesh extends Element implements org.opensourcephysics.display.Mesh {

  // Configuration variables
  private double[][] mPoints;
  private double[][] mValues;
  private double[][][]mCellValues;
  private int[][] mCellsGeometry;
  private boolean mAutoscaleZ=true; 
  private double mVectorLength=1;

  private int[][] mBoundaryGeometry;
  private int[] mBoundaryLabels;
  private Color[] mBoundaryColors = null; // an array with a color for each index. If null, the element color is used
  private Stroke mBoundaryStroke = new BasicStroke(1);
  private boolean mBoundaryDraw = true;
  
  // Implementation variables
  private boolean mValuesChanged = false;
  private int mA[] = new int[0];
  private int mB[] = new int[0];
  private int mTileA[][] = new int[0][0];
  private int mTileB[][] = new int[0][0];
  private double mTileZ[][] = new double[0][0];
  private double mTileVector[][][] = new double[0][0][0];
  private int mBoundaryA[][] = new int[0][0];
  private int mBoundaryB[][] = new int[0][0];
  private ColorCodedDrawer mDrawer = new ColorCodedDrawer();
  private int mDimension = 1;
//  private VectorField mField;

  {
    setSize (new double[] {1,1} );
    getStyle().setRelativePosition(Style.NORTH_EAST);
//    mField = new VectorField();
//    mField.setVisible(false);
//    addElement(mField);
  }

  // -------------------------------------
  // Public methods for tiles geometry
  // -------------------------------------

  /**
   * Provides the array of points that conform the mesh and its boundary
   * @param points The double[nPoints][2] where nPoints is the number of points in the mesh
   */
  public void setPoints(double[][] points) {
    mPoints = points;
    setElementChanged();
  }

  /**
   * Provides the field value for each point in the mesh
   * @param values The double[nPoints][dimension] array with the value for each mesh vertex
   */
  public void setFieldAtPoints(double[][] values) {
    mValues = values;
    mDimension = (mValues==null) ? 1 : mValues[0].length;
    mCellValues = null;
    mValuesChanged = (mValues!=null);
//    mField.setVisible(mDimension>1);
  }

  /**
   * Provides the geometry of the mesh
   * @param tiles the int[nCells][nPoints] array with the points in each cell.
   * For example,if the first cell is a triangle joining points 0,3,7, one gets: cells[0] = { 0, 3, 7 }; 
   */
  public void setCells(int[][] cells) {
    mCellsGeometry = cells;
  }

  /**
   * Provides the field value for the points in each tile
   * @param values The double[nTiles][nPointsPerTile][dimension] array with the value for each cell vertex
   */
  public void setFieldAtCells(double[][][] values) {
    mCellValues = values;
    mDimension = (mCellValues==null) ? 1 : mCellValues[0][0].length;
    mValues = null;
    mValuesChanged = (mCellValues!=null);
//    mField.setVisible(mDimension>1);
  }
  
  /**
   * Provides the geometry of the mesh
   * @param length the length for vectors in the field 
   */
  public void setVectorLength(double length) {
    mVectorLength = length;
  }
  
  /**
   * Provides the data for the boundary.
   * @param tiles The int[nOfSegments][nPointsInSegment] array with the tile information, where:
   * <ul>
   * <li>First index = nOfSegments : number of segments in the boundary</li>
   * <li>Second index = nPointsInSegment : the points in this segment</li>
   * </ul>
   */
  public void setBoundary(int[][] boundary) {
    mBoundaryGeometry = boundary;
  }

  /**
   * Provides the label for each boundary segment
   * @param values The int[nOfSegments] array with the label for each boundary segment
   */
  public void setBoundaryLabels(int[] labels) {
    mBoundaryLabels = labels;
  }

  /**
   * The color to use for each boundary index 
   * There must be a color for each index 
   * If not enough colors are given, or colors is null, the element draw color is used
   * @param colors
   */
  public void setBoundaryColors(Color[] colors) {
    mBoundaryColors = colors;
  }
  
  public void setBoundaryLineWidth (float width) {
    mBoundaryStroke = new BasicStroke(Math.max(1, width));
  }

  public void setBoundaryStroke (Stroke stroke) {
    mBoundaryStroke = stroke;
  }

  public void setDrawBoundary(boolean draw) {
    mBoundaryDraw = draw;
  }

  // -------------------------------------
  // Public methods for coloring the tiles
  // -------------------------------------

  /**
   * Returns the ColorCodedDrawer for customization
   * @return
   */
  public ColorCodedDrawer getDrawer() {
    return mDrawer;
  }
  
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
    if (mAutoscaleZ) mValuesChanged = (mValues!=null || mCellValues!=null);
    else mDrawer.setScale(floor, ceil);
  }
  
  /**
   * Gets the autoscale flag for z.
   *
   * @return boolean
   */
  public boolean isAutoscaleZ() {
    return mAutoscaleZ;
  }
  
  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------

  @Override
  public Interactive findInteractive(DrawingPanel _panel, int _xpix, int _ypix) {
    return null; // Not interactive
  }

  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible()) return;
    if (mPoints==null) return;
    
    if (hasChanged() || needsToProject()) projectPoints(_panel);

    Graphics2D g2 = (Graphics2D) _g;
    boolean fillTiles = getStyle().isDrawingFill();
    Color color = getStyle().getLineColor();
    if (!getStyle().isDrawingLines()) color = null;
    g2.setStroke(getStyle().getLineStroke());

    // First the tiles
    if (mCellsGeometry!=null) {
      if (mValues!=null || mCellValues!=null) {
        if (mAutoscaleZ && mValuesChanged) {
          setScales();
          mValuesChanged = false;
        }
        for (int i=0, n=mCellsGeometry.length; i<n; i++) {
          int[] as = mTileA[i];
          int[] bs = mTileB[i];
          if (fillTiles) mDrawer.drawColorCoded(g2,as,bs,mTileZ[i], color==null);
          if (color!=null) {
            g2.setColor(color);
            g2.drawPolygon(as,bs,as.length);
          }
        }
      }
      else {
        if (fillTiles) {
          Paint fill = getStyle().getFillColor();
          if (fill!=null) g2.setPaint(fill);
          else fillTiles = false;
        }
        if (color!=null) g2.setColor(color);
        for (int i=0, n=mCellsGeometry.length; i<n; i++) {
          int[] as = mTileA[i];
          int[] bs = mTileB[i];
          if (fillTiles) {
//            g2.setPaint(fill);
            g2.fillPolygon (as,bs,as.length);
          }
          if (color!=null) {
//            g2.setColor(color);
            g2.drawPolygon(as,bs,as.length);
          }
        }
      }
    }
    // Now the boundary
    if (mBoundaryDraw && mBoundaryGeometry!=null) {
      g2.setStroke(mBoundaryStroke);

      for (int i=0, n=mBoundaryGeometry.length; i<n; i++) {
        Color segmentColor = getStyle().getLineColor();
        if (mBoundaryColors!=null) {
          int colorIndex = i;
          if (mBoundaryLabels!=null) colorIndex = mBoundaryLabels[i];
          if (colorIndex<mBoundaryColors.length) segmentColor = mBoundaryColors[colorIndex];
          else segmentColor = DisplayColors.getLineColor(colorIndex);
        }
        if (segmentColor!=null) {
          g2.setColor(segmentColor);
          g2.drawPolygon(mBoundaryA[i],mBoundaryB[i],mBoundaryA[i].length);
        }
      }
    }
    // Finally the vector field
    if (mDimension>1 && mVectorLength>0 && mCellsGeometry!=null && (mValues!=null || mCellValues!=null) ) {
    	double[] sizes = new double[] {mVectorLength,mVectorLength};
    	if      (_panel instanceof DrawingPanel2D) sizes = ((DrawingPanel2D)_panel).projectSize(new double[2], sizes, new double[2]);
    	else if (_panel instanceof PlottingPanel2D) sizes = ((PlottingPanel2D)_panel).projectSize(new double[2], sizes,new double[2]);
      g2.setStroke(getStyle().getLineStroke());
    	for (int i=0, n=mCellsGeometry.length; i<n; i++) {
    		mDrawer.drawColorCodedArrows(g2, getStyle().getLineColor(), mTileA[i], mTileB[i], sizes, mTileZ[i], mTileVector[i]);
    	}
    }
//    super.draw(_panel, _g); in case it is a Group
  }
  
  @Override
  protected void updateExtrema() {
    if (!hasChanged()) return;
    if (mPoints==null) return;
    initExtrema();
    double[] aPoint = new double[2];
    for(int k = 0, n = mPoints.length;k<n;k++) {
      System.arraycopy(mPoints[k], 0, aPoint, 0, 2);
      getTotalTransform().transform(aPoint,0,aPoint,0,1);
      compareToAllExtrema(aPoint[0],aPoint[1]);
    }
  }
  
  // -------------------------------------
  // Private methods
  // -------------------------------------
 
//  private void setScales() {
//    double min = Double.MAX_VALUE;
//    double max = Double.MIN_VALUE;
//    if (mValues!=null) {
//      for (int i=0, n=mValues.length; i<n; i++) {
//        double value = mValues[i][0];
//        max = Math.max (max, value);
//        min = Math.min (min, value);            
//      }
//    }
//    else {
//      for (int i=0, n=mCellValues.length; i<n; i++) {
//        double[][] cellValue = mCellValues[i];
//        for (int j=0,m=cellValue.length; j<m; j++) {        
//          double value = cellValue[j][0];
//          max = Math.max (max, value);
//          min = Math.min (min, value);            
//        }
//      }
//    }
//    mDrawer.setAutoscale(min,max);
//  }
  
  private void setScales() {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    for (int i=0, n=mTileZ.length; i<n; i++) {
      double[] tileZ = mTileZ[i];
      for (int k=0; k<tileZ.length; k++) {
        double value = tileZ[k];
        max = Math.max (max, value);
        min = Math.min (min, value);
      }
    }
    mDrawer.setAutoscale(min,max);
  }

  static private double vectorNorm (double[] vector) {
    double norm = 0;
    for (int k=0; k<vector.length; k++) norm += vector[k]*vector[k];
    return Math.sqrt(norm);
  }
  
  private void projectPoints(org.opensourcephysics.display.DrawingPanel _panel) {
    java.awt.geom.AffineTransform tr = getPixelTransform(_panel);
    { // Project the points
      double[] point = new double[2];
      int nPoints = mPoints.length;
      if (nPoints!=mA.length) {
        mA = new int[nPoints];
        mB = new int[nPoints];
      }
      for (int i=0; i<nPoints; i++) {
        System.arraycopy(mPoints[i],0,point,0,2);
        tr.transform(point,0,point,0,1);
        mA[i] = (int) point[0];
        mB[i] = (int) point[1];
      }
    }
    // Build the tiles and boundary polygons (faster drawing but more memory consuming)
    if (mCellsGeometry!=null && mCellsGeometry.length>0) {
      int nTiles = mCellsGeometry.length;
      if (mTileA.length!=nTiles) {
        mTileA = new int[nTiles][0];
        mTileB = new int[nTiles][0];
        mTileZ = new double[nTiles][0];
        mTileVector = new double[nTiles][0][0];
      }
      for (int i=0; i<nTiles; i++) {
        int[] tile = mCellsGeometry[i];
        int nTilePoints = tile.length;
        int[] tileA, tileB;
        double[] tileZ;
        double[][] tileVector;
        if (mTileA[i].length!=nTilePoints) {
          tileA = mTileA[i] = new int[nTilePoints];
          tileB = mTileB[i] = new int[nTilePoints];
          tileZ = mTileZ[i] = new double[nTilePoints];
          tileVector = mTileVector[i] = new double[nTilePoints][mDimension];
        }
        else {
          tileA = mTileA[i];
          tileB = mTileB[i];
          tileZ = mTileZ[i];
          tileVector = mTileVector[i];
        }
        for (int j=0; j<nTilePoints; j++) {
          int p = tile[j];
          tileA[j] = mA[p];
          tileB[j] = mB[p];
//          tileZ[j] = mValues[p];
        }
        if (mValues!=null) {
          for (int j=0; j<nTilePoints; j++) {
            int p = tile[j];
            tileZ[j] = (mDimension<=1) ? mValues[p][0] : vectorNorm(mValues[p]);
            System.arraycopy(mValues[p],0,tileVector[j],0,mDimension); 
          }
        }
        else if (mCellValues!=null) {
          double[][] tileValues = mCellValues[i];
//          System.err.println("N Tiles = "+nTiles);
//          System.err.println("N Tiles Values= "+mCellValues);
//          System.err.println("TilePoints["+i+"] = "+nTilePoints);
//          System.err.println("TileValuePoints = "+tileValues.length);
          for (int j=0; j<nTilePoints; j++) {
        	  tileZ[j] = (mDimension<=1) ? tileValues[j][0] : vectorNorm(tileValues[j]);
              System.arraycopy(tileValues[j],0,tileVector[j],0,mDimension); 
          }
        }
      }
    }
    
    if (mBoundaryGeometry!=null && mBoundaryGeometry.length>0) {
      int nSegments = mBoundaryGeometry.length;
      if (mBoundaryA.length!=nSegments) {
        mBoundaryA = new int[nSegments][0];
        mBoundaryB = new int[nSegments][0];
      }
      for (int i=0; i<nSegments; i++) {
        int[] segment = mBoundaryGeometry[i];
        int nSegmentPoints = segment.length;
        int[] segmentA, segmentB;
        if (mBoundaryA[i].length!=nSegmentPoints) {
          segmentA = mBoundaryA[i] = new int[nSegmentPoints];
          segmentB = mBoundaryB[i] = new int[nSegmentPoints];
        }
        else {
          segmentA = mBoundaryA[i];
          segmentB = mBoundaryB[i];
        }
        for (int j=0; j<nSegmentPoints; j++) {
          int p = segment[j];
          segmentA[j] = mA[p];
          segmentB[j] = mB[p];
        }
      }
    }
//    if (mCellsGeometry!=null && mCellsGeometry.length>0 && mDimension>1) {
//      if (mPointsChanged) { // recompute the positions of the arrows
//        computeVectors();
//        mPointsChanged = false;
//      }
//      updateVectors();
//      mField.setVisible(true);
//    }
//    else mField.setVisible(false);

    setNeedToProject(false);
  }
  
  
//  private void computeVectors() {
//    double xmin = Double.MAX_VALUE;
//    double xmax = Double.MIN_VALUE;
//    double ymin = Double.MAX_VALUE;
//    double ymax = Double.MIN_VALUE;
//    for (int i=0, n=mPoints.length; i<n; i++) {
//      double[] point = mPoints[i];
//      xmax = Math.max (xmax, point[0]);
//      xmin = Math.min (xmin, point[0]);
//      ymax = Math.max (ymax, point[1]);
//      ymin = Math.min (ymin, point[1]);
//    }
//   super.removeAllElements();
//   double dx = (xmax-xmin)/(mNx-1);
//   double dy = (ymax-ymin)/(mNy-1);
//   int nTiles = mCellsGeometry.length;
//   for (int i=0; i<nTiles; i++) {
//     int[] tile = mCellsGeometry[i];
//     int nTilePoints = tile.length;
//
//     for (int j=0; j<nTilePoints; j++) {
//       double[] vertex = mPoints[tile[j]];
//       ElementArrow arrow = new ElementArrow();
//       arrow.setDataObject(tile[j]);
//       arrow.setXY(vertex[0], vertex[1]);
//       arrow.setSizeXY(dx/2, dy/2);
////       arrow.getStyle().setRelativePosition(Style.CENTERED);
//       super.addElement(arrow);
//     }
//   }
//   if (true) {
//     TileInfo tileInfo = new TileInfo();
//     tileInfo.x = x;
//     tileInfo.y = y;
//     tileInfo.tile = 0;
//     return tileInfo;
//   }
//   return null;
//    double dx = (xmax-xmin)/(mNx-1);
//    double dy = (ymax-ymin)/(mNy-1);
//    for (int i=0; i<mNx; i++) {
//      double x = xmin + i*dx;
//      for (int j=0; j<mNy; j++) {
//        double y = ymin + j*dy;
//        TileInfo tile = tileContaining(x,y);
//        if (tile!=null) {
//          ElementArrow arrow = new ElementArrow();
//          arrow.setDataObject(tile);
//          arrow.setXY(tile.x, tile.y);
//          arrow.setSizeXY(dx/2, dy/2);
//          arrow.getStyle().setRelativePosition(Style.CENTERED);
//          super.addElement(arrow);
//        }
//      }
//    }
//  }

//  private TileInfo tileContaining (double x, double y) {
//    int nTiles = mCellsGeometry.length;
//    for (int i=0; i<nTiles; i++) {
//      int[] tile = mCellsGeometry[i];
//      int nTilePoints = tile.length;
//      
//      for (int j=0; j<nTilePoints; j++) {
//        double[] vertex = mPoints[tile[j]];
//      }
//    if (true) {
//      TileInfo tileInfo = new TileInfo();
//      tileInfo.x = x;
//      tileInfo.y = y;
//      tileInfo.tile = 0;
//      return tileInfo;
//    }
//    return null;
//  }
//
//  static private class TileInfo {
//    int tile;
//    double x,y;
//    
//    
//  }
  
  
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
