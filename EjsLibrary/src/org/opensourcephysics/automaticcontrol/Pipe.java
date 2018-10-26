/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.automaticcontrol;

import java.awt.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.displayejs.*;

public class Pipe extends InteractivePoligon {
  // Configuration variables
  protected boolean filled = false, endClosed = false;
  protected double width = 0.05;
  protected Paint emptyPattern = null;

  // Implementation variables
  protected int sides = 0;
  protected int sidePointsA[]=null, sidePointsB[]=null;
//  protected double sideCoordinates[][] = null;


  public Pipe () {
    setXYZ(0.0,0.0,0.0);
    setSizeXYZ(1.0,1.0,1.0);
    closed = false;
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof Pipe) {
      setWidth(((Pipe) _element).getWidth());
      setFilled(((Pipe) _element).isFilled());
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  // Overwrites its parent
  public void setClosed (boolean _closed) { closed = false; }

  // Overwrites its parent
  public void setConnections (boolean[] _c) {
    for (int i=0; i<numPoints; i++)  connect[i] = true;
  }

  public void setNumberOfPoints (int _n) {
    if (_n==numPoints) return;
    if (_n<1) return;
    super.setNumberOfPoints(_n);
    sidePointsA = new int [2*_n+1];
    sidePointsB = new int [2*_n+1];
//    sideCoordinates = new double [3][2*_n];
    for (int i=0; i<numPoints; i++) coordinates[0][i] = i*0.2/(numPoints-1);
  }

  public void setWidth (double _width) {
    hasChanged = (this.width!=_width);
    this.width = _width;
  }
  public double getWidth () {  return this.width; }

  public void setFilled (boolean _filled) { filled = _filled; }
  public boolean isFilled () {  return filled; }

  public void setEndClosed (boolean _closed) { endClosed = _closed; }
  public boolean isEndClosed () {  return endClosed; }

  public void setEmptyPattern (Paint _pattern) { emptyPattern = _pattern; }
  public Paint getEmptyPattern () {  return emptyPattern; }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  private int[] pieceA = new int[4], pieceB = new int[4];

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    if (_index<(numPoints-1)) { // Only regular segments
      int left = _index, right = 2*numPoints-1-_index;
      Paint theFillPattern = null;
      if (filled) theFillPattern = style.getFillPattern();
      else theFillPattern = emptyPattern;
      if (theFillPattern!=null) {
        if (theFillPattern instanceof Color)
          theFillPattern = _panel.projectColor((Color) theFillPattern,lineObjects[_index].distance);
        pieceA[0] = sidePointsA[left];    pieceB[0]=sidePointsB[left];
        pieceA[1] = sidePointsA[left+1];  pieceB[1]=sidePointsB[left+1];
        pieceA[2] = sidePointsA[right-1]; pieceB[2]=sidePointsB[right-1];
        pieceA[3] = sidePointsA[right];   pieceB[3]=sidePointsB[right];
        _g2.setPaint(theFillPattern);
        _g2.fillPolygon(pieceA,pieceB,4);
      }
      if (style.getEdgeColor()!=null) {
        java.awt.Color theColor = _panel.projectColor(style.getEdgeColor(),lineObjects[_index].distance);
        _g2.setStroke(style.getEdgeStroke());
        _g2.setColor (theColor);
        _g2.drawLine(sidePointsA[left], sidePointsB[left], sidePointsA[left+1], sidePointsB[left+1]);
        _g2.drawLine(sidePointsA[right], sidePointsB[right], sidePointsA[right-1], sidePointsB[right-1]);
      }
    }
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!(numPoints>0 && visible)) return;
    Graphics2D g2 = (Graphics2D) _g;
    // if (hasChanged || _panel!=panelWithValidProjection)
    projectPoints(_panel);
    Paint theFillPattern = null;
    if (filled) theFillPattern = style.getFillPattern();
    else theFillPattern = emptyPattern;
    if (theFillPattern!=null) {
      g2.setPaint(theFillPattern);
      g2.fillPolygon(sidePointsA,sidePointsB,2*numPoints+1);
      if (theFillPattern instanceof Color) { // Otherwise an empty line appears
        g2.setColor ((Color)theFillPattern);
        g2.setStroke(style.getEdgeStroke());
        g2.drawPolyline(sidePointsA,sidePointsB,2*numPoints+1);
//        g2.drawLine(sidePointsA[numPoints-1], sidePointsB[numPoints-1], sidePointsA[numPoints], sidePointsB[numPoints]);
      }
    }
    if (style.getEdgeColor()!=null) {
      g2.setColor (style.getEdgeColor());
      g2.setStroke(style.getEdgeStroke());
      for (int left=1,right=numPoints; left<numPoints; left++,right++) {
        g2.drawLine(sidePointsA[left-1], sidePointsB[left-1], sidePointsA[left], sidePointsB[left]);
        g2.drawLine(sidePointsA[right], sidePointsB[right], sidePointsA[right+1], sidePointsB[right+1]);
      }
      if (endClosed)
        g2.drawLine(sidePointsA[numPoints-1], sidePointsB[numPoints-1], sidePointsA[numPoints], sidePointsB[numPoints]);
    }
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected double[] origin  = new double[6];
  protected double[] pixelOrigin  = new double[5];

