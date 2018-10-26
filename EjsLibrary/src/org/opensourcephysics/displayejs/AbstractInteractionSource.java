/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.util.*;

/**
 * A base abstract class for objects that need to implement InteractionSource
 */
public abstract class AbstractInteractionSource implements InteractionSource {

  private List<InteractionListener> listeners = new ArrayList<InteractionListener>();

  public void addListener (InteractionListener _listener) {
    if (_listener==null || listeners.contains(_listener)) return;
    listeners.add(_listener);
  }

  public void removeListener (InteractionListener _listener) { listeners.remove(_listener); }

  public void removeAllListeners () { listeners = new ArrayList<InteractionListener>(); }

  public void invokeActions (InteractionEvent _event) { for (InteractionListener listener : listeners) listener.interactionPerformed (_event); }

}

