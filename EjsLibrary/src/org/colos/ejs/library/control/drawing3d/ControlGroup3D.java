/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import java.util.*;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.Group;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlGroup3D extends ControlElement3D implements NeedsPreUpdate, ControlParentOfElement3D {
  private Group group;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.Group"; }

  protected Element createElement () {
    return group = new Group();
  }

  protected int getPropertiesDisplacement () { return 0; }

  public boolean acceptsChild (ControlElement _child) {
    if (_child instanceof ControlElement3D) return true;
    return super.acceptsChild(_child);
  }
  
// ------------------------------------------
// Implementation of ControlElements3DParent
// ------------------------------------------

    // List of children that need to do something before repainting the panel
    private Vector<NeedsPreUpdate> preupdateList = new Vector<NeedsPreUpdate>();

    public void preupdate () { // Pass it over to children
      for (Enumeration<NeedsPreUpdate> e=preupdateList.elements(); e.hasMoreElements(); ) e.nextElement().preupdate();
    }

    public void addToPreupdateList (NeedsPreUpdate _child) { preupdateList.add(_child); }

    public void removeFromPreupdateList (NeedsPreUpdate _child) { preupdateList.remove(_child); }

    public void addElement (Element _element) { group.addElement(_element); }

    public void removeElement (Element _element) { group.removeElement(_element); }

    public DrawingPanel3D getDrawingPanel3D() { return myParent.getDrawingPanel3D(); }

} // End of class
