package org.opensourcephysics.numerics.rk.irk;

/**
 * Simplified Newton interation algebraic equations system solver.
 * Purposed for numerical solution of algebraic equations systems
 * that can be fitted into <code>IRKAlgebraicEquation</code>.<br>
 *
 * This code is transferred from the Fortran sources.
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
 *
 *
 * @author Andrei Goussev
 * @see IRKAlgebraicEquation
 */

public class IRKSimplifiedNewton extends IRKSimplifiedNewtonStep implements AlgebraicEquationSolver{
    private int numEqn;
    private final static int maxIterations = 7;
    protected int nIteration = 0;
    private double rightHand [][];
    private double [] tolerance;
    protected double thetaqOld;

    private double fnewt;
    private double convergenceRateOld = 1;
    private double faccon = 1;
/**
 * Constructs the simplified Newton solver for a system of algebraic equation equations
 * @param eqn system of algebraic equation equations in a form of <code>IRKAlgebraicEquation</code>
 */
    public IRKSimplifiedNewton(IRKAlgebraicEquation eqn) {
        super(eqn);
        initialize(eqn);
    }

// TODO: unsafe constructor because LUsolver is available outside. I have no any idea.
    /**
     * Constructs the simplified Newton solver for a system of algebraic equation equations. Inner Newton
     * solver's linear algebraic equations system will be solved by assigned instance of solver.
     * @param eqn the system of algebraic equation equations in a form of <code>IRKAlgebraicEquation</code>
     * @param laeSolver the solver of the systems of linear algebraic equations, instanced outside the
     *        Newton solver.
     * @see LAESolverLU
     */
    public IRKSimplifiedNewton(IRKAlgebraicEquation eqn, LAESolverLU laeSolver) {
        super(eqn, laeSolver);
        initialize(eqn);
    }
    /**
     * Initializes the simplified newton solver.
     * @param eqn the system of algebraic equation equations in a form of <code>IRKAlgebraicEquation</code>
     */
    private void initialize(IRKAlgebraicEquation eqn){
        numEqn = eqn.getApproximation()[0].length;
        rightHand = super.getSubstitutedApproximationIncrement();
        tolerance = new double[numEqn];
        for (int i = 0; i < numEqn; i++)
            setTolerance(i, 1e-6);
    }
    /**
     * Controls the invokes to <code>SimplifiedNewtonStep</code> solver to ensure the error
     * level. (<code>SimplifiedNewtonStep</code> solver adjusts current approximation but does
     * not guarantee the error).
     * @return the convergence rate
     * @throws NewtonLostOfConvergence the lost of convergence error
     * @throws NewtonLastIterationErrorIsTooLarge the predicted error on last interation
     *         exceeds the tolerance.
     */
    public double resolve ()
            throws NewtonLostOfConvergence, NewtonLastIterationErrorIsTooLarge{
// TODO: if LU.decomp made an error ??? ... -> stepsize/2
        double incrementNorm;
        nIteration = 0;
        faccon = Math.pow(Math.max(faccon, uround), 0.8);
        double convergenceRate = 0.001;
        do {
            nIteration++;
// call to super SimplifiedNewtonStep to adjust approximation sush as it can
            double tmp = super.singleStep();
// TODO: estimateIncrementNorm() invokes twice with the same input data, (also through super.singleStep)
            incrementNorm = estimateIncrementNorm(rightHand);
            if ((1 < nIteration) && (nIteration < maxIterations)){
                if (nIteration == 2){
                    convergenceRate = tmp;
                }
                else
                {
                    convergenceRate = Math.sqrt(tmp*convergenceRateOld);
                }
                convergenceRateOld = tmp;
                if (convergenceRate > 0.99) {
                    throw new NewtonLostOfConvergence(nIteration, convergenceRate);
                }
                faccon = convergenceRate /  (1 - convergenceRate);
                double error = predictLastIterationError(incrementNorm, convergenceRate);
                if (error > 1) {
                    throw new NewtonLastIterationErrorIsTooLarge(nIteration, maxIterations, error);
                }
            }
            super.commitStep();
        } while( !convergenceAchieved(incrementNorm, convergenceRate) );
        return convergenceRate;
    }

