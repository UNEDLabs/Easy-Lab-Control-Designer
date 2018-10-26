/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs.utils;

import org.opensourcephysics.numerics.Transformation;

/**
 * <p>Title: TransformationComposition</p>
 * <p>Description: A composition of two transformations.</p>
 * <p>Copyright: Copyright (c) Esquembre Jan 2004</p>
 * <p>Company: Universidad de Murcia</p>
 * @author Francisco Esquembre (fem@um.es)
 * @version 1.0
 */

public class TransformationComposition implements Transformation {
  /**
   *   The first transformation
   */
  protected Transformation first;
  /**
   *   The second transformation
   */
  protected Transformation second;


  /**
   * Creates a transformation made of two other transformations
   * @param first The first transformation
   * @param second The second transformation
   */
  public TransformationComposition (Transformation first, Transformation second) {
    this.first = first;
    this.second = second;
  }

  public Object clone () {
    return new TransformationComposition ((Transformation)first.clone(), (Transformation)second.clone());
  }

  public double[] direct (double[] input) {
    first.direct(input);
    second.direct(input);
    return input;
  }

  public double[] inverse (double[] input) {
    first.inverse(input);
    second.inverse(input);
    return input;
  }

}


