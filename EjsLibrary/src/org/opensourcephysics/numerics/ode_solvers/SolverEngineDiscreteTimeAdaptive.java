/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR;

/**
 * Title:        SolverInterpolatorDiscreteTimeAdaptive
 * Description:  Abstract class for an adaptive explicit discrete time solver with Hermite interpolation.
 * Based on by Andrei Goussev and Yuri B. Senichenkov 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version 1.0 May 2010
 * @version 2.0 March 2011
 */

public abstract class SolverEngineDiscreteTimeAdaptive extends SolverEngineDiscreteTime {
  // step size estimation's parameters
  // FAC1, FAC2, - parameters for step size selection
  // BETA - for step control stabilization
  // SAFE - safety factor
  static private final double FAC1 = 0.33;
  static private final double FAC2 = 6;
  static private final double BETA = 0;
  static private final double SAFE = 0.9;
  
  // step size estimation's variables
  private final double mExpO1 = 1.0 / getMethodOrder() - BETA * 0.75;
  private double mErrOld = 1.e-4;
  private double mFactor = 0;

  // variables
  private boolean mEstimate=false; // Whether to mEstimate the first step size
  private double mActualStepSize;
  private double mAbsoluteTolerance=Double.NaN;
  private double mRelativeTolerance=Double.NaN;
  protected double[] mAbsTol; // array of absolute tolerances
  protected double[] mRelTol; // array of relative tolerances

  // --------------------------------------------
  // abstract methods
  // --------------------------------------------

  /**
   * Computes the proposed approximation for finalState[] and
   * returns the estimated error obtained
   * @return the estimated error
   */
  abstract protected double computeApproximation(double step);

  /**
   * Returns the order+1 of the accepted approximation
   */
  abstract protected double getMethodOrder();

  // --------------------------------------------
  // Methods overriden
  // --------------------------------------------

  @Override
  protected void allocateOtherArrays() {
    mRelTol = new double[mDimension];
    mAbsTol = new double[mDimension];
    setTolerances(1.0e-6,1.0e-6);
  }

  @Override
  public void reinitialize(double[] state) {
    super.reinitialize(state);
    if (mEstimate) mActualStepSize = limitStepSize(estimateFirstStepSize(mStepSize));
    else mActualStepSize = limitStepSize(mStepSize);
  }

  @Override
  final public void setEstimateFirstStep(boolean estimate) {
    mEstimate = estimate;
  }

  @Override
  public void setMaximumStepSize(double stepSize) {
    super.setMaximumStepSize(stepSize);
    mActualStepSize = limitStepSize(mActualStepSize);
  }
  
  @Override
  final public void setTolerances(double absTol, double relTol) {
    if (mAbsoluteTolerance==absTol && mRelativeTolerance==relTol) return;
    mAbsoluteTolerance = absTol;
    mRelativeTolerance =relTol;
    for (int i=0; i<mDimension; i++) {
      mAbsTol[i] = absTol;
      mRelTol[i] = relTol;
    }
    mFinalTime = Double.NaN;
    mErrorCode = ERROR.NO_ERROR;
    if (mEstimate) mActualStepSize = limitStepSize(estimateFirstStepSize(mStepSize));
    else mActualStepSize = limitStepSize(mStepSize);
  }
  
  final public void setTolerances(double[] absTol, double[] relTol) {
    mAbsoluteTolerance = mRelativeTolerance = Double.NaN;
    System.arraycopy(absTol,0,mAbsTol,0,mDimension);
    System.arraycopy(relTol,0,mRelTol,0,mDimension);
    mFinalTime = Double.NaN;
    mErrorCode=ERROR.NO_ERROR;
    if (mEstimate) mActualStepSize = limitStepSize(estimateFirstStepSize(mStepSize));
    else mActualStepSize = limitStepSize(mStepSize);
  }

