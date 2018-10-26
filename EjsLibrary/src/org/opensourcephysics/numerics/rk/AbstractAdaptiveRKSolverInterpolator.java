/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

import org.opensourcephysics.numerics.*;

/**
 * Title:        HermiteAdaptiveRKSolverInterpolator
 * Description:  Abstract class for an adaptive explicit Runge Kutta solver with Hermite interpolation.
 * Based on by Andrei Goussev and Yuri B. Senichenkov 
 * @author Francisco Esquembre
 * @version 1.0 December 2008
 */

public abstract class AbstractAdaptiveRKSolverInterpolator extends AbstractExplicitRKSolverInterpolator {
  private boolean estimate=false; // Whether to estimate the first step size
  private double actualStepSize;
  private double absoluteTolerance=Double.NaN, relativeTolerance=Double.NaN;
  protected double[] relTol; // array of relative tolerances
  protected double[] absTol; // array of absolute tolerances

  // step size estimation's parameters
  // fac1, fac2, - parameters for step size selection
  // beta - for step control stabilization
  // safe - safety factor
  static private final double fac1 = 0.33;
  static private final double fac2 = 6;
  static private final double beta = 0;
  static private final double safe = 0.9;
  private final double expO1 = 1.0 / getMethodOrder() - beta * 0.75;

  // step size estimation's variables
  private double errOld = 1.e-4;
  private double fac = 0;

  /**
   * Computes the accepted approximation for finalState[] and
   * returns the estimated error obtained
   * @return the estimated error
   */
  abstract protected double computeApproximations(double _step);

  /**
   * Computes the finalRate[]. We derived it to the subclass (instead
   * of calling ode.getRate(finalState,finalRate), because sometimes 
   * the subclass has already computed it.
   */
  abstract protected void computeFinalRate();

  /**
   * Returns the order+1 of the accepted approximation
   */
  abstract protected double getMethodOrder();

  
  @Override
  protected void allocateOtherArrays() {
    relTol = new double[dimension];
    absTol = new double[dimension];
    setTolerances(1.0e-6,1.0e-6);
  }

  @Override
  public void reinitialize(double[] _state) {
    super.reinitialize(_state);
    if (estimate) actualStepSize = limitStepSize(estimateFirstStepSize(stepSize));
    else actualStepSize = limitStepSize(stepSize);
  }

  @Override
  final public void setEstimateFirstStep(boolean _estimate) {
    estimate = _estimate;
  }

  public void setMaximumStepSize(double stepSize) {
    super.setMaximumStepSize(stepSize);
    actualStepSize = limitStepSize(actualStepSize);
  }
  
  @Override
  final public void setTolerances(double _absTol, double _relTol) {
    if (absoluteTolerance==_absTol && relativeTolerance==_relTol) return;
    absoluteTolerance = _absTol;
    relativeTolerance =_relTol;
    for (int i=0; i<dimension; i++) {
      absTol[i] = _absTol;
      relTol[i] = _relTol;
    }
    finalTime = Double.NaN;
    error_code=ODEAdaptiveSolver.NO_ERROR;
    if (estimate) actualStepSize = limitStepSize(estimateFirstStepSize(stepSize));
    else actualStepSize = limitStepSize(stepSize);
  }

  final public void setTolerances(double[] _absTol, double[] _relTol) {
    absoluteTolerance = relativeTolerance = Double.NaN;
    System.arraycopy(_absTol,0,absTol,0,dimension);
    System.arraycopy(_relTol,0,relTol,0,dimension);
    finalTime = Double.NaN;
    error_code=ODEAdaptiveSolver.NO_ERROR;
    if (estimate) actualStepSize = limitStepSize(estimateFirstStepSize(stepSize));
    else actualStepSize = limitStepSize(stepSize);
  }

  @Override
  final protected void computeOneStep() {
    error_code=ODEAdaptiveSolver.NO_ERROR;
    for (int iterations=0; iterations<500; iterations++) { // maximum number of attempts
      double err = computeApproximations(actualStepSize);
      counter += evals;
      if (err<=1.0) {
        computeFinalRate();
        finalTime = initialTime + actualStepSize;
        if (iterations>0) actualStepSize = limitStepSize(Math.min(actualStepSize, estimatedStepSize(err)));
        else actualStepSize = limitStepSize(estimatedStepSize(err)); // Can grow only if converged at first attempt
        return;
      }
      actualStepSize = limitStepSize(Math.min(actualStepSize, estimatedStepSize(err)));
    }
    // It did not converge
    finalTime = Double.NaN;
    error_code=ODEAdaptiveSolver.DID_NOT_CONVERGE;
  }

