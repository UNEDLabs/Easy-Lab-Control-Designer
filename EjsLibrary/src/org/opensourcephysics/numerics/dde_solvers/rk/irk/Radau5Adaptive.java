package org.opensourcephysics.numerics.dde_solvers.rk.irk;

import org.opensourcephysics.numerics.ODEAdaptiveSolver;
import org.opensourcephysics.numerics.ODE;

/**
 * Numerical solution of a stiff system of first order ordinary
 * differential equations. The method used is an implicit
 * Runge-Kutta method (Radau IIA) of order 5 with step size
 * control.<br>
 *
 * The code is transferred from the Fortran sources.
 * authors of original Fortran code:
 *    E. Hairer and G. Wanner
 *    Universite de Geneve, Dept. De Mathematiques
 *    ch-1211 Geneve 24, Switzerland
 *    e-mail:  rnst.hairer@math.unige.ch
 *             gerhard.wanner@math.unige.ch <br>
 *
 * original Fortran code is part of the book:
 *    E. Hairer and G. Wanner, Solving ordinary differential
 *    equations II. Stiff and differential-algebraic problems.
 *    Springer series in computational mathematics 14,
 *    Springer-Verlag 1991, second edition 1996.
 * @author Andrei Goussev
 */

public class Radau5Adaptive extends Radau5Light {
    protected int error_code=ODEAdaptiveSolver.NO_ERROR;
    private double currentStepSize;    
    private int nRejected = 0;
    private int nAcceptedSteps = 0;
    private double [] scal;
    private ErrorFirstAproximationEquation errorApproximationEquation;
    private LAESolverLU laeSolver;

    /**
     * Departure from the style of object oriented programming only for
     * the reason to get access to the nIteration parameter.
     */
    private class MyNewton extends IRKSimplifiedNewton{
        MyNewton(IRKAlgebraicEquation eqn, LAESolverLU laeSolver){
            super(eqn, laeSolver);
        }
        MyNewton(IRKAlgebraicEquation eqn){
            super(eqn);
        }
        double getnIter(){
            return nIteration;
        }
    }

    /**
     * Constructs the Radau5 ODEAdaptiveSolver for a system of ordinary differential equations.
     *
     * @param ode the system of differential equations.
     */
    public Radau5Adaptive(ODE ode) {
        super(ode);
    }

    protected void allocateArrays(int n) {
      super.allocateArrays(n);
      errorApproximationEquation = new ErrorFirstAproximationEquation(n);
      scal = new double[n];
    }

    public void reinitialize(double[] _state) {
      super.reinitialize(_state);
      error_code=ODEAdaptiveSolver.NO_ERROR;
    }

    /**
     * Assigns simplified newton iteration solver as default solver for the difference scheme
     * equation.
     * @param algEqn system of algebraic equations (Indeed the difference scheme equation)
     * @return simplified newton iteration solver instance
     */
    protected AlgebraicEquationSimpleSolver getInnerSolver(IRKAlgebraicEquation algEqn) {
        laeSolver = new LAESolverLU(mDimension);
//        return new IRKSimplifiedNewton(algEqn, laeSolver);
        return new MyNewton(algEqn, laeSolver);
    }
    /**
     * Predicts the increment to the stages vectors array for the next step
     * scaling on the relation of the current step on last iteration taken step
     * @param initialvalue the value to be adjusted
     */
    protected void estimateNewtonInitialValue(double [][] initialvalue){
        double factor = currentStepSize / mActualStepSize; // taken step size in denominator indeed
        for (int j = 0; j < 3; j++)
            for (int i = 0; i < mDimension; i++)
                 initialvalue[j][i] *= factor;
    }

    /**
     * Before the step performing actions
     */
    protected void preStepPreparations() {
        super.preStepPreparations();
        nRejected = 0;
    }

