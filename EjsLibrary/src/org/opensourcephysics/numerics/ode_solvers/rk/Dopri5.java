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
 * Title:        Dopri5
 * Description:  Dorman Prince 5 ODE solver with 4th order C1 dense output
 * @author Francisco Esquembre (based on code by Andrei Goussev)
 * @author Maria Jose Cano
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class Dopri5 extends SolverEngineDiscreteTimeAdaptive {

  static private final double 
  A_11 =     1.0/5.0,
  A_21 =     3.0/40.0,   A_22 =      9.0/40.0,
  A_31 =    44.0/45.0,   A_32 =    -56.0/15.0,   A_33 =    32.0/9.0,
  A_41 = 19372.0/6561.0, A_42 = -25360.0/2187.0, A_43 = 64448.0/6561.0, A_44 = -212.0/729.0,
  A_51 =  9017.0/3168.0, A_52 =   -355.0/33.0,   A_53 = 46732.0/5247.0, A_54 =   49.0/176.0, A_55 = -5103.0/18656.0;

  // B5 are the 5th order coefficients
  static private final double B5_1 = 35.0/384.0, B5_2 = 0.0, B5_3 = 500.0/1113.0, B5_4 =125.0/192.0, B5_5 = -2187.0/6784.0, B5_6 = 11.0/84.0;
  static private final double D_1 = -12715105075.0/11282082432.0,  D_2 = 0.0, D_3 = 87487479700.0/32700410799.0, D_4 = -10690763975.0/1880347072.0,
      D_5 = 701980252875.0/199316789632.0, D_6 = -1453857185.0/822651844.0, D_7 = 69997945.0/29380423.0;
  // Error coefficients
  static private final double E_1 = 71.0/57600.0, E_2 = 0.0, E_3 = -71.0/16695.0, E_4 = 71.0/1920.0, E_5 = -17253.0/339200.0, E_6 = 22.0/525.0, E_7 = -1.0/40.0;

  private double[][] mCoeffs;
  private double[] mRate2, mRate3, mRate4, mRate5, mRate6;

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
    mCoeffs = new double [5][mDimension];
  }

  @Override
  protected void computeIntermediateStep(double step, double[] state) {
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
    // The final rate has already been computed (it is needed to estimate the error) 
    // calculation of interpolation coefficients 
    double dt = mFinalTime-mInitialTime;
    for (int i=0; i<mDimension; i++) {
      double initStateI = mInitialState[i]; // for efficiency
      double initRateI  = mInitialRate[i];
      double finalRateI = mFinalRate[i];
      mCoeffs[0][i] = initStateI; 
      double coeff1i = mFinalState[i] - initStateI;
      double coeff2i = dt*initRateI - coeff1i;
      mCoeffs[3][i] = coeff1i - dt*finalRateI - coeff2i;
      mCoeffs[4][i] = dt*(D_1*initRateI+D_2*mRate2[i]+D_3*mRate3[i]+D_4*mRate4[i]+D_5*mRate5[i]+D_6*mRate6[i]+D_7*finalRateI);
      mCoeffs[1][i] = coeff1i;
      mCoeffs[2][i] = coeff2i;
    }
    return new Dopri5IntervalData(mInitialState, mFinalState, mCoeffs);
  }


  @Override
  protected double getMethodOrder() { return 5; }

  @Override
  protected double computeApproximation(double step) {
    mODE.getRate(mFinalState, mFinalRate);
    double error = 0;
    for (int i=0; i<mDimension; i++) {
      double sk = mAbsTol[i] + mRelTol[i] * Math.max(Math.abs(mFinalState[i]), Math.abs(mInitialState[i]));
      double errorI = (E_1*mInitialRate[i]+E_2*mRate2[i]+E_3*mRate3[i]+E_4*mRate4[i]+E_5*mRate5[i]+E_6*mRate6[i]+E_7*mFinalRate[i])/sk;
      error += errorI*errorI;
    }
    return Math.sqrt(error/mDimension);
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
