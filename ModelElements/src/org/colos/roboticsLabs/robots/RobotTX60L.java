package org.colos.roboticsLabs.robots;
/**
 * @author Almudena Ruiz
 */
import java.awt.Color;
import java.io.DataOutputStream;
import java.net.Socket;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementArrow;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.drawing3d.Group;
import org.opensourcephysics.drawing3d.utils.transformations.YAxisRotation;
import org.opensourcephysics.drawing3d.utils.transformations.ZAxisRotation;
import org.opensourcephysics.numerics.Matrix3DTransformation;
import org.colos.roboticsLabs.robots.utils.maths.*;

import com.jme.bounding.OrientedBoundingBox;
import com.jme.math.Vector3f;

public class RobotTX60L extends AbstractRobot {
  
  static private final int sDOF = 6;
  static private final int sCARTESIAN_ARRAYS_LENGTH = 6;
  static final protected double sLengthOfBase = 0.375; //d1
  static final protected double sWidthOfShoulder = 0.217; //d2
  static final protected double sWidthOfElbow = 0.197; //d3
  static final protected double sLengthOfForearm = 0.450; //d4
  static final protected double sLengthOfWrist = 0.070; //d6
  static final protected double sLengthOfArm = 0.400; //a2
  static final protected double a3 = 0.0;  //a3
  protected double mLengthOfTool = 0.0;
  
  static private final int [] sDHq = new int[] { 0,1,2,3,4,5};
  static private final double[] sHOME = { 0, 0, 0, 0, 0, 0 };
  static private final double[] sJOINT_MINIMA = { -180, -127.5,  -152, -270, -122.5, -270 };
  static private final double[] sJOINT_MAXIMA = {  180,  127.5,   152,  270,  133.5,  270 };
  static private final double[] sSPEED_LIMITS = {  435,  385,     500,  995, 1065,  1445  };
  static private final double[] sACCEL_LIMITS = { 1373, 1373,    3096, 2802, 1707,  8167  };
  
  static private final String sBASE = "org/colos/roboticsLabs/robots/obj_files/TX60L/";

  // Remote connection
  private Socket clientSocket;
  private DataOutputStream outToServer;
  
  // Graphics
  private Group flangeGroup;
  private AxisRotationWrapper mShoulderRotationZ, mArmRotationY, 
                              mElbowRotationY,    mForearmRotationZ, 
                              mWristRotationY,    mFlangeRotationZ;

  //Required to implement inverse kinematics
  protected double Px, Py, Pz;
  protected double[] O = new double[3];
  private double[] A = new double[3];
  
  private Group mainGroup;
  protected OrientedBoundingBox obbObject;
 
  public RobotTX60L() {
    super();
    mCurrentQ = getHome();
  }

  // ---------------------------------------
  // Information about the robot
  // ---------------------------------------
  public int getDOF() { return sDOF; }
 
  public int getCartesianArraysLength() { return sCARTESIAN_ARRAYS_LENGTH; }
  
  public double[] getHome() {
    double[] home = new double[sDOF];
    System.arraycopy(sHOME,0,home,0,sDOF);
    return home; 
  }

  protected int getDHQ(int index) { return sDHq[index]; }

  public double[] getDHParameters() { 
	    return new double[] { 0, -AbstractRobot.PI2, AbstractRobot.PI2,0 ,0, 0, 
	      sLengthOfBase, sWidthOfShoulder, -sWidthOfElbow, sLengthOfForearm, 0, sLengthOfWrist + mLengthOfTool,
	      0, sLengthOfArm, 0, 0, 0, 0,
	      -AbstractRobot.PI2, 0.0, AbstractRobot.PI2, -AbstractRobot.PI2, AbstractRobot.PI2, 0
	    };
	  }

  protected double[] calculateDH(double[] q){
    Matrix A01 = FKinematics(q[0] * sTO_RADIANS, 1);
    Matrix A02 = FKinematics(q[1] * sTO_RADIANS - AbstractRobot.PI2, 2);
    Matrix A03 = FKinematics(q[2] * sTO_RADIANS + AbstractRobot.PI2, 3);
    Matrix A04 = FKinematics(q[3] * sTO_RADIANS, 4);
    Matrix A05 = FKinematics(q[4] * sTO_RADIANS, 5);
    Matrix A06 = FKinematics(q[5] * sTO_RADIANS, 6);

    // For Inverse Kinematics
    O[0] = A06.get(0,1);
    O[1] = A06.get(1,1);
    O[2] = A06.get(2,1);
    //System.out.println("Anterior O"+ O[0] + " "+O[1]+" "+O[2]);
    A[0] = A06.get(0,2);
    A[1] = A06.get(1,2);
    A[2] = A06.get(2,2);
    //System.out.println("Anterior A"+ A[0] + " "+A[1]+" "+A[2]);
    Px = A06.get(0,3);
    Py = A06.get(1,3);
    Pz = A06.get(2,3);
    
    return A06.getVectorColumn(3,0,2);
  }
  
  public double[] inverseKinematics(double[] coordinates, double[] q, boolean shoulder, boolean elbow, boolean wrist) {
    return q;
  }
  
  // ---------------------------------------
  // Positioning
  // ---------------------------------------

  protected void moveRealRobot() throws Exception {
    String data = "2\n" + mCurrentQ[0] + "\n" + mCurrentQ[1] + "\n" + mCurrentQ[2] + "\n"
        + mCurrentQ[3] + "\n" + mCurrentQ[4] + "\n" + mCurrentQ[5] + "\n";
    writeSocket(data);
  }
  
  public void openTool() throws Exception {
    mOpenTool=true;
    if (mIsConnected) writeSocket("6\n");
  }

