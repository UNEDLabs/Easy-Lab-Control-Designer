/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ODEAdaptiveSolver;
import org.opensourcephysics.numerics.ODESolverInterpolator;
import org.opensourcephysics.numerics.rk.irk.*;

/**
 * Title:        Radau5
 * Description:  5th order implicit RK Radau solver
 * @author Francisco Esquembre (based on code by Andrei Goussev / based on Fortran code by E. Hairer and G. Wanner)
 * @version 1.0 January 2009
 */

public class Radau5 extends Radau5Adaptive implements ODESolverInterpolator {
  final static double c1 = (4.0 - Math.sqrt(6.0)) / 10.0;
  final static double c2 = (4.0 + Math.sqrt(6.0)) / 10.0;
  final static double c1m1 = c1 - 1.0;
  final static double c2m1 = c2 - 1.0;
  final static double c1mc2 = c1 - c2;

  private double takenStepSize = 0;
  private double initialTime = Double.NaN;
  private double finalTime = Double.NaN;
  private double[] initialState;
  private double [][] interpolationCoeffs;

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
    initialTime = _state[numEqn-1];
    System.arraycopy(_state, 0, initialState, 0, numEqn);
    ode.getRate(initialState, rate);
    finalTime = Double.NaN;
    error_code=ODEAdaptiveSolver.NO_ERROR;
  }

  public double[] getCurrentRate() { return rate; }

  public void setEstimateFirstStep(boolean _estimate) {}

  public double getMaximumTime() {
    if (error_code!=ODEAdaptiveSolver.NO_ERROR) return Double.NaN;
    if (Double.isNaN(finalTime)) return internalStep();
    return finalTime; 
  }

  final public double internalStep() {
    initialTime = state[numEqn-1];
    System.arraycopy(state, 0, initialState, 0, numEqn);
    takenStepSize = super.doStep();
    constructInterpolationCoeffs();
    if (error_code!=ODEAdaptiveSolver.NO_ERROR) finalTime = Double.NaN; 
    else finalTime = initialTime + takenStepSize;
    return finalTime;  // the final time that was computed
  }

  public double getInternalStepSize() { return takenStepSize; }

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

  public void setMemoryLength(double length) {}

  // No memory state for this class
  public org.opensourcephysics.numerics.dde_solvers.interpolation.StateMemory getStateMemory() {
    return null;
  }

  /**
   * Implements continuous vector function that approximates the solution
   * of the ODE. Value of the vector function constructs using interpolation coefficients
   * @param time the point where approximation to the solution to be obtained.
   *        (0 < time < takenstepSize corresponds to the interpolation, in other cases
   *         output will indeed the extrapolation)
   * @param result the result, i.e. approximated solution of ODE
   */
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) {
    //      if (_time==initialTime) {
    //        System.arraycopy(initialState, 0, _state, 0, mNumEqn);
    //        return _state;
    //      }
    //      if (Double.isNaN(finalTime)) return null;
    //      if (_time==finalTime) {
    //        System.arraycopy(state, 0, _state, 0, mNumEqn);
    //        return _state;
    //      }
    double s = (_time-finalTime) / takenStepSize;
    for (int i = 0,n=numEqn-1; i < n; i++) {
      _state[i] = interpolationCoeffs[0][i] + s * (interpolationCoeffs[1][i] + (s - c2m1) * (interpolationCoeffs[2][i] + (s - c1m1) * interpolationCoeffs[3][i]));
    }
    _state[numEqn-1] = _time;
    return _state;
  }

  public double[] interpolate(double _time, boolean _useLeftApproximation, double[] _state, int _beginIndex, int _length) {
    double s = (_time-finalTime) / takenStepSize;
    for (int i = 0,index=_beginIndex; i < _length; i++,index++) {
      _state[i] = interpolationCoeffs[0][index] + s * (interpolationCoeffs[1][index] + (s - c2m1) * (interpolationCoeffs[2][index] + (s - c1m1) * interpolationCoeffs[3][index]));
    }
    return _state;
  }

  public double interpolate(double _time, boolean _useLeftApproximation, int _index) {
    double s = (_time-finalTime) / takenStepSize;
    return interpolationCoeffs[0][_index] + s * (interpolationCoeffs[1][_index] + (s - c2m1) * (interpolationCoeffs[2][_index] + (s - c1m1) * interpolationCoeffs[3][_index]));
  }

  public double[] bestInterpolate(double _time, double[] _state) {
    return interpolate(_time,false,_state);  
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
