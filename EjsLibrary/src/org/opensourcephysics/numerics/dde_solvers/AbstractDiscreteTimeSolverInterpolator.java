/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers;

import org.opensourcephysics.numerics.*;
import org.opensourcephysics.numerics.dde_solvers.interpolation.*;
import java.util.*;

/**
 * Title:        AbstractDiscreteTimeSolverInterpolator
 * Description:  Abstract class for a solver based on time discretization with state memory for interpolation
 * @author Francisco Esquembre
 * @author Maria Jose Cano 
 * @version 1.0 Feb 2011
 */

public abstract class AbstractDiscreteTimeSolverInterpolator implements ODESolverInterpolator {
  
  protected int mErrorCode=ODEAdaptiveSolver.NO_ERROR;
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
  protected Wrapper mWrapper;

  // DDE variables
  protected DDE mDDE;
  protected Set<Double> mDiscontinuities = new HashSet<Double>();
  private Set<Double> mDiscontinuitiesToRemove = new HashSet<Double>();
  
  // Memory
  protected StateMemory mStateMemory;
  protected double mStateMemoryLength;

  // --------------------------------------------
  // Constructor
  // --------------------------------------------

  protected AbstractDiscreteTimeSolverInterpolator(org.opensourcephysics.numerics.ODE ode) {
    mODE = ode;
    double[] state = mODE.getState();
    mDimension = state.length;
    mTimeIndex = mDimension-1;
    if (mODE instanceof DDE) {
      mDDE = (DDE) ode;
      mStateMemoryLength = Math.abs(mDDE.getMaximumDelay());
      mWrapper = new WrapperDDE(mDDE,this,state);
    }
    else {
      mDDE = null;
      mStateMemoryLength = 0;
      mWrapper = new WrapperODE(mODE);
    }
    mStateMemory = new StateMemory(mODE);
  } 

  // ------------------------------------------------
  // Implementation of ODESolverInterpolator
  //------------------------------------------------
  
