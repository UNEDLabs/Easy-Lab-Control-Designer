/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.*;
import java.awt.geom.*;

import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementShape;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: SimpleElementShape</p>
 * <p>Description: A Shape using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementShape extends SimpleElement {
  
  // Implementation variables
  private double[] coordinates = new double[3];
  private double[] size = new double[3];
  private double[] pixel = new double[3];     // The ouput of position projections
  private RectangularShape shape;
  private Line2D line1, line2;
  private Shape trShape, trLine1, trLine2;
  private AffineTransform transform = new AffineTransform();
  
  public SimpleElementShape(ElementShape _element) { 
    super(_element); 
    objects = new Object3D[] {new Object3D(this, 0)};
    recreateShape();
  }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & Element.CHANGE_SHAPE)!=0) {
      recreateShape();
      projectPointAndSize();
    }
    else if ((_cummulativeChange & FORCE_RECOMPUTE)!=0 || (_cummulativeChange & Element.CHANGE_PROJECTION)!=0) { 
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
  void draw(Graphics2D _g2, int _index) {
    DrawingPanel3D panel = element.getPanel();
    double d = objects[0].getDistance();
    // Allow the panel to adjust color according to depth
    drawIt(_g2, panel.projectColor(style.getLineColor(),d), panel.projectPaint(style.getFillColor(), d));
  }

  @Override
  void drawQuickly(Graphics2D _g2) {
    drawIt(_g2, style.getLineColor(), style.getFillColor());
  }

  // -------------------------------------
  // Private methods
  // ---------------------------

  private void projectPointAndSize() {
    ElementShape elementShape = (ElementShape) element;
    // First compute the projected position
    System.arraycopy(Element.STD_ORIGIN, 0, coordinates, 0, 3);
    element.sizeAndToSpaceFrame(coordinates);
    System.arraycopy(coordinates,0,pixel,0,3);
    element.getPanel().projectPosition(pixel);
    objects[0].setDistance(pixel[2]*style.getDepthFactor());
    size[0] = element.getSizeX();
    size[1] = element.getSizeY();
    size[2] = element.getSizeZ();
    if (!elementShape.isPixelSize()) { // compute the projected size
      element.getPanel().projectSize(coordinates, size);
    }
    transform.setToTranslation(pixel[0], pixel[1]);
    double angle = elementShape.getRotationAngle(); 
    if (angle!=0.0) transform.rotate(-angle);
    transform.scale(size[0],size[1]);
    trShape = transform.createTransformedShape(shape); 
    trLine1 = transform.createTransformedShape(line1); 
    trLine2 = transform.createTransformedShape(line2); 
  }

  private void drawIt(Graphics2D _g2, Color _color, Paint _fill) {
    _g2.setStroke(style.getLineStroke());
    if (shape==null) {
      _g2.setColor(_color);
      _g2.drawOval( (int) pixel[0], (int) pixel[1], 1, 1); // draw a point
      return;
    }
    ElementShape elementShape = (ElementShape) element;
    if (style.isDrawingFill()) { // First fill the inside
      _g2.setPaint(_fill);
      _g2.fill(trShape);
    }
    if (style.isDrawingLines()) {
      _g2.setColor(_color);
      if (elementShape.getShapeType()==ElementShape.WHEEL) {
        _g2.draw(trLine1);
        _g2.draw(trLine2);
      }
      _g2.draw(trShape);
    }
  }

  private void recreateShape () {
    ElementShape elementShape = (ElementShape) element;
    int shapeType = elementShape.getShapeType();
    if (shapeType==ElementShape.NONE) {
      shape = null;
      return;
    }
    double x1,y1;
    switch (style.getRelativePosition()) {
      default :
      case Style.CENTERED   : x1 = -0.5; y1 = -0.5; break;
      case Style.NORTH      : x1 = -0.5; y1 =  0.0; break;
      case Style.SOUTH      : x1 = -0.5; y1 = -1.0; break;
      case Style.EAST       : x1 = -1.0; y1 = -0.5; break;
      case Style.WEST       : x1 =  0.0; y1 = -0.5; break;
      case Style.NORTH_EAST : x1 = -1.0; y1 =  0.0; break;
      case Style.NORTH_WEST : x1 =  0.0; y1 =  0.0; break;
      case Style.SOUTH_EAST : x1 = -1.0; y1 = -1.0; break;
      case Style.SOUTH_WEST : x1 =  0.0; y1 = -1.0; break;
    }
    double x2 = x1+1, y2 = y1+1;
    switch (shapeType) {
      case ElementShape.WHEEL           :  line1 = new Line2D.Double(x1+0.5,y1,x1+0.5,y2);
                                           line2 = new Line2D.Double(x1,y1+0.5,x2,y1+0.5);
                                           shape = new Ellipse2D.Double(x1,y1,1.0,1.0); 
                                           break;
      default :
      case ElementShape.ELLIPSE         : shape = new Ellipse2D.Double(x1,y1,1.0,1.0); break;
      case ElementShape.RECTANGLE       : shape = new Rectangle2D.Double(x1,y1,1.0,1.0); break;
      case ElementShape.ROUND_RECTANGLE : shape = new RoundRectangle2D.Double(x1,y1,1.0,1.0,0.3,0.3); break;
    }
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
