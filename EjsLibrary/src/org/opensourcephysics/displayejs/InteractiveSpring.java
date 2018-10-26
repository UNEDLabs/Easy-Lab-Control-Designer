/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.displayejs.utils.VectorAlgebra;

public class InteractiveSpring extends AbstractInteractiveElement {
  // Configuration variables
  protected int loops=-1, pointsPerLoop=-1;  // Make sure arrays are allocated

  protected double solenoid=0.0;

  protected boolean thinExtremes = true;
  /**
   * The radius of the spring (normal to its direction)
   */
  protected double radius=0.1;

  // Implementation variables
  private int    segments = 0;
  private int    aPoints[]=null, bPoints[]=null;
  private double pointCoordinates[][] = null;
  private double pixel[]  = new double[3];
  private Object3D[] objects=null;

  /**
   * Default constructor
   */
  public InteractiveSpring () { this (0.1); }
  /**
   * Special constructor that allows to specify the radius of the spring
   * @param _radius the radius of the spring (normal to its direction)
   */
  public InteractiveSpring (double _radius) {
    setRadius (_radius);
    resolution=new Resolution (8,15); // 8 loops, 15 points per loop
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveSpring) setRadius (((InteractiveSpring) _element).getRadius());
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  /**
   * Set the radius of the spring.
   * @param _radius the radius of the spring (normal to its direction)
   */
  public void setRadius (double _radius) { this.radius = _radius; hasChanged = true; }

  /**
   * Get the radius of the spring.
   */
  public double getRadius () { return this.radius; }

  public void setSolenoid(double _sol) { solenoid = _sol; hasChanged = true; }

  public void setThinExtremes(boolean _thin) { thinExtremes = _thin; hasChanged = true; }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged) { computePoints(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    if (sizeEnabled     && Math.abs(aPoints[segments]-_xpix)<SENSIBILITY && Math.abs(bPoints[segments]-_ypix)<SENSIBILITY) return new InteractionTargetElementSize(this);
    if (positionEnabled && Math.abs(aPoints[0]  -_xpix)<SENSIBILITY      && Math.abs(bPoints[0]  -_ypix)<SENSIBILITY)      return new InteractionTargetElementPosition(this);
    return null;
   }

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!visible) return null;
    if (hasChanged) { computePoints(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
    return objects;
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = _panel.projectColor(style.edgeColor,objects[_index].distance);
    _g2.setStroke(style.edgeStroke);
    _g2.setColor (theColor);
    _g2.drawLine(aPoints[_index],bPoints[_index],aPoints[_index+1],bPoints[_index+1]);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!visible) return;
    if (hasChanged) { computePoints(); projectPoints(_panel); }
    else // if (_panel!=panelWithValidProjection)
      projectPoints(_panel);
    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(style.edgeStroke);
    g2.setColor (style.edgeColor);
    g2.drawPolyline(aPoints,bPoints,segments+1);
//    for (int i=0; i<segments; i++) _g.drawLine(aPoints[i],bPoints[i],aPoints[i+1],bPoints[i+1]);
  }

// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

  public double getXMin () {
    if (group==null) return x + Math.min(sizex,0);
    return group.x + x + Math.min(sizex*group.sizex,0);
  }
  public double getXMax () {
    if (group==null) return x + Math.max(sizex,0);
    return group.x + x + Math.max(sizex*group.sizex,0);
  }
  public double getYMin () {
    if (group==null) return y + Math.min(sizey,0);
    return group.y + y + Math.min(sizey*group.sizey,0);
  }
  public double getYMax () {
    if (group==null) return y + Math.max(sizey,0);
    return group.y + y + Math.max(sizey*group.sizey,0);
  }
  public double getZMin () {
    if (group==null) return z + Math.min(sizez,0);
    return group.z + z + Math.min(sizez*group.sizez,0);
  }
  public double getZMax () {
    if (group==null) return z + Math.max(sizez,0);
    return group.z + z + Math.max(sizez*group.sizez,0);
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
//    System.out.println("Projecting spring");
    if (group==null) {
      for (int i = 0; i < segments; i++) {
        _panel.project(pointCoordinates[i], pixel);
        aPoints[i] = (int) pixel[0];
        bPoints[i] = (int) pixel[1];
        objects[i].distance = pixel[2]; //distance is given by the first point
      }
      _panel.project(pointCoordinates[segments], pixel);
      aPoints[segments] = (int) pixel[0];
      bPoints[segments] = (int) pixel[1];
    }
    else {
      double[] projInput = new double[3];
      for (int i = 0; i < segments; i++) {
        projInput[0] = group.x + pointCoordinates[i][0]*group.sizex;
        projInput[1] = group.y + pointCoordinates[i][1]*group.sizey;
        projInput[2] = group.z + pointCoordinates[i][2]*group.sizez;
        _panel.project(projInput, pixel);
        aPoints[i] = (int) pixel[0];
        bPoints[i] = (int) pixel[1];
        objects[i].distance = pixel[2]; //distance is given by the first point
      }
      projInput[0] = group.x + pointCoordinates[segments][0]*group.sizex;
      projInput[1] = group.y + pointCoordinates[segments][1]*group.sizey;
      projInput[2] = group.z + pointCoordinates[segments][2]*group.sizez;
      _panel.project(projInput, pixel);
      aPoints[segments] = (int) pixel[0];
      bPoints[segments] = (int) pixel[1];
    }
    panelWithValidProjection = _panel;
  }

  private void computePoints () {
//    System.out.println("Computing spring");
    int theLoops = loops, thePPL = pointsPerLoop;
    if (resolution!=null) {
      switch (resolution.type) {
        case Resolution.DIVISIONS :
          theLoops = Math.max(resolution.n1,0);
          thePPL   = Math.max(resolution.n2,1);
          break;
      }
    }
    if (theLoops==loops && thePPL==pointsPerLoop); // No need to reallocate arrays
    else { // Reallocate arrays
      loops = theLoops;
      pointsPerLoop = thePPL;
      segments = loops*pointsPerLoop; // + 3;
      pointCoordinates  = new double [segments+1][3];
      aPoints = new int [segments+1];
      bPoints = new int [segments+1];
      objects = new Object3D[segments];
      for (int i=0; i<segments; i++) objects[i] = new Object3D(this,i);
    }
    Point3D  size = new Point3D (sizex,sizey,sizez);
    Point3D u1 = VectorAlgebra.normalTo (size);
    Point3D u2 = VectorAlgebra.normalize (VectorAlgebra.crossProduct(size,u1));
    double delta=2.0*Math.PI/pointsPerLoop;
    if(radius<0)delta*=-1;
    int pre = pointsPerLoop/2;
    for (int i=0; i<=segments; i++) {
      int k;
      if (thinExtremes) {
        if (i < pre) k = 0;
        else if (i < pointsPerLoop) k = i - pre;
        else if (i > (segments - pre)) k = 0;
        else if (i > (segments - pointsPerLoop)) k = segments - i - pre;
        else k = pre;
      }
      else k = pre;
      double angle = Math.PI/2 + i*delta;
      double cos = Math.cos(angle), sin = Math.sin(angle);
      pointCoordinates[i][0] = x + i*size.x/segments + k*radius*(cos*u1.x + sin*u2.x)/pre;
      pointCoordinates[i][1] = y + i*size.y/segments + k*radius*(cos*u1.y + sin*u2.y)/pre;
      pointCoordinates[i][2] = z + i*size.z/segments + k*radius*(cos*u1.z + sin*u2.z)/pre;
      if (solenoid!=0.0)  {
        double cte = k*Math.cos(i*2*Math.PI/pointsPerLoop)/pre;
        pointCoordinates[i][0] += solenoid*cte*size.x;
        pointCoordinates[i][1] += solenoid*cte*size.y;
        pointCoordinates[i][2] += solenoid*cte*size.z;
      }
    }
    hasChanged = false;
  }

}
