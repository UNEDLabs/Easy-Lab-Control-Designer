package org.colos.roboticsLabs.components;

import java.awt.Color;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementTetrahedron;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.ElementCylinder;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.drawing3d.Group;
import org.opensourcephysics.drawing3d.utils.transformations.Matrix3DTransformation;

public class Belt extends AbstractComponent {

	static private double sLength = 1.3; // unit in meters
	static private double sWidth = 0.24;// unit in meters
	static private double sHigh = 0.88;// unit in meters
	static private double sDiameterRoller = 0.09; // unit in meters
	static private double sMotorFrequency = 50; // unit in Hz
	static private int sVariatorFrequency = 40; // unit in Hz
	static private double sRev = 1370;
	static private double sReductionFactor = 40;
	static private double sEncoderPulses = 2000;

	// static private double mDistancePulse = (Math.PI * sDiameterRoller)/sEncoderPulses;

	double mLength = sLength;
	double mWidth = sWidth;
	double mHigh = sHigh;
	protected boolean mIsConnected;
	protected boolean mHasViewGroup = false;
	protected boolean mHasObjects = false;

	private java.util.Set<Element> mObjectsSet = new java.util.HashSet<Element>();
	protected boolean mEncoder;

	int mFrequency = sVariatorFrequency;
	double mRevMotor;
	double mRPM;
	double mLinealDistance; // linear distance per seconds
	double mPulses = 0;
	double timeBelt = 0.0;
	double distanceBelt = 0.0;
	
	Group mbeltGroup = new Group();
	Group arrowGroup1, arrowGroup2;
	ElementCylinder led;
	Thread mEncoderThread = null;
	Thread mMotionThread = null;
	static private final int sDELAY = 100; // 10 times per second

	public Belt(boolean encoder) {
		super();
		mEncoder = encoder;
		initializeVariables();
	};

	public Belt(double length, double width, double high, boolean encoder) {
		super();
		if (length > 0.0)
			mLength = length;
		if (width > 0.0)
			mWidth = width;
		if (high > 0.0)
			mHigh = high;
		mEncoder = encoder;
		initializeVariables();
	}

	private void initializeVariables() {
		mRevMotor = (mFrequency * sRev) / (sMotorFrequency);
	//	System.out.println("RevMotor: " + mRevMotor);
		mRPM = mRevMotor / sReductionFactor;
	//	System.out.println("RPM: " + mRPM);
		mLinealDistance = (mRPM * (sDiameterRoller * Math.PI)) / 60.; // (meters/seg)
	}

	/**
	 * Turns on the movement of the belt
	 */
	public void connect() {
		mIsConnected = true;
		playBelt();
		if (mHasViewGroup) updateView();
	}

	/**
	 * Stops the movement of the belt
	 */

	public void disconnect() {
		mIsConnected = false;
		pauseBelt();
		if (mHasViewGroup) updateView();
	}
	
	/**
	 * Returns the length of the belt
	 * @return 
	 */
	public double getLength(){return mLength;}
	
	/**
	 * Returns the width of the belt
	 * @return
	 */
	
	public double getWidth(){return mWidth;}
	
	/**
	 * Returns the high of the belt
	 * @return
	 */
	
	public double getHigh(){return mHigh;}

	/**
	 * Sets the variator frequency of the belt
	 * 
	 * @param variatorFrequency
	 *            , the variator frequency (unit in Hz)
	 */
	public void setVariatorFrequency(int variatorFrequency) {
		mFrequency = variatorFrequency;
		initializeVariables();
	}

	/**
	 * Returns the actual frequency of the belt variator
	 * 
	 * @return mFrequency, the variator frequency (unit in Hz)
	 */
	public int getVariatorFrequency() {return mFrequency;}

	/**
	 * Returns the number of encoder pulses
	 * 
	 * @return mPulses
	 */

	public double getNumberOfPulses() {return mPulses;}
	
	/**
	 * Returns the linear distance of the belt in meters/seconds
	 * 
	 * @return mLinealDistance
	 */

	public double getLinealDistance() {return mLinealDistance;}


