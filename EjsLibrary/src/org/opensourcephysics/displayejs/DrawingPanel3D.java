/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import java.util.*;
import java.text.NumberFormat;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.InteractivePanel;

/**
 * Drawing3DPanel renders drawable 3D objects on its canvas.
 * @author Francisco Esquembre, August 2003
 *
 */
public class DrawingPanel3D extends InteractivePanel implements InteractionSource {
  private static final long serialVersionUID = 1L;

  static public final int TRANSPARENCY_LEVEL=100;

  private static final int AXIS_DIVISIONS = 10;

  public static final int DISPLAY_PLANAR  = 0;
  public static final int DISPLAY_PLANAR_XY  = DISPLAY_PLANAR;
  public static final int DISPLAY_PLANAR_XZ  = 1;
  public static final int DISPLAY_PLANAR_YZ  = 2;
  public static final int DISPLAY_3D  = 10; // Thus displayMode<DISPLAY_3D implies any PLANAR mode
  public static final int DISPLAY_PERSPECTIVE = DISPLAY_3D;
  public static final int DISPLAY_NO_PERSPECTIVE = 12;

  public static final int CURSOR_NONE = 0;
  public static final int CURSOR_XYZ = 1;
  public static final int CURSOR_CUBE = 2;
  public static final int CURSOR_CROSSHAIR = 3;

  public static final int DECORATION_NONE = 0;
  public static final int DECORATION_AXES = 1;
  public static final int DECORATION_CUBE = 2;

  // Configuration variables
  protected boolean autoscaleZ = false, removeHiddenLines = true, respondToMouse=true,
                    showPosition=true, useColorDepth=true;
  protected boolean allowQuickRedraw = true; // Whether to allow quick redraws when dragging
  protected int displayMode=DISPLAY_PERSPECTIVE, cursorMode=CURSOR_XYZ, decorationType=DECORATION_CUBE;
  protected int deltaa = 0, deltab = 0;
  protected double alpha=0, beta=0, zoom=1.0;
  protected double  zmin = -1.0, zmax = 1.0;
  protected double  zminPreferred = -10.0, zmaxPreferred = 10.0;
  protected double  zfloor = Double.NaN, zceil = Double.NaN;
  protected java.util.List<InteractionListener> listeners = new ArrayList<InteractionListener>();
  protected NumberFormat xFormat = new java.text.DecimalFormat("x=0.000;x=-0.000");
  protected NumberFormat yFormat = new java.text.DecimalFormat("y=0.000;y=-0.000");
  protected NumberFormat zFormat = new java.text.DecimalFormat("z=0.000;z=-0.000");

  // Implementation variables
  private boolean quickRedrawOn = false;
  private double cosAlpha=Math.cos(alpha), sinAlpha=Math.sin(alpha),
                 cosBeta =Math.cos(beta),  sinBeta =Math.sin(beta);
  private double ratioToPlane=2.5, ratioToCenter=2.0;
  private double centerX, centerY, centerZ;
  private double aconstant, bconstant, viewToPlane, viewToCenter;
  private int acenter, bcenter;
  private int trackersVisible, keyPressed=-1;
  private Point3D trackerPoint = new Point3D(0.0,0.0,0.0);
  private ArrayList<Object3D> list3D = new ArrayList<Object3D>();
  private Comparator3D comparator = new Comparator3D();

  // Variables for decoration
  private InteractiveArrow xAxis, yAxis, zAxis;
  private InteractiveText  xText, yText, zText;
  private InteractiveArrow[] boxSides = new InteractiveArrow[12], trackerLines=null;

  public DrawingPanel3D() { this(DISPLAY_PERSPECTIVE); }

  public DrawingPanel3D(int _displayMode) {
    super.setSquareAspect (true);
    super.setShowCoordinates(false);
    super.enableInspector(false);
    super.removeOptionController();
    autoscaleX = autoscaleY = autoscaleZ = false;

    addComponentListener (
      new java.awt.event.ComponentAdapter () {
       public void componentResized (java.awt.event.ComponentEvent e) { 
         computeConstants (1);
         if (!getIgnoreRepaint()) repaint();
       }
     }
    );
    addKeyListener (
      new java.awt.event.KeyAdapter() {
        public void keyPressed  (java.awt.event.KeyEvent _e) { keyPressed = _e.getKeyCode(); }
        public void keyReleased (java.awt.event.KeyEvent _e) { keyPressed = -1; }
      }
    );
    /* Decoration of the scene */
    // Create the bounding box
    Resolution axesRes = new Resolution(AXIS_DIVISIONS);
    for (int i=0, n=boxSides.length; i<n; i++) {
      boxSides[i] = new InteractiveArrow(InteractiveArrow.SEGMENT);
      boxSides[i].setResolution(axesRes);
      boxSides[i].setEnabled(false);
      boxSides[i].canBeMeasured(false);
    }
    // Create the axes
    xAxis = new InteractiveArrow(InteractiveArrow.ARROW);
    xAxis.setResolution(axesRes);    xAxis.setEnabled(false);    xAxis.canBeMeasured(false);
    xText = new InteractiveText ("X");
    xText.getStyle().setFont(new Font("Dialog",Font.PLAIN,12));  xText.setEnabled(false);    xText.canBeMeasured(false);
    yAxis = new InteractiveArrow(InteractiveArrow.ARROW);
    yAxis.setResolution(axesRes);    yAxis.setEnabled(false);    yAxis.canBeMeasured(false);
    yText = new InteractiveText ("Y");
    yText.getStyle().setFont(new Font("Dialog",Font.PLAIN,12));  yText.setEnabled(false);    yText.canBeMeasured(false);
    zAxis = new InteractiveArrow(InteractiveArrow.ARROW);
    zAxis.setResolution(axesRes);    zAxis.setEnabled(false);    zAxis.canBeMeasured(false);
    zText = new InteractiveText ("Z");
    zText.getStyle().setFont(new Font("Dialog",Font.PLAIN,12));  zText.setEnabled(false);    zText.canBeMeasured(false);
    // Create the trackers
    trackerLines = new InteractiveArrow[9];
    for (int i=0, n=trackerLines.length; i<n; i++) {
      trackerLines[i] = new InteractiveArrow(InteractiveArrow.SEGMENT);
      trackerLines[i].setResolution(axesRes); trackerLines[i].setEnabled(false); trackerLines[i].setVisible(false); trackerLines[i].canBeMeasured(false);
    }
    // Properly color the basic elements
    setForeground(this.getForeground());
    // Add the basic elements
    clear();
//    resetDecoration();  Will be done below in computeConstants();
    /* End of decoration */
    // Set default for displayMode
    this.displayMode = _displayMode;
    if (displayMode<DISPLAY_3D) { // i.e. 2D
      setDecorationType(DECORATION_NONE);
      setCursorMode(CURSOR_NONE);
      setUseColorDepth(false);
    }
    else {
      setDecorationType(DECORATION_CUBE);
      setCursorMode(CURSOR_XYZ);
      setUseColorDepth(true);
    }
    setPixelScale();
    computeConstants(2);
    setPreferredMinMax(-1.0,1.0,-1.0,1.0,-1.0,1.0);
  }

// ------------------------------------------------
//  Configuration methods
// ------------------------------------------------

/**
 * Whether to keep aspect ratio (in 2D modes)
 */
  public void setSquareAspect(boolean _val) {
    if(squareAspect==_val) {
      return;
    }
    squareAspect = _val;
    computeConstants(3);
    invalidateImage(); // validImage = false;
    repaint();
  }

