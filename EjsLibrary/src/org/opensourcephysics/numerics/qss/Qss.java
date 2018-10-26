/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.qss;

import org.opensourcephysics.numerics.*;

/**
 * An abstract class for Solvers based on QSS method
 * @author       Francisco Esquembre
 * @author       Ernesto Kofman
 *
 */

public class Qss implements ODESolverInterpolator {
  protected final ODE ode;
  protected final MultirateODE multirate_ode;

  protected int dimension; // dimension of the problem
  protected int timeIndex; // dimension - 1. It is the index of the time variable
  protected int max_index; // Indicates which state will change next
  
  protected double stepSize=0.1;
  protected double infinity = Double.POSITIVE_INFINITY; // Infinity in the direction of the step size
  protected double max_t;  // When the next state will change
  protected double lastDt; // last increment of time. Used to compute d f/ d t

  private double absoluteTolerance = Double.NaN, relativeTolerance=Double.NaN;
  protected double[] dq; // Quantization size
  protected double[] dqRel; // Relative quantization size
  protected double[] dqAbs; // Absolute quantization size
  protected double[] x; // State
  protected double[] q; // Quantized state
  protected double[] qPlusDt; // Used to compute the partial derivative d f / d t
  protected double[] dx; // Rate array
  protected double[] tLast; // time for the last update for each state
  protected double[] tNext; // time for the next event for each state

  public Qss(ODE ode) {
    this.ode = ode;
    if (ode instanceof MultirateODE) {
      multirate_ode = (MultirateODE) ode; 
    }else{
      multirate_ode = null;
    }
  }
  
  final public ODE getODE() { return this.ode; }

  /**
   * Allocates arrays. Separated from initialize() for easier overwriting.
   */
  protected void allocateArrays () {
    dimension = ode.getState().length;
    timeIndex = dimension - 1; // time is treated differently
    dq = new double[dimension];
    dqRel = new double[dimension];
    dqAbs = new double[dimension];
    x = new double[dimension];
    q = new double[dimension];
    if (multirate_ode!=null) qPlusDt = new double[dimension];
    dx = new double[dimension];
    tLast = new double[dimension];
    tNext = new double[dimension];
  }
  
  final public double[] getCurrentRate() { return dx; }
  
  final public void setEstimateFirstStep(boolean _estimate) {}
  
  final public double getStepSize() { return this.stepSize; }
  
  final public void setStepSize(double _stepSize) {
    this.stepSize = _stepSize;
    double oldInfinity = infinity;
    infinity = stepSize>0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    if (oldInfinity!=infinity) reinitialize(ode.getState());
  }

  /**
   * Does nothing
   */
  public void setMaximumStepSize(double stepSize) {}
  
  public void initialize(double _stepSize) {
    this.stepSize = _stepSize;
    infinity = _stepSize>0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    allocateArrays();
    double[] ode_state = ode.getState();
    for (int i = 0; i < dimension; i++) {
      x[i] = q[i] = ode_state[i];
      tLast[i] = ode_state[timeIndex];
    }
    ode.getRate(x, dx);
    setTolerances(1.0e-3,0);
    if(ode instanceof EstimatedMultirateODE){
      ((EstimatedMultirateODE)ode).estimateIncidenceMatrix();
    }
    if (isAutonomous()) tNext[timeIndex] = infinity;
    lastDt = dq[timeIndex];
  }

  public void reinitialize(double[] state) {
    for (int i = 0; i < dimension; i++) {
      x[i] = q[i] = state[i];
      tLast[i] = state[timeIndex];
      dq[i] = Math.max(dqRel[i]*Math.abs(q[i]), dqAbs[i]);
    }
    ode.getRate(x, dx);
    for (int i = 0; i < dimension; i++) tNext[i] = findNextTime(i,tLast[i]);
    if(ode instanceof EstimatedMultirateODE){
      ((EstimatedMultirateODE)ode).estimateIncidenceMatrix();
    }
    if (isAutonomous()) tNext[timeIndex] = infinity;
  }

  public double getMaximumTime () { return max_t; }

  public double getInternalStepSize() { return Double.NaN; }
  
  public double internalStep() {
    q[max_index] = x[max_index] = findState(max_index, max_t);
    if (max_index!=timeIndex) dq[max_index] = Math.max(dqRel[max_index]*Math.abs(q[max_index]),dqAbs[max_index]);

    tLast[max_index] = max_t;
    if (multirate_ode==null) {
      for (int i=0; i<dimension; i++) x[i] = findState(i, max_t);
      ode.getRate(q,dx);
      for (int i=0; i<dimension; i++) {
        tNext[i] = recomputeNextTime(i,max_t);
        tLast[i] = max_t;
      }
    }
    else {
      double incFt=0.0;
      int[][] inverseMatrix = multirate_ode.getInverseIncidenceMatrix();
      boolean nonAutonomous = inverseMatrix[timeIndex].length!=0;
      int[] row = inverseMatrix[max_index];
      q[timeIndex] = max_t;
      for (int j=0; j<row.length; j++) {
        int k = row[j];
        x[k] = findState(k,max_t);
        dx[k] = ((MultirateODE) ode).getRate(q,k);
        tNext[k] = recomputeNextTime(k,max_t);
        tLast[k] = max_t;
        if(k!=max_index){
        }
        if (nonAutonomous && max_index==timeIndex) { // Estimate using partial derivative
          System.arraycopy(q,0,qPlusDt,0,timeIndex);
          qPlusDt[timeIndex] = q[timeIndex]+ dq[timeIndex];
          double ftplusTol = ((MultirateODE) ode).getRate(qPlusDt,k);
          incFt = Math.max(Math.abs(ftplusTol-dx[k]),incFt);
        }
      }
      if (nonAutonomous && max_index==timeIndex) {
        lastDt = Math.min((dq[timeIndex]*dq[timeIndex])/incFt,2*lastDt);
        tNext[timeIndex] = max_t + lastDt;
      }
      if (tNext[max_index]==tLast[max_index]) {
        tNext[max_index] = findNextTime(max_index,max_t);
      }
    }
    findNextIntegrationTime();
    return max_t;
  }
  