  @Override
  final protected void computeOneStep(boolean hasDiscontinuities) {
    mErrorCode = ERROR.NO_ERROR;
    for (int iterations=0; iterations<500; iterations++) { // maximum number of attempts
      if (hasDiscontinuities) {
        switch (computeIntermediateStep(mEventSolver, mActualStepSize, mFinalState)) {
          case DISCONTINUITY_PRODUCED_ERROR : 
            mFinalTime = Double.NaN; 
            return;
          case NO_DISCONTINUITY_ALONG_STEP  :  // no discontinuity in this interval
          case DISCONTINUITY_EXACTLY_ON_STEP : // discontinuity just at the end of the step
            mFinalTime = mFinalState[mTimeIndex];
            break; 
          case DISCONTINUITY_JUST_PASSED :
          case DISCONTINUITY_ALONG_STEP : // Iterate until finding the precise point of the discontinuity
            mFinalTime = findTheDiscontinuity(mActualStepSize);
            mActualStepSize = mFinalTime - mInitialTime;
            break;
        }
        if (Double.isNaN(mFinalTime)) {
          mErrorCode=ERROR.DISCONTINUITY_PRODUCED_ERROR;
          return;
        }
      }
      else {
        computeIntermediateStep(mActualStepSize,mFinalState);
        mFinalTime = mFinalState[mTimeIndex];
      }
      double err = computeApproximation(mActualStepSize);
      mAccumulatedEvaluations += getNumberOfEvaluations();
      if (err<=1.0) {  // It has converged
        // Clean memory, if required
        mStateHistory.clean(mInitialTime);
        // Add new interval data to memory
        mStateHistory.addIntervalData(computeFinalRateAndCreateIntervalData());

        // adjust the step for next time
        if (iterations>0) mActualStepSize = limitStepSize(mActualStepSize>0 ? Math.min(mActualStepSize, estimatedStepSize(err)) : Math.max(mActualStepSize, estimatedStepSize(err)));
        else mActualStepSize = limitStepSize(estimatedStepSize(err)); // Can grow only if converged at first attempt
        return;
      }
      mActualStepSize = limitStepSize(mActualStepSize>0 ? Math.min(mActualStepSize, estimatedStepSize(err)) : Math.max(mActualStepSize, estimatedStepSize(err)));
    }
    // It did not converge
    mFinalTime = Double.NaN;
    mErrorCode=ERROR.DID_NOT_CONVERGE;
  }
  
  @Override
  protected double getActualStepSize() { return mActualStepSize; }

  // --------------------------------------------
  // Private or protected methods
  // --------------------------------------------

  /**
   * Based on code by Andrei Goussev and Yuri B. Senichenkov 
   * Adapted by Francisco Esquembre
   * @param state the state to compare to
   * @return
   */
  final protected double computeError(double[] state) {
    double error = 0;
    for(int i = 0; i < mDimension; i++) {
      double sk = mAbsTol[i] + mRelTol[i] * Math.max(Math.abs(mFinalState[i]), Math.abs(mInitialState[i]));
      double errorI = (mFinalState[i]-state[i])/sk;
      error += errorI*errorI;
    }
    return Math.sqrt(error/mDimension);
  }

  /**
   * Based on code by Andrei Goussev and Yuri B. Senichenkov 
   * Adapted by Francisco Esquembre
   * @param err
   * @return
   */
  private double estimatedStepSize(double err){
    double fac11 = 0;
    // taking decision for HNEW/H value
    if (err != 0) {
      fac11 = Math.pow(err,mExpO1);
      mFactor = fac11 / Math.exp(BETA * Math.log(mErrOld)); // stabilization
      mFactor = Math.max (1.0/FAC2, Math.min(1.0/FAC1, mFactor/SAFE)); // we require FAC1 <= HNEW/H <= FAC2
    }
    else {
      fac11 = 1.0/FAC1;
      mFactor = 1.0/FAC2;
    }
    if (err<=1.0) { // step accepted
      mErrOld = Math.max(err, 1.0e-4);
      return mActualStepSize / mFactor;
    }
    return mActualStepSize/Math.min(1.0/FAC1, fac11/SAFE); // step rejected
  }

