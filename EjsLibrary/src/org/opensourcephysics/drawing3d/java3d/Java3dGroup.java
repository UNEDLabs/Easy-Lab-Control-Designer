/*
 * Created on January 29, 2005
 * Complete copy of the JOGL version
 *  - don't know if I need blend priority in this one
 */

package org.opensourcephysics.drawing3d.java3d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.Group;

/**
 * <p>Title: Group</p>
 * <p>Description: A Group is an element that is made of other elements.</p>
 * The group's position, size, visibility and transformation do affect the
 * elements in the group. The group's style doesn't, though.
 * 
* <p>Copyright: Open Source Physics project</p>
* @author Carlos Jara Bravo
* @author Francisco Esquembre
* @version August 2009
* Based on previous work by Glenn Ford
*/

public class Java3dGroup extends Java3dElement {
	
	public Java3dGroup(Group _element) {
		super(_element);
		addNode(null);
	}
	
  @Override
  public boolean isPrimitive() { return false; }
  
  public synchronized void processChanges(int _change, int _cummulativeChange){
    super.processChanges(_change, _cummulativeChange);
    if ((_change & Element.CHANGE_VISIBILITY) != 0) {
      for (Element elementJ3D : ((Group)this.element).getElements()) {
        ((Java3dElement)elementJ3D.getImplementingObject()).makeVisible(this.element.isVisible() ? elementJ3D.isVisible() : this.element.isVisible());
      }
    }
  }

}
