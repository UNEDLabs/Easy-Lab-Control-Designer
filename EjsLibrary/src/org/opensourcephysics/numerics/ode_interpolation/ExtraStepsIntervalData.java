/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

/**
 * Uses interpolation based on extra evaluations of the ODE
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public abstract class ExtraStepsIntervalData extends IntervalData {
	protected int mTimeIndex;
	protected double mDeltaTime;
	protected double[][] mCoeffs;

	/**
	 * 
	 * @param aState the left state
	 * @param bState the right state
	 * @param coeffs coefficients of the interpolation, previously computed by the algorithm
	 */
	public ExtraStepsIntervalData(double[] aState, double[] bState, double[][] coeffs) {
    super(aState[aState.length-1],bState[bState.length-1]);
	  int dimension = aState.length;
		mTimeIndex = dimension-1;
    mDeltaTime = bState[mTimeIndex]-aState[mTimeIndex];   
		mCoeffs = new double [coeffs.length][dimension];
		for (int i=0; i<coeffs.length; i++) System.arraycopy(coeffs[i],0,mCoeffs[i],0,dimension);
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
