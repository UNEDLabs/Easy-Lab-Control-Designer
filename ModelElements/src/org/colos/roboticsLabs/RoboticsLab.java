package org.colos.roboticsLabs;


import java.util.Hashtable;

import org.colos.roboticsLabs.components.AbstractComponent;
import org.colos.roboticsLabs.robots.AbstractRobot;
import org.colos.roboticsLabs.robots.utils.restrictions.Restriction;
import org.colos.roboticsLabs.robots.utils.restrictions.BlockRestriction;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.Group;

import com.jme.bounding.OrientedBoundingBox;


public class RoboticsLab implements RoboticsManager {

	private Hashtable<AbstractRobot, String> mRobotHashtable = new Hashtable<AbstractRobot, String>();
	private Hashtable<AbstractComponent, String> mComponentHashtable = new Hashtable<AbstractComponent, String>();

	final DrawingPanel3D mRoboticsLabPanel = new DrawingPanel3D(DrawingPanel3D.IMPLEMENTATION_JAVA3D);

	// mRoboticsLabPanel.getVisualizationHints().setDecorationType(VisualizationHints.DECORATION_CUBE);
	// mRoboticsLabPanel.setPreferredMinMax(-1.5, 1.5, -1.5, 1.5, 0.0, 2.5);
	// mRoboticsLabPanel.getCamera().adjust();

	// Restrictions
	private java.util.Set<Restriction> mRestrictionLabSet = new java.util.HashSet<Restriction>();

	public RoboticsLab() {}

	/**
	 * Method to add a robot in the robotics laboratory
	 * @param robot the robot element
	 * @param robotGroup the 3D Group to add the view of the element
	 * @param addView true to display the element in the virtual lab and false in otherwise
	 */
	public void addRobot(AbstractRobot robot, Group robotGroup, boolean addView) {
		if(robotGroup == null) return;
		double [] position = robotGroup.getPosition();
		String robotPosition = "";
		for (int i = 0; i < position.length; i++) 
			robotPosition = robotPosition + Double.toString(position[i]) + " , ";
		
		if (!mRobotHashtable.containsValue(robotPosition) && !mComponentHashtable.containsValue(robotPosition)) {
			mRobotHashtable.put(robot, robotPosition);
			if (addView){
				mRoboticsLabPanel.addElement(robotGroup);
				robot.addToViewGroup(robotGroup);
			}

		} else
			System.out .println("Error: The position for the robot "
							+ (getNumberOfRobots() + 1)
							+ " is impossible. (There exists an element with the same position in the lab)");
	}

	/**
	 * Method to add a robot in the robotics laboratory
	 * @param robot the robot element
	 * @param position the robot position in the laboratory
	 * @param addView true to display the element in the virtual lab and false in otherwise
	 */
	public void addRobot(AbstractRobot robot, double[] position, boolean addView) {
		String robotPosition = "";
		for (int i = 0; i < position.length; i++) {
			robotPosition = robotPosition + Double.toString(position[i])
					+ " , ";
		}
		if (!mRobotHashtable.containsValue(robotPosition)
				&& !mComponentHashtable.containsValue(robotPosition)) {
			mRobotHashtable.put(robot, robotPosition);
			if (addView)
				addToViewGroup(robot, position);

		} else
			System.out
					.println("Error: The position for the robot "
							+ (getNumberOfRobots() + 1)
							+ " is impossible. (There exists an element with the same position in the lab)");
	}

	/**
	 * Returns if the lab has robots
	 */
	public boolean hasRobot() {return !mRobotHashtable.isEmpty();}

	/**
	 * Gets the number of robots added in the lab
	 */
	public int getNumberOfRobots() {return mRobotHashtable.size();}

	/**
	 * Removes a robot element of the lab
	 * @param robot the robot element
	 */
	public void removeRobot(AbstractRobot robot) {
		mRobotHashtable.remove(robot);
		robot.removeFromViewGroup();
	}
	