  public void closeTool() throws Exception {
    mOpenTool=false;
    if (mIsConnected) writeSocket("7\n");
  }
  

  protected boolean setSpeedRealRobot(int speed) throws Exception {   
    writeSocket("8\n" + speed + "\n");  
    return true;
  }
   
 /*TODO
  */
  protected boolean setAccelRealRobot(int accel) {return false;}
  
  
  
  //---------------------------------------
  // Restrictions
  // ---------------------------------------
 
  public double getJointMinimum(int joint) {
    if (joint<=0 || joint>sDOF) return Double.NaN;
    return sJOINT_MINIMA[joint-1];	   
  }

  public double getJointMaximum(int joint) {
    if (joint<=0 || joint>sDOF) return Double.NaN;
    return sJOINT_MAXIMA[joint-1];    
  }

  public double getJointSpeedMaximum(int joint) {
    if (joint<=0 || joint>sDOF) return Double.NaN;
    return sSPEED_LIMITS[joint - 1];
  }

  public double getJointAccelerationMaximum(int joint) {
    if (joint<=0 || joint>sDOF) return Double.NaN;
    return sACCEL_LIMITS[joint - 1];
  }


  //------------------------------------------------
  // Remote communication with the robot
  // -----------------------------------------------
  
  public void connect(String hostIP, int portNumber, String user, String password) throws Exception {
    openSocket(hostIP, portNumber, user, password);
    mIsConnected = true;
  }

  public void disconnect() throws Exception {
    closeSocket();
    mIsConnected = false;
  }
  
  public void stopHardware() throws Exception {
    if (mIsConnected) writeSocket("9\n");
  }

  //---------------------------------------
  // Required for implementation
  // ---------------------------------------

