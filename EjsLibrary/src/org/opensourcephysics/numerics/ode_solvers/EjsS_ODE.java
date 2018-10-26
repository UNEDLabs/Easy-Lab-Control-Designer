/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

import org.opensourcephysics.numerics.ODE;

/**
 * EjsS_ODE is an interface for ODEs in EjsS 5.0 and beyond
 * 
 * @author Francisco Esquembre. January 2014
 */
public interface EjsS_ODE extends ODE {

  /**
   * Returns the actual event solver method
   * @return 
   * @see org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver
   */
  public InterpolatorEventSolver getEventSolver(); 
  
  /**
   * Allows changing the solver in run-time. 
   * @param aClass A class that implements org.opensourcephysics.numerics.ode_solvers.SolverEngine
   * @see  org.opensourcephysics.numerics.ode_solvers.SolverEngine
   */
  public void setSolverClass (Class<?> aClass);
  
  /**
   * Allows changing the solver in run-time. 
   * @param className The name of a class that points to a right solver, as indicated in EJS combo box: Euler, EulerRichardson,
   * @return the name of the implementing class to be used 
   * @see org.opensourcephysics.numerics.ode_solvers.SolverEngine
   */
  public String setSolverClass (String className);
  
  /**
   * Initializes the solver. 
   * This is done automatically at the end of the initialization, but the user may require to do
   * it 'by hand' if she want to step the solver before the initialization completes.
   */
  public void initializeSolver ();
  
  /**
   * Steps the ODE solution as much as the ODE increment indicates.
   * If an event is found and the "End step at event" checkbox is checked, the step is interrupted at the event.
   * @return the step actually taken
   */
  public double step ();
  
  /**
   * Steps the ODE solution as much as the given tolerance allows. 
   * If a fixed-step method is used, this step equals the internal step (or the increment, if the internal step is not set).
   * The step is interrupted if an event is found.
   * @return the step actually taken
   */
  public double solverStep ();
  
  /**
   * Whether the ODE page is enabled. True by default for enabled pages.
   * @param _enabled
   */
  public void setEnabled (boolean _enabled);
  
  /**
   * Returns the current value  of the independent variable
   * @return
   */
  public double getIndependentVariableValue ();
  
  /**
   * Returns the value of the internal step size of the solver
   * @return
   */
  public double getInternalStepSize ();

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