	/**
	 * Method to add a component in the robotics laboratory
	 * @param component the component element
	 * @param position the component position in the laboratory
	 * @param addView true to display the element in the virtual lab and false in otherwise
	 */
	public void addComponent(AbstractComponent component, double[] position, boolean addView) {
		String componentPosition = "";
		for (int i = 0; i < position.length; i++) {
			componentPosition = componentPosition
					+ Double.toString(position[i]) + " , ";
		}
		if (!mComponentHashtable.containsValue(componentPosition)
				&& !mRobotHashtable.containsValue(componentPosition)) {
			mComponentHashtable.put(component, componentPosition);
			if (addView)
				addToViewGroup(component, position);

			double xmin = position[0] - component.getWidth() / 2.;
			double xmax = position[0] + component.getWidth() / 2.;
			double ymin = position[1] - component.getLength() / 2.;
			double ymax = position[1] + component.getLength() / 2.;
			double zmin = position[2] - component.getHigh() / 2.;
			double zmax = position[2] + component.getHigh();

			//System.out.println("min y max:" + xmin + " " + xmax + " " + ymin + " " + ymax + " " + zmin + " " + zmax);

			addRestrictionLab(new BlockRestriction(new double[] { xmin, xmax,ymin, ymax, zmin, zmax }) {
				public void action(double x, double y, double z) {System.out.println("Error: The point below does not verify the component's restrictions of the lab: \n"
									+ "  X = "+ x + "\n  Y = "+ y + "\n  Z = " + z);
				}});
		} else
			System.out.println("Error: The position for the component " + (getNumberOfComponents() + 1) 
					+ " is impossible. (There exists an element with the same position in the lab)");
	}
	
	
	/**
	 * Returns if the lab has components
	 */

	public boolean hasComponent() {
		return !mComponentHashtable.isEmpty();
	}
	
	/**
	 * Gets the number of components added in the lab
	 */

	public int getNumberOfComponents() {
		return mComponentHashtable.size();
	}
	
	/**
	 * Removes a robot element of the lab
	 * @param robot the robot element
	 */

	public void removeComponent(AbstractComponent component) {
		mComponentHashtable.remove(component);
		component.removeFromViewGroup();
	}
	
	/*
	 * Lab Restrictions 
	 */

	/**
	 * Method to add a new restriction to the lab.
	 * 
	 * @param rest the lab restriction 
	 */
	public void addRestrictionLab(Restriction rest) {
		mRestrictionLabSet.add(rest);
	}

	/**
	 * Method to remove a restriction from the lab.
	 * 
	 * @param rest the lab restriction
	 */
	public void removeRestrictionLab(Restriction rest) {
		mRestrictionLabSet.remove(rest);
	}

	/**
	 * Checks if the robotics lab has restrictions or not
	 * 
	 * @return true if there are restrictions or false in otherwise
	 */

	public boolean hasRestrictionsLab() {
		return !mRestrictionLabSet.isEmpty();
	}
	
	/*
	 * Moving the robots
	 */
	
	public void trajectoryStep(AbstractRobot robot, double step) {
		if (robot.mTrajectory != null) {
			robot.mTrajectoryTime += step;
			try {
				moveRobot(robot, robot.mTrajectory.getPoint(robot.mTrajectoryTime));
			} catch (Exception exc) {
				System.out.println("Error: step invalid");
			}
		}
	}
	
	public void moveRobot(AbstractRobot robot, double[] q) throws Exception {
		double[] positionLab = robot.calculatePositionLab(q);
		if(!checkRestrictionLab(robot.calculatePositionLab(q)) || detectionCollision(robot))
			throw new Exception("Trying to move to invalid point: (" +positionLab[0] + "," + positionLab[1] + "," + positionLab[2] + ")");	
		
		else robot.moveToQ(q);
	}

	/*
	 * Auxiliary methods to complete the robotics lab
	 */
	
