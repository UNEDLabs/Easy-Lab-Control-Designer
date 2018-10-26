/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Color;

import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing2d.utils.ColorCodedDrawer;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: ElementMesh</p>
 * <p>Description: A 3D implementation of org.opensourcephysics.display.Mesh</p>
 * @author Francisco Esquembre
 */
public class ElementMesh extends Group implements org.opensourcephysics.display.Mesh {
  
  static final public int CHANGE_BOUNDARY = CHANGE_SHAPE | CHANGE_COLOR;

  // Configuration variables
  private double[][] mPoints;
  private int[][] mCellsGeometry;
  private double[][] mValues;
  private double[][][] mCellValues;

  private int[][] mBoundaryData;
  private int[] mBoundaryLabels;
  private Color[] mBoundaryColors = null; // an array with a color for each index. If null, the element color is used

  // Implementation variables
  private ElementTessellation mTessellation;
  private MultiTrail mTrail;
  private double[][][] mTiles = new double[0][0][0];
  private double[][] mTessellationValues = new double[0][0];

  private int mDimension = 1;
  private double mSize=0.1;
  private Set mArrowSet;

  {
    mTessellation = new ElementTessellation();
    mTrail = new MultiTrail();
    mArrowSet = new Set();
    mArrowSet.setVisible(false);
    addElement(mTessellation);
    addElement(mTrail);
    addElement(mArrowSet);
  }
  
  public ElementTessellation getTessellation() { return mTessellation; }