  /**
   * Whether to automatically compute the extrema in the Z coordinate
   * @param value the desired value
   */
  public void setAutoscaleZ(boolean _autoscale) { 
    this.autoscaleZ = _autoscale; 
    invalidateImage(); // validImage = false;
  }
  /**
   * Whether to automatically compute the extrema in the Z coordinate
   * @return the value
   */
  public boolean isAutoscaleZ() { return this.autoscaleZ; }

  /**
   * Whether the panel should try to remove hidden lines
   * @param value the desired value
   */
  public void setRemoveHiddenLines (boolean _value) { 
    this.removeHiddenLines = _value; 
    invalidateImage(); // validImage = false;
  }
  /**
   * Whether the panel tries to remove hidden lines
   * @return the value
   */
  public boolean isRemoveHiddenLines () { return this.removeHiddenLines; }

  /**
   * Whether to allow quick redraws when rotating the scene.
   * This is independent of the value of removeHiddenLines.
   * @param value the desired value
   */
  public void setAllowQuickRedraw (boolean _allow) { 
    this.allowQuickRedraw = _allow; 
    invalidateImage(); // validImage = false;
  }
  /**
   * Whether quick redraw is allowed when rotating the scene
   * @return the value
   */
  public boolean isAllowQuickRedraw() { return this.allowQuickRedraw; }

  /**
   * Whether to display coordinates when interacting
   * @param value the desired value
   */
  public void setShowCoordinates(boolean _val) { this.showPosition = _val; }
  /**
   * Whether to display coordinates when interacting
   * @return the value
   */
  public boolean isShowCoordinates() { return this.showPosition; }


  /**
   * Sets the format for the display of the X coordinate
   * @param _format the desired format
   */
  public void setXFormat(java.text.NumberFormat _format) { this.xFormat = _format; }
  /**
   * Sets the format for the display of the Y coordinate
   * @param _format the desired format
   */
  public void setYFormat(java.text.NumberFormat _format) { this.yFormat = _format; }
  /**
   * Sets the format for the display of the Z coordinate
   * @param _format the desired format
   */
  public void setZFormat(java.text.NumberFormat _format) { this.zFormat = _format; }

 /**
  * Whether to change color according to the distance
   * @param value the desired value
  */
  public void setUseColorDepth (boolean _mode) { 
    this.useColorDepth = _mode; 
    invalidateImage(); // validImage = false;
  }
 /**
  * Whether it changes color according to the distance
   * @return the value
  */
  public boolean isUseColorDepth () { return this.useColorDepth; }

  /**
   * The display mode for the panel. One of the following
   * <ul>
   *   <li><b>DISPLAY_PERSPECTIVE</b>: 3D view with perspective. The default.</li>
   *   <li><b>DISPLAY_NO_PERSPECTIVE</b>: 3D view with no perspective.</li>
   *   <li><b>DISPLAY_XY</b>: 2D view using X and Y coordinates.</li>
   *   <li><b>DISPLAY_XZ</b>: 2D view using X and Z coordinates.</li>
   *   <li><b>DISPLAY_YZ</b>: 2D view using Y and Z coordinates.</li>
   * </ul>
   * @param value the desired value
   */
  public void setDisplayMode (int _mode) {
    if (this.displayMode==_mode) return; // Save time, if possible
    this.displayMode = _mode;
    setDecorationType (this.decorationType); // Because in 2D some axes labels get hidden
    computeConstants(4); // constants get affected by the displayMode
    invalidateImage(); // validImage = false;
    if(!getIgnoreRepaint()) {
      repaint();
    }
  }
  /**
   * The display mode for the panel.
   * @return the current value
   */
  public int getDisplayMode () { return this.displayMode; }

  /**
   * The style to display the cursor.One of the following
   * <ul>
   *   <li><b>CURSOR_NONE</b>: No cursor</li>
   *   <li><b>CURSOR_CUBE</b>: A cube from the minima to the cursor point</li>
   *   <li><b>CURSOR_XYZ</b>: An xyz line from the minima to the cursor point</li>
   *   <li><b>CURSOR_CROSHAIR</b>: Lines from the minima to the maxima through the cursor point</li>
   * </ul>
   * @param value the desired value
   */
  public void setCursorMode (int _mode) {
    this.cursorMode = _mode;
    switch (_mode) {
      case CURSOR_NONE      : trackersVisible = 0; break;
      case CURSOR_XYZ       : trackersVisible = 3; break;
      case CURSOR_CUBE      : trackersVisible = 9; break;
      default :
      case CURSOR_CROSSHAIR : trackersVisible = 3; break;
    }
  }
  /**
   * The style to display the cursor
   * @return the current mode
   */
  public int getCursorMode () { return this.cursorMode; }

  /**
   * Types of decoration displayed.One of the following
   * <ul>
   *   <li><b>DECORATION_NONE</b>: No decoration</li>
   *   <li><b>DECORATION_AXES</b>: Displays labelled axes</li>
   *   <li><b>DECORATION_CUBE</b>: Displays the boundig box</li>
   * </ul>
   * @param value the desired value
   */
  public void setDecorationType (int _value) { // Can't save time (see setDisplayMode)
    this.decorationType = _value;
    switch (_value) {
    case DECORATION_NONE :
      xAxis.setVisible(false); xText.setVisible(false);
      yAxis.setVisible(false); yText.setVisible(false);
      zAxis.setVisible(false); zText.setVisible(false);
      for (int i=0, n=boxSides.length; i<n; i++) boxSides[i].setVisible(false);
      break;
    case DECORATION_AXES :
      boolean showX = displayMode==DISPLAY_PLANAR_XY || displayMode==DISPLAY_PLANAR_XZ || displayMode>=DISPLAY_3D;
      boolean showY = displayMode==DISPLAY_PLANAR_XY || displayMode==DISPLAY_PLANAR_YZ || displayMode>=DISPLAY_3D;
      boolean showZ = displayMode==DISPLAY_PLANAR_YZ || displayMode==DISPLAY_PLANAR_XZ || displayMode>=DISPLAY_3D;
      xAxis.setVisible(showX); xText.setVisible(showX);
      yAxis.setVisible(showY); yText.setVisible(showY);
      zAxis.setVisible(showZ); zText.setVisible(showZ);
      for (int i=0, n=boxSides.length; i<n; i++) boxSides[i].setVisible(false);
      break;
    case DECORATION_CUBE :
      xAxis.setVisible(false); xText.setVisible(false);
      yAxis.setVisible(false); yText.setVisible(false);
      zAxis.setVisible(false); zText.setVisible(false);
      for (int i=0, n=boxSides.length; i<n; i++) boxSides[i].setVisible(true);
      break;
    }
    invalidateImage(); // validImage = false;
    if(!getIgnoreRepaint()) {
      repaint();
    }
  }
  /**
   * Type of decoration displayed
   * @return the current type
   */
  public int getDecorationType () { return this.decorationType; }

/**
 * Set the resolution of the lines and arrows in the decoration and cursor.
 * @param _res the desired Resolution
 * @see Resolution
 */
  public void setDecorationResolution(Resolution _res) {
    for (int i=0, n=boxSides.length; i<n; i++) boxSides[i].setResolution(_res);
    xAxis.setResolution(_res); yAxis.setResolution(_res); zAxis.setResolution(_res);
    for (int i=0, n=trackerLines.length; i<n; i++) trackerLines[i].setResolution(_res);
  }

