package org.opensourcephysics.numerics.rk.irk;

/**
 * AlgebraicEquationSolver extends the AlgebraicEquationSimpleSolver to add an error
 * control capabilities.
 *
 * AlgebraicEquationSolver solvers adjust the approximation until that the desired
 * tolerance is reached.
 *
 * @author Andrei Goussev
 */

public interface AlgebraicEquationSolver extends AlgebraicEquationSimpleSolver{
    /**
     * Gets the tolerance of the any approximation component of the algebraic
     * equation solver.
     * @return the components tolerance
     */
    double getTolerance(int index);
    /**
     * Sets the tolerance to each aproximation component of the algebraic
     * equation solver.
     *
     * @param index the numer of a component
     * @param tolerance the value of desired tolerance
     */
    void setTolerance(int index, double tolerance);
}
