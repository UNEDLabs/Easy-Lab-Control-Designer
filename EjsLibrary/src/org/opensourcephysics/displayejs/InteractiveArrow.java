/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.numerics.Transformation;

public class InteractiveArrow extends AbstractInteractiveElement implements Body {
  static final public int ARROW      = 0;
  static final public int SEGMENT    = 1;
  static final public int BOX        = 2;

  /* Configuration variables */
  protected int arrowType = ARROW;
  // For Body
  protected Transformation transformation=null;
  protected double originx = 0.0, originy = 0.0, originz = 0.0;
  protected boolean originIsRelative=true;

  /* Implementation variables */
//  private boolean hideLines=true;
  private int div=-1; // the number of subdivisions of the arrow. Useful for big arrows. -1 to make sure new arrays are allocated
  protected double xmin=Double.NaN,xmax=Double.NaN,ymin=Double.NaN,ymax=Double.NaN,zmin=Double.NaN,zmax=Double.NaN;
  private double[] coordinates  = new double[3]; // the input for all projections
  private double[] pixel   = new double[3]; // The output for all projections
  private Object3D[] objects    = null;
  private int aCoord[] = null, bCoord[] = null;
  private double points[][]=null; // coordinates for the points of the arrow and its subdivisions
  protected double[] pixelOrigin  = new double[3], pixelEndpoint = new double[3];

  /**
   * Default constructor
   */
  public InteractiveArrow () { this(ARROW); }

