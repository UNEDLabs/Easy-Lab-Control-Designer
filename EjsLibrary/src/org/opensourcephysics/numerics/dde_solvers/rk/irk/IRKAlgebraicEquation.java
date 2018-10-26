package org.opensourcephysics.numerics.dde_solvers.rk.irk;

/**
 * Specifies partial case of algebraic equation system G(X) = 0 in a        <br>
 * special form if G(X) can be represented as c * X - A (&*) F(X), where    <br>
 *     X = [x_1, ..., x_s], dimension(x_i) = n;                             <br>
 *     F(X) = [f(x_1), ..., f(x_s)], * dimension(f) = n;                    <br>
 *     A is a matrix, dimension(A) = s * s;                                 <br>
 *     c is a scalar multiplier                                             <br>
 *                                                                          <br>
 * G(x) = 0 is shown below:                                                 <br>
 * c * x_1 - a_11 * f(x_1) + ... a_1s * f(x_s) = 0                          <br>
 * ...                                                                      <br>
 * c * x_s - a_n1 * f(x_1) + ... a_nn * f(x_s) = 0                          <br>
 *       pay attension that dimension(x_i) = dimension(f_i) = n             <br>
 *                                                                          <br>
 * "A" matrix of elements a_ij specifies through decomposition of its       <br>
 * own inverse iA (i.e. A &* iA = 1):                                       <br>
 *      iA = T * L * iT                                                     <br>
 *          where L is block-diagonal matrix with eigenvalues on diagonal,  <br>
 *          T &* iT = 1                                                     <br>
 *                                                                          <br>
 *   Table of particular symbols                                            <br>
 *   (&*) : orthogonal matrix multiplication                                <br>
 *   &* : matrix multiplication                                             <br>
 *   1  : unitary matrix                                                    <br>
 *                                                                          <br>
 */
// TODO: Split onto IRK and IRK with analytical Jacobian.
public interface IRKAlgebraicEquation{
    /**
     * Gets the current approximation
     *
     * The getApproximation method is invoked by an AgebraicEquationSolver to obtain
     * the initial approximation of the system.
     * The algebraic equation solver adjust the aproximation and then copies new values
     * into the aproximation array at the end of the solution step.
     *
     * @return approximation the approximation [x_1, ..., x_s], x_i = [x_i1, ..., x_in]
     */

    public double [][] getApproximation();
    /**
     * Gets the real eigenvalues of "L" matrix
     *
     * getRealEigenvalues method contributes in specifying of "A" matrix
     * @return realEigenvalues the real eigenvalues array, like [lamda1, labda2 ...]
     */
    public double [] getRealEigenvalues();
    /**
     * Gets the complex eigenvalues of "L" matrix
     *
     * getRealEigenvalues method contributes in specifying of "A" matrix
     * @return complexEigenvalues the complex eigenvalues array, like [alpha1, beta1, alpha2, beta2, ...],
     *                            dimension of this array is always even
     */
    public double [] getComplexEigenvalues();
    /**
     * Performs left matrix multiplication of some vector variable on "iT" matrix
     *
     * directChangeOfVariables method contributes in specifying of "A" matrix because it
     * implicitly defines "iT" matrix
     *
     * In other words it performs the change of variables z = iT*x, where x is a free variable;
     *
     * @param freeVariable the variable to be substituted
     * @param substitutedVariable the result of substitution
     */
    public void directChangeOfVariables(double[][] freeVariable, double[][] substitutedVariable);
    /**
     * Performs left matrix multiplication of some vector variable on "T" matrix
     *
     * inverseChangeOfVariables method contributes in specifying of "A" matrix because it
     * implicitly defines "T" matrix
     *
     * In other words it performs the change of variables x = T*z, where z is a sunstituted variable;
     *
     * @param substitutedVariable the variable to be inversely subsitituted
     * @param freeVariable the result of inverse substitution
     */
    public void inverseChangeOfVariables(double[][] substitutedVariable, double[][] freeVariable);

    /**
     * Gets the scalar mupltiplier "c" of defining algebraic equation system
     *
     * @return c the scalar mupltiplier
     */
    public double getScalarMultiplier();
    /**
     * Evaluates the "f(x)" expression of defining algebraic equation system
     *
     * @param freeVariable the arguments array
     * @param functionValue the vector function value
     */
    public void evaluateNonLinearComponent(double[] freeVariable, double [] functionValue);
    /**
     * Evaluates jacobian matrix of the "f(x)" function
     *
     * @param freeVariable the arguments array
     * @param jacobian the jacobian matrix
     */
    public void evaluateNonLinearComponentJacobian(double[] freeVariable, double [][] jacobian);
}
