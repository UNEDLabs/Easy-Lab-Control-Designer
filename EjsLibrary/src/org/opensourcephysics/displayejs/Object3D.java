/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

public class Object3D {

  /**
   * Constructor for this Object3D
   * @param _drawable The drawable this Object3D is part of
   * @param _index    An integer that identifies the object within the drawable
   */
  public Object3D (Drawable3D _drawable, int _index) {
    this.drawable3D = _drawable;
    this.index = _index;
    this.distance = Double.NaN;
  }

  /**
   * The Drawable3D in which it is defined (and which knows how
   * to draw it).
   */
  public Drawable3D drawable3D=null;

  /**
   * The reference number within its parent Drawable3D
   */
  public int index=-1;

  /**
   * This number provides a criterion to determine the order in
   * which to draw all Drawables3D in a EjsDrawingPanel3D.
   * If this Double.isNaN(distance) the Object3D will be ignored
   * by the panel. This can be used by Drawables3D to hide a given
   * Object3D.
   * Thi snumber is also used to modify the color of an object, so that
   * objects far away look darker and objects closer look brighter.
   */
  public double distance=Double.NaN;
}

