package org.colos.roboticsLabs.robots;

import java.awt.Color;
import java.util.Hashtable;

import org.colos.roboticsLabs.robots.utils.maths.*;
import org.colos.roboticsLabs.robots.utils.restrictions.*;
import org.colos.roboticsLabs.robots.utils.trajectories.*;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.ElementTrail;
import org.opensourcephysics.drawing3d.Group;
import org.opensourcephysics.drawing3d.utils.transformations.AxisRotation;
import org.opensourcephysics.drawing3d.utils.TransformationWrapper;
import org.opensourcephysics.numerics.Transformation;
import org.opensourcephysics.drawing3d.Element;

import com.jme.bounding.OrientedBoundingBox;
import com.jme.math.Vector3f;

/**
 * @class AbstractRobot
 * @author Almudena Ruiz
 */
public abstract class AbstractRobot {
  // Common constants for calculations
  static protected final double sTO_RADIANS = Math.PI / 180.0;
  static protected final double sTO_DEGREES = 180 / Math.PI;
  static protected final double sFACTOR = 0.2;
  static protected final double PI2 = Math.PI / 2.0;

  // Characteristics  
  protected boolean mOpenTool = true;
  private int mSpeedPercent, mAccelPercent;
 // public double mP0x=0, mP0y=0, mP0z=0;
  protected double mP0x, mP0y, mP0z;
  protected double[] mRobotPosition = new double []{mP0x, mP0y, mP0z};
  private double[] mSpeedLimit, mAccLimit;
  protected double[] mDHParameters;
  protected double[] mCurrentQ; // the current joint values
  private Matrix Tint = new Matrix (4,4);
  // Connection
  protected boolean mIsConnected;
  // Trajectory
  //private Trajectory mTrajectory = null;
 // private double mTrajectoryTime=0;
 
  public Trajectory mTrajectory = null;
  public double mTrajectoryTime=0;
  // Restrictions
  private java.util.Set<Restriction> mRestrictionSet = new java.util.HashSet<Restriction>();
  
  //Collision Detection
  protected Hashtable<OrientedBoundingBox,Element> obbTable = new Hashtable<OrientedBoundingBox,Element>();
  protected java.util.ArrayList<OrientedBoundingBox> obbList = new java.util.ArrayList<OrientedBoundingBox>();
  protected Hashtable<OrientedBoundingBox,Element[]> obbTriedroTable = new Hashtable<OrientedBoundingBox,Element[]>();
  
  // Graphics
  protected boolean mHasViewGroup = false;
  private boolean mAddedToView = false;
  private Group mRobotGroup, mHostGroup;
  private ElementTrail mTrajectoryTrail;

  // ---------------------------------------
  // Information about the robot
  // ---------------------------------------

  /**
   * Protected constructor. To be called only from subclasses
   */
  protected AbstractRobot() {
    int dof = getDOF();
    mSpeedLimit = new double[dof];
    mAccLimit = new double[dof];
    mDHParameters = new double[dof*4];
    mCurrentQ = new double[dof];
    for (int i = 0; i < dof; i++) {
      mSpeedLimit[i] = getJointSpeedMaximum(i + 1);
      mAccLimit[i] = getJointAccelerationMaximum(i + 1);
    }
    for (int i = 0, n=dof*4; i<n; i++) {
      mDHParameters[i] = getDHParameters()[i];
    }
  }
 
  /**
   * Gets the Oriented Bounding Boxes (OBBs) of the robot 
   * @return a set with the OBBs of the robot
   */
 
  public java.util.Set<OrientedBoundingBox> getOrientedBoxes() {	
		for (OrientedBoundingBox box : obbTable.keySet()) {
	      Element objectForBox = obbTable.get(box);
	      Vector3f extent = box.getExtent();
	      double[] center = objectForBox.toSpaceFrame(new double[] {0,0,0});
	      double[] xTip = objectForBox.toSpaceFrame(new double[] {extent.x,0,0});
	      double[] yTip = objectForBox.toSpaceFrame(new double[] {0,extent.y,0});
	      double[] zTip = objectForBox.toSpaceFrame(new double[] {0,0,extent.z});
	      Vector3f xAxis = new Vector3f((float) (xTip[0]-center[0]),(float) (xTip[1]-center[1]), (float) (xTip[2]-center[2]));
	      Vector3f yAxis = new Vector3f((float) (yTip[0]-center[0]),(float) (yTip[1]-center[1]), (float) (yTip[2]-center[2]));
	      Vector3f zAxis = new Vector3f((float) (zTip[0]-center[0]),(float) (zTip[1]-center[1]), (float) (zTip[2]-center[2]));
	 
	      box.setCenter(new Vector3f((float)(center[0]),(float) center[1], (float) center[2]));
	      box.setXAxis(xAxis.normalize());
	      box.setYAxis(yAxis.normalize());
	      box.setZAxis(zAxis.normalize());
	      
	     // System.out.println("Extend OBB" + " " + extent.x + " " + extent.y + " " +extent.z); 
		//   System.out.println ("Center of "+box+" = "+ box.getCenter());
			
	     //System.out.println ("Center of "+box+" = "+box.getCenter());
	  /*    
	      Element[] triedro = obbTriedroTable.get(box);
	      
//	      System.out.println ("Centro TX60L es "+ (center[0]) + " " +  center[1] + " " +  center[2]);
//	      System.out.println ("Extend TX60L es "+ extent.getX() + " " + extent.getY() + " " +  extent.getZ());
	      triedro[0].setXYZ(center[0],center[1],center[2]);
	      triedro[0].setSizeXYZ(xAxis.x,xAxis.y,xAxis.z);
	      triedro[1].setXYZ(center[0],center[1],center[2]);
	      triedro[1].setSizeXYZ(yAxis.x,yAxis.y,yAxis.z);
	      triedro[2].setXYZ(center[0],center[1],center[2]);
	      triedro[2].setSizeXYZ(zAxis.x,zAxis.y,zAxis.z);
	      
	  	  for (int i=0; i<3; i++) {
	  	  //   if (triedro[i].getPanel()==null) panel3D.addElement(triedro[i]);
	  	  }
	*/
		}
		return obbTable.keySet();
	  }
	 