  protected Group createViewGroup() {
	if (mainGroup!=null) return mainGroup;
	mainGroup = new Group();
    Color orangeColor = new Color(255, 128, 0);
    mHasViewGroup = true;
    mainGroup.setTransformation(Matrix3DTransformation.rotationZ(Math.PI));
    Group robotGroup = new Group();
    robotGroup.setName("robotGroup");
    robotGroup.setXYZ(0, 0, 0);
    mainGroup.addElement(robotGroup);

    ElementObject baseObj = new ElementObject();
    baseObj.setXYZ(0.05, 0, 0.0925);
    baseObj.setSizeXYZ(0.176, 0.155, 0.176);
    baseObj.setObjectFile(sBASE+"base.obj");
    baseObj.getStyle().setFillColor(orangeColor);
   // baseObj.setVisible(false);
    Matrix3DTransformation tr = Matrix3DTransformation.rotationZ(Math.PI);
    tr.multiply(Matrix3DTransformation.rotationX(3 * PI2));
    baseObj.setTransformation(tr);
    robotGroup.addElement(baseObj);
    robotGroup.addElement(baseObj);

    //OrientedBoundingBox0
    ElementBox OBB0 = new ElementBox();
    OBB0.setXYZ(0, 0, 0.0925);
    OBB0.setSizeXYZ(0.238, 0.238, 0.176);
    //OBB0.getStyle().setFillColor(Color.RED);
    OBB0.getStyle().setDrawingFill(false);
    OBB0.setVisible(false);
    robotGroup.addElement(OBB0);
    
    {
        OrientedBoundingBox obb0 = new OrientedBoundingBox();
        obb0.setExtent(new Vector3f((float)(OBB0.getSizeX()/2), (float)(OBB0.getSizeY()/2), (float)(OBB0.getSizeZ()/2)));
        obbTable.put(obb0, OBB0);
        obbList.add(obb0);
        Element[] triedro = new Element[3];
        for (int i=0; i<3; i++) {
        	triedro[i] = new ElementArrow();
        	triedro[i].getStyle().setLineColor(Color.RED);
        	triedro[i].getStyle().setLineWidth(4);

//        	mainGroup.addElement(triedro[i]);
        }
        triedro[0].getStyle().setLineColor(Color.RED);
        triedro[1].getStyle().setLineColor(Color.YELLOW);
        triedro[2].getStyle().setLineColor(Color.BLUE);
        obbTriedroTable.put(obb0, triedro);
        }
    
    Group shoulderGroup = new Group();
    shoulderGroup.setName("shoulderGroup");
    shoulderGroup.setXYZ(0.006, 0, 0.3145);
    shoulderGroup.setTransformation(Matrix3DTransformation.rotationZ(Math.PI));
    robotGroup.addElement(shoulderGroup);

    ElementObject shoulderObj = new ElementObject();
    shoulderObj.setXYZ(0, 0.024, 0);
    shoulderObj.setSizeXYZ(0.176 * 0.84, 0.256 * 0.59, 0.13);
    shoulderObj.setObjectFile(sBASE+"shoulder.obj");
    shoulderObj.getStyle().setFillColor(orangeColor);
    //shoulderObj.setVisible(false);
    shoulderObj.setTransformation(Matrix3DTransformation.rotationX(Math.PI));
    shoulderGroup.addElement(shoulderObj);

    mShoulderRotationZ = new AxisRotationWrapper(new ZAxisRotation(),
        shoulderGroup);
    shoulderGroup.addSecondaryTransformation(mShoulderRotationZ);

    //OrientedBoundingBox1
    ElementBox OBB1 = new ElementBox();
    OBB1.setXYZ(0, 0.024, 0);
    OBB1.setSizeXYZ(0.19, 0.256, 0.26);
    OBB1.getStyle().setFillColor(Color.blue);
    OBB1.getStyle().setDrawingFill(false);
    OBB1.setVisible(false);
    shoulderGroup.addElement(OBB1);
    
    {
        OrientedBoundingBox obb1 = new OrientedBoundingBox();
        obb1.setExtent(new Vector3f((float)(OBB1.getSizeX()/2), (float)(OBB1.getSizeY()/2), (float)(OBB1.getSizeZ()/2)));
        obbTable.put(obb1, OBB1);
        obbList.add(obb1);
        Element[] triedro = new Element[3];
        for (int i=0; i<3; i++) {
        	triedro[i] = new ElementArrow();
        	triedro[i].getStyle().setLineColor(Color.RED);
        	triedro[i].getStyle().setLineWidth(4);

//        	mainGroup.addElement(triedro[i]);
        }
        triedro[0].getStyle().setLineColor(Color.RED);
        triedro[1].getStyle().setLineColor(Color.YELLOW);
        triedro[2].getStyle().setLineColor(Color.BLUE);
        obbTriedroTable.put(obb1, triedro);
        }
    
    
    Group armGroup = new Group();
    armGroup.setName("armGroup");
    armGroup.setXYZ(-0.001, 0.18, 0.045);
    shoulderGroup.addElement(armGroup);

    ElementObject armObj = new ElementObject();
    armObj.setXYZ(0, 0, 0.2035);
    armObj.setSizeXYZ(0.176 * 1.738, 0.21, 0.55 * 0.537);
    armObj.setObjectFile(sBASE+"arm.obj");
    armObj.getStyle().setFillColor(orangeColor);
    //armObj.setVisible(false);
    armObj.setTransformation(Matrix3DTransformation.rotationX(Math.PI));
    armGroup.addElement(armObj);

    mArmRotationY = new AxisRotationWrapper(new YAxisRotation(), armGroup);
    armGroup.addSecondaryTransformation(mArmRotationY);

    //OrientedBoundingBox2
    ElementBox OBB2 = new ElementBox();
    OBB2.setXYZ(0, 0, 0.2035);
    OBB2.setSizeXYZ(0.176, 0.07, 0.55+0.04);
    OBB2.getStyle().setFillColor(Color.red);
    OBB2.getStyle().setDrawingFill(false);
    OBB2.setVisible(false);
    armGroup.addElement(OBB2);
    
    {
    	
        OrientedBoundingBox obb2 = new OrientedBoundingBox();
        obb2.setExtent(new Vector3f((float)(OBB2.getSizeX()/2), (float)(OBB2.getSizeY()/2), (float)(OBB2.getSizeZ()/2)));
        obbTable.put(obb2, OBB2);
        obbList.add(obb2);
        Element[] triedro = new Element[3];
        for (int i=0; i<3; i++) {
        	triedro[i] = new ElementArrow();
        	triedro[i].getStyle().setLineColor(Color.RED);
        	triedro[i].getStyle().setLineWidth(4);

//        	mainGroup.addElement(triedro[i]);
        }
        triedro[0].getStyle().setLineColor(Color.RED);
        triedro[1].getStyle().setLineColor(Color.YELLOW);
        triedro[2].getStyle().setLineColor(Color.BLUE);
        obbTriedroTable.put(obb2, triedro);
        }  
    
    Group elbowGroup = new Group();
    elbowGroup.setName("elbowGroup");
    elbowGroup.setXYZ(0, -0.137, 0.425);
    armGroup.addElement(elbowGroup);

    ElementObject elbowObj = new ElementObject();
    elbowObj.setXYZ(0.002, 0, -0.0085);
    elbowObj.setSizeXYZ(0.16 * 0.77, 0.13, 0.23 * 0.53);
    elbowObj.setObjectFile(sBASE+"elbow.obj");
    elbowObj.getStyle().setFillColor(orangeColor);
    elbowObj.setTransformation(Matrix3DTransformation.rotationX(Math.PI));
    elbowGroup.addElement(elbowObj);
  // elbowObj.setVisible(false);
    mElbowRotationY = new AxisRotationWrapper(new YAxisRotation(),
        elbowGroup);
    elbowGroup.addSecondaryTransformation(mElbowRotationY);

    //OrientedBoundingBox3
    ElementBox OBB3 = new ElementBox();
    OBB3.setXYZ(0.002, 0, -0.0085);
    OBB3.setSizeXYZ(0.16, 0.2, 0.23);
    OBB3.getStyle().setFillColor(Color.blue);
    OBB3.getStyle().setDrawingFill(false);
    OBB3.setVisible(false);
    elbowGroup.addElement(OBB3);
    
    {
        OrientedBoundingBox obb3 = new OrientedBoundingBox();
        obb3.setExtent(new Vector3f((float)(OBB3.getSizeX()/2), (float)(OBB3.getSizeY()/2), (float)(OBB3.getSizeZ()/2)));
        obbTable.put(obb3, OBB3);
        obbList.add(obb3);
        Element[] triedro = new Element[3];
        for (int i=0; i<3; i++) {
        	triedro[i] = new ElementArrow();
        	triedro[i].getStyle().setLineColor(Color.RED);
        	triedro[i].getStyle().setLineWidth(4);

//        	mainGroup.addElement(triedro[i]);
        }
        triedro[0].getStyle().setLineColor(Color.RED);
        triedro[1].getStyle().setLineColor(Color.YELLOW);
        triedro[2].getStyle().setLineColor(Color.BLUE);
        obbTriedroTable.put(obb3, triedro);
        }
    
    
    Group forearmGroup = new Group();
    forearmGroup.setName("forearmGroup");
    forearmGroup.setXYZ(0, -0.018, 0.305);
    elbowGroup.addElement(forearmGroup);

    ElementObject forearmObj = new ElementObject();
    forearmObj.setXYZ(-0.0065, -0.0005, 0);
    forearmObj.setSizeXYZ(0.2013, 0.1925, 0.2079);
    forearmObj.setObjectFile(sBASE+"forearm.obj");
    forearmObj.getStyle().setFillColor(orangeColor);
    //forearmObj.setVisible(false);

    Matrix3DTransformation tr2 = Matrix3DTransformation.rotationX(-PI2);
    forearmObj.setTransformation(tr2);
    forearmGroup.addElement(forearmObj);

    mForearmRotationZ = new AxisRotationWrapper(new ZAxisRotation(),
        forearmGroup);
    forearmGroup.addSecondaryTransformation(mForearmRotationZ);
   
    //OrientedBoundingBox4
    ElementBox OBB4 = new ElementBox();
    OBB4.setXYZ(-0.0065, -0.0005, 0);
    OBB4.setSizeXYZ(0.13, 0.15, 0.385);
    OBB4.getStyle().setFillColor(Color.red);
    OBB4.getStyle().setDrawingFill(false);
    OBB4.setVisible(false);
    forearmGroup.addElement(OBB4);
   
    {	
        OrientedBoundingBox obb4 = new OrientedBoundingBox();
        obb4.setExtent(new Vector3f((float)(OBB4.getSizeX()/2), (float)(OBB4.getSizeY()/2), (float)(OBB4.getSizeZ()/2)));
        obbTable.put(obb4, OBB4);
        obbList.add(obb4);
        Element[] triedro = new Element[3];
        for (int i=0; i<3; i++) {
        	triedro[i] = new ElementArrow();
        	triedro[i].getStyle().setLineColor(Color.RED);
        	triedro[i].getStyle().setLineWidth(4);

//        	mainGroup.addElement(triedro[i]);
        }
        triedro[0].getStyle().setLineColor(Color.RED);
        triedro[1].getStyle().setLineColor(Color.YELLOW);
        triedro[2].getStyle().setLineColor(Color.BLUE);
        obbTriedroTable.put(obb4, triedro);
        }
    
    
    Group wristGroup = new Group();
    wristGroup.setName("wristGroup");
    wristGroup.setXYZ(0, 0, 0.147);
    forearmGroup.addElement(wristGroup);

    ElementObject wristObj = new ElementObject();
    wristObj.setXYZ(0, 0, 0.015);
    wristObj.setSizeXYZ(0.052, 0.11 * 0.44, 0.04);
    wristObj.setObjectFile(sBASE+"wrist.obj");
    wristObj.getStyle().setFillColor(Color.LIGHT_GRAY);
    wristObj.setTransformation(Matrix3DTransformation.rotationX(-PI2));
    wristGroup.addElement(wristObj);
   // wristObj.setVisible(false);

    mWristRotationY = new AxisRotationWrapper(new YAxisRotation(),
        wristGroup);
    wristGroup.addSecondaryTransformation(mWristRotationY);

    //OrientedBoundingBox5
    ElementBox OBB5 = new ElementBox();
    OBB5.setXYZ(0, 0, 0.015);
    OBB5.setSizeXYZ(0.04, 0.04, 0.11);
    OBB5.getStyle().setFillColor(Color.blue);
    OBB5.getStyle().setDrawingFill(false);
    OBB5.setVisible(false);
    wristGroup.addElement(OBB5);
    
    {
    	
    OrientedBoundingBox obb5 = new OrientedBoundingBox();
    obb5.setExtent(new Vector3f((float)(OBB5.getSizeX()/2), (float)(OBB5.getSizeY()/2), (float)(OBB5.getSizeZ()/2)));
    obbTable.put(obb5, OBB5);
    obbList.add(obb5);
    Element[] triedro = new Element[3];
    for (int i=0; i<3; i++) {
    	triedro[i] = new ElementArrow();
    	triedro[i].getStyle().setLineColor(Color.RED);
    	triedro[i].getStyle().setLineWidth(4);

//    	mainGroup.addElement(triedro[i]);
    }
    triedro[0].getStyle().setLineColor(Color.RED);
    triedro[1].getStyle().setLineColor(Color.YELLOW);
    triedro[2].getStyle().setLineColor(Color.BLUE);
    obbTriedroTable.put(obb5, triedro);
    }
    
    flangeGroup = new Group();
    flangeGroup.setName("flangeGroup");
    flangeGroup.setXYZ(0, 0, 0.062);
    wristGroup.addElement(flangeGroup);

    ElementObject flangeObj = new ElementObject();
    flangeObj.setXYZ(0, 0, 0);
    flangeObj.setSizeXYZ(0.02, 0.02, 0.02);
    flangeObj
    .setObjectFile(sBASE+"flange.obj");
    flangeObj.getStyle().setFillColor(Color.LIGHT_GRAY);
    flangeObj.setTransformation(Matrix3DTransformation.rotationX(-PI2));
    flangeGroup.addElement(flangeObj);

    mFlangeRotationZ = new AxisRotationWrapper(new ZAxisRotation(),
        flangeGroup);
    flangeGroup.addSecondaryTransformation(mFlangeRotationZ);

    return mainGroup;
  }
  
