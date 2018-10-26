/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.rk;

import org.opensourcephysics.numerics.dde_solvers.AbstractDiscreteTimeSolverInterpolator;
import org.opensourcephysics.numerics.dde_solvers.interpolation.*;

/**
 * Title:        Euler
 * Description:  1st order Euler solver
 * @author Francisco Esquembre
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class Euler extends AbstractDiscreteTimeSolverInterpolator {
  
	public Euler(org.opensourcephysics.numerics.ODE ode) {
		super(ode);
	}
	
	@Override
	protected int getNumberOfEvaluations() { return 1; }
	
	@Override
	protected void allocateOtherArrays() {}
	
	@Override
	protected double[] computeIntermediateStep(double step, double[] state) {
	  for (int i = 0; i<mDimension; i++) state[i] = mInitialState[i] + step*mInitialRate[i];
	  return state;
	}

  @Override
  protected IntervalData computeFinalRateAndCreateIntervalData() {
    mWrapper.evaluateRate(mFinalState, mFinalRate);
    return new EulerIntervalData(mInitialState, mInitialRate,mFinalState[mTimeIndex]);
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
