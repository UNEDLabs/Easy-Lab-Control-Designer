package org.colos.ejs.library.utils;

import org.colos.ejs.library.utils.EditedIPCapture;
import java.awt.Image;
import java.util.ArrayList;
import processing.core.*;
import org.colos.ejs.library.control.drawing2d.ControlBasicWebCam2D;
import org.colos.ejs.library.utils.WebCamElement;


/**
 * WebVideo obtains an image from a web cam and passes it to a listener
 * Adapted from ElementRemoteAR code by Jacobo Sáenz, UNED - 2014
 * @author Francisco Esquembre May 2012
 */
public class WebCamElement{
  
	  private boolean mEnabled = false;
	  private String mURL = null;
	  private ArrayList<WebVideoListener> mListeners = new ArrayList<WebVideoListener>();
	  private String mCameraPassword = null;
	  private String mCameraUsername = null;
	  private EditedIPCapture mCameraURLCapture;   
	  private int mCameraXresolution=640;
	  private int mCameraYresolution=480;
	  private int mFPS = 15;
	  private Thread mRunningThread;
	  private PApplet pApplet;
	  private int maxAuto = 15;
	  private int minAuto = 15;
	  private boolean mIsRecording = false;
	  private boolean mAutoFPS = false;

  
	public WebCamElement(ControlBasicWebCam2D controlParent) {
		this();
	}

	public WebCamElement() {}
	
// -----------------------------------
// Configuration
// -----------------------------------
	
/**
 * Sets the camera URL. Changing it while recording resets the connection.
 */
	public void setURL(String url) {
		System.err.println("Setting url to "+url);
		if (url==null || url.trim().length()==0) {
			stop();
			mURL = null;
			return;
		}
		if (!url.equals(mURL)) {
			boolean wasRunning = isRecording();
			stop();
			mURL = url;
			if (wasRunning){
				connect();
			}
		}
	}

	public void setListener(WebVideoListener listener) { 
		System.err.println("Zona Depuracion: ChangeListener");
		if (!mListeners.contains(listener)) mListeners.add(listener);	
	}

	public boolean addListener(WebVideoListener listener) {
		if (!mListeners.contains(listener)) return mListeners.add(listener);
		return false;
	}

/**
 * Sets the user required to access a protected camera 
 * @param username Username (if required to access tFe camera, null if not needed)
 */
	public void setUsername(String username) {
		boolean changed;
		if (username==null) changed = (mCameraUsername!=null);
		else changed = !username.equals(mCameraUsername);
		if (changed) {
			boolean wasRunning = isRecording();
			stop();
			mCameraUsername = username;
			if (wasRunning){connect();}
		}
	}

/**
 * Sets the required to access a protected camera 
 * @param password (if required to access the camera, null if not needed)
 */

	public void setPassword(String password) {
		boolean changed;
		if (password==null) changed = (mCameraPassword!=null);
		else changed = !password.equals(mCameraPassword);
		if (changed) {
			boolean wasRunning = isRecording();
			stop();
			mCameraPassword = password;
			if (wasRunning){connect();}
		}
	}

/**
 * Activate or disable the FPS auto-tune  
 * @param autoFps (Boolean true to activate)
 */
	
	public void setAutoFPS(boolean autoFps){
		mAutoFPS = autoFps;
	}
	
/**
 * Set the maximum value for FPS in auto-tune mode
 * @param max (Maximum FPS)
 */	
	
	public void setMaxAutoFPS(int max){maxAuto = max;}

/**
 * Set the minimum value for FPS in auto-tune mode
 * @param min (Minimum FPS)
 */
	public void setMinAutoFPS(int min){minAuto = min;}
	
	public String toString() {
		return "Remote camera at "+this.mURL;
	}

	public int getResolutionX() { return mCameraXresolution; }
  
	public int getResolutionY() { return mCameraYresolution; }

// ---------------------------------------------
// Operation
// ---------------------------------------------

	/**
	 * Starts the capturing device
	 */
	protected void startCapture(PApplet applet) {
		System.err.println("Starting capture on WebCamElement -> "+mURL);
		mCameraURLCapture = new EditedIPCapture(applet, mURL, mCameraUsername, mCameraPassword);
		mCameraURLCapture.start();
	    if (!mEnabled) return;
	    if (mURL==null) return;
	    if (mRunningThread!=null) return; // thread is already running
	    mRunningThread = new Thread(new Runnable() {
	      public void run() { readImage(); }
	    });
	    mRunningThread.setPriority(Thread.MIN_PRIORITY);
	    mRunningThread.setDaemon(true);
	    mRunningThread.start();
	}

