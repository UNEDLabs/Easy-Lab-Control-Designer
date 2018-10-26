/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs.utils;

import org.opensourcephysics.displayejs.Point3D;

/**
 * Some utility functions
 */
public class VectorAlgebra {

  static public double norm (Point3D u) {
    return Math.sqrt(u.x*u.x + u.y*u.y + u.z*u.z);
  }

  static public Point3D crossProduct (Point3D u, Point3D v) {
    return new Point3D (u.y*v.z - u.z*v.y, u.z*v.x - u.x*v.z, u.x*v.y - u.y*v.x);
  }

  static public Point3D normalize (Point3D u) {
    double r = norm(u);
    return new Point3D (u.x/r, u.y/r, u.z/r);
  }

  static public Point3D normalTo (Point3D vector) {
    if      (vector.x==0.0) return new Point3D (1.0, 0.0, 0.0);
    else if (vector.y==0.0) return new Point3D (0.0, 1.0, 0.0);
    else if (vector.z==0.0) return new Point3D (0.0, 0.0, 1.0);
    else {
      double norm = Math.sqrt(vector.x*vector.x + vector.y*vector.y);
      return new Point3D (-vector.y/norm, vector.x/norm, 0.0);
    }
  }

} // end of clas