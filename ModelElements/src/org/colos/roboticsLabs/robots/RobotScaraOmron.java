package org.colos.roboticsLabs.robots;

import java.awt.Color;

import org.colos.roboticsLabs.robots.utils.connections.*;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementArrow;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.Group;
import org.opensourcephysics.drawing3d.utils.transformations.ZAxisRotation;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.numerics.Matrix3DTransformation;
import org.colos.roboticsLabs.robots.utils.maths.*;

import com.jme.bounding.OrientedBoundingBox;
import com.jme.math.Vector3f;


/**
 * @author Almudena Ruiz
 */
public class RobotScaraOmron extends AbstractRobot {
	static private final int sDOF = 4;
	static private final int sCARTESIAN_ARRAYS_LENGTH = 4;
	
	static private final double sLengthOfBase = 0.105;// (d1)
	static private final double sLengthOfForearm = 0.322;// (d2)
	static private final double sWidthOfShoulder = 0.105;// (l1)
	static private final double sWidthOfArm = 0.075;// (l2)
	static protected final double sHighAxisZ = 0.05; 
	static protected double mHighBasement = 0.0; 
//	static private final double sLengthOfTool = 0;

	static private final double[] sHOME = { 0, 0, 0, 0};
	static private final double[] sJOINT_MINIMA = { -125, -145, -0.003, -360 };
	static private final double[] sJOINT_MAXIMA = { 125, 145, 0.05, 360 };
	static private final double[] sSPEED_LIMITS = { 3.3, 3.3, 0.9, 1.7 }; // (m/seg)
	static private final double[] sACCEL_LIMITS = { 19, 19, 16, 756 * sTO_DEGREES }; // ( m/seg^2)

	static private final String sBASE = "org/colos/roboticsLabs/robots/obj_files/ScaraOmron/";
	
	
	// Denavit-Hartenberg parameters
	static double q1, q2, q3, q4 = 0.0;
	static private final int[] sDHq = { 0, 1, 6, 3 };
	//static private final double[] sDH_PARAMETERS = {q1, q2, 0, q4, sLengthOfBase, 0, q3, -sLengthOfForearm, sWidthOfShoulder, sWidthOfArm, 0, 0, 0, 0, 0, Math.PI, };
	
	static private final double[] sDH_PARAMETERS = {q1, q2, 0, q4, sLengthOfBase, 0, q3, -0.105, sWidthOfShoulder, sWidthOfArm, 0, 0, 0, 0, 0, Math.PI};
  // Remote connection
	private TelnetClient TelnetClient;
	// Graphics
	private AxisRotationWrapper mLink1RotationZ, mLink2RotationZ, mLink4RotationZ;
	private double mLink3TraslationZ;
	private Group Link3Group;
	private ElementObject Link3Obj;
	protected double factorSize = 2.0;
	 private Group mainGroup;
     protected OrientedBoundingBox obbObject;
     protected ElementBox OBBbox;
  /**
   * Public constructor
   */
  public RobotScaraOmron() {
		super();
		mCurrentQ = getHome();
	}

	// ---------------------------------------
	// Information about the robot
	// ---------------------------------------

  public int getDOF() {return sDOF;}

  public int getCartesianArraysLength() { return sCARTESIAN_ARRAYS_LENGTH; }
  
  public double[] getHome() {
	  double[] home = new double[sDOF];
	  System.arraycopy(sHOME,0,home,0,sDOF);
	  return home; 
  }
  
  protected int getDHQ(int index) { return sDHq[index]; }
  
  public double[] getDHParameters() { 
	  double[] dhParam = new double[sDH_PARAMETERS.length];
	  System.arraycopy(sDH_PARAMETERS,0,dhParam,0,sDH_PARAMETERS.length);
	  return sDH_PARAMETERS; 
  }

  protected double[] calculateDH(double[] q){
    //REVIEW
    sDH_PARAMETERS[0] = q[0]*sTO_RADIANS;;
    sDH_PARAMETERS[1] = q[1] * sTO_RADIANS;
    //sDH_PARAMETERS[6] = -q[2];
    sDH_PARAMETERS[6] = mHighBasement + sHighAxisZ -q[2];
    sDH_PARAMETERS[3] = q[3] * sTO_RADIANS;
    Matrix A01 = FKinematics(sDH_PARAMETERS[0], 1);
    Matrix A02 = FKinematics(sDH_PARAMETERS[1], 2);
    Matrix A03 = FKinematics(sDH_PARAMETERS[6], 3);  
    Matrix A04 = FKinematics(q[3] * sTO_RADIANS,4);  
    return A04.getVectorColumn(3,0,2);
  }
  