  /**
   * Gets an arrayList with the Oriented Bounding Boxes (OBBs) of the robot 
   * @return an arrayList with the OBBs of the robot
   */
	  
	  public  java.util.ArrayList<OrientedBoundingBox> getOrderedOrientedBoxes() {
		 // System.out.println("Extend2"); 
		  for (int i=0; i < obbList.size(); i++) {
			   OrientedBoundingBox obb = obbList.get(i);		      
			   Element objectForBox = obbTable.get(obb);
			   Vector3f extent = obb.getExtent();
			   double[] center = objectForBox.toSpaceFrame(new double[] {0,0,0});
			   double[] xTip = objectForBox.toSpaceFrame(new double[] {extent.x,0,0});
			   double[] yTip = objectForBox.toSpaceFrame(new double[] {0,extent.y,0});
			   double[] zTip = objectForBox.toSpaceFrame(new double[] {0,0,extent.z});		  
			   Vector3f xAxis = new Vector3f((float) (xTip[0]-center[0]),(float) (xTip[1]-center[1]), (float) (xTip[2]-center[2]));
			   Vector3f yAxis = new Vector3f((float) (yTip[0]-center[0]),(float) (yTip[1]-center[1]), (float) (yTip[2]-center[2]));
			   Vector3f zAxis = new Vector3f((float) (zTip[0]-center[0]),(float) (zTip[1]-center[1]), (float) (zTip[2]-center[2]));	 
			   
			   obb.setCenter(new Vector3f((float)(center[0]),(float) center[1], (float) center[2]));
			   obb.setXAxis(xAxis.normalize());
			   obb.setYAxis(yAxis.normalize());
			   obb.setZAxis(zAxis.normalize());
			//   System.out.println("Extend OBB" + i + " " + extent.x + " " + extent.y + " " +extent.z); 
			//   System.out.println ("Center of "+obb+" = "+ obb.getCenter());
				
		   }
			return obbList;
	  }
	  
	  /**
	   * Draws the Oriented Bounding Boxes of the robot
	   * @param panel3D
	   */
  
	  public void drawOrientedBoxes(DrawingPanel3D panel3D) {
		for (OrientedBoundingBox box : obbTable.keySet()) {
	      Element objectForBox = obbTable.get(box);
	      Vector3f extent = box.getExtent();
	      double[] center = objectForBox.toSpaceFrame(new double[] {0,0,0});
	      double[] xTip = objectForBox.toSpaceFrame(new double[] {extent.x,0,0});
	      double[] yTip = objectForBox.toSpaceFrame(new double[] {0,extent.y,0});
	      double[] zTip = objectForBox.toSpaceFrame(new double[] {0,0,extent.z});
	      Vector3f xAxis = new Vector3f((float) (xTip[0]-center[0]),(float) (xTip[1]-center[1]), (float) (xTip[2]-center[2]));
	      Vector3f yAxis = new Vector3f((float) (yTip[0]-center[0]),(float) (yTip[1]-center[1]), (float) (yTip[2]-center[2]));
	      Vector3f zAxis = new Vector3f((float) (zTip[0]-center[0]),(float) (zTip[1]-center[1]), (float) (zTip[2]-center[2]));
	      box.setCenter(new Vector3f((float) center[0],(float) (center[1]), (float) center[2]));
	      box.setXAxis(xAxis.normalize());
	      box.setYAxis(yAxis.normalize());
	      box.setZAxis(zAxis.normalize());
	      	      
	      Element[] triedro = obbTriedroTable.get(box);
	      triedro[0].setXYZ(center[0],center[1],center[2]);
	      triedro[0].setSizeXYZ(xAxis.x,xAxis.y,xAxis.z);
	      triedro[1].setXYZ(center[0],center[1],center[2]);
	      triedro[1].setSizeXYZ(yAxis.x,yAxis.y,yAxis.z);
	      triedro[2].setXYZ(center[0],center[1],center[2]);
	      triedro[2].setSizeXYZ(zAxis.x,zAxis.y,zAxis.z);
	      
	  	  for (int i=0; i<3; i++) {
	  		  if (triedro[i].getPanel()==null) panel3D.addElement(triedro[i]);
	  	  }
		}
	  }
  

