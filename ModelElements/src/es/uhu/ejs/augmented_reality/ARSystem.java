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

package es.uhu.ejs.augmented_reality;

import ipcapture.IPCapture;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import jp.nyatla.nyar4psg.MultiMarker;
import jp.nyatla.nyar4psg.NyAR4PsgConfig;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.utils.Camera;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import codeanticode.gsvideo.GSCapture;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * <p> Title: ARSystem </p>
 * <p>Description: A class that allows 3D tracking of virtual objects based on ARToolKit markers. 
 * Any object in an OSP/EJS DrawingPanel3D can be referenced with a fiducial marker. 
 * With ARSystem you can build augmented reality (AR) applications and simulations with OSP/EJS</p>
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version 1.0 September 2012
 */
public class ARSystem extends PApplet {

  // -------------------------------------------------
  // Static part (see static utilities at the bottom)
  // -------------------------------------------------

  static private final String PATTERN_PATH; // The path to the .patt pattern files
  static private final String DEFAULT_CAMERA_PARAMETERS; // Default camera calibration file
  static private final int FPS = 25; // Default frame rate calculating markers positions
  static private final int MINIMUM_X = 100; // Default min X in DrawingPanel3D
  static private final int MAXIMUM_X = 10000; // Default max X in DrawingPanel3D
  static private final double FACTOR_640x480 = 41.53; 
  static private final double FACTOR_800x600 = 33.23;
  static private final double FACTOR_1280x720 = 27.48;
  
  static {
    String packageName = ARSystem.class.getPackage().getName().replace('.', '/'); // Automatic detection of package path
    PATTERN_PATH = packageName + "/data/patterns/";
    DEFAULT_CAMERA_PARAMETERS = packageName + "/data/cameras/default_parameters.dat";
  }
  
  /**
   * Returns the filename of one of the predefined 4x4 numeric markers.
   * Predefined markers have a width of 80 mm.
   * @param reference the reference number (from 1 to 100)
   * @return filename of one of the predefined 4x4 numeric markers
   */
  static public String getPredefinedMarker(int reference) {
    return PATTERN_PATH+"4x4_"+reference+".patt";
  }
  
  /**
   * Returns the filename of one of the predefined keyword markers
   * Predefined markers have a width of 80 mm.
   * @param keyword the keyword, such as "hiro", "ejs", or "4x4_11" 
   * @return filename of one of the predefined keyword markers
   */
  static public String getPredefinedMarker(String keyword) {
    keyword = keyword.trim().toLowerCase();
    if (!keyword.endsWith(".patt")) keyword += ".patt";
    return PATTERN_PATH+keyword;
  }
  
  // ---------------------------------------------
  // Instance variables
  // ---------------------------------------------

  private boolean mIsCameraSet = false;
  private int mCameraXresolution;
  private int mCameraYresolution;
  private String mCameraURL;
  private String mCameraUsername = null;
  private String mCameraPassword = null;
  private ArrayList<String> mMarkerList = new ArrayList<String>();
  private IPCapture mCameraURLCapture;   
  private GSCapture mCameraLocalCapture;   
  private MultiMarker mMultimarker; // AR object supporting multiple markers
  private PMatrix3D[] mMatrixList = {};
  private DrawingPanel3D mPanel3D;
  private double[] mUserCameraParams = new double[4];
  
  
  // ---------------------------------------------
  // Configuration the camera
  // ---------------------------------------------

  /**
   * Sets a 3D drawing panel to display the video as background
   * @param panel3D
   */
  public void setDrawingPanel3D(DrawingPanel3D panel3D) { 
    mPanel3D = panel3D; 
    if (mPanel3D!=null) {
      mPanel3D.setPreferredMinMax(MINIMUM_X, MAXIMUM_X, -mCameraXresolution/2.0, mCameraXresolution/2.0, -mCameraYresolution/2.0, mCameraYresolution/2.0);
      Camera camera = mPanel3D.getCamera();
      camera.setDistanceToScreen(mUserCameraParams[3]);
      camera.setXYZ(0, 0, 0);
      camera.setFocusXYZ(mUserCameraParams);
    }
  }
  
