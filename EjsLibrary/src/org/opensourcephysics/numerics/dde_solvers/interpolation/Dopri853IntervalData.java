/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

/**
 * Uses interpolation based on extra evaluations appropriated to DoPri853 algorithm
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class Dopri853IntervalData extends ExtraStepsIntervalData {

  public Dopri853IntervalData(double[] aState, double[] bState, double[][] coeffs) {
    super(aState, bState, coeffs);
  }
	
  public double interpolate(double time, int index) {
    double theta = (time-mLeft)/mDeltaTime;
    double theta1 = 1 - theta;
    return mCoeffs[0][index] + theta * (mCoeffs[1][index] + theta1 * (mCoeffs[2][index] + theta * (mCoeffs[3][index] + 
        theta1 * (mCoeffs[4][index] + theta * (mCoeffs[5][index] + theta1 * (mCoeffs[6][index] + theta * mCoeffs[7][index]))))));
    }

  public double[] interpolate(double time, double[] state, int beginIndex, int length) {
    double theta = (time-mLeft)/mDeltaTime;
    double theta1 = 1 - theta;
    for (int index=beginIndex, i=0; i < length; index++, i++) {
      state[i] = mCoeffs[0][index] + theta * (mCoeffs[1][index] + theta1 * (mCoeffs[2][index] + theta * (mCoeffs[3][index] + 
          theta1 * (mCoeffs[4][index] + theta * (mCoeffs[5][index] + theta1 * (mCoeffs[6][index] + theta * mCoeffs[7][index]))))));
    }
    return state;
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
