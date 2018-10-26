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

import org.opensourcephysics.drawing3d.DrawingPanel3D;

import codeanticode.gsvideo.GSCapture;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * <p> Title: ElementLocalAR </p>
 * <p>Description: A subclass of AbstractElementAR that connects to a local (USB) Camera. 
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version 1.0 September 2012
 * @version 2.0 December 2012
 */
public class ElementLocalAR extends AbstractElementAR {

	public ElementLocalAR() {}

	public ElementLocalAR(DrawingPanel3D panel3D, String cameraPort) {
		this();
		setPort(cameraPort);
		setDrawingPanel3D(panel3D);
	}
	
  // ---------------------------------------------
  // Instance variables
  // ---------------------------------------------

  // Configuration variables
  
  private String mCameraPort;
  
  // Implementation variables
  private GSCapture mCameraLocalCapture;   
  
  // ---------------------------------------------
  // Configuration of the camera
  // ---------------------------------------------

  /**
   * Sets the camera device. Changing it while recording resets the connection.
   * @param port The device to access the camera stream, such as "#" where # is the camera source name for GSCapture (see codeanticode.gsvideo.GSCapture).
   * In 64 bits MacOSX, #=0 for the built-in camera, #=1 for the next USB connected camera, and so on. 
   * Local cameras require that you provide access to the gsstreamer folder next to the simulation jar file
   */
  public void setPort(String port) {
    boolean reconnect=false;
    if (port==null) reconnect = (mCameraPort!=null);
    else reconnect = !port.equals(mCameraPort);
    if (reconnect) {
      boolean wasRunning = isRecording();
      stop();
      mCameraPort = port;
      if (wasRunning) connect();
    }
  }
  
  public String toString() {
    return "Local camera at port "+this.mCameraPort;
  }

  // ---------------------------------------------
  // Operation
  // ---------------------------------------------

  /**
   * Starts the capturing device
   */
  protected void startCapture(PApplet applet) {
    if (mCameraPort==null) 
      mCameraLocalCapture = new GSCapture(applet, getResolutionX(), getResolutionY());
    else 
      mCameraLocalCapture = new GSCapture(applet, getResolutionX(), getResolutionY(),mCameraPort);
    mCameraLocalCapture.start();
  }

  /**
   * Stops the capturing device
   */
  protected void stopCapture() {
    if (mCameraLocalCapture!=null) mCameraLocalCapture.stop();
  }
  
  /**
   * Reads one image and sends it to the panel3D
   * @return true if successfull, false otherwise
   */
  protected PImage readImage() {
    if (mCameraLocalCapture!=null)  {
      if (!mCameraLocalCapture.available()) return null;
      mCameraLocalCapture.read();
      DrawingPanel3D panel3D = getPanel();
      if (panel3D!=null && panel3D.canRender()) {
//      	System.out.println ("Setting image to "+panel3D); 
      	panel3D.getVisualizationHints().setBackgroundImage(mCameraLocalCapture.getImage());
      }
      return mCameraLocalCapture;
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