	protected Group createViewGroup() {
		mHasViewGroup = true;
		//mbeltGroup = new Group();
		mbeltGroup.setName("beltGroup");
		mbeltGroup.setTransformation(Matrix3DTransformation.rotationZ(Math.PI / 2.));

		ElementBox basement = new ElementBox();
		basement.setXYZ(0, 0, mHigh - 0.09);
		basement.setSizeXYZ(mLength, mWidth, 0.18);
		basement.getStyle().setFillColor(Color.GRAY);
		mbeltGroup.addElement(basement);

		ElementBox basement2 = new ElementBox();
		basement2.setXYZ(0, 0, mHigh - 0.089);
		basement2.setSizeXYZ(mLength, mWidth - 0.04, 0.18);
		basement2.getStyle().setFillColor(Color.WHITE);
		mbeltGroup.addElement(basement2);

		ElementCylinder column1 = new ElementCylinder();
		column1.setXYZ(mLength / 4., mWidth / 4., (mHigh - 0.18) / 2.);
		column1.setSizeXYZ(0.05, 0.05, mHigh - 0.18);
		column1.getStyle().setFillColor(Color.DARK_GRAY);
		mbeltGroup.addElement(column1);

		ElementCylinder column2 = new ElementCylinder();
		column2.setXYZ(mLength / 4., -mWidth / 4., (mHigh - 0.18) / 2.);
		column2.setSizeXYZ(0.05, 0.05, mHigh - 0.18);
		column2.getStyle().setFillColor(Color.DARK_GRAY);
		mbeltGroup.addElement(column2);

		ElementCylinder column3 = new ElementCylinder();
		column3.setXYZ(-mLength / 4., mWidth / 4., (mHigh - 0.18) / 2.);
		column3.setSizeXYZ(0.05, 0.05, mHigh - 0.18);
		column3.getStyle().setFillColor(Color.DARK_GRAY);
		mbeltGroup.addElement(column3);

		ElementCylinder column4 = new ElementCylinder();
		column4.setXYZ(-mLength / 4., -mWidth / 4., (mHigh - 0.18) / 2.);
		column4.setSizeXYZ(0.05, 0.05, mHigh - 0.18);
		column4.getStyle().setFillColor(Color.DARK_GRAY);
		mbeltGroup.addElement(column4);

		// Control Panel
		ElementBox control = new ElementBox();
		control.setXYZ(-mLength / 4., -mWidth / 2. - 0.01, mHigh - 0.09);
		control.setSizeXYZ(0.02, 0.15, 0.18);
		control.getStyle().setFillColor(Color.DARK_GRAY);
		control.setTransformation(Matrix3DTransformation
				.rotationZ(Math.PI / 2.));
		mbeltGroup.addElement(control);

		led = new ElementCylinder();
		led.setXYZ(-mLength / 4., -mWidth / 2. - 0.02, mHigh - 0.09);
		led.setSizeXYZ(0.08, 0.08, 0.005);
		if (mIsConnected)
			led.getStyle().setFillColor(Color.GREEN);
		else
			led.getStyle().setFillColor(Color.RED);
		led.setTransformation(Matrix3DTransformation.rotationX(Math.PI / 2.));
		mbeltGroup.addElement(led);

		// Movement
		arrowGroup1 = new Group();
		// arrowGroup1.setXYZ(mLength/2.-0.15,0,mHigh);
		arrowGroup1.setXYZ(0, 0, mHigh);
		mbeltGroup.addElement(arrowGroup1);

		ElementTetrahedron f1 = new ElementTetrahedron();
		f1.setXYZ(0, 0, 0.003);
		f1.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		f1.getStyle().setFillColor(Color.YELLOW);
		f1.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup1.addElement(f1);

		ElementTetrahedron f2 = new ElementTetrahedron();
		f2.setXYZ(-0.03, 0, 0.004);
		f2.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		f2.getStyle().setFillColor(Color.WHITE);
		f2.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup1.addElement(f2);

		ElementTetrahedron f3 = new ElementTetrahedron();
		f3.setXYZ(-0.06, 0, 0.005);
		f3.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		f3.getStyle().setFillColor(Color.YELLOW);
		f3.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup1.addElement(f3);

		ElementTetrahedron f4 = new ElementTetrahedron();
		f4.setXYZ(-0.09, 0, 0.006);
		f4.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		f4.getStyle().setFillColor(Color.WHITE);
		f4.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup1.addElement(f4);

		arrowGroup2 = new Group();
		arrowGroup2.setXYZ(-mLength / 2. + 0.15, 0, mHigh);
		mbeltGroup.addElement(arrowGroup2);

		ElementTetrahedron fb1 = new ElementTetrahedron();
		fb1.setXYZ(0, 0, 0.003);
		fb1.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		fb1.getStyle().setFillColor(Color.YELLOW);
		fb1.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup2.addElement(fb1);

		ElementTetrahedron fb2 = new ElementTetrahedron();
		fb2.setXYZ(-0.03, 0, 0.004);
		fb2.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		fb2.getStyle().setFillColor(Color.WHITE);
		fb2.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup2.addElement(fb2);

		ElementTetrahedron fb3 = new ElementTetrahedron();
		fb3.setXYZ(-0.06, 0, 0.005);
		fb3.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		fb3.getStyle().setFillColor(Color.YELLOW);
		fb3.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup2.addElement(fb3);

		ElementTetrahedron fb4 = new ElementTetrahedron();
		fb4.setXYZ(-0.09, 0, 0.006);
		fb4.setSizeXYZ(0.001, mWidth - 0.05, mWidth / 2.);
		fb4.getStyle().setFillColor(Color.WHITE);
		fb4.setTransformation(Matrix3DTransformation.rotationY(Math.PI / 2.));
		arrowGroup2.addElement(fb4);

		// Encoder
		if (mEncoder) {
			ElementObject encoderObj = new ElementObject();
			encoderObj.setXYZ(mLength / 4. + 0.25, -mWidth / 2. - 0.05,
					mHigh - 0.08);
			encoderObj.setSizeXYZ(0.05, 0.13, 0.13);
			encoderObj
					.setObjectFile("org/colos/roboticsLabs/components/obj_files/encoder.obj");
			encoderObj.getStyle().setFillColor(Color.DARK_GRAY);
			encoderObj.setTransformation(Matrix3DTransformation
					.rotationZ(Math.PI / 2.));
			mbeltGroup.addElement(encoderObj);

		}

		return mbeltGroup;
	}

