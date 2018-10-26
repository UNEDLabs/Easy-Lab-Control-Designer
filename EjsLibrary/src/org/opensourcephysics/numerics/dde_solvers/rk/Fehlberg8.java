/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.rk;

import org.opensourcephysics.numerics.dde_solvers.AbstractDiscreteTimeSolverInterpolator;
import org.opensourcephysics.numerics.dde_solvers.interpolation.*;

/**
 * Title:        Fehlberg8
 * Description:  8rd order fixed step RK solver by Fehlberg with Hermite interpolation
 * @author Francisco Esquembre
 * @author Maria Jose Cano Vicente
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class Fehlberg8 extends AbstractDiscreteTimeSolverInterpolator {
  
  private double[] mRate2, mRate3, mRate4, mRate5, mRate6, mRate7, mRate8, mRate9, mRate10, mRate11, mRate12, mRate13;

  public Fehlberg8(org.opensourcephysics.numerics.ODE _ode) {
		super(_ode);
	}
	
	@Override
	protected int getNumberOfEvaluations() { return 13; }
	
	@Override
	protected void allocateOtherArrays() {
    mRate2 = new double[mDimension];
    mRate3 = new double[mDimension];
    mRate4 = new double[mDimension];
    mRate5 = new double[mDimension];
    mRate6 = new double[mDimension];
    mRate7 = new double[mDimension];
    mRate8 = new double[mDimension];
    mRate9 = new double[mDimension];
    mRate10 = new double[mDimension];
    mRate11 = new double[mDimension];
    mRate12 = new double[mDimension];
    mRate13 = new double[mDimension];
	}
	
	 @Override
	  protected double[] computeIntermediateStep(double step, double[] state) {
	    double timeRate = mInitialRate[mTimeIndex];

	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*Fehlberg78.A_11*mInitialRate[i];
	    mWrapper.evaluateRate(state, mRate2);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_21*mInitialRate[i]+Fehlberg78.A_22*mRate2[i]);
	    mWrapper.evaluateRate(state, mRate3);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_31*mInitialRate[i]+Fehlberg78.A_33*mRate3[i]);
	    mWrapper.evaluateRate(state, mRate4);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_41*mInitialRate[i]+Fehlberg78.A_43*mRate3[i]+Fehlberg78.A_44*mRate4[i]);
	    mWrapper.evaluateRate(state, mRate5);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_51*mInitialRate[i]+Fehlberg78.A_54*mRate4[i]+Fehlberg78.A_55*mRate5[i]);
	    mWrapper.evaluateRate(state, mRate6);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_61*mInitialRate[i]+Fehlberg78.A_64*mRate4[i]+Fehlberg78.A_65*mRate5[i]+Fehlberg78.A_66*mRate6[i]);
	    mWrapper.evaluateRate(state, mRate7);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_71*mInitialRate[i]+Fehlberg78.A_75*mRate5[i]+Fehlberg78.A_76*mRate6[i]+Fehlberg78.A_77*mRate7[i]);
	    mWrapper.evaluateRate(state, mRate8);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_81*mInitialRate[i]+Fehlberg78.A_84*mRate4[i]+
	        Fehlberg78.A_85*mRate5[i]+Fehlberg78.A_86*mRate6[i]+Fehlberg78.A_87*mRate7[i]+Fehlberg78.A_88*mRate8[i]);
	    mWrapper.evaluateRate(state, mRate9);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_91*mInitialRate[i]+Fehlberg78.A_94*mRate4[i]+
	        Fehlberg78.A_95*mRate5[i]+Fehlberg78.A_96*mRate6[i]+Fehlberg78.A_97*mRate7[i]+Fehlberg78.A_98*mRate8[i]+Fehlberg78.A_99*mRate9[i]);
	    mWrapper.evaluateRate(state, mRate10);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_101*mInitialRate[i]+Fehlberg78.A_104*mRate4[i]+
	        Fehlberg78.A_105*mRate5[i]+Fehlberg78.A_106*mRate6[i]+Fehlberg78.A_107*mRate7[i]+Fehlberg78.A_108*mRate8[i]+Fehlberg78.A_109*mRate9[i]+Fehlberg78.A_1010*mRate10[i]);
	    mWrapper.evaluateRate(state, mRate11);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_111*mInitialRate[i]+
	        Fehlberg78.A_116*mRate6[i]+Fehlberg78.A_117*mRate7[i]+Fehlberg78.A_118*mRate8[i]+Fehlberg78.A_119*mRate9[i]+Fehlberg78.A_1110*mRate10[i]);
	    mWrapper.evaluateRate(state, mRate12);
	    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(Fehlberg78.A_121*mInitialRate[i]+Fehlberg78.A_124*mRate4[i]+
	        Fehlberg78.A_125*mRate5[i]+Fehlberg78.A_126*mRate6[i]+Fehlberg78.A_127*mRate7[i]+Fehlberg78.A_128*mRate8[i]+Fehlberg78.A_129*mRate9[i]+Fehlberg78.A_1210*mRate10[i]+Fehlberg78.A_1212*mRate12[i]);
	    mWrapper.evaluateRate(state, mRate13);

	    for (int i=0; i<mTimeIndex; i++)  state[i] = mInitialState[i] + step*(Fehlberg78.B7_1*mInitialRate[i]+Fehlberg78.B7_6*mRate6[i]+Fehlberg78.B7_7*mRate7[i]+Fehlberg78.B7_8*mRate8[i]+Fehlberg78.B7_9*mRate9[i]+Fehlberg78.B7_10*mRate10[i]+Fehlberg78.B7_11*mRate11[i]);
	    state[mTimeIndex] = mInitialTime + step*timeRate;

	    return state;
	  }

	  @Override
	  protected IntervalData computeFinalRateAndCreateIntervalData() {
	    mWrapper.evaluateRate(mFinalState, mFinalRate);
	    return new Bootstrap2IntervalData(mInitialState, mInitialRate,mFinalState, mFinalRate, mODE, mStateMemory);
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
