/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.numerics.qss;

public class Utils  {

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
  
} // End of class


