/*
 * The displayejs package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : August 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import java.util.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.controls.*;

/**
 * This is the basic abstract class for the family
 */
public abstract class AbstractInteractiveElement implements InteractiveElement {
  static public int SENSIBILITY = 5;

  // Configuration variables
  protected boolean canBeMeasured=true; // Needed so that the decoration of DrawingPanel3D doesn't affect the autoscale
  protected boolean visible=true, positionEnabled = false, sizeEnabled = false, positionGroupEnabled=true, sizeGroupEnabled=false;
  protected int actualSensitivity = SENSIBILITY;
  protected double x=0.0, y=0.0, z=0.0;
  protected double sizex=0.1, sizey=0.1, sizez=0.1;
//  protected double offsetx=0.0,offsety=0.0,offsetz=0.0;
  protected final Style style=new Style (this);
  protected Resolution resolution=null;
  protected Group group=null;      // The group to which it belongs, if any
  protected ElementSet set=null;   // The set to which it belongs, if any
  protected int setIndex=-1;       // The index in the set
  protected Control control=null;  // The control used to access the element, if any
  protected Object dataObject=null;// User's data

  // Implementation variables
  protected boolean hasChanged=true; // Whether the element should recompute data that depends on position, size, scale, resolution, ...
  protected DrawingPanel panelWithValidProjection = null; // Stores the panel for which the projection is valid. Can help save time.
                                                          // It is necessary because a Drawable can be added to more than one panel
  public void copyFrom (InteractiveElement _element) {
    setX(_element.getX()); 
    setY(_element.getY());         
    setZ(_element.getZ());
    setSizeX(_element.getSizeX()); setSizeY(_element.getSizeY()); setSizeZ(_element.getSizeZ());
    setVisible(_element.isVisible());
    setEnabled(InteractiveElement.TARGET_POSITION,_element.isEnabled(InteractiveElement.TARGET_POSITION));
    setEnabled(InteractiveElement.TARGET_SIZE,    _element.isEnabled(InteractiveElement.TARGET_SIZE));
    getStyle().copyFrom(_element.getStyle());
    setResolution(_element.getResolution());
    setGroup(_element.getGroup());
    setGroupEnabled(InteractiveElement.TARGET_POSITION,_element.isGroupEnabled(InteractiveElement.TARGET_POSITION));
    setGroupEnabled(InteractiveElement.TARGET_SIZE,    _element.isGroupEnabled(InteractiveElement.TARGET_SIZE));
    setControl(_element.getControl());
  }

  public void initializeMemberOfSet() {}

  public void setSensitivity (int _s) {
    if (_s<=0) actualSensitivity = SENSIBILITY;
    else actualSensitivity = _s;
  }
  public int getSensitivity () {  return actualSensitivity; }


// ----------------------------------------------------
// Implementation of new methods in InteractiveElement
// ----------------------------------------------------

  /* Position in space of the element */
  public void setX (double _x) { x = _x; hasChanged = true; }
  public double getX () {  return x; }

  public void setY (double _y) { y = _y; hasChanged = true; }
  public double getY () {  return y; }

  public void setZ (double _z) { z = _z; hasChanged = true; }
  public double getZ () {  return z; }

  public void setXY (double _x, double _y) { x = _x; y = _y; hasChanged = true; }

  public void setXYZ (double _x, double _y, double _z) { x = _x; y = _y; z = _z; hasChanged = true; }

/*
  public void setOffsetX (double _x) { offsetx = _x; hasChanged = true; } // Not yet used
  public double getOffsetX () { return offsetx; }

  public void setOffsetY (double _y) { offsety = _y; hasChanged = true; } // Not yet used
  public double getOffsetY () { return offsety; }

  public void setOffsetZ (double _z) { offsetz = _z; hasChanged = true; } // Not yet used
  public double getOffsetZ () { return offsetz; }
*/

  /* Size of the element */
  public void setSizeX (double _sizex) { sizex = _sizex; hasChanged = true; }
  public double getSizeX () { return sizex; }

  public void setSizeY (double _sizey) { sizey = _sizey; hasChanged = true; }
  public double getSizeY () { return sizey; }

  public void setSizeZ (double _sizez) { sizez = _sizez; hasChanged = true; }
  public double getSizeZ () { return sizez; }

  public void setSizeXY (double _sizex, double _sizey) { 
    sizex = _sizex; sizey = _sizey; hasChanged = true; 
  }

  public void setSizeXYZ (double _sizex, double _sizey, double _sizez) { sizex = _sizex; sizey = _sizey; sizez = _sizez; hasChanged = true; }

  /* Visibility and interactivity */
  public void setVisible (boolean _visible) { visible = _visible; }
  public boolean isVisible () { return visible; }

