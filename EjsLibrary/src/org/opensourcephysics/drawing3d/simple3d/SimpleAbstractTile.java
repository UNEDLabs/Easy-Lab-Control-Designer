/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.*;

import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;

/**
 * <p>Title: SimpleAbstractTile</p>
 * <p>Description: Painter's algorithm implementation of a Tile</p>
 * A tile is the basic abstract superclass for all Elements which 
 * consist of a sequence of 3D colored planar polygons (tiles). 
 * Such as: Box, Ellipsoid, Cylinder, ...
 * 
 * @author Francisco Esquembre
 * @version August 2009
 */
public abstract class SimpleAbstractTile extends SimpleElement {
  // Subclasses must add the Element.CHANGE_SHAPE change when a recomputation
  // of vertex is required, such as when adding or removing vertex
  static protected final int RECOMPUTE_VERTEX = Element.CHANGE_SHAPE | Element.CHANGE_RESOLUTION;
 
  /* Implementation variables */
  protected int a[][] = null, b[][] = null; // The projection of the vertex
  private int numberOfTiles = -1;
  private double[][][] standardTile = null; // the 0,1 sized tile fo this class
  private double[][][] vertex = null; // the actual coordinates with the right position, size, and orientation
  private double[] pixel = new double[3]; // The point for all projections
  private double[] center = new double[3];

  public SimpleAbstractTile(Element _element) {
    super(_element);
//    computeVertex();
  }
	
