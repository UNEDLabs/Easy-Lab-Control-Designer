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
 * DelayDifferentialEquation defines a system of delay differential equations
 *
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * 
 */
public interface DelayDifferentialEquation extends ODE {

  /**
   * Sets the StateHistory object.
   * A Delay Differential Equation needs a way to obtain the state at past values of time.
   * A StateHistory is an object that does precisely this.
   * DDEs can retrieve the past state of any state variable at the ith-delay using
   * memory.interpolate(time,i);
   * or parts of or even the whole state at once (more efficient if many past values are required) using
   * memory.interpolate(time,pastState);
   * or
   * memory.interpolate(time,pastState, int beginIndex, int length);
   * @param memory
   */
  public void setStateHistory(StateHistory history);
  
	/**
	 * The largest possible delay. This value must be constant unless you initialize() the solver.
	 * In case the delays array is not constant, the maximum delay should be large enough to cover all possible delays.
	 * A solver will use this value to keep the state memory for at least this length back in time.
	 * @return
	 */
	public double getMaximumDelay();
	
	/**
	 * Returns the array of delays for a given state. 
	 * The number of delays, i.e. the length of this array must be constant, unless you initialize() the solver. 
	 * @return
	 */
	public double[] getDelays(double[] state);
  
	/**
	 * Returns the initial condition for an interval backwards in time longer than or equal to the maximum delay  
	 * @param time the given time back in time
	 * @param state a place holder for the state
	 */
	public void getInitialCondition(double time, double state[]);
	
	/**
	 * Returns an array with possible discontinuities of the initial condition. 
	 * (i.e. where the initial state is not C-k continuous)
	 * @return null or zero-length array if there are no such discontinuities
	 */
	public double[] getInitialConditionDiscontinuities();
	
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
