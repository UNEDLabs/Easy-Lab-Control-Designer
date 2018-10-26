/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers.rk;

import org.opensourcephysics.numerics.ode_interpolation.*;
import org.opensourcephysics.numerics.ode_solvers.SolverEngine;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR;
import org.opensourcephysics.numerics.ode_solvers.rk.irk.Radau5Adaptive;

/**
 * Title:        Radau5
 * Description:  5th order implicit RK Radau solver
 * @author Francisco Esquembre (based on code by Andrei Goussev / based on Fortran code by E. Hairer and G. Wanner)
 * @version 1.0 Jan 2014
 */

public class Radau5 extends Radau5Adaptive implements SolverEngine {
  static public final double c1 = (4.0 - Math.sqrt(6.0)) / 10.0;
  static public final double c2 = (4.0 + Math.sqrt(6.0)) / 10.0;
  static public final double c1m1 = c1 - 1.0;
  static public final double c2m1 = c2 - 1.0;
  static public final double c1mc2 = c1 - c2;

  private double takenStepSize = 0;
  private double initialTime = Double.NaN;
  private double finalTime = Double.NaN;
  private double[] initialState;
  private double [][] interpolationCoeffs;

  protected void allocateArrays(int n) {
    super.allocateArrays(n);
    initialState = new double[n];
    interpolationCoeffs = new double[4][n];
  }

  public void reinitialize(double[] _state) {
    super.reinitialize(_state);
    initialTime = _state[numEqn-1];
    System.arraycopy(_state, 0, initialState, 0, numEqn);
    ode.getRate(initialState, rate);
    finalTime = Double.NaN;
    error_code=ERROR.NO_ERROR;
  }

  public double[] getCurrentRate() { return rate; }

  public void setEstimateFirstStep(boolean _estimate) {}

  public double getMaximumTime(boolean withDiscontinuities) {
    if (error_code!=ERROR.NO_ERROR) return Double.NaN;
    if (Double.isNaN(finalTime)) return internalStep(withDiscontinuities);
    return finalTime; 
  }

  public double internalStep(boolean withDiscontinuities) {
    if (withDiscontinuities) {
      error_code = ERROR.INTERNAL_SOLVER_ERROR;
      finalTime = Double.NaN;
      return finalTime;
    }
    error_code = ERROR.NO_ERROR;
    initialTime = state[numEqn-1];
    System.arraycopy(state, 0, initialState, 0, numEqn);
    takenStepSize = super.doStep();
    constructInterpolationCoeffs();
    if (error_code!=ERROR.NO_ERROR) finalTime = Double.NaN; 
    else finalTime = initialTime + takenStepSize;
    // Finalize the step and add to memory
    mStateHistory.addIntervalData(new Radau5IntervalData(initialState, state, interpolationCoeffs));
    return finalTime;  // the final time that was computed
  }

  public double getInternalStepSize() { return takenStepSize; }

  // ------------------------------------------------
  // Private methods
  //------------------------------------------------

  /**
   * Constructs the interpolation coefficients.
   * Method uses the increment to the initial state on performed iteration
   * as input data.
   */
  private void constructInterpolationCoeffs(){
    for(int i = 0; i < numEqn; i++) {
      interpolationCoeffs[0][i] = state[i]; // not intialState !!!!
      interpolationCoeffs[1][i] = (intermediateStagesIncrement[1][i] - intermediateStagesIncrement[2][i]) / c2m1;
      double ak = (intermediateStagesIncrement[0][i] - intermediateStagesIncrement[1][i]) / c1mc2;
      double acont3 = intermediateStagesIncrement[0][i] / c1;
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
    double s = actualStepSize / takenStepSize;
    double s1 = c1 * s;
    double s2 = c2 * s;
    for (int i = 0; i < numEqn; i++) {
      initialvalue[0][i] = s1 * (interpolationCoeffs[1][i] + (s1 - c2m1) * (interpolationCoeffs[2][i] + (s1 - c1m1) * interpolationCoeffs[3][i]));
      initialvalue[1][i] = s2 * (interpolationCoeffs[1][i] + (s2 - c2m1) * (interpolationCoeffs[2][i] + (s2 - c1m1) * interpolationCoeffs[3][i]));
      initialvalue[2][i] = s * (interpolationCoeffs[1][i] + (s - c2m1) * (interpolationCoeffs[2][i] + (s - c1m1) * interpolationCoeffs[3][i]));
    }
  }

}
