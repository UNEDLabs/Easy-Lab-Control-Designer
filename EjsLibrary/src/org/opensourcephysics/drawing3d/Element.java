/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Color;
import java.util.*;
import org.opensourcephysics.numerics.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing3d.utils.*;
import org.opensourcephysics.drawing3d.interaction.*;

/**
 *
 * <p>Title: Element</p>
 *
 * <p>Element is an abstract class for a 3D object. Each element has:</p>
 * <ul>
 *   <li> a name
 *   <li> a visibility condition (true/false)
 *   <li> a 3D position
 *   <li> a 3D size
 *   <li> a transformation which creates its own body coordinate system. Additional transformations can be added as well.
 *   <li> a drawing style, which a given implementation can honor in full or in part only
 * </ul>
 *  
 * <p>Interaction: An Element is interactive and can include two targets:</p>
 * <ul>
 *   <li> TARGET_POSITION : Allows the element to be repositioned
 *   <li> TARGET_SIZE : Allows the element to be resized
 * </ul>
 * The actual location of the target depends on the type of element.
 * An element has a list of listeners that are invoked when the element is interacted.
 *
 * @author Francisco Esquembre
 * @author Carlos Jara (CJB)
 * @version August 2009
 */
public abstract class Element implements InteractionSource {
  // Useful constants
  static final public double[] STD_ORIGIN = new double[]{0,0,0};
  static final public double[] STD_END    = new double[]{1,1,1};
  static final public double[] STD_CENTERED_ORIGIN = new double[]{-0.5,-0.5,-0.5};
  static final public double[] STD_CENTERED_END    = new double[]{0.5,0.5,0.5};

  static final public double TO_RADIANS = Math.PI/180.0;
  static final public double[] X_UNIT_VECTOR = {1.0, 0.0, 0.0}; // Standard X unit vector
  static final public double[] Y_UNIT_VECTOR = {0.0, 1.0, 0.0}; // Standard Y unit vector
  static final public double[] Z_UNIT_VECTOR = {0.0, 0.0, 1.0}; // Standard Z unit vector

  /**
   * The id for the target that allows to reposition the element.
   */
  static public final int TARGET_POSITION = 0;
  /**
   * The id for the target that allows to resize the element.
   */
  static public final int TARGET_SIZE = 1;

  // Possible changes applied to the element
  static final public int CHANGE_NONE       =   0; 
  static final public int CHANGE_VISIBILITY =   1;
  static final public int CHANGE_POSITION   =   2;
  static final public int CHANGE_SIZE       =   4;
  static final public int CHANGE_SHAPE      =   8;
  static final public int CHANGE_RESOLUTION =  16;
  static final public int CHANGE_GROUP      =  32;
  static final public int CHANGE_PROJECTION =  64;
  static final public int CHANGE_COLOR      = 128;
  static final public int CHANGE_TRANSFORMATION = 256;  
//  static final public int CHANGE_ON = 512;
  
  static final public int CHANGE_POSITION_AND_SIZE = CHANGE_POSITION | CHANGE_SIZE | CHANGE_GROUP;
  static final public int CHANGE_INTERACTION_POINTS = CHANGE_POSITION_AND_SIZE | CHANGE_PROJECTION;
  
  // Configuration variables
  private String name = "unnamed"; //$NON-NLS-1$
  private boolean visible = true;                       // is the object visible?
  private boolean canBeMeasured = true;               // Whether it is measured 
  private double x = 0.0, y = 0.0, z = 0.0;             // position of the element
  private double sizeX = 1.0, sizeY = 1.0, sizeZ = 1.0; // the size of the element
  private Transformation transformation = null;
  private Style style = new Style(this);
  private Group group = null;
  private Object dataObject=null; // a place holder for custom objects
  
  private boolean levelBelowWhenEqual = true;
  private double levelx = 0.0, levely = 0.0, levelz = 0.0, leveldx = 0.0, leveldy = 0.0, leveldz = 1.0;
  private double[] levelZ;
  private Color[] levelColors;
  
  // Implementation variables
  protected int changeType=CHANGE_NONE;
  private int implementation=-1;
  private DrawingPanel3D panel;
  private ImplementingObject implementingObject, previousImplementingObject;
  protected double[] center = new double[3]; // The center point
  protected final InteractionTarget targetPosition = new InteractionTarget(this, TARGET_POSITION);
  protected final InteractionTarget targetSize = new InteractionTarget(this, TARGET_SIZE);
  private ArrayList<InteractionListener> listeners = new ArrayList<InteractionListener>();
  private ArrayList<TransformationWrapper> additionalTransformations = new ArrayList<TransformationWrapper>();

