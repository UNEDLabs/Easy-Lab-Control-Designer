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
 * @author       Ernesto Kofman
 * @author       Gonzalo Farias
 * @author       Francisco Esquembre
 *
 */
public class Qss2 extends Qss {
  protected double[] der_dx; // Second derivative
  protected double[] der_q; // Slope of q
  protected double[] dx_old; // previous value of dx
  protected double[] q_copy, q_after;

  private final double COEFF = 1000.0;

  public Qss2(ODE ode) {
    super(ode);
  }

  protected void allocateArrays () {
    super.allocateArrays();
    der_dx = new double[dimension];
    der_q = new double[dimension];
    dx_old = new double[dimension];
    q_copy = new double[dimension];
    q_after = new double[dimension];
  }

  final public void initialize(double _stepSize) {
    this.stepSize = _stepSize;
    infinity = _stepSize>0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    allocateArrays();
    reinitialize(ode.getState());
    setTolerances(1.0e-3,0);
    
  }

  public void reinitialize(double[] state) {
    for (int i = 0; i < dimension; i++) {
      x[i] = q[i] = state[i];
      tLast[i] = state[timeIndex];
      dq[i] = Math.max(dqRel[i] * Math.abs(q[i]), dqAbs[i]);
    }
    ode.getRate(x, dx);
    System.arraycopy(dx,0,der_q,0,dimension);

    double t = state[timeIndex];
    double dt = stepSize/COEFF;
    for (int i=0; i<dimension; i++) q_after[i] = find_q(i,t+dt);
    ode.getRate(q_after,dx_old);
    for (int i = 0; i < dimension; i++) {
      der_dx[i] = (dx_old[i] - dx[i])/dt;
    }
    for (int i = 0; i < dimension; i++) tNext[i] = findNextTime(i,tLast[i]);
    tNext[timeIndex] = infinity;
    der_q[timeIndex] = 1.0;
  }

    public double internalStep() {
      double dt = Math.max( (max_t - tLast[max_index]) / COEFF, 1.e-9); // Used to compute der_dx
      q[max_index] = x[max_index] = findState(max_index, max_t);
      der_q[max_index] = dx[max_index] = findDerState(max_index, max_t);
      if (max_index != timeIndex) dq[max_index] = Math.max(dqRel[max_index] * Math.abs(q[max_index]),dqAbs[max_index]);
      tLast[max_index] = max_t;
 //     System.arraycopy(q, 0, q_copy, 0, dimension);
 //     for (int i=0; i<dimension; i++) q_copy[i] = find_q(i, max_t);
      if (multirate_ode==null) {
        for (int i=0; i<dimension; i++) q_copy[i] = find_q(i, max_t);
        System.arraycopy(dx,0,dx_old,0,dimension);
        for (int i=0; i<dimension; i++) {
          x[i] = findState(i, max_t);
          q[i] = q_copy[i];
        }
        ode.getRate(q_copy,dx);
        for (int i=0; i<dimension; i++) {
          if (max_t!=tLast[i]) der_dx[i] = (dx[i]-dx_old[i])/(max_t-tLast[i]);
          tNext[i] = recomputeNextTime(i,max_t);
          tLast[i] = max_t;
        }
      }
      else {
        int[][] inverseMatrix = multirate_ode.getInverseIncidenceMatrix();
        int[] row = inverseMatrix[max_index];
        q[timeIndex] = max_t;
        tLast[timeIndex] = max_t;
        x[timeIndex] = max_t;
//        System.arraycopy(q, 0, q_after, 0, dimension);
//        for (int i=0; i<dimension; i++) q_after[i] = find_q(i,max_t+dt);
        for (int j=0; j<row.length; j++) {
          int k = row[j];
          find_q(q_copy,k,max_t);
          find_q(q_after,k,max_t+dt);
          x[k] = findState(k, max_t);
          dx[k] = multirate_ode.getRate(q_copy,k);
          double dx_after = multirate_ode.getRate(q_after,k);
          der_dx[k] = (dx_after - dx[k])/dt;
          q[k] = find_q(k,max_t);
          tNext[k] = recomputeNextTime(k,max_t);
          tLast[k] = max_t;
        }
        if (tNext[max_index]==tLast[max_index]) {
          tNext[max_index] = findNextTime(max_index,max_t);
        }
      }
      findNextIntegrationTime();
      return max_t;
    }

  // QSS2 order-dependent methods

  protected double findNextTime(int index, double t) {
    if (der_q[index]==dx[index]) {
      if (der_dx[index] == 0.0) return infinity;
      return t + Math.sqrt(Math.abs(2 * dq[index] / der_dx[index]));
    }
    return recomputeNextTime(index,t);
  }

  public double minimumPositiveRoot (double a, double b, double c) {
    if (a==0) {
      if (b==0) return infinity;
      double r = -c/b;
      if (r<0) return infinity;
      return r;
    }
    double disc = b*b-4*a*c;
    if (disc<0) return infinity;
    disc = Math.sqrt(disc);
    a = 2*a;
    double r1 = (-b+disc)/a;
    double r2 = (-b-disc)/a;
    if (r1<0) r1 = infinity;
    if (r2<0) r2 = infinity;
    return stepSize>0 ? Math.min(r1,r2) : Math.max(r1, r2);
  }

  /**
   * Similar to findNextTime() not asuming that q[]==state[]
   * AND that der_q[]==dx[]
   * | q(t) - x(dt)| = dq[], where
   * q(dt) = q + dt * der_q
   * x(dt) = state + dt*dx + 1/2 * dt^2 * der_dx;
   * @param index int
   * @param t double
   * @return double
   */
  protected double recomputeNextTime(int index, double t) {
    if (Math.abs(x[index]-q[index])>dq[index]) return t;
    double a = der_dx[index]/2;
    double b = dx[index]-der_q[index];
    double c1 = x[index]-q[index]+dq[index];
    double c2 = x[index]-q[index]-dq[index];
    return t + Math.min(minimumPositiveRoot(a,b,c1),minimumPositiveRoot(a,b,c2));
  }

  protected double findState(int index, double t) {
    double dt = t-tLast[index];
    return x[index] +dt*dx[index] +dt*dt*der_dx[index]/2;
  }

  protected double findDerState(int index, double t) {
    double dt = t-tLast[index];
    return dx[index] +dt*der_dx[index];
  }

  protected double find_q(int index, double t) {
    double dt = t-tLast[index];
    return q[index] +dt*der_q[index];
  }

  /**
   * Interpolates the q's only for those needed for a given rate index
   * @param qLocal double[] a place holder for the interpolated q
   * @param index int
   * @param t double
   */
  protected void find_q(double[] qLocal, int index, double t) {
    int[] row = ( (MultirateODE) ode).getDirectIncidenceMatrix()[index];
    for (int j=0; j<row.length; j++) {
      int k = row[j];
      qLocal[k] = find_q(k,t);
    }
  }


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
