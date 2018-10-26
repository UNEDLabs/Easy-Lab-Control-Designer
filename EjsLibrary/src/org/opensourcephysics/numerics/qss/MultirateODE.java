/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.qss;

/**
 * This interface extends ODE to define systems of differential equations
 * in which the rate for each state variable can be computed independently
 * of the others. These ODEs are suitable for QSS integration methods.
 *
 * @author       Francisco Esquembre
 * @author       Ernesto Kofman
 *
 */
public interface MultirateODE extends org.opensourcephysics.numerics.ODE {

  /**
   * Gets the rate of a single state variable using the
   * argument's state.
   *
   * @param state  the state array
   * @param index  the index (position in the state array) of the state 
   * variable affected
   */
  public double getRate(double[] state, int index);

  /**
   * Returns the matrix of which states are needed to compute a rate.
   * i.e. row j indicates which states appear in the computation of rate j.
   * This method will be called very frequently (because only the ODE knows
   * if the matrix ever changes), hence it should not involve many computations.
   * 
   * Can be computed from the inverse matrix using
   * MultirateUtils.getReciprocalMatrix(getInverseIncidenceMatrix())
   * @param matrix int[][]
   */
  public int [][] getDirectIncidenceMatrix ();

  /**
   * Returns the matrix of which state rates are affected by each state.
   * i.e. row j indicates which rates are affected by state j.
   * This method will be called very frequently (because only the ODE knows
   * if the matrix ever changes), hence it should not involve many computations.
   * 
   * Can be computed from the direct matrix using
   * MultirateUtils.getReciprocalMatrix(getDirectIncidenceMatrix())
   * @param matrix int[][]
   */
  public int [][] getInverseIncidenceMatrix ();

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

