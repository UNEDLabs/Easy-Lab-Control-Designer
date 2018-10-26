/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : Jan 2004
 */

package org.opensourcephysics.displayejs.utils;

import org.opensourcephysics.numerics.Transformation;

/**
 * <p>Title: FunnyTransformation</p>
 * <p>Description: A class that implements a funny transformation.
 */
public class FunnyTransformation implements Transformation {

// -------------------------------
// Configuration variables
// -------------------------------
  double bending;

// -------------------------------
// Implementation variables
// -------------------------------

// -------------------------------
// Constructors and configuration
// -------------------------------

  public FunnyTransformation (double bending) {
    this.bending = bending;
  }

// -------------------------------------
// Implementation of Transformation
// -------------------------------------

  public Object clone () {
    try { return super.clone(); }
    catch (CloneNotSupportedException exc) { exc.printStackTrace(); return null; }
  }


  public double[] direct (double[] input) {
    input[1]  = input[1] + bending*input[0]*input[0];
    return input;
  }

  public double[]  inverse (double[] input) throws UnsupportedOperationException {
    input[1]  = input[1] - bending*input[0]*input[0];
    return input;
  }

// -----------------------------------------
// End of Implementation of Transformation
// -----------------------------------------

}


