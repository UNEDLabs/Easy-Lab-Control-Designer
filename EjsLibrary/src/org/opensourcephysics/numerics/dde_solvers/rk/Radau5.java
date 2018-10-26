/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.rk;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opensourcephysics.numerics.*;
import org.opensourcephysics.numerics.dde_solvers.interpolation.*;
import org.opensourcephysics.numerics.dde_solvers.rk.irk.Radau5Adaptive;

/**
 * Title:        Radau5
 * Description:  5th order implicit RK Radau solver
 * @author Francisco Esquembre (based on code by Andrei Goussev / based on Fortran code by E. Hairer and G. Wanner)
 * @author Maria Jose Cano 
 * @version 1.0 Feb 2011
 */

public class Radau5 extends Radau5Adaptive implements ODESolverInterpolator {
  final static public double c1 = (4.0 - Math.sqrt(6.0)) / 10.0;
  final static public double c2 = (4.0 + Math.sqrt(6.0)) / 10.0;
  final static public double c1m1 = c1 - 1.0;
  final static public double c2m1 = c2 - 1.0;
  final static public double c1mc2 = c1 - c2;

  private double takenStepSize = 0;
  private double initialTime = Double.NaN;
  private double finalTime = Double.NaN;
  private double[] initialState;
  private double [][] interpolationCoeffs;

  private Set<Double> mDiscontinuities = new HashSet<Double>();
  private Set<Double> mDiscontinuitiesToRemove = new HashSet<Double>();

  public Radau5(ODE ode) {
    super(ode);
  }

  protected void allocateArrays(int n) {
    super.allocateArrays(n);
    initialState = new double[n];
    interpolationCoeffs = new double[4][n];
  }

