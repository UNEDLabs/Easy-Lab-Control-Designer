package org.opensourcephysics.numerics.dde_solvers.rk.irk;

import org.opensourcephysics.numerics.DDE;
import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.dde_solvers.interpolation.IntervalData;
import org.opensourcephysics.numerics.dde_solvers.interpolation.StateMemory;

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
 */
public class Radau5Light {
  static private final int mMaxNewtonIterations = 7;

  protected double mInitialStepSize=1e-6;
  protected double mActualStepSize = 1e-6;
  protected double mMaximumStepSize = Double.POSITIVE_INFINITY;
  protected int mDimension;
  protected int mJacobianAge = 0;
  protected double [] mState;
  protected double [] mRate;
  protected double [][] mIntermediateStagesIncrement;

  protected AlgebraicEquationSimpleSolver mAeSolver;

  protected ODE mODE;
  protected Wrapper mWrapper;

  // DDE variables
  protected DDE mDDE;

  // Memory
  protected StateMemory mStateMemory;
  protected double mStateMemoryLength;

  public Radau5Light(ODE ode) {
    mODE = ode;
    double[] ode_state = mODE.getState();
    mDimension = ode_state.length;
    if (mODE instanceof DDE) {
      mDDE = (DDE) ode;
      mStateMemoryLength = Math.abs(mDDE.getMaximumDelay());
      mWrapper = new WrapperDDE(mDDE,this,ode_state);
    }
    else {
      mDDE = null;
      mStateMemoryLength = 0;
      mWrapper = new WrapperODE(mODE);
    }
    mStateMemory = new StateMemory(mODE);
  }

  // ------------------------------------------------
  // Part of the implementation of ODESolverInterpolator
  //------------------------------------------------

  final public ODE getODE() { return this.mODE; }

  final public long getCounter() { return -1; }

  protected void allocateArrays(int n) { // Paco
    mState = new double[n];
    mRate  = new double[n];
    mIntermediateStagesIncrement = new double[3][n];
  }

  final public void setStepSize(double stepSize) {
    this.mInitialStepSize = stepSize;
  }

  public void setMaximumStepSize(double stepSize) {
    this.mMaximumStepSize = Math.abs(stepSize);  
  }

  /**
   * Makes sure the intended step does not exceed the maximum step
   * @param intendedStep
   * @return
   */
  final protected double limitStepSize(double intendedStep) {
    if (intendedStep>=0) return Math.min(intendedStep, mMaximumStepSize);
    return Math.max(intendedStep, -mMaximumStepSize);
  }

  final public double getStepSize() {
    return this.mInitialStepSize; 
  }

  final public void initialize(double _stepSize) { // Paco
    mInitialStepSize = _stepSize;
    mActualStepSize = limitStepSize(mInitialStepSize);
    double[] ode_state = mODE.getState();
    if (mState==null || (mState.length != ode_state.length)) {
      mDimension = ode_state.length;
      allocateArrays(mDimension);
    }
    mAeSolver = getInnerSolver(new DifferenceSchemeEquation(mDimension));
    reinitialize(ode_state);
  }

  public void reinitialize(double[] _state) { // Paco
    mActualStepSize = limitStepSize(mInitialStepSize);
    System.arraycopy(_state, 0, mState, 0, mDimension);
    if (mDDE!=null) mWrapper.prepareStep(mState[mDimension-1]+mInitialStepSize/2, mDDE.getDelays(mState));
    mWrapper.evaluateRate(mState, mRate);
    mAeSolver.restart(false);
    //  Is this better???    mAeSolver.updateInitialValue();
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
          return mIntermediateStagesIncrement;
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
          return 1.0 / mActualStepSize;
        }

        /**
         * Evaluates the non linear component in the systems of algebraic equations
         * result depends on current state of ODE therefore equation on each ODE solver step
         * is differ
         * @param freeVariable the arguments vector
         * @param functionValue the result function vector
         */
        public void evaluateNonLinearComponent(double[] freeVariable, double[] functionValue) {
          for (int i = 0; i < mDimension; i++)
            someState[i] = mState[i] + freeVariable[i];
          if (mDDE!=null) mWrapper.prepareStep(someState[mDimension-1], mDDE.getDelays(someState)); // hace falta?
          mWrapper.evaluateRate(someState, functionValue);
        }

        /**
         * Evaluates the jacobian for the algebraic equations systems solver. Jacobian matrix
         * estimates for the moments of the model time concuiding the steps, even in case if
         * the delta within the step size persists
         * @param freeVariable the arguments vector
         * @param jacobian the result jacobian matrix
         */
        public void evaluateNonLinearComponentJacobian(double[] freeVariable, double[][] jacobian) {
          System.arraycopy(mState, 0, someState, 0, mDimension);
          for(int i = 0; i < mDimension; i++) {
            double delta = Math.sqrt(uRound * Math.max(1.0e-5, Math.abs(mState[i])));
            someState[i] += delta;
            if (mDDE!=null) mWrapper.prepareStep(someState[mDimension-1], mDDE.getDelays(someState)); // hace falta?
            mWrapper.evaluateRate(someState, someRate);
            someState[i] -= delta;
            for(int j = 0; j < mDimension; j++) {
              jacobian[j][i] = (someRate[j] - mRate[j]) / delta;
            }
          }
          mJacobianAge = 0;
        }

        public void directChangeOfVariables(double[][] freeVariable, double[][] substitutedVariable) {
          for (int k = 0; k < mDimension; k ++)
            for (int i = 0; i < nStgs; i++){
              substitutedVariable[i][k] = 0;
              for (int j = 0; j < nStgs; j++)
                substitutedVariable[i][k] += inverseT[i][j]*freeVariable[j][k];
            }
        }

        public void inverseChangeOfVariables(double[][] substitutedVariable, double[][] freeVariable) {
          for (int k = 0; k < mDimension; k ++)
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
    if (mDDE!=null) mWrapper.prepareStep(mState[mDimension-1]+mInitialStepSize/2, mDDE.getDelays(mState));
    mWrapper.evaluateRate(mState, mRate);
  }

  /**
   * Calculates the increment to the intermediate stages of difference
   * scheme as first approximation obtained by one iteration of the
   * simplified Newton solver.
   */
  protected void performStep(){
    double convergenceRate = 0;
    for (int i = 0; i < mMaxNewtonIterations; i++)
      convergenceRate += mAeSolver.resolve() / mMaxNewtonIterations;
    // TODO: 0.001 is a parameter
    mAeSolver.restart((convergenceRate > 0.001));
  }

  /**
   * Posts the results after the iteration step
   */
  protected void commitStepResults(){
    for (int i = 0; i < mDimension; i++)
      mState[i] += mIntermediateStagesIncrement[2][i];
  }

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

  public double[] bestInterpolate(double _time, double[] _state) {
    return interpolate(_time,true,_state);  
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
    private Radau5Light mSolver;
    private IntervalData[] mIntervals;

    public WrapperDDE(DDE dde, Radau5Light solver, double[] state) {
      mDDE = dde;
      mSolver = solver;
      mIntervals = new IntervalData[dde.getDelays(state).length];
    }

    public void prepareStep (double time, double[] delays) {
      //        if (delays.length!=mIntervals.length) mIntervals = new IntervalData[delays.length];
      for (int i=0; i<delays.length; i++) mIntervals[i] = mSolver.mStateMemory.findInterval(time-delays[i], false);
    }

    public void evaluateRate(double[] state, double[] rate) {
      mDDE.getRate(state, mIntervals,rate);
    }

  }


}
