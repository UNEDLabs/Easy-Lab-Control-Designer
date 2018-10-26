package org.colos.roboticsLabs.robots.utils.restrictions;
/**
 * @author Almudena Ruiz
 */

abstract public class BoxRestriction implements Restriction {
	
  double xmin, xmax,ymin, ymax, zmin, zmax;

  public BoxRestriction(double[] bounds) {
    xmin = bounds[0];
    xmax = bounds[1];
    ymin = bounds[2];
    ymax = bounds[3];
    zmin = bounds[4];
    zmax = bounds[5];

  }

  public boolean allowsPoint(double x, double y, double z) {

    return (x >= xmin && x <= xmax && y >= ymin && y <= ymax && z >= zmin && z <= zmax);
  }

  abstract public void action(double x, double y, double z);

}
