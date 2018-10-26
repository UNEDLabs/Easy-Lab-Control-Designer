/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.awt.Graphics2D;
import org.opensourcephysics.drawing2d.utils.VectorAlgebra;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementSpring;
import org.opensourcephysics.drawing3d.utils.Resolution;

/**
 * <p>Title: SimpleElementSpring</p>
 * <p>Description: A Spring using the painter's algorithm</p> 
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleElementSpring extends SimpleElementSegment {
  private int loops = -1, pointsPerLoop = -1; // Make sure arrays are allocated

  public SimpleElementSpring(ElementSpring _element) { 
    super(_element); 
  }

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    if ((_cummulativeChange & FORCE_RECOMPUTE)!=0 || (_cummulativeChange & Element.CHANGE_SHAPE)!=0) {
      computePoints();
      projectPoints();
    }
    else if ((_cummulativeChange & Element.CHANGE_PROJECTION)!=0) projectPoints();
  }

  // -------------------------------------
  // Implementation of SimpleElement
  // -------------------------------------

  public void draw(Graphics2D _g2, int _index) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(element.getPanel().projectColor(style.getLineColor(), objects[_index].getDistance()));
    _g2.drawLine(aCoord[_index], bCoord[_index], aCoord[_index+1], bCoord[_index+1]);
  }

  public void drawQuickly(Graphics2D _g2) {
    _g2.setStroke(style.getLineStroke());
    _g2.setColor(style.getLineColor());
    _g2.drawPolyline(aCoord, bCoord, aCoord.length);
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void computePoints() {
    int theLoops = loops, thePPL = pointsPerLoop;
    Resolution res = style.getResolution();
    if (res==null) {
      theLoops = 8;
      thePPL = 15;
    }
    else {
      switch(res.getType()) {
         case Resolution.DIVISIONS :
           theLoops = Math.max(res.getN1(), 0);
           thePPL = Math.max(res.getN2(), 1);
           break;
         case Resolution.MAX_LENGTH :
           theLoops = Math.max((int) Math.round(0.49+element.getDiagonalSize()/res.getMaxLength()), 1);
           thePPL = 15;
           break;
      }
    }
    if (theLoops!=loops || thePPL!=pointsPerLoop) { // reallocate arrays
      loops = theLoops;
      pointsPerLoop = thePPL;
      int segments = loops*pointsPerLoop;
      div = segments; //CJB
      points = new double[segments+1][3];
      aCoord = new int[segments+1];
      bCoord = new int[segments+1];
      objects = new Object3D[segments];
      for (int i = 0; i<segments; i++) objects[i] = new Object3D(this, i);
    }
    ElementSpring spring = (ElementSpring) element;
    int segments = objects.length;
    div = segments; //CJB
    double delta=2.0*Math.PI/pointsPerLoop;
    double radius = spring.getRadius(), solenoid = spring.getSolenoid(); 
    if (radius<0) delta*=-1;
    int pre = pointsPerLoop/2;
    double[] size = element.getSize();
    double[] v = {1,0,0}; 
    v[0] = size[0];v[1] = size[1]; v[2] = size[2];
    if(size[0]==0.0) size[0]=1.0;
    if(size[1]==0.0) size[1]=1.0;
    if(size[2]==0.0) size[2]=1.0;
    
    double[] u1 = VectorAlgebra.normalTo(v);
    double[] u2 = VectorAlgebra.normalize(VectorAlgebra.crossProduct(v, u1));
    
    for (int i=0; i<=segments; i++) {
      int k;
      if (spring.isThinExtremes()) {
        if (i < pre) k = 0;
        else if (i < pointsPerLoop) k = i - pre;
        else if (i > (segments - pre)) k = 0;
        else if (i > (segments - pointsPerLoop)) k = segments - i - pre;
        else k = pre;
      }
      else k = pre;
      double angle = i*delta;
      double cos = Math.cos(angle), sin = Math.sin(angle);
      if (solenoid!=0.0)  {
        double cte = k*Math.cos(i*2*Math.PI/pointsPerLoop)/pre;
        for (int c=0; c<3; c++) //CJB
          points[i][c] = (solenoid*cte*v[c] + i*v[c]/segments+k*radius*(cos*u1[c]+sin*u2[c])/pre);
      }
      else{
    	  for (int c=0; c<3; c++) //CJB
    		  points[i][c] = (i*v[c]/segments+k*radius*(cos*u1[c]+sin*u2[c])/pre);
      }
      element.toSpaceFrame(points[i]); // apply the transformation(s)
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
