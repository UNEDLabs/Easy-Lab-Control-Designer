package org.opensourcephysics.numerics.rk.irk;

/**
 * Specifies a system of linear algebraic equation that is defined for complex
 * arithmetic
 * @author Andrei Goussev
 */
public interface LAComplexEquation {
    /**
     * Gets number of equations in a system of linear algebraic equations
     * @return the number of equations
     */
    int getDimension();

    /**
     * Provides the complex left hand matrix as pair of real matrix and
     * imaginary matrix
     * @param matrixRe the real matrix
     * @param matrixIm the imaginary matrix
     */
    void getMatrixes(double [][] matrixRe, double [][] matrixIm);

    /**
     * Provides the complex right hand vector as pair of real matrix and
     * imaginary matrix
     * @param vectorRe the real matrix
     * @param vectorIm the imaginary matrix
     */
    void getVectors(double [] vectorRe, double [] vectorIm);
}