  protected void updateView() {
    if(mHasViewGroup){
      mShoulderRotationZ.mTr.setAngle(mCurrentQ[0] * sTO_RADIANS);
      mArmRotationY.mTr.setAngle(mCurrentQ[1] * sTO_RADIANS);
      mElbowRotationY.mTr.setAngle(mCurrentQ[2] * sTO_RADIANS);
      mForearmRotationZ.mTr.setAngle(mCurrentQ[3] * sTO_RADIANS);
      mWristRotationY.mTr.setAngle(mCurrentQ[4] * sTO_RADIANS);
      mFlangeRotationZ.mTr.setAngle(mCurrentQ[5] * sTO_RADIANS);
     }
  }

  /**
   * Required to defining the TX60L with components class
   * @return the flange Group of the robot
   */
  protected Group getFlangeGroup() { return flangeGroup; }

 
	public void attachObject(Element object) {
		if (object == null) System.out.println("Error: It is necessary to add a element");
		else {       
			if(mHasViewGroup){
				object.setXYZ(0, 0, 0.013 + object.getSizeZ()/2);	
				//OBB	
				ElementBox OBBbox = new ElementBox();
				OBBbox.setXYZ(object.getX(), object.getY(), object.getZ());
				OBBbox.setSizeXYZ(object.getSizeX(),object.getSizeY(),object.getSizeZ());
				OBBbox.getStyle().setFillColor(object.getStyle().getFillColor());
				flangeGroup.addElement(OBBbox); //Adds object to the view
				
				OrientedBoundingBox obbObject = new OrientedBoundingBox();	
		        obbObject.setExtent(new Vector3f((float)(OBBbox.getSizeX()/2), (float)(OBBbox.getSizeY()/2), (float)(OBBbox.getSizeZ()/2)));   
		        obbTable.put(obbObject, OBBbox);
		        obbList.add(obbObject);
		       
		        Element[] triedro = new Element[3];
		        for (int i=0; i<3; i++) {
		        	triedro[i] = new ElementArrow();
		        	triedro[i].getStyle().setLineColor(Color.RED);
		        	triedro[i].getStyle().setLineWidth(4);

//		        	mainGroup.addElement(triedro[i]);
		        }
		        triedro[0].getStyle().setLineColor(Color.RED);
		        triedro[1].getStyle().setLineColor(Color.YELLOW);
		        triedro[2].getStyle().setLineColor(Color.BLUE);
		        obbTriedroTable.put(obbObject, triedro);
		        }	
		}
	}	
	
