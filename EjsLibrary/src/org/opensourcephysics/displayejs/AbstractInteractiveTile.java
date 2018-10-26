/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.numerics.Transformation;

/**
 * This is the basic class for all InteractiveElements which consist of a sequence
 * of 3D colored tiles: Surface, Box, 3D Lattices and GridPlots...
 */
public abstract class AbstractInteractiveTile extends AbstractInteractiveElement implements Body {
  // Configuration variables
  protected boolean drawQuickInterior = false;
  protected int interiorTransparency=128;
  protected int numberOfTiles=0;
  protected double corners[][][] = null; // the numberOfTiles x vertex x 3  (vertex = 4 for 4-sided tiles
  protected boolean levelBelowWhenEqual = true;
  protected double displacementFactor = 1.0;
  protected double levelx=0.0, levely=0.0, levelz=0.0, leveldx=0.0, leveldy=0.0, leveldz=1.0;
  protected double [] levelZ      = null;
  protected Paint [] levelColors = null;

  protected Transformation transformation=null;
  protected double originx = 0.5, originy = 0.5, originz = 0.5;
  protected boolean originIsRelative=true;

  // Implementation variables
  protected double xmin=Double.NaN,xmax=Double.NaN,ymin=Double.NaN,ymax=Double.NaN,zmin=Double.NaN,zmax=Double.NaN;
  protected double[] pixel  = new double[3]; // The output for all projections
  protected double[] coordinates = new double[3]; // The input for all projections
  protected double[] pixelOrigin  = new double[3], pixelEndpoint = new double[3];
  protected Object3D[] objects = null;

  private double center[] = new double[3];
  private int a[][] = null, b[][] = null;


  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof AbstractInteractiveTile) {
      AbstractInteractiveTile old = (AbstractInteractiveTile) _element;
      setOrigin(old.originx,old.originy,old.originz,old.originIsRelative);
      setTransformation (old.transformation);
    }
  }

// ----------------------------------------------
// Configuration
// See also below for everything related to the use of z-coded color
// ----------------------------------------------

  /**
   * An optional displacement factor to apply to the tiles when computing
   * their distance to the eye.
   * Setting it to a number bigger that 1 (say 1.03) is useful when you want
   * to draw lines on top of an object.
   * @param factor the desired displacement factor
   */
  public void setDisplacementFactor (double factor) {
    this.displacementFactor = factor;
  }
  /**
   * Gets the displacement factor
   * @return the current displacement factor
   */
  public double GetDisplacementFactor () {
    return this.displacementFactor;
  }

  /**
   * Draw a transparent interior when in quickDraw mode.
   * Default is <b>false</b>
   * @param draw the value desired
   * @param transparency the desired level of transparency (from 0=fully transparent to 255=opaque)
   */
  public void setDrawQuickInterior (boolean draw, int transparency) {
    drawQuickInterior = draw;
    interiorTransparency = Math.max(0,Math.min(transparency,255));
  }

  /**
   * Whether a value equal to one of the thresholds should be drawn using the color
   * below or above
   * @param belowWhenEqual <b>true</b> to use the color below, <b>false</b> to use teh color above
   */
  public void setColorBelowWhenEqual (boolean belowWhenEqual) {
    levelBelowWhenEqual = belowWhenEqual;
  }

  /**
   * Sets the origin and direction of the color change.
   * Default is (0,0,0) and (0,0,1), giving z-coded regions
   * @param x the x coordinate of the origin
   * @param y the y coordinate of the origin
   * @param z the z coordinate of the origin
   * @param dx the x coordinate of the direction vector
   * @param dy the y coordinate of the direction vector
   * @param dz the z coordinate of the direction vector
   */
  public void setColorOriginAndDirection (double x, double y, double z, double dx, double dy, double dz) {
    levelx  = x;  levely  = y;  levelz  = z;
    leveldx = dx; leveldy = dy; leveldz = dz;
  }

  /**
   * Set the levels and color for regional color separation
   * @param thresholds an array on n doubles that separate the n+1 regions.
   * <b>null</b> for no region separation
   * @param colors an array on n+1 colors, one for each of the regions
   */
  public void setColorRegions (double thresholds[], Paint colors[]) {
    if (thresholds==null || colors==null) { levelZ = null; levelColors = null; return; }
    levelZ = new double[thresholds.length];
    levelColors = new Paint[thresholds.length+1];
    for (int i=0; i<thresholds.length; i++) levelZ[i] = thresholds[i];
    for (int i=0; i<thresholds.length+1; i++) {
      if (i<colors.length) levelColors[i] = colors[i];
      else levelColors[i] = colors[colors.length-1];
    }
    hasChanged = true;
  }