  /**
   * Sets the user and password required ot access a protected camera 
   * @param username Username (if required to access the camera, null if not needed)
   * @param password (if required to access the camera, null if not needed)
   */
  public void setUserAndPassword(String username, String password) {
    mCameraUsername = username;
    mCameraPassword = password;
  }
  
  /**
   * Sets the camera parameters using user provided camera calibration parameters 
   * @param url The URL to access the camera stream or "local:#" where # is the camera source name for GSCapture (see codeanticode.gsvideo.GSCapture).
   * In 64 bits MacOSX, #=0 for the built-in camera, #=1 for the next USB connected camera, and so on. 
   * Local cameras require that you provide access to the gsstreamer folder
   * @param xRes X resolution of the camera (in pixels)
   * @param yRes Y resolution of the camera (in pixels)
   * @param parametersFilename The camera calibration filename 
   * @return true if successful, false otherwise
   */
  public boolean setCamera(String url, int xRes, int yRes, String parametersFilename) {
    mCameraURL = url;
    mCameraXresolution = xRes;
    mCameraYresolution = yRes;
    String params = getFile(parametersFilename);
    if (params==null) { 
      errorMessage("The camera calibration file is not available");
      return false;
    }
    // 
    mUserCameraParams = getParameters(params,mCameraXresolution,mCameraYresolution);
    //
    NyAR4PsgConfig config = new NyAR4PsgConfig(NyAR4PsgConfig.CS_RIGHT_HAND, NyAR4PsgConfig.TM_NYARTK);
    mMultimarker = new MultiMarker(this, mCameraXresolution, mCameraYresolution, params, config);
    mIsCameraSet = true;
    return true;
  }
  
  /**
   * Sets the camera parameters using default camera calibration parameters 
   * @param url The URL to access the camera stream or "local:#" where # is the camera source name for GSCapture (see codeanticode.gsvideo.GSCapture).
   * In 64 bits MacOSX, #=0 for the built-in camera, #=1 for the next USB connected camera, and so on. 
   * Local cameras require that you provide access to the gsstreamer folder
   * @param xRes X resolution of the camera (in pixels)
   * @param yRes Y resolution of the camera (in pixels)
   * @return true if successful, false otherwise
   */
  public boolean setCamera(String url, int xRes, int yRes) {
    return this.setCamera(url, xRes, yRes, DEFAULT_CAMERA_PARAMETERS);
  }
  
  // ---------------------------------------------
  // Operation
  // ---------------------------------------------
  
  /**
   * Connect to the camera
   */
  public boolean connect() {
    init();
//    PApplet.main(new String[] { ARSystem.class.getName() });
    return true;
  }
  
  /**
   * Establishes the connection to the camera
   */
  public void setup() {
    size(mCameraXresolution,mCameraYresolution,PConstants.P3D);
    println(MultiMarker.VERSION); // prints the tracking library version used
    colorMode(RGB, 100);
    frameRate(FPS);
    if (mCameraURL==null) {
      mCameraLocalCapture = new GSCapture(this, mCameraXresolution, mCameraYresolution);
      mCameraLocalCapture.start();
    }
    else if (mCameraURL.startsWith("local:")) {
      mCameraLocalCapture = new GSCapture(this, mCameraXresolution, mCameraYresolution,mCameraURL.substring(6));
      mCameraLocalCapture.start();
    }
    else {
      mCameraURLCapture = new IPCapture(this, mCameraURL, mCameraUsername, mCameraPassword);
      mCameraURLCapture.start();
    }
  }
  
  /**
   * Closes the connection to the camera
   */
  public void dispose(){
    super.stop();
    if (mCameraLocalCapture!=null) mCameraLocalCapture.stop();
    if (mCameraURLCapture!=null) mCameraURLCapture.stop(); // Paco: I added this a bit blindly
    super.destroy();
  }
  