    /**
     * Steps (advances) the differential equations by the value that less or equal the stepSize
     * with convergence guarantee
     * @return the taken step size value
     */
    public double doStep() {
        error_code=ODEAdaptiveSolver.NO_ERROR;
        preStepPreparations();
        double error = 0;
        if (nAcceptedSteps > 0) {
            estimateNewtonInitialValue(mIntermediateStagesIncrement);
            mAeSolver.updateInitialValue();
        }
        do {
            currentStepSize = mActualStepSize;
            try{
                double newtonConvergenceRate = mAeSolver.resolve();
                error = estimateError();
                // TODO: ((MyNewton)mAeSolver).getnIter()!!!
                mActualStepSize = limitStepSize(estimateStepSize(error, ((MyNewton)mAeSolver).getnIter(), 7));
                if (error < 1) {
                 // TODO: 0.001 is a parameter
                    if ((Math.abs((mActualStepSize / currentStepSize)-1.1) <= 0.1)&&(newtonConvergenceRate <= 0.001)){
                      mActualStepSize = currentStepSize; // it's a very cheap to stay stepsize as is.
                    }
                    mAeSolver.restart(newtonConvergenceRate > 0.001);
                }
                else {
                    nRejected++;
                    mAeSolver.restart((mJacobianAge > 0));
                }
            } catch (NewtonLostOfConvergence e){
                mActualStepSize = currentStepSize / 2;
                mAeSolver.restart(mJacobianAge > 0);
                nRejected++;
                error = 10; // also one loop, no other means
// TODO: "hhfac = 0.5; && nmSingularMatrix" of original code had been lost on translation !!!
            } catch (NewtonLastIterationErrorIsTooLarge e){
                double qnewt = Math.max(1.0e-4, Math.min(20.0, e.getToleranceViolation()));
                mActualStepSize = limitStepSize(currentStepSize * 0.8 * Math.pow(qnewt, -1.0 / (4.0 + e.getMaxIterationsAllowed() - 1 - e.getIterationNumber())));
                mAeSolver.restart(mJacobianAge > 0);
                nRejected++;
                error = 10; // also one loop, no other means
            }
// TODO: to do something
        } while (error > 1);
        commitStepResults();
        return currentStepSize;
    }

    /**
     * Posts the results after the iteration step
     */
    protected void commitStepResults() {
        super.commitStepResults();
        nAcceptedSteps++;
        mJacobianAge++;
    }

    /**
     * Defines the linear algebraic equation for the estimating of the first
     * approximation of the error. Solution of the equation gives the error vector
     * first approximation.
     */
    private class ErrorFirstAproximationEquation implements LAEquation{
        private double [] err = new double[] {-10.048809399827414, 1.382142733160748, -0.3333333333333333};
        private double [] temporary;
        ErrorSecondApproximationEquation errorSecondApproximationEquation;

        /**
         * Constructs described below the linear algebraic equation
         * @param mNumEqn the number of components in the error vector
         */
        public ErrorFirstAproximationEquation(int numEqn) {
            temporary = new double[numEqn];
        }
        /**
         * Gets the number of components in the error vector. This information
         * is necessary for the solver.
         * @return the number of components in the error vector
         */
        public int getDimension() {
            return mDimension;
        }

        /**
         * Left hand matrix is the same as in the linear equation of the
         * <code>IRKSimplifiedNewton<code>. To get access to the decomposition of
         * left hand matrix  solver should be same too.
         * @param matrix
         */
        public void getMatrix(double[][] matrix) {
        }
        /**
         * Defines the right hand vector for essential the linear algebraic equation.
         * @param vector the right hand vector.
         */
        public void getVector(double[] vector) {
            for (int i = 0; i < mDimension; i++) {
                temporary[i] = 0;
                for (int j = 0; j < err.length; j++)
                    temporary[i] += (err[j]/currentStepSize)*mIntermediateStagesIncrement[j][i];
                vector[i] = temporary[i] + mRate[i];
            }
        }
        /**
         * Instanced the linear algebraic equation for the estimating of the second
         * approximation of the error
         * @param errorApproximation obtained first error approximation
         * @return the instanced the essential linear algebraic equation
         */
        public ErrorSecondApproximationEquation getSecondErrorAproximationEquation(double [] errorApproximation) {
            if (errorSecondApproximationEquation == null)
                errorSecondApproximationEquation = new ErrorSecondApproximationEquation(mDimension, errorApproximation);
            return errorSecondApproximationEquation;
        }

        /**
         * Defines the linear algebraic equation for the estimating of the second
         * approximation of the error. Solution of the equation gives the error vector
         * second approximation. Equations constructs under the first approximation error
         */
        private class ErrorSecondApproximationEquation implements LAEquation{
            double [] tmpRate;
            double [] errorApproximation;

            /**
             * Constructs described below linear algebraic equation
             * @param mNumEqn number of the components in the error vector
             * @param errorApproximation first approximation error
             */
            public ErrorSecondApproximationEquation(int numEqn, double [] errorApproximation) {
                tmpRate = new double [numEqn];
                this.errorApproximation = errorApproximation;
            }