// ----------------------------------------------
// Implementation of Body
// ----------------------------------------------

  public void setOrigin (double ox, double oy, double oz, boolean relativeToSize) {
    originx = ox; originy = oy; originz = oz;
    originIsRelative = relativeToSize;
    hasChanged = true;
  }

  public void setTransformation (Transformation transformation) {
    this.transformation = (Transformation) transformation.clone();
    hasChanged = true;
  }

  public void toSpaceFrame (double[] vector) {
    if (transformation!=null) transformation.direct(vector);
    vector[0] += x; vector[1] += y; vector[2] += z;
  }

  public void toBodyFrame (double[] vector) throws UnsupportedOperationException {
    vector[0] -= x; vector[1] -= y; vector[2] -= z;
    if (transformation!=null) transformation.inverse(vector);
  }


// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

// Overwrite this method or modify the computation of pixelEndPoint
// if the tiles do not start at (x,y,z) and end at (x+sizex,...,...)

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged) { computeCorners(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    if (sizeEnabled     && Math.abs(pixelEndpoint[0]-_xpix)<SENSIBILITY && Math.abs(pixelEndpoint[1]-_ypix)<SENSIBILITY) return new InteractionTargetElementSize(this);
    if (positionEnabled && Math.abs(pixelOrigin[0]  -_xpix)<SENSIBILITY && Math.abs(pixelOrigin[1]  -_ypix)<SENSIBILITY) return new InteractionTargetElementPosition(this);
    return null;
   }

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!visible) return null;
    if (hasChanged) { computeCorners(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    if (numberOfTiles<1) return null;
    return objects;
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    if (levelZ!=null) { drawColorCoded (_panel,_g2,_index); return; }
    // Allow the panel to adjust color according to depth
    int sides = corners[_index].length;
    if (style.fillPattern!=null) { // First fill the inside
      Paint theFillPattern = style.fillPattern;
      if (theFillPattern instanceof Color) theFillPattern = _panel.projectColor((Color) theFillPattern,objects[_index].distance);
      _g2.setPaint(theFillPattern);
      _g2.fillPolygon(a[_index],b[_index],sides);
    }
    if (style.edgeColor!=null) {
      Color theColor = _panel.projectColor(style.edgeColor,objects[_index].distance);
      _g2.setColor(theColor);
      _g2.setStroke(style.edgeStroke);
      _g2.drawPolygon(a[_index], b[_index], sides);
    }
  }

  public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2) {
    if (!visible) return;
    if (hasChanged) { computeCorners(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    if (numberOfTiles<1) return;
    _g2.setStroke(style.edgeStroke);
    if (drawQuickInterior && style.fillPattern instanceof Color) {
      Color fillColor = (Color) style.fillPattern;
      if (fillColor.getAlpha()>interiorTransparency)
        fillColor = new Color (fillColor.getRed(),fillColor.getGreen(),fillColor.getBlue(),interiorTransparency);
      _g2.setPaint(fillColor);
      for (int i=0; i<numberOfTiles; i++) _g2.fillPolygon(a[i],b[i],corners[i].length);
    }
    if (style.edgeColor!=null) _g2.setColor(style.edgeColor);
    else _g2.setColor(Color.black);
    for (int i=0; i<numberOfTiles; i++) _g2.drawPolygon(a[i], b[i], corners[i].length);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!visible) return;
    if (hasChanged) { computeCorners(); projectPoints(_panel); }
    else // if (_panel!=panelWithValidProjection)
      projectPoints(_panel);
    if (numberOfTiles<1) return;
    // Draw all the tiles in any order
    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(style.edgeStroke);
    for (int i=0; i<numberOfTiles; i++) {
      int sides = corners[i].length;
      if (style.fillPattern!=null) { // First fill the inside
        g2.setPaint(style.fillPattern);
        g2.fillPolygon(a[i],b[i],sides);
      }
      if (style.edgeColor!=null) {
        g2.setColor(style.edgeColor);
        g2.drawPolygon(a[i], b[i], sides);
      }
    }
  }

// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

  public double getXMin () { if (hasChanged) { computeCorners(); computeExtrema(); } else if (Double.isNaN(xmin)) computeExtrema(); return xmin; }
  public double getXMax () { if (hasChanged) { computeCorners(); computeExtrema(); } else if (Double.isNaN(xmax)) computeExtrema(); return xmax; }
  public double getYMin () { if (hasChanged) { computeCorners(); computeExtrema(); } else if (Double.isNaN(ymin)) computeExtrema(); return ymin; }
  public double getYMax () { if (hasChanged) { computeCorners(); computeExtrema(); } else if (Double.isNaN(ymax)) computeExtrema(); return ymax; }
  public double getZMin () { if (hasChanged) { computeCorners(); computeExtrema(); } else if (Double.isNaN(zmin)) computeExtrema(); return zmin; }
  public double getZMax () { if (hasChanged) { computeCorners(); computeExtrema(); } else if (Double.isNaN(zmax)) computeExtrema(); return zmax; }

// -------------------------------------
//  Private or protected methods
// -------------------------------------

  /**
   * This will be used by subclasses whenever there is a need to recompute
   * the actual values of the corners before drawing.
   * Synchronization is recomended.
   */
  abstract protected void computeCorners();

  /**
   * This method computes the abolute coordinates of the origin of the body
   * if given in relative form. Subclasses may want to override the default
   * computation provided here.
   * @param result a placeolder for the result
   */
  protected void computeAbsoluteDifference (double[] result) {
    result[0] = originx*sizex;
    result[1] = originy*sizey;
    result[2] = originz*sizez;
  }

  /**
   * This is a convenience method that adjusts the coordinates
   * of a single point.
   */
  protected void transformPoint (double[] result, boolean displace) {
    if (displace) {
      double[] disp = new double[3];
      computeAbsoluteDifference(disp);
      result[0] -= disp[0]; result[1] -= disp[1]; result[2] -= disp[2];
    }
    if (transformation!=null) {
      result[0] -= x; result[1] -= y; result[2] -= z;
      transformation.direct(result);
      result[0] += x; result[1] += y; result[2] += z;
    }
  }

  /**
   * This is a convenience method that adjusts the coordinates
   * of a body taken into account its origin and its orientation.
   * That is, it centers the body around the origin and transforms it.
   */
  protected void transformCorners () {
    if (originIsRelative) computeAbsoluteDifference (coordinates);
    else { coordinates[0] = originx; coordinates[1] = originy; coordinates[2] = originz; }
    for (int tile=0; tile<numberOfTiles; tile++) {
      for (int j=0, sides=corners[tile].length; j<sides; j++) {
        corners[tile][j][0] -= coordinates[0];
        corners[tile][j][1] -= coordinates[1];
        corners[tile][j][2] -= coordinates[2];
        if (transformation!=null) {
          corners[tile][j][0] -= x; corners[tile][j][1] -= y; corners[tile][j][2] -= z;
          transformation.direct(corners[tile][j]);
          corners[tile][j][0] += x; corners[tile][j][1] += y; corners[tile][j][2] += z;
        }
      }
    }
  }

  protected void setCorners (double[][][] _data) {
    corners = _data;
    if (corners==null) { numberOfTiles = 0; a = null; b = null; return; }
    numberOfTiles = corners.length;
    a = new int[numberOfTiles][];
    b = new int[numberOfTiles][];
    objects = new Object3D[numberOfTiles];
    for (int i=0; i<numberOfTiles; i++) {
      int sides = corners[i].length;
      a[i] = new int[sides];
      b[i] = new int[sides];
      objects[i] = new Object3D(this,i);
    }
  }

// Modify the computation of pixelEndPoint
// if the tiles do not start at (x,y,z) and end at (x+sizex,...,...)

  protected void projectPoints (DrawingPanel _panel) {
//    System.out.println("Projecting tile");
    if (group==null) {
      coordinates[0] = x; coordinates[1] = y; coordinates[2] = z;
      transformPoint (coordinates,false);
      _panel.project(coordinates,pixelOrigin);
      coordinates[0] = x+sizex; coordinates[1] = y+sizey; coordinates[2] = z+sizez;
      transformPoint (coordinates,true);
      _panel.project(coordinates,pixelEndpoint);

      for (int i=0; i<numberOfTiles; i++) {
        int sides = corners[i].length;
        for (int k=0; k<3; k++) center[k] = 0.0; // Reset coordinates of the center
        for (int j=0; j<sides; j++) {
          _panel.project (corners[i][j],pixel);  // Project each corner
          a[i][j] = (int) pixel[0];
          b[i][j] = (int) pixel[1];
          for (int k=0; k<3; k++) center[k] += corners[i][j][k]; // Add to the coordinates of the center
        }
        for (int k=0; k<3; k++) center[k] /= sides;
        _panel.project (center,pixel);      // Project the center and take it
        objects[i].distance = pixel[2]*displacementFactor;     // as reference for the distance
      }
    }
    else {
      coordinates[0] = x; coordinates[1] = y; coordinates[2] = z;
      transformPoint (coordinates,false);
      coordinates[0] = group.x + coordinates[0]*group.sizex;
      coordinates[1] = group.y + coordinates[1]*group.sizey;
      coordinates[2] = group.z + coordinates[2]*group.sizez;
      _panel.project(coordinates,pixelOrigin);
      coordinates[0] = x+sizex; coordinates[1] = y+sizey; coordinates[2] = z+sizez;
      transformPoint (coordinates,true);
      coordinates[0] = group.x + coordinates[0]*group.sizex;
      coordinates[1] = group.y + coordinates[1]*group.sizey;
      coordinates[2] = group.z + coordinates[2]*group.sizez;
      _panel.project(coordinates,pixelEndpoint);

      for (int i=0; i<numberOfTiles; i++) {
        int sides = corners[i].length;
        for (int k=0; k<3; k++) center[k] = 0.0; // Reset coordinates of the center
        for (int j=0; j<sides; j++) {
          coordinates[0] = group.x + corners[i][j][0]*group.sizex;
          coordinates[1] = group.y + corners[i][j][1]*group.sizey;
          coordinates[2] = group.z + corners[i][j][2]*group.sizez;
          _panel.project (coordinates,pixel);  // Project each corner
          a[i][j] = (int) pixel[0];
          b[i][j] = (int) pixel[1];
          for (int k=0; k<3; k++) center[k] += coordinates[k]; // Add to the coordinates of the center
        }
        for (int k=0; k<3; k++) center[k] /= sides;
        _panel.project (center,pixel);      // Project the center and take it
        objects[i].distance = pixel[2]*displacementFactor;     // as reference for the distance
      }
    }
    panelWithValidProjection = _panel;
  }

// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

// Overwrite this parte if the tiles can scape the (x,y,z) - (x+sizex,...,...) cube
// For instance, with something similar to the following.
// Or, better yet, an implementation that takes into account the origin of the tiles.

  protected void computeExtrema () {
    xmin = ymin = zmin = Double.MAX_VALUE;
    xmax = ymax = zmax = -Double.MAX_VALUE;
    for (int i=0; i<numberOfTiles; i++) {
      int sides = corners[i].length;
      for (int j=0; j<sides; j++) {
        double aux = corners[i][j][0];
        if (aux<xmin) xmin = aux;
        if (aux>xmax) xmax = aux;
        aux = corners[i][j][1];
        if (aux<ymin) ymin = aux;
        if (aux>ymax) ymax = aux;
        aux = corners[i][j][2];
        if (aux<zmin) zmin = aux;
        if (aux>zmax) zmax = aux;
      }
    }
    if (group!=null) {
      xmin = group.x + xmin*group.sizex;
      xmax = group.x + xmax*group.sizex;
      ymin = group.y + ymin*group.sizey;
      ymax = group.y + ymax*group.sizey;
      zmin = group.z + zmin*group.sizez;
      zmax = group.z + zmax*group.sizez;
    }
  }

// ----------------------------------------------
// Everything related to the use of z-coded color
// ----------------------------------------------

  private double levelScalarProduct (double point[]) {
    if (group==null) return (point[0]-levelx)*leveldx + (point[1]-levely)*leveldy + (point[2]-levelz)*leveldz;
    return (group.x + point[0]*group.sizex-levelx)*leveldx
         + (group.y + point[1]*group.sizey-levely)*leveldy
         + (group.z + point[2]*group.sizez-levelz)*leveldz;
  }

  private void drawColorCoded (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    int sides = corners[_index].length;
    // Compute in which region is each point
    int region[] = new int[sides];
    if (levelBelowWhenEqual) for (int j=0; j<sides; j++) {
      region[j] = 0;
      double level = levelScalarProduct(corners[_index][j]);
      for (int k=levelZ.length-1; k>=0; k--) {  // for each level
        if (level>levelZ[k]) { region[j] = k+1; break; }
      }
    }
    else for (int j=0; j<sides; j++) {
      region[j] = levelZ.length;
      double level = levelScalarProduct(corners[_index][j]);
      for (int k=0, l=levelZ.length; k<l; k++) {  // for each level
        if (level<levelZ[k]) { region[j] = k; break; }
      }
    }
    // Compute the subpoligon in each region
    int newCornersA[] = new int [sides*2];
    int newCornersB[] = new int [sides*2];
    for (int k=0, l = levelZ.length; k<=l; k++) {  // for each level
      int newCornersCounter = 0;
      for (int j=0; j<sides; j++) {  // for each point
        int next = (j+1) % sides;
        if (region[j]<=k && region[next]>=k) { // intersection bottom-up
          if (region[j]==k) { newCornersA[newCornersCounter] = a[_index][j]; newCornersB[newCornersCounter] = b[_index][j]; newCornersCounter++; }
          else { // It started further down
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k-1] - t) / (levelScalarProduct(corners[_index][next]) - t);
            newCornersA[newCornersCounter] = (int) Math.round( a[_index][j] + t*(a[_index][next]-a[_index][j]) );
            newCornersB[newCornersCounter] = (int) Math.round( b[_index][j] + t*(b[_index][next]-b[_index][j]) );
            newCornersCounter++;
          }
          if (region[next]>k) { // This segment contributes with a second point
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k] - t) / (levelScalarProduct(corners[_index][next]) - t);
            newCornersA[newCornersCounter] = (int) Math.round( a[_index][j] + t*(a[_index][next]-a[_index][j]) );
            newCornersB[newCornersCounter] = (int) Math.round( b[_index][j] + t*(b[_index][next]-b[_index][j]) );
            newCornersCounter++;
          }
        }
        else if (region[j]>=k && region[next]<=k) { // intersection top-down
          if (region[j]==k) { newCornersA[newCornersCounter] = a[_index][j]; newCornersB[newCornersCounter] = b[_index][j]; newCornersCounter++; }
          else { // It started further up
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k] - t) / (levelScalarProduct(corners[_index][next]) - t);
            newCornersA[newCornersCounter] = (int) Math.round( a[_index][j] + t*(a[_index][next]-a[_index][j]) );
            newCornersB[newCornersCounter] = (int) Math.round( b[_index][j] + t*(b[_index][next]-b[_index][j]) );
            newCornersCounter++;
          }
          if (region[next]<k) { // This segment contributes with a second point
            double t = levelScalarProduct(corners[_index][j]);
            t = (levelZ[k-1] - t) / (levelScalarProduct(corners[_index][next]) - t);
            newCornersA[newCornersCounter] = (int) Math.round( a[_index][j] + t*(a[_index][next]-a[_index][j]) );
            newCornersB[newCornersCounter] = (int) Math.round( b[_index][j] + t*(b[_index][next]-b[_index][j]) );
            newCornersCounter++;
          }
        }
      }
      if (newCornersCounter>0) { // Draw the subpoligon
        Paint theFillPattern = levelColors[k];
//        if (theFillPattern instanceof Color) theFillPattern = _panel.projectColor((Color) theFillPattern,objects[_index].distance);
        _g2.setPaint(theFillPattern);
        _g2.fillPolygon(newCornersA,newCornersB,newCornersCounter);
      }
    }
    if (style.edgeColor!=null) {
      _g2.setColor(_panel.projectColor(style.edgeColor,objects[_index].distance));
      _g2.setStroke(style.edgeStroke);
      _g2.drawPolygon(a[_index], b[_index], sides);
    }
  }


}