  public double[] inverseKinematics(double[] coordinates, double[] q, boolean shoulder, boolean elbow, boolean wrist) {
    if (coordinates.length != sCARTESIAN_ARRAYS_LENGTH) return null;
    if (q == null) 
    	q = new double[sDOF];
    
    double l_OA = sLengthOfBase - sLengthOfForearm; // Esto no seria solo d1?
    double l_AB = sWidthOfShoulder;
    double l_BC = sWidthOfArm;
    double theta, beta, phi, sbeta, cbeta, theta1, theta2;

    //System.out.println("Initial Position " + mP0x + " " + mP0y + " " + mP0z);
    double x = coordinates[0] - mP0x;
    double y = coordinates[1] - mP0y;
    // double l3 = mP0z + l_OA - coordinates[2]; 
    // double l3 = mP0z + mHighBasement +sHighAxisZ + coordinates[2]/10.; //asi???
    double l3 = mP0z + mHighBasement +(sHighAxisZ- coordinates[2]);

    // Solution of the second joint: q2
    double a = x * x + y * y - l_AB * l_AB - l_BC * l_BC;
    double b = 2 * l_AB * l_BC;
    double c2 = a / b;
    double s2 = Math.sqrt(1 - c2 * c2);
    theta = Math.atan2(s2, c2);
    if ((new Double(theta)).isNaN())
      theta = 0.0;

    // Solution of the first joint: q1
    double sphi = y;
    double cphi = x;
    if (x < 0)
      phi = Math.atan2(sphi, cphi);
    else {
      if (x == 0.0) {
        if (y < 0)
          phi = -Math.PI / 2.;
        else {
          if (y == 0.0)
            phi = 0; // null vector
          else
            phi = Math.PI / 2.;
        }
      } else
        phi = Math.atan2(sphi, cphi);
    }
    sbeta = l_BC * Math.sin(theta);
    cbeta = l_AB + l_BC * Math.cos(theta);
    beta = Math.atan2(sbeta, cbeta);
    if ((new Double(beta)).isNaN())
      beta = 0.0;

    if (elbow) {
      theta1 = (phi - beta);
      theta2 = theta;
    } else {
      theta1 = (phi - beta);
      theta2 = -theta;
    }

    q[0] = theta1 * sTO_DEGREES;
    q[1] = theta2 * sTO_DEGREES;
    q[2] = l3; 
    q[3] = coordinates[3];

   // System.out.println(q[0] + " " + q[1] + " " +  + q[2] + " "  + q[3]);
    if(!checkJointPosition(new double[]{q[0], q[1], q[2], q[3]})) return null;
    return q;
  }

  //---------------------------------------
  // Positioning
  // ---------------------------------------

 @Override //BORRAR PARA EL NUEVO SCARA CON DIMENSIONES EXACTAS
  public void moveToQ(double[] q) throws Exception{
	if(!checkJointPosition(q)) throw new Exception("The values of the joints are invalid.");
    
	if (hasRestrictions()) {
      double[] p = forwardKinematics(q,null);
      if (!checkValidity(p)) throw new Exception("Trying to move to invalid point: ("+p[0]+","+p[1]+","+p[2]+")");
    }
	
    System.arraycopy(q, 0, mCurrentQ, 0, getDOF());
    if (mHasViewGroup){ 
    	updateView();
    	//if(autocollisionDetection()) throw new Exception(" Autocollision: Trying to move to invalid point");
    }
    if (mIsConnected) moveRealRobot();
  }
  
  
  protected void moveRealRobot() {
    move("P", new PointScara(1000, mCurrentQ));
  }
  
  public void openTool() throws Exception {}
  
  public void closeTool() throws Exception {}
  
  protected boolean setSpeedRealRobot(int speed) throws Exception  {
    return TelnetClient.write("@ SPEED " + speed);
  }

  protected boolean setAccelRealRobot(int accel) throws Exception  {  
    return TelnetClient.write("@ ACCEL " + accel);
  }
  
@Override
  public double[] calculatePositionLab(double[] q){
	 double[] p = forwardKinematics(q, null);
	 double[] positionLab = {factorSize * p[0] + getInitialPosition()[0],  factorSize * p[1] + getInitialPosition()[1], getInitialPosition()[2] + (sHighAxisZ + p[2]/10.) };
	  return positionLab;
  }
	
  //---------------------------------------
  // Restrictions
  // ---------------------------------------
 
  public double getJointMinimum(int joint) {
		if (joint <= 0 || joint > sDOF)
			return Double.NaN;
		return sJOINT_MINIMA[joint - 1];
	}