  /**
   * Gets the number of degrees of freedom (DOF) of the robot
   */
  abstract public int getDOF();

  /**
   * Gets the length of arrays with Cartesian information of points
   */
  abstract public int getCartesianArraysLength();

  /**
   * Returns the default position of the joints
   * @return a newly allocated array with the home positions for the joints
   */
  abstract public double[] getHome();

  /**
   * Returns the default position of the effector in Cartesian coordinates
   * @return a newly allocated array with the home position of the end-effector
   *         and its orientation
   */
  public double[] getCartesianHome() {
	//  double [] home = getHome();
	/*  System.out.println("Joint Position");
	  for(int i = 0; i< home.length; i++){
		 System.out.print(" " + home[i]); 
	  }
	  System.out.println();*/
    return inverseKinematics(getHome(), new double[getCartesianArraysLength()], true, true, true);
  }

  /**
   * Required to link joints to DH parameters
   * @param index
   */
  abstract protected int getDHQ(int index);

  /**
   * Returns the Denavit-Hartenberg parameters of the robot
   * @return a newly allocated array with the parameters
   */
  abstract public double[] getDHParameters();


  /**
   * Per robot information required to implement the forward kinematics 
   */
  abstract protected double [] calculateDH(double [] q);

  /**
   * Translates a point from joint coordinates to Cartesian coordinates
   * @param q the joint coordinates (q1, q2, ...,qn)
   * @param coordinates a place holder for the result of the transformation, if null a new array is allocated
   * @return the Cartesian coordinates of the end-effector and its orientation,
   *         null if error
   */
  public double[] forwardKinematics(double[] q, double[] coordinates) {
    if (q.length!=getDOF()) return null;
    if (coordinates==null) coordinates = new double[getCartesianArraysLength()];   
    double[] coord = calculateDH(q);
    for (int i = 0; i < 3; i++) coordinates[i] = coord[i];
    for (int i = 3, n=coordinates.length; i<n; i++) coordinates[i] = q[i];
    
  //  double [] absoluteCoordinates = calculatePositionLab(coordinates);
  // System.out.println("prueba " + absoluteCoordinates[0] + " " + absoluteCoordinates[1] + " "  + absoluteCoordinates[2] +  " " + absoluteCoordinates[3]);
	
    return coordinates;
  }

  /**
   * Translates a point from Cartesian coordinates to joint coordinates
   * @param coordinates the Cartesian coordinates of the point in space (x, y, z, rx, ry, rz)
   * @param q a place holder for the result of the transformation, if null a new array is allocated
   * @param shoulder true if shoulder is left or false in otherwise
   * @param elbow true if elbow is up or false in otherwise
   * @param wrist true if wrist is up or false in otherwise
   * @return the joint position or null if there exist an error
   */
  abstract public double[] inverseKinematics(double[] coordinates, double[] q, boolean shoulder, boolean elbow, boolean wrist);

  // ---------------------------------------
  // Positioning the robot
  // ---------------------------------------

  /**
   * Moves the robot to the initial position
   */
  public void home() throws Exception {
      moveToQ(getHome());
  }

  /**
   * Returns the current position of the joints
   * @return a newly allocated array with the home positions for the joints
   */
  public double[] getCurrentQ() { 
    int dof = getDOF();
    double[] currentQ = new double[dof];
    System.arraycopy(mCurrentQ, 0, currentQ, 0, dof);
    return currentQ;
  }
  
  public double[] getInitialPosition(){
	  double[] robotPosition = new double[3];
	  System.arraycopy(mRobotPosition, 0, robotPosition, 0, 3);
	  //System.out.println("posiciones lab: " + robotPosition[0] + " " +   robotPosition[1] + " " +  robotPosition[2]);
	  return robotPosition;
  }
 
  public double[] calculatePositionLab(double[] q){
	 double[] p = forwardKinematics(q, null);
	 return new double [] {p[0] + getInitialPosition()[0], p[1] + getInitialPosition()[1], p[2] + getInitialPosition()[2]};
	
  }
  
  
  /**
   * Moves the robot to the given joint position
   * @param q the joint coordinates (q1, q2, ..., qn)
   * @return true if the move is possible or false in otherwise
   */
  public void moveToQ(double[] q) throws Exception{
	if(!checkJointPosition(q)) throw new Exception("The values of the joints are invalid.");
    
	if (hasRestrictions()) {
      double[] p = forwardKinematics(q,null);
      if (!checkValidity(p)) throw new Exception("Trying to move to invalid point: ("+p[0]+","+p[1]+","+p[2]+")");
    }
	
    System.arraycopy(q, 0, mCurrentQ, 0, getDOF());
    if (mHasViewGroup){ 
    	updateView();
    	if(autocollisionDetection()) throw new Exception(" Autocollision: Trying to move to invalid point");
    }
    if (mIsConnected) moveRealRobot();
  }
 
