/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        Dopri5
 * Description:  Dorman Prince 5 ODE solver with 4th order C1 dense output
 * @author       Francisco Esquembre (based on code by Andrei Goussev / based on code by Hairer)
 * @version 1.0  December 2008
 */
public class Dopri5 extends AbstractAdaptiveRKSolverInterpolator {

  static private final double 
    A_11 =     1.0/5.0,
    A_21 =     3.0/40.0,   A_22 =      9.0/40.0,
    A_31 =    44.0/45.0,   A_32 =    -56.0/15.0,   A_33 =    32.0/9.0,
    A_41 = 19372.0/6561.0, A_42 = -25360.0/2187.0, A_43 = 64448.0/6561.0, A_44 = -212.0/729.0,
    A_51 =  9017.0/3168.0, A_52 =   -355.0/33.0,   A_53 = 46732.0/5247.0, A_54 =   49.0/176.0, A_55 = -5103.0/18656.0;

  // B5 are the 5th order coefficients
  static private final double B5_1 = 35.0/384.0, B5_2 = 0.0, B5_3 = 500.0/1113.0, B5_4 =125.0/192.0, B5_5 = -2187.0/6784.0, B5_6 = 11.0/84.0;
  // Error coefficients
  static private final double E_1 = 71.0/57600.0, E_2 = 0.0, E_3 = -71.0/16695.0, E_4 = 71.0/1920.0, E_5 = -17253.0/339200.0, E_6 = 22.0/525.0, E_7 = -1.0/40.0;

  static private final double D_1 = -12715105075.0/11282082432.0,  D_2 = 0.0, D_3 = 87487479700.0/32700410799.0, D_4 = -10690763975.0/1880347072.0, 
                              D_5 = 701980252875.0/199316789632.0, D_6 = -1453857185.0/822651844.0, D_7 = 69997945.0/29380423.0;

  private boolean computeCoefficients=true;
  private double [][] coeffs;
  private double[] rate2, rate3, rate4, rate5, rate6;

  public Dopri5(org.opensourcephysics.numerics.ODE _ode) {
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
    coeffs = new double [5][dimension];
  }


  @Override
  protected double getMethodOrder() { return 5; }

  @Override
  protected int getNumberOfEvaluations() { return 6; }
  
  @Override
  protected double computeApproximations(double _step) {
    computeIntermediateStep(_step, finalState);
    ode.getRate(finalState, finalRate);

    double error = 0;
    for(int i = 0; i < dimension; i++) {
      double sk = absTol[i] + relTol[i] * Math.max(Math.abs(finalState[i]), Math.abs(initialState[i]));
      double errorI = (E_1*initialRate[i]+E_2*rate2[i]+E_3*rate3[i]+E_4*rate4[i]+E_5*rate5[i]+E_6*rate6[i]+E_7*finalRate[i])/sk;
      error += errorI*errorI;
    }
    return Math.sqrt(error/dimension);
  }

  @Override
  protected void computeFinalRate() {
    computeCoefficients = true;
    // Done already
  }

  protected double[] computeIntermediateStep(double _step, double[] _state) {
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
 
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) {
//    if (_time==initialTime) {
//      System.arraycopy(initialState, 0, _state, 0, dimension);
//      return _state;
//    }
//    if (Double.isNaN(finalTime)) return null;
//    if (_time==finalTime) {
//      System.arraycopy(finalState, 0, _state, 0, dimension);
//      return _state;
//    }
    if (computeCoefficients) {
      computeCoefficients = false;
      // calculation of coeffs matrix.
      for (int i=0; i<dimension; i++) {
        coeffs[0][i] = initialState[i]; // i'am not sure -> Y[i]
        coeffs[1][i] = finalState[i] - initialState[i];
        coeffs[2][i] = deltaTime*initialRate[i] - coeffs[1][i];
        coeffs[3][i] = coeffs[1][i] - deltaTime*finalRate[i] - coeffs[2][i];
        coeffs[4][i] = deltaTime*(D_1*initialRate[i]+D_2*rate2[i]+D_3*rate3[i]+D_4*rate4[i]+D_5*rate5[i]+D_6*rate6[i]+D_7*finalRate[i]);
      }
    }
    double theta = (_time-initialTime)/deltaTime;
    double theta1 = 1 - theta;
    for (int i=0; i<dimension; i++) _state[i] = coeffs[0][i] + theta*(coeffs[1][i] + theta1*(coeffs[2][i] + theta*(coeffs[3][i] + theta1*coeffs[4][i])));
    _state[timeIndex] = _time;
    return _state;
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
