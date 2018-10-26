/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs.utils;

import org.opensourcephysics.displayejs.Point3D;

/**
 * <p>Title: RotationAroundAxis</p>
 * <p>Description: A class to implement a rotation around an axis
 * The direction vector doesn't need to be unitary, but it must have non-zero
 * norm.</p>
 * <p>Copyright: Copyright (c) Esquembre Jan 2004</p>
 * <p>Company: Universidad de Murcia</p>
 * @author Francisco Esquembre (fem@um.es)
 * @version 1.0
 */
public class RotationAroundAxis extends QuaternionRotation {
  protected double rotationAngle;
  protected double dx, dy, dz;

  /**
   * Creates a rotation of a given angle around a given direction vector
   * @param angle The angle for the rotation
   * @param vector The vector around which to rotate
   */
  public RotationAroundAxis (double angle, Point3D vector) {
    rotationAngle = angle;
    dx = vector.x; dy = vector.y; dz = vector.z;
    setAngle (angle);
  }

  /**
   * Sets the angle of the rotation preserving the rotation axis.
   * @param anAngle the new angle
   */
  public void setAngle (double angle) {
    rotationAngle = angle;
    angle /= 2.0;
    double cos = Math.cos(angle), sin = Math.sin(angle);
    super.setCoordinates (cos,dx*sin,dy*sin,dz*sin);
  }

  /**
   * Sets the new rotation axis preserving the angle
   * @param vector the new axis
   */
  public void setAxis (Point3D vector) {
    dx = vector.x; dy = vector.y; dz = vector.z;
    double angle = rotationAngle/2.0;
    double cos = Math.cos(angle), sin = Math.sin(angle);
    super.setCoordinates (cos,dx*sin,dy*sin,dz*sin);
  }

}


