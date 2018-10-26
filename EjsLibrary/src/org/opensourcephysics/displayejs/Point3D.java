/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.text.DecimalFormat;

/**
 * A simple object that has three double coordinates. The coordinates are public.
 */
public class Point3D {
  static protected final DecimalFormat scientificFormat = new DecimalFormat("0.###E0");
  static protected final DecimalFormat decimalFormat    = new DecimalFormat("0.00");

  /**
   * The x coordinate of the point
   */
  public double x=0.0;
  /**
   * The y coordinate of the point
   */
  public double y=0.0;
  /**
   * The z coordinate of the point
   */
  public double z=0.0;

  /**
   * Constructor that sets the x,y,z coordinates of the point
   */
  public Point3D (double _x, double _y, double _z) {
    x = _x;
    y = _y;
    z = _z;
  }

  /**
   * Sets the x,y,z coordinates of the point
   */
  public void setXYZ (double _x, double _y, double _z) {
    x = _x;
    y = _y;
    z = _z;
  }

  public double[] toArray() { return new double[] { x,y,z}; }

  public String toString() {
    String msg;
    if((Math.abs(x) > 100) || (Math.abs(x) < 0.01) || (Math.abs(y) > 100) || (Math.abs(y) < 0.01)) {
      msg = "x=" + scientificFormat.format(x) + "  y=" + scientificFormat.format(y) + "  z=" + scientificFormat.format(z);
    } else {
      msg = "x=" + decimalFormat.format(x) + "  y=" + decimalFormat.format(y) + "  z=" + decimalFormat.format(z);
    }
    return msg;
  }


}

