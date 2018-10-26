/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_interpolation.StateHistory;

/**
 * SolverEngine is an interface for objects which solve an ODE at its own internal step 
 * and use an internal memory to keep a set of internal states that are used to interpolate values of 
 * the ODE solution to create a so-called dense output.
 * 
 * Typically, this scheme is used with adaptive solvers that can step much
 * further than the prescribed step size set by the user without loosing 
 * precision. But also for fixed-step methods whose state can be read more 
 * frequently than it is computed.
 * 
 * An SolverEngine can also keep a memory of the states in past values of time.
 * This memory can be used to retrieve those past values when needed. For instance,
 * when solving Delay Differential Equations, or when plotting values already computed. 
 * 
 * SolverEngines are not to be used directly, but in the context of an InterpolatorEventSolver.
 * Methods in this interface are not to be used directly, but through the InterpolatorEventSolver.
 *  
 * @author Francisco Esquembre
 * @version 1.0 November 2007
 * @version 2.0 February 2011
 * @version 3.0 December 2013
 *
 * @see InterpolatorEventSolver
 */
public interface SolverEngine {

  /**
   * Sets the eventSolver and the ODE
   * Not to be used directly, but by the InterpolatorEventSolver object,
   * @return
   */
  public void setODE(InterpolatorEventSolver eventSolver, ODE ode);

  /**
   * Initializes the interpolator and clears the memory.
   * Implementing classes typically use this method to declare arrays, 
   * initialize the underlying Solver, set its step size, and call 
   * reinitialize().
   */
  public void initialize(double stepSize);

  /**
   * Does the minimum (soft) initialization of the solver to a given state. 
   * The memory is preserved.
   * Users of a SolverInterpolator MUST call reinitialize (or the harder
   * initialize) whenever they change the state.
   * Typically, solvers synchronize their internal states and auxiliary
   * variables using the provided state.
   * @param state double[]
   */
  public void reinitialize(double[] state);
  
  /**
   * Returns the rate of the current state
   * @return
   */
  public double[] getCurrentRate();

  /**
   * Changes the internal step size. This is the step at which internal steps are computed
   * for fixed step methods, and the initial step size (after reinitialize()) for variable
   * step methods.
   * @param stepSize
   */
  public void setStepSize(double stepSize);

  /**
   * Sets a maximum step size for variable step solvers. Has no effect on fixed-step solvers.
   * @param stepSize
   */
  public void setMaximumStepSize(double stepSize);
  
  /**
   * Returns the current step size
   * @return
   */
  public double getStepSize();

  /**
   * Returns the actual internal step size
   * @return
   */
  public double getInternalStepSize();
  
  /**
   * Makes adaptive steps estimate the best initial step after reinitialize().
   * If false, the given initial step (as set by setStepSize()) is used.
   * @param estimate
   */
  public void setEstimateFirstStep(boolean estimate);

  /**
   * The preferred absolute and relative tolerance desired for the solution if the 
   * underlying solver supports it. If the solver does not support this feature, the 
   * method is ignored. 
   * Changing the tolerances typically involves a re-computation of the current step.
   * @param tol
   */
  public void setTolerances(double absTol, double relTol);

  /**
   * Returns the maximum time forward in time (or minimum, if the step is negative)
   * for which the solver can interpolate without doing any internal step.
   * @param withDiscontinuities a flag that indicates if there may be discontinuities present
   * @return double The maximum advance time or NaN if there was any error.
   */
  public double getMaximumTime(boolean withDiscontinuities);

  /**
   * Steps the internal step as much as possible (respecting the step size 
   * and the tolerance, if any).
   * @param withDiscontinuities a flag that indicates if there may be discontinuities present
   * @return double Same as getMaximumTime()
   */
  public double internalStep(boolean withDiscontinuities);

  /**
   * Returns the number of function evaluations used since last initialize()
   * @return
   */
  public long getCounter();
  
  /**
   * Provides access to the internal StateHistory responsible for interpolations.
   * @return
   */
  public StateHistory getStateHistory();
  
  /**
   * Returns the value of the ODE's state[] at the given time in the provided state[] array. 
   * The time must be in the range of the history or between the current time and getMaximumTime() for correct operation.
   * @param time the time for the interpolation desired
   * @param state placeholder for the returned state  
   * @return The state provided as argument or null if there is no interpolation available at this time.
   */
  public double[] interpolate(double time, double[] state);
  
  /**
   * Similar to interpolate(), but using the best estimate the algorithm can provide and only between the current time and getMaximumTime().
   * In some RK schemes, this results in computing the RK algorithm from the initial
   * point. Therefore, this method typically involves a bigger computational load.
   * @return The state provided as argument or null if there was any previous 
   * error.
   */
  public double[] bestInterpolate(double time, double[] state);

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
