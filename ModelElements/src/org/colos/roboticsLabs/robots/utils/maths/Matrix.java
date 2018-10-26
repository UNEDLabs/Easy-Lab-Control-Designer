package org.colos.roboticsLabs.robots.utils.maths;

/**
 * @author Almudena Ruiz
 */

public class Matrix {
  
  private int m, n;
  private double A [] [];
   
  public Matrix(int m, int n){
    this.m = m;
    this.n = n;
    A = new double[m][n];
  }

  /**
   * Gets a matrix of "i" rows and "j" columns
   * @param i number of rows
   * @param j number of columns 
   * @return
   */
  public double get(int i, int j){return A[i][j];}
  
  /**
   * Gets a row vector
   * @param r, row number
   * @param j0
   * @param j1
   * @return
   */
  
  public double[] getVectorRow(int r, int j0, int j1){
    double vector[] = new double[(j1 - j0) + 1];
    try {
      for(int i = 0; i < vector.length; i++)
        vector[i] = A[r][j0 + i];
    }catch(ArrayIndexOutOfBoundsException e){
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return vector;
  }
  
  /**
   * Gets a column vector
   * @param c, column number
   * @param j0
   * @param j1
   * @return
   */
  
  public double[] getVectorColumn(int c, int j0, int j1){
    double vector[] = new double[(j1 - j0) + 1];
    try {
      for(int i = 0; i < vector.length; i++)
        vector[i] = A[j0 + i][c];
      }catch(ArrayIndexOutOfBoundsException  e){
        throw new ArrayIndexOutOfBoundsException("Submatrix indices");
      }
    return vector;
  }
  
  /**
   * Sets a value of the matrix
   * @param i, row number
   * @param j, column number 
   * @param s, value
   */
  
  public void set(int i, int j, double s){ A[i][j] = s; }

  /**
   * Method required to implement the forward kinematics 
   * @param B
   * @return
  */
  
  public Matrix times(Matrix B){
    if(B.m != n) throw new IllegalArgumentException("Matrix inner dimensions must agree.");
    Matrix X = new Matrix(m, B.n);
    double C[][] = X.getMatrix();
    double Bcolj[] = new double[n];
    for(int j = 0; j < B.n; j++){
      for(int k = 0; k < n; k++)
        Bcolj[k] = B.A[k][j];
        for(int i = 0; i < m; i++){
          double Arowi[] = A[i];
          double s = 0.0D;
          for(int k = 0; k < n; k++)
            s += Arowi[k] * Bcolj[k];
          C[i][j] = s;
          }
      }
      return X;
  }
  
  /**
   * @return Returns a matrix
   */

  protected double[][] getMatrix(){ return A; }

}
