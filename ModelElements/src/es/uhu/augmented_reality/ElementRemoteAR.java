/*
 * Copyright (C) 2012 Francisco Esquembre / Marco A. Marquez / Andres Mejias  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package es.uhu.augmented_reality;

import ipcapture.IPCapture;

import org.opensourcephysics.drawing3d.DrawingPanel3D;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * <p> Title: ElementRemoteAR </p>
 * <p>Description: A subclass of AbstractElementAR that connects to a remote (Web) Camera. 
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version 1.0 September 2012
 * @version 2.0 December 2012
 */
public class ElementRemoteAR extends AbstractElementAR {

//  static private Hashtable<String,ElementRemoteAR> sClientTable = new Hashtable<String,ElementRemoteAR>();
//
//  static public ElementRemoteAR createConnection(String url) {
//    ElementRemoteAR client = sClientTable.get(url);
//    if (client==null) {
//      client = new ElementRemoteAR();
//      client.setURL(url);
//      sClientTable.put(url, client);
//    }
//    return client;
//  }

  // ---------------------------------------------
  // Instance variables
  // ---------------------------------------------

  // Configuration variables
  
  private String mCameraURL=null;
  private String mCameraUsername = null;
  private String mCameraPassword = null;
  
  // Implementation variables
  private IPCapture mCameraURLCapture;   
  
  // ---------------------------------------------
  // Configuration of the camera
  // ---------------------------------------------

	public ElementRemoteAR() {}

	public ElementRemoteAR(DrawingPanel3D panel3D, String url) {
		this();
		setURL(url);
		setDrawingPanel3D(panel3D);
	}
	
  /**
   * Sets the camera URL. Changing it while recording resets the connection.
   * @param url The URL to access the camera stream or "local:#" where # is the camera source name for GSCapture (see codeanticode.gsvideo.GSCapture).
   * In 64 bits MacOSX, #=0 for the built-in camera, #=1 for the next USB connected camera, and so on. 
   * Local cameras require that you provide access to the gsstreamer folder
   */
  public void setURL(String url) {
//    System.err.println("Setting url to "+url);
    if (url==null || url.trim().length()==0) {
      stop();
      mCameraURL = null;
      return;
    }
    if (!url.equals(mCameraURL)) {
      boolean wasRunning = isRecording();
      stop();
      mCameraURL = url;
      if (wasRunning) connect();
    }
  }
  
  /**
   * Sets the user required to access a protected camera 
   * @param username Username (if required to access the camera, null if not needed)
   */
  public void setUsername(String username) {
    boolean changed;
    if (username==null) changed = (mCameraUsername!=null);
    else changed = !username.equals(mCameraUsername);
    if (changed) {
      boolean wasRunning = isRecording();
      stop();
      mCameraUsername = username;
      if (wasRunning) connect();
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
      if (wasRunning) connect();
    }
  }

  public String toString() {
    return "Remote camera at "+this.mCameraURL;
  }

  // ---------------------------------------------
  // Operation
  // ---------------------------------------------

  /**
   * Starts the capturing device
   */
  protected void startCapture(PApplet applet) {
    System.err.println("Starting capture on "+mCameraURL);
    mCameraURLCapture = new IPCapture(applet, mCameraURL, mCameraUsername, mCameraPassword);
    mCameraURLCapture.start();
  }

  /**
   * Restarts the capturing device
   */
  public void camInit() {
    if (mCameraURLCapture!=null) mCameraURLCapture.start(); // Andres: Added to restart camera after restart SARLAB
  }
  
  /**
   * Stops the capturing device
   */
  protected void stopCapture() {
    if (mCameraURLCapture!=null) mCameraURLCapture.stop(); // Paco: I added this a bit blindly
  }
  
  /**
   * Reads one image and sends it to the panel3D
   * @return true if successfull, false otherwise
   */
  protected PImage readImage() {
    if (mCameraURLCapture!=null) {
      if (!mCameraURLCapture.isAvailable()) return null;
      mCameraURLCapture.read();
      DrawingPanel3D panel3D = getPanel();
      if (panel3D!=null && panel3D.canRender()) panel3D.getVisualizationHints().setBackgroundImage(mCameraURLCapture.getImage());
      return mCameraURLCapture;
    }
    return null;
    // The user should not have changed these settings of the panel and its camera
    //      mPanel3D.setPreferredMinMax(MINIMUM_X, MAXIMUM_X, -mCameraXresolution/2.0, mCameraXresolution/2.0, -mCameraYresolution/2.0, mCameraYresolution/2.0);
    //      Camera camera = mPanel3D.getCamera();
    //      camera.setDistanceToScreen(mUserCameraParams[3]);
    //      camera.setXYZ(0, 0, 0);
    //      camera.setFocusXYZ(mUserCameraParams);
  }

}




