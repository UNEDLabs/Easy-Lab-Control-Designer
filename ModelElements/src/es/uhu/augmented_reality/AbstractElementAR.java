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

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.nyatla.nyar4psg.MultiMarker;
import jp.nyatla.nyar4psg.NyAR4PsgConfig;

import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.utils.Camera;
import org.opensourcephysics.drawing3d.utils.ImplementationChangeListener;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * <p> Title: AbstractElementAR </p>
 * <p>Description: A base class that allows 3D tracking of virtual objects based on ARToolKit markers. 
 * Any object in an OSP/EJS DrawingPanel3D can be referenced with a fiducial marker. 
 * With AbstractElementAR you can build augmented reality (AR) applications and simulations with OSP/EJS</p>
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version 1.0 September 2012
 * @version 2.0 December 2012
 */
public abstract class AbstractElementAR {

  // -------------------------------------------------
  // Static part (see static utilities at the bottom)
  // -------------------------------------------------

  static private final String PATTERN_PATH; // The path to the .patt pattern files
  static private final String DEFAULT_CAMERA_PARAMETERS; // Default camera calibration file
  static private final int DEFAULT_FPS = 10; // Default frame rate calculating markers positions
  static private final int MINIMUM_X = 100; // Default min X in DrawingPanel3D
  static private final int MAXIMUM_X = 10000; // Default max X in DrawingPanel3D
  static private final double FACTOR_640x480_SIMPLE3D = 41.53;
  static private final double FACTOR_640x480_JAVA3D = 28.88;
  static private final double FACTOR_800x600_SIMPLE3D = 33.23;
  static private final double FACTOR_800x600_JAVA3D = 19.78;
  static private final double FACTOR_1280x720_SIMPLE3D = 27.48;
  static private final double FACTOR_1280x720_JAVA3D = 13.70;
  
    
  static {
    String packageName = AbstractElementAR.class.getPackage().getName().replace('.', '/'); // Automatic detection of package path
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

  // Configuration variables
  private int mCameraXresolution=640;
  private int mCameraYresolution=480;
  private String mConfigurationFile = DEFAULT_CAMERA_PARAMETERS;
  private DrawingPanel3D mPanel3D=null;
  private ChangeListener mListener=null;
  private int mThreshold = MultiMarker.THLESHOLD_AUTO;
  private double mConfidence = Double.NaN;
  private int mLostDelay=10;
  private int mFPS = DEFAULT_FPS;
  
  // Implementation variables
  private PApplet pApplet;
  private ArrayList<Marker> mMarkerList = new ArrayList<Marker>();
  private boolean mIsRecording = false;
  private MultiMarker mMultimarker; // AR object supporting multiple markers
  private PMatrix3D[] mMatrixList;
  private double[] mUserCameraParams = new double[4];
  private ChangeEvent mChangeEvent = new ChangeEvent(this); 
  
  // ---------------------------------------------
  // Abstract part
  // ---------------------------------------------

  /**
   * Starts the capturing device
   */
  abstract protected void startCapture(PApplet applet);

  /**
   * Stops the capturing device
   */
  abstract protected void stopCapture();
  
  /**
   * Reads one image and sends it to the panel3D
   * @return true if successfull, false otherwise
   */
  abstract protected PImage readImage();

  // ---------------------------------------------
  // Configuration of the camera
  // ---------------------------------------------

  /**
   * Sets a 3D drawing panel to display the video as background. Changing it while resets the connection.
   * @param panel3D
   */
  public void setDrawingPanel3D(DrawingPanel3D panel3D) { 
    if (mPanel3D!=panel3D) {
      boolean wasRunning = mIsRecording;
      stop();
      DrawingPanel3D oldPanel = mPanel3D;
      mPanel3D = panel3D;
      if (oldPanel!=null && oldPanel.canRender()) { // Clear background of previous panel
        oldPanel.getVisualizationHints().setBackgroundImage((Image)null);
        oldPanel.update();
      }
      if (mPanel3D==null) return; 
      mPanel3D.addImplementationChangeListener(new ImplementationChangeListener() {
        public void implementationChanged(int toImplementation) {
          setPanel();
        }
      });
      if (mPanel3D.canRender()) {
        setPanel();
        if (wasRunning) connect();
      }
    }
  }
  
  public DrawingPanel3D getPanel() {
    return mPanel3D;
  }

  /**
   * Sets a listener that will be reported whenever the image changes
   * @param panel3D
   */
  public void setChangeListener(ChangeListener listener) { 
    mListener = listener;
  }

  /**
   * Enables/Disables the video. Changing it while recording resets the connection.
   * @param enabled
   */
  public void setFPS(int fps) {
    fps = Math.max(1, fps);
    if (fps!=mFPS) {
      boolean wasRunning = mIsRecording;
      stop();
      mFPS = fps;
      if (wasRunning) connect();
    }
  }
  
  /**
   * Sets the camera X resolution. Changing it while recording resets the connection.
   * @param xRes X resolution of the camera (in pixels)
   */
  public void setResolution(int xRes, int yRes) {
    if (xRes!=mCameraXresolution || yRes!=mCameraYresolution) {
      boolean wasRunning = mIsRecording;
      stop();
      mCameraXresolution = xRes;
      mCameraYresolution = yRes;
      if (mPanel3D!=null && mPanel3D.canRender()) {
        setPanel();
        if (wasRunning) connect();
      }
    }
  }
  
  public int getResolutionX() { return mCameraXresolution; }
  
  public int getResolutionY() { return mCameraYresolution; }

  /**
   * Sets the camera configuration file. Changing it while recording resets the connection.
   * @param filename The camera calibration filename, null for default parameters
   * @return true if the file can be found, falase otherwise
   */
  public boolean setConfigurationFile(String filename) {
    if (filename==null || filename.trim().isEmpty()) filename = DEFAULT_CAMERA_PARAMETERS;
    if (filename!=mConfigurationFile) {
      boolean wasRunning = mIsRecording;
      stop();
      mConfigurationFile = filename;
      if (!fileExists(mConfigurationFile)) { 
        errorMessage("The camera calibration file is not available");
        mConfigurationFile = DEFAULT_CAMERA_PARAMETERS;
        return false;
      }
      setPanel();
      if (wasRunning) connect();
    }
    return true;
  }

  /**
   * Enables/Disables the video
   * @param enabled
   */
  public void setEnabled(boolean enabled) {
    if (enabled) connect();
    else stop();
  }
  
  /**
   * Whether the camera is recording
   * @return
   */
  public boolean isRecording() { return mIsRecording; }
  
  // ---------------------------------------------
  // Handling markers
  // ---------------------------------------------

  /**
   * Removes all markers. Doing so while recording resets the connection.
   */
  public void removeAllMarkers() {
    if (mMarkerList.isEmpty()) return;
    boolean wasRunning = mIsRecording;
    stop();
    mMarkerList.clear();
    if (wasRunning) connect();
  }
  
  /**
   * Add a marker from a file. Markers are later referenced by the integer number returned by this method
   * @param filename The filename for the user-provided pattern
   * @param width Width of the pattern (in milimeters), including its black square border
   * @return int the integer for further reference of the marker, -1 if failed to add the marker
   */
  public int addMarker(String filename, int width) {
    if (width<=0) {
      warningMessage("ElementAR warning: Invalid size for marker: " + filename+ " : "+width);
      return -1;
    }
    // Avoid repetitions
    int index = getMarkerReference(filename);
    if (index>=0) { // set the width, in case it is different
      Marker marker = mMarkerList.get(index);
      marker.width = width;
      return index;
    }
    // Locate the file
    if (!fileExists(filename)) {
      warningMessage("ElementAR warning: Marker file not found: " + filename);
      return -1;
    }
    Marker marker = new Marker(filename,width);
    mMarkerList.add(marker);
    if (mMultimarker!=null) { // In case you add the marker while recording
      mMultimarker.addARMarker(extractFile(marker.filename),marker.width);
      mMatrixList = (PMatrix3D[]) PApplet.append(mMatrixList, new PMatrix3D());
    }
//    System.out.println("Marker added"+marker.filename+" w = "+marker.width);
    return mMarkerList.indexOf(marker);
  }

  /**
   * Returns the reference index of a previously added marker
   * @param filename the filename of the marker
   * @return the reference index of a previously added marker
   */
  public int getMarkerReference(String filename) {
    for (int i = 0, nMarkers = mMarkerList.size(); i<nMarkers; i++) {
      Marker marker = mMarkerList.get(i);
      if (marker.filename.equals(filename)) return i;
    }
    return -1;
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
  public void setThreshold(int threshold) {
    mThreshold = threshold;
    if (mMultimarker!=null) mMultimarker.setThreshold(mThreshold);
  }

  /**
   * Set the default threshold value
   */
  public void setThreshold() {
    setThreshold(MultiMarker.THLESHOLD_AUTO);
  }

  /**
   * Sets the confidence value, i.e. the probablity with which to decide the validity of a marker
   * @param confidence A value between 0 and 1
   */
  public void setConfidence(double confidence) {
    mConfidence = confidence;
    if (mMultimarker!=null) mMultimarker.setConfidenceThreshold(confidence);
  }

  /**
   * Sets the delay to indicate the disappearance of the marker. 
   * @param delay Delay in tenths of a second (greater than 0)
   */
  public void setLostDelay(int delay) {
    if (delay<1) {
      warningMessage("setLostDelay error: The value must be > 0. LostDelay unchanged.");
      return;
    }
    mLostDelay = delay; 
    if (mMultimarker!=null) mMultimarker.setLostDelay(delay);
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
    if (reference<0 || reference>=mMarkerList.size()) return false;
    if (!mIsRecording) return false;
    return mMultimarker.isExistMarker(reference);
  }

  /**
   * Returns the position in space of a marker
   * @param reference The integer reference of the marker
   * @param position the double[3] array with the position
   * @return
   */
  public double[] getMarkerPosition(int reference, double[] position) {
    if (!isMarkerDetected(reference)) return position;
    PMatrix3D pMatrix  = mMatrixList[reference];
    position[0] = -pMatrix.m23;
    position[1] = -pMatrix.m03;
    position[2] = -pMatrix.m13;
    return position;
  }
  
  /**
   * Returns the 3D orientation of a marker
   * @param reference The integer reference of the marker
   * @return the 3x3 matrix, null if the marker is invalid or not detected 
   */
  public double[][] getMarkerOrientation(int reference, double[][] orientation) {
    if (!isMarkerDetected(reference)) return orientation;
    PMatrix3D pMatrix = mMatrixList[reference];
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
   * @param reference The integer reference of the marker
   * @return int[] and integer array with the x and y screen coordinates, null if marker not detected
   */
  public int[] getMarkerXY(int reference){
    if (!isMarkerDetected(reference)) return null;
    PVector coords = mMultimarker.marker2ScreenCoordSystem(reference, 0.0, 0.0, 0.0);
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
    return PApplet.dist(pFirstMatrix.m03,  pFirstMatrix.m13,  pFirstMatrix.m23, 
                        pSecondMatrix.m03, pSecondMatrix.m13, pSecondMatrix.m23); // Paco comment: is this just the standard distance? Can we then compute it as a double? ***YES***
  }

  /**
   * Returns the distance from the camera to one marker
   * @param reference The integer reference of the marker
   * @return the distance in millimeters
   */
  public double getDistanceFromCamera(int reference) {
    if (!isMarkerDetected(reference)) return Double.NaN;
    PMatrix3D pMatrix  = mMatrixList[reference];
    return Math.sqrt(pMatrix.m03*pMatrix.m03 + pMatrix.m13*pMatrix.m13 + pMatrix.m23*pMatrix.m23);
  }

  /**
   * Returns the confidence value of a marker 
   * @param reference The integer reference of the marker
   * @return the confidence value, Double.NaN if the marker is invalid or not detected
   */
  public double getConfidence(int reference) {
    if (!isMarkerDetected(reference)) return Double.NaN;
    return mMultimarker.getConfidence(reference);
  }

  /**
   * Returns the life value of a marker.
   * Life value is incremented each time the marker is recognized
   * @param reference The integer reference of the marker
   * @return long life value, -1 if the marker is invalid or not detected 
   */
  public long getLife(int reference) {
    if (!isMarkerDetected(reference)) return -1;
    return mMultimarker.getLife(reference);
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
    return new double[][] {
        { (double) pMatrix.m00, (double) pMatrix.m01, (double) pMatrix.m02, (double) pMatrix.m03 },
        { (double) pMatrix.m10, (double) pMatrix.m11, (double) pMatrix.m12, (double) pMatrix.m13 },
        { (double) pMatrix.m20, (double) pMatrix.m21, (double) pMatrix.m22, (double) pMatrix.m23 },
        { (double) pMatrix.m30, (double) pMatrix.m31, (double) pMatrix.m32, (double) pMatrix.m33 }
    };
  }
  
  // ---------------------------------------------
  // Operation
  // ---------------------------------------------

  /**
   * Connect to the camera
   */
  public void connect() {
    if (mIsRecording) return;
    if (pApplet!=null) pApplet.destroy();
    pApplet = new MyPApplet();
//    setPanel();
    NyAR4PsgConfig config = new NyAR4PsgConfig(NyAR4PsgConfig.CS_RIGHT_HAND, NyAR4PsgConfig.TM_NYARTK);
    mMultimarker = new MultiMarker(pApplet, mCameraXresolution, mCameraYresolution, extractFile(mConfigurationFile), config);
    mMultimarker.setThreshold(mThreshold);
    if (!Double.isNaN(mConfidence)) mMultimarker.setConfidenceThreshold(mConfidence);
    mMultimarker.setLostDelay(mLostDelay);
    mMatrixList = new PMatrix3D[] {}; // Clear the matrix list

    for (int i = 0, nMarkers = mMarkerList.size(); i<nMarkers; i++) {
      Marker marker = mMarkerList.get(i);
      mMultimarker.addARMarker(extractFile(marker.filename),marker.width);
      mMatrixList = (PMatrix3D[]) PApplet.append(mMatrixList, new PMatrix3D());
    }
    mIsRecording = true;
    pApplet.init();
  }
  
  public void stop() {
    if (!mIsRecording) return;
    mIsRecording = false;
    pApplet.stop();
    stopCapture();
  }
  
  /**
   * Closes the connection to the camera
   */
  public void dispose(){
    stop();
    if (pApplet!=null) pApplet.destroy();
  }
  
  // ---------------------------------------------
  // Private utilities
  // ---------------------------------------------

  private void setPanel() {
    if (mPanel3D!=null && mPanel3D.canRender()) {
      mUserCameraParams = getParameters(mPanel3D,mConfigurationFile,mCameraXresolution,mCameraYresolution);
      mPanel3D.setPreferredMinMax(MINIMUM_X, MAXIMUM_X, -mCameraXresolution/2.0, mCameraXresolution/2.0, -mCameraYresolution/2.0, mCameraYresolution/2.0);
      Camera camera = mPanel3D.getCamera();
      camera.setDistanceToScreen(mUserCameraParams[3]);
      camera.setXYZ(0, 0, 0);
      camera.setFocusXYZ(mUserCameraParams);
    }
  }
  
  private class MyPApplet extends PApplet {
    private static final long serialVersionUID = 1L;

    /**
     * Establishes the connection to the camera.
     */
    public void setup() {
      size(mCameraXresolution,mCameraYresolution,PConstants.P3D);
//      println(MultiMarker.VERSION); // prints the tracking library version used
      colorMode(RGB, 100);
      frameRate(mFPS);
      startCapture(this);
    }
    
    /**
     * Loop to obtain the 3D matrix of all declared markers
     */
    public void draw() {
      if (!mIsRecording) return;
      PImage pImage = readImage();
      if (pImage==null) return;
      //        image(pImage, 0, 0);
      int nMarkers = mMarkerList.size();
      if (nMarkers>0) {
        try {
          mMultimarker.detect(pImage);
          for (int marker = 0; marker < nMarkers; marker++) {
            if ((!mMultimarker.isExistMarker(marker))) continue;
            mMultimarker.beginTransform(marker);
            // gets the 3D matrix (4x4), including rotation, scaling and traslation 
            // and saves it in the matrix list
            mMatrixList[marker]= mMultimarker.getMarkerMatrix(marker);
            mMultimarker.endTransform();
          }
        }
        catch (Exception exc) {
          exc.printStackTrace();
          System.err.println("Error detecting marker from "+AbstractElementAR.this.toString());
        }
      }
      //perspective();
      if (mListener!=null) mListener.stateChanged(mChangeEvent);
    }
    
  } // end of pApplet class


  // ---------------------------------------------
  // Static utilities
  // ---------------------------------------------

  static private class Marker {
    private String filename;
    private int width;
    
    public Marker (String filename, int width) {
      this.filename = filename;
      this.width = width;
    }

  }
  
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
  static private boolean fileExists(String filename)  {
    try {
      Resource res = ResourceLoader.getResource(filename);
//      System.err.println("Resource for "+filename +" = "+res.getAbsolutePath());
      if (res!=null) return true;
    }
    catch (Exception exc) {
      exc.printStackTrace();
    }
    return false;
  }

  static private String extractFile (String filename) {
    try {
      Resource res = ResourceLoader.getResource(filename);
      if (res==null) return filename; // desperate try
      if (res.getFile()!=null && res.getFile().exists()) return res.getAbsolutePath();
      // Extract it from its location to a temp file
      File file = File.createTempFile("ElementAR", "tmp");
      file.deleteOnExit();
      BufferedInputStream in = new BufferedInputStream(res.openInputStream());
      FileOutputStream fout = new FileOutputStream(file);
      int c;
      while ((c = in.read()) != -1) fout.write(c);
      in.close();
      fout.close();
//      System.out.println("ElementAR: resource "+filename+ " extracted to "+file.getAbsolutePath());
      return file.getAbsolutePath();
    }
    catch(Exception exc) {
      System.out.println("ElementAR error: resource "+filename+ " could NOT be extracted");
      exc.printStackTrace();
      return filename;
    }
  }  
  
  /**
   * Returns an array with EJS camera parameters (projection screen, X focus, Y focus and Z focus)
   * @param filename the name of the camera parameters file
   * @return parameters of DrawingPanel3D camera
   */
  static private double[] getParameters(DrawingPanel3D panel3D, String filename, int xRes, int yRes)  {
    double[] paramValues = new double[17];
    double[] ejsCameraParams = new double[4];
    boolean isJava3D = panel3D.getImplementation()==DrawingPanel3D.IMPLEMENTATION_JAVA3D;
    int i=0;
    double projectionScreen;
    double factor = 1.0;
    try {
      Resource res = ResourceLoader.getResource(filename);
      if (res==null) {
        System.err.println("ElementAR error: Configuration file not found = "+filename);
        return new double[] { 713.5228259763805, -3.5, 1.5, 29632.602962799087}; // These are default values for 640x480
      }
      DataInputStream dataInput = new DataInputStream(res.openInputStream());
      try {
        while(true) {         
         paramValues[i] = dataInput.readDouble();
         i++;
        }  
      }
      catch(EOFException eof) {          
//        String line = null;
//        System.err.println("------------------");
//        System.err.println("Camera Parameters:");
//        line = "{"+String.valueOf(paramValues[1])+", "+String.valueOf(paramValues[2])+", "+String.valueOf(paramValues[3])+", "+String.valueOf(paramValues[4])+",";
//        System.err.println(line);
//        line = String.valueOf(paramValues[5])+", "+String.valueOf(paramValues[6])+", "+String.valueOf(paramValues[7])+", "+String.valueOf(paramValues[8])+",";
//        System.err.println(line);
//        line = String.valueOf(paramValues[9])+", "+String.valueOf(paramValues[10])+", "+String.valueOf(paramValues[11])+", "+String.valueOf(paramValues[12])+"}";
//        System.err.println(line);
//        line = "Distortion Factor: {"+String.valueOf(paramValues[13])+", "+String.valueOf(paramValues[14])+", "+String.valueOf(paramValues[15])+", "+String.valueOf(paramValues[16])+"}";
//        System.err.println(line);
//        System.err.println("------------------");
      }
      finally { dataInput.close(); }
      // projection screen of ejs camera
      switch (xRes) {
        default:
        case 640:   if (yRes == 480) factor = isJava3D ? FACTOR_640x480_JAVA3D : FACTOR_640x480_SIMPLE3D; break;
        case 800:   if (yRes == 600) factor = isJava3D ? FACTOR_800x600_JAVA3D : FACTOR_800x600_SIMPLE3D; break;
        case 1280:  if (yRes == 720) factor = isJava3D ? FACTOR_1280x720_JAVA3D : FACTOR_1280x720_SIMPLE3D; break;
      }
      projectionScreen = ((paramValues[1] + paramValues[6])/2.0)*factor;
      ejsCameraParams[3] = projectionScreen;
      ejsCameraParams[0] = ((paramValues[1] + paramValues[6])/2.0); // X focus 
      ejsCameraParams[1] = paramValues[3]-(xRes/2); // Y Focus
      ejsCameraParams[2] = paramValues[7]-(yRes/2); // Z Focus
//      for (int j=0; j<ejsCameraParams.length; j++) {
//        System.err.println("Param ["+j+"] = "+ejsCameraParams[j]);
//      }
      return ejsCameraParams;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return null;
    }
  } 
  
//  static public void main(String args[]) {
//    Package pack = ElementAR.class.getPackage();
//    System.out.println ("Pack = "+pack.getName().replace('.', '/'));
//    //    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "es.uhu.AR.ARSystem" });
//  }

}




