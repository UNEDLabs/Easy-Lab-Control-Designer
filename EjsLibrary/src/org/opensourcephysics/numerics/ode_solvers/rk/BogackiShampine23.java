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
 * Title:        BogackiShampine23
 * Description:  2/3rd order RK solver by BogackiShampine
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class BogackiShampine23 extends SolverEngineDiscreteTimeAdaptive {
  static private final double B3_1 = 2.0/9.0,  B3_2 = 1.0/3.0, B3_3 = 4.0/9.0;
  static private final double B2_1 = 7.0/24.0, B2_2 = 1.0/4.0, B2_3 = 1.0/3.0, B2_4 = 1.0/8.0;

  private double[] mRate2, mRate3, mOrder2;

  @Override
  protected int getNumberOfEvaluations() { return 3; }

  @Override
  protected void allocateOtherArrays() {
    super.allocateOtherArrays();
    mRate2 = new double[mDimension];
    mRate3 = new double[mDimension];
    mOrder2 = new double[mDimension];
  }

  @Override
  protected void computeIntermediateStep(double step, double[] state) {
    double halfStep = step/2;

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + halfStep*mInitialRate[i];
    mODE.getRate(state, mRate2);

    double threeQuarterStep = 0.75*step;
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + threeQuarterStep*mRate2[i]; // 3/4
    mODE.getRate(state, mRate3);

    for (int i=0; i<mTimeIndex; i++) state[i] = mInitialState[i] + step*(B3_1*mInitialRate[i]+B3_2*mRate2[i]+B3_3*mRate3[i]);
    state[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];

  }

  @Override
  protected DISCONTINUITY_CODE computeIntermediateStep(InterpolatorEventSolver eventSolver, double step, double[] state) {
    double halfStep = step/2;

    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + halfStep*mInitialRate[i];
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate2);

    double threeQuarterStep = 0.75*step;
    for (int i=0; i<mDimension; i++) state[i] = mInitialState[i] + threeQuarterStep*mRate2[i]; // 3/4
    switch (eventSolver.checkDiscontinuity(state, false)) {
      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
    }
    mODE.getRate(state, mRate3);

    for (int i=0; i<mTimeIndex; i++) state[i] = mInitialState[i] + step*(B3_1*mInitialRate[i]+B3_2*mRate2[i]+B3_3*mRate3[i]);
    state[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];

    return eventSolver.checkDiscontinuity(state, true);
  }

  @Override
  protected IntervalData computeFinalRateAndCreateIntervalData() {
    // The final rate has already been computed (it was needed to estimate the error) 
    return new HermiteIntervalData(mInitialState, mInitialRate,mFinalState, mFinalRate);
  }

  @Override
  protected double getMethodOrder() { return 3; }

  @Override
  protected double computeApproximation(double step) {
    mODE.getRate(mFinalState, mFinalRate);

    for (int i=0; i<mTimeIndex; i++) mOrder2[i] = mInitialState[i] + step*(B2_1*mInitialRate[i]+B2_2*mRate2[i]+B2_3*mRate3[i]+B2_4*mFinalRate[i]);
    mOrder2[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];

    return super.computeError(mOrder2);
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
