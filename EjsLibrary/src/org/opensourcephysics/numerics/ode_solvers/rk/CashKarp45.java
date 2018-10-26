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
 * Title:        CashKarp45
 * Description:  Cash-Karp 4th and 5th solver with local extrapolation
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class CashKarp45 extends SolverEngineDiscreteTimeAdaptive {

  static private final double A_11 =   1.0/5.0,
      A_21 =   3.0/40.0, A_22 =  9.0/40.0,
      A_31 =   3.0/10.0, A_32 = -9.0/10.0, A_33 =  6.0/5.0,
      A_41 = -11.0/54.0, A_42 =  5.0/2.0,  A_43 = -70.0/27.0, A_44 = 35.0/27.0,
      A_51 = 1631.0/55296.0, A_52 = 175.0/512.0, A_53 = 575.0/13824.0, A_54 = 44275.0/110592.0, A_55 = 253.0/4096.0;
  // B4 are the 4th order coefficients
  static private final double B4_1 = 2825./27648., B4_2 = 0., B4_3 = 18575./48384., B4_4 = 13525./55296., B4_5 = 277./14336., B4_6 = 1./4.;
  // B5 are the 5th order coefficients
  static private final double B5_1 = 37./378.,     B5_2 = 0., B5_3 = 250./621.,     B5_4 = 125./594.,     B5_5 = 0.,          B5_6 = 512./1771.;
  //  static private final double E_1 = 277./64512.,   E_2 = 0.,  E_3 = -6925./370944., E_4 = 6925./202752.,  E_5 = 277./14336.,  E_6 = -277./7084.;

  private double[] mRate2, mRate3, mRate4, mRate5, mRate6, mOrder4;

  @Override
  protected int getNumberOfEvaluations() { return 6; }

  @Override
  protected void allocateOtherArrays() {
    super.allocateOtherArrays();
    mRate2 = new double[mDimension];
    mRate3 = new double[mDimension];
    mRate4 = new double[mDimension];
    mRate5 = new double[mDimension];
    mRate6 = new double[mDimension];
    mOrder4 = new double[mDimension];
  }

  @Override
  protected void computeIntermediateStep(double step, double[] state){
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*A_11*mInitialRate[i];
    mODE.getRate(state, mRate2);
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_21*mInitialRate[i]+A_22*mRate2[i]);
    mODE.getRate(state, mRate3);
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_31*mInitialRate[i]+A_32*mRate2[i]+A_33*mRate3[i]);
    mODE.getRate(state, mRate4);
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_41*mInitialRate[i]+A_42*mRate2[i]+A_43*mRate3[i]+A_44*mRate4[i]);
    mODE.getRate(state, mRate5);
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_51*mInitialRate[i]+A_52*mRate2[i]+A_53*mRate3[i]+A_54*mRate4[i]+A_55*mRate5[i]);
    mODE.getRate(state, mRate6);

    for (int i=0; i<mTimeIndex; i++) state[i] = mInitialState[i] + step*(B5_1*mInitialRate[i]+B5_2*mRate2[i]+B5_3*mRate3[i]+B5_4*mRate4[i]+B5_5*mRate5[i]+B5_6*mRate6[i]);
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

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_31*mInitialRate[i]+A_32*mRate2[i]+A_33*mRate3[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate4);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_41*mInitialRate[i]+A_42*mRate2[i]+A_43*mRate3[i]+A_44*mRate4[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate5);

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + step*(A_51*mInitialRate[i]+A_52*mRate2[i]+A_53*mRate3[i]+A_54*mRate4[i]+A_55*mRate5[i]);
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate6);

    for (int i=0; i<mTimeIndex; i++) state[i] = mInitialState[i] + step*(B5_1*mInitialRate[i]+B5_2*mRate2[i]+B5_3*mRate3[i]+B5_4*mRate4[i]+B5_5*mRate5[i]+B5_6*mRate6[i]);
    state[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];

    return eventSolver.checkDiscontinuity(state, true);
  }

  @Override
  protected IntervalData computeFinalRateAndCreateIntervalData() {
    mODE.getRate(mFinalState, mFinalRate);
    return new Bootstrap2IntervalData(mInitialState, mInitialRate,mFinalState, mFinalRate, mODE);
  }

  @Override
  protected double getMethodOrder() { return 5; }

  @Override
  protected double computeApproximation(double step) {
    for (int i=0; i<mTimeIndex; i++) mOrder4[i] = mInitialState[i] + step*(B4_1*mInitialRate[i]+B4_2*mRate2[i]+B4_3*mRate3[i]+B4_4*mRate4[i]+B4_5*mRate5[i]+B4_6*mRate6[i]);
    mOrder4[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];
    return super.computeError(mOrder4);
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
