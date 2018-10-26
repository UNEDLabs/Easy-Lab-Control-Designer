/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.util.*;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.drawing2d.interaction.*;

/**
 * <p>Title: Element</p>
 * <p>Description: A basic individual, interactive drawing element.</p>
 * <p>Interaction: An Element includes the following targets:</p>
 * <ul>
 *   <li> TARGET_POSITION : Allows the element to be repositioned
 *   <li> TARGET_SIZE : Allows the element to be resized
 * </ul>
 * The actual position (and implementation) of the target depends on the
 * element.
 * @author Francisco Esquembre
 * @version July 2008
 */
public abstract class Element implements org.opensourcephysics.display.Interactive,
                                         org.opensourcephysics.display.LogMeasurable {

  /**
   * The id for the target that allows to reposition the element.
   */
  static public final int TARGET_POSITION = 0;

  /**
   * The id for the target that allows to resize the element.
   */
  static public final int TARGET_SIZE = 1;
  
  static final protected double[] STD_ORIGIN = new double[]{0,0};
  static final protected double[] STD_END    = new double[]{1,1};

  // Configuration variables
  private boolean visible = true; // visibility of the element
  private double x = 0, y = 0;    // position of the element
  private double sizeX = 1, sizeY = 1; // the size of the element in each dimension
  private String name = "unnamed"; //$NON-NLS-1$
  private Style style = new Style(this);
  private Group group = null;
  private Object dataObject=null;
  private AffineTransform transformation=new AffineTransform();

  // Implementation variables
  private boolean elementChanged = true; // The element changed, the transformation needs to be updated
  private boolean needsToProject = true; // The drawing panel changed forcing the element to recompute the transformations
  protected boolean canBeMeasured = true; // Whether 
  private DrawingPanel panel;
  private double[] corners = new double[8]; // The coordinates of the four points of the bounding rectangle of the eleemnt
  private AffineTransform totalTransformation=new AffineTransform();

  private double xmax;  // the maximum x value 
  private double ymax;  // the maximum y value 
  private double xmin;  // the minimum x value 
  private double ymin;  // the minimum y value 

  private double xmaxLogscale;  // the maximum x value when using a log scale
  private double ymaxLogscale;  // the maximum y value when using a log scale
  private double xminLogscale;  // the minimum x value when using a log scale
  private double yminLogscale;  // the minimum y value when using a log scale

  // Variables for interaction
  private List<InteractionListener> listeners       = new ArrayList<InteractionListener>();
  protected final InteractionTarget targetPosition  = new InteractionTarget(this, TARGET_POSITION);
  protected final InteractionTarget targetSize      = new InteractionTarget(this, TARGET_SIZE);

  /**
   * A place holder for data objects
   * To be used internally by ControlElements
   * @param _object Object
   */
  final public void setDataObject(Object _object) {
    this.dataObject = _object;
  }

  /**
   * Returns the data object
   * @return Object
   */
  final public Object getDataObject() {
    return this.dataObject;
  }
  
  // ----------------------------------------
  // Panel and group
  // ----------------------------------------

  /**
   * To be used internally by DrawingPanel only! Sets the panel for this element.
   * @param _panel DrawingPanel
   */
  final public void setPanel(DrawingPanel _panel) {
    this.panel = _panel;
    needsToProject = true;
  }

  /**
   * Returns the DrawingPanel in which it (or its final ancestor group) is displayed.
   * @return org.opensourcephysics.drawing.core.DrawingPanel
   */
  final public DrawingPanel getPanel() {
    Element el = this;
    while (el.group!=null) el = el.group;
    return el.panel;
  }

  /**
   * To be used internally by Group only! Sets the group of this element.
   * @param _group Group
   */
  final protected void setGroup(Group _group) {
    this.group = _group;
    elementChanged = true;
  }

  final protected Group getGroup() {
    return this.group; 
  }

  // ----------------------------------------
  // Name of the element
  // ----------------------------------------
  
  /**
   * Gives a name to the element.
   * Naming an element is optional, but the element may use its name to identify itself.
   * @param name String
   */
  final public void setName(String _aName) {
    this.name = _aName;
  }

  /**
   * Gets the name of the element
   * @return String the name
   */
  final public String getName() {
    return this.name;
  }

  // ----------------------------------------
  // Position of the element
  // ----------------------------------------
  
  /**
   * Set the X coordinate of the element
   * @param x double
   */
  final public void setX(double _x) {
    this.x = _x;
    elementChanged = true;
  }

  /**
   * Get the X coordinate of the element
   * @return double
   */
  final public double getX() {
    return this.x;
  }

  /**
   * Set the Y coordinate of the element
   * @param y double
   */
  final public void setY(double _y) {
    this.y = _y;
    elementChanged = true;
  }

  /**
   * Get the Y coordinate of the element
   * @return double
   */
  final public double getY() {
    return this.y;
  }

  /**
   * Set the coordinates of the element
   * @param _x double the x position
   * @param _y double the y position
   */
  final public void setXY(double _x, double _y) { // required by Measurable (in Interactive)
    this.x = _x;
    this.y = _y;
    elementChanged = true;
  }
  
  /**
   * Set the coordinates of the element
   * @param pos double[] an array of dimension 2
   */
  final public void setPosition(double[] _pos) {
    this.x = _pos[0];
    this.y = _pos[1];
    elementChanged = true;
  }

  /**
   * Get the coordinates of the element
   * @return double[]
   */
  final public double[] getPosition() {
    return new double[] {this.x, this.y};
  }

  // ----------------------------------------
  // Size of the element
  // ----------------------------------------
  
  /**
   * Set the size along the X axis
   * @param sizeX double
   */
  public void setSizeX(double _sizeX) {
    this.sizeX = _sizeX;
    elementChanged = true;
  }

  /**
   * Get the size along the X axis
   * @return double
   */
  final public double getSizeX() {
    return this.sizeX;
  }

  /**
   * Set the size along the Y axis
   * @param sizeY double
   */
  public void setSizeY(double _sizeY) {
    this.sizeY = _sizeY;
    elementChanged = true;
  }

  /**
   * Get the size along the Y axis
   * @return double
   */
  final public double getSizeY() {
    return this.sizeY;
  }

  /**
   * Set the size along the axes
   * @param sizeX double the size along the X axis
   * @param sizeY double the size along the Y axis
   */
  public void setSizeXY(double _sizeX, double _sizeY) {
    this.sizeX = _sizeX;
    this.sizeY = _sizeY;
    elementChanged = true;
  }

  /**
   * Set the size along the axes
   * @param size double[] an array of the dimension 2
   */
  public void setSize(double[] _size) {
    this.sizeX = _size[0];
    this.sizeY = _size[1];
    elementChanged = true;
  }

  /**
   * Get the size along the axes
   * @return double[]
   */
  final public double[] getSize() {
    return new double[] { this.sizeX, this.sizeY };
  }

  // ----------------------------------------
  // Measurements
  // ----------------------------------------

  /**
   * Whether the element is taken into account when autoscaling
   * @param measured boolean
   */
  public void setCanBeMeasured(boolean measured) { 
    canBeMeasured = measured; 
  }

  /**
   * The element measurable flag. The actuable measurabilty may depend on other things, such as a trail with no points
   * @return boolean the measurability flag as set by the user
   */
  public boolean getCanBeMeasured() { 
    return canBeMeasured; 
  }

  /**
   * Returns the diagonal size of the element, i.e., Math.sqrt(sizeX*sizeX+sizeY*sizeY)
   * @return double
   */
  final protected double getDiagonalSize() {
    return Math.sqrt(sizeX*sizeX+sizeY*sizeY);
  }

  public double getXMin() { // required by Measurable (in Interactive)
    updateExtrema();
    return xmin;
  }

  public double getXMax() { // required by Measurable (in Interactive)
    updateExtrema();
    return xmax;
  }
  
  public double getYMin() { // required by Measurable (in Interactive)
    updateExtrema();
    return ymin;
  }
  
  public double getYMax() { // required by Measurable (in Interactive)
    updateExtrema();
    return ymax;
  }
  
  public double getXMinLogscale(){
    updateExtrema();
    return xminLogscale;
  }

  public double getXMaxLogscale(){
    updateExtrema();
    return xmaxLogscale;
  }
  
  public double getYMinLogscale(){
    updateExtrema();
    return yminLogscale;
  }

  public double getYMaxLogscale(){
    updateExtrema();
    return ymaxLogscale;
  }

  /**
   * Returns the corners of an element in the form x0,y0,x1,y1,... (up to four corners)
   * @param _corners
   * @return the numer of corners to consider
   */
  protected int getCorners(double[] _corners) {
    _corners[0] = _corners[6] = 0;
    _corners[2] = _corners[4] = 1;
    _corners[1] = _corners[3] = 0;
    _corners[5] = _corners[7] = 1;
    return 4;
  }
  
  final protected void initExtrema() {
    xmin = ymin = xminLogscale = yminLogscale = Double.MAX_VALUE; 
    xmax = ymax = xmaxLogscale = ymaxLogscale = -Double.MAX_VALUE;
  }

  final protected void compareToAllExtrema(double x1, double y1) {
    compareToExtrema(x1,y1);
    compareToLogExtrema(x1,y1);
  }

  final protected void compareToExtrema(double x1, double y1) {
    xmin = Math.min(xmin, x1);
    xmax = Math.max(xmax, x1);
    ymin = Math.min(ymin, y1);
    ymax = Math.max(ymax, y1);
  }

  final protected void compareToLogExtrema(double x1, double y1) {
    if (x1>0) {
      xminLogscale = Math.min(xminLogscale, x1);
      xmaxLogscale = Math.max(xmaxLogscale, x1);
    }
    if (y1>0) {
      yminLogscale = Math.min(yminLogscale, y1);
      ymaxLogscale = Math.max(ymaxLogscale, y1);
    }
  }
  
  protected void updateExtrema() {
    if (!hasChanged()) return;
    initExtrema();
    int points = getCorners(corners);
    getTotalTransform().transform(corners,0,corners,0,points);
    for (int i=0, n=2*points; i<n; i+=2) compareToAllExtrema(corners[i],corners[i+1]);
  }

  /* Not final because Group and Trail need to overwrite it */
  public boolean isMeasured() { // required by Measurable (in Interactive)
    return canBeMeasured;
  }
  
  // -------------------------------------
  // Visibility and style
  // -------------------------------------
  
  /**
   * Sets the visibility of the element
   * @param visible boolean
   */
  final public void setVisible(boolean _visible) {
    this.visible = _visible;
  }

  /**
   * Whether the element is visible
   * @return boolean
   */
  final public boolean isVisible() {
    return this.visible;
  }

  /**
   * Returns the real visibility status of the element, which will be false if
   * it belongs to an invisible group
   * @return boolean
   */
  final protected boolean isReallyVisible() {
    Element el = this.group;
    while (el != null) {
      if (!el.visible) return false;
      el = el.group;
    }
    return this.visible;
  }

  /**
   * Gets the style of the element
   * @return Style
   * @see Style
   */
  final public Style getStyle() {
    return this.style;
  }

  /**
   * Used by Style to notify possible changes.
   * @param styleThatChanged int
   */
  protected void styleChanged(int styleThatChanged) {}

  // ----------------------------------------
  // Changes
  // ----------------------------------------

  /**
   * Returns whether the element has changed significantly.
   * This can be used by implementing classes to help improve performance.
   * Not final because Group overwrites it
   * @return boolean
   */
  public boolean hasChanged() {
    Element el = this;
    while (el!=null) {
      if (el.elementChanged) return true;
      el = el.group;
    }
    return false;
  }

  /**
   * Tells the element that it has changed.
   * Typically used by subclasses when they change something
   * @param change Whether the element has changed
   */
  final public void setElementChanged() {
    elementChanged = true;
  }

  // ----------------------------------------
  // Transformation of the element
  // ----------------------------------------
  
  /**
   * Sets the internal transformation of the element, that is, the
   * transformation that converts the standard world axes to the body's
   * internal reference axes.
   * The class of the object must be one of:
   * <ul>
   *   <li>null : sets the transformation to identity
   *   <li>org.opensourcephysics.numerics.Matrix2DTransformation</li>
   *   <li>double[6]</li>
   *   <li>double[2][2]</li>
   *   <li>double[2][3]</li>
   *   <li>AffineTransform</li>
   * </ul>
   * (i.e. a 2D transformation for 2D elements, a 3D transformation for 3d objects, ...)
   * 
   * The transformation is copied and cannot be accessed by users
   * directly. This implies that changing the original transformation
   * has no effect on the element unless a new setTransformation() is invoked.
   * The transformation uses the body's position as its origin.
   * @param transformation the new transformation
   * @throws ClassCastException if an object of a non-supported class is passed as argument
   */
  final public void setTransformation (Object _transform) throws ClassCastException {
    if (_transform==null) transformation.setToIdentity();
    else if (_transform instanceof org.opensourcephysics.numerics.Matrix2DTransformation)
      transformation.setTransform(((org.opensourcephysics.numerics.Matrix2DTransformation) _transform).getTotalTransform());
    else if (_transform instanceof double[]) {
      double[] array = (double[]) _transform;
      if (array.length>=6) transformation.setTransform(new AffineTransform(array));
    }
    else if (_transform instanceof double[][]) {
      double[][] array = (double[][]) _transform;
      if (array.length>=2) {
        if (array[0].length==2)      transformation.setTransform(new AffineTransform(array[0][0],array[1][0],array[0][1],array[1][1],0,0));
        else if (array[0].length>=3) transformation.setTransform(new AffineTransform(array[0][0],array[1][0],array[0][1],array[1][1],array[0][2],array[1][2]));
      }
      else throw new ClassCastException();
    }
    else transformation.setTransform((AffineTransform)_transform);
    elementChanged = true;
  }
  
  /**
   * Returns a copy of the element transformation
   * @return Object
   */
  final public Object getTransformation() { return new AffineTransform(transformation); }

  /**
   * Returns the actual transformation. Use with caution (do not change the transformation)
   * @return
   */
  final protected AffineTransform getTheTransformation() { return this.transformation; }
  
  /**
   * Returns the total transformation, taking into account the position, size, and transformation
   * of the element and that of its group (and supergroups), if any.
   * @return
   */
  final protected AffineTransform getTotalTransform () {
    if (elementChanged) {
      totalTransformation = AffineTransform.getTranslateInstance(this.x,this.y);
      totalTransformation.concatenate(transformation);
      totalTransformation.scale(this.sizeX,this.sizeY);
      elementChanged = false;
      setNeedToProject(true);
    }
    if (group==null) return totalTransformation;
    AffineTransform tr = new AffineTransform(group.getTotalTransform());
    tr.concatenate(totalTransformation);
    return tr;
  }

  /**
   * Returns the total transformation combined with that of its panel's
   * @param _panel
   * @return
   */
  final protected AffineTransform getPixelTransform (org.opensourcephysics.display.DrawingPanel _panel) {
    AffineTransform transform = _panel.getPixelTransform();
    transform.concatenate(getTotalTransform());
    return transform;
  }

  /**
   * This method transforms a double[] vector from the body's frame to
   * the space's frame.
   * @param vector double[] The original coordinates in the body frame
   * @return double[] The same array once transformed
   */
  final public double[] toSpaceFrame(double[] vector) {
    transformation.transform(vector,0,vector,0,1);
    vector[0] += this.x;
    vector[1] += this.y;
    Element el = group;
    while (el!=null) {
      vector[0] *= el.sizeX;
      vector[1] *= el.sizeY;
      el.transformation.transform(vector,0,vector,0,1);
      vector[0] += el.x;
      vector[1] += el.y;
      el = el.group;
    }
    return vector;
  }

  /**
   * This method converts a double[] vector from the space's frame to
   * the body's frame. </p>
   * This only works properly if the internal transformation is not set
   * (i.e. it is the identity) or if it is invertible.
   * Otherwise, a call to this method will throw an
   * UnsupportedOperationException exception.
   * @param vector double[] The original coordinates in the space
   * @return double[] The same array with the body coordinates
   */
  final public double[] toBodyFrame(double[] vector) throws java.awt.geom.NoninvertibleTransformException {
    List<Element> elList = new ArrayList<Element>();
    Element el = this;
    do {
      elList.add(el);
      el = el.group;
    } while(el!=null);
    
    for (int k = elList.size()-1;k>=0;k--) { // Done in the reverse order
      el = elList.get(k);
      vector[0] -= el.x;
      vector[1] -= el.y;
      el.transformation.inverseTransform(vector,0,vector,0,1);
      if (el!=this) {
        if (el.sizeX!=0.0) vector[0] /= el.sizeX;
        if (el.sizeY!=0.0) vector[1] /= el.sizeY;
      }
    }
    return vector;
  }

  // ----------------------------------------------------
  // Needed by the drawing mechanism
  // ----------------------------------------------------

  /**
   * Draws the element on a given Graphics2D.
   * Required by Drawable (in Interactive)
   */
  abstract public void draw (org.opensourcephysics.display.DrawingPanel _panel, java.awt.Graphics _g); 
  
  /**
   * Tells the element whether it should reproject its points because the panel
   * has changed its projection parameters. Or, the other way round, sets it to false
   * if someone (typically methods in subclasses) took care of this already.
   */
  public void setNeedToProject(boolean _need) {
    needsToProject = _need;
  }

  /**
   * Whether the element needs to project
   * @return boolean
   * @see #setNeedToProject(boolean)
   */
    final protected boolean needsToProject() {
      return needsToProject;
    }

  // -----------------------------------------
  // Implementation of InteractionSource
  // -----------------------------------------
  
  /**
   * Sets the enabled condition of all targets at once.
   * Required by Interactive 
   */
  public void setEnabled (boolean _enabled) { 
    targetPosition.setEnabled(_enabled); 
    targetSize.setEnabled(_enabled); 
  }
  
  /**
   * Whether any of the targets is enabled.
   * Required by Interactive
   */
  public boolean isEnabled () { 
    return targetPosition.isEnabled() || targetSize.isEnabled(); 
  }
  
  /**
   * Provides access to the element's targets
   */
  final public InteractionTarget getInteractionTarget(int target) {
    switch (target) {
      case TARGET_POSITION : return targetPosition;
      case TARGET_SIZE :     return targetSize;
    }
    return null;
  }

  final public void addInteractionListener(InteractionListener listener) {
    if (listener==null || listeners.contains(listener)) return;
    listeners.add(listener);
  }

  final public void removeInteractionListener(InteractionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Invokes the interactionPerformed() methods on the registered
   * interaction listeners.
   * @param event InteractionEvent
   */
  final public void invokeActions(InteractionEvent event) {
    //for (InteractionListener listener : listeners) listener.interactionPerformed(event);
    Iterator<InteractionListener> it = listeners.iterator();
    while(it.hasNext()) {
      it.next().interactionPerformed(event);
    } 
  }

  /**
   * Gets the target that is under the (x,y) position of the screen.
   * Required by Interactive
   * @param _panel org.opensourcephysics.display.DrawingPanel The drawing panel which originated the call
   * @param _xpix int
   * @param _ypix int
   * @return Interactive
   */
  abstract public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix);
  
  /**
   * Returns the body coordinates of the specified hot spot.
   * Not final because of ElementArrow
   * @return double[]
   */
  protected double[] getHotSpotBodyCoordinates(InteractionTarget target) {
    if (target==targetPosition) return new double[]{0,0};
    if (target==targetSize) return new double[]{sizeX==0 ? 0:1, sizeY==0 ? 0:1};
    return null;
  }

  /**
   * This method returns the coordinates of the given target.
   * @param target InteractionTarget
   * @return double[]
   */
  final private double[] getHotSpot(InteractionTarget target) {
    double[] coordinates = getHotSpotBodyCoordinates(target);
    if (coordinates!=null) getTotalTransform().transform(coordinates,0,coordinates,0,1);
    return coordinates;
  }

  /**
   * This method updates the position or size of the element
   * according to the position of the cursor during the interaction.
   * Notice that, for targetSize, if any of the sizes of the element
   * is zero, this dimension cannot be changed.
   * @param target InteractionTarget The target interacted
   * @param point double[] The position of the cursor during the interaction
   */
  public void updateHotSpot(InteractionTarget target, double[] point) {
    if (target.getEnabled() == InteractionTarget.ENABLED_NO_MOVE) return; // No move
    Element gr = group; //getTopGroup();
    switch (target.getType()) {
      case Element.TARGET_POSITION :
        if (gr!=null && target.getAffectsGroup()) { // Move the whole group
          double[] origin = getHotSpot(target);
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X :  gr.setX(gr.x + point[0] - origin[0]); break;
            case InteractionTarget.ENABLED_Y :  gr.setY(gr.y + point[1] - origin[1]); break;
            default :  gr.setXY(gr.x + point[0] - origin[0], gr.y + point[1] - origin[1]); break;
          }
        } 
        else { // Move only the element
          double[] coordinates = point.clone(); //new double[] {point[0], point[1], point[2]};
          groupInverseTransformations(coordinates);
          double[] origin = getHotSpotBodyCoordinates(target);
          origin[0] *= sizeX;
          origin[1] *= sizeY;
          transformation.transform(origin,0,origin,0,1);
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X : setX(coordinates[0]-origin[0]); break;
            case InteractionTarget.ENABLED_Y : setY(coordinates[1]-origin[1]); break;
            default : setXY(coordinates[0]-origin[0],coordinates[1]-origin[1]); break;
          }
        }
        break;
      case Element.TARGET_SIZE :
        if (gr!=null && target.getAffectsGroup()) { // Resize the whole group
          double[] coordinates = point.clone();
          coordinates[0] -= gr.x;
          coordinates[1] -= gr.y;
          try { gr.transformation.inverseTransform(coordinates,0,coordinates,0,1); } 
          catch (Exception exc) {};
          double[] origin = getHotSpotBodyCoordinates(target);
          elementDirectTransformations(origin);
          // If any of the dimensions is zero, a division by zero would occur.
          // Not dividing is not enough.
          if (origin[0]!=0) coordinates[0] /= origin[0];
          else coordinates[0] = gr.x;
          if (origin[1]!=0) coordinates[1] /= origin[1];
          else coordinates[1] = gr.y;
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X :  gr.setSizeX(coordinates[0]); break;
            case InteractionTarget.ENABLED_Y :  gr.setSizeY(coordinates[1]); break;
            default :  gr.setSize(coordinates); break;
          }
        } 
        else { // Resize only the element
          double[] coordinates = point.clone();
          groupInverseTransformations(coordinates);
          coordinates[0] -= this.x;
          coordinates[1] -= this.y;
          try { this.transformation.inverseTransform(coordinates,0,coordinates,0,1); } 
          catch (Exception exc) {};
          double[] origin = getHotSpotBodyCoordinates(target);
          if (origin[0]!=0) coordinates[0] /= origin[0];
          if (origin[1]!=0) coordinates[1] /= origin[1];
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X : setSizeX(coordinates[0]); break;
            case InteractionTarget.ENABLED_Y : setSizeY(coordinates[1]); break;
            default : setSize(coordinates); break;
          }
        }
        break;
    }
  }

  /**
   * All the inverse transformations of toBodyFrame except that of the
   * element itself
   * @param vector double[]
   * @throws UnsupportedOperationException
   */
  final protected void groupInverseTransformations(double[] vector) throws UnsupportedOperationException {
    List<Element> elList = new ArrayList<Element>();
    Element el = this.group;
    while (el!=null) {
      elList.add(el);
      el = el.group;
    }
    for (int k = elList.size()-1; k>=0; k--) { // Done in the reverse order
      el = elList.get(k);
      vector[0] -= el.x;
      vector[1] -= el.y;
      try { el.transformation.inverseTransform(vector,0,vector,0,1); } 
      catch (Exception exc) {};
      if (el.sizeX!=0.0) vector[0] /= el.sizeX;
      if (el.sizeY!=0.0) vector[1] /= el.sizeY;
    }
  }

  /**
   * All the direct transformations of sizeAndToSpaceFrame except that of the
   * top group
   * @param vector double[]
   */
  final protected void elementDirectTransformations(double[] vector) {
    Element el = this;
    do {
      if (el.sizeX!=0.0) vector[0] *= el.sizeX;
      if (el.sizeY!=0.0) vector[1] *= el.sizeY;
      if (el.transformation!=null) el.transformation.transform(vector,0,vector,0,1);
      vector[0] += this.x;
      vector[1] += this.y;
      el = el.group;
    } while (el!=null && el.group!=null);
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

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
