/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Resolution;;

/**
 * <p>Title: ElementObject</p>
 * <p>Description: Base class for a 3D object as given by a file in any of the supported loader formats: VRML, OBJ, 3DS, ...</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementObject extends Element {

	//Configuration variables
  private String file = null;
  
  {
    getStyle().setResolution(new Resolution(3,3,3)); // For simple3d which just displays a box
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
        default :
        case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
          return new org.opensourcephysics.drawing3d.simple3d.SimpleElementObject(this);
        case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementObject(this);
    }
  }

  // -------------------------------------
  // Configuration
  // -------------------------------------

  /**
   * Sets the file with the 3D description of the object to be displayed
   * @param _filename String the filename
   */
  public void setObjectFile(String _filename) {
	if(_filename==null) return;
    if (file!=null && file.equals(_filename)) return;
    if(_filename.startsWith("\"")) _filename = _filename.substring(1, _filename.length()-1);
    this.file = _filename;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the filename of the 3D description of the object displayed
   * @return String
   */
  public String getObjectFile() { return this.file; }

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