    /**
     * Estimates the weighted by tolerance norm of a vectors array.
     * @param dataArray a some verctors array
     * @return the inversly weigthed by tolerance vectors array norm
     */
    protected double estimateIncrementNorm(double [][] dataArray){
        double incrementNorm = 0.0;
        for (int i = 0; i < numEqn; i++)
            for (int j = 0; j < 3; j++){
                incrementNorm += Math.pow(dataArray[j][i] / tolerance[i], 2);
            }
        return Math.sqrt(incrementNorm /(3*numEqn));
    }
    /**
     * Predicts the error norm on the last iteration
     * @param norm the weighted norm of the increment to the approximation on current iteration
     * @param convergenceRate the convergence rate on current iteration
     * @return the predicted value of the error on last iteration
     */
    private double predictLastIterationError(double norm, double convergenceRate){
        return convergenceRate / (1.0 - convergenceRate) * norm * Math.pow(convergenceRate, maxIterations - 1 - nIteration) / fnewt;
    }
    /**
     * Checks of the achievment requested error level on basics of
     * convegence rate and weighted norm of the increment to the current approximation
     * @param incrementNorm the weighted increment norm
     * @param convergenceRate the current convergence rate
     * @return
     */
    private boolean convergenceAchieved(double incrementNorm, double convergenceRate){
       return (faccon * incrementNorm < fnewt);
    }

    void setFNewton (double _value) { fnewt = _value; }
    
    public double getTolerance(int index) {
        return tolerance[index];
    }

    public void setTolerance(int index, double tolerance) {
        this.tolerance[index] = tolerance;
    }

}

/**
 * Indicates that algebraic equation system can not be solved by current
 * (simplified Newton) solver because the lost of convergence.
 *
 * Exception contains number of successfully performed by solver iterations and
 * approximation convergence rate to the moment when the error detected
 */
class NewtonLostOfConvergence extends RuntimeException {
    private double convergenceRate;
    private int iterationNumber;
    /**
     * The constructor of <code>NewtonLostOfConvergence</code>.
     *
     * @param iterationNumber the number of successfully performed interation
     * @param convergenceRate the approximation convergence rate
     */

    public NewtonLostOfConvergence(int iterationNumber, double convergenceRate) {
        this.convergenceRate = convergenceRate;
        this.iterationNumber = iterationNumber;
    }
    /**
     * Gets the approximation convergence rate to the moment when convergence was
     * lost
     * @return the approximation convergence rate
     */
    public double getConvergenceRate() {
        return convergenceRate;
    }

    /**
     * Gets the number of successfully performed interation to the moment when
     * convergence was lost
     * @return the number of successfully performed interation
     */

    public int getIterationNumber() {
        return iterationNumber;
    }
    /**
     * Generates the error comment string
     * @return the error comment string
     */
    public String toString() {
        return "NewtonLostOfConvergence: rate of convergence "+convergenceRate+". Iteration # "+iterationNumber; //$NON-NLS-1$ //$NON-NLS-2$
    }
}

/**
 * Indicates that algebraic equation system can not be solved by current
 * (simplified Newton) solver because the fact that desired error can not be
 * achived even if all interations would be performed is detected . Usually
 * numeric software developers limitates maximum number of iteration to prevent
 * a solver infinite looping.
 *
 * Exception contains solver's paramethers related to the moment when error
 * was detected. They are approximation convergence rate, number of successfully performed
 * iteration and predicted violation of the tolerance on the last iteration.
 */
class NewtonLastIterationErrorIsTooLarge extends RuntimeException{
    private int iterationNumber;
    private int maxIterationsAllowed;
    private double toleranceViolation;
    /**
     * The constructor of <code>NewtonLastIterationErrorIsTooLarge</code>.
     *
     * @param iterationNumber the number of successfully performed interation
     * @param maxSteps the maximum allowed number of solver interations
     * @param toleranceViolation the predicted violation of the tolerance
     *        on the last iteration
     */

    public NewtonLastIterationErrorIsTooLarge(int iterationNumber, int maxSteps, double toleranceViolation) {
        this.iterationNumber = iterationNumber;
        this.maxIterationsAllowed = maxSteps;
        this.toleranceViolation = toleranceViolation;
    }
    /**
     * Gets the number of successfully performed interation
     * @return inerationNumber number of successfully performed interation
     */

    public int getIterationNumber() {
        return iterationNumber;
    }
    /**
     * Gets the maximum allowed number of solver interations
     * @return maxIterationsAllowed the maximum allowed number of solver interations
     */
    public int getMaxIterationsAllowed() {
        return maxIterationsAllowed;
    }
    /**
     * Gets toleranceViolation the predicted violation of the tolerance
     * on the last iteration
     * @return toleranceViolation the predicted violation of the tolerance
     *         on the last iteration
     */
    public double getToleranceViolation() {
        return toleranceViolation;
    }
}
