/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        BogackiShampine23
 * Description:  2/3rd order RK solver by BogackiShampine
 * @author Francisco Esquembre
 * @version 1.0 December 2008
 */

public class BogackiShampine23 extends AbstractAdaptiveRKSolverInterpolator {

  static private final double B3_1 = 2.0/9.0,  B3_2 = 1.0/3.0, B3_3 = 4.0/9.0;
  static private final double B2_1 = 7.0/24.0, B2_2 = 1.0/4.0, B2_3 = 1.0/3.0, B2_4 = 1.0/8.0;

  private double[] rate2, rate3, order2;

  public BogackiShampine23(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  @Override
  protected void allocateOtherArrays() {
    super.allocateOtherArrays();
    rate2 = new double[dimension];
    rate3 = new double[dimension];
    order2 = new double[dimension];
  }

  @Override
  protected double getMethodOrder() { return 3; }

  @Override
  protected int getNumberOfEvaluations() { return 3; }
  
  @Override
  protected double computeApproximations(double _step) {
    computeIntermediateStep(_step, finalState);
    ode.getRate(finalState, finalRate);   
    for (int i=0; i<dimension; i++) order2[i] = initialState[i]+_step*(B2_1*initialRate[i]+B2_2*rate2[i]+B2_3*rate3[i]+B2_4*finalRate[i]);
    return super.computeError(order2);
  }

  @Override
  protected void computeFinalRate() {  
    // Done already
    }

  @Override 
  protected double[] computeIntermediateStep(double _step, double[] _state) {
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*initialRate[i]/2;
    ode.getRate(_state, rate2);   
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+0.75*_step*rate2[i]; // 3/4
    ode.getRate(_state, rate3);   
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(B3_1*initialRate[i]+B3_2*rate2[i]+B3_3*rate3[i]);
    return _state;
  }

  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) { 
    return interpolateHermite(_time, _state);
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
