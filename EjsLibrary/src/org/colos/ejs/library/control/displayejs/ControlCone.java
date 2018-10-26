/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

public class ControlCone extends ControlCylinder {

  protected Drawable createDrawable () {
    InteractiveCone cone = new InteractiveCone();
    cone.setOrigin(0.5,0.5,0,true);
    return cone;
  }

} // End of interface