  /**
   * Constructor for a given type. Either ARROW, SEGMENT or BOX
   */
  public InteractiveArrow (int _type) {
    setArrowType(_type);
    div = -1;
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveArrow) {
      InteractiveArrow old = (InteractiveArrow) _element;
      setArrowType(old.arrowType);
      setOrigin(old.originx,old.originy,old.originz,old.originIsRelative);
      setTransformation (old.transformation);
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  /**
   * Set the type of decoration at the head of the arrow. Either ARROW, SEGMENT (none) or BOX
   */
  public void setArrowType (int _type) {
    arrowType = _type;
    panelWithValidProjection = null; // So that to compute the head
  }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged) { computeDivisions(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    if (sizeEnabled     && Math.abs(pixelEndpoint[0]-_xpix)<SENSIBILITY && Math.abs(pixelEndpoint[1]-_ypix)<SENSIBILITY) return new InteractionTargetElementSize(this);
    if (positionEnabled && Math.abs(pixelOrigin[0]  -_xpix)<SENSIBILITY && Math.abs(pixelOrigin[1]  -_ypix)<SENSIBILITY) return new InteractionTargetElementPosition(this);
//    if (sizeEnabled     && Math.abs(aCoord[div]-_xpix)<SENSIBILITY && Math.abs(bCoord[div]-_ypix)<SENSIBILITY) return new InteractionTargetElementSize(this);
//    if (positionEnabled && Math.abs(aCoord[0]  -_xpix)<SENSIBILITY && Math.abs(bCoord[0]  -_ypix)<SENSIBILITY) return new InteractionTargetElementPosition(this);
    return null;
   }

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!visible) return null;
//    if (!hideLines) { hasChanged = true; hideLines = true; }
    if (hasChanged) { computeDivisions(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    return objects;
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = _panel.projectColor(style.edgeColor,objects[_index].distance);
    if (_index<(div-1) || arrowType==SEGMENT) {
      _g2.setStroke(style.edgeStroke);
      _g2.setColor (theColor);
      _g2.drawLine (aCoord[_index], bCoord[_index], aCoord[_index+1], bCoord[_index+1]);
      return;
    }
    // Draw the head
    Paint theFillPattern = style.fillPattern;
    if (theFillPattern instanceof Color)
      theFillPattern = _panel.projectColor((Color) theFillPattern,objects[_index].distance);
    drawHead (_panel,_g2,aCoord[_index],bCoord[_index],theColor,theFillPattern);
  }

  public synchronized void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2) {
    if (!visible) return;
//    if (hideLines) { hasChanged = true; hideLines = false; }
    if (hasChanged) { computeDivisions(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    drawHead (_panel,_g2,aCoord[0], bCoord[0],style.edgeColor,style.fillPattern);
  }

  public synchronized void draw (DrawingPanel _panel, Graphics _g) {
    if (!visible) return;
//    if (hideLines) { hasChanged = true; hideLines = false; }
    if (hasChanged) { computeDivisions(); projectPoints(_panel); }
    else
//    if (_panel!=panelWithValidProjection)
      projectPoints(_panel);
    drawHead (_panel,(Graphics2D) _g,aCoord[0], bCoord[0],style.edgeColor,style.fillPattern);
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
    if (transformation==null) this.transformation = null;
    else this.transformation = (Transformation) transformation.clone();
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


// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

  public double getXMin () { if (hasChanged) { computeDivisions(); computeExtrema(); } else if (Double.isNaN(xmin)) computeExtrema(); return xmin; }
  public double getXMax () { if (hasChanged) { computeDivisions(); computeExtrema(); } else if (Double.isNaN(xmax)) computeExtrema(); return xmax; }
  public double getYMin () { if (hasChanged) { computeDivisions(); computeExtrema(); } else if (Double.isNaN(ymin)) computeExtrema(); return ymin; }
  public double getYMax () { if (hasChanged) { computeDivisions(); computeExtrema(); } else if (Double.isNaN(ymax)) computeExtrema(); return ymax; }
  public double getZMin () { if (hasChanged) { computeDivisions(); computeExtrema(); } else if (Double.isNaN(zmin)) computeExtrema(); return zmin; }
  public double getZMax () { if (hasChanged) { computeDivisions(); computeExtrema(); } else if (Double.isNaN(zmax)) computeExtrema(); return zmax; }

/*
  public double getXMin () {
    if (group==null) return x + Math.min(sizex,0);
    else return group.x + x + Math.min(sizex*group.sizex,0);
  }
  public double getXMax () {
    if (group==null) return x + Math.max(sizex,0);
    else return group.x + x + Math.max(sizex*group.sizex,0);
  }
  public double getYMin () {
    if (group==null) return y + Math.min(sizey,0);
    else return group.y + y + Math.min(sizey*group.sizey,0);
  }
  public double getYMax () {
    if (group==null) return y + Math.max(sizey,0);
    else return group.y + y + Math.max(sizey*group.sizey,0);
  }
  public double getZMin () {
    if (group==null) return z + Math.min(sizez,0);
    else return group.z + z + Math.min(sizez*group.sizez,0);
  }
  public double getZMax () {
    if (group==null) return z + Math.max(sizez,0);
    else return group.z + z + Math.max(sizez*group.sizez,0);
  }
*/
  protected void computeExtrema () {
    xmin = ymin = zmin = Double.MAX_VALUE;
    xmax = ymax = zmax = -Double.MAX_VALUE;
    for (int i=0; i<=div; i++) {
      double aux = points[i][0];
      if (aux<xmin) xmin = aux;
      if (aux>xmax) xmax = aux;
      aux = points[i][1];
      if (aux<ymin) ymin = aux;
      if (aux>ymax) ymax = aux;
      aux = points[i][2];
      if (aux<zmin) zmin = aux;
      if (aux>zmax) zmax = aux;
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

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
    // System.out.println("Projecting arrow");
    if (group==null) {
      coordinates[0] = x; coordinates[1] = y; coordinates[2] = z;
      transformPoint (coordinates,false);
      _panel.project(coordinates,pixelOrigin);
      coordinates[0] = x+sizex; coordinates[1] = y+sizey; coordinates[2] = z+sizez;
      transformPoint (coordinates,true);
      _panel.project(coordinates,pixelEndpoint);

      for (int i = 0; i < div; i++) {
        _panel.project(points[i], pixel);
        aCoord[i] = (int) pixel[0];
        bCoord[i] = (int) pixel[1];
        for (int j = 0; j < 3; j++) coordinates[j] = (points[i][j] + points[i + 1][j]) * 0.5; // The coordinates for the middle point
        _panel.project(coordinates, pixel);
        objects[i].distance = pixel[2];
      }
      // Project last point
      _panel.project(points[div], pixel);
      aCoord[div] = (int) pixel[0];
      bCoord[div] = (int) pixel[1];
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

      for (int i = 0; i < div; i++) {
        coordinates[0] = group.x + points[i][0]*group.sizex;
        coordinates[1] = group.y + points[i][1]*group.sizey;
        coordinates[2] = group.z + points[i][2]*group.sizez;
        _panel.project(coordinates, pixel);
        aCoord[i] = (int) pixel[0];
        bCoord[i] = (int) pixel[1];
        for (int j = 0; j < 3; j++) coordinates[j] = (points[i][j] + points[i + 1][j]) * 0.5; // The coordinates for the middle point
        coordinates[0] = group.x + coordinates[0]*group.sizex;
        coordinates[1] = group.y + coordinates[1]*group.sizey;
        coordinates[2] = group.z + coordinates[2]*group.sizez;
        _panel.project(coordinates, pixel);
        objects[i].distance = pixel[2];
      }
      // Project last point
      coordinates[0] = group.x + points[div][0]*group.sizex;
      coordinates[1] = group.y + points[div][1]*group.sizey;
      coordinates[2] = group.z + points[div][2]*group.sizez;
      _panel.project(coordinates, pixel);
      aCoord[div] = (int) pixel[0];
      bCoord[div] = (int) pixel[1];
    }
    computeHead(); // aCoord[div-1],bCoord[div-1]);
    panelWithValidProjection = _panel;
  }

  protected void computeDivisions () {
    int theDiv = 1;
//    if (hideLines && resolution!=null) {
    if (resolution!=null) {
      switch (resolution.type) {
        case Resolution.MAX_LENGTH :
          double length = Math.sqrt(sizex*sizex+sizey*sizey+sizez*sizez);
          theDiv = Math.max((int) Math.round(0.49 + length/resolution.maxLength), 1);
          break;
        case Resolution.DIVISIONS :
          theDiv = Math.max(resolution.n1,1);
          break;
      }
    }
    if (div==theDiv); // No need to reallocate arrays
    else { // Reallocate arrays
      div = theDiv;
      points = new double[div+1][3];
      aCoord = new int[div+1];
      bCoord = new int[div+1];
      objects = new Object3D[div];
      for (int i=0; i<div; i++) objects[i] = new Object3D(this,i);
    }
    points[0][0] = x;  points[0][1] = y; points[0][2] = z;
    double dx = sizex, dy = sizey,       dz = sizez;
    points[div][0] = x + dx; points[div][1] = y + dy; points[div][2] = z+dz;
    dx /= div; dy /= div; dz /= div;
    for (int i=1; i<div; i++) { points[i][0] = x + i*dx; points[i][1] = y + i*dy; points[i][2] = z + i*dz; }
    // Now apply the transformation
    transformDivisions();
    xmin = xmax = ymin = ymax = zmin = zmax = Double.NaN; // To signal out that extrema may be out of date
    hasChanged = false;
  }

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

  protected void transformDivisions () {
    if (originIsRelative) computeAbsoluteDifference (coordinates);
    else { coordinates[0] = originx; coordinates[1] = originy; coordinates[2] = originz; }
    for (int i=0; i<=div; i++) {
      points[i][0] -= coordinates[0];
      points[i][1] -= coordinates[1];
      points[i][2] -= coordinates[2];
      if (transformation!=null) {
        points[i][0] -= x; points[i][1] -= y; points[i][2] -= z;
        transformation.direct(points[i]);
        points[i][0] += x; points[i][1] += y; points[i][2] += z;
      }
    }
  }

  static final private double ARROW_CST=0.35;
  static final private double ARROW_MAX=25.0;

  private int headPoints=0;
  private int headA[] = new int [10], headB[] = new int [10]; // Used to display the head

  private void computeHead () { // int a1, int b1) {
    if (arrowType==SEGMENT) { headPoints = 0; return; }
    double a = aCoord[div] - aCoord[0];
    double b = bCoord[div] - bCoord[0];
    double h = Math.sqrt (a*a+b*b);
//FKH 20020331
    if (h==0.0) { headPoints = 0; return; }
    a = ARROW_CST*a / h; b = ARROW_CST*b / h;
    if(h>ARROW_MAX){ a*=ARROW_MAX/h; b*=ARROW_MAX/h;}
    int p0 = (int) (aCoord[div] - a*h);
    int q0 = (int) (bCoord[div] - b*h);
    a *= h/2.0; b *= h/2.0;
    switch (arrowType) {
      default :
      case ARROW :
        headPoints = 6;
        headA[0] = p0;                 headB[0] = q0;
        headA[1] = p0-(int)b;          headB[1] = q0+(int)a;
        headA[2] = aCoord[div];        headB[2] = bCoord[div];
        headA[3] = p0+(int)b;          headB[3] = q0-(int)a;
        headA[4] = p0;                 headB[4] = q0;
        break;
      case BOX :
        headPoints = 7;
        headA[0] = p0;                 headB[0] = q0;
        headA[1] = p0-(int)b;          headB[1] = q0+(int)a;
        headA[2] = aCoord[div]-(int)b; headB[2] = bCoord[div]+(int)a;
        headA[3] = aCoord[div]+(int)b; headB[3] = bCoord[div]-(int)a;
        headA[4] = p0+(int)b;          headB[4] = q0-(int)a;
        headA[5] = p0;                 headB[5] = q0;
        break;
     }
  }

  private void drawHead (DrawingPanel _panel, Graphics2D _g2, int a1, int b1, Color _color, Paint _fill) {
    _g2.setStroke (style.edgeStroke);
    if (headPoints==0) {
      _g2.setColor (_color);
      _g2.drawLine (a1,b1,aCoord[div],bCoord[div]);
    }
    else {
      int n = headPoints-1;
      headA[n] = a1;
      headB[n] = b1;
      if (_fill!=null) {
        _g2.setPaint(_fill);
        _g2.fillPolygon(headA, headB, n);
      }
      _g2.setColor (_color);
      _g2.drawPolyline (headA,headB,headPoints);
    }
  }

}
