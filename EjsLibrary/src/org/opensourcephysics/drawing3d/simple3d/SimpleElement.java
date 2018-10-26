/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: SimpleElement</p>
 * <p>Description: Defines methods required by the painter's algorithm drawing scheme.</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public abstract class SimpleElement implements ImplementingObject {

  /**
   * Any of these changes forces a recomputation of the SimpleElement
   */
  static public final int FORCE_RECOMPUTE = Element.CHANGE_GROUP | Element.CHANGE_POSITION | Element.CHANGE_SIZE | 
                                            Element.CHANGE_TRANSFORMATION | Element.CHANGE_RESOLUTION;

  protected Element element;
  protected Style style;
  protected Object3D[] objects = null; // The Objects3D for this Drawable3D

  protected SimpleElement(Element _element) { 
    this.element = _element;
    this.style = element.getStyle();
    element.addChange(FORCE_RECOMPUTE);
  }
  
  // --------------------------------------
  // Implementation of ImplementingObject
  // --------------------------------------

  final public void addToScene() {} // Does nothing

  final public void removeFromScene() {} // Does nothing

  abstract public void processChanges(int _change, int _cummulativeChange);

  public void styleChanged(int _change) {
    switch(_change) {
      case Style.CHANGED_RELATIVE_POSITION: element.addChange(Element.CHANGE_POSITION); break;
      case Style.CHANGED_RESOLUTION: element.addChange(Element.CHANGE_RESOLUTION); break;
    }
  }

  // --------------------------------------
  // Methods for the painter's algorithm
  // --------------------------------------

  /**
   * Returns an array of Objects3D to sort according to its distance to the eye.
   */
  public Object3D[] getObjects3D() { return objects; }

  /**
   * Draws a given Object3D (indicated by its index).
   */
  abstract void draw(java.awt.Graphics2D g, int index);

  /**
   * Sketches the drawable
   */
  abstract void drawQuickly(java.awt.Graphics2D g);

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
