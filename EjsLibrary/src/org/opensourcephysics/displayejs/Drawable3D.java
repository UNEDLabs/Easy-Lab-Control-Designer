/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

/**
 * This interface defines methods that DrawingPanel3D needs to properly display
 * the drawable
 */
public interface Drawable3D extends org.opensourcephysics.display.Drawable {

  /**
   * Returns an array of Objects3D to (sort according to its distance and)
   * draw.
   */
  public Object3D[] getObjects3D (DrawingPanel3D _panel);

  /**
   * Draws a given Object3D (indicated by its index).
   */
  public void draw (DrawingPanel3D panel, java.awt.Graphics2D g, int index);

  /**
   * Sketches the drawable
   */
  public void drawQuickly (DrawingPanel3D panel, java.awt.Graphics2D g);

  /**
   * Tells the drawable that it should reproject its points because this panel
   * has changed its projection parameters.
   * @param _panel the DrawingPanel that has changed or null if the drawable should update
   * its projection in all possible panels
   */
   public void needsToProject(org.opensourcephysics.display.DrawingPanel _panel);

}

