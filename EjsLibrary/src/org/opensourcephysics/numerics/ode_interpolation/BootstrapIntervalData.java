/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

import org.opensourcephysics.numerics.ODE;

/**
 * Uses one step of BootStrapping from Hermite interpolation to get one extra precission order
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class BootstrapIntervalData extends HermiteIntervalData {
  // Coefficients of the first bootstrap
  static private final double ALPHA = 0.25;
  static private final double bt1_den = ALPHA*(ALPHA-1)*(4*ALPHA-2);
  static private final double bt1_cf1 = ALPHA*(-3*ALPHA+2);
  static private final double bt1_cf0 = ALPHA*(-3*ALPHA+4)-1; 
  static private final double bt1_cys = 6*ALPHA*(ALPHA-1);
  
	protected ODE mODE;
	
	private double[] state_bt1 = null, rate_bt1, bt1_c2, bt1_c3, bt1_c4;
	
	public BootstrapIntervalData(double[] aState, double[] aRate, double[] bState, double[] bRate, ODE ode) {
		super(aState,aRate,bState,bRate);
		mODE = ode;
		prepareFirstBootstrap(); // this must be done here or there can be conflicts when asking for interpolation
	}

  @Override
	public double interpolate(double time, int index) {
	  double step = (time - getLeft())/mDeltaTime;
	  return mLeftState[index] + step*(mDeltaTime*mLeftRate[index] + step*(bt1_c2[index] + step*(bt1_c3[index] + step*bt1_c4[index])));
	}

  @Override
	public double[] interpolate(double time, double[] state, int beginIndex, int length) {
	  bootstrap1((time - getLeft())/mDeltaTime, state, beginIndex, length);
	  return state;
	}

	protected void bootstrap1(double step, double[] state, int beginIndex, int length) {
	  for (int index=beginIndex, i=0; i<length; index++, i++) {
	    state[i] = mLeftState[index] + step*(mDeltaTime*mLeftRate[index] + step*(bt1_c2[index] + step*(bt1_c3[index] + step*bt1_c4[index])));
	  }
	}
	
	/**
	 * Prepares the first bootstrap
	 */
	private void prepareFirstBootstrap() {
	  int dimension = mTimeIndex+1;
	  if (state_bt1==null) {
	    state_bt1 = new double[dimension];
	    rate_bt1 = new double[dimension];
	    bt1_c2 = new double[dimension];
	    bt1_c3 = new double[dimension];
	    bt1_c4 = new double[dimension];
	  }
	  super.hermite(ALPHA, state_bt1, 0, mTimeIndex);
	  state_bt1[mTimeIndex] = getLeft() + ALPHA*mDeltaTime;
	  mODE.getRate(state_bt1, rate_bt1);

	  for (int i=0; i<dimension; i++) {
	    double dif = mRightState[i]-mLeftState[i], f0 = mDeltaTime*mLeftRate[i], f1 = mDeltaTime*mRightRate[i];
	    double c4 = (mDeltaTime*rate_bt1[i] + bt1_cf1*f1 + bt1_cf0*f0 + bt1_cys*dif)/bt1_den;
	    double c3 = f1 + f0 - 2*dif - 2*c4;
	    bt1_c4[i] = c4;
	    bt1_c3[i] = c3;
	    bt1_c2[i] = dif - f0 - c3 - c4;
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
