/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

import org.opensourcephysics.numerics.*;

/**
 * Title:        AbstractExplicitRKSolverInterpolator
 * Description:  Abstract class for an explicit Runge Kutta solver with Hermite interpolation.
 * Actually, the name is not appropriated, since the same base class can be (and actually are) used for other, 
 * non-RK, discrete-time methods, such as VelocityVerlet. A better name would be AbstractDiscreteTimeSolverInterpolator
 * @author Francisco Esquembre
 * @version 1.0 December 2008
 */
public abstract class AbstractExplicitRKSolverInterpolator implements ODESolverInterpolator {
  protected double stepSize = 0.1;
  protected double maximumStepSize = Double.POSITIVE_INFINITY;
  protected int dimension;
  protected int timeIndex;
  protected int error_code=ODEAdaptiveSolver.NO_ERROR;
  protected double initialTime=0;
  protected double finalTime=0;
  protected double deltaTime=0;
  protected double [] initialState;
  protected double [] initialRate;
  protected double [] finalState;
  protected double [] finalRate;
  protected ODE ode;

  protected long counter = 0, evals=0;
  private boolean bootStrap1Ready = false;
  private boolean bootStrap2Ready = false;

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
   * @param _step the length of the step
   * @param _state the target array
   * @return same as _state
   */
  abstract protected double[] computeIntermediateStep(double _step, double[] _state);

  /**
   * Computes finalState[] and finalTime out of the current initialState[] and initialTime,
   * taking into account the stepSize and the tolerance (which ever applies).
   * Not final because Adaptive solvers make it otherwise.
   */
  protected void computeOneStep() {
    computeIntermediateStep(stepSize,finalState);
    ode.getRate(finalState, finalRate);
    finalTime = initialTime + stepSize;
    counter += evals;
  }

  final public ODE getODE() { return this.ode; }

  final public void initialize(double _stepSize) {
    this.stepSize = _stepSize;
    double[] ode_state = ode.getState();
    if (initialState==null || (initialState.length != ode_state.length)) {
      dimension = ode_state.length;
      timeIndex = dimension-1;
      initialState = new double[dimension];
      initialRate  = new double[dimension];
      finalState = new double[dimension];
      finalRate  = new double[dimension];
      allocateOtherArrays();
    }
    counter = 0;
    evals = getNumberOfEvaluations();
    reinitialize(ode_state);
  }

  public void reinitialize(double[] _state) {
    initialTime = _state[timeIndex];
    System.arraycopy(_state, 0, initialState, 0, dimension);
    ode.getRate(initialState, initialRate);
    finalTime = Double.NaN;
    error_code=ODEAdaptiveSolver.NO_ERROR;
    bootStrap1Ready = bootStrap2Ready = false;
  }

  public double[] getCurrentRate() { return initialRate; }

  public void setEstimateFirstStep(boolean _estimate) {}

  final public void setStepSize(double stepSize) {
    this.stepSize = stepSize;
  }

  public void setMaximumStepSize(double _stepSize) {
    this.maximumStepSize = Math.abs(_stepSize);  
  }

  final public double getStepSize() { return this.stepSize; }

  public void setTolerances(double absTol, double relTol) {}

  final public double getMaximumTime() {
    if (error_code!=ODEAdaptiveSolver.NO_ERROR) return Double.NaN;
    if (Double.isNaN(finalTime)) {
      computeOneStep();
      deltaTime = finalTime-initialTime;
      bootStrap1Ready = bootStrap2Ready = false;
    }
    return finalTime; 
  }

  final public long getCounter()  { return counter; }

  final public double internalStep() {
    initialTime = finalTime;
    System.arraycopy(finalState, 0, initialState, 0, dimension);
    System.arraycopy(finalRate,  0, initialRate,  0, dimension);
    error_code=ODEAdaptiveSolver.NO_ERROR;
    computeOneStep();
    bootStrap1Ready = bootStrap2Ready = false;
    deltaTime = finalTime-initialTime;
    return finalTime;  // the final time that was computed
  }

  final public double getInternalStepSize() { return deltaTime; }

  // --------------------------------------------
  // All about interpolation
  // --------------------------------------------

  // Coefficients of the first bootstrap
  static private final double ALPHA = 0.25;
  static private final double bt1_den = ALPHA*(ALPHA-1)*(4*ALPHA-2);
  static private final double bt1_cf1 = ALPHA*(-3*ALPHA+2);
  static private final double bt1_cf0 = ALPHA*(-3*ALPHA+4)-1; 
  static private final double bt1_cys = 6*ALPHA*(ALPHA-1);
  //    static private double bt1_21, bt1_22, bt1_23, bt1_24;
  //    static private double bt1_31, bt1_32, bt1_33, bt1_34;
  //    static private double bt1_41, bt1_42, bt1_43, bt1_44;

