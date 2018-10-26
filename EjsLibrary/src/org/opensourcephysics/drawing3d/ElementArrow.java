/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: ElementArrow</p>
 * <p>Description: An Arrow</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara (CJB)
 * @version August 2009
 */
public class ElementArrow extends ElementSegment {
  /**
   * The element has an arrow head
   */
  static final public int ARROW      = 0;
  /**
   * The element looks like a segment
   */
  static final public int SEGMENT    = 1;
  /**
   * The element has a box at its top
   */
  static final public int BOX        = 2;
  /**
   * The element looks like a triangle with a width set by the width percentage
   */
  static final public int TRIANGLE   = 3;
  /**
   * The element looks like a double triangle with a width set by the width percentage.
   * The line and fill colors are used to color the two triangles 
   */
  static final public int RHOMBUS   = 4;

  private int arrowType = ARROW;
  
  private double headSize = 1.25;
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementArrow(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementArrow(this);
    }
  }
  
  {
    setSize (new double[]{0.1,0.1,0.1});
    setArrowType(ARROW);
    getStyle().setDrawingFill(true);
    getStyle().setRelativePosition(Style.NORTH_EAST);
  }
  
  //-------------------------------------
  //New configuration methods
  //-------------------------------------

  /**
   * Set the type of decoration at the head of the arrow. Either ARROW, SEGMENT (none) or BOX
   */
  public void setArrowType (int _type) {
    if (arrowType==_type) return;
    arrowType = _type;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the arrow type
   * @return int the arrow type
   */
  public int getArrowType() { return this.arrowType; }
  
  public void setHeadSize(double value){ 
	  this.headSize = value;
	  addChange(Element.CHANGE_POSITION_AND_SIZE);
  }
  
  /**
   * Returns the head size
   * @return double the size of the head
   */
  public double getHeadSize(){ return this.headSize; }
  
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
