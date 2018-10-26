package org.opensourcephysics.numerics.ode_solvers.rk.irk;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_interpolation.StateHistory;
import org.opensourcephysics.numerics.ode_solvers.DelayDifferentialEquation;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver;

/**
 * Numerical solution of a stiff system of first order ordinary differential
 * equations. The solver used is an implicit Runge-Kutta method (Radau IIA).
 * Solver holds persistant step size value assigned by an user. Therefore
 * it can be used only as simplest ODE solver in cases when error is
 * of no importance or as base for the solvers with error control.<br>
 *
 * The code is transferred from the Fortran sources.
 * authors of original Fortran code:
 *    E. Hairer and G. Wanner
 *    Universite de Geneve, Dept. De Mathematiques
 *    ch-1211 Geneve 24, Switzerland
 *    e-mail:  rnst.hairer@math.unige.ch
 *             gerhard.wanner@math.unige.ch <br>
 *
 * original Fortran code is part of the book:
 *    E. Hairer and G. Wanner, Solving ordinary differential
 *    equations II. Stiff and differential-algebraic problems.
 *    Springer series in computational mathematics 14,
 *    Springer-Verlag 1991, second edition 1996.
 * @author Andrei Goussev
 * @author Adapted by Francisco Esquembre January 2009
 * @author Modified by Francisco Esquembre and Maria Jose Cano March 2011
 * @author Adapted by Francisco Esquembre Jan 2014
 */
public class Radau5Light {

  protected ODE ode;

  private double initialStepSize=1e-6;
  protected double actualStepSize = 1e-6;
  protected double maximumStepSize = Double.POSITIVE_INFINITY;
  protected int numEqn;
  protected int jacobianAge = 0;

  protected double [] state;
  protected double [] rate;
  protected double [][] intermediateStagesIncrement;

  protected AlgebraicEquationSimpleSolver aeSolver;
  protected static int maxNewtonIterations = 7;

  // Memory
  protected InterpolatorEventSolver mEventSolver;
  protected StateHistory mStateHistory;
  protected double mStateHistoryLength;

  // --------------------------------------------
  // Initialization
  // --------------------------------------------

  public void setODE(InterpolatorEventSolver eventSolver, ODE _ode) { 
    mEventSolver = eventSolver;
    ode = _ode;
    double[] ode_state = ode.getState();
    numEqn = ode_state.length;
    if (ode instanceof DelayDifferentialEquation) {
      DelayDifferentialEquation dde = (DelayDifferentialEquation) _ode;
      mStateHistoryLength = Math.abs(dde.getMaximumDelay());
    }
    else {
      mStateHistoryLength = 0;
    }
    mStateHistory = new StateHistory(_ode);
  }

  final public long getCounter() { return -1; }

  protected void allocateArrays(int n) { // Paco
    state = new double[n];
    rate  = new double[n];
    intermediateStagesIncrement = new double[3][n];
  }

  final public void setStepSize(double stepSize) {
    this.initialStepSize = stepSize;
  }

  public void setMaximumStepSize(double stepSize) {
    this.maximumStepSize = Math.abs(stepSize);  
  }

  /**
   * Makes sure the intended step does not exceed the maximum step
   * @param intendedStep
   * @return
   */
  final protected double limitStepSize(double intendedStep) {
    if (intendedStep>=0) return Math.min(intendedStep, maximumStepSize);
    return Math.max(intendedStep, -maximumStepSize);
  }

  final public double getStepSize() { return this.initialStepSize; }


  final public void initialize(double _stepSize) { // Paco
    initialStepSize = _stepSize;
    actualStepSize = limitStepSize(initialStepSize);
    double[] ode_state = ode.getState();
    if (state==null || (state.length != ode_state.length)) {
      numEqn = ode_state.length;
      allocateArrays(numEqn);
    }
    aeSolver = getInnerSolver(new DifferenceSchemeEquation(numEqn));
    mStateHistory.clearAll();
    reinitialize(ode_state);
  }

  public void reinitialize(double[] _state) { // Paco
    actualStepSize = limitStepSize(initialStepSize);
    System.arraycopy(_state, 0, state, 0, numEqn);
    ode.getRate(state, rate);
    aeSolver.restart(false);
    //  Is this better???    aeSolver.updateInitialValue();
  }


  final public void setHistoryLength(double length) {
    length = Math.abs(length);
    if (ode instanceof DelayDifferentialEquation) {
      DelayDifferentialEquation dde = (DelayDifferentialEquation) ode;
      mStateHistoryLength = Math.max(length,Math.abs(dde.getMaximumDelay()));
    }
    else mStateHistoryLength = length;
    if (mStateHistoryLength==0) mStateHistory.clearAll();
  }

  final public StateHistory getStateHistory() { return mStateHistory; }

  final public double[] interpolate(double time, double[] state) {
    return mStateHistory.interpolate(time, state);
  }

  final public double[] bestInterpolate(double _time, double[] _state) {
    return interpolate(_time,_state);  
  }

  /**
   * Gets the instance of a algebraic equation solver assigned to the system of algebraic equation
   * @param algEqn the system of algebraic equation
   * @return the instance of a algebraic equation solver
   */
  protected AlgebraicEquationSimpleSolver getInnerSolver(IRKAlgebraicEquation algEqn){
    return new IRKSimplifiedNewtonStep(algEqn);
  }

