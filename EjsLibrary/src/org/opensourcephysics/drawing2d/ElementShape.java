/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;
import java.awt.geom.*;

/**
 * <p>Title: ElementShape</p>
 * <p>Description: Draws a shape at its position with the given size.
 * @author Francisco Esquembre
 * @version 1.0 June 2008
 * @version 1.1 December 2008
 */
public class ElementShape extends Element {
  public final static int NONE             = 0;
  public final static int ELLIPSE          = 1;
  public final static int RECTANGLE        = 2;
  public final static int ROUND_RECTANGLE  = 3;
  public final static int WHEEL            = 4;

  // Configuration variables
  protected int shapeType = -1;  // Make sure a shape is created
  protected boolean trueSize = false; // Whether size is in pixels

  // Implementation variables
  private double[] coordinates = new double[2];
  private double[] pixel = new double[2]; // The output of position projections
  private double[] pixelSize = new double[2]; // The output of size projections
  private RectangularShape shape;
  private Line2D line1, line2;
  private AffineTransform trueSizeTransform=new AffineTransform(); // additional transformation for trueSize

  {
    setSize (new double[]{0.1,0.1});
    setShapeType(ELLIPSE);
  }
  
  // -------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Set the type of the shape to draw
   */
  public void setShapeType (int _type) {
    if (shapeType==_type) return;
    shapeType = _type;
    recreateShape();
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
    trueSize = _s;
    setNeedToProject(true);
  }

  /**
   * Get whether the size is taken in pixels
   * @return boolean
   */
  public boolean isPixelSize () { return trueSize; }
  
  @Override
  protected void styleChanged(int styleThatChanged) {
    if (styleThatChanged==Style.CHANGED_RELATIVE_POSITION) {
      recreateShape();
      setNeedToProject(true);
    }
  }

  // -------------------------------------
  // Overwritten from its parent
  // -------------------------------------
  
  @Override
  protected int getCorners(double[] _corners) {
    if (trueSize) {
      _corners[0] = _corners[1] = 0;
      return 1;
    }
    switch (getStyle().getRelativePosition()) {
      default :
      case Style.CENTERED   : _corners[0] = -0.5; _corners[1] = -0.5; break;
      case Style.NORTH      : _corners[0] = -0.5; _corners[1] = -1.0; break;
      case Style.SOUTH      : _corners[0] = -0.5; _corners[1] =  0.0; break;
      case Style.EAST       : _corners[0] = -1.0; _corners[1] = -0.5; break;
      case Style.WEST       : _corners[0] =  0.0; _corners[1] = -0.5; break;
      case Style.NORTH_EAST : _corners[0] = -1.0; _corners[1] = -1.0; break;
      case Style.NORTH_WEST : _corners[0] =  0.0; _corners[1] = -1.0; break;
      case Style.SOUTH_EAST : _corners[0] = -1.0; _corners[1] =  0.0; break;
      case Style.SOUTH_WEST : _corners[0] =  0.0; _corners[1] =  0.0; break;
    }
    _corners[4] = _corners[2] = _corners[0] + 1.0;
    _corners[6] = _corners[0];
    _corners[3] = _corners[1];
    _corners[5] = _corners[7] = _corners[1] + 1.0;
    return 4;
  }
  
  // -------------------------------------
  // Drawing
  // -------------------------------------

  @Override
  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible()) return;
