/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

/**
 * A StateEvent consists in that a given double value (which depends on the ODE state) 
 * crosses zero (from any direction in croossing events, from positive to negative in the other cases). 
 * In crossing and positive events, zeros must be appropriately separated for the solver to find them. 
 * Hence, crossing and positive events are NOT appropriated for simultaneous events (an event action 
 * triggers another event which was at zero). For this cases use a state event.
 * 
 * The user should take care to place the solution in 
 * an appropriated state in the action of the event. The exact ZERO value can still be used when crossing from
 * positive to negative, but not in the other direction.
 * *
 * @author Francisco Esquembre
 * @version 1.0 November 2007
 */
public interface StateEvent {
  static final public int STATE_EVENT = 0;
  static final public int POSITIVE_EVENT = 1;
  static final public int CROSSING_EVENT = 2;
  
  /**
   * Root finding method using bisection
   */
  static final public int BISECTION = 0;
  /**
   * Root finding method using a line
   */
  static final public int SECANT    = 1;
  
  /**
   * Returns the type of event. One of:
   * <ul>
   *   <li> STATE_EVENT : same as StateEvent, i.e. an event that assumes the state always provides a positive evaluation of f.
   *   <li> POSITIVE_EVENT : A positive event is an event in which the event consist in that the function
   * crosses zero from positive to negative. 
   *   <li> CROSSING_EVENT : A crossing event is an event in which the event consist in that the function
   * crosses zero in ANY direction. 
   * </ul>
   * Except on plain STATE_EVENTs, negative values of the function are considered legal states. This implies that zeroes must be appropriately 
   * separated (by positive or negative states) for the solver to find them. 
   * Hence, only STATE_EVENTs are appropriated for simultaneous events (an event action triggers another event which was at zero).
   * @return
   */
  public int getTypeOfEvent();
  
  /**
   * Returns the value f(t) for a given state of the model.
   * The values returned by this method will be used by the solver
   * to find the exact time at which the event took place.
   * @param state The current state of the ODE
   * @return the value for this state
   */
  public double evaluate(double[] state);

  /**
   * What to do when the event has taken place.
   * The return value tells the solver whether it should stop the
   * computation at the exact moment of the event or continue
   * solving the ODE for the rest of the prescribed dt.
   * @return true if the solver should return at this instant of time,
   * false otherwise.
   */
  public boolean action();

  /**
   * Returns the tolerance for the event.
   * @return the tolerance
   */
  public double getTolerance();

  /**
   * Returns the method used to locate the exact point of the event. One of:
   * <ul>
   *   <li> BISECTION : Proposes the point at the center of the checked interval
   *   <li> SECANT : Proposes the point of intersection of a line with the axis
   * </ul>
   * @return
   */
  public int getRootFindingMethod();
  
  /**
   * Returns the maximum number of iterations allowed when locating the event.
   * @return
   */
  public int getMaxIterations();

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