	protected void updateView() {
		if (mIsConnected) 
			led.getStyle().setFillColor(Color.GREEN);
		else
			led.getStyle().setFillColor(Color.RED);
	}

	private void moveBelt() {

		double delta = mLinealDistance / 10; // AJUSTAR // 10 times per second
		if (arrowGroup1.getX() < mLength / 2. - 0.1)
			arrowGroup1.setX(arrowGroup1.getX() + delta);
		else
			arrowGroup1.setX(-mLength / 2. + 0.1);

		if (arrowGroup2.getX() < mLength / 2. - 0.1)
			arrowGroup2.setX(arrowGroup2.getX() + delta);

		else
			arrowGroup2.setX(-mLength / 2. + 0.1);

		// System.out.println("f2 " +arrowGroup2.getX());

		timeBelt = timeBelt + 0.1;
		//System.out.println("timeBelt =" + timeBelt);
		distanceBelt = distanceBelt + delta;
	//	System.out.println("distanceBelt =" + distanceBelt);
		if (mEncoder) {
			mPulses = (distanceBelt * sEncoderPulses) / (sDiameterRoller * Math.PI);
			//System.out.println("Pulses =" + mPulses);
		}
	}
	
	
	public void attachObject(Element object) {
		if (object == null) System.out.println("Error: It is necessary to add a element");
		else {	
			mObjectsSet.add(object); 	
			if(mHasViewGroup){
				object.setXYZ(object.getY(), object.getX()-object.getSizeX() ,sHigh + object.getSizeZ()/2);	
				mbeltGroup.addElement(object);		 //Adds object to the view			
			}	
		}	
	}
	
	public void detachObject (Element object){
		mObjectsSet.remove(object);
		if(mHasViewGroup){
			mbeltGroup.removeElement(object);	
			double [] pos = object.toSpaceFrame(new double[]{0,0,0});
			object.setPosition(new double[]{pos[1], pos[0], pos[2]});
			//System.out.println("posFinal: " + pos[0] + " " +  pos[1] + " " + pos[2] + " " );
		}
	}
	
	private void moveObjects() {
		double delta = mLinealDistance / 10; // AJUSTAR // 10 times per second
		for (Element objects : mObjectsSet) {
			if (objects.getX() <= mLength / 2.)
				objects.setX(objects.getX() + delta);
			else {
				objects.setX(mLength / 2. + 0.05);
				System.out.println("Alarm: the object falls from the belt");
				if (objects.getZ() > 0)
					objects.setZ(objects.getZ() - 2 * delta);
				else
					objects.setZ(0);
			}
		}
	}

	protected synchronized void playBelt() {
		if (mMotionThread != null)
			return; // animation is running
		mMotionThread = new Thread() {
			public void run() {
				while (mMotionThread == Thread.currentThread()) {
					long currentTime = System.currentTimeMillis();
					moveBelt();
					if (!mObjectsSet.isEmpty())
						moveObjects();
					
					// adjust the sleep time to try and achieve a constant
					// animation rate
					// some VMs will hang if sleep time is less than 10
					long sleepTime = sDELAY
							- (System.currentTimeMillis() - currentTime); // Math.max(10,
																			// delay-(System.currentTimeMillis()-currentTime));
					if (sleepTime < 10)
						Thread.yield();
					else {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException ie) {
						}
					}
				}
			}
		};
		mMotionThread.setPriority(Thread.MIN_PRIORITY);
		mMotionThread.setDaemon(true);
		mMotionThread.start(); // start the animation
	}

	protected synchronized void pauseBelt() {
		if (mMotionThread == null)
			return; // animation thread is already dead
		Thread tempThread = mMotionThread; // local reference
		mMotionThread = null; // signal the animation to stop
		if (Thread.currentThread() == tempThread)
			return; // cannot join with own thread so return
		// another thread has called this method in order to stop the animation
		// thread
		try { // guard against an exception in applet mode
			tempThread.interrupt(); // get out of a sleep state
			tempThread.join(100); // wait up to 1 second for animation thread to
									// stop
		} catch (Exception e) {
			// System.out.println("excetpion in stop animation"+e);
		}
	}

}
