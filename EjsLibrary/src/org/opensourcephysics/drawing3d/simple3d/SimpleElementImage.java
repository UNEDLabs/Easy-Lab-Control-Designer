/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.*;
import java.awt.geom.*;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementImage;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: SimpleElementImage</p>
 * <p>Description: A Shape using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementImage extends SimpleElement {
  
  // Implementation variables
  private double[] coordinates = new double[3];
  private double[] size = new double[3];
  private double[] pixel = new double[3];     // The ouput of position projections
  private AffineTransform transform = new AffineTransform();
  
  public SimpleElementImage(ElementImage _element) { 
    super(_element); 
    objects = new Object3D[] {new Object3D(this, 0)};
  }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & FORCE_RECOMPUTE)!=0 || (_cummulativeChange & Element.CHANGE_PROJECTION)!=0 || (_cummulativeChange & Element.CHANGE_SHAPE)!=0) { 
      projectPointAndSize();
    }
  }

  @Override
  public void styleChanged(int _change) {
    // Special case, since the change of rel position can be computed once
    if (_change==Style.CHANGED_RELATIVE_POSITION) element.addChange(Element.CHANGE_SHAPE); 
    else super.styleChanged(_change);
  }

  // -------------------------------------
  // Implementation of SimpleElement
  // -------------------------------------

  @Override
  void draw(Graphics2D _g2, int _index) { drawIt(_g2); }

  @Override
  void drawQuickly(Graphics2D _g2) { drawIt(_g2); }

  // -------------------------------------
  // Private methods
  // ---------------------------

  private void projectPointAndSize() {
    ElementImage elementImage = (ElementImage) element;
    Image image = elementImage.getImage();
    if (image==null) return;
    // First compute the projected position
    System.arraycopy(Element.STD_ORIGIN, 0, coordinates, 0, 3);
    element.sizeAndToSpaceFrame(coordinates);
    System.arraycopy(coordinates,0,pixel,0,3);
    element.getPanel().projectPosition(pixel);
    objects[0].setDistance(pixel[2]*style.getDepthFactor());
    if (elementImage.isTrueSize()) {
      size[0] = image.getWidth(null);
      size[1] = image.getHeight(null);
    } 
    else { // compute the projected size
      size[0] = element.getSizeX();
      size[1] = element.getSizeY();
      size[2] = element.getSizeZ();
      element.getPanel().projectSize(coordinates, size);
    }
    double dx, dy;
    switch (style.getRelativePosition()) {
      default :
      case Style.CENTERED   : dx = size[0]/2; dy = size[1]/2; break;
      case Style.NORTH      : dx = size[0]/2; dy = 0;         break;
      case Style.SOUTH      : dx = size[0]/2; dy = size[1];   break;
      case Style.EAST       : dx = size[0];   dy = size[1]/2; break;
      case Style.WEST       : dx = 0;         dy = size[1]/2; break;
      case Style.NORTH_EAST : dx = size[0];   dy = 0;         break;
      case Style.NORTH_WEST : dx = 0;         dy = 0;         break;
      case Style.SOUTH_EAST : dx = size[0];   dy = size[1];   break;
      case Style.SOUTH_WEST : dx = 0;         dy = size[1];   break;
    }
    transform.setToTranslation(pixel[0], pixel[1]);
    double angle = elementImage.getRotationAngle(); 
    if (angle!=0.0) transform.rotate(-angle);
    transform.translate(-dx,-dy);
    if (!elementImage.isTrueSize())transform.scale(size[0]/image.getWidth(null),size[1]/image.getHeight(null));
  }

  private void drawIt(Graphics2D _g2) {
    ElementImage elementImage = (ElementImage) element;
    Image image = elementImage.getImage();
    if (image==null) return;
    _g2.drawImage(image,transform,element.getPanel().getComponent()); // Setting the panel as observer is important!

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
