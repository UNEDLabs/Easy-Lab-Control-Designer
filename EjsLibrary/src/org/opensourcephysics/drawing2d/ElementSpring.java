/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;
import java.awt.geom.GeneralPath;
import org.opensourcephysics.drawing2d.utils.VectorAlgebra;

/**
 * <p>Title: ElementSpring</p>
 * <p>Description: A spring.</p>
 * @author Francisco Esquembre
 * @version June 2008
 */
public class ElementSpring extends ElementSegment {
  static public final double DEF_RADIUS = 0.05;
  static public final int DEF_LOOPS = 8;
  static public final int DEF_PPL = 15;
  
  // Configuration variables
  private double radius = DEF_RADIUS;
  private double solenoid = 0.0;
  private boolean thinExtremes = true;
  private int loops;
  private int pointsPerLoop;
  
  // Implementation variables
  private boolean needsToComputePath = true;
  private double[] pixelRadius = new double[2];
  private GeneralPath path;

  { // Initialization block
    setSize (new double[]{0.1,0.1});
    setResolution (DEF_LOOPS,DEF_PPL); // make sure arrays are allocated
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  /**
   * Sets the radius of the spring.
   * @param radius the radius of the spring (normal to its direction)
   */
  public void setRadius(double radius) {
    this.radius = radius;
    this.setNeedToProject(true);
  }

  /**
   * Gets the radius of the spring.
   */
  public double getRadius() {
    return this.radius;
  }

  /**
   * A double factor that makes the spring look like a solenoid.
   * @param _sol
   */
  public void setSolenoid(double _sol) { 
    this.solenoid = _sol;
    needsToComputePath = true;
  }

  /**
   * Returns the double factor used to make the spring look like a solenoid.
   * @return
   */
  public double getSolenoid() {
    return this.solenoid;
  }

  /**
   * Whether the extremes of the spring should be thin.
   * True by default
   * @param _thin
   */
  public void setThinExtremes(boolean _thin) { 
    this.thinExtremes = _thin; 
    needsToComputePath = true;
  }

  /**
   * Whether the extremes of the spring should be thin.
   * @return
   */
  public boolean isThinExtremes() {
    return this.thinExtremes;
  }

  /**
   * Configures the number of points used to display the spring
   * @param loops the number of total loops
   * @param pointsPerLoop the number of points per loop
   */
  public void setResolution (int _loops, int _ppl) {
    if(_loops==loops && _ppl==pointsPerLoop) return; // No need to recompute the path
    loops = _loops;
    pointsPerLoop = _ppl;
    needsToComputePath = true;
  }
  
  /**
   * Returns the number of loops of the spring
   * @return
   */
  public int getLoops() { return this.loops; }
  
  /**
   * Returns the number of points per loop
   * @return
   */
  public int getPointsPerLoop() { return this.pointsPerLoop; }
  
  // -------------------------------------
  // Parent methods overwritten
  // -------------------------------------

  @Override
  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible()) return;
    if (hasChanged() || needsToProject()) projectPoints();
    if (needsToComputePath) computePath();
    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(getStyle().getLineStroke());
    g2.setColor(getStyle().getLineColor());
    g2.draw(path);
  }
  
  @Override
  protected void projectPoints() {
    super.projectPoints();
    getPanel().projectSize(getPosition(), new double[] { radius, 0 }, pixelRadius);
    needsToComputePath = true;
  }
  
  // -------------------------------------
  // Private methods
  // -------------------------------------
  
  private void computePath () {
    int segments = loops*pointsPerLoop;
    double delta=2.0*Math.PI/pointsPerLoop;
    double rad = pixelRadius[0];
    if (radius<0) delta*=-1;
    int pre = pointsPerLoop/2;
    GeneralPath newPath = new GeneralPath(); // requires 1.6 java.awt.geom.Path2D.WIND_NON_ZERO,segments+1);
    double sx = pixelEnd[0]-pixelOrigin[0];
    double sy = pixelEnd[1]-pixelOrigin[1];
    double[] size = new double[] {sx, sy, 0};
    double[] u1 = VectorAlgebra.normalTo(size);
    double[] u2 = VectorAlgebra.normalize(VectorAlgebra.crossProduct(size, u1));
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
      double x = pixelOrigin[0] + i*sx/segments + k*rad*(cos*u1[0] + sin*u2[0])/pre;
      double y = pixelOrigin[1] + i*sy/segments + k*rad*(cos*u1[1] + sin*u2[1])/pre;
      if (solenoid!=0.0)  {
        double cte = k*Math.cos(i*2*Math.PI/pointsPerLoop)/pre;
        x += solenoid*cte*sx;
        y += solenoid*cte*sy;
      }
      if (i==0) newPath.moveTo((float)x,(float)y); // typecasting to float is required prior to 1.6
      else newPath.lineTo((float)x,(float)y);
    }
    path = newPath;
    needsToComputePath = false;
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
