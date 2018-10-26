package org.opensourcephysics.numerics.ode_solvers.rk.irk;

/**
 * Objective wrap for the LU solver of the liear equations systems that is
 * defined for(above) complex arithmetic.
 * Source solver is placed in the <code>IRKLinearAlgebra</code>. class
 * @author Andrei Goussev
 */

public class LAEComplexSolverLU {
    LAComplexEquation eqn;
    private IRKLinearAlgebra linalg;
    int numEqn;
    double [][] luDecompositionRe;
    double [][] luDecompositionIm;
    double [] rightHandVectorRe;
    double [] rightHandVectorIm;
    int [] stuff;

    /**
     * Instances the solver for a system of equations
     * @param eqn the system of linear algebraic equations
     */
    public LAEComplexSolverLU(LAComplexEquation eqn) {
        this.linalg = new IRKLinearAlgebra();
        numEqn = eqn.getDimension();
        assignEquation(eqn);
        luDecompositionRe = new double[numEqn][numEqn];
        luDecompositionIm = new double[numEqn][numEqn];
        rightHandVectorRe = new double[numEqn];
        rightHandVectorIm = new double[numEqn];
        stuff = new int[numEqn];
    }

    /**
     * Assignes the system of equations to the solver. Assigning
     * system should have the same numer of equations
     * Several systems of equations can be sequentially asigned
     * to the one solver inctance
     * @param _equation the system of linear algebraic equations
     */
    public void assignEquation(LAComplexEquation _equation) {
        if (_equation.getDimension() == numEqn) {
            this.eqn = _equation;
        }
        else{
            System.err.println("Equation hasn't been added because dimension"); //$NON-NLS-1$
        }
    }

    /**
     * Performs LU decomposition of the left hand complex matrix of an equations system
     */
    public void initialize(){
        eqn.getMatrixes(luDecompositionRe, luDecompositionIm);
        linalg.decc(numEqn, numEqn, luDecompositionRe, luDecompositionIm, stuff);
    }

    /**
     * Solves an equations system
     * @param solutionRe the real component of the solution
     * @param solutionIm the imaginary component of the solution
     */
    public void resolve(double [] solutionRe, double [] solutionIm){
        if ((luDecompositionRe != null)&&((luDecompositionIm != null))) {
            eqn.getVectors(rightHandVectorRe, rightHandVectorIm);
            linalg.solc(numEqn,numEqn, luDecompositionRe,luDecompositionIm, rightHandVectorRe,rightHandVectorIm, stuff);
            System.arraycopy(rightHandVectorRe, 0, solutionRe, 0, numEqn);
            System.arraycopy(rightHandVectorIm, 0, solutionIm, 0, numEqn);
        }
    }
}
