/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        Euler
 * Description:  1st order Euler solver
 * @author Francisco Esquembre
 * @version 1.0 December 2008
 */

public class Euler extends AbstractExplicitRKSolverInterpolator {

  public Euler(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  @Override
  protected void allocateOtherArrays() {}
  
  @Override
  protected int getNumberOfEvaluations() { return 1; }
  
  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state) {
    for(int i = 0;i<dimension;i++) _state[i] = initialState[i]+_step*initialRate[i];
    return _state;
  }

  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) {
    return computeIntermediateStep(_time-initialTime, _state);
  }
  
}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