  public void setAxesLabels (String[] _labels) {
   xText.getStyle().setDisplayObject(_labels[0]);
   yText.getStyle().setDisplayObject(_labels[1]);
   zText.getStyle().setDisplayObject(_labels[2]);
  }

  /**
   * Set the displacement of the center of the scene (in pixels) with respect to
   * the center of the panel. Default is 0,0.
   * @param _deltaa The horizontal displacement
   * @param _deltab The vertical displacement
   */
  public void setPan (int _deltaa, int _deltab) { this.deltaa = _deltaa; this.deltab = _deltab; computeConstants (5); }
  /**
   * Get the displacement of the center of the scene with respect to the center of the panel
   * @return the current displacement
   */
  public java.awt.Point getPan () { return new java.awt.Point(this.deltaa,this.deltab); }

  /**
   * Set the angle (in radians) to rotate the scene horizontally before projecting. Default is 0.0.
   * @param _alpha the desired angle
   */
  public void setAlpha (double _alpha) { this.alpha = _alpha; cosAlpha = Math.cos(alpha); sinAlpha = Math.sin(alpha);  reportTheNeedToProject(1); }

  /**
   * Get the angle (in degrees) to rotate the scene horizontally before projecting.
   * @return the current value
   */
  public double getAlpha () { return this.alpha; }
  /**
   * Set the angle (in radians) to rotate the scene vertically before projecting. Default is 0.0.
   * @param _beta the desired angle
   */
  public void setBeta (double _beta) { this.beta = _beta; cosBeta=Math.cos(beta); sinBeta=Math.sin(beta); reportTheNeedToProject(2); }
  /**
   * Get the angle (in radians) to rotate the scene vertically before projecting
   * @return the current value
   */
  public double getBeta () { return this.beta; }

  /**
   * Set the angles (in radians) to rotate the scene horizontally and vertically before projecting
   * @param _alpha the desired horizontal angle
   * @param _beta the desired vertical angle
   */
  public void setAlphaAndBeta (double _alpha, double _beta) {
    this.alpha = _alpha; cosAlpha = Math.cos(alpha); sinAlpha = Math.sin(alpha);
    this.beta  = _beta;  cosBeta  = Math.cos(beta);  sinBeta  = Math.sin(beta);
    reportTheNeedToProject(3);
  }

  /**
   * Set the magnifying factor to apply to the scene. Default is 1.0.
   * @param _zoom the desired value
   */
  public void setZoom (double _zoom) { this.zoom = _zoom; computeConstants (6); }
  /**
   * Get the magnifying factor applied to the scene.
   * @return the current value
   */
  public double getZoom () { return this.zoom; }

  /**
   * Set the ratio between the distance to the user's eye and the center of the scene
   * @param _value the desired value
   */
  public void setRatioToCenter (double _value) { this.ratioToCenter = _value; computeConstants(7); }
  /**
   * Get the ratio between the distance to the user's eye and the center of the scene
   * @return the current value
   */
  public double getRatioToCenter () { return this.ratioToCenter; }
  /**
   * Set the ratio between the distance to the user's eye and the projecting plane (actually, the screen)
   * @param _value the desired value
   */
  public void setRatioToPlane (double _value) { this.ratioToPlane = _value; computeConstants(8); }
  /**
   * Get the ratio between the distance to the user's eye and the projecting plane (actually, the screen)
   * @return the current value
   */
  public double getRatioToPlane () { return this.ratioToPlane; }

// ------------------------------------------------
//  Dealing with the new coordinate
// ------------------------------------------------

  /**
   * Set the extrema in the X, Y and Z coordinates at once
   */
  public void setPreferredMinMax(double _xmin, double _xmax, double _ymin, double _ymax, double _zmin, double _zmax) {
    super.setPreferredMinMax(_xmin,_xmax,_ymin,_ymax);
    this.setPreferredMinMaxZ(_zmin,_zmax);
  }

  /**
   * Sets the preferred scale in the Z direction.
   * @param _min the minimum value
   * @param _max the maximum value
   */
  public void setPreferredMinMaxZ(double _min, double _max) {
    if((this.zminPreferred==_min)&&(this.zmaxPreferred==_max)) {
      return;
    }
    if(Double.isNaN(_min)) {
      _min = this.yminPreferred;
    }
    if(Double.isNaN(_max)) {
      _max = this.ymaxPreferred;
    }
    autoscaleZ = false;
    if(_min==_max) {
      _min = 0.9*_min-0.5;
      _max = 1.1*_max+0.5;
    }
    this.zminPreferred = _min;
    this.zmaxPreferred = _max;
    invalidateImage(); // validImage = false;
  }

