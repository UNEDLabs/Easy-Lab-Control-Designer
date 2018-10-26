/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.opensourcephysics.drawing3d.*;

/**
 * A set of Segments
 */
public class ControlSegmentSet3D extends ControlSet3D {

  protected int getPropertiesAddedToSet () { return 0; }

  protected Element createAnElement() {
    return new ElementSegment();
  }

} // End of class