	public void detachObject(Element object){
		if(mHasViewGroup){
			flangeGroup.removeElement(object);	
			obbTable.remove(obbObject);
			obbList.remove(obbObject);
			obbTriedroTable.remove(obbObject);
					
			double[] pos = flangeGroup.toSpaceFrame(new double[]{0,0,0});
			object.setXYZ(pos[0]-object.getSizeX(), pos[1], pos[2]);
		}
	}

  //---------------------------------------
  // Private methods
  // ---------------------------------------

  /**
   * Opens a new socket to establish the communication with the real robot
   * @param hostIP the IP address
   * @param portNumber the port number
   * @param user the user name
   * @param password the password
   * @return clientSocket, the socket to communicate with the real robot
   * @throws Exception
   */
  private Socket openSocket(String hostIP, int portNumber, String user,
      String password) throws Exception {
    clientSocket = new Socket(hostIP, portNumber);
    outToServer = new DataOutputStream(clientSocket.getOutputStream());
    return clientSocket;
  }

  /**
   * Closes the socket defined to establish the communication with the real robot
   * @throws Exception
   */
  private void closeSocket() throws Exception {
    clientSocket.close();
    System.out.println("The connection is completed");
  }

  /**
   * Writes data in the socket established
   * @param data
   * @throws Exception
   */
  protected void writeSocket(String data) throws Exception {
    outToServer.writeBytes(data);
  }
  
  
  
//=============================================================================
//INVERSE KINEMATICS (NEW METHODS)
//=============================================================================

 	//=============================================================================
	// Member Variables
	//=============================================================================
		    double theta_1, theta_2, theta_3, theta_4, theta_5, theta_6 ;
		    double d_1, d_2, a_2, d_3, d_4, d_6;
		    double a_theta_1,b_theta_1, c_theta_1, a_theta_2, b_theta_2, c_theta_2, d_theta_2, 
		    h_theta_2, h_theta_3, k_theta_3, a_theta_4, b_theta_4;
		    double cos_theta_1, sin_theta_1, cos_theta_2,sin_theta_2, cos_theta_3,sin_theta_3,
		    cos_theta_4, sin_theta_4,cos_theta_5, sin_theta_5, cos_theta_6, sin_theta_6;
		    double n_x, n_y, n_z, o_x, o_y, o_z, a_x, a_y, a_z, p_x, p_y, p_z;
		    Matrix tcp = new Matrix (4,4);
		   		    
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// Public Members
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

		    //=============================================================================
			 // Construction
			 //=============================================================================

			 public void kinematicsComponentTX60L(){
			          d_1 = sLengthOfBase;
			          d_2 = sWidthOfShoulder;
			          a_2 = sLengthOfArm;
			          d_3 =sWidthOfElbow;
			          d_4 = sLengthOfForearm;
			          d_6 = sLengthOfWrist;
			     }

	//=============================================================================
	// Direct kinematics
	//=============================================================================

