/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Resolution;

/**
 * <p>Title: ElementSpring</p>
 * <p>Description: A 3D spring</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
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

  {                                           
    setSize (new double[]{0.1,0.1,0.1});
    setResolution (DEF_LOOPS,DEF_PPL); // make sure arrays are allocated
  }

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementSpring(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementSpring(this);
    }
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Sets the radius of the spring.
   * @param _radius the radius of the spring (normal to its direction)
   */
  public void setRadius(double _radius) {
    if (this.radius==_radius) return;
    this.radius = _radius;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Gets the radius of the spring.
   */
  public double getRadius() { return this.radius; }

  /**
   * A double factor that makes the spring look like a solenoid.
   * @param _sol
   */
  public void setSolenoid(double _sol) { 
    if (this.solenoid==_sol) return;
    this.solenoid = _sol;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the double factor used to make the spring look like a solenoid.
   * @return double
   */
  public double getSolenoid() { return this.solenoid; }

  /**
   * Whether the extremes of the spring should be thin.
   * True by default
   * @param _thin
   */
  public void setThinExtremes(boolean _thin) { 
    if (this.thinExtremes==_thin) return; 
    this.thinExtremes = _thin; 
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the extremes of the spring should be thin.
   * @return boolean
   */
  public boolean isThinExtremes() { return this.thinExtremes; }

  /**
   * Configures the number of points used to display the spring
   * @param _loops the number of total loops
   * @param _ppl the number of points per loop
   */
  public void setResolution (int _loops, int _ppl) {
    if(_loops==loops && _ppl==pointsPerLoop) return; // No need to recompute the path
    loops = _loops;
    pointsPerLoop = _ppl;
    getStyle().setResolution(new Resolution(loops,pointsPerLoop,1)); // The 1 is meaningless
    addChange(Element.CHANGE_SHAPE);
  }
  
  /**
   * Returns the number of loops of the spring
   * @return int
   */
  public int getLoops() { return this.loops; }
  
  /**
   * Returns the number of points per loop
   * @return int
   */
  public int getPointsPerLoop() { return this.pointsPerLoop; }
  
  // -------------------------------------
  // Private methods
  // -------------------------------------
  
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