  // ----------------------------------------
  // Implementation of the element
  // ----------------------------------------

  /**
   * Changes the implementing object to match the given implementation 
   * @param _implementation
   */
  abstract protected ImplementingObject createImplementingObject(int _implementation);
  
  /**
   * Returns the implementing object
   * @return The implementing object
   * @see ImplementingObject
   */
  public final ImplementingObject getImplementingObject() { return implementingObject; }

  public final ImplementingObject getPreviousImplementingObject() { return previousImplementingObject; }

  protected void setImplementation(int _implementation) {
    if (_implementation==this.implementation && implementingObject!=null) return;
    this.implementation = _implementation;
    previousImplementingObject = implementingObject;
    implementingObject = createImplementingObject(implementation);
  }
  
  final protected int getImplementation() { return this.implementation; }
  
  // ----------------------------------------
  // Panel and group
  // ----------------------------------------

  /**
   * To be used internally by DrawingPanel3D only! Sets the panel for this element.
   * @param _panel DrawingPanel3D
   */
  public void setPanel(DrawingPanel3D _panel) {
      if(implementingObject!=null) implementingObject.removeFromScene();
      this.panel = _panel;
      setImplementation(panel.getImplementation());
      implementingObject.addToScene();
//    changeType |= CHANGE_PANEL;
  }

  /**
   * Remove its current panel
   */
  public void removePanel() {
    if (implementingObject!=null){
      implementingObject.removeFromScene();
      this.panel = null; 
      implementingObject = null;
    } 
  }

  /**
   * Returns the DrawingPanel3D in which it (or its final ancestor group) is displayed.
   * @return DrawingPanel3D
   */
  final public DrawingPanel3D getPanel() {
    Element el = this;
    while (el.group!=null) el = el.group;
    return el.panel;
  }

  /**
   * To be used internally by Group only! Sets the group of this element.
   * @param _group Group
   */
  void setGroup(Group _group) {
    if (implementingObject!=null) implementingObject.removeFromScene();
    this.group = _group;
    if (group!=null) {
      panel=_group.getPanel(); 
      if((this instanceof Set) || (this instanceof Group)){
    	  for(Element element : ((Group)this).elementList)
    		  element.panel = _group.getPanel();
      }
      setImplementation(_group.getImplementation());
      if (implementingObject!=null) implementingObject.addToScene(); // The group may have no implementation, yet
    }
    changeType |= CHANGE_GROUP;
  }

  /**
   * Returns the group to which the element belongs
   * @return Group Returns null if it doesn't belong to a group
   */
  final public Group getGroup() { return group; }
  
  // ----------------------------------------
  // Name of the element
  // ----------------------------------------
  
  /**
   * Gives a name to the element.
   * Naming an element is optional, but the element may use its name to identify itself.
   * @param aName String
   */
  public void setName(String aName) { this.name = aName; }

  /**
   * Gets the name of the element
   * @return String the name
   */
  public String getName() { return this.name; } // Not final because of ElementTrail

  // -------------------------------------
  // Visibility
  // -------------------------------------

  /**
   * Sets the visibility of the element
   * @param _visible boolean
   */
  public void setVisible(boolean _visible) { 
    if (this.visible!=_visible) {
      this.visible = _visible; 
      changeType |= CHANGE_VISIBILITY;
    }
  }

  /**
   * Whether the element is visible
   * @return boolean
   */
  final public boolean isVisible() { return this.visible; }

  /**
   * Utility method that returns the real visibility status of the element, 
   * which will be false, for instance, if it belongs to an invisible group
   * @return boolean
   *
  final public boolean isReallyVisible() {
    Element el = this.group;
    while (el != null) {
      if (!el.visible) return false;
      el = el.group;
    }
    return this.visible;
  }
*/
  
  // ----------------------------------------
  // Position of the element
  // ----------------------------------------

  /**
   * Sets the X coordinate of the element
   * @param _x double
   */
  public void setX(double _x) {
    this.x = _x;
    changeType |= CHANGE_POSITION;
  }

  /**
   * Gets the X coordinate of the element
   * @return double
   */
  final public double getX() { return this.x; }

  /**
   * Sets the Y coordinate of the element
   * @param _y double
   */
  public void setY(double _y) {
    this.y = _y;
    changeType |= CHANGE_POSITION;
  }

  /**
   * Gets the Y coordinate of the element
   * @return double
   */
  final public double getY() { return this.y; }

  /**
   * Sets the Y coordinate of the element
   * @param _z double
   */
  public void setZ(double _z) {
    this.z = _z;
    changeType |= CHANGE_POSITION;
  }

