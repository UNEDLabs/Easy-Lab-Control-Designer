/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.*;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.display.TextLine;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementText;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: SimpleElementText</p>
 * <p>Description: A Text using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementText extends SimpleElement {
	
  private double[] coordinates = new double[3]; // The transformed coordinates
  private double[] pixel = new double[3]; // The point for all projections
  private AffineTransform transform = new AffineTransform();

  public SimpleElementText(ElementText _element) {
    super(_element);
    objects = new Object3D[] {new Object3D(this, 0)};
  } 
    
  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & FORCE_RECOMPUTE)!=0) {
      computePosition();
      projectPoint();
    }
    else if ((_cummulativeChange & Element.CHANGE_PROJECTION)!=0) projectPoint();
  }

  @Override
  public void styleChanged(int _change) {
    if (_change==Style.CHANGED_RELATIVE_POSITION) {
      TextLine textLine = ((ElementText) element).getTextLine(); 
      switch (style.getRelativePosition()) {
        default :
        case Style.CENTERED   : 
        case Style.NORTH      : 
        case Style.SOUTH      : textLine.setJustification(TextLine.CENTER); break;
        case Style.EAST       : 
        case Style.NORTH_EAST : 
        case Style.SOUTH_EAST : textLine.setJustification(TextLine.RIGHT); break;
        case Style.WEST       : 
        case Style.NORTH_WEST : 
        case Style.SOUTH_WEST : textLine.setJustification(TextLine.LEFT); break;
      }
    }
    else super.styleChanged(_change);
  }

  // -------------------------------------
  // Implementation of SimpleElement
  // -------------------------------------

  @Override
  public void draw(Graphics2D _g2, int _index) {
    Color theColor = element.getPanel().projectColor(style.getLineColor(), objects[0].getDistance());
    drawIt(_g2, theColor);
  }

  @Override
  public void drawQuickly(Graphics2D _g2) { 
    drawIt(_g2, style.getLineColor()); 
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  
  private void computePosition() {
    System.arraycopy(Element.STD_ORIGIN, 0, coordinates, 0, 3);
    element.sizeAndToSpaceFrame(coordinates);
  }
  
  private void projectPoint() {
    System.arraycopy(coordinates,0,pixel,0,3);
    element.getPanel().projectPosition(pixel);
    objects[0].setDistance(pixel[2]*style.getDepthFactor());
  }

  private void drawIt(Graphics2D _g2, Color _color) {
    ElementText textElement = (ElementText) element;
    TextLine textLine = textElement.getTextLine();
    textLine.setColor(_color);
    double angle = textElement.getRotationAngle();
    if(angle!=0.0) {
      AffineTransform originalTransform = _g2.getTransform();
      transform.setTransform(originalTransform);
      transform.rotate(-angle, pixel[0], pixel[1]);
      _g2.setTransform(transform);
      textLine.drawText(_g2, (int) pixel[0], (int) pixel[1]);
      _g2.setTransform(originalTransform);
    } 
    else textLine.drawText(_g2, (int) pixel[0], (int) pixel[1]);
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
