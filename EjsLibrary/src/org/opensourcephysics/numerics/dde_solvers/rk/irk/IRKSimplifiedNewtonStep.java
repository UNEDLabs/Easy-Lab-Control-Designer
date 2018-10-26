package org.opensourcephysics.numerics.dde_solvers.rk.irk;

/**
 * Core of the simplified Newton iteration solver. <code>IRKSimplifiedNewtonStep</code>
 * can be used as simplest algebraic equation systems solver in cases when error is
 * of no importance or as base for the solvers with error control. Current version of the
 * dolver is purposed only for a systems of algebraic equation that can be fitted into
 * <code>IRKAlgebraicEquation</code> with the A marix having an 1 real eigenvalue and
 * 1 complex pair of eigenvalues.<br>
 *
 * The code is transferred from the Fortran sources.
 * authors of original Fortran code:
 *            E. Hairer and G. Wanner
 *            Universite de Geneve, Dept. De Mathematiques
 *            ch-1211 Geneve 24, Switzerland
 *            e-mail:  rnst.hairer@math.unige.ch
 *                     gerhard.wanner@math.unige.ch <br>
 *
 *   original Fortran code is part of the book:
 *       E. Hairer and G. Wanner, Solving ordinary differential
 *       equations II. Stiff and differential-algebraic problems.
 *       Springer series in computational mathematics 14,
 *       Springer-Verlag 1991, second edition 1996.
 * @author Andrei Goussev
 */
public class IRKSimplifiedNewtonStep implements AlgebraicEquationSimpleSolver{
    protected double uround = 2.220446049250313E-16;
    private IRKAlgebraicEquation eqn;
    private int numEqn;
    private InnerLinearAlgebraicEquation innerLinearAlgebraicEquation;
    private LAESolverLU laeSolver;
    private LAEComplexSolverLU laeComplexSolver;
    private double [][] temporary;

    private double multiplier = 1;
    private boolean bestConvergenceRequired;
    private boolean refreshLAEsMatrixes;

    private double [][] substitutedApproximation;
    private double [][] approximation;
    private double [][] substitutedApproximationIncrement;
    private double incrementNormOnPreviousStep = 1;

    /**
     * Simplified newton solver obtains each next increment to the current approximation
     * vector as soulution of two linear algebraic systems applying change of variables
     * for the approximation vector.  <code> InnerLinearAlgebraicEquation </code> is
     * responsible for constructing rigth and left hands of the both of these linear
     * algebraic systems. Changed variables and linear equations updates each time next
     * approximation should be calculated.
     */
    private class InnerLinearAlgebraicEquation implements LAEquation, LAComplexEquation {
        private double [][] jac;
        private double [] realEigenvalues;
        private double [] complexEigenvalues;
        private double [][] substitutedNonLinearComponent ;

        /**
         * Constructs the instance of the inner newton equations class. But before the
         * solving it sould be initialiazed.
         */
        public InnerLinearAlgebraicEquation() {
            realEigenvalues = eqn.getRealEigenvalues();
            complexEigenvalues = eqn.getComplexEigenvalues();
            substitutedNonLinearComponent = new double [3][numEqn];
            jac = new double [numEqn][numEqn];
        }

        /**
         * Constructs the data for building matrixes and vectors of linear equations systems.
         * <code>initialize()</code> should be invoked each time environment updates
         */
        public void initialize(){
// TODO: may be better to split it onto RightHand- & Jacobian- preparation methods (y/n)?
            for (int i = 0; i < 3; i++){
                eqn.evaluateNonLinearComponent(approximation[i], temporary[i]);
            }
            eqn.directChangeOfVariables(temporary, substitutedNonLinearComponent);
            if (bestConvergenceRequired){
                eqn.evaluateNonLinearComponentJacobian(approximation[2], jac);
            }
        }

        /**
         * Gets the same dimension for both linear equation systems
         * @return mNumEqn the count of equations
         */
        public int getDimension() {
            return numEqn;
        }

