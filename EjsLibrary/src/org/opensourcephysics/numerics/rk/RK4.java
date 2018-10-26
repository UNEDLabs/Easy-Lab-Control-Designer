/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        RK4
 * Description:  4th order classical RK solver
 * @author Francisco Esquembre
 * @version 1.0 December 2008
 */

public class RK4 extends AbstractExplicitRKSolverInterpolator {
//  static private final double TWO_THIRDS = 2.0/3.0;
  
  public RK4(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  private double[] rate2, rate3, rate4;
  
  @Override
  protected void allocateOtherArrays() {
    rate2 = new double[dimension];
    rate3 = new double[dimension];
    rate4 = new double[dimension];
  }

  @Override
  protected int getNumberOfEvaluations() { return 4; }
  
  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state){
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*initialRate[i]/2;
    ode.getRate(_state, rate2);
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*rate2[i]/2;
    ode.getRate(_state, rate3);
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*rate3[i];
    ode.getRate(_state, rate4);
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*(initialRate[i]+2*rate2[i]+2*rate3[i]+rate4[i])/6.0;
    return _state;
  }
  
  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) { 
    return super.interpolateBootstrap1(_time, _state);
  }
//  public double[] interpolate(double _time, double[] _state) { 
//    return interpolateHermite(_time, _state);
//  }
  
//  @Override
//  public double[] interpolate(double _time, double[] _state) {
//    if (_time==initialTime) {
//      System.arraycopy(initialState, 0, _state, 0, dimension);
//      return _state;
//    }
//    if (Double.isNaN(finalTime)) return null;
//    if (_time==finalTime) {
//      System.arraycopy(finalState, 0, _state, 0, dimension);
//      return _state;
//    }
//    // Dense output order 3 interpolation but not globally C1. Hermite is globally C1
//    double theta = (_time-initialTime)/deltaTime;
//    double theta2 = theta*theta;
//    double theta3 = TWO_THIRDS*theta*theta2;
//    double b1 = theta - 1.5*theta2+theta3;
//    double b2 = theta2 - theta3;
//    double b4 = -theta2/2.0 + theta3;
//    for (int i=0; i<timeIndex; i++) {
//      _state[i] = initialState[i] + deltaTime*(b1*initialRate[i] + b2*(rate2[i] + rate3[i]) + b4*rate4[i]);
//    }
//    _state[timeIndex] = _time;
//    return _state;
//  }

  
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
