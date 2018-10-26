/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.qss;
import org.opensourcephysics.numerics.*;

/**
 * A utility class for MultirateODE odes.
 * 
 * @author       Francisco Esquembre
 * @author       Ernesto Kofman
 *
 */
public class MultirateUtils {

  /**
   * Creates a direct incidence matrix for a QSS solver from a boolean matrix of double indices 
   * @param matrix The [i][j] entry of the array must be true if the derivative of the i-th variable depends on the j-th variable  
   * @return the direct incidence matrix to implement org.opensourcephysics.numerics.qss.MultirateODE
   */
  static public int[][] getIncidenceMatrix(boolean[][] matrix) {
    int dimension = matrix.length;
    int timeIndex = dimension-1;
    int [][] dest=new int[dimension][];
    for (int i = 0; i<timeIndex; i++) {
      int counter = 0;
      for (int j = 0; j<dimension; j++) if (matrix[i][j]) counter++;
      dest[i] = new int[counter];
      counter=0;
      for (int j = 0; j<dimension; j++) if (matrix[i][j]) dest[i][counter++] = j;
    }
    dest[timeIndex]=new int[0];
    return dest;    
  }
  
//static private final int TEST_POINTS = 5;
  /**
   * Returns the incidence matrix of the ODE estimated in x.
   * I.e. estimates the directIncidenceMatrix from the state vector x 
   *
   DOES NOT WORK
  static public int[][] getIncMat (ODE ode, double[] state, double stepSize) {
    stepSize = Math.max(0.001, Math.abs(stepSize));
    System.out.println ("Step size = "+stepSize);
    int dimension=ode.getState().length;
    int timeIndex = dimension-1;

    double[][] rates=new double[TEST_POINTS][dimension];
    double[][] states=new double[TEST_POINTS][dimension];
    double[] stateTest=new double[dimension];
    double[] rateTest=new double[dimension];
    
    // Do a first Euler step
    System.arraycopy(state,0,states[0],0,dimension);
    ode.getRate(states[0], rates[0]);
    for (int test = 1; test<TEST_POINTS; test++) {
      for(int i = 0; i < dimension; i++) states[test][i] = states[test-1][i] + stepSize*rates[test-1][i];
      ode.getRate(states[test], rates[test]);
    }
    
    boolean[][] changes=new boolean[timeIndex][timeIndex];
    for(int i = 0; i<timeIndex; i++) for(int j = 0; j<timeIndex; j++) changes[i][j]=false;
    for (int test = 0; test<TEST_POINTS; test++) {
      for(int i = 0; i<timeIndex; i++) {
        System.arraycopy(states[test],0,stateTest,0,dimension);
        double dx=0.001*stateTest[i]+1e-3;
        stateTest[i]=stateTest[i]+dx;
        ode.getRate(stateTest, rateTest);
        for (int j = 0; j<timeIndex; j++) if (rateTest[j]!=rates[test][j]) changes[i][j] = true;
      }
    }
    int [][] dest=new int[dimension][];
    for (int i = 0; i<timeIndex; i++) {
      int counter = 0;
      for (int j = 0; j<dimension; j++) if (changes[i][j]) counter++;
      dest[i] = new int[counter];
      counter=0;
      for (int j = 0; j<dimension; j++) if (changes[i][j]) dest[i][counter++] = j;
    }
    dest[timeIndex]=new int[0];

    return dest;    
  }
  */

