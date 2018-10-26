/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;

/**
 * This class is a simple extension of ActionEvent so that
 * the event can pass any arbitrary Object as additional information.
 * It is up to the event source to decide what info to pass (and to document
 * it properly) and to the listener to process it accordingly.
 */
public class InteractionEvent extends ActionEvent {
  static public final int MOUSE_PRESSED=AWTEvent.RESERVED_ID_MAX+1;
  static public final int MOUSE_DRAGGED=AWTEvent.RESERVED_ID_MAX+2;
  static public final int MOUSE_RELEASED=AWTEvent.RESERVED_ID_MAX+3;
  static public final int MOUSE_ENTERED=AWTEvent.RESERVED_ID_MAX+4;
  static public final int MOUSE_EXITED=AWTEvent.RESERVED_ID_MAX+5;
  static public final int MOUSE_MOVED=AWTEvent.RESERVED_ID_MAX+6;

  private Object target;

  public InteractionEvent (Object _source, int _id, String _command, Object _target) {
    super(_source, _id, _command);
    this.target = _target;
  }

  public Object getTarget() { return this.target; }

}

