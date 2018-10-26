/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

import org.opensourcephysics.numerics.DDE;

/**
 * An IntervalData that return the initial conditions of a DDE
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version March 2011
 */

public class InitialConditionData extends IntervalData {

  protected DDE mDDE;
  protected double[] mState;
  
  public InitialConditionData(DDE dde) {
    mDDE = dde;
    double[] state = dde.getState();
    mState = new double[state.length];
  }
  
	/**
	 * Returns the interpolation of one index of the state at the given time
	 * @param time the time for the interpolation
	 * @returns the interpolated state 
	 */
	public double interpolate(double time, int index) {
    mDDE.getInitialCondition(time, mState);
    return mState[index];
	}
	
	/**
	 * Returns the interpolation of the state at the given time only for the given indexes
	 * @param time the time for the interpolation
	 * @param state a placeholder for the returned state
	 * @returns the interpolated state (same as the passed state array)
	 */
	public double[] interpolate(double time, double[] state, int beginIndex, int length) {
	  mDDE.getInitialCondition(time, mState);
	  System.arraycopy(mState, beginIndex, state, 0, length);
	  return state;
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
