package org.opensourcephysics.numerics.ode_solvers.rk.irk;
/**
 * Created by IntelliJ IDEA.
 * User: Andrei
 * Date: 15.09.2004
 * Time: 11:56:59
 * To change this template use Options | File Templates.
 * Describes equation like:
 *
 * T * Lambda * invT (*) F(z) = multifier*1(*)z
 *
 */
// TODO: Split onto IRK and IRK with explicit Jacob.
public interface IRKequation{
    public void evaluate(double[][] freeVariable, double [][] residual);
    public double [][] getApproximation();
    public double [] getRealEigenvalues();
    public double [] getComplexEigenvalues();
    public double getScalarMultiplier();
    public void evaluateNonLinearComponent(double[] freeVariable, double [] functionValue);
    public void evaluateNonLinearComponentJacobian(double[] freeVariable, double [][] jacobian);
    public void directChangeOfVariables(double[][] freeVariable, double[][] substituted);
    public void inverseChangeOfVariables(double[][] substituted, double[][] freeVariable);
}