        /**
         * Constructs the left hand matix of the real linear equations system
         * related to the real eigenvalue.
         * @param matrix the left hand matix of linear equations system
         */
        public void getMatrix(double[][] matrix) {
            for (int i = 0 ; i < numEqn; i++ ){
                for(int j = 0; j < numEqn; j++)
                    matrix[i][j] = - jac[i][j];
                matrix[i][i] += multiplier*realEigenvalues[0];
            }
        }

        /**
         * Constructs the left hand matix of the complex linear equations
         * system related to the complex eigenvalue.
         * @param matrixRe the real part of the left hand matix of linear equations system
         * @param matrixIm the imaginary part of the left hand matix of linear equations system
         */
        public void getMatrixes(double[][] matrixRe, double [][] matrixIm) {
            for (int i = 0 ; i < numEqn; i++ ){
                for(int j = 0; j < numEqn; j++){
                    matrixRe[i][j] = - jac[i][j];
                    matrixIm[i][j] = 0;
                }
                matrixRe[i][i] += multiplier*complexEigenvalues[0];
                matrixIm[i][i] += multiplier*complexEigenvalues[1];
            }
        }
        /**
         * Constructs the right hand vector of the real linear equations system
         * related to the real eigenvalue.
         * @param vector the left hand vector of linear equations system
         */
        public void getVector(double[] vector) {
            for (int i = 0; i < numEqn; i++){
                double lambdaN = multiplier*realEigenvalues[0];
                vector[i] = substitutedNonLinearComponent[0][i] - lambdaN*substitutedApproximation[0][i];
            }
        }

        /**
         * Constructs the right hand vector of the complex linear equations
         * system related to the complex eigenvalue.
         * @param vectorRe the real part of the left hand vector of linear equations system
         * @param vectorIm the imaginary part of the left hand vector of linear equations system
         */
        public void getVectors(double[] vectorRe, double[] vectorIm) {
            for (int i = 0; i < numEqn; i++){
                double alphaN = multiplier*complexEigenvalues[0];
                double betaN = multiplier*complexEigenvalues[1];
                double s2 = -substitutedApproximation[1][i];
                double s3 = -substitutedApproximation[2][i];
                vectorRe[i] = substitutedNonLinearComponent[1][i];
                vectorIm[i] = substitutedNonLinearComponent[2][i];
                vectorRe[i] += s2*alphaN - s3*betaN;
                vectorIm[i] += s3*alphaN + s2*betaN;
            }

        }
    }

    /**
     * Constructs the simplest simplified Newton solver for a system of algebraic equation
     * equations
     * @param eqn the system of algebraic equation equations in a form of
     *        <code>IRKAlgebraicEquation</code>
     */
    public IRKSimplifiedNewtonStep(IRKAlgebraicEquation eqn) {
        this.eqn = eqn;
        numEqn = eqn.getApproximation()[0].length;
        innerLinearAlgebraicEquation = new InnerLinearAlgebraicEquation();
        initialize(new LAESolverLU(innerLinearAlgebraicEquation));
    }