  public void reinitialize(double[] _state) {
    super.reinitialize(_state);
    initialTime = _state[mDimension-1];
    System.arraycopy(_state, 0, initialState, 0, mDimension);
    
    if (mDDE!=null) { // DDE
      double maxDelay = Math.abs(mDDE.getMaximumDelay());
      double checkPoint = (mInitialStepSize>0) ? initialTime-maxDelay : initialTime+maxDelay;
      // Reset discontinuities
      mDiscontinuities.clear();
      // add initial condition discontinuities
      double[] icDisc = mDDE.getInitialConditionDiscontinuities(); 
      if (icDisc!=null && icDisc.length>0) {
        if (mInitialStepSize>0) { // forwards
          for (int i=0; i<icDisc.length; i++) if (icDisc[i]>checkPoint) mDiscontinuities.add(icDisc[i]);    
        }
        else { // backwards
          for (int i=0; i<icDisc.length; i++) if (icDisc[i]<checkPoint) mDiscontinuities.add(icDisc[i]);    
        }
      }
      // add previous discontinuities
      double[] delays = mDDE.getDelays(mState);
      if (mInitialStepSize>0) { // forwards
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
      for (int i=0; i<delays.length; i++) mDiscontinuities.add(initialTime+delays[i]);
      // prepare the first step
      double allowedStep = mActualStepSize;
      double nextDiscont = getNextDiscontinuity(allowedStep);
      if (!Double.isInfinite(nextDiscont)) allowedStep = nextDiscont-initialTime;
      mWrapper.prepareStep(initialTime+allowedStep/2, mDDE.getDelays(initialState));
    } // end of DDE
    
    mWrapper.evaluateRate(initialState, mRate);
    finalTime = Double.NaN;
    error_code=ODEAdaptiveSolver.NO_ERROR;
  }

  /**
   * Returns next discontinuity in the desired step
   * @return the disccontinuity if there is one, an infinite value if there is none
   */
  private double getNextDiscontinuity(double desiredStep) {
    // For a DDE you cannot step beyond one of the discontinuities propagated by the delays
    double discFound;
    double maxIntended = initialTime + desiredStep;
    if (desiredStep>0) { // going forwards
      // First, do some housekeeping : delete unnecesary (old) discontinuities
      mDiscontinuitiesToRemove.clear();
      for (Double discont: mDiscontinuities) if (initialTime>discont) mDiscontinuitiesToRemove.add(discont);
      mDiscontinuities.removeAll(mDiscontinuitiesToRemove);

      // Check not to step beyond a discontinuity
      discFound = Double.POSITIVE_INFINITY;
      for (Double discont : mDiscontinuities) {
        if (initialTime<discont &&  maxIntended>discont) discFound = Math.min(discFound, discont);
      }
    }
    else { // going backwards
      // First, do some housekeeping : delete unnecesary (old) discontinuities
      mDiscontinuitiesToRemove.clear();
      for (Double discont: mDiscontinuities) if (initialTime<discont) mDiscontinuitiesToRemove.add(discont);
      mDiscontinuities.removeAll(mDiscontinuitiesToRemove);
      
      // Check not to step beyond a discontinuity
      discFound = Double.NEGATIVE_INFINITY;
      for (Double discont : mDiscontinuities) {
        if (initialTime>discont &&  maxIntended<discont) discFound = Math.max(discFound, discont);
      }
    }
    return discFound; 
  }

  public double[] getCurrentRate() { return mRate; }

  final public void setEstimateFirstStep(boolean _estimate) {}

  final public double getMaximumTime() {
    if (error_code!=ODEAdaptiveSolver.NO_ERROR) return Double.NaN;
    if (Double.isNaN(finalTime)) return internalStep();
    return finalTime; 
  }

  final public double internalStep() {
    initialTime = mState[mDimension-1];
    System.arraycopy(mState, 0, initialState, 0, mDimension);
        
    double discFound=Double.POSITIVE_INFINITY;
    if (mDDE!=null) { // DDE
      // For a DDE, you cannot step beyond one of the discontinuities propagated by the delays
      double maxIntended = initialTime + mActualStepSize;
      // Check not to step beyond a discontinuity
      if (mActualStepSize>=0) { // going forwards
//        discFound = Double.POSITIVE_INFINITY;
        for (Double discont : mDiscontinuities) {
          if (initialTime<discont && maxIntended>discont) discFound = Math.min(discFound, discont);
        }
      }
      else { // going backwards
        discFound = Double.NEGATIVE_INFINITY;
        for (Double discont : mDiscontinuities) {
          if (initialTime>discont && maxIntended<discont) discFound = Math.max(discFound, discont);
        }
      }
      if (!Double.isInfinite(discFound)) { // do not step further than the discontinuity found
        mActualStepSize = discFound - initialTime;
      }
      // delete unnecesary discontinuities
      Set<Double> disctToRemove = new HashSet<Double>();
      if (mActualStepSize>0)  {
        for (Double discont: mDiscontinuities) if (initialTime>discont) disctToRemove.add(discont);
      }
      else {
        for (Double discont: mDiscontinuities) if (initialTime<discont) disctToRemove.add(discont);
      }
      mDiscontinuities.removeAll(disctToRemove);
    } // end DDE
    
    takenStepSize = super.doStep();
    constructInterpolationCoeffs();
    if (error_code!=ODEAdaptiveSolver.NO_ERROR) finalTime = Double.NaN; 
    else finalTime = initialTime + takenStepSize;
    
    // propagate discontinuity if there was one
    if (!Double.isInfinite(discFound) && Math.abs(finalTime-discFound)<1.0e-14) { // then it is a DDE and there was a discontinuity
      double[] delays = mDDE.getDelays(mState);
      for (int i=0; i<delays.length; i++) mDiscontinuities.add(discFound + delays[i]);
    }

    // Clean memory, if required
    if (mStateMemoryLength==0) mStateMemory.clearAll(); // remember only the new interval
    else if (!Double.isInfinite(mStateMemoryLength)) mStateMemory.clearBefore(mActualStepSize>0 ? initialTime-mStateMemoryLength : initialTime+mStateMemoryLength);

    // Finalize the step and add to memory
    mStateMemory.addIntervalData(new Radau5IntervalData(initialState, mState, interpolationCoeffs));

    return finalTime;  // the final time that was computed
  }    

  public double getInternalStepSize() { return takenStepSize; }

  /**
   * Constructs the interpolation coefficients.
   * Method uses the increment to the initial state on performed iteration
   * as input data.
   */
  private void constructInterpolationCoeffs(){
    for(int i = 0; i < mDimension; i++) {
      interpolationCoeffs[0][i] = mState[i]; // not intialState !!!!
      interpolationCoeffs[1][i] = (mIntermediateStagesIncrement[1][i] - mIntermediateStagesIncrement[2][i]) / c2m1;
      double ak = (mIntermediateStagesIncrement[0][i] - mIntermediateStagesIncrement[1][i]) / c1mc2;
      double acont3 = mIntermediateStagesIncrement[0][i] / c1;
      acont3 = (ak - acont3) / c2;
      interpolationCoeffs[2][i] = (ak - interpolationCoeffs[1][i]) / c1m1;
      interpolationCoeffs[3][i] = interpolationCoeffs[2][i] - acont3;
    }
  }

  /**
   * Predicts the increment to the stages vectors array for the next step
   * using the extrapolation
   * @param initialvalue the value to be adjusted
   */
  protected void estimateNewtonInitialValue(double[][] initialvalue) {
    double s = mActualStepSize / takenStepSize;
    double s1 = c1 * s;
    double s2 = c2 * s;
    for (int i = 0; i < mDimension; i++) {
      initialvalue[0][i] = s1 * (interpolationCoeffs[1][i] + (s1 - c2m1) * (interpolationCoeffs[2][i] + (s1 - c1m1) * interpolationCoeffs[3][i]));
      initialvalue[1][i] = s2 * (interpolationCoeffs[1][i] + (s2 - c2m1) * (interpolationCoeffs[2][i] + (s2 - c1m1) * interpolationCoeffs[3][i]));
      initialvalue[2][i] = s * (interpolationCoeffs[1][i] + (s - c2m1) * (interpolationCoeffs[2][i] + (s - c1m1) * interpolationCoeffs[3][i]));
    }
  }

}
