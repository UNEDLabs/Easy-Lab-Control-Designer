/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers.symplectic;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_interpolation.*;
import org.opensourcephysics.numerics.ode_solvers.SolverEngineDiscreteTime;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.DISCONTINUITY_CODE;

/**
 * Title:        EulerRichardson
 * Description:  2nd order Velocity Verlet method with Hermite Interpolation 
 * @author Francisco Esquembre
 * @author Maria Jose Cano 
 * @version 1.0 May 2010
 * @version 1.0 Feb 2011
 */
public class VelocityVerlet extends SolverEngineDiscreteTime {
  
  private boolean accelerationIndependentOfVelocity;
	private double[] mRate2;
	
  public void setODE(InterpolatorEventSolver eventSolver, ODE ode) { 
		super.setODE(eventSolver,ode);
    if (ode instanceof VelocityVerletSavvy) {
      accelerationIndependentOfVelocity = ((VelocityVerletSavvy) ode).isAccelerationIndependentOfVelocity();
    }
    else accelerationIndependentOfVelocity = false;
	}
	
	@Override
	protected int getNumberOfEvaluations() { return 2; }
	
	@Override
	protected void allocateOtherArrays() {
		mRate2 = new double[mDimension];
	}
	
	@Override
	protected void computeIntermediateStep(double step, double[] state) {
    double dt2 = (step*step)/2; // the step size squared
    // increment the positions using the velocity and acceleration
    for(int i = 0; i<mTimeIndex; i+=2) state[i] = mInitialState[i] + step*mInitialRate[i] + dt2*mInitialRate[i+1];
    mODE.getRate(state, mRate2);
    double halfStep = step/2;
    // increment the velocities with the average rate
    for(int i = 1; i<mTimeIndex; i+=2) state[i] = mInitialState[i] + halfStep*(mInitialRate[i]+mRate2[i]);
    // the independent variable
    state[mTimeIndex] = mInitialState[mTimeIndex] + step*mInitialRate[mTimeIndex];
	}

	 @Override
	  protected DISCONTINUITY_CODE computeIntermediateStep(InterpolatorEventSolver eventSolver, double step, double[] state) {
	    double dt2 = (step*step)/2; // the step size squared
	    // increment the positions using the velocity and acceleration
	    for (int i = 0; i<mTimeIndex; i+=2) state[i] = mInitialState[i] + step*mInitialRate[i] + dt2*mInitialRate[i+1];
	    switch (eventSolver.checkDiscontinuity(state, false)) {
	      case DISCONTINUITY_PRODUCED_ERROR  : return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR; 
	      case DISCONTINUITY_JUST_PASSED     : return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED;   
	      case DISCONTINUITY_ALONG_STEP      : return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
	      case DISCONTINUITY_EXACTLY_ON_STEP : break; // doubtful. But check for the real point 
	      case NO_DISCONTINUITY_ALONG_STEP   : break; // go ahead 
	    }
	    mODE.getRate(state, mRate2);

	    double halfStep = step/2;
	    // increment the velocities with the average rate
	    for (int i = 1; i<mTimeIndex; i+=2) state[i] = mInitialState[i] + halfStep*(mInitialRate[i]+mRate2[i]);
	    // the independent variable
	    state[mTimeIndex] = mInitialState[mTimeIndex] + step*mInitialRate[mTimeIndex];

	    return eventSolver.checkDiscontinuity(state, true);
	 }
	 
  @Override
  protected IntervalData computeFinalRateAndCreateIntervalData() {
    if (accelerationIndependentOfVelocity) {
      for (int i=0,j=1; i<mTimeIndex; i+=2,j+=2) {
        mFinalRate[i] = mFinalState[j];
        mFinalRate[j] = mRate2[j];
      }
      mFinalRate[mTimeIndex] = mRate2[mTimeIndex];
    }
    else {
      mODE.getRate(mFinalState, mFinalRate);
      // Accumulate the counter
      mAccumulatedEvaluations += getNumberOfEvaluations();
    }
    return new HermiteIntervalData(mInitialState, mInitialRate,mFinalState, mFinalRate);
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
