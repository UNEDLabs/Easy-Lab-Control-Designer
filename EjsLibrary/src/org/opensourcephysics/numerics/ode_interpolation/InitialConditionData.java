/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

import org.opensourcephysics.numerics.ode_solvers.DelayDifferentialEquation;

/**
 * An IntervalData that return the initial conditions of a DDE
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version March 2011
 */

public class InitialConditionData extends IntervalData {

  private DelayDifferentialEquation mDDE;
  private double[] mState;
  
  public InitialConditionData(DelayDifferentialEquation dde) {
    super(Double.NaN,Double.NaN);
    double[] state = dde.getState();
    mDDE = dde;
    mState = new double[state.length];
  }

  @Override
	public double interpolate(double time, int index) {
    mDDE.getInitialCondition(time, mState);
    return mState[index];
	}
	
  @Override
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
