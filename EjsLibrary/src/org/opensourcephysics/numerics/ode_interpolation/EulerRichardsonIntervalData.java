/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

/**
 * Provides third order interpolation for the E-R algorithm
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2010
 */
public class EulerRichardsonIntervalData extends IntervalData {
  private int mTimeIndex;
  private double mStepSize;
  private double[] mLeftState, mLeftRate;
  private double[] mK2; // Extra rate

  public EulerRichardsonIntervalData(double[] aState, double[] aRate, double right, double[] K2) {
    super(aState[aState.length-1],right);
    int dimension = aState.length;
    mLeftState = new double[dimension]; 
    mLeftRate  = new double[dimension]; 
    mK2        = new double[dimension]; 
    System.arraycopy(aState,0,mLeftState,0,dimension);
    System.arraycopy(aRate, 0,mLeftRate, 0,dimension);
    System.arraycopy(K2,    0,mK2,       0,dimension);
    mTimeIndex = aState.length-1;
    mStepSize = right - aState[mTimeIndex];
  }

  @Override
  public double interpolate(double time, int index) { // Algorithm taken from the book by Bellen and Zenaro, pp. 125
    double theta = (time-getLeft())/mStepSize;
    double b2 = theta*theta*mStepSize;
    double b1 = mStepSize*theta - b2;
    return mLeftState[index]+ b1*mLeftRate[index] + b2*mK2[index];
  }

  @Override
  public double[] interpolate(double time, double[] state, int beginIndex, int length) {
    double theta = (time-getLeft())/mStepSize;
    double b2 = theta*theta*mStepSize;
    double b1 = mStepSize*theta - b2;
    for (int index=beginIndex, i=0; i<length; index++, i++) {
      state[i] = mLeftState[index]+ b1*mLeftRate[index] + b2*mK2[index];
    }
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
