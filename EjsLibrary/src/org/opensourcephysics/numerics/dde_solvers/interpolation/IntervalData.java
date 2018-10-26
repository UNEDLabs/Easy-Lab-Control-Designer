/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

/**
 * An IntervalData keeps information of an ODE state in an interval [left, right]
 * and uses it to interpolate the state in that interval. 
 * The left value cannot be changed, the right value can be adjusted in case the solver finds a discontinuity. 
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */

public abstract class IntervalData {

  protected double mLeft;
  protected double mRight;
  protected boolean mEndsAtDiscontinuity=false;
  
  /**
   * Returns the left side of the interval
   * @return
   */
	final public double getLeft() { return mLeft; } 
	
	/**
	 * Returns the right side of the interval
	 * @return
	 */
	final public double getRight() { return mRight; }
		
	/**
	 * Changes the right side of the interval
	 * @param rightSide
	 */
	public void setEndsAtDiscontinuity(double right) {
	  mEndsAtDiscontinuity = true;
	  mRight = right; 
	}
	
	/**
	 * Whether the interval ends at a discontinuity of the solution
	 * @return
	 */
	public boolean endsAtDiscontinuity() { 
	  return mEndsAtDiscontinuity; 
	}
		
	/**
	 * Returns the interpolation of one index of the state at the given time
	 * @param time the time for the interpolation
	 * @returns the interpolated state 
	 */
	abstract public double interpolate(double time, int index);
	
	/**
	 * Returns the interpolation of the complete state at the given time
	 * @param time the time for the interpolation
	 * @param state a placeholder for the returned state
	 * @returns the interpolated state (same as the passed state array)
	 */
	final public double[] interpolate(double time, double[] state) {
	  int timeIndex = state.length-1;
	  interpolate(time, state, 0, timeIndex);
	  state[timeIndex] = time;
	  return state;
	}

	/**
	 * Returns the interpolation of the state at the given time only for the given indexes
	 * @param time the time for the interpolation
	 * @param state a placeholder for the returned state
	 * @returns the interpolated state (same as the passed state array)
	 */
	abstract public double[] interpolate(double time, double[] state, int beginIndex, int length);

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
