/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.simple3d;

import java.util.*;
import org.opensourcephysics.drawing3d.*;


/**
 * <p>Title: SimpleGroup</p>
 * <p>Description: A Group for the painter's algorithm</p> 
 * @author Francisco Esquembre
 * @version August 2009
 */
public class SimpleGroup extends SimpleElement {
  protected Group group;
  private List<Object3D> list3D = new ArrayList<Object3D>(); // The list of Objects3D
  private Object3D[] minimalObjects = new Object3D[1]; // The array of Objects3D

  public SimpleGroup(Group _element) { 
    super(_element);
    group = _element; 
  }

  // --------------------------------------
  // Implementation of ImplementingObject
  // --------------------------------------

  public void processChanges(int _change, int _cummulativeChange) {} // Does nothing in this implementation

  // --------------------------------------
  // Methods for the painter's algorithm
  // --------------------------------------

  public Object3D[] getObjects3D() {
    list3D.clear();
    for (Element el : group.getElements()) {
      if (!el.isVisible()) continue; 
      Object3D[] elObjects = ((SimpleElement) el.getImplementingObject()).getObjects3D();
      if (elObjects!=null) for(int i = 0, n = elObjects.length;i<n;i++) list3D.add(elObjects[i]);
    }
    if (list3D.isEmpty()) return null;
    return list3D.toArray(minimalObjects);
  }

  public void draw(java.awt.Graphics2D _g2, int _index) {
    System.err.println("Group draw (i): I should not be called!"); //$NON-NLS-1$
  }

  public void drawQuickly(java.awt.Graphics2D _g2) {
    for (Element el : group.getElements()) if (el.isVisible()) ((SimpleElement) el.getImplementingObject()).drawQuickly(_g2);
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
