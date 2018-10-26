/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;
import java.awt.geom.Line2D;
import org.opensourcephysics.drawing2d.interaction.InteractionTarget;

/**
 * <p>Title: ElementSegment</p>
 * <p>Description: A Segment</p>
 * @author Francisco Esquembre
 * @version July 2008
 */
public class ElementSegment extends Element {
  static protected final Line2D.Double NORMAL_LINE = new Line2D.Double(0,0,1,1);
  static protected final Line2D.Double CENTERED_LINE = new Line2D.Double(-0.5,-0.5,0.5,0.5);
  static final protected double[] STD_CENTERED_ORIGIN = new double[]{-0.5,-0.5};
  static final protected double[] STD_CENTERED_END    = new double[]{0.5,0.5};
  static final protected double[] STD_CENTERED_TRIANGLE_ORIGIN = new double[]{-0.4,-0.4};
  static final protected double[] STD_CENTERED_TRIANGLE_END    = new double[]{0.6,0.6};

  /* Implementation variables */
  protected double[] origin = new double[2]; // The origin
  protected double[] end    = new double[2]; // The end point
  protected double[] pixelOrigin = new double[2]; // The projected origin
  protected double[] pixelEnd    = new double[2]; // The projected end point
  protected double[] center    = new double[2]; // The end point
  protected double[] pixelCenter = new double[2]; // The projected origin
  protected Line2D.Double line = NORMAL_LINE;
  
  protected int arrowType = ElementArrow.ARROW;
  
  {
    setSize (new double[]{0.1,0.1});
    getStyle().setRelativePosition(Style.NORTH_EAST);
  }

  @Override
  protected void styleChanged(int styleThatChanged) {
    if (styleThatChanged==Style.CHANGED_RELATIVE_POSITION) {
      switch (getStyle().getRelativePosition()) {
        default :  line = NORMAL_LINE; break;
        case Style.CENTERED : line = CENTERED_LINE; break;
      }
      setNeedToProject(true);
    }
  }


  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------

  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (!isReallyVisible()) return;
    Graphics2D g2 = (Graphics2D) _g;
    g2.setStroke(getStyle().getLineStroke());
    g2.setColor(getStyle().getLineColor());
    g2.draw(getPixelTransform(_panel).createTransformedShape(line));
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  
  @Override
  protected double[] getHotSpotBodyCoordinates(InteractionTarget target) {
    switch (getStyle().getRelativePosition()) {
      default : 
        if (target==targetPosition) return new double[]{0,0};
        if (target==targetSize) return new double[]{getSizeX()==0 ? 0:1, getSizeY()==0 ? 0:1};
        break;
      case Style.CENTERED : 
        if (target==targetPosition) return new double[]{0,0};
        if (target==targetSize) return new double[]{getSizeX()==0 ? 0:0.5, getSizeY()==0 ? 0:0.5};
        break;
    }
    return null;
  }

  @Override
  public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix) {
    if (! (targetPosition.isEnabled() || targetSize.isEnabled()) ) return null;
    if (!isReallyVisible()) return null;
    if (needsToProject()) projectPoints();
    int sensitivity = getStyle().getSensitivity();
    switch (getStyle().getRelativePosition()) {
      default : 
        if (targetPosition.isEnabled()&&Math.abs(pixelOrigin[0]-_xpix)<sensitivity&&Math.abs(pixelOrigin[1]-_ypix)<sensitivity) return this.targetPosition;
        if (targetSize.isEnabled()&&Math.abs(pixelEnd[0]-_xpix)<sensitivity&&Math.abs(pixelEnd[1]-_ypix)<sensitivity) return this.targetSize;
        break;
      case Style.CENTERED :
        if (targetPosition.isEnabled()&&Math.abs(pixelCenter[0]-_xpix)<sensitivity&&Math.abs(pixelCenter[1]-_ypix)<sensitivity) return this.targetPosition;
        if (targetSize.isEnabled()&&Math.abs(pixelEnd[0]-_xpix)<sensitivity&&Math.abs(pixelEnd[1]-_ypix)<sensitivity) return this.targetSize;
        break;
      case Style.SOUTH_WEST : 
        if (targetPosition.isEnabled()&&Math.abs(pixelEnd[0]-_xpix)<sensitivity&&Math.abs(pixelEnd[1]-_ypix)<sensitivity) return this.targetPosition;
        if (targetSize.isEnabled()&&Math.abs(pixelOrigin[0]-_xpix)<sensitivity&&Math.abs(pixelOrigin[1]-_ypix)<sensitivity) return this.targetSize;
        break;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  protected void projectPoints() {
    switch (getStyle().getRelativePosition()) {
      default : 
        getTotalTransform().transform(STD_ORIGIN, 0, origin, 0, 1);
        getTotalTransform().transform(STD_END,    0, end, 0, 1);
        break;
      case Style.CENTERED : 
        if (arrowType==ElementArrow.TRIANGLE) {
          getTotalTransform().transform(STD_CENTERED_TRIANGLE_ORIGIN, 0, origin, 0, 1);
          getTotalTransform().transform(STD_CENTERED_TRIANGLE_END,    0, end, 0, 1);
        }
        else {
          getTotalTransform().transform(STD_CENTERED_ORIGIN, 0, origin, 0, 1);
          getTotalTransform().transform(STD_CENTERED_END,    0, end, 0, 1);
        }
        getTotalTransform().transform(STD_ORIGIN,    0, center, 0, 1);
        getPanel().projectPosition(center, pixelCenter);
        break;
      case Style.SOUTH_WEST : 
        getTotalTransform().transform(STD_ORIGIN, 0, end, 0, 1);
        getTotalTransform().transform(STD_END,    0, origin, 0, 1);
        break;
    }
    DrawingPanel panel = getPanel();
    if (panel!=null) {
      panel.projectPosition(origin, pixelOrigin);
      panel.projectPosition(end, pixelEnd);
      setNeedToProject(false);
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