	public DrawingPanel3D getRoboticsPanel3D() {
		return mRoboticsLabPanel;
	}
	
	public Element encapsuleObject(Element element1, Element element2, int axis){
		ElementBox object = new ElementBox();
	//	object.setXYZ((element1.getY() + element2.getY())/2,(element1.getX() + element2.getX())/2,
		//		(element1.getZ() + element2.getZ())/2);
		//Axis X
		 if(axis == 1){
			 object.setSizeXYZ(element1.getSizeX() + element2.getSizeX(), 
					 Math.max(element1.getSizeY(),element2.getSizeY()),
					 Math.max(element1.getSizeZ(),element2.getSizeY()));
			 
			 object.setXYZ((element1.getY() + element2.getY())/2 + 2*object.getSizeY(),
					 (element1.getX() + element2.getX())/2, 
					 (element1.getZ() + element2.getZ())/2);
		
			// System.out.println("new : " +object.getX() + " " +object.getY() + " " +object.getZ() + " " );
		 }
		//Axis Y
		if(axis == 2){
			 object.setSizeXYZ(Math.max(element1.getSizeX(),element2.getSizeX()),
					 element1.getSizeY() + element2.getSizeY(),
					 Math.max(element1.getSizeZ(),element2.getSizeZ()));
			 
			 object.setXYZ((element1.getY() + element2.getY())/2,(element1.getX() + element2.getX())/2,
					 object.getSizeZ()/2);
			 
		}
		
		//Axis Z	 
		 if(axis == 3){
			 object.setSizeXYZ(Math.max(element1.getSizeX(),element2.getSizeX()), 
					 Math.max(element1.getSizeY(),element2.getSizeY()),
					 element1.getSizeZ() + element2.getSizeZ());
		 
		 	object.setXYZ((element1.getY() + element2.getY())/2,(element1.getX() + element2.getX())/2,
				 object.getSizeZ()/2);
		 }
		return object;
	}

	
	
	/*
	 * Private Methods
	 */
	
	private void addToViewGroup(AbstractRobot robot, double[] position) {
		Group robotGroup = new Group();
		robotGroup.setPosition(position);
		mRoboticsLabPanel.addElement(robotGroup);
		robot.addToViewGroup(robotGroup);
	}

	private void addToViewGroup(AbstractComponent component, double[] position) {
		Group componentGroup = new Group();
		componentGroup.setPosition(position);
		mRoboticsLabPanel.addElement(componentGroup);
		component.addToViewGroup(componentGroup);
	}


	/**
	 * Checks if the point verifies the lab restrictions
	 * @param p the point 
	 * @return true if the point verifies the lab restrictions and false in otherwise
	 */
	
	private boolean checkRestrictionLab(double[] p){
		if(!hasRestrictionsLab()) return true;
		else{
			for (Restriction rest : mRestrictionLabSet) {
				if (!rest.allowsPoint(p[0], p[1], p[2])) {
					rest.action(p[0], p[1], p[2]);
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * Checks if a robot collides with the other robots of the laboratory
	 * @param robot
	 * @return true if the robot collides with other robot and false in otherwise
	 */
	
	private boolean detectionCollision(AbstractRobot robot){
		if(getNumberOfRobots() <= 1) return false; // There is only this robot
		java.util.Set<OrientedBoundingBox> obb1Set = robot.getOrientedBoxes();
	    	
		for(AbstractRobot robot2: mRobotHashtable.keySet()){
			if(!robot.equals(robot2)){
				java.util.Set<OrientedBoundingBox> obb2Set = robot2.getOrientedBoxes();
				for(OrientedBoundingBox obb1: obb1Set){
					for(OrientedBoundingBox obb2: obb2Set){
					     if(obb1.intersectsOrientedBoundingBox(obb2)) {
					    	 System.err.println("Error: Possible Collision between " + robot + " and " + robot2);
					    	 return true;	
					     }
					}
				}
			}
		}
		return false;
	}
	
}