  protected void projectPoints (DrawingPanel _panel) {
    super.projectPoints(_panel);
    // Compute the real width of the pipe
    origin[0] = (_panel.getXMin() + _panel.getXMax()) / 2;
    origin[1] = (_panel.getYMin() + _panel.getYMax()) / 2;
    if (_panel instanceof DrawingPanel3D) {
      DrawingPanel3D panel3D = (DrawingPanel3D) _panel;
      origin[2] = (panel3D.getZMin() + panel3D.getZMax()) / 2;
    }
    else origin[2] = 0.0;
    origin[3] = width/2; origin[4] = width/2; origin[5] = width/2;
    _panel.project(origin,pixelOrigin);
    double realWidth = Math.max(pixelOrigin[2],pixelOrigin[3]);

    // Now compute the coordinates of the sides of the pipe
    // The sides for the first center point
    double r1x = aPoints[1]-aPoints[0], r1y = bPoints[1]-bPoints[0], s1x,s1y;
    double mod = Math.sqrt(r1x*r1x + r1y*r1y);
    if (mod==0.0) { s1x = realWidth; s1y = 0; }
    else { s1x = -r1y/mod *realWidth; s1y = r1x/mod *realWidth; }
    int i=0,n=numPoints-1,last = 2*numPoints-1;
    sidePointsA[i]    = aPoints[i] + (int) s1x; sidePointsB[i]    = bPoints[i] + (int) s1y;
    sidePointsA[last] = aPoints[i] - (int) s1x; sidePointsB[last] = bPoints[i] - (int) s1y;
    // Now, the sides for all intermediate points
    for (i=1,last--; i<n; i++, last--) {
      double r0x = r1x, r0y = r1y;
      r1x = aPoints[i+1]-aPoints[i]; r1y = bPoints[i+1]-bPoints[i];
      mod = Math.sqrt(r1x*r1x + r1y*r1y);
      if (mod==0.0) { s1x = realWidth; s1y = 0; }
      else { s1x = -r1y/mod *realWidth; s1y = r1x/mod *realWidth; }
      double den = r0y*r1x-r0x*r1y;
      if (Math.abs(den)<0.1) {
        sidePointsA[i]    = aPoints[i] + (int) s1x; sidePointsB[i]    = bPoints[i] + (int) s1y;
        sidePointsA[last] = aPoints[i] - (int) s1x; sidePointsB[last] = bPoints[i] - (int) s1y;
      }
      else {
        double t = ( (bPoints[i]-sidePointsB[i-1]+s1y)*r1x-(aPoints[i]-sidePointsA[i-1]+s1x)*r1y)/den;
        sidePointsA[i]    = sidePointsA[i-1] + (int) (r0x*t);
        sidePointsB[i]    = sidePointsB[i-1] + (int) (r0y*t);
        t = ( (bPoints[i+1]-sidePointsB[last+1]-s1y)*r1x-(aPoints[i+1]-sidePointsA[last+1]-s1x)*r1y)/den;
        sidePointsA[last] = sidePointsA[last+1] + (int) (r0x*t);
        sidePointsB[last] = sidePointsB[last+1] + (int) (r0y*t);
      }
    }
    // Now the sides for the last point
    if (Math.abs(mod)<1.0e-4) {
      sidePointsA[i]    = aPoints[i] + (int) realWidth; sidePointsB[i]    = bPoints[i] + 0;
      sidePointsA[last] = aPoints[i] - (int) realWidth; sidePointsB[last] = bPoints[i] - 0;
    }
    else {
      mod *= mod;
      double t = ( (aPoints[i]-sidePointsA[i-1])*r1x+(bPoints[i]-sidePointsB[i-1])*r1y )/mod;
      sidePointsA[i]    = sidePointsA[i-1] + (int) (r1x*t);
      sidePointsB[i]    = sidePointsB[i-1] + (int) (r1y*t);
      t = ( (aPoints[i]-sidePointsA[last+1])*r1x+(bPoints[i]-sidePointsB[last+1])*r1y )/mod;
      sidePointsA[last] = sidePointsA[last+1] + (int) (r1x*t);
      sidePointsB[last] = sidePointsB[last+1] + (int) (r1y*t);
    }
    i = 2*numPoints;
    sidePointsA[i] = sidePointsA[0];
    sidePointsB[i] = sidePointsB[0];
}
/*
    computeSides(_panel);
    for (int i=0,n=2*numPoints; i<n; i++) {
      for (int k=0; k<3; k++) point[k] = origin[k] + sideCoordinates[k][i]*size[k];
      _panel.project (point,pixel);
      sidePointsA[i] = (int) pixel[0];
      sidePointsB[i] = (int) pixel[1];
    }
  }

  private void computeSides (DrawingPanel _panel) {
    // The sides for the first center point
    double r1x = coordinates[0][1]-coordinates[0][0], r1y = coordinates[1][1]-coordinates[1][0], s1x,s1y;
    double mod = Math.sqrt(r1x*r1x + r1y*r1y);
    double w = width/2;
    if (mod==0.0) { s1x = w; s1y = 0; }
    else { s1x = -r1y/mod *w; s1y = r1x/mod *w; }
    int i=0,n=numPoints-1,last = 2*numPoints-1;
    sideCoordinates[0][i]    = coordinates[0][i] + s1x; sideCoordinates[1][i]    = coordinates[1][i] + s1y;
    sideCoordinates[0][last] = coordinates[0][i] - s1x; sideCoordinates[1][last] = coordinates[1][i] - s1y;
    // Now, the sides for all intermediate points
    for (i=1,last--; i<n; i++, last--) {
      double r0x = r1x, r0y = r1y;
      r1x = coordinates[0][i+1]-coordinates[0][i]; r1y = coordinates[1][i+1]-coordinates[1][i];
      mod = Math.sqrt(r1x*r1x + r1y*r1y);
      if (mod==0.0) { s1x = w; s1y = 0; }
      else { s1x = -r1y/mod *w; s1y = r1x/mod *w; }
      double den = r0y*r1x-r0x*r1y;
      if (Math.abs(den)<1.0e-4) {
        sideCoordinates[0][i]    = coordinates[0][i] + s1x; sideCoordinates[1][i]    = coordinates[1][i] + s1y;
        sideCoordinates[0][last] = coordinates[0][i] - s1x; sideCoordinates[1][last] = coordinates[1][i] - s1y;
      }
      else {
        double t = ( (coordinates[1][i]-sideCoordinates[1][i-1]+s1y)*r1x-(coordinates[0][i]-sideCoordinates[0][i-1]+s1x)*r1y)/den;
        sideCoordinates[0][i]    = sideCoordinates[0][i-1] + (r0x*t);
        sideCoordinates[1][i]    = sideCoordinates[1][i-1] + (r0y*t);
        t = ( (coordinates[1][i+1]-sideCoordinates[1][last+1]-s1y)*r1x-(coordinates[0][i+1]-sideCoordinates[0][last+1]-s1x)*r1y)/den;
        sideCoordinates[0][last] = sideCoordinates[0][last+1] + (r0x*t);
        sideCoordinates[1][last] = sideCoordinates[1][last+1] + (r0y*t);
      }
    }
    // Now the sides for the last point
    if (Math.abs(mod)<1.0e-4) {
      sideCoordinates[0][i]    = coordinates[0][i] + w; sideCoordinates[1][i]    = coordinates[1][i];
      sideCoordinates[0][last] = coordinates[0][i] - w; sideCoordinates[1][last] = coordinates[1][i];
    }
    else {
      mod *= mod;
      double t = ( (coordinates[0][i]-sideCoordinates[0][i-1])*r1x+(coordinates[1][i]-sideCoordinates[1][i-1])*r1y )/mod;
      sideCoordinates[0][i]    = sideCoordinates[0][i-1] + (r1x*t);
      sideCoordinates[1][i]    = sideCoordinates[1][i-1] + (r1y*t);
      t = ( (coordinates[0][i]-sideCoordinates[0][last+1])*r1x+(coordinates[1][i]-sideCoordinates[1][last+1])*r1y )/mod;
      sideCoordinates[0][last] = sideCoordinates[0][last+1] + (r1x*t);
      sideCoordinates[1][last] = sideCoordinates[1][last+1] + (r1y*t);
    }

  }

*/

}
