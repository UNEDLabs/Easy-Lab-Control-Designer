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
 * <p>Title: ElementEllipsoid</p>
 * <p>Description: A 3D ellipsoid</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementEllipsoid extends Element {

  // Configuration variables
  private boolean closedBottom = true, closedTop = true;
  private boolean closedLeft = true, closedRight = true;
  private int minAngleU = 0, maxAngleU = 360;
  private int minAngleV = -90, maxAngleV = 90;
  
  {
    getStyle().setResolution(new Resolution(3, 12, 12));
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementEllipsoid(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementEllipsoid(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Whether the ellipsoid should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close){
    if (closedBottom==close) return;
	  closedBottom = close;
	  addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the ellipsoid is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom() { return closedBottom; }

  /**
   * Whether the (truncated) ellipsoid should be closed at its top.
   * @param close the desired value
   */
  public void setClosedTop(boolean close){
    if (closedTop==close) return;
	  closedTop = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (truncated) ellipsoid is closed at its top.
   * @return the value
   */
  public boolean isClosedTop() { return closedTop; }

  /**
   * Whether the (non-360 degrees) ellipsoid should be closed at its left side.
   * @param close the desired value
   */
  public void setClosedLeft(boolean close) {
    if (closedLeft==close) return;
    closedLeft = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (non-360 degrees) ellipsoid is closed at its left side.
   * @return the value
   */
  public boolean isClosedLeft() { return closedLeft; }

  /**
   * Whether the (non-360 degrees) ellipsoid should be closed at its right side.
   * @param close the desired value
   */
  public void setClosedRight(boolean close) {
    if (closedRight==close) return;
    closedRight = close;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Whether the (non-360 degrees) ellipsoid is closed at its right side.
   * @return the value
   */
  public boolean isClosedRight() { return closedRight; }

  /**
   * Sets the starting azimuthal angle for a non-360 degrees ellipsoid.
   * @param angle the start angle in degrees, between 0 and 360
   */
  public void setMinimumAngleU(int angle) {
    if (this.minAngleU==angle) return;
    this.minAngleU = Math.max(0,Math.min(360,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the starting azimuthal angle for a possible non-360 degrees ellipsoid.
   * @return int the minimum angle
   */
  public int getMinimumAngleU() { return this.minAngleU; }

  /**
   * Sets the end azimuthal angle for a non-360 degrees ellipsoid.
   * @param angle the end angle in degrees, between 0 and 360, 
   * and usually bigger than the minimum angle
   */
  public void setMaximumAngleU(int angle) {
    if (this.maxAngleU==angle) return;
    this.maxAngleU = Math.max(0,Math.min(360,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the end azimuthal angle for a possible non-360 degrees ellipsoid.
   * @return int the maximum angle
   */
  public int getMaximumAngleU() { return this.maxAngleU; }
  
  /**
   * Sets the starting altitude angle for a non-360 degrees ellipsoid.
   * @param angle the start angle in degrees, between -90 and 90
   */
  public void setMinimumAngleV(int angle) {
    if (this.minAngleV==angle) return;
    this.minAngleV = Math.max(-90,Math.min(90,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the starting altitude angle for a possible non-360 degrees ellipsoid.
   * @return int the minimum altitude angle
   */
  public int getMinimumAngleV() { return this.minAngleV; }

  /**
   * Sets the end altitude angle for a non-360 degrees ellipsoid.
   * @param angle the end angle in degrees, between -90 and 90, 
   * and usually bigger than the minimum angle
   */
  public void setMaximumAngleV(int angle) {
    if (this.maxAngleV==angle) return;
    this.maxAngleV = Math.max(-90,Math.min(90,angle));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the end altitude angle for a possible non-360 degrees ellipsoid.
   * @return int the maximum altitude angle
   */
  public int getMaximumAngleV() { return this.maxAngleV; }
  
  
  /**
   * Returns true if the ellipsoid is a complete primitive shape.
   * @return boolean whether the ellipsoid is complete
   */
  public boolean checkStandarEllipsoid() { 
	if(minAngleU==0 && maxAngleU==360 && minAngleV==-90 && maxAngleV==90) return true;
	return false;
  }
  
  // -------------------------------------
  // Super's methods overriden
  // -------------------------------------

  // ------------------------------------
  // Utility methods
  // -------------------------------------

  /**
   * Returns the data for a standard cylinder (from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) )
   * with the given parameters
   */
  static public double[][][] createStandardEllipsoid(int nr, int nu, int nv, 
      double angleu1, double angleu2, double anglev1, double anglev2, 
      boolean top, boolean bottom, boolean left, boolean right) {
	  int totalN = nu*nv;
	  if(Math.abs(anglev2-anglev1)<180) {
		  if(bottom)  totalN += nr*nu;
		  if(top) totalN += nr*nu;
	  }
	  if(Math.abs(angleu2-angleu1)<360) {
		  if(left) totalN += nr*nv;
		  if(right) totalN += nr*nv;
	  }
	  double[][][] data = new double[totalN][4][3];
	  // Compute sines and cosines
	  double[] cosu = new double[nu+1], sinu = new double[nu+1];
	  double[] cosv = new double[nv+1], sinv = new double[nv+1];
	  for(int u = 0;u<=nu;u++) {
		  double angle = ((nu-u)*angleu1+u*angleu2)*TO_RADIANS/nu;
		  cosu[u] = Math.cos(angle);
		  sinu[u] = Math.sin(angle);
	  }
	  for(int v = 0;v<=nv;v++) {
		  double angle = ((nv-v)*anglev1+v*anglev2)*TO_RADIANS/nv;
		  cosv[v] = Math.cos(angle)/2; // /2 because the size is the diameter
		  sinv[v] = Math.sin(angle)/2;
	  }
	  // Now compute the tiles
	  int tile = 0;
	  double[] vectorx = Element.X_UNIT_VECTOR;
	  double[] vectory = Element.Y_UNIT_VECTOR;
	  double[] vectorz = Element.Z_UNIT_VECTOR;
	  double[] center = new double[] {0, 0, 0};
	  {                                     // Tiles along the z axis
		  for(int v = 0;v<nv;v++) {
			  for(int u = 0;u<nu;u++, tile++) { // This ordering is important for the computations below (see ref)
				  for(int k = 0;k<3;k++) {
					  data[tile][0][k] = (cosu[u]*vectorx[k]+sinu[u]*vectory[k])*cosv[v]+sinv[v]*vectorz[k];
					  data[tile][1][k] = (cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*cosv[v]+sinv[v]*vectorz[k];
					  data[tile][2][k] = (cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*cosv[v+1]+sinv[v+1]*vectorz[k];
					  data[tile][3][k] = (cosu[u]*vectorx[k]+sinu[u]*vectory[k])*cosv[v+1]+sinv[v+1]*vectorz[k];
				  }
			  }
		  }	
	  }
	  // Note : the computations below are valid only for the given vectorx, vectory and vectorz
	  if(Math.abs(anglev2-anglev1)<180) { // No need to close top or bottom is the sphere is 'round' enough
		  if(bottom) {                                                      // Tiles at bottom
			  center[2] = sinv[0];
			  // int ref=0; // not used
			  for(int u = 0;u<nu;u++) {
				  for(int i = 0;i<nr;i++, tile++) {
					  for(int k = 0;k<3;k++) {
						  data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr; // should be ref+u
						  data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
						  data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
						  data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr; // should be ref+u
					  }
				  }
			  }
		  }
		  if(top) { // Tiles at top
			  center[2] = sinv[nv];
			  int ref = nu*(nv-1);
			  for(int u = 0;u<nu;u++) {
				  for(int i = 0;i<nr;i++, tile++) {
					  for(int k = 0;k<3;k++) {
						  data[tile][0][k] = ((nr-i)*center[k]+i*data[ref+u][3][k])/nr;
						  data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][3][k])/nr;
						  data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][2][k])/nr;
						  data[tile][3][k] = ((nr-i)*center[k]+i*data[ref+u][2][k])/nr;
					  }
				  }
			  }
		  }
	  }
	  if(Math.abs(angleu2-angleu1)<360) { // No need to close left or right if the sphere is 'round' enough
		  // System.out.println ("Computing lateral tiles");
		  double[] nextCenter = new double[] {0, 0, 0};
		  if(right) { // Tiles at right
			  int ref = 0;
			  for(int j = 0;j<nv;j++, ref += nu) {
				  center[2] = sinv[j];
				  nextCenter[2] = sinv[j+1];
				  for(int i = 0;i<nr;i++, tile++) {
					  for(int k = 0;k<3;k++) {
						  data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][0][k])/nr;
						  data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][0][k])/nr;
						  data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][3][k])/nr;
						  data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][3][k])/nr;
					  }
				  }
			  }
		  }
		  if(left) { // Tiles at left
			  int ref = nu-1;
			  for(int j = 0;j<nv;j++, ref += nu) {
				  center[2] = sinv[j];
				  nextCenter[2] = sinv[j+1];
				  for(int i = 0;i<nr;i++, tile++) {
					  for(int k = 0;k<3;k++) {
						  data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr;
						  data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr;
						  data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][2][k])/nr;
						  data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][2][k])/nr;
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
