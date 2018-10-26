/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.*;
import org.opensourcephysics.drawing3d.interaction.*;

/**
 * <p>Title: ElementSegment</p>
 * <p>Description: A Segment</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementSegment extends Element {

  /* Implementation variables */
  protected double[] origin = new double[3]; // The origin
  protected double[] end    = new double[3]; // The end point

  {
    setSize (new double[]{0.1,0.1,0.1});
    getStyle().setRelativePosition(Style.NORTH_EAST); 
    getStyle().setDrawingLines(true);
    getStyle().setDrawingFill(false);
  }

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementSegment(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementSegment(this);
    }
  }
  
  // -------------------------------------
  // Private methods
  // -------------------------------------

  @Override
  public void projectInteractionPoints() {
    switch (getStyle().getRelativePosition()) {
      case Style.NORTH_EAST : 
        System.arraycopy(STD_ORIGIN, 0, origin, 0, 3);
        System.arraycopy(STD_CENTERED_END, 0, center, 0, 3);
        System.arraycopy(STD_END, 0, end, 0, 3);
        break;
      default :
      case Style.CENTERED : 
        System.arraycopy(STD_CENTERED_ORIGIN, 0, origin, 0, 3);
        System.arraycopy(STD_ORIGIN, 0, center, 0, 3);
        System.arraycopy(STD_CENTERED_END, 0, end, 0, 3);
        break;
      case Style.SOUTH_WEST : 
        System.arraycopy(STD_END, 0, origin, 0, 3);
        System.arraycopy(STD_CENTERED_END, 0, center, 0, 3);
        System.arraycopy(STD_ORIGIN, 0, end, 0, 3);
        break;
    }
    sizeAndToSpaceFrame(origin);
    sizeAndToSpaceFrame(center);
    sizeAndToSpaceFrame(end);
    getPanel().projectPosition(origin);
    getPanel().projectPosition(center);
    getPanel().projectPosition(end);
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  
  @Override
  protected InteractionTarget getTargetHit(int _xpix, int _ypix) {
//    if (!isVisible()) return null;
    if (!isEnabled()) return null;
    int sensitivity = getStyle().getSensitivity();
    if (targetPosition.isEnabled()) {
      if (Math.abs(origin[0]-_xpix)<sensitivity && Math.abs(origin[1]-_ypix)<sensitivity) return this.targetPosition;
      if (Math.abs(center[0]-_xpix)<sensitivity && Math.abs(center[1]-_ypix)<sensitivity) return this.targetPosition;
    }
    if (targetSize.isEnabled()&&Math.abs(end[0]-_xpix)<sensitivity&&Math.abs(end[1]-_ypix)<sensitivity) return this.targetSize;
    return null;
  }

  /**
   * Returns the coordinates of the projection of the origin of the segment
   * @return
   */
  double[] getProjectedOrigin() { return origin; }
  
  /**
   * Returns the coordinates of the projection of the end point of the segment
   * @return
   */
  double[] getProjectedEnd() { return end; }
  
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
