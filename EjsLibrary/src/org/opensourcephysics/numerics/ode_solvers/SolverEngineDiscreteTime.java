/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_interpolation.*;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.DISCONTINUITY_CODE;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR;

/**
 * Title:        SolverInterpolatorDiscreteTime
 * Description:  Abstract class for a solver based on time discretization with state memory for interpolation
 * @author Francisco Esquembre
 * @author Maria Jose Cano 
 * @version 1.0 Feb 2011
 * @version 3 Aug 2013
 */

public abstract class SolverEngineDiscreteTime implements SolverEngine {
  
  // ODE variables
  protected ERROR mErrorCode=ERROR.NO_ERROR;
  protected int mDimension;
  protected int mTimeIndex;
  protected long mAccumulatedEvaluations = 0;
  protected double mStepSize = 0.1;
  protected double mMaximumStepSize = Double.POSITIVE_INFINITY;
  protected double mInitialTime=0;
  protected double mFinalTime=0;
  protected double [] mInitialState;
  protected double [] mInitialRate;
  protected double [] mFinalState;
  protected double [] mFinalRate;
  protected ODE mODE;
  protected InterpolatorEventSolver mEventSolver;
  
  // Memory
  protected StateHistory mStateHistory;

  // --------------------------------------------
  // abstract methods
  // --------------------------------------------

  /**
   * The number of function evaluations per step
   */
  abstract protected int getNumberOfEvaluations();

  /**
   * Allocates other arrays needed for the method
   */
  abstract protected void allocateOtherArrays();

  /**
   * Computes one intermediate step not caring about precision
   * @param step the length of the step
   * @param state the target array
   */
  abstract protected void computeIntermediateStep(double step, double[] state);

  /**
   * Computes one intermediate step not caring about precision but taking care of possible discontinuities
   * @param eventSolver the InterpolatorEventSolver which is taking care of the discontinuities 
   * @param _step the length of the step
   * @param _state the target array
   * @return one of the flags in the checkDiscontinuity(double[]) method of ODEInterpolatorEventSolver
   * @see ODEInterpolatorEventSolver#checkDiscontinuity(double[])
   */
  abstract protected DISCONTINUITY_CODE computeIntermediateStep(InterpolatorEventSolver eventSolver, double step, double[] state);

  /**
   * Computes the final rate and creates a new IntervalData for interpolation
   */
  abstract protected IntervalData computeFinalRateAndCreateIntervalData(); 

  public String toString() {
    return this.getClass().getSimpleName();
  }
  
  
  // --------------------------------------------
  // Initialization
  // --------------------------------------------

  public void setODE(InterpolatorEventSolver eventSolver, ODE ode) { 
    mEventSolver = eventSolver;
    mODE = ode;
    double[] state = mODE.getState();
    mDimension = state.length;
    mTimeIndex = mDimension-1;
    mStateHistory = new StateHistory(ode);
    if (mODE instanceof DelayDifferentialEquation) {
      DelayDifferentialEquation dde = (DelayDifferentialEquation) mODE;
      mStateHistory.setMinimumLength(dde.getMaximumDelay()); // Make sure we have enough memory for delays
    }
  } 

  // ------------------------------------------------
  // Implementation of SolverEngine
  //------------------------------------------------
  
  final public void initialize(double stepSize) {
    mStepSize = stepSize;
    double[] state = mODE.getState();
    if (mInitialState==null || mInitialState.length!=state.length) {
      mDimension = state.length;
      mTimeIndex = mDimension-1;
      mInitialState = new double[mDimension];
      mInitialRate  = new double[mDimension];
      mFinalState   = new double[mDimension];
      mFinalRate    = new double[mDimension];
      allocateOtherArrays();
    }
    mAccumulatedEvaluations = 0;
    mStateHistory.clearAll();
    if (mODE instanceof DelayDifferentialEquation) {
      DelayDifferentialEquation dde = (DelayDifferentialEquation) mODE;
      mStateHistory.setMinimumLength(Math.max(Math.abs(dde.getMaximumDelay()),Math.abs(stepSize))); // Make sure we have enough memory for delays and events
    }
    else mStateHistory.setMinimumLength(stepSize); // Make sure we have enough memory for events
    reinitialize(state);
  }

  public void reinitialize(double[] state) {
    mInitialTime = state[mTimeIndex];
    System.arraycopy(state, 0, mInitialState, 0, mDimension);
    mODE.getRate(mInitialState, mInitialRate);
    mAccumulatedEvaluations++;
    mFinalTime = Double.NaN;
    mErrorCode = ERROR.NO_ERROR;
  }

  public double[] getCurrentRate() { return mInitialRate; }

  final public void setStepSize(double stepSize) { 
    mStepSize = stepSize; 
    if (mODE instanceof DelayDifferentialEquation) {
      DelayDifferentialEquation dde = (DelayDifferentialEquation) mODE;
      mStateHistory.setMinimumLength(Math.max(Math.abs(dde.getMaximumDelay()),Math.abs(stepSize))); // Make sure we have enough memory for delays and events
    }
    else mStateHistory.setMinimumLength(stepSize); // Make sure we have enough memory for events
  }

  public void setMaximumStepSize(double stepSize) { mMaximumStepSize = Math.abs(stepSize); }

  final public double getStepSize() { return mStepSize; }
  
  final public double getInternalStepSize() { return mFinalTime-mInitialTime; }
  
  public void setEstimateFirstStep(boolean estimate) {}
  
