/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        EulerRichardson
 * Description:  2nd order Euler-Richarson solver 
 * @author Francisco Esquembre
 * @version 1.0 January 2009
 */

public class EulerRichardson extends AbstractExplicitRKSolverInterpolator {

  private double[] rate2, K2;
  
  public EulerRichardson(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  @Override
  protected int getNumberOfEvaluations() { return 2; }
  
  @Override
  protected void allocateOtherArrays() {
    rate2 = new double[dimension];
    K2 = new double[dimension];
  }
  
  protected void computeOneStep() {
    super.computeOneStep();
    System.arraycopy(rate2, 0, K2, 0, dimension); // keep it for the interpolation
  }

  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state) {
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*initialRate[i]/2;
    ode.getRate(_state, rate2); 
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*rate2[i];
    return _state;
  }

//  @Override
//  public double[] interpolate(double _time, double[] _state) { 
//    return interpolateHermite(_time, _state);
//  }
  
  // Book by Bellen and Zenaro, pp. 125
  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) {
    double delta = _time-initialTime;
    double b2 = delta*delta/stepSize;
    double b1 = delta - b2;
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+ b1*initialRate[i]+b2*K2[i];
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
