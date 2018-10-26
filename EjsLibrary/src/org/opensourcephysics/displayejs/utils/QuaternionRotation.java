/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs.utils;

import org.opensourcephysics.numerics.Transformation;

/**
 * <p>Title: QuaternionRotation</p>
 * <p>Description: A class to implement rotations using quaternions.
 * The quaternion doesn't need to be unitary, but it can't be null
 * (i.e. must have non-zero norm).</p>
 * <p>Copyright: Copyright (c) Esquembre Jan 2004</p>
 * <p>Company: Universidad de Murcia</p>
 * @author Francisco Esquembre (fem@um.es)
 * @version 1.0
 */
public class QuaternionRotation implements Transformation {

// -------------------------------
// Configuration variables
// -------------------------------
  /**
   *   The coordinates of the origin for the rotation
   */
  protected double ox=0, oy=0, oz=0;
  /**
   *   The coordinates of the quaternion
   */
  protected double q0, q1 , q2 , q3;

// -------------------------------
// Implementation variables
// -------------------------------
  /**
   * The coordinates of the rotation matrix
   */
  private double m1, m2, m3, m4, m5, m6, m7, m8, m9;

// -------------------------------
// Constructors and configuration
// -------------------------------

  /**
   * Creates a quaternion rotation from a fiven point and the four coordinates
   * of the quaternion.
   * @param s The scalar coordinate
   * @param u1 The first coordinate of the vector part
   * @param u2 The first coordinate of the vector part
   * @param u3 The first coordinate of the vector part
   */
  public QuaternionRotation (double s, double u1, double u2, double u3) {
    q0 = s; q1 = u1; q2 = u2; q3 = u3;  // The minus signs get a right-handed rotation
    computeRotationMatrix();
  }

  /**
   * Constructor only for the use of its subclasses. DO NOT USE!
   */
  QuaternionRotation () {
    q0 = q1 = q2 = q3 = 0.0;
    // No need to compute the rotationmatrix
  }

  /**
   * Sets the four coordinates of the quaternion
   * @param s The scalar coordinate
   * @param u1 The first coordinate of the vector part
   * @param u2 The first coordinate of the vector part
   * @param u3 The first coordinate of the vector part
   */
  public void setCoordinates (double s, double u1, double u2, double u3) {
    q0 = s; q1 = u1; q2 = u2; q3 = u3; // The minus signs get a right-handed rotation
    computeRotationMatrix();
  }

  /**
   * Sets the coordinates of the origin of the rotation.
   * By default these coordinates are all zero.
   * @param x The X coordinate of the origin
   * @param y The Y coordinate of the origin
   * @param z The Z coordinate of the origin
   */
  public void setOrigin (double x, double y, double z) {
    ox = x; oy = y; oz = z;
  }

// -------------------------------------
// Implementation of Transformation
// -------------------------------------

  public Object clone () {
    try { return super.clone(); }
    catch (CloneNotSupportedException exc) { exc.printStackTrace(); return null; }
  }


  public double[] direct (double[] input) {
    input[0] -= ox; input[1] -= oy; input[2] -= oz;
    double r0 = input[0]*m1+input[1]*m2+input[2]*m3;
    double r1 = input[0]*m4+input[1]*m5+input[2]*m6;
    input[2] =  input[0]*m7+input[1]*m8+input[2]*m9;
    input[0] = r0; input[1] = r1;
    input[0] += ox; input[1] += oy; input[2] += oz;
    return input;
  }

  public double[]  inverse (double[] input) throws UnsupportedOperationException {
    input[0] -= ox; input[1] -= oy; input[2] -= oz;
    double r0 = input[0]*m1+input[1]*m4+input[2]*m7;
    double r1 = input[0]*m2+input[1]*m5+input[2]*m8;
    input[2] =  input[0]*m3+input[1]*m6+input[2]*m9;
    input[0] = r0; input[1] = r1;
    input[0] += ox; input[1] += oy; input[2] += oz;
    return input;
  }

// -----------------------------------------
// End of Implementation of Transformation
// -----------------------------------------

  /**
   * Computes the direct rotation matrix of this quaternion rotation
   */
  protected void computeRotationMatrix () {
    double q0q0 = q0 * q0, q0q1 = q0 * q1, q0q2 = q0 * q2, q0q3 = q0 * q3;
    double q1q1 = q1 * q1, q1q2 = q1 * q2, q1q3 = q1 * q3;
    double q2q2 = q2 * q2, q2q3 = q2 * q3;
    double q3q3 = q3 * q3;
    // Divide by the norm in case the quaternion is a non-unit one
    double norm = q0q0+q1q1+q2q2+q3q3;
    m1 = (q0q0+q1q1-q2q2-q3q3)/norm; m4 = 2*(q0q3+q1q2)/norm        ; m7 = 2*(-q0q2+q1q3)/norm       ;
    m2 = 2*(-q0q3+q1q2)/norm       ; m5 = (q0q0-q1q1+q2q2-q3q3)/norm; m8 = 2*( q0q1+q2q3)/norm       ;
    m3 = 2*( q0q2+q1q3)/norm       ; m6 = 2*(-q0q1+q2q3)/norm       ; m9 = (q0q0-q1q1-q2q2+q3q3)/norm;
  }

}