  /**
   * Loop to obtain the 3D matrix of all declared markers
   */
  public void draw() {
    //mMultimarker.setARPerspective();
    java.awt.Image image = null;
    if (mCameraURLCapture!=null) {
      if (!mCameraURLCapture.isAvailable()) return; // Paco: Should we set a boolean flag if the reading failed?
      mCameraURLCapture.read();
      image(mCameraURLCapture, 0, 0);
      mMultimarker.detect(mCameraURLCapture);
      if (mPanel3D!=null) image = mCameraURLCapture.getImage();
    }
    else if (mCameraLocalCapture!=null)  {
      if (!mCameraLocalCapture.available()) return; // Paco: Should we set a boolean flag if the reading failed?
      mCameraLocalCapture.read();
      image(mCameraLocalCapture, 0, 0);
      mMultimarker.detect(mCameraLocalCapture);
      if (mPanel3D!=null) image = mCameraLocalCapture.getImage();
    }
    if (mPanel3D!=null) {
      mPanel3D.getVisualizationHints().setBackgroundImage(image);
      // The user should not have changed these settings of the panel and its camera
//      mPanel3D.setPreferredMinMax(MINIMUM_X, MAXIMUM_X, -mCameraXresolution/2.0, mCameraXresolution/2.0, -mCameraYresolution/2.0, mCameraYresolution/2.0);
//      Camera camera = mPanel3D.getCamera();
//      camera.setDistanceToScreen(mUserCameraParams[3]);
//      camera.setXYZ(0, 0, 0);
//      camera.setFocusXYZ(mUserCameraParams);
    }
    
    for (int marker = 0, nMarkers = mMarkerList.size(); marker < nMarkers; marker++) {
      if ((!mMultimarker.isExistMarker(marker))) continue;
      mMultimarker.beginTransform(marker);
      // gets the 3D matrix (4x4), including rotation, scaling and traslation 
      // and saves it in the matrix list
      mMatrixList[marker]= mMultimarker.getMarkerMatrix(marker);
      mMultimarker.endTransform();
    }
    //perspective();
  }

  // ---------------------------------------------
  // Handling markers
  // ---------------------------------------------

  /**
   * Add a marker from a file. Markers are later referenced by the integer number returned by this method
   * @param filename The filename for the user-provided pattern
   * @param width Width of the pattern (in milimeters), including its black square border
   * @return int the integer for further reference of the marker, -1 if failed to add the marker
   */
  public int addMarker(String filename, int width) {
    if (!mIsCameraSet){
      errorMessage("addMarker error: Camera not set!");
      return -1;
    }
    // Avoid repetitions
    int index = mMarkerList.indexOf(filename);
    if (index>=0) return index;
    // Locate the file
    String singlePatternPath = getFile(filename);
    if (singlePatternPath==null) { 
      warningMessage("Marker not found: " + filename);
      return -1;
    }
    mMultimarker.addARMarker(singlePatternPath, width);
    mMatrixList = (PMatrix3D[]) super.append(mMatrixList, new PMatrix3D()); // Paco: Is this being used correctly?
    mMarkerList.add(filename);
    return mMarkerList.indexOf(filename);
  }

  /**
   * Returns the reference index of a previously added marker
   * @param filename the filename of the marker
   * @return reference index of a previously added marker
   */
  public int getMarkerReference(String filename) {
    return mMarkerList.indexOf(filename);
  }

  // ---------------------------------------------
  // Measuring distances
  // ---------------------------------------------

  /**
   * Check if a marker is detected
   * @param reference The integer reference of the marker
   * @return true if marker is detected, false otherwise
   * see getMarkerReference()
   */ 
  public boolean isMarkerDetected(int reference) {
    if (reference<0) return false;
    if (!mIsCameraSet) return false;
    return mMultimarker.isExistMarker(reference);
  }

  /**
   * Returns the position in space of a marker
   * @param markerReference The integer reference of the marker
   * @param position the double[3] array with the position
   * @return
   */
  public double[] getMarkerPosition(int markerReference, double[] position) {
    if (!isMarkerDetected(markerReference)) return position;
    PMatrix3D pMatrix  = mMatrixList[markerReference];
    position[0] = -pMatrix.m23;
    position[1] = -pMatrix.m03;
    position[2] = -pMatrix.m13;
    return position;
  }
  