  /**
   * Gets the Z coordinate of the element
   * @return double
   */
  final public double getZ() { return this.z; }

  /**
   * Sets the X, Y, and Z coordinates of the element
   * @param _x double
   * @param _y double
   * @param _z double
   */
  public void setXYZ(double _x, double _y, double _z) {
    this.x = _x;
    this.y = _y;
    this.z = _z;
    changeType |= CHANGE_POSITION;
  }

  /**
   * Sets the coordinates of the element from the given array
   * @param _pos double[] a one-dimensional array with at least three doubles
   */
  public void setPosition(double[] _pos) { setXYZ(_pos[0],_pos[1],_pos[2]); }

  /**
   * Get the coordinates of the element
   * @return double[] the position of the element
   */
  final public double[] getPosition() { return new double[] {this.x, this.y, this.z}; }

  /**
   * Returns the position once scaled and mapped to the axes
   * @return double[] the array with the scaled position
   */
  final public double[] getScaledPosition() { return panel.scalePosition(getPosition()); }

  // ----------------------------------------
  // Size of the element
  // ----------------------------------------

  /**
   * Sets the size along the X axis
   * @param _sizeX double
   */
  public void setSizeX(double _sizeX) {
    this.sizeX = _sizeX;
    changeType |= CHANGE_SIZE;
  }

  /**
   * Gets the size along the X axis
   * @return double
   */
  final public double getSizeX() { return this.sizeX; }

  /**
   * Sets the size along the Y axis
   * @param _sizeY double
   */
  public void setSizeY(double _sizeY) {
  	this.sizeY = _sizeY;
    changeType |= CHANGE_SIZE;
  }

  /**
   * Gets the size along the Y axis
   * @return double
   */
  final public double getSizeY() { return this.sizeY; }

  /**
   * Sets the size along the Z axis
   * @param _sizeZ double
   */
  public void setSizeZ(double _sizeZ) {
	  this.sizeZ = _sizeZ;
    changeType |= CHANGE_SIZE;
  }

  /**
   * Gets the size along the Z axis
   * @return double
   */
  final public double getSizeZ() { return this.sizeZ; }

  /**
   * Sets the size along the X, Y and Z axes
   * @param _sizeX double
   * @param _sizeY double
   * @param _sizeZ double
   */
  public void setSizeXYZ(double _sizeX, double _sizeY, double _sizeZ) {
    this.sizeX = _sizeX;
    this.sizeY = _sizeY;
    this.sizeZ = _sizeZ;
    changeType |= CHANGE_SIZE;
  }

  /**
   * Sets the size of the element.
   * @param _size double[] a one-dimensional array with at least three doubles
   */
  public void setSize(double[] _size) { setSizeXYZ(_size[0],_size[1],_size[2]); }

  /**
   * Get the size along the axes
   * @return double[] the array with the size of the element
   */
  final public double[] getSize() { return new double[] { this.sizeX, this.sizeY, this.sizeZ }; }

  /**
   * Returns the size once scaled and mapped to the axes
   * @return double[] the array with the scaled size
   */
  final public double[] getScaledSize() { return panel.scaleSize(getSize()); }

  // ----------------------------------------
  // Transformation of the element
  // ----------------------------------------

  /**
   * Sets the internal or primary transformation of the element.
   * Besides this transformation, elements can have additional or secondary transformations.
   * The combined effect of all transformations is applied so that they 
   * convert the standard XYZ axes to the body's internal reference axes.
   * The primary transformation is copied and cannot be accessed by users
   * directly. This implies that changing the original transformation
   * has no effect on the element unless a new setTransformation() is invoked.
   * The transformation uses the body's position as its origin.
   * @param _transformation the new transformation
   * @see org.opensourcephysics.numerics.Transformation
   */
  public void setTransformation(Transformation _transformation) {
    if (_transformation==null) this.transformation = null;
    else this.transformation = (Transformation) _transformation.clone();
    changeType |= CHANGE_TRANSFORMATION;
  }

  /**
   * Returns a clone of the element's primary transformation
   * @return Transformation a clone of the element's transformation
   */
  public Transformation getTransformation() {
    if (transformation==null) return null;
    return (Transformation) transformation.clone();
  }
  
  /**
   * Adds a secondary transformation to the element.
   * Secondary transformations are not copied. This means that the user can change the transformation directly.
   * When she does, she must call the element's addChange(Element.CHANGE_TRANSFORMATION) method to make sure the change is reflected.
   * @param _transform
   */
  public void addSecondaryTransformation(TransformationWrapper _transform) {
    addSecondaryTransformation(_transform,-1);
  }

