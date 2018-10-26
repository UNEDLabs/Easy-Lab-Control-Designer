package org.colos.ejs.model_elements.apache_numerics;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;

/**
 * Encapsulates access to a UnivariateRealFunction
 * @author Francisco Esquembre
 * @version 1.0, March 2012
 *
 */
public class DifferentiableFunction extends RealFunction {
  static public final String SOLVER_NEWTON = "Newton";

  private DifferentiableUnivariateFunction mDiffFunction;
  private BaseUnivariateSolver<DifferentiableUnivariateFunction> mDiffSolver;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public DifferentiableFunction(DifferentiableUnivariateFunction function) {
    super(function);
    this.mDiffFunction = function;
    this.mDiffSolver = null;
  }

  /**
   * Sets the root finding method
   * @param solverMethod
   * @return true is supported, false otherwise
   */
  public boolean setSolver(String solverMethod) {
    mDiffSolver = null;
    if (SOLVER_NEWTON.equals(solverMethod)) mDiffSolver = new NewtonSolver();

    if (mDiffSolver!=null) {
      setSolverAccuracies();
      return true;
    }
    return super.setSolver(solverMethod);
  }

  /**
   * Sets the solver accuracies... yes, there should be a better way of doing this!
   * @param solverMethod
   */
  @Override
  protected void setSolverAccuracies() {
    if (mDiffSolver!=null) { 
      if (mDiffSolver instanceof NewtonSolver) mDiffSolver = new NewtonSolver(mAbsoluteAccuracy);
    }
    else super.setSolverAccuracies();
  }

  /**
   * Sets the root finding solver
   * @param solver UnivariateRealSolver
   */
  public void setDiffSolver(BaseUnivariateSolver<DifferentiableUnivariateFunction> solver) { mDiffSolver = solver; }
  
  /**
   * Returns the root finder solver
   * @return
   */
  public BaseUnivariateSolver<DifferentiableUnivariateFunction> getDiffSolver() { return mDiffSolver; }
  
  /**
   * Evaluates the derivative of the function at the argument
   * @return Double.NaN if failed
   */
  public double derivative(double x) {
    try { return mDiffFunction.derivative().value(x); }
    catch (Exception exc) {
      exc.printStackTrace();
      return Double.NaN;
    }
  }

  /**
   * Solves for a zero root in the given interval
   * @return Double.NaN if failed
   */
  public double solve(int maxEvals, double min, double max) {
    if (mDiffSolver!=null) {
      try { return mDiffSolver.solve(maxEvals,mDiffFunction,min,max); }
      catch (Exception exc) {
        exc.printStackTrace();
        return Double.NaN;
      }
    }
    return super.solve(maxEvals, min, max);
  }

  /**
   * Solves for a zero root in the given interval with the given start value
   * @return Double.NaN if failed
   */
  public double solve(int maxEvals, double min, double max, double start) {
    if (mDiffSolver!=null) {
      try { return mDiffSolver.solve(maxEvals,mDiffFunction,min,max,start); }
      catch (Exception exc) {
        exc.printStackTrace();
        return Double.NaN;
      }
    }
    return super.solve(maxEvals, min, max,start);
  }

  /**
   * Solves for a zero root in the vicinity of the start value
   * @return Double.NaN if failed
   */
  public double solve(int maxEvals, double start) {
    if (mDiffSolver!=null) {
      try { return mDiffSolver.solve(maxEvals,mDiffFunction,start); }
      catch (Exception exc) {
        exc.printStackTrace();
        return Double.NaN;
      }
    }
    return super.solve(maxEvals, start);
  }

}
