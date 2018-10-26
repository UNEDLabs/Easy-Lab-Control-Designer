package org.colos.ejs.model_elements.roboticsLabs;

import org.colos.ejs.library.Model;
import org.colos.roboticsLabs.RoboticsLab;
import org.colos.roboticsLabs.components.AbstractComponent;
import org.colos.roboticsLabs.robots.AbstractRobot;


/**
 * @author Almudena Ruiz
 * @version October 2015
 */
public class RoboticsLabAdapter extends RoboticsLab {
	private Model mModel;
	/**
	 * Standard constructor to be called by the simulation
	 * @param model the model of the simulation
	 * @param ipAddress the IP address
	 * @param portNumber the port number
	 * @param user the user name
	 * @param password the password
	 * @param basement adds a base to the robot or not
	 * @param belt adds a belt to the simulation or not
	 * @param tool adds a tool to the robot or not
	 */

	public RoboticsLabAdapter(Model model) {
		super();
		mModel = model;
		}
	
	public void addRobot(AbstractRobot robot){
		double [] robotPosition = robot.getInitialPosition();
		super.addRobot(robot, robotPosition, false);
	}
		
	public void addComponent(AbstractComponent component){
		double [] componentPosition = component.getInitialPosition();
		super.addComponent(component, componentPosition, false);
	}	
	
}