	/**
	 * Restarts the capturing device
	 */
	public void camInit() {
		if (mCameraURLCapture!=null){
			mCameraURLCapture.start(); // Andres: Added to restart camera after restart SARLAB
		}
	}

	/**
	 * Stops the capturing device
	 */
	protected void stopCapture() {
		if (mCameraURLCapture!=null) mCameraURLCapture.stop(); // Paco: I added this a bit blindly
	}

	/**
	 * Reads one image and sends it to ControlBasicWebCam
	 * @return IPCapture Object if successfull, null otherwise
	 */
	protected PImage readImage() {
		if (mEnabled){
			if (mCameraURLCapture!=null) {
				if (!mCameraURLCapture.isAvailable()){
					return null;
				}else{
					mCameraURLCapture.read();
					Image bi = null;
					bi = mCameraURLCapture.getImage();
					bi.flush();
					sendImage((Image) bi);
					return mCameraURLCapture;
				}
			}else   return null;
		}else	  return null;
	}
	
	public void connect() {
		//if (mEnabled){
			if (mIsRecording){return;}
			if (pApplet!=null){	pApplet.destroy();}
			pApplet = new MyPApplet();
			mIsRecording = true;
			pApplet.init();
		//}
	}
  
	
	public void stop() {
		if (!mIsRecording) return;
		mIsRecording = false;
		//sendImage(null);
		if (pApplet!=null)	pApplet.stop();
		stopCapture();
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		if (enabled){
			connect();
		}else{
			stop();
		}
	}
  
	public void dispose(){
		stop();
		if (pApplet!=null) pApplet.destroy();
	}

	void sendImage(Image image) {
		image.flush();
		for (WebVideoListener listener : mListeners) listener.imageChanged(image);	   
	}
  
  
	public void setFPS(int fps) {
		if (mFPS != fps)  mFPS = fps;
		System.err.println("FPS = " + mFPS + " means a Delay of : " + 1000/mFPS + "miliSecs");
	}

	public void setDelay(int Delay) {
		if (Delay != 0){
			if (mFPS != ((int) (1000/Delay)))	 mFPS = (int) (1000/Delay);
			System.err.println("Delay = " + Delay + " means FPS = " + ( (int) 1000/Delay));
		}
	}
  
	public void setResolution(int xRes, int yRes) {
		if (xRes!=mCameraXresolution || yRes!=mCameraYresolution) {
			stop();
			mCameraXresolution = xRes;
			mCameraYresolution = yRes;
		}
	}

	private class MyPApplet extends PApplet {
		private static final long serialVersionUID = 2L;
		private boolean mFirstReceived = false;
		private int mLostCount = 0;
		private int mCatchCount = 1;
		/**
		 * Establishes the connection to the camera.
		 */
		public void setup() {
			size(mCameraXresolution,mCameraYresolution,"processing.core.PGraphicsJava2D");
			colorMode(RGB, 100);
			frameRate(mFPS);      
			startCapture(this);
		}
 
		public void draw() {
			if (!mIsRecording){return;}
			PImage pImage = null;
			pImage = readImage();
			// Changing parameter FPS if mAutoChange = true
			if (pImage==null && mFirstReceived){
				if (mAutoFPS){
					if ((mLostCount+mCatchCount) == 100){
						if (mLostCount*1.0/((mLostCount+mCatchCount)*1.0) > 0.5){
							System.err.println("Lost 50% of the images: Decreasing FPS in 1%");
							if ((mFPS*1.0 - mFPS/100.0)< minAuto)	setFPS(minAuto);
							else	setFPS((int) (mFPS*1.0 - mFPS/100.0));
						}else if (mLostCount*1.0/((mLostCount+mCatchCount)*1.0) < 0.01){
							System.err.println("Catching 100% of the images: Increasing FPS in 1%");
							if ((mFPS*1.0 + mFPS/100.0)> maxAuto)	 setFPS(maxAuto);
							else	setFPS((int) (mFPS*1.0 + mFPS/100.0));
						}
						mLostCount = 0;
						mCatchCount = 1;
					}
				mLostCount++;
				}
				return;
			}else if (pImage!=null)	{
				mFirstReceived = true;
				mCatchCount++;
			}
		}   
	} // end of pApplet class
	public boolean isRecording() { return mIsRecording; }
//==============================================================

}