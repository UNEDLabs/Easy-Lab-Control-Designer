/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.drawing3d.utils.*;
import org.opensourcephysics.drawing3d.interaction.*;
import org.opensourcephysics.ejs.EjsRes;
import org.opensourcephysics.tools.VideoTool;

/**
 *
 * <p>Title: DrawingPanel3D</p>
 *
 * <p>Description: The generic implementation of a DrawingPanel3D.</p>
 *
 * <p>Interaction: The panel has only one target, the panel itself.
 * If enabled, the panel issues MOUSE_ENTER, MOUSE_EXIT,
 * MOUSE_MOVED, and MOUSE_DRAGGED InteractionEvents with target=null.
 * When the ALT key is held, the panel also issues MOUSE_PRESSED,
 * MOUSE_DRAGGED (again), and MOUSE_RELEASED InteractionEvents.
 * In this second case, the getInfo() method of the event returns a double[3]
 * with the coordinates of the point selected.</p>
 * <p>Even if the panel is disabled, the panel can be panned, zoomed and (in 3D
 * modes) rotated and the elements in it can be enabled.</p>
 * <p>The interaction capabilities are not XML serialized.</p>
 *
 * <p>Copyright: Open Source Physics project</p>
 * @author Francisco Esquembre
 * @version August 2009
 */
public class DrawingPanel3D implements InteractionSource, org.opensourcephysics.display.Renderable {
  //  static public boolean DELAY_IMPLEMENTATION = false;

  /** Whether Java 3D is installed */
  static private final boolean isJava3DInstalled = OSPRuntime.hasJava3D();
  static private boolean askedToInstallJava3D = false;

  /** A basic, generic implementation of the panel based on the painter's algorithm */
  static public final int IMPLEMENTATION_SIMPLE3D = 0;
  /** An implementation of the panel based on Java 3D*/
  static public final int IMPLEMENTATION_JAVA3D = 1;

  /** The panel itself as the only target of the panel */
  static public final int TARGET_PANEL = 0;

  /** Message box location */
  static public final int BOTTOM_LEFT = 0;
  /** Message box location */
  static public final int BOTTOM_RIGHT = 1;
  /** Message box location */
  static public final int TOP_RIGHT = 2;
  /** Message box location */
  static public final int TOP_LEFT = 3;

  /** No drag allowed with the mouse **/
  static public final int DRAG_NONE = 0;
  /** Any drag allowed with the mouse **/
  static public final int DRAG_ANY = 1;
  /** Drag only to change the azimuth **/
  static public final int DRAG_AZIMUTH = 2;
  /** Drag allowed only to change the altitude **/
  static public final int DRAG_ALTITUDE = 3;


  // Configuration variables
  private boolean squareAspect = true;
  private int implementation=-1;
  private double xmin, xmax, ymin, ymax, zmin, zmax;
  private double axisXSize=Double.NaN, axisYSize=Double.NaN, axisZSize=Double.NaN;
  private VisualizationHints visHints;
  private Camera camera;
  private Decoration decoration;
  private VideoTool vidCap; // The video capture tool for this panel.
  private boolean resetCameraOnChanges=true;

  // Implementation variables
  private ImplementingPanel implementingPanel, previousImplementingPanel;
  private double axisXScale, axisYScale, axisZScale, maximumSize;
  private double[] center = new double[3]; // The center of the 3D scene given by either the extrema or the box size
  private double aconstant, bconstant;
  private int acenter, bcenter; // The center of the swing panel
  private java.util.List<Element> elementList = new ArrayList<Element>(); // The list of elements in this DrawingPanel3D
  private ArrayList<ElementText> textList = new ArrayList<ElementText>(); //List of text elements
  //  protected Rectangle viewRect = null; // the clipping rectangle within a scroll pane viewport

  // Variables for interaction
  private int draggable=DRAG_ANY; 
  private final InteractionTarget myTarget = new InteractionTarget(null, 0);
  private int keyPressed = -1;
  private int lastX = 0, lastY = 0;
  private InteractionTarget targetHit = null, targetEntered = null;
  private double[] trackerPoint = null;
  private java.util.List<InteractionListener> listeners = new ArrayList<InteractionListener>();
  private java.util.List<ImplementationChangeListener> implementationListeners = new ArrayList<ImplementationChangeListener>();

  private Thread firstTimeThread = null;
  private boolean canRender = true;
  private boolean mImmediateImplementation = false;
  private double mEyeDistance;

  //  static { // Check if Java 3D is present
  //    try {
  //      boolean tryIt=true;
  //      String version = System.getProperty("java.version");
  ////      System.err.println ("Version = "+version);
  //      if (version.indexOf("1.7")>=0) { // Danger: release 1.7 is non official on Mac computers!
  //        String home = System.getProperty("java.home");
  ////        System.err.println ("Home = "+home);
  ////        System.err.println ("Checking file = "+home+"/lib/ext/j3dcore.jar");
  //        tryIt = (new java.io.File(home+"/lib/ext/j3dcore.jar")).exists();
  ////        System.err.println ("File exists = "+tryIt);
  //      }
  //      if (tryIt) {
  ////        System.err.println ("Trying to find Java 3D");
  //        Class.forName("com.sun.j3d.utils.universe.SimpleUniverse"); //$NON-NLS-1$
  //        isJava3DInstalled = true;
  //      }
  //      else isJava3DInstalled = false;
  //    }
  //    catch (ClassNotFoundException exc) { // Do not complain
  ////      exc.printStackTrace();
  //      isJava3DInstalled = false; 
  //    }
  //  }

  /**
   * Preferred constructor
   * @param implementation
   */
  public DrawingPanel3D(int implementation) {
    if (implementation==IMPLEMENTATION_JAVA3D) mImmediateImplementation = true;
    mEyeDistance=Double.NaN;
    visHints = new VisualizationHints(this);
    camera = new Camera(this);
    decoration = new Decoration(this);
    setImplementation(implementation); // create implementation dependent panel
    setPreferredMinMax(-1, 1, -1, 1, -1, 1);
    camera.reset();
  }

  /**
   * Constructor for EjsS only
   */
  public DrawingPanel3D() {
    int impl = IMPLEMENTATION_SIMPLE3D;
    //    impl = isJava3DInstalled ? IMPLEMENTATION_JAVA3D : IMPLEMENTATION_SIMPLE3D;
    mEyeDistance=Double.NaN;
    visHints = new VisualizationHints(this);
    camera = new Camera(this);
    decoration = new Decoration(this);
    setImplementation(impl); // create implementation dependent panel
    setPreferredMinMax(-1, 1, -1, 1, -1, 1);
    camera.reset();
  }

  // -----------------------------
  // Setters and getters
  // -----------------------------

  // ********************************** Andres *************************************************************************
  // 
  /**
   * Sets the eye distance for stereo view.
   * @param distance The distance between eyes
   */
  public void setEyeDistance(double distance) {
    mEyeDistance = distance;
    if (implementingPanel!=null) {
      implementingPanel.setEyeDistance(mEyeDistance);
      implementingPanel.forceRefresh();   
    }
  }
  