  /**
   * Makes sure the intended step does not exceed the maximum step
   * @param intendedStep
   * @return
   */
  final private double limitStepSize(double intendedStep) {
	if (intendedStep>=0) {
		if (mODE instanceof DelayDifferentialEquation) { // Don't step more than half the minimum delay
			double[] delays = ((DelayDifferentialEquation) mODE).getDelays(mInitialState);
			for (int i=0, n=delays.length; i<n; i++) intendedStep = Math.min(intendedStep, delays[i]/2);
		}
		return Math.min(intendedStep, mMaximumStepSize);
	}
	if (mODE instanceof DelayDifferentialEquation) {
		double[] delays = ((DelayDifferentialEquation) mODE).getDelays(mInitialState);
		for (int i=0, n=delays.length; i<n; i++) intendedStep = Math.max(intendedStep, delays[i]/2);
	}
    return Math.max(intendedStep, -mMaximumStepSize);
       
  }
  
  /**
   * Based on code by Andrei Goussev and Yuri B. Senichenkov 
   * Adapted by Francisco Esquembre
   * @param hMax
   * @return
   */
  final private double estimateFirstStepSize(double hMax){
    int posneg =(hMax<0)?-1:1;
    hMax = Math.abs(hMax);
    double normF = 0.0, normX = 0.0 ;
    for(int i = 0; i < mDimension; i++) {
      double sk = mAbsTol[i] + mRelTol[i]*Math.abs(mInitialState[i]);
      double aux = mInitialRate[i]/sk;
      normF += aux*aux;
      aux = mInitialState[i]/sk;
      normX += aux*aux;
    }
    double h;
    if((normF <= 1.e-10) || (normX <= 1.e-10)) h = 1.0e-6;
    else h = Math.sqrt(normX / normF) * 0.01;
    h = posneg*Math.min(h, hMax);
    // perform an Euler step and estimate the rate, reusing finalState (it is SAFE to do so)
    if (mODE instanceof DelayDifferentialEquation) {
      mEventSolver.resetDiscontinuities(mInitialState);
      int counter = 0;
      boolean done = false;
      while (!done) {
        for (int i = 0; i < mDimension; i++) mFinalState[i] = mInitialState[i] + h * mInitialRate[i];
        switch (mEventSolver.checkDiscontinuity(mFinalState, false)) {
          case DISCONTINUITY_PRODUCED_ERROR  : return Double.NaN; 
          case DISCONTINUITY_JUST_PASSED     :
          case DISCONTINUITY_ALONG_STEP      : h = h/2; break;
          case DISCONTINUITY_EXACTLY_ON_STEP : done = true; break;  
          case NO_DISCONTINUITY_ALONG_STEP   : done = true; break; 
        }
        if ((++counter)>100) return Double.NaN;
      }
    }
    else {
      for (int i = 0; i < mDimension; i++) 
        mFinalState[i] = mInitialState[i] + h * mInitialRate[i];
    }
    mODE.getRate(mFinalState, mFinalRate);
    double der2 = 0.0;
    for(int i = 0; i < mDimension; i++) {
      double sk = mAbsTol[i] + mRelTol[i] * Math.abs(mInitialState[i]);
      double aux = (mFinalRate[i] - mInitialRate[i]) / sk;
      der2 += aux*aux;
    }
    der2 = Math.sqrt(der2) / h;
    //step size is computed as follows
    //h^order * max ( norm (initialRate), norm (der2)) = 0.01
    double der12 = Math.max(Math.abs(der2), Math.sqrt(normF));
    double h1;
    if (der12 <= 1.0e-15) h1 = Math.max(1.0e-6, Math.abs(h) * 1.0e-3);
    else h1 = Math.exp((1.0 / getMethodOrder()) * Math.log(0.01 / der12));
    h = posneg*Math.min(100*h, h1);
    if (hMax != 0) h = posneg*Math.min(Math.abs(h),hMax);
    return h;
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