  /**
   * Computes the vertex of all tiles
   * @return
   */
  abstract protected double[][][] computeTile();

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------
  
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & RECOMPUTE_VERTEX)!=0) {
      computeVertex();
      computeCoordinates();
      projectPoints();
    }
    else if ((_cummulativeChange & SimpleElement.FORCE_RECOMPUTE)!=0) {
      computeCoordinates();
      projectPoints();
    }
    else if ((_cummulativeChange & Element.CHANGE_PROJECTION)!=0) projectPoints();
  }

  // ------------------------------------
  // Implementation of SimpleElement
  // -------------------------------------

  public void draw(Graphics2D _g2, int _index) {
    if (element.getColorLevels()!=null && element.getColorPalette()!=null) {
      drawColorCoded(_g2, _index);
      return;
    }
    DrawingPanel3D panel = element.getPanel();
    int sides = vertex[_index].length;
    if (style.isDrawingFill()) { // First fill the inside
      _g2.setPaint(panel.projectPaint(style.getFillColor(), objects[_index].getDistance()));
      _g2.fillPolygon(a[_index], b[_index], sides);
    }
    if (style.isDrawingLines()) {
      _g2.setStroke(style.getLineStroke());
      _g2.setColor(panel.projectColor(style.getLineColor(), objects[_index].getDistance()));
      _g2.drawPolygon(a[_index], b[_index], sides);
    }
  }

  public void drawQuickly(Graphics2D _g2) {
    if (style.isDrawingFill() || drawQuickInterior) {
      Paint fill = style.getFillColor();
      if (drawQuickInterior && (fill instanceof Color)) {
        Color color = (Color) fill;
        if (color.getAlpha()>interiorTransparency) {
          fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), interiorTransparency);
        }
      }
      _g2.setPaint(fill);
      for (int i = 0; i<numberOfTiles; i++) _g2.fillPolygon(a[i], b[i], vertex[i].length);
    }
    if (style.isDrawingLines()) {
      _g2.setStroke(style.getLineStroke());
      _g2.setColor(style.getLineColor());
      for (int i = 0;i<numberOfTiles; i++) _g2.drawPolygon(a[i], b[i], vertex[i].length);
    }
  }

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------

  final protected void computeVertex() {
    standardTile = computeTile();
    if (standardTile==null) {
      numberOfTiles = 0;
      return;
    }
    if (standardTile.length!=numberOfTiles) {
      numberOfTiles = standardTile.length;
      vertex = new double[numberOfTiles][][];
      a = new int[numberOfTiles][];
      b = new int[numberOfTiles][];
      objects = new Object3D[numberOfTiles];
    }
    for (int i = 0; i<numberOfTiles; i++) {
      int sides = standardTile[i].length;
      if (a[i]==null || sides!=a[i].length) {
        vertex[i] = new double[sides][3];
        a[i] = new int[sides];
        b[i] = new int[sides];
      }
      objects[i] = new Object3D(this, i);
    }
  }

  final protected void computeCoordinates() {
    for (int i=0; i<numberOfTiles; i++) {
      for (int j=0, sides=vertex[i].length; j<sides; j++) {
        System.arraycopy(standardTile[i][j], 0, vertex[i][j], 0, 3);
        element.sizeAndToSpaceFrame(vertex[i][j]);
      }
    }
  }

  final protected void projectPoints() {
    DrawingPanel3D panel = element.getPanel();
    double depthFactor = style.getDepthFactor();
    for (int i=0; i<numberOfTiles; i++) {
      int sides=vertex[i].length;
      for (int k = 0; k<3; k++) center[k] = 0.0; // Reset coordinates of the center
      for (int j = 0; j<sides; j++) {
        System.arraycopy(vertex[i][j], 0, pixel, 0, 3);
        panel.projectPosition(pixel); // Project each corner
        a[i][j] = (int) pixel[0];
        b[i][j] = (int) pixel[1];
        for(int k = 0;k<3;k++) center[k] += vertex[i][j][k]; // Add to the coordinates of the center
      }
      for(int k = 0;k<3;k++) center[k] /= sides;
      panel.projectPosition(center);                                   // Project the center and take it
      objects[i].setDistance(center[2]*depthFactor); // as reference for the distance
    }
  }
  
  // -------------------------------------
  // New configuration methods related to the use of z-coded color
  // NOT TESTED!
  // -------------------------------------

  // Configuration variables for z-coded colors
  private boolean drawQuickInterior = false;
  private int interiorTransparency = 128;

  /**
   * Draw a transparent interior when in quickDraw mode.
   * Default is <b>false</b>
   * @param draw the value desired
   * @param transparency the desired level of transparency (from 0=fully transparent to 255=opaque)
   */
  public void setDrawQuickInterior(boolean draw, int transparency) {
    this.drawQuickInterior = draw;
    this.interiorTransparency = Math.max(0, Math.min(transparency, 255));
  }

  // -------------------------------------
  // protected methods related to the use of z-coded color
  // -------------------------------------
  
  private void drawColorCoded(Graphics2D _g2, int _index) {
    int sides = vertex[_index].length;
    // Compute in which region is each point
    int region[] = new int[sides];
    double[] levelZ = element.getColorLevels();
    
    if(element.getColorBelowWhenEqual()) {
      for(int j = 0;j<sides;j++) {
        region[j] = 0;
        double level = element.levelScalarProduct(vertex[_index][j]);
        for(int k = levelZ.length-1;k>=0;k--) {     // for each level
          if(level>levelZ[k]) {
            region[j] = k+1;
            break;
          }
        }
      }
    } 
    else {
      for(int j = 0;j<sides;j++) {
        region[j] = levelZ.length;
        double level = element.levelScalarProduct(vertex[_index][j]);
        for(int k = 0, l = levelZ.length;k<l;k++) { // for each level
          if(level<levelZ[k]) {
            region[j] = k;
            break;
          }
        }
      }
    }
    // Compute the subpoligon in each region
    int newCornersA[] = new int[sides*2];
    int newCornersB[] = new int[sides*2];
    Color[] levelColors = element.getColorPalette();
    for(int k = 0, l = levelZ.length;k<=l;k++) { // for each level
      int newCornersCounter = 0;
      for(int j = 0;j<sides;j++) {               // for each point
        int next = (j+1)%sides;
        if(region[j]<=k&&region[next]>=k) {      // intersection bottom-up
          if(region[j]==k) {
            newCornersA[newCornersCounter] = a[_index][j];
            newCornersB[newCornersCounter] = b[_index][j];
            newCornersCounter++;
          } else {                               // It started further down
            double t = element.levelScalarProduct(vertex[_index][j]);
            t = (levelZ[k-1]-t)/(element.levelScalarProduct(vertex[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
          if(region[next]>k) { // This segment contributes with a second point
            double t = element.levelScalarProduct(vertex[_index][j]);
            t = (levelZ[k]-t)/(element.levelScalarProduct(vertex[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
        } else if(region[j]>=k&&region[next]<=k) { // intersection top-down
          if(region[j]==k) {
            newCornersA[newCornersCounter] = a[_index][j];
            newCornersB[newCornersCounter] = b[_index][j];
            newCornersCounter++;
          } else {                                 // It started further up
            double t = element.levelScalarProduct(vertex[_index][j]);
            t = (levelZ[k]-t)/(element.levelScalarProduct(vertex[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
          if(region[next]<k) { // This segment contributes with a second point
            double t = element.levelScalarProduct(vertex[_index][j]);
            t = (levelZ[k-1]-t)/(element.levelScalarProduct(vertex[_index][next])-t);
            newCornersA[newCornersCounter] = (int) Math.round(a[_index][j]+t*(a[_index][next]-a[_index][j]));
            newCornersB[newCornersCounter] = (int) Math.round(b[_index][j]+t*(b[_index][next]-b[_index][j]));
            newCornersCounter++;
          }
        }
      }
      if(newCornersCounter>0) { // Draw the subpoligon
        Color theFillColor = (k>=levelColors.length) ? levelColors[levelColors.length-1] : levelColors[k];
        // theFillPattern = _panel.projectPaint(theFillPattern,objects[_index].distance);
        _g2.setPaint(theFillColor);
        _g2.fillPolygon(newCornersA, newCornersB, newCornersCounter);
      }
    }
    _g2.setColor(element.getPanel().projectColor(style.getLineColor(), objects[_index].getDistance()));
    _g2.setStroke(style.getLineStroke());
    _g2.drawPolygon(a[_index], b[_index], sides);
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