            /**
             * Gets the number of components in the error vector. This information
             * is necessary for the solver.
             * @return the number of components in the error vector
             */
            public int getDimension() {
                return mDimension;
            }
            /**
             * Void because the same reason that and error first approximation equation.
             * @param matrix
             */
            public void getMatrix(double[][] matrix) {
            }
            /**
             * Constructs the right hand vector of the error second approximation equation.
             * Second approximation requires the also one call to the right hand function of ODE.
             * It uses intermediate results obtained on estimating the error first approximation
             * @param vector the right hand vector.
             */
            public void getVector(double[] vector) {
                if (temporary == null) System.err.println("Inner's getVector should be invoked earlier than that one"); //$NON-NLS-1$
                for (int i = 0; i < mDimension; i++) {
                    errorApproximation[i] += mState[i];
                }
// TODO: develop exception to throw every time getRate fault
                mWrapper.evaluateRate(errorApproximation, tmpRate);
                for(int i = 0; i < mDimension; i++) {
                    vector[i] = temporary[i] + tmpRate[i];
                }
            }
        }
    }
    /**
     * Estimates the error on the current ODE solver iteration by solving error equations.
     * @return the inversive weithed by tolerance error vector norm
     */
    protected double estimateError(){
        double [] someState = new double [mDimension];
        double error = 0;
        LAEquation newtonsLAE = laeSolver.getEquation();

        laeSolver.assignEquation(errorApproximationEquation);
        laeSolver.resolve(someState);
        error = 0;

        for (int i = 0; i < mDimension; i++) {
            error += Math.pow(someState[i] / scal[i], 2);
        }
        error = Math.max(Math.sqrt(error / mDimension), 1.0e-10);
        if((error > 1.0)&&((nRejected > 0) || (nAcceptedSteps == 0))) {
            laeSolver.assignEquation(errorApproximationEquation.getSecondErrorAproximationEquation(someState));
            laeSolver.resolve(someState);
            error = 0;
            for (int i = 0; i < mDimension; i++) {
                error += Math.pow(someState[i] / scal[i], 2);
            }
            error = Math.max(Math.sqrt(error / mDimension), 1.0e-10);
        }
        laeSolver.assignEquation(newtonsLAE);
        return error;
    }

    double hacc = 1;
    double erracc = 1;
    /**
     * Estimates the stepSize error
     * @param error the inversive weithed by tolerance error vector norm
     * @param nNewtonIteration the count of iterations had taken by an algebraic
     *        equation solver on difference scheme equation solving.
     * @param _maxNewtonIterations the paramether limiting the count of iterations for the
     *        algebraic equation solver
     * @return estimated stepSize value for the next ODE solver iteration (related to the
     *         case of the <code>error</code> < 1) or for the next try to perform the current one
     *         (if the <code>error</code> > 1)
     */
    protected double estimateStepSize(double error, double nNewtonIteration, double _maxNewtonIterations){
        double safe = 0.9;
        double facLeft = 5.0;
        double facRight = 1.0 / 8.0;
        double cfac = safe * (1 + 2 * _maxNewtonIterations);
        double fac = Math.min(safe, cfac / (nNewtonIteration + 2 * _maxNewtonIterations));
        double quot = Math.max(facRight, Math.min(facLeft, Math.pow(error, 0.25) / fac));
        double hnew = currentStepSize / quot;
        if (error < 1) {
            // --- predictive controller of Gustafsson
            if (nAcceptedSteps > 0){
                double facgus = (hacc / currentStepSize) * Math.pow(error * error / erracc, 0.25) / safe;
                facgus = Math.max(facRight, Math.min(facLeft, facgus));
                quot = Math.max(quot, facgus);
                hnew = currentStepSize / quot;
            }
            hacc = currentStepSize;
            erracc = Math.max(1.0e-2, error);
        } else {
            if (nAcceptedSteps == 0){
               hnew = currentStepSize * 0.1;
            }
        }
        return hnew;
    }

    public void setTolerances(double absTol, double relTol) {
      if (relTol!=0.0) {
        double quot = absTol/relTol;
        relTol = 0.1*Math.pow(relTol, 2.0 / 3.0);
        absTol = relTol*quot;
      }
//        this.tolerance = tolerance;
        for (int i =0; i < mDimension; i++){
// todo: it's unavailable to have state here
            scal[i] = absTol + relTol*Math.abs(mState[i]);
            ((AlgebraicEquationSolver)mAeSolver).setTolerance(i, scal[i]);
        }
// TODO: check equivalence with source. Here was SCAL = ..., that is used in Newton && ErrEst
        double uround = 2.220446049250313E-16;
        if (relTol==0) ((IRKSimplifiedNewton)mAeSolver).setFNewton(Double.POSITIVE_INFINITY);
        else ((IRKSimplifiedNewton)mAeSolver).setFNewton(Math.max(10 * uround / relTol, Math.min(0.03, Math.pow(relTol, 0.5))));
    }

    /**
     * Gets the error code.
     * Error codes:
     *   ODEAdaptiveSolver.NO_ERROR
     *   ODEAdaptiveSolver.DID_NOT_CONVERGE
     *   ODEAdaptiveSolver.BISECTION_EVENT_NOT_FOUND=2;
     * @return int
     */
    public int getErrorCode() {
      return error_code;
    }

}
