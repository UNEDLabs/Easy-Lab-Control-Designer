/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

public interface InteractionSource {

  /**
   * Enables all the targets of this source
   */
  public void setEnabled (boolean _enabled);

  /**
   * Whether any of the targets of this source is enabled
   */
  public boolean isEnabled ();

  /**
   * Enables a particular target of this source
   * @param target An integer that identifies a target in the source. Sources should provide a list of their
   *               available targets.
   */

  public void setEnabled (int _target, boolean _enabled);
  /**
   * Whether a particular target of this source is enabled
   * @param target An integer that identifies a target in the source. Sources should provide a list of their
   *               available targets.
   */
  public boolean isEnabled (int _target);

  /**
   * Adds the specified interaction listener to receive interaction events to any of its targets from this source
   * @param listener An object that implements the InteractionListener interface
   * @see InteractionListener
   */
  public void addListener (InteractionListener _listener);

  /**
   * Removes the specified interaction listener
   * @see InteractionListener
   */
  public void removeListener (InteractionListener _listener);

  /**
   * Removes all the interaction listeners
   */
  public void removeAllListeners ();

  /**
   * Invokes actions on all listeners with the prescribed event
   * @see InteractionEvent
   */
  public void invokeActions (InteractionEvent _event);

}

