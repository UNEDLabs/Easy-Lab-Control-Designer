/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * GeneralStateEvent is an extension of the older StateEvents which allows for other types 
 * of zero-crossing of the evaluate function, f.
 * An event consists now in that a given numeric value crosses zero (from any direction in croossing
 * events, from positive to negative in the other cases). 
 * In crossing and positive events, zeros must be appropriately separated for the solver to find them. 
 * Hence, crossing and positive events are NOT appropriated for simultaneous events (an event action 
 * triggers another event which was at zero). For this cases use a state event.
 * *
 * @author Francisco Esquembre. November 2007
 */
public interface GeneralStateEvent extends org.opensourcephysics.numerics.StateEvent {
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
   *   <li> POSITIVE_EVENT : A positive event is a StateEvent in which the event consist in that the function
   * crosses zero from positive to negative. 
   *   <li> CROSSING_EVENT : A crossing event is a StateEvent in which the event consist in that the function
   * crosses zero in ANY direction. 
   * </ul>
   * Differently to normal StateEvents, in positive and crossing events, negative values 
   * of the function are considered legal states. This implies that zeroes must be appropriately 
   * separated (by positive or negative states) for the solver to find them. 
   * Hence, positive and crossing events are NOT appropriated for simultaneous events (an event action 
   * triggers another event which was at zero).
   * @return
   */
  public int getTypeOfEvent();
  
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
