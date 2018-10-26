/*
 * The displayejs package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : August 2003
 */

package org.opensourcephysics.displayejs;

import java.util.*;

public class Group {

  // Configuration variables
  protected double x=0.0, y=0.0, z=0.0;
  protected double sizex=1.0, sizey=1.0, sizez=1.0;

  // Implementation variables
  protected List<InteractiveElement> list = new ArrayList<InteractiveElement>();  // The list of elements of this group

// -------------------------------------------
// Adding and removing elements to the group
//    ---      Package private        ---
//    --- NOT TO BE USED BY END-USERS ---
// -------------------------------------------

  /**
   * Adds an element to the group. An element can only be added once.
   * @param _element the InteractiveElement to be added
   */
  void addElement (InteractiveElement element) {
    if (!list.contains(element)) list.add(element);
  }

  /**
   * removes an element from the set
   * @param _element the InteractiveElement to be removed
   */
  boolean removeElement (InteractiveElement element) {
    return list.remove(element);
  }

// -------------------------------------------
// Moving and resizing the group
// -------------------------------------------

  /* Position in space of the group */
  /**
   * Moves the whole group to a new X location
   * @param x the new base X location for the group
   */
  public void setX (double x) { this.x = x; reportChange(); }
  /**
   * Returns the X location of the group
   * @return the base X location for the group
   */
  public double getX () {  return this.x; }

  /**
   * Moves the whole group to a new Y location
   * @param y the new base Y location for the group
   */
  public void setY (double y) { this.y = y; reportChange(); }
  /**
   * Returns the Y location of the group
   * @return the base Y location for the group
   */
  public double getY () {  return this.y; }

  /**
   * Moves the whole group to a new Z location
   * @param z the new base Z location for the group
   */
  public void setZ (double z) { this.z = z; reportChange(); }
  /**
   * Returns the Z location of the group
   * @return the base Z location for the group
   */
  public double getZ () {  return this.z; }

  /**
   * Moves the whole group to a new X and Y location
   * @param x the new base X location for the group
   * @param y the new base Y location for the group
   */
  public void setXY (double x, double y) { this.x = x; this.y = y; reportChange(); }

  /**
   * Moves the whole group to a new X, Y, and Z location
   * @param x the new base X location for the group
   * @param y the new base Y location for the group
   * @param z the new base Z location for the group
   */
  public void setXYZ (double x, double y, double z) { this.x = x; this.y = y; this.z = z; reportChange(); }

  /* Size of the element */
  /**
   * Resizes the whole group in the X dimension
   * @param sizex the new base X size for the group
   */
  public void setSizeX (double sizex) { this.sizex = sizex; reportChange(); }
  /**
   * Returns the base size of the group in the X dimension
   * @return the base X size of the group
   */
  public double getSizeX () { return this.sizex; }

  /**
   * Resizes the whole group in the Y dimension
   * @param sizey the new base Y size for the group
   */
  public void setSizeY (double sizey) { this.sizey = sizey; reportChange(); }
  /**
   * Returns the base size of the group in the Y dimension
   * @return the base Y size of the group
   */
  public double getSizeY () { return this.sizey; }

  /**
   * Resizes the whole group in the Z dimension
   * @param sizez the new base Z size for the group
   */
  public void setSizeZ (double sizez) { this.sizez = sizez; reportChange(); }
  /**
   * Returns the base size of the group in the Z dimension
   * @return the base Z size of the group
   */
  public double getSizeZ () { return this.sizez; }

  /**
   * Resizes the whole group in the X and Y dimensions
   * @param sizex the new base X size for the group
   * @param sizey the new base Y size for the group
   */
  public void setSizeXY (double sizex, double sizey) { this.sizex = sizex; this.sizey = sizey; reportChange(); }

  /**
   * Resizes the whole group in the X, Y, and Z dimensions
   * @param sizex the new base X size for the group
   * @param sizey the new base Y size for the group
   * @param sizez the new base Z size for the group
   */
  public void setSizeXYZ (double sizex, double sizey, double sizez) { this.sizex = sizex; this.sizey = sizey; this.sizez = sizez; reportChange(); }

  /* Visibility */
  /**
   * Sets the visibility of the whole group at once
   * @param visible the desired visibility for the group
   */
  public void setVisible (boolean visible) {
    for (Iterator<InteractiveElement> it = list.iterator(); it.hasNext(); ) it.next().setVisible(visible);
  }
  /**
   * Whether <i>any</i> element of the group is visible
   * @return <b>true</b> if any of the elements is visible, <b>false</b> otherwise
   */
  public boolean isVisible () {
    for (Iterator<InteractiveElement> it = list.iterator(); it.hasNext(); ) if (it.next().isVisible()) return true;
    return false;
  }

// -----------------------------------------
//  Private methods
// -----------------------------------------

  public void reportChange() {
    for (Iterator<InteractiveElement>  it = list.iterator(); it.hasNext(); ) it.next().needsToProject(null);
  }

}

