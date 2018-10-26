/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementPlane</p>
 * <p>Description: A 3D Plane</p>
 * A plane is determined by two director vectors, called the first and second directions. 
 * The sizeX and sizeY are taken then as multipliers along these directions. The sizeZ is ignored. 
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementPlane extends Element {

  // Configuration variables
  private double vectorU[] = {1.0, 0.0, 0.0};
  private double vectorV[] = {0.0, 1.0, 0.0};
  private double sizeU = 1.0;
  private double sizeV = 1.0;
  
  // Implementation variables
  private double vectorUSize = 1.0, vectorVSize = 1.0;

//  {
//    getStyle().setResolution(new Resolution(3, 3, 3)); backwards compatibility is to leave this to null
//  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementPlane(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementPlane(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets the first direction vector of the plane
   * @param vector double[] an array with the x,y,z coordinates of the vector
   */
  public void setFirstDirection(double[] vector) {
    if (vector==null) return;
    if (java.util.Arrays.equals(vector,vectorU)) return;
    System.arraycopy(vector,0,vectorU,0,3);
    vectorUSize = Math.sqrt(vectorU[0]*vectorU[0]+vectorU[1]*vectorU[1]+vectorU[2]*vectorU[2]);
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Sets the size in the first direction vector.
   * (If the direction vector is not unitary, the actual size will vary)
   * @param size
   */
  public void setSizeFirstDirection(double size) {
    if (size==this.sizeU) return;
    this.sizeU = size;
    addChange(Element.CHANGE_SHAPE);
  }
  
  /**
   * Returns the size in the first direction vector.
   * (If the direction vector is not unitary, the actual size will vary)
   * @return
   */
  public double getSizeFirstDirection() {
    return this.sizeU;
  }
  
  /**
   * Sets the size in the second direction vector.
   * (If the direction vector is not unitary, the actual size will vary)
   * @param size
   */
  public void setSizeSecondDirection(double size) {
    if (size==this.sizeV) return;
    this.sizeV = size;
    addChange(Element.CHANGE_SHAPE);
  }
  
  /**
   * Returns the size in the second direction vector.
   * (If the direction vector is not unitary, the actual size will vary)
   * @return
   */
  public double getSizeSecondDirection() {
    return this.sizeV;
  }
  
  /**
   * Returns a copy of the first direction vector of the plane
   * @return an array with the x,y,z coordinates of the vector
   */
  public double[] getFirstDirection() { return vectorU.clone(); }

  /**
   * Sets the second direction vector of the plane
   * @param vector double[] an array with the x,y,z coordinates of the vector
   */
  public void setSecondDirection(double[] vector) {
    if (vector==null) return;
    if (java.util.Arrays.equals(vector,vectorV)) return;
    System.arraycopy(vector,0,vectorV,0,3);
    vectorVSize = Math.sqrt(vectorV[0]*vectorV[0]+vectorV[1]*vectorV[1]+vectorV[2]*vectorV[2]);
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns a copy of the second direction vector of the plane
   * @return an array with the x,y,z coordinates of the vector
   */
  public double[] getSecondDirection() { return vectorV.clone(); }

  /**
   * Returns the size measured along the first direction
   */
  public double getFirstSize() { return getSizeX()*sizeU*vectorUSize; }

  /**
   * Returns the size measured along the second direction
   */
  public double getSecondSize() { return getSizeY()*sizeV*vectorVSize; }

  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------
  
  // ------------------------------------
  // Utility methods
  // -------------------------------------

  /**
   * Creates the coordinates for the vertex of a given plane.
   * The sizeX and sizeY are considered the sizes along the first
   * and second direction 
   */
  static public double[][][] createPlane(ElementPlane plane, int nu, int nv) {
    double[][][] data = new double[nu*nv][4][3];
    int tile = 0;
    double su = plane.sizeU/2,  sv = plane.sizeV/2;
    double du = plane.sizeU/nu, dv = plane.sizeV/nv;
    
    for (int i = 0; i<nu; i++) {
      double u = i*du-su;
      for (int j = 0; j<nv; j++) {
        double v = j*dv-sv;
        for (int k = 0; k<3; k++) data[tile][0][k] = u*plane.vectorU[k]+v*plane.vectorV[k];
        for (int k = 0; k<3; k++) data[tile][1][k] = (u+du)*plane.vectorU[k]+v*plane.vectorV[k];
        for (int k = 0; k<3; k++) data[tile][2][k] = (u+du)*plane.vectorU[k]+(v+dv)*plane.vectorV[k];
        for (int k = 0; k<3; k++) data[tile][3][k] = u*plane.vectorU[k]+(v+dv)*plane.vectorV[k];
        tile++;
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