  /**
   * Moves the robot to the given Cartesian position
   * @param point the Cartesian coordinates (x, y ,z, Rx, Ry, Rz)
   * @return true if the move is possible or false in otherwise
   */
  public void moveToC(double[] point) throws Exception {
    moveToQ(inverseKinematics(point, null, true,true, true));
  }

  /**
   * Moves the real robot to the actual position. 
   */

  abstract protected void moveRealRobot() throws Exception;

  /**
   * Opens the robot tool. 
   */
  abstract public void openTool() throws Exception;

  /**
   * Closes the robot tool.
   */
  abstract public void closeTool() throws Exception;

  //---------------------------------------
  // Trajectories
  // ---------------------------------------

  /**
   * Sets a trajectory for the robot
   * @param trajectory the trajectory
   * @return a new trajectory for the robot
   */
  public Trajectory setTrajectory(Trajectory trajectory) {
    mTrajectory = trajectory;
    mTrajectoryTime = 0;
    return mTrajectory;
  }

  /**
   * Defines and sets a Linear trajectory with the variables in joint coordinates.
   * @param qi the starting point of the trajectory
   * @param qf the end point of the trajectory
   * @param duration the duration of the trajectory
   * @return Linear trajectory
   */
  public Trajectory setLinearTrajectory(double[] qi, double[] qf, double duration) {
    return setTrajectory(new LinearTrajectory(qi, qf, duration));
  }

  /**
   * Defines and sets a Linear trajectory with the variables in Cartesian coordinates.
   * @param xi the starting point of the trajectory
   * @param xf the end point of the trajectory
   * @param duration the duration of the trajectory
   * @return Linear Trajectory
   */
  public Trajectory setCartesianLinearTrajectory(double[] xi, double[] xf, double duration) {
    return setTrajectory(new LinearTrajectory(inverseKinematics(xi, null, true, true, true),
        inverseKinematics(xf, null, true, true, true), duration));
  }

  /**
   * Defines and sets a Spline trajectory with an intermediate point and the variables in joint coordinates
   * @param qi the starting point of the trajectory
   * @param qInt the intermediate point of the trajectory
   * @param qf the end point of the trajectory
   * @param tInt the time corresponding to the intermediate point
   * @param duration the duration of the trajectory
   * @return SplineTrajectory
   */
  public Trajectory setSplineTrajectory(double[] qi, double[] qInt, double[] qf, double tInt, double duration) {
    return setTrajectory(new SplineTrajectory(qi, qInt, qf, tInt, duration));
  }

  /**
   * Defines and sets a Spline trajectory with an intermediate point and the variables in Cartesian coordinates
   * @param xi the starting point of the trajectory
   * @param xInt the intermediate point of the trajectory
   * @param xf the end point of the trajectory
   * @param tInt the time corresponding to the intermediate point
   * @param duration the duration of the trajectory
   * @return SplineTrajectory
   */
  public Trajectory setCartesianSplineTrajectory(double[] xi, double[] xInt, double[] xf, double tInt, double duration) {
    return setTrajectory(new SplineTrajectory(inverseKinematics(xi, null, true, true, true),
        inverseKinematics(xInt, null, true, true, true), inverseKinematics(xf, null, true, true, true), tInt, duration));
  }

  /**
   * Defines and sets a Spline trajectory with two intermediate point and the variables in joint coordinates
   * @param qi the starting point of the trajectory
   * @param qInt1 the first intermediate point of the trajectory
   * @param qInt2 the second intermediate point of the trajectory
   * @param qf the end point of the trajectory
   * @param tInt1 the time corresponding to the first intermediate point
   * @param tInt2 the time corresponding to the second intermediate point
   * @param duration the duration of the trajectory
   * @return SplineTrajectory2
   */
  public Trajectory setSpline2Trajectory(double[] qi, double[] qInt1, double[] qInt2, double[] qf, double tInt1, double tInt2, double duration) {
    return setTrajectory(new Spline2Trajectory(qi, qInt1, qInt2, qf, tInt1, tInt2, duration));
  }

  /**
   * Defines and sets a Spline trajectory with two intermediate point and the variables in Cartesian coordinates
   * @param xi the starting point of the trajectory
   * @param xInt1 the first intermediate point of the trajectory
   * @param xInt2 the second intermediate point of the trajectory
   * @param xf the end point of the trajectory
   * @param tInt1 the time corresponding to the first intermediate point
   * @param tInt2 the time corresponding to the second intermediate point
   * @param duration the duration of the trajectory
   * @return SplineTrajectory2
   */
  public Trajectory setCartesianSpline2Trajectory(double[] xi, double[] xInt1, double[] xInt2, double[] xf, double tInt1, double tInt2, double duration) {
    return setTrajectory(new Spline2Trajectory(inverseKinematics(xi, null, true, true, true),
        inverseKinematics(xInt1, null, true, true, true), inverseKinematics(xInt2, null, true, true, true),
        inverseKinematics(xf, null, true, true, true), tInt1, tInt2, duration));
  }

