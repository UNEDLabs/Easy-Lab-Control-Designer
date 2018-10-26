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
public class Qss3 extends Qss2 {
  protected double[] der_der_dx; // Second state derivative
  protected double[] der_der_q; // Quadratic slope of q
  protected double[] der_dx_old; // Previous value of der_dx
  protected double[] dx_new; // Another value of dx
  private final double COEFF = 100.0;
  
  public Qss3(ODE ode) {
    super(ode);
  }

  protected void allocateArrays () {
    super.allocateArrays();
    der_der_dx = new double[dimension];
    der_der_q = new double[dimension];
    der_dx_old = new double[dimension];
    dx_new = new double[dimension];
   }

  
  public void reinitialize(double[] state) {
    double t = state[timeIndex];
    double dt = stepSize/COEFF;

    for (int i = 0; i < dimension; i++) {
      x[i] = q[i] = state[i];
      tLast[i] = t;
      dq[i] = Math.max(dqRel[i] * Math.abs(q[i]), dqAbs[i]);
      der_q[i]=der_der_q[i]=der_dx[i]=der_der_dx[i]=0;
    }
    ode.getRate(x, dx);
    System.arraycopy(dx,0,der_q,0,dimension);
    for (int i=0; i<dimension; i++) q_after[i] = find_q(i,t+dt);
    ode.getRate(q_after,dx_old);
    for (int i = 0; i < dimension; i++) {
      der_der_q[i]=der_dx[i] = (dx_old[i] - dx[i])/dt;
    }
    for (int i=0; i<dimension; i++) q_after[i] = find_q(i,t+2*dt);
    ode.getRate(q_after,dx_new);
    for (int i = 0; i < dimension; i++) {
      der_dx_old[i] = (dx_new[i] - dx_old[i])/dt;
      der_der_dx[i]=(der_dx_old[i]-der_dx[i])/dt;
    }

    for (int i = 0; i < dimension; i++) {
      tNext[i] = findNextTime(i,tLast[i]);
    }
    tNext[timeIndex] = infinity;
    der_q[timeIndex] = 1.0;
    der_der_q[timeIndex] = 0;

  }

