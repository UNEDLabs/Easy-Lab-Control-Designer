/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;

/**
 * A container to hold Elements3D.
 */
public interface ControlParentOfElement3D {

  public DrawingPanel3D getDrawingPanel3D(); 
  
  public void addToPreupdateList(NeedsPreUpdate _child);

  public void removeFromPreupdateList(NeedsPreUpdate _child);

  public void addElement (Element _element);

  public void removeElement (Element _element);
  
}