	public double getJointMaximum(int joint) {
		if (joint <= 0 || joint > sDOF)
			return Double.NaN;
		return sJOINT_MAXIMA[joint - 1];
	}

	public double getJointSpeedMaximum(int joint) {
		if (joint <= 0 || joint > sDOF)
			return Double.NaN;
		return sSPEED_LIMITS[joint - 1];
	}

	public double getJointAccelerationMaximum(int joint) {
		if (joint <= 0 || joint > sDOF)
			return Double.NaN;
		return sACCEL_LIMITS[joint - 1];
	}
	
	//------------------------------------------------
  // Remote communication with the robot
  // -----------------------------------------------
  
  public void connect(String hostIP, int portNumber, String user, String password) throws Exception{
    if(openTelnet(hostIP, portNumber, user, password)) {
      mIsConnected = true;
      servo(true);
    }
    else System.out.println("Eror: The connection is failed");
  }

  public void disconnect() throws Exception{
    closeTelnet();
    servo(false);
    mIsConnected = false;
  }

	public void stopHardware() throws Exception {
		if (mIsConnected) {
			if (!TelnetClient.write("@ HALT PROGRAM FIN"))
				System.out.println("Error");
			else {
				String res = TelnetClient.read();
				if (res == null) {
					System.out.println("Error. Undefined identifier");
					TelnetClient.closeTelnetConnection();
				}
			}
		}
	}
	
	//---------------------------------------
  // Required for implementation
  // ---------------------------------------

	protected Group createViewGroupReal() {
		if (mainGroup!=null) return mainGroup;
		mainGroup = new Group();
	    mHasViewGroup = true;
	
	    mainGroup.setTransformation(Matrix3DTransformation.rotationZ(Math.PI)); 
	   
	    ElementObject Link0Obj = new ElementObject();
	    Link0Obj.setSizeXYZ(0.184*0.55, 0.053*2, 0.105*0.9);
	    Link0Obj.setXYZ(0, 0, 0.016 + Link0Obj.getSizeZ()/2);
	    Link0Obj.setObjectFile(sBASE + "base.obj");
	    Link0Obj.getStyle().setFillColor(Color.GRAY);
	    //Link0Obj.setVisible(false);
	    mainGroup.addElement(Link0Obj);

	    //OrientedBoundingBox0
	    ElementBox OBB0 = new ElementBox();
	    OBB0.setSizeXYZ(0.184, 0.053, 0.105);
	    OBB0.setXYZ(0, 0,  OBB0.getSizeZ()/2.);
	    OBB0.getStyle().setFillColor(Color.RED);
	    OBB0.getStyle().setLineColor(Color.RED);
	    OBB0.getStyle().setDrawingFill(false);
	    OBB0.setVisible(false);
	    mainGroup.addElement(OBB0);    
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
//	        	mainGroup.addElement(triedro[i]);
	        }
	        triedro[0].getStyle().setLineColor(Color.RED);
	        triedro[1].getStyle().setLineColor(Color.YELLOW);
	        triedro[2].getStyle().setLineColor(Color.BLUE);
	        obbTriedroTable.put(obb0, triedro);
	        }
	   
	    Group Link1Group = new Group();
	    Link1Group.setName("Link1Group");
	    Link1Group.setXYZ(-0.05, 0, 0.112-0.006);
	    mainGroup.addElement(Link1Group);

	    ElementObject Link1Obj = new ElementObject();
	    Link1Obj.setSizeXYZ(0.15*0.55, 0.035*2.2 ,0.04*1.8);
	    Link1Obj.setXYZ(-0.043,  0, 0.0075);
	    Link1Obj.setObjectFile(sBASE + "link1.obj");
	    Link1Obj.getStyle().setFillColor(Color.LIGHT_GRAY);
	    Link1Group.addElement(Link1Obj);
	    //Link1Obj.setVisible(false);
	    mLink1RotationZ = new AxisRotationWrapper(new ZAxisRotation(),Link1Group);
	    Link1Group.addSecondaryTransformation(mLink1RotationZ);

	    //OrientedBoundingBox1
	    ElementBox OBB1 = new ElementBox();
		OBB1.setSizeXYZ(0.15, 0.035 ,0.04);
	    OBB1.setXYZ(-0.05, 0, 0.01);
	    OBB1.getStyle().setDrawingFill(false);
	    OBB1.getStyle().setLineColor(Color.BLUE);
	    Link1Group.addElement(OBB1);
	    OBB1.setVisible(false);

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

