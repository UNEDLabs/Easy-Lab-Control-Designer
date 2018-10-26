package org.colos.roboticsLabs.robots.utils.restrictions;

/**
 * @author Almudena Ruiz
 */

abstract public class SphereRestriction implements Restriction {

  double a, b, c, r, sphere;

  public SphereRestriction(double[] center, double radius) {
    a = center[0];
    b = center[1];
    c = center[2];
    r = radius;

  }

  public boolean allowsPoint(double x, double y, double z) {   
    sphere = (x-a)*(x-a) + (y-b)*(y-b) + (z-c)*(z-c);
    return (sphere < (r*r));
  }

  abstract public void action(double x, double y, double z);
}
