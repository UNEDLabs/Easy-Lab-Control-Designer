/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;

/**
 * <p>Title: ElementArrow </p>
 * <p>Description: An arrow.</p>
 * @author Francisco Esquembre
 * @version June 2008
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
  
  {
    setSize (new double[]{0.1,0.1});
    setArrowType(ARROW);
    getStyle().setRelativePosition(Style.NORTH_EAST);
  }
  
  //-------------------------------------
  //New configuration methods
  //-------------------------------------

  /**
   * Set the type of decoration at the head of the arrow. Either ARROW, SEGMENT (none) or BOX
   */
  public void setArrowType (int _type) {
    arrowType = _type;
    setNeedToProject(true); // So that to compute the head
  }

  /**
   * Returns the arrow type
   * @return
   */
  public int getArrowType() { return this.arrowType; }
  
  // -------------------------------------
  // Super methods overwritten
  // -------------------------------------

  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible()) return;
    if (hasChanged() || needsToProject()) projectPoints();
    drawHead((java.awt.Graphics2D)_g);
  }

  // -------------------------------------
  // The head
  // -------------------------------------
  
  static final private double ARROW_CST = 0.35;
  static final private double ARROW_MAX = 25.0;
  private int headPoints = 0;
  private int headA[] = new int[7], headB[] = new int[7]; // Used to display the head
  private int secondHeadA[] = new int[4], secondHeadB[] = new int[4]; // Used to display the head in RHOMBUS mode

  @Override
  protected void projectPoints() {
    super.projectPoints();
    // Now compute the head
    if (arrowType==SEGMENT) { 
      headPoints = 2; 
      headA[0] = (int) pixelOrigin[0];
      headB[0] = (int) pixelOrigin[1];
      headA[1] = (int) pixelEnd[0];
      headB[1] = (int) pixelEnd[1];
      return; 
    }
    double a = pixelEnd[0]-pixelOrigin[0];
    double b = pixelEnd[1]-pixelOrigin[1];
    double h = Math.sqrt(a*a+b*b);
    if (h==0.0) { 
      headPoints = 2; 
      headA[0] = (int) pixelOrigin[0];
      headB[0] = (int) pixelOrigin[1];
      headA[1] = (int) pixelEnd[0];
      headB[1] = (int) pixelEnd[1];
      return; 
    }
    a = ARROW_CST*a/h;
    b = ARROW_CST*b/h;
    if (arrowType==RHOMBUS) {
        a *= 0.9;
        b *= 0.9;
    }
    else if (arrowType==TRIANGLE) {
      a *= 2;
      b *= 2;
    }
    else if (h>ARROW_MAX) {
      a *= ARROW_MAX/h;
      b *= ARROW_MAX/h;
    }
    double p0 = pixelEnd[0]-a*h;
    double q0 = pixelEnd[1]-b*h;
    a *= h/2.0;
    b *= h/2.0;
    switch (arrowType) {
      default :
      case ARROW :
        headPoints = 6;
        headA[0] = (int) p0;
        headB[0] = (int) q0;
        headA[1] = (int) (p0-b);
        headB[1] = (int) (q0+a);
        headA[2] = (int) pixelEnd[0];
        headB[2] = (int) pixelEnd[1];
        headA[3] = (int) (p0+b);
        headB[3] = (int) (q0-a);
        headA[4] = (int) p0;
        headB[4] = (int) q0;
        headA[5] = (int) pixelOrigin[0];
        headB[5] = (int) pixelOrigin[1];
        break;
      case BOX :
        headPoints = 7;
        headA[0] = (int) p0;
        headB[0] = (int) q0;
        headA[1] = (int) (p0-b);
        headB[1] = (int) (q0+a);
        headA[2] = (int) (pixelEnd[0]-b); 
        headB[2] = (int) (pixelEnd[1]+a);
        headA[3] = (int) (pixelEnd[0]+b); 
        headB[3] = (int) (pixelEnd[1]-a);
        headA[4] = (int) (p0+b);
        headB[4] = (int) (q0-a);
        headA[5] = (int) p0;
        headB[5] = (int) q0;
        headA[6] = (int) pixelOrigin[0];
        headB[6] = (int) pixelOrigin[1];
        break;
      case TRIANGLE :
        headPoints = 4;
        headA[0] = (int) (pixelOrigin[0]-b);
        headB[0] = (int) (pixelOrigin[1]+a);
        headA[1] = (int) (pixelOrigin[0]+b);
        headB[1] = (int) (pixelOrigin[1]-a);
        headA[2] = (int) pixelEnd[0]; 
        headB[2] = (int) pixelEnd[1];
        headA[3] = headA[0]; 
        headB[3] = headB[0];
        break;
      case RHOMBUS :
        double centerA = (pixelOrigin[0]+pixelEnd[0])/2;
        double centerB = (pixelOrigin[1]+pixelEnd[1])/2;
        headPoints = 4;
        headA[0] = (int) pixelEnd[0]; 
        headB[0] = (int) pixelEnd[1];
        headA[1] = (int) (centerA-b);
        headB[1] = (int) (centerB+a);
        headA[2] = (int) (centerA+b);
        headB[2] = (int) (centerB-a);
        headA[3] = headA[0]; 
        headB[3] = headB[0];
        secondHeadA[0] = (int) pixelOrigin[0]; 
        secondHeadB[0] = (int) pixelOrigin[1];
        secondHeadA[1] = headA[1]; 
        secondHeadB[1] = headB[1];
        secondHeadA[2] = headA[2]; 
        secondHeadB[2] = headB[2];
        secondHeadA[3] = secondHeadA[0]; 
        secondHeadB[3] = secondHeadB[0];
        break;
    }
  }

  private void drawHead(Graphics2D _g2) {
    Paint fill = getStyle().getFillColor();
    _g2.setStroke(getStyle().getLineStroke());
    if (arrowType==RHOMBUS) {
      if (fill!=null) {
        _g2.setPaint(fill);
        _g2.fillPolygon(headA, headB, 4);
      }
      Color extra = getStyle().getExtraColor();
      if (extra!=null) {
        _g2.setPaint(extra);
        _g2.fillPolygon(secondHeadA, secondHeadB, 4);
      }
      return;
    }
    if (headPoints>2 && fill!=null && getStyle().isDrawingFill()) {
      _g2.setPaint(fill);
      _g2.fillPolygon(headA, headB, headPoints-1);
    }
    _g2.setColor(getStyle().getLineColor());
    _g2.drawPolyline(headA, headB, headPoints);
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
