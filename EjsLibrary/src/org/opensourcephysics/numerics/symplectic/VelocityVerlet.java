/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.symplectic;

import org.opensourcephysics.numerics.rk.AbstractExplicitRKSolverInterpolator;

/**
 * Title:        VelocityVerlet
 * Description:  2nd order VelocityVerlet solver 
 * @author Francisco Esquembre
 * @version 1.0 December 2010
 */
public class VelocityVerlet extends AbstractExplicitRKSolverInterpolator {

  private boolean accelerationIndependentOfVelocity;
  private double[] rate2;

  public VelocityVerlet(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
    if (_ode instanceof org.opensourcephysics.numerics.EJSODE) {
      accelerationIndependentOfVelocity = ((org.opensourcephysics.numerics.EJSODE) _ode).isAccelerationIndependentOfVelocity();
    }
    else accelerationIndependentOfVelocity = false;
  }

  @Override
  protected int getNumberOfEvaluations() { return 2; }
  
  @Override
  protected void allocateOtherArrays() {
    rate2 = new double[dimension];
  }
  
  @Override
  protected void computeOneStep() {
    computeIntermediateStep(stepSize,finalState);
    // Prepare next step
    if (accelerationIndependentOfVelocity) {
      for(int i=0,j=1; i<timeIndex; i+=2,j+=2) {
        finalRate[i] = finalState[j];
        finalRate[j] = rate2[j];
      }
      finalRate[timeIndex] = rate2[timeIndex];
    }
    else ode.getRate(finalState, finalRate);
    finalTime = initialTime + stepSize;
    counter += evals;
  }

  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state) {
    double dt2 = _step*_step/2; // the step size squared
    // increment the positions using the velocity and acceleration
    for(int i = 0; i<timeIndex; i+=2) _state[i] = initialState[i]+_step*initialRate[i] + dt2*initialRate[i+1];
    ode.getRate(_state, rate2);
    double halfStep = _step/2;
    // increment the velocities with the average rate
    for(int i = 1; i<timeIndex; i+=2) _state[i] = initialState[i]+halfStep*(initialRate[i]+rate2[i]);
    // the independent variable
    _state[timeIndex] = initialState[timeIndex]+_step*initialRate[timeIndex];
    return _state;
  }

  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) { 
    return interpolateHermite(_time, _state);
  }
  
  // Book by Bellen and Zenaro, pp. 125
//  public double[] interpolate(double _time, double[] _state) {
//    double theta = (_time-initialTime)/stepSize;
//    double b2 = theta*theta*stepSize;
//    double b1 = stepSize*theta - b2;
//    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+ b1*initialRate[i]+b2*K2[i];
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