  /**
   * Returns the 3D orientation of a marker
   * @param markerReference The integer reference of the marker
   * @return the 3x3 matrix, null if the marker is invalid or not detected 
   */
  public double[][] getMarkerOrientation(int markerReference, double[][] orientation) {
    if (!isMarkerDetected(markerReference)) return orientation;
    PMatrix3D pMatrix = mMatrixList[markerReference];
    orientation[0][0] = -pMatrix.m20;
    orientation[1][0] = -pMatrix.m00;
    orientation[2][0] = -pMatrix.m10;

    orientation[0][1] = -pMatrix.m21;
    orientation[1][1] = -pMatrix.m01;
    orientation[2][1] = -pMatrix.m11;

    orientation[0][2] = -pMatrix.m22;
    orientation[1][2] = -pMatrix.m02;
    orientation[2][2] = -pMatrix.m12;
    return orientation;
  }

  
  /**
   * Returns the position of the center of a marker in the screen coordinate system
   * @param markerReference The integer reference of the marker
   * @return int[] and integer array with the x and y screen coordinates, null if marker not detected
   */
  public int[] getMarkerXY(int markerReference){
    if (!isMarkerDetected(markerReference)) return null;
    PVector coords = mMultimarker.marker2ScreenCoordSystem(markerReference, 0.0, 0.0, 0.0);
    return new int[] { (int) coords.x, (int) coords.y };
  }

  /**
   * Returns the distance between the centers of two markers
   * @param firstMarker  The integer reference of the first marker
   * @param secondMarker The integer reference of the second marker
   * @return double distance in milimeters, Double.NaN if markers invalid or not detected
   */
  public double getDistance(int firstMarker, int secondMarker){
    if (!isMarkerDetected(firstMarker))  return Double.NaN;
    if (!isMarkerDetected(secondMarker)) return Double.NaN;
    PMatrix3D pFirstMatrix  = mMatrixList[firstMarker];
    PMatrix3D pSecondMatrix = mMatrixList[secondMarker];
    return dist(pFirstMatrix.m03,  pFirstMatrix.m13,  pFirstMatrix.m23, 
                pSecondMatrix.m03, pSecondMatrix.m13, pSecondMatrix.m23); // Paco comment: is this just the standard distance? Can we then compute it as a double? ***YES***
  }

  /**
   * Returns the distance from the camera to one marker
   * @param markerReference The integer reference of the marker
   * @return the distance in millimeters
   */
  public double getDistanceFromCamera(int markerReference) {
    if (!isMarkerDetected(markerReference)) return Double.NaN;
    PMatrix3D pMatrix  = mMatrixList[markerReference];
    return Math.sqrt(pMatrix.m03*pMatrix.m03 + pMatrix.m13*pMatrix.m13 + pMatrix.m23*pMatrix.m23);
  }

  // ---------------------------------------------
  // Threshold, confidence, lost delay methods
  // ---------------------------------------------

  /**
   * Set the threshold value of the camera
   * Poor lightning conditions may prevent the virtual image from appearing, 
   * or cause it to  flicker in and out of view, 
   * This problem can often be fixed by changing the lighting threshold
   * value used by the image processing routines.  
   * @param threshold A threshold value from 0 to 255, default is 100. 
   */
  public void setThreshold(int threshold){
    mMultimarker.setThreshold(threshold);
  }

  /**
   * Set the default threshold value
   */
  public void setThreshold() {
    mMultimarker.setThreshold(MultiMarker.THLESHOLD_AUTO);
  }

  /**
   * Returns the current threshold level
   * @return Threshold level (from 0 to 255)
   */
  public int getThreshold() {
    return mMultimarker.getCurrentThreshold();
  }

  /**
   * Sets the confidence value, i.e. the probablity with which to decide the validity of a marker
   * @param confidence A value between 0 and 1
   */
  public void setConfidence(double confidence) {
    mMultimarker.setConfidenceThreshold(confidence);
  }

  /**
   * Returns the confidence value of a marker 
   * @param markerReference The integer reference of the marker
   * @return the confidence value, Double.NaN if the marker is invalid or not detected
   */
  public double getConfidence(int markerReference) {
    if (!isMarkerDetected(markerReference)) return Double.NaN;
    return mMultimarker.getConfidence(markerReference);
  }

  /**
   * Sets the delay to indicate the disappearance of the marker. 
   * @param delay Delay in tenths of a second (greater than 0)
   */
  public void setLostDelay(int delay) {
    if (!mIsCameraSet) {
      errorMessage("setLostDelay error: Camera not set!");
      return;
    }
    if (delay<1) {
      warningMessage("setLostDelay error: The value must be > 0. LostDelay unchanged.");
      return;
    }
    mMultimarker.setLostDelay(delay);
  }