  /**
   * Gets the preferred maximum z world coordinate
   * @return zmaxPreferred
   */
  public double getPreferredZMax() {
    return this.zmaxPreferred;
  }
  /**
   * Gets the preferred minimum z world coordinate
   * @return zminPreferred
   */
  public double getPreferredZMin() {
    return this.zminPreferred;
  }
  /**
   * Get the minimum in the Z coordinate
   */
  public double getZMin() { return this.zmin; }
  /**
   * Get the maximum in the Z coordinate
   */
  public double getZMax() { return this.zmax; }
  /**
   * Limits the zmin and zmax values during autoscaling so that the mininimum value
   * will be no greater than the floor and the maximum value will be no
   * smaller than the ceil.
   *
   * Setting a floor or ceil value to <code>Double.NaN<\code> will disable that limit.
   *
   * @param _floor the zfloor value
   * @param _ceil the zceil value
   */
  public void limitAutoscaleZ(double _floor, double _ceil) {
    if(_ceil-_floor<Float.MIN_VALUE) { // insures that floor and ceiling some separation
      _floor = 0.9*_floor-Float.MIN_VALUE;
      _ceil = 1.1*_ceil+Float.MIN_VALUE;
    }
    this.zfloor = _floor;
    this.zceil  = _ceil;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets axis scales if autoscale is true using the max and min values of the objects in the given list.
   */
  protected void scale(ArrayList<Drawable> tempList) {
    super.scale(tempList);
    if(autoscaleZ) {
      scaleZ(tempList);
    }
  }

// -------------------------------------
// Implementation of InteractionSource
// -------------------------------------

  public void setEnabled (boolean _value) { this.respondToMouse = _value; } // Not yet implemented
  public boolean isEnabled () { return true; } // { return respondToMouse; }

  public void setEnabled (int _target, boolean _value) { this.respondToMouse = _value; } // Not yet implemented
  public boolean isEnabled (int _target) { return true; } // { return respondToMouse; }

  public void addListener (InteractionListener _listener) {
    if (_listener==null || listeners.contains(_listener)) return;
    listeners.add(_listener);
  }

  public void removeListener (InteractionListener _listener) { listeners.remove(_listener); }

  public void removeAllListeners () { listeners = new ArrayList<InteractionListener>(); }

  public void invokeActions (InteractionEvent _event) {
    Iterator<InteractionListener>  it = listeners.iterator();
    while(it.hasNext()) it.next().interactionPerformed (_event);
  }

// -------------------------------------
// Implementation changes for superclasses
// -------------------------------------

  public void setForeground (Color _color) {
    super.setForeground(_color);
    if (xAxis!=null) {
      for (int i=0, n=boxSides.length; i<n; i++) boxSides[i].getStyle().setEdgeColor(_color);
      xAxis.getStyle().setEdgeColor(_color); xText.getStyle().setEdgeColor(_color);
      yAxis.getStyle().setEdgeColor(_color); yText.getStyle().setEdgeColor(_color);
      zAxis.getStyle().setEdgeColor(_color); zText.getStyle().setEdgeColor(_color);
      for (int i=0, n=trackerLines.length; i<n; i++) trackerLines[i].getStyle().setEdgeColor(_color.brighter().brighter());
    }
  }

  // This method doesn't look very elegant, but ....
  public void setPixelScale() {
//    boolean anyChange = true;
    double xm = xmin, xM = xmax, ym = ymin, yM = ymax, zm = zmin, zM = zmax;
    xmin = xminPreferred; // start with the preferred values.
    xmax = xmaxPreferred;
    ymin = yminPreferred;
    ymax = ymaxPreferred;
    zmin = zminPreferred;
    zmax = zmaxPreferred;
//    width       = getWidth();
//    height      = getHeight();
//    xPixPerUnit = (width - leftGutter - rightGutter - 1) / (xmax - xmin);
//    yPixPerUnit = (height - bottomGutter - topGutter - 1) / (ymax - ymin);  // the y scale in pixels
//    if(squareAspect) {
//      double stretch = Math.abs(xPixPerUnit / yPixPerUnit);
//      if(stretch >= 1) {                                                        // make the x range bigger so that aspect ratio is one
//        stretch     = Math.min(stretch, width);                                 // limit the stretch
//        xmin        = xminPreferred - (xmaxPreferred - xminPreferred) * (stretch - 1) / 2.0;
//        xmax        = xmaxPreferred + (xmaxPreferred - xminPreferred) * (stretch - 1) / 2.0;
//        xPixPerUnit = (width - leftGutter - rightGutter - 1) / (xmax - xmin);   // the x scale in pixels per unit
//      } else {                                                                  // make the y range bigger so that aspect ratio is one
//        stretch     = Math.max(stretch, 1.0 / height);                          // limit the stretch
//        ymin        = yminPreferred - (ymaxPreferred - yminPreferred) * (1.0 / stretch - 1) / 2.0;
//        ymax        = ymaxPreferred + (ymaxPreferred - yminPreferred) * (1.0 / stretch - 1) / 2.0;
//        yPixPerUnit = (height - bottomGutter - topGutter - 1) / (ymax - ymin);  // the y scale in pixels per unit
//      }
//    }
    if (xm!=xmin || xM!=xmax || ym!=ymin || yM!=ymax || zm!=zmin || zM!=zmax) computeConstants(9); // Saves time
  }

  public void clear () {
    super.clear();  // line added by Wolfgang Christian
    for (int i=0, n=boxSides.length; i<n; i++) this.addDrawable(boxSides[i]);
    this.addDrawable(xAxis); this.addDrawable(xText);
    this.addDrawable(yAxis); this.addDrawable(yText);
    this.addDrawable(zAxis); this.addDrawable(zText);
    for (int i=0, n=trackerLines.length; i<n; i++) this.addDrawable(trackerLines[i]);
  }


  public void paintDrawableList(Graphics g, ArrayList<Drawable> tempList) {
    Graphics2D g2        = (Graphics2D) g;
    Iterator<Drawable> it = tempList.iterator();
    Shape      clipShape = g2.getClip();
    int w = getWidth()-leftGutter-rightGutter;
    int h = getHeight()-bottomGutter-topGutter;
    if((w<0)||(h<0)) {
      return;
    }
    if(clipAtGutter) {
      g2.clipRect(leftGutter, topGutter, w, h);
    }
    if (quickRedrawOn || !removeHiddenLines) { // Do a quick sketch of the scene
      while (it.hasNext()) {
        if(!isValidImage()) {
          break; // abort drawing
        }
        Drawable drawable = it.next();
        if (drawable instanceof Drawable3D) ((Drawable3D) drawable).drawQuickly(this,g2);
        else drawable.draw(this,g2);
      }
    }
    else { // synchronized(list3D)  { // Collect objects, sort and draw them one by one. Takes time!!!
      list3D.clear();
      while (it.hasNext()) {
        if(!isValidImage()) {
          break; // abort drawing
        }
        Drawable drawable = it.next();
        if (drawable instanceof Drawable3D) {
          Object3D[] objects = ((Drawable3D) drawable).getObjects3D(this);
          if (objects!=null)
            for (int i=0, n=objects.length; i<n; i++) {
              // providing NaN as distance can be used by Drawables3D to hide a given Object3D
              if (objects[i]!=null && !Double.isNaN(objects[i].distance)) list3D.add(objects[i]);
            }
        }
        else drawable.draw(this, g2); // Normal drawables are drawn BEFORE Drawables3D
      }
      if (list3D.size()>0) {
        Object[] objects = list3D.toArray();
        Arrays.sort(objects,comparator);
        for (int i=0, n=objects.length; i<n; i++) {
          Object3D obj = (Object3D) objects[i];
          obj.drawable3D.draw(this,g2,obj.index);
        }
      }
    }
    g2.setClip(clipShape);
  }

/**
 * Convert a 3D point of the scene into a 2D point of the screen. It can
 * compute simultaneously the equivalent in screen coordinates of a distance in
 * the scene. Finally, it also provides a number measuring the relative
 * distance of the point to the useTODr's viewpoint.
 * distance = 1.0 means at the center of the scene,
 * distance > 1.0 means farther than the center of the scene,
 * distance < 1.0 means closer than the center of the scene,
 * @param coordinate The coordinates of the point of the scene
 * <itemize>
 * <li>If the input array has length 2, it is considered a single 2D point x,y
 * <li>If the input array has length 3, it is considered a single 3D point x,y,z
 * <li>If the input array has length 4, it is considered a 2D point plus a 2D vector x,y, dx,dy
 * <li>If the input array has length 6, it is considered a 3D point plus a 3D vector x,y,z, dx,dy,dz
 * </itemize>
 * @param pixel a place-holder for the coordinates of the point of the screen
 * <itemize>
 * <li>If the input array had length 2 or 3, it returns a,b and the distance
 * <li>If the input array had length 4 or 6, it returns a,b, da,db and the distance
 * </itemize>
 * @return The coordinates of the point of the screen
 */
  public double[] project (double[] coordinate, double[] pixel) {
    double x = coordinate[0] - centerX, y = coordinate[1] - centerY, z = 0.0;
    double xprime, yprime, zprime, factor;
    switch (coordinate.length) {
      case 2: // Input is x,y
      case 4 : // Input is x,y,dx,dy
        z = 0.0;
        break;
      case 3: // Input is x,y,z
      case 6 : // Input is x,y,z,dx,dy,dz
        z = coordinate[2] - centerZ;
        break;
      default :
        throw new IllegalArgumentException("Method project not supported for this length.");
    }
    switch (displayMode) {
      case DISPLAY_PLANAR_XY : xprime = z; yprime = x; zprime = y; factor = 1.8; break;
      case DISPLAY_PLANAR_XZ : xprime = y; yprime = x; zprime = z; factor = 1.8; break;
      case DISPLAY_PLANAR_YZ : xprime = x; yprime = y; zprime = z; factor = 1.8; break;
      case DISPLAY_NO_PERSPECTIVE :
        xprime =  x*cosAlpha + y*sinAlpha;
        yprime = -x*sinAlpha + y*cosAlpha;
        zprime = -xprime*sinBeta + z*cosBeta;
        xprime =  xprime*cosBeta + z*sinBeta;
        factor = 1.3;
        break;
      default :
      case DISPLAY_PERSPECTIVE :
        xprime =  x*cosAlpha + y*sinAlpha;
        yprime = -x*sinAlpha + y*cosAlpha;
        zprime = -xprime*sinBeta + z*cosBeta;
        xprime =  xprime*cosBeta + z*sinBeta;
        double aux = viewToCenter-xprime;
        if (Math.abs(aux)<1.0e-2) aux = 1.0e-2;  // This is to avoid division by zero
        factor = viewToPlane /aux; // (viewToCenter-xprime);
        break;
    }
    pixel[0] = acenter + yprime*factor*aconstant;
    pixel[1] = bcenter - zprime*factor*bconstant;
    switch (coordinate.length) {
      case 2: // Input is x,y
      case 3: // Input is x,y,z
        pixel[2] = (viewToCenter-xprime)/viewToCenter;  // A number reporting about the distance to us
        break;
      case 4 : // Input is x,y,dx,dy
        pixel[2] = coordinate[2]*factor*aconstant;
        pixel[3] = coordinate[3]*factor*bconstant;
        pixel[4] = (viewToCenter-xprime)/viewToCenter; // A number reporting about the distance to us
        break;
      case 6 : // Input is x,y,z,dx,dy,dz
        switch (displayMode) {
          case DISPLAY_PLANAR_XY : pixel[2] = coordinate[3]*factor*aconstant; pixel[3] = coordinate[4]*factor*bconstant; break; // dx,dy are used
          case DISPLAY_PLANAR_XZ : pixel[2] = coordinate[3]*factor*aconstant; pixel[3] = coordinate[5]*factor*bconstant; break; // dx,dz are used
          case DISPLAY_PLANAR_YZ : pixel[2] = coordinate[4]*factor*aconstant; pixel[3] = coordinate[5]*factor*bconstant; break; // dy,dz are used
          default : /* 3D */       pixel[2] = Math.max(coordinate[3],coordinate[4])*factor*aconstant; pixel[3] = coordinate[5]*factor*bconstant; break;
          // max(dx,dy) and dz are used
        }
        pixel[4] = (viewToCenter-xprime)/viewToCenter;  // A number reporting about the distance to us
        break;
    }
    return pixel;
  }

// -------------------------------------
//    Other methods
// -------------------------------------

  private float[] crc = new float[4]; // Stands for ColorRGBComponent

  /**
   * Compute the display color of a given drawable3D based on its original color and its depth.
   * Transparency of the original color is not affected.
   * @param java.awt.Color _aColor the original color
   * @param _depth the depth value of the color
   */
  public Color projectColor (Color _aColor, double _depth) {
//    if (_aColor==null) return null;
    if (!useColorDepth) return _aColor;
//    if      (_depth<0.9) return _aColor.brighter().brighter();
//    else if (_depth>1.1) return _aColor.darker().darker();
//    else return _aColor;
    try {
      _aColor.getRGBComponents(crc);
      // Do not affect transparency
      for (int i=0; i<3; i++) { crc[i] /= _depth; crc[i] = (float) Math.max(Math.min(crc[i],1.0),0.0); }
      return new Color(crc[0],crc[1],crc[2],crc[3]);
    } catch (Exception _exc)  { return _aColor; }
  }

  /**
   * Converts a point on the screen into a world point
   * Private because it only works properly for planar display modes
   */
  private Point3D worldPoint (int a, int b) {
    double factor=1.8;
    switch (displayMode) {
      case DISPLAY_PLANAR_XY : return new Point3D (centerX+(a-acenter)/(factor*aconstant),centerY+(bcenter-b)/(factor*bconstant),zmax);
      case DISPLAY_PLANAR_XZ : return new Point3D (centerX+(a-acenter)/(factor*aconstant),ymax,centerZ+(bcenter-b)/(factor*bconstant));
      case DISPLAY_PLANAR_YZ : return new Point3D (xmax,centerY+(a-acenter)/(factor*aconstant),centerZ+(bcenter-b)/(factor*bconstant));
      default : /* 3D */       return new Point3D((xmin+xmax)/2,(ymin+ymax)/2,(zmin+zmax)/2);
    }
  }

  /**
   * Converts into a world distance a distance on the screen
   * Private because it only works properly for planar display modes
   */
  private Point3D worldDistance (int dx, int dy) {
    double factor=1.8;
    switch (displayMode) {
      case DISPLAY_PLANAR_XY : return new Point3D (dx/(factor*aconstant),-dy/(factor*bconstant),0.0);
      case DISPLAY_PLANAR_XZ : return new Point3D (dx/(factor*aconstant),0.0,-dy/(factor*bconstant));
      case DISPLAY_PLANAR_YZ : return new Point3D (0.0,dx/(factor*aconstant),-dy/(factor*bconstant));
      default : /* 3D */       return new Point3D (dx/(1.3*aconstant),dy/(1.3*bconstant),0.0);
    }
  }

// -------------------------------------
//    Interaction
// -------------------------------------

  // Variables for interaction
  private int lastX=0, lastY=0;
  private InteractionTarget targetHit=null;
  private Interactive iad=null;

  // This is so that the panel accepts KeyEvents
  // Deprecated in 1.4 but can be implemented for 1.3 compatibility
  //public boolean isFocusTraversable () { return true; }

  // This is so that the panel accepts KeyEvents
  public boolean isFocusable () { return true; }

//  public int getKeyPressed () { return keyPressed; }

  // returns true if the tracker was moved
  private boolean mouseDraggedComputations(java.awt.event.MouseEvent e) {
    if (e.isControlDown ())    { setPan (deltaa + (e.getX()-lastX), deltab + (e.getY()-lastY)); return false; } // Panning
    else if (e.isShiftDown ()) { setZoom (zoom - (e.getY()-lastY)*0.01); return false; } // Zooming
    else if (displayMode>=DISPLAY_3D && iad==null && !e.isAltDown()) { // Rotating (in 3D)
      setAlphaAndBeta(alpha - (e.getX()-lastX)*0.01, beta  + (e.getY()-lastY)*0.01);
      return false;
    }
    // In all other cases, you are moving the tracker
    Point3D point = worldDistance(e.getX()-lastX,e.getY()-lastY);
    if (displayMode<DISPLAY_3D) {
      switch (keyPressed) {
        case 88 : trackerPoint.x += point.x; break; // X is pressed
        case 89 : trackerPoint.y += point.y; break; // Y is pressed
        case 90 : trackerPoint.z += point.z; break; // Z is pressed
        default : trackerPoint.x += point.x; trackerPoint.y += point.y; trackerPoint.z += point.z; break; // No key is pressed
      }
    }
    else { // all 3D cases
      int factor = 1;
      if (cosBeta<0) factor = -1;
      switch (keyPressed) {
        case 88 : // X is pressed
          if      (cosAlpha>=0 && Math.abs(sinAlpha)<cosAlpha)  trackerPoint.x += point.y;
          else if (sinAlpha>=0 && Math.abs(cosAlpha)<sinAlpha)  trackerPoint.x -= point.x;
          else if (cosAlpha<0  && Math.abs(sinAlpha)<-cosAlpha) trackerPoint.x -= point.y;
          else                                                  trackerPoint.x += point.x;
          break;
        case 89 : // Y is pressed
          if      (cosAlpha>=0 && Math.abs(sinAlpha)<cosAlpha)  trackerPoint.y += point.x;
          else if (sinAlpha>=0 && Math.abs(cosAlpha)<sinAlpha)  trackerPoint.y += point.y;
          else if (cosAlpha<0  && Math.abs(sinAlpha)<-cosAlpha) trackerPoint.y -= point.x;
          else                                                  trackerPoint.y -= point.y;
          break;
        case 90 : // Z is pressed
          if (cosBeta>=0) trackerPoint.z -= point.y; else trackerPoint.z += point.y;
          break;
        default : // No key is pressed
          trackerPoint.z -= factor*point.y;
          if      (cosAlpha>=0 && Math.abs(sinAlpha)<cosAlpha)  trackerPoint.y += point.x;
          else if (sinAlpha>=0 && Math.abs(cosAlpha)<sinAlpha)  trackerPoint.x -= point.x;
          else if (cosAlpha<0  && Math.abs(sinAlpha)<-cosAlpha) trackerPoint.y -= point.x;
          else                                                  trackerPoint.x += point.x;
          break;
      }
    }
    return true;
  }

// ------------------------------------------
// Implementation of InteractiveMouseHandler
// ------------------------------------------

/* TODO : overwrite findInteractive so that it checks first objects that are closer */

  private void resetInteraction (InteractivePanel _panel) {
    iad=null;
    targetHit=null;
    showTrackers(false);
    blMessageBox.setText(null);
    repaint();
  }

  public void handleMouseAction(InteractivePanel _panel, java.awt.event.MouseEvent _evt) {
    switch (_panel.getMouseAction ()) {
      case InteractivePanel.MOUSE_PRESSED :
        requestFocus();
        if (_evt.isPopupTrigger() || _evt.getModifiers() == java.awt.event.InputEvent.BUTTON3_MASK) return;
//        if (allowQuickRedraw && ((_evt.getModifiers()&java.awt.event.InputEvent.BUTTON1_MASK)!=0)) quickRedrawOn = true;
//        else quickRedrawOn = false;
        lastX = _evt.getX(); lastY = _evt.getY();
        targetHit = null;
        iad = _panel.getInteractive ();
        if (iad instanceof InteractionTarget) {
          targetHit = (InteractionTarget) iad;
          trackerPoint = targetHit.getHotspot(_panel);
//          displayTrackerPosition(); // Will be changed too quickly to see this one
          targetHit.getSource().invokeActions (new InteractionEvent (targetHit.getSource(),InteractionEvent.MOUSE_PRESSED,null,targetHit));
//          invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_PRESSED,null,targetHit));
          trackerPoint=targetHit.getHotspot(_panel); // because the listener may change the position of the element
        }
        else if (iad!=null) { // a non-3D interactive has been hit
          if (!iad.isEnabled()) return;
          switch (displayMode) {
            default : /* 3D */
            case DISPLAY_PLANAR_XY : trackerPoint.x = iad.getX();    trackerPoint.y = iad.getY();    trackerPoint.z = zmax; break;
            case DISPLAY_PLANAR_XZ : trackerPoint.x = iad.getX();    trackerPoint.z = iad.getY();    trackerPoint.y = ymax; break;
            case DISPLAY_PLANAR_YZ : trackerPoint.y = iad.getX();    trackerPoint.z = iad.getY();    trackerPoint.x = xmax; break;
          }
          invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_PRESSED,null,iad));
          switch (displayMode) { // because the listener may change the position of the interactive
            default : /* 3D */
            case DISPLAY_PLANAR_XY : trackerPoint.x = iad.getX(); trackerPoint.y = iad.getY(); trackerPoint.z = zmax; break;
            case DISPLAY_PLANAR_XZ : trackerPoint.x = iad.getX(); trackerPoint.z = iad.getY(); trackerPoint.y = ymax; break;
            case DISPLAY_PLANAR_YZ : trackerPoint.y = iad.getX(); trackerPoint.z = iad.getY(); trackerPoint.x = xmax; break;
          }
        }
        else { // No interactive has been hit
          if (displayMode<DISPLAY_3D || _evt.isAltDown()) { // In 2D by default, in 3D only if you hold ALT down
            // You are trying to track a given point
            trackerPoint = worldPoint(_evt.getX(),_evt.getY());
            invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_PRESSED,null,trackerPoint));
          }
          else {
            invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_PRESSED,null,null));
            resetInteraction(_panel);
            return;
          }
        }
        if (showPosition) displayPosition(trackerPoint.x,trackerPoint.y,trackerPoint.z);
        positionTrackers();
        showTrackers (true); // should trackers appear only in 3D mode?
        _panel.repaint();
        break;
      case InteractivePanel.MOUSE_DRAGGED :
        if (_evt.isPopupTrigger() || _evt.getModifiers() == java.awt.event.InputEvent.BUTTON3_MASK) return;
        if (iad!=null && iad.isEnabled()==false) return;
        quickRedrawOn = allowQuickRedraw && keyPressed!=83;
        boolean trackerMoved = mouseDraggedComputations(_evt);
        lastX = _evt.getX(); lastY = _evt.getY();
        if (!trackerMoved) { // Report any listener that the projection has changed. Data is NULL!
          invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_DRAGGED,null,null));
          resetInteraction(_panel);
          return;
        }
        if (targetHit!=null) {
          targetHit.updateHotspot(_panel,trackerPoint);
          targetHit.getSource().invokeActions (new InteractionEvent (targetHit.getSource(),InteractionEvent.MOUSE_DRAGGED,null,targetHit));
//          invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_DRAGGED,null,targetHit));
          trackerPoint = targetHit.getHotspot(_panel); // The listener may change the position of the element
        }
        else if (iad!=null) invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_DRAGGED,null,iad));
        else invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_DRAGGED,null,trackerPoint));
        if (showPosition) displayPosition(trackerPoint.x,trackerPoint.y,trackerPoint.z);
        positionTrackers();
        showTrackers (true); // should trackers appear only in 3D mode?
        _panel.repaint ();
        break;
      case InteractivePanel.MOUSE_RELEASED :
        if (_evt.isPopupTrigger() || _evt.getModifiers() == java.awt.event.InputEvent.BUTTON3_MASK) return;
        if (iad!=null && iad.isEnabled()==false) return;
        if (targetHit!=null) {
          targetHit.getSource().invokeActions (new InteractionEvent (targetHit.getSource(),InteractionEvent.MOUSE_RELEASED,null,targetHit));
//          invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_RELEASED,null,targetHit));
        }
        else if (iad!=null) invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_RELEASED,null,iad));
        else {
          if (displayMode<DISPLAY_3D || _evt.isAltDown())
            invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_RELEASED,null,trackerPoint));
          else
            invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_RELEASED,null,null));
        }
        quickRedrawOn = false;
        resetInteraction(_panel);
        break;
      case InteractivePanel.MOUSE_EXITED : // TODO : Should panel invoke actions here?
        invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_EXITED,null,null));
