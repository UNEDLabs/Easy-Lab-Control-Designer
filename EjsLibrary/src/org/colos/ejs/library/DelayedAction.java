/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

/**
 * An interface for actions that need to be delayed if the simulation is playing
 */

public interface DelayedAction  {

  public void performAction ();

} // End of class


