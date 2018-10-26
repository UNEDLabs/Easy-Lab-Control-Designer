package org.colos.roboticsLabs.robots.utils.restrictions;

/**
 * @author Almudena Ruiz
 */

public interface Restriction {
  
  /**
   * Checks if a point in Cartesian coordinates is valid for operation
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   * @return Returns true if a point in space is valid for operation and false otherwise
   */
  public boolean allowsPoint(double x, double y, double z);
  
  /**
   * Executes whatever correcting or information (warning) action is required if the point is invalid
   */
  
  public void action(double x, double y, double z);
}