  /**
   * Changes the implementation of the drawing mechanism.
   * @param _implementation int One of <ul>
   * <li>DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : basic mechanism based on the painter's algorithm
   * <li>DrawingPanel3D.IMPLEMENTATION_JAVA3D : Java3D hardware acceleration
   * </ul>
   * @return true if the desired implementation is supported
   */
  public boolean setImplementation(final int _implementation) {
    if (this.implementation==_implementation) return true;

    if (_implementation==IMPLEMENTATION_JAVA3D && !isJava3DInstalled) {
      if (!askedToInstallJava3D) {
        Object[] options = { EjsRes.getString("DrawingPanel3D.VisitJava3DSite"), EjsRes.getString("Ok")};
        String message = EjsRes.getString("DrawingPanel3D.InstallJava3D");
        int option = JOptionPane.showOptionDialog(null,message,"Easy Java Simulations",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (option==0)  org.opensourcephysics.desktop.OSPDesktop.displayURL("http://java3d.java.net/binary-builds.html"); 
        askedToInstallJava3D = true;
      }
      return false;
    }

    if (mImmediateImplementation) {
      return doSetImplementation(_implementation);
    }
    if (_implementation==IMPLEMENTATION_JAVA3D && !implementingPanel.getComponent().isShowing()) {
      if (firstTimeThread!=null && firstTimeThread.isAlive()) {
//        System.err.println ("Waiting for the panel to be shown");
        return false;
      }
//      System.err.println ("The panel is not yet showing. Delaying it");
      canRender = false;
      firstTimeThread = new Thread(new Runnable() {
        public void run () { 
          while (!implementingPanel.getComponent().isShowing()) {
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
//          System.err.println ("The panel is NOW showing");
          doSetImplementation(_implementation);
          update();
        }
      });
      firstTimeThread.start();
      return true;
    }
    return doSetImplementation(_implementation);
  }

  private boolean doSetImplementation(int _implementation) {
//    System.err.println ("Setting the implementation to "+_implementation);
    firstTimeThread = null;

    implementation = _implementation;
    previousImplementingPanel = implementingPanel;

    // Remove the previous panel of the elements
    for (Element el : elementList) el.removePanel();
    for (Element el : decoration.getElementList()) el.removePanel();

    switch (implementation) {
      default :
      case IMPLEMENTATION_SIMPLE3D : 
        implementingPanel = new org.opensourcephysics.drawing3d.simple3d.SimpleDrawingPanel3D(this);
        break;
      case IMPLEMENTATION_JAVA3D : 
        implementingPanel = new org.opensourcephysics.drawing3d.java3d.Java3dDrawingPanel3D(this);
        break;
    }
    if (!Double.isNaN(mEyeDistance)) {
      implementingPanel.setEyeDistance(mEyeDistance);
      //implementingPanel.forceRefresh(); 
    }

    getComponent().addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent e) { 
        computeConstants(getComponent().getWidth(), getComponent().getHeight());
        for (Element el : elementList) el.addChange(Element.CHANGE_PROJECTION);
        for (Element el : decoration.getElementList()) el.addChange(Element.CHANGE_PROJECTION);
        implementingPanel.forceRefresh();
        getComponent().repaint();
      }
//      public void componentShown(java.awt.event.ComponentEvent e) {
//        System.err.println("Shown with implementation "+getImplementation());
//        canRender = true;
//      }
    });
    IADMouseController mouseController = new IADMouseController();
    getComponent().addMouseListener(mouseController);
    getComponent().addMouseMotionListener(mouseController);
    getComponent().addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent _e)  { keyPressed = _e.getKeyCode(); }
      public void keyReleased(java.awt.event.KeyEvent _e) { keyPressed = -1; }
    });
    getComponent().setFocusable(true);
    getComponent().setPreferredSize(new Dimension(300, 300));

    // Change the implementation of the elements
    for (Element el : elementList) el.setPanel(this);
    for (Element el : decoration.getElementList()) el.setPanel(this);

    cameraChanged(Camera.CHANGE_ANY);
    if(visHints.getBackgroundImage()!=null) implementingPanel.visualizationChanged(VisualizationHints.HINT_BACKGROUND_IMAGE);
    canRender = true;
