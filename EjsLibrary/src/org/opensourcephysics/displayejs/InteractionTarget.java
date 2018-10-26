/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Status : Untested
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.DrawingPanel;

/**
 * This is an extension (and implementation) of the Interactive interface.
 */
public interface InteractionTarget extends org.opensourcephysics.display.Interactive {
  // Needs to extend Interactive for backwards compatibility

  public InteractionSource getSource ();

  public Point3D getHotspot (DrawingPanel _panel);

  public void updateHotspot (DrawingPanel _panel, Point3D _point);

}

