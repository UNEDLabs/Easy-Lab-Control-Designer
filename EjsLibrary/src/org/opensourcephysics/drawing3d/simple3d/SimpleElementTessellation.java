/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.Color;
import java.awt.Graphics2D;

import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementTessellation;

/**
 * <p>Title: SimpleElementTessellation</p>
 * <p>Description: Painter's algorithm implementation of a Tessellation</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class SimpleElementTessellation extends SimpleAbstractTile {

  public SimpleElementTessellation(ElementTessellation _element) { super(_element); }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  
  @Override
  protected double[][][] computeTile() {
    return ((ElementTessellation) element).getTiles();
  }

  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & Element.CHANGE_COLOR)!=0) {
      ((ElementTessellation) element).checkScales();
    }
    super.processChanges(_change, _cummulativeChange);
  }
  
  // -------------------------------------
  // Super methods overwritten
  // -------------------------------------

  public void draw(Graphics2D g2, int index) {
    if (((ElementTessellation) element).getTiles()==null) return;
    double[][] values = ((ElementTessellation) element).getValues(); 
    if (values!=null) {
      DrawingPanel3D panel = element.getPanel();
      Color color = style.getLineColor(); 
      if (style.isDrawingLines() && color!=null) color = panel.projectColor(color, objects[index].getDistance());
      else color = null;
      g2.setStroke(style.getLineStroke());

      ((ElementTessellation) element).getDrawer().drawColorCoded(g2,a[index],b[index],values[index], color==null);
      if (color!=null) {
        g2.setColor(color);
        g2.drawPolygon(a[index],b[index],a[index].length);
      }
    }
    else super.draw(g2, index);
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