  public MultiTrail getTrail() { return mTrail; }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Provides the array of points that conform the tile and its boundary
   * @param points The double[nPoints][3] where nPoints is the number of points in the tile
   */
  public void setPoints(double[][] points) {
    mPoints = points;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Provides the geometry of the tile
   * @param tiles the int[nCells][nPoints] array with the points in each cell.
   * For example,if the first cell is a triangle joining points 0,3,7, one gets: cell[0] = { 0, 3, 7 }; 
   */
  public void setCells(int[][] cells) {
    mCellsGeometry = cells;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Provides the field value for each point in the tile
   * @param values The double[nPoints][] array with the value for each mesh vertex
   */
  public void setFieldAtPoints(double[][] values) {
    mValues = values;
    mDimension = (mValues==null) ? 1 : mValues[0].length;
    mCellValues = null;
    mArrowSet.setVisible(mDimension>1);
    addChange(Element.CHANGE_SHAPE);
  }
  
  /**
   * Provides the field value for the points in each tile
   * @param values The double[nTiles][nPointsPerTile][] array with the value for each cell vertex
   */
  public void setFieldAtCells(double[][][] values) {
    mCellValues = values;
    mValues = null;
    mArrowSet.setVisible(mDimension>1);
    addChange(Element.CHANGE_SHAPE);
  }
  
  /**
   * Provides the geometry of the mesh
   * @param length the length for vectors in the field 
   */
  public void setVectorLength(double length) {
    mSize = length;
  }
  
  public void setBoundary(int[][] boundary) {
    mBoundaryData = boundary;
    addChange(Element.CHANGE_SHAPE);
  }
  
  public void setBoundaryLabels(int[] labels) {
    mBoundaryLabels = labels;
    addChange(Element.CHANGE_COLOR);
  }

  public void setBoundaryColors(Color[] colors) {
    mBoundaryColors = colors;
    addChange(Element.CHANGE_COLOR);
  }

  // -------------------------------------
  // Parent methods overwritten
  // -------------------------------------
  
  @Override
  public void processChanges(int _cummulativeChange) {
    int change = getChange();
    if (mPoints==null) {
      mTessellation.setTiles(null);
      mTrail.clear();
      super.processChanges(_cummulativeChange);
      return;
    }
    synchronized(mPoints) {
      if ((change & CHANGE_SHAPE)!=0) {
        buildSimpleTiles();
        buildBoundary();
      }
      else if ((change & CHANGE_COLOR)!=0) {
        buildBoundary();
      }
      super.processChanges(_cummulativeChange);
    }
  }
  
  private void buildSimpleTiles() {
    mArrowSet.removeAllElements();
    if (mPoints==null || mCellsGeometry==null) {
      mTessellation.setTiles(null);
      return;
    }
    int nTiles = mCellsGeometry.length;
    if (mTiles.length!=nTiles) mTiles = new double[nTiles][0][];
    for (int i=0; i<nTiles; i++) {
      int[] geometry = mCellsGeometry[i];
      int nPoints = geometry.length;
      double[][] tile = mTiles[i];
      if (tile.length!=nPoints) tile = mTiles[i] = new double[nPoints][];
      if (mPoints[0].length>=3) {
        for (int j=0; j<nPoints; j++) {
          tile[j] = mPoints[geometry[j]];
        }
      }
      else for (int j=0; j<nPoints; j++) {
        int pointIndex = geometry[j];
        double[] point = mPoints[pointIndex];
        tile[j] = new double[] { point[0], point[1], 0 };
        if (mCellValues!=null) tile[j][2] =  (mDimension<=1) ? mCellValues[i][j][0] : vectorNorm(mCellValues[i][j]);
      }
    }
    mTessellation.setTiles(mTiles);
    // Pass the values
    if (mValues==null && mCellValues==null) {
      mTessellation.setValues(null);
      return;
    }
    if (mTessellationValues.length!=nTiles) mTessellationValues = new double[nTiles][0];
    ColorCodedDrawer colorDrawer = mTessellation.getDrawer();
    if (mValues!=null) { // The user provided the field at the points
      for (int i=0; i<nTiles; i++) {
        int[] geometry = mCellsGeometry[i];
        int nPoints = geometry.length;
        double[] values = mTessellationValues[i];
        if (values.length!=nPoints) values = mTessellationValues[i] = new double[nPoints];
        for (int j=0; j<nPoints; j++) {
          int pointIndex = geometry[j];
          values[j] = (mDimension<=1) ? mValues[pointIndex][0] : vectorNorm(mValues[pointIndex]);
          if (mDimension>1) {
            ElementArrow arrow = new ElementArrow();
            arrow.setPosition(mPoints[pointIndex]);
            setSize(arrow,mSize,mValues[pointIndex]);
            Style style = arrow.getStyle(); 
            style.setLineColor(this.getStyle().getLineColor());
            style.setLineWidth(this.getStyle().getLineWidth());
            arrow.setDataObject(new Double(values[j]));
            mArrowSet.addElement(arrow);
          }          
        }
      }
    }
    else {
      for (int i=0; i<nTiles; i++) {
        double[][] tile = mTiles[i];
        double[][] cellValues = mCellValues[i];
        int nPoints = cellValues.length;
        double[] values = mTessellationValues[i];
        if (values.length!=nPoints) values = mTessellationValues[i] = new double[nPoints];
        for (int j=0; j<nPoints; j++) {
          values[j] = (mDimension<=1) ? cellValues[j][0] : vectorNorm(cellValues[j]);
          if (mDimension>1) {
            ElementArrow arrow = new ElementArrow();
            arrow.setPosition(tile[j]);
            setSize(arrow,mSize,cellValues[j]);
            Style style = arrow.getStyle(); 
            style.setLineColor(this.getStyle().getLineColor());
            style.setLineWidth(this.getStyle().getLineWidth());
            arrow.setDataObject(new Double(values[j]));
            mArrowSet.addElement(arrow);
          }
        }
      }
    }
    mTessellation.setValues(mTessellationValues);
    if (mDimension>1) {
      mTessellation.checkScales();
      for (Element arrow : mArrowSet.getElements()) {
        double value = ((Double) arrow.getDataObject()).doubleValue();
        arrow.getStyle().setFillColor(colorDrawer.doubleToColor(value));
      }
    }
    
  }
  
  static private void setSize(Element element, double size, double[] vector) {
    double sx = vector[0];
    double sy = vector[1];
    double alpha = Math.atan2(sy,sx);
    if (vector.length>=3) {
      double beta = Math.asin(vector[2]/vectorNorm(vector));
      double cosBeta = Math.cos(beta); 
      element.setSizeXYZ(size*Math.cos(alpha)*cosBeta,size*Math.sin(alpha)*cosBeta,size*Math.sin(beta));
    }
    else { // 2D vector
      element.setSizeXYZ(size*Math.cos(alpha),size*Math.sin(alpha),0);
    }
  }
  
  static private double vectorNorm (double[] vector) {
    double norm = 0;
    for (int k=0; k<vector.length; k++) norm += vector[k]*vector[k];
    return Math.sqrt(norm);
  }
  
  private void buildBoundary() {
    mTrail.clear();
    if (mPoints==null || mBoundaryData==null) return;
    int nSegments = mBoundaryData.length;
    for (int i=0; i<nSegments; i++) {
      if (i>0) mTrail.newSegment();
      Color segmentColor = getStyle().getLineColor();
      if (mBoundaryColors!=null) { 
        int colorIndex = i;
        if (mBoundaryLabels!=null) {
          colorIndex = mBoundaryLabels[i];
          if (colorIndex<mBoundaryColors.length) segmentColor = mBoundaryColors[colorIndex];
          else segmentColor = DisplayColors.getLineColor(colorIndex);
        }
      }
      if (segmentColor!=null) {
        mTrail.getStyle().setLineColor(segmentColor);
        int[] segment = mBoundaryData[i];
        int nPoints = segment.length;
        for (int j=0; j<nPoints; j++) {
          int index = segment[j];
          double[] point = mPoints[index];
          if (point.length>2) mTrail.addPoint(point);
          else if (mValues!=null) mTrail.addPoint(point[0], point[1], mValues[index][0]); // real scalar field
          else mTrail.addPoint(point[0], point[1], 0);
        }
      }
    }
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