  /**
   * Returns the life value of a marker.
   * Life value is incremented each time the marker is recognized
   * @param markerReference The integer reference of the marker
   * @return long life value, -1 if the marker is invalid or not detected 
   */
  public long getLife(int markerReference) {
    if (!isMarkerDetected(markerReference)) return -1;
    return mMultimarker.getLife(markerReference);
  }

  // ---------------------------------------------
  // Matrix related methods
  // ---------------------------------------------

  /**
   * Returns the 3D matrix of a marker
   * @param markerReference The integer reference of the marker
   * @return the 4x4 matrix, null if the marker is invalid or not detected 
   */
  public double[][] getMatrix(int markerReference) {
    if (!isMarkerDetected(markerReference)) return null;
    PMatrix3D pMatrix = mMatrixList[markerReference];
    return new double[][] {
        { pMatrix.m00, pMatrix.m01, pMatrix.m02, pMatrix.m03 },
        { pMatrix.m10, pMatrix.m11, pMatrix.m12, pMatrix.m13 },
        { pMatrix.m20, pMatrix.m21, pMatrix.m22, pMatrix.m23 },
        { pMatrix.m30, pMatrix.m31, pMatrix.m32, pMatrix.m33 }
    };
  }

  /**
   * Returns the projection matrix
   * @return
   */
  public double[][] getProjectionMatrix() {
    PMatrix3D pMatrix = mMultimarker.getProjectionMatrix();
    //pMatrix.print(); // Paco: Is this needed? ***NO***
    return new double[][] {
        { (double) pMatrix.m00, (double) pMatrix.m01, (double) pMatrix.m02, (double) pMatrix.m03 },
        { (double) pMatrix.m10, (double) pMatrix.m11, (double) pMatrix.m12, (double) pMatrix.m13 },
        { (double) pMatrix.m20, (double) pMatrix.m21, (double) pMatrix.m22, (double) pMatrix.m23 },
        { (double) pMatrix.m30, (double) pMatrix.m31, (double) pMatrix.m32, (double) pMatrix.m33 }
    };
  }


//  /**
//   * Returns the projection screen value for DrawingPanel3D camera
//   * @return projecton screen
//   */
//  public double projectionScreen() {
//    return mUserCameraParams[3];
//  }
//  
//  /**
//   * Returns the X focus value for DrawingPanel3D camera
//   * @return X focus
//   */
//  public double xFocus() {
//    return mUserCameraParams[0];
//  }
//  
//  /**
//   * Returns the Y focus value for DrawingPanel3D camera
//   * @return Y focus
//   */
//  public double yFocus() {
//    return mUserCameraParams[1];
//  }
//  
//  /**
//   * Returns the Z focus value for DrawingPanel3D camera
//   * @return Z focus
//   */
//  public double zFocus() {
//    return mUserCameraParams[2];
//  }
//  
   
//  /**
//   * Returns the X minimum for DrawingPanel3D
//   * @return X min
//   */
//  public int xMin() {
//    return X_MIN; //experimental
//  }
//
//  /**
//   * Returns the X maximum for DrawingPanel3D
//   * @return X max
//   */
//  public int xMax() {
//    return X_MAX; //experimental
//  }
//  
//  /**
//   * Returns the Y minimum for DrawingPanel3D
//   * @return Y min
//   */
//  public int yMin() {
//    return 0-mCameraXresolution/2;
//  }
//
//  /**
//   * Returns the Y maximum for DrawingPanel3D
//   * @return Y max
//   */
//  public int yMax() {
//    return mCameraXresolution/2;
//  }
//  
//  /**
//   * Returns the Z minimum for DrawingPanel3D
//   * @return Z min
//   */
//  public int zMin() {
//    return 0-mCameraYresolution/2;
//  }
//
//  /**
//   * Returns the Z maximum for DrawingPanel3D
//   * @return Z max
//   */
//  public int zMax() {
//    return mCameraYresolution/2;
//  }
 
  // ---------------------------------------------
  // Static utilities
  // ---------------------------------------------