  /**
   * Defines and sets a Trapezoid trajectory with the variables in joint coordinates.
   * @param qi the starting point of the trajectory
   * @param qf the end point of the trajectory
   * @param duration the duration of the trajectory
   * @return TrapezoidTrajectory
   */
  public Trajectory setTrapezoidTrajectory(double[] qi, double[] qf, double duration) {
    return setTrajectory(new TrapezoidTrajectory(qi, qf, duration, mSpeedLimit, mAccLimit));
  }

  /**
   * Defines and sets a Trapezoid trajectory with the variables in Cartesian coordinates.
   * @param qi the starting point of the trajectory
   * @param qf the end point of the trajectory
   * @param duration the duration of the trajectory
   * @return TrapezoidTrajectory
   */
  public Trajectory setCartesianTrapezoidTrajectory(double[] xi, double[] xf,double duration) {
    return setTrajectory(new TrapezoidTrajectory(inverseKinematics(xi, null, true, true, true),
        inverseKinematics(xf, null, true, true, true), duration, mSpeedLimit, mAccLimit));
  }

  /**
   * Defines and sets a Trapezoid trajectory with an intermediate point and the variables in joint coordinates
   * @param qi the starting point of the trajectory
   * @param qInt the intermediate point of the trajectory
   * @param qf the end point of the trajectory
   * @param tInt the time corresponding to the intermediate point
   * @param duration the duration of the trajectory
   * @return TrapezoidTrajectory2
   */
  public Trajectory setTrapezoid2Trajectory(double[] qi, double[] qInt, double[] qf, double tInt, double duration) {
    return setTrajectory(new Trapezoid2Trajectory(qi, qInt, qf, tInt, duration, mSpeedLimit));
  }

  /**
   * Defines and sets a Trapezoid trajectory with an intermediate point and the variables in Cartesian coordinates
   * @param xi the starting point of the trajectory
   * @param xInt the intermediate point of the trajectory
   * @param xf the end point of the trajectory
   * @param tInt the time corresponding to the intermediate point
   * @param duration the duration of the trajectory
   * @return TrapezoidTrajectory2
   */
  public Trajectory setTrapezoid2CartesianTrajectory(double[] xi, double[] xInt, double[] xf, double tInt, double duration) {
    return setTrajectory(new Trapezoid2Trajectory(inverseKinematics(xi, null, true, true, true),
        inverseKinematics(xInt, null, true, true, true), inverseKinematics(xf, null, true, true, true), tInt, duration, mSpeedLimit));
  }

  //-------------------
  //Moving along a path
  //-------------------

  /**
   * Moves the robot to the starting point of the trajectory
   * @throws exception if the move is not possible
   */
  public void trajectoryHome()  throws Exception {
    if (mTrajectory != null) {
      mTrajectoryTime = 0;
      moveToQ(mTrajectory.getQi());
    }
  }

  /**
   * Moves the robot to the end-point of the trajectory
   * @throws exception if the move is not possible
   */
  public void trajectoryEnd()  throws Exception {
    if (mTrajectory != null) {
      mTrajectoryTime = mTrajectory.getDuration();
      moveToQ(mTrajectory.getQf());
    }
  }

  /**
   * Moves the robot to the point of the trajectory at time t
   * @param t the time value 
   * @throws exception if the move is not possible
   */
  public void trajectoryAtTime(double t) throws Exception {
    if (mTrajectory != null) {
      mTrajectoryTime = t;
      moveToQ(mTrajectory.getPoint(mTrajectoryTime));
    }
  }

  /**
   * Moves the robot to the next point of the trajectory
   * @param step the delta time value
   * @throws exception if the move is not possible
   */
  public void trajectoryStep(double step) throws Exception {
    if (mTrajectory != null) {
      mTrajectoryTime += step;
      moveToQ(mTrajectory.getPoint(mTrajectoryTime));
    }
  }

  // Utilities 

  /**
   * Returns the point of the trajectory at time "t".
   * @param t the time value
   * @return the joint coordinates of the point
   */
  public double[] trajectoryGetPoint(double t) { 
    return mTrajectory.getPoint(t);
  }

  /**
   * Returns the current time of the trajectory
   * @return the time value 
   */
  public double trajectoryTime() { return mTrajectoryTime; }

  /**
   * Returns whether the robot is at the end-point of the trajectory (after a move of "deltaT")
   * @param deltaT the delta time value
   * @return true if the path has finished or false in otherwise
   */
  public boolean trajectoryFinished(double deltaT) {
    return mTrajectoryTime + deltaT > mTrajectory.getDuration();
  }

  // Visualizing trajectories

  /**
   * Draws the point of the trajectory to the instant "t"
   * @param t the time value
   */
  public void drawPoint(double t, boolean alsoInvalids) {
    double[] q = mTrajectory.getPoint(t);
    double[] point = forwardKinematics(q, null);
    if (alsoInvalids) mTrajectoryTrail.addPoint(point);
    else if (checkValidity(point)) mTrajectoryTrail.addPoint(point);
  }

