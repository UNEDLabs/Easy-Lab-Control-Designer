/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.awt.event.MouseEvent;
import org.opensourcephysics.drawing2d.interaction.InteractionTarget;
import org.opensourcephysics.drawing2d.interaction.InteractionEvent;

/**
 * Used to delay the update of the target when the simulation is running
 * @author Francisco Esquembre
 *
 */
class UpdateHotSpot2DDelayedAction implements org.colos.ejs.library.DelayedAction {
  private InteractionTarget target2D;
  private double[] point;
  private MouseEvent event;

  UpdateHotSpot2DDelayedAction(InteractionTarget _target2D, double[] _point, MouseEvent _evt) {
    target2D = _target2D;
    point  = _point;
    event = _evt;
  }
  
  public void performAction() { 
    target2D.getElement().updateHotSpot(target2D, point); 
    org.opensourcephysics.drawing2d.Element element = target2D.getElement();
    element.invokeActions(new InteractionEvent(element, InteractionEvent.MOUSE_DRAGGED, target2D.getActionCommand(),target2D, event));
  }
  
}

