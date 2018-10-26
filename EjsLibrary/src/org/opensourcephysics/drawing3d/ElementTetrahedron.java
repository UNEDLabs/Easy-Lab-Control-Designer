/*
* Open Source Physics software is free software as described near the bottom of this code file.
*
* For additional information and documentation on Open Source Physics please see:
* <http://www.opensourcephysics.org/>
*/

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Resolution;


/**
 * <p>Title: ElementTetrahedron</p>
 * <p>Description: A thetrahedron</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementTetrahedron extends Element {
	
  //Static varibles for tetrahedron
  static private final double SQRT3 = Math.sqrt(3.0);
  static private final double HEIGHT = Math.sqrt(6.0)/3.0f;
  static private final double XCENTER = SQRT3/6.0f;
  static private final double ZCENTER = HEIGHT/3.0f;

  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private double truncationHeight = Double.NaN;

  {
    getStyle().setResolution(new Resolution(3, 12, 5));
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementTetrahedron(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementTetrahedron(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------
  
  /**
   * Whether the cone should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close){
    if (closedBottom==close) return;
    closedBottom = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the cone is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom() { return closedBottom; }

  /**
   * Whether the (truncated) cone should be closed at its top.
   * @param close the desired value
   */
  public void setClosedTop(boolean close){
    if (closedTop==close) return;
    closedTop = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (truncated) cone is closed at its top.
   * @return the value
   */
  public boolean isClosedTop() { return closedTop; }

  /**
   * Sets the height at which to truncate the cone
   * @param height The desired height of the truncated cone. 
   * Double.NaN or negative for a non truncated cone.
   */
  public void setTruncationHeight(double height) {
    if (truncationHeight==height) return;
    if (height<0) height = Double.NaN;
    truncationHeight = height;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the height at which to truncate the cone
   * @return The height of the truncated cone. Double.NaN for a non truncated cone.
   */
  public double getTruncationHeight() { return truncationHeight; }

  // ------------------------------------
  // Utility methods
  // -------------------------------------

  /**
   * Returns the data for a standard tetrahedron (from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) )
   * with the given parameters
   */
  static public double[][][] createStandardTetrahedron(boolean top, boolean bottom, double height) {
    int totalN ;  //number of tiles
    int pointsN = 4; //number of points
    if (Double.isNaN(height)) totalN = bottom ? 4 : 3;
    else {
      pointsN += 2;
      if (top) totalN = bottom ? 5 : 4;
      else totalN = bottom ? 4 : 3;
    }
    double[][][] data = new double[totalN][4][3];
    double[][] points = new double[pointsN][];
    //Base points
    points[0] = new double[] {XCENTER, 0.5f, -ZCENTER}; //p1
    points[1] = new double[] {XCENTER, -0.5f,-ZCENTER}; //p2
    points[2] = new double[] {-XCENTER*2.0f, 0.0f,-ZCENTER}; //p2
    if (Double.isNaN(height)) {
      points[3] = new double[] { 0.0f, 0.0f, HEIGHT-ZCENTER}; //p4
      if (bottom) {
        int[] serie = {0, 1, 3, 3, 0, 2, 3, 3, 1, 2, 3, 3, 0, 2, 1, 1};
          /*p1, p2, p4, p4   // front face
            p1, p3, p4, p4     // left, back face
            p2, p3, p4, p4     // right, back face
            p1, p3, p2, p2     // bottom face*/
        for (int i = 0; i<totalN; i++) {
          for (int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      }
      else { // no bottom
        int[] serie = {0, 1, 3, 3, 0, 2, 3, 3, 1, 2, 3, 3};
          /*p1, p2, p4, p4  // front face
            p1, p3, p4, p4    // left, back face
            p2, p3, p4, p4    // right, back face*/
        for (int i = 0; i<totalN; i++) {
          for (int j = 0; j<3; j++) {
            data[i][0][j] = points[serie[i*4]][j];
            data[i][1][j] = points[serie[i*4+1]][j];
            data[i][2][j] = points[serie[i*4+2]][j];
            data[i][3][j] = points[serie[i*4+3]][j];
          }
        }
      }
    }
    else { // height is not a NaN
      points[3] = new double[] {XCENTER*(1-height),0.5f-0.5f*height,HEIGHT*height-ZCENTER}; //p4
      points[4] = new double[] {XCENTER*(1-height),-0.5f+0.5f*height,HEIGHT*height-ZCENTER}; //p5
      points[5] = new double[] {-XCENTER*2.0f*(1-height),0.0f,HEIGHT*height-ZCENTER}; //p6
      if (top) {
        if (bottom) { // top and bottom
          int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2, 0, 1, 2, 2, 3, 5, 4, 4};
          /*p1, p4, p5, p2,    // front face
            p3, p6, p4, p1,      // left face
            p2, p5, p6, p3,      // right face
            p1, p2, p3, p3,      // bottom face
          p4, p6, p5, p5,     // top face*/
          for(int i = 0; i<totalN; i++) {
            for(int j = 0; j<3; j++) {
              data[i][0][j] = points[serie[i*4]][j];
              data[i][1][j] = points[serie[i*4+1]][j];
              data[i][2][j] = points[serie[i*4+2]][j];
              data[i][3][j] = points[serie[i*4+3]][j];
            }
          }
        }
        else { // top and not bottom
          int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2, 3, 5, 4, 4};
          /*p1, p4, p5, p2,    // front face
            p3, p6, p4, p1,      // left face
            p2, p5, p6, p3,      // right face
          p4, p6, p5, p5,     // top face*/
          for(int i = 0; i<totalN; i++) {
            for(int j = 0; j<3; j++) {
              data[i][0][j] = points[serie[i*4]][j];
              data[i][1][j] = points[serie[i*4+1]][j];
              data[i][2][j] = points[serie[i*4+2]][j];
              data[i][3][j] = points[serie[i*4+3]][j];
            }
          }
        }
      }
      else {
        if (bottom) { // not top and bottom
          int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2, 0, 1, 2, 2};
          /*p1, p4, p5, p2,    // front face
              p3, p6, p4, p1,      // left face
              p2, p5, p6, p3,      // right face
              p1, p2, p3, p3,      // bottom face*/
          for(int i = 0; i<totalN; i++) {
            for(int j = 0; j<3; j++) {
              data[i][0][j] = points[serie[i*4]][j];
              data[i][1][j] = points[serie[i*4+1]][j];
              data[i][2][j] = points[serie[i*4+2]][j];
              data[i][3][j] = points[serie[i*4+3]][j];
            }
          }
        }
        else { // not top and not bottom
          int[] serie = {0, 3, 4, 1, 2, 5, 3, 0, 1, 4, 5, 2};
          /*p1, p4, p5, p2,    // front face
              p3, p6, p4, p1,      // left face
              p2, p5, p6, p3,      // right face*/
          for(int i = 0; i<totalN; i++) {
            for(int j = 0; j<3; j++) {
              data[i][0][j] = points[serie[i*4]][j];
              data[i][1][j] = points[serie[i*4+1]][j];
              data[i][2][j] = points[serie[i*4+2]][j];
              data[i][3][j] = points[serie[i*4+3]][j];
            }
          }
        }
      }
    }
    return data;
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