  /**
   * Draws the points of the trajectory to the instants "times"
   * @param times array with the time value of each point 
   */
  public void drawPoints(double[] times, boolean alsoInvalids) {
    for (int i = 0; i < times.length; i++) {
      drawPoint(times[i], alsoInvalids);
    }
  }

  /**
   * Draws a number of points of the path
   * @param nPoints the number of points of the path 
   */
  public void drawTrajectory(int nPoints, boolean alsoInvalids) {
    mTrajectoryTrail.clear();
    double[][] points = mTrajectory.getPoints(nPoints);
    double[] point = new double[getCartesianArraysLength()];
    for (int i = 0; i < nPoints; i++) {
      forwardKinematics(points[i], point);
      if (alsoInvalids) mTrajectoryTrail.addPoint(point);
      else if (checkValidity(point)) mTrajectoryTrail.addPoint(point);
    }
  }

  /**
   * Makes the path is visible or not
   */
  public void trajectorySetVisible(boolean show) {
    mTrajectoryTrail.setVisible(show);
  }

  /**
   * Clears the trajectory designed
   */
  public void trajectoryClear() {
    mTrajectoryTrail.clear(); 
    mHostGroup.removeElement(mTrajectoryTrail);
    mTrajectoryTrail = new ElementTrail();
    mTrajectoryTrail.getStyle().setLineColor(Color.RED);
    mHostGroup.addElement(mTrajectoryTrail);
  }

  /**
   * Sets a specific color to the trajectory
   * @param color the color to draw the trajectory
   */
  public void trajectorySetColor(Color color) {
    mTrajectoryTrail.getStyle().setLineColor(color);
  }

  /**
   * Sets a specific width to the trajectory
   * @param width the width of the line
   */
  public void trajectorySetLineWidth(float width) {
    mTrajectoryTrail.getStyle().setLineWidth(width);
  }

  // ---------------------------------------
  // Restrictions
  // ---------------------------------------
  /**
   * Gets the minimum allowed value for the given joint
   * @param joint the joint number (from 1 to getDOF(), both included)
   * @return double the minimum possible values for the joint in degrees, NaN if incorrect joint number
   */
  abstract public double getJointMinimum(int joint);

  /**
   * Gets the maximum allowed values of the given joint
   * @param joint the joint number (from 1 to getDOF(), both included)
   * @return double the maximum possible values for the joint in degrees, NaN if incorrect joint number
   */
  abstract public double getJointMaximum(int joint);

  /**
   * Gets the maximum allowed velocity value for the given joint
   * @param joint the joint number
   * @return maximum allowed velocity in degrees per second, NaN if incorrect joint number
   */
  abstract public double getJointSpeedMaximum(int joint);

  /**
   * Gets the maximum allowed acceleration value for the given joint
   * @param joint the joint number
   * @return maximum allowed acceleration in degrees per second squared, NaN if incorrect joint number
   */
  abstract public double getJointAccelerationMaximum(int joint);

  /**
   * Checks if the new value for the given joint is between the limits allowed
   * @param joint the joint number
   * @param value the joint value for the joint in degrees
   * @return true if the value is between their allowed limits or false in
   *         otherwise
   */
  protected boolean checkJointLimit(int joint, double value) {
    if (joint <= 0 || joint > getDOF()) return false;
    if (getJointMinimum(joint) > value || value > getJointMaximum(joint)) return false;
    return true;
  }
  
  /**
   * Checks if the new joint position is between the limits allowed
   * @param q the joint position (q1, q2, ..., qn) 
   * @return true if the values are between the allowed limits or false in
   *         otherwise
   */
  protected boolean checkJointPosition(double [] q){
	  for(int i = 0; i < q.length; i++){
		  if(!checkJointLimit(i+1,q[i])){
			  System.err.println("The value of the joint " + (i+1) + " is invalid. " +
			  		"Its possible range is: (" + getJointMinimum(i+1) + " , " + getJointMaximum(i+1) + ").");
		  		return false;
		  }	
	  }
	  return true;
  }

  /**
   * Checks if the new speed value for the given joint is allowed
   * @param joint the joint number
   * @param speed the speed value for the joint
   * @return true if the speed value is allowed or false in otherwise
   */
  protected boolean checkSpeedLimit(int joint, double speed) {
    if (joint <= 0 || joint > getDOF()) return false;
    if (speed < 0 || speed > mSpeedLimit[joint]) return false;
    return true;
  }

  /**
   * Checks if the new acceleration value for the given joint is allowed
   * @param joint the joint number
   * @param accel the acceleration value is allowed or false in otherwise
   * @return
   */
  protected boolean checkAccelLimit(int joint, double accel) {
    if (joint <= 0 || joint > getDOF()) return false;
    if (accel < 0 || accel > mAccLimit[joint]) return false;
    return true;
  }

