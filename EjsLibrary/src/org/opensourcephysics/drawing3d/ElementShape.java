/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.*;

/**
 * <p>Title: ElementShape</p>
 * <p>Description: A 2D shape displayed in a 3D scene</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementShape extends Element {
  public final static int NONE             = 0;
  public final static int ELLIPSE          = 1;
  public final static int RECTANGLE        = 2;
  public final static int ROUND_RECTANGLE  = 3;
  public final static int WHEEL            = 4;

  // Configuration variables
  private double angle = 0.0;
  private int shapeType = -1;  // Make sure a shape is created
  private boolean trueSize = false; // Whether size is in pixels

  {
    setSizeXYZ(0.1, 0.1, 0.1);
    setShapeType(ELLIPSE);
  }

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementShape(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementShape(this);
    }
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  /**
   * Sets the angle to rotate the shape (in 2D) before displying it
   */
  public void setRotationAngle(double angle) {
    if (this.angle==angle) return;
    this.angle = angle;
    addChange(Element.CHANGE_PROJECTION);
  }

  /**
   * Return the angle to rotate the shape (in 2D) before displying it
   * @return double
   */
  public double getRotationAngle() {
    return this.angle;
  }

  /**
   * Set the type of the shape to draw
   */
  public void setShapeType (int _type) {
    if (shapeType==_type) return;
    shapeType = _type;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Get the type of the shape to draw
   */
  public int getShapeType () { return shapeType; }

  /**
   * Set whether the size is taken in pixels
   * @param _s boolean
   */
  public void setPixelSize (boolean _s) {
    if (trueSize==_s) return;
    trueSize = _s;
    addChange(Element.CHANGE_SIZE);
  }

  /**
   * Get whether the size is taken in pixels
   * @return boolean
   */
  public boolean isPixelSize () { return trueSize; }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  // -------------------------------------
  // Interaction
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
