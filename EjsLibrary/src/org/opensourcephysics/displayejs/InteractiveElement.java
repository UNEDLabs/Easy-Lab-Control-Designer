/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;
import org.opensourcephysics.controls.Control;

/**
 * This is the interface for elements that can be properly added to DrawingPanel3D
 */
/**
 * <p><code>InteractiveElement</code> is an interface that extends Drawable3D and Measurable3D, but also Interactive and InteractionSource.
 * This means already that an interactive element is ready to be drawn in three dimensions, responds to user interaction, and is able to
 * send interactive events to registered listeners.</p>
 * <p>This interface also defines methods designed to help you work with elements in a way similar to that you work with physical objects
 * in real life. Using implementing classes, you will find yourself creating and handling (moving, resizing,...) objects in a way similar
 * to what you would do in real life.</p>
 * <p>This interface provides an API for three-dimensional (interactive, drawable) objects that:
 * <ul>
 *   <li> have a position, and size in space,</li>
 *   <li> accept <code>Style</code> and <code>Resolution</code> suggestions for their representation on the screen,</li>
 *   <li> can change their visibility status,</li>
 *   <li> act as interaction sources and can have one or more interaction targets,</li>
 *   <li> can be grouped with other elements,</li>
 *   <li> can be easily created in large numbers and moved and resized using arrays,</li>
 *   <li> can have their own control.</li>
 * </ul>
 * </p>
 * @author Francisco Esquembre (http://fem.um.es)
 * @version 1.0, August 2003
 */
public interface InteractiveElement extends Drawable3D, InteractionSource, Interactive, Measurable3D, HasDataObjectInterface {
  // It needs to implement Interactive for backwards compatibility (so that it will be asked by the panel for its targets)

  public static final int TARGET_POSITION  = 0;
  public static final int TARGET_SIZE      = 1;

  /* Position in space of the element */
  /**
   * Moves the element to a new X location
   * @param x the new X location for the element
   */
  public void setX (double x);
  /**
   * Returns the X location of the element
   * @return the X location for the element
   */
  public double getX ();

  /**
   * Moves the element to a new Y location
   * @param y the new Y location for the element
   */
  public void setY (double y);
  /**
   * Returns the Y location of the element
   * @return the Y location for the element
   */
  public double getY ();

  /**
   * Moves the element to a new Z location
   * @param z the new Z location for the element
   */
  public void setZ (double z);
  /**
   * Returns the Z location of the element
   * @return the Z location for the element
   */
  public double getZ ();

  /**
   * Moves the element to a new X and Y location
   * @param x the new X location for the element
   * @param y the new Y location for the element
   */
  public void setXY (double x, double y);

  /**
   * Moves the element to a new X, Y, and Z location
   * @param x the new X location for the element
   * @param y the new Y location for the element
   * @param z the new Z location for the element
   */
  public void setXYZ (double x, double y, double z);

  /* Size of the element */
  /**
   * Resizes the element in the X dimension
   * @param sizex the new X size for the element
   */
  public void setSizeX (double x);
  /**
   * Returns the size of the element in the X dimension
   * @return the X size of the element
   */
  public double getSizeX ();

  /**
   * Resizes the element in the Y dimension
   * @param sizey the new Y size for the element
   */
  public void setSizeY (double y);
  /**
   * Returns the size of the element in the Y dimension
   * @return the Y size of the element
   */
  public double getSizeY ();

  /**
   * Resizes the element in the Z dimension
   * @param sizez the new Z size for the element
   */
  public void setSizeZ (double z);
  /**
   * Returns the size of the element in the Z dimension
   * @return the Z size of the element
   */
  public double getSizeZ ();

  /**
   * Resizes the element in the X and Y dimensions
   * @param sizex the new X size for the element
   * @param sizey the new Y size for the element
   */
  public void setSizeXY (double x, double y);

  /**
   * Resizes the element in the X, Y, and Z dimensions
   * @param sizex the new X size for the element
   * @param sizey the new Y size for the element
   * @param sizez the new Z size for the element
   */
  public void setSizeXYZ (double x, double y, double z);

  /* Graphical appearance */
  /**
   * Sets the visibility of the element
   * @param visible the desired visibility for the element
   */
  public void setVisible (boolean visible);
  /**
   * Whether the element is visible or not
   * @return the visibility status
   */
  public boolean isVisible ();

  /**
   * Whether the element should be taken into account for computing the scales.
   * This is used by DrawingPanel3D's decoration.
   */
  public void canBeMeasured (boolean _canBe);

  /**
   * The set of style suggestions for this element. The style instance itself can't be changed, but its fields are mutable.
   * @return the instance of the Style class for this element.
   * @see Style
   */
  public Style getStyle();
//  public void setStyle (Style style); // Cannot be changed because of the performance changes that introduced Drawable3D.needsToProject();

  /**
   * Resolution suggestions for the element.
   * @param the new resolution for the element.
   * @see Resolution
   */
  public void setResolution (Resolution resolution);
  /**
   * Get the resolution suggestions for this element.
   * @return the current resolution of the element.
   * @see Resolution
   */
  public Resolution getResolution();

  /**
   * Sets the size of the interaction hot spot (in pixels).
   * If zero or negative, some elements (such as Images and Particles) may use the full area.
   * Others will use a default of 5.
   * @param sensitiviy int
   */
  public void setSensitivity (int sensitiviy);
  public int getSensitivity ();

  /* element behaviour */
  /**
   * Sets whether this element should move and size as part of a group.
   * @param _element the group to which the element belongs. <b>null</b> if it doesn't belong to any group.
   * @see Group
   */
  public void setGroup (Group group);
  /**
   * Gets the group to which the element belongs.
   * @return the group to which the element belongs. <b>null</b> if it doesn't belong to any group.
   * @see Group
   */
  public Group getGroup ();

  /**
   * Whether interaction on any of the targets of the element should affect the whole group (if the element is in a group).
   * By default, only TARGET_POSITION targets are group-enabled.
   * @param affectsGroup <b>true</b> if interactions should affect the whole group,
   * <b>false</b> if they should affect only the element within the group.
   */
  public void setGroupEnabled (boolean affectsGroup);
  /**
   * Whether interaction on any of the targets of this element affects the whole group
   * @return <b>true</b> (the default) if the interaction affects the whole group,
   * <b>false</b> if the motion affects only the element within the group.
   */
  public boolean isGroupEnabled ();
  /**
   * Whether interaction on a given target of the element should affect the whole group (if the element is in a group)
   * By default, only TARGET_POSITION targets are group-enabled.
   * @param target An integer that identifies a target of the element.
   * @param affectsGroup <b>true</b> (the default) if interactions on this target should affect the whole group,
   * <b>false</b> if they should affect only the element within the group.
   */
  public void setGroupEnabled (int target, boolean affectsGroup);
  /**
   * Whether interaction on a given target of this element affects the whole group
   * @param target An integer that identifies a target of the element.
   * @return <b>true</b> (the default) if the interaction affects the whole group,
   * <b>false</b> if the motion affects only the element within the group.
   */
  public boolean isGroupEnabled (int target);

  /* Set behaviour */
  public void setSet (ElementSet set, int index);
  public ElementSet getSet ();
  public int getSetIndex ();
  /**
   * Special initialization when used as a memeber of a Set
   */
  public void initializeMemberOfSet();

  /* Control behaviour */
  public void setControl (Control control);
  public Control getControl ();

  /* Input/Output */
  /**
   * Get an XML description of this element.
   * @return the XML formatted description of the element
   */
  public String toXML ();

  public void copyFrom (InteractiveElement _element);

} // End of class
