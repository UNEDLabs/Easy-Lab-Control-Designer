package org.opensourcephysics.numerics.dde_solvers.rk.irk;

/**
 * Objective wrap for the LU solver of the liear equations systems.
 * Source solver is placed in the <code>IRKLinearAlgebra</code>. class
 * @author Andrei Goussev
 */
public class LAESolverLU {
    private LAEquation eqn;
    private IRKLinearAlgebra linalg;
    private int numEqn;
    double [][] luDecomposition;
    double [] rightHandVector;
    int [] stuff;

    public LAEquation getEquation() {
        return eqn;
    }

    /**
     * Instances the solver considering that system of equations
     * will be assigned later
     */
    public LAESolverLU(int numEqn) {
        this.linalg = new IRKLinearAlgebra();
        this.numEqn = numEqn;
        luDecomposition = new double[numEqn][numEqn];
        rightHandVector = new double[numEqn];
        stuff = new int[numEqn];
    }

    /**
     * Instances the solver for a system of equations
     * @param eqn the system of linear algebraic equations
     */
    public LAESolverLU(LAEquation eqn) {
        this.linalg = new IRKLinearAlgebra();
        assignEquation(eqn);
        numEqn = eqn.getDimension();
        luDecomposition = new double[numEqn][numEqn];
        rightHandVector = new double[numEqn];
        stuff = new int[numEqn];
    }

    /**
     * Assignes the system of equations to the solver. Assigning
     * system should have the same numer of equations
     * Several systems of equations can be sequentially asigned
     * to the one solver inctance
     * @param _equation the system of linear algebraic equations
     */
    public void assignEquation(LAEquation _equation){
        if (_equation.getDimension() == numEqn) {
            this.eqn = _equation;
        }
        else{
            System.err.println("Equation hasn't been added because dimension"); //$NON-NLS-1$
        }
    }

    /**
     * Performs LU decomposition of the left hand matrix of an equations system
     */
    public void initialize(){
        eqn.getMatrix(luDecomposition);
        linalg.dec(numEqn, numEqn, luDecomposition, stuff);
    }

    /**
     * Solves an equations system
     * @param solution the solution
     */
    public void resolve(double [] solution){
        if (luDecomposition != null) {
            eqn.getVector(rightHandVector);
            linalg.sol(numEqn,numEqn, luDecomposition, rightHandVector, stuff);
            System.arraycopy(rightHandVector, 0, solution, 0, numEqn);
        }
    }
}
