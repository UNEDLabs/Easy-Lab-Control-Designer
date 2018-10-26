/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.displayejs;

import java.awt.*;

import org.opensourcephysics.display.*;

public class InteractivePoints extends AbstractInteractiveElement {
  // Configuration variables
  protected int numPoints=-1;
  protected double coordinates[][] = null;
  private Color pointColor[] = null; 
  private Stroke pointStroke[] = null; 

  // Implementation variables
  protected int aPoints[]=null, bPoints[]=null;
  protected double pixel[]  = new double[3]; // Output of panel's projections
  protected double point[]  = new double[3]; // Auxiliary arrays

  protected Object3D[] pointObjects=null;  // Objects3D for each of the points

  public InteractivePoints () {
    setXYZ(0.0,0.0,0.0);
    setSizeXYZ(1.0,1.0,1.0);
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractivePoints) {
      InteractivePoints old = (InteractivePoints) _element;
      setNumberOfPoints(old.getNumberOfPoints());
      setData(old.getData());
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  protected void setNumberOfPoints (int _n) {
    if (_n==numPoints) return;
    if (_n<1) return;
    numPoints = _n;
    coordinates  = new double [numPoints][3];
    aPoints = new int [numPoints];
    bPoints = new int [numPoints];
    pointObjects = new Object3D[numPoints];
    for (int i=0; i<numPoints; i++) {
      coordinates[i][0] = coordinates[i][1] = coordinates[i][2] = 0.0;
      pointObjects[i] = new Object3D(this,i);
    }
    hasChanged = true;
  }
  public int getNumberOfPoints () {  return numPoints; }

  public void setData (double[][] _data) {
    if (_data==null) { numPoints=-1; coordinates = null; return; }
    if (numPoints!=_data.length) setNumberOfPoints(_data.length);
    int maxPoints = Math.max(_data.length,numPoints);
    int n = Math.min(_data[0].length,3);
    for (int i=0; i<maxPoints; i++)  for (int k=0; k<n; k++) coordinates[i][k] = _data[i][k];
    hasChanged = true;
  }

  public double[][] getData () { return coordinates; }

  /**
   * Allow for setting individual colors to each point
   * @param colors null if all points should be of the same color (given by style) 
   */
  public void setColors(Color[] colors) {
    pointColor = colors;
    hasChanged = true;
  }

  /**
   * Allow for setting individual colors to each point
   * @param colors null if all points should be of the same color (given by style) 
   */
  public void setColors(int[] colors) {
    if (colors==null) pointColor = null;
    else {
      if (pointColor==null || pointColor.length!=colors.length) pointColor = new Color[colors.length];
      for (int i=0; i<colors.length; i++) pointColor[i] = DisplayColors.getLineColor(colors[i]);
    }
    hasChanged = true;
  }

  /**
   * Returns the color of the point with that index
   * @param index
   * @return
   */
  public Color getPointColor(int index) {
    if (pointColor==null) return style.edgeColor;
    if (index<0 || index>=pointColor.length) return style.edgeColor;
    return pointColor[index];
  }

  /**
   * Allow for setting individual widths to each point
   * @param widths null if all points should be of the same width (given by style) 
   */
  public void setWidths(int[] widths) {
    if (widths==null) pointStroke = null;
    else {
      if (pointStroke==null || pointStroke.length!=widths.length) pointStroke = new Stroke[widths.length];
      for (int i=0; i<widths.length; i++) pointStroke[i] = new BasicStroke(widths[i]);
    }
    hasChanged = true;
  }

  /**
   * Allow for setting individual widths to each point
   * @param widths null if all points should be of the same width (given by style) 
   */
  public void setWidths(double[] widths) {
    if (widths==null) pointStroke = null;
    else {
      if (pointStroke==null || pointStroke.length!=widths.length) pointStroke = new Stroke[widths.length];
      for (int i=0; i<widths.length; i++) pointStroke[i] = new BasicStroke((float) widths[i]);
    }
    hasChanged = true;
  }
  
// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------


  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    return null;
   }

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!(numPoints>0 && visible)) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints(_panel);
    return pointObjects;
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    if (style.edgeColor!=null) {
      java.awt.Color theColor = _panel.projectColor(getPointColor(_index),pointObjects[_index].distance);
      _g2.setStroke(pointStroke==null ? style.edgeStroke : pointStroke[Math.min(_index,pointStroke.length-1)]);
      _g2.setColor (theColor);
      _g2.drawLine(aPoints[_index],bPoints[_index],aPoints[_index],bPoints[_index]);
    }
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!(numPoints>0 && visible)) return;
    Graphics2D g2 = (Graphics2D) _g;
    // if (hasChanged || _panel!=panelWithValidProjection)
    projectPoints(_panel);
    if (style.edgeColor!=null) {
//      g2.setColor (style.edgeColor);
      g2.setStroke(style.edgeStroke);
      for (int i=0; i<numPoints; i++) {
        g2.setColor (getPointColor(i));
        if (pointStroke!=null) g2.setStroke(pointStroke[Math.min(i,pointStroke.length-1)]);
        g2.drawLine(aPoints[i],bPoints[i],aPoints[i],bPoints[i]);
      }
    }
  }

// ----------------------------------------------
// Implementation of Measured3D
// ----------------------------------------------

  public boolean isMeasured () { return canBeMeasured && visible && numPoints>0;  }

  public double getXMin () {
    if (numPoints<=0) return 0.0;
    double min = Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[i][0]<min) min = coordinates[i][0];
    if (group==null) return min;
    return group.x + min*group.sizex;
  }
  public double getXMax () {
    if (numPoints<=0) return 0.0;
    double max = -Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[i][0]>max) max = coordinates[i][0];
    if (group==null) return max;
    return group.x + max*group.sizex;
  }
  public double getYMin () {
    if (numPoints<=0) return 0.0;
    double min = Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[i][1]<min) min = coordinates[i][1];
    if (group==null) return min;
    return group.y + min*group.sizey;
  }
  public double getYMax () {
    if (numPoints<=0) return 0.0;
    double max = -Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[i][1]>max) max = coordinates[i][1];
    if (group==null) return max;
    return group.y + max*group.sizey;
  }
  public double getZMin () {
    if (numPoints<=0) return 0.0;
    double min = Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[i][2]<min) min = coordinates[i][2];
    if (group==null) return min;
    return group.z + min*group.sizez;
  }
  public double getZMax () {
    if (numPoints<=0) return 0.0;
    double max = -Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[i][2]>max) max = coordinates[i][2];
    if (group==null) return max;
    return group.z + max*group.sizez;
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
//    System.out.println("Projecting polygon");
    // Project all points and compute the center of the polygon
    double origin[] = new double[3];
    double size[] = new double[3];
    if (group==null) {
      origin[0] = x;     origin[1] = y;     origin[2] = z;
      size[0]   = sizex; size[1]   = sizey; size[2] = sizez;
    }
    else {
      origin[0] = group.x + x*group.sizex;   origin[1] = group.y + y*group.sizey;   origin[2] = group.z + z*group.sizez;
      size[0]   = sizex*group.sizex;         size[1]   = sizey*group.sizey;         size[2]   = sizez*group.sizez;
    }
    for (int i=0; i<numPoints; i++) {
      for (int k=0; k<3; k++) {
        double delta = coordinates[i][k]*size[k];
        point[k] = origin[k] + delta;
      }
      _panel.project (point,pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      pointObjects[i].distance = pixel[2];
    }
    hasChanged = false;
    panelWithValidProjection = _panel;
  }

}