  public void setTolerances(double absTol, double relTol) {}

  final public double getMaximumTime(boolean withDiscontinuities) {
    if (mErrorCode!=ERROR.NO_ERROR) return Double.NaN;
    if (Double.isNaN(mFinalTime)) {
      computeOneStep(withDiscontinuities);
    }
    return mFinalTime; 
  }

  final public double internalStep(boolean withDiscontinuities) {
    mInitialTime = mFinalTime;
    mErrorCode = ERROR.NO_ERROR;
    System.arraycopy(mFinalState, 0, mInitialState, 0, mDimension);
    System.arraycopy(mFinalRate,  0, mInitialRate,  0, mDimension);
    computeOneStep(withDiscontinuities);
    return mFinalTime;  // the final time that was computed
  }

  final public long getCounter()  { return mAccumulatedEvaluations; }
  
  public StateHistory getStateHistory() { return mStateHistory; }

  public double[] interpolate(double time, double[] state) {
    return mStateHistory.interpolate(time, state);
  }

  /*
   * Provides 'brute-force' interpolation by re-stepping from the initial time every time
   */
  public double[] bestInterpolate(double time, double[] state) {
    if (Double.isNaN(mFinalTime)) return null;
    if (time==mFinalTime) {
      System.arraycopy(mFinalState, 0, state, 0, mDimension);
      return state;
    }
    if (time==mInitialTime) {
      System.arraycopy(mInitialState, 0, state, 0, mDimension);
      return state;
    }
    computeIntermediateStep(time-mInitialTime, state);
    return state;
  }

  /**
   * Returns the actual step size to take. Adaptive solvers override this method
   * @return
   */
  protected double getActualStepSize() { return mStepSize; }
    
  protected double findTheDiscontinuity(double step) {
    int counter = 0;
    double left = 0;
    double right = step;
//    double lastValid = 0;
    int maxAttempts = mEventSolver.getDDEIterations();
//    System.err.println ("Finding the discontinuity in "+mInitialState[mTimeIndex]+", "+(mInitialState[mTimeIndex]+step)+" ------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    while (counter<maxAttempts) {
      if (Math.abs(left-right)<InterpolatorEventSolver.EPSILON) { // 1.0e-15) { // Too close
//        System.err.println("Left and right are equal!: "+counter+" / "+maxAttempts);
        break;
      }
      double testPoint = (left+right)/2;
//      System.err.println ("\nTest point = "+(mInitialState[mTimeIndex]+testPoint));
      switch (computeIntermediateStep(mEventSolver, testPoint, mFinalState)) {
        default : 
        case DISCONTINUITY_PRODUCED_ERROR : return Double.NaN; 
        case NO_DISCONTINUITY_ALONG_STEP  :  
//          lastValid = mFinalState[mTimeIndex];
          left = testPoint; // and keep on searching
          break; 
        case DISCONTINUITY_EXACTLY_ON_STEP : 
//          System.err.println ("Found at point = "+mFinalState[mTimeIndex]+" ------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
          return mFinalState[mTimeIndex] ; // discontinuity just at the end of the step
        case DISCONTINUITY_JUST_PASSED :
          right = right - (right-left)/4; // and keep on searching but closer to the right end
          break;
        case DISCONTINUITY_ALONG_STEP : // Iterate until finding the precise point of the discontinuity
          right = testPoint; // and keep on searching
          break;
      }
      counter++; // try again
    }
//    System.err.println("Not found. taking last valid point = "+lastValid+" ------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    computeIntermediateStep(mEventSolver, left, mFinalState);
    return mFinalState[mTimeIndex]; // not found, so take the last valid point
  }
  
  /**
   * Computes mFinalState[] and mFinalTime out of the current mInitialState[] and mInitialTime,
   * taking into account the mStepSize and the tolerance (which ever applies).
   * Not final because Adaptive solvers make it otherwise.
   */
  protected void computeOneStep(boolean hasDiscontinuities) {
    double step = mStepSize;
//    System.err.println("Trying to step to "+(mInitialState[mTimeIndex]+step));
    if (hasDiscontinuities) {
      switch (computeIntermediateStep(mEventSolver, step, mFinalState)) {
        case DISCONTINUITY_PRODUCED_ERROR : 
          mFinalTime = Double.NaN; 
          return;
        case NO_DISCONTINUITY_ALONG_STEP  :  // no discontinuity in this interval
        case DISCONTINUITY_EXACTLY_ON_STEP : // discontinuity just at the end of the step
          mFinalTime = mFinalState[mTimeIndex];
          break; 
        case DISCONTINUITY_JUST_PASSED :
        case DISCONTINUITY_ALONG_STEP : // Iterate until finding the precise point of the discontinuity
          mFinalTime = findTheDiscontinuity(step);
          break;
      }
      if (Double.isNaN(mFinalTime)) {
        mErrorCode=ERROR.DISCONTINUITY_PRODUCED_ERROR;
        return;
      }
    }
    else {
      computeIntermediateStep(step,mFinalState);
      mFinalTime = mFinalState[mTimeIndex];
    }
//    System.err.println("Will really step to "+mFinalState[mTimeIndex]);
    // Accumulate the counter
    mAccumulatedEvaluations += getNumberOfEvaluations();
    // Clean memory, if required
    mStateHistory.clean(mInitialTime);
    // Add new interval data to memory
    mStateHistory.addIntervalData(computeFinalRateAndCreateIntervalData());
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
