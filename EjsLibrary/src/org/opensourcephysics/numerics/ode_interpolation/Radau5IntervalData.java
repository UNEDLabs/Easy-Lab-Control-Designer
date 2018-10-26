/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

import org.opensourcephysics.numerics.ode_solvers.rk.Radau5;

/**
 * Uses a particular interpolation appropriated for Radau5
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class Radau5IntervalData extends ExtraStepsIntervalData {
	private double mFinalTime;

	public Radau5IntervalData(double[] aState, double[] bState, double[][] coeffs) {
	  super(aState,bState,coeffs);
	  mFinalTime = bState[bState.length-1]; // because getRight() may change
	}
	
  @Override
	public double interpolate(double time, int index) {
	  double theta = (time-mFinalTime)/mDeltaTime;
	  return mCoeffs[0][index] + theta * (mCoeffs[1][index] + (theta - Radau5.c2m1) * (mCoeffs[2][index] + (theta - Radau5.c1m1) * mCoeffs[3][index]));
	}

  @Override
	public double[] interpolate(double time, double[] state, int beginIndex, int length) {
	  double theta = (time-mFinalTime)/mDeltaTime;
    for (int index=beginIndex, i=0; i < length; index++, i++) {
	    state[i] = mCoeffs[0][index] + theta * (mCoeffs[1][index] + (theta - Radau5.c2m1) * (mCoeffs[2][index] + (theta - Radau5.c1m1) * mCoeffs[3][index]));
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