  /**
   * Based on code by Andrei Goussev and Yuri B. Senichenkov 
   * Adapted by Francisco Esquembre
   * @param _compState the state to compare to
   * @return
   */
  final protected double computeError(double[] _compState) {
    double error = 0;
    for(int i = 0; i < dimension; i++) {
      double sk = absTol[i] + relTol[i] * Math.max(Math.abs(finalState[i]), Math.abs(initialState[i]));
      double errorI = (finalState[i]-_compState[i])/sk;
      error += errorI*errorI;
    }
    return Math.sqrt(error/dimension);
  }

  /**
   * Based on code by Andrei Goussev and Yuri B. Senichenkov 
   * Adapted by Francisco Esquembre
   * @param err
   * @return
   */
  protected double estimatedStepSize(double err){
    double fac11 = 0;
    // taking decision for HNEW/H value
    if (err != 0) {
      fac11 = Math.pow(err,expO1);
      fac = fac11 / Math.exp(beta * Math.log(errOld)); // stabilization
      fac = Math.max (1.0/fac2, Math.min(1.0/fac1, fac/safe)); // we require fac1 <= HNEW/H <= fac2
    }
    else {
      fac11 = 1.0/fac1;
      fac = 1.0/fac2;
    }
    if (err<=1.0) { // step accepted
      errOld = Math.max(err, 1.0e-4);
      return actualStepSize / fac;
    }
    return actualStepSize/Math.min(1.0/fac1, fac11/safe); // step rejected
  }

  /**
   * Makes sure the intended step does not exceed the maximum step
   * @param intendedStep
   * @return
   */
  private double limitStepSize(double intendedStep) {
    if (intendedStep>=0) return Math.min(intendedStep, maximumStepSize);
    return Math.max(intendedStep, -maximumStepSize);
  }
  
  /**
   * Based on code by Andrei Goussev and Yuri B. Senichenkov 
   * Adapted by Francisco Esquembre
   * @param hMax
   * @return
   */
  final private double estimateFirstStepSize(double hMax){
    int posneg =(hMax<0)?-1:1;
    hMax = Math.abs(hMax);
    double normF = 0.0, normX = 0.0 ;
    for(int i = 0; i < dimension; i++) {
      double sk = absTol[i] + relTol[i]*Math.abs(initialState[i]);
      double aux = initialRate[i]/sk;
      normF += aux*aux;
      aux = initialState[i]/sk;
      normX += aux*aux;
    }
    double h;
    if((normF <= 1.e-10) || (normX <= 1.e-10)) h = 1.0e-6;
    else h = Math.sqrt(normX / normF) * 0.01;
    h = posneg*Math.min(h, hMax);
    // perform an Euler step an estimate the rate, reusing finalState (it is safe to do so)
    for(int i = 0; i < dimension; i++) finalState[i] = initialState[i] + h * initialRate[i];
    ode.getRate(finalState, finalRate);
    
    double der2 = 0.0;
    for(int i = 0; i < dimension; i++) {
      double sk = absTol[i] + relTol[i] * Math.abs(initialState[i]);
      double aux = (finalRate[i] - initialRate[i]) / sk;
      der2 += aux*aux;
    }
    der2 = Math.sqrt(der2) / h;
    //step size is computed as follows
    //h^order * max ( norm (initialRate), norm (der2)) = 0.01
    double der12 = Math.max(Math.abs(der2), Math.sqrt(normF));
    double h1;
    if (der12 <= 1.0e-15) h1 = Math.max(1.0e-6, Math.abs(h) * 1.0e-3);
    else h1 = Math.exp((1.0 / getMethodOrder()) * Math.log(0.01 / der12));
    h = posneg*Math.min(100*h, h1);
    if (hMax != 0) h = posneg*Math.min(Math.abs(h),hMax);
    return h;
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