//	        	mainGroup.addElement(triedro[i]);
	        }
	        triedro[0].getStyle().setLineColor(Color.RED);
	        triedro[1].getStyle().setLineColor(Color.YELLOW);
	        triedro[2].getStyle().setLineColor(Color.BLUE);
	        obbTriedroTable.put(obb1, triedro);
	        }
	         
	    Group Link2Group = new Group();
	    Link2Group.setName("Link2Group");
	   // Link2Group.setXYZ(-0.275, 0.01, 0.208);
	    Link2Group.setXYZ(-0.105, 0, 0.119);
	    Link1Group.addElement(Link2Group);

	    ElementObject Link2Obj = new ElementObject();
	    Link2Obj.setSizeXYZ(0.15*0.75, 0.05*2.2, 0.245*0.5);
	    Link2Obj.setXYZ(-0.003, 0.002, -0.0255);
	    Link2Obj.setObjectFile(sBASE + "link2.obj");
	    Link2Obj.getStyle().setFillColor(Color.GRAY);
	    Link2Group.addElement(Link2Obj);
	    //Link2Obj.setVisible(false);
	    mLink2RotationZ = new AxisRotationWrapper(new ZAxisRotation(),Link2Group);
	    Link2Group.addSecondaryTransformation(mLink2RotationZ);

	    //OrientedBoundingBox2
	    ElementBox OBB2 = new ElementBox();
	    OBB2.setSizeXYZ(0.15, 0.05, 0.245); 
	    OBB2.setXYZ(-0.0275, 0, -0.0255 );
	    OBB2.getStyle().setFillColor(Color.red);
	    OBB2.getStyle().setLineColor(Color.RED);
	    OBB2.getStyle().setDrawingFill(false);
	    Link2Group.addElement(OBB2);
	    OBB2.setVisible(false);
	    
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

//	        	mainGroup.addElement(triedro[i]);
	        }
	        triedro[0].getStyle().setLineColor(Color.RED);
	        triedro[1].getStyle().setLineColor(Color.YELLOW);
	        triedro[2].getStyle().setLineColor(Color.BLUE);
	        obbTriedroTable.put(obb2, triedro);
	        }  
	      
	    Link3Group = new Group();
	    Link3Group.setName("Link3Group");
	  //  Link3Group.setXYZ(-0.175, -0.01, -0.26 - mLink3TraslationZ);
	    Link3Group.setXYZ(-0.068, 0, -0.13 - mLink3TraslationZ);
	  //  Link3Group.setXYZ(-0.05, -0.01, -0.26- mLink3TraslationZ);
	    Link2Group.addElement(Link3Group);

	   Link3Obj = new ElementObject();
	   Link3Obj.setXYZ(0.069, 0, 0);
	   // Link3Obj.setSizeXYZ(0.2, 0.2, 0.2);
	    Link3Obj.setSizeXYZ(0.02*4, 0.02*4, 0.07);
	    Link3Obj.setObjectFile(sBASE + "link3.obj");
	    Link3Obj.getStyle().setFillColor(Color.DARK_GRAY);
	    Link3Group.addElement(Link3Obj);
	   // Link3Obj.setVisible(false);
	    
	    //OrientedBoundingBox3
	    ElementBox OBB3 = new ElementBox();
	    OBB3.setXYZ(0, 0, -0.001);
	    OBB3.setSizeXYZ(0.02, 0.02, 0.075); 
	    OBB3.getStyle().setFillColor(Color.red);
	    OBB3.getStyle().setLineColor(Color.RED);
	    OBB3.getStyle().setDrawingFill(false);
	    OBB3.setVisible(false);
	    Link3Group.addElement(OBB3);

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