  /**
   * Adds a secondary transformation to the element at a given index in the list
   * Secondary transformations are not copied. This means that the user can change the transformation directly.
   * When she does, she must call the element's addChange(Element.CHANGE_TRANSFORMATION) method to make sure the change is reflected.
   * @param _transform
   */
  public void addSecondaryTransformation(TransformationWrapper _transform, int _index) {
//    System.out.println (_transform +" added to "+this+ " at "+_index);
    if (_index < 0 || _index > additionalTransformations.size()) additionalTransformations.add(_transform);
    else additionalTransformations.add(_index, _transform);
    changeType |= CHANGE_TRANSFORMATION;
//    for (int i=0; i< additionalTransformations.size(); i++) System.out.println ("Tr "+i+" is "+additionalTransformations.get(i)); 
  }

  /**
   * Adds a list of secondary transformation to the element.
   * Secondary transformations are not copied. This means that the user can change the transformation directly.
   * When she does, she must call the element's addChange(Element.CHANGE_TRANSFORMATION) method to make sure the change is reflected.
   * @param _transformList
   */
  public void addSecondaryTransformations(List<TransformationWrapper> _transformList) {
    if (additionalTransformations.addAll(_transformList)) changeType |= CHANGE_TRANSFORMATION;
  }

  /**
   * Returns a copy of the list of element's secondary transformations. 
   * Each entry in the list is also a copy of the actual transformation.
   * @return a list with the optional transformations
   * @see TransformationWrapper
   */
  public List<TransformationWrapper> getSecondaryTransformations() {
    ArrayList<TransformationWrapper> list = new ArrayList<TransformationWrapper>();
    for (TransformationWrapper tr : additionalTransformations) {
      TransformationWrapper cloned = (TransformationWrapper) tr.clone();
      cloned.setEnabled(tr.isEnabled()); // A good TransformationWrapper should be able to clone this, but just in case...
      list.add(cloned);
    }
    return list;
  }
  
  /**
   * 
   * @param _transform
   */
  public void removeSecondaryTransformation(TransformationWrapper _transform) {
    if (additionalTransformations.remove(_transform)) changeType |= CHANGE_TRANSFORMATION;
  }
  
  /**
   * Remove all secondary transformations
   */
  public void removeAllSecondaryTransformations() {
    if (!additionalTransformations.isEmpty()) {
      additionalTransformations.clear();
      changeType |= CHANGE_TRANSFORMATION;
    }
  }
  
  /**
   * This method transforms a double[3] vector from the body's frame to the space's frame.
   * @param vector double[] The original coordinates in the body frame
   * @return double[] The same array once transformed
   */
  public double[] toSpaceFrame(double[] vector) {
    if (transformation!=null) transformation.direct(vector);
    for (TransformationWrapper trWrapper : additionalTransformations) {
      if (trWrapper.isEnabled()) trWrapper.getTransformation().direct(vector);
    }
    vector[0] += x;
    vector[1] += y;
    vector[2] += z;
    Element el = group;
    while (el!=null) {
      vector[0] *= el.sizeX;
      vector[1] *= el.sizeY;
      vector[2] *= el.sizeZ;
      if (el.transformation!=null) el.transformation.direct(vector);
      for (TransformationWrapper trWrapper : el.additionalTransformations) {
        if (trWrapper.isEnabled()) {
//          if (this.getName().equals("torqueVector")) {
//            Transformation tr = trWrapper.getTransformation();
//            if (tr instanceof Quaternion) {
//              double q[] = ((Quaternion)tr).getCoordinates();
//              System.err.println (el.getName()+" Q final = "+q[0]+","+q[1]+","+q[2]+","+q[3]);
//            }
//          }
          trWrapper.getTransformation().direct(vector);
        }
      }
      vector[0] += el.x;
      vector[1] += el.y;
      vector[2] += el.z;
      el = el.group;
    }
    return vector;
  }

  /**
   * This method converts a double[3] vector from the space's frame to
   * the body's frame. </p>
   * This only works properly if the internal transformation is not set
   * (i.e. it is the identity) and has no secondary transformations, or if al of them are invertible.
   * Otherwise, a call to this method will throw an
   * UnsupportedOperationException exception.
   * @param vector double[] The original coordinates in the space
   * @return double[] The same array with the body coordinates
   */
  public double[] toBodyFrame(double[] vector) throws UnsupportedOperationException {
    java.util.ArrayList<Element> elList = new java.util.ArrayList<Element>();
    Element el = this;
    do {
      elList.add(el);
      el = el.group;
    } while (el!=null);
    for (int i = elList.size()-1; i>=0; i--) { // Done in the reverse order
      el = elList.get(i);
      vector[0] -= el.x;
      vector[1] -= el.y;
      vector[2] -= el.z;
      for (int j = el.additionalTransformations.size()-1; j>=0; j--) el.additionalTransformations.get(j).getTransformation().inverse(vector);
      if (el.transformation!=null) el.transformation.inverse(vector);
      if (el!=this) {
        if (el.sizeX!=0.0) vector[0] /= el.sizeX;
        if (el.sizeY!=0.0) vector[1] /= el.sizeY;
        if (el.sizeZ!=0.0) vector[2] /= el.sizeZ;
      }
    }
    return vector;
  }