  /**
   * Sets the speed limit of the joint
   * @param joint the joint number
   * @param maxSpeed the new speed value for the joint
   * @return true if the new speed value is changed or false in otherwise
   */
  public boolean setSpeedLimit(int joint, double maxSpeed) {
    if (!checkSpeedLimit(joint, maxSpeed)) return false;
    mSpeedLimit[joint - 1] = maxSpeed;
    return true;
  }

  /**
   * Sets the speed percent for the robot
   * @param speed the new speed percent for the real robot (from 1 to 100, both included)
   * @return true if the speed percent is changed or false in otherwise
   */

  public boolean setSpeed(int speed) throws Exception{
    if (speed < 1 || speed > 100) return false;
    mSpeedPercent = speed;
    if(!mIsConnected) return false;
    return setSpeedRealRobot(speed);
  }

  /**
   * Changes the speed percent to the real robot 
   * @param speed the new speed percent for the real robot (from 1 to 100, both included)
   * @return true if the speed percent is changed or false in otherwise
   * @throws Exception
   */

  abstract protected boolean setSpeedRealRobot(int speed) throws Exception;

  /**
   * Sets the acceleration limit of the joint
   * @param joint the joint number
   * @param maxAccel the new acceleration value for the joint
   * @return true if the new acceleration value is changed or false in otherwise
   */
  public boolean setAccelLimit(int joint, double maxAccel) {
    if (!checkAccelLimit(joint, maxAccel)) return false;
    mAccLimit[joint - 1] = maxAccel;
    return true;
  }

  /**
   * Sets the acceleration percent for the robot
   * @param accel the new acceleration percent for the real robot (from 1 to 100, both included)
   * @return true if the acceleration percent is changed or false in otherwise
   */

  public boolean setAccel(int accel)throws Exception{
    if (accel < 1 || accel > 100) return false;
    mAccelPercent = accel;
    if(!mIsConnected) return false;
    return setAccelRealRobot(accel);
  }

  abstract protected boolean setAccelRealRobot(int accel) throws Exception;


  /**
   * Method to add a new restriction.
   * @param rest
   */
  public void addRestriction(Restriction rest) {mRestrictionSet.add(rest);}

  /**
   * Checks if the robot has restrictions or not
   * @return true if there are restrictions or false in otherwise
   */

 protected boolean hasRestrictions() {return !mRestrictionSet.isEmpty();}
  
  /**
   * Private method to check if a point (x,y,z) is valid for all restrictions
   * OR invoke a restriction action otherwise
   * @param point
   * @return True if the point is valid or false in otherwise
   */
 protected boolean checkValidity(double[] p) {
	//  public boolean checkValidity(double[] p) {
    double x = p[0], y = p[1], z = p[2];
    for (Restriction rest : mRestrictionSet) {
      if (!rest.allowsPoint(x,y,z)) {
        rest.action(x,y,z);
        return false;
      }
    }
    return true;
  }

  /**
   * Method to check if a point (x,y,z) is valid or not.
   * 
   * @param point
   * @return True if the point is valid or false in otherwise
   */
/*  public boolean isValidPoint(double x, double y, double z) {
    for (Restriction rest : mRestrictionSet) {
      if (!rest.allowsPoint(x, y, z)) return false;
    }
    return true;
  }*/
 
 //------------------------------------------------
 // Autocollision detection
 // -----------------------------------------------
 
 /**
  * Method to check if the robot collides with itself
  * @return True if the robot collides with itself and false in otherwise 
  */
	public boolean autocollisionDetection(){
		 java.util.ArrayList<OrientedBoundingBox> list = getOrderedOrientedBoxes();		
		  int nBoxes = list.size();
		    for (int i=0; i<nBoxes; i++) {
		      OrientedBoundingBox obb1 = list.get(i);
		       for (int j=i+2; j<nBoxes; j++) {
		        OrientedBoundingBox obb2 = list.get(j);
		       if(obb1.intersectsOrientedBoundingBox(obb2)) {
	    			System.err.println("Error: Possible autoCollision between OBB" + i + " and OBB" + j);
			        System.out.println("  Center "+obb1.getCenter()+ " con "+obb2.getCenter());
			        System.out.println("  Extent "+obb1.getExtent()+ " con "+obb2.getExtent());
					return true;	
				  }
		       }
		    }
		return false;
	}
 
  
  //------------------------------------------------
  // Remote communication with the robot
  // -----------------------------------------------
  /**
   * Establishes the remote connection using the prescribed variables
   * @param hostIP the IP address
   * @param portNumber the port number
   * @param user the user name
   * @param password the password
   * @throws Exception
   */ 
  abstract public void connect(String hostIP, int portNumber, String user, String password) throws Exception;

  /**
   * Closes the remote connection
   * @throws Exception 
   */ 
  abstract public void disconnect() throws Exception;


  /**
   * Stops the hardware in event of a malfunction
   * @throws Exception
   */
  abstract public void stopHardware() throws Exception;

  // ---------------------------------------
  // Required for implementation
  // ---------------------------------------

  protected abstract Group createViewGroup();