  public double internalStep() {
    double dt = Math.max( (max_t - tLast[max_index]) / COEFF, 1.e-9); // Used to compute der_dx
    q[max_index] = x[max_index] = findState(max_index, max_t);
    der_q[max_index] = dx[max_index] = findDerState(max_index, max_t);
    der_der_q[max_index] = der_dx[max_index] = findDerDerState(max_index, max_t);
//    System.out.println("State " + max_index + ": t="+max_t+" x= " + x[max_index] + " dx= "+ dx[max_index]+" ddx= "+ der_dx[max_index]+" dddx= "+ der_der_dx[max_index]);
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
//      System.arraycopy(q, 0, q_after, 0, dimension);
//      for (int i=0; i<dimension; i++) q_after[i] = find_q(i,max_t+dt);
      for (int j=0; j<row.length; j++) {
        int k = row[j];
        x[k] = findState(k, max_t);
        find_q(q_copy,k,max_t);
        dx[k] = multirate_ode.getRate(q_copy,k);
        find_q(q_copy,k,max_t+dt);
        double dx_after = multirate_ode.getRate(q_copy,k);
        der_dx[k] = (dx_after - dx[k])/dt;
        find_q(q_copy,k,max_t+2*dt);
        double dx_after_after=multirate_ode.getRate(q_copy,k);
//        System.out.println("Der State "+k+": " + dx[k] + "; " +dx_after + "; "+ dx_after_after);
        der_der_dx[k]=(dx_after_after-2*dx_after+dx[k])/dt/dt;
//        System.out.println("Ext_State " + k + ": t="+max_t+" x= " + x[k] + " dx= "+ dx[k]+" ddx= "+ der_dx[k]+" dddx= "+ der_der_dx[k]);        
        q[k] = find_q(k,max_t);
        der_q[k]= find_der_q(k,max_t);
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

  
  // QSS3 order-dependent methods

  protected double findNextTime(int index, double t) {
    if (der_q[index] == dx[index] && der_der_q[index] == der_dx[index]) {
      if (der_der_dx[index] == 0.0) return infinity;
      return t + Math.cbrt(Math.abs(6 * dq[index] / der_der_dx[index]));
    }
    return recomputeNextTime(index,t);
  }
   

  private double minimumPositiveRoot (double a, double b, double c, double d) {
    if (a==0){  //cuadratic equataion with 2 roots
      return minimumPositiveRoot(b,c,d);
    }
    //Cubic Equation with 3 roots
    double p=c/a-b*b/a/a/3;
    double q1  = (2*b*b*b/a/a/a - 9*b*c/a/a + 27*d/a)/27;
    double DD = p*p*p/27 + q1*q1/4; //discriminant
    if (DD<0){ //three real unequal roots
      double phi = Math.acos(-q1/2/Math.sqrt(Math.abs(p*p*p)/27));
      double temp1=2*Math.sqrt(Math.abs(p)/3);
      double r1 =  temp1*Math.cos(phi/3);
      double r2 = -temp1*Math.cos((phi+Math.PI)/3);
      double r3 = -temp1*Math.cos((phi-Math.PI)/3);
      temp1=b/a/3;
      r1 = r1-temp1;
      r2 = r2-temp1;
      r3 = r3-temp1;
      if (r1<0) r1 = Double.POSITIVE_INFINITY;
      if (r2<0) r2 = Double.POSITIVE_INFINITY;
      if (r3<0) r3 = Double.POSITIVE_INFINITY;
      
      return Math.min(Math.min(r1,r2),r3);
    }
    if (DD==0) { // three real roots, two of them are identical
      double r1 = 2*Math.cbrt(-q1/2);
      double r2 = -r1/2;
      double temp1=b/a/3;
      r1=r1-temp1;
      r2=r2-temp1;
      if (r1<0) r1 = Double.POSITIVE_INFINITY;
      if (r2<0) r2 = Double.POSITIVE_INFINITY;
      return Math.min(r1,r2);
      
    }
    //one real root an two complex roots 
    double temp1 =  Math.sqrt(DD);
    double r1  = Math.cbrt(-q1/2+temp1) + Math.cbrt(-q1/2-temp1)-b/a/3;
    
    if(r1<0) r1 = Double.POSITIVE_INFINITY;
    
    return r1;

  }
  

  /**
   * Similar to findNextTime() not asuming that q[]==state[]
   * AND that der_q[]==dx[]
   * | q(t) - x(dt)| = dq[], where
   * q(dt) = q + dt * der_q + dt * dt * der_der_q/2
   * x(dt) = x + dt*dx + 1/2 * dt^2 * der_dx + 1/6 * dt^3;
   * @param index int
   * @param t double
   * @return double
   */
  protected double recomputeNextTime(int index, double t) {
    if (Math.abs(x[index]-q[index])>dq[index]) return t;
    double a = der_der_dx[index]/6;
    double b = (der_dx[index]-der_der_q[index])/2;
    double c = dx[index]-der_q[index];
    double d1 = x[index]-q[index]+dq[index];
    double d2 = x[index]-q[index]-dq[index];
    return t + Math.min(minimumPositiveRoot(a,b,c,d1),minimumPositiveRoot(a,b,c,d2));
  }

  protected double findState(int index, double t) {
    double dt = t-tLast[index];
    return x[index] +dt*dx[index] +dt*dt*der_dx[index]/2+dt*dt*dt*der_der_dx[index]/6;
  }

  protected double findDerState(int index, double t) {
    double dt = t-tLast[index];
    return dx[index] +dt*der_dx[index]+dt*dt*der_der_dx[index]/2;
  }
  protected double findDerDerState(int index, double t) {
    double dt = t-tLast[index];
    return der_dx[index] +dt*der_der_dx[index];
  }
  
  protected double find_q(int index, double t) {
    double dt = t-tLast[index];
    return q[index] +dt*der_q[index]+dt*dt*der_der_q[index]/2;
  }

  protected double find_der_q(int index, double t) {
    double dt = t-tLast[index];
    return der_q[index]+dt*der_der_q[index];
  }
  
  /**
   * Interpolates the q's only for those needed for a given rate index
   * @param qLocal double[] a place holder for the intrepolated q
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
