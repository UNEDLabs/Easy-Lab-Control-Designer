/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers.rk;

import org.opensourcephysics.numerics.ode_interpolation.*;
import org.opensourcephysics.numerics.ode_solvers.SolverEngineDiscreteTimeAdaptive;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.DISCONTINUITY_CODE;

/**
 * Title:        Fehlberg78
 * Description:  7rd order with 8th order error control RK solver by Fehlberg
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class Fehlberg78 extends SolverEngineDiscreteTimeAdaptive {
  
	static final double
	A_11 =   2.0/27.0,
	A_21 =   1.0/36.0, A_22 =  1.0/12.0,
	A_31 =   1.0/24.0,                   A_33 =  1.0/8.0,
	A_41 =   5.0/12.0,                   A_43 =-25.0/16.0, A_44 =  25.0/16.0,
	A_51 =   1.0/20.0,                                     A_54 =  1.0/4.0,  A_55 =   1.0/5.0,
	A_61 = -25.0/108.0,                                    A_64 =125.0/108.0, A_65 =-65.0/27.0, A_66 = 125.0/54.0,
	A_71 =  31.0/300.0,                                                       A_75 =  61.0/225.0, A_76 =  -2.0/9.0,   A_77 = 13.0/900.0,
	A_81 =   2.0,                                          A_84 =-53.0/6.0,   A_85 = 704.0/45.0,  A_86 =-107.0/9.0,   A_87 = 67.0/90.0 , 
    A_88 = 3.0,
    A_91 = -91.0/108.0,                                    A_94 = 23.0/108.0, A_95 = -976.0/135.0,A_96 = 311.0/54.0,  A_97 = -19.0/60.0,
    A_98 = 17.0/6.0,  A_99 = -1.0/12.0,
    A_101= 2383.0/4100.0,                                  A_104=-341.0/164.0,A_105= 4496.0/1025.0,A_106=-301.0/82.0, A_107 =2133.0/4100.0,
    A_108=45.0/82.0, A_109=45.0/164.0, A_1010 = 18.0/41.0,
    A_111= 3./205.0,                                                                               A_116=-6.0/41.0,   A_117 = -3.0/205.0,
    A_118=-3.0/41.0, A_119=3.0/41.0,   A_1110 = 6.0/41.0,
    A_121 = -1777.0/4100.0,                                A_124=-341.0/164.0,A_125=4496.0/1025.0, A_126=-289.0/82.0, A_127= 2193.0/4100.0,
    A_128=51.0/82.0, A_129=33.0/164.0, A_1210=12.0/41.0,                    A_1212=1.0;
  
	// 7th order method
	static final double
	B7_1 = 41.0/840.0, B7_6 = 34.0/105.0, B7_7 = 9.0/35.0, B7_8 = 9.0/35.0, B7_9 = 9.0/280.0, B7_10 = 9.0/280.0, B7_11 = 41.0/840.0;
	
	// 8th order error control
	static final double 
    B8_6 = 34.0/105.0, B8_7 = 9.0/35.0, B8_8 = 9.0/35.0, B8_9 = 9.0/280.0, B8_10 = 9.0/280.0, B8_12 = 41.0/840.0, B8_13 = 41.0/840.0;
  
	private double[] mRate2, mRate3, mRate4, mRate5, mRate6, mRate7, mRate8, mRate9, mRate10, mRate11, mRate12, mRate13, mOrder8;

	@Override
	protected int getNumberOfEvaluations() { return 13; }
	
	@Override
	protected void allocateOtherArrays() {
		super.allocateOtherArrays();
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
		mOrder8 = new double[mDimension];
	}
	
	@Override
	protected void computeIntermediateStep(double step, double[] state){
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*A_11*mInitialRate[i];
	  mODE.getRate(state, mRate2);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_21*mInitialRate[i]+A_22*mRate2[i]);
	  mODE.getRate(state, mRate3);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_31*mInitialRate[i]+A_33*mRate3[i]);
	  mODE.getRate(state, mRate4);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_41*mInitialRate[i]+A_43*mRate3[i]+A_44*mRate4[i]);
	  mODE.getRate(state, mRate5);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_51*mInitialRate[i]+A_54*mRate4[i]+A_55*mRate5[i]);
	  mODE.getRate(state, mRate6);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_61*mInitialRate[i]+A_64*mRate4[i]+A_65*mRate5[i]+A_66*mRate6[i]);
	  mODE.getRate(state, mRate7);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_71*mInitialRate[i]+A_75*mRate5[i]+A_76*mRate6[i]+A_77*mRate7[i]);
	  mODE.getRate(state, mRate8);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_81*mInitialRate[i]+A_84*mRate4[i]+
	      A_85*mRate5[i]+A_86*mRate6[i]+A_87*mRate7[i]+A_88*mRate8[i]);
	  mODE.getRate(state, mRate9);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_91*mInitialRate[i]+A_94*mRate4[i]+
	      A_95*mRate5[i]+A_96*mRate6[i]+A_97*mRate7[i]+A_98*mRate8[i]+A_99*mRate9[i]);
	  mODE.getRate(state, mRate10);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_101*mInitialRate[i]+A_104*mRate4[i]+
	      A_105*mRate5[i]+A_106*mRate6[i]+A_107*mRate7[i]+A_108*mRate8[i]+A_109*mRate9[i]+A_1010*mRate10[i]);
	  mODE.getRate(state, mRate11);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_111*mInitialRate[i]+
	      A_116*mRate6[i]+A_117*mRate7[i]+A_118*mRate8[i]+A_119*mRate9[i]+A_1110*mRate10[i]);
	  mODE.getRate(state, mRate12);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_121*mInitialRate[i]+A_124*mRate4[i]+
	      A_125*mRate5[i]+A_126*mRate6[i]+A_127*mRate7[i]+A_128*mRate8[i]+A_129*mRate9[i]+A_1210*mRate10[i]+A_1212*mRate12[i]);
	  mODE.getRate(state, mRate13);
	  
	  for (int i=0; i<mTimeIndex; i++)  state[i] = mInitialState[i] + step*(B7_1*mInitialRate[i]+B7_6*mRate6[i]+B7_7*mRate7[i]+B7_8*mRate8[i]+B7_9*mRate9[i]+B7_10*mRate10[i]+B7_11*mRate11[i]);
    state[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];
	}

  @Override
  protected DISCONTINUITY_CODE computeIntermediateStep(InterpolatorEventSolver eventSolver, double step, double[] state) {
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*A_11*mInitialRate[i];
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate2);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_21*mInitialRate[i]+A_22*mRate2[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate3);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_31*mInitialRate[i]+A_33*mRate3[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate4);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_41*mInitialRate[i]+A_43*mRate3[i]+A_44*mRate4[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate5);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_51*mInitialRate[i]+A_54*mRate4[i]+A_55*mRate5[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate6);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_61*mInitialRate[i]+A_64*mRate4[i]+A_65*mRate5[i]+A_66*mRate6[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate7);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_71*mInitialRate[i]+A_75*mRate5[i]+A_76*mRate6[i]+A_77*mRate7[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate8);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_81*mInitialRate[i]+A_84*mRate4[i]+
        A_85*mRate5[i]+A_86*mRate6[i]+A_87*mRate7[i]+A_88*mRate8[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate9);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_91*mInitialRate[i]+A_94*mRate4[i]+
        A_95*mRate5[i]+A_96*mRate6[i]+A_97*mRate7[i]+A_98*mRate8[i]+A_99*mRate9[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate10);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_101*mInitialRate[i]+A_104*mRate4[i]+
        A_105*mRate5[i]+A_106*mRate6[i]+A_107*mRate7[i]+A_108*mRate8[i]+A_109*mRate9[i]+A_1010*mRate10[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate11);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_111*mInitialRate[i]+
        A_116*mRate6[i]+A_117*mRate7[i]+A_118*mRate8[i]+A_119*mRate9[i]+A_1110*mRate10[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate12);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_121*mInitialRate[i]+A_124*mRate4[i]+
        A_125*mRate5[i]+A_126*mRate6[i]+A_127*mRate7[i]+A_128*mRate8[i]+A_129*mRate9[i]+A_1210*mRate10[i]+A_1212*mRate12[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate13);
    
    for (int i=0; i<mTimeIndex; i++)  state[i] = mInitialState[i] + step*(B7_1*mInitialRate[i]+B7_6*mRate6[i]+B7_7*mRate7[i]+B7_8*mRate8[i]+B7_9*mRate9[i]+B7_10*mRate10[i]+B7_11*mRate11[i]);
    state[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];

    return eventSolver.checkDiscontinuity(state, true);
  }

  @Override
	protected IntervalData computeFinalRateAndCreateIntervalData() {
	  mODE.getRate(mFinalState, mFinalRate);
	  return new Bootstrap2IntervalData(mInitialState, mInitialRate,mFinalState, mFinalRate, mODE);
	}

	@Override
	protected double getMethodOrder() { return 7; }
	
	@Override
	protected double computeApproximation(double step) {
		for (int i=0; i<mTimeIndex; i++) mOrder8[i] = mInitialState[i]+step*(B8_6*mRate6[i]+B8_7*mRate7[i]+B8_8*mRate8[i]+B8_9*mRate9[i]+B8_10*mRate10[i]+B8_12*mRate12[i]+B8_13*mRate13[i]);
		mOrder8[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];
		return super.computeError(mOrder8);
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
