package org.opensourcephysics.numerics.ode_solvers.rk.irk;

/**
 * AlgebraicEquationSimpleSolver defines a core of algebraic equation system
 * solver supporting runtime modification of equation.
 *
 * In other words it defines a core of root finder software but equation's
 * right hand function is a multivariable vector function with same dimention that
 * and argument.
 * @author Andrei Goussev
 */
public interface AlgebraicEquationSimpleSolver {

    /**
     * Adjust the current approximation as close to the root of right hand
     * function of the algebraic equation system as implemented algorithm can.
     * After invoking this method current approximation becomes a quite
     * exact.
     * @return the convergence rate
     */

    double resolve();

    /**
     * Performs preparation in case if algebraic equation was updated.
     *
     * @param bestConvergenceRequired the requirement to achive best
     *        convergence rate. Computation resources can be saved
     *        due to some data will stays from previous equation, if
     *        paramether set to false
     */
    void restart(boolean bestConvergenceRequired);

    /**
     * Informs the algebraic equation solver that current approximation was
     * changed by user.
     *
     * It is necessary when new initial values is assigned.
     */
    void updateInitialValue();
}