//    System.out.println ("drawing for x,y to "+this.getX()+", "+this.getY());
//    System.out.println ("size = "+this.getSizeX()+", "+this.getSizeY());

    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(getStyle().getLineStroke());
    Color color = getStyle().getLineColor();
    if (shape==null) {
      if (hasChanged() || needsToProject()) projectPoints();
      g2.setColor(color);
      g2.drawOval( (int) pixel[0], (int) pixel[1], 1, 1); // draw a point
      return;
    }
    Paint fill = getStyle().getFillColor();
    AffineTransform tr;
    if (trueSize) {
      if (hasChanged() || needsToProject()) projectPoints();
      tr = new AffineTransform (trueSizeTransform);
      tr.preConcatenate(getPixelTransform(_panel));
    }
    else tr = getPixelTransform(_panel);
    Shape trShape = tr.createTransformedShape(shape);
    if (fill!=null && getStyle().isDrawingFill()) { // First fill the inside
      g2.setPaint(fill);
      g2.fill(trShape);
    }
    if (color!=null && getStyle().isDrawingLines()) {
      g2.setColor(color);
      if (shapeType==WHEEL) {
        g2.draw(tr.createTransformedShape(line1));
        g2.draw(tr.createTransformedShape(line2));
      }
      g2.draw(trShape);
    }
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  
  @Override
  public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix) {
    if (!targetPosition.isEnabled()) return null;
    if (!isReallyVisible()) return null;
    if (hasChanged() || needsToProject()) projectPoints();
    int sensitivity = getStyle().getSensitivity();
    if (sensitivity<=0) {
      if (shape==null) {
        if (Math.abs(pixel[0]-_xpix)<1 && Math.abs(pixel[1]-_ypix)<1) return this.targetPosition;
        return null;
      }
      AffineTransform tr;
      if (trueSize) {
        tr = new AffineTransform (trueSizeTransform);
        tr.preConcatenate(getPixelTransform(_panel));
      }
      else tr = getPixelTransform(_panel);
      Shape trShape = tr.createTransformedShape(shape);
      if (trShape.contains(_xpix,_ypix)) return this.targetPosition;
    }
    else {
      if (Math.abs(pixel[0]-_xpix)<sensitivity && Math.abs(pixel[1]-_ypix)<sensitivity) return this.targetPosition;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  @SuppressWarnings("fallthrough")
  private void recreateShape () {
    if (shapeType==NONE) {
      shape = null;
      return;
    }
    double x1,y1;
    switch (getStyle().getRelativePosition()) {
      default :
      case Style.CENTERED   : x1 = -0.5; y1 = -0.5; break;
      case Style.NORTH      : x1 = -0.5; y1 = -1.0; break;
      case Style.SOUTH      : x1 = -0.5; y1 =  0.0; break;
      case Style.EAST       : x1 = -1.0; y1 = -0.5; break;
      case Style.WEST       : x1 =  0.0; y1 = -0.5; break;
      case Style.NORTH_EAST : x1 = -1.0; y1 = -1.0; break;
      case Style.NORTH_WEST : x1 =  0.0; y1 = -1.0; break;
      case Style.SOUTH_EAST : x1 = -1.0; y1 =  0.0; break;
      case Style.SOUTH_WEST : x1 =  0.0; y1 =  0.0; break;
    }
    double x2 = x1+1, y2 = y1+1;
    switch (shapeType) {
      case WHEEL           :  line1 = new Line2D.Double(x1+0.5,y1,x1+0.5,y2);
                              line2 = new Line2D.Double(x1,y1+0.5,x2,y1+0.5);
                              // do not break;
      default :
      case ELLIPSE         : shape = new Ellipse2D.Double(x1,y1,1.0,1.0); break;
      case RECTANGLE       : shape = new Rectangle2D.Double(x1,y1,1.0,1.0); break;
      case ROUND_RECTANGLE : shape = new RoundRectangle2D.Double(x1,y1,1.0,1.0,0.3,0.3); break;
    }
    setElementChanged();
  }

  private void projectPoints() {
    coordinates[0] = 0.0; 
    coordinates[1] = 0.0;
    getTotalTransform().transform(coordinates,0,coordinates,0,1);
    getPanel().projectPosition(coordinates, pixel);
    if (trueSize) {
      getPanel().projectSize(coordinates, getSize(), pixelSize);
      trueSizeTransform = AffineTransform.getScaleInstance(pixelSize[0]==0?0:getSizeX()/pixelSize[0], pixelSize[1]==0?0:getSizeY()/pixelSize[1]);
    }
    setNeedToProject(false);
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
