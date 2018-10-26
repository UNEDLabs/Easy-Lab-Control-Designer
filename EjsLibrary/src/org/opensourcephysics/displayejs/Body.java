/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : Jan 2004
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.numerics.Transformation;

/**
 * An interface for bodies in 3D. A body is an element which
 * has its own coordinate system (or frame). This frame is specified
 * by :
 * <ul>
 *   <li>An origin, i.e. a given point in the body (relative to the
 *   body's position).</li>
 *   <li>An internal transformation relative to this origin. </li>
 * </ul>
 * A transformation is just a mapping of R^3 into itself.
 * The transformation is relative to the origin in the sense that,
 * if this transformation preserves the point (0,0,0) of R^3,
 * then the origin will not be changed when the transformation is applied.</p>
 * Typically, the transformation is a rotation which leaves (0,0,0) fixed,
 * thus defining an orientation of the body. </p>
 * But transformations can be of many different types, thus allowing
 * for non-rigid bodies, for instance,...
 */
public interface Body {

  /**
   * Sets the origin of the body's own coordinate system.
   * This implies that:
   * <ul>
   *   <li>The (x,y,z) position of the element will correspond
   *    to this point of the body.</li>
   *   <li>The transformation will be applied relative to this point.</li>
   * </ul>
   * The point can be specified either relative to the body's dimension
   * (sizex,sizey,sizez) or in absolute coordinates.
   * If relative, the point is computed as (x+ox*sizex,y+oy*sizey,z+oz*sizez).
   * If absolute, as (x+ox,y+oy,z+oz). Specifying the point relatively
   * is useful so that the body will automatically make the neccesary
   * adjustments if it is subsequently resized.
   * </p>
   * Notice that nothing forces the origin point to be inside the body.
   * If the relative position are not within the 0-1 range, or the
   * absolute position are not within the corresponding 0-size range,
   * the reference point will lie outside of the body.
   *
   * @param ox
   * @param oy
   * @param oz
   * @param relative
   */
  public void setOrigin (double ox, double oy, double oz, boolean relativeToSize);

  /**
   * Sets the internal transformation of the element, that is, the
   * transformation that takes the standard XYZ axes to the body's
   * internal reference axes.
   * The transformation is copied and should not be accessed by users
   * directy. This implies that changing the original transformation
   * has no effect on the element unless a new setTransformation is invoked.
   * The transformation uses the body's position as its origin.
   * @param transformation the new transformation
   * @see Transformation
   */
  public void setTransformation (Transformation transformation);

  /**
   * This method converts a double[3] vector from the body's frame to
   * the space's frame.
   * @param the vector to be converted
   */
  public void toSpaceFrame (double[] vector);

  /**
   * This method converts a double[3] vector from the space's frame to
   * the body's frame. </p>
   * This only works properly if the internal transformation is not set
   * (i.e. it is the identity) or if it is invertible.
   * Otherwise, a call to this method will throw a
   * UnsupportedOperationException exception.
   * @param the vector to be converted
   */
  public void toBodyFrame (double[] vector) throws UnsupportedOperationException;

}