	public void directKinematics (double[] q){
		   kinematicsComponentTX60L();
	       theta_1 = q[0]*sTO_RADIANS;
	       theta_2 = q[1]*sTO_RADIANS;
	       theta_3 = q[2]*sTO_RADIANS;
	       theta_4 = q[3]*sTO_RADIANS;
	       theta_5 = q[4]*sTO_RADIANS;
	       theta_6 = q[5]*sTO_RADIANS;
	       
	       cos_theta_1 = Math.cos(theta_1);
	       cos_theta_4 = Math.cos(theta_4);
	       cos_theta_5 = Math.cos(theta_5);
	       cos_theta_6 = Math.cos(theta_6);
	       cos_theta_2 = Math.cos(theta_2);
	       sin_theta_4 = Math.sin(theta_4);
	       sin_theta_6 = Math.sin(theta_6);
	       sin_theta_5 = Math.sin(theta_5);
	       sin_theta_1 = Math.sin(theta_1);
	       sin_theta_2 = Math.sin(theta_2);

	       tcp.set(0,0,cos_theta_1 * Math.cos(theta_2 + theta_3) * cos_theta_4 * cos_theta_5 *
	       cos_theta_6 - cos_theta_1 * Math.cos(theta_2 + theta_3) * sin_theta_4 *
	       sin_theta_6 - cos_theta_1 * cos_theta_6 * Math.sin(theta_2 + theta_3) *
	       sin_theta_5 - cos_theta_4 * sin_theta_1 * sin_theta_6 - cos_theta_5 *
	       cos_theta_6 * sin_theta_1 * sin_theta_4);

	       tcp.set(1,0,cos_theta_1 * cos_theta_4 * sin_theta_6 + cos_theta_1 * cos_theta_5 *
	       cos_theta_6 * sin_theta_4 + Math.cos(theta_2 + theta_3) * cos_theta_4 *
	       cos_theta_5 * cos_theta_6 * sin_theta_1 - Math.cos(theta_2 + theta_3) *
	       sin_theta_1 * sin_theta_4 * sin_theta_6 - cos_theta_6 * sin_theta_1 *
	       Math.sin(theta_2 + theta_3) * sin_theta_5);

	       tcp.set(2,0, -Math.cos(theta_2 + theta_3) * cos_theta_6 * sin_theta_5 - cos_theta_4 *
	       cos_theta_5 * cos_theta_6 * Math.sin(theta_2 + theta_3) + Math.sin(theta_2 +
	       theta_3) * sin_theta_4 * sin_theta_6);

	       tcp.set(3,0,0.0);
	       
	       tcp.set(0,1, -cos_theta_1 * Math.cos(theta_2 + theta_3) * cos_theta_4 * cos_theta_5 *
	       sin_theta_6 - cos_theta_1 * Math.cos(theta_2 + theta_3) * cos_theta_6 *
	       sin_theta_4 + cos_theta_1 * Math.sin(theta_2 + theta_3) * sin_theta_5 *
	       sin_theta_6 - cos_theta_4 * cos_theta_6 * sin_theta_1 + cos_theta_5 *
	       sin_theta_1 * sin_theta_4 * sin_theta_6);

	       tcp.set(1,1, cos_theta_1 * cos_theta_4 * cos_theta_6 - cos_theta_1 * cos_theta_5 *
	       sin_theta_4 * sin_theta_6 - Math.cos(theta_2 + theta_3) * cos_theta_4 *
	       cos_theta_5 * sin_theta_1 * sin_theta_6 - Math.cos(theta_2 + theta_3) *
	       cos_theta_6 * sin_theta_1 * sin_theta_4 + sin_theta_1 * Math.sin(theta_2 +
	       theta_3) * sin_theta_5 * sin_theta_6);

	       tcp.set(2,1, Math.cos(theta_2 + theta_3) * sin_theta_5 * sin_theta_6 + cos_theta_4 *
	       cos_theta_5 * Math.sin(theta_2 + theta_3) * sin_theta_6 + cos_theta_6 *
	       Math.sin(theta_2 + theta_3) * sin_theta_4);

	       tcp.set(3,1,0.0);

	       tcp.set(0,2, cos_theta_1 * Math.cos(theta_2 + theta_3) * cos_theta_4 * sin_theta_5 +
	       cos_theta_1 * cos_theta_5 * Math.sin(theta_2 + theta_3) - sin_theta_1 *
	       sin_theta_4 * sin_theta_5);

	       tcp.set(1,2, cos_theta_1 * sin_theta_4 * sin_theta_5 + Math.cos(theta_2 + theta_3) *
	       cos_theta_4 * sin_theta_1 * sin_theta_5 + cos_theta_5 * sin_theta_1 *
	       Math.sin(theta_2 + theta_3));

	       tcp.set(2,2, Math.cos(theta_2 + theta_3) * cos_theta_5 - cos_theta_4 * Math.sin(theta_2 +
	       theta_3) * sin_theta_5);

	       tcp.set(3,2,0.0);

	       tcp.set(0,3,a_2 * cos_theta_1 * sin_theta_2 - d_2 * sin_theta_1 + d_3 *
	       sin_theta_1 + d_4 * cos_theta_1 * Math.sin(theta_2 + theta_3) + d_6 *
	       cos_theta_1 * Math.cos(theta_2 + theta_3) * cos_theta_4 * sin_theta_5 +
	       d_6 * cos_theta_1 * cos_theta_5 * Math.sin(theta_2 + theta_3) - d_6 *
	       sin_theta_1 * sin_theta_4 * sin_theta_5);

	       tcp.set(1,3,a_2 * sin_theta_1 * sin_theta_2 + d_2 * cos_theta_1 - d_3 *
	       cos_theta_1 + d_4 * sin_theta_1 * Math.sin(theta_2 + theta_3) + d_6 *
	       cos_theta_1 * sin_theta_4 * sin_theta_5 + d_6 * Math.cos(theta_2 +
	       theta_3) * cos_theta_4 * sin_theta_1 * sin_theta_5 + d_6 *
	       cos_theta_5 * sin_theta_1 * Math.sin(theta_2 + theta_3));

	       tcp.set(2,3, a_2 * cos_theta_2 + d_1 + d_4 * Math.cos(theta_2 + theta_3) + d_6 *
	       Math.cos(theta_2 + theta_3) * cos_theta_5 - d_6 * cos_theta_4 *
	       Math.sin(theta_2 + theta_3) * sin_theta_5);

	       tcp.set(3,3,1.0);
	       
	      // System.out.println("Px= "+tcp.get(0,3)+" Py= "+ tcp.get(1,3) + "Pz= "+tcp.get(2,3));
	      // System.out.println("Ox= "+tcp.get(0,1)+" Oy= "+ tcp.get(1,1) + "Oz= "+tcp.get(2,1));
	      // System.out.println("Ax= "+tcp.get(0,2)+" Ay= "+ tcp.get(1,2) + "Az= "+tcp.get(2,2));

	    }