  public void setMemoryLength(double length) {}
  
  // No memory state for this class
  public org.opensourcephysics.numerics.dde_solvers.interpolation.StateMemory getStateMemory() {
    return null;
  }

  public double[] interpolate (double time, boolean useLeftApproximation, double[] state) {
    for (int i=0; i<timeIndex; i++) state[i] = findState(i,time);
    state[timeIndex] = time;
    return state;
  }

  public double[] interpolate(double time, boolean useLeftApproximation, double[] state, int beginIndex, int length) {
    for (int i=0,index=beginIndex; i<length; i++) state[i] = findState(index,time);
    return state;
  }

  public double interpolate(double time, boolean useLeftApproximation, int index) {
    return findState(index,time);
  }

  public double[] bestInterpolate(double _time, double[] _state) {
    return interpolate(_time,false,_state);  
  }
  

  // QSS order - dependent methods

  /**
   * Computes the maximum step allowed before a change in q[]
   */
  protected void findNextIntegrationTime() {
    max_t=tNext[0];
    max_index = 0;
    // Find the minimum next time
    for (int i=1; i<dimension; i++) {
      if (max_t > tNext[i]) {
        max_index = i;
        max_t = tNext[i];
      }
    }
  }

  /**
   * Computes the time at which the x[] will deviate from q[]
   * a quantity equal to the quantum dq[]
   * @param index int
   * @param t double
   * @return double
   */
  protected double findNextTime(int index, double t) {
    if (dx[index]==0.0) return infinity;
    return t + Math.abs(dq[index]/dx[index]);
  }

  /**
   * Similar to findNextTime() not assuming that q[]==state[]
   * @param index int
   * @param t double
   * @return double
   */
  protected double recomputeNextTime(int index, double t) {
    if (dx[index]>0) return t + (q[index]+dq[index]-x[index])/dx[index];
    if (dx[index]<0) return t + (q[index]-dq[index]-x[index])/dx[index];
    return infinity;
  }

  protected double findState(int index, double t) {
    return x[index] +(t-tLast[index])*dx[index];
  }


  /**
   * Sets the same relative and absolute tolerances for all states
   * @param tol double
   */
  final public void setTolerances(double absTol, double relTol) {
    if (this.absoluteTolerance==absTol && this.relativeTolerance==relTol) return;
    this.absoluteTolerance = absTol;
    this.relativeTolerance = relTol;
    for (int i=0; i<timeIndex; i++) {
      dqRel[i] = relTol;
      dqAbs[i] = absTol;
      dq[i] = Math.max(dqRel[i]*Math.abs(q[i]),dqAbs[i]);
      tNext[i] = findNextTime(i,tLast[i]);
    }
    // Relative tolerance of time is always 0
    dqRel[timeIndex] = 0;
    lastDt = dq[timeIndex] = dqAbs[timeIndex] = absTol;
    if(ode instanceof EstimatedMultirateODE){
      ((EstimatedMultirateODE)ode).estimateIncidenceMatrix();
    }
    if (!isAutonomous()) tNext[timeIndex] = findNextTime(timeIndex,tLast[timeIndex]);
    findNextIntegrationTime();
  }

  /**
   * Sets a possibly different tolerance for each state
   * @param tol double[]
   */
  public void setToleranceArray(double[] absTol, double[] relTol) {
    this.absoluteTolerance = this.relativeTolerance = Double.NaN;
    if (relTol.length!=dimension || absTol.length!=dimension) {
      System.err.println("Tolerance arrays have wrong dimension!"); //$NON-NLS-1$
      return;
    }
    for (int i=0; i<timeIndex; i++) {
      dqRel[i] = relTol[i];
      dqAbs[i] = absTol[i];
      dq[i] = Math.max(dqRel[i]*Math.abs(q[i]),dqAbs[i]);
      tNext[i] = findNextTime(i,tLast[i]);
    }
    // Relative tolerance of time is always 0
    dqRel[timeIndex] = 0;
    lastDt = dq[timeIndex] = dqAbs[timeIndex] = absTol[timeIndex];
    if(ode instanceof EstimatedMultirateODE){
      ((EstimatedMultirateODE)ode).estimateIncidenceMatrix();
    }
    if (!isAutonomous()) tNext[timeIndex] = findNextTime(timeIndex,tLast[timeIndex]);
    findNextIntegrationTime();
  }


  /**
   * Whether the ODE defines an autonomous system.
   */
  protected final boolean isAutonomous() {
    return (multirate_ode==null) ? false : multirate_ode.getInverseIncidenceMatrix()[timeIndex].length==0;
  }

  public long getCounter() { return -1; }

} // End of class

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
