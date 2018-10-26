/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

/**
 * This is an interface for objects that have a data object attached
 */

public interface HasDataObjectInterface {
  /**
   * This method can be used by a user to store its own data in the
   * element. The data can be retrieved with the method <b>getDataObject()</b>.
   * @param data
   */
  public void setDataObject (Object data);
  /**
   * This method is used to retrieve whatever data the user has previously
   * stored in this data holder.
   * @return
   * @see setDataObject
   */
  public Object getDataObject ();

} // End of class