  /**
   * Utility method that translates a point of the standard (0,0,0) to (1,1,1) element
   * to its real spatial coordinate. Thus, if the point has a coordinate of 1,
   * the result will be the size of the element.
   * @param vector the vector to be converted
   */
  final public double[] sizeAndToSpaceFrame(double[] vector) {
    vector[0] *= sizeX;
    vector[1] *= sizeY;
    vector[2] *= sizeZ;
    return toSpaceFrame(vector);
  }

  // -------------------------------------
  // Style
  // -------------------------------------

  /**
   * Gets the style of the element. The style object can not be changed.
   * But it can be customized.
   * @return Style
   * @see Style
   */
   public Style getStyle() { return this.style; } // Not final because of MultiTrail

  /**
   * Used by Style to notify the element of possible changes in style.
   * @param _change int
   */
  public void styleChanged(int _change) {
    if (implementingObject!=null) implementingObject.styleChanged(_change); // May be null during the instantiation
  }

  // ----------------------------------------
  // Measurements
  // ----------------------------------------

  /**
   * Whether the element is taken into account when autoscaling
   * @param measured boolean
   */
  public void setCanBeMeasured(boolean measured) { canBeMeasured = measured; }

  /**
   * Whether the element is taken into account when autoscaling
   */
  public boolean getCanBeMeasured() { return canBeMeasured; }

  /**
   * Returns the diagonal size of the element, i.e., Math.sqrt(sizeX*sizeX+sizeY*sizeY+sizeZ*sizeZ)
   * @return double
   */
  public double getDiagonalSize() { return Math.sqrt(sizeX*sizeX+sizeY*sizeY+sizeZ*sizeZ); }

  /**
   * Returns the extreme points of a box that contains the element.
   * @param min double[] A previously allocated double[3] array that will hold the minimum point
   * @param max double[] A previously allocated double[3] array that will hold the maximum point
   */
  protected void getExtrema(double[] min, double[] max) {
    switch (getStyle().getRelativePosition()) {
      case Style.NORTH_EAST : 
        System.arraycopy(STD_ORIGIN, 0, min, 0, 3);
        System.arraycopy(STD_END, 0, max, 0, 3);
        break;
      default :
      case Style.CENTERED : 
        System.arraycopy(STD_CENTERED_ORIGIN, 0, min, 0, 3);
        System.arraycopy(STD_CENTERED_END, 0, max, 0, 3);
        break;
      case Style.SOUTH_WEST : 
        System.arraycopy(STD_END, 0, min, 0, 3);
        System.arraycopy(STD_ORIGIN, 0, max, 0, 3);
        break;
    }
    sizeAndToSpaceFrame(min);
    sizeAndToSpaceFrame(max);
  }

  // -------------------------------------
  // Data object method
  // -------------------------------------

  /**
   * A place holder for data objects
   * To be used internally by ControlElements
   * @param _object Object
   */
  final public void setDataObject(Object _object) { this.dataObject = _object; }

  /**
   * Returns the data object
   * @return Object
   */
  final public Object getDataObject() { return this.dataObject; }

  // ---------------------------------
  // Implementation of Interaction
  // ---------------------------------

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
   * Gives access to one of the targets of this source.
   * Sources should document the list of their available targets.
   * @param target An integer number that identifies the target in the source.
   * @return InteractionTarget
   */
  public InteractionTarget getInteractionTarget(int target) {
    switch (target) {
      case TARGET_POSITION : return targetPosition;
      case TARGET_SIZE :     return targetSize;
    }
    return null;
  }

  /**
   * Adds the specified interaction listener to receive interaction events
   * to any of its targets from this source.
   * @param listener An object that implements the InteractionListener interface
   * @see InteractionListener
   */
  public void addInteractionListener(InteractionListener listener) {
    if (listener==null || listeners.contains(listener))  return;
    listeners.add(listener);
  }

  /**
   * Removes the specified interaction listener
   * @see InteractionListener
   */
  public void removeInteractionListener(InteractionListener listener) { listeners.remove(listener); }

