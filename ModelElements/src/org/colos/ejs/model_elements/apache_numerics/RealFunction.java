package org.colos.ejs.model_elements.apache_numerics;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;
import org.apache.commons.math3.analysis.integration.*;

/**
 * Encapsulates access to a UnivariateRealFunction
 * @author Francisco Esquembre
 * @version 1.0, December 2010
 * @version 2.0, March 2012
 *
 */
public class RealFunction {
  static public final String SOLVER_BISECTION = "Bisection";
  static public final String SOLVER_BRENT_DEKKER = "Brent-Dekker";
  static public final String SOLVER_SECANT = "Secant";
  static public final String SOLVER_MULLER = "Muller";

  static public final String INTEGRATOR_ROMBERG = "Romberg";
  static public final String INTEGRATOR_SIMPSON = "Simpson";
  static public final String INTEGRATOR_TRAPEZOID = "Trapezoid";
  static public final String INTEGRATOR_LEGENDRE_GAUSS = "Legendre-Gauss";

  private UnivariateFunction mFunction;
  private BaseUnivariateSolver<UnivariateFunction> mSolver;
  private UnivariateIntegrator mIntegrator;
  protected double mAbsoluteAccuracy = 1.0e-6;
  protected double mRelativeAccuracy = Double.NaN;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public RealFunction(UnivariateFunction function) {
    this.mFunction = function;
    this.mSolver = new BisectionSolver();
    this.mIntegrator = new RombergIntegrator();
  }

  /**
   * Sets the root finding method
   * @param solverMethod
   * @return true is supported, false otherwise
   */
  public boolean setSolver(String solverMethod) {
    if      (SOLVER_BISECTION.equals(solverMethod))    mSolver = new BisectionSolver();
    else if (SOLVER_BRENT_DEKKER.equals(solverMethod)) mSolver = new BrentSolver();
    else if (SOLVER_SECANT.equals(solverMethod))       mSolver = new SecantSolver();
    else if (SOLVER_MULLER.equals(solverMethod))       mSolver = new MullerSolver();
    else return false;
    setSolverAccuracies ();
    return true;
  }

  /**
   * Sets the mSolver accuracies... yes, there should be a better way of doing this!
   * @param solverMethod
   */
  protected void setSolverAccuracies() {
    if (mSolver instanceof BisectionSolver) {
      if (Double.isNaN(mRelativeAccuracy)) mSolver = new BisectionSolver(mAbsoluteAccuracy);
      else mSolver = new BisectionSolver(mAbsoluteAccuracy,mRelativeAccuracy);
    }
    else if (mSolver instanceof BrentSolver) {
      if (Double.isNaN(mRelativeAccuracy)) mSolver = new BrentSolver(mAbsoluteAccuracy);
      else new BrentSolver(mAbsoluteAccuracy,mRelativeAccuracy);
    }
    else if (mSolver instanceof SecantSolver) {
      if (Double.isNaN(mRelativeAccuracy)) mSolver = new SecantSolver(mAbsoluteAccuracy);
      else new SecantSolver(mAbsoluteAccuracy,mRelativeAccuracy);
    }
    else if (mSolver instanceof MullerSolver) {
      if (Double.isNaN(mRelativeAccuracy)) mSolver = new MullerSolver(mAbsoluteAccuracy);
      else new MullerSolver(mAbsoluteAccuracy,mRelativeAccuracy);
    }
  }

  /**
   * Sets the mSolver absolute accuracy
   * @param accuracy
   */
  public void setSolverAbsoluteAccuracy(double accuracy) {
    if (accuracy!=mAbsoluteAccuracy) {
      mAbsoluteAccuracy = accuracy;
      setSolverAccuracies();
    }
  }

  /**
   * Sets the mSolver relative accuracy
   * @param accuracy
   */
  public void setSolverRelativeAccuracy(double accuracy) {
    if (accuracy!=mRelativeAccuracy) {
      mRelativeAccuracy = accuracy;
      setSolverAccuracies();
    }
  }

  /**
   * Sets the root finding mSolver
   * @param solver UnivariateRealSolver
   */
  public void setSolver(BaseUnivariateSolver<UnivariateFunction> solver) { mSolver = solver; }
  
  /**
   * Returns the root finder mSolver
   * @return
   */
  public BaseUnivariateSolver<UnivariateFunction> getSolver() { return mSolver; }
  
  /**
   * Sets the integration method
   * @param integratorMethod
   * @return true is supported, false otherwise
*/
  public boolean setIntegrator(String integratorMethod)  {
    if      (INTEGRATOR_ROMBERG.equals(integratorMethod))        mIntegrator = new RombergIntegrator();
    else if (INTEGRATOR_SIMPSON.equals(integratorMethod))        mIntegrator = new SimpsonIntegrator();
    else if (INTEGRATOR_TRAPEZOID.equals(integratorMethod))      mIntegrator = new TrapezoidIntegrator();
    else if (INTEGRATOR_LEGENDRE_GAUSS.equals(integratorMethod)) mIntegrator = new LegendreGaussIntegrator(2,10,100);
    else return false;
    return true;
  }

  /**
   * Sets the mIntegrator
   * @param _integrator UnivariateRealIntegrator
   */
  public void setIntegrator(UnivariateIntegrator integrator) { mIntegrator = integrator; }

  /**
   * Returns the mIntegrator
   * @return
   */
  public UnivariateIntegrator getIntegrator() { return this.mIntegrator; }
  
  /**
   * Evaluates the mFunction at the argument
   * @return Double.NaN if failed
   */
  public double value(double x) {
    try { return mFunction.value(x); }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  // Backwards compatibility
  public double integrate(double min, double max) {
    return integrate (Integer.MAX_VALUE,min,max);
  }

  /**
   * Integrates the mFunction in the given interval, allowing for a maximum number of function evaluations
   * @return Double.NaN if failed
   */
  public double integrate(int maxEvals, double min, double max) {
    try { return mIntegrator.integrate(maxEvals,mFunction,min,max); }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  // Backwards compatibility
  public double solve(double min, double max) {
    return solve (Integer.MAX_VALUE,min,max);
  }

  // Backwards compatibility
  public double solve(double min, double max, double start) {
    return solve (Integer.MAX_VALUE,min,max,start);
  }

  // Backwards compatibility (well, not really)
  public double solve(double start) {
    return solve (Integer.MAX_VALUE,start);
  }

  /**
   * Solves for a zero root in the given interval, allowing for a maximum number of function evaluations
   * @return Double.NaN if failed
   */
  public double solve(int maxEvals, double min, double max) {
    try { return mSolver.solve(maxEvals,mFunction,min,max); }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Solves for a zero root in the given interval with the given start value, allowing for a maximum number of function evaluations
   * @return Double.NaN if failed
   */
  public double solve(int maxEvals, double min, double max, double start) {
    try { return mSolver.solve(maxEvals,mFunction,min,max,start); }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Solves for a zero root in the vicinity of the start value, allowing for a maximum number of function evaluations
   * @return Double.NaN if failed
   */
  public double solve(int maxEvals, double start) {
    try { return mSolver.solve(maxEvals,mFunction,start); }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

}
