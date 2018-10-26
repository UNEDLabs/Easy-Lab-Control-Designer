package org.opensourcephysics.drawing3d.utils;

import org.opensourcephysics.numerics.Transformation;

/**
 * A TransformationWrapper is a wrapper for a transformation that can be enabled and disabled in run time.
 * This way, a transformation can be added to an element, but can be turned on and off at will
 * @author Paco
 *
 */
public interface TransformationWrapper extends Cloneable {

  /**
   * Provides a copy of this optional transformation.
   * This is used by an OSP 3D Element that will explicitely get a clone of it.
   */
  public Object clone();

  /**
   * Enables/Disables the transformation
   * @param _enable
   */
  public void setEnabled(boolean _enable);
  
  /**
   * Whether the transformation is enabled
   * @return boolean
   */
  public boolean isEnabled();
  
  /**
   * An optional transformation may actually be the wrap of a Transformation object.
   * Some users may ask for this wrapped transformation
   * @return the wrapped transformation or 'this' if it is not a wrap
   */
  public Transformation getTransformation();
  
}