//        resetInteraction(_panel);
//        quickRedrawOn = false;
        break;
      case InteractivePanel.MOUSE_ENTERED : // TODO : Should panel invoke actions here?
        invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_ENTERED,null,null));
//        resetInteraction(_panel);
//        quickRedrawOn = false;
        break;
      case InteractivePanel.MOUSE_MOVED :  // TODO : Should panel invoke actions here?
        if (_panel.getInteractive()!=null) _panel.setMouseCursor (java.awt.Cursor.getPredefinedCursor (java.awt.Cursor.HAND_CURSOR));
        else _panel.setMouseCursor (java.awt.Cursor.getPredefinedCursor (java.awt.Cursor.CROSSHAIR_CURSOR));
        invokeActions (new InteractionEvent (this,InteractionEvent.MOUSE_MOVED,null,null));
//        resetInteraction(_panel); // Unnecesarily repaints the panel
        break;
    }
  }

// -------------------------------------
//    Private methods
// -------------------------------------

  @SuppressWarnings("unchecked")
  private void reportTheNeedToProject(int _count) {
//    System.out.println("Reporting the need to project "+_count);
    ArrayList<Drawable> tempList = (ArrayList<Drawable>) drawableList.clone();
    Iterator<Drawable>  it = tempList.iterator();
    while (it.hasNext()) {
      Object drawable = it.next();
      if (drawable instanceof Drawable3D) ((Drawable3D) drawable).needsToProject(this);
    }
  }

  private void computeConstants (int _caller) {
//    System.out.println ("Computing constants "+_caller);
//    System.out.println ("xmin="+xmin+", xmax="+xmax);
//    System.out.println ("ymin="+ymin+", ymax="+ymax);
//    System.out.println ("zmin="+zmin+", zmax="+zmax);
//    System.out.println ("xminPreferred="+xminPreferred+", xmaxPreferred="+xmaxPreferred);
//    System.out.println ("yminPreferred="+yminPreferred+", ymaxPreferred="+ymaxPreferred);
//    System.out.println ("zminPreferred="+zminPreferred+", zmaxPreferred="+zmaxPreferred);

    int thisWidth  = this.getWidth(), thisHeight = this.getHeight();
    acenter = deltaa+thisWidth/2; bcenter = deltab+thisHeight/2;
    if (squareAspect) thisWidth = thisHeight = Math.min(thisWidth,thisHeight);
    double dx = xmax-xmin, dy = ymax-ymin, dz = zmax-zmin;
    double maxSpace;
    switch (displayMode) {
      case DISPLAY_PLANAR_XY : maxSpace = Math.max(dx,dy); break;
      case DISPLAY_PLANAR_XZ : maxSpace = Math.max(dx,dz); break;
      case DISPLAY_PLANAR_YZ : maxSpace = Math.max(dy,dz); break;
      default : /* 3D */       maxSpace = Math.max(Math.max(dx,dy),dz); break;
    }
    centerX = (xmax+xmin)/2.0; centerY = (ymax+ymin)/2.0; centerZ = (zmax+zmin)/2.0;
    aconstant = 0.5*zoom*thisWidth/maxSpace; bconstant = 0.5*zoom*thisHeight/maxSpace;
    viewToPlane = ratioToPlane*maxSpace;
    viewToCenter = ratioToCenter*maxSpace;
    resetDecoration(dx,dy,dz);
    reportTheNeedToProject(3);
  }

  private void scaleZ(ArrayList<Drawable> tempList) {  // Copied from DrawingPanel
    double    newZMin         = Double.MAX_VALUE;
    double    newZMax         = -Double.MAX_VALUE;
    boolean   measurableFound = false;
    Iterator<Drawable>  it              = tempList.iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(!(obj instanceof Measurable3D)) {
        continue;                // object is not measurable3D
      }
      Measurable3D measurable = (Measurable3D) obj;
      if(!measurable.isMeasured()) {
        continue;                // objects' measure not yet set
      }
      if(!Double.isNaN(measurable.getZMax()) &&!Double.isNaN(measurable.getZMin())) {
        newZMin         = Math.min(newZMin, measurable.getZMin());
        newZMin         = Math.min(newZMin, measurable.getZMax());
        newZMax         = Math.max(newZMax, measurable.getZMax());
        newZMax         = Math.max(newZMax, measurable.getZMin());
        measurableFound = true;  // we have at least one valid min-max meausre
      }
    }
    // do not change change values unless there is at least one measurable object.
    if(measurableFound) {
      if(newZMin == newZMax) {  //bracket the value
        newZMin = 0.9 * newZMin - 0.5;
        newZMax = 1.1 * newZMax + 0.5;
      }
      double range = newZMax - newZMin;
      zminPreferred = newZMin - autoscaleMargin * range;
      zmaxPreferred = newZMax + autoscaleMargin * range;
    }
    if(!Double.isNaN(zfloor)) {
      zminPreferred = Math.min(zfloor, zminPreferred);
    }
    if(!Double.isNaN(zceil)) {
      zmaxPreferred = Math.max(zceil, zmaxPreferred);
    }
  }

  private void displayPosition (double x, double y, double z) {
    switch (displayMode) {
      case DISPLAY_PLANAR_XY : blMessageBox.setText (xFormat.format(x) + " " + yFormat.format(y)); break;
      case DISPLAY_PLANAR_XZ : blMessageBox.setText (xFormat.format(x) + " " + zFormat.format(z)); break;
      case DISPLAY_PLANAR_YZ : blMessageBox.setText (yFormat.format(y) + " " + zFormat.format(z)); break;
      default : /* 3D */       blMessageBox.setText (xFormat.format(x) + " " + yFormat.format(y) + " " + zFormat.format(z)); break;
    }
  }

  /**
   * Whether to display the X, Y and Z axes
   */
  private void showTrackers (boolean value) {
    for (int i=0, n=trackerLines.length; i<n; i++) {
      if (i<trackersVisible) trackerLines[i].setVisible(value);
      else trackerLines[i].setVisible(false);
    }
  }

  private void positionTrackers () {
//    I commented this out because it displaces the cursor (specially in 2D modes) when it crosses the axes
//    if (trackerPoint.x<xmin) trackerPoint.x = xmin; else if (trackerPoint.x>xmax) trackerPoint.x = xmax;
//    if (trackerPoint.y<ymin) trackerPoint.y = ymin; else if (trackerPoint.y>ymax) trackerPoint.y = ymax;
//    if (trackerPoint.z<zmin) trackerPoint.z = zmin; else if (trackerPoint.z>zmax) trackerPoint.z = zmax;
    switch (cursorMode) {
      case CURSOR_NONE : return;
      default :
      case CURSOR_XYZ  :
        trackerLines[0].setXYZ(trackerPoint.x,ymin,zmin);           trackerLines[0].setSizeXYZ(0,trackerPoint.y-ymin,0);
        trackerLines[1].setXYZ(xmin,trackerPoint.y,zmin);           trackerLines[1].setSizeXYZ(trackerPoint.x-xmin,0,0);
        trackerLines[2].setXYZ(trackerPoint.x,trackerPoint.y,zmin); trackerLines[2].setSizeXYZ(0,0,trackerPoint.z-zmin);
        break;
      case CURSOR_CUBE :
        trackerLines[0].setXYZ(xmin,trackerPoint.y,trackerPoint.z); trackerLines[0].setSizeXYZ(trackerPoint.x-xmin,0,0);
        trackerLines[1].setXYZ(trackerPoint.x,ymin,trackerPoint.z); trackerLines[1].setSizeXYZ(0,trackerPoint.y-ymin,0);
        trackerLines[2].setXYZ(trackerPoint.x,trackerPoint.y,zmin); trackerLines[2].setSizeXYZ(0,0,trackerPoint.z-zmin);
        trackerLines[3].setXYZ(trackerPoint.x,ymin,zmin);            trackerLines[3].setSizeXYZ(0,trackerPoint.y-ymin,0);
        trackerLines[4].setXYZ(xmin,trackerPoint.y,zmin);            trackerLines[4].setSizeXYZ(trackerPoint.x-xmin,0,0);
        trackerLines[5].setXYZ(trackerPoint.x,ymin,zmin);            trackerLines[5].setSizeXYZ(0,0,trackerPoint.z-zmin);
        trackerLines[6].setXYZ(xmin,ymin,trackerPoint.z);            trackerLines[6].setSizeXYZ(trackerPoint.x-xmin,0,0);
        trackerLines[7].setXYZ(xmin,trackerPoint.y,zmin);            trackerLines[7].setSizeXYZ(0,0,trackerPoint.z-zmin);
        trackerLines[8].setXYZ(xmin,ymin,trackerPoint.z);            trackerLines[8].setSizeXYZ(0,trackerPoint.y-ymin,0);
        break;
      case CURSOR_CROSSHAIR :
        trackerLines[0].setXYZ(xmin,trackerPoint.y,trackerPoint.z);  trackerLines[0].setSizeXYZ(xmax-xmin,0.0,0.0);
        trackerLines[1].setXYZ(trackerPoint.x,ymin,trackerPoint.z);  trackerLines[1].setSizeXYZ(0.0,ymax-ymin,0.0);
        trackerLines[2].setXYZ(trackerPoint.x,trackerPoint.y,zmin);  trackerLines[2].setSizeXYZ(0.0,0.0,zmax-zmin);
        break;
    }
  }

  private void resetDecoration (double _dx, double _dy, double _dz) {
    if (boxSides==null || boxSides[0]==null) return;
    boxSides[ 0].setXYZ(xmin,ymin,zmin); boxSides[ 0].setSizeXYZ(_dx,0.0,0.0);
    boxSides[ 1].setXYZ(xmax,ymin,zmin); boxSides[ 1].setSizeXYZ(0.0,_dy,0.0);
    boxSides[ 2].setXYZ(xmin,ymax,zmin); boxSides[ 2].setSizeXYZ(_dx,0.0,0.0);
    boxSides[ 3].setXYZ(xmin,ymin,zmin); boxSides[ 3].setSizeXYZ(0.0,_dy,0.0);
    boxSides[ 4].setXYZ(xmin,ymin,zmax); boxSides[ 4].setSizeXYZ(_dx,0.0,0.0);
    boxSides[ 5].setXYZ(xmax,ymin,zmax); boxSides[ 5].setSizeXYZ(0.0,_dy,0.0);
    boxSides[ 6].setXYZ(xmin,ymax,zmax); boxSides[ 6].setSizeXYZ(_dx,0.0,0.0);
    boxSides[ 7].setXYZ(xmin,ymin,zmax); boxSides[ 7].setSizeXYZ(0.0,_dy,0.0);
    boxSides[ 8].setXYZ(xmin,ymin,zmin); boxSides[ 8].setSizeXYZ(0.0,0.0,_dz);
    boxSides[ 9].setXYZ(xmax,ymin,zmin); boxSides[ 9].setSizeXYZ(0.0,0.0,_dz);
    boxSides[10].setXYZ(xmax,ymax,zmin); boxSides[10].setSizeXYZ(0.0,0.0,_dz);
    boxSides[11].setXYZ(xmin,ymax,zmin); boxSides[11].setSizeXYZ(0.0,0.0,_dz);
    xAxis.setXYZ(xmin,ymin,zmin); xAxis.setSizeXYZ(_dx,0.0,0.0); xText.setXYZ(xmax+_dx*0.02,ymin,zmin);
    yAxis.setXYZ(xmin,ymin,zmin); yAxis.setSizeXYZ(0.0,_dy,0.0); yText.setXYZ(xmin,ymax+_dx*0.02,zmin);
    zAxis.setXYZ(xmin,ymin,zmin); zAxis.setSizeXYZ(0.0,0.0,_dz); zText.setXYZ(xmin,ymin,zmax+_dx*0.02);
  }

}  // End of class DrawingPanel3D

class Comparator3D implements java.util.Comparator<Object> {
  public int compare(Object o1, Object o2) {
    try {
      if      ( ((Object3D)o1).distance > ((Object3D)o2).distance) return -1;
      else if ( ((Object3D)o1).distance < ((Object3D)o2).distance) return +1;
      else return 0;
    } catch (Exception _e) { return 0; } // Sometimes a NullPointerException happens
  }
}