  // Coefficients of the second bbootstrap
  static private final double BETA1 = 0.7, BETA2 = 0.85;
  static private final double bt2_den = 2*BETA1*(BETA1-1)*BETA2*(BETA2-1)*(BETA2-BETA1)*(10*BETA1*BETA2-5*BETA2-5*BETA1+3);
  static private final double bt2_cf11 = BETA1*(-3*BETA1+2);
  static private final double bt2_cf01 = BETA1*(-3*BETA1+4)-1; 
  static private final double bt2_cys1 = 6*BETA1*(BETA1-1);
  static private final double bt2_cf12 = BETA2*(-3*BETA2+2);
  static private final double bt2_cf02 = BETA2*(-3*BETA2+4)-1; 
  static private final double bt2_cys2 = 6*BETA2*(BETA2-1);

  static private final double bt2_m44 = BETA1*(2+BETA1*(-6+4*BETA1));
  static private final double bt2_m54 = BETA2*(2+BETA2*(-6+4*BETA2));
  static private final double bt2_m45 = BETA1*(4+BETA1*(-9+5*BETA1*BETA1));
  static private final double bt2_m55 = BETA2*(4+BETA2*(-9+5*BETA2*BETA2));

  //    static {
    //      double AL2 = ALPHA*ALPHA;
  //      double AL3 = AL2*ALPHA;
  //      bt1_21 = (12*AL3 - 12*AL2)/bt1_den; bt1_22 = (-8*AL3 + 9*AL2 - 1.0)  /bt1_den; bt1_23 = (3*AL2 - 4*AL3)  /bt1_den; bt1_24 =  1.0/bt1_den;
  //      bt1_31 = (8*ALPHA - 8*AL3)/bt1_den; bt1_32 = (4*AL3 - 6*ALPHA + 2.0) /bt1_den; bt1_33 = (4*AL3 - 2*ALPHA)/bt1_den; bt1_34 = -2.0/bt1_den;
  //      bt1_41 = (6*AL2 - 6*ALPHA)/bt1_den; bt1_42 = (-3*AL2 + 4*ALPHA - 1.0)/bt1_den; bt1_43 = (2*ALPHA - 3*AL2)/bt1_den; bt1_44 =  1.0/bt1_den;
  //    }

  private double[] state_bt1 = null, rate_bt1, bt1_c2, bt1_c3, bt1_c4;
  private double[] state_bt2 = null, rate_bt21, rate_bt22, bt2_c2, bt2_c3, bt2_c4, bt2_c5;

  /**
   * Provides Hermite interpolation. This is a 3rd order interpolation.
   * @param _time
   * @param _state
   * @return
   */
  protected double[] interpolateHermite(double _time, double[] _state) {
    hermite((_time-initialTime)/deltaTime,_state);
    _state[timeIndex] = _time;
    return _state;
  }

  private double[] hermite(double _theta, double[] _state) {
    double minus1 = _theta-1;
    double prod1 = _theta*minus1;
    double prod2 = prod1*(1-2*_theta);
    double coefX0 = -minus1 - prod2;
    double coefX1 = _theta + prod2;
    double coefF0 = prod1*minus1*deltaTime;
    double coefF1 = prod1*_theta*deltaTime;
    for (int i=0; i<timeIndex; i++) {
      _state[i] = coefX0*initialState[i] + coefX1*finalState[i] + coefF0*initialRate[i] + coefF1*finalRate[i];
    }
    return _state;
  }

  /**
   * Prepares the first bootstrap
   */
  private void prepareFirstBootstrap() {
    if (state_bt1==null) {
      state_bt1 = new double[dimension];
      rate_bt1 = new double[dimension];
      bt1_c2 = new double[dimension];
      bt1_c3 = new double[dimension];
      bt1_c4 = new double[dimension];
    }
    hermite(ALPHA,state_bt1);
    state_bt1[timeIndex] = initialTime + ALPHA*deltaTime;
    ode.getRate(state_bt1, rate_bt1);
    for (int i=0; i<timeIndex; i++) {
      double dif = finalState[i]-initialState[i], f0 = deltaTime*initialRate[i], f1 = deltaTime*finalRate[i];
      double c4 = (deltaTime*rate_bt1[i] + bt1_cf1*f1 + bt1_cf0*f0 + bt1_cys*dif)/bt1_den;
      double c3 = f1 + f0 - 2*dif - 2*c4;
      bt1_c4[i] = c4;
      bt1_c3[i] = c3;
      bt1_c2[i] = dif - f0 - c3 - c4;
      //        double rbt = deltaTime*rate_bt1[i];
      //        bt1_c2[i] = bt1_21*dif + bt1_22*f0 + bt1_23*f1 + bt1_24*rbt;
      //        bt1_c3[i] = bt1_31*dif + bt1_32*f0 + bt1_33*f1 + bt1_34*rbt;
      //        bt1_c4[i] = bt1_41*dif + bt1_42*f0 + bt1_43*f1 + bt1_44*rbt;
    }
    bootStrap1Ready = true;
  }

