/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;


/**
 * An interface for children elements that need to be preupdated
 * before its parent gets updated.
 * For instance, certain drawables need to be changed before a
 * render of its parent drawing panel takes place.
 */
public interface NeedsPreUpdate {

  public void preupdate();

} // End of class