  /**
   * Invokes the interactionPerformed() methods on the registered
   * interaction listeners.
   * @param event InteractionEvent
   */
  final void invokeActions(InteractionEvent event) { for (InteractionListener listener : listeners) listener.interactionPerformed(event); }

  // -------------------------------------
  // Other utility methods
  // -------------------------------------

  /**
   * Tells the element a change has taken place
   * @param _change the int key of the change
   */
  final public void addChange(int _change) { changeType |= _change; }

  /**
   * Whether the element has changed in any form
   * @return true of the element has changed, false otherwise
   */
  public boolean hasChanged() {
    return changeType!=CHANGE_NONE;
  }
  
  /**
   * Returns the change flag
   * @return int the change flag
   */
  protected int getChange() { return this.changeType; }
  
  /**
   * Process all the changes (usually prior to use them)
   * Usually, visible elements will be asked to process changes 
   * @param _cumulativeChange int the cummulativeChange produced by changes in parent groups
   */
  public void processChanges(int _cumulativeChange) {
    _cumulativeChange |= changeType;
    if ((_cumulativeChange & CHANGE_VISIBILITY)==0) { // unless it includes a change of visibility
      if (!visible) {
        changeType |= _cumulativeChange; // remember the change for later
        return; // do not process changes of invisible elements 
      }
    }
    // process the change
    if (implementingObject!=null) implementingObject.processChanges(changeType, _cumulativeChange);
    if ((_cumulativeChange & CHANGE_INTERACTION_POINTS)!=0) 
    	projectInteractionPoints();
    changeType = CHANGE_NONE;
  }
  
  // ---------------------------------
  // Utility methods for interaction
  // ---------------------------------

  /**
   * Project the points required for interaction
   */
  protected void projectInteractionPoints() {
    System.arraycopy(STD_ORIGIN, 0, center, 0, 3);
    sizeAndToSpaceFrame(center);
    getPanel().projectPosition(center);
  }
  
  /**
   * Gets the target that is under the (x,y) position of the screen
   * @param x int
   * @param y int
   * @return InteractionTarget
   */
  protected InteractionTarget getTargetHit(int _xpix, int _ypix) {
//    if (!visible) return null;
    if (!targetPosition.isEnabled()) return null;
    int sensitivity = getStyle().getSensitivity();
    if (Math.abs(center[0]-_xpix)<sensitivity && Math.abs(center[1]-_ypix)<sensitivity) return this.targetPosition;
    return null;
  }

  /**
   * Returns the body coordinates of the specified hotspot
   * @return double[]
   */
  protected double[] getHotSpotBodyCoordinates(InteractionTarget target) {
    switch (getStyle().getRelativePosition()) {
      default : 
        if (target==targetPosition) return new double[]{0,0,0};
        if (target==targetSize) return new double[]{getSizeX()==0 ? 0:1, getSizeY()==0 ? 0:1, getSizeZ()==0 ? 0:1};
        break;
      case Style.CENTERED : 
        if (target==targetPosition) return new double[]{0,0,0};
        if (target==targetSize) return new double[]{getSizeX()==0 ? 0:0.5, getSizeY()==0 ? 0:0.5, getSizeZ()==0 ? 0:0.5};
        break;
    }
    return null;
  }

  /**
   * This method returns the coordinates of the given target.
   * @param target InteractionTarget
   * @return double[]
   */
  public final double[] getHotSpot(InteractionTarget target) {
    double[] coordinates = getHotSpotBodyCoordinates(target);
    if (coordinates!=null) sizeAndToSpaceFrame(coordinates);
    return coordinates;
  }

