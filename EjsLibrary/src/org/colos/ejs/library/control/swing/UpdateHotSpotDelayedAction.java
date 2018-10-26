/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.displayejs.InteractionEvent;
import org.opensourcephysics.displayejs.InteractionTarget;
import org.opensourcephysics.displayejs.Point3D;


/**
 * Used to delay the update of the target when the simulation is running
 * @author Francisco Esquembre
 *
 */
class UpdateHotSpotDelayedAction implements org.colos.ejs.library.DelayedAction {
  private InteractionTarget target;
  private InteractivePanel panel;
  private Point3D point;

  UpdateHotSpotDelayedAction(InteractionTarget _target, InteractivePanel _panel, Point3D _point) {
    target = _target;
    panel = _panel;
    point  = new Point3D(_point.x,_point.y,_point.z);
  }
  
  public void performAction() { 
    target.updateHotspot(panel, point); 
    target.getSource().invokeActions (new InteractionEvent (target.getSource(),InteractionEvent.MOUSE_DRAGGED,null,target));
  }
  
}

