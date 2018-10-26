package org.colos.ejs.model_elements.roboticsLabs.robots.TX60L;

import org.colos.ejs.library.Model;



import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.roboticsLabs.robots.RobotTX60LWithComponents;

/**
 * @author Almudena Ruiz
 * @version Noviembre 2012
 */
public class RobotTX60LWithComponentsAdapter extends RobotTX60LWithComponents {
	private Model mModel;
	private String mIP, mPortNumber, mUser, mPassword;
	
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

	//public RobotTX60LWithComponentsAdapter(Model model, String ipAddress, String portNumber, String user, String password, boolean basement, boolean belt, boolean tool) {
	public RobotTX60LWithComponentsAdapter(Model model, String ipAddress, String portNumber, String user, String password, boolean basement, boolean tool) {
			
		super(basement, tool);
		mModel = model;
		mIP = ipAddress.trim();
		mPortNumber = portNumber.trim();
		mUser = user.trim();
		mPassword = password;
	}

	// ------------------------------------------------
	// Remote communication with the robot
	// -----------------------------------------------

	public void connect() throws Exception {
		String ipAddress = ModelElementsUtilities.getValue(mModel, mIP);
		int portNumber = ModelElementsUtilities.getIntegerValue(mModel,mPortNumber);
		String user = ModelElementsUtilities.getValue(mModel, mUser);
		String password = ModelElementsUtilities.getValue(mModel, mPassword);
		super.connect(ipAddress, portNumber, user, password);
	
	}
}