  /**
   * Provides the first bootstrap step based on Hermite interpolation. 
   * This is a 4th order interpolation.
   * @param _time
   * @param _state
   * @return
   */
  protected double[] interpolateBootstrap1(double _time, double[] _state) {
    bootstrap1((_time-initialTime)/deltaTime,_state);
    _state[timeIndex] = _time;
    return _state;
  }

  private double[] bootstrap1(double _step, double[] _state) {
    if (!bootStrap1Ready) prepareFirstBootstrap();
    for (int i=0; i<timeIndex; i++) {
      _state[i] = initialState[i] + _step*(deltaTime*initialRate[i] + _step*(bt1_c2[i] + _step*(bt1_c3[i] + _step*bt1_c4[i])));
    }
    return _state;
  }

  /**
   * Prepares the second bootstrap
   */
  private void prepareSecondBootstrap() {
    if (state_bt2==null) {
      state_bt2 = new double[dimension];
      rate_bt21 = new double[dimension];
      rate_bt22 = new double[dimension];
      bt2_c2 = new double[dimension];
      bt2_c3 = new double[dimension];
      bt2_c4 = new double[dimension];
      bt2_c5 = new double[dimension];
    }
    bootstrap1(BETA1,state_bt2);
    state_bt2[timeIndex] = initialTime + BETA1*deltaTime;
    ode.getRate(state_bt2, rate_bt21);
    bootstrap1(BETA2,state_bt2);
    state_bt2[timeIndex] = initialTime + BETA2*deltaTime;
    ode.getRate(state_bt2, rate_bt22);

    for (int i=0; i<timeIndex; i++) {
      double dif = finalState[i]-initialState[i], f0 = deltaTime*initialRate[i], f1 = deltaTime*finalRate[i];
      double e1 = deltaTime*rate_bt21[i] + bt2_cf11*f1 + bt2_cf01*f0 + bt2_cys1*dif;
      double e2 = deltaTime*rate_bt22[i] + bt2_cf12*f1 + bt2_cf02*f0 + bt2_cys2*dif;
      double c4 = (bt2_m55*e1 - bt2_m45*e2)/bt2_den;
      double c5 = (bt2_m44*e2 - bt2_m54*e1)/bt2_den;
      double c3 = f1 + f0 - 2*dif - 2*c4 - 3*c5;
      bt2_c5[i] = c5;
      bt2_c4[i] = c4;
      bt2_c3[i] = c3;
      bt2_c2[i] = dif - f0 - c3 - c4 - c5;
    }
    bootStrap2Ready = true;
  }

  /**
   * Provides the second bootstrap step based on Hermite interpolation. 
   * This is a 5th order interpolation.
   * @param _time
   * @param _state
   * @return
   */
  protected double[] interpolateBootstrap2(double _time, double[] _state) {
    bootstrap2((_time-initialTime)/deltaTime,_state);
    _state[timeIndex] = _time;
    return _state;
  }

  private double[] bootstrap2(double _step, double[] _state) {
    if (!bootStrap2Ready) prepareSecondBootstrap();
    for (int i=0; i<timeIndex; i++) {
      _state[i] = initialState[i] + _step*(deltaTime*initialRate[i] + _step*(bt2_c2[i] + _step*(bt2_c3[i] + _step*(bt2_c4[i]+_step*bt2_c5[i]))));
    }
    return _state;
  }

  public void setMemoryLength(double length) {} // Does nothing in this class

  // No memory state for this class
  public org.opensourcephysics.numerics.dde_solvers.interpolation.StateMemory getStateMemory() {
    return null;
  }

  /*
   * Default interpolation
   */
  abstract public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state);
  //      return bestInterpolate(_time,_state);  
  //    }

  // Inneficient, but these solvers do not support past states
  public double[] interpolate(double _time, boolean _useLeftApproximation, double[] _state, int _beginIndex, int _length) {
    double[] fullState = interpolate(_time,_useLeftApproximation,new double[dimension]);
    System.arraycopy(fullState, _beginIndex, _state, 0, _length);
    return _state;
  }

  // Inneficient, but these solvers do not support past states
  public double interpolate(double _time, boolean _useLeftApproximation, int _index) {
    return interpolate(_time,_useLeftApproximation,new double[dimension])[_index];
  }

  /*
   * Provides 'brute-force' interpolation by re-stepping from the initial time every time
   */
  public double[] bestInterpolate(double _time, double[] _state) {
    if (Double.isNaN(finalTime)) return null;
    if (_time==finalTime) {
      System.arraycopy(finalState, 0, _state, 0, dimension);
      return _state;
    }
    if (_time==initialTime) {
      System.arraycopy(initialState, 0, _state, 0, dimension);
      return _state;
    }
    return computeIntermediateStep(_time-initialTime, _state);
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
