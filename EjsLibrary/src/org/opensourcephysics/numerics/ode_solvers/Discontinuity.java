/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

/**
 * A Discontinuity for an ODE is a point in time that the ODE should never cross without explicitly telling the solver.
 * It typically signals a discontinuity in the function that defines the ODE.
 * A discontinuity is detected numerically by a given double value crossing zero.
 * In crossing from positive to negative, the value of 0 is NOT allowed for the solver to reach.
 * In crossing from negative to positive, the value of 0 IS allowed for the solver to reach.
 *  
 * @author Francisco Esquembre
 * @version 1.0 April 2012
 */
public interface Discontinuity {

  /**
   * Returns the value f(t) for a given state of the model.
   * The values returned by this method are used by the solver
   * to find the exact time at which the discontinuity takes place.
   * The event must take place when crossing from positive to negative (in which case, the value of 0 is not allowed),
   * or from negative to positive (in which case, 0 is allowed).
   * @param state The current state of the ODE
   * @return the value for this state
   */
  public double evaluate(double[] state);

  /**
   * What to do when the event has taken place.
   * @return true if the solver should return at this instant of time,
   * false otherwise.
   */
  public boolean action();

  /**
   * Whether the solver should reinitialize when the discontinuity is reached
   * @return true if the solver should reinitialize at the discontinuity
   * false otherwise.
   */
//  public boolean shouldReinitialize();

  /**
   * Returns the tolerance for locating the discontinuity.
   * @return the tolerance
   */
  public double getTolerance();

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
