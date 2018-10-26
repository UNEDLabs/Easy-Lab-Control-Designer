package org.opensourcephysics.numerics.rk.irk;

/**
 * Specifies a system of linear algebraic equation
 * @author Andrei Goussev
 */
public interface LAEquation {
    /**
     * Gets number of equations in a system of linear algebraic equations
     * @return the number of equations
     */
    int getDimension();

    /**
     * Provides the left hand matrix of the system of linear algebraic equations system
     * @param matrix the left hand matrix
     */
    void getMatrix(double [][] matrix);
    /**
     * Provides the right hand vector of the system of linear algebraic equations system
     * @param vector the left hand matrix
     */
    void getVector(double [] vector);
}