  /**
   * Adds the view of the robot to a given group
   * @param group a group element
   */
  public void addToViewGroup(Group group) {
    // System.out.println ("Adding "+this+" to view group = "+group);
    if (mHostGroup != null) {
      mHostGroup.removeElement(mRobotGroup);
      mHostGroup.removeElement(mTrajectoryTrail);
      mHostGroup = null;
    }
    if (group == null) return;
    if (mRobotGroup == null) {
      mRobotGroup = createViewGroup();
      if (mRobotGroup == null) return;
      mTrajectoryTrail = new ElementTrail();
      mTrajectoryTrail.getStyle().setLineColor(Color.RED);
    }
    mAddedToView = true;
    mTrajectoryTrail.clear();
    mHostGroup = group;
    mHostGroup.addElement(mRobotGroup);
    mHostGroup.addElement(mTrajectoryTrail);
    DrawingPanel3D panel = mHostGroup.getPanel();
    mRobotGroup.setPanel(panel);
    mTrajectoryTrail.setPanel(panel);
    panel.render();
    
    
    // Initial position
   mP0x = group.getX();
   mP0y = group.getY();
   mP0z = group.getZ();
   
   //System.out.println("Initial Position " + mP0x + " " + mP0y + " " + mP0z);
  
    mRobotPosition = new double[]{ mHostGroup.getX(),  mHostGroup.getY(), 
    		mHostGroup.getZ()};

  }

  /**
   * Removes the view of the robot
   * @return true if the view is removed or false in otherwise
   */
  public void removeFromViewGroup() {
    if (mHostGroup != null) {
      if (mAddedToView) {
        mHostGroup.removeElement(mRobotGroup);
        mHostGroup.removeElement(mTrajectoryTrail);
      } 
      mHostGroup = null;
      mAddedToView = false;
    }
  }

  /**
   * To be implemented by implemented classes
   */ 
  abstract protected void updateView();
  
  
  //public Group getRobotGroup(){return mHostGroup;}
  
  
  //-------------------------------------
  // Required for interact with objects
  //-------------------------------------
	
	abstract public void attachObject(Element object);
	
	abstract public void detachObject(Element object);

  //---------------------------------------
  // Private methods
  // ---------------------------------------



  /**
   * Required to implement the forward kinematics
   * @param q 
   * @param number a integer number
   * @return a Matrix 
   */

  protected Matrix FKinematics(double q, int number){
    int dof = getDOF();
    Matrix res = new Matrix(4, 4);
    int index = number - 1;
    if (number == 1){
      mDHParameters[getDHQ(index)] = q;
      res = denavit(mDHParameters[index], mDHParameters[index + dof], mDHParameters[index + 2 * dof], mDHParameters[index + 3 * dof]);
      res.set(0, 3, res.get(0, 3) + mP0x);
      res.set(1, 3, res.get(1, 3) + mP0y);
      res.set(2, 3, res.get(2, 3) + mP0z);
      Tint = res;
    }
    else {
      mDHParameters[getDHQ(index)] = q;
      res = denavit(mDHParameters[index], mDHParameters[index + dof], mDHParameters[index + 2 * dof], mDHParameters[index + 3 * dof]);
      Tint = Tint.times(res);
      res = Tint;
    }
    return res;
  }

  /**
   * Required to implement the forward kinematics
   * @param q
   * @param d
   * @param a
   * @param alfa
   * @return
   */
  protected Matrix denavit(double q, double d, double a, double alfa) {
    Matrix res = new Matrix(4, 4);
    res.set(0, 0, Math.cos(q));
    res.set(0, 1, -Math.cos(alfa) * Math.sin(q));
    res.set(0, 2, Math.sin(alfa) * Math.sin(q));
    res.set(0, 3, a * Math.cos(q));
    res.set(1, 0, Math.sin(q));
    res.set(1, 1, Math.cos(alfa) * Math.cos(q));
    res.set(1, 2, -Math.sin(alfa) * Math.cos(q));
    res.set(1, 3, a * Math.sin(q));
    res.set(2, 0, 0.0D);
    res.set(2, 1, Math.sin(alfa));
    res.set(2, 2, Math.cos(alfa));
    res.set(2, 3, d);
    res.set(3, 3, 1.0D);
    return res;
  }


  /**
   * Inner class to wrap rotations
   */
  protected static class AxisRotationWrapper implements TransformationWrapper {
    AxisRotation mTr;
    Element mElement;
    boolean mEnabled = true;

    AxisRotationWrapper(AxisRotation tr, Element element) {
      mTr = tr;
      mTr.setElement(mElement = element);
    }

    public boolean isEnabled() {
      return mEnabled;
    }

    final public void setEnabled(boolean _enabled) {
      this.mEnabled = _enabled;
    }

    final public Transformation getTransformation() {
      return mTr;
    }

    public Object clone() {
      AxisRotationWrapper ct = new AxisRotationWrapper(
          (AxisRotation) mTr.clone(), mElement);
      ct.mEnabled = this.mEnabled;
      ct.mTr.setElement(mElement);
      return ct;
    }

  } // end of inner static class AxisRotationWrapper

}
