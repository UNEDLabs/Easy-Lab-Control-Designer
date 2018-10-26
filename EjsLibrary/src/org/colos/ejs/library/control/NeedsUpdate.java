/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) Jan 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control;

/**
 * An interface to distinguish which elements need to call update()
 * when the group is updated
 */
public interface NeedsUpdate {

  public void update ();

} // End of class