  /**
   * RadoIIIA difference scheme of the solver can be fitted into the <code>IRKAlgebraicEquation</code>
   * Radau5 solver obtains next increment to the state vector as solution of the
   * <code>DifferenceSchemeEquation</code> equation.
   */
  protected class DifferenceSchemeEquation implements IRKAlgebraicEquation{
    private double [] realEigenvalues = {3.6378342527444962};
    private double [] complexEigenvalues = {2.6810828736277523, 3.0504301992474105};
    private double [][] T = {
        {9.1232394870892942792e-2,-0.14125529502095420843,-3.0029194105147424492e-2},
        {0.24171793270710701896,0.20412935229379993199,0.38294211275726193779},
        {0.96604818261509293619,1,0}
    };
    private double [][] inverseT = {
        {4.3255798900631553510,0.33919925181580986954,0.54177053993587487119},
        {-4.1787185915519047273,-0.32768282076106238708,0.47662355450055045196},
        {-0.50287263494578687595,2.5719269498556054292,-0.59603920482822492497}
    };
    private int nStgs = 3;

    private double [] someState;
    private double [] someRate;
    // TODO: uRound ??? to do something
    double uRound = 2.220446049250313E-16; // (2^52)^(-1)

    /**
     * Constructs the RadoIIA difference scheme equation
     * @param mNumEqn
     */
    public DifferenceSchemeEquation(int numEqn) {
      someState = new double[numEqn];
      someRate = new double[numEqn];

    }

    /**
     * Gets the increment to the current intermediate stages vectors array
     * as approximation to the solution of system of equations.
     * @return array of vectors of increment to the current intermediate stages
     */
    public double[][] getApproximation() {
      return intermediateStagesIncrement;
    }

    public double[] getComplexEigenvalues() {
      return complexEigenvalues;
    }

    public double[] getRealEigenvalues() {
      return realEigenvalues;
    }

    /**
     * Gets the scalar multiplier. For the case of RadoIIA equations inverse of
     * stepSize in role of scalar multiplier value is returned
     * @return the scalar multiplier
     */
    public double getScalarMultiplier() {
      return 1.0 / actualStepSize;
    }

    /**
     * Evaluates the non linear component in the systems of algebraic equations
     * result depends on current state of ODE therefore equation on each ODE solver step
     * is differ
     * @param freeVariable the arguments vector
     * @param functionValue the result function vector
     */
    public void evaluateNonLinearComponent(double[] freeVariable, double[] functionValue) {
      for (int i = 0; i < numEqn; i++)
        someState[i] = state[i] + freeVariable[i];
      ode.getRate(someState, functionValue);
    }

    /**
     * Evaluates the jacobian for the algebraic equations systems solver. Jacobian matrix
     * estimates for the moments of the model time concuiding the steps, even in case if
     * the delta within the step size persists
     * @param freeVariable the arguments vector
     * @param jacobian the result jacobian matrix
     */
    public void evaluateNonLinearComponentJacobian(double[] freeVariable, double[][] jacobian) {
      System.arraycopy(state, 0, someState, 0, numEqn);
      for(int i = 0; i < numEqn; i++) {
        double delta = Math.sqrt(uRound * Math.max(1.0e-5, Math.abs(state[i])));
        someState[i] += delta;
        ode.getRate(someState, someRate);
        someState[i] -= delta;
        for(int j = 0; j < numEqn; j++) {
          jacobian[j][i] = (someRate[j] - rate[j]) / delta;
        }
      }
      jacobianAge = 0;
    }

    public void directChangeOfVariables(double[][] freeVariable, double[][] substitutedVariable) {
      for (int k = 0; k < numEqn; k ++)
        for (int i = 0; i < nStgs; i++){
          substitutedVariable[i][k] = 0;
          for (int j = 0; j < nStgs; j++)
            substitutedVariable[i][k] += inverseT[i][j]*freeVariable[j][k];
        }
    }

    public void inverseChangeOfVariables(double[][] substitutedVariable, double[][] freeVariable) {
      for (int k = 0; k < numEqn; k ++)
        for (int i = 0; i < nStgs; i++){
          freeVariable[i][k] = 0;
          for (int j = 0; j < nStgs; j++)
            freeVariable[i][k] += T[i][j]*substitutedVariable[j][k];
        }
    }
  }

  /**
   * Before the step performing actions
   */
  protected void preStepPreparations(){
    ode.getRate(state, rate);
  }
  /**
   * Calculates the increment to the intermediate stages of difference
   * scheme as first approximation obtained by one iteration of the
   * simplified Newton solver.
   */
  protected void performStep(){
    double convergenceRate = 0;
    for (int i = 0; i < maxNewtonIterations; i++)
      convergenceRate += aeSolver.resolve() / maxNewtonIterations;
    // TODO: 0.001 is a parameter
    aeSolver.restart((convergenceRate > 0.001));
  }

  /**
   * Posts the results after the iteration step
   */
  protected void commitStepResults(){
    for (int i = 0; i < numEqn; i++)
      state[i] += intermediateStagesIncrement[2][i];
  }
  /**
   * Adjusts the ODE state vector on a fixed step size without
   * convergence guarantees
   * @return the taken step size value
   *
    public double doStep() {
        preStepPreparations();
        commitStepResults();
        aeSolver.restart((++nSteps)%4 == 0);
        return actualStepSize;
    }
   */

}