    /**
     * Constructs the simplest simplified Newton solver for a system of algebraic equation
     * equations. Inner Newton solver's linear algebraic equations system will be solved by
     * assigned instance of solver.
     * @param eqn the system of algebraic equation equations in a form of
     *        <code>IRKAlgebraicEquation</code>
     * @param laeSolver the solver of the systems of linear algebraic equations, instanced
     *        outside the Newton solver.
     * @see LAESolverLU
     */
    public IRKSimplifiedNewtonStep(IRKAlgebraicEquation eqn, LAESolverLU laeSolver) {
        this.eqn = eqn;
        numEqn = eqn.getApproximation()[0].length;
        innerLinearAlgebraicEquation = new InnerLinearAlgebraicEquation();
        initialize(laeSolver);
    }
    /**
     * Common parts of both of the constructors
     * @param _laeSolver the real algebraic equations systems solver, that will be used for
     * solving inner real algebraic equations system.
     */
    private void initialize(LAESolverLU _laeSolver){
        this.laeSolver = _laeSolver;
        _laeSolver.assignEquation(innerLinearAlgebraicEquation);
        laeComplexSolver = new LAEComplexSolverLU(innerLinearAlgebraicEquation);
        this.approximation = eqn.getApproximation();
        temporary = new double [3][numEqn];
        substitutedApproximation = new double [3][numEqn];
        substitutedApproximationIncrement = new double [3][numEqn];
        refreshLAEsMatrixes = true;
        bestConvergenceRequired = true;
    }
    /**
     * Synchronize the change of approximation vertor and updated by user
     * the approximation vector.
     */
    public void updateInitialValue(){
        eqn.directChangeOfVariables(this.approximation, substitutedApproximation);
    }
    /**
     * Performs restarting of the solver for the cases when source system of
     * algebgraic equation had been changed by user.
     * @param _bestConvergenceRequired best convergence required. true value is
     *        the command to refresh the Jacobian, otherwise Jacobian matrix will
     *        stay as is.
     */
    public void restart(boolean _bestConvergenceRequired){
        refreshLAEsMatrixes = (multiplier != eqn.getScalarMultiplier());
// TODO: may be isn't necessary:
        eqn.directChangeOfVariables(this.approximation, substitutedApproximation);
        this.bestConvergenceRequired = _bestConvergenceRequired;
        incrementNormOnPreviousStep = 0;
    }

    /**
     * Gets the vector containing the intermediate result of the computations.
     * @return the substituted increment to the current approximation vector.
     */
    protected double[][] getSubstitutedApproximationIncrement() {
        return substitutedApproximationIncrement;
    }

    /**
     * Calculates the increment to the current approximation vertor, but does not adjust it.
     * @return the current convergence rate
     */
    public double singleStep(){
        multiplier = eqn.getScalarMultiplier();
        innerLinearAlgebraicEquation.initialize();
        if ((refreshLAEsMatrixes) || (bestConvergenceRequired)){
            laeSolver.initialize();
            laeComplexSolver.initialize();
        }
        laeSolver.resolve(substitutedApproximationIncrement[0]);
        laeComplexSolver.resolve(substitutedApproximationIncrement[1], substitutedApproximationIncrement[2]);
        refreshLAEsMatrixes = false;
        bestConvergenceRequired = false;

        double incrementNorm = estimateIncrementNorm(substitutedApproximationIncrement);
        double convergenceRate;
        if (incrementNormOnPreviousStep != 0) {
            convergenceRate = incrementNorm / incrementNormOnPreviousStep;
        }
        else{
            convergenceRate = 0.001;
        }
        incrementNormOnPreviousStep = Math.max (incrementNorm, uround);
        return convergenceRate;
    }

    /**
     * Increments the current approximation vector on the already calculated increment
     * value
     */
    protected void commitStep (){
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < numEqn; j++)
                substitutedApproximation[i][j] += substitutedApproximationIncrement[i][j];
        eqn.inverseChangeOfVariables(substitutedApproximation, approximation);
    }
    /**
     * Adjusts the current approximation vertor considering the one iteration of
     * simplified Newton iterations method.
     * @return the current convergence rate
     */
    public double resolve(){
        double convergenceRate = singleStep();
        commitStep();
        return convergenceRate;
    }
    /**
     * Estimates the norm of a verctors array
     * @param dataArray a some verctors array
     * @return the norm
     */
    protected double estimateIncrementNorm(double [][] dataArray){
        double incrementNorm = 0.0;
        for (int i = 0; i < numEqn; i++)
            for (int j = 0; j < 3; j++)
                incrementNorm += Math.pow(dataArray[j][i], 2);
        return Math.sqrt(incrementNorm /(3*numEqn));
    }

}
