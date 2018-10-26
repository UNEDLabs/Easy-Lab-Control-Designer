/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Component;
import org.opensourcephysics.drawing2d.DrawingPanel2D;
import org.opensourcephysics.drawing3d.utils.*;


/**
 * <p>Title: ElementImage</p>
 * <p>Description: A 2D image displayed in a 3D scene</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementPanel2D extends ElementImage {
	
  
  private Component component = null; 
  private DrawingPanel2D dp;
  static private final int IMAGE_SIZE = 1024;
  private java.awt.image.BufferedImage bimage;
  
  {
    setSizeXYZ(2, 2, 2);
    setImageFile (null);
    getStyle().setRelativePosition(Style.SOUTH_WEST);
    dp = new DrawingPanel2D();
    dp.setSize(IMAGE_SIZE,IMAGE_SIZE);
    bimage =  new java.awt.image.BufferedImage(IMAGE_SIZE,IMAGE_SIZE, java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementImage(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementImage(this,true);
    }
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  public Component getComponent(){
	  return this.component;
  }
  
  public DrawingPanel2D getDrawingPanel(){
	  return dp;
  }
  
  public void refresh(){
//    System.out.println ("Refreshing "+this);
    dp.validateImage();
    dp.render(bimage);
    super.setImage(bimage);
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