//    System.err.println ("Call listeners"+_implementation);
    for (ImplementationChangeListener listener : implementationListeners) {
//      System.err.println ("Calling listener"+_implementation+": "+listener);
      listener.implementationChanged(implementation);
    }
    //    if (_implementation==this.IMPLEMENTATION_JAVA3D) return false;
    return true;
  }

  public final int getImplementation() { return this.implementation; } 

  /**
   * Getting the pointer to the real JPanel in it
   * @return Component
   */
  public java.awt.Component getComponent() { return implementingPanel.getComponent(); }

  /**
   * Provides the list of visualization hints that the panel uses
   * to display the 3D scene
   * @return VisualizationHints
   * @see VisualizationHints
   */
  public VisualizationHints getVisualizationHints() { return visHints; }

  /**
   * Provides the Camera object used to project the scene in 3D modes.
   * @return Camera
   * @see Camera
   */
  public Camera getCamera() { return camera; }

  public Decoration getDecoration() { return decoration; }

  public ImplementingPanel getImplementingPanel() { return implementingPanel; } 

  public ImplementingPanel getPreviousImplementingPanel() { return previousImplementingPanel; } 

  public void setResetCameraOnChanges(boolean _resetOnChanges) { this.resetCameraOnChanges = _resetOnChanges; }

  public boolean getResetCameraOnChanges() { return this.resetCameraOnChanges; }

  // ------------------------------------
  // Coordinate system
  // ------------------------------------

  /**
   * Sets the relative sizes of the bounding box. If an axis size is set to Double.NaN (default)
   * then the difference between the mininum and maximum is used instead.
   * @param sizeX double the size of the X axis
   * @param sizeY double the size of the Y axis
   * @param sizeZ double the size of the Z axis
   * @see Camera
   */
  public void setAxesSize(double sizeX, double sizeY, double sizeZ) {
    if (this.axisXSize==sizeX && this.axisYSize==sizeY && this.axisZSize==sizeZ) return;
    this.axisXSize = sizeX;
    this.axisYSize = sizeY;
    this.axisZSize = sizeZ;
    adjustScales();
    maximumSize = getMaximum3DSize();
    computeConstants(getComponent().getWidth(), getComponent().getHeight());
    if (resetCameraOnChanges) camera.adjust();
    for (Element el : elementList) el.addChange(Element.CHANGE_PROJECTION);
    for (Element el : decoration.getElementList()) el.addChange(Element.CHANGE_PROJECTION);
    implementingPanel.forceRefresh();
  }

  /**
   * Gets the size of the first axis
   * @return double
   */
  final public double getAxisXSize() { return this.axisXSize; }

  /**
   * Gets the size of the second axis
   * @return double
   */
  final public double getAxisYSize() { return this.axisYSize; }

  /**
   * Gets the size of the third axis
   * @return double
   */
  final public double getAxisZSize() { return this.axisZSize; }

  /**
   * Sets the preferred extrema for the panel. This resets the camera 
   * of the panel to its default.
   * @param minX double
   * @param maxX double
   * @param minY double
   * @param maxY double
   * @param minZ double
   * @param maxZ double
   * @see Camera
   */
  public void setPreferredMinMax(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
    if (this.xmin==minX && this.xmax==maxX && 
        this.ymin==minY && this.ymax==maxY && 
        this.zmin==minZ && this.zmax==maxZ) return;
    this.xmin = minX;
    this.xmax = maxX;
    this.ymin = minY;
    this.ymax = maxY;
    this.zmin = minZ;
    this.zmax = maxZ;
    adjustScales();
    double newSize = getMaximum3DSize();
    if (newSize!=maximumSize) { // May be equal if axes sizes have been set
      maximumSize = newSize;
      computeConstants(getComponent().getWidth(), getComponent().getHeight());
      if (resetCameraOnChanges) camera.adjust();
    }
    decoration.reset();
    for (Element el : elementList) el.addChange(Element.CHANGE_PROJECTION);
    for (Element el : decoration.getElementList()) el.addChange(Element.CHANGE_PROJECTION);
    visHints.setScaleBackground(getMaximum3DSize());
    implementingPanel.forceRefresh();
  }

  /**
   * Sets the preferred min and max in each dimension so that all
   * elements currently in the panel are visible.
   */
  public void zoomToFit() {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] firstPoint = new double[3], secondPoint = new double[3];
    for (Element el : elementList) {
      el.getExtrema(firstPoint, secondPoint);
      minX = Math.min(Math.min(minX, firstPoint[0]), secondPoint[0]);
      maxX = Math.max(Math.max(maxX, firstPoint[0]), secondPoint[0]);
      minY = Math.min(Math.min(minY, firstPoint[1]), secondPoint[1]);
      maxY = Math.max(Math.max(maxY, firstPoint[1]), secondPoint[1]);
      minZ = Math.min(Math.min(minZ, firstPoint[2]), secondPoint[2]);
      maxZ = Math.max(Math.max(maxZ, firstPoint[2]), secondPoint[2]);
    }
    double max = Math.max(Math.max(maxX-minX, maxY-minY), maxZ-minZ);
    if(max==0.0) {
      max = 2;
    }
    if(minX>=maxX) {
      minX = maxX-max/2;
      maxX = minX+max;
    }
    if(minY>=maxY) {
      minY = maxY-max/2;
      maxY = minY+max;
    }
    if(minZ>=maxZ) {
      minZ = maxZ-max/2;
      maxZ = minZ+max;
    }
    setPreferredMinMax(minX, maxX, minY, maxY, minZ, maxZ);
  }

  /**
   * Gets the preferred minimum in the X coordinate
   * @return double
   */
  final public double getPreferredMinX() { return this.xmin; }

  /**
   * Gets the preferred maximum in the X coordinate
   * @return double
   */
  final public double getPreferredMaxX() { return this.xmax; }

  /**
   * Gets the preferred minimum in the Y coordinate
   * @return double
   */
  final public double getPreferredMinY() { return this.ymin; }

  /**
   * Gets the preferred maximum in the Y coordinate
   * @return double
   */
  final public double getPreferredMaxY() { return this.ymax; }

  /**
   * Gets the preferred minimum in the Z coordinate
   * @return double
   */
  final public double getPreferredMinZ() { return this.zmin; }

  /**
   * Gets the preferred maximum in the Z coordinate
   * @return double
   */
  final public double getPreferredMaxZ() { return this.zmax; }

  //-----------------------------------
  // Adding elements
  //-----------------------------------

  /**
   * Adds an Element to this DrawingPanel3D.
   * @param element Element
   * @see Element
   */
  public void addElement(Element element) {      
    if (!elementList.contains(element)) {
      element.setPanel(this);
      elementList.add(element);
      if(element instanceof ElementText) textList.add((ElementText) element);
      implementingPanel.forceRefresh();
    }
  }

  /**
   * Removes an Element from this DrawingPanel3D
   * @param element Element
   * @see Element
   */
  public void removeElement(Element element) {
    if (elementList.remove(element)){
      if(textList.contains(element)) textList.remove(element); 
      element.removePanel();
      implementingPanel.forceRefresh();
    }

  }

  /**
   * Removes all Elements from this DrawingPanel3D
   * @see Element
   */
  public void removeAllElements() {
    elementList.clear();
    implementingPanel.forceRefresh();
  }

  /**
   * Gets the (cloned) list of Elements.
   * @return cloned list
   */
  public synchronized java.util.List<Element> getElements() {
    return new ArrayList<Element>(elementList);
  }

  /**
   * Gets the (cloned) list of TextElements.
   * @return cloned list
   */
  public synchronized java.util.List<ElementText> getTextElements() {
    return new ArrayList<ElementText>(textList);
  }

  // ---------------------------------
  // Video capturing tool
  // ---------------------------------

  /**
   * Gets the video capture tool. May be null.
   *
   * @return the video capture tool
   */
  public VideoTool getVideoTool() {
    return vidCap;
  }

  /**
   * Sets the video capture tool. May be set to null.
   *
   * @param videoTool the video capture tool
   */
  public void setVideoTool(VideoTool videoTool) {
    if(vidCap!=null) vidCap.setVisible(false); // hide the current video capture tool
    vidCap = videoTool;
  }

  public boolean canRender() { return canRender; }

  public BufferedImage render(BufferedImage image) { return implementingPanel.render(image); }

  public BufferedImage render() { return implementingPanel.render(); }

  public void update() { 
    if (canRender) implementingPanel.update(); 
  }

  // ---------------------------------
  // Implementation of InteractionSource
  // ---------------------------------

  public InteractionTarget getInteractionTarget(int target) { return myTarget; }

  public void addInteractionListener(InteractionListener listener) { listeners.add(listener); }

  public void removeInteractionListener(InteractionListener listener) { listeners.remove(listener); }

  public void addImplementationChangeListener(ImplementationChangeListener listener) { implementationListeners.add(listener); }

  /**
   * Invokes the interactionPerformed() method of all registered
   * interaction listeners
   * @param event InteractionEvent
   */
  private void invokeActions(InteractionEvent event) {
    for (InteractionListener listener : listeners) listener.interactionPerformed(event);
  }

  // ----------------------------------------------------
  // Projection, package and private methods
  // ----------------------------------------------------

  /**
   * This will be called by VisualizationHints whenever hints change.
   * @see VisualizationHints
   */
  public void hintChanged(int hintThatChanged) {
    switch(hintThatChanged) {
      case VisualizationHints.HINT_ANY: 
        decoration.updateAxesLabels(); 
        decoration.setCursorMode();
        decoration.updateType();
        break;
      case VisualizationHints.HINT_DECORATION_TYPE :
        decoration.updateType();
        decoration.reset();
        break;
      case VisualizationHints.HINT_AXES_LABELS :
        decoration.updateAxesLabels(); 
        break;
      case VisualizationHints.HINT_CURSOR_TYPE :
        decoration.setCursorMode();
        break;
      case VisualizationHints.HINT_COLORS :
        decoration.adjustColors();
        implementingPanel.visualizationChanged(hintThatChanged);
        break;
      case VisualizationHints.HINT_FONT :
        decoration.adjustFont();
        break;
      case VisualizationHints.HINT_BACKGROUND_IMAGE:
        implementingPanel.visualizationChanged(hintThatChanged);
        break;
      case VisualizationHints.HINT_BACKGROUND_MOVEABLE:
        implementingPanel.visualizationChanged(hintThatChanged);
        break;
      case VisualizationHints.HINT_BACKGROUND_SCALE:
        implementingPanel.visualizationChanged(hintThatChanged);
        break;
      case VisualizationHints.HINT_DEFAULT_ILLUMINATION:
        implementingPanel.visualizationChanged(hintThatChanged);
        break;

    }
    if (implementingPanel!=null) implementingPanel.forceRefresh();
  }

  /**
   * This will be called by the camera whenever it changes.
   * @see Camera
   */
  public void cameraChanged(int howItChanged) {
    switch(howItChanged) {
      case Camera.CHANGE_MODE :
        maximumSize = getMaximum3DSize();
        //        decoration.reset();
        computeConstants(getComponent().getWidth(),getComponent().getHeight());
        if (resetCameraOnChanges) camera.adjust();
        implementingPanel.update();
        break;
    }
    implementingPanel.cameraChanged(howItChanged);
    for (Element el : elementList) el.addChange(Element.CHANGE_PROJECTION);
    for (Element el : decoration.getElementList()) el.addChange(Element.CHANGE_PROJECTION);
    implementingPanel.forceRefresh();
  }

  // ----------------------
  // projection methods
  // ----------------------

  final public double[] getCenter() { return center.clone(); } 

  final public double getDiagonal() {
    double dx = Double.isNaN(axisXSize) ? xmax-xmin : axisXSize;
    double dy = Double.isNaN(axisYSize) ? ymax-ymin : axisYSize;
    double dz = Double.isNaN(axisZSize) ? zmax-zmin : axisZSize;
    return Math.sqrt(dx*dx+dy*dy+dz*dz); 
  }

  final public double getMaximum3DSize() {
    double dx = Double.isNaN(axisXSize) ? xmax-xmin : axisXSize;
    double dy = Double.isNaN(axisYSize) ? ymax-ymin : axisYSize;
    double dz = Double.isNaN(axisZSize) ? zmax-zmin : axisZSize;
    switch(camera.getProjectionMode()) {
      case Camera.MODE_PLANAR_XY : return Math.max(dx, dy);
      case Camera.MODE_PLANAR_XZ : return Math.max(dx, dz);
      case Camera.MODE_PLANAR_YZ : return Math.max(dy, dz);
      default : return Math.max(Math.max(dx, dy), dz);
    }
  }

  public void setSquareAspect(boolean square) {
    if (squareAspect==square) return; // only recompute if there is a change
    squareAspect = square;
    computeConstants(getComponent().getWidth(), getComponent().getHeight());
    for (Element el : elementList) el.addChange(Element.CHANGE_PROJECTION);
    for (Element el : decoration.getElementList()) el.addChange(Element.CHANGE_PROJECTION);
    implementingPanel.forceRefresh();
  }

  public boolean isSquareAspect() { return squareAspect; }

  /**
   * Computes the center and adjusts the scales from box size to coordinates extrema
   */
  private void adjustScales() {
    center[0] = Double.isNaN(axisXSize) ? (xmax+xmin)/2.0 : axisXSize/2;
    center[1] = Double.isNaN(axisYSize) ? (ymax+ymin)/2.0 : axisYSize/2;
    center[2] = Double.isNaN(axisZSize) ? (zmax+zmin)/2.0 : axisZSize/2;
    axisXScale = axisXSize/(xmax-xmin);
    axisYScale = axisYSize/(ymax-ymin);
    axisZScale = axisZSize/(zmax-zmin);
  }

  /**
   * Computes the constants for the given size in pixels.
   * @param width int
   * @param height int
   */
  public void computeConstants(int width, int height) {
    acenter = width/2;
    bcenter = height/2;
    if (squareAspect) {
      width = height = Math.min(width, height);
    }
    aconstant = 0.5*width/maximumSize;
    bconstant = 0.5*height/maximumSize;
  }

  /**
   * Converts a point on the screen into a world point
   * It only works properly for planar display modes
   */
  private double[] worldPoint(int a, int b) {
    double factor = 1.8;
    double x, y, z;
    switch(camera.getProjectionMode()) {
      case Camera.MODE_PLANAR_XY :
        x = center[0]+(a-acenter)/(factor*aconstant);
        if (!Double.isNaN(axisXSize)) x = xmin + x/axisXScale;
        y = center[1]+(bcenter-b)/(factor*bconstant);
        if (!Double.isNaN(axisYSize)) y = ymin + y/axisYScale;
        z = Double.NaN;
        break;
      case Camera.MODE_PLANAR_XZ : 
        x = center[0]+(a-acenter)/(factor*aconstant);
        if (!Double.isNaN(axisXSize)) x = xmin + x/axisXScale;
        y = Double.NaN; 
        z = center[2]+(bcenter-b)/(factor*bconstant);
        if (!Double.isNaN(axisZSize)) z = zmin + z/axisZScale;
        break;
      case Camera.MODE_PLANAR_YZ : 
        x = Double.NaN;
        y = center[1]+(a-acenter)/(factor*aconstant);
        if (!Double.isNaN(axisYSize)) y = ymin + y/axisYScale;
        z = center[2]+(bcenter-b)/(factor*bconstant);
        if (!Double.isNaN(axisZSize)) z = zmin + z/axisZScale;
        break;
      default : /* 3D */  return camera.inverseMapping(center.clone());
    }
    return new double[]{x,y,z};
  }

  // ----------------------------------------------------
  // Projection, package and private methods
  // ----------------------------------------------------

  /**
   * Scales a point and maps it to the axes
   */
  public double[] scalePosition(double[] point) {
    if (!Double.isNaN(axisXSize)) point[0] = (point[0]-xmin)*axisXScale; 
    if (!Double.isNaN(axisYSize)) point[1] = (point[1]-ymin)*axisYScale;
    if (!Double.isNaN(axisZSize)) point[2] = (point[2]-zmin)*axisZScale;
    return camera.map(point);
  }

  /**
   * Scales a size and maps it to the axes
   */
  public double[] scaleSize(double[] point) {
    if (!Double.isNaN(axisXSize)) point[0] *= axisXScale; 
    if (!Double.isNaN(axisYSize)) point[1] *= axisYScale;
    if (!Double.isNaN(axisZSize)) point[2] *= axisZScale;
    return camera.map(point);
  }


  /**
   * Converts a 3D point of the scene into a 2D point of the screen 
   * and a number which measures the relative distance of the point
   * to the camera.
   * distance = 1.0 means at the center of the scene,
   * distance > 1.0 means farther than the center of the scene,
   * distance < 1.0 means closer than the center of the scene,
   * @param point The coordinates of the point of the scene
   * @return The transformed point on the screen 2D
   */
  public double[] projectPosition(double[] point) {
    if (!Double.isNaN(axisXSize)) point[0] = (point[0]-xmin)*axisXScale; 
    if (!Double.isNaN(axisYSize)) point[1] = (point[1]-ymin)*axisYScale;
    if (!Double.isNaN(axisZSize)) point[2] = (point[2]-zmin)*axisZScale;
    camera.projectPosition(point);
    double factor = 1.8;
    switch(camera.getProjectionMode()) {
      case Camera.MODE_NO_PERSPECTIVE : case Camera.MODE_PERSPECTIVE_OFF : factor = 1.3; break;
      case Camera.MODE_PERSPECTIVE :    case Camera.MODE_PERSPECTIVE_ON :  factor = 1;   break;
    }
    point[0] = acenter+point[0]*factor*aconstant;
    point[1] = bcenter-point[1]*factor*bconstant;
    return point;
  }

  /**
   * Converts a world size at a given point into a size in the screen
   * @param point double[] The coordinates of the point at which the 3D
   * size was measured. This array may be modified.
   * @param size double[] The size in the X,Y,Z coordinates
   * @return double[] returns the transformed input size only coordinates 0 and 1 are relevant
   */
  public double[] projectSize(double[] point, double[] size) {
    if (!Double.isNaN(axisXSize)) {
      point[0] = (point[0]-xmin)*axisXScale;
      size[0] *= axisXScale;
    }
    if (!Double.isNaN(axisYSize)) {
      point[1] = (point[1]-ymin)*axisYScale;
      size[1] *= axisYScale;
    }
    if (!Double.isNaN(axisZSize)) {
      point[2] = (point[2]-zmin)*axisZScale;
      size[2] *= axisZScale;
    }
    camera.projectSize(point, size);
    double factor = 1.8;
    switch(camera.getProjectionMode()) {
      case Camera.MODE_NO_PERSPECTIVE : case Camera.MODE_PERSPECTIVE_OFF : factor = 1.3; break;
      case Camera.MODE_PERSPECTIVE : case Camera.MODE_PERSPECTIVE_ON :  factor = 1;   break;
    }
    size[0] *= factor*aconstant;
    size[1] *= factor*bconstant;
    return size;
  }

  /**
   * Computes the display fill paint of a given drawable3D based 
   * on its original paint and its depth.
   * If not a color, the original paint is returned.
   * Transparency of the original color is not affected.
   * @param _fillPaint the original paint pattern
   * @param _depth the depth value of the color
   */
  public Paint projectPaint(Paint _fillPaint, double _depth) {
    if (_fillPaint instanceof Color) return projectColor((Color) _fillPaint, _depth);
    return _fillPaint;
  }

  /**
   * Computes the display color of a given drawable3D based on its original color and its depth.
   * Transparency of the original color is not affected.
   * @param _aColor the original color
   * @param _depth the depth value of the color
   */
  public Color projectColor(Color _aColor, double _depth) {
    if (!visHints.isUseColorDepth()) return _aColor;
    // if      (_depth<0.9) return _aColor.brighter().brighter();
    // else if (_depth>1.1) return _aColor.darker().darker();
    // else return _aColor;
    float[] crc = new float[4]; // Stands for ColorRGBComponent
    try {
      _aColor.getRGBComponents(crc);
      // Do not affect transparency
      for(int i = 0; i<3; i++) {
        crc[i] /= _depth;
        crc[i] = (float) Math.max(Math.min(crc[i], 1.0), 0.0);
      }
      return new Color(crc[0], crc[1], crc[2], crc[3]);
    } catch(Exception _exc) {
      return _aColor;
    }
  }

  // ----------------------------------------------------
  // Interaction
  // ----------------------------------------------------

  /**
   * Whether the user can rotate the scene (click-dragging the mouse on it)
   */
  public void setDraggable(boolean _canDrag) { 
    if (_canDrag) draggable = DRAG_ANY; 
    else draggable = DRAG_NONE; 
  }

  /**
   * Whether the user can rotate the scene (click-dragging the mouse on it)
   */
  public boolean isDraggable() { return draggable==DRAG_ANY; }

  /**
   * A finer way of specifying the possible drags
   * Options are:
   * <ul>
   *   <li> DrawingPanel3D.DRAG_NONE no drag allowed</li>
   *   <li> DrawingPanel3D.DRAG_ANY any drag is allowed (changes altitude and azimuth)</li>
   *   <li> DrawingPanel3D.DRAG_AZIMUTH rotation to change the azimuth only, respecting the altitude</li>
   *   <li> DrawingPanel3D.DRAG_ALTITUDE rotation to change the altitude, respecting the azimuth</li>
   * </ul>
   * @param _dragType
   */
  public void setDraggable(int _dragType) { draggable = _dragType; }

  /**
   * Returns the int code for the dragging allowed on the panel
   * @return int
   */
  public int getDraggable() { return this.draggable; }

  private InteractionTarget getTargetHit(int x, int y) {
    for (Element el : elementList) {
      if (!el.isVisible()) continue; 
      if (!el.isEnabled()) continue;
      el.processChanges(Element.CHANGE_NONE);
      InteractionTarget target = el.getTargetHit(x, y);
      if (target!=null) return target;
    }
    return null;
  }

  private void setMouseCursor(Cursor cursor) {
    implementingPanel.getComponent().setCursor(cursor);
    //     Container c = implementingPanel.getComponent().getTopLevelAncestor();
    //     if (c!=null) c.setCursor(cursor);
  }

  private void displayPosition(double[] _point) {
    visHints.displayPosition(camera.getProjectionMode(), _point);
    decoration.positionTrackers(_point);
  }

  // returns true if the tracker was moved
  private boolean mouseDraggedComputations(java.awt.event.MouseEvent e) {
    if(e.isControlDown()) { // Panning
      if (!isDraggable()) return false; 
      if(camera.is3dMode()) {
        double fx = camera.getFocusX(), fy = camera.getFocusY(), fz = camera.getFocusZ();
        double dx = (e.getX()-lastX)*maximumSize*0.01, dy = (e.getY()-lastY)*maximumSize*0.01;
        switch(keyPressed) {
          case 88 :      // X is pressed
            if((camera.getCosAlpha()>=0)&&(Math.abs(camera.getSinAlpha())<camera.getCosAlpha())) {
              camera.setFocusXYZ(fx+dy, fy, fz);
            } else if((camera.getSinAlpha()>=0)&&(Math.abs(camera.getCosAlpha())<camera.getSinAlpha())) {
              camera.setFocusXYZ(fx+dx, fy, fz);
            } else if((camera.getCosAlpha()<0)&&(Math.abs(camera.getSinAlpha())<-camera.getCosAlpha())) {
              camera.setFocusXYZ(fx-dy, fy, fz);
            } else {
              camera.setFocusXYZ(fx-dx, fy, fz);
            }
            break;
          case 89 :      // Y is pressed
            if((camera.getCosAlpha()>=0)&&(Math.abs(camera.getSinAlpha())<camera.getCosAlpha())) {
              camera.setFocusXYZ(fx, fy-dx, fz);
            } else if((camera.getSinAlpha()>=0)&&(Math.abs(camera.getCosAlpha())<camera.getSinAlpha())) {
              camera.setFocusXYZ(fx, fy+dy, fz);
            } else if((camera.getCosAlpha()<0)&&(Math.abs(camera.getSinAlpha())<-camera.getCosAlpha())) {
              camera.setFocusXYZ(fx, fy+dx, fz);
            } else {
              camera.setFocusXYZ(fx, fy-dy, fz);
            }
            break;
          case 90 :      // Z is pressed
            if(camera.getCosBeta()>=0) {
              camera.setFocusXYZ(fx, fy, fz+dy);
            } else {
              camera.setFocusXYZ(fx, fy, fz-dy);
            }
            break;
          default :
            if(camera.getCosBeta()<0) {
              dy = -dy;
            }
            if((camera.getCosAlpha()>=0)&&(Math.abs(camera.getSinAlpha())<camera.getCosAlpha())) {
              camera.setFocusXYZ(fx, fy-dx, fz+dy);
            } else if((camera.getSinAlpha()>=0)&&(Math.abs(camera.getCosAlpha())<camera.getSinAlpha())) {
              camera.setFocusXYZ(fx+dx, fy, fz+dy);
            } else if((camera.getCosAlpha()<0)&&(Math.abs(camera.getSinAlpha())<-camera.getCosAlpha())) {
              camera.setFocusXYZ(fx, fy+dx, fz-dy);
            } else {
              camera.setFocusXYZ(fx-dx, fy, fz-dy);
            }
            break;
        }
      }
      else { // 2D modes
        double fx = camera.getFocusX(), fy = camera.getFocusY(), fz = camera.getFocusZ();
        double dx = (e.getX()-lastX)*maximumSize*0.01, dy = (e.getY()-lastY)*maximumSize*0.01;
        switch (camera.getProjectionMode()) {
          case Camera.MODE_PLANAR_XY :  camera.setFocusXYZ(fx-dx, fy+dy, fz); break;
          case Camera.MODE_PLANAR_YZ :  camera.setFocusXYZ(fx   , fy-dx, fz+dy); break;
          case Camera.MODE_PLANAR_XZ :  camera.setFocusXYZ(fx-dx, fy   , fz+dy); break;
        }
      }
      return false;
    }   // End of panning

    if(e.isShiftDown()) { // Zooming
      camera.setDistanceToScreen(camera.getDistanceToScreen()-(e.getY()-lastY)*maximumSize*0.01);
      return false;
    }

    if(camera.is3dMode()&&(targetHit==null)&&!e.isAltDown()) { // Rotating (in 3D)
      mouseDraggedComputationsForDraggingTheScene(e, this.draggable);
      //        if (isDraggable()) camera.setAzimuthAndAltitude(camera.getAzimuth()-(e.getX()-lastX)*0.01, camera.getAltitude()+(e.getY()-lastY)*0.005);
      return false;
    }

    if (trackerPoint==null)  return true;

    // In all other cases, you are moving the tracker
    if (!camera.is3dMode()) { // 2D modes
      double[] point = worldPoint(e.getX(), e.getY());
      switch(keyPressed) {
        case 88 : /* X is pressed */ if (!Double.isNaN(point[0])) trackerPoint[0] = point[0]; break;
        case 89 : /* Y is pressed */ if (!Double.isNaN(point[1])) trackerPoint[1] = point[1]; break;
        case 90 : /* Z is pressed */ if (!Double.isNaN(point[2])) trackerPoint[2] = point[2]; break;
        default : /* No key is pressed */ 
          if (!Double.isNaN(point[0])) trackerPoint[0] = point[0];
          if (!Double.isNaN(point[1])) trackerPoint[1] = point[1];
          if (!Double.isNaN(point[2])) trackerPoint[2] = point[2];
          break; 
      }
    } // End of 2D modes
    else { // 3D modes
      double dH = (e.getX()-lastX)/(1.3*aconstant);
      double dV = (e.getY()-lastY)/(1.3*bconstant);
      double zeroCase;
      double[] point = new double[3];
      if (camera.getCosBeta()>=0) point[2] = -dV;
      else point[2] = dV;
      if      ((camera.getCosAlpha()>=0)&&(Math.abs(camera.getSinAlpha())<camera.getCosAlpha())) { point[0] =   0; point[1] =  dH; zeroCase = -dH; }
      else if ((camera.getSinAlpha()>=0)&&(Math.abs(camera.getCosAlpha())<camera.getSinAlpha())) { point[0] = -dH; point[1] =   0; zeroCase =  dH; }
      else if((camera.getCosAlpha()<0)&&(Math.abs(camera.getSinAlpha())<-camera.getCosAlpha()))  { point[0] =   0; point[1] = -dH; zeroCase =  dH; }
      else { point[0] = dH; point[1] =  0; zeroCase = -dH; }
      camera.inverseMapping(point);
      double sx = Double.isNaN(axisXSize) ? 1 : axisXScale;
      double sy = Double.isNaN(axisYSize) ? 1 : axisYScale;
      double sz = Double.isNaN(axisZSize) ? 1 : axisZScale;
      switch(keyPressed) {
        case 88 : /* X is pressed */ trackerPoint[0] += (point[0]==0) ? zeroCase/sx: point[0]/sx; break;
        case 89 : /* Y is pressed */ trackerPoint[1] += (point[1]==0) ? zeroCase/sy : point[1]/sy; break;
        case 90 : /* Z is pressed */ trackerPoint[2] += (point[2]==0) ? zeroCase/sz : point[2]/sz; break;
        default : /* No key is pressed */ 
          trackerPoint[0] += point[0]/sx;
          trackerPoint[1] += point[1]/sy;
          trackerPoint[2] += point[2]/sz;
          break;
      }
    } // End of 3D modes
    return true;
  }

  static private class AxisInfo {
    double deltaH, deltaV, scale;
  }

  private AxisInfo xAxisInfo = new AxisInfo(), yAxisInfo = new AxisInfo(), zAxisInfo = new AxisInfo();

  private AxisInfo getAxisInfo(int _axisNumber, AxisInfo _info) {
    ElementSegment axis;
    switch (_axisNumber) {
      default : 
        axis = decoration.getXAxis(); 
        _info.scale = Double.isNaN(axisXSize) ? 1 : axisXScale;
        break;
      case 1 : 
        axis = decoration.getYAxis(); 
        _info.scale = Double.isNaN(axisYSize) ? 1 : axisYScale;
        break;
      case 2 : 
        axis = decoration.getZAxis(); 
        _info.scale = Double.isNaN(axisZSize) ? 1 : axisZScale;
        break;
    }
    axis.projectInteractionPoints();
    double[] origin = axis.getProjectedOrigin();
    double[] end    = axis.getProjectedEnd();
    _info.deltaH = end[0] - origin[0];
    _info.deltaV = end[1] - origin[1];
    return _info;
  }

  // returns true if the tracker was moved
  private boolean mouseDraggedComputationsForDraggingTheScene(java.awt.event.MouseEvent e, int _targetRestrictions) {
    if (_targetRestrictions==DRAG_NONE) return false; // You can not change the scene projection
    double dAzi = (e.getX()-lastX)*0.01;
    double dAlt = (e.getY()-lastY)*0.005;
    if (_targetRestrictions==DRAG_AZIMUTH) { // Change only altitude
      camera.setAzimuthAndAltitude(camera.getAzimuth()-dAzi, camera.getAltitude());
    } 
    else if (_targetRestrictions==DRAG_ALTITUDE) { // Change only altitude
      camera.setAzimuthAndAltitude(camera.getAzimuth(), camera.getAltitude()+dAlt);
    } 
    else { // no restrictions
      camera.setAzimuthAndAltitude(camera.getAzimuth()-dAzi, camera.getAltitude()+dAlt);
    }
    return false;
  }

  // returns true if the tracker was moved
  private boolean mouseDraggedComputationsOnTarget(java.awt.event.MouseEvent e, int _targetRestrictions) {
    if (!camera.is3dMode()) { // 2D modes
      double[] point = worldPoint(e.getX(), e.getY());
      if (keyPressed==88 || _targetRestrictions==InteractionTarget.ENABLED_X) { // X is pressed or only X is allowed  
        if (!Double.isNaN(point[0])) trackerPoint[0] = point[0]; 
      }
      else if (keyPressed==89 || _targetRestrictions==InteractionTarget.ENABLED_Y) { // Y is pressed or only Y is allowed  
        if (!Double.isNaN(point[1])) trackerPoint[1] = point[1]; 
      } 
      else if (keyPressed==90 || _targetRestrictions==InteractionTarget.ENABLED_Z) { // Z is pressed or only Z is allowed  
        if (!Double.isNaN(point[2])) trackerPoint[2] = point[2]; 
      } 
      else if (_targetRestrictions==InteractionTarget.ENABLED_XY) { // Only XY is allowed  
        if (!Double.isNaN(point[0])) trackerPoint[0] = point[0];
        if (!Double.isNaN(point[1])) trackerPoint[1] = point[1];
      } 
      else if (_targetRestrictions==InteractionTarget.ENABLED_XZ) { // Only XZ is allowed  
        if (!Double.isNaN(point[0])) trackerPoint[0] = point[0];
        if (!Double.isNaN(point[2])) trackerPoint[2] = point[2];
      } 
      else if (_targetRestrictions==InteractionTarget.ENABLED_YZ) { // Only YZ is allowed  
        if (!Double.isNaN(point[1])) trackerPoint[1] = point[1];
        if (!Double.isNaN(point[2])) trackerPoint[2] = point[2];
      }
      else { // no restrictions
        if (!Double.isNaN(point[0])) trackerPoint[0] = point[0];
        if (!Double.isNaN(point[1])) trackerPoint[1] = point[1];
        if (!Double.isNaN(point[2])) trackerPoint[2] = point[2];
      }
    } // End of 2D modes
    else { // 3D modes
      double dH = (e.getX()-lastX)/(1.3*aconstant);
      double dV = (e.getY()-lastY)/(1.3*bconstant);
      if (keyPressed==88 || _targetRestrictions==InteractionTarget.ENABLED_X) { // X is pressed or only X is allowed
        AxisInfo info = getAxisInfo(0, xAxisInfo);
        if (Math.abs(info.deltaH)>=Math.abs(info.deltaV)) trackerPoint[0] += Math.signum(info.deltaH)*dH/info.scale;
        else trackerPoint[0] += Math.signum(info.deltaV)*dV/info.scale;
      }
      else if (keyPressed==89 || _targetRestrictions==InteractionTarget.ENABLED_Y) { // Y is pressed or only Y is allowed  
        AxisInfo info = getAxisInfo(1, yAxisInfo);
        if (Math.abs(info.deltaH)>=Math.abs(info.deltaV)) trackerPoint[1] += Math.signum(info.deltaH)*dH/info.scale;
        else trackerPoint[1] += Math.signum(info.deltaV)*dV/info.scale;
      } 
      else if (keyPressed==90 || _targetRestrictions==InteractionTarget.ENABLED_Z) { // Z is pressed or only Z is allowed  
        AxisInfo info = getAxisInfo(2, zAxisInfo);
        if (Math.abs(info.deltaH)>=Math.abs(info.deltaV)) trackerPoint[2] += Math.signum(info.deltaH)*dH/info.scale;
        else trackerPoint[2] += Math.signum(info.deltaV)*dV/info.scale;
      } 
      else if (_targetRestrictions==InteractionTarget.ENABLED_XY) { // Only XY is allowed  
        AxisInfo xInfo = getAxisInfo(0, xAxisInfo);
        AxisInfo yInfo = getAxisInfo(1, yAxisInfo);
        if (Math.abs(xInfo.deltaH)>=Math.abs(yInfo.deltaH)) { // horizontal for X, vertical for Y
          trackerPoint[0] += Math.signum(xInfo.deltaH)*dH/xInfo.scale;
          trackerPoint[1] += Math.signum(yInfo.deltaV)*dV/yInfo.scale;
        }
        else { // horizontal for Y, vertical for X
          trackerPoint[0] += Math.signum(xInfo.deltaV)*dV/xInfo.scale;
          trackerPoint[1] += Math.signum(yInfo.deltaH)*dH/yInfo.scale;
        }           
      } 
      else if (_targetRestrictions==InteractionTarget.ENABLED_XZ) { // Only XZ is allowed  
        AxisInfo xInfo = getAxisInfo(0, xAxisInfo);
        AxisInfo zInfo = getAxisInfo(2, zAxisInfo);
        if (Math.abs(xInfo.deltaH)>=Math.abs(zInfo.deltaH)) { // horizontal for X, vertical for Z
          trackerPoint[0] += Math.signum(xInfo.deltaH)*dH/xInfo.scale;
          trackerPoint[2] += Math.signum(zInfo.deltaV)*dV/zInfo.scale;
        }
        else { // horizontal for Z, vertical for X
          trackerPoint[0] += Math.signum(xInfo.deltaV)*dV/xInfo.scale;
          trackerPoint[2] += Math.signum(zInfo.deltaH)*dH/zInfo.scale;
        }           
      } 
      else if (_targetRestrictions==InteractionTarget.ENABLED_YZ) { // Only YZ is allowed  
        AxisInfo yInfo = getAxisInfo(1, yAxisInfo);
        AxisInfo zInfo = getAxisInfo(2, zAxisInfo);
        if (Math.abs(yInfo.deltaH)>=Math.abs(zInfo.deltaH)) { // horizontal for Y, vertical for Z
          trackerPoint[1] += Math.signum(yInfo.deltaH)*dH/yInfo.scale;
          trackerPoint[2] += Math.signum(zInfo.deltaV)*dV/zInfo.scale;
        }
        else { // horizontal for Z, vertical for Y
          trackerPoint[1] += Math.signum(yInfo.deltaV)*dV/yInfo.scale;
          trackerPoint[2] += Math.signum(zInfo.deltaH)*dH/zInfo.scale;
        }           
      }
      else { // no restrictions
        AxisInfo xInfo = getAxisInfo(0, xAxisInfo);
        AxisInfo yInfo = getAxisInfo(1, yAxisInfo);
        AxisInfo zInfo = getAxisInfo(2, zAxisInfo);
        // Which axis gets the horizontal move?
        int horMove;
        if (Math.abs(xInfo.deltaH)>=Math.abs(yInfo.deltaH)) { // Y will NOT get the horizontal move
          if (Math.abs(xInfo.deltaH)>=Math.abs(zInfo.deltaH)) { // horizontal for X
            horMove = 0;
            trackerPoint[0] += Math.signum(xInfo.deltaH)*dH/xInfo.scale;
          }
          else { // Horizontal for Z
            horMove = 2;
            trackerPoint[2] += Math.signum(zInfo.deltaH)*dH/zInfo.scale;
          }
        }
        else { // X will NOT get the horizontal move
          if (Math.abs(yInfo.deltaH)>=Math.abs(zInfo.deltaH)) { // horizontal for Y
            horMove = 1;
            trackerPoint[1] += Math.signum(yInfo.deltaH)*dH/yInfo.scale;
          }
          else {
            horMove = 2;
            trackerPoint[2] += Math.signum(zInfo.deltaH)*dH/zInfo.scale; // Horizontal for Z
          }
        }
        // Which axis gets the vertical move?
        if (horMove==0) { // X got the horizontal motion, so only Y and Z can get the vertical one
          if (Math.abs(yInfo.deltaV)>=Math.abs(zInfo.deltaV)) trackerPoint[1] += Math.signum(yInfo.deltaV)*dV/yInfo.scale; // Y gets the vertical motion 
          else trackerPoint[2] += Math.signum(zInfo.deltaV)*dV/zInfo.scale; // vertical for Z
        }
        else if (horMove==1) { // Y got the horizontal motion, so only X and Z can get the vertical one
          if (Math.abs(xInfo.deltaV)>=Math.abs(zInfo.deltaV)) trackerPoint[0] += Math.signum(xInfo.deltaV)*dV/xInfo.scale; // X gets the vertical motion 
          else trackerPoint[2] += Math.signum(zInfo.deltaV)*dV/zInfo.scale; // vertical for Z
        }
        else { // Z got the horizontal motion, so only X and Y can get the vertical one
          if (Math.abs(xInfo.deltaV)>=Math.abs(yInfo.deltaV)) trackerPoint[0] += Math.signum(xInfo.deltaV)*dV/xInfo.scale; // X gets the vertical motion 
          else trackerPoint[1] += Math.signum(yInfo.deltaV)*dV/yInfo.scale; // vertical for Y
        }
      }

    } // End of 3D modes
    return true;
  }

  private void resetInteraction() {
    targetHit = null;
    displayPosition(null);
    implementingPanel.update();
  }

  /**
   * The inner class that will handle all mouse related events.
   */
  private class IADMouseController extends MouseInputAdapter {

    public void mousePressed(MouseEvent _evt) {
      implementingPanel.getComponent().requestFocus();
      if (_evt.isPopupTrigger() || _evt.getModifiers() == InputEvent.BUTTON3_MASK) return;
      //         quickRedrawOn = visHints.isAllowQuickRedraw() || keyPressed==83;  // 's' is pressed
      /*
         if(visHints.isAllowQuickRedraw()&&((_evt.getModifiers()&InputEvent.BUTTON1_MASK)!=0)) {
            quickRedrawOn = true;
         } else {
            quickRedrawOn = false;
         }
       */
      lastX = _evt.getX();
      lastY = _evt.getY();
      targetHit = getTargetHit(lastX, lastY);
      if (targetHit!=null) {
        Element el = targetHit.getElement();
        trackerPoint = el.getHotSpot(targetHit);
        el.invokeActions(new InteractionEvent(el, InteractionEvent.MOUSE_PRESSED, targetHit.getActionCommand(),
            targetHit, _evt));
        trackerPoint = el.getHotSpot(targetHit);     // because the listener may change the position of the element
        if (targetHit.getEnabled()!=InteractionTarget.ENABLED_NO_MOVE) displayPosition(trackerPoint);
      } 
      else if(myTarget.isEnabled()) {               // No interactive has been hit
        if((!camera.is3dMode())||_evt.isAltDown()) { // In 2D by default, in 3D only if you hold ALT down
          // You are trying to track a given point
          trackerPoint = worldPoint(_evt.getX(), _evt.getY());
          if (Double.isNaN(trackerPoint[0])) trackerPoint[0] = xmax;
          if (Double.isNaN(trackerPoint[1])) trackerPoint[1] = ymax;
          if (Double.isNaN(trackerPoint[2])) trackerPoint[2] = zmax;
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_PRESSED,
              myTarget.getActionCommand(), trackerPoint, _evt));
          if (targetHit.getEnabled()!=InteractionTarget.ENABLED_NO_MOVE) displayPosition(trackerPoint);
        } else {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_PRESSED,
              myTarget.getActionCommand(), null, _evt));
          resetInteraction();
          return;
        }
      } else {
        resetInteraction();
        return;
      }
      // repaint();  removed by W. Christian
      implementingPanel.update();
      //         implementingPanel.forceRefresh();
      //         dirtyImage = true;
      //         updatePanel();
    }

    public void mouseReleased(MouseEvent _evt) {
      if (_evt.isPopupTrigger() || _evt.getModifiers() == InputEvent.BUTTON3_MASK) return;
      if(targetHit!=null) {
        Element el = targetHit.getElement();
        el.invokeActions(new InteractionEvent(el, InteractionEvent.MOUSE_RELEASED, targetHit.getActionCommand(),
            targetHit, _evt));
      } else if(myTarget.isEnabled()) {
        if((!camera.is3dMode())||_evt.isAltDown()) { // In 2D by default, in 3D only if you hold ALT down
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_RELEASED,
              myTarget.getActionCommand(), trackerPoint, _evt));
        } else {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_RELEASED,
              myTarget.getActionCommand(), null, _evt));
        }
      }
      implementingPanel.setFastRedraw(false);
      resetInteraction();
      // setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseDragged(MouseEvent _evt) {
      if (_evt.isPopupTrigger() || _evt.getModifiers() == InputEvent.BUTTON3_MASK) return;
      implementingPanel.setFastRedraw(visHints.isAllowQuickRedraw() && keyPressed!=83);
      if (targetHit!=null) { // Moving or sizing an element
        mouseDraggedComputationsOnTarget(_evt,targetHit.getEnabled());
        lastX = _evt.getX();
        lastY = _evt.getY();
        Element el = targetHit.getElement();
        el.updateHotSpot(targetHit, trackerPoint);
        el.invokeActions(new InteractionEvent(el, InteractionEvent.MOUSE_DRAGGED, targetHit.getActionCommand(),targetHit, _evt));
        trackerPoint = el.getHotSpot(targetHit); // The listener may change the position of the element
        if (targetHit.getEnabled()!=InteractionTarget.ENABLED_NO_MOVE) displayPosition(trackerPoint);
      }
      else { // Panning, rotating, or dragging the cursor
        boolean trackerMoved = mouseDraggedComputations(_evt);
        lastX = _evt.getX();
        lastY = _evt.getY();
        if(!trackerMoved) { // Report any listener that the projection has changed. Data is NULL!
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_DRAGGED, myTarget.getActionCommand(), null, _evt));
          resetInteraction();
          return;
        }
        if(myTarget.isEnabled()) {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_DRAGGED,
              myTarget.getActionCommand(), trackerPoint, _evt));
          displayPosition(trackerPoint);
        }
      }
      implementingPanel.update();
    }

    public void mouseEntered(MouseEvent _evt) {
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if(myTarget.isEnabled()) {
        invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_ENTERED,
            myTarget.getActionCommand(), null, _evt));
      }
      targetHit = targetEntered = null;
    }

    public void mouseExited(MouseEvent _evt) {
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      if(myTarget.isEnabled()) {
        invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_EXITED,
            myTarget.getActionCommand(), null, _evt));
      }
      targetHit = targetEntered = null;
    }

    public void mouseClicked(MouseEvent _evt) {
      if (_evt.isMetaDown()){} //Right click
    }

    public void mouseMoved(MouseEvent _evt) {
      InteractionTarget target = getTargetHit(_evt.getX(), _evt.getY());
      if(target!=null) {
        if(targetEntered==null) {
          target.getElement().invokeActions(new InteractionEvent(target.getElement(),
              InteractionEvent.MOUSE_ENTERED, target.getActionCommand(), target, _evt));
        }
        setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      } else { // No target under the cursor
        if(targetEntered!=null) {
          targetEntered.getElement().invokeActions(new InteractionEvent(targetEntered.getElement(),
              InteractionEvent.MOUSE_EXITED, targetEntered.getActionCommand(), targetEntered, _evt));
        } else if(myTarget.isEnabled()) {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_MOVED,
              myTarget.getActionCommand(), null, _evt));
        }
        setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
      targetEntered = target;
    }
  }

}
/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
