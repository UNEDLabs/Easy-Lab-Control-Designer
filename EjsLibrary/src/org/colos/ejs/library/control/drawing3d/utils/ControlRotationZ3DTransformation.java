/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.utils.transformations.AxisRotation;
import org.opensourcephysics.drawing3d.utils.transformations.ZAxisRotation;
import org.opensourcephysics.numerics.Matrix3DTransformation;
import org.opensourcephysics.numerics.Transformation;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
public class ControlRotationZ3DTransformation extends ControlRotation3DTransformation {

  @Override
  protected Transformation createTransformation () {
    origin = new double[]{0.0,0.0,0.0};
    rotation = new ZAxisRotation();
    return rotation;
  }

//------------------------------------------------
// Implementation of Transformation
//------------------------------------------------

  public Object clone() {
    ControlRotationZ3DTransformation ct = new ControlRotationZ3DTransformation();
    ct.enabled = this.enabled;
    ct.transformation = (Matrix3DTransformation) this.transformation.clone();
    ct.myParent = null;
    ct.rotation = (AxisRotation) ct.transformation;
    ct.origin = this.origin.clone();
    return ct;
  }
  
} // End of interface