//	        	mainGroup.addElement(triedro[i]);
	        }
	        triedro[0].getStyle().setLineColor(Color.RED);
	        triedro[1].getStyle().setLineColor(Color.YELLOW);
	        triedro[2].getStyle().setLineColor(Color.BLUE);
	        obbTriedroTable.put(obb3, triedro);
	        }
	    
	    mLink4RotationZ = new AxisRotationWrapper(new ZAxisRotation(),
	        Link3Group);
	    Link3Group.addSecondaryTransformation(mLink4RotationZ);
	   
	  
	    return mainGroup;
	  }
		
	
	
  protected Group createViewGroup() {
	if (mainGroup!=null) return mainGroup;
	mainGroup = new Group();
    mHasViewGroup = true;
 
    mainGroup.setSizeXYZ(factorSize*0.348,factorSize*0.5, factorSize*0.322);
 
    mainGroup.setTransformation(Matrix3DTransformation.rotationZ(Math.PI)); 
    ElementObject Link0Obj = new ElementObject();
    Link0Obj.setXYZ(0, 0, 0.1+0.04);
    Link0Obj.setSizeXYZ(0.2,0.2,0.2);
    Link0Obj.setObjectFile(sBASE + "base.obj");
    Link0Obj.getStyle().setFillColor(Color.GRAY);
    //Link0Obj.setVisible(false);
    mainGroup.addElement(Link0Obj);

    //OrientedBoundingBox0
    ElementBox OBB0 = new ElementBox();
    OBB0.setXYZ(0, 0, (0.1)*factorSize/2.);
    OBB0.setSizeXYZ((0.184)*factorSize, 0.053*factorSize, (0.1)*factorSize);
    OBB0.getStyle().setFillColor(Color.RED);
    OBB0.getStyle().setLineColor(Color.RED);
    OBB0.getStyle().setDrawingFill(false);
    OBB0.setVisible(false);
    mainGroup.addElement(OBB0);
    
    {
        OrientedBoundingBox obb0 = new OrientedBoundingBox();
        obb0.setExtent(new Vector3f((float)(OBB0.getSizeX()/2),
        		(float)(OBB0.getSizeY()/2), (float)(OBB0.getSizeZ()/2)));
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
    
    Group Link1Group = new Group();
    Link1Group.setName("Link1Group");
    Link1Group.setXYZ(-0.11, 0, 0.22);
    mainGroup.addElement(Link1Group);

    ElementObject Link1Obj = new ElementObject();
    Link1Obj.setXYZ(-0.115, -0.001, -0.01);
    Link1Obj.setSizeXYZ(0.2, 0.2, 0.2);
    Link1Obj.setObjectFile(sBASE + "link1.obj");
    Link1Obj.getStyle().setFillColor(Color.LIGHT_GRAY);
    Link1Group.addElement(Link1Obj);
  // Link1Obj.setVisible(false);
    mLink1RotationZ = new AxisRotationWrapper(new ZAxisRotation(),Link1Group);
    Link1Group.addSecondaryTransformation(mLink1RotationZ);

    //OrientedBoundingBox1
    ElementBox OBB1 = new ElementBox();
   	OBB1.setXYZ(-0.115,  -0.001, -0.01);
   	OBB1.setSizeXYZ(0.15*factorSize, 0.046*factorSize, 0.028*factorSize);
    OBB1.getStyle().setDrawingFill(false);
    OBB1.getStyle().setLineColor(Color.BLUE);
    Link1Group.addElement(OBB1);
    OBB1.setVisible(false);

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
      
    
    Group Link2Group = new Group();
    Link2Group.setName("Link2Group");
    Link2Group.setXYZ(-0.275, 0.01, 0.208);
    Link1Group.addElement(Link2Group);

    ElementObject Link2Obj = new ElementObject();
    Link2Obj.setXYZ(0, -0.004, 0);
    Link2Obj.setSizeXYZ(0.3, 0.3, 0.3);
    Link2Obj.setObjectFile(sBASE + "link2.obj");
    Link2Obj.getStyle().setFillColor(Color.GRAY);
    Link2Group.addElement(Link2Obj);
    //Link2Obj.setVisible(false);
    mLink2RotationZ = new AxisRotationWrapper(new ZAxisRotation(),Link2Group);
    Link2Group.addSecondaryTransformation(mLink2RotationZ);

    //OrientedBoundingBox2
    ElementBox OBB2 = new ElementBox();
    OBB2.setXYZ(-0.054, -0.004, 0 +0.04);
   OBB2.setSizeXYZ(0.2*factorSize, 0.054*factorSize, (0.218+0.08)*factorSize);
    OBB2.setSizeXYZ(0.21*factorSize, 0.05*factorSize, (0.218)*factorSize); 
    OBB2.getStyle().setFillColor(Color.red);
    OBB2.getStyle().setLineColor(Color.RED);
    OBB2.getStyle().setDrawingFill(false);
    Link2Group.addElement(OBB2);
    OBB2.setVisible(false);
    
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
   
    Link3Group = new Group();
    Link3Group.setName("Link3Group");
    Link3Group.setXYZ(-0.175, -0.01, -0.26 - mLink3TraslationZ);
    Link2Group.addElement(Link3Group);

    Link3Obj = new ElementObject();
    Link3Obj.setXYZ(0.175, 0, 0);
    Link3Obj.setSizeXYZ(0.2, 0.2, 0.2);
    Link3Obj.setObjectFile(sBASE + "link3.obj");
    Link3Obj.getStyle().setFillColor(Color.DARK_GRAY);
    Link3Group.addElement(Link3Obj);
   // Link3Obj.setVisible(false);
    
    //OrientedBoundingBox3
    ElementBox OBB3 = new ElementBox();
    OBB3.setXYZ(0, 0, -0.1);
    OBB3.setSizeXYZ(0.02*factorSize, 0.02*factorSize, 0.05*factorSize); 
    OBB3.getStyle().setFillColor(Color.red);
    OBB3.getStyle().setLineColor(Color.RED);
    OBB3.getStyle().setDrawingFill(false);
    OBB3.setVisible(false);
    Link3Group.addElement(OBB3);

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
    
    mLink4RotationZ = new AxisRotationWrapper(new ZAxisRotation(),
        Link3Group);
    Link3Group.addSecondaryTransformation(mLink4RotationZ);
   
    
    return mainGroup;
  }
	
		
	protected void updateView() {
    if (mHasViewGroup) {
      mLink1RotationZ.mTr.setAngle(mCurrentQ[0] * sTO_RADIANS);
      mLink2RotationZ.mTr.setAngle(mCurrentQ[1] * sTO_RADIANS);
      //mLink3TraslationZ = (mCurrentQ[2])/1000.; // Pasamos q(2) a mm.
      mLink3TraslationZ = (mCurrentQ[2]);
     // Link3Group.setZ(-0.13 - mLink3TraslationZ); REAL
      Link3Group.setZ(-0.26 - mLink3TraslationZ);
      mLink4RotationZ.mTr.setAngle(mCurrentQ[3] * sTO_RADIANS);
    }
  }


	public void attachObject(Element object) {
		if (object == null) System.out.println("Error: It is necessary to add a element");
		else {	
			if(mHasViewGroup){ 
		      //  double[] center = object.toSpaceFrame(new double[] {0,0,0});
				//OBB	
				 object.setXYZ(0,0, - Link3Obj.getSizeZ()/2. - object.getSizeZ()/2 );	
				 OBBbox = new ElementBox();
				// OBBbox.setXYZ(center[0],center[1], center[2] - Link3Obj.getSizeZ()/2. - object.getSizeZ()/2 );	
				// OBBbox.setXYZ(0, 0,- Link3Obj.getSizeZ()/2. - object.getSizeZ()/2 );	 
				 OBBbox.setXYZ(object.getX(), object.getY(), object.getZ());		
				 OBBbox.setSizeXYZ(object.getSizeX(),object.getSizeY(),object.getSizeZ());
				// OBBbox.getStyle().setDrawingFill(false);
				// OBBbox.getStyle().setFillColor(object.getStyle().getFillColor());
					
				 OBBbox.setVisible(false);
				 Link3Group.addElement(OBBbox);
				 Link3Group.addElement(object);
				
			    obbObject = new OrientedBoundingBox();	
		        obbObject.setExtent(new Vector3f((float)(OBBbox.getSizeX()/2), (float)(OBBbox.getSizeY()/2), (float)(OBBbox.getSizeZ()/2)));   
		        obbTable.put(obbObject, OBBbox);
		        obbList.add(obbObject);
		       
		        Element[] triedro = new Element[3];
		        for (int i=0; i<3; i++) {
		        	triedro[i] = new ElementArrow();
		        	triedro[i].getStyle().setLineColor(Color.RED);
		        	triedro[i].getStyle().setLineWidth(4);

		        }
		        triedro[0].getStyle().setLineColor(Color.RED);
		        triedro[1].getStyle().setLineColor(Color.YELLOW);
		        triedro[2].getStyle().setLineColor(Color.BLUE);
		        obbTriedroTable.put(obbObject, triedro);
		        
				//object.setXYZ(center[0],center[1], center[2] - Link3Obj.getSizeZ()/2. - object.getSizeZ()/2 );	
			
				
				//Link3Group.addElement(object);//Adds object to the view		
			}
		}	
	}
	
	
	public void detachObject(Element object){		
		if(mHasViewGroup){		
			obbTable.remove(obbObject);
			obbList.remove(obbObject);
			obbTriedroTable.remove(obbObject);
			Link3Group.removeElement(OBBbox);
			Link3Group.removeElement(object);
			double[] pos = Link3Group.toSpaceFrame(new double[]{0,0,0});
		object.setXYZ(pos[0]+object.getSizeX(), pos[1], pos[2]);
		//	object.setXYZ(pos[0], pos[1], pos[2]);
		}
	}
	

	//---------------------------------------
  // Private methods
  // ---------------------------------------

	/**
   * Opens a new Telnet connection to establish the communication with the real robot
   * @param hostIP the IP address
   * @param portNumber the port number
   * @param user the user name
   * @param password the password
   * @return true if the Telnet connection has been established or false in otherwise
   * @throws Exception
   */

  private boolean openTelnet(String hostIP, int portNumber, String user, String password){
    TelnetClient = new TelnetClient(hostIP, portNumber, user, password);
    if (!TelnetClient.createTelnetConnection()) return false;
    return true;
  }
  

  /**
   * Closes the Telnet connection established
   * @return true if the Telnet connection has been disconnected or false in otherwise
   */

  private boolean closeTelnet() {
    if (!TelnetClient.closeTelnetConnection())return false;
    return true;
  }
  
  /**
   * Turns on the servos and off of the remote robot
   * @param on true to turn on the servos and false in otherwise
   * @return true if the servos are turn on or off and false in otherwise
   */

  private boolean servo(boolean on){
    if(on){
      if (!TelnetClient.write("@ SERVO ON"))return false;
      else {
        if (TelnetClient.read() == null) {
          System.out.println("Error. Undefined identifier");
          TelnetClient.closeTelnetConnection();
          return false;
        } else{
          System.out.println("The servos are turned");
          return true;
        }     
      }
    }
    else{
      if (!TelnetClient.write("@ SERVO OFF"))return false;
      else {
        if (TelnetClient.read() == null) {
          System.out.println("Error. Undefined identifier");
          TelnetClient.closeTelnetConnection();
          return false;
        } else{
          System.out.println("The servos are turned off");
          return true;
        }
      }
    }
  }

  
  //REVIEW
  

/*
	public boolean setAccelPercent(int[] accel_percent) {
		if (accel_percent.length != sDOF)
			return false;
		for (int i = 0; i < accel_percent.length; i++) {
			if (!setAccel(accel_percent[i]))
				return false;
		}
		return true;
	}
	*/

	/**
	 * Sends a point to the real robot
	 * 
	 * @param point
	 *            a Scara Point
	 * @return true if the point has been sent or false in otherwise
	 */

	public boolean sendPoint(PointScara point) {
		if (!TelnetClient.write("@ " + point.name + "= " + point.X + " "
				+ point.Y + " " + point.Z + " " + " " + point.R + " " + point.A
				+ " " + point.B))
			return false;
		else {
			String res = TelnetClient.read();
			if (res == null) {
				System.out.println("Error. Undefined identifier");
				TelnetClient.closeTelnetConnection();
				return false;
			} else
				return true;
		}
	}

	/**
	 * Sends a point to the real robot
	 * 
	 * @param name
	 *            the name of the point (it has to be in the form P + a number,
	 *            for example P3)
	 * @param coord
	 *            the Cartesian coordinates of the point
	 * @return true if the point has been sent or false in otherwise
	 */

	public boolean sendPoint(String name, double[] coord) {
		PointScara point = new PointScara(name, coord);
		if (sendPoint(point) == false)
			return false;
		else {
			String res = TelnetClient.read();
			if (res == null) {
				System.out.println("Error. Undefined identifier");
				TelnetClient.closeTelnetConnection();
				return false;
			} else
				return true;
		}
	}

	/**
	 * Executes an absolute movement of main robot axes. Movement type: PTP, P,
	 * L or C
	 * 
	 * @param type
	 *            the type of movement (PTP, P, L or C)
	 * @param namePoint
	 *            the name of the end point of the movement
	 * @return true if the movement has been completed or false in otherwise
	 */

	public boolean move(String type, String namePoint) {

		if (!TelnetClient.write("@ MOVE " + type.toUpperCase() + ", " + namePoint))
			return false;
		if (TelnetClient.read() == null) {
				System.out.println("Error. Undefined identifier");
				TelnetClient.closeTelnetConnection();
				return false;
			} 
		return true;
	}

	/**
	 * Executes an absolute movement of main robot axes. Movement type: PTP, P,
	 * L or C
	 * 
	 * @param type
	 *            the type of movement (PTP, P, L or C)
	 * @param p
	 *            the end point of the movement. The point is of type Scara
	 *            Point
	 * @return true if the movement has been completed or false in otherwise
	 */

	public boolean move(String type, PointScara p) {
		if (!TelnetClient.write("@ MOVE " + type.toUpperCase() + ", " + p.X
				+ " " + p.Y + " " + p.Z + " " + p.R + " " + p.A + " " + p.B))
			return false;
		if (TelnetClient.read() == null) {
				System.out.println("Error. Undefined identifier");
				TelnetClient.closeTelnetConnection();
				return false;
			}
		return true;
	}

	/**
	 * Executes an absolute movement of main robot axes. You can specify speed,
	 * velocity, acceleration... Movement type: PTP, P, L or C
	 * 
	 * @param type
	 *            the type of movement (PTP, P, L or C)
	 * @param namePoint
	 *            the name of the end point of the movement
	 * @param option
	 *            the possible options are "V" (velocity) or "S" (speed)
	 * @param optValue
	 *            the value velocity (from 1 to 750mm/sec both included) or the
	 *            value speed (from 1 to 100 both included)
	 * @return true if the movement has been completed or false in otherwise
	 */

	public boolean move(String type, String namePoint, String option,
			int optValue) {

		if (option == "S") {

			if (optValue > 0 && optValue <= 100) {
				if (!TelnetClient.write("@ MOVE " + type.toUpperCase() + ", "
						+ namePoint + ", " + "S = " + optValue))
					return false;
				else {
					String res = TelnetClient.read();
					if (res == null) {
						System.out.println("Error. Undefined identifier");
						TelnetClient.closeTelnetConnection();
						return false;
					}
					return true;
				}

			} else {
				System.out.println("Error: Speed is out of range");
				return false;
			}
		} else {
			if (option == "V") {
				if (!TelnetClient.write("@ MOVE " + type.toUpperCase() + ", "
						+ namePoint + ", " + "VEL = " + optValue))
					return false;
				else {
					String res = TelnetClient.read();
					if (res == null) {
						System.out.println("Error. Undefined identifier");
						TelnetClient.closeTelnetConnection();
						return false;
					}
					return true;
				}
			} else {
				System.out
						.println("Error: Invalid option. The options are S or V.");
				return false;
			}
		}
	}

	/**
	 * Executes an absolute movement of main robot axes. You can specify speed,
	 * velocity, acceleration...
	 * 
	 * @param type
	 *            the type of movement (PTP, P, L or C)
	 * @param p
	 *            the point of Scara type.
	 * @param option
	 *            the possible options are "V" (velocity) or "S" (speed)
	 * @param optValue
	 *            the value velocity (from 1 to 750mm/sec both included) or the
	 *            value speed (from 1 to 100 both included)
	 * @return true if the movement has been completed or false in otherwise
	 */
	public boolean move(String type, PointScara p, String option, int optValue) {

		if (option == "S") {

			if (optValue > 0 && optValue <= 100) {
				if (!TelnetClient.write("@ MOVE " + type.toUpperCase() + ", "
						+ p.X + " " + p.Y + " " + p.Z + " " + p.R + " " + p.A
						+ " " + p.B + ", " + "S = " + optValue))
					return false;
				else {
					String res = TelnetClient.read();
					if (res == null) {
						System.out.println("Error. Undefined identifier");
						TelnetClient.closeTelnetConnection();
						return false;
					}
					return true;
				}

			} else {
				System.out.println("Error: Speed is out of range (1-100%)");
				return false;
			}
		} else {
			if (option == "V") {
				if (optValue >= 1 && optValue <= 750) {
					if (!TelnetClient.write("@ MOVE " + type.toUpperCase()
							+ ", " + p.X + " " + p.Y + " " + p.Z + " " + p.R
							+ " " + p.A + " " + p.B + ", " + "VEL = "
							+ optValue))
						return false;
					else {
						String res = TelnetClient.read();
						if (res == null) {
							System.out.println("Error. Undefined identifier");
							TelnetClient.closeTelnetConnection();
							return false;
						} else
							return true;
					}
				} else {
					System.out
							.println("Error: Vel is out of range (1-750mm/sec)");
					return false;
				}
			} else {
				System.out
						.println("Error: Invalid option. The options are S or V.");
				return false;
			}
		}
	}

	/**
	 * Executes a delay
	 * 
	 * @param delay
	 *            the value of the delay (from 1 to 3600000ms both included)
	 * @return true if the delay has been executed or false in otherwise
	 */

	public boolean delay(int delay) {
		if(delay < 10 || delay > 3600000){
		  System.out.println("Error: Delay is out of range (1-3600000 ms)");
		  return false;
		}
	  if (!TelnetClient.write("@ DELAY " + delay)) return false;
		return true;
	}

	/**
	 * Reads a Point saved in the real robot
	 * 
	 * @param point
	 *            the point name that we want to read (the name is written in
	 *            the way "Pnumber, for example "P2").
	 * @return a point of type Scara
	 */

	public PointScara readPoint(String point) {

		if (!TelnetClient.write("@ READ " + point.toUpperCase())) {
			System.out.println("Error escribiendo.");
			TelnetClient.closeTelnetConnection();
			return null;
		} else {
			String res = "";
			if ((res = TelnetClient.read()) == null) {
				System.out.println("Error leyendo punto.");
				TelnetClient.closeTelnetConnection();
				return null;
			} else {
				System.out.println(res);
				return PointScara.createPoint(res);
			}
		}
	}

}