  public void canBeMeasured (boolean _canBe) { this.canBeMeasured = _canBe; }

  /* Graphical appearance */
//  public void setStyle (Style _style) { style = _style; }
  public Style getStyle ()   { return style; }

  public void setResolution (Resolution _resolution) { this.resolution = _resolution; hasChanged = true; }
  public Resolution getResolution() { return resolution; }

  /* Group behaviour */
  public void setGroup (Group _group) {
    if (group!=null) group.removeElement(this);
    group = _group;
    if (group!=null) group.addElement(this);
    hasChanged = true;
  }
  public Group getGroup () { return group; }

  public void setGroupEnabled (boolean affectsGroup) { this.positionGroupEnabled = this.sizeGroupEnabled = affectsGroup; }
  public boolean isGroupEnabled () { return this.positionGroupEnabled || this.sizeGroupEnabled; }

  public void setGroupEnabled (int target, boolean affectsGroup) {
    switch (target) {
      case TARGET_POSITION : positionGroupEnabled = affectsGroup; break;
      case TARGET_SIZE     : sizeGroupEnabled     = affectsGroup; break;
    }
  }
  public boolean isGroupEnabled (int target) {
    switch (target) {
      case TARGET_POSITION : return (positionGroupEnabled);
      case TARGET_SIZE     : return (sizeGroupEnabled);
    }
    return false;
  }

  public void setSet (ElementSet _set, int _index) { set = _set; setIndex = _index; }
  public ElementSet getSet () { return set; }
  public int getSetIndex () { return setIndex; }

  public void setControl (Control _control) { this.control = _control; } // Not yet used
  public Control getControl () { return control; }

  /* Input/Output */
  public String toXML () { return this.toString(); } // Not yet used

  /* A repository for user data */
  public void setDataObject (Object _data) { this.dataObject = _data; }
  public Object getDataObject () { return this.dataObject; }

// -------------------------------------
// Implementation of Interactive
// -------------------------------------

  abstract public Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix);

// -------------------------------------
// Implementation of Drawable3D
// -------------------------------------

  public void needsToProject(DrawingPanel _panel)   {
//    if (_panel==this.panelWithValidProjection) This is very unlikely to happen, let's save one check
    panelWithValidProjection = null;
  }

  abstract public Object3D[] getObjects3D(DrawingPanel3D _panel);

  abstract public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index);

  public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2) { draw (_panel, _g2); }
//  abstract public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2);

  abstract public void draw (DrawingPanel _panel, java.awt.Graphics _g);

// -------------------------------------
// Implementation of InteractionSource
// -------------------------------------

  public void setEnabled (boolean _enabled) { positionEnabled = sizeEnabled = _enabled; }
  public boolean isEnabled () { return positionEnabled || sizeEnabled; }

  public void setEnabled (int _target, boolean _enabled) {
    switch (_target) {
      case TARGET_POSITION : positionEnabled = _enabled; break;
      case TARGET_SIZE     : sizeEnabled     = _enabled; break;
    }
  }
  public boolean isEnabled (int _target) {
    switch (_target) {
      case TARGET_POSITION : return (positionEnabled);
      case TARGET_SIZE     : return (sizeEnabled);
    }
    return false;
  }

  private java.util.List<InteractionListener> listeners = new ArrayList<InteractionListener>();

  public void addListener (InteractionListener _listener) {
    if (set!=null) System.out.println ("Warning: elements in sets should not add listeners! "+toString());
    if (_listener==null || listeners.contains(_listener)) return;
    listeners.add(_listener);
  }

  public void removeListener (InteractionListener _listener) { listeners.remove(_listener); }

  public void removeAllListeners () { listeners = new ArrayList<InteractionListener>(); }

  public void invokeActions (InteractionEvent _event) { for (InteractionListener listener : listeners) listener.interactionPerformed (_event); }

// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

  public boolean isMeasured () { return canBeMeasured && visible;  }
  
  public double getXMin () {
    if (group==null) return x - Math.abs(sizex);
    return group.x + x - Math.abs(sizex*group.sizex);
  }
  public double getXMax () {
    if (group==null) return x + Math.abs(sizex);
    return group.x + x + Math.abs(sizex*group.sizex);
  }
  public double getYMin () {
    if (group==null) return y - Math.abs(sizey);
    return group.y + y - Math.abs(sizey*group.sizey);
  }
  public double getYMax () {
    if (group==null) return y + Math.abs(sizey);
    return group.y + y + Math.abs(sizey*group.sizey);
  }
  public double getZMin () {
    if (group==null) return z - Math.abs(sizez);
    return group.z + z - Math.abs(sizez*group.sizez);
  }
  public double getZMax () {
    if (group==null) return z + Math.abs(sizez);
    return group.z + z + Math.abs(sizez*group.sizez);
  }

} // End of class