	//=============================================================================
	// Compute Jacobian
	//=============================================================================

	/*    public void computeJacobian(double[] q){
	       Matrix J = new Matrix(6,6);
	       theta_1 = q[0];
	       theta_2 = q[1];
	       theta_3 = q[2];
	       theta_4 = q[3];
	       theta_5 = q[4];
	       theta_6 = q[5];
	       
	       cos_theta_1 =  Math.cos(theta_1);
	       cos_theta_4 =  Math.cos(theta_4);
	       cos_theta_5 =  Math.cos(theta_5);
	       cos_theta_6 =  Math.cos(theta_6);
	       cos_theta_2 =  Math.cos(theta_2);
	       sin_theta_4 =  Math.sin(theta_4);
	       sin_theta_6 =  Math.sin(theta_6);
	       sin_theta_5 =  Math.sin(theta_5);
	       sin_theta_1 =  Math.sin(theta_1);
	       sin_theta_2 =  Math.sin(theta_2);
	   }*/

	//=============================================================================
	// Inverse kinematics
	//=============================================================================

	    public void inverseKinematics (double[] q){
	       n_x = tcp.get(0,0); 
	       n_y = tcp.get(1,0); 
	       n_z = tcp.get(2,0); 
	       o_x = tcp.get(0,1); 
	       o_y = tcp.get(1,1); 
	       o_z = tcp.get(2,1); 
	       a_x = tcp.get(0,2); 
	       a_y = tcp.get(1,2); 
	       a_z = tcp.get(2,2); 
	       p_x = tcp.get(0,3); 
	       p_y = tcp.get(1,3); 
	       p_z = tcp.get(2,3); 

	       compute_AUX_THETA_1();  
	       computeTHETA_1_1(); 
	       cos_theta_1 =  Math.cos(theta_1);
	       sin_theta_1 =  Math.sin(theta_1);
	       compute_AUX_THETA_2();  
	       computeTHETA_2_1(); 
	       cos_theta_2 =  Math.cos(theta_2);
	       sin_theta_2 =  Math.sin(theta_2);
	       compute_AUX_THETA_3();  computeTHETA_3(); 
	       sin_theta_3 =  Math.sin(theta_3);
	       compute_AUX_THETA_4();  computeTHETA_4_1(); 
	       cos_theta_4 =  Math.cos(theta_4);
	       sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	       //pretty();

	       computeTHETA_4_2();
	        cos_theta_4 =  Math.cos(theta_4);
	        sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	       //pretty();

	       computeTHETA_2_2();
	              cos_theta_2 =  Math.cos(theta_2);
	              sin_theta_2 =  Math.sin(theta_2);
	       compute_AUX_THETA_3();  computeTHETA_3(); 
	              sin_theta_3 =  Math.sin(theta_3);
	       compute_AUX_THETA_4();  computeTHETA_4_1(); 
	              cos_theta_4 =  Math.cos(theta_4);
	              sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	       //pretty();

	       computeTHETA_4_2();
	              cos_theta_4 =  Math.cos(theta_4);
	              sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	      // pretty();

	       computeTHETA_1_2();
	              cos_theta_1 =  Math.cos(theta_1);
	              sin_theta_1 = Math.sin(theta_1);
	       compute_AUX_THETA_2();  computeTHETA_2_1(); 
	              cos_theta_2 =  Math.cos(theta_2);
	              sin_theta_2 =  Math.sin(theta_2);
	       compute_AUX_THETA_3();  computeTHETA_3(); 
	              sin_theta_3 =  Math.sin(theta_3);
	       compute_AUX_THETA_4();  computeTHETA_4_1(); 
	              cos_theta_4 =  Math.cos(theta_4);
	              sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	      // pretty();

	       computeTHETA_4_2();
	              cos_theta_4 =  Math.cos(theta_4);
	              sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	       //pretty();

	       computeTHETA_2_2();
	              cos_theta_2 = Math.cos(theta_2);
	              sin_theta_2 = Math.sin(theta_2);
	       compute_AUX_THETA_3();  computeTHETA_3(); 
	              sin_theta_3 =  Math.sin(theta_3);
	       compute_AUX_THETA_4();  computeTHETA_4_1(); 
	              cos_theta_4 =  Math.cos(theta_4);
	              sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	      // pretty();

	       computeTHETA_4_2();
	              cos_theta_4 =  Math.cos(theta_4);
	              sin_theta_4 =  Math.sin(theta_4);
	       computeTHETA_6(); 
	       computeTHETA_5(); 
	       pretty();
	    }

	    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		// Private Members
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


	   private void compute_AUX_THETA_1(){
	       a_theta_1 = -a_y * d_6 + p_y;
	       b_theta_1 = a_x * d_6 - p_x;
	       c_theta_1 = d_2 - d_3;
	    }
 
	   private void computeTHETA_1_1(){
	       theta_1 = Math.atan2(c_theta_1, Math.sqrt(Math.pow(a_theta_1, 2) + Math.pow(b_theta_1, 2) -
	       Math.pow(c_theta_1, 2))) - Math.atan2(a_theta_1, b_theta_1);
	    }

	   private void computeTHETA_1_2(){
	       theta_1 = Math.atan2(c_theta_1, -(Math.sqrt(Math.pow(a_theta_1, 2) + Math.pow(b_theta_1, 2) -
	    	Math.pow(c_theta_1, 2)))) - Math.atan2(a_theta_1, b_theta_1);
	    }