  static private void errorMessage(String message) {
    JOptionPane.showMessageDialog(null, message,"AR System Error",JOptionPane.ERROR_MESSAGE);
  }

  static private void warningMessage(String message) {
    JOptionPane.showMessageDialog(null, message,"AR System Warning",JOptionPane.INFORMATION_MESSAGE);
  }

    
  /**
   * Returns the path of the file desired (which may be inside a JAR)
   * @param filename the name of the file
   * @return String the path of the actual file, null if not found or unable to extract it from the JAR
   */
  static private String getFile(String filename)  {
    try {
      Resource res = ResourceLoader.getResource(filename);
      if (res==null) return null;
      return filename;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Returns an array with EJS camera parameters (projection screen, X focus, Y focus and Z focus)
   * @param filename the name of the camera parameters file
   * @return parameters of DrawingPanel3D camera
   */
  static private double[] getParameters(String filename, int xRes, int yRes)  {
    double[] paramValues = new double[17];
    double[] ejsCameraParams = new double[4];
    int i=0;
    double projectionScreen;
    double factor = 1.0;
    String line = null;
    try {
      Resource res = ResourceLoader.getResource(filename);
      if (res==null) return null;
      //println(res.getAbsolutePath());
      //
      // Extract to a temporary file
      InputStream input = res.openInputStream();
      File targetFile = File.createTempFile("ar_temp", "");
      targetFile.getParentFile().mkdirs();
      OutputStream output = new FileOutputStream(targetFile);
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = input.read(buf)) > 0) output.write(buf, 0, len);
      input.close();
      output.close();
      // Reading data from temporary file
      File f = new File(targetFile.getAbsolutePath());              
      DataInputStream dataInput = null;
      dataInput = new DataInputStream(new FileInputStream(f));  
      try {
      while(true)
        {         
         paramValues[i] = dataInput.readDouble();
         i++;
         //System.out.println(paramValues[i]);             
        }  
      }
      catch(EOFException eof)
      {          
        println("------------------");
        println("Camera Parameters:");
        line = "{"+String.valueOf(paramValues[1])+", "+String.valueOf(paramValues[2])+", "+String.valueOf(paramValues[3])+", "+String.valueOf(paramValues[4])+",";
        println(line);
        line = String.valueOf(paramValues[5])+", "+String.valueOf(paramValues[6])+", "+String.valueOf(paramValues[7])+", "+String.valueOf(paramValues[8])+",";
        println(line);
        line = String.valueOf(paramValues[9])+", "+String.valueOf(paramValues[10])+", "+String.valueOf(paramValues[11])+", "+String.valueOf(paramValues[12])+"}";
        println(line);
        line = "Distortion Factor: {"+String.valueOf(paramValues[13])+", "+String.valueOf(paramValues[14])+", "+String.valueOf(paramValues[15])+", "+String.valueOf(paramValues[16])+"}";
        println(line);
        println("------------------");
      }
      finally
      {
          dataInput.close();         
      }
      //System.out.println ("Temporary file "+filename+" extracted to "+targetFile.getAbsolutePath());
      //
      // EJS camera parameters (projection screen, X focus, Y focus and Z focus)
      //
      // projection screen of ejs camera
      switch (xRes) {
        case 640:   if (yRes == 480) factor = FACTOR_640x480;
                    break;
        case 800:   if (yRes == 600) factor = FACTOR_800x600;
                    break;
        case 1280:  if (yRes == 720) factor = FACTOR_1280x720;
                    break;
      }
      projectionScreen = ((paramValues[1] + paramValues[6])/2.0)*factor;
      ejsCameraParams[3] = projectionScreen;
      //
      // X focus 
      ejsCameraParams[0] = ((paramValues[1] + paramValues[6])/2.0);
      //
      // Y Focus
      ejsCameraParams[1] = paramValues[3]-(xRes/2);
      //
      // Y Focus
      ejsCameraParams[2] = paramValues[7]-(yRes/2);
      //return targetFile.getAbsolutePath();
      return ejsCameraParams;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return null;
    }
  } 
  
  static public void main(String args[]) {
    Package pack = ARSystem.class.getPackage();
    System.out.println ("Pack = "+pack.getName().replace('.', '/'));
    //    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "es.uhu.AR.ARSystem" });
  }

}




