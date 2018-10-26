/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;
import java.util.*;

public class GroupDrawable extends Group implements Interactive, Drawable3D, Measurable3D, InteractionSource {

  // Configuration variables

  // Implementation variables
  private   ArrayList<Object3D> list3D = new ArrayList<Object3D>();  // The list of Objects3D
  private Object3D[] minimalObjects = new Object3D[1];  // The array of Objects3D

// -------------------------------------------
// Adding and removing elements to the group
// -------------------------------------------

  /**
   * Adds an element to the group
   * @param _element the InteractiveElement to be added
   */
  public void add (InteractiveElement element) {
    element.setGroup(this);
  }

  /**
   * removes an element from the group
   * @param _element the InteractiveElement to be removed
   */
  public void remove (InteractiveElement element) {
    element.setGroup(null);
  }

  /*
   * removes all elements in the group
   */
    public void removeAll () {
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) it.next().setGroup(null);
  }

// -----------------------------------------
//  Interaction
// -----------------------------------------

  public void setEnabled (boolean _enabled) {
    for (Iterator<InteractiveElement> it = list.iterator(); it.hasNext(); ) it.next().setEnabled(_enabled);
  }
  public boolean isEnabled () {
    for (Iterator<InteractiveElement> it = list.iterator(); it.hasNext(); ) if (it.next().isEnabled()) return true;
    return false;
  }

  public void setEnabled (int _target, boolean _enabled) {
    for (Iterator<InteractiveElement> it = list.iterator(); it.hasNext(); ) it.next().setEnabled(_target, _enabled);
  }
  public boolean isEnabled (int _target) {
    for (Iterator<InteractiveElement> it = list.iterator(); it.hasNext(); ) if (it.next().isEnabled(_target)) return true;
    return false;
  }

  public Interactive findInteractive(DrawingPanel _panel, int _xpix, int _ypix){
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) {
      InteractiveElement element = it.next();
      Interactive iad = element.findInteractive (_panel,_xpix,_ypix);
      if (iad instanceof InteractionTargetElementPosition) return new InteractionTargetGroupDrawableElement(this, element, (InteractionTarget) iad);
      if (iad!=null) return iad;  // Allow group's elements to report its own targets
    }
    return null;
  }


// -----------------------------------------
//  Implementation of Drawable3D
// -----------------------------------------

  public void needsToProject(DrawingPanel _panel) {
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) it.next().needsToProject(_panel);
  }

  /* Drawable3D */
  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    list3D.clear();
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) {
      Object3D[] objects = it.next().getObjects3D(_panel);
      if (objects!=null) for (int i=0, n=objects.length; i<n;  i++) list3D.add(objects[i]);
    }
    if (list3D.size()==0) return null;
    return list3D.toArray(minimalObjects);
  }

  public void draw (DrawingPanel3D _panel, java.awt.Graphics2D _g2, int _index) {
    System.out.println ("Group draw (i): I should not be called!");
  }

  public void drawQuickly (DrawingPanel3D _panel, java.awt.Graphics2D _g2) {
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) it.next().drawQuickly(_panel, _g2);
  }

  public void draw (DrawingPanel _panel, java.awt.Graphics _g) {
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) it.next().draw(_panel, _g);
  }

// -----------------------------------------
//  Implementation of Measurable3D
// -----------------------------------------

  public boolean isMeasured () { return !list.isEmpty(); }
  public double getXMin () {
    double min = Double.MAX_VALUE;
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) min = Math.min(min,it.next().getXMin());
    return min;
  }
  public double getXMax () {
    double max = -Double.MAX_VALUE;
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) max = Math.max(max,it.next().getXMax());
    return max;
  }
  public double getYMin () {
    double min = Double.MAX_VALUE;
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) min = Math.min(min,it.next().getYMin());
    return min;
  }
  public double getYMax () {
    double max = -Double.MAX_VALUE;
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) max = Math.max(max,it.next().getYMax());
    return max;
  }
  public double getZMin () {
    double min = Double.MAX_VALUE;
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) min = Math.min(min,it.next().getZMin());
    return min;
  }
  public double getZMax () {
    double max = -Double.MAX_VALUE;
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) max = Math.max(max,it.next().getZMax());
    return max;
  }

// -------------------------------------------
// InteractionSource
// -------------------------------------------

  private List<InteractionListener> listeners = new ArrayList<InteractionListener>();

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

}