	   private void compute_AUX_THETA_2(){
	       a_theta_2 = a_x * d_6 * cos_theta_1 + a_y * d_6 * sin_theta_1 - p_x * cos_theta_1 - p_y * sin_theta_1;
	       b_theta_2 = a_z * d_6 + d_1 - p_z;
	       c_theta_2 = a_2;
	       d_theta_2 = -d_4;
	       h_theta_2 = (-Math.pow(a_theta_2, 2) - Math.pow(b_theta_2, 2) - Math.pow(c_theta_2, 2) + 
	    		   Math.pow(d_theta_2, 2)) / (2 * c_theta_2);
	    }

	   private void computeTHETA_2_1(){
	       theta_2 = Math.atan2(h_theta_2, Math.sqrt(Math.pow(a_theta_2, 2) + Math.pow(b_theta_2, 2) -
	    		   Math.pow(h_theta_2, 2))) - Math.atan2(b_theta_2, a_theta_2);
	    }

	   private void computeTHETA_2_2(){
	       theta_2 = Math.atan2(h_theta_2, -(Math.sqrt(Math.pow(a_theta_2, 2) + Math.pow(b_theta_2, 2) -
	    		   Math.pow(h_theta_2, 2)))) - Math.atan2(b_theta_2, a_theta_2);
	    }

	   private void compute_AUX_THETA_3(){
	       h_theta_3 = a_theta_2 * sin_theta_2 + b_theta_2 * cos_theta_2 + c_theta_2;
	       k_theta_3 = -a_theta_2 * cos_theta_2 + b_theta_2 * sin_theta_2;
	    }

	   private void computeTHETA_3(){
		   theta_3 = Math.atan2(-d_theta_2 * k_theta_3, d_theta_2 * h_theta_3);
	   }

	   private void compute_AUX_THETA_4(){
	       a_theta_4 = d_2 - d_3 + p_x * sin_theta_1 - p_y * cos_theta_1;
	       b_theta_4 = a_2 * sin_theta_3 + d_1 * Math.sin(theta_2 + theta_3) + p_x * cos_theta_1 *
	    		   Math.cos(theta_2 + theta_3) + p_y *  Math.cos(theta_2 + theta_3) * sin_theta_1 -
	    		   p_z *  Math.sin(theta_2 + theta_3);
	    }

	   private void computeTHETA_4_1(){
		   theta_4 = Math.atan2(-a_theta_4, b_theta_4);
	   }

	   private void computeTHETA_4_2() {
		   theta_4 = Math.atan2(a_theta_4, -b_theta_4);
	    }
	   
	   private void computeTHETA_6() {
	       theta_6 = Math.atan2(-n_x * cos_theta_1 * Math.cos(theta_2 + theta_3) * sin_theta_4 - n_x *
	    		   cos_theta_4 * sin_theta_1 + n_y * cos_theta_1 * cos_theta_4 - n_y * 
	    		   Math.cos(theta_2 + theta_3) * sin_theta_1 * sin_theta_4 + n_z * 
	    		   Math.sin(theta_2 + theta_3) * sin_theta_4, -o_x * cos_theta_1 * 
	    		   Math.cos(theta_2 + theta_3) * sin_theta_4 - o_x * cos_theta_4 * 
	    		   sin_theta_1 + o_y * cos_theta_1 * cos_theta_4 - o_y * Math.cos(theta_2 + theta_3) *
	    		   sin_theta_1 * sin_theta_4 + o_z * Math.sin(theta_2 + theta_3) * sin_theta_4);
	    }

	   private void computeTHETA_5() {
	       theta_5 = Math.atan2(a_x * cos_theta_1 * Math.cos(theta_2 + theta_3) * cos_theta_4 - a_x * 
	    		   sin_theta_1 * sin_theta_4 + a_y * cos_theta_1 * sin_theta_4 + a_y * Math.cos(theta_2 + theta_3) *
	    		   cos_theta_4 * sin_theta_1 - a_z * cos_theta_4 * Math.sin(theta_2 + theta_3), a_x * cos_theta_1 *
	    		   Math.sin(theta_2 + theta_3) + a_y * sin_theta_1 * Math.sin(theta_2 + theta_3) + a_z * Math.cos(theta_2 + theta_3));
	    }

	//=============================================================================
	// Pretty printing
	//=============================================================================

	   private void pretty(){	        
	        System.out.println("theta_1: " + theta_1*sTO_DEGREES);
	        System.out.println("theta_2: " + theta_2*sTO_DEGREES);
	        System.out.println("theta_3: " + theta_3*sTO_DEGREES);
	        System.out.println("theta_4: " + theta_4*sTO_DEGREES);
	        System.out.println("theta_5: " + theta_5*sTO_DEGREES);
	        System.out.println("theta_6: " + theta_6*sTO_DEGREES);
	    }
 
	//=============================================================================
	// Check joint limits
	//=============================================================================
/*
	    public int checkJointLimits()
	    {
	        if ((theta_1 < sJOINT_MINIMA[0]) || (theta_1 > sJOINT_MAXIMA[0]) || 
	            (theta_2 < sJOINT_MINIMA[1]) || (theta_2 > sJOINT_MAXIMA[1]) || 
	            (theta_3 < sJOINT_MINIMA[2]) || (theta_3 > sJOINT_MAXIMA[2]) || 
	            (theta_4 < sJOINT_MINIMA[3]) || (theta_4 > sJOINT_MAXIMA[3]) || 
	            (theta_5 < sJOINT_MINIMA[4]) || (theta_5 > sJOINT_MAXIMA[4]) || 
	            (theta_6 < sJOINT_MINIMA[5]) || (theta_6 > sJOINT_MAXIMA[5]))

	           return 0;

	        return 1;
	    }
  */
 
}
