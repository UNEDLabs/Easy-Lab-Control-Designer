/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

import org.opensourcephysics.numerics.*;

/**
 * Uses a second step of BootStrapping from BootStrapping1 interpolation to get one extra precission order
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class Bootstrap2IntervalData extends BootstrapIntervalData {
  // Coefficients of the second bootstrap
  static private final double BETA1 = 0.7, BETA2 = 0.85;
  static private final double bt2_den = 2*BETA1*(BETA1-1)*BETA2*(BETA2-1)*(BETA2-BETA1)*(10*BETA1*BETA2-5*BETA2-5*BETA1+3);
  static private final double bt2_cf11 = BETA1*(-3*BETA1+2);
  static private final double bt2_cf01 = BETA1*(-3*BETA1+4)-1; 
  static private final double bt2_cys1 = 6*BETA1*(BETA1-1);
  static private final double bt2_cf12 = BETA2*(-3*BETA2+2);
  static private final double bt2_cf02 = BETA2*(-3*BETA2+4)-1; 
  static private final double bt2_cys2 = 6*BETA2*(BETA2-1);
  static private final double bt2_m44 = BETA1*(2+BETA1*(-6+4*BETA1));
  static private final double bt2_m54 = BETA2*(2+BETA2*(-6+4*BETA2));
  static private final double bt2_m45 = BETA1*(4+BETA1*(-9+5*BETA1*BETA1));
  static private final double bt2_m55 = BETA2*(4+BETA2*(-9+5*BETA2*BETA2));

  private double[] state_bt2 = null, rate_bt21, rate_bt22, bt2_c2, bt2_c3, bt2_c4, bt2_c5;

  public Bootstrap2IntervalData(double[] aState, double[] aRate,double[] bState, double[] bRate, ODE ode) {
    super(aState,aRate,bState, bRate, ode);
    prepareSecondBootstrap(); // this must be done here or there can be conflicts when asking for interpolation
  }

  @Override
  public double interpolate(double time, int index) {
    double step = (time - getLeft())/mDeltaTime;
    return mLeftState[index] + step*(mDeltaTime*mLeftRate[index] + step*(bt2_c2[index] + step*(bt2_c3[index] + step*(bt2_c4[index]+step*bt2_c5[index]))));
  }

  @Override
  public double[] interpolate(double time, double[] state, int beginIndex, int length) {
    bootstrap2((time - getLeft())/mDeltaTime, state, beginIndex, length);
    return state;
  }

  protected void bootstrap2(double step, double[] state, int beginIndex, int length) {
    for (int index=beginIndex, i=0; i<length; index++, i++) {
      state[i] = mLeftState[index] + step*(mDeltaTime*mLeftRate[index] + step*(bt2_c2[index] + step*(bt2_c3[index] + step*(bt2_c4[index]+step*bt2_c5[index]))));
    }
  }

  /**
   * Prepares the second bootstrap
   */
  private void prepareSecondBootstrap() {
    int dimension = mTimeIndex+1;
    if (state_bt2==null) {
      state_bt2 = new double[dimension];
      rate_bt21 = new double[dimension];
      rate_bt22 = new double[dimension];
      bt2_c2 = new double[dimension];
      bt2_c3 = new double[dimension];
      bt2_c4 = new double[dimension];
      bt2_c5 = new double[dimension];
    }
    super.bootstrap1(BETA1,state_bt2,0,mTimeIndex);
    state_bt2[mTimeIndex] = getLeft() + BETA1*mDeltaTime;
    mODE.getRate(state_bt2, rate_bt21);

    super.bootstrap1(BETA2,state_bt2,0,mTimeIndex);
    state_bt2[mTimeIndex] = getLeft()  + BETA2*mDeltaTime;
    mODE.getRate(state_bt2, rate_bt22);

    for (int i=0; i<dimension; i++) {
      double dif = mRightState[i]-mLeftState[i], f0 = mDeltaTime*mLeftRate[i], f1 = mDeltaTime*mRightRate[i];
      double e1 = mDeltaTime*rate_bt21[i] + bt2_cf11*f1 + bt2_cf01*f0 + bt2_cys1*dif;
      double e2 = mDeltaTime*rate_bt22[i] + bt2_cf12*f1 + bt2_cf02*f0 + bt2_cys2*dif;
      double c4 = (bt2_m55*e1 - bt2_m45*e2)/bt2_den;
      double c5 = (bt2_m44*e2 - bt2_m54*e1)/bt2_den;
      double c3 = f1 + f0 - 2*dif - 2*c4 - 3*c5;
      bt2_c5[i] = c5;
      bt2_c4[i] = c4;
      bt2_c3[i] = c3;
      bt2_c2[i] = dif - f0 - c3 - c4 - c5;
    }
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
