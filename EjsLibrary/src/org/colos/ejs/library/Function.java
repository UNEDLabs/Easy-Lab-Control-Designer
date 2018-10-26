/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

/**
 * A base class for functions.
 * Subclasses must overwrite the methods
 */

public class Function  {

  /**
   * evaluates the function
   */
  public double eval () { return 0.0; }

 /**
   * evaluates the function on a given variable
   */
  public double eval (double x) { return 0.0; }

  /**
    * evaluates the function on the given variables
    */
   public double eval (double x, double y) { return 0.0; }

   /**
     * evaluates the function on the given variables
     */
    public double eval (double[] x) { return 0.0; }

} // End of class


