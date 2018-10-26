/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        CashKarp45
 * Description:  Cash-Karp 4th and 5th solver with local extrapolation
 * @author Francisco Esquembre
 * @version 1.0 December 2008
 */

public class CashKarp45 extends AbstractAdaptiveRKSolverInterpolator {
//  static private final boolean STANDARD_STYLE = false;
  static private final double A_11 =   1.0/5.0,
                              A_21 =   3.0/40.0, A_22 =  9.0/40.0,
                              A_31 =   3.0/10.0, A_32 = -9.0/10.0, A_33 =  6.0/5.0,
                              A_41 = -11.0/54.0, A_42 =  5.0/2.0,  A_43 = -70.0/27.0, A_44 = 35.0/27.0,
                              A_51 = 1631.0/55296.0, A_52 = 175.0/512.0, A_53 = 575.0/13824.0, A_54 = 44275.0/110592.0, A_55 = 253.0/4096.0;
  // B4 are the 4th order coefficients
  static private final double B4_1 = 2825./27648., B4_2 = 0., B4_3 = 18575./48384., B4_4 = 13525./55296., B4_5 = 277./14336., B4_6 = 1./4.;
  // B5 are the 5th order coefficients
  static private final double B5_1 = 37./378.,     B5_2 = 0., B5_3 = 250./621.,     B5_4 = 125./594.,     B5_5 = 0.,          B5_6 = 512./1771.;
//  static private final double E_1 = 277./64512.,   E_2 = 0.,  E_3 = -6925./370944., E_4 = 6925./202752.,  E_5 = 277./14336.,  E_6 = -277./7084.;

  private double[] rate2, rate3, rate4, rate5, rate6, order4;

  public CashKarp45(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  @Override
  protected void allocateOtherArrays() {
    super.allocateOtherArrays();
    rate2 = new double[dimension];
    rate3 = new double[dimension];
    rate4 = new double[dimension];
    rate5 = new double[dimension];
    rate6 = new double[dimension];
    order4 = new double[dimension];
  }

  @Override
  protected double getMethodOrder() { return 5; }

  @Override
  protected int getNumberOfEvaluations() { return 6; }
  
  @Override
  protected void computeFinalRate() {
    ode.getRate(finalState, finalRate);
  }

//  /**
//   * Wolfgang's Christian way 
//   * Adapted by Francisco Esquembre
//   * @param err
//   * @return
//   */
//  @Override
//  protected double estimatedStepSizeNO(double err){
//    if (err<=Float.MIN_VALUE) return actualStepSize*10; // error too small to be meaningful, increase stepSize x10
//    if (err>1) {// shrink, no more than x10
//      double fac = 0.9*Math.pow(err, -0.25);
//      return actualStepSize*Math.max(fac,0.1);
//    }
//    if (err<0.1) { // grow, but no more than factor of 10
//      double fac = 0.9*Math.pow(err, -0.2);
//      if (fac>1) return actualStepSize*Math.min(fac, 10); // sometimes fac is <1 because error/tol is close to one
////      return actualStepSize;
//    }
//    return actualStepSize;
//  }
  
  @Override
  protected double computeApproximations(double _step) {
    computeIntermediateStep(_step, finalState);
    for (int i=0; i<dimension; i++) {
      order4[i] = initialState[i]+_step*(B4_1*initialRate[i]+B4_2*rate2[i]+B4_3*rate3[i]+B4_4*rate4[i]+B4_5*rate5[i]+B4_6*rate6[i]);
    }
    return super.computeError(order4);
  }

  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state){
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*A_11*initialRate[i];
    ode.getRate(_state, rate2);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_21*initialRate[i]+A_22*rate2[i]);
    ode.getRate(_state, rate3);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_31*initialRate[i]+A_32*rate2[i]+A_33*rate3[i]);
    ode.getRate(_state, rate4);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_41*initialRate[i]+A_42*rate2[i]+A_43*rate3[i]+A_44*rate4[i]);
    ode.getRate(_state, rate5);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_51*initialRate[i]+A_52*rate2[i]+A_53*rate3[i]+A_54*rate4[i]+A_55*rate5[i]);
    ode.getRate(_state, rate6);

    for (int i=0; i<dimension; i++) {
      _state[i] = initialState[i]+_step*(B5_1*initialRate[i]+B5_2*rate2[i]+B5_3*rate3[i]+B5_4*rate4[i]+B5_5*rate5[i]+B5_6*rate6[i]);
    }
    return _state;
  }

  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) { 
    return super.interpolateBootstrap2(_time, _state);
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
