/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

/**
 * Uses Hermite interpolation to interpolate data for an interval
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class HermiteIntervalData extends IntervalData {
	protected int mTimeIndex;
	protected double mDeltaTime;
	protected double[] mLeftState, mLeftRate, mRightState, mRightRate;
	
	public HermiteIntervalData(double[] aState, double[] aRate, double[] bState, double[] bRate) {
	  int dimension = aState.length;
	  // Copy the states and rates
		mLeftState  = new double[dimension]; System.arraycopy(aState,0,mLeftState, 0,dimension);
		mRightState = new double[dimension]; System.arraycopy(bState,0,mRightState,0,dimension);
		mLeftRate   = new double[dimension]; System.arraycopy(aRate, 0,mLeftRate,  0,dimension);
		mRightRate  = new double[dimension]; System.arraycopy(bRate, 0,mRightRate, 0,dimension);
		// Prepare further computations
		mTimeIndex = dimension-1;
		mLeft  = aState[mTimeIndex]; 
		mRight = bState[mTimeIndex]; 
		mDeltaTime = mRight-mLeft;		
	}
	
	public double interpolate(double time, int index) {
	  double theta = (time - mLeft)/mDeltaTime;
	  double minus1 = theta - 1;
	  double prod1 = theta*minus1;
	  double prod2 = prod1*(1 - 2*theta);
	  double coefX0 = - minus1 - prod2;
	  double coefX1 = theta + prod2;
	  double coefF0 = prod1*minus1*mDeltaTime;
	  double coefF1 = prod1*theta*mDeltaTime;
	  return coefX0*mLeftState[index] + coefX1*mRightState[index] + coefF0*mLeftRate[index] + coefF1*mRightRate[index];
	}
	
	public double[] interpolate(double time, double[] state, int beginIndex, int length) {
		hermite((time - mLeft)/mDeltaTime, state, beginIndex, length);
		return state;
	}

	/**
	 * Does the Hermite interpolation for a given theta = (t - left)/Delta
	 * for the given indexes
	 * Will also be used by subclasses
	 * @param theta
	 * @param state
	 */
	final protected void hermite(double theta, double[] state, int beginIndex, int length) {
		double minus1 = theta - 1;
		double prod1 = theta*minus1;
		double prod2 = prod1*(1 - 2*theta);
		double coefX0 = - minus1 - prod2;
		double coefX1 = theta + prod2;
		double coefF0 = prod1*minus1*mDeltaTime;
		double coefF1 = prod1*theta*mDeltaTime;
		
		for (int index=beginIndex, i=0; i<length; index++, i++) {
			state[i] = coefX0*mLeftState[index] + coefX1*mRightState[index] + coefF0*mLeftRate[index] + coefF1*mRightRate[index];
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
