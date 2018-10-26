/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

import org.opensourcephysics.numerics.*;

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
	protected DDE mDDE;
	protected StateMemory mStateMemory;
//	private boolean mBootStrap1Ready = false;
	
	private double[] state_bt1 = null, rate_bt1, bt1_c2, bt1_c3, bt1_c4;
	
	public BootstrapIntervalData(double[] aState, double[] aRate, double[] bState, double[] bRate, ODE ode, StateMemory stateMemory) {
		super(aState,aRate,bState,bRate);
		mODE = ode;
		if (ode instanceof DDE) mDDE = (DDE) ode;
		else mDDE = null;
		mStateMemory = stateMemory;
		prepareFirstBootstrap(); // this must be done here or there can be conflicts when asking for interpolation
	}

	public double interpolate(double time, int index) {
	  double step = (time - mLeft)/mDeltaTime;
//	  if (!mBootStrap1Ready) prepareFirstBootstrap();
	  return mLeftState[index] + step*(mDeltaTime*mLeftRate[index] + step*(bt1_c2[index] + step*(bt1_c3[index] + step*bt1_c4[index])));
	}

	public double[] interpolate(double time, double[] state, int beginIndex, int length) {
	  bootstrap1((time - mLeft)/mDeltaTime, state, beginIndex, length);
	  return state;
	}

	protected void bootstrap1(double step, double[] state, int beginIndex, int length) {
//	  if (!mBootStrap1Ready) prepareFirstBootstrap();
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
	  state_bt1[mTimeIndex] = mLeft + ALPHA*mDeltaTime;
	  if (mDDE!=null) {
	    double[] delays = mDDE.getDelays(state_bt1);
	    IntervalData[] intervals = new IntervalData[delays.length];
	    for (int i=0; i<delays.length; i++) intervals[i] = mStateMemory.findInterval(state_bt1[mTimeIndex]-delays[i], false);
	    mDDE.getRate(state_bt1, intervals, rate_bt1);
	  }
	  else mODE.getRate(state_bt1, rate_bt1);

	  for (int i=0; i<dimension; i++) {
	    double dif = mRightState[i]-mLeftState[i], f0 = mDeltaTime*mLeftRate[i], f1 = mDeltaTime*mRightRate[i];
	    double c4 = (mDeltaTime*rate_bt1[i] + bt1_cf1*f1 + bt1_cf0*f0 + bt1_cys*dif)/bt1_den;
	    double c3 = f1 + f0 - 2*dif - 2*c4;
	    bt1_c4[i] = c4;
	    bt1_c3[i] = c3;
	    bt1_c2[i] = dif - f0 - c3 - c4;
	    //double rbt = deltaTime*rate_bt1[i];
	    //        bt1_c2[i] = bt1_21*dif + bt1_22*f0 + bt1_23*f1 + bt1_24*rbt;
	    //        bt1_c3[i] = bt1_31*dif + bt1_32*f0 + bt1_33*f1 + bt1_34*rbt;
	    //        bt1_c4[i] = bt1_41*dif + bt1_42*f0 + bt1_43*f1 + bt1_44*rbt;
	  }
//	  mBootStrap1Ready = true;
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