  final public ODE getODE() {	return mODE; }

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
    mStateMemory.clearAll();
    reinitialize(state);
  }

  public void reinitialize(double[] state) {
    mInitialTime = state[mTimeIndex];
    System.arraycopy(state, 0, mInitialState, 0, mDimension);

    if (mDDE!=null) { // DDE
      double maxDelay = Math.abs(mDDE.getMaximumDelay());
      double checkPoint = (mStepSize>0) ? mInitialTime-maxDelay : mInitialTime+maxDelay;
      // Reset discontinuities
      mDiscontinuities.clear();
      // add initial condition discontinuities
      double[] icDisc = mDDE.getInitialConditionDiscontinuities(); 
      if (icDisc!=null && icDisc.length>0) {
        if (mStepSize>0) { // forwards
          for (int i=0; i<icDisc.length; i++) if (icDisc[i]>checkPoint) mDiscontinuities.add(icDisc[i]);    
        }
        else { // backwards
          for (int i=0; i<icDisc.length; i++) if (icDisc[i]<checkPoint) mDiscontinuities.add(icDisc[i]);    
        }
      }
      // add previous discontinuities
      double[] delays = mDDE.getDelays(state);
      if (mStepSize>0) { // forwards
        for (Iterator<IntervalData> iterator = mStateMemory.getDescendingIterator(); iterator.hasNext(); ) {
          IntervalData interval = iterator.next();
          if (interval.endsAtDiscontinuity() && (interval.getRight()>checkPoint)) {
            for (int i=0; i<delays.length; i++) mDiscontinuities.add(interval.getRight()+delays[i]);
          }
        }
      }
      else { // backwards
        for (Iterator<IntervalData> iterator = mStateMemory.getDescendingIterator(); iterator.hasNext(); ) {
          IntervalData interval = iterator.next();
          if (interval.endsAtDiscontinuity() && (interval.getRight()<checkPoint)) {
            for (int i=0; i<delays.length; i++) mDiscontinuities.add(interval.getRight()+delays[i]);
          }
        }
      }
      // add discontinuities starting from current time
      for (int i=0; i<delays.length; i++) mDiscontinuities.add(mInitialTime+delays[i]);
      // prepare the first step
      double allowedStep = getActualStepSize();
      double nextDiscont = getNextDiscontinuity(allowedStep);
      if (!Double.isInfinite(nextDiscont)) allowedStep = nextDiscont-mInitialTime;
      mWrapper.prepareStep(mInitialTime+allowedStep/2, mDDE.getDelays(mInitialState));
    } // end of DDE

    mWrapper.evaluateRate(mInitialState, mInitialRate);
    mFinalTime = Double.NaN;
    mErrorCode = ODEAdaptiveSolver.NO_ERROR;
  }

  public double[] getCurrentRate() { return mInitialRate; }

  final public void setStepSize(double stepSize) { mStepSize = stepSize; }

  public void setMaximumStepSize(double stepSize) { mMaximumStepSize = Math.abs(stepSize); }

  final public double getStepSize() { return mStepSize; }
  
  final public double getInternalStepSize() { return mFinalTime-mInitialTime; }
  
  public void setEstimateFirstStep(boolean _estimate) {}
  
  public void setTolerances(double absTol, double relTol) {}

  final public double getMaximumTime() {
    if (mErrorCode!=ODEAdaptiveSolver.NO_ERROR) return Double.NaN;
    if (Double.isNaN(mFinalTime)) {
      if (mDDE==null) computeOneStep(getActualStepSize(),Double.POSITIVE_INFINITY);
      else {
        double allowedStep = getActualStepSize();
        double nextDiscont = getNextDiscontinuity(allowedStep);
        if (!Double.isInfinite(nextDiscont)) allowedStep = nextDiscont-mInitialTime;
        mWrapper.prepareStep(mInitialTime+allowedStep/2, mDDE.getDelays(mInitialState));
        computeOneStep(allowedStep,nextDiscont);
      }
    }
    return mFinalTime; 
  }

  final public double internalStep() {
    mInitialTime = mFinalTime;
    mErrorCode = ODEAdaptiveSolver.NO_ERROR;
    System.arraycopy(mFinalState, 0, mInitialState, 0, mDimension);
    if (mDDE==null) {
      System.arraycopy(mFinalRate,  0, mInitialRate,  0, mDimension);
      computeOneStep(getActualStepSize(),Double.POSITIVE_INFINITY);
    }
    else { // need first to recalculate the initial rate, due to possible discontinuities.
      double allowedStep = getActualStepSize();
      double nextDiscont = getNextDiscontinuity(allowedStep);
      if (!Double.isInfinite(nextDiscont)) allowedStep = nextDiscont-mInitialTime;
      mWrapper.prepareStep(mInitialTime+allowedStep/2, mDDE.getDelays(mInitialState));
      mWrapper.evaluateRate(mInitialState, mInitialRate);
      computeOneStep(allowedStep,nextDiscont);
    }
    return mFinalTime;  // the final time that was computed
  }

  final public long getCounter()  { return mAccumulatedEvaluations; }
  
  final public void setMemoryLength(double length) {
    length = Math.abs(length);
    if (mDDE!=null) mStateMemoryLength = Math.max(length,Math.abs(mDDE.getMaximumDelay()));
    else mStateMemoryLength = length;
    if (mStateMemoryLength==0) mStateMemory.clearAll();
  }
  
  public StateMemory getStateMemory() { return mStateMemory; }

  public double[] interpolate(double time, boolean useLeftApproximation, double[] state) {
    return mStateMemory.interpolate(time, useLeftApproximation, state);
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
    return computeIntermediateStep(time-mInitialTime, state);
  }

  // --------------------------------------------
  // Private or protected methods
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
   * @return same as state
   */
  abstract protected double[] computeIntermediateStep(double step, double[] state);

  /**
   * Computes the final rate and creates a new IntervalData for interpolation
   */
  abstract protected IntervalData computeFinalRateAndCreateIntervalData(); 

  /**
   * Returns the actual step size to take. Adaptive solvers override this method
   * @return
   */
  protected double getActualStepSize() { return mStepSize; }
  
  /**
   * Returns next discontinuity in the desired step
   * @return the disccontinuity if there is one, an infinite value if there is none
   */
  private double getNextDiscontinuity(double desiredStep) {
    // For a DDE you cannot step beyond one of the discontinuities propagated by the delays
    double discFound;
    double maxIntended = mInitialTime + desiredStep;
    if (desiredStep>0) { // going forwards
      // First, do some housekeeping : delete unnecesary (old) discontinuities
      mDiscontinuitiesToRemove.clear();
      for (Double discont: mDiscontinuities) if (mInitialTime>discont) mDiscontinuitiesToRemove.add(discont);
      mDiscontinuities.removeAll(mDiscontinuitiesToRemove);

      // Check not to step beyond a discontinuity
      discFound = Double.POSITIVE_INFINITY;
      for (Double discont : mDiscontinuities) {
        if (mInitialTime<discont &&  maxIntended>discont) discFound = Math.min(discFound, discont);
      }
    }
    else { // going backwards
      // First, do some housekeeping : delete unnecesary (old) discontinuities
      mDiscontinuitiesToRemove.clear();
      for (Double discont: mDiscontinuities) if (mInitialTime<discont) mDiscontinuitiesToRemove.add(discont);
      mDiscontinuities.removeAll(mDiscontinuitiesToRemove);
      
      // Check not to step beyond a discontinuity
      discFound = Double.NEGATIVE_INFINITY;
      for (Double discont : mDiscontinuities) {
        if (mInitialTime>discont &&  maxIntended<discont) discFound = Math.max(discFound, discont);
      }
    }
    return discFound; 
  }

  /**
   * Computes mFinalState[] and mFinalTime out of the current mInitialState[] and mInitialTime,
   * taking into account the mStepSize and the tolerance (which ever applies).
   * Not final because Adaptive solvers make it otherwise.
   */
  protected void computeOneStep(double step, double discontinuity) {
    computeIntermediateStep(step,mFinalState);
    mFinalTime = mFinalState[mTimeIndex];
    if (!Double.isInfinite(discontinuity)) { // a DDE with a discontinuity
      // Propagate this discontinuity further on
      double[] delays = mDDE.getDelays(mFinalState);
      for (int i=0; i<delays.length; i++) mDiscontinuities.add(mFinalTime + delays[i]);            
    } // end DDE

    // Clean memory, if required
    if (mStateMemoryLength==0) mStateMemory.clearAll(); // remember only the new interval
    else if (!Double.isInfinite(mStateMemoryLength)) mStateMemory.clearBefore(mStepSize>0 ? mInitialTime-mStateMemoryLength : mInitialTime+mStateMemoryLength);

    // Accumulate the counter
    mAccumulatedEvaluations += getNumberOfEvaluations();
    
    // Add new interval data to memory
    mStateMemory.addIntervalData(computeFinalRateAndCreateIntervalData());
  }

  // --------------------------------------------
  // Private or protected classes
  // --------------------------------------------

  
  static protected interface Wrapper {
    /**
     * Prepares the ODE for a sequence of evaluations of the algorithm in an interval containing this time
     * @param time
     * @param delays
     */
    void prepareStep (double time, double[] delays);
    
    /**
     * Evaluates the rate at the given state
     * @param state
     * @param rate
     */
    void evaluateRate(double[] state, double[] rate);
  }
  
  static protected class WrapperODE implements Wrapper {
    private ODE mODE;
    
    public WrapperODE(ODE ode) { mODE = ode; }
    
    public void prepareStep (double time, double[] delays) { } // Does nothing
    
    public void evaluateRate(double[] state, double[] rate) { mODE.getRate(state, rate); }
  }
  
  static public class WrapperDDE implements Wrapper {
    private DDE mDDE;
    private AbstractDiscreteTimeSolverInterpolator mSolver;
    private IntervalData[] mIntervals;

    public WrapperDDE(DDE dde, AbstractDiscreteTimeSolverInterpolator solver, double[] state) {
      mDDE = dde;
      mSolver = solver;
      mIntervals = new IntervalData[dde.getDelays(state).length];
    }
    
    public void prepareStep (double time, double[] delays) {
//      if (delays.length!=mIntervals.length) mIntervals = new IntervalData[delays.length];
      for (int i=0; i<delays.length; i++) mIntervals[i] = mSolver.mStateMemory.findInterval(time-delays[i], false);
    }
    
    public void evaluateRate(double[] state, double[] rate) {
      mDDE.getRate(state, mIntervals,rate);
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
