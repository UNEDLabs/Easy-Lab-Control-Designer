/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.opensourcephysics.display.*;

/**
 * The interface that all parents of Drawables must implement
 */
public interface ControlParentOfDrawables {

  public DrawingPanel getDrawingPanel(); 
  
  public void addToPreupdateList(NeedsPreUpdate _child);

  public void removeFromPreupdateList(NeedsPreUpdate _child);

  public void addDrawable(Drawable _drawable);

  public void addDrawableAtIndex(int _index, Drawable _drawable);
  
  public void removeDrawable (Drawable _drawable);

}
