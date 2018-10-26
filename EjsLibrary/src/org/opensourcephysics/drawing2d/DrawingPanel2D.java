/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

/**
 *
 * <p>Title: DrawingPanel2D</p>
 *
 * <p>Description: The 2D implementation of a basic drawing panel that uses an org.opensourcephysics.display.InteractivePanel.</p>
 * 
 * <p>Copyright: Open Source Physics project</p>
 * @author Francisco Esquembre
 * @version July 2008
 */
public class DrawingPanel2D extends org.opensourcephysics.display.InteractivePanel implements DrawingPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DrawingPanel2D() { super(); }

  // ---------------------------------
  // Implementation of DrawingPanel
  // ---------------------------------
  
  /**
   * Getting the pointer to the real JPanel in it
   * @return java.awt.Component
   */
  public java.awt.Component getComponent() { return this; }

  /**
   * Converts a world point of the scene into a 2D point of the screen.
   * It can also provide additional information, depending on the implementation.
   * 
   * @param coordinate The coordinates of the point of the scene
   * The input coordinates are not modified.
   * @param pixel A place-holder for the coordinates of the point of the screen.
   * @return returns the same input pixel
   */
  public double[] projectPosition(double[] p, double[] pixel) {
    pixel[0] = super.xToPix(p[0]);
    pixel[1] = super.yToPix(p[1]);
    return pixel;
  }

  /**
   * Converts a world size at a given point into a size in the screen
   * @param p double[] The coordinates of the point at which the world
   * size was measured.
   * @param size double[] The size in the world coordinates
   * @param pixelSize double[] A place-holder for the result
   * @return double[] returns the same input pixelSize
   */
  public double[] projectSize(double[] p, double[] size, double[] pixelSize) {
    pixelSize[0] = xPixPerUnit*size[0];
    pixelSize[1] = yPixPerUnit*size[1];
    return pixelSize;
  }

  // ---------------------------------
  // Overwriting super's methods
  // ---------------------------------

  @Override
  public void addDrawable(org.opensourcephysics.display.Drawable drawable) {
    if (drawable instanceof Element) ((Element) drawable).setPanel(this);
    super.addDrawable(drawable);
  }

  @Override
  public void addDrawableAtIndex(int _index, org.opensourcephysics.display.Drawable drawable) {
    if (drawable instanceof Element) ((Element) drawable).setPanel(this);
    super.addDrawableAtIndex(_index,drawable);
  }

  @Override
  public void invalidateImage() {
    super.invalidateImage();
    // Instruct all child elements that they need to re-project themselves
    for (Object drawable : super.getDrawables()) 
      if (drawable instanceof Element) ((Element)drawable).setNeedToProject(true);
  }

  @Override
  protected boolean isValidImage() {
    if (!super.isValidImage()) return false;
    for (Object drawable : super.getDrawables()) 
      if (drawable instanceof Element && ((Element)drawable).hasChanged()) return false;
    return true;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
