/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

/**
 * Extends org.opensourcephysics.display.Measurable with a third dimension, Z.
 */
public interface Measurable3D extends org.opensourcephysics.display.Measurable {

  /**
   * Gets the minimum z needed to draw this object.
   * @return minimum
   */
  public double getZMin();

  /**
   * Gets the maximum z needed to draw this object.
   * @return maximum
   */
  public double getZMax();

}