  /**
   * This method updates the position or size of the element
   * according to the position of the 3D cursor during the interaction.
   * Notice that, for targetSize, if any of the sizes of the element
   * is zero, this dimension cannot be changed.
   * @param target InteractionTarget The target interacted
   * @param point double[] The position of the cursor during the interaction
   */
  final void updateHotSpot(InteractionTarget target, double[] point) {
    if (target.getEnabled() == InteractionTarget.ENABLED_NO_MOVE) return; // No move
    Element gr = this.group; //getTopGroup();
    switch (target.getType()) {
      case TARGET_POSITION :
        if (gr!=null && target.getAffectsGroup()) { // Move the whole group
          double[] origin = getHotSpot(target);
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X  : gr.setX(gr.x + point[0] - origin[0]); break;
            case InteractionTarget.ENABLED_Y  : gr.setY(gr.y + point[1] - origin[1]); break;
            case InteractionTarget.ENABLED_Z  : gr.setZ(gr.z + point[2] - origin[2]); break;
            case InteractionTarget.ENABLED_XY : gr.setX(gr.x + point[0] - origin[0]); gr.setY(gr.y + point[1] - origin[1]); break;
            case InteractionTarget.ENABLED_XZ : gr.setX(gr.x + point[0] - origin[0]); gr.setZ(gr.z + point[2] - origin[2]); break;
            case InteractionTarget.ENABLED_YZ : gr.setY(gr.y + point[1] - origin[1]); gr.setZ(gr.z + point[2] - origin[2]); break;
            default : gr.setXYZ(gr.x+point[0]-origin[0], gr.y+point[1]-origin[1], gr.z+point[2]-origin[2]); break;
          }
        } 
        else { // Move only the element
          double[] coordinates = point.clone(); // new double[] {point[0], point[1], point[2]};
          groupInverseTransformations(coordinates);
          double[] origin = getHotSpotBodyCoordinates(target);
          origin[0] *= sizeX;
          origin[1] *= sizeY;
          origin[2] *= sizeZ;
          if (transformation!=null) transformation.direct(origin);
          for (TransformationWrapper trWrapper : additionalTransformations) {
            if (trWrapper.isEnabled()) trWrapper.getTransformation().direct(origin);
          }
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X  : setX(coordinates[0]-origin[0]); break;
            case InteractionTarget.ENABLED_Y  : setY(coordinates[1]-origin[1]); break;
            case InteractionTarget.ENABLED_Z  : setZ(coordinates[2]-origin[2]); break;
            case InteractionTarget.ENABLED_XY : setX(coordinates[0]-origin[0]); setY(coordinates[1]-origin[1]); break;
            case InteractionTarget.ENABLED_XZ : setX(coordinates[0]-origin[0]); setZ(coordinates[2]-origin[2]); break;
            case InteractionTarget.ENABLED_YZ : setY(coordinates[1]-origin[1]); setZ(coordinates[2]-origin[2]); break;
            default : setXYZ(coordinates[0]-origin[0], coordinates[1]-origin[1], coordinates[2]-origin[2]); break;
          }
        }
        break;
      case TARGET_SIZE :
        if (gr!=null && target.getAffectsGroup()) { // Resize the whole group
          double[] coordinates = point.clone(); // new double[] {point[0], point[1], point[2]};
          coordinates[0] -= gr.x;
          coordinates[1] -= gr.y;
          coordinates[2] -= gr.z;
          try { 
            for (int j = gr.additionalTransformations.size()-1; j>=0; j--) gr.additionalTransformations.get(j).getTransformation().inverse(coordinates);
            if (gr.transformation!=null) gr.transformation.inverse(coordinates); 
          }
          catch (Exception exc) {};
          double[] origin = getHotSpotBodyCoordinates(target);
          elementDirectTransformations(origin);
          // If any of the dimensions is zero, a division by zero would occur.
          // Not dividing is not enough.
          if (origin[0]!=0) coordinates[0] /= origin[0];
          else coordinates[0] = gr.sizeX;
          if (origin[1]!=0) coordinates[1] /= origin[1];
          else coordinates[1] = gr.sizeY;
          if (origin[2]!=0) coordinates[2] /= origin[2];
          else coordinates[2] = gr.sizeZ;
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X  :  gr.setSizeX(coordinates[0]); break;
            case InteractionTarget.ENABLED_Y  :  gr.setSizeY(coordinates[1]); break;
            case InteractionTarget.ENABLED_Z  :  gr.setSizeZ(coordinates[2]); break;
            case InteractionTarget.ENABLED_XY :  gr.setSizeX(coordinates[0]); gr.setSizeY(coordinates[1]); break;
            case InteractionTarget.ENABLED_XZ :  gr.setSizeX(coordinates[0]); gr.setSizeZ(coordinates[2]); break;
            case InteractionTarget.ENABLED_YZ :  gr.setSizeY(coordinates[1]); gr.setSizeZ(coordinates[2]); break;
            default :  gr.setSize(coordinates); break;
          }
        } 
        else { // Resize only the element
          double[] coordinates = point.clone(); //new double[] {point[0], point[1], point[2]};
          groupInverseTransformations(coordinates);
          coordinates[0] -= x;
          coordinates[1] -= y;
          coordinates[2] -= z;
          try { 
            for (int j = additionalTransformations.size()-1; j>=0; j--) additionalTransformations.get(j).getTransformation().inverse(coordinates);
            if (transformation!=null) transformation.inverse(coordinates); 
          }
          catch (Exception exc) {};
          double[] origin = getHotSpotBodyCoordinates(target);
          if (origin[0]!=0) coordinates[0] /= origin[0];
          if (origin[1]!=0) coordinates[1] /= origin[1];
          if (origin[2]!=0) coordinates[2] /= origin[2];
          switch (target.getEnabled()) {
            case InteractionTarget.ENABLED_X  : setSizeX(coordinates[0]); break;
            case InteractionTarget.ENABLED_Y  : setSizeY(coordinates[1]); break;
            case InteractionTarget.ENABLED_Z  : setSizeZ(coordinates[2]); break;
            case InteractionTarget.ENABLED_XY : setSizeX(coordinates[0]); setSizeY(coordinates[1]); break;
            case InteractionTarget.ENABLED_XZ : setSizeX(coordinates[0]); setSizeZ(coordinates[2]); break;
            case InteractionTarget.ENABLED_YZ : setSizeY(coordinates[1]); setSizeZ(coordinates[2]); break;
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
  private final void groupInverseTransformations(double[] vector) throws UnsupportedOperationException {
    java.util.ArrayList<Element> elList = new java.util.ArrayList<Element>();
    Element el = this.group;
    while (el!=null) {
      elList.add(el);
      el = el.group;
    }
    for(int i = elList.size()-1;i>=0;i--) { // Done in the reverse order
      el = elList.get(i);
      vector[0] -= el.x;
      vector[1] -= el.y;
      vector[2] -= el.z;
      try { 
        for (int j = el.additionalTransformations.size()-1; j>=0; j--) el.additionalTransformations.get(j).getTransformation().inverse(vector);
        if (el.transformation!=null) el.transformation.inverse(vector); 
      }
      catch (Exception exc) {};
      if (el.sizeX!=0.0) vector[0] /= el.sizeX;
      if (el.sizeY!=0.0) vector[1] /= el.sizeY;
      if (el.sizeZ!=0.0) vector[2] /= el.sizeZ;
    }
  }

  /**
   * All the direct transformations of sizeAndToSpaceFrame except that of the
   * top group
   * @param vector double[]
   */
  private final void elementDirectTransformations(double[] vector) {
    Element el = this;
    do {
      if (el.sizeX!=0) vector[0] *= el.sizeX;
      if (el.sizeY!=0) vector[1] *= el.sizeY;
      if (el.sizeZ!=0) vector[2] *= el.sizeZ;
      if (el.transformation!=null) el.transformation.direct(vector);
      for (TransformationWrapper trWrapper : el.additionalTransformations) {
        if (trWrapper.isEnabled()) trWrapper.getTransformation().direct(vector);
      }
      vector[0] += el.x;
      vector[1] += el.y;
      vector[2] += el.z;
      el = el.group;
    } while (el!=null && el.group!=null);
  }

  
  
  /**
   * Whether a value equal to one of the thresholds should be drawn using the color
   * below or above
   * @param belowWhenEqual <b>true</b> to use the color below, <b>false</b> to use teh color above
   */
  public void setColorBelowWhenEqual(boolean belowWhenEqual) {
    this.levelBelowWhenEqual = belowWhenEqual;
  }

  public boolean getColorBelowWhenEqual() { return levelBelowWhenEqual; }
  public double[] getColorLevels() { return levelZ; }
  public Color[] getColorPalette() { return levelColors; }
  
  /**
   * Sets the origin of the color change.
   * Default is (0,0,0) 
   *
   * @param origin double[]
   */
  public void setColorOrigin(double[] origin) {
    levelx = origin[0];
    levely = origin[1];
    levelz = origin[2];
  }

  public void setColorDirection(double[] direction) {
    leveldx = direction[0];
    leveldy = direction[1];
    leveldz = direction[2];
  }

  /**
   * Set the levels and color for regional color separation
   * @param thresholds an array on n doubles that separate the n+1 regions.
   * <b>null</b> for no region separation
   */
  public void setColorRegions(double thresholds[]) {
    levelZ = thresholds;
  }
  
  /**
   * Set a palette of colors for regional color separation
   * @param Color[] colors an array on n+1 colors, one for each of the regions
   */
  public void setColorPalette(Color colors[]) {
    levelColors = colors;
  }

  /**
   * Set a palette of colors for regional color separation
   * @param int[] colors an array on n+1 colors, one for each of the regions
   */
  public void setColorPalette(int colors[]) {
    levelColors = new Color[colors.length];
    for (int i=0; i<colors.length; i++) levelColors[i] = DisplayColors.getLineColor(colors[i]);
  }

  public double levelScalarProduct(double point[]) {
    return(point[0]-levelx)*leveldx+(point[1]-levely)*leveldy+(point[2]-levelz)*leveldz;
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
