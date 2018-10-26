package org.colos.roboticsLabs;



import org.colos.roboticsLabs.robots.AbstractRobot;
import org.colos.roboticsLabs.robots.utils.restrictions.Restriction;

public interface RoboticsManager {

	/**
	 * Adds a robot element
	 * @param robot the new robot element
	 * @param position the robot position in Cartesian coordinates (x,y,z)
	 */
	public void addRobot(AbstractRobot robot, double [] position, boolean addView);

	/**
	 * Checks if the labs has robots or not
	 * 
	 * @return true if there are robots or false in otherwise
	 */
	public boolean hasRobot();
	
	/**
	 * Gets the number of robots added 
	 * @return the number of robots
	 */
	
	public int getNumberOfRobots();

	/**
	 * Removes the robot element from the lab
	 */

	public void removeRobot(AbstractRobot robot);
	
	

	
	/**
	 * Adds a restriction to the laboratory
	 * 
	 * @param component
	 */
	public void addRestrictionLab(Restriction rest);

	/**
	 * Checks if the labs has restrictions or not
	 * 
	 * @return true if there are restrictions or false in otherwise
	 */
	
	 public boolean hasRestrictionsLab();

}