  /**
   * Returns the incidence matrix of the ODE estimated in x.
   * I.e. estimates the directIncidenceMatrix from the state vector x 
   */
  static public int[][] estimateIncidenceMatrix (ODE ode, double[] x) {
    int dimension=ode.getState().length;
    double[] rate=new double[dimension];
    double[] rate1=new double[dimension];
    int[][] Inc=new int[dimension-1][dimension-1];
    double[] x1=new double[dimension];
    double[] dx=new double[dimension];
    int[] cont=new int[dimension];
    for(int i = 0; i < dimension-1; i++){
      cont[i]=0;
    }
    for(int i = 0; i < dimension-1; i++){
      System.arraycopy(x,0,x1,0,dimension);
      dx[i]=0.00001*x1[i]+1e-10;
      x1[i]=x1[i]+dx[i];
      ode.getRate(x, rate);
      ode.getRate(x1, rate1);
      for(int k = 0; k < dimension-1; k++){
        if((rate1[k]-rate[k])/dx[i]!=0) {
          Inc[k][cont[k]]=i;
          cont[k]++;
        }
      }
    }
    int [][] dest=new int[dimension][];
    for(int k = 0; k < dimension-1; k++){
      int[] row= new int[cont[k]];
      System.arraycopy(Inc[k],0,row,0,cont[k]);
      dest[k]=new int[cont[k]];
      for(int i=0;i<cont[k];i++){
        dest[k][i]=row[i];
      }
    }
    dest[dimension-1]=new int[0];
    return dest;    
  }

  
  
   /**
   * Returns the incidence matrix of the ODE estimated in x.
   * I.e. estimates the directIncidenceMatrix from the state vector x 
   */
  static public double[][] getJacobian(ODE ode,double x[]) { 
    int dimension=ode.getState().length;
    double[] rate=new double[dimension];
    double[] rate1=new double[dimension];
    double[][] Jac=new double[dimension-1][dimension-1];
    double[] x1=new double[dimension];
    double[] dx=new double[dimension];
    
    for(int i = 0; i < dimension-1; i++){
      System.arraycopy(x,0,x1,0,dimension);
      dx[i]=0.00001*x1[i]+1e-10;
      x1[i]=x1[i]+dx[i];
      ode.getRate(x, rate);
      ode.getRate(x1, rate1);
  
      for(int k = 0; k < dimension-1; k++){        
        Jac[k][i]=(rate1[k]-rate[k])/dx[i];
      }
    }
//    System.out.println( "Jacobian" );
//    System.out.println( Jac[0][0]+"  "+Jac[0][1]);
//    System.out.println( Jac[1][0]+"  "+Jac[1][1]);
//    System.out.println( "---------------");
    return Jac;
  }
  
  /**
   * Returns the reciprocal of the src matrix.
   * I.e. computes the directIncidenceMatrix from the 
   * inverseIncidenceMatrix and viceversa.
   * @param src int[][]
   * @param dest int[][]
   */
  static public int[][] getReciprocalMatrix (int[][] src) {
    int[][] dest = new int[src.length][];
    for (int index=0; index<src.length; index++) { // for each state
      java.util.ArrayList<Integer> list = new java.util.ArrayList<Integer>();
      for (int i = 0; i < src.length; i++) { // for each rate
        int[] row = src[i];
        for (int j = 0; j < row.length; j++) { // for each state in the direct matrix
          if (row[j] == index) list.add(i); // add this state to the list
        }
      }
      int n = list.size();
      dest[index] = new int[n];
      for (int i=0; i<n; i++) dest[index][i] = list.get(i);
    }
    return dest;
  }

  /**
   * Prints the provided incidence matrix
   * @param matrix int[][]
   */
  public static void printMatrix (int matrix[][]) {
    System.out.println("{"); //$NON-NLS-1$
    for (int i = 0; i < matrix.length; i++) { // for each state
      int[] row = matrix[i];
      System.out.print("  { "); //$NON-NLS-1$
      for (int j = 0; j < row.length; j++) { // for each rate in the direct matrix
        if (j==0) System.out.print(row[j]);
        else System.out.print(", "+row[j]); //$NON-NLS-1$
      }
      System.out.println(" }"); //$NON-NLS-1$
    }
    System.out.println("}"); //$NON-NLS-1$
  }

  /**
   * Test of the routine
   * @param args String[]
   *
  public static void main (String args[]) {
    int[][] direct = new int[][] {
      { 1 },
      { 0,1 },
      { }
    };
    direct = new int[][] {
      { 1 },
      { 0,1 },
      { }
    };
    printMatrix(direct);
    int[][] inverse = getReciprocalMatrix(direct);
    printMatrix(inverse);
  }
  */
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